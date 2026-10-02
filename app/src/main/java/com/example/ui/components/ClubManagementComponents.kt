package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ClubEntity
import com.example.data.local.EventApprovalDecisionEntity
import com.example.model.ClubAdminPermissions
import com.example.model.EventStatus
import com.example.ui.theme.AmberPending
import com.example.ui.theme.AmberPendingLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.IndigoBadge
import com.example.ui.theme.IndigoBadgeLight
import com.example.ui.theme.RoseConflict
import com.example.ui.theme.RoseConflictLight
import com.example.ui.theme.RoyalBlue600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Visual multi-step tracker showing:
 * Draft -> Submitted -> Under Review -> Approved -> Published
 */
@Composable
fun ApprovalWorkflowTracker(
    currentStatus: String,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        EventStatus.DRAFT.name to "Draft",
        EventStatus.SUBMITTED.name to "Submitted",
        EventStatus.UNDER_REVIEW.name to "Under Review",
        EventStatus.APPROVED.name to "Approved",
        EventStatus.PUBLISHED.name to "Published"
    )

    val currentNormalized = when (currentStatus.uppercase()) {
        "DRAFT" -> 0
        "SUBMITTED", "PENDING" -> 1
        "UNDER_REVIEW" -> 2
        "APPROVED" -> 3
        "PUBLISHED" -> 4
        "CHANGES_REQUESTED" -> 1 // treated as needing revision back at submitted/review stage
        "REJECTED", "CANCELLED" -> -1
        else -> 1
    }

    AppleCard(modifier = modifier.testTag("approval_workflow_tracker")) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Approval Workflow Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (currentStatus.uppercase()) {
                        "PUBLISHED" -> EmeraldGreenLight
                        "APPROVED" -> EmeraldGreenLight
                        "UNDER_REVIEW" -> IndigoBadgeLight
                        "SUBMITTED", "PENDING" -> AmberPendingLight
                        "CHANGES_REQUESTED" -> AmberPendingLight
                        "REJECTED", "CANCELLED" -> RoseConflictLight
                        else -> Slate100
                    }
                ) {
                    Text(
                        text = currentStatus.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (currentStatus.uppercase()) {
                            "PUBLISHED", "APPROVED" -> EmeraldGreen
                            "UNDER_REVIEW" -> IndigoBadge
                            "SUBMITTED", "PENDING", "CHANGES_REQUESTED" -> AmberPending
                            "REJECTED", "CANCELLED" -> RoseConflict
                            else -> Slate700
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (currentStatus.uppercase() in listOf("REJECTED", "CANCELLED")) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoseConflictLight, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = RoseConflict)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "This proposal has been marked as ${currentStatus.replace("_", " ")}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoseConflict,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, (key, label) ->
                        val isCompleted = index <= currentNormalized
                        val isCurrent = index == currentNormalized

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (isCompleted) RoyalBlue600 else Slate200,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 10.sp,
                                        color = Slate500,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) RoyalBlue600 else if (isCompleted) Slate900 else Slate400,
                                maxLines = 1
                            )
                        }

                        if (index < steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .height(2.dp)
                                    .background(if (index < currentNormalized) RoyalBlue600 else Slate200)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card rendering an official EventApprovalDecisionEntity audit record.
 */
@Composable
fun ApprovalDecisionCard(
    decision: EventApprovalDecisionEntity,
    modifier: Modifier = Modifier
) {
    val dateStr = try {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        sdf.format(Date(decision.timestamp))
    } catch (e: Exception) {
        "Recorded"
    }

    val decisionColor = when (decision.decision.uppercase()) {
        "APPROVED" -> EmeraldGreen
        "PUBLISHED" -> RoyalBlue600
        "UNDER_REVIEW" -> IndigoBadge
        "SUBMITTED" -> AmberPending
        "CHANGES_REQUESTED" -> AmberPending
        "REJECTED", "CANCELLED" -> RoseConflict
        else -> Slate700
    }

    val decisionBg = when (decision.decision.uppercase()) {
        "APPROVED" -> EmeraldGreenLight
        "PUBLISHED" -> IndigoBadgeLight
        "UNDER_REVIEW" -> IndigoBadgeLight
        "SUBMITTED", "CHANGES_REQUESTED" -> AmberPendingLight
        "REJECTED", "CANCELLED" -> RoseConflictLight
        else -> Slate100
    }

    AppleCard(modifier = modifier.testTag("approval_decision_${decision.decisionId}")) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = decisionBg,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (decision.decision.uppercase()) {
                                    "APPROVED", "PUBLISHED" -> Icons.Default.CheckCircle
                                    "CHANGES_REQUESTED" -> Icons.Default.RateReview
                                    "REJECTED", "CANCELLED" -> Icons.Default.Close
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = decisionColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = decision.reviewerName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${decision.reviewerRole} • $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = decisionBg
                ) {
                    Text(
                        text = decision.decision.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = decisionColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${decision.previousStatus} ➔ ${decision.newStatus}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )

            if (decision.comments.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"${decision.comments}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog to submit an event as a Club Member (without auto-publishing)
 */
@Composable
fun SubmitClubEventProposalDialog(
    club: ClubEntity,
    onSubmit: (title: String, desc: String, category: String, date: String, sTime: String, eTime: String, venue: String, isDraft: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technical") }
    var date by remember { mutableStateOf("2026-10-15") }
    var startTime by remember { mutableStateOf("10:00 AM") }
    var endTime by remember { mutableStateOf("01:00 PM") }
    var venue by remember { mutableStateOf("Seminar Hall A") }

    val categories = listOf("Technical", "Cultural", "Sports", "Workshop", "Hackathon")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Submit Event Proposal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Proposals are submitted to ${club.name} administration for review. They are not auto-published.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Event Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        OutlinedButton(
                            onClick = { category = cat },
                            colors = if (category == cat) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(cat, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Proposed Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = { Text("Target Venue") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    OutlinedButton(
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank()) {
                                onSubmit(title, description, category, date, startTime, endTime, venue, true)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Draft")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank()) {
                                onSubmit(title, description, category, date, startTime, endTime, venue, false)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Submit for Review")
                    }
                }
            }
        }
    }
}

/**
 * Dialog to configure Club Admin permissions (President only)
 */
@Composable
fun ConfigAdminPermissionsDialog(
    club: ClubEntity,
    onSave: (ClubAdminPermissions) -> Unit,
    onDismiss: () -> Unit
) {
    var canEditProfile by remember { mutableStateOf(club.adminCanEditProfile) }
    var canManageMembers by remember { mutableStateOf(club.adminCanManageMembers) }
    var canApproveMembers by remember { mutableStateOf(club.adminCanApproveMembers) }
    var canRemoveMembers by remember { mutableStateOf(club.adminCanRemoveMembers) }
    var canReviewEvents by remember { mutableStateOf(club.adminCanReviewEvents) }
    var canEditEvents by remember { mutableStateOf(club.adminCanEditEvents) }
    var canCancelEvents by remember { mutableStateOf(club.adminCanCancelEvents) }
    var canViewHistory by remember { mutableStateOf(club.adminCanViewEventHistory) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Configure Admin Permissions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Customize the operational permissions granted to Club Admins in ${club.name}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                PermissionSwitchRow("Edit Club Profile", "Allow admins to update info & contacts", canEditProfile) { canEditProfile = it }
                PermissionSwitchRow("Manage Members", "Allow adding members to the roster", canManageMembers) { canManageMembers = it }
                PermissionSwitchRow("Approve Members", "Allow approving pending member requests", canApproveMembers) { canApproveMembers = it }
                PermissionSwitchRow("Remove Members", "Allow removing inactive members", canRemoveMembers) { canRemoveMembers = it }
                PermissionSwitchRow("Review & Approve Events", "Allow approving event workflow proposals", canReviewEvents) { canReviewEvents = it }
                PermissionSwitchRow("Edit Club Events", "Allow updating venue, timings, details", canEditEvents) { canEditEvents = it }
                PermissionSwitchRow("Cancel Club Events", "Allow cancelling scheduled events", canCancelEvents) { canCancelEvents = it }
                PermissionSwitchRow("View Event History", "Allow viewing the complete audit timeline", canViewHistory) { canViewHistory = it }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                ClubAdminPermissions(
                                    canEditProfile = canEditProfile,
                                    canManageMembers = canManageMembers,
                                    canApproveMembers = canApproveMembers,
                                    canRemoveMembers = canRemoveMembers,
                                    canReviewEvents = canReviewEvents,
                                    canEditEvents = canEditEvents,
                                    canCancelEvents = canCancelEvents,
                                    canViewEventHistory = canViewHistory
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Permissions")
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.labelSmall, color = Slate500)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = RoyalBlue600)
        )
    }
    HorizontalDivider(color = Slate100, modifier = Modifier.padding(vertical = 2.dp))
}

/**
 * Dialog to edit club profile (President or permitted Admin)
 */
@Composable
fun EditClubProfileDialog(
    club: ClubEntity,
    onSave: (ClubEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(club.name) }
    var description by remember { mutableStateOf(club.description) }
    var department by remember { mutableStateOf(club.department) }
    var email by remember { mutableStateOf(club.contactEmail) }
    var socialLink by remember { mutableStateOf(club.socialLink) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Manage Club Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Club Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = socialLink,
                    onValueChange = { socialLink = it },
                    label = { Text("Social / Portal Link") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                club.copy(
                                    name = name,
                                    description = description,
                                    department = department,
                                    contactEmail = email,
                                    socialLink = socialLink
                                )
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}

/**
 * Dialog to request changes on an event proposal
 */
@Composable
fun RequestChangesDialog(
    eventTitle: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("Please provide clearer venue capacity and registration timeline.") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Request Changes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Provide feedback for the event author regarding '$eventTitle'. The status will update to 'Changes Requested'.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Requested Changes / Notes *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (reason.isNotBlank()) onSubmit(reason)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPending),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Send Feedback")
                    }
                }
            }
        }
    }
}

/**
 * Dialog to reject an event proposal
 */
@Composable
fun RejectProposalDialog(
    eventTitle: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("Proposal does not align with current club schedule or budget constraints.") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Reject Event Proposal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RoseConflict
                )
                Text(
                    text = "This will reject '$eventTitle' and record the decision in the club audit log.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Rejection Reason *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (reason.isNotBlank()) onSubmit(reason)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseConflict),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reject Proposal")
                    }
                }
            }
        }
    }
}
