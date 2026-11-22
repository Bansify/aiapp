package com.bansify.ai.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.ai.data.auth.AuthRepository
import com.bansify.ai.data.auth.AuthState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    val state: StateFlow<AuthState> = repo.state

    fun restore() {
        viewModelScope.launch { repo.restore() }
    }

    fun switchTo(userId: String) {
        viewModelScope.launch { repo.switchTo(userId) }
    }

    fun signOut(userId: String) {
        viewModelScope.launch { repo.signOut(userId) }
    }

    fun signOutAll() {
        viewModelScope.launch { repo.signOutAll() }
    }
}
