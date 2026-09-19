package com.felipeg.bluetooth_mic.audio.settings

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingSettings
import com.felipeg.bluetooth_mic.audio.processing.AudioSourceProfile
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.audioProcessingDataStore by preferencesDataStore(name = "audio_processing_settings")

internal class DataStoreAudioProcessingSettingsRepository(
    context: Context,
    scope: CoroutineScope,
) : AudioProcessingSettingsRepository, AudioProcessingSettingsProvider {
    private val dataStore = context.audioProcessingDataStore
    private val mutableSettings = MutableStateFlow(AudioProcessingSettings())
    private val snapshot = AtomicReference(mutableSettings.value)
    override val settings = mutableSettings.asStateFlow()

    init {
        scope.launch {
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        Log.w(TAG, "Could not read audio processing settings; using defaults", exception)
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map(AudioProcessingPreferences::read)
                .collect(::publish)
        }
    }

    override fun currentSettings(): AudioProcessingSettings = snapshot.get()

    override suspend fun updateInputGainDb(value: Float) = updateCurrent { it.copy(inputGainDb = value.coerceIn(-18f, 0f)) }
    override suspend fun updateHighPassEnabled(enabled: Boolean) = updateCurrent { it.copy(highPassEnabled = enabled) }
    override suspend fun updateHighPassCutoffHz(value: Float) = updateCurrent { it.copy(highPassCutoffHz = value.coerceIn(80f, 150f)) }
    override suspend fun updateExpanderEnabled(enabled: Boolean) = updateCurrent { it.copy(expanderEnabled = enabled) }
    override suspend fun updateExpanderThresholdDb(value: Float) =
        updateCurrent { it.copy(expanderThresholdDb = value.coerceIn(-60f, -20f)) }
    override suspend fun updateExpanderRatio(value: Float) = updateCurrent { it.copy(expanderRatio = value.coerceIn(1f, 6f)) }
    override suspend fun updateEchoCancellationEnabled(enabled: Boolean) =
        updateCurrent { it.copy(echoCancellationEnabled = enabled) }
    override suspend fun updateNoiseSuppressionEnabled(enabled: Boolean) =
        updateCurrent { it.copy(noiseSuppressionEnabled = enabled) }
    override suspend fun updateAudioSourceProfile(profile: AudioSourceProfile) =
        updateCurrent { it.copy(audioSourceProfile = profile) }

    override suspend fun update(settings: AudioProcessingSettings) {
        val sanitized = settings.sanitized()
        publish(sanitized)
        dataStore.edit { AudioProcessingPreferences.write(it, sanitized) }
    }

    override suspend fun reset() {
        val defaults = AudioProcessingSettings()
        publish(defaults)
        dataStore.edit { it.clear() }
    }

    private suspend fun updateCurrent(transform: (AudioProcessingSettings) -> AudioProcessingSettings) {
        update(transform(snapshot.get()))
    }

    private fun publish(value: AudioProcessingSettings) {
        snapshot.set(value)
        mutableSettings.value = value
    }

    private fun AudioProcessingSettings.sanitized() = copy(
        inputGainDb = inputGainDb.coerceIn(-18f, 0f),
        highPassCutoffHz = highPassCutoffHz.coerceIn(80f, 150f),
        expanderThresholdDb = expanderThresholdDb.coerceIn(-60f, -20f),
        expanderRatio = expanderRatio.coerceIn(1f, 6f),
    )

    private companion object {
        const val TAG = "AudioProcessingSettings"
    }
}

internal object AudioProcessingPreferences {
    private val inputGainDb = floatPreferencesKey("input_gain_db")
    private val highPassEnabled = booleanPreferencesKey("high_pass_enabled")
    private val highPassCutoffHz = floatPreferencesKey("high_pass_cutoff_hz")
    private val expanderEnabled = booleanPreferencesKey("expander_enabled")
    private val expanderThresholdDb = floatPreferencesKey("expander_threshold_db")
    private val expanderRatio = floatPreferencesKey("expander_ratio")
    private val echoCancellationEnabled = booleanPreferencesKey("echo_cancellation_enabled")
    private val noiseSuppressionEnabled = booleanPreferencesKey("noise_suppression_enabled")
    private val audioSourceProfile = stringPreferencesKey("audio_source_profile")

    fun read(preferences: Preferences): AudioProcessingSettings {
        val defaults = AudioProcessingSettings()
        return AudioProcessingSettings(
            inputGainDb = preferences[inputGainDb] ?: defaults.inputGainDb,
            highPassEnabled = preferences[highPassEnabled] ?: defaults.highPassEnabled,
            highPassCutoffHz = preferences[highPassCutoffHz] ?: defaults.highPassCutoffHz,
            expanderEnabled = preferences[expanderEnabled] ?: defaults.expanderEnabled,
            expanderThresholdDb = preferences[expanderThresholdDb] ?: defaults.expanderThresholdDb,
            expanderRatio = preferences[expanderRatio] ?: defaults.expanderRatio,
            echoCancellationEnabled = preferences[echoCancellationEnabled] ?: defaults.echoCancellationEnabled,
            noiseSuppressionEnabled = preferences[noiseSuppressionEnabled] ?: defaults.noiseSuppressionEnabled,
            audioSourceProfile = preferences[audioSourceProfile]
                ?.let { stored -> AudioSourceProfile.entries.firstOrNull { it.name == stored } }
                ?: defaults.audioSourceProfile,
        )
    }

    fun write(preferences: MutablePreferences, settings: AudioProcessingSettings) {
        preferences[inputGainDb] = settings.inputGainDb
        preferences[highPassEnabled] = settings.highPassEnabled
        preferences[highPassCutoffHz] = settings.highPassCutoffHz
        preferences[expanderEnabled] = settings.expanderEnabled
        preferences[expanderThresholdDb] = settings.expanderThresholdDb
        preferences[expanderRatio] = settings.expanderRatio
        preferences[echoCancellationEnabled] = settings.echoCancellationEnabled
        preferences[noiseSuppressionEnabled] = settings.noiseSuppressionEnabled
        preferences[audioSourceProfile] = settings.audioSourceProfile.name
    }
}
