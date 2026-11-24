package com.bansify.ai.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bansify.ai.core.designsystem.Motion
import com.bansify.ai.data.repo.ModelRepository
import com.bansify.ai.feature.chat.ChatScreen
import com.bansify.ai.feature.chat.ChatViewModel
import com.bansify.ai.feature.history.HistoryScreen
import com.bansify.ai.feature.history.HistoryViewModel
import com.bansify.ai.feature.models.ModelsViewModel
import com.bansify.ai.feature.onboarding.GoalsScreen
import com.bansify.ai.feature.onboarding.NameScreen
import com.bansify.ai.feature.onboarding.OnboardingViewModel
import com.bansify.ai.feature.onboarding.ProfessionScreen
import com.bansify.ai.feature.onboarding.WelcomeScreen
import com.bansify.ai.feature.profile.ProfileScreen
import com.bansify.ai.feature.settings.AboutScreen
import com.bansify.ai.feature.settings.AppearanceScreen
import com.bansify.ai.feature.settings.DataScreen
import com.bansify.ai.feature.settings.MemoryScreen
import com.bansify.ai.feature.settings.PersonaScreen
import com.bansify.ai.feature.settings.SettingsScreen
import com.bansify.ai.feature.settings.SettingsViewModel
import com.bansify.ai.feature.comingsoon.ComingSoonScreen
import com.bansify.ai.feature.splash.SplashScreen
import com.bansify.ai.feature.studio.StudioViewModel

