package com.felipeg.bluetooth_mic.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.felipeg.bluetooth_mic.audio.AudioDeviceState
import com.felipeg.bluetooth_mic.audio.MicrophoneController
import com.felipeg.bluetooth_mic.audio.MicrophoneMode
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class MainViewModel(
    private val controller: MicrophoneController,
    private val deviceState: StateFlow<AudioDeviceState>,
    private val selectInput: (Int) -> Unit,
    private val selectOutput: (Int) -> Unit,
    processingSettings: Flow<AudioProcessingSettings> = MutableStateFlow(AudioProcessingSettings()),
) : ViewModel() {
    private val permissions = MutableStateFlow(PermissionUiState())

    val uiState: StateFlow<MainUiState> = combine(
        controller.state,
        deviceState,
        permissions,
        processingSettings,
        ::mapMainUiState,
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = mapMainUiState(
                controller.state.value,
                deviceState.value,
                permissions.value,
                AudioProcessingSettings(),
            ),
        )

    fun updatePermissions(microphoneGranted: Boolean, notificationsGranted: Boolean) {
        permissions.value = PermissionUiState(microphoneGranted, notificationsGranted)
    }

    fun onAction(action: MainUiAction) {
        when (action) {
            MainUiAction.StartHold -> if (uiState.value.canTalk && !uiState.value.isActive) {
                controller.start(MicrophoneMode.HOLD)
            }
            MainUiAction.StopHold -> controller.stopHeldMicrophone()
            MainUiAction.StartOpenMic -> if (uiState.value.canTalk && !uiState.value.isActive) {
                controller.start(MicrophoneMode.OPEN)
            }
            MainUiAction.Stop -> controller.stop()
            is MainUiAction.SelectInput -> if (!uiState.value.isActive) selectInput(action.id)
            is MainUiAction.SelectOutput -> if (!uiState.value.isActive) selectOutput(action.id)
        }
    }

    class Factory(
        private val controller: MicrophoneController,
        private val deviceState: StateFlow<AudioDeviceState>,
        private val selectInput: (Int) -> Unit,
        private val selectOutput: (Int) -> Unit,
        private val processingSettings: Flow<AudioProcessingSettings>,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            require(modelClass.isAssignableFrom(MainViewModel::class.java))
            return MainViewModel(controller, deviceState, selectInput, selectOutput, processingSettings) as T
        }
    }
}
