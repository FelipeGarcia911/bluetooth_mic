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
import com.felipeg.bluetooth_mic.audio.settings.DataStoreAudioProcessingSettingsRepository
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MicrophoneApplication : Application() {
    internal val container by lazy { MicrophoneContainer(this) }
}

/** Composition root. Application scope preserves sessions across activity recreation. */
internal class MicrophoneContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val sessions = MicrophoneSessionStore()
    val controller: MicrophoneController = AndroidMicrophoneController(context, sessions)
    private val audioManager = context.getSystemService(AudioManager::class.java)
    val devices = AudioDeviceRepository(audioManager)
    val processingSettings = DataStoreAudioProcessingSettingsRepository(context, applicationScope)
    private val audioWorker = Executors.newSingleThreadExecutor { task -> Thread(task, "MicrophoneAudio") }

    fun createEngine(
        input: AudioDeviceInfo,
        output: AudioDeviceInfo,
        onLive: () -> Unit,
        onLevel: (Float) -> Unit,
        onFinished: (MicrophoneProblem?) -> Unit,
    ): AudioEngine {
        val settings = processingSettings.currentSettings()
        return AudioLoop(
            streamFactory = AndroidPcmStreamFactory(input, output, settings),
            processingSettings = processingSettings,
            sessionSettings = settings,
            worker = audioWorker,
            onLive = onLive,
            onLevel = onLevel,
            onFinished = onFinished,
        )
    }
}
