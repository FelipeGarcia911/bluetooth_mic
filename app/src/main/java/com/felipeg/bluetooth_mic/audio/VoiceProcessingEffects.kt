package com.felipeg.bluetooth_mic.audio

import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AudioEffect
import android.media.audiofx.NoiseSuppressor
import android.util.Log

/** Owns optional platform voice-processing effects attached to one recording session. */
internal class VoiceProcessingEffects private constructor(
    private val echoCanceler: AcousticEchoCanceler?,
    private val noiseSuppressor: NoiseSuppressor?,
) : AutoCloseable {
    val isEchoCancellationEnabled: Boolean
        get() = echoCanceler?.enabled == true
    val isNoiseSuppressionEnabled: Boolean
        get() = noiseSuppressor?.enabled == true

    override fun close() {
        echoCanceler.releaseSafely()
        noiseSuppressor.releaseSafely()
    }

    private fun AudioEffect?.releaseSafely() {
        if (this == null) return
        try {
            release()
        } catch (exception: RuntimeException) {
            Log.w(TAG, "Could not release a voice-processing effect", exception)
        }
    }

    companion object {
        private const val TAG = "VoiceProcessing"

        fun attach(audioSessionId: Int): VoiceProcessingEffects {
            val echoCanceler = createEffect("AEC") {
                if (AcousticEchoCanceler.isAvailable()) AcousticEchoCanceler.create(audioSessionId) else null
            }
            val noiseSuppressor = createEffect("noise suppression") {
                if (NoiseSuppressor.isAvailable()) NoiseSuppressor.create(audioSessionId) else null
            }
            return VoiceProcessingEffects(echoCanceler, noiseSuppressor).also {
                Log.i(
                    TAG,
                    "AEC enabled=${it.isEchoCancellationEnabled}, " +
                        "noise suppression enabled=${it.isNoiseSuppressionEnabled}",
                )
            }
        }

        private inline fun <T : AudioEffect> createEffect(name: String, create: () -> T?): T? {
            var effect: T? = null
            return try {
                effect = create()
                effect?.also {
                    if (it.hasControl()) it.enabled = true
                }
            } catch (exception: RuntimeException) {
                try {
                    effect?.release()
                } catch (releaseException: RuntimeException) {
                    exception.addSuppressed(releaseException)
                }
                Log.w(TAG, "Could not enable $name", exception)
                null
            }
        }
    }
}
