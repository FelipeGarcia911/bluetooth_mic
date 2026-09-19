package com.felipeg.bluetooth_mic.audio

import androidx.annotation.MainThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Main-thread state owner. Session IDs reject cancelled commands and stale worker callbacks. */
@MainThread
internal class MicrophoneSessionStore {
    private var generation = 0L
    private var activeId: Long? = null
    private val mutableState = MutableStateFlow(MicrophoneState())
    val state = mutableState.asStateFlow()

    fun begin(mode: MicrophoneMode): Long? {
        if (state.value.isActive) return null
        val id = ++generation
        activeId = id
        mutableState.value = MicrophoneState(MicrophonePhase.STARTING, mode)
        return id
    }

    fun accepts(id: Long): Boolean = activeId == id

    fun markLive(id: Long, outputName: String) {
        if (!accepts(id)) return
        mutableState.value = state.value.copy(phase = MicrophonePhase.LIVE, outputName = outputName)
    }

    fun finish(id: Long, problem: MicrophoneProblem? = null) {
        if (!accepts(id)) return
        cancel(problem)
    }

    fun cancel(problem: MicrophoneProblem? = null) {
        activeId = null
        mutableState.value = if (problem == null) MicrophoneState() else
            MicrophoneState(phase = MicrophonePhase.ERROR, problem = problem)
    }
}
