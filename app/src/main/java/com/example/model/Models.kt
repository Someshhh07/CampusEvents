package com.example.model

enum class UserRole(val displayName: String) {
    STUDENT("Student"),
    COMMUNITY_MEMBER("Community Member"),
    CLUB_MEMBER("Club Member"),
    CLUB_ADMIN("Club Admin"),
    CLUB_PRESIDENT("Club President"),
    FACULTY("Faculty Coordinator"),
    PLATFORM_ADMIN("Platform Administrator")
}

enum class OdStatus(val label: String, val shortLabel: String) {
    AVAILABLE("OD Available", "✓ OD"),
    NOT_AVAILABLE("No OD", "No OD"),
    PENDING("OD Pending", "OD Pending"),
    NOT_APPLICABLE("OD N/A", "N/A")
}

enum class VerificationStatus(val label: String) {
    UNVERIFIED("Unverified"),
    PENDING("Pending Verification"),
    VERIFIED("✓ Verified"),
    CONFLICT("⚠ Conflict Detected"),
    REJECTED("Rejected")
}

enum class EventStatus(val label: String) {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    APPROVED("Approved"),
    PUBLISHED("Published"),
    CHANGES_REQUESTED("Changes Requested"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    PENDING("Pending Approval")
}

data class ClubAdminPermissions(
    val canEditProfile: Boolean = true,
    val canManageMembers: Boolean = true,
    val canApproveMembers: Boolean = true,
    val canRemoveMembers: Boolean = false,
    val canReviewEvents: Boolean = true,
    val canEditEvents: Boolean = true,
    val canCancelEvents: Boolean = false,
    val canViewEventHistory: Boolean = true
)

enum class SourceType(val label: String) {
    COLLEGE_API("College Official API"),
    COLLEGE_PORTAL("College Portal Feed"),
    FACULTY("Faculty Notice"),
    CLUB_PRESIDENT("Club President"),
    CLUB_ADMIN("Club Admin"),
    CLUB_MEMBER("Club Member Submission"),
    COMMUNITY_MEMBER("Community Contributor"),
    AI_EXTRACTION("AI Extraction (Poster/Notice)")
}

data class User(
    val userId: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val department: String,
    val clubAffiliation: String? = null,
    val clubRole: UserRole? = null,
    val isAnonymous: Boolean = false,
    val photoUrl: String? = null,
    val authProvider: String = "Campus Directory"
)
