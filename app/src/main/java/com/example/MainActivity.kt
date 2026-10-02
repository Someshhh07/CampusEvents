package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassBottomNav
import com.example.ui.components.NotificationsBottomSheet
import com.example.ui.components.RoleSwitcherDialog
import com.example.ui.screens.ClubsScreen
import com.example.ui.screens.EventDetailsScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfilePortalsScreen
import com.example.ui.screens.SavedEventsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.viewmodel.CollegeEventsViewModel
import com.example.ui.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {

    private val viewModel: CollegeEventsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                CollegeAppMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CollegeAppMainScreen(viewModel: CollegeEventsViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val savedEvents by viewModel.savedEvents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    // Authentication Gate: If explicitly signed out, allow logging in or continuing as guest
    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { _ ->
                viewModel.setLoggedIn(true)
            },
            onDismiss = {
                viewModel.setLoggedIn(true)
            }
        )
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var showRoleSwitcher by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    // Display status messages via Snackbar
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearStatusMessage()
        }
    }

    // Handle back button when event or portal is open
    BackHandler(enabled = selectedEvent != null) {
        viewModel.selectEvent(null)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_main_scaffold"),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (selectedEvent == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    LiquidGlassBottomNav(
                        currentTab = currentTab,
                        onTabSelected = { tab -> viewModel.setTab(tab) },
                        savedCount = savedEvents.size
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Tab View
            when (currentTab) {
                NavigationTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onOpenNotifications = { showNotificationsSheet = true },
                    onOpenRoleSwitcher = { showRoleSwitcher = true }
                )
                NavigationTab.EVENTS -> EventsScreen(viewModel = viewModel)
                NavigationTab.CLUBS -> ClubsScreen(viewModel = viewModel)
                NavigationTab.SAVED -> SavedEventsScreen(viewModel = viewModel)
                NavigationTab.SETTINGS, NavigationTab.PROFILE -> SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { viewModel.setLoggedIn(false) }
                )
            }

            // Event Details Overlay Screen
            AnimatedVisibility(
                visible = selectedEvent != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                selectedEvent?.let { event ->
                    EventDetailsScreen(
                        event = event,
                        viewModel = viewModel,
                        onBack = { viewModel.selectEvent(null) }
                    )
                }
            }
        }
    }

    // Role Switcher Modal Dialog
    if (showRoleSwitcher) {
        RoleSwitcherDialog(
            currentRole = currentUser.role,
            onRoleSelected = { role ->
                viewModel.switchRole(role)
            },
            onDismiss = { showRoleSwitcher = false }
        )
    }

    // Notifications Bottom Sheet
    if (showNotificationsSheet) {
        NotificationsBottomSheet(
            viewModel = viewModel,
            onDismiss = { showNotificationsSheet = false }
        )
    }
}
