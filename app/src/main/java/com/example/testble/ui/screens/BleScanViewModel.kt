package com.example.testble.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testble.data.BleScanController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BleScanViewModel @Inject constructor(
    private val bleScanControllerImpl: BleScanController
) : ViewModel() {

    private val _bleUiState = MutableStateFlow(BleUiState())
    val bleUiState = combine(
        bleScanControllerImpl.scannedDevices,
        bleScanControllerImpl.pairedDevices,
        bleScanControllerImpl.isScanning,
        _bleUiState
    ) { scannedDevices, pairedDevices, isScanning, _bleUiState ->
        _bleUiState.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            isScanning = isScanning
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        _bleUiState.value
    )

    fun startBleScan() {
        bleScanControllerImpl.startLeDevicesScan()
    }

    fun stopBleScan() {
        bleScanControllerImpl.stopLeDevicesScan()
    }

    fun getPairedBleDevices() {
        bleScanControllerImpl.getPairedBleDevices()
    }
}