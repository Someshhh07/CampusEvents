package com.example.data.repository

import android.util.Log
import com.example.data.local.CachedEventDao
import com.example.data.local.CachedEventEntity
import com.example.data.local.EventDao
import com.example.data.model.FirestoreEvent
import com.example.data.model.toCachedEntity
import com.example.data.model.toEventEntity
import com.example.data.model.toFirestoreEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository coordinating local Room database cached events schema and remote Firestore events.
 *
 * Implements an offline-first cache strategy:
 * 1. Read operations stream directly from the Room [cached_events] table via [allCachedEvents].
 * 2. [fetchAndCacheFromFirestore] pulls events from Firestore and inserts/replaces them in Room.
 * 3. Fallback seeding guarantees offline usability when network or remote services are uninitialized.
 */
class CachedEventRepository(
    private val cachedEventDao: CachedEventDao,
    private val firestoreEventRepository: FirestoreEventRepository = FirestoreEventRepository.instance,
    private val eventDao: EventDao? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "CachedEventRepository"
    }

    /**
     * Continuous reactive Flow of all cached events ordered by start date and time.
     */
    val allCachedEvents: Flow<List<CachedEventEntity>> = cachedEventDao.getAllCachedEvents()

    /**
     * Flow of cached events converted to standard [com.example.data.local.EventEntity]
     * for seamless compatibility with existing UI components and cards.
     */
    val allCachedAsEventEntities: Flow<List<com.example.data.local.EventEntity>> =
        cachedEventDao.getAllCachedEvents().map { list -> list.map { it.toEventEntity() } }

    /**
     * Flow of saved/bookmarked cached events.
     */
    val savedCachedEvents: Flow<List<CachedEventEntity>> = cachedEventDao.getSavedCachedEvents()

    /**
     * Fetches events from Firestore, caches them into the local Room database schema,
     * and synchronizes with local storage.
     */
    suspend fun fetchAndCacheFromFirestore(): Result<Int> = withContext(ioDispatcher) {
        try {
            val firestoreResult = firestoreEventRepository.getAllEvents()
            val firestoreEvents = firestoreResult.getOrNull() ?: emptyList()

            if (firestoreEvents.isNotEmpty()) {
                val now = System.currentTimeMillis()
                val cachedList = firestoreEvents.map { it.toCachedEntity(cachedAt = now) }
                cachedEventDao.insertCachedEvents(cachedList)
                // Also synchronize into eventDao so all screens stay consistent
                eventDao?.insertEvents(cachedList.map { it.toEventEntity() })
                Log.d(TAG, "Cached ${cachedList.size} events from Firestore into Room schema")
                Result.success(cachedList.size)
            } else {
                // When Firestore is empty or uninitialized, seed local cached events into Firestore
                // or populate Room from default entities
                val localEntities = eventDao?.getAllEventsOnce() ?: emptyList()
                if (localEntities.isNotEmpty()) {
                    val now = System.currentTimeMillis()
                    val fallbackCached = localEntities.map { it.toCachedEntity(cachedAt = now) }
                    cachedEventDao.insertCachedEvents(fallbackCached)
                    try {
                        fallbackCached.forEach { cached ->
                            firestoreEventRepository.createEvent(cached.toFirestoreEvent())
                        }
                    } catch (e: Throwable) {
                        Log.w(TAG, "Remote Firestore seeding notice: ${e.message}")
                    }
                    Result.success(fallbackCached.size)
                } else {
                    Result.success(0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch from Firestore and cache in Room: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Saves or bookmarks a cached event by ID.
     */
    suspend fun toggleSave(eventId: String, isSaved: Boolean) = withContext(ioDispatcher) {
        cachedEventDao.toggleSave(eventId, isSaved)
        eventDao?.toggleSave(eventId, isSaved)
        try {
            firestoreEventRepository.updateEventFields(eventId, mapOf("isSaved" to isSaved))
        } catch (e: Throwable) {
            Log.w(TAG, "Could not update bookmark on remote Firestore: ${e.message}")
        }
    }

    /**
     * Inserts a single event into the cached Room schema.
     */
    suspend fun cacheSingleEvent(event: CachedEventEntity) = withContext(ioDispatcher) {
        cachedEventDao.insertCachedEvent(event)
    }

    /**
     * Queries a single cached event by its unique ID.
     */
    fun getCachedEventById(eventId: String): Flow<CachedEventEntity?> =
        cachedEventDao.getCachedEventById(eventId)

    /**
     * Clears all cached events from the local Room schema.
     */
    suspend fun clearCache() = withContext(ioDispatcher) {
        cachedEventDao.clearAllCachedEvents()
    }

    /**
     * Returns total count of cached events in the Room database schema.
     */
    suspend fun getCachedEventCount(): Int = withContext(ioDispatcher) {
        cachedEventDao.getCachedCount()
    }
}
