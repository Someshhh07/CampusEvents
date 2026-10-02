package com.example.data.repository

import com.example.data.local.ClubDao
import com.example.data.local.ClubEntity
import com.example.data.local.ClubMemberEntity
import com.example.data.security.ClubSecurityService
import com.example.model.ClubAdminPermissions
import com.example.model.User
import kotlinx.coroutines.flow.Flow

class ClubRepository(
    private val clubDao: ClubDao,
    private val securityService: ClubSecurityService = ClubSecurityService.instance
) {
    val allClubs: Flow<List<ClubEntity>> = clubDao.getAllClubs()

    fun getClubById(clubId: String): Flow<ClubEntity?> = clubDao.getClubById(clubId)
    suspend fun getClubByIdOnce(clubId: String): ClubEntity? = clubDao.getClubByIdOnce(clubId)
    fun getClubMembers(clubId: String): Flow<List<ClubMemberEntity>> = clubDao.getClubMembers(clubId)

    suspend fun toggleFollowClub(clubId: String, isFollowed: Boolean) {
        clubDao.toggleFollowClub(clubId, isFollowed)
    }

    suspend fun addMember(member: ClubMemberEntity) {
        clubDao.insertMember(member)
    }

    // Backend-enforced Member Removal
    suspend fun removeMember(user: User, clubId: String, memberId: Int): Result<Unit> {
        val club = clubDao.getClubByIdOnce(clubId)
            ?: return Result.failure(IllegalArgumentException("Club '$clubId' not found"))
        val targetMember = clubDao.getMemberByIdOnce(memberId)
            ?: return Result.failure(IllegalArgumentException("Member not found"))

        val check = securityService.canRemoveMember(user, club, targetMember)
        if (check.isFailure) return check

        clubDao.deleteMember(memberId)
        return Result.success(Unit)
    }

    // Backend-enforced Member Approval
    suspend fun approveMemberRequest(user: User, clubId: String, memberId: Int): Result<Unit> {
        val club = clubDao.getClubByIdOnce(clubId)
            ?: return Result.failure(IllegalArgumentException("Club '$clubId' not found"))

        val check = securityService.canApproveMembers(user, club)
        if (check.isFailure) return check

        clubDao.updateMemberStatus(memberId, "ACTIVE")
        return Result.success(Unit)
    }

    // Assign Admins: Promote Member -> Admin (President only)
    suspend fun assignAdmin(user: User, clubId: String, memberId: Int): Result<Unit> {
        val check = securityService.canAssignAdmin(user, clubId)
        if (check.isFailure) return check

        clubDao.updateMemberRole(memberId, "ADMIN")
        return Result.success(Unit)
    }

    // Remove Admins: Demote Admin -> Member (President only)
    suspend fun removeAdmin(user: User, clubId: String, memberId: Int): Result<Unit> {
        val check = securityService.canRemoveAdmin(user, clubId)
        if (check.isFailure) return check

        clubDao.updateMemberRole(memberId, "MEMBER")
        return Result.success(Unit)
    }

    // Configure Club Admin Permissions (President only)
    suspend fun updateAdminPermissions(
        user: User,
        clubId: String,
        permissions: ClubAdminPermissions
    ): Result<Unit> {
        val club = clubDao.getClubByIdOnce(clubId)
            ?: return Result.failure(IllegalArgumentException("Club '$clubId' not found"))

        val check = securityService.canConfigureAdminPermissions(user, club)
        if (check.isFailure) return check

        clubDao.updateAdminPermissions(
            clubId = clubId,
            canEditProfile = permissions.canEditProfile,
            canManageMembers = permissions.canManageMembers,
            canApproveMembers = permissions.canApproveMembers,
            canRemoveMembers = permissions.canRemoveMembers,
            canReviewEvents = permissions.canReviewEvents,
            canEditEvents = permissions.canEditEvents,
            canCancelEvents = permissions.canCancelEvents,
            canViewHistory = permissions.canViewEventHistory
        )
        return Result.success(Unit)
    }

    // Backend-enforced Club Profile Update
    suspend fun updateClubProfile(user: User, club: ClubEntity): Result<Unit> {
        val check = securityService.canManageClubProfile(user, club)
        if (check.isFailure) return check

        clubDao.updateClub(club)
        return Result.success(Unit)
    }

    // Direct legacy / non-enforced overloads for compatibility
    suspend fun removeMember(id: Int) {
        clubDao.deleteMember(id)
    }

    suspend fun approveMemberRequest(id: Int) {
        clubDao.updateMemberStatus(id, "ACTIVE")
    }

    suspend fun updateMemberRole(id: Int, role: String) {
        clubDao.updateMemberRole(id, role)
    }

    suspend fun updateClubProfile(club: ClubEntity) {
        clubDao.updateClub(club)
    }
}
