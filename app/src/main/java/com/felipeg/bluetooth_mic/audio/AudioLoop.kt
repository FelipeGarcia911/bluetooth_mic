package com.felipeg.bluetooth_mic.audio

import android.os.Process
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/** Pumps audio on the supplied serial worker; callbacks are delivered on that worker. */
internal class AudioLoop(
    private val streamFactory: PcmStreamFactory,
    private val worker: Executor,
    private val onLive: () -> Unit,
    private val onFinished: (MicrophoneProblem?) -> Unit,
) : AudioEngine {
    private val started = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)
    private val streamLock = Any()
    private var activeStream: PcmStream? = null

    override fun start() {
        check(started.compareAndSet(false, true)) { "An audio engine can only start once" }
        worker.execute(::runSession)
    }

    override fun stop() {
        cancelled.set(true)
        synchronized(streamLock) { activeStream?.let(::muteSafely) }
    }

    private fun runSession() {
        var problem: MicrophoneProblem? = null
        try {
            if (cancelled.get()) return
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            streamFactory.create().use { stream ->
                try {
                    synchronized(streamLock) { activeStream = stream }
                    if (!cancelled.get()) {
                        stream.start()
                        pumpAudio(stream)
                    }
                } finally {
                    // stop() cannot access a stream after its resources have been released.
                    synchronized(streamLock) {
                        activeStream = null
                        muteSafely(stream)
                    }
                }
            }
        } catch (exception: Exception) {
            if (!cancelled.get()) {
                Log.e(TAG, "Audio session failed", exception)
                problem = (exception as? AudioException)?.problem ?: MicrophoneProblem.INITIALIZATION_FAILED
            }
        } finally {
            onFinished(problem)
        }
    }

    private fun muteSafely(stream: PcmStream) {
        try {
            stream.setMuted(true)
        } catch (exception: RuntimeException) {
            Log.w(TAG, "Cannot mute an invalidated audio output", exception)
        }
    }

    private fun pumpAudio(stream: PcmStream) {
        val buffer = ShortArray(stream.chunkSize)
        val routeDeadline = SystemClock.elapsedRealtime() + ROUTE_TIMEOUT_MS
        var transmitting = false
        while (!cancelled.get()) {
            val routeReady = stream.isRouteReady
            verifyRoute(routeReady, transmitting, routeDeadline)
            if (stream.isMicrophoneSilenced) throw AudioException(MicrophoneProblem.MICROPHONE_SILENCED)

            val count = stream.read(buffer)
            if (!routeReady) buffer.fill(0)
            if (routeReady && !transmitting) {
                enableOutput(stream)
                transmitting = true
            }
            writeChunk(stream, buffer, count, transmitting)
            if (count == 0) Thread.sleep(RETRY_DELAY_MS)
        }
    }

    private fun verifyRoute(ready: Boolean, transmitting: Boolean, deadline: Long) {
        if (ready) return
        if (transmitting) throw AudioException(MicrophoneProblem.ROUTE_CHANGED)
        if (SystemClock.elapsedRealtime() >= deadline) throw AudioException(MicrophoneProblem.ROUTING_FAILED)
    }

    private fun enableOutput(stream: PcmStream) {
        synchronized(streamLock) {
            if (!cancelled.get()) stream.setMuted(false)
        }
        if (!cancelled.get()) onLive()
    }

    private fun writeChunk(stream: PcmStream, buffer: ShortArray, count: Int, transmitting: Boolean) {
        var offset = 0
        val deadline = SystemClock.elapsedRealtime() + WRITE_TIMEOUT_MS
        while (offset < count && !cancelled.get()) {
            if (transmitting && !stream.isRouteReady) throw AudioException(MicrophoneProblem.ROUTE_CHANGED)
            val written = stream.write(buffer, offset, count - offset)
            offset += written
            if (written == 0) {
                if (SystemClock.elapsedRealtime() >= deadline) throw AudioException(MicrophoneProblem.PLAYBACK_FAILED)
                Thread.sleep(RETRY_DELAY_MS)
            }
        }
    }

    private companion object {
        const val TAG = "AudioLoop"
        const val ROUTE_TIMEOUT_MS = 4_000L
        const val WRITE_TIMEOUT_MS = 2_000L
        const val RETRY_DELAY_MS = 3L
    }
}
