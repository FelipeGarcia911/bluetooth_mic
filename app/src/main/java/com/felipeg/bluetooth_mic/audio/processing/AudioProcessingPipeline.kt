package com.felipeg.bluetooth_mic.audio.processing

internal class AudioProcessingPipeline(
    private val gain: GainProcessor = GainProcessor(),
    private val highPass: HighPassProcessor = HighPassProcessor(),
    private val expander: DownwardExpanderProcessor = DownwardExpanderProcessor(),
) {
    fun process(buffer: ShortArray, count: Int, sampleRate: Int, settings: AudioProcessingSettings) {
        gain.gainDb = settings.inputGainDb
        highPass.enabled = settings.highPassEnabled
        highPass.cutoffHz = settings.highPassCutoffHz
        expander.enabled = settings.expanderEnabled
        expander.thresholdDb = settings.expanderThresholdDb
        expander.ratio = settings.expanderRatio

        gain.process(buffer, count, sampleRate)
        highPass.process(buffer, count, sampleRate)
        expander.process(buffer, count, sampleRate)
    }
}
