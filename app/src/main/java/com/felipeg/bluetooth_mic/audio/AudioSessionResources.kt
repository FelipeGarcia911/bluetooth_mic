package com.felipeg.bluetooth_mic.audio

import android.annotation.SuppressLint
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.PowerManager
import android.util.Log

/** Acquires and releases the Android privileges needed by a live audio session. */
internal class AudioSessionResources(
    private val manager: AudioManager,
    private val powerManager: PowerManager,
    private val handler: Handler,
) : AutoCloseable {
    private var focus: AudioFocusRequest? = null
    private var wakeLock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout") // User-controlled FGS session; close is called on every exit and onDestroy.
    fun acquire(onFocusLost: () -> Unit) {
        close()
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(microphoneAudioAttributes)
            .setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener({ change ->
                if (change != AudioManager.AUDIOFOCUS_GAIN) onFocusLost()
            }, handler)
            .build()
        focus = request
        if (manager.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            throw AudioException(MicrophoneProblem.AUDIO_BUSY)
        }
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothMic:audio")
        wakeLock?.acquire()
    }

    override fun close() {
        val previousFocus = focus
        val previousWakeLock = wakeLock
        focus = null
        wakeLock = null
        try {
            previousFocus?.let { manager.abandonAudioFocusRequest(it) }
        } catch (exception: RuntimeException) {
            Log.w("AudioSessionResources", "Cannot abandon audio focus", exception)
        } finally {
            if (previousWakeLock?.isHeld == true) previousWakeLock.release()
        }
    }
}
