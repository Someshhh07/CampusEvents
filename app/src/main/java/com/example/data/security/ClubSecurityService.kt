package com.example.data.security

import com.example.data.local.ClubEntity
import com.example.data.local.ClubMemberEntity
import com.example.data.local.EventEntity
import com.example.model.ClubAdminPermissions
import com.example.model.EventStatus
import com.example.model.User
import com.example.model.UserRole

/**
 * Backend permission & authorization engine enforcing:
 * 1. Strict Hierarchy: Club President -> Club Admin -> Club Member
 * 2. Cross-Club Protection: A club member cannot modify another club's data
 * 3. Configurable Club Admin permissions (managed by Club President)
 * 4. Club Member event submission without auto-publish
 * 5. Event Approval Workflow enforcement
 */
class ClubSecurityService {

    companion object {
        val instance: ClubSecurityService by lazy { ClubSecurityService() }
    }

    /**
     * Verify whether a user is authorized to perform operations on the specified club.
     * Central Platform Admins can administer all clubs.
     * Club Presidents, Admins, and Members are strictly scoped to their own club affiliation.
     */
    fun checkClubAffiliation(user: User, targetClubId: String): Result<Unit> {
        if (user.role == UserRole.PLATFORM_ADMIN) {
            return Result.success(Unit)
        }
        val userClub = user.clubAffiliation
        if (userClub.isNullOrBlank() || !userClub.equals(targetClubId, ignoreCase = true)) {
            return Result.failure(
                SecurityException(
                    "Access Denied: You are not authorized to modify data for club '$targetClubId'. Cross-club modifications are prohibited."
                )
            )
        }
        return Result.success(Unit)
    }

    /**
     * Check if user can manage club profile:
     * - Club President: Yes
     * - Club Admin: Only if club.adminCanEditProfile == true
     * - Club Member: No
     */
    fun canManageClubProfile(user: User, club: ClubEntity): Result<Unit> {
        checkClubAffiliation(user, club.clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        if (user.role == UserRole.CLUB_ADMIN) {
            return if (club.adminCanEditProfile) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Admin permission denied: The Club President has disabled profile editing for Club Admins."))
            }
        }
        return Result.failure(SecurityException("Club Members do not have permission to edit the club profile."))
    }

