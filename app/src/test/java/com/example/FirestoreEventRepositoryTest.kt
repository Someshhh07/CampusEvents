package com.example

import com.example.data.local.CachedEventEntity
import com.example.data.local.EventEntity
import com.example.data.model.FirestoreEvent
import com.example.data.model.toCachedEntity
import com.example.data.model.toEventEntity
import com.example.data.model.toFirestoreEvent
import com.example.data.repository.FirestoreEventRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreEventRepositoryTest {

    @Test
    fun firestoreEvent_supportsAllRequiredFields() {
        val event = FirestoreEvent(
            eventId = "EVT-TEST-001",
            title = "Annual Hackathon 2026",
            description = "Build innovative AI solutions over 36 hours",
            category = "Technical",
            subcategory = "Hackathon",
            startDate = "2026-09-25",
            endDate = "2026-09-26",
            startTime = "09:00 AM",
            endTime = "06:00 PM",
            venue = "Main Tech Auditorium",
            organizer = "Coding Club",
            organizerType = "CLUB",
            organizerId = "club_coding",
            department = "Computer Science",
            odStatus = "AVAILABLE",
            registrationRequired = true,
            registrationUrl = "https://example.com/register",
            contactInformation = "lead@codingclub.edu",
            posterUrl = "poster_hackathon",
            source = "Official College Calendar",
            sourceType = "COLLEGE_API",
            sourceUrl = "https://example.com/source",
            externalId = "EXT-12345",
            createdBy = "President Alex",
            updatedBy = "Coordinator Sarah",
            verifiedBy = "Dr. Robert Vance",
            verificationStatus = "VERIFIED",
            eventStatus = "PUBLISHED",
            createdAt = 1770000000000L,
            updatedAt = 1770003600000L,
            isSaved = true,
            conflictDetails = null,
            isDuplicateCandidate = false,
            duplicateWithId = null
        )

        // 1. Verify Map conversion has all 34 fields
        val map = event.toMap()
        assertEquals(34, map.size)
        assertEquals("EVT-TEST-001", map["eventId"])
        assertEquals("Annual Hackathon 2026", map["title"])
        assertEquals("Technical", map["category"])
        assertEquals("2026-09-25", map["startDate"])
        assertEquals("Main Tech Auditorium", map["venue"])
        assertEquals("AVAILABLE", map["odStatus"])
        assertEquals(true, map["registrationRequired"])
        assertEquals("VERIFIED", map["verificationStatus"])
        assertEquals("PUBLISHED", map["eventStatus"])

        // 2. Verify Room Entity round-trip conversion
        val entity = event.toEntity()
        assertEquals(event.eventId, entity.eventId)
        assertEquals(event.title, entity.title)
        assertEquals(event.venue, entity.venue)
        assertEquals(event.odStatus, entity.odStatus)
        assertEquals(event.verificationStatus, entity.verificationStatus)

        val restoredEvent = entity.toFirestoreEvent()
        assertEquals(event.eventId, restoredEvent.eventId)
        assertEquals(event.title, restoredEvent.title)
        assertEquals(event.description, restoredEvent.description)
        assertEquals(event.organizer, restoredEvent.organizer)
        assertEquals(event.department, restoredEvent.department)
        assertEquals(event.isSaved, restoredEvent.isSaved)
    }

    @Test
    fun firestoreEventRepository_initializesGracefully() {
        val repo = FirestoreEventRepository.instance
        assertNotNull(repo)
        assertEquals(FirestoreEventRepository.COLLECTION_NAME, repo.collectionName)
    }

    @Test
    fun cachedEventEntity_convertsBidirectionallyWithFirestoreEvent() {
        val original = FirestoreEvent(
            eventId = "CACHE-TEST-001",
            title = "Machine Learning Symposium",
            description = "Symposium on cutting-edge deep learning models",
            category = "Technical",
            subcategory = "AI Conference",
            startDate = "2026-10-15",
            endDate = "2026-10-16",
            startTime = "10:00 AM",
            endTime = "04:00 PM",
            venue = "Auditorium Alpha",
            organizer = "AI Society",
            organizerType = "CLUB",
            organizerId = "club_ai",
            department = "Computer Science",
            odStatus = "AVAILABLE",
            registrationRequired = true,
            registrationUrl = "https://ai.example.edu/register",
            contactInformation = "contact@ai.example.edu",
            posterUrl = "poster_ai",
            source = "Firestore",
            sourceType = "COLLEGE_PORTAL",
            sourceUrl = "https://example.edu/portal",
            externalId = "EXT-SYMP-01",
            createdBy = "Admin",
            updatedBy = "Coordinator",
            verifiedBy = "Dean Dr. Rao",
            verificationStatus = "VERIFIED",
            eventStatus = "PUBLISHED",
            createdAt = 1772000000000L,
            updatedAt = 1772005000000L,
            isSaved = true
        )

        // Convert FirestoreEvent -> CachedEventEntity
        val cachedEntity = original.toCachedEntity(cachedAt = 1772006000000L)
        assertEquals("CACHE-TEST-001", cachedEntity.eventId)
        assertEquals("Machine Learning Symposium", cachedEntity.title)
        assertEquals("Technical", cachedEntity.category)
        assertEquals("Auditorium Alpha", cachedEntity.venue)
        assertEquals(true, cachedEntity.isSaved)
        assertEquals(1772006000000L, cachedEntity.cachedAt)
        assertEquals(true, cachedEntity.isSyncedWithFirestore)

        // Convert CachedEventEntity -> FirestoreEvent
        val restoredFirestoreEvent = cachedEntity.toFirestoreEvent()
        assertEquals(original.eventId, restoredFirestoreEvent.eventId)
        assertEquals(original.title, restoredFirestoreEvent.title)
        assertEquals(original.description, restoredFirestoreEvent.description)
        assertEquals(original.venue, restoredFirestoreEvent.venue)
        assertEquals(original.organizer, restoredFirestoreEvent.organizer)

        // Convert CachedEventEntity -> EventEntity (for Compose UI EventCard)
        val eventEntity = cachedEntity.toEventEntity()
        assertEquals(original.eventId, eventEntity.eventId)
        assertEquals(original.title, eventEntity.title)
        assertEquals(original.category, eventEntity.category)
        assertEquals(original.venue, eventEntity.venue)
        assertEquals(original.odStatus, eventEntity.odStatus)
    }
}
