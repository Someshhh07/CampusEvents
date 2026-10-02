package com.example.data.model

import com.example.data.local.CachedEventEntity
import com.example.data.local.EventEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName

/**
 * Data model for an Event stored in Firebase Firestore under the "events" collection.
 * Default values are provided for all fields to support no-arg constructor reflection
 * by the Firebase Firestore SDK.
 */
data class FirestoreEvent(
    @get:PropertyName("eventId")
    @set:PropertyName("eventId")
    var eventId: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = "",

    @get:PropertyName("subcategory")
    @set:PropertyName("subcategory")
    var subcategory: String = "",

    @get:PropertyName("startDate")
    @set:PropertyName("startDate")
    var startDate: String = "",

    @get:PropertyName("endDate")
    @set:PropertyName("endDate")
    var endDate: String = "",

    @get:PropertyName("startTime")
    @set:PropertyName("startTime")
    var startTime: String = "",

    @get:PropertyName("endTime")
    @set:PropertyName("endTime")
    var endTime: String = "",

    @get:PropertyName("venue")
    @set:PropertyName("venue")
    var venue: String = "",

    @get:PropertyName("organizer")
    @set:PropertyName("organizer")
    var organizer: String = "",

    @get:PropertyName("organizerType")
    @set:PropertyName("organizerType")
    var organizerType: String = "CLUB",

    @get:PropertyName("organizerId")
    @set:PropertyName("organizerId")
    var organizerId: String = "",

    @get:PropertyName("department")
    @set:PropertyName("department")
    var department: String = "",

    @get:PropertyName("odStatus")
    @set:PropertyName("odStatus")
    var odStatus: String = "AVAILABLE",

    @get:PropertyName("registrationRequired")
    @set:PropertyName("registrationRequired")
    var registrationRequired: Boolean = true,

    @get:PropertyName("registrationUrl")
    @set:PropertyName("registrationUrl")
    var registrationUrl: String = "",

    @get:PropertyName("contactInformation")
    @set:PropertyName("contactInformation")
    var contactInformation: String = "",

    @get:PropertyName("posterUrl")
    @set:PropertyName("posterUrl")
    var posterUrl: String = "",

    @get:PropertyName("source")
    @set:PropertyName("source")
    var source: String = "Campus App",

    @get:PropertyName("sourceType")
    @set:PropertyName("sourceType")
    var sourceType: String = "COLLEGE_PORTAL",

    @get:PropertyName("sourceUrl")
    @set:PropertyName("sourceUrl")
    var sourceUrl: String = "",

    @get:PropertyName("externalId")
    @set:PropertyName("externalId")
    var externalId: String = "",

    @get:PropertyName("createdBy")
    @set:PropertyName("createdBy")
    var createdBy: String = "System",

    @get:PropertyName("updatedBy")
    @set:PropertyName("updatedBy")
    var updatedBy: String = "System",

    @get:PropertyName("verifiedBy")
    @set:PropertyName("verifiedBy")
    var verifiedBy: String = "",

    @get:PropertyName("verificationStatus")
    @set:PropertyName("verificationStatus")
    var verificationStatus: String = "VERIFIED",

    @get:PropertyName("eventStatus")
    @set:PropertyName("eventStatus")
    var eventStatus: String = "PUBLISHED",

    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("updatedAt")
    @set:PropertyName("updatedAt")
    var updatedAt: Long = System.currentTimeMillis(),

    @get:PropertyName("isSaved")
    @set:PropertyName("isSaved")
    var isSaved: Boolean = false,

    @get:PropertyName("conflictDetails")
    @set:PropertyName("conflictDetails")
    var conflictDetails: String? = null,

    @get:PropertyName("isDuplicateCandidate")
    @set:PropertyName("isDuplicateCandidate")
    var isDuplicateCandidate: Boolean = false,

    @get:PropertyName("duplicateWithId")
    @set:PropertyName("duplicateWithId")
    var duplicateWithId: String? = null
) {
    /**
     * Converts this FirestoreEvent to a Map of key-value pairs suitable for Firestore document writes.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "eventId" to eventId,
            "title" to title,
            "description" to description,
            "category" to category,
            "subcategory" to subcategory,
            "startDate" to startDate,
            "endDate" to endDate,
            "startTime" to startTime,
            "endTime" to endTime,
            "venue" to venue,
            "organizer" to organizer,
            "organizerType" to organizerType,
            "organizerId" to organizerId,
            "department" to department,
            "odStatus" to odStatus,
            "registrationRequired" to registrationRequired,
            "registrationUrl" to registrationUrl,
            "contactInformation" to contactInformation,
            "posterUrl" to posterUrl,
            "source" to source,
            "sourceType" to sourceType,
            "sourceUrl" to sourceUrl,
            "externalId" to externalId,
            "createdBy" to createdBy,
            "updatedBy" to updatedBy,
            "verifiedBy" to verifiedBy,
            "verificationStatus" to verificationStatus,
            "eventStatus" to eventStatus,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "isSaved" to isSaved,
            "conflictDetails" to conflictDetails,
            "isDuplicateCandidate" to isDuplicateCandidate,
            "duplicateWithId" to duplicateWithId
        )
    }

    /**
     * Converts to local Room EventEntity for offline caching and local database operations.
     */
    fun toEntity(): EventEntity {
        return EventEntity(
            eventId = eventId,
            title = title,
            description = description,
            category = category,
            subcategory = subcategory,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime,
            venue = venue,
            organizer = organizer,
            organizerType = organizerType,
            organizerId = organizerId,
            department = department,
            odStatus = odStatus,
            registrationRequired = registrationRequired,
            registrationUrl = registrationUrl,
            contactInformation = contactInformation,
            posterUrl = posterUrl,
            source = source,
            sourceType = sourceType,
            sourceUrl = sourceUrl,
            externalId = externalId,
            createdBy = createdBy,
            updatedBy = updatedBy,
            verifiedBy = verifiedBy,
            verificationStatus = verificationStatus,
            eventStatus = eventStatus,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isSaved = isSaved,
            conflictDetails = conflictDetails,
            isDuplicateCandidate = isDuplicateCandidate,
            duplicateWithId = duplicateWithId
        )
    }

    companion object {
        /**
         * Safely creates a FirestoreEvent from a Firestore DocumentSnapshot.
         */
        fun fromDocument(doc: DocumentSnapshot): FirestoreEvent? {
            return try {
                doc.toObject(FirestoreEvent::class.java)?.apply {
                    if (eventId.isEmpty()) {
                        eventId = doc.id
                    }
                }
            } catch (e: Exception) {
                // Fallback manual parse if reflection encounters typed schema mismatch
                val data = doc.data ?: return null
                FirestoreEvent(
                    eventId = (data["eventId"] as? String)?.takeIf { it.isNotEmpty() } ?: doc.id,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    category = data["category"] as? String ?: "",
                    subcategory = data["subcategory"] as? String ?: "",
                    startDate = data["startDate"] as? String ?: "",
                    endDate = data["endDate"] as? String ?: "",
                    startTime = data["startTime"] as? String ?: "",
                    endTime = data["endTime"] as? String ?: "",
                    venue = data["venue"] as? String ?: "",
                    organizer = data["organizer"] as? String ?: "",
                    organizerType = data["organizerType"] as? String ?: "CLUB",
                    organizerId = data["organizerId"] as? String ?: "",
                    department = data["department"] as? String ?: "",
                    odStatus = data["odStatus"] as? String ?: "AVAILABLE",
                    registrationRequired = data["registrationRequired"] as? Boolean ?: true,
                    registrationUrl = data["registrationUrl"] as? String ?: "",
                    contactInformation = data["contactInformation"] as? String ?: "",
                    posterUrl = data["posterUrl"] as? String ?: "",
                    source = data["source"] as? String ?: "Campus App",
                    sourceType = data["sourceType"] as? String ?: "COLLEGE_PORTAL",
                    sourceUrl = data["sourceUrl"] as? String ?: "",
                    externalId = data["externalId"] as? String ?: "",
                    createdBy = data["createdBy"] as? String ?: "System",
                    updatedBy = data["updatedBy"] as? String ?: "System",
                    verifiedBy = data["verifiedBy"] as? String ?: "",
                    verificationStatus = data["verificationStatus"] as? String ?: "VERIFIED",
                    eventStatus = data["eventStatus"] as? String ?: "PUBLISHED",
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    isSaved = data["isSaved"] as? Boolean ?: false,
                    conflictDetails = data["conflictDetails"] as? String,
                    isDuplicateCandidate = data["isDuplicateCandidate"] as? Boolean ?: false,
                    duplicateWithId = data["duplicateWithId"] as? String
                )
            }
        }
    }
}

