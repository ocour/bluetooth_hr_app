package com.example.testble.ui.screens

import com.example.testble.data.BleConnectionController
import com.example.testble.data.BleData

data class BleConnectionUiState(
    val connectionState: Int = BleConnectionController.STATE_DISCONNECTED,
    val bleData: Result<BleData> = Result.success(BleData())
)
