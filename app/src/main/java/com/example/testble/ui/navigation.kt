package com.example.testble.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.testble.ui.screens.ConnectionScreen
import com.example.testble.ui.screens.ScanScreen
import kotlinx.coroutines.launch

@Composable
fun Navigation(
    onBluetoothStateChanged: () -> Unit,
    isBluetoothEnabled: Boolean,
    onGPSStateChanged: () -> Unit,
    isGPSEnabled: Boolean,
) {
    val navController = rememberNavController()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Screen.ScanScreen.route
    ) {
        composable(Screen.ScanScreen.route) {
            ScanScreen(
                snackBarHostState = snackBarHostState,
                onBluetoothStateChanged = onBluetoothStateChanged,
                isBluetoothEnabled = isBluetoothEnabled,
                onGPSStateChanged = onGPSStateChanged,
                isGPSEnabled = isGPSEnabled,
                onDeviceSelection = { address ->
                    navController.navigate("connection_screen/$address")
                }
            )
        }

        composable(
            Screen.ConnectionScreen.route,
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) {
            ConnectionScreen(
                onError = {
                    navController.popBackStack()
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(
                            message = "An error occurred.",
                            withDismissAction = true
                        )
                    }
                },
                onNavigationBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object ScanScreen : Screen("scan_screen")
    object ConnectionScreen : Screen("connection_screen/{address}")
}