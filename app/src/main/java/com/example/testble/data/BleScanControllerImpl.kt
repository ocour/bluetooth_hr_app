package com.example.testble.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BleScanControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BleScanController {

    private val TAG = "BleScanControllerImpl"

    private val bluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val handler = Handler(Looper.getMainLooper())

    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDevice>>
        get() = _pairedDevices.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDevice>>
        get() = _scannedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean>
        get() = _isScanning.asStateFlow()

    /**
     *  Device scan callback
     */
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val newDevice = result.device

            // Add new device to list if not already in list
            _scannedDevices.update { devices ->
                if(newDevice in devices)
                    devices
                else
                    devices + newDevice
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "Scanning for ble devices failed.")
            _isScanning.value = false
        }
    }

    /**
     *  Starts scanning for ble devices
     */
    @SuppressLint("MissingPermission")
    override fun startLeDevicesScan() {
        // Check that bluetooth is enabled
        if(!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }

        // Check for permissions
        if(!checkForRequiredPermissions()) {
            Log.e(TAG, "App does not have permission to start scanning ble devices.")
            return
        }

        if(_isScanning.value) {
            Log.e(TAG, "Already scanning.")
            return
        }

        val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        // Stops scanning after predefined duration
        handler.postDelayed({
            Log.d(TAG, "-handler- Stopped Scanning for ble devices.")
            _isScanning.value = false

            // Check that bluetooth is enabled
            if(bluetoothAdapter.isEnabled) bluetoothLeScanner.stopScan(leScanCallback)
        }, SCAN_PERIOD)

        Log.d(TAG, "Started Scanning for ble devices.")
        _isScanning.value = true
        bluetoothLeScanner.startScan(null, scanSettings, leScanCallback)
    }

    /**
     *  Stops scanning for ble devices
     */
    @SuppressLint("MissingPermission")
    override fun stopLeDevicesScan() {
        // Check that bluetooth is enabled
        if(!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }

        if(!checkForRequiredPermissions()) {
            Log.e(TAG, "App does not have permission to stop scanning ble devices.")
            return
        }

        if(!_isScanning.value) {
            Log.d(TAG, "Not Scanning.")
            return
        }

        Log.d(TAG, "Stopped Scanning for ble devices.")
        _isScanning.value = false
        bluetoothLeScanner.stopScan(leScanCallback)
    }

    /**
     *  Get paired le devices
     */
    @SuppressLint("MissingPermission")
    override fun getPairedBleDevices() {
        // Check that bluetooth is enabled
        if(!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }

        if(!checkForRequiredPermissions()) {
            Log.e(TAG, "App does not have permission to get paired devices.")
            return
        }

        // Find only paired devices which support Ble
        bluetoothAdapter.bondedDevices.filter { device ->
            device.type == BluetoothDevice.DEVICE_TYPE_LE
                    || device.type == BluetoothDevice.DEVICE_TYPE_DUAL
        }.also { bleDevices ->
            _pairedDevices.update {
                bleDevices
            }
        }
    }

    private fun checkForRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (checkForPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    && checkForPermission(Manifest.permission.BLUETOOTH_SCAN))
        } else {
            (checkForPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    && checkForPermission(Manifest.permission.BLUETOOTH)
                    && checkForPermission(Manifest.permission.BLUETOOTH_ADMIN))
        }
    }

    private fun checkForPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val SCAN_PERIOD: Long = 20000
    }
}