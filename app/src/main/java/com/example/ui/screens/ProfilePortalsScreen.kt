package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.AuthAndUserManagementDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ConflictEntity
import com.example.data.local.EventEntity
import com.example.data.local.EventReportEntity
import com.example.model.OdStatus
import com.example.model.SourceType
import com.example.model.UserRole
import com.example.ui.components.AppleCard
import com.example.ui.components.CategoryBadge
import com.example.ui.components.OdBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.AmberPending
import com.example.ui.theme.AmberPendingLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.IndigoBadge
import com.example.ui.theme.IndigoBadgeLight
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoseConflict
import com.example.ui.theme.RoseConflictLight
import com.example.ui.theme.RoyalBlue600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CollegeEventsViewModel
import com.example.ui.viewmodel.PortalView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfilePortalsScreen(
    viewModel: CollegeEventsViewModel,
    onOpenRoleSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentPortal by viewModel.currentPortal.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    var showLoginScreen by remember { mutableStateOf(false) }

    when (currentPortal) {
        PortalView.FACULTY_PORTAL -> FacultyPortalView(viewModel = viewModel, onBack = { viewModel.closePortal() })
        PortalView.CLUB_PRESIDENT_PORTAL -> ClubPresidentPortalView(viewModel = viewModel, onBack = { viewModel.closePortal() })
        PortalView.COMMUNITY_PORTAL -> CommunityPortalView(viewModel = viewModel, onBack = { viewModel.closePortal() })
        PortalView.ADMIN_DASHBOARD -> AdminDashboardView(viewModel = viewModel, onBack = { viewModel.closePortal() })
        PortalView.NONE -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("profile_portals_screen"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // User Profile Card
                item {
                    AppleCard(backgroundColor = MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = currentUser.name.take(1),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = currentUser.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = currentUser.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate500
                                        )
                                        Text(
                                            text = currentUser.department,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RoyalBlue600
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Active Permission Role", style = MaterialTheme.typography.labelSmall, color = Slate500)
                                    Text(
                                        text = currentUser.role.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Auth: ${currentUser.authProvider}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate500
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = onOpenRoleSwitcher,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("switch_role_portal_button")
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Role")
                                    }

                                    Button(
                                        onClick = { showLoginScreen = true },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue600),
                                        modifier = Modifier.testTag("open_login_screen_button")
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sign In")
                                    }
                                }
                            }
                        }
                    }
                }

                // Workspace Theme Settings (Light & Dark theme options)
                item {
                    WorkspaceThemeSelectorCard(
                        currentTheme = themeMode,
                        onSelectTheme = { mode -> viewModel.setThemeMode(mode) }
                    )
                }

                // Section: Role Portals
                item {
                    Text(
                        text = "Role Portals & Modules",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Access dedicated role-based interfaces matching your current privileges",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                // Community Portal Launcher
                item {
                    PortalLauncherCard(
                        title = "Community Contributor Portal",
                        description = "Submit community events, upload circular announcements, AI event parser",
                        icon = Icons.Default.PostAdd,
                        accentColor = IndigoBadge,
                        onClick = { viewModel.openPortal(PortalView.COMMUNITY_PORTAL) },
                        tag = "launch_community_portal"
                    )
                }

                // Faculty Portal Launcher
                item {
                    PortalLauncherCard(
                        title = "Faculty Coordinator Portal",
                        description = "Verify department events, grant On-Duty (OD) credit, update venue & schedule",
                        icon = Icons.Default.School,
                        accentColor = EmeraldGreen,
                        onClick = { viewModel.openPortal(PortalView.FACULTY_PORTAL) },
                        tag = "launch_faculty_portal"
                    )
                }

                // Club President Portal Launcher
                item {
                    PortalLauncherCard(
                        title = "Club Administration Portal",
                        description = "Approve member-submitted events, manage member roles, publish announcements",
                        icon = Icons.Default.Group,
                        accentColor = RoyalBlue600,
                        onClick = { viewModel.openPortal(PortalView.CLUB_PRESIDENT_PORTAL) },
                        tag = "launch_club_president_portal"
                    )
                }

                // Platform Admin Dashboard Launcher
                item {
                    PortalLauncherCard(
                        title = "Central Platform Admin Dashboard",
                        description = "Data conflict resolution, duplicate merge, College API sync engine, reports resolution",
                        icon = Icons.Default.AdminPanelSettings,
                        accentColor = RoseConflict,
                        onClick = { viewModel.openPortal(PortalView.ADMIN_DASHBOARD) },
                        tag = "launch_admin_dashboard"
                    )
                }

                // Platform Information
                item {
                    AppleCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("College Event Aggregator v1.0", fontWeight = FontWeight.Bold)
                            Text("Built with Kotlin, Jetpack Compose, Room Database, and RBAC Engine.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Principle: Many Sources → Central Platform → Validation → Verification → Students", style = MaterialTheme.typography.labelSmall, color = Slate700)
                        }
                    }
                }
            }
        }
    }

    if (showLoginScreen) {
        Dialog(
            onDismissRequest = { showLoginScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            com.example.ui.screens.auth.LoginScreen(
                onLoginSuccess = { showLoginScreen = false },
                onDismiss = { showLoginScreen = false }
            )
        }
    }
}

