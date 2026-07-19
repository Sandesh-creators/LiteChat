package com.litechat.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.litechat.app.ui.screens.auth.LoginScreen
import com.litechat.app.ui.screens.call.VoiceCallScreen
import com.litechat.app.ui.screens.call.VideoCallScreen
import com.litechat.app.ui.screens.chat.ChatListScreen
import com.litechat.app.ui.screens.chat.ChatScreen
import com.litechat.app.ui.screens.contacts.ContactsScreen
import com.litechat.app.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object ChatList : Screen("chat_list")
    data object Contacts : Screen("contacts")
    data object Settings : Screen("settings")
    data object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    data object VoiceCall : Screen("call/voice/{peerId}") {
        fun createRoute(peerId: String) = "call/voice/$peerId"
    }
    data object VideoCall : Screen("call/video/{peerId}") {
        fun createRoute(peerId: String) = "call/video/$peerId"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.ChatList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(200)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(200)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(200))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(200)
            )
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ChatList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ChatList.route) {
            ChatListScreen(
                onChatClick = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId))
                },
                onContactsClick = { navController.navigate(Screen.Contacts.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Contacts.route) {
            ContactsScreen(
                onBackClick = { navController.popBackStack() },
                onChatWithContact = { peerId ->
                    navController.navigate(Screen.Chat.createRoute(peerId))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationId = conversationId,
                onBackClick = { navController.popBackStack() },
                onVoiceCall = { peerId ->
                    navController.navigate(Screen.VoiceCall.createRoute(peerId))
                },
                onVideoCall = { peerId ->
                    navController.navigate(Screen.VideoCall.createRoute(peerId))
                }
            )
        }
        composable(
            route = Screen.VoiceCall.route,
            arguments = listOf(navArgument("peerId") { type = NavType.StringType })
        ) {
            VoiceCallScreen(
                onEndCall = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.VideoCall.route,
            arguments = listOf(navArgument("peerId") { type = NavType.StringType })
        ) {
            VideoCallScreen(
                onEndCall = { navController.popBackStack() }
            )
        }
    }
}