/**
 * Extension function to convert a local Room EventEntity to a FirestoreEvent model.
 */
fun EventEntity.toFirestoreEvent(): FirestoreEvent {
    return FirestoreEvent(
        eventId = eventId,
        title = title,
        description = description,
        category = category,
        subcategory = subcategory,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        venue = venue,
        organizer = organizer,
        organizerType = organizerType,
        organizerId = organizerId,
        department = department,
        odStatus = odStatus,
        registrationRequired = registrationRequired,
        registrationUrl = registrationUrl,
        contactInformation = contactInformation,
        posterUrl = posterUrl,
        source = source,
        sourceType = sourceType,
        sourceUrl = sourceUrl,
        externalId = externalId,
        createdBy = createdBy,
        updatedBy = updatedBy,
        verifiedBy = verifiedBy,
        verificationStatus = verificationStatus,
        eventStatus = eventStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSaved = isSaved,
        conflictDetails = conflictDetails,
        isDuplicateCandidate = isDuplicateCandidate,
        duplicateWithId = duplicateWithId
    )
}

/**
 * Extension function to convert a FirestoreEvent model to a local Room CachedEventEntity.
 */
fun FirestoreEvent.toCachedEntity(cachedAt: Long = System.currentTimeMillis()): CachedEventEntity {
    return CachedEventEntity(
        eventId = eventId,
        title = title,
        description = description,
        category = category,
        subcategory = subcategory,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        venue = venue,
        organizer = organizer,
        organizerType = organizerType,
        organizerId = organizerId,
        department = department,
        odStatus = odStatus,
        registrationRequired = registrationRequired,
        registrationUrl = registrationUrl,
        contactInformation = contactInformation,
        posterUrl = posterUrl,
        source = source,
        sourceType = sourceType,
        sourceUrl = sourceUrl,
        externalId = externalId,
        createdBy = createdBy,
        updatedBy = updatedBy,
        verifiedBy = verifiedBy,
        verificationStatus = verificationStatus,
        eventStatus = eventStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSaved = isSaved,
        conflictDetails = conflictDetails,
        isDuplicateCandidate = isDuplicateCandidate,
        duplicateWithId = duplicateWithId,
        cachedAt = cachedAt,
        isSyncedWithFirestore = true
    )
}

