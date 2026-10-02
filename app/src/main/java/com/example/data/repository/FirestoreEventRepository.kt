package com.example.data.repository

import android.util.Log
import com.example.data.local.EventEntity
import com.example.data.model.FirestoreEvent
import com.example.data.model.toFirestoreEvent
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

/**
 * Concrete repository handling Firestore CRUD operations and real-time streaming
 * for the "events" collection, supporting all required event fields and bidirectional
 * conversion between Firestore models and local Room EventEntity representations.
 *
 * Supported Event Fields:
 * - [eventId]: Unique event identifier (Document ID).
 * - [title]: Full title of the event.
 * - [description]: Comprehensive event description.
 * - [category]: Primary classification (Technical, Cultural, Sports, Academic, Workshop, Hackathon).
 * - [subcategory]: Optional granular sub-topic.
 * - [startDate]: Date in ISO format (YYYY-MM-DD).
 * - [endDate]: Optional concluding date.
 * - [startTime]: Starting time string (e.g., "10:00 AM").
 * - [endTime]: Concluding time string (e.g., "05:00 PM").
 * - [venue]: Campus location or hall.
 * - [organizer]: Name of the organizing club/department.
 * - [organizerType]: Category of organizer (CLUB, DEPARTMENT, COLLEGE, COMMUNITY).
 * - [organizerId]: Identifier for permissions and club affiliation matching.
 * - [department]: Academic department associated with the event.
 * - [odStatus]: On-Duty attendance status (AVAILABLE, NOT_AVAILABLE, PENDING, NOT_APPLICABLE).
 * - [registrationRequired]: Flag indicating whether attendees must register in advance.
 * - [registrationUrl]: External or portal link for registration.
 * - [contactInformation]: Point of contact phone/email.
 * - [posterUrl]: Resource identifier or URL for poster graphics.
 * - [source]: Provenance attribution (e.g., "College Official API").
 * - [sourceType]: Source hierarchy (COLLEGE_API, FACULTY, CLUB_PRESIDENT, etc.).
 * - [sourceUrl]: External link to source announcement.
 * - [externalId]: External system sync ID.
 * - [createdBy]: User name or ID who initiated the event record.
 * - [updatedBy]: User name or ID who last altered the event record.
 * - [verifiedBy]: Faculty or administrator certifying the event.
 * - [verificationStatus]: Verification state (UNVERIFIED, PENDING, VERIFIED, CONFLICT, REJECTED).
 * - [eventStatus]: Lifecycle state (DRAFT, PENDING, PUBLISHED, CANCELLED, COMPLETED).
 * - [createdAt]: Epoch millisecond creation timestamp.
 * - [updatedAt]: Epoch millisecond modification timestamp.
 * - [isSaved]: Local bookmark state.
 * - [conflictDetails]: Description of scheduling/venue conflicts if detected.
 * - [isDuplicateCandidate]: Flag for duplicate detection heuristics.
 * - [duplicateWithId]: Matching conflicting event ID.
 */
