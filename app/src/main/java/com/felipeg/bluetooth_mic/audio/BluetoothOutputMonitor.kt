package com.felipeg.bluetooth_mic.audio

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Observes available media outputs without scanning or managing Bluetooth pairing. */
internal class BluetoothOutputMonitor(private val manager: AudioManager) : AutoCloseable {
    private val mutableOutputName = MutableStateFlow<String?>(null)
    val outputName = mutableOutputName.asStateFlow()
    private var registered = false
    private val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>) = refresh()
        override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) = refresh()
    }

    fun start() {
        if (registered) return
        manager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        registered = true
        refresh()
    }

    private fun refresh() {
        mutableOutputName.value = manager.bluetoothOutputs().firstOrNull()?.productName?.toString()
    }

    override fun close() {
        if (!registered) return
        manager.unregisterAudioDeviceCallback(callback)
        registered = false
    }
}
