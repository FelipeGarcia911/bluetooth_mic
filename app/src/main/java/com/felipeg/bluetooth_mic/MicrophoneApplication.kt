package com.felipeg.bluetooth_mic

import android.app.Application
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import com.felipeg.bluetooth_mic.audio.AndroidMicrophoneController
import com.felipeg.bluetooth_mic.audio.AndroidPcmStreamFactory
import com.felipeg.bluetooth_mic.audio.AudioDeviceRepository
import com.felipeg.bluetooth_mic.audio.AudioEngine
import com.felipeg.bluetooth_mic.audio.AudioLoop
import com.felipeg.bluetooth_mic.audio.MicrophoneController
import com.felipeg.bluetooth_mic.audio.MicrophoneProblem
import com.felipeg.bluetooth_mic.audio.MicrophoneSessionStore
import java.util.concurrent.Executors

class MicrophoneApplication : Application() {
    internal val container by lazy { MicrophoneContainer(this) }
}

/** Composition root. Application scope preserves sessions across activity recreation. */
internal class MicrophoneContainer(context: Context) {
    val sessions = MicrophoneSessionStore()
    val controller: MicrophoneController = AndroidMicrophoneController(context, sessions)
    private val audioManager = context.getSystemService(AudioManager::class.java)
    val devices = AudioDeviceRepository(audioManager)
    private val audioWorker = Executors.newSingleThreadExecutor { task -> Thread(task, "MicrophoneAudio") }

    fun createEngine(
        input: AudioDeviceInfo,
        output: AudioDeviceInfo,
        onLive: () -> Unit,
        onFinished: (MicrophoneProblem?) -> Unit,
    ): AudioEngine = AudioLoop(AndroidPcmStreamFactory(input, output), audioWorker, onLive, onFinished)
}
