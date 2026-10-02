package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.EventEntity
import com.example.data.local.EventHistoryEntity
import com.example.model.EventStatus
import com.example.model.OdStatus
import com.example.model.UserRole
import com.example.model.VerificationStatus
import com.example.ui.components.AppleCard
import com.example.ui.components.ApprovalDecisionCard
import com.example.ui.components.ApprovalWorkflowTracker
import com.example.ui.components.CategoryBadge
import com.example.ui.components.OdBadge
import com.example.ui.components.RejectProposalDialog
import com.example.ui.components.ReportIncorrectInfoDialog
import com.example.ui.components.RequestChangesDialog
import com.example.ui.components.VerificationBadge
import com.example.ui.components.launchAddToCalendar
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
import com.example.ui.viewmodel.CollegeEventsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    event: EventEntity,
    viewModel: CollegeEventsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val eventHistory by viewModel.selectedEventHistory.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var showHistoryExpanded by remember { mutableStateOf(true) }

    // Faculty modal controls
    var showFacultyOdDialog by remember { mutableStateOf(false) }
    var showFacultyUpdateDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val isFaculty = currentUser.role == UserRole.FACULTY || currentUser.role == UserRole.PLATFORM_ADMIN
    val isClubPresident = currentUser.role == UserRole.CLUB_PRESIDENT && currentUser.clubAffiliation == event.organizerId
    val isAuthorizedLeader = (currentUser.role == UserRole.CLUB_PRESIDENT || currentUser.role == UserRole.CLUB_ADMIN || currentUser.role == UserRole.PLATFORM_ADMIN) &&
            (currentUser.clubAffiliation == event.organizerId || currentUser.role == UserRole.PLATFORM_ADMIN)
    val isCrossClubUser = (currentUser.role == UserRole.CLUB_PRESIDENT || currentUser.role == UserRole.CLUB_ADMIN) &&
            currentUser.clubAffiliation != null && currentUser.clubAffiliation != event.organizerId

    val approvalDecisions by viewModel.selectedEventApprovalDecisions.collectAsState()
    var showRequestChangesDialog by remember { mutableStateOf(false) }
    var showRejectProposalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("event_details_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Event Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("event_details_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { launchAddToCalendar(context, event) },
                    modifier = Modifier.testTag("event_details_calendar_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Add to Calendar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) },
                    modifier = Modifier.testTag("event_details_save_button")
                ) {
                    Icon(
                        imageVector = if (event.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (event.isSaved) RoyalBlue600 else MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Scrollable Event Details
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // 1. Hero Poster Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                val imageRes = when (event.posterUrl) {
                    "poster_cultural" -> R.drawable.poster_cultural
                    "poster_hackathon" -> R.drawable.poster_hackathon
                    else -> R.drawable.poster_hackathon
                }

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0x99000000), Color(0xEE000000)),
                                startY = 60f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(18.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoryBadge(category = event.category)
                        VerificationBadge(status = event.verificationStatus)
                        OdBadge(status = event.odStatus)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                // Event Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Conflict Banner if detected
                if (event.verificationStatus == VerificationStatus.CONFLICT.name && !event.conflictDetails.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RoseConflictLight,
                        border = BorderStroke(1.dp, RoseConflict),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RoseConflict)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DATA CONFLICT DETECTED",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseConflict
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.conflictDetails,
                                style = MaterialTheme.typography.bodySmall,
                                color = RoseConflict
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Approval Workflow Status Tracker
                ApprovalWorkflowTracker(
                    currentStatus = event.eventStatus,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 2. IMMEDIATELY VISIBLE KEY INFORMATION (Date, Time, Venue, OD, Organizer, Registration, Verification)
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                KeyInformationSummaryCard(
                    event = event,
                    onRegisterClick = {
                        if (event.registrationUrl.isNotEmpty()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.registrationUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Primary Action Buttons (Add to Calendar, Register, Bookmark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { launchAddToCalendar(context, event) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_to_calendar_button")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Calendar")
                    }

                    if (event.registrationUrl.isNotEmpty()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.registrationUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("register_now_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Register")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { showReportDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Report Issue")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. About This Event Description
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 5. VISUAL TIMELINE FOR EVENT CHANGES
                Text(
                    text = "Activity History",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Verified updates and administrative modifications",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                VisualEventChangeTimeline(
                    event = event,
                    history = eventHistory
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 6. Data Provenance & Pedigree Card
                Text(
                    text = "Information Source",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Data origins and verification pedigree",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AppleCard(backgroundColor = MaterialTheme.colorScheme.surface) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProvenanceRow("Data Source", event.source)
                        ProvenanceRow("Source Type", event.sourceType)
                        ProvenanceRow("Event Owner", event.organizer)
                        ProvenanceRow("Created By", event.createdBy)
                        ProvenanceRow("Last Updated By", event.updatedBy)
                        ProvenanceRow(
                            "Verified By",
                            if (event.verifiedBy.isNotEmpty()) "✓ ${event.verifiedBy}" else "Pending Verification"
                        )
                        ProvenanceRow(
                            "Last Updated",
                            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(event.updatedAt))
                        )
                        ProvenanceRow("Inquiries", event.contactInformation)
                    }
                }

                // 7. Secondary Action: Report Incorrect Information
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.testTag("report_button")
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Report Inaccurate Details", color = MaterialTheme.colorScheme.error)
                    }
                }

                // 8. Faculty Coordinator Controls
                if (isFaculty) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Faculty Coordinator Controls",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You have coordinator permissions to verify, update OD, change venue, or cancel this event.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.facultyVerifyEvent(event.eventId, true, "Verified by Faculty Coordinator")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("faculty_verify_button")
                        ) {
                            Text("✓ Verify Event")
                        }

                        OutlinedButton(
                            onClick = { showFacultyOdDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("faculty_od_button")
                        ) {
                            Text("Update OD")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFacultyUpdateDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit Venue/Time")
                        }

                        Button(
                            onClick = { showCancelDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseConflict),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel Event")
                        }
                    }
                }

                // 9. Club Governance & Approval Workflow Section
                if (isCrossClubUser) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AmberPendingLight,
                        border = BorderStroke(1.dp, AmberPending),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPending)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Cross-Club Protection Active", fontWeight = FontWeight.Bold, color = Slate900, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "This event belongs to ${event.organizer}. As a member/leader of ${currentUser.clubAffiliation}, you cannot modify or approve another club's events.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }
                        }
                    }
                } else if (isAuthorizedLeader) {
                    Spacer(modifier = Modifier.height(18.dp))
                    AppleCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Club Approval Workflow",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Role: ${currentUser.role.name} • Submitted by: ${event.createdBy}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (event.eventStatus.uppercase()) {
                                        "PUBLISHED" -> EmeraldGreenLight
                                        "APPROVED" -> EmeraldGreenLight
                                        "UNDER_REVIEW" -> IndigoBadgeLight
                                        "SUBMITTED", "PENDING" -> AmberPendingLight
                                        "CHANGES_REQUESTED" -> AmberPendingLight
                                        else -> RoseConflictLight
                                    }
                                ) {
                                    Text(
                                        text = event.eventStatus.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (event.eventStatus.uppercase()) {
                                            "PUBLISHED", "APPROVED" -> EmeraldGreen
                                            "UNDER_REVIEW" -> IndigoBadge
                                            "SUBMITTED", "PENDING", "CHANGES_REQUESTED" -> AmberPending
                                            else -> RoseConflict
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons according to workflow
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (event.eventStatus.uppercase()) {
                                    "DRAFT", "SUBMITTED", "PENDING" -> {
                                        OutlinedButton(
                                            onClick = { viewModel.moveEventToReview(event.eventId, event.organizerId) },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Review", fontSize = 12.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.approveClubEvent(event.eventId, event.organizerId) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("club_approve_button")
                                        ) {
                                            Text("Approve", fontSize = 12.sp)
                                        }
                                    }
                                    "UNDER_REVIEW" -> {
                                        Button(
                                            onClick = { viewModel.approveClubEvent(event.eventId, event.organizerId) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("club_approve_button")
                                        ) {
                                            Text("Approve Proposal", fontSize = 12.sp)
                                        }
                                    }
                                    "APPROVED" -> {
                                        Button(
                                            onClick = { viewModel.publishClubEvent(event.eventId, event.organizerId) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue600),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("club_publish_button")
                                        ) {
                                            Text("Publish Campus-Wide", fontSize = 12.sp)
                                        }
                                    }
                                    else -> {}
                                }

                                OutlinedButton(
                                    onClick = { showRequestChangesDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Request Changes", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { showRejectProposalDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reject", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 10. Recorded Approval Decisions Audit History
                if (approvalDecisions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Approval Decisions Audit Log (${approvalDecisions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chronological record of every decision and comment made for this event.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    approvalDecisions.forEach { decision ->
                        ApprovalDecisionCard(
                            decision = decision,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Report Incorrect Information Modal
    if (showReportDialog) {
        ReportIncorrectInfoDialog(
            event = event,
            onSubmit = { issue, desc ->
                viewModel.submitReport(event.eventId, event.title, issue, desc)
            },
            onDismiss = { showReportDialog = false }
        )
    }

    // Faculty OD Update Dialog
    if (showFacultyOdDialog) {
        Dialog(onDismissRequest = { showFacultyOdDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Update OD (On-Duty) Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OdStatus.values().forEach { od ->
                        Button(
                            onClick = {
                                viewModel.facultyUpdateOd(event.eventId, od, "Faculty academic review")
                                showFacultyOdDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(od.label)
                        }
                    }
                }
            }
        }
    }

    // Faculty Venue & Time Dialog
    if (showFacultyUpdateDialog) {
        var venue by remember { mutableStateOf(event.venue) }
        var date by remember { mutableStateOf(event.startDate) }
        var time by remember { mutableStateOf(event.startTime) }
        var reason by remember { mutableStateOf("Department schedule coordination") }

        Dialog(onDismissRequest = { showFacultyUpdateDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Update Venue & Timing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Start Time") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason for change") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showFacultyUpdateDialog = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                viewModel.facultyUpdateVenueAndTime(event.eventId, venue, date, time, reason)
                                showFacultyUpdateDialog = false
                            }
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    // Cancel Event Dialog
    if (showCancelDialog) {
        var reason by remember { mutableStateOf("Inclement weather / administrative directive") }
        Dialog(onDismissRequest = { showCancelDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Cancel Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = RoseConflict)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This will notify all registered students and mark the event as CANCELLED.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Cancellation Reason") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCancelDialog = false }) { Text("Close") }
                        Button(
                            onClick = {
                                viewModel.facultyCancelEvent(event.eventId, reason)
                                showCancelDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoseConflict)
                        ) {
                            Text("Confirm Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showRequestChangesDialog) {
        RequestChangesDialog(
            eventTitle = event.title,
            onSubmit = { reason ->
                viewModel.requestChangesOnClubEvent(event.eventId, event.organizerId, reason)
                showRequestChangesDialog = false
            },
            onDismiss = { showRequestChangesDialog = false }
        )
    }

    if (showRejectProposalDialog) {
        RejectProposalDialog(
            eventTitle = event.title,
            onSubmit = { reason ->
                viewModel.rejectClubEvent(event.eventId, event.organizerId, reason)
                showRejectProposalDialog = false
            },
            onDismiss = { showRejectProposalDialog = false }
        )
    }
}

/**
 * Executive Key Information Grid: Date, Time, Venue, OD, Organizer, Registration, Verification
 */
@Composable
fun KeyInformationSummaryCard(
    event: EventEntity,
    onRegisterClick: () -> Unit
) {
    AppleCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date
                KeyInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Event,
                    iconTint = RoyalBlue600,
                    label = "DATE",
                    primaryValue = event.startDate,
                    secondaryValue = if (event.endDate != event.startDate) "Until ${event.endDate}" else "Single Day"
                )

                // Time
                KeyInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    iconTint = MaterialTheme.colorScheme.primary,
                    label = "TIME",
                    primaryValue = "${event.startTime} - ${event.endTime}",
                    secondaryValue = "IST Standard"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Row 2: Venue & OD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Venue
                KeyInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LocationOn,
                    iconTint = MaterialTheme.colorScheme.primary,
                    label = "VENUE",
                    primaryValue = event.venue,
                    secondaryValue = "Main Campus"
                )

                // OD
                val isOdAvailable = event.odStatus == OdStatus.AVAILABLE.name
                KeyInfoItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.School,
                    iconTint = if (isOdAvailable) EmeraldGreen else AmberPending,
                    label = "OD (ON-DUTY)",
                    primaryValue = if (isOdAvailable) "Granted" else "Not Applicable",
                    secondaryValue = if (isOdAvailable) "Attendance Credit" else "No Academic OD"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Row 3: Organizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("ORGANIZER", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${event.organizer} • ${event.department}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Row 4: Registration & Verification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Registration
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HowToReg, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("REGISTRATION", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (event.registrationRequired) "Registration Required" else "Free & Open Access",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (event.registrationUrl.isNotEmpty()) {
                        Text(
                            text = "Portal link available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(onClick = onRegisterClick)
                        )
                    } else {
                        Text(
                            text = "No pre-registration needed",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }

                // Verification
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (event.verificationStatus == "VERIFIED") Icons.Default.Verified else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (event.verificationStatus == "VERIFIED") EmeraldGreen else AmberPending,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("VERIFICATION", style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.verificationStatus.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (event.verificationStatus == "VERIFIED") EmeraldGreen else AmberPending
                    )
                    Text(
                        text = if (event.verifiedBy.isNotEmpty()) "By ${event.verifiedBy}" else "Pending Faculty Audit",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun KeyInfoItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    primaryValue: String,
    secondaryValue: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconTint.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate500, fontWeight = FontWeight.Bold)
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = secondaryValue,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Clear Visual Timeline for Event Changes
 */
@Composable
fun VisualEventChangeTimeline(
    event: EventEntity,
    history: List<EventHistoryEntity>
) {
    AppleCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // First Timeline Node: Event Creation
            TimelineNodeRow(
                isLast = history.isEmpty(),
                icon = Icons.Default.CheckCircle,
                iconTint = EmeraldGreen,
                title = "Event Published & Registered",
                timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(event.createdAt)),
                author = "${event.createdBy} (${event.organizer})",
                changeSummary = "Original verified schedule created with status ${event.eventStatus}",
                reason = "Official college calendar entry"
            )

            // Subsequent Timeline Nodes for changes
            history.forEachIndexed { index, item ->
                val isLast = index == history.size - 1
                val (nodeIcon, nodeTint) = when (item.changeType) {
                    "OD_UPDATE" -> Pair(Icons.Default.School, EmeraldGreen)
                    "VENUE_CHANGE" -> Pair(Icons.Default.LocationOn, RoyalBlue600)
                    "TIME_CHANGE" -> Pair(Icons.Default.Schedule, AmberPending)
                    "VERIFICATION" -> Pair(Icons.Default.Verified, EmeraldGreen)
                    "STATUS_CHANGE" -> Pair(Icons.Default.Info, RoyalBlue600)
                    else -> Pair(Icons.Default.Edit, RoyalBlue600)
                }

                TimelineNodeRow(
                    isLast = isLast,
                    icon = nodeIcon,
                    iconTint = nodeTint,
                    title = item.changeType.replace("_", " "),
                    timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(item.timestamp)),
                    author = "${item.userName} (${item.userRole})",
                    changeSummary = "${item.oldValue} → ${item.newValue}",
                    reason = item.reason.ifEmpty { "Administrative update" }
                )
            }
        }
    }
}

@Composable
fun TimelineNodeRow(
    isLast: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    timestamp: String,
    author: String,
    changeSummary: String,
    reason: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // Left timeline column: node icon + vertical connecting line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right timeline content column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "By $author",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                fontSize = 11.sp
            )

            // Change difference pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 4.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = changeSummary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                }
            }

            if (reason.isNotEmpty()) {
                Text(
                    text = "Note: $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ProvenanceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
