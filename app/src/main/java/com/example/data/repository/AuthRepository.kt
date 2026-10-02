package com.example.data.repository

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Awaiting extension for Firebase Tasks within Coroutines.
 */
internal suspend fun <T> Task<T>.awaitTaskResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result -> cont.resume(result) }
    addOnFailureListener { exception -> cont.resumeWithException(exception) }
    addOnCanceledListener { cont.cancel() }
}

/**
 * Repository encapsulating all direct Firebase Authentication interactions.
 * Provides clean decoupling from the raw Firebase Auth SDK, allowing
 * robust credential sign-in, email authentication, guest sessions, and sign-out.
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Throwable) {
        Log.w(TAG, "Firebase Auth not available in runtime environment: ${e.message}")
        null
    }
) {

    companion object {
        private const val TAG = "AuthRepository"

        @Volatile
        private var INSTANCE: AuthRepository? = null

        val instance: AuthRepository
            get() = INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthRepository().also { INSTANCE = it }
            }
    }

    /**
     * Indicates whether the Firebase Auth SDK is properly configured and available.
     */
    val isFirebaseAvailable: Boolean
        get() = firebaseAuth != null

    /**
     * Returns the currently authenticated [FirebaseUser], if any.
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth?.currentUser

    /**
     * Unique UID of the current authenticated user, or null if not logged in.
     */
    val currentUserId: String?
        get() = firebaseAuth?.currentUser?.uid

    /**
     * Returns true if a Firebase user session currently exists.
     */
    val isUserLoggedIn: Boolean
        get() = firebaseAuth?.currentUser != null

    /**
     * Cold Flow emitting changes to the current [FirebaseUser].
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val auth = firebaseAuth
        if (auth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            trySend(fbAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        // Emit initial value
        trySend(auth.currentUser)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Registers a raw [FirebaseAuth.AuthStateListener].
     */
    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth?.addAuthStateListener(listener)
    }

    /**
     * Removes a previously registered [FirebaseAuth.AuthStateListener].
     */
    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth?.removeAuthStateListener(listener)
    }

    /**
     * Authenticates with an [AuthCredential] (e.g. from Google Sign-In / Credential Manager).
     */
    suspend fun signInWithCredential(credential: AuthCredential): Result<AuthResult> {
        val auth = firebaseAuth ?: return Result.failure(
            IllegalStateException("Firebase Auth is not available in the current environment.")
        )
        return try {
            val authResult = auth.signInWithCredential(credential).awaitTaskResult()
            Log.d(TAG, "signInWithCredential successful for uid: ${authResult.user?.uid}")
            Result.success(authResult)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithCredential failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Authenticates with college email and password.
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        val auth = firebaseAuth ?: return Result.failure(
            IllegalStateException("Firebase Auth is not available.")
        )
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).awaitTaskResult()
            Log.d(TAG, "signInWithEmailAndPassword successful for uid: ${result.user?.uid}")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmailAndPassword failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Registers a new user with email and password.
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        val auth = firebaseAuth ?: return Result.failure(
            IllegalStateException("Firebase Auth is not available.")
        )
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).awaitTaskResult()
            Log.d(TAG, "createUserWithEmailAndPassword successful for uid: ${result.user?.uid}")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "createUserWithEmailAndPassword failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Authenticates anonymously (e.g. for guest / demo users).
     */
    suspend fun signInAnonymously(): Result<AuthResult> {
        val auth = firebaseAuth ?: return Result.failure(
            IllegalStateException("Firebase Auth is not available.")
        )
        return try {
            val result = auth.signInAnonymously().awaitTaskResult()
            Log.d(TAG, "signInAnonymously successful for uid: ${result.user?.uid}")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "signInAnonymously failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates the user's profile display name and optional photo URI.
     */
    suspend fun updateProfile(
        user: FirebaseUser? = firebaseAuth?.currentUser,
        displayName: String? = null,
        photoUri: Uri? = null
    ): Result<Unit> {
        val targetUser = user ?: return Result.failure(IllegalStateException("No authenticated user to update."))
        return try {
            val requestBuilder = UserProfileChangeRequest.Builder()
            displayName?.let { requestBuilder.setDisplayName(it) }
            photoUri?.let { requestBuilder.setPhotoUri(it) }
            targetUser.updateProfile(requestBuilder.build()).awaitTaskResult()
            Log.d(TAG, "updateProfile successful for uid: ${targetUser.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "updateProfile failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sends a password reset email to the specified email address.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val auth = firebaseAuth ?: return Result.failure(
            IllegalStateException("Firebase Auth is not available.")
        )
        return try {
            auth.sendPasswordResetEmail(email.trim()).awaitTaskResult()
            Log.d(TAG, "sendPasswordResetEmail sent to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordResetEmail failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Signs out the current user from Firebase Authentication.
     */
    fun signOut() {
        try {
            firebaseAuth?.signOut()
            Log.d(TAG, "Firebase Auth sign out executed.")
        } catch (e: Exception) {
            Log.w(TAG, "Error executing Firebase Auth signOut: ${e.message}", e)
        }
    }
}
