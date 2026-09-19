package com.felipeg.bluetooth_mic.presentation.processing

import com.felipeg.bluetooth_mic.audio.MicrophoneState
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingPreset
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingSettings
import com.felipeg.bluetooth_mic.audio.processing.AudioSourceProfile
import com.felipeg.bluetooth_mic.audio.settings.AudioProcessingSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioProcessingViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting aggressive preset applies all preset settings`() = runTest {
        val repository = FakeAudioProcessingSettingsRepository()
        val viewModel = AudioProcessingViewModel(repository, MutableStateFlow(MicrophoneState()))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onAction(AudioProcessingUiAction.SelectPreset(AudioProcessingPreset.AGGRESSIVE))
        advanceUntilIdle()

        assertEquals(-9f, repository.current.inputGainDb)
        assertEquals(120f, repository.current.highPassCutoffHz)
        assertEquals(-34f, repository.current.expanderThresholdDb)
        assertEquals(4f, repository.current.expanderRatio)
        assertEquals(AudioProcessingPreset.AGGRESSIVE, viewModel.uiState.value.preset)
    }

    @Test
    fun `changing one parameter converts preset to custom`() = runTest {
        val repository = FakeAudioProcessingSettingsRepository()
        val viewModel = AudioProcessingViewModel(repository, MutableStateFlow(MicrophoneState()))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onAction(AudioProcessingUiAction.SetInputGain(-5f))
        advanceUntilIdle()

        assertEquals(AudioProcessingPreset.CUSTOM, viewModel.uiState.value.preset)
    }

    @Test
    fun `reset from custom restores echo reduction defaults`() = runTest {
        val repository = FakeAudioProcessingSettingsRepository(AudioProcessingSettings(inputGainDb = -5f))
        val viewModel = AudioProcessingViewModel(repository, MutableStateFlow(MicrophoneState()))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onAction(AudioProcessingUiAction.ResetPreset)
        advanceUntilIdle()

        assertEquals(AudioProcessingSettings(), repository.current)
        assertEquals(AudioProcessingPreset.ECHO_REDUCTION, viewModel.uiState.value.preset)
    }
}

private class FakeAudioProcessingSettingsRepository(
    initial: AudioProcessingSettings = AudioProcessingSettings(),
) : AudioProcessingSettingsRepository {
    private val mutableSettings = MutableStateFlow(initial)
    override val settings = mutableSettings.asStateFlow()
    val current: AudioProcessingSettings get() = mutableSettings.value

    override suspend fun updateInputGainDb(value: Float) = change { it.copy(inputGainDb = value) }
    override suspend fun updateHighPassEnabled(enabled: Boolean) = change { it.copy(highPassEnabled = enabled) }
    override suspend fun updateHighPassCutoffHz(value: Float) = change { it.copy(highPassCutoffHz = value) }
    override suspend fun updateExpanderEnabled(enabled: Boolean) = change { it.copy(expanderEnabled = enabled) }
    override suspend fun updateExpanderThresholdDb(value: Float) = change { it.copy(expanderThresholdDb = value) }
    override suspend fun updateExpanderRatio(value: Float) = change { it.copy(expanderRatio = value) }
    override suspend fun updateEchoCancellationEnabled(enabled: Boolean) = change { it.copy(echoCancellationEnabled = enabled) }
    override suspend fun updateNoiseSuppressionEnabled(enabled: Boolean) = change { it.copy(noiseSuppressionEnabled = enabled) }
    override suspend fun updateAudioSourceProfile(profile: AudioSourceProfile) = change { it.copy(audioSourceProfile = profile) }
    override suspend fun update(settings: AudioProcessingSettings) {
        mutableSettings.value = settings
    }
    override suspend fun reset() {
        mutableSettings.value = AudioProcessingSettings()
    }

    private fun change(transform: (AudioProcessingSettings) -> AudioProcessingSettings) {
        mutableSettings.value = transform(mutableSettings.value)
    }
}
