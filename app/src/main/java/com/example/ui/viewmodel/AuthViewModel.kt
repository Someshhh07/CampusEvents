package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserSessionManager
import com.example.model.User
import com.example.model.UserRole
import com.example.service.AuthService
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface AuthUiState {
    object Idle : AuthUiState
    data class Loading(val message: String = "Signing in...") : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authService: AuthService = AuthService.instance,
    private val authRepository: AuthRepository = AuthRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<User> = UserSessionManager.currentUser

    val isFirebaseAvailable: Boolean
        get() = authRepository.isFirebaseAvailable

    /**
     * Initiates the Google Sign-In flow via AndroidX Credential Manager and Firebase Auth.
     */
    fun initiateGoogleSignIn(
        context: Context,
        serverClientId: String? = null,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Signing in with Google...")
            val result = authService.signInWithGoogle(context, serverClientId)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user)
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Google Sign-In failed or was cancelled."
                _uiState.value = AuthUiState.Error(errorMsg)
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    /**
     * Directly signs in with an AuthCredential (e.g. GoogleAuthProvider credential) via AuthRepository.
     */
    fun signInWithCredential(
        credential: AuthCredential,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Authenticating credential...")
            val result = authRepository.signInWithCredential(credential)
            result.onSuccess { authResult ->
                val fbUser = authResult.user
                val email = fbUser?.email ?: ""
                val role = AuthService.determineRoleFromEmail(email)
                val user = User(
                    userId = fbUser?.uid ?: UUID.randomUUID().toString(),
                    name = fbUser?.displayName ?: email.substringBefore("@").ifBlank { "Campus User" },
                    email = email.ifBlank { "user@college.edu" },
                    role = role,
                    department = "Campus Student Affairs",
                    photoUrl = fbUser?.photoUrl?.toString(),
                    authProvider = "Firebase Credential"
                )
                UserSessionManager.switchUser(user)
                _uiState.value = AuthUiState.Success(user)
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Credential authentication failed."
                _uiState.value = AuthUiState.Error(errorMsg)
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    /**
     * Authenticates using college email and password.
     */
    fun signInWithEmail(
        email: String,
        pass: String,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Authenticating credentials...")
            val result = authService.signInWithEmail(email, pass)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user)
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Invalid email or password."
                _uiState.value = AuthUiState.Error(errorMsg)
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    /**
     * Registers a new account with customized role and department.
     */
    fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        role: UserRole,
        department: String,
        clubAffiliation: String? = null,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Creating your campus account...")
            val result = authService.signUpWithEmail(email, pass, name, role, department, clubAffiliation)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user)
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Registration failed."
                _uiState.value = AuthUiState.Error(errorMsg)
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    /**
     * Authenticates with any user ID (Student ID, Roll No, Faculty ID, or custom ID).
     */
    fun signInWithUserId(
        userId: String,
        role: UserRole? = null,
        name: String? = null,
        department: String? = null,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Signing in...")
            val result = authService.signInWithUserId(userId, role, name, department)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user)
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Sign-in failed."
                _uiState.value = AuthUiState.Error(errorMsg)
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    /**
     * Continues as an anonymous guest with guest permissions.
     */
    fun signInAnonymously(
        guestName: String = "Campus Guest",
        role: UserRole = UserRole.STUDENT,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Initializing guest session...")
            val result = authService.signInAnonymously(guestName, role)
            result.onSuccess { user ->
                _uiState.value = AuthUiState.Success(user)
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Guest sign-in failed."
                _uiState.value = AuthUiState.Error(errorMsg)
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    /**
     * Sends password reset email to the given address.
     */
    fun sendPasswordResetEmail(
        email: String,
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading("Sending password reset email...")
            val result = authService.sendPasswordResetEmail(email)
            result.onSuccess {
                _uiState.value = AuthUiState.Idle
                onComplete?.invoke(true, null)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Could not send password reset email."
                _uiState.value = AuthUiState.Error(errorMsg)
                onComplete?.invoke(false, errorMsg)
            }
        }
    }

    /**
     * Signs out the user and clears credentials via AuthRepository and AuthService.
     */
    fun signOut(context: Context? = null) {
        viewModelScope.launch {
            authService.signOut(context)
            authRepository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    /**
     * Clears error state.
     */
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    /**
     * Selects one of the predefined demo profiles for instant evaluation.
     */
    fun selectDemoUser(user: User) {
        UserSessionManager.switchUser(user)
        _uiState.value = AuthUiState.Success(user)
    }
}
