package com.felipeg.bluetooth_mic.presentation.main

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.presentation.components.AudioDeviceCard
import com.felipeg.bluetooth_mic.presentation.components.AudioLevelMeter
import com.felipeg.bluetooth_mic.presentation.components.DeviceStatus
import com.felipeg.bluetooth_mic.presentation.components.MicrophoneGlyph
import com.felipeg.bluetooth_mic.presentation.components.PermissionBanner
import com.felipeg.bluetooth_mic.presentation.components.PushToTalkButton
import com.felipeg.bluetooth_mic.presentation.theme.AppSpacing
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme
import com.felipeg.bluetooth_mic.presentation.theme.TalkOrange

@Composable
internal fun MainScreen(
    state: MainUiState,
    onAction: (MainUiAction) -> Unit,
    onChooseInput: () -> Unit,
    onChooseOutput: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(22.dp))
                AudioDeviceCard(
                    title = stringResource(R.string.audio_output),
                    deviceName = state.selectedOutput?.name ?: stringResource(R.string.connect_speaker),
                    status = if (state.selectedOutput == null) DeviceStatus.DISCONNECTED else DeviceStatus.CONNECTED,
                    enabled = !state.isActive,
                    modifier = Modifier.fillMaxWidth(),
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.ic_volume_up),
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    onClick = onChooseOutput,
                )
                Spacer(Modifier.height(12.dp))
                AudioDeviceCard(
                    title = stringResource(R.string.input_microphone),
                    deviceName = state.selectedInput?.name ?: stringResource(R.string.no_microphone_available),
                    status = null,
                    enabled = !state.isActive,
                    modifier = Modifier.fillMaxWidth(),
                    leadingContent = { MicrophoneGlyph(TalkOrange, Modifier.size(26.dp)) },
                    onClick = onChooseInput,
                )

                if (!state.permissionsGranted) {
                    Spacer(Modifier.height(14.dp))
                    PermissionBanner(
                        microphoneGranted = state.microphoneGranted,
                        onRequestPermissions = onRequestPermissions,
                        onOpenPermissions = onOpenPermissions,
                    )
                }

                Spacer(Modifier.height(28.dp))
                AudioLevelMeter(state.microphoneLevel)
                Text(
                    stringResource(R.string.microphone_level),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(22.dp))
                PushToTalkButton(
                    active = state.isActive && state.isHoldMode,
                    enabled = state.canTalk && (!state.isActive || state.isHoldMode),
                    onHoldStart = { onAction(MainUiAction.StartHold) },
                    onHoldEnd = { onAction(MainUiAction.StopHold) },
                )
                Spacer(Modifier.height(AppSpacing.md))
                SessionLabel(state)
                Spacer(Modifier.height(22.dp))

                val openModeActive = state.isActive && state.transmissionMode == TransmissionUiMode.OPEN
                Button(
                    onClick = { onAction(if (openModeActive) MainUiAction.Stop else MainUiAction.StartOpenMic) },
                    enabled = state.canTalk && (!state.isActive || openModeActive),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(stringResource(if (openModeActive) R.string.stop else R.string.open_microphone))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpenBluetooth, modifier = Modifier.weight(1f)) {
                        Icon(painterResource(R.drawable.ic_bluetooth), contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.bluetooth), modifier = Modifier.padding(start = AppSpacing.sm))
                    }
                    OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.settings), modifier = Modifier.padding(start = AppSpacing.sm))
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun SessionLabel(state: MainUiState) {
    val text = when (state.transmissionState) {
        TransmissionUiState.IDLE -> stringResource(R.string.hold_to_talk)
        TransmissionUiState.STARTING -> stringResource(R.string.microphone_starting)
        TransmissionUiState.LIVE -> if (state.transmissionMode == TransmissionUiMode.OPEN) {
            stringResource(R.string.open_microphone_active)
        } else {
            stringResource(R.string.microphone_live, state.activeOutputName.orEmpty())
        }
        TransmissionUiState.ERROR -> stringResource(state.message.messageResource())
    }
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (state.transmissionState == TransmissionUiState.ERROR) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@StringRes
private fun MainMessage?.messageResource(): Int = when (this) {
    MainMessage.PERMISSION_REQUIRED -> R.string.error_microphone_permission
    MainMessage.NO_BLUETOOTH_OUTPUT -> R.string.connect_speaker
    MainMessage.NO_AUDIO_INPUT -> R.string.error_no_microphone
    MainMessage.INPUT_ROUTE_UNAVAILABLE -> R.string.error_input_route
    MainMessage.UNSUPPORTED_FORMAT -> R.string.error_audio_format
    MainMessage.INITIALIZATION_FAILED -> R.string.error_audio_initialization
    MainMessage.ROUTING_FAILED -> R.string.error_audio_routing
    MainMessage.ROUTE_CHANGED -> R.string.error_route_changed
    MainMessage.MICROPHONE_SILENCED -> R.string.error_microphone_silenced
    MainMessage.RECORDING_FAILED -> R.string.error_audio_capture
    MainMessage.PLAYBACK_FAILED -> R.string.error_audio_playback
    MainMessage.AUDIO_BUSY -> R.string.error_audio_busy
    MainMessage.AUDIO_INTERRUPTED -> R.string.error_audio_interrupted
    MainMessage.DEVICE_DISCONNECTED -> R.string.error_disconnected
    MainMessage.START_FAILED, null -> R.string.error_start
}

private val previewInput = AudioDeviceUiModel(1, "Phone microphone", "Built-in microphone", AudioDeviceUiType.PHONE, true)
private val previewOutput = AudioDeviceUiModel(2, "JBL PartyBox 310", "Bluetooth output", AudioDeviceUiType.OUTPUT, true)

@Preview(name = "Idle", showBackground = true)
@Composable
private fun MainScreenIdlePreview() = MainScreenPreview(
    MainUiState(
        inputDevices = listOf(previewInput),
        outputDevices = listOf(previewOutput),
        selectedInput = previewInput,
        selectedOutput = previewOutput,
        microphoneGranted = true,
        notificationsGranted = true,
    ),
)

@Preview(name = "Transmitting", showBackground = true)
@Composable
private fun MainScreenTransmittingPreview() = MainScreenPreview(
    MainUiState(
        selectedInput = previewInput,
        selectedOutput = previewOutput,
        microphoneLevel = 0.72f,
        transmissionState = TransmissionUiState.LIVE,
        transmissionMode = TransmissionUiMode.HOLD,
        activeOutputName = previewOutput.name,
        microphoneGranted = true,
        notificationsGranted = true,
    ),
)

@Preview(name = "Permission missing", showBackground = true)
@Composable
private fun MainScreenPermissionPreview() = MainScreenPreview(MainUiState(selectedInput = previewInput))

@Composable
private fun MainScreenPreview(state: MainUiState) {
    BluetoothMicTheme {
        MainScreen(state, {}, {}, {}, {}, {}, {}, {})
    }
}
