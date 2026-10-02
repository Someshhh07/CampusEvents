package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TabRow
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.UserSessionManager
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IndigoBadge
import com.example.ui.theme.RoyalBlue600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.viewmodel.CollegeEventsViewModel

@Composable
fun AuthAndUserManagementDialog(
    viewModel: CollegeEventsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = RoyalBlue600,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Authentication & Roles",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Firebase & Google Sign-In",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }

                    TextButton(onClick = onDismiss) {
                        Text("Close", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Session Summary Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RoyalBlue600,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentUser.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${currentUser.role.displayName} • ${currentUser.authProvider}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.signOut(context)
                                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = Slate500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tabs: Sign In / Register / Manage Role / Demo
                val tabs = listOf("Sign In", "Register", "Manage Role", "Demo")
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp), color = RoyalBlue600)
                    }
                } else {
                    when (selectedTab) {
                        0 -> SignInTab(
                            viewModel = viewModel,
                            onLoading = { isLoading = it },
                            onSuccess = { onDismiss() }
                        )
                        1 -> RegisterTab(
                            viewModel = viewModel,
                            onLoading = { isLoading = it },
                            onSuccess = { onDismiss() }
                        )
                        2 -> ManageRoleTab(
                            currentUser = currentUser,
                            onSave = { newRole, newDept ->
                                viewModel.updateUserRoleAndProfile(newRole, newDept)
                                onDismiss()
                            }
                        )
                        3 -> DemoProfilesTab(
                            onSelectUser = { user ->
                                UserSessionManager.switchUser(user)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignInTab(
    viewModel: CollegeEventsViewModel,
    onLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Google Sign-In Button
        Button(
            onClick = {
                onLoading(true)
                viewModel.signInWithGoogle(context) { success, msg ->
                    onLoading(false)
                    if (success) {
                        onSuccess()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_google_signin"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = RoyalBlue600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Continue with Google", fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                " or email ",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("College Email") },
            placeholder = { Text("e.g. anand.k@college.edu") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Slate500) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_signin_email")
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate500) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Slate500
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_signin_password")
        )

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    onLoading(true)
                    viewModel.signInWithEmail(email, password) { success, _ ->
                        onLoading(false)
                        if (success) onSuccess()
                    }
                } else {
                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_email_signin"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue600)
        ) {
            Text("Sign In with Email", fontWeight = FontWeight.Bold)
        }

        // Guest session option
        OutlinedButton(
            onClick = {
                onLoading(true)
                viewModel.signInAnonymously { success, _ ->
                    onLoading(false)
                    if (success) onSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Continue as Anonymous Guest", color = Slate700)
        }
    }
}

@Composable
private fun RegisterTab(
    viewModel: CollegeEventsViewModel,
    onLoading: (Boolean) -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Computer Science & Engineering") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            placeholder = { Text("e.g. Priya Sharma") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Slate500) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("College Email") },
            placeholder = { Text("e.g. priya.s@college.edu") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Slate500) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Create Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Slate500) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Department / Faculty") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Slate500) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Select User Role / Privilege Level:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

        val roleOptions = listOf(
            UserRole.STUDENT,
            UserRole.CLUB_MEMBER,
            UserRole.CLUB_ADMIN,
            UserRole.CLUB_PRESIDENT,
            UserRole.FACULTY,
            UserRole.COMMUNITY_MEMBER
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            roleOptions.forEach { role ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (selectedRole == role) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (selectedRole == role) RoyalBlue600 else Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRole = role }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(role.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                if (fullName.isNotBlank() && email.isNotBlank() && password.length >= 6) {
                    onLoading(true)
                    viewModel.signUpWithEmail(
                        email = email,
                        pass = password,
                        name = fullName,
                        role = selectedRole,
                        department = department
                    ) { success, _ ->
                        onLoading(false)
                        if (success) onSuccess()
                    }
                } else {
                    Toast.makeText(context, "Please complete fields (password min 6 chars)", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_register_submit"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Account & Set Role", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ManageRoleTab(
    currentUser: User,
    onSave: (UserRole, String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentUser.role) }
    var department by remember { mutableStateOf(currentUser.department) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Role-Based Access Control",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Assign or elevate privileges for user '${currentUser.name}'. Permissions take effect immediately.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )

        OutlinedTextField(
            value = department,
            onValueChange = { department = it },
            label = { Text("Assigned Department") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Text("Select Permission Role:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

        UserRole.values().forEach { role ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selectedRole == role) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (selectedRole == role) RoyalBlue600 else Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = role }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(role.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = when (role) {
                                UserRole.STUDENT -> "View, search, register, save, and report events"
                                UserRole.CLUB_MEMBER -> "Submit drafts to club president"
                                UserRole.CLUB_ADMIN -> "Manage club events, members, and announcements"
                                UserRole.CLUB_PRESIDENT -> "Approve drafts, submit verified events, manage roster"
                                UserRole.FACULTY -> "Faculty verification queue, OD allocation, venue changes"
                                UserRole.PLATFORM_ADMIN -> "Platform governance, conflict resolution, API sync"
                                UserRole.COMMUNITY_MEMBER -> "Community & alumni announcements, flyer uploads"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { onSave(selectedRole, department) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_role_change"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue600)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Update User Privileges", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DemoProfilesTab(
    onSelectUser: (User) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Quick Demo Profiles",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Switch into pre-configured campus identities with mock credentials for instant evaluation.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )

        UserSessionManager.predefinedUsers.forEach { user ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectUser(user) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RoyalBlue600.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = user.name.take(1),
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(user.email, style = MaterialTheme.typography.bodySmall, color = Slate500, fontSize = 11.sp)
                        Text(
                            "${user.role.displayName} • ${user.department}",
                            style = MaterialTheme.typography.labelSmall,
                            color = RoyalBlue600
                        )
                    }

                    Text("Select", color = RoyalBlue600, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
