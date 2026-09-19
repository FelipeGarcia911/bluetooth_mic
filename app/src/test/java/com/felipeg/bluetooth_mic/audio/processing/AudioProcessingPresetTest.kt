package com.felipeg.bluetooth_mic.audio.processing

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioProcessingPresetTest {
    @Test
    fun `echo reduction settings detect echo reduction`() {
        assertEquals(
            AudioProcessingPreset.ECHO_REDUCTION,
            detectPreset(settingsForPreset(AudioProcessingPreset.ECHO_REDUCTION)),
        )
    }

    @Test
    fun `manual change detects custom`() {
        assertEquals(
            AudioProcessingPreset.CUSTOM,
            detectPreset(AudioProcessingSettings().copy(inputGainDb = -5f)),
        )
    }

    @Test
    fun `preset detection tolerates insignificant float differences`() {
        assertEquals(
            AudioProcessingPreset.ECHO_REDUCTION,
            detectPreset(AudioProcessingSettings().copy(expanderRatio = 3.005f)),
        )
    }
}
