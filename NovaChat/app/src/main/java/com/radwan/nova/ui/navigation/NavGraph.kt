package com.radwan.nova.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.radwan.nova.data.repository.FakeNovaRepository
import com.radwan.nova.ui.screens.chat.ChatScreen
import com.radwan.nova.ui.screens.home.HomeScreen
import com.radwan.nova.ui.screens.settings.AboutScreen
import com.radwan.nova.ui.screens.settings.AppearanceScreen
import com.radwan.nova.ui.screens.splash.SplashScreen
import com.radwan.nova.ui.theme.NovaAccent
import com.radwan.nova.ui.theme.ThemeMode
import com.radwan.nova.viewmodel.ChatViewModel
import com.radwan.nova.viewmodel.HomeViewModel

@Composable
fun NovaNavGraph(
    navController: NavHostController,
    currentThemeMode: ThemeMode,
    currentAccent: NovaAccent,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onAccentChanged: (NovaAccent) -> Unit
) {
    val repository = remember { FakeNovaRepository() }
    val homeViewModel = remember { HomeViewModel(repository) }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onChatClick = { chatId ->
                    navController.navigate(Screen.Chat.createRoute(chatId))
                },
                onNavigateToCalls = { /* Navigate to Calls */ },
                onNavigateToSpaces = { /* Navigate to Spaces */ },
                onNavigateToProfile = { navController.navigate(Screen.About.route) },
                onNavigateToMoments = { /* Navigate to Moments */ }
            )
        }

        composable(Screen.Chat.route) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: "chat_marquesh"
            val chatViewModel = remember(chatId) { ChatViewModel(repository, chatId) }

            ChatScreen(
                viewModel = chatViewModel,
                onBackClick = { navController.popBackStack() },
                onVoiceCallClick = { /* Start Voice Call */ },
                onVideoCallClick = { /* Start Video Call */ }
            )
        }

        composable(Screen.Appearance.route) {
            AppearanceScreen(
                currentThemeMode = currentThemeMode,
                currentAccent = currentAccent,
                onThemeModeChanged = onThemeModeChanged,
                onAccentChanged = onAccentChanged,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
