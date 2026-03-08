package com.example.accessed.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessed.data.model.User
import com.example.accessed.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        _currentUser.value = repository.getCurrentUser()
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.login(email, pass).fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    onSuccess()
                },
                onFailure = { e ->
                    _error.value = e.message ?: "Login failed"
                }
            )
            _isLoading.value = false
        }
    }

    fun signup(name: String, email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.signup(name, email, pass).fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    onSuccess()
                },
                onFailure = { e ->
                    _error.value = e.message ?: "Signup failed"
                }
            )
            _isLoading.value = false
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.continueAsGuest().fold(
                onSuccess = { user ->
                    _currentUser.value = user
                    onSuccess()
                },
                onFailure = { e ->
                    _error.value = e.message ?: "Guest login failed"
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
    }
}
