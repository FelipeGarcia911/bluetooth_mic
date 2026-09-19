package com.felipeg.bluetooth_mic.presentation.main

import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingPreset

internal enum class AudioDeviceUiType {
    PHONE,
    BLUETOOTH,
    WIRED,
    USB,
    EXTERNAL,
    OUTPUT,
}

internal data class AudioDeviceUiModel(
    val id: Int,
    val name: String,
    val subtitle: String,
    val type: AudioDeviceUiType,
    val selected: Boolean,
)

internal enum class TransmissionUiState { IDLE, STARTING, LIVE, ERROR }
internal enum class TransmissionUiMode { HOLD, OPEN }

internal enum class MainMessage {
    PERMISSION_REQUIRED,
    NO_BLUETOOTH_OUTPUT,
    NO_AUDIO_INPUT,
    INPUT_ROUTE_UNAVAILABLE,
    UNSUPPORTED_FORMAT,
    INITIALIZATION_FAILED,
    ROUTING_FAILED,
    ROUTE_CHANGED,
    MICROPHONE_SILENCED,
    RECORDING_FAILED,
    PLAYBACK_FAILED,
    AUDIO_BUSY,
    AUDIO_INTERRUPTED,
    DEVICE_DISCONNECTED,
    START_FAILED,
}

internal data class MainUiState(
    val inputDevices: List<AudioDeviceUiModel> = emptyList(),
    val outputDevices: List<AudioDeviceUiModel> = emptyList(),
    val selectedInput: AudioDeviceUiModel? = null,
    val selectedOutput: AudioDeviceUiModel? = null,
    val microphoneLevel: Float = 0f,
    val transmissionState: TransmissionUiState = TransmissionUiState.IDLE,
    val transmissionMode: TransmissionUiMode? = null,
    val activeOutputName: String? = null,
    val message: MainMessage? = null,
    val microphoneGranted: Boolean = false,
    val notificationsGranted: Boolean = false,
    val audioProcessingPreset: AudioProcessingPreset = AudioProcessingPreset.ECHO_REDUCTION,
) {
    val isActive: Boolean
        get() = transmissionState == TransmissionUiState.STARTING || transmissionState == TransmissionUiState.LIVE

    val isHoldMode: Boolean
        get() = transmissionMode == TransmissionUiMode.HOLD

    val permissionsGranted: Boolean
        get() = microphoneGranted && notificationsGranted

    val canTalk: Boolean
        get() = microphoneGranted && selectedInput != null && selectedOutput != null
}
