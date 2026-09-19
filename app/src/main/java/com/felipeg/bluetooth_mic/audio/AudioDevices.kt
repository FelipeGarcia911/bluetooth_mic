package com.felipeg.bluetooth_mic.audio

import android.media.AudioDeviceInfo
import android.media.AudioManager

internal fun AudioDeviceInfo.isBluetoothOutput(): Boolean = isSink &&
    (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
        type == AudioDeviceInfo.TYPE_BLE_HEADSET)

internal fun AudioManager.bluetoothOutputs(): List<AudioDeviceInfo> =
    getDevices(AudioManager.GET_DEVICES_OUTPUTS).filter { it.isBluetoothOutput() }

internal fun AudioManager.selectableInputs(): List<AudioDeviceInfo> =
    getDevices(AudioManager.GET_DEVICES_INPUTS)
        .filter(AudioDeviceInfo::isSelectableInput)
        .sortedBy { it.inputPriority() }

internal fun AudioDeviceInfo.isBluetoothInput(): Boolean = isSource &&
    (type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || type == AudioDeviceInfo.TYPE_BLE_HEADSET)

internal fun AudioManager.communicationDeviceFor(input: AudioDeviceInfo): AudioDeviceInfo? {
    if (!input.isBluetoothInput()) return null
    val candidates = availableCommunicationDevices.filter { candidate ->
        candidate.type == input.type ||
            (input.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO && candidate.type == AudioDeviceInfo.TYPE_BLE_HEADSET) ||
            (input.type == AudioDeviceInfo.TYPE_BLE_HEADSET && candidate.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
    }
    return candidates.firstOrNull { it.address.isNotEmpty() && it.address == input.address }
        ?: candidates.firstOrNull { it.productName.toString() == input.productName.toString() }
        ?: candidates.singleOrNull()
}

private fun AudioDeviceInfo.isSelectableInput(): Boolean = isSource && type in setOf(
    AudioDeviceInfo.TYPE_BUILTIN_MIC,
    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
    AudioDeviceInfo.TYPE_BLE_HEADSET,
    AudioDeviceInfo.TYPE_WIRED_HEADSET,
    AudioDeviceInfo.TYPE_USB_ACCESSORY,
    AudioDeviceInfo.TYPE_USB_DEVICE,
    AudioDeviceInfo.TYPE_USB_HEADSET,
    AudioDeviceInfo.TYPE_LINE_ANALOG,
    AudioDeviceInfo.TYPE_AUX_LINE,
)

private fun AudioDeviceInfo.inputPriority(): Int = when (type) {
    AudioDeviceInfo.TYPE_BUILTIN_MIC -> 0
    AudioDeviceInfo.TYPE_BLUETOOTH_SCO, AudioDeviceInfo.TYPE_BLE_HEADSET -> 1
    AudioDeviceInfo.TYPE_WIRED_HEADSET -> 2
    AudioDeviceInfo.TYPE_USB_ACCESSORY, AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> 3
    else -> 4
}
