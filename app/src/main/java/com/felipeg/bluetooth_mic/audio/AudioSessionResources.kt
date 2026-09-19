package com.felipeg.bluetooth_mic.audio

import android.annotation.SuppressLint
import android.media.AudioDeviceInfo
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
    private var previousAudioMode: Int? = null
    private var communicationDeviceSelected = false

    @SuppressLint("WakelockTimeout") // User-controlled FGS session; close is called on every exit and onDestroy.
    fun acquire(communicationDevice: AudioDeviceInfo?, onFocusLost: () -> Unit) {
        close()
        try {
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
            previousAudioMode = manager.mode
            manager.mode = AudioManager.MODE_IN_COMMUNICATION
            if (communicationDevice != null) {
                if (!manager.setCommunicationDevice(communicationDevice)) {
                    throw AudioException(MicrophoneProblem.INPUT_ROUTE_UNAVAILABLE)
                }
                communicationDeviceSelected = true
            }
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothMic:audio")
            wakeLock?.acquire()
        } catch (exception: Exception) {
            close()
            throw exception
        }
    }

    override fun close() {
        val previousFocus = focus
        val previousWakeLock = wakeLock
        val modeToRestore = previousAudioMode
        val shouldClearCommunicationDevice = communicationDeviceSelected
        focus = null
        wakeLock = null
        previousAudioMode = null
        communicationDeviceSelected = false
        try {
            previousFocus?.let { manager.abandonAudioFocusRequest(it) }
        } catch (exception: RuntimeException) {
            Log.w("AudioSessionResources", "Cannot abandon audio focus", exception)
        } finally {
            try {
                if (previousWakeLock?.isHeld == true) previousWakeLock.release()
            } finally {
                if (shouldClearCommunicationDevice) {
                    try {
                        manager.clearCommunicationDevice()
                    } catch (exception: RuntimeException) {
                        Log.w("AudioSessionResources", "Cannot clear the communication device", exception)
                    }
                }
                if (modeToRestore != null && manager.mode == AudioManager.MODE_IN_COMMUNICATION) {
                    try {
                        manager.mode = modeToRestore
                    } catch (exception: RuntimeException) {
                        Log.w("AudioSessionResources", "Cannot restore the audio mode", exception)
                    }
                }
            }
        }
    }
}
