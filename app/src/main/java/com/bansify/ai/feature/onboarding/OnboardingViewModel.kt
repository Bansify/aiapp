package com.bansify.ai.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bansify.ai.data.mock.UserProfile
import com.bansify.ai.data.repo.ChatRepository
import com.bansify.ai.data.repo.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val name: String = "",
    val professionText: String = "",
    val selectedProfessionChip: String? = null,
    val selectedGoals: Set<String> = emptySet(),
    val isSaving: Boolean = false
)

class OnboardingViewModel(
    private val prefsRepo: UserPreferencesRepository,
    private val chatRepo: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onNameChanged(input: String) {
        if (input.length <= 40) {
            _state.update { it.copy(name = input) }
        }
    }

    fun onProfessionTyped(input: String) {
        _state.update {
            it.copy(
                professionText = input.take(40),
                selectedProfessionChip = null // §4: Typing clears chip selection
            )
        }
    }

    fun onProfessionChipTapped(chip: String) {
        _state.update {
            if (it.selectedProfessionChip == chip) {
                it.copy(selectedProfessionChip = null, professionText = "")
            } else {
                it.copy(selectedProfessionChip = chip, professionText = chip)
            }
        }
    }

    fun onGoalToggled(goal: String) {
        _state.update { current ->
            val updated = if (goal in current.selectedGoals) {
                current.selectedGoals - goal
            } else if (current.selectedGoals.size < 3) {
                current.selectedGoals + goal
            } else {
                current.selectedGoals
            }
            current.copy(selectedGoals = updated)
        }
    }

    fun completeAsGuest(onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            prefsRepo.saveOnboarding(
                name = "",
                profession = "",
                goals = emptyList(),
                isGuest = true
            )
            chatRepo.createPostOnboardConversation(
                UserProfile(
                    name = "",
                    profession = "",
                    goals = emptyList(),
                    isGuest = true,
                    onboardingCompleted = true
                )
            )
            _state.update { it.copy(isSaving = false) }
            onDone()
        }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            val current = _state.value
            _state.update { it.copy(isSaving = true) }
            val finalName = current.name.trim().ifEmpty { "Pawan" }
            val finalProfession = current.professionText.trim()
            val finalGoals = current.selectedGoals.toList()
            prefsRepo.saveOnboarding(
                name = finalName,
                profession = finalProfession,
                goals = finalGoals,
                isGuest = false
            )
            chatRepo.createPostOnboardConversation(
                UserProfile(
                    name = finalName,
                    profession = finalProfession,
                    goals = finalGoals,
                    isGuest = false,
                    onboardingCompleted = true
                )
            )
            _state.update { it.copy(isSaving = false) }
            onDone()
        }
    }
}
