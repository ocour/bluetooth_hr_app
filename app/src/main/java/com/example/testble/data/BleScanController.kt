package com.example.testble.data

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface BleScanController {

    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val isScanning: StateFlow<Boolean>

    /**
     *  Starts scanning for ble devices
     */
    fun startLeDevicesScan()

    /**
     *  Stops scanning for ble devices
     */
    fun stopLeDevicesScan()

    /**
     *  Get paired ble devices
     */
    fun getPairedBleDevices()
}