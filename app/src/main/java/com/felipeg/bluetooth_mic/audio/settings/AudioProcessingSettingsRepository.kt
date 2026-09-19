package com.felipeg.bluetooth_mic.audio.settings

import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingSettings
import com.felipeg.bluetooth_mic.audio.processing.AudioSourceProfile
import kotlinx.coroutines.flow.Flow

interface AudioProcessingSettingsRepository {
    val settings: Flow<AudioProcessingSettings>

    suspend fun updateInputGainDb(value: Float)
    suspend fun updateHighPassEnabled(enabled: Boolean)
    suspend fun updateHighPassCutoffHz(value: Float)
    suspend fun updateExpanderEnabled(enabled: Boolean)
    suspend fun updateExpanderThresholdDb(value: Float)
    suspend fun updateExpanderRatio(value: Float)
    suspend fun updateEchoCancellationEnabled(enabled: Boolean)
    suspend fun updateNoiseSuppressionEnabled(enabled: Boolean)
    suspend fun updateAudioSourceProfile(profile: AudioSourceProfile)
    suspend fun update(settings: AudioProcessingSettings)
    suspend fun reset()
}

internal fun interface AudioProcessingSettingsProvider {
    fun currentSettings(): AudioProcessingSettings
}
