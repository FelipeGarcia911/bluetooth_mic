package com.felipeg.bluetooth_mic.audio

import android.media.AudioDeviceInfo
import android.media.AudioManager

internal fun AudioDeviceInfo.isBluetoothOutput(): Boolean = isSink &&
    (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
        type == AudioDeviceInfo.TYPE_BLE_HEADSET)

internal fun AudioManager.bluetoothOutputs(): List<AudioDeviceInfo> =
    getDevices(AudioManager.GET_DEVICES_OUTPUTS).filter { it.isBluetoothOutput() }
