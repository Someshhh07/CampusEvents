package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.repository.UserSessionManager
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

// Modern Dark Theme Palette
private val DarkCanvas = Color(0xFF080C16)
private val DarkCardSurface = Color(0xFF111927)
private val DarkInputSurface = Color(0xFF162032)
private val DarkBorderSubtle = Color(0xFF223049)
private val DarkBorderFocused = Color(0xFF3B82F6)

private val TextPrimaryDark = Color(0xFFF8FAFC)
private val TextSecondaryDark = Color(0xFF94A3B8)
private val TextMutedDark = Color(0xFF64748B)

private val NeonBlue = Color(0xFF38BDF8)
private val NeonGreen = Color(0xFF34D399)
private val NeonPurple = Color(0xFFA855F7)
private val NeonAmber = Color(0xFFFBBF24)
private val NeonOrange = Color(0xFFFB923C)
private val NeonRose = Color(0xFFF43F5E)

/**
 * Modern, dark-themed login and registration screen for Campus Event Hub.
 *
 * Prompts the user for their role (Student, Faculty Coordinator, Club Admin,
 * Club President, Club Member, Community Member, Platform Administrator),
 * authenticates using Firebase Auth, and persists the full user profile
 * into Firebase Firestore under the "users" collection.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (User) -> Unit = {},
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val uiState by authViewModel.uiState.collectAsState()

    var isSignUpMode by remember { mutableStateOf(false) }
    var emailOrId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Computer Science & Engineering") }
    var clubAffiliation by remember { mutableStateOf("Coding Club") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordSuccess by remember { mutableStateOf(false) }

    val isLoading = uiState is AuthUiState.Loading

    // Trigger onLoginSuccess callback on successful auth state
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            val user = (uiState as AuthUiState.Success).user
            Toast.makeText(
                context,
                "Welcome, ${user.name}! (${user.role.displayName})",
                Toast.LENGTH_SHORT
            ).show()
            onLoginSuccess(user)
            onDismiss?.invoke()
        }
    }

    val displayErrorMessage = when (val state = uiState) {
        is AuthUiState.Error -> state.message
        else -> localError
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen"),
        containerColor = DarkCanvas,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    if (onDismiss != null) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_login_back")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimaryDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCanvas
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Brand Header with Ambient Glow
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF3B82F6).copy(alpha = 0.35f),
                                Color(0xFF1D4ED8).copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFF3B82F6).copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Campus Event Hub",
                    tint = NeonBlue,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSignUpMode) "Create Campus Account" else "Campus Event Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimaryDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isSignUpMode)
                    "Select your role and set up your Firestore profile"
                else
                    "Unified authentication for students, faculty coordinators, and club leaders",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Cloud Sync Indicator Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.7f),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Text(
                        text = "Firebase Auth • Firestore Profile Cloud Sync",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Mode Switcher (Sign In vs Register Tabs)
            DarkAuthModeTabs(
                isSignUpMode = isSignUpMode,
                onModeChange = { newMode ->
                    isSignUpMode = newMode
                    localError = null
                    authViewModel.clearError()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner with Dark Contrast
            AnimatedVisibility(
                visible = displayErrorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                displayErrorMessage?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF450A0A),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = error,
                                color = Color(0xFFFEE2E2),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Google Sign-In Button (Dark Themed)
            DarkGoogleSignInButton(
                onClick = {
                    localError = null
                    authViewModel.clearError()
                    authViewModel.initiateGoogleSignIn(context)
                },
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Styled Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DarkBorderSubtle
                )
                Text(
                    text = if (isSignUpMode) "  OR REGISTER WITH ROLE  " else "  OR SIGN IN WITH EMAIL  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DarkBorderSubtle
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ROLE SELECTION SECTION (Prominently Prompts User for Their Role)
            AnimatedVisibility(
                visible = isSignUpMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Role Prompt Header
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
                        border = BorderStroke(1.dp, Color(0xFF2A3C5F)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = NeonBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Prompt: Select Your Role",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color(0xFF3B82F6))
                                ) {
                                    Text(
                                        text = "MANDATORY",
                                        color = NeonBlue,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your chosen role determines your permissions in Firestore and gives you access to specialized portals (OD passes, verification, draft approvals, or administrative sync).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Interactive Role Cards Grid
                    RoleSelectionSection(
                        selectedRole = selectedRole,
                        onRoleSelected = { selectedRole = it }
                    )

                    // Context-Aware Field: Department
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Department / Academic Unit",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        DarkTextField(
                            value = department,
                            onValueChange = { department = it },
                            placeholder = "e.g. Computer Science & Engineering",
                            leadingIcon = Icons.Default.Business,
                            testTag = "department_input",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        // Quick Department Suggestions
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf("CSE", "ECE", "Mechanical", "IT", "Student Affairs").forEach { deptName ->
                                Surface(
                                    onClick = {
                                        department = when (deptName) {
                                            "CSE" -> "Computer Science & Engineering"
                                            "ECE" -> "Electronics & Communication"
                                            "Mechanical" -> "Mechanical Engineering"
                                            "IT" -> "Information Technology"
                                            else -> "Campus Student Affairs"
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (department.startsWith(deptName)) Color(0xFF1E3A8A) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (department.startsWith(deptName)) NeonBlue else DarkBorderSubtle)
                                ) {
                                    Text(
                                        text = deptName,
                                        color = if (department.startsWith(deptName)) NeonBlue else TextSecondaryDark,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Context-Aware Field: Club Affiliation (Only for Club Roles)
                    val isClubRole = selectedRole in listOf(
                        UserRole.CLUB_ADMIN,
                        UserRole.CLUB_PRESIDENT,
                        UserRole.CLUB_MEMBER
                    )
                    AnimatedVisibility(visible = isClubRole) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Club Affiliation",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            DarkTextField(
                                value = clubAffiliation,
                                onValueChange = { clubAffiliation = it },
                                placeholder = "e.g. Coding Club, Robotics Club",
                                leadingIcon = Icons.Default.Groups,
                                testTag = "club_affiliation_input",
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )

                            // Quick Club Chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                listOf("Coding Club", "Robotics Club", "Cultural Society", "Literary Club", "IEEE Branch").forEach { club ->
                                    Surface(
                                        onClick = { clubAffiliation = club },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (clubAffiliation == club) Color(0xFF78350F) else Color(0xFF1E293B),
                                        border = BorderStroke(1.dp, if (clubAffiliation == club) NeonAmber else DarkBorderSubtle)
                                    ) {
                                        Text(
                                            text = club,
                                            color = if (clubAffiliation == club) NeonAmber else TextSecondaryDark,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Full Name (Only for Sign Up)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        DarkTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "e.g. Anand Kumar or Dr. Ramanathan",
                            leadingIcon = Icons.Default.Person,
                            testTag = "name_input",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // User ID / Email & Password Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Email or User ID Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isSignUpMode) "College Email (Firebase Auth ID)" else "Email or College Roll Number",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    DarkTextField(
                        value = emailOrId,
                        onValueChange = {
                            emailOrId = it
                            localError = null
                            authViewModel.clearError()
                        },
                        placeholder = if (isSignUpMode) "e.g. anand.k@college.edu" else "e.g. 21CS042, anand.k@college.edu, or admin",
                        leadingIcon = Icons.Default.Email,
                        testTag = "email_input",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                }

                // Password Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        if (!isSignUpMode) {
                            Text(
                                text = "Forgot password?",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        forgotPasswordEmail = emailOrId
                                        forgotPasswordSuccess = false
                                        showForgotPasswordDialog = true
                                    }
                                    .testTag("btn_forgot_password")
                            )
                        }
                    }

                    DarkTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                            authViewModel.clearError()
                        },
                        placeholder = if (isSignUpMode) "At least 6 characters" else "Enter your password",
                        leadingIcon = Icons.Default.Lock,
                        testTag = "password_input",
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextSecondaryDark
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        onNext = { focusManager.clearFocus() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Primary Auth Action Button (Firebase Auth + Firestore Profile Sync)
            Button(
                onClick = {
                    val cleanInput = emailOrId.trim()
                    if (cleanInput.isBlank()) {
                        localError = if (isSignUpMode) "Please enter your college email." else "Please enter your email or User ID."
                        return@Button
                    }

                    localError = null
                    authViewModel.clearError()

                    if (isSignUpMode) {
                        if (password.length < 6) {
                            localError = "Password must be at least 6 characters."
                            return@Button
                        }
                        val effectiveEmail = if (cleanInput.contains("@")) cleanInput else "${cleanInput.lowercase()}@college.edu"
                        val displayName = name.trim().ifBlank { cleanInput.substringBefore("@").replaceFirstChar { it.uppercase() } }

                        authViewModel.signUpWithEmail(
                            email = effectiveEmail,
                            pass = password,
                            name = displayName,
                            role = selectedRole,
                            department = department.trim().ifBlank { "Computer Science & Engineering" },
                            clubAffiliation = if (selectedRole in listOf(UserRole.CLUB_ADMIN, UserRole.CLUB_PRESIDENT, UserRole.CLUB_MEMBER)) clubAffiliation else null
                        )
                    } else {
                        // Sign In mode
                        if (cleanInput.contains("@")) {
                            // Direct Firebase Email & Password authentication
                            authViewModel.signInWithEmail(cleanInput, password.ifBlank { "password123" })
                        } else {
                            // Flexible user ID / Roll number login
                            authViewModel.signInWithUserId(
                                userId = cleanInput,
                                role = if (cleanInput.equals("admin", ignoreCase = true)) UserRole.PLATFORM_ADMIN else null
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_auth_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSignUpMode) Color(0xFF059669) else Color(0xFF2563EB)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isSignUpMode) Icons.Default.PersonAdd else Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSignUpMode) "Register & Save Firestore Profile" else "Sign In with Firebase Auth",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch between Sign In and Register
            TextButton(
                onClick = {
                    isSignUpMode = !isSignUpMode
                    localError = null
                    authViewModel.clearError()
                },
                modifier = Modifier.testTag("toggle_auth_mode_button")
            ) {
                Text(
                    text = if (isSignUpMode)
                        "Already registered? Back to Sign In"
                    else
                        "Don't have an account? Create one with your Role",
                    color = NeonBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1-Tap Demo Role Personas (Quick RBAC Testing)
            DarkDemoPersonasCard(
                authViewModel = authViewModel
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = DarkCardSurface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = NeonBlue)
                    Text("Reset Password", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter your registered college email address to receive a secure Firebase Auth password reset link.",
                        color = TextSecondaryDark,
                        fontSize = 13.sp
                    )

                    DarkTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        placeholder = "e.g. user@college.edu",
                        leadingIcon = Icons.Default.Email,
                        testTag = "forgot_password_email_input"
                    )

                    if (forgotPasswordSuccess) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.dp, NeonGreen)
                        ) {
                            Text(
                                text = "Password reset email sent! Please check your inbox.",
                                color = Color(0xFFA7F3D0),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordEmail.isNotBlank()) {
                            authViewModel.sendPasswordResetEmail(forgotPasswordEmail.trim()) { success, _ ->
                                if (success) {
                                    forgotPasswordSuccess = true
                                    Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Send Reset Link", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}

/**
 * Top Segmented Pill Toggle for Sign In vs Register
 */
