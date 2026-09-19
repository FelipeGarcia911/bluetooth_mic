package com.felipeg.bluetooth_mic.presentation.processing

import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingPreset
import com.felipeg.bluetooth_mic.audio.processing.AudioSourceProfile

internal sealed interface AudioProcessingUiAction {
    data class SelectPreset(val preset: AudioProcessingPreset) : AudioProcessingUiAction
    data class SetInputGain(val db: Float) : AudioProcessingUiAction
    data class SetHighPassEnabled(val enabled: Boolean) : AudioProcessingUiAction
    data class SetHighPassCutoff(val hz: Float) : AudioProcessingUiAction
    data class SetExpanderEnabled(val enabled: Boolean) : AudioProcessingUiAction
    data class SetExpanderThreshold(val db: Float) : AudioProcessingUiAction
    data class SetExpanderRatio(val ratio: Float) : AudioProcessingUiAction
    data class SetAecEnabled(val enabled: Boolean) : AudioProcessingUiAction
    data class SetNoiseSuppressionEnabled(val enabled: Boolean) : AudioProcessingUiAction
    data class SetAudioSourceProfile(val profile: AudioSourceProfile) : AudioProcessingUiAction
    data object ResetPreset : AudioProcessingUiAction
}