@Composable
fun BansifyNavGraph(
    navController: NavHostController,
    isOnboarded: Boolean?,
    modelRepo: ModelRepository,
    onboardingViewModel: OnboardingViewModel,
    chatViewModel: ChatViewModel,
    historyViewModel: HistoryViewModel,
    modelsViewModel: ModelsViewModel,
    studioViewModel: StudioViewModel,
    settingsViewModel: SettingsViewModel
) {
    val providers by modelRepo.providers.collectAsStateWithLifecycle()
    val studioProjects by studioViewModel.repos.collectAsStateWithLifecycle()

    fun navigateToChatClearingOnboarding() {
        navController.navigate(Route.Chat) {
            popUpTo(Route.Splash) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Splash,
        enterTransition = { fadeIn(animationSpec = tween(Motion.Screen)) },
        exitTransition = { fadeOut(animationSpec = tween(Motion.Screen)) },
        popEnterTransition = { fadeIn(animationSpec = tween(Motion.Screen)) },
        popExitTransition = { fadeOut(animationSpec = tween(Motion.Screen)) }
    ) {
        composable<Route.Splash> {
            SplashScreen(
                isOnboarded = isOnboarded,
                onFinished = { goToOnboarding ->
                    val target: Route = if (goToOnboarding) Route.OnboardingWelcome else Route.Chat
                    navController.navigate(target) {
                        popUpTo(Route.Splash) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<Route.OnboardingWelcome> {
            WelcomeScreen(
                onContinueWithGoogle = { navController.navigate(Route.OnboardingName) },
                onContinueAsGuest = {
                    onboardingViewModel.completeAsGuest { navigateToChatClearingOnboarding() }
                },
                onSkip = {
                    onboardingViewModel.completeAsGuest { navigateToChatClearingOnboarding() }
                }
            )
        }

        composable<Route.OnboardingName> {
            val state by onboardingViewModel.state.collectAsStateWithLifecycle()
            NameScreen(
                name = state.name,
                onNameChange = onboardingViewModel::onNameChanged,
                onContinue = { navController.navigate(Route.OnboardingProfession) },
                onBack = { navController.popBackStack() },
                onSkip = {
                    onboardingViewModel.completeAsGuest { navigateToChatClearingOnboarding() }
                }
            )
        }

        composable<Route.OnboardingProfession> {
            val state by onboardingViewModel.state.collectAsStateWithLifecycle()
            ProfessionScreen(
                professionText = state.professionText,
                selectedChip = state.selectedProfessionChip,
                onTextTyped = onboardingViewModel::onProfessionTyped,
                onChipTapped = onboardingViewModel::onProfessionChipTapped,
                onContinue = { navController.navigate(Route.OnboardingGoals) },
                onBack = { navController.popBackStack() },
                onSkip = {
                    onboardingViewModel.completeAsGuest { navigateToChatClearingOnboarding() }
                }
            )
        }

        composable<Route.OnboardingGoals> {
            val state by onboardingViewModel.state.collectAsStateWithLifecycle()
            GoalsScreen(
                selectedGoals = state.selectedGoals,
                isSaving = state.isSaving,
                onToggleGoal = onboardingViewModel::onGoalToggled,
                onStart = {
                    onboardingViewModel.completeOnboarding { navigateToChatClearingOnboarding() }
                },
                onBack = { navController.popBackStack() },
                onSkip = {
                    onboardingViewModel.completeAsGuest { navigateToChatClearingOnboarding() }
                }
            )
        }

        composable<Route.Chat> {
            ChatScreen(
                viewModel = chatViewModel,
                providers = providers,
                autoModel = modelRepo.autoModel,
                studioProjects = studioProjects,
                onNavigateHistory = { navController.navigate(Route.History) },
                onNavigateLibrary = { navController.navigate(Route.Library) },
                onNavigateScheduled = { navController.navigate(Route.Scheduled) },
                onNavigateCustomise = { navController.navigate(Route.SettingsPersona) },
                onNavigateModels = { navController.navigate(Route.Models) },
                onNavigateStudio = { navController.navigate(Route.StudioHome) },
                onNavigateProject = { repoId -> navController.navigate(Route.StudioRepo(repoId)) },
                onNavigateSettings = { navController.navigate(Route.Settings) },
                onNavigateProfile = { navController.navigate(Route.Profile) }
            )
        }

        composable<Route.History> {
            HistoryScreen(
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() },
                onOpenConversation = { id ->
                    chatViewModel.openConversation(id)
                    navController.popBackStack()
                },
                onStartNewChat = {
                    chatViewModel.startNewChat()
                    navController.popBackStack()
                }
            )
        }

        // ---- Coming soon ---------------------------------------------------
        // Chat is live; every other surface is honest about being unfinished
        // instead of showing a mock screen that does nothing.

        composable<Route.Library> {
            ComingSoonScreen(
                title = "Library",
                subtitle = "Saved prompts, files aur projects — next release me aayenge.",
                onBack = { navController.popBackStack() },
                actionLabel = "Chat pe wapas jao",
                onAction = { navController.popBackStack() },
            )
        }

        composable<Route.Scheduled> {
            ComingSoonScreen(
                title = "Scheduled",
                subtitle = "Queued tasks aur reminders next release me.",
                onBack = { navController.popBackStack() },
                actionLabel = "Chat pe wapas jao",
                onAction = { navController.popBackStack() },
            )
        }

        composable<Route.Models> {
            ComingSoonScreen(
                title = "Model catalog",
                subtitle = "Sarvam aur OpenRouter ke saare models, limits ke saath — next release me.",
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.StudioHome> {
            ComingSoonScreen(
                title = "Studio",
                subtitle = "Repo browse, code preview aur zip upload — next release me.",
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.StudioRepo> {
            ComingSoonScreen(
                title = "Studio · Repo",
                subtitle = "Repo browsing next release me aayega.",
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.StudioFileViewer> {
            ComingSoonScreen(
                title = "Studio · File",
                subtitle = "File viewer next release me aayega.",
                onBack = { navController.popBackStack() },
            )
        }

        composable<Route.Settings> {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onOpenAppearance = { navController.navigate(Route.SettingsAppearance) },
                onOpenPersona = { navController.navigate(Route.SettingsPersona) },
                onOpenMemory = { navController.navigate(Route.SettingsMemory) },
                onOpenModels = { navController.navigate(Route.Models) },
                onOpenData = { navController.navigate(Route.SettingsData) },
                onOpenAbout = { navController.navigate(Route.SettingsAbout) },
                onOpenProfile = { navController.navigate(Route.Profile) }
            )
        }

        composable<Route.SettingsAppearance> {
            AppearanceScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.SettingsPersona> {
            PersonaScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.SettingsMemory> {
            MemoryScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.SettingsData> {
            DataScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.SettingsAbout> {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Route.Profile> {
            ProfileScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onEditPersona = { navController.navigate(Route.SettingsPersona) }
            )
        }
    }
}
