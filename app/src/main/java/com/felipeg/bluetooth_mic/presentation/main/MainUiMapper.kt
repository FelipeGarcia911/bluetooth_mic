package com.felipeg.bluetooth_mic.presentation.main

import com.felipeg.bluetooth_mic.audio.AudioDeviceState
import com.felipeg.bluetooth_mic.audio.AudioInputKind
import com.felipeg.bluetooth_mic.audio.AudioInputOption
import com.felipeg.bluetooth_mic.audio.AudioOutputOption
import com.felipeg.bluetooth_mic.audio.MicrophoneMode
import com.felipeg.bluetooth_mic.audio.MicrophonePhase
import com.felipeg.bluetooth_mic.audio.MicrophoneProblem
import com.felipeg.bluetooth_mic.audio.MicrophoneState

internal data class PermissionUiState(
    val microphoneGranted: Boolean = false,
    val notificationsGranted: Boolean = false,
)

internal fun mapMainUiState(
    microphone: MicrophoneState,
    devices: AudioDeviceState,
    permissions: PermissionUiState,
): MainUiState {
    val inputs = devices.inputs.map { it.toUiModel(it.id == devices.selectedInputId) }
    val outputs = devices.outputs.map { it.toUiModel(it.id == devices.selectedOutputId) }
    return MainUiState(
        inputDevices = inputs,
        outputDevices = outputs,
        selectedInput = inputs.firstOrNull { it.selected },
        selectedOutput = outputs.firstOrNull { it.selected },
        microphoneLevel = microphone.microphoneLevel,
        transmissionState = microphone.phase.toUiState(),
        transmissionMode = microphone.mode?.toUiMode(),
        activeOutputName = microphone.outputName,
        message = microphone.problem?.toUiMessage(),
        microphoneGranted = permissions.microphoneGranted,
        notificationsGranted = permissions.notificationsGranted,
    )
}

private fun AudioInputOption.toUiModel(selected: Boolean) = AudioDeviceUiModel(
    id = id,
    name = if (kind == AudioInputKind.PHONE) "Phone microphone" else productName,
    subtitle = when (kind) {
        AudioInputKind.PHONE -> "Built-in microphone"
        AudioInputKind.BLUETOOTH -> "Bluetooth"
        AudioInputKind.WIRED -> "Wired headset"
        AudioInputKind.USB -> "USB"
        AudioInputKind.EXTERNAL -> "External microphone"
    },
    type = when (kind) {
        AudioInputKind.PHONE -> AudioDeviceUiType.PHONE
        AudioInputKind.BLUETOOTH -> AudioDeviceUiType.BLUETOOTH
        AudioInputKind.WIRED -> AudioDeviceUiType.WIRED
        AudioInputKind.USB -> AudioDeviceUiType.USB
        AudioInputKind.EXTERNAL -> AudioDeviceUiType.EXTERNAL
    },
    selected = selected,
)

private fun AudioOutputOption.toUiModel(selected: Boolean) = AudioDeviceUiModel(
    id = id,
    name = productName,
    subtitle = "Bluetooth output",
    type = AudioDeviceUiType.OUTPUT,
    selected = selected,
)

private fun MicrophonePhase.toUiState() = when (this) {
    MicrophonePhase.IDLE -> TransmissionUiState.IDLE
    MicrophonePhase.STARTING -> TransmissionUiState.STARTING
    MicrophonePhase.LIVE -> TransmissionUiState.LIVE
    MicrophonePhase.ERROR -> TransmissionUiState.ERROR
}

private fun MicrophoneMode.toUiMode() = when (this) {
    MicrophoneMode.HOLD -> TransmissionUiMode.HOLD
    MicrophoneMode.OPEN -> TransmissionUiMode.OPEN
}

private fun MicrophoneProblem.toUiMessage() = MainMessage.valueOf(name)
