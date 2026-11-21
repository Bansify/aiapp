package com.bansify.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.bansify.ai.core.designsystem.BansifyTheme
import com.bansify.ai.core.designsystem.ThemeMode
import com.bansify.ai.data.mock.AppSettings
import com.bansify.ai.data.repo.ChatRepository
import com.bansify.ai.data.repo.ModelRepository
import com.bansify.ai.data.repo.StudioRepository
import com.bansify.ai.data.repo.UserPreferencesRepository
import com.bansify.ai.feature.chat.ChatViewModel
import com.bansify.ai.feature.history.HistoryViewModel
import com.bansify.ai.feature.models.ModelsViewModel
import com.bansify.ai.feature.onboarding.OnboardingViewModel
import com.bansify.ai.feature.settings.SettingsViewModel
import com.bansify.ai.feature.studio.StudioViewModel
import com.bansify.ai.nav.BansifyNavGraph
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsRepo = UserPreferencesRepository(applicationContext)
        val modelRepo = ModelRepository()
        val chatRepo = ChatRepository()
        val studioRepo = StudioRepository()

        // TODO(pawan): Replace manual container with Hilt when real backend/network layers are added.
        val onboardingViewModel = OnboardingViewModel(prefsRepo, chatRepo)
        val chatViewModel = ChatViewModel(chatRepo, modelRepo, prefsRepo)
        val historyViewModel = HistoryViewModel(chatRepo)
        val modelsViewModel = ModelsViewModel(modelRepo, prefsRepo)
        val studioViewModel = StudioViewModel(studioRepo)
        val settingsViewModel = SettingsViewModel(prefsRepo, chatRepo, modelRepo)

        setContent {
            val settings by prefsRepo.appSettings.collectAsStateWithLifecycle(initialValue = AppSettings())
            val isOnboardedFlow = remember { prefsRepo.userProfile.map { it.onboardingCompleted } }
            val isOnboarded by isOnboardedFlow.collectAsStateWithLifecycle(initialValue = null)

            val systemDark = isSystemInDarkTheme()
            val isDark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            LaunchedEffect(isDark) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }

            BansifyTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
                fontScaleMultiplier = settings.fontScale
            ) {
                val navController = rememberNavController()
                BansifyNavGraph(
                    navController = navController,
                    isOnboarded = isOnboarded,
                    modelRepo = modelRepo,
                    onboardingViewModel = onboardingViewModel,
                    chatViewModel = chatViewModel,
                    historyViewModel = historyViewModel,
                    modelsViewModel = modelsViewModel,
                    studioViewModel = studioViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
