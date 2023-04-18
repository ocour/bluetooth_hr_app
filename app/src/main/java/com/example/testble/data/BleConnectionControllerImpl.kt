package com.example.testble.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.testble.util.BleException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*

class BleConnectionControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) : BleConnectionController {
    private val TAG = "BleConnectionController"

    private val _connectionState = MutableStateFlow(BleConnectionController.STATE_CONNECTING)
    override val connectionState: StateFlow<Int>
        get() = _connectionState.asStateFlow()

    private val _state: MutableStateFlow<Result<BleData>> = MutableStateFlow(Result.success(BleData()))
    override val state: StateFlow<Result<BleData>>
        get() = _state.asStateFlow()

    private lateinit var device: BluetoothDevice
    private lateinit var gatt: BluetoothGatt

    private val HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb"
    private val HEART_RATE_CHARA_UUID = "00002a37-0000-1000-8000-00805f9b34fb"          // PROPERTY_NOTIFY
    private val HEART_RATE_DESC_UUID = "00002902-0000-1000-8000-00805f9b34fb"           // FOR SETTING NOTIFICATIONS
    private val BODY_SENSOR_LOC_CHARA_UUID = "00002a38-0000-1000-8000-00805f9b34fb"     // PROPERTY_READ

    private var heartRateCharacteristic: BluetoothGattCharacteristic? = null
    private var heartRateDescriptor: BluetoothGattDescriptor? = null

    init {
        Log.i(TAG, "CREATED.")
    }

    private val gattConnectionCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            try {
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d(TAG, "CONNECTED!")
                        _connectionState.value = BleConnectionController.STATE_CONNECTED
                        gatt?.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d(TAG, "DISCONNECTED!")
                        _connectionState.value = BleConnectionController.STATE_DISCONNECTED
                        gatt?.close()
                    }
                }
                else {
                    gatt?.close()
                    throw BleException("Could not connect to device.")
                }
            }
            catch(e: Exception) {
                _state.value = Result.failure(e)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            try {
                if(status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Services discovered.")

                    if(!hasAllRequiredServices(gatt!!)) {
                        Log.e(TAG, "Does not support services.")
                        disconnect()
                        throw BleException("Connected device does not support all required services.")
                    }

                    if(!enableHeartRateNotifications(gatt)) {
                        Log.e(TAG, "ENABLING HEART RATE NOTIFICATIONS FAILED.")
                        disconnect()
                        throw BleException("An problem occurred while enabling notifications")
                    }

                    Log.i(TAG, "ENABLING HEART RATE NOTIFICATIONS SUCCEEDED.")
                    _connectionState.value = BleConnectionController.STATE_READY
                }
            }
            catch(e: Exception) {
                _state.value = Result.failure(e)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)

            if(characteristic.uuid.toString() == HEART_RATE_CHARA_UUID) {
                updateHeartRateValue(value.toUByteArray())
            }
        }

        // FOR LEGACY
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            if(characteristic?.uuid.toString() == HEART_RATE_CHARA_UUID) {
                updateHeartRateValue(characteristic?.value?.toUByteArray()!!)
            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun updateHeartRateValue(bytes: UByteArray) {
        val byte = bytes[1]
        _state.update {
            it.map {  data ->
                data.copy(heartRate = byte.toInt())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableHeartRateNotifications(gatt: BluetoothGatt): Boolean {

        if(!gatt.setCharacteristicNotification(heartRateCharacteristic, true)) {
            return false
        }

        if(heartRateDescriptor == null) {
            return false
        }

        var res: Boolean = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(
                heartRateDescriptor!!,
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            ) == BluetoothStatusCodes.SUCCESS
        } else {
            heartRateDescriptor!!.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            gatt.writeDescriptor(
                heartRateDescriptor
            )
        }

        return res
    }

    /**
     *  Has all required services
     */
    private fun hasAllRequiredServices(gatt: BluetoothGatt): Boolean {
        return hasRequiredHearRateServices(gatt)
    }

    /**
     *  Checks that heart rate characteristic is supported by device
     */
    private fun hasRequiredHearRateServices(gatt: BluetoothGatt): Boolean {
        val heartRateService: BluetoothGattService? = gatt.getService(UUID.fromString(HEART_RATE_SERVICE_UUID))

        if(heartRateService != null) {
            heartRateCharacteristic = heartRateService.getCharacteristic(UUID.fromString(HEART_RATE_CHARA_UUID))
            if(heartRateCharacteristic != null) {
                heartRateDescriptor = heartRateCharacteristic!!.getDescriptor(UUID.fromString(HEART_RATE_DESC_UUID))
            }
        }
        return heartRateCharacteristic != null && heartRateDescriptor != null
    }

    @SuppressLint("MissingPermission")
    override fun connect(deviceAddress: String) {
        try {
            Log.d(TAG, "Connecting.")
            Log.d(TAG, "Received address: $deviceAddress")

            if (!bluetoothAdapter.isEnabled) {
                Log.e(TAG, "Bluetooth is not enabled")
                throw BleException("Bluetooth is not enabled.")
            }

            if (!checkForRequiredPermissions()) {
                Log.e(TAG, "App does not have required permissions.")
                throw BleException("App does not have the required permissions.")
            }

            if(!BluetoothAdapter.checkBluetoothAddress(deviceAddress)) {
                Log.e(TAG, "Received address is faulty.")
                throw BleException("Received address is faulty.")
            }

            device = bluetoothAdapter.getRemoteDevice(deviceAddress)
            gatt = device.connectGatt(context, false, gattConnectionCallback)
        }
        catch (e: Exception) {
            _state.value = Result.failure(e)
        }
    }

    @SuppressLint("MissingPermission")
    override fun disconnect() {
        try {
            Log.d(TAG, "Disconnecting.")

            if (!bluetoothAdapter.isEnabled) {
                Log.e(TAG, "Bluetooth is not enabled")
                throw BleException("Bluetooth is not enabled.")
            }

            if (!checkForRequiredPermissions()) {
                Log.e(TAG, "App does not have required permissions.")
                throw BleException("App does not have the required permissions.")
            }

            gatt.disconnect()
        }
        catch (e: Exception) {
            _state.value = Result.failure(e)
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
}