package com.taloscore.guessapp.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.taloscore.guessapp.FinishedScreen
import com.taloscore.guessapp.LobbyScreen
import com.taloscore.guessapp.PlayGroundScreen

import com.taloscore.guessapp.viewmodel.AuthViewModel
import com.taloscore.guessapp.viewmodel.WebsocketViewModel

sealed class AppScreen(val route: String) {
    object AuthScreen : AppScreen("auth_screen")
    object DashboardScreen : AppScreen("dashboard_screen")
    object LobbyScreen: AppScreen("lobby_screen")
    object PlayGroundScreen: AppScreen("play_ground_screen")
    object FinishedScreen: AppScreen("finished_screen")
}

@Composable
fun AppNavigation(modifier: Modifier, token: String?, authViewModel: AuthViewModel, websocketViewModel: WebsocketViewModel = hiltViewModel()) {
    val navHostController = rememberNavController()
    NavHost(
        navController = navHostController,
        startDestination = if (token == "") AppScreen.AuthScreen.route else AppScreen.DashboardScreen.route
    ) {
        composable(AppScreen.AuthScreen.route) {
            AuthScreen(modifier, authViewModel, navHostController)
        }
        composable(AppScreen.DashboardScreen.route) {
            DashboardScreen(modifier, navHostController, token)
        }
        composable(AppScreen.LobbyScreen.route) {
            LobbyScreen(modifier, navHostController, token, websocketViewModel)
        }

        composable(AppScreen.PlayGroundScreen.route) {
            PlayGroundScreen(modifier, navHostController, websocketViewModel)
        }

        composable(AppScreen.FinishedScreen.route) {
            FinishedScreen(modifier, navHostController, websocketViewModel)
        }
    }

}