    /**
     * Check if user can configure admin permissions:
     * - Only Club President (and Platform Admin) can configure admin permissions
     */
    fun canConfigureAdminPermissions(user: User, club: ClubEntity): Result<Unit> {
        checkClubAffiliation(user, club.clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        return Result.failure(SecurityException("Only the Club President can configure Club Admin permissions."))
    }

    /**
     * Check if user can approve new club membership requests:
     * - Club President: Yes
     * - Club Admin: If club.adminCanApproveMembers == true
     * - Club Member: No
     */
    fun canApproveMembers(user: User, club: ClubEntity): Result<Unit> {
        checkClubAffiliation(user, club.clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        if (user.role == UserRole.CLUB_ADMIN) {
            return if (club.adminCanApproveMembers) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Admin permission denied: The Club President has disabled member approvals for Club Admins."))
            }
        }
        return Result.failure(SecurityException("Club Members cannot approve membership requests."))
    }

    /**
     * Check if user can remove members:
     * - Cannot remove the President
     * - Removing Admins: Only President
     * - Removing standard Members: President always; Admin only if club.adminCanRemoveMembers == true
     */
    fun canRemoveMember(user: User, club: ClubEntity, targetMember: ClubMemberEntity): Result<Unit> {
        checkClubAffiliation(user, club.clubId).onFailure { return Result.failure(it) }
        if (targetMember.roleInClub.equals("PRESIDENT", ignoreCase = true)) {
            if (user.role == UserRole.PLATFORM_ADMIN) return Result.success(Unit)
            return Result.failure(SecurityException("The Club President cannot be removed."))
        }
        if (targetMember.roleInClub.equals("ADMIN", ignoreCase = true)) {
            if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
                return Result.success(Unit)
            }
            return Result.failure(SecurityException("Only the Club President can remove or demote Club Admins."))
        }
        // Removing standard Member
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        if (user.role == UserRole.CLUB_ADMIN) {
            return if (club.adminCanRemoveMembers) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Admin permission denied: The Club President has disabled member removal for Club Admins."))
            }
        }
        return Result.failure(SecurityException("Club Members do not have permission to remove members."))
    }

    /**
     * Assign admins (promote member to admin):
     * - Only Club President
     */
    fun canAssignAdmin(user: User, clubId: String): Result<Unit> {
        checkClubAffiliation(user, clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        return Result.failure(SecurityException("Only the Club President can assign Club Admins."))
    }

    /**
     * Remove admins (demote admin to member):
     * - Only Club President
     */
    fun canRemoveAdmin(user: User, clubId: String): Result<Unit> {
        checkClubAffiliation(user, clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        return Result.failure(SecurityException("Only the Club President can remove Club Admins."))
    }

    /**
     * Check if user can submit an event for the club:
     * - Club Member, Club Admin, or Club President of this club
     * - Cross-club submission rejected
     */
    fun canSubmitEvent(user: User, clubId: String): Result<Unit> {
        checkClubAffiliation(user, clubId).onFailure { return Result.failure(it) }
        val isAffiliated = user.role == UserRole.CLUB_MEMBER ||
                user.role == UserRole.CLUB_ADMIN ||
                user.role == UserRole.CLUB_PRESIDENT ||
                user.role == UserRole.PLATFORM_ADMIN
        return if (isAffiliated) {
            Result.success(Unit)
        } else {
            Result.failure(SecurityException("Only active club members, admins, or presidents can submit events."))
        }
    }

    /**
     * Check if user can review, approve, request changes, or reject events:
     * - Club President: Yes
     * - Club Admin: If club.adminCanReviewEvents == true
     * - Club Member: No
     */
    fun canReviewAndApproveEvent(user: User, club: ClubEntity): Result<Unit> {
        checkClubAffiliation(user, club.clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        if (user.role == UserRole.CLUB_ADMIN) {
            return if (club.adminCanReviewEvents) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Admin permission denied: The Club President has disabled event review and approvals for Club Admins."))
            }
        }
        return Result.failure(SecurityException("Club Members are not authorized to approve or review events."))
    }

    /**
     * Check if user can edit club events:
     * - Club President: Yes
     * - Club Admin: If club.adminCanEditEvents == true
     * - Club Member: Only their own unapproved submission (draft or changes-requested)
     */
    fun canEditClubEvent(user: User, club: ClubEntity, event: EventEntity): Result<Unit> {
        checkClubAffiliation(user, club.clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        if (user.role == UserRole.CLUB_ADMIN) {
            return if (club.adminCanEditEvents) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Admin permission denied: The Club President has disabled event editing for Club Admins."))
            }
        }
        if (user.role == UserRole.CLUB_MEMBER) {
            val isAuthor = event.createdBy.contains(user.name) || event.createdBy.contains(user.userId)
            val isEditableStatus = event.eventStatus == EventStatus.DRAFT.name ||
                    event.eventStatus == EventStatus.CHANGES_REQUESTED.name ||
                    event.eventStatus == EventStatus.SUBMITTED.name
            if (isAuthor && isEditableStatus) {
                return Result.success(Unit)
            }
            return Result.failure(SecurityException("Club Members can only edit their own draft or pending event submissions."))
        }
        return Result.failure(SecurityException("Unauthorized to edit this event."))
    }

    /**
     * Check if user can cancel club events:
     * - Club President: Yes
     * - Club Admin: If club.adminCanCancelEvents == true
     * - Club Member: No
     */
    fun canCancelClubEvent(user: User, club: ClubEntity): Result<Unit> {
        checkClubAffiliation(user, club.clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        if (user.role == UserRole.CLUB_ADMIN) {
            return if (club.adminCanCancelEvents) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Admin permission denied: The Club President has disabled event cancellation for Club Admins."))
            }
        }
        return Result.failure(SecurityException("Club Members are not authorized to cancel club events."))
    }

    /**
     * Check if user can view event history:
     * - Club President: Yes
     * - Club Admin: If club.adminCanViewEventHistory == true
     * - Club Member: Basic view only
     */
    fun canViewEventHistory(user: User, club: ClubEntity): Result<Unit> {
        checkClubAffiliation(user, club.clubId).onFailure { return Result.failure(it) }
        if (user.role == UserRole.PLATFORM_ADMIN || user.role == UserRole.CLUB_PRESIDENT) {
            return Result.success(Unit)
        }
        if (user.role == UserRole.CLUB_ADMIN) {
            return if (club.adminCanViewEventHistory) {
                Result.success(Unit)
            } else {
                Result.failure(SecurityException("Admin permission denied: Event history viewing is not enabled for Club Admins."))
            }
        }
        return Result.failure(SecurityException("Club Members cannot view full administrative event history."))
    }
}
