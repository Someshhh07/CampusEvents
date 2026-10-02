package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val eventId: String,
    val title: String,
    val description: String,
    val category: String, // Technical, Cultural, Sports, Academic, Workshop, Hackathon
    val subcategory: String = "",
    val startDate: String, // YYYY-MM-DD
    val endDate: String = "",
    val startTime: String, // e.g. 10:00 AM
    val endTime: String = "", // e.g. 05:00 PM
    val venue: String,
    val organizer: String,
    val organizerType: String = "CLUB", // CLUB, DEPARTMENT, COLLEGE, COMMUNITY
    val organizerId: String = "",
    val department: String,
    val odStatus: String = "AVAILABLE", // AVAILABLE, NOT_AVAILABLE, PENDING, NOT_APPLICABLE
    val registrationRequired: Boolean = true,
    val registrationUrl: String = "",
    val contactInformation: String = "",
    val posterUrl: String = "", // e.g. "poster_hackathon", "poster_cultural"
    val source: String, // e.g. "Authorized College Event API v2"
    val sourceType: String = "COLLEGE_API", // COLLEGE_API, COLLEGE_PORTAL, FACULTY, CLUB_PRESIDENT, CLUB_ADMIN, CLUB_MEMBER, COMMUNITY_MEMBER, AI_EXTRACTION
    val sourceUrl: String = "",
    val externalId: String = "",
    val createdBy: String = "System",
    val updatedBy: String = "System",
    val verifiedBy: String = "",
    val verificationStatus: String = "VERIFIED", // UNVERIFIED, PENDING, VERIFIED, CONFLICT, REJECTED
    val eventStatus: String = "PUBLISHED", // DRAFT, PENDING, PUBLISHED, CANCELLED, COMPLETED
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false,
    val conflictDetails: String? = null,
    val isDuplicateCandidate: Boolean = false,
    val duplicateWithId: String? = null
)

@Entity(tableName = "clubs")
data class ClubEntity(
    @PrimaryKey val clubId: String,
    val name: String,
    val tagline: String,
    val description: String,
    val category: String, // Technical, Cultural, Arts, Sports, Academic
    val department: String,
    val leadPresident: String,
    val leadPresidentId: String,
    val memberCount: Int = 1,
    val contactEmail: String,
    val socialLink: String = "",
    val isFollowed: Boolean = false,
    // Configurable Admin Permissions for this club
    val adminCanEditProfile: Boolean = true,
    val adminCanManageMembers: Boolean = true,
    val adminCanApproveMembers: Boolean = true,
    val adminCanRemoveMembers: Boolean = false,
    val adminCanReviewEvents: Boolean = true,
    val adminCanEditEvents: Boolean = true,
    val adminCanCancelEvents: Boolean = false,
    val adminCanViewEventHistory: Boolean = true
)

@Entity(tableName = "club_members")
data class ClubMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clubId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val roleInClub: String, // PRESIDENT, ADMIN, MEMBER
    val status: String = "ACTIVE", // ACTIVE, PENDING_APPROVAL
    val joinedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "event_approval_decisions")
data class EventApprovalDecisionEntity(
    @PrimaryKey(autoGenerate = true) val decisionId: Int = 0,
    val eventId: String,
    val eventTitle: String,
    val clubId: String,
    val reviewerId: String,
    val reviewerName: String,
    val reviewerRole: String, // PRESIDENT, ADMIN, MEMBER
    val decision: String, // SUBMITTED, UNDER_REVIEW, APPROVED, CHANGES_REQUESTED, REJECTED, PUBLISHED
    val previousStatus: String,
    val newStatus: String,
    val comments: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "event_history")
data class EventHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val eventId: String,
    val userId: String,
    val userName: String,
    val userRole: String,
    val changeType: String, // VENUE_CHANGED, TIME_CHANGED, OD_UPDATED, VERIFIED, CREATED, STATUS_CHANGED, SUBMITTED
    val oldValue: String,
    val newValue: String,
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "event_reports")
data class EventReportEntity(
    @PrimaryKey val reportId: String,
    val eventId: String,
    val eventTitle: String,
    val userId: String,
    val userName: String,
    val issueType: String, // Wrong date, Wrong time, Wrong venue, Wrong OD, Wrong description, Registration issue, Event cancelled, Other
    val description: String,
    val status: String = "PENDING", // PENDING, INVESTIGATING, RESOLVED
    val submittedAt: Long = System.currentTimeMillis(),
    val resolutionNotes: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // NEW_EVENT, VENUE_CHANGED, TIME_CHANGED, OD_UPDATED, EVENT_CANCELLED, VERIFIED, REPORT_RESOLVED, CLUB_UPDATE
    val eventId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "conflicts")
data class ConflictEntity(
    @PrimaryKey val conflictId: String,
    val eventId: String,
    val eventTitle: String,
    val field: String, // e.g. "Event Time", "Venue"
    val sourceA: String, // e.g. "Authorized College Event API"
    val valueA: String, // e.g. "10:00 AM"
    val sourceB: String, // e.g. "Club President Submission"
    val valueB: String, // e.g. "11:00 AM"
    val detectedAt: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false,
    val resolvedValue: String = "",
    val resolvedBy: String = ""
)

/**
 * Room database entity schema representing cached events fetched from Firestore.
 * Supports offline-first persistence, cache timestamps, and synchronization metadata.
 */
@Entity(tableName = "cached_events")
data class CachedEventEntity(
    @PrimaryKey val eventId: String,
    val title: String,
    val description: String,
    val category: String,
    val subcategory: String = "",
    val startDate: String,
    val endDate: String = "",
    val startTime: String,
    val endTime: String = "",
    val venue: String,
    val organizer: String,
    val organizerType: String = "CLUB",
    val organizerId: String = "",
    val department: String = "",
    val odStatus: String = "AVAILABLE",
    val registrationRequired: Boolean = true,
    val registrationUrl: String = "",
    val contactInformation: String = "",
    val posterUrl: String = "",
    val source: String = "Firestore",
    val sourceType: String = "COLLEGE_PORTAL",
    val sourceUrl: String = "",
    val externalId: String = "",
    val createdBy: String = "System",
    val updatedBy: String = "System",
    val verifiedBy: String = "",
    val verificationStatus: String = "VERIFIED",
    val eventStatus: String = "PUBLISHED",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false,
    val conflictDetails: String? = null,
    val isDuplicateCandidate: Boolean = false,
    val duplicateWithId: String? = null,
    val cachedAt: Long = System.currentTimeMillis(),
    val isSyncedWithFirestore: Boolean = true
)
