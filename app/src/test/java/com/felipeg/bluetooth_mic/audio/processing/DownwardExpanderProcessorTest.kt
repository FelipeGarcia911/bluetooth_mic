package com.felipeg.bluetooth_mic.audio.processing

import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Test

class DownwardExpanderProcessorTest {
    @Test
    fun `signal above threshold remains effectively unchanged`() {
        val buffer = ShortArray(1_000) { 10_000 }

        DownwardExpanderProcessor().process(buffer, buffer.size, 48_000)

        assertTrue(buffer.takeLast(100).all { abs(it.toInt() - 10_000) <= 1 })
    }

    @Test
    fun `signal below threshold is progressively attenuated`() {
        val buffer = ShortArray(4_800) { 100 }

        DownwardExpanderProcessor().process(buffer, buffer.size, 48_000)

        assertTrue(buffer.takeLast(480).map { abs(it.toInt()) }.average() < 35.0)
    }

    @Test
    fun `gain recovery is smoothed after a quiet signal`() {
        val processor = DownwardExpanderProcessor()
        val quiet = ShortArray(4_800) { 100 }
        processor.process(quiet, quiet.size, 48_000)
        val voice = ShortArray(4_800) { 10_000 }

        processor.process(voice, voice.size, 48_000)

        assertTrue(abs(voice.first().toInt()) < abs(voice.last().toInt()))
        assertTrue(voice.all { abs(it.toInt()) <= 10_000 })
    }
}
