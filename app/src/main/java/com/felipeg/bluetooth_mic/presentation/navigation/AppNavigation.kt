package com.felipeg.bluetooth_mic.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.presentation.components.AudioDeviceSheet
import com.felipeg.bluetooth_mic.presentation.main.MainScreen
import com.felipeg.bluetooth_mic.presentation.main.MainUiAction
import com.felipeg.bluetooth_mic.presentation.main.MainUiState
import com.felipeg.bluetooth_mic.presentation.settings.SettingsScreen

private enum class AppDestination { MAIN, SETTINGS }
private enum class DeviceSheet { INPUT, OUTPUT }

@Composable
internal fun AppNavigation(
    state: MainUiState,
    onAction: (MainUiAction) -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenBluetooth: () -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.MAIN) }
    var visibleSheet by rememberSaveable { mutableStateOf<DeviceSheet?>(null) }

    BackHandler(enabled = destination == AppDestination.SETTINGS && visibleSheet == null) {
        destination = AppDestination.MAIN
    }

    when (destination) {
        AppDestination.MAIN -> MainScreen(
            state = state,
            onAction = onAction,
            onChooseInput = { if (!state.isActive) visibleSheet = DeviceSheet.INPUT },
            onChooseOutput = { if (!state.isActive) visibleSheet = DeviceSheet.OUTPUT },
            onRequestPermissions = onRequestPermissions,
            onOpenPermissions = onOpenPermissions,
            onOpenBluetooth = onOpenBluetooth,
            onOpenSettings = { destination = AppDestination.SETTINGS },
        )
        AppDestination.SETTINGS -> SettingsScreen(
            state = state,
            onBack = { destination = AppDestination.MAIN },
            onChooseInput = { if (!state.isActive) visibleSheet = DeviceSheet.INPUT },
            onChooseOutput = { if (!state.isActive) visibleSheet = DeviceSheet.OUTPUT },
            onOpenBluetooth = onOpenBluetooth,
            onOpenPermissions = onOpenPermissions,
        )
    }

    when (visibleSheet) {
        DeviceSheet.INPUT -> AudioDeviceSheet(
            title = stringResource(R.string.microphone_source),
            emptyMessage = stringResource(R.string.no_microphone_available),
            devices = state.inputDevices,
            onSelect = {
                onAction(MainUiAction.SelectInput(it))
                visibleSheet = null
            },
            onDismiss = { visibleSheet = null },
        )
        DeviceSheet.OUTPUT -> AudioDeviceSheet(
            title = stringResource(R.string.audio_output),
            emptyMessage = stringResource(R.string.connect_speaker),
            devices = state.outputDevices,
            onSelect = {
                onAction(MainUiAction.SelectOutput(it))
                visibleSheet = null
            },
            onDismiss = { visibleSheet = null },
        )
        null -> Unit
    }
}
