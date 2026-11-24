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
import com.bansify.accounts.core.AccountState
import com.bansify.accounts.core.AuthFailure
import com.bansify.ai.auth.AiAuth
import com.bansify.ai.core.designsystem.BansifyTheme
import com.bansify.ai.core.designsystem.ThemeMode
import com.bansify.ai.feature.auth.SignInScreen
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

/** Turns an SDK sign-in failure into something a person can read. */
private fun failureMessage(failure: AuthFailure): String = when (failure) {
    AuthFailure.BrowserUnavailable -> "Koi browser nahi mila — Chrome install karke try karo."
    AuthFailure.InvalidCallback -> "Sign-in link valid nahi tha. Dobara try karo."
    AuthFailure.LoginCancelled -> "Sign-in cancel ho gaya."
    AuthFailure.SessionExpired -> "Session expire ho gaya — dobara sign in karo."
    AuthFailure.Unavailable -> "Accounts service abhi reachable nahi hai."
    AuthFailure.StorageUnavailable -> "Device pe secure storage available nahi hai."
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsRepo = UserPreferencesRepository(applicationContext)
        val auth = AiAuth()
        val modelRepo = ModelRepository(auth)
        val chatRepo = ChatRepository(auth)
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

            val authState by auth.state.collectAsStateWithLifecycle()
            val authFailure by auth.failure.collectAsStateWithLifecycle()

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
                // Chat needs a Bansify Account: history lives on the account, not on
                // the device. Everything before sign-in is the shared Accounts UI.
                // The view models are built once in onCreate, before sign-in, so the
                // history fetch has to be re-triggered the moment a session appears.
                LaunchedEffect(authState) {
                    if (authState is AccountState.SignedIn) chatViewModel.refreshFromServer()
                }

                when (authState) {
                    is AccountState.SignedIn -> {
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

                    is AccountState.SignedOut, is AccountState.StorageCorrupted -> SignInScreen(
                        onSignIn = { auth.signIn(this@MainActivity) },
                        onSignInWithDifferentAccount = { auth.signIn(this@MainActivity, chooseAccount = true) },
                        isInitializing = false,
                        errorMessage = authFailure?.let { failureMessage(it) }
                            ?: (authState as? AccountState.StorageCorrupted)?.let {
                                "Saved session could not be read. Sign in again."
                            }
                    )

                    AccountState.Initializing -> SignInScreen(
                        onSignIn = {},
                        onSignInWithDifferentAccount = {},
                        isInitializing = true,
                    )
                }
            }
        }
    }
}
