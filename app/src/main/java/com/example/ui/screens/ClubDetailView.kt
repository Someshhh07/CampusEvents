package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.ClubMemberEntity
import com.example.data.local.EventEntity
import com.example.model.EventStatus
import com.example.model.UserRole
import com.example.ui.components.AppleCard
import com.example.ui.components.ApprovalDecisionCard
import com.example.ui.components.ApprovalWorkflowTracker
import com.example.ui.components.ConfigAdminPermissionsDialog
import com.example.ui.components.EditClubProfileDialog
import com.example.ui.components.EventCard
import com.example.ui.components.RejectProposalDialog
import com.example.ui.components.RequestChangesDialog
import com.example.ui.components.SubmitClubEventProposalDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailView(
    club: ClubEntity,
    viewModel: CollegeEventsViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val clubMembers by viewModel.selectedClubMembers.collectAsState()
    val approvalDecisions by viewModel.selectedClubApprovalDecisions.collectAsState()

    // Permissions & Affiliation Check
    val isAffiliatedWithThisClub = currentUser.clubAffiliation == club.clubId
    val isPresidentOfThisClub = (currentUser.role == UserRole.CLUB_PRESIDENT && isAffiliatedWithThisClub) ||
            currentUser.role == UserRole.PLATFORM_ADMIN
    val isAdminOfThisClub = (currentUser.role == UserRole.CLUB_ADMIN && isAffiliatedWithThisClub) || isPresidentOfThisClub
    val isMemberOfThisClub = (currentUser.role == UserRole.CLUB_MEMBER && isAffiliatedWithThisClub) || isAdminOfThisClub

    // Cross club detection
    val isCrossClubUser = currentUser.clubAffiliation != null &&
            currentUser.clubAffiliation != club.clubId &&
            currentUser.role != UserRole.PLATFORM_ADMIN &&
            currentUser.role != UserRole.STUDENT

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val clubEvents = allEvents.filter { it.organizerId == club.clubId || it.organizer == club.name }

    // Dialog States
    var showSubmitEventDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showConfigPermissionsDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var eventToRequestChanges by remember { mutableStateOf<EventEntity?>(null) }
    var eventToReject by remember { mutableStateOf<EventEntity?>(null) }
    var eventToCancel by remember { mutableStateOf<EventEntity?>(null) }

    val presidents = clubMembers.filter { it.roleInClub == "PRESIDENT" }
    val admins = clubMembers.filter { it.roleInClub == "ADMIN" }
    val members = clubMembers.filter { it.roleInClub == "MEMBER" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("club_detail_view")
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(club.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(club.department, style = MaterialTheme.typography.labelSmall, color = Slate500)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                OutlinedButton(
                    onClick = { viewModel.toggleFollowClub(club.clubId, club.isFollowed) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(if (club.isFollowed) "✓ Following" else "+ Follow")
                }
            }
        )

        // Cross-Club Warning Banner if member of another club
        if (isCrossClubUser) {
            Surface(
                color = AmberPendingLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AmberPending, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cross-Club Protection Active: You are affiliated with ${currentUser.clubAffiliation}. Editing and approvals for ${club.name} are restricted.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate900,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Events") })
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Members (${clubMembers.size})") })
            Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Audit Log (${approvalDecisions.size})") })
            if (isAdminOfThisClub) {
                Tab(selected = selectedTabIndex == 3, onClick = { selectedTabIndex = 3 }, text = { Text("Administration") })
            }
        }

        when (selectedTabIndex) {
            0 -> {
                // Tab 0: Events & Submissions
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AppleCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("About ${club.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(club.description, style = MaterialTheme.typography.bodyMedium, color = Slate700)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Lead Coordinator: ${club.leadPresident} • Contact: ${club.contactEmail}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Club Events (${clubEvents.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Proposals by club members require approval before publishing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { showSubmitEventDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("propose_club_event_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Event Proposal (Approval Workflow)")
                        }
                    }

                    if (clubEvents.isEmpty()) {
                        item {
                            Text("No events scheduled yet.", color = Slate500, modifier = Modifier.padding(vertical = 12.dp))
                        }
                    } else {
                        items(clubEvents) { event ->
                            EventCard(
                                event = event,
                                onClick = { viewModel.selectEvent(event) },
                                onSaveToggle = { viewModel.toggleSaveEvent(event.eventId, event.isSaved) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // Tab 1: Hierarchy: President -> Admin -> Member
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Club Hierarchy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("Club President ➔ Club Admin ➔ Club Member", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                            if (isPresidentOfThisClub || (isAdminOfThisClub && club.adminCanManageMembers)) {
                                Button(
                                    onClick = { showAddMemberDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Section 1: Club President
                    item {
                        Text("👑 Club President", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    items(presidents) { member ->
                        MemberItemCard(
                            member = member,
                            currentUserIsPresident = isPresidentOfThisClub,
                            onPromote = {},
                            onDemote = {},
                            onApprove = {},
                            onRemove = {}
                        )
                    }

                    // Section 2: Club Admins
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🛡️ Club Admins (${admins.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    if (admins.isEmpty()) {
                        item {
                            Text("No admins assigned yet. President can promote members to Admin.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    } else {
                        items(admins) { member ->
                            MemberItemCard(
                                member = member,
                                currentUserIsPresident = isPresidentOfThisClub,
                                onPromote = {},
                                onDemote = { viewModel.removeClubAdmin(club.clubId, member.id, member.userName) },
                                onApprove = { viewModel.approveMembershipRequest(club.clubId, member.id, member.userName) },
                                onRemove = { viewModel.removeClubMember(club.clubId, member.id, member.userName) }
                            )
                        }
                    }

                    // Section 3: Club Members
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("👥 Club Members (${members.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    if (members.isEmpty()) {
                        item {
                            Text("No active members in roster.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        }
                    } else {
                        items(members) { member ->
                            MemberItemCard(
                                member = member,
                                currentUserIsPresident = isPresidentOfThisClub,
                                onPromote = { viewModel.assignClubAdmin(club.clubId, member.id, member.userName) },
                                onDemote = {},
                                onApprove = { viewModel.approveMembershipRequest(club.clubId, member.id, member.userName) },
                                onRemove = { viewModel.removeClubMember(club.clubId, member.id, member.userName) }
                            )
                        }
                    }
                }
            }
            2 -> {
                // Tab 2: Audit Log of Approval Decisions
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Event Approval Decisions Audit Log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Immutable chronological record of every status transition and approval decision for ${club.name}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }

                    if (approvalDecisions.isEmpty()) {
                        item {
                            Text("No approval decisions recorded yet.", color = Slate500, modifier = Modifier.padding(vertical = 12.dp))
                        }
                    } else {
                        items(approvalDecisions) { decision ->
                            ApprovalDecisionCard(decision = decision)
                        }
                    }
                }
            }
            3 -> {
                // Tab 3: Club Administration Panel
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text("Club Administration & Controls", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Enforce governance, manage configurable admin permissions, review event proposals, and maintain profile details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }

                    // President-Only Controls: Configurable Admin Permissions
                    if (isPresidentOfThisClub) {
                        item {
                            AppleCard {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Club Admin Permissions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                            Text("Configurable operational authority for admins", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                        }
                                        Button(
                                            onClick = { showConfigPermissionsDialog = true },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Configure", fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AdminPermissionBadge("Review Events", club.adminCanReviewEvents)
                                        AdminPermissionBadge("Edit Events", club.adminCanEditEvents)
                                        AdminPermissionBadge("Manage Roster", club.adminCanManageMembers)
                                    }
                                }
                            }
                        }
                    }

                    // Manage Club Profile Button (President or permitted Admin)
                    if (isPresidentOfThisClub || club.adminCanEditProfile) {
                        item {
                            AppleCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Club Profile Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Update description, department, contact email", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    }
                                    OutlinedButton(
                                        onClick = { showEditProfileDialog = true },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Edit Profile", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Event Approval Workflow Queue
                    val pendingEvents = clubEvents.filter {
                        it.eventStatus in listOf(
                            EventStatus.SUBMITTED.name,
                            EventStatus.UNDER_REVIEW.name,
                            EventStatus.APPROVED.name,
                            EventStatus.CHANGES_REQUESTED.name,
                            EventStatus.DRAFT.name,
                            "PENDING"
                        )
                    }

                    item {
                        Text(
                            text = "Approval Workflow Queue (${pendingEvents.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Draft ➔ Submitted ➔ Under Review ➔ Approved ➔ Published",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }

                    if (pendingEvents.isEmpty()) {
                        item {
                            Text("All submitted proposals have been processed.", color = Slate500)
                        }
                    } else {
                        items(pendingEvents) { event ->
                            WorkflowApprovalQueueItem(
                                event = event,
                                club = club,
                                onMoveToReview = { viewModel.moveEventToReview(event.eventId, club.clubId) },
                                onApprove = { viewModel.approveClubEvent(event.eventId, club.clubId) },
                                onPublish = { viewModel.publishClubEvent(event.eventId, club.clubId) },
                                onRequestChanges = { eventToRequestChanges = event },
                                onReject = { eventToReject = event },
                                onCancel = { eventToCancel = event }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showSubmitEventDialog) {
        SubmitClubEventProposalDialog(
            club = club,
            onSubmit = { title, desc, cat, date, sTime, eTime, venue, isDraft ->
                viewModel.submitClubEvent(
                    title = title,
                    description = desc,
                    category = cat,
                    startDate = date,
                    startTime = sTime,
                    endTime = eTime,
                    venue = venue,
                    club = club,
                    isDraft = isDraft
                )
                showSubmitEventDialog = false
            },
            onDismiss = { showSubmitEventDialog = false }
        )
    }

    if (showConfigPermissionsDialog) {
        ConfigAdminPermissionsDialog(
            club = club,
            onSave = { permissions ->
                viewModel.updateClubAdminPermissions(club.clubId, permissions)
                showConfigPermissionsDialog = false
            },
            onDismiss = { showConfigPermissionsDialog = false }
        )
    }

    if (showEditProfileDialog) {
        EditClubProfileDialog(
            club = club,
            onSave = { updatedClub ->
                viewModel.updateClubProfile(updatedClub)
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (eventToRequestChanges != null) {
        RequestChangesDialog(
            eventTitle = eventToRequestChanges!!.title,
            onSubmit = { reason ->
                viewModel.requestChangesOnClubEvent(eventToRequestChanges!!.eventId, club.clubId, reason)
                eventToRequestChanges = null
            },
            onDismiss = { eventToRequestChanges = null }
        )
    }

    if (eventToReject != null) {
        RejectProposalDialog(
            eventTitle = eventToReject!!.title,
            onSubmit = { reason ->
                viewModel.rejectClubEvent(eventToReject!!.eventId, club.clubId, reason)
                eventToReject = null
            },
            onDismiss = { eventToReject = null }
        )
    }

    if (eventToCancel != null) {
        RejectProposalDialog(
            eventTitle = eventToCancel!!.title,
            onSubmit = { reason ->
                viewModel.cancelClubEvent(eventToCancel!!.eventId, club.clubId, reason)
                eventToCancel = null
            },
            onDismiss = { eventToCancel = null }
        )
    }

    if (showAddMemberDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("MEMBER") }

        Dialog(onDismissRequest = { showAddMemberDialog = false }) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Club Member", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("College Email") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { role = "MEMBER" },
                            colors = if (role == "MEMBER") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                        ) { Text("Member") }
                        Button(
                            onClick = { role = "ADMIN" },
                            colors = if (role == "ADMIN") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                        ) { Text("Admin") }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddMemberDialog = false }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (name.isNotBlank() && email.isNotBlank()) {
                                    viewModel.addClubMember(club.clubId, name, email, role)
                                    showAddMemberDialog = false
                                }
                            }
                        ) { Text("Add") }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPermissionBadge(label: String, granted: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (granted) EmeraldGreenLight else Slate100
    ) {
        Text(
            text = if (granted) "✓ $label" else "✕ $label",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (granted) EmeraldGreen else Slate500,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun MemberItemCard(
    member: ClubMemberEntity,
    currentUserIsPresident: Boolean,
    onPromote: () -> Unit,
    onDemote: () -> Unit,
    onApprove: () -> Unit,
    onRemove: () -> Unit
) {
    AppleCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = when (member.roleInClub) {
                        "PRESIDENT" -> EmeraldGreenLight
                        "ADMIN" -> IndigoBadgeLight
                        else -> Slate100
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = member.userName.take(1),
                            fontWeight = FontWeight.Bold,
                            color = when (member.roleInClub) {
                                "PRESIDENT" -> EmeraldGreen
                                "ADMIN" -> IndigoBadge
                                else -> Slate700
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(member.userName, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${member.userEmail} • Status: ${member.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (member.status == "PENDING") {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Approve", fontSize = 11.sp)
                    }
                }

                if (currentUserIsPresident && member.roleInClub == "MEMBER") {
                    OutlinedButton(
                        onClick = onPromote,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Promote to Admin", fontSize = 11.sp)
                    }
                } else if (currentUserIsPresident && member.roleInClub == "ADMIN") {
                    OutlinedButton(
                        onClick = onDemote,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Demote", fontSize = 11.sp)
                    }
                }

                if (member.roleInClub != "PRESIDENT") {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkflowApprovalQueueItem(
    event: EventEntity,
    club: ClubEntity,
    onMoveToReview: () -> Unit,
    onApprove: () -> Unit,
    onPublish: () -> Unit,
    onRequestChanges: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
) {
    AppleCard {
        Column(modifier = Modifier.padding(14.dp)) {
            ApprovalWorkflowTracker(currentStatus = event.eventStatus)

            Spacer(modifier = Modifier.height(10.dp))

            Text(event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Submitted by: ${event.createdBy} • Target Date: ${event.startDate} • Venue: ${event.venue}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (event.eventStatus.uppercase()) {
                    "DRAFT", "SUBMITTED", "PENDING" -> {
                        Button(
                            onClick = onMoveToReview,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Move to Review", fontSize = 11.sp)
                        }
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Approve", fontSize = 11.sp)
                        }
                    }
                    "UNDER_REVIEW" -> {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Approve", fontSize = 11.sp)
                        }
                    }
                    "APPROVED" -> {
                        Button(
                            onClick = onPublish,
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue600),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Publish Campus-Wide", fontSize = 11.sp)
                        }
                    }
                    else -> {}
                }

                OutlinedButton(
                    onClick = onRequestChanges,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Changes", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reject", fontSize = 11.sp)
                }
            }
        }
    }
}
