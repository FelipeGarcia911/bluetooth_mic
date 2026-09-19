package com.felipeg.bluetooth_mic.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.audio.MicrophoneMode
import com.felipeg.bluetooth_mic.audio.MicrophonePhase
import com.felipeg.bluetooth_mic.audio.MicrophoneState

/** Temporary functional controls. No service, permission, or audio APIs belong here. */
@Composable
internal fun MicrophoneControls(
    state: MicrophoneState,
    outputName: String?,
    microphoneGranted: Boolean,
    notificationsGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onOpenPermissions: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onOpenMicrophone: () -> Unit,
    onStop: () -> Unit,
    onOpenBluetooth: () -> Unit,
) {
    val canStart = microphoneGranted && outputName != null
    val canHold = canStart && (!state.isActive || state.mode == MicrophoneMode.HOLD)
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.microphone_title), style = MaterialTheme.typography.headlineSmall)
            Text(if (outputName == null) stringResource(R.string.connect_speaker) else stringResource(R.string.speaker_name, outputName))
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
