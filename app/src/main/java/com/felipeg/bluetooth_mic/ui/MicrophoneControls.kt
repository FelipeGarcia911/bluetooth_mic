package com.felipeg.bluetooth_mic.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.audio.AudioInputKind
import com.felipeg.bluetooth_mic.audio.AudioInputOption
import com.felipeg.bluetooth_mic.audio.AudioOutputOption
import com.felipeg.bluetooth_mic.audio.MicrophoneMode
import com.felipeg.bluetooth_mic.audio.MicrophonePhase
import com.felipeg.bluetooth_mic.audio.MicrophoneState

/** Temporary functional controls. No service, permission, or audio APIs belong here. */
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
    val selectedInput = inputs.firstOrNull { it.id == selectedInputId }
    val selectedOutput = outputs.firstOrNull { it.id == selectedOutputId }
    val canStart = microphoneGranted && selectedOutput != null && selectedInput != null
    val canHold = canStart && (!state.isActive || state.mode == MicrophoneMode.HOLD)
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.microphone_title), style = MaterialTheme.typography.headlineSmall)
            OutputSelector(
                outputs = outputs,
                selectedOutput = selectedOutput,
                enabled = !state.isActive,
                onSelectOutput = onSelectOutput,
            )
            MicrophoneSelector(
                inputs = inputs,
                selectedInput = selectedInput,
                enabled = !state.isActive,
                onSelectInput = onSelectInput,
            )
            SessionStatus(state)
            if (!microphoneGranted || !notificationsGranted) {
                Button(onClick = onRequestPermissions, enabled = !state.isActive) {
                    Text(stringResource(if (!microphoneGranted) R.string.allow_microphone else R.string.allow_notifications))
                }
                OutlinedButton(onClick = onOpenPermissions) { Text(stringResource(R.string.open_permissions)) }
            }
            HoldToTalkControl(canHold, onHoldStart, onHoldEnd)
            Button(enabled = canStart && !state.isActive, onClick = onOpenMicrophone) {
                Text(stringResource(R.string.open_microphone))
            }
            Button(enabled = state.isActive, onClick = onStop) { Text(stringResource(R.string.stop)) }
            OutlinedButton(onClick = onOpenBluetooth) { Text(stringResource(R.string.configure_bluetooth)) }
            Text(stringResource(R.string.audio_hint))
        }
    }
}

@Composable
private fun MicrophoneSelector(
    inputs: List<AudioInputOption>,
    selectedInput: AudioInputOption?,
    enabled: Boolean,
    onSelectInput: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DeviceSelector(
            label = stringResource(R.string.input_microphone),
            selectedName = selectedInput?.displayName(),
            emptyName = stringResource(R.string.no_microphone_available),
            options = inputs.map { it.id to it.displayName() },
            enabled = enabled,
            onSelect = onSelectInput,
        )
        if (selectedInput?.kind == AudioInputKind.BLUETOOTH) {
            Text(
                stringResource(R.string.bluetooth_microphone_note),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun OutputSelector(
    outputs: List<AudioOutputOption>,
    selectedOutput: AudioOutputOption?,
    enabled: Boolean,
    onSelectOutput: (Int) -> Unit,
) {
    DeviceSelector(
        label = stringResource(R.string.output_device),
        selectedName = selectedOutput?.productName,
        emptyName = stringResource(R.string.connect_speaker),
        options = outputs.map { it.id to it.productName },
        enabled = enabled,
        onSelect = onSelectOutput,
    )
}

@Composable
private fun DeviceSelector(
    label: String,
    selectedName: String?,
    emptyName: String,
    options: List<Pair<Int, String>>,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box {
            OutlinedButton(
                enabled = enabled && options.isNotEmpty(),
                onClick = { expanded = true },
            ) {
                Text(selectedName ?: emptyName)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onSelect(id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioInputOption.displayName(): String = when (kind) {
    AudioInputKind.PHONE -> stringResource(R.string.phone_microphone)
    AudioInputKind.BLUETOOTH -> stringResource(R.string.bluetooth_microphone, productName)
    AudioInputKind.WIRED -> stringResource(R.string.wired_microphone, productName)
    AudioInputKind.USB -> stringResource(R.string.usb_microphone, productName)
    AudioInputKind.EXTERNAL -> stringResource(R.string.external_microphone, productName)
}

@Composable
private fun HoldToTalkControl(enabled: Boolean, onStart: () -> Unit, onEnd: () -> Unit) {
    val start by rememberUpdatedState(onStart)
    val end by rememberUpdatedState(onEnd)
    Surface(
        color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().pointerInput(enabled) {
            if (enabled) detectTapGestures(onPress = {
                start()
                try {
                    awaitRelease()
                } finally {
                    end()
                }
            })
        },
    ) {
        Text(stringResource(R.string.hold_to_talk), Modifier.padding(24.dp))
    }
}

@Composable
private fun SessionStatus(state: MicrophoneState) {
    val message = when (state.phase) {
        MicrophonePhase.IDLE -> stringResource(R.string.microphone_idle)
        MicrophonePhase.STARTING -> stringResource(R.string.microphone_starting)
        MicrophonePhase.LIVE -> stringResource(R.string.microphone_live, state.outputName.orEmpty())
        MicrophonePhase.ERROR -> stringResource(state.problem.messageResource())
    }
    Text(message)
}
