package com.example.testble.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testble.R
import com.example.testble.data.BleConnectionController
import com.example.testble.ui.components.ConnectionTopBar
import com.example.testble.ui.components.HeartRateCard

private const val TAG ="ConnectionScreen"

@Composable
fun ConnectionScreen(
    modifier: Modifier = Modifier,
    onNavigationBack: () -> Unit,
    onError: () -> Unit,
    viewModel: BleConnectionViewModel = hiltViewModel()
) {
    val bleConnectionUiState = viewModel.bleConnectionUiState.collectAsStateWithLifecycle()

    val navigate: () -> Unit = {
        viewModel.disconnect()
        onNavigationBack()
    }

    BackHandler {
        navigate()
    }

    LaunchedEffect(bleConnectionUiState.value.bleData) {
        Log.d(TAG, "BleData state changed.")
        if(bleConnectionUiState.value.bleData.isFailure) {
            Log.e(TAG, bleConnectionUiState.value.bleData.exceptionOrNull().toString())
            Log.i(TAG, "Navigating back.")
            onError()
        }
    }

    Scaffold(
        topBar = {
            ConnectionTopBar(
                title = stringResource(id = R.string.connection_screen_title),
                onNavigationBack = navigate
            )
        }
    ) { paddingValues ->
        when(bleConnectionUiState.value.connectionState) {
            BleConnectionController.STATE_READY -> {
                Column(modifier = modifier.padding(paddingValues)) {
                    HeartRateCard(
                        title = stringResource(id = R.string.hear_rate_title),
                        hearRate = bleConnectionUiState.value.bleData.getOrNull()?.heartRate
                    )
                }
            }
            else -> {
                ConnectionLoading(
                    modifier = modifier.padding(paddingValues),
                    connectionState = bleConnectionUiState.value.connectionState
                )
            }
        }
    }
}

@Composable
fun ConnectionLoading(
    modifier: Modifier = Modifier,
    connectionState: Int
) {
    val connectionStateString: String = when(connectionState) {
        BleConnectionController.STATE_DISCONNECTED -> "Disconnected"
        BleConnectionController.STATE_CONNECTING -> "Connecting..."
        BleConnectionController.STATE_CONNECTED -> "Connected..."
        BleConnectionController.STATE_READY -> "Ready"
        else -> "Error"
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connection state: $connectionStateString",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.padding(20.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionLoadingPrev() {
    ConnectionLoading(
        connectionState = BleConnectionController.STATE_CONNECTING
    )
}