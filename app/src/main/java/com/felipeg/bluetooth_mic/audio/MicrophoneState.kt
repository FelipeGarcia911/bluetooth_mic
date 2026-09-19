package com.felipeg.bluetooth_mic.audio

enum class MicrophoneMode { HOLD, OPEN }
enum class MicrophonePhase { IDLE, STARTING, LIVE, ERROR }
enum class MicrophoneProblem {
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

data class MicrophoneState(
    val phase: MicrophonePhase = MicrophonePhase.IDLE,
    val mode: MicrophoneMode? = null,
    val outputName: String? = null,
    val problem: MicrophoneProblem? = null,
    val microphoneLevel: Float = 0f,
) {
    val isActive: Boolean
        get() = phase == MicrophonePhase.STARTING || phase == MicrophonePhase.LIVE
}
