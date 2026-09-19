package com.felipeg.bluetooth_mic.audio.processing

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import org.junit.Assert.assertTrue
import org.junit.Test

class HighPassProcessorTest {
    @Test
    fun `constant signal is strongly attenuated`() {
        val buffer = ShortArray(4_800) { 10_000 }

        HighPassProcessor().apply { cutoffHz = 100f }.process(buffer, buffer.size, 48_000)

        val tailAverage = buffer.takeLast(480).sumOf { abs(it.toInt()) } / 480.0
        assertTrue(tailAverage < 100.0)
    }

    @Test
    fun `voice-band signal is reasonably preserved`() {
        val sampleRate = 48_000
        val input = ShortArray(4_800) { index ->
            (10_000 * sin(2.0 * PI * 1_000 * index / sampleRate)).toInt().toShort()
        }
        val originalAverage = input.sumOf { abs(it.toInt()) } / input.size.toDouble()

        HighPassProcessor().apply { cutoffHz = 100f }.process(input, input.size, sampleRate)

        val processedAverage = input.takeLast(2_400).sumOf { abs(it.toInt()) } / 2_400.0
        assertTrue(processedAverage > originalAverage * 0.85)
    }
}
