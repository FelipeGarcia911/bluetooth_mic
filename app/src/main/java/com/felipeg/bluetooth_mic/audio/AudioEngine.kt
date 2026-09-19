package com.felipeg.bluetooth_mic.audio

/** One session. start is called once; stop is idempotent and may precede start. */
internal interface AudioEngine {
    fun start()
    fun stop()
}

/** PCM transport boundary. Only mute may run concurrently with the worker. */
internal interface PcmStream : AutoCloseable {
    val chunkSize: Int
    val isRouteReady: Boolean
    val isMicrophoneSilenced: Boolean
    fun start()
    fun read(buffer: ShortArray): Int
    fun write(buffer: ShortArray, offset: Int, count: Int): Int
    fun setMuted(muted: Boolean)
}

internal fun interface PcmStreamFactory {
    fun create(): PcmStream
}

internal class AudioException(val problem: MicrophoneProblem) : Exception(problem.name)
