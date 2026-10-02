package com.example.service

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirestoreUserProfileRepository
import com.example.data.repository.UserSessionManager
import com.example.model.User
import com.example.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AuthService(
    private val authRepository: AuthRepository = AuthRepository.instance,
    private val firestoreUserProfileRepository: FirestoreUserProfileRepository = FirestoreUserProfileRepository.instance
) {

    companion object {
        private const val TAG = "AuthService"
        val instance: AuthService by lazy { AuthService() }

        fun determineRoleFromEmail(email: String): UserRole {
            val lower = email.lowercase()
            return when {
                lower.contains("admin") -> UserRole.PLATFORM_ADMIN
                lower.contains("faculty") || lower.contains("prof") || lower.contains("dean") || lower.contains("hod") -> UserRole.FACULTY
                lower.contains("president") || lower.contains("pres.") -> UserRole.CLUB_PRESIDENT
                lower.contains("clubadmin") || lower.contains("club.admin") -> UserRole.CLUB_ADMIN
                lower.contains("club") -> UserRole.CLUB_MEMBER
                lower.contains("alumni") || lower.contains("community") -> UserRole.COMMUNITY_MEMBER
                else -> UserRole.STUDENT
            }
        }
    }

    val isFirebaseAvailable: Boolean
        get() = authRepository.isFirebaseAvailable

    val currentFirebaseUser: FirebaseUser?
        get() = authRepository.currentUser

    init {
        authRepository.addAuthStateListener { fbAuth ->
            val fbUser = fbAuth.currentUser
            if (fbUser != null) {
                val current = UserSessionManager.currentUser.value
                val email = fbUser.email ?: ""
                val defaultRole = determineRoleFromEmail(email)
                val resolvedUser = User(
                    userId = fbUser.uid,
                    name = fbUser.displayName ?: email.substringBefore("@").ifBlank { "Campus User" },
                    email = email.ifBlank { "anonymous@college.edu" },
                    role = if (current.userId == fbUser.uid) current.role else defaultRole,
                    department = current.department.ifBlank { "General Campus" },
                    clubAffiliation = current.clubAffiliation,
                    isAnonymous = fbUser.isAnonymous,
                    photoUrl = fbUser.photoUrl?.toString(),
                    authProvider = if (fbUser.isAnonymous) "Anonymous Guest" else "Firebase Auth"
                )
                UserSessionManager.switchUser(resolvedUser)
            }
        }
    }

    /**
     * Sign in via Google using AndroidX Credential Manager and Firebase Auth via AuthRepository.
     */
    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String? = null
    ): Result<User> {
        return try {
            val credentialManager = CredentialManager.create(context)
            // Use provided clientId or fallback to college oauth web client id
            val effectiveClientId = serverClientId?.ifBlank { null }
                ?: "college-events-platform.apps.googleusercontent.com"

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(effectiveClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(request = request, context = context)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                val profilePic = googleIdTokenCredential.profilePictureUri?.toString()

                if (authRepository.isFirebaseAvailable) {
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = authRepository.signInWithCredential(authCredential).getOrThrow()
                    val fbUser = authResult.user

                    // Check if user profile already exists in Firestore
                    val existingProfile = fbUser?.uid?.let { uid ->
                        firestoreUserProfileRepository.getUserProfile(uid).getOrNull()
                    }

                    val user = if (existingProfile != null) {
                        existingProfile.copy(
                            photoUrl = fbUser.photoUrl?.toString() ?: profilePic ?: existingProfile.photoUrl
                        )
                    } else {
                        val role = determineRoleFromEmail(email)
                        val newUser = User(
                            userId = fbUser?.uid ?: UUID.randomUUID().toString(),
                            name = fbUser?.displayName ?: displayName,
                            email = fbUser?.email ?: email,
                            role = role,
                            department = "Computer Science & Engineering",
                            photoUrl = fbUser?.photoUrl?.toString() ?: profilePic,
                            authProvider = "Google Sign-In"
                        )
                        // Store in Firestore
                        firestoreUserProfileRepository.saveUserProfile(newUser)
                        newUser
                    }
                    UserSessionManager.switchUser(user)
                    Result.success(user)
                } else {
                    // Fallback local auth mode if Firebase backend is offline
                    val role = determineRoleFromEmail(email)
                    val user = User(
                        userId = "google_${UUID.randomUUID().toString().take(8)}",
                        name = displayName,
                        email = email,
                        role = role,
                        department = "Computer Science & Engineering",
                        photoUrl = profilePic,
                        authProvider = "Google Sign-In"
                    )
                    firestoreUserProfileRepository.saveUserProfile(user)
                    UserSessionManager.switchUser(user)
                    Result.success(user)
                }
            } else {
                Result.failure(IllegalArgumentException("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sign in with Email and Password using AuthRepository.
     * Retrieves stored user profile and role from Firestore.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            if (authRepository.isFirebaseAvailable) {
                val result = authRepository.signInWithEmailAndPassword(email.trim(), password).getOrThrow()
                val fbUser = result.user ?: throw IllegalStateException("Firebase user was null")

                // Retrieve stored profile from Firestore
                val existingProfile = firestoreUserProfileRepository.getUserProfile(fbUser.uid).getOrNull()
                    ?: firestoreUserProfileRepository.getUserProfileByEmail(email).getOrNull()

                val user = if (existingProfile != null) {
                    existingProfile.copy(
                        userId = fbUser.uid,
                        email = fbUser.email ?: existingProfile.email
                    )
                } else {
                    val role = determineRoleFromEmail(email)
                    val newUser = User(
                        userId = fbUser.uid,
                        name = fbUser.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = fbUser.email ?: email,
                        role = role,
                        department = "Campus Student Affairs",
                        authProvider = "Firebase Email"
                    )
                    // Persist to Firestore
                    firestoreUserProfileRepository.saveUserProfile(newUser)
                    newUser
                }
                UserSessionManager.switchUser(user)
                Result.success(user)
            } else {
                // Local dev auth mode
                val existing = firestoreUserProfileRepository.getUserProfileByEmail(email).getOrNull()
                val role = existing?.role ?: determineRoleFromEmail(email)
                val user = existing ?: User(
                    userId = "usr_${UUID.randomUUID().toString().take(8)}",
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = email.trim(),
                    role = role,
                    department = "Campus Student Affairs",
                    authProvider = "Email Authentication"
                )
                firestoreUserProfileRepository.saveUserProfile(user)
                UserSessionManager.switchUser(user)
                Result.success(user)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Register a new user account with specified role and department using AuthRepository
     * and store their profile in Firestore.
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        name: String,
        role: UserRole,
        department: String,
        clubAffiliation: String? = null
    ): Result<User> {
        return try {
            if (authRepository.isFirebaseAvailable) {
                val result = authRepository.createUserWithEmailAndPassword(email.trim(), password).getOrThrow()
                val fbUser = result.user ?: throw IllegalStateException("User creation returned null")

                // Update profile display name via repository
                authRepository.updateProfile(user = fbUser, displayName = name)

                val user = User(
                    userId = fbUser.uid,
                    name = name,
                    email = fbUser.email ?: email,
                    role = role,
                    department = department,
                    clubAffiliation = clubAffiliation,
                    authProvider = "Firebase Email"
                )
                // Persist full profile to Firestore
                firestoreUserProfileRepository.saveUserProfile(user)
                UserSessionManager.switchUser(user)
                Result.success(user)
            } else {
                // Local dev auth mode
                val user = User(
                    userId = "usr_${UUID.randomUUID().toString().take(8)}",
                    name = name,
                    email = email.trim(),
                    role = role,
                    department = department,
                    clubAffiliation = clubAffiliation,
                    authProvider = "Campus Directory"
                )
                firestoreUserProfileRepository.saveUserProfile(user)
                UserSessionManager.switchUser(user)
                Result.success(user)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Guest / Anonymous Sign-In using AuthRepository.
     */
    suspend fun signInAnonymously(
        guestName: String = "Campus Guest",
        role: UserRole = UserRole.STUDENT
    ): Result<User> {
        return try {
            if (authRepository.isFirebaseAvailable) {
                val result = authRepository.signInAnonymously().getOrThrow()
                val fbUser = result.user
                val user = User(
                    userId = fbUser?.uid ?: UUID.randomUUID().toString(),
                    name = guestName,
                    email = "guest@college.edu",
                    role = role,
                    department = "Guest Visitor",
                    isAnonymous = true,
                    authProvider = "Anonymous Guest"
                )
                UserSessionManager.switchUser(user)
                Result.success(user)
            } else {
                val user = User(
                    userId = "guest_${UUID.randomUUID().toString().take(8)}",
                    name = guestName,
                    email = "guest@college.edu",
                    role = role,
                    department = "Guest Visitor",
                    isAnonymous = true,
                    authProvider = "Guest Session"
                )
                UserSessionManager.switchUser(user)
                Result.success(user)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Anonymous sign-in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Authenticates with any user ID (Student ID, Roll No, Faculty ID, or custom ID).
     */
    fun signInWithUserId(
        userId: String,
        role: UserRole? = null,
        name: String? = null,
        department: String? = null
    ): Result<User> {
        val cleanId = userId.trim().ifBlank { "std_${UUID.randomUUID().toString().take(6)}" }
        val predefined = UserSessionManager.predefinedUsers.find {
            it.userId.equals(cleanId, ignoreCase = true) ||
            it.email.equals(cleanId, ignoreCase = true) ||
            it.name.equals(cleanId, ignoreCase = true)
        }
        val resolvedUser = if (predefined != null) {
            if (role != null) predefined.copy(role = role) else predefined
        } else {
            val autoRole = role ?: determineRoleFromEmail(cleanId)
            val autoEmail = if (cleanId.contains("@")) cleanId else "${cleanId.lowercase()}@college.edu"
            val autoName = name?.trim()?.ifBlank { null } ?: cleanId.replaceFirstChar { it.uppercase() }
            User(
                userId = cleanId,
                name = autoName,
                email = autoEmail,
                role = autoRole,
                department = department?.trim()?.ifBlank { "Computer Science & Engineering" } ?: "Computer Science & Engineering",
                authProvider = "Campus Pulse ID"
            )
        }
        UserSessionManager.switchUser(resolvedUser)
        return Result.success(resolvedUser)
    }

    /**
     * Role-based user management: update role, department, or club affiliation
     * and sync to Firestore.
     */
    fun updateUserRole(
        role: UserRole,
        department: String? = null,
        clubAffiliation: String? = null
    ) {
        UserSessionManager.updateUserProfile(
            role = role,
            department = department,
            clubAffiliation = clubAffiliation
        )
        val current = UserSessionManager.currentUser.value
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestoreUserProfileRepository.saveUserProfile(current)
            } catch (e: Exception) {
                Log.w(TAG, "Could not sync updated role to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Sends password reset email using Firebase Auth.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authRepository.sendPasswordResetEmail(email)
    }

    /**
     * Sign out user from Firebase Auth via AuthRepository and clear Credential Manager state
     */
    suspend fun signOut(context: Context? = null) {
        authRepository.signOut()

        if (context != null) {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Error clearing credential state: ${e.message}")
            }
        }

        UserSessionManager.signOut()
    }
}
