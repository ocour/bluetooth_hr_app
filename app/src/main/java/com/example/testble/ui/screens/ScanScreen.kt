package com.example.testble.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.testble.R
import com.example.testble.permissions.SystemBroadcastReceiver
import com.example.testble.ui.components.BleDeviceListHeader
import com.example.testble.ui.components.BleDeviceListItem
import com.example.testble.ui.components.ScanTopBar
import com.google.accompanist.permissions.*

private const val TAG = "ScanScreen"

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onBluetoothStateChanged: () -> Unit,
    isBluetoothEnabled: Boolean,
    onGPSStateChanged: () -> Unit,
    isGPSEnabled: Boolean,
    onDeviceSelection: (String) -> Unit,
    bleScanViewModel: BleScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val bleUiState = bleScanViewModel.bleUiState.collectAsStateWithLifecycle()

    // Intent to enabled bluetooth
    val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    // Intent to enable gps
    val enableGPSIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

    // permission request
    val bluetoothPermissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(
            permissions = listOf(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN,
            )
        )
    } else {
        rememberMultiplePermissionsState(
            permissions = listOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    }

    /** Once bluetooth is enabled, get paired devices */
    LaunchedEffect(isBluetoothEnabled) {
        if(isBluetoothEnabled) {
            bleScanViewModel.getPairedBleDevices()
        }
    }

    // Listens to bluetooth state
    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if(action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = bluetoothState.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            if(state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF) {
                onBluetoothStateChanged()
            }
        }
    }

    // Listens to gps state
    SystemBroadcastReceiver(systemAction = LocationManager.MODE_CHANGED_ACTION) {
        onGPSStateChanged()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            ScanTopBar(
                checked = (bleUiState.value.isScanning && isBluetoothEnabled),
                onCheckedChanged = { checked ->
                    /** Check for GPS */
                    if(!isGPSEnabled) {
                        Log.d(TAG, "GPS not enabled, navigating to settings.")
                        context.startActivity(enableGPSIntent)
                        return@ScanTopBar
                    }
                    Log.d(TAG, "GPS is enabled.")

                    /** Check for permissions */
                    if(!bluetoothPermissionsState.allPermissionsGranted) {
                        bluetoothPermissionsState.launchMultiplePermissionRequest()
                        return@ScanTopBar
                    }
                    Log.d(TAG, "Permissions are granted.")

                    /** Check bluetooth status */
                    if(!isBluetoothEnabled) {
                        Log.d(TAG, "Requesting bluetooth to be enabled.")
                        context.startActivity(enableBTIntent)
                        return@ScanTopBar
                    }
                    Log.d(TAG, "Bluetooth is enabled.")

                    if(checked) {
                        bleScanViewModel.startBleScan()
                    }
                    else {
                        bleScanViewModel.stopBleScan()
                    }
                },
                title = stringResource(id = R.string.scan_screen_title)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            BleDeviceList(
                bleUiState = bleUiState.value,
                hasPermissions = bluetoothPermissionsState.allPermissionsGranted,
                onDeviceSelection = {
                    bleScanViewModel.stopBleScan()
                    onDeviceSelection(it)
                }
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BleDeviceList(
    modifier: Modifier = Modifier,
    bleUiState: BleUiState,
    hasPermissions: Boolean,
    onDeviceSelection: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            BleDeviceListHeader(
                headline = stringResource(id = R.string.header_paired_devices),
                supporting = stringResource(id = R.string.subheader_paired_devices),
                showSupporting = bleUiState.pairedDevices.isEmpty()
            )
        }
        if(hasPermissions) {
            items(bleUiState.pairedDevices) { device ->
                BleDeviceListItem(
                    name = device.name,
                    address = device.address,
                    onClick = { onDeviceSelection(device.address) }
                )
            }
        }
        item {
            BleDeviceListHeader(
                headline = stringResource(id = R.string.header_scanned_devices),
                supporting = stringResource(id = R.string.subheader_scanned_devices),
                showSupporting = bleUiState.scannedDevices.isEmpty()
            )
        }
        if(hasPermissions) {
            items(bleUiState.scannedDevices) { device ->
                BleDeviceListItem(
                    name = device.name,
                    address = device.address,
                    onClick = { onDeviceSelection(device.address) }
                )
            }
        }
    }
}