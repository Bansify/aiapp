package com.bansify.ai.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.ai.data.auth.AuthRepository
import com.bansify.ai.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LoginStep { IDENTIFIER, SIGN_UP, MFA, FORGOT }

data class LoginUiState(
    val step: LoginStep = LoginStep.IDENTIFIER,
    val identifier: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val recoveryEmail: String = "",
    val mfaCode: String = "",
    val challenge: String = "",
    val isBusy: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val signedIn: Boolean = false
)

class LoginViewModel(private val authRepo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onIdentifierChange(value: String) { _state.value = _state.value.copy(identifier = value, error = null) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value, error = null) }
    fun onFirstNameChange(value: String) { _state.value = _state.value.copy(firstName = value, error = null) }
    fun onLastNameChange(value: String) { _state.value = _state.value.copy(lastName = value, error = null) }
    fun onUsernameChange(value: String) {
        _state.value = _state.value.copy(username = value.filter { it.isLetterOrDigit() }.lowercase(), error = null)
    }
    fun onRecoveryEmailChange(value: String) { _state.value = _state.value.copy(recoveryEmail = value, error = null) }
    fun onMfaCodeChange(value: String) { _state.value = _state.value.copy(mfaCode = value.filter { it.isLetterOrDigit() }, error = null) }

    fun goTo(step: LoginStep) {
        _state.value = _state.value.copy(step = step, error = null, notice = null)
    }

    fun signIn() {
        val current = _state.value
        if (current.isBusy) return
        _state.value = current.copy(isBusy = true, error = null)
        viewModelScope.launch {
            when (val result = authRepo.signIn(current.identifier, current.password)) {
                is AuthResult.Success -> _state.value = _state.value.copy(isBusy = false, signedIn = true)
                is AuthResult.MfaRequired -> _state.value = _state.value.copy(
                    isBusy = false,
                    step = LoginStep.MFA,
                    challenge = result.challenge,
                    notice = result.message
                )
                is AuthResult.Failure -> _state.value = _state.value.copy(isBusy = false, error = result.message)
            }
        }
    }

    fun verifyMfa() {
        val current = _state.value
        if (current.isBusy || current.challenge.isBlank()) return
        _state.value = current.copy(isBusy = true, error = null)
        viewModelScope.launch {
            when (val result = authRepo.verifyMfa(current.challenge, current.mfaCode)) {
                is AuthResult.Success -> _state.value = _state.value.copy(isBusy = false, signedIn = true)
                is AuthResult.MfaRequired -> _state.value = _state.value.copy(
                    isBusy = false,
                    challenge = result.challenge,
                    notice = result.message
                )
                is AuthResult.Failure -> _state.value = _state.value.copy(isBusy = false, error = result.message)
            }
        }
    }

    fun signUp() {
        val current = _state.value
        if (current.isBusy) return
        _state.value = current.copy(isBusy = true, error = null)
        viewModelScope.launch {
            when (
                val result = authRepo.signUp(
                    firstName = current.firstName,
                    lastName = current.lastName,
                    username = current.username,
                    password = current.password,
                    recoveryEmail = current.recoveryEmail
                )
            ) {
                is AuthResult.Success -> _state.value = _state.value.copy(isBusy = false, signedIn = true)
                is AuthResult.MfaRequired -> _state.value = _state.value.copy(
                    isBusy = false,
                    step = LoginStep.MFA,
                    challenge = result.challenge,
                    notice = result.message
                )
                is AuthResult.Failure -> _state.value = _state.value.copy(isBusy = false, error = result.message)
            }
        }
    }

    fun sendResetLink() {
        val current = _state.value
        if (current.isBusy) return
        _state.value = current.copy(isBusy = true, error = null)
        viewModelScope.launch {
            val message = authRepo.forgotPassword(current.identifier)
            _state.value = _state.value.copy(isBusy = false, notice = message)
        }
    }

    fun reset() {
        _state.value = LoginUiState()
    }
}
