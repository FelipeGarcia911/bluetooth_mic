package com.felipeg.bluetooth_mic.ui

import androidx.annotation.StringRes
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.audio.MicrophoneProblem

@StringRes
internal fun MicrophoneProblem?.messageResource(): Int = when (this) {
    MicrophoneProblem.PERMISSION_REQUIRED -> R.string.error_microphone_permission
    MicrophoneProblem.NO_BLUETOOTH_OUTPUT -> R.string.connect_speaker
    MicrophoneProblem.NO_PHONE_MICROPHONE -> R.string.error_no_microphone
    MicrophoneProblem.UNSUPPORTED_FORMAT -> R.string.error_audio_format
    MicrophoneProblem.INITIALIZATION_FAILED -> R.string.error_audio_initialization
    MicrophoneProblem.ROUTING_FAILED -> R.string.error_audio_routing
    MicrophoneProblem.ROUTE_CHANGED -> R.string.error_route_changed
    MicrophoneProblem.MICROPHONE_SILENCED -> R.string.error_microphone_silenced
    MicrophoneProblem.RECORDING_FAILED -> R.string.error_audio_capture
    MicrophoneProblem.PLAYBACK_FAILED -> R.string.error_audio_playback
    MicrophoneProblem.AUDIO_BUSY -> R.string.error_audio_busy
    MicrophoneProblem.AUDIO_INTERRUPTED -> R.string.error_audio_interrupted
    MicrophoneProblem.DEVICE_DISCONNECTED -> R.string.error_disconnected
    MicrophoneProblem.START_FAILED, null -> R.string.error_start
}
