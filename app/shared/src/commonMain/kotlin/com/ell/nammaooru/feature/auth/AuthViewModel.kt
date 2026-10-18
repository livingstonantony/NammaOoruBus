package com.ell.nammaooru.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ell.nammaooru.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginUiState(error = "Enter username and password")
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginUiState(loading = true)
            repository.login(username.trim(), password).fold(
                onSuccess = { _loginState.value = LoginUiState() },
                onFailure = { _loginState.value = LoginUiState(error = it.message ?: "Login failed") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }
}
