package com.felipeg.bluetooth_mic.audio.processing

internal fun interface AudioProcessor {
    fun process(buffer: ShortArray, count: Int, sampleRate: Int)
}
