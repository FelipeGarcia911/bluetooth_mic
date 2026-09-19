package com.felipeg.bluetooth_mic.audio.settings

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingSettings
import com.felipeg.bluetooth_mic.audio.processing.AudioSourceProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioProcessingPreferencesTest {
    @Test
    fun `settings survive preference serialization`() {
        val expected = AudioProcessingSettings(
            inputGainDb = -9f,
            highPassCutoffHz = 120f,
            expanderThresholdDb = -34f,
            expanderRatio = 4f,
            echoCancellationEnabled = false,
            audioSourceProfile = AudioSourceProfile.VOICE_RECOGNITION,
        )
        val preferences = mutablePreferencesOf()

        AudioProcessingPreferences.write(preferences, expected)

        assertEquals(expected, AudioProcessingPreferences.read(preferences))
    }

    @Test
    fun `empty preferences restore recommended defaults`() {
        assertEquals(AudioProcessingSettings(), AudioProcessingPreferences.read(emptyPreferences()))
    }
}
