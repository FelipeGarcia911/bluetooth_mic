package com.felipeg.bluetooth_mic.audio.processing

import kotlin.math.abs

enum class AudioSourceProfile {
    VOICE_COMMUNICATION,
    VOICE_RECOGNITION,
}

data class AudioProcessingSettings(
    val inputGainDb: Float = -6f,
    val highPassEnabled: Boolean = true,
    val highPassCutoffHz: Float = 100f,
    val expanderEnabled: Boolean = true,
    val expanderThresholdDb: Float = -38f,
    val expanderRatio: Float = 3f,
    val echoCancellationEnabled: Boolean = true,
    val noiseSuppressionEnabled: Boolean = true,
    val audioSourceProfile: AudioSourceProfile = AudioSourceProfile.VOICE_COMMUNICATION,
)

enum class AudioProcessingPreset {
    NATURAL,
    ECHO_REDUCTION,
    AGGRESSIVE,
    CUSTOM,
}

fun settingsForPreset(preset: AudioProcessingPreset): AudioProcessingSettings = when (preset) {
    AudioProcessingPreset.NATURAL -> AudioProcessingSettings(
        inputGainDb = 0f,
        highPassCutoffHz = 80f,
        expanderEnabled = false,
    )
    AudioProcessingPreset.ECHO_REDUCTION -> AudioProcessingSettings()
    AudioProcessingPreset.AGGRESSIVE -> AudioProcessingSettings(
        inputGainDb = -9f,
        highPassCutoffHz = 120f,
        expanderThresholdDb = -34f,
        expanderRatio = 4f,
    )
    AudioProcessingPreset.CUSTOM -> AudioProcessingSettings()
}

fun detectPreset(settings: AudioProcessingSettings): AudioProcessingPreset = when {
    settings.matches(settingsForPreset(AudioProcessingPreset.NATURAL)) -> AudioProcessingPreset.NATURAL
    settings.matches(settingsForPreset(AudioProcessingPreset.ECHO_REDUCTION)) -> AudioProcessingPreset.ECHO_REDUCTION
    settings.matches(settingsForPreset(AudioProcessingPreset.AGGRESSIVE)) -> AudioProcessingPreset.AGGRESSIVE
    else -> AudioProcessingPreset.CUSTOM
}

private fun AudioProcessingSettings.matches(other: AudioProcessingSettings): Boolean =
    closeTo(inputGainDb, other.inputGainDb) &&
        highPassEnabled == other.highPassEnabled &&
        closeTo(highPassCutoffHz, other.highPassCutoffHz) &&
        expanderEnabled == other.expanderEnabled &&
        closeTo(expanderThresholdDb, other.expanderThresholdDb) &&
        closeTo(expanderRatio, other.expanderRatio) &&
        echoCancellationEnabled == other.echoCancellationEnabled &&
        noiseSuppressionEnabled == other.noiseSuppressionEnabled &&
        audioSourceProfile == other.audioSourceProfile

private fun closeTo(first: Float, second: Float): Boolean = abs(first - second) <= 0.01f
