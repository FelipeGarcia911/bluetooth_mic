package com.felipeg.bluetooth_mic.audio.processing

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class GainProcessorTest {
    @Test
    fun `zero decibels does not modify samples`() {
        val buffer = shortArrayOf(Short.MIN_VALUE, -1_000, 0, 1_000, Short.MAX_VALUE)
        val original = buffer.copyOf()

        GainProcessor().apply { gainDb = 0f }.process(buffer, buffer.size, 48_000)

        assertArrayEquals(original, buffer)
    }

    @Test
    fun `minus six decibels reduces amplitude by approximately half`() {
        val buffer = shortArrayOf(-20_000, 20_000)

        GainProcessor().apply { gainDb = -6f }.process(buffer, buffer.size, 48_000)

        assertEquals(-10_024, buffer[0].toInt(), 2)
        assertEquals(10_024, buffer[1].toInt(), 2)
    }

    @Test
    fun `positive gain is rejected and samples remain inside short range`() {
        val buffer = shortArrayOf(Short.MIN_VALUE, Short.MAX_VALUE)

        GainProcessor().apply { gainDb = 12f }.process(buffer, buffer.size, 48_000)

        assertEquals(Short.MIN_VALUE, buffer[0])
        assertEquals(Short.MAX_VALUE, buffer[1])
    }
}
