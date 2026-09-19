package com.felipeg.bluetooth_mic.audio.processing

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

internal class DownwardExpanderProcessor : AudioProcessor {
    var enabled: Boolean = true
        set(value) {
            if (field && !value) smoothedGain = 1f
            field = value
        }
    var thresholdDb: Float = -38f
    var ratio: Float = 3f

    private var smoothedGain = 1f
    private var configuredRate = 0
    private var attackCoefficient = 1f
    private var releaseCoefficient = 1f

    override fun process(buffer: ShortArray, count: Int, sampleRate: Int) {
        if (!enabled || count <= 0) return
        updateCoefficients(sampleRate)
        val threshold = thresholdDb.coerceIn(-60f, -20f)
        val expansionRatio = ratio.coerceIn(1f, 6f)
        var currentGain = smoothedGain
        for (index in 0 until count) {
            val magnitude = abs(buffer[index].toInt()).coerceAtLeast(1) / Short.MAX_VALUE.toFloat()
            val inputDb = 20f * log10(magnitude)
            val gainDb = if (inputDb < threshold) {
                ((threshold + (inputDb - threshold) * expansionRatio) - inputDb).coerceAtLeast(MIN_GAIN_DB)
            } else {
                0f
            }
            val targetGain = 10f.pow(gainDb / 20f)
            val smoothing = if (targetGain < currentGain) attackCoefficient else releaseCoefficient
            currentGain += (targetGain - currentGain) * smoothing
            buffer[index] = (buffer[index] * currentGain).roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        smoothedGain = currentGain
    }

    private fun updateCoefficients(sampleRate: Int) {
        if (configuredRate == sampleRate) return
        attackCoefficient = smoothingCoefficient(ATTACK_MS, sampleRate)
        releaseCoefficient = smoothingCoefficient(RELEASE_MS, sampleRate)
        configuredRate = sampleRate
    }

    private fun smoothingCoefficient(milliseconds: Float, sampleRate: Int): Float =
        (1.0 - exp(-1.0 / (milliseconds * 0.001 * sampleRate))).toFloat()

    private companion object {
        const val ATTACK_MS = 10f
        const val RELEASE_MS = 150f
        const val MIN_GAIN_DB = -80f
    }
}