@Composable
fun PortalLauncherCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    AppleCard(
        onClick = onClick,
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Slate400)
        }
    }
}

// ---------------------- COMMUNITY PORTAL ----------------------
@Composable
fun CommunityPortalView(
    viewModel: CollegeEventsViewModel,
    onBack: () -> Unit
) {
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showAiExtractDialog by remember { mutableStateOf(false) }
    val allEvents by viewModel.allEvents.collectAsState()
    val communityEvents = allEvents.filter { it.sourceType == SourceType.COMMUNITY_MEMBER.name || it.sourceType == SourceType.AI_EXTRACTION.name }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back") }
                Text("Community Contributor Portal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("Community members and student volunteers can propose events for faculty validation.", style = MaterialTheme.typography.bodySmall, color = Slate500)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showSubmitDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("community_submit_event_button")
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Submit Event")
                }

                Button(
                    onClick = { showAiExtractDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoBadge),
                    modifier = Modifier.weight(1f).testTag("community_ai_extract_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Poster/Text")
                }
            }
        }

        item {
            Text("Community Proposed Submissions (${communityEvents.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (communityEvents.isEmpty()) {
            item {
                Text("No community events submitted yet. Tap 'Submit Event' or 'AI Poster/Text' to create one.", color = Slate500)
            }
        } else {
            items(communityEvents) { event ->
                AppleCard(onClick = { viewModel.selectEvent(event) }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            VerificationBadge(status = event.verificationStatus)
                        }
                        Text("${event.startDate} • ${event.venue}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text("Source: ${event.source}", style = MaterialTheme.typography.labelSmall, color = IndigoBadge)
                    }
                }
            }
        }
    }

    if (showSubmitDialog) {
        CreateEventDialog(
            sourceType = SourceType.COMMUNITY_MEMBER,
            onSubmit = { title, desc, cat, date, sTime, eTime, venue, org, dept, od, regUrl, srcUrl ->
                viewModel.submitNewEvent(title, desc, cat, date, sTime, eTime, venue, org, dept, od, regUrl, SourceType.COMMUNITY_MEMBER, srcUrl)
                showSubmitDialog = false
            },
            onDismiss = { showSubmitDialog = false }
        )
    }

    if (showAiExtractDialog) {
        AiExtractDialog(
            onExtract = { rawText ->
                viewModel.extractEventFromAnnouncement(rawText)
                showAiExtractDialog = false
            },
            onDismiss = { showAiExtractDialog = false }
        )
    }
}

