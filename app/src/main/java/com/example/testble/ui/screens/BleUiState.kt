package com.example.testble.ui.screens

import android.bluetooth.BluetoothDevice

data class BleUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isScanning: Boolean = false
)
