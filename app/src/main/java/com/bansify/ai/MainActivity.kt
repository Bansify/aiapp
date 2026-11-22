package com.bansify.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.bansify.ai.core.designsystem.BansifyTheme
import com.bansify.ai.core.designsystem.ThemeMode
import com.bansify.ai.data.ai.AiChatClient
import com.bansify.ai.data.ai.ApiKeyStore
import com.bansify.ai.data.auth.AccountStore
import com.bansify.ai.data.auth.AccountsClient
import com.bansify.ai.data.auth.AuthRepository
import com.bansify.ai.data.mock.AppSettings
import com.bansify.ai.data.mock.UserProfile
import com.bansify.ai.data.repo.ChatRepository
import com.bansify.ai.data.repo.ModelRepository
import com.bansify.ai.data.repo.StudioRepository
import com.bansify.ai.data.repo.UserPreferencesRepository
import com.bansify.ai.feature.account.AuthViewModel
import com.bansify.ai.feature.auth.LoginScreen
import com.bansify.ai.feature.auth.LoginViewModel
import com.bansify.ai.feature.chat.ChatViewModel
import com.bansify.ai.feature.history.HistoryViewModel
import com.bansify.ai.feature.models.ModelsViewModel
import com.bansify.ai.feature.onboarding.OnboardingViewModel
import com.bansify.ai.feature.settings.SettingsViewModel
import com.bansify.ai.feature.studio.StudioViewModel
import com.bansify.ai.nav.BansifyNavGraph
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsRepo = UserPreferencesRepository(applicationContext)
        val keyStore = ApiKeyStore(applicationContext)
        val modelRepo = ModelRepository(keyStore)
        val chatRepo = ChatRepository(applicationContext)
        val studioRepo = StudioRepository()
        val chatEngine = AiChatClient()
        val authRepo = AuthRepository(AccountsClient(), AccountStore(applicationContext))

        // TODO(pawan): Replace manual container with Hilt when real backend/network layers are added.
        val onboardingViewModel = OnboardingViewModel(prefsRepo, chatRepo)
        val chatViewModel = ChatViewModel(chatRepo, modelRepo, prefsRepo, chatEngine)
        val historyViewModel = HistoryViewModel(chatRepo)
        val modelsViewModel = ModelsViewModel(modelRepo, prefsRepo)
        val studioViewModel = StudioViewModel(studioRepo)
        val settingsViewModel = SettingsViewModel(prefsRepo, chatRepo, modelRepo)
        val authViewModel = AuthViewModel(authRepo)
        val loginViewModel = LoginViewModel(authRepo)

        lifecycleScope.launch { authRepo.restore() }

        setContent {
            val settings by prefsRepo.appSettings.collectAsStateWithLifecycle(initialValue = AppSettings())
            val profile by prefsRepo.userProfile.collectAsStateWithLifecycle(initialValue = UserProfile())
            val authState by authViewModel.state.collectAsStateWithLifecycle()
            val isOnboardedFlow = remember { prefsRepo.userProfile.map { it.onboardingCompleted } }
            val isOnboardedValue by isOnboardedFlow.collectAsStateWithLifecycle(initialValue = null)
            val isOnboarded = isOnboardedValue ?: if (authState.isSignedIn) true else null
            val scope = rememberCoroutineScope()
            val showLogin = !authState.isRestoring && !authState.isSignedIn && !profile.isGuest

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
                if (showLogin) {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onSignedIn = {},
                        onContinueAsGuest = {
                            scope.launch { prefsRepo.setGuestMode(true) }
                        }
                    )
                } else {
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
                        settingsViewModel = settingsViewModel,
                        authViewModel = authViewModel,
                        loginViewModel = loginViewModel
                    )
                }
            }
        }
    }
}
