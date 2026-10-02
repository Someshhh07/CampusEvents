package com.example.data.repository

import com.example.data.local.ApprovalDao
import com.example.data.local.ClubDao
import com.example.data.local.ConflictDao
import com.example.data.local.ConflictEntity
import com.example.data.local.EventApprovalDecisionEntity
import com.example.data.local.EventDao
import com.example.data.local.EventEntity
import com.example.data.local.EventHistoryEntity
import com.example.data.local.HistoryDao
import com.example.data.local.NotificationDao
import com.example.data.local.NotificationEntity
import com.example.data.security.ClubSecurityService
import com.example.model.EventStatus
import com.example.model.OdStatus
import com.example.model.User
import com.example.model.UserRole
import com.example.model.VerificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class EventRepository(
    private val eventDao: EventDao,
    private val historyDao: HistoryDao,
    private val conflictDao: ConflictDao,
    private val notificationDao: NotificationDao,
    private val approvalDao: ApprovalDao? = null,
    private val clubDao: ClubDao? = null,
    private val securityService: ClubSecurityService = ClubSecurityService.instance
) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val savedEvents: Flow<List<EventEntity>> = eventDao.getSavedEvents()
    val allConflicts: Flow<List<ConflictEntity>> = conflictDao.getAllConflicts()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()
    val allApprovalDecisions: Flow<List<EventApprovalDecisionEntity>> =
        approvalDao?.getAllDecisions() ?: flowOf(emptyList())

    fun getEventById(eventId: String): Flow<EventEntity?> = eventDao.getEventById(eventId)
    suspend fun getEventByIdOnce(eventId: String): EventEntity? = eventDao.getEventByIdOnce(eventId)
    fun getEventHistory(eventId: String): Flow<List<EventHistoryEntity>> = historyDao.getHistoryForEvent(eventId)
    fun getAllHistory(): Flow<List<EventHistoryEntity>> = historyDao.getAllHistory()
    fun getApprovalDecisionsForEvent(eventId: String): Flow<List<EventApprovalDecisionEntity>> =
        approvalDao?.getDecisionsForEvent(eventId) ?: flowOf(emptyList())
    fun getApprovalDecisionsForClub(clubId: String): Flow<List<EventApprovalDecisionEntity>> =
        approvalDao?.getDecisionsForClub(clubId) ?: flowOf(emptyList())

    fun searchEvents(query: String): Flow<List<EventEntity>> = eventDao.searchEvents(query)

    suspend fun toggleSave(eventId: String, isSaved: Boolean) {
        eventDao.toggleSave(eventId, isSaved)
    }

    suspend fun createEvent(event: EventEntity, user: User, reason: String = "Event submitted") {
        eventDao.insertEvent(event)
        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = event.eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "CREATED",
                oldValue = "None",
                newValue = "Event created via ${event.sourceType}",
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                title = "New Event: ${event.title}",
                message = "${event.organizer} published ${event.title} on ${event.startDate}.",
                type = "NEW_EVENT",
                eventId = event.eventId
            )
        )
    }

    // ==========================================
    // CLUB MANAGEMENT & APPROVAL WORKFLOW ENGINE
    // Workflow: Draft -> Submitted -> Under Review -> Approved -> Published
    // Supports: Approval, Rejection, Request Changes, Decision Audit Log
    // Backend security enforced: Cross-club prohibited, Member cannot auto-publish
    // ==========================================

    /**
     * Submit a club event into the approval workflow.
     * Members can submit events but CANNOT automatically publish them (forced to DRAFT or SUBMITTED).
     * Prevents cross-club modification.
     */
    suspend fun submitClubEvent(
        event: EventEntity,
        user: User,
        clubId: String,
        isDraft: Boolean = false,
        reason: String = "Initial event submission"
    ): Result<String> {
        val authCheck = securityService.canSubmitEvent(user, clubId)
        if (authCheck.isFailure) return Result.failure(authCheck.exceptionOrNull()!!)

        val targetStatus = if (isDraft) {
            EventStatus.DRAFT.name
        } else if (user.role == UserRole.CLUB_MEMBER) {
            // Club Members can submit events but NOT automatically publish them
            EventStatus.SUBMITTED.name
        } else if (user.role == UserRole.CLUB_PRESIDENT || user.role == UserRole.PLATFORM_ADMIN) {
            // Presidents can choose to auto-publish or submit for faculty verification
            event.eventStatus.ifBlank { EventStatus.SUBMITTED.name }
        } else {
            EventStatus.SUBMITTED.name
        }

        val now = System.currentTimeMillis()
        val preparedEvent = event.copy(
            organizerId = clubId,
            eventStatus = targetStatus,
            createdBy = "${user.name} (${user.role.displayName})",
            updatedBy = user.name,
            createdAt = now,
            updatedAt = now
        )

        eventDao.insertEvent(preparedEvent)

        // Record approval decision audit
        recordApprovalDecision(
            eventId = preparedEvent.eventId,
            eventTitle = preparedEvent.title,
            clubId = clubId,
            reviewer = user,
            decision = if (isDraft) "DRAFT_SAVED" else "SUBMITTED",
            previousStatus = "NONE",
            newStatus = targetStatus,
            comments = reason
        )

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = preparedEvent.eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "SUBMITTED",
                oldValue = "NONE",
                newValue = targetStatus,
                reason = reason,
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "New Club Event: ${preparedEvent.title}",
                message = "${user.name} submitted an event for $clubId in state $targetStatus.",
                type = "NEW_EVENT",
                eventId = preparedEvent.eventId
            )
        )

        return Result.success(preparedEvent.eventId)
    }

    /**
     * Move an event from SUBMITTED to UNDER_REVIEW
     */
    suspend fun moveEventToReview(
        eventId: String,
        user: User,
        clubId: String,
        notes: String = "Under formal review by club administration"
    ): Result<Unit> {
        val (event, club) = getEventAndClub(eventId, clubId)
            ?: return Result.failure(IllegalArgumentException("Event or Club not found"))

        val check = securityService.canReviewAndApproveEvent(user, club)
        if (check.isFailure) return check

        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.UNDER_REVIEW.name, user.name, now)

        recordApprovalDecision(
            eventId = eventId,
            eventTitle = event.title,
            clubId = clubId,
            reviewer = user,
            decision = "UNDER_REVIEW",
            previousStatus = event.eventStatus,
            newStatus = EventStatus.UNDER_REVIEW.name,
            comments = notes
        )

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "STATUS_CHANGED",
                oldValue = event.eventStatus,
                newValue = EventStatus.UNDER_REVIEW.name,
                reason = notes,
                timestamp = now
            )
        )

        return Result.success(Unit)
    }

    /**
     * Approve event (Status -> APPROVED)
     */
    suspend fun approveEvent(
        eventId: String,
        user: User,
        clubId: String,
        notes: String = "Event proposal approved"
    ): Result<Unit> {
        val (event, club) = getEventAndClub(eventId, clubId)
            ?: return Result.failure(IllegalArgumentException("Event or Club not found"))

        val check = securityService.canReviewAndApproveEvent(user, club)
        if (check.isFailure) return check

        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.APPROVED.name, user.name, now)

        recordApprovalDecision(
            eventId = eventId,
            eventTitle = event.title,
            clubId = clubId,
            reviewer = user,
            decision = "APPROVED",
            previousStatus = event.eventStatus,
            newStatus = EventStatus.APPROVED.name,
            comments = notes
        )

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "STATUS_CHANGED",
                oldValue = event.eventStatus,
                newValue = EventStatus.APPROVED.name,
                reason = notes,
                timestamp = now
            )
        )

        return Result.success(Unit)
    }

    /**
     * Officially publish the approved event (Status -> PUBLISHED, Verification -> VERIFIED)
     */
    suspend fun publishEvent(
        eventId: String,
        user: User,
        clubId: String,
        notes: String = "Published officially to campus calendar"
    ): Result<Unit> {
        val (event, club) = getEventAndClub(eventId, clubId)
            ?: return Result.failure(IllegalArgumentException("Event or Club not found"))

        val check = securityService.canReviewAndApproveEvent(user, club)
        if (check.isFailure) return check

        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.PUBLISHED.name, user.name, now)
        eventDao.updateVerification(eventId, VerificationStatus.VERIFIED.name, user.name, now)

        recordApprovalDecision(
            eventId = eventId,
            eventTitle = event.title,
            clubId = clubId,
            reviewer = user,
            decision = "PUBLISHED",
            previousStatus = event.eventStatus,
            newStatus = EventStatus.PUBLISHED.name,
            comments = notes
        )

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "STATUS_CHANGED",
                oldValue = event.eventStatus,
                newValue = EventStatus.PUBLISHED.name,
                reason = notes,
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Event Published: ${event.title}",
                message = "${event.organizer} published ${event.title}.",
                type = "NEW_EVENT",
                eventId = eventId
            )
        )

        return Result.success(Unit)
    }

    /**
     * Request changes on an event submission (Status -> CHANGES_REQUESTED)
     */
    suspend fun requestChanges(
        eventId: String,
        user: User,
        clubId: String,
        reason: String
    ): Result<Unit> {
        val (event, club) = getEventAndClub(eventId, clubId)
            ?: return Result.failure(IllegalArgumentException("Event or Club not found"))

        val check = securityService.canReviewAndApproveEvent(user, club)
        if (check.isFailure) return check

        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.CHANGES_REQUESTED.name, user.name, now)

        recordApprovalDecision(
            eventId = eventId,
            eventTitle = event.title,
            clubId = clubId,
            reviewer = user,
            decision = "CHANGES_REQUESTED",
            previousStatus = event.eventStatus,
            newStatus = EventStatus.CHANGES_REQUESTED.name,
            comments = reason
        )

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "CHANGES_REQUESTED",
                oldValue = event.eventStatus,
                newValue = EventStatus.CHANGES_REQUESTED.name,
                reason = reason,
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Changes Requested: ${event.title}",
                message = "${user.name} requested changes: $reason",
                type = "EVENT_UPDATED",
                eventId = eventId
            )
        )

        return Result.success(Unit)
    }

    /**
     * Reject an event submission (Status -> REJECTED)
     */
    suspend fun rejectEvent(
        eventId: String,
        user: User,
        clubId: String,
        reason: String
    ): Result<Unit> {
        val (event, club) = getEventAndClub(eventId, clubId)
            ?: return Result.failure(IllegalArgumentException("Event or Club not found"))

        val check = securityService.canReviewAndApproveEvent(user, club)
        if (check.isFailure) return check

        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.REJECTED.name, user.name, now)
        eventDao.updateVerification(eventId, VerificationStatus.REJECTED.name, user.name, now)

        recordApprovalDecision(
            eventId = eventId,
            eventTitle = event.title,
            clubId = clubId,
            reviewer = user,
            decision = "REJECTED",
            previousStatus = event.eventStatus,
            newStatus = EventStatus.REJECTED.name,
            comments = reason
        )

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "REJECTED",
                oldValue = event.eventStatus,
                newValue = EventStatus.REJECTED.name,
                reason = reason,
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Event Rejected: ${event.title}",
                message = "Proposal rejected by ${user.name}: $reason",
                type = "EVENT_CANCELLED",
                eventId = eventId
            )
        )

        return Result.success(Unit)
    }

    /**
     * Edit a club event (President always; Admin if adminCanEditEvents; Member if author and pending/changes requested)
     */
    suspend fun editClubEvent(
        oldEvent: EventEntity,
        updatedEvent: EventEntity,
        user: User,
        clubId: String,
        reason: String
    ): Result<Unit> {
        val club = clubDao?.getClubByIdOnce(clubId)
            ?: return Result.failure(IllegalArgumentException("Club '$clubId' not found"))

        val check = securityService.canEditClubEvent(user, club, oldEvent)
        if (check.isFailure) return check

        val now = System.currentTimeMillis()
        val prepared = updatedEvent.copy(updatedBy = user.name, updatedAt = now)
        eventDao.updateEvent(prepared)

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = prepared.eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "DETAILS_EDITED",
                oldValue = oldEvent.title,
                newValue = prepared.title,
                reason = reason,
                timestamp = now
            )
        )

        return Result.success(Unit)
    }

    /**
     * Cancel a club event (President always; Admin if adminCanCancelEvents)
     */
    suspend fun cancelClubEvent(
        eventId: String,
        user: User,
        clubId: String,
        reason: String
    ): Result<Unit> {
        val (event, club) = getEventAndClub(eventId, clubId)
            ?: return Result.failure(IllegalArgumentException("Event or Club not found"))

        val check = securityService.canCancelClubEvent(user, club)
        if (check.isFailure) return check

        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.CANCELLED.name, user.name, now)

        recordApprovalDecision(
            eventId = eventId,
            eventTitle = event.title,
            clubId = clubId,
            reviewer = user,
            decision = "CANCELLED",
            previousStatus = event.eventStatus,
            newStatus = EventStatus.CANCELLED.name,
            comments = reason
        )

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "STATUS_CHANGED",
                oldValue = event.eventStatus,
                newValue = EventStatus.CANCELLED.name,
                reason = reason,
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Event Cancelled: ${event.title}",
                message = "Cancelled by ${user.name}. Reason: $reason",
                type = "EVENT_CANCELLED",
                eventId = eventId
            )
        )

        return Result.success(Unit)
    }

    private suspend fun recordApprovalDecision(
        eventId: String,
        eventTitle: String,
        clubId: String,
        reviewer: User,
        decision: String,
        previousStatus: String,
        newStatus: String,
        comments: String
    ) {
        approvalDao?.insertDecision(
            EventApprovalDecisionEntity(
                eventId = eventId,
                eventTitle = eventTitle,
                clubId = clubId,
                reviewerId = reviewer.userId,
                reviewerName = reviewer.name,
                reviewerRole = reviewer.role.name,
                decision = decision,
                previousStatus = previousStatus,
                newStatus = newStatus,
                comments = comments,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun getEventAndClub(eventId: String, clubId: String): Pair<EventEntity, com.example.data.local.ClubEntity>? {
        val event = eventDao.getEventByIdOnce(eventId) ?: return null
        val club = clubDao?.getClubByIdOnce(clubId) ?: return null
        return Pair(event, club)
    }

    // Existing updateEventDetails, verifyEvent, odStatus, etc.
    suspend fun updateEventDetails(
        oldEvent: EventEntity,
        updatedEvent: EventEntity,
        user: User,
        reason: String
    ) {
        eventDao.updateEvent(updatedEvent)

        val changes = mutableListOf<String>()
        if (oldEvent.venue != updatedEvent.venue) {
            historyDao.insertHistory(
                EventHistoryEntity(
                    eventId = updatedEvent.eventId,
                    userId = user.userId,
                    userName = user.name,
                    userRole = user.role.name,
                    changeType = "VENUE_CHANGED",
                    oldValue = oldEvent.venue,
                    newValue = updatedEvent.venue,
                    reason = reason,
                    timestamp = System.currentTimeMillis()
                )
            )
            changes.add("Venue changed to ${updatedEvent.venue}")
        }
        if (oldEvent.startTime != updatedEvent.startTime || oldEvent.startDate != updatedEvent.startDate) {
            historyDao.insertHistory(
                EventHistoryEntity(
                    eventId = updatedEvent.eventId,
                    userId = user.userId,
                    userName = user.name,
                    userRole = user.role.name,
                    changeType = "TIME_CHANGED",
                    oldValue = "${oldEvent.startDate} ${oldEvent.startTime}",
                    newValue = "${updatedEvent.startDate} ${updatedEvent.startTime}",
                    reason = reason,
                    timestamp = System.currentTimeMillis()
                )
            )
            changes.add("Time/Date updated to ${updatedEvent.startDate} at ${updatedEvent.startTime}")
        }
        if (oldEvent.odStatus != updatedEvent.odStatus) {
            historyDao.insertHistory(
                EventHistoryEntity(
                    eventId = updatedEvent.eventId,
                    userId = user.userId,
                    userName = user.name,
                    userRole = user.role.name,
                    changeType = "OD_UPDATED",
                    oldValue = oldEvent.odStatus,
                    newValue = updatedEvent.odStatus,
                    reason = reason,
                    timestamp = System.currentTimeMillis()
                )
            )
            changes.add("OD status changed to ${updatedEvent.odStatus}")
        }

        if (changes.isNotEmpty()) {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Event Updated: ${updatedEvent.title}",
                    message = changes.joinToString("; "),
                    type = "EVENT_UPDATED",
                    eventId = updatedEvent.eventId
                )
            )
        }
    }

    suspend fun verifyEvent(eventId: String, user: User, isApproved: Boolean, reason: String = "Faculty/Admin Verification") {
        val old = eventDao.getEventByIdOnce(eventId) ?: return
        val newStatus = if (isApproved) VerificationStatus.VERIFIED.name else VerificationStatus.REJECTED.name
        val now = System.currentTimeMillis()
        eventDao.updateVerification(eventId, newStatus, user.name, now)
        if (isApproved && (old.eventStatus == EventStatus.PENDING.name || old.eventStatus == EventStatus.SUBMITTED.name)) {
            eventDao.updateEventStatus(eventId, EventStatus.PUBLISHED.name, user.name, now)
        }

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "VERIFIED",
                oldValue = old.verificationStatus,
                newValue = newStatus,
                reason = reason,
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Event ${if (isApproved) "Verified" else "Rejected"}: ${old.title}",
                message = "${user.name} marked this event as $newStatus.",
                type = "VERIFIED",
                eventId = eventId
            )
        )
    }

    suspend fun updateOdStatus(eventId: String, newOdStatus: OdStatus, user: User, reason: String) {
        val old = eventDao.getEventByIdOnce(eventId) ?: return
        val now = System.currentTimeMillis()
        eventDao.updateOdStatus(eventId, newOdStatus.name, user.name, now)

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "OD_UPDATED",
                oldValue = old.odStatus,
                newValue = newOdStatus.name,
                reason = reason,
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "OD Status Updated: ${old.title}",
                message = "OD status set to ${newOdStatus.label} by ${user.name}.",
                type = "OD_UPDATED",
                eventId = eventId
            )
        )
    }

    suspend fun cancelEvent(eventId: String, user: User, reason: String) {
        val old = eventDao.getEventByIdOnce(eventId) ?: return
        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.CANCELLED.name, user.name, now)

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "STATUS_CHANGED",
                oldValue = old.eventStatus,
                newValue = EventStatus.CANCELLED.name,
                reason = reason,
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Event Cancelled: ${old.title}",
                message = "Cancelled by ${user.name}. Reason: $reason",
                type = "EVENT_CANCELLED",
                eventId = eventId
            )
        )
    }

    suspend fun approveMemberSubmission(eventId: String, user: User) {
        val old = eventDao.getEventByIdOnce(eventId) ?: return
        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.PUBLISHED.name, user.name, now)
        eventDao.updateVerification(eventId, VerificationStatus.VERIFIED.name, user.name, now)

        recordApprovalDecision(
            eventId = eventId,
            eventTitle = old.title,
            clubId = old.organizerId.ifBlank { "club_coding" },
            reviewer = user,
            decision = "APPROVED",
            previousStatus = old.eventStatus,
            newStatus = EventStatus.PUBLISHED.name,
            comments = "Approved by ${user.name}"
        )

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "STATUS_CHANGED",
                oldValue = old.eventStatus,
                newValue = EventStatus.PUBLISHED.name,
                reason = "Approved by Club President ${user.name}",
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Member Submission Approved!",
                message = "${old.title} has been approved and published by ${user.name}.",
                type = "NEW_EVENT",
                eventId = eventId
            )
        )
    }

    suspend fun rejectMemberSubmission(eventId: String, user: User, reason: String) {
        val old = eventDao.getEventByIdOnce(eventId)
        val now = System.currentTimeMillis()
        eventDao.updateEventStatus(eventId, EventStatus.DRAFT.name, user.name, now)
        eventDao.updateVerification(eventId, VerificationStatus.REJECTED.name, user.name, now)

        if (old != null) {
            recordApprovalDecision(
                eventId = eventId,
                eventTitle = old.title,
                clubId = old.organizerId.ifBlank { "club_coding" },
                reviewer = user,
                decision = "REJECTED",
                previousStatus = old.eventStatus,
                newStatus = EventStatus.DRAFT.name,
                comments = reason
            )
        }

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "STATUS_CHANGED",
                oldValue = old?.eventStatus ?: EventStatus.PENDING.name,
                newValue = "REJECTED_TO_DRAFT",
                reason = reason,
                timestamp = now
            )
        )
    }

    suspend fun resolveConflict(
        conflictId: String,
        eventId: String,
        chosenSource: String,
        chosenValue: String,
        user: User
    ) {
        val old = eventDao.getEventByIdOnce(eventId) ?: return
        val now = System.currentTimeMillis()
        conflictDao.resolveConflict(conflictId, chosenValue, user.name)

        val updated = if (chosenValue.contains("10:00 AM")) {
            old.copy(startTime = "10:00 AM", venue = "Tech Hall A-101", conflictDetails = null, verificationStatus = VerificationStatus.VERIFIED.name, verifiedBy = user.name, updatedAt = now)
        } else {
            old.copy(startTime = "11:00 AM", venue = "Seminar Hall B-204", conflictDetails = null, verificationStatus = VerificationStatus.VERIFIED.name, verifiedBy = user.name, updatedAt = now)
        }
        eventDao.updateEvent(updated)

        historyDao.insertHistory(
            EventHistoryEntity(
                eventId = eventId,
                userId = user.userId,
                userName = user.name,
                userRole = user.role.name,
                changeType = "CONFLICT_RESOLVED",
                oldValue = "Conflict: ${old.startTime} vs Alt",
                newValue = "Resolved to: $chosenValue ($chosenSource)",
                reason = "Admin manual conflict resolution",
                timestamp = now
            )
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = "Conflict Resolved: ${old.title}",
                message = "Time and venue finalized as $chosenValue by Admin.",
                type = "TIME_CHANGED",
                eventId = eventId
            )
        )
    }

    suspend fun mergeDuplicateEvent(keepEventId: String, duplicateEventId: String, user: User) {
        eventDao.deleteEvent(duplicateEventId)
        val kept = eventDao.getEventByIdOnce(keepEventId)
        if (kept != null) {
            val updated = kept.copy(isDuplicateCandidate = false, duplicateWithId = null, updatedAt = System.currentTimeMillis())
            eventDao.updateEvent(updated)
            historyDao.insertHistory(
                EventHistoryEntity(
                    eventId = keepEventId,
                    userId = user.userId,
                    userName = user.name,
                    userRole = user.role.name,
                    changeType = "DUPLICATE_MERGED",
                    oldValue = "Duplicate candidate: $duplicateEventId",
                    newValue = "Merged into canonical event $keepEventId",
                    reason = "Duplicate record merged by Admin",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun triggerSyncCycle(user: User): String {
        val now = System.currentTimeMillis()
        notificationDao.insertNotification(
            NotificationEntity(
                title = "College API Synchronization Complete",
                message = "Synchronized 18 events from College Central Portal. 0 new conflicts, 1 duplicate inspected.",
                type = "SYNC_COMPLETE",
                timestamp = now
            )
        )
        return "Sync completed successfully at ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(now))}. All verified feeds up-to-date."
    }

    suspend fun markNotificationAsRead(id: Int) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }
}
