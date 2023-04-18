package com.example.testble.ui.screens

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testble.data.BleConnectionController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class BleConnectionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val bleConnectionController: BleConnectionController
) : ViewModel() {

    private val _bleConnectionUiState = MutableStateFlow(BleConnectionUiState())
    val bleConnectionUiState = combine(
        bleConnectionController.connectionState,
        bleConnectionController.state,
        _bleConnectionUiState
    ) { connectionState, state, _uiState ->
        _uiState.copy(
            connectionState = connectionState,
            bleData = state
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        _bleConnectionUiState.value
    )

    private val deviceAddress: String = savedStateHandle["address"] ?: ""

    init {
        connect()
    }

    private fun connect() {
        bleConnectionController.connect(deviceAddress)
    }

    fun disconnect() {
        bleConnectionController.disconnect()
    }
}