/**
 * Extension function to convert a CachedEventEntity back to a FirestoreEvent.
 */
fun CachedEventEntity.toFirestoreEvent(): FirestoreEvent {
    return FirestoreEvent(
        eventId = eventId,
        title = title,
        description = description,
        category = category,
        subcategory = subcategory,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        venue = venue,
        organizer = organizer,
        organizerType = organizerType,
        organizerId = organizerId,
        department = department,
        odStatus = odStatus,
        registrationRequired = registrationRequired,
        registrationUrl = registrationUrl,
        contactInformation = contactInformation,
        posterUrl = posterUrl,
        source = source,
        sourceType = sourceType,
        sourceUrl = sourceUrl,
        externalId = externalId,
        createdBy = createdBy,
        updatedBy = updatedBy,
        verifiedBy = verifiedBy,
        verificationStatus = verificationStatus,
        eventStatus = eventStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSaved = isSaved,
        conflictDetails = conflictDetails,
        isDuplicateCandidate = isDuplicateCandidate,
        duplicateWithId = duplicateWithId
    )
}

/**
 * Extension function to convert a CachedEventEntity to a standard EventEntity for UI components.
 */
fun CachedEventEntity.toEventEntity(): EventEntity {
    return EventEntity(
        eventId = eventId,
        title = title,
        description = description,
        category = category,
        subcategory = subcategory,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        venue = venue,
        organizer = organizer,
        organizerType = organizerType,
        organizerId = organizerId,
        department = department,
        odStatus = odStatus,
        registrationRequired = registrationRequired,
        registrationUrl = registrationUrl,
        contactInformation = contactInformation,
        posterUrl = posterUrl,
        source = source,
        sourceType = sourceType,
        sourceUrl = sourceUrl,
        externalId = externalId,
        createdBy = createdBy,
        updatedBy = updatedBy,
        verifiedBy = verifiedBy,
        verificationStatus = verificationStatus,
        eventStatus = eventStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSaved = isSaved,
        conflictDetails = conflictDetails,
        isDuplicateCandidate = isDuplicateCandidate,
        duplicateWithId = duplicateWithId
    )
}

/**
 * Extension function to convert a standard EventEntity to a CachedEventEntity.
 */
fun EventEntity.toCachedEntity(cachedAt: Long = System.currentTimeMillis()): CachedEventEntity {
    return CachedEventEntity(
        eventId = eventId,
        title = title,
        description = description,
        category = category,
        subcategory = subcategory,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        venue = venue,
        organizer = organizer,
        organizerType = organizerType,
        organizerId = organizerId,
        department = department,
        odStatus = odStatus,
        registrationRequired = registrationRequired,
        registrationUrl = registrationUrl,
        contactInformation = contactInformation,
        posterUrl = posterUrl,
        source = source,
        sourceType = sourceType,
        sourceUrl = sourceUrl,
        externalId = externalId,
        createdBy = createdBy,
        updatedBy = updatedBy,
        verifiedBy = verifiedBy,
        verificationStatus = verificationStatus,
        eventStatus = eventStatus,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isSaved = isSaved,
        conflictDetails = conflictDetails,
        isDuplicateCandidate = isDuplicateCandidate,
        duplicateWithId = duplicateWithId,
        cachedAt = cachedAt,
        isSyncedWithFirestore = true
    )
}

