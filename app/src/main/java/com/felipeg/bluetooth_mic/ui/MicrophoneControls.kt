package com.felipeg.bluetooth_mic.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.felipeg.bluetooth_mic.audio.AudioInputOption
import com.felipeg.bluetooth_mic.audio.AudioOutputOption
import com.felipeg.bluetooth_mic.audio.MicrophoneState

private enum class DeviceSheet { INPUT, OUTPUT }

/** UI coordinator. Audio and Android framework work remain behind callbacks. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MicrophoneControls(
    state: MicrophoneState,
    inputs: List<AudioInputOption>,
    selectedInputId: Int?,
    outputs: List<AudioOutputOption>,
    selectedOutputId: Int?,
    microphoneGranted: Boolean,
    notificationsGranted: Boolean,
    onSelectInput: (Int) -> Unit,
    onSelectOutput: (Int) -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenPermissions: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onOpenMicrophone: () -> Unit,
    onStop: () -> Unit,
    onOpenBluetooth: () -> Unit,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var visibleSheet by rememberSaveable { mutableStateOf<DeviceSheet?>(null) }
    val selectedInput = inputs.firstOrNull { it.id == selectedInputId }
    val selectedOutput = outputs.firstOrNull { it.id == selectedOutputId }

    if (showSettings) {
        SettingsScreen(
            selectedInput = selectedInput,
            selectedOutput = selectedOutput,
            devicesEnabled = !state.isActive,
            onBack = { showSettings = false },
            onChooseInput = { visibleSheet = DeviceSheet.INPUT },
            onChooseOutput = { visibleSheet = DeviceSheet.OUTPUT },
            onOpenBluetooth = onOpenBluetooth,
            onOpenPermissions = onOpenPermissions,
        )
    } else {
        MainScreen(
            state = state,
            selectedInput = selectedInput,
            selectedOutput = selectedOutput,
            microphoneGranted = microphoneGranted,
            notificationsGranted = notificationsGranted,
            onChooseInput = { if (!state.isActive) visibleSheet = DeviceSheet.INPUT },
            onChooseOutput = { if (!state.isActive) visibleSheet = DeviceSheet.OUTPUT },
            onRequestPermissions = onRequestPermissions,
            onOpenPermissions = onOpenPermissions,
            onHoldStart = onHoldStart,
            onHoldEnd = onHoldEnd,
            onOpenMicrophone = onOpenMicrophone,
            onStop = onStop,
            onOpenBluetooth = onOpenBluetooth,
            onOpenSettings = { showSettings = true },
        )
    }

    when (visibleSheet) {
        DeviceSheet.INPUT -> InputDeviceSheet(
            devices = inputs,
            selectedId = selectedInputId,
            onSelect = {
                onSelectInput(it)
                visibleSheet = null
            },
            onDismiss = { visibleSheet = null },
        )
        DeviceSheet.OUTPUT -> OutputDeviceSheet(
            devices = outputs,
            selectedId = selectedOutputId,
            onSelect = {
                onSelectOutput(it)
                visibleSheet = null
            },
            onDismiss = { visibleSheet = null },
        )
        null -> Unit
    }
}