class FirestoreEventRepository(
    firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Throwable) {
        try { Log.w("FirestoreEventRepository", "FirebaseFirestore instance unavailable: ${e.message}") } catch (_: Throwable) {}
        null
    }
) : BaseFirestoreRepository<FirestoreEvent>(COLLECTION_NAME, firestore) {

    companion object {
        const val COLLECTION_NAME = "events"
        private const val TAG = "FirestoreEventRepository"

        @Volatile
        private var INSTANCE: FirestoreEventRepository? = null

        val instance: FirestoreEventRepository
            get() = INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirestoreEventRepository().also { INSTANCE = it }
            }
    }

    override fun documentToItem(document: DocumentSnapshot): FirestoreEvent? {
        return FirestoreEvent.fromDocument(document)
    }

    override fun itemToMap(item: FirestoreEvent): Map<String, Any?> {
        return item.toMap()
    }

    override fun getItemId(item: FirestoreEvent): String {
        return item.eventId
    }

    override fun setItemId(item: FirestoreEvent, id: String): FirestoreEvent {
        return item.copy(eventId = id)
    }

    // ==========================================
    // CRUD Operations for Events
    // ==========================================

    /**
     * Creates a new event in the "events" collection with all required fields.
     * Generates a unique UUID if [event.eventId] is empty.
     */
    suspend fun createEvent(event: FirestoreEvent): Result<String> {
        val eventToCreate = if (event.eventId.isBlank()) {
            event.copy(
                eventId = "EVT-${UUID.randomUUID().toString().take(8).uppercase()}",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        } else {
            event.copy(
                updatedAt = System.currentTimeMillis()
            )
        }
        return create(eventToCreate)
    }

    /**
     * Convenience method to create an event directly from a Room [EventEntity].
     */
    suspend fun createEvent(entity: EventEntity): Result<String> {
        return createEvent(entity.toFirestoreEvent())
    }

    /**
     * Fetches an event by its unique [eventId].
     */
    suspend fun getEventById(eventId: String): Result<FirestoreEvent?> {
        return get(eventId)
    }

    /**
     * Real-time Flow observing a single event by [eventId].
     */
    fun observeEventById(eventId: String): Flow<FirestoreEvent?> {
        return observe(eventId)
    }

    /**
     * Fetches all events from the "events" collection once.
     */
    suspend fun getAllEvents(): Result<List<FirestoreEvent>> {
        return getAll()
    }

    /**
     * Real-time Flow observing all events in the "events" collection.
     */
    fun observeAllEvents(): Flow<List<FirestoreEvent>> {
        return observeAll()
    }

    /**
     * Updates an entire event document in Firestore, automatically updating [updatedAt].
     */
    suspend fun updateEvent(event: FirestoreEvent): Result<Unit> {
        val updated = event.copy(updatedAt = System.currentTimeMillis())
        return update(updated)
    }

    /**
     * Updates an entire event document from a local Room [EventEntity].
     */
    suspend fun updateEvent(entity: EventEntity): Result<Unit> {
        return updateEvent(entity.toFirestoreEvent())
    }

    /**
     * Selectively updates specific fields of an event document by [eventId].
     * Automatically includes [updatedAt] in the modification payload.
     */
    suspend fun updateEventFields(eventId: String, updates: Map<String, Any?>): Result<Unit> {
        val mutableUpdates = updates.toMutableMap()
        if (!mutableUpdates.containsKey("updatedAt")) {
            mutableUpdates["updatedAt"] = System.currentTimeMillis()
        }
        return updateFields(eventId, mutableUpdates)
    }

    /**
     * Deletes an event from the "events" collection by [eventId].
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return delete(eventId)
    }

    // ==========================================
    // Filtered Query Real-Time Streams
    // ==========================================

    /**
     * Streams events filtered by category (e.g. "Technical", "Cultural", "Sports").
     */
    fun observeEventsByCategory(category: String): Flow<List<FirestoreEvent>> {
        val col = collection ?: return flowOf(emptyList())
        val query = col.whereEqualTo("category", category)
        return observeQuery(query)
    }

    /**
     * Streams events filtered by organizer identifier.
     */
    fun observeEventsByOrganizer(organizerId: String): Flow<List<FirestoreEvent>> {
        val col = collection ?: return flowOf(emptyList())
        val query = col.whereEqualTo("organizerId", organizerId)
        return observeQuery(query)
    }

    /**
     * Streams events filtered by specific start date (YYYY-MM-DD).
     */
    fun observeEventsByDate(date: String): Flow<List<FirestoreEvent>> {
        val col = collection ?: return flowOf(emptyList())
        val query = col.whereEqualTo("startDate", date)
        return observeQuery(query)
    }

    /**
     * Streams events with a specific On-Duty (OD) status.
     */
    fun observeEventsByOdStatus(odStatus: String): Flow<List<FirestoreEvent>> {
        val col = collection ?: return flowOf(emptyList())
        val query = col.whereEqualTo("odStatus", odStatus)
        return observeQuery(query)
    }

    /**
     * Streams officially published events.
     */
    fun observePublishedEvents(): Flow<List<FirestoreEvent>> {
        val col = collection ?: return flowOf(emptyList())
        val query = col.whereEqualTo("eventStatus", "PUBLISHED")
        return observeQuery(query)
    }

    /**
     * Streams verified events.
     */
    fun observeVerifiedEvents(): Flow<List<FirestoreEvent>> {
        val col = collection ?: return flowOf(emptyList())
        val query = col.whereEqualTo("verificationStatus", "VERIFIED")
        return observeQuery(query)
    }
}
