package com.felipeg.bluetooth_mic.presentation.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.felipeg.bluetooth_mic.audio.MicrophoneState
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingPreset
import com.felipeg.bluetooth_mic.audio.processing.detectPreset
import com.felipeg.bluetooth_mic.audio.processing.settingsForPreset
import com.felipeg.bluetooth_mic.audio.settings.AudioProcessingSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class AudioProcessingViewModel(
    private val repository: AudioProcessingSettingsRepository,
    microphoneState: StateFlow<MicrophoneState>,
) : ViewModel() {
    val uiState: StateFlow<AudioProcessingUiState> = combine(repository.settings, microphoneState) { settings, microphone ->
        AudioProcessingUiState(
            preset = detectPreset(settings),
            settings = settings,
            sessionActive = microphone.isActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AudioProcessingUiState(),
    )

    fun onAction(action: AudioProcessingUiAction) {
        viewModelScope.launch {
            when (action) {
                is AudioProcessingUiAction.SelectPreset -> if (action.preset != AudioProcessingPreset.CUSTOM) {
                    repository.update(settingsForPreset(action.preset))
                }
                is AudioProcessingUiAction.SetInputGain -> repository.updateInputGainDb(action.db)
                is AudioProcessingUiAction.SetHighPassEnabled -> repository.updateHighPassEnabled(action.enabled)
                is AudioProcessingUiAction.SetHighPassCutoff -> repository.updateHighPassCutoffHz(action.hz)
                is AudioProcessingUiAction.SetExpanderEnabled -> repository.updateExpanderEnabled(action.enabled)
                is AudioProcessingUiAction.SetExpanderThreshold -> repository.updateExpanderThresholdDb(action.db)
                is AudioProcessingUiAction.SetExpanderRatio -> repository.updateExpanderRatio(action.ratio)
                is AudioProcessingUiAction.SetAecEnabled -> repository.updateEchoCancellationEnabled(action.enabled)
                is AudioProcessingUiAction.SetNoiseSuppressionEnabled ->
                    repository.updateNoiseSuppressionEnabled(action.enabled)
                is AudioProcessingUiAction.SetAudioSourceProfile -> repository.updateAudioSourceProfile(action.profile)
                AudioProcessingUiAction.ResetPreset -> {
                    val preset = uiState.value.preset.takeUnless { it == AudioProcessingPreset.CUSTOM }
                        ?: AudioProcessingPreset.ECHO_REDUCTION
                    repository.update(settingsForPreset(preset))
                }
            }
        }
    }

    class Factory(
        private val repository: AudioProcessingSettingsRepository,
        private val microphoneState: StateFlow<MicrophoneState>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass.isAssignableFrom(AudioProcessingViewModel::class.java))
            return AudioProcessingViewModel(repository, microphoneState) as T
        }
    }
}
