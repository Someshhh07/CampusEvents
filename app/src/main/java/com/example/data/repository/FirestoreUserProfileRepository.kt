package com.example.data.repository

import android.util.Log
import com.example.model.User
import com.example.model.UserRole
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Concrete repository handling Firestore CRUD operations for the "users" collection,
 * persisting user profiles, roles, and departmental permissions.
 */
class FirestoreUserProfileRepository(
    firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Throwable) {
        try { Log.w("FirestoreUserProfileRepo", "FirebaseFirestore instance unavailable: ${e.message}") } catch (_: Throwable) {}
        null
    }
) : BaseFirestoreRepository<User>(
    collectionName = COLLECTION_NAME,
    firestore = firestore
) {
    companion object {
        const val COLLECTION_NAME = "users"
        val instance: FirestoreUserProfileRepository by lazy { FirestoreUserProfileRepository() }
    }

    // Local in-memory cache for offline/emulator/local testing
    private val localUserCache = ConcurrentHashMap<String, User>()

    override fun documentToItem(document: DocumentSnapshot): User? {
        val data = document.data ?: return null
        val userId = data["userId"] as? String ?: document.id
        val name = data["name"] as? String ?: ""
        val email = data["email"] as? String ?: ""
        val roleStr = data["role"] as? String ?: UserRole.STUDENT.name
        val role = try { UserRole.valueOf(roleStr) } catch (_: Throwable) { UserRole.STUDENT }
        val department = data["department"] as? String ?: "General"
        val clubAffiliation = data["clubAffiliation"] as? String
        val clubRoleStr = data["clubRole"] as? String
        val clubRole = clubRoleStr?.let { try { UserRole.valueOf(it) } catch (_: Throwable) { null } }
        val isAnonymous = data["isAnonymous"] as? Boolean ?: false
        val photoUrl = data["photoUrl"] as? String
        val authProvider = data["authProvider"] as? String ?: "Campus Directory"

        return User(
            userId = userId,
            name = name,
            email = email,
            role = role,
            department = department,
            clubAffiliation = clubAffiliation,
            clubRole = clubRole,
            isAnonymous = isAnonymous,
            photoUrl = photoUrl,
            authProvider = authProvider
        )
    }

    override fun itemToMap(item: User): Map<String, Any?> {
        return mapOf(
            "userId" to item.userId,
            "name" to item.name,
            "email" to item.email,
            "role" to item.role.name,
            "department" to item.department,
            "clubAffiliation" to item.clubAffiliation,
            "clubRole" to item.clubRole?.name,
            "isAnonymous" to item.isAnonymous,
            "photoUrl" to item.photoUrl,
            "authProvider" to item.authProvider,
            "updatedAt" to System.currentTimeMillis()
        )
    }

    override fun getItemId(item: User): String = item.userId

    override fun setItemId(item: User, id: String): User = item.copy(userId = id)

    suspend fun getUserProfile(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            if (!isFirestoreAvailable) {
                return@withContext Result.success(localUserCache[userId])
            }
            val result = get(userId)
            val user = result.getOrNull()
            if (user != null) {
                localUserCache[userId] = user
                localUserCache[user.email.lowercase()] = user
            }
            Result.success(user ?: localUserCache[userId])
        } catch (e: Exception) {
            Log.w(tag, "Failed to get user profile by userId: ${e.message}")
            Result.success(localUserCache[userId])
        }
    }

    suspend fun getUserProfileByEmail(email: String): Result<User?> = withContext(Dispatchers.IO) {
        val normalized = email.trim().lowercase()
        try {
            if (!isFirestoreAvailable) {
                return@withContext Result.success(localUserCache[normalized])
            }
            val snapshot = collection?.whereEqualTo("email", email.trim())?.get()?.awaitTask()
            val doc = snapshot?.documents?.firstOrNull()
            val user = doc?.let { documentToItem(it) }
            if (user != null) {
                localUserCache[user.userId] = user
                localUserCache[normalized] = user
                Result.success(user)
            } else {
                Result.success(localUserCache[normalized])
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to get user profile by email: ${e.message}")
            Result.success(localUserCache[normalized])
        }
    }

    suspend fun saveUserProfile(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        localUserCache[user.userId] = user
        localUserCache[user.email.lowercase()] = user
        try {
            if (isFirestoreAvailable) {
                set(user.userId, user, merge = true).getOrThrow()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(tag, "Failed to persist user profile to Firestore: ${e.message}")
            Result.success(Unit)
        }
    }
}
