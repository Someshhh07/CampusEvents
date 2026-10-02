package com.example.service

import com.example.data.local.EventEntity
import com.example.model.EventStatus
import com.example.model.OdStatus
import com.example.model.SourceType
import com.example.model.VerificationStatus
import java.util.UUID

data class ExtractedEventDraft(
    val title: String,
    val description: String,
    val category: String,
    val startDate: String,
    val startTime: String,
    val endTime: String,
    val venue: String,
    val organizer: String,
    val department: String,
    val odStatus: String,
    val registrationUrl: String,
    val contactInformation: String
)

object AiExtractionService {

    /**
     * Parses raw poster OCR text, email text, or circular announcements.
     * Extracts structured parameters and forms a Draft Event requiring human verification.
     */
    fun extractEventFromText(rawText: String): ExtractedEventDraft {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        // Heuristic extraction
        var title = lines.firstOrNull() ?: "Extracted Campus Event"
        var description = rawText
        var category = "Technical"
        var startDate = "2026-10-10"
        var startTime = "10:00 AM"
        var endTime = "04:00 PM"
        var venue = "Main Campus Auditorium"
        var organizer = "Student Tech Society"
        var department = "Computer Science & Engineering"
        var odStatus = OdStatus.PENDING.name
        var registrationUrl = ""
        var contactInformation = "events-info@college.edu"

        val lower = rawText.lowercase()

        // Category
        if (lower.contains("hackathon") || lower.contains("code") || lower.contains("coding") || lower.contains("ai") || lower.contains("cyber")) {
            category = "Technical"
        } else if (lower.contains("dance") || lower.contains("music") || lower.contains("fest") || lower.contains("singing") || lower.contains("drama") || lower.contains("cultural")) {
            category = "Cultural"
        } else if (lower.contains("cricket") || lower.contains("football") || lower.contains("sports") || lower.contains("tournament") || lower.contains("badminton")) {
            category = "Sports"
        } else if (lower.contains("workshop") || lower.contains("hands-on") || lower.contains("bootcamp")) {
            category = "Workshop"
        }

        // OD
        if (lower.contains("od available") || lower.contains("on duty available") || lower.contains("attendance granted")) {
            odStatus = OdStatus.AVAILABLE.name
        } else if (lower.contains("no od") || lower.contains("not available")) {
            odStatus = OdStatus.NOT_AVAILABLE.name
        }

        // Venue, title, and metadata extraction
        for (line in lines) {
            val l = line.lowercase()
            if (l.startsWith("event:") || l.startsWith("title:") || l.startsWith("event name:")) {
                title = line.substringAfter(":").trim()
            }
            if (l.startsWith("venue:") || l.startsWith("location:") || l.startsWith("place:")) {
                venue = line.substringAfter(":").trim()
            }
            if (l.startsWith("date:") || l.startsWith("when:")) {
                startDate = line.substringAfter(":").trim()
            }
            if (l.startsWith("time:")) {
                val timeStr = line.substringAfter(":").trim()
                if (timeStr.contains("-")) {
                    startTime = timeStr.substringBefore("-").trim()
                    endTime = timeStr.substringAfter("-").trim()
                } else {
                    startTime = timeStr
                }
            }
            if (l.startsWith("organizer:") || l.startsWith("organized by:") || l.startsWith("club:")) {
                organizer = line.substringAfter(":").trim()
            }
            if (l.startsWith("contact:")) {
                contactInformation = line.substringAfter(":").trim()
            }
            if (l.contains("http://") || l.contains("https://") || l.contains("forms.gle")) {
                val match = Regex("""https?://[^\s]+""").find(line)
                if (match != null) registrationUrl = match.value
            }
        }

        return ExtractedEventDraft(
            title = title,
            description = description,
            category = category,
            startDate = startDate,
            startTime = startTime,
            endTime = endTime,
            venue = venue,
            organizer = organizer,
            department = department,
            odStatus = odStatus,
            registrationUrl = registrationUrl,
            contactInformation = contactInformation
        )
    }

    fun createDraftEntity(draft: ExtractedEventDraft, sourceNote: String = "AI Announcement OCR"): EventEntity {
        val id = "EVT-AI-${UUID.randomUUID().toString().take(6).uppercase()}"
        val now = System.currentTimeMillis()
        return EventEntity(
            eventId = id,
            title = draft.title,
            description = draft.description,
            category = draft.category,
            startDate = draft.startDate,
            startTime = draft.startTime,
            endTime = draft.endTime,
            venue = draft.venue,
            organizer = draft.organizer,
            organizerType = "COMMUNITY",
            department = draft.department,
            odStatus = draft.odStatus,
            registrationRequired = draft.registrationUrl.isNotEmpty(),
            registrationUrl = draft.registrationUrl,
            contactInformation = draft.contactInformation,
            posterUrl = "",
            source = sourceNote,
            sourceType = SourceType.AI_EXTRACTION.name,
            createdBy = "AI Extraction Engine",
            updatedBy = "AI Assistant",
            verifiedBy = "",
            verificationStatus = VerificationStatus.PENDING.name, // Strict mandate: Never allow AI to silently publish
            eventStatus = EventStatus.DRAFT.name, // Strict mandate: AI creates DRAFT EVENT
            createdAt = now,
            updatedAt = now
        )
    }
}
