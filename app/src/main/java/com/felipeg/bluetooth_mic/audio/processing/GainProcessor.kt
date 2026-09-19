package com.felipeg.bluetooth_mic.audio.processing

import kotlin.math.pow
import kotlin.math.roundToInt

internal class GainProcessor : AudioProcessor {
    var gainDb: Float = 0f

    override fun process(buffer: ShortArray, count: Int, sampleRate: Int) {
        val linearGain = 10f.pow(gainDb.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB) / 20f)
        if (linearGain == 1f) return
        for (index in 0 until count) {
            buffer[index] = (buffer[index] * linearGain).roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
    }

    private companion object {
        const val MIN_GAIN_DB = -18f
        const val MAX_GAIN_DB = 0f
    }
}
