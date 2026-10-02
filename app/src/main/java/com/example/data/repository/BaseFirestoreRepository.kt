package com.example.data.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Extension to await completion of any Google Play Services / Firebase Task within coroutines.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result -> cont.resume(result) }
    addOnFailureListener { exception -> cont.resumeWithException(exception) }
    addOnCanceledListener { cont.cancel() }
}

/**
 * Generic base repository class encapsulating Firebase Firestore CRUD operations,
 * real-time snapshots, error handling, and offline resilience.
 *
 * Subclasses specialize this class for specific collections (e.g. "events").
 *
 * @param T The domain/data type managed by this repository.
 * @param collectionName The name of the Firestore collection (e.g. "events").
 * @param firestore The [FirebaseFirestore] instance. Can be injected or defaults to FirebaseFirestore.getInstance().
 */
abstract class BaseFirestoreRepository<T : Any>(
    val collectionName: String,
    protected val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Throwable) {
        try { Log.w("BaseFirestoreRepository", "FirebaseFirestore not initialized in environment: ${e.message}") } catch (_: Throwable) {}
        null
    }
) {
    protected val tag: String = "BaseFirestoreRepo[$collectionName]"

    protected fun logDebug(msg: String) {
        try { Log.d(tag, msg) } catch (_: Throwable) {}
    }

    protected fun logError(msg: String, tr: Throwable? = null) {
        try { Log.e(tag, msg, tr) } catch (_: Throwable) {}
    }

    /**
     * Flag indicating whether Firestore is configured and reachable in this runtime.
     */
    val isFirestoreAvailable: Boolean
        get() = firestore != null

    /**
     * Direct reference to the Firestore collection.
     */
    protected val collection: CollectionReference?
        get() = firestore?.collection(collectionName)

    /**
     * Maps a Firestore [DocumentSnapshot] to the entity [T].
     */
    abstract fun documentToItem(document: DocumentSnapshot): T?

    /**
     * Maps the entity [T] to a key-value Map for Firestore writes.
     */
    abstract fun itemToMap(item: T): Map<String, Any?>

    /**
     * Extracts the primary identifier from entity [T].
     */
    abstract fun getItemId(item: T): String

    /**
     * Injects or overrides the primary identifier onto entity [T].
     */
    abstract fun setItemId(item: T, id: String): T

    // ==========================================
    // C - CREATE OPERATIONS
    // ==========================================

    /**
     * Creates or inserts an item into the Firestore collection.
     * If [getItemId] is non-empty, writes to that specific document ID.
     * Otherwise, Firestore generates a unique auto-ID.
     *
     * @return Result containing the document ID on success.
     */
    open suspend fun create(item: T): Result<String> {
        val col = collection ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val id = getItemId(item)
            val docRef = if (id.isNotEmpty()) col.document(id) else col.document()
            val finalItem = if (id.isEmpty()) setItemId(item, docRef.id) else item
            val data = itemToMap(finalItem)

            docRef.set(data).awaitTask()
            logDebug("Successfully created document: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            logError("Error creating document in $collectionName: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sets or overwrites an item at a specific document [id].
     *
     * @param id The document ID.
     * @param item The entity to write.
     * @param merge If true, merges fields with existing document rather than overwriting.
     */
    open suspend fun set(id: String, item: T, merge: Boolean = false): Result<Unit> {
        val col = collection ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val data = itemToMap(item)
            if (merge) {
                col.document(id).set(data, SetOptions.merge()).awaitTask()
            } else {
                col.document(id).set(data).awaitTask()
            }
            logDebug("Successfully set document: $id (merge=$merge)")
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Error setting document $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // R - READ OPERATIONS
    // ==========================================

    /**
     * Fetches a single item by document [id] once.
     */
    open suspend fun get(id: String): Result<T?> {
        val col = collection ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val doc = col.document(id).get().awaitTask()
            val item = if (doc.exists()) documentToItem(doc) else null
            Result.success(item)
        } catch (e: Exception) {
            logError("Error fetching document $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow observing a single document by [id].
     */
    open fun observe(id: String): Flow<T?> {
        val col = collection ?: return flowOf(null)
        return callbackFlow {
            val listener = col.document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logError("Error listening to document $id: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(documentToItem(snapshot))
                } else {
                    trySend(null)
                }
            }
            awaitClose { listener.remove() }
        }
    }

    /**
     * Fetches all items in the collection once.
     */
    open suspend fun getAll(): Result<List<T>> {
        val col = collection ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            val snapshot = col.get().awaitTask()
            val items = snapshot.documents.mapNotNull { documentToItem(it) }
            Result.success(items)
        } catch (e: Exception) {
            logError("Error fetching all documents: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time Flow observing all items in the collection.
     */
    open fun observeAll(): Flow<List<T>> {
        val col = collection ?: return flowOf(emptyList())
        return observeQuery(col)
    }

    /**
     * Helper to observe any custom Firestore [Query] as a Kotlin [Flow].
     */
    protected fun observeQuery(query: Query): Flow<List<T>> {
        return callbackFlow {
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logError("Error listening to query: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { documentToItem(it) } ?: emptyList()
                trySend(items)
            }
            awaitClose { listener.remove() }
        }
    }

    // ==========================================
    // U - UPDATE OPERATIONS
    // ==========================================

    /**
     * Updates an entire entity in Firestore.
     */
    open suspend fun update(item: T): Result<Unit> {
        val id = getItemId(item)
        if (id.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cannot update item with empty ID"))
        }
        return set(id, item, merge = true)
    }

    /**
     * Updates specific fields of document [id].
     *
     * @param id The document ID to update.
     * @param fields Key-value map of fields to update.
     */
    open suspend fun updateFields(id: String, fields: Map<String, Any?>): Result<Unit> {
        val col = collection ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            col.document(id).update(fields).awaitTask()
            logDebug("Successfully updated fields for document: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Error updating fields for document $id: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // D - DELETE OPERATIONS
    // ==========================================

    /**
     * Deletes a document by [id].
     */
    open suspend fun delete(id: String): Result<Unit> {
        val col = collection ?: return Result.failure(IllegalStateException("Firestore is not available"))
        return try {
            col.document(id).delete().awaitTask()
            logDebug("Successfully deleted document: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Error deleting document $id: ${e.message}", e)
            Result.failure(e)
        }
    }
}
