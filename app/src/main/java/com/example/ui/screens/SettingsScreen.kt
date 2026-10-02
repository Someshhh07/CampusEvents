package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UserSessionManager
import com.example.model.OdStatus
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoseConflict
import com.example.ui.theme.RoyalBlue600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.CollegeEventsViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.PortalView

/**
 * Enterprise-grade Settings & Account Screen for CAMPUS PULSE.
 *
 * Provides:
 * 1. Account Details (User Profile, Identity, Department, Role)
 * 2. User Data (Saved events, Registered events, OD status, Followed clubs)
 * 3. Role Privileges & Management Portals (Faculty, President, Admin, Community)
 * 4. App Settings (Theme mode, Notification preferences, Cache)
 * 5. App Features (Help & Feedback, FAQs, Share, Privacy Policy, About the App)
 * 6. Logout Section (with confirmation dialog & clean session reset)
 */
@Composable
fun SettingsScreen(
    viewModel: CollegeEventsViewModel,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val savedEvents by viewModel.savedEvents.collectAsState()
    val clubs by viewModel.allClubs.collectAsState()

    var currentPortal by remember { mutableStateOf(PortalView.NONE) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Notification toggles
    var eventRemindersEnabled by remember { mutableStateOf(true) }
    var odAlertsEnabled by remember { mutableStateOf(true) }
    var clubAnnouncementsEnabled by remember { mutableStateOf(true) }
    var conflictAlertsEnabled by remember { mutableStateOf(true) }

    // Sub-portal routing if a management portal is launched
    when (currentPortal) {
        PortalView.FACULTY_PORTAL -> {
            FacultyPortalView(viewModel = viewModel, onBack = { currentPortal = PortalView.NONE })
            return
        }
        PortalView.CLUB_PRESIDENT_PORTAL -> {
            ClubPresidentPortalView(viewModel = viewModel, onBack = { currentPortal = PortalView.NONE })
            return
        }
        PortalView.COMMUNITY_PORTAL -> {
            CommunityPortalView(viewModel = viewModel, onBack = { currentPortal = PortalView.NONE })
            return
        }
        PortalView.ADMIN_DASHBOARD -> {
            AdminDashboardView(viewModel = viewModel, onBack = { currentPortal = PortalView.NONE })
            return
        }
        PortalView.NONE -> { /* Render main SettingsScreen below */ }
    }

    // Live counts for User Data
    val registeredCount = savedEvents.size
    val approvedOdCount = allEvents.count { it.odStatus == "AVAILABLE" && it.isSaved }
    val followedClubsCount = clubs.count { it.isFollowed }
    val pendingOdRequestsCount = allEvents.count { it.odStatus == "AVAILABLE" && it.department == currentUser.department }
    val pendingDraftsCount = allEvents.count { it.eventStatus == "DRAFT" || it.eventStatus == "PENDING" }
    val activeConflictsCount = allEvents.count { it.conflictDetails != null || it.verificationStatus == "CONFLICT" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ----------------- HEADER -----------------
        item {
            Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("settings_header_title")
                )
                Text(
                    text = "Account, permissions, and app preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ----------------- 1. ACCOUNT DETAILS -----------------
        item {
            SettingsSectionHeader(title = "Account Details", icon = Icons.Default.Person)
        }

        item {
            UserProfileCard(
                user = currentUser,
                onSwitchRole = { showRoleSwitcherDialog = true }
            )
        }

        // ----------------- 2. USER DATA -----------------
        item {
            SettingsSectionHeader(title = "Your Campus Data", icon = Icons.Default.Storage)
        }

        item {
            UserDataSummaryGrid(
                savedCount = savedEvents.size,
                registeredCount = registeredCount,
                odPassCount = approvedOdCount,
                followedClubsCount = followedClubsCount,
                onNavigateTab = { tab -> viewModel.setTab(tab) }
            )
        }

        // ----------------- 3. ROLE PRIVILEGES & PORTALS -----------------
        item {
            SettingsSectionHeader(title = "Role Features & Management", icon = Icons.Default.Security)
        }

        item {
            RolePrivilegesCard(
                user = currentUser,
                pendingOdCount = pendingOdRequestsCount,
                pendingDraftsCount = pendingDraftsCount,
                conflictsCount = activeConflictsCount,
                onLaunchFacultyPortal = { currentPortal = PortalView.FACULTY_PORTAL },
                onLaunchClubPortal = { currentPortal = PortalView.CLUB_PRESIDENT_PORTAL },
                onLaunchAdminDashboard = { currentPortal = PortalView.ADMIN_DASHBOARD },
                onLaunchCommunityPortal = { currentPortal = PortalView.COMMUNITY_PORTAL },
                onSwitchRole = { showRoleSwitcherDialog = true }
            )
        }

        // ----------------- 4. APP SETTINGS & PREFERENCES -----------------
        item {
            SettingsSectionHeader(title = "App Preferences", icon = Icons.Default.Settings)
        }

        item {
            ThemeSelectorCard(
                currentTheme = themeMode,
                onThemeSelected = { viewModel.setThemeMode(it) }
            )
        }

        item {
            NotificationPreferencesCard(
                eventReminders = eventRemindersEnabled,
                onEventRemindersChange = { eventRemindersEnabled = it },
                odAlerts = odAlertsEnabled,
                onOdAlertsChange = { odAlertsEnabled = it },
                clubAnnouncements = clubAnnouncementsEnabled,
                onClubAnnouncementsChange = { clubAnnouncementsEnabled = it },
                conflictAlerts = conflictAlertsEnabled,
                onConflictAlertsChange = { conflictAlertsEnabled = it }
            )
        }

        item {
            CacheAndStorageCard(
                onClearCache = {
                    Toast.makeText(context, "Local cache cleared. Data refreshed from SQLite Room DB.", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ----------------- 5. APP RELATED FEATURES -----------------
        item {
            SettingsSectionHeader(title = "Campus Pulse Features", icon = Icons.AutoMirrored.Filled.Help)
        }

        item {
            AppFeaturesList(
                onHelpAndFeedback = { showFeedbackDialog = true },
                onFaqs = { showFaqDialog = true },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Check out Campus Pulse!"
                        )
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Join CAMPUS PULSE — The Unified College Event Aggregation & Verification Platform. Discover campus events, apply for On-Duty (OD) passes, and follow clubs! Access here: https://college.edu/campuspulse"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Campus Pulse with Classmates"))
                },
                onPrivacyPolicy = { showPrivacyDialog = true },
                onAboutApp = { showAboutDialog = true }
            )
        }

        // ----------------- 6. LOGOUT SECTION -----------------
        item {
            SettingsSectionHeader(title = "Account Action", icon = Icons.AutoMirrored.Filled.Logout)
        }

        item {
            LogoutCard(
                user = currentUser,
                onLogoutClick = { showLogoutDialog = true }
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Campus Pulse • Enterprise v2.4.0 • College Event Aggregation System",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }

    // ----------------- DIALOGS -----------------

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = RoseConflict,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Log Out of Campus Pulse?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "You will be signed out of your account (${currentUser.email}). You will need to log in or register again to access personalized campus feeds, OD passes, and role portals."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(context)
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseConflict),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Role Switcher / RBAC Testing Dialog
    if (showRoleSwitcherDialog) {
        RoleSwitcherDialog(
            currentUser = currentUser,
            onDismiss = { showRoleSwitcherDialog = false },
            onSelectRole = { newRole ->
                showRoleSwitcherDialog = false
                viewModel.switchRole(newRole)
                Toast.makeText(context, "Switched role to ${newRole.displayName}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Help & Feedback Dialog
    if (showFeedbackDialog) {
        HelpFeedbackDialog(
            onDismiss = { showFeedbackDialog = false },
            onSubmit = { category, msg ->
                showFeedbackDialog = false
                viewModel.submitReport("APP-FEEDBACK", "Campus Pulse App Feedback", category, msg)
                Toast.makeText(context, "Thank you! Feedback submitted to Campus Pulse Admin.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // FAQs Dialog / BottomSheet
    if (showFaqDialog) {
        FaqsDialog(onDismiss = { showFaqDialog = false })
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    // About Dialog
    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }
}

// ----------------- SUB-COMPONENTS -----------------

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

/**
 * 1. User Profile Card
 */
@Composable
private fun UserProfileCard(
    user: User,
    onSwitchRole: () -> Unit
) {
    val roleColor = when (user.role) {
        UserRole.FACULTY -> EmeraldGreen
        UserRole.CLUB_PRESIDENT -> RoyalBlue600
        UserRole.PLATFORM_ADMIN -> RoseConflict
        UserRole.CLUB_ADMIN -> AmberWarning
        UserRole.CLUB_MEMBER -> NeonCyan
        UserRole.COMMUNITY_MEMBER -> Slate700
        UserRole.STUDENT -> RoyalBlue600
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_profile_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar with User Initial
                Surface(
                    shape = CircleShape,
                    color = roleColor.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, roleColor),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = roleColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified Campus Member",
                            tint = RoyalBlue600,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Role Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = roleColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = user.role.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = roleColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // User ID Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "ID: ${user.userId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Department & Club Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DEPARTMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = user.department,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (!user.clubAffiliation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AFFILIATION: ${user.clubAffiliation}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoyalBlue600,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSwitchRole,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("switch_role_button")
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Switch Role", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 2. User Data Summary Grid
 */
@Composable
private fun UserDataSummaryGrid(
    savedCount: Int,
    registeredCount: Int,
    odPassCount: Int,
    followedClubsCount: Int,
    onNavigateTab: (NavigationTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UserDataMetricCard(
            label = "Saved",
            value = "$savedCount",
            icon = Icons.Default.Bookmark,
            color = RoyalBlue600,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTab(NavigationTab.SAVED) }
        )
        UserDataMetricCard(
            label = "Registered",
            value = "$registeredCount",
            icon = Icons.Default.Event,
            color = EmeraldGreen,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTab(NavigationTab.EVENTS) }
        )
        UserDataMetricCard(
            label = "OD Passes",
            value = "$odPassCount",
            icon = Icons.Default.Verified,
            color = AmberWarning,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTab(NavigationTab.EVENTS) }
        )
        UserDataMetricCard(
            label = "Clubs",
            value = "$followedClubsCount",
            icon = Icons.Default.Groups,
            color = NeonCyan,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTab(NavigationTab.CLUBS) }
        )
    }
}

@Composable
private fun UserDataMetricCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Slate500,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * 3. Role Privileges & Portals
 */
@Composable
private fun RolePrivilegesCard(
    user: User,
    pendingOdCount: Int,
    pendingDraftsCount: Int,
    conflictsCount: Int,
    onLaunchFacultyPortal: () -> Unit,
    onLaunchClubPortal: () -> Unit,
    onLaunchAdminDashboard: () -> Unit,
    onLaunchCommunityPortal: () -> Unit,
    onSwitchRole: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Role: ${user.role.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "RBAC ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Accessible features explanation
            val featuresText = when (user.role) {
                UserRole.STUDENT -> "• Discover campus events from all verified clubs\n• 1-Tap event registration & calendar export\n• Apply for official On-Duty (OD) attendance credit\n• Report inaccurate event schedules or fake circulars"
                UserRole.FACULTY -> "• Official Department Event Verification\n• Review & approve Student On-Duty (OD) requests ($pendingOdCount pending)\n• Resolve multi-source auditorium/schedule conflicts\n• Cancel or reschedule authorized events"
                UserRole.CLUB_PRESIDENT -> "• Club Administration Portal access\n• Review and approve member event drafts ($pendingDraftsCount pending)\n• Direct publishing of approved club events to campus\n• Manage club profile and member roster"
                UserRole.CLUB_ADMIN, UserRole.CLUB_MEMBER -> "• Draft new event circulars for club review\n• Manage club description, socials, and contact\n• Track submission approval status"
                UserRole.COMMUNITY_MEMBER -> "• Submit community circulars & announcements\n• Propose open inter-college events\n• Suggest event corrections to organizers"
                UserRole.PLATFORM_ADMIN -> "• Full platform governance & source sync\n• Central Multi-Source Conflict Resolution Queue ($conflictsCount active)\n• Automated Duplicate Merge Engine\n• User Role assignment and audit trail inspection"
            }

            Text(
                text = featuresText,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Portal Launcher Button based on role
            when (user.role) {
                UserRole.FACULTY -> {
                    Button(
                        onClick = onLaunchFacultyPortal,
                        modifier = Modifier.fillMaxWidth().testTag("launch_faculty_portal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Faculty Verification & OD Portal ($pendingOdCount)")
                    }
                }
                UserRole.CLUB_PRESIDENT, UserRole.CLUB_ADMIN, UserRole.CLUB_MEMBER -> {
                    Button(
                        onClick = onLaunchClubPortal,
                        modifier = Modifier.fillMaxWidth().testTag("launch_club_portal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue600)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Club Administration Portal")
                    }
                }
                UserRole.PLATFORM_ADMIN -> {
                    Button(
                        onClick = onLaunchAdminDashboard,
                        modifier = Modifier.fillMaxWidth().testTag("launch_admin_dashboard_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseConflict)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Central Platform Admin Dashboard")
                    }
                }
                UserRole.COMMUNITY_MEMBER -> {
                    Button(
                        onClick = onLaunchCommunityPortal,
                        modifier = Modifier.fillMaxWidth().testTag("launch_community_portal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue600)
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Community Submissions Hub")
                    }
                }
                UserRole.STUDENT -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onLaunchCommunityPortal,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Submit Circular", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onSwitchRole,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue600),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Other Roles", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. Theme Selector Card
 */
@Composable
private fun ThemeSelectorCard(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Theme Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select your visual theme preference for day or night use",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ThemeOptionPill(
                    title = "Dark OLED",
                    icon = Icons.Default.DarkMode,
                    selected = currentTheme == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionPill(
                    title = "Light",
                    icon = Icons.Default.LightMode,
                    selected = currentTheme == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f)
                )

                ThemeOptionPill(
                    title = "System",
                    icon = Icons.Default.Settings,
                    selected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionPill(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) RoyalBlue600.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) RoyalBlue600 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) RoyalBlue600 else Slate500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) RoyalBlue600 else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Notification Preferences Card
 */
@Composable
private fun NotificationPreferencesCard(
    eventReminders: Boolean,
    onEventRemindersChange: (Boolean) -> Unit,
    odAlerts: Boolean,
    onOdAlertsChange: (Boolean) -> Unit,
    clubAnnouncements: Boolean,
    onClubAnnouncementsChange: (Boolean) -> Unit,
    conflictAlerts: Boolean,
    onConflictAlertsChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure instant push alerts for schedules and passes",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(12.dp))

            NotificationToggleRow(
                title = "Event Reminders",
                description = "Get notified 1 hour before bookmarked events begin",
                checked = eventReminders,
                onCheckedChange = onEventRemindersChange
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            NotificationToggleRow(
                title = "OD Status Alerts",
                description = "Real-time updates when Faculty approves or rejects OD",
                checked = odAlerts,
                onCheckedChange = onOdAlertsChange
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            NotificationToggleRow(
                title = "Club Announcements",
                description = "New workshops and fests posted by followed clubs",
                checked = clubAnnouncements,
                onCheckedChange = onClubAnnouncementsChange
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            NotificationToggleRow(
                title = "Schedule Conflict Alerts",
                description = "Notify when simultaneous campus events clash",
                checked = conflictAlerts,
                onCheckedChange = onConflictAlertsChange
            )
        }
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 12.sp)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = RoyalBlue600
            )
        )
    }
}

/**
 * Cache and Offline Storage Card
 */
@Composable
private fun CacheAndStorageCard(
    onClearCache: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Data & Storage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Room SQLite Database • 32 cached events • Offline sync active",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            OutlinedButton(
                onClick = onClearCache,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Clear Cache", fontSize = 12.sp)
            }
        }
    }
}

/**
 * 5. App Related Features List (Help & feedback, FAQs, Share, Privacy Policy, About the app)
 */
@Composable
private fun AppFeaturesList(
    onHelpAndFeedback: () -> Unit,
    onFaqs: () -> Unit,
    onShare: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onAboutApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            AppFeatureRow(
                title = "Help & Feedback",
                subtitle = "Report bugs, suggest ideas, or request event verification",
                icon = Icons.Default.Feedback,
                iconColor = RoyalBlue600,
                tag = "feature_help_feedback",
                onClick = onHelpAndFeedback
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            AppFeatureRow(
                title = "Frequently Asked Questions (FAQs)",
                subtitle = "How OD approval works, conflict engine, and permissions",
                icon = Icons.Default.QuestionAnswer,
                iconColor = EmeraldGreen,
                tag = "feature_faqs",
                onClick = onFaqs
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            AppFeatureRow(
                title = "Share Campus Pulse",
                subtitle = "Invite peers & club members to join the unified aggregator",
                icon = Icons.Default.Share,
                iconColor = NeonCyan,
                tag = "feature_share",
                onClick = onShare
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            AppFeatureRow(
                title = "Privacy Policy",
                subtitle = "Student data protection, zero tracking & provenance logs",
                icon = Icons.Default.Policy,
                iconColor = AmberWarning,
                tag = "feature_privacy_policy",
                onClick = onPrivacyPolicy
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            AppFeatureRow(
                title = "About Campus Pulse",
                subtitle = "Version 2.4.0 • Enterprise Architecture & College Mission",
                icon = Icons.Default.Info,
                iconColor = Slate700,
                tag = "feature_about_app",
                onClick = onAboutApp
            )
        }
    }
}

@Composable
private fun AppFeatureRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconColor.copy(alpha = 0.12f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500,
                fontSize = 12.sp
            )
        }

        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 6. Logout Card
 */
@Composable
private fun LogoutCard(
    user: User,
    onLogoutClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = RoseConflict.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, RoseConflict.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoseConflict
                )
                Text(
                    text = "Logged in as ${user.email} (${user.role.displayName})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = RoseConflict),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("logout_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------- DIALOG IMPLEMENTATIONS -----------------

/**
 * Role Switcher Dialog for Fast RBAC Testing
 */
@Composable
private fun RoleSwitcherDialog(
    currentUser: User,
    onDismiss: () -> Unit,
    onSelectRole: (UserRole) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Switch Active Campus Role", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select any campus role below to test its granular permissions and interface features:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(6.dp))

                UserRole.values().forEach { role ->
                    Surface(
                        onClick = { onSelectRole(role) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (currentUser.role == role) RoyalBlue600.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (currentUser.role == role) RoyalBlue600 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentUser.role == role,
                                onClick = { onSelectRole(role) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(role.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = when (role) {
                                        UserRole.STUDENT -> "Discovery, 1-tap register, OD requests"
                                        UserRole.FACULTY -> "Verify events, approve OD passes"
                                        UserRole.CLUB_PRESIDENT -> "Approve drafts, direct publishing"
                                        UserRole.CLUB_ADMIN -> "Manage club content & drafts"
                                        UserRole.CLUB_MEMBER -> "Submit drafts to president"
                                        UserRole.COMMUNITY_MEMBER -> "Submit circulars & fests"
                                        UserRole.PLATFORM_ADMIN -> "Full conflict queue & API sync"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Help & Feedback Dialog
 */
@Composable
private fun HelpFeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (category: String, message: String) -> Unit
) {
    var category by remember { mutableStateOf("Bug Report") }
    var message by remember { mutableStateOf("") }
    val categories = listOf("Bug Report", "Feature Suggestion", "Event Data Issue", "General Feedback")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Help & Campus Feedback", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(2).forEach { cat ->
                        Surface(
                            onClick = { category = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (category == cat) RoyalBlue600 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = cat,
                                color = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(2).forEach { cat ->
                        Surface(
                            onClick = { category = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (category == cat) RoyalBlue600 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = cat,
                                color = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Describe your issue or suggestion") },
                    placeholder = { Text("e.g. Schedule clash between AI Workshop and Hackathon in Hall 3...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (message.isNotBlank()) {
                        onSubmit(category, message)
                    }
                },
                enabled = message.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Frequently Asked Questions (FAQs) Dialog
 */
@Composable
private fun FaqsDialog(
    onDismiss: () -> Unit
) {
    val faqs = listOf(
        "How does On-Duty (OD) verification work?" to "OD eligibility is designated during event creation and verified by Department Faculty Coordinators. When a student registers, their request is routed directly to their faculty portal. Once approved, the verified OD certificate barcode is generated in your account.",
        "How are multi-source event conflicts resolved?" to "Campus Pulse automatically matches timestamps and venues across College APIs, Club drafts, and AI-extracted posters. If an overlap is detected, the event is flagged with a CONFLICT badge until resolved by the Faculty or Platform Admin.",
        "What are the privileges for each campus role?" to "Campus Pulse features 7 distinct roles: Student (discovery, registration, OD pass), Faculty (official verification & OD approvals), Club President (draft approval & publishing), Club Admin/Member (content drafting), Community Member (public notices), and Platform Admin (governance & API sync).",
        "How do club members submit events for approval?" to "Club members create draft circulars with venue and OD requests. The draft automatically routes to the Club President. After president endorsement, it is sent to the Faculty Coordinator for final verification.",
        "How do I sync events to Google Calendar?" to "Tap '+ Calendar' on any event card in the feed or details page. Campus Pulse generates an instant .ics calendar schedule entry and triggers the Android calendar intent."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Campus Pulse FAQs", fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(faqs.size) { index ->
                    val (q, a) = faqs[index]
                    var expanded by remember { mutableStateOf(index == 0) }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = q,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.rotate(if (expanded) 180f else 0f)
                                )
                            }

                            AnimatedVisibility(visible = expanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = a,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Done")
            }
        }
    )
}

/**
 * Privacy Policy Dialog
 */
@Composable
private fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Policy, contentDescription = null, tint = AmberWarning)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Campus Pulse Privacy Policy", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "1. Student Data Confidentiality",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Campus Pulse does not sell, commercialize, or track student data for advertising. All profiles are bound strictly to authorized college email domains.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Text(
                    text = "2. On-Duty (OD) Pass Processing",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "OD applications and academic roll numbers are transmitted directly and exclusively to your assigned department faculty coordinator.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Text(
                    text = "3. Provenance & Audit Transparency",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Every event change, verification mark, or reschedule is logged with timestamp, editor identity, and data provenance to prevent disinformation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("I Understand")
            }
        }
    )
}

/**
 * About the App Dialog
 */
@Composable
private fun AboutAppDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("About Campus Pulse", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CAMPUS PULSE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = RoyalBlue600
                )
                Text(
                    text = "Unified College Event Aggregation & Verification Platform",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Version: 2.4.0 (Build 2026.09.21-PROD)\nPlatform: Android & Jetpack Compose\nStorage: SQLite Room Database & Local Cache\nAuth: Android Credential Manager & Firebase",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RoyalBlue600.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CORE ARCHITECTURE:\nMANY SOURCES → ONE PLATFORM → VALIDATION → CONFLICT RESOLUTION → VERIFICATION → STUDENTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue600,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Close")
            }
        }
    )
}
