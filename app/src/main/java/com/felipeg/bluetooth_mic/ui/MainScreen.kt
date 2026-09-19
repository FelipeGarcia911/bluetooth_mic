package com.felipeg.bluetooth_mic.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.audio.AudioInputOption
import com.felipeg.bluetooth_mic.audio.AudioOutputOption
import com.felipeg.bluetooth_mic.audio.MicrophoneMode
import com.felipeg.bluetooth_mic.audio.MicrophonePhase
import com.felipeg.bluetooth_mic.audio.MicrophoneState
import com.felipeg.bluetooth_mic.ui.theme.SignalGreen
import com.felipeg.bluetooth_mic.ui.theme.TalkOrange
import com.felipeg.bluetooth_mic.ui.theme.TalkOrangeDark

@Composable
internal fun MainScreen(
    state: MicrophoneState,
    selectedInput: AudioInputOption?,
    selectedOutput: AudioOutputOption?,
    microphoneGranted: Boolean,
    notificationsGranted: Boolean,
    onChooseInput: () -> Unit,
    onChooseOutput: () -> Unit,
    onRequestPermissions: () -> Unit,
    onOpenPermissions: () -> Unit,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    onOpenMicrophone: () -> Unit,
    onStop: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val ready = microphoneGranted && selectedInput != null && selectedOutput != null
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(22.dp))
            OutputStatusCard(
                output = selectedOutput,
                enabled = !state.isActive,
                onClick = onChooseOutput,
            )
            Spacer(Modifier.height(12.dp))
            InputDeviceCard(
                input = selectedInput,
                enabled = !state.isActive,
                onClick = onChooseInput,
            )

            if (!microphoneGranted || !notificationsGranted) {
                Spacer(Modifier.height(14.dp))
                PermissionCard(
                    microphoneGranted = microphoneGranted,
                    onRequestPermissions = onRequestPermissions,
                    onOpenPermissions = onOpenPermissions,
                )
            }

            Spacer(Modifier.height(28.dp))
            MicrophoneLevelMeter(state.microphoneLevel, Modifier.fillMaxWidth())
            Text(
                stringResource(R.string.microphone_level),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(22.dp))
            PushToTalkButton(
                state = state,
                enabled = ready,
                onHoldStart = onHoldStart,
                onHoldEnd = onHoldEnd,
            )
            Spacer(Modifier.height(16.dp))
            SessionLabel(state)
            Spacer(Modifier.height(22.dp))

            val openModeActive = state.isActive && state.mode == MicrophoneMode.OPEN
            Button(
                onClick = if (openModeActive) onStop else onOpenMicrophone,
                enabled = ready && (!state.isActive || openModeActive),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(if (openModeActive) R.string.stop else R.string.open_microphone))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onOpenBluetooth, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.bluetooth))
                }
                OutlinedButton(onClick = onOpenSettings, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings))
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun OutputStatusCard(output: AudioOutputOption?, enabled: Boolean, onClick: () -> Unit) {
    val statusColor = if (output == null) MaterialTheme.colorScheme.error else SignalGreen
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.audio_output),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    output?.productName ?: stringResource(R.string.connect_speaker),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Canvas(Modifier.size(10.dp)) {
                drawCircle(statusColor)
            }
            Text(
                stringResource(if (output == null) R.string.not_connected else R.string.connected),
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
            )
            Text("⌄", modifier = Modifier.padding(start = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun InputDeviceCard(input: AudioInputOption?, enabled: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MicrophoneGlyph(TalkOrange, Modifier.size(26.dp))
            Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(R.string.input_microphone),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    input?.displayName() ?: stringResource(R.string.no_microphone_available),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text("⌄", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PushToTalkButton(
    state: MicrophoneState,
    enabled: Boolean,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
) {
    val holdEnabled = enabled && (!state.isActive || state.mode == MicrophoneMode.HOLD)
    val active = state.isActive && state.mode == MicrophoneMode.HOLD
    val start by rememberUpdatedState(onHoldStart)
    val end by rememberUpdatedState(onHoldEnd)
    val background by animateColorAsState(
        if (active) TalkOrangeDark else MaterialTheme.colorScheme.surfaceVariant,
        label = "pttBackground",
    )
    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val diameter = minOf(maxWidth, 228.dp)
        Surface(
            modifier = Modifier.size(diameter).alpha(if (holdEnabled) 1f else 0.48f)
                .pointerInput(holdEnabled) {
                    if (holdEnabled) detectTapGestures(onPress = {
                        start()
                        try {
                            awaitRelease()
                        } finally {
                            end()
                        }
                    })
                },
            shape = CircleShape,
            color = background,
            border = BorderStroke(if (active) 6.dp else 3.dp, if (active) TalkOrange else MaterialTheme.colorScheme.outline),
            shadowElevation = if (active) 18.dp else 4.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MicrophoneGlyph(if (active) Color.White else TalkOrange, Modifier.size(62.dp))
                    Spacer(Modifier.height(14.dp))
                    Text(
                        stringResource(if (active) R.string.transmitting else R.string.hold_to_talk_short),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionLabel(state: MicrophoneState) {
    val text = when (state.phase) {
        MicrophonePhase.IDLE -> stringResource(R.string.hold_to_talk)
        MicrophonePhase.STARTING -> stringResource(R.string.microphone_starting)
        MicrophonePhase.LIVE -> if (state.mode == MicrophoneMode.OPEN) {
            stringResource(R.string.open_microphone_active)
        } else {
            stringResource(R.string.microphone_live, state.outputName.orEmpty())
        }
        MicrophonePhase.ERROR -> stringResource(state.problem.messageResource())
    }
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (state.phase == MicrophonePhase.ERROR) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PermissionCard(
    microphoneGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onOpenPermissions: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(if (microphoneGranted) R.string.notification_permission_message else R.string.microphone_permission_message),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRequestPermissions) { Text(stringResource(R.string.allow)) }
                OutlinedButton(onClick = onOpenPermissions) { Text(stringResource(R.string.app_settings)) }
            }
        }
    }
}
