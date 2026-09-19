package com.felipeg.bluetooth_mic.audio

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log

internal val microphoneAudioAttributes: AudioAttributes = AudioAttributes.Builder()
    .setUsage(AudioAttributes.USAGE_MEDIA)
    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
    .build()

/** Owns the native resources; construction releases partially created resources on failure. */
internal class AndroidPcmStreamFactory(
    private val manager: AudioManager,
    private val output: AudioDeviceInfo,
) : PcmStreamFactory {
    @SuppressLint("MissingPermission") // Checked by the service before scheduling the worker.
    override fun create(): PcmStream {
        val input = manager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC }
            ?: throw AudioException(MicrophoneProblem.NO_PHONE_MICROPHONE)
        val configuration = selectConfiguration()
        val recorder = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(configuration.format(AudioFormat.CHANNEL_IN_MONO))
            .setBufferSizeInBytes(configuration.inputBufferBytes)
            .build()
        var player: AudioTrack? = null
        try {
            player = AudioTrack.Builder()
                .setAudioAttributes(microphoneAudioAttributes)
                .setAudioFormat(configuration.format(AudioFormat.CHANNEL_OUT_MONO))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(configuration.outputBufferBytes)
                .build()
            if (recorder.state != AudioRecord.STATE_INITIALIZED || player.state != AudioTrack.STATE_INITIALIZED) {
                throw AudioException(MicrophoneProblem.INITIALIZATION_FAILED)
            }
            if (!recorder.setPreferredDevice(input) || !player.setPreferredDevice(output)) {
                throw AudioException(MicrophoneProblem.ROUTING_FAILED)
            }
            player.setVolume(0f)
            return AndroidPcmStream(recorder, player, input.id, output.id, configuration.chunkSamples)
        } catch (exception: Exception) {
            try { player?.release() } finally { recorder.release() }
            throw exception
        }
    }

    private fun selectConfiguration(): PcmConfiguration {
        for (rate in SUPPORTED_SAMPLE_RATES) {
            val inputMinimum = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val outputMinimum = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            if (inputMinimum > 0 && outputMinimum > 0) {
                return PcmConfiguration(rate, inputMinimum, outputMinimum)
            }
        }
        throw AudioException(MicrophoneProblem.UNSUPPORTED_FORMAT)
    }

    private companion object {
        val SUPPORTED_SAMPLE_RATES = listOf(48_000, 44_100)
    }
}

private data class PcmConfiguration(val sampleRate: Int, val inputMinimum: Int, val outputMinimum: Int) {
    val chunkSamples: Int = sampleRate * CHUNK_DURATION_MS / 1_000
    private val minimumBufferBytes = chunkSamples * BYTES_PER_SAMPLE * BUFFER_CHUNKS
    val inputBufferBytes = maxOf(minimumBufferBytes, inputMinimum)
    val outputBufferBytes = maxOf(minimumBufferBytes, outputMinimum)

    fun format(channels: Int): AudioFormat = AudioFormat.Builder()
        .setSampleRate(sampleRate)
        .setChannelMask(channels)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .build()

    private companion object {
        const val CHUNK_DURATION_MS = 10
        const val BYTES_PER_SAMPLE = 2
        const val BUFFER_CHUNKS = 2
    }
}

private class AndroidPcmStream(
    private val recorder: AudioRecord,
    private val player: AudioTrack,
    private val inputId: Int,
    private val outputId: Int,
    override val chunkSize: Int,
) : PcmStream {
    override val isRouteReady: Boolean
        get() = recorder.routedDevice?.id == inputId && player.routedDevice?.id == outputId
    override val isMicrophoneSilenced: Boolean
        get() = recorder.activeRecordingConfiguration?.isClientSilenced == true

    override fun start() {
        recorder.startRecording()
        player.play()
    }

    override fun read(buffer: ShortArray): Int {
        val count = recorder.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING)
        if (count < 0) throw AudioException(MicrophoneProblem.RECORDING_FAILED)
        return count
    }

    override fun write(buffer: ShortArray, offset: Int, count: Int): Int {
        val written = player.write(buffer, offset, count, AudioTrack.WRITE_NON_BLOCKING)
        if (written < 0) throw AudioException(MicrophoneProblem.PLAYBACK_FAILED)
        return written
    }

    override fun setMuted(muted: Boolean) {
        player.setVolume(if (muted) 0f else 1f)
    }

    override fun close() {
        // Each release is attempted even when a device was invalidated by Android.
        releaseSafely { player.pause() }
        releaseSafely { player.flush() }
        releaseSafely { player.release() }
        releaseSafely { recorder.stop() }
        releaseSafely { recorder.release() }
    }

    private inline fun releaseSafely(action: () -> Unit) {
        try {
            action()
        } catch (exception: RuntimeException) {
            Log.w("AndroidPcmStream", "Audio resource cleanup failed", exception)
        }
    }
}
