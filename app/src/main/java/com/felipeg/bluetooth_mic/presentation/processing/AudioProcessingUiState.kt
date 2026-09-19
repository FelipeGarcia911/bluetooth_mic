package com.felipeg.bluetooth_mic.presentation.processing

import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingPreset
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingSettings

internal data class AudioProcessingUiState(
    val preset: AudioProcessingPreset = AudioProcessingPreset.ECHO_REDUCTION,
    val settings: AudioProcessingSettings = AudioProcessingSettings(),
    val sessionActive: Boolean = false,
)
