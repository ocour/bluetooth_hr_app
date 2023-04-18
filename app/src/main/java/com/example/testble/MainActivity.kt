package com.example.testble

import android.bluetooth.BluetoothAdapter
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.testble.ui.Navigation
import com.example.testble.ui.theme.TestBleTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var bluetoothAdapter: BluetoothAdapter
    @Inject lateinit var locationManager: LocationManager

    private var isBluetoothEnabled by mutableStateOf(false)
    private var isGPSEnabled by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestBleTheme {
                // A surface container using the 'background' color from the theme
                Navigation(
                    onBluetoothStateChanged = { updateBluetoothState() },
                    isBluetoothEnabled = isBluetoothEnabled,
                    onGPSStateChanged = { updateGPSState() },
                    isGPSEnabled = isGPSEnabled
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateBluetoothState()
        updateGPSState()
    }

    private fun updateBluetoothState() {
        isBluetoothEnabled = bluetoothAdapter.isEnabled
    }

    private fun updateGPSState() {
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
}
