package com.bansify.ai.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bansify.ai.core.designsystem.BansifyTextStyles
import com.bansify.ai.core.designsystem.ChipGroup
import com.bansify.ai.core.designsystem.PrimaryButton
import com.bansify.ai.core.designsystem.SectionHeader
import com.bansify.ai.core.designsystem.Space
import com.bansify.ai.core.designsystem.TextFieldOutlined
import com.bansify.ai.core.ui.BansifyTopBar
import com.bansify.ai.data.mock.MockUser
import kotlinx.coroutines.launch

@Composable
fun PersonaScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(profile.name) }
    var profession by remember { mutableStateOf(profile.profession) }
    var goals by remember { mutableStateOf(profile.goals.toSet()) }
    var aboutYou by remember { mutableStateOf(profile.aboutYou) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!initialized) {
            name = profile.name
            profession = profile.profession
            goals = profile.goals.toSet()
            aboutYou = profile.aboutYou
            initialized = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { BansifyTopBar(title = "Persona", onBack = onBack) }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.md),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Space.md)
                .testTag("persona_screen")
        ) {
            Text(
                text = "Bansify tailors responses based on your profile and preferences.",
                style = BansifyTextStyles.Body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextFieldOutlined(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = "Name",
                placeholder = "e.g. Pawan",
                supportingText = "${name.length}/40",
                testTag = "persona_name_field"
            )

            TextFieldOutlined(
                value = profession,
                onValueChange = { if (it.length <= 40) profession = it },
                label = "Profession",
                placeholder = "Student, Developer, Doctor…",
                testTag = "persona_profession_field"
            )

            SectionHeader(title = "Goals (${goals.size}/3)")
            ChipGroup(
                options = MockUser.goalOptions,
                selectedOptions = goals,
                onToggle = { goal ->
                    goals = if (goal in goals) {
                        goals - goal
                    } else if (goals.size < 3) {
                        goals + goal
                    } else goals
                }
            )

            TextFieldOutlined(
                value = aboutYou,
                onValueChange = { if (it.length <= 280) aboutYou = it },
                label = "About you",
                placeholder = "Preferred tone, tech stack, or study focus…",
                singleLine = false,
                maxLines = 5,
                supportingText = "${aboutYou.length}/280",
                testTag = "persona_about_field"
            )

            Spacer(Modifier.height(Space.xs))
            PrimaryButton(
                text = "Save persona",
                onClick = {
                    viewModel.updatePersona(name, profession, goals.toList(), aboutYou)
                    scope.launch { snackbarHostState.showSnackbar("Persona saved locally") }
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "persona_save_button"
            )
        }
    }
}
