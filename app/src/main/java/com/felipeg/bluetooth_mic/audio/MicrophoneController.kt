package com.felipeg.bluetooth_mic.audio

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.StateFlow

/** UI contract. Implementations own command transport and session lifetime. */
@MainThread
interface MicrophoneController {
    val state: StateFlow<MicrophoneState>
    fun start(mode: MicrophoneMode)
    fun stop()
    fun stopHeldMicrophone()
}

internal class AndroidMicrophoneController(
    private val context: Context,
    private val sessions: MicrophoneSessionStore,
) : MicrophoneController {
    override val state: StateFlow<MicrophoneState> = sessions.state

    override fun start(mode: MicrophoneMode) {
        val sessionId = sessions.begin(mode) ?: return
        try {
            ContextCompat.startForegroundService(context, MicrophoneService.startIntent(context, sessionId))
        } catch (exception: RuntimeException) {
            Log.e("MicrophoneController", "Service start failed", exception)
            sessions.finish(sessionId, MicrophoneProblem.START_FAILED)
        }
    }

    override fun stop() {
        sessions.cancel()
        context.stopService(Intent(context, MicrophoneService::class.java))
    }

    override fun stopHeldMicrophone() {
        if (state.value.mode == MicrophoneMode.HOLD) stop()
    }
}
