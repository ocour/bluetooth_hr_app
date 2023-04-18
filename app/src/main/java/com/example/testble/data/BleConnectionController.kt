package com.example.testble.data

import kotlinx.coroutines.flow.StateFlow
import java.lang.Thread.State

interface BleConnectionController {
    val connectionState: StateFlow<Int>
    val state: StateFlow<Result<BleData>>

    fun connect(deviceAddress: String)

    fun disconnect()

    companion object {
        const val STATE_DISCONNECTED: Int = 0
        const val STATE_CONNECTED: Int = 1
        const val STATE_CONNECTING: Int = 2
        const val STATE_READY: Int = 3
    }
}