// ---------------------- FACULTY PORTAL ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyPortalView(
    viewModel: CollegeEventsViewModel,
    onBack: () -> Unit
) {
    val allEvents by viewModel.allEvents.collectAsState()
    val pendingVerifications = allEvents.filter { it.verificationStatus == "PENDING" }
    val verifiedEvents = allEvents.filter { it.verificationStatus == "VERIFIED" }

    var selectedTab by remember { mutableIntStateOf(0) }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back") }
                Text("Faculty Coordinator Portal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("Review and certify pending events, allocate On-Duty (OD) credits, and resolve logistics.", style = MaterialTheme.typography.bodySmall, color = Slate500)
        }

        item {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Pending (${pendingVerifications.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Certified Events (${verifiedEvents.size})") })
            }
        }

        val displayList = if (selectedTab == 0) pendingVerifications else verifiedEvents

        if (displayList.isEmpty()) {
            item {
                Text(if (selectedTab == 0) "No pending events requiring faculty verification." else "No verified events.", color = Slate500)
            }
        } else {
            items(displayList) { event ->
                AppleCard(onClick = { viewModel.selectEvent(event) }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            VerificationBadge(status = event.verificationStatus)
                        }
                        Text("${event.startDate} • ${event.venue} • Organized by ${event.organizer}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            OdBadge(status = event.odStatus)
                            if (event.verificationStatus == "PENDING") {
                                Button(
                                    onClick = { viewModel.facultyVerifyEvent(event.eventId, true, "Faculty verified via portal") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                ) {
                                    Text("✓ Certify Event")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- CLUB PRESIDENT PORTAL ----------------------
@Composable
fun ClubPresidentPortalView(
    viewModel: CollegeEventsViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val pendingMemberSubmissions = allEvents.filter {
        (it.organizerId == currentUser.clubAffiliation || currentUser.role == UserRole.PLATFORM_ADMIN) &&
                it.eventStatus in listOf("PENDING", "SUBMITTED", "UNDER_REVIEW", "CHANGES_REQUESTED", "DRAFT")
    }
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back") }
                Text("Club Administration Portal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text("Review draft events submitted by your club members and publish official events for ${currentUser.clubAffiliation ?: "your club"}.", style = MaterialTheme.typography.bodySmall, color = Slate500)
        }

        item {
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PostAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Official Club Event")
            }
        }

        item {
            Text("Member Submissions Approval Queue (${pendingMemberSubmissions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        if (pendingMemberSubmissions.isEmpty()) {
            item {
                Text("No pending member event submissions at this time.", color = Slate500)
            }
        } else {
            items(pendingMemberSubmissions) { event ->
                AppleCard(onClick = { viewModel.selectEvent(event) }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Submitted by: ${event.createdBy}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                        Text("${event.startDate} • ${event.venue}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.approveClubEvent(event.eventId, event.organizerId) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Approve & Publish")
                            }
                            OutlinedButton(
                                onClick = { viewModel.rejectClubEvent(event.eventId, event.organizerId, "Needs more detail") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            sourceType = SourceType.CLUB_PRESIDENT,
            onSubmit = { title, desc, cat, date, sTime, eTime, venue, org, dept, od, regUrl, srcUrl ->
                viewModel.submitNewEvent(title, desc, cat, date, sTime, eTime, venue, org, dept, od, regUrl, SourceType.CLUB_PRESIDENT, srcUrl)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

// ---------------------- ADMIN DASHBOARD ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardView(
    viewModel: CollegeEventsViewModel,
    onBack: () -> Unit
) {
    val allEvents by viewModel.allEvents.collectAsState()
    val allConflicts by viewModel.allConflicts.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val allClubs by viewModel.allClubs.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Metrics", "Conflicts (${allConflicts.size})", "Duplicates", "API Sync", "Reports (${allReports.size})")

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.testTag("admin_dashboard_view")
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back") }
                Text("Central Platform Admin", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        item {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { idx, title ->
                    Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(title, fontSize = 12.sp) })
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Metrics
                item {
                    Text("System Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard(label = "Total Events", value = "${allEvents.size}", modifier = Modifier.weight(1f))
                        MetricCard(label = "Active Clubs", value = "${allClubs.size}", modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard(label = "Conflicts", value = "${allConflicts.size}", color = RoseConflict, modifier = Modifier.weight(1f))
                        MetricCard(label = "Pending Reports", value = "${allReports.filter { it.status == "PENDING" }.size}", color = AmberPending, modifier = Modifier.weight(1f))
                    }
                }
            }
            1 -> {
                // Conflicts Resolution Center
                item {
                    Text("Data Conflict Resolution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Resolve discrepancies between authoritative and unofficial sources.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                }

                if (allConflicts.isEmpty()) {
                    item { Text("✓ No data conflicts detected. All feeds agree.", color = EmeraldGreen) }
                } else {
                    items(allConflicts) { conflict ->
                        AppleCard {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Conflict in Event ID: ${conflict.eventId}", fontWeight = FontWeight.Bold)
                                Text("Field: ${conflict.field}", color = RoseConflict, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Source A (${conflict.sourceA}):", style = MaterialTheme.typography.labelSmall)
                                        Text(conflict.valueA, fontWeight = FontWeight.SemiBold)
                                        Button(
                                            onClick = {
                                                viewModel.adminResolveConflict(conflict.conflictId, conflict.eventId, conflict.sourceA, conflict.valueA)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text("Use Source A", fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Source B (${conflict.sourceB}):", style = MaterialTheme.typography.labelSmall)
                                        Text(conflict.valueB, fontWeight = FontWeight.SemiBold)
                                        Button(
                                            onClick = {
                                                viewModel.adminResolveConflict(conflict.conflictId, conflict.eventId, conflict.sourceB, conflict.valueB)
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Text("Use Source B", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Duplicates Merge
                item {
                    Text("Duplicate Detection & Merge Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Smart similarity matching detected potential duplicate event entries.", style = MaterialTheme.typography.bodySmall, color = Slate500)
                }

                val duplicates = allEvents.filter { it.isDuplicateCandidate }
                if (duplicates.isEmpty()) {
                    item { Text("✓ No unresolved duplicate event records found.", color = EmeraldGreen) }
                } else {
                    items(duplicates) { dup ->
                        AppleCard {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Candidate: ${dup.title}", fontWeight = FontWeight.Bold)
                                Text("Organized by ${dup.organizer} on ${dup.startDate} at ${dup.venue}", style = MaterialTheme.typography.bodySmall)
                                Text("Similar to canonical event ID: ${dup.duplicateWithId ?: "EVT-001"}", color = AmberPending)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.adminMergeDuplicate(dup.duplicateWithId ?: "EVT-001", dup.eventId) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoBadge)
                                ) {
                                    Icon(Icons.Default.MergeType, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Merge & Consolidate")
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // API Sync Engine
                item {
                    AppleCard(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = RoyalBlue600)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("College Central API Sync Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Scheduled Frequency: Every 15 minutes (Background worker enabled)")
                            Text("Feed Status: Connected & Healthy (Authorized Token: Active)")
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.triggerCollegeApiSync() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trigger Instant Sync")
                            }
                        }
                    }
                }
            }
            4 -> {
                // Reports Resolution
                item {
                    Text("Student Reported Inaccuracies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (allReports.isEmpty()) {
                    item { Text("No student inaccuracy reports submitted.", color = Slate500) }
                } else {
                    items(allReports) { report ->
                        AppleCard {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(report.eventTitle, fontWeight = FontWeight.Bold)
                                    Surface(shape = RoundedCornerShape(6.dp), color = if (report.status == "PENDING") AmberPendingLight else EmeraldGreenLight) {
                                        Text(report.status, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Issue: ${report.issueType}", color = RoseConflict, fontWeight = FontWeight.SemiBold)
                                Text(report.description, style = MaterialTheme.typography.bodySmall)
                                Text("Reported by ${report.userName}", style = MaterialTheme.typography.labelSmall, color = Slate500)

                                if (report.status == "PENDING") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.resolveReport(report.reportId, "Verified against official circular; updated.") },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Mark Resolved")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, color: Color = RoyalBlue600, modifier: Modifier = Modifier) {
    AppleCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = Slate500)
        }
    }
}

@Composable
fun CreateEventDialog(
    sourceType: SourceType,
    onSubmit: (
        title: String,
        description: String,
        category: String,
        startDate: String,
        startTime: String,
        endTime: String,
        venue: String,
        organizer: String,
        department: String,
        odStatus: OdStatus,
        registrationUrl: String,
        sourceUrl: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technical") }
    var date by remember { mutableStateOf("2026-10-15") }
    var startTime by remember { mutableStateOf("02:00 PM") }
    var endTime by remember { mutableStateOf("05:00 PM") }
    var venue by remember { mutableStateOf("AB1 Hall 201") }
    var organizer by remember { mutableStateOf("Student Robotics Club") }
    var department by remember { mutableStateOf("Mechanical & Mechatronics") }
    var odStatus by remember { mutableStateOf(OdStatus.PENDING) }
    var regUrl by remember { mutableStateOf("https://forms.gle/robotics2026") }
    var srcUrl by remember { mutableStateOf("https://college.edu/events/robotics") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            LazyColumn(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Submit Campus Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Source: ${sourceType.label}", style = MaterialTheme.typography.labelSmall, color = RoyalBlue600)
                }
                item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
                item { OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start Time") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = organizer, onValueChange = { organizer = it }, label = { Text("Organizer Club/Society") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = regUrl, onValueChange = { regUrl = it }, label = { Text("Registration Link") }, modifier = Modifier.fillMaxWidth()) }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSubmit(title, description, category, date, startTime, endTime, venue, organizer, department, odStatus, regUrl, srcUrl)
                                }
                            },
                            enabled = title.isNotBlank()
                        ) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiExtractDialog(
    onExtract: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var rawText by remember {
        mutableStateOf(
            "CAMPUS CIRCULAR ANNOUNCEMENT:\n" +
                    "Event: Web3 & AI Hackathon 2026\n" +
                    "Organized By: Blockchain & AI Developers Guild\n" +
                    "Date: 2026-10-24\n" +
                    "Time: 09:00 AM - 06:00 PM\n" +
                    "Venue: Computer Center Lab 4\n" +
                    "OD Available for all registered participants with attendance certification.\n" +
                    "Registration Link: https://forms.gle/web3hack2026\n" +
                    "Contact: guild@college.edu"
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IndigoBadge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Event Extraction", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("Paste circular, email, or poster OCR text. The AI parser extracts structured parameters into a DRAFT event requiring verification.", style = MaterialTheme.typography.bodySmall, color = Slate500, modifier = Modifier.padding(vertical = 6.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("Raw Announcement Text") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onExtract(rawText) },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoBadge),
                        enabled = rawText.isNotBlank()
                    ) {
                        Text("Extract & Create Draft")
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceThemeSelectorCard(
    currentTheme: ThemeMode,
    onSelectTheme: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    AppleCard(
        modifier = modifier.testTag("workspace_theme_card"),
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Workspace Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Select visual style & contrast",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = when (currentTheme) {
                            ThemeMode.LIGHT -> "☀️ Light Active"
                            ThemeMode.DARK -> "🌙 Dark Active"
                            ThemeMode.SYSTEM -> "⚙️ System Active"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Theme Options Cards (Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Light Theme Option
                ThemeOptionItem(
                    title = "Light",
                    subtitle = "Daylight",
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == ThemeMode.LIGHT,
                    accentColor = RoyalBlue600,
                    previewBg = Color(0xFFF8FAFC),
                    previewCard = Color.White,
                    onClick = { onSelectTheme(ThemeMode.LIGHT) },
                    tag = "theme_option_light",
                    modifier = Modifier.weight(1f)
                )

                // Dark Theme Option
                ThemeOptionItem(
                    title = "Dark",
                    subtitle = "OLED Deep",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == ThemeMode.DARK,
                    accentColor = NeonCyan,
                    previewBg = Color(0xFF0B0F17),
                    previewCard = Color(0xFF111827),
                    onClick = { onSelectTheme(ThemeMode.DARK) },
                    tag = "theme_option_dark",
                    modifier = Modifier.weight(1f)
                )

                // System Default Option
                ThemeOptionItem(
                    title = "System",
                    subtitle = "Auto Sync",
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = currentTheme == ThemeMode.SYSTEM,
                    accentColor = SoftEmerald,
                    previewBg = Color(0xFF1E293B),
                    previewCard = Color(0xFF334155),
                    onClick = { onSelectTheme(ThemeMode.SYSTEM) },
                    tag = "theme_option_system",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick toggle button pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (currentTheme == ThemeMode.LIGHT) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (currentTheme == ThemeMode.LIGHT) "Active: Light Mode" else if (currentTheme == ThemeMode.DARK) "Active: Dark Mode" else "Active: System Dynamic",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(
                        onClick = {
                            if (currentTheme == ThemeMode.DARK) {
                                onSelectTheme(ThemeMode.LIGHT)
                            } else {
                                onSelectTheme(ThemeMode.DARK)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("quick_toggle_theme_button")
                    ) {
                        Text(
                            text = if (currentTheme == ThemeMode.DARK) "Switch to Light" else "Switch to Dark",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    previewBg: Color,
    previewCard: Color,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        modifier = modifier.testTag(tag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual swatch preview
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = previewBg,
                border = BorderStroke(1.dp, if (isSelected) accentColor else Color.Gray.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Small card preview inside
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = previewCard,
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(14.dp)
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Icon with checkmark or glow
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
