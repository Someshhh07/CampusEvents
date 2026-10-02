package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.screens.auth.LoginScreen as AuthLoginScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CollegeEventsViewModel

/**
 * Backward compatibility wrapper delegating to [AuthLoginScreen] in the `ui.screens.auth` package.
 */
@Composable
fun LoginScreen(
    viewModel: CollegeEventsViewModel? = null,
    onLoginSuccess: () -> Unit = {},
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AuthLoginScreen(
        onLoginSuccess = { onLoginSuccess() },
        onDismiss = onDismiss,
        modifier = modifier
    )
}