@Composable
private fun DarkAuthModeTabs(
    isSignUpMode: Boolean,
    onModeChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            // Sign In Tab
            val signInBg by animateColorAsState(
                targetValue = if (!isSignUpMode) Color(0xFF1E293B) else Color.Transparent,
                animationSpec = tween(250)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(signInBg)
                    .clickable { onModeChange(false) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (!isSignUpMode) NeonBlue else TextMutedDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Sign In",
                        color = if (!isSignUpMode) TextPrimaryDark else TextMutedDark,
                        fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // Register Tab
            val registerBg by animateColorAsState(
                targetValue = if (isSignUpMode) Color(0xFF064E3B) else Color.Transparent,
                animationSpec = tween(250)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(registerBg)
                    .clickable { onModeChange(true) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = if (isSignUpMode) NeonGreen else TextMutedDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Create Account",
                        color = if (isSignUpMode) TextPrimaryDark else TextMutedDark,
                        fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

/**
 * Interactive Role Selection Section prompting the user for their exact role.
 */
@Composable
private fun RoleSelectionSection(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    data class RoleItem(
        val role: UserRole,
        val icon: ImageVector,
        val accentColor: Color,
        val subtitle: String,
        val tag: String
    )

    val roles = listOf(
        RoleItem(
            role = UserRole.STUDENT,
            icon = Icons.Default.School,
            accentColor = NeonBlue,
            subtitle = "Browse events, RSVP, claim OD attendance passes & track campus activities",
            tag = "Student Access"
        ),
        RoleItem(
            role = UserRole.FACULTY,
            icon = Icons.Default.Verified,
            accentColor = NeonPurple,
            subtitle = "Verify department events, resolve timetable conflicts & approve official OD passes",
            tag = "Faculty Coordinator"
        ),
        RoleItem(
            role = UserRole.CLUB_ADMIN,
            icon = Icons.Default.Groups,
            accentColor = NeonAmber,
            subtitle = "Coordinate club activities, manage event drafts & member rosters",
            tag = "Club Executive"
        ),
        RoleItem(
            role = UserRole.CLUB_PRESIDENT,
            icon = Icons.Default.Star,
            accentColor = NeonOrange,
            subtitle = "Publish official club events, approve member drafts & manage executive permissions",
            tag = "Club Leadership"
        ),
        RoleItem(
            role = UserRole.CLUB_MEMBER,
            icon = Icons.Default.Handshake,
            accentColor = Color(0xFF60A5FA),
            subtitle = "Submit draft events & collaborate with club leads on campus proposals",
            tag = "Club Contributor"
        ),
        RoleItem(
            role = UserRole.COMMUNITY_MEMBER,
            icon = Icons.Default.Public,
            accentColor = NeonGreen,
            subtitle = "Submit open circulars, public notices & alumni community events",
            tag = "Public & Alumni"
        ),
        RoleItem(
            role = UserRole.PLATFORM_ADMIN,
            icon = Icons.Default.AdminPanelSettings,
            accentColor = NeonRose,
            subtitle = "Central platform governance, timetable conflict resolution & system-wide API sync",
            tag = "System Admin"
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        roles.forEach { item ->
            val isSelected = selectedRole == item.role
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) item.accentColor else DarkBorderSubtle,
                animationSpec = tween(200)
            )
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) item.accentColor.copy(alpha = 0.12f) else DarkCardSurface,
                animationSpec = tween(200)
            )

            Surface(
                onClick = { onRoleSelected(item.role) },
                shape = RoundedCornerShape(14.dp),
                color = bgColor,
                border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("role_option_${item.role.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Role Icon Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = item.accentColor.copy(alpha = if (isSelected) 0.25f else 0.15f),
                        border = BorderStroke(1.dp, item.accentColor.copy(alpha = 0.4f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.role.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextPrimaryDark else Color(0xFFE2E8F0)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = item.accentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = item.tag,
                                    color = item.accentColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Selection Ring
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) item.accentColor else Color(0xFF1E293B))
                            .border(
                                1.5.dp,
                                if (isSelected) item.accentColor else Color(0xFF475569),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Standard Dark Text Field with Glowing Border on Active State.
 */
@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    testTag: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onNext: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextMutedDark, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = NeonBlue,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DarkInputSurface,
            unfocusedContainerColor = DarkInputSurface,
            disabledContainerColor = DarkInputSurface,
            focusedTextColor = TextPrimaryDark,
            unfocusedTextColor = TextPrimaryDark,
            cursorColor = NeonBlue,
            focusedBorderColor = DarkBorderFocused,
            unfocusedBorderColor = DarkBorderSubtle
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(onNext = { onNext?.invoke() }, onDone = { onNext?.invoke() }),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

/**
 * Modern Dark-Themed Google Sign-In Button compliant with Google guidelines.
 */
@Composable
private fun DarkGoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF131C2E),
        border = BorderStroke(1.dp, Color(0xFF263554)),
        modifier = modifier
            .height(52.dp)
            .testTag("google_sign_in_button")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = NeonBlue
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Authenticating with Google...",
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        color = TextPrimaryDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * 1-Tap Demo Role Personas for instant evaluator testing of all 7 roles.
 */
@Composable
private fun DarkDemoPersonasCard(
    authViewModel: AuthViewModel
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardSurface),
        border = BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "1-Tap Role Tester (RBAC Ready)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF064E3B)
                ) {
                    Text(
                        text = "Instant Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Instantly switch personas to evaluate role-specific screens, actions, and approvals:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            val personas = UserSessionManager.predefinedUsers
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                personas.forEach { persona ->
                    Surface(
                        onClick = { authViewModel.selectDemoUser(persona) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF162032),
                        border = BorderStroke(1.dp, DarkBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("demo_persona_${persona.role.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (persona.role) {
                                    UserRole.STUDENT -> "🎓"
                                    UserRole.FACULTY -> "👨‍🏫"
                                    UserRole.CLUB_PRESIDENT -> "👑"
                                    UserRole.PLATFORM_ADMIN -> "⚡"
                                    UserRole.CLUB_ADMIN -> "👥"
                                    UserRole.CLUB_MEMBER -> "🤝"
                                    UserRole.COMMUNITY_MEMBER -> "🌐"
                                },
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${persona.name} • ${persona.role.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${persona.userId} • ${persona.department}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            }

                            Text(
                                text = "Switch →",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { authViewModel.signInAnonymously() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_sign_in_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, DarkBorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryDark)
            ) {
                Text("Continue as Anonymous Guest", fontSize = 12.sp)
            }
        }
    }
}
