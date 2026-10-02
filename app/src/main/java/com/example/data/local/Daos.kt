package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY startDate ASC, startTime ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE eventId = :eventId LIMIT 1")
    fun getEventById(eventId: String): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE eventId = :eventId LIMIT 1")
    suspend fun getEventByIdOnce(eventId: String): EventEntity?

    @Query("SELECT * FROM events WHERE isSaved = 1 ORDER BY startDate ASC")
    fun getSavedEvents(): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR organizer LIKE '%' || :query || '%' 
           OR venue LIKE '%' || :query || '%' 
           OR department LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%'
        ORDER BY startDate ASC
    """)
    fun searchEvents(query: String): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("UPDATE events SET isSaved = :isSaved WHERE eventId = :eventId")
    suspend fun toggleSave(eventId: String, isSaved: Boolean)

    @Query("UPDATE events SET odStatus = :odStatus, updatedBy = :updatedBy, updatedAt = :updatedAt WHERE eventId = :eventId")
    suspend fun updateOdStatus(eventId: String, odStatus: String, updatedBy: String, updatedAt: Long)

    @Query("UPDATE events SET verificationStatus = :status, verifiedBy = :verifiedBy, updatedAt = :updatedAt WHERE eventId = :eventId")
    suspend fun updateVerification(eventId: String, status: String, verifiedBy: String, updatedAt: Long)

    @Query("UPDATE events SET eventStatus = :status, updatedBy = :updatedBy, updatedAt = :updatedAt WHERE eventId = :eventId")
    suspend fun updateEventStatus(eventId: String, status: String, updatedBy: String, updatedAt: Long)

    @Query("DELETE FROM events WHERE eventId = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("SELECT * FROM events ORDER BY startDate ASC, startTime ASC")
    suspend fun getAllEventsOnce(): List<EventEntity>
}

@Dao
interface CachedEventDao {
    @Query("SELECT * FROM cached_events ORDER BY startDate ASC, startTime ASC")
    fun getAllCachedEvents(): Flow<List<CachedEventEntity>>

    @Query("SELECT * FROM cached_events ORDER BY startDate ASC, startTime ASC")
    suspend fun getAllCachedEventsOnce(): List<CachedEventEntity>

    @Query("SELECT * FROM cached_events WHERE eventId = :eventId LIMIT 1")
    fun getCachedEventById(eventId: String): Flow<CachedEventEntity?>

    @Query("SELECT * FROM cached_events WHERE eventId = :eventId LIMIT 1")
    suspend fun getCachedEventByIdOnce(eventId: String): CachedEventEntity?

    @Query("SELECT * FROM cached_events WHERE isSaved = 1 ORDER BY startDate ASC")
    fun getSavedCachedEvents(): Flow<List<CachedEventEntity>>

    @Query("""
        SELECT * FROM cached_events 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR organizer LIKE '%' || :query || '%' 
           OR venue LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%'
        ORDER BY startDate ASC
    """)
    fun searchCachedEvents(query: String): Flow<List<CachedEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedEvent(event: CachedEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedEvents(events: List<CachedEventEntity>)

    @Update
    suspend fun updateCachedEvent(event: CachedEventEntity)

    @Query("UPDATE cached_events SET isSaved = :isSaved WHERE eventId = :eventId")
    suspend fun toggleSave(eventId: String, isSaved: Boolean)

    @Query("DELETE FROM cached_events WHERE eventId = :eventId")
    suspend fun deleteCachedEvent(eventId: String)

    @Query("DELETE FROM cached_events")
    suspend fun clearAllCachedEvents()

    @Query("SELECT COUNT(*) FROM cached_events")
    suspend fun getCachedCount(): Int
}

@Dao
interface ClubDao {
    @Query("SELECT * FROM clubs ORDER BY name ASC")
    fun getAllClubs(): Flow<List<ClubEntity>>

    @Query("SELECT * FROM clubs WHERE clubId = :clubId LIMIT 1")
    fun getClubById(clubId: String): Flow<ClubEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClub(club: ClubEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClubs(clubs: List<ClubEntity>)

    @Update
    suspend fun updateClub(club: ClubEntity)

    @Query("UPDATE clubs SET isFollowed = :isFollowed WHERE clubId = :clubId")
    suspend fun toggleFollowClub(clubId: String, isFollowed: Boolean)

    @Query("SELECT * FROM club_members WHERE clubId = :clubId ORDER BY roleInClub ASC, userName ASC")
    fun getClubMembers(clubId: String): Flow<List<ClubMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: ClubMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<ClubMemberEntity>)

    @Query("DELETE FROM club_members WHERE id = :id")
    suspend fun deleteMember(id: Int)

    @Query("UPDATE club_members SET status = :status WHERE id = :id")
    suspend fun updateMemberStatus(id: Int, status: String)

    @Query("UPDATE club_members SET roleInClub = :role WHERE id = :id")
    suspend fun updateMemberRole(id: Int, role: String)

    @Query("SELECT * FROM clubs WHERE clubId = :clubId LIMIT 1")
    suspend fun getClubByIdOnce(clubId: String): ClubEntity?

    @Query("SELECT * FROM club_members WHERE clubId = :clubId AND userId = :userId LIMIT 1")
    suspend fun getMemberByUserId(clubId: String, userId: String): ClubMemberEntity?

    @Query("SELECT * FROM club_members WHERE id = :id LIMIT 1")
    suspend fun getMemberByIdOnce(id: Int): ClubMemberEntity?

    @Query("UPDATE clubs SET adminCanEditProfile = :canEditProfile, adminCanManageMembers = :canManageMembers, adminCanApproveMembers = :canApproveMembers, adminCanRemoveMembers = :canRemoveMembers, adminCanReviewEvents = :canReviewEvents, adminCanEditEvents = :canEditEvents, adminCanCancelEvents = :canCancelEvents, adminCanViewEventHistory = :canViewHistory WHERE clubId = :clubId")
    suspend fun updateAdminPermissions(
        clubId: String,
        canEditProfile: Boolean,
        canManageMembers: Boolean,
        canApproveMembers: Boolean,
        canRemoveMembers: Boolean,
        canReviewEvents: Boolean,
        canEditEvents: Boolean,
        canCancelEvents: Boolean,
        canViewHistory: Boolean
    )
}

@Dao
interface ApprovalDao {
    @Query("SELECT * FROM event_approval_decisions WHERE eventId = :eventId ORDER BY timestamp DESC")
    fun getDecisionsForEvent(eventId: String): Flow<List<EventApprovalDecisionEntity>>

    @Query("SELECT * FROM event_approval_decisions WHERE clubId = :clubId ORDER BY timestamp DESC")
    fun getDecisionsForClub(clubId: String): Flow<List<EventApprovalDecisionEntity>>

    @Query("SELECT * FROM event_approval_decisions ORDER BY timestamp DESC LIMIT 100")
    fun getAllDecisions(): Flow<List<EventApprovalDecisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: EventApprovalDecisionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecisions(decisions: List<EventApprovalDecisionEntity>)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM event_history WHERE eventId = :eventId ORDER BY timestamp DESC")
    fun getHistoryForEvent(eventId: String): Flow<List<EventHistoryEntity>>

    @Query("SELECT * FROM event_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<EventHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: EventHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<EventHistoryEntity>)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM event_reports ORDER BY submittedAt DESC")
    fun getAllReports(): Flow<List<EventReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: EventReportEntity)

    @Query("UPDATE event_reports SET status = :status, resolutionNotes = :notes WHERE reportId = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String, notes: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

@Dao
interface ConflictDao {
    @Query("SELECT * FROM conflicts ORDER BY detectedAt DESC")
    fun getAllConflicts(): Flow<List<ConflictEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: ConflictEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflicts(conflicts: List<ConflictEntity>)

    @Query("UPDATE conflicts SET isResolved = 1, resolvedValue = :resolvedValue, resolvedBy = :resolvedBy WHERE conflictId = :conflictId")
    suspend fun resolveConflict(conflictId: String, resolvedValue: String, resolvedBy: String)
}
