package com.felipeg.bluetooth_mic.audio.processing

import kotlin.math.PI
import kotlin.math.roundToInt

internal class HighPassProcessor : AudioProcessor {
    var enabled: Boolean = true
        set(value) {
            if (field && !value) reset()
            field = value
        }
    var cutoffHz: Float = 100f

    private var previousInput = 0f
    private var previousOutput = 0f
    private var configuredRate = 0
    private var configuredCutoff = Float.NaN
    private var coefficient = 0f

    override fun process(buffer: ShortArray, count: Int, sampleRate: Int) {
        if (!enabled || count <= 0) return
        updateCoefficient(sampleRate)
        var inputState = previousInput
        var outputState = previousOutput
        for (index in 0 until count) {
            val input = buffer[index].toFloat()
            outputState = coefficient * (outputState + input - inputState)
            inputState = input
            buffer[index] = outputState.roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        previousInput = inputState
        previousOutput = outputState
    }

    fun reset() {
        previousInput = 0f
        previousOutput = 0f
    }

    private fun updateCoefficient(sampleRate: Int) {
        val cutoff = cutoffHz.coerceIn(20f, sampleRate * 0.45f)
        if (configuredRate == sampleRate && configuredCutoff == cutoff) return
        val timeStep = 1.0 / sampleRate
        val rc = 1.0 / (2.0 * PI * cutoff)
        coefficient = (rc / (rc + timeStep)).toFloat()
        configuredRate = sampleRate
        configuredCutoff = cutoff
    }
}
