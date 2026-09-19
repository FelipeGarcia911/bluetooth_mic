package com.felipeg.bluetooth_mic.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.audio.AudioInputOption
import com.felipeg.bluetooth_mic.audio.AudioOutputOption
import com.felipeg.bluetooth_mic.ui.theme.SignalGreen

@Composable
internal fun SettingsScreen(
    selectedInput: AudioInputOption?,
    selectedOutput: AudioOutputOption?,
    devicesEnabled: Boolean,
    onBack: () -> Unit,
    onChooseInput: () -> Unit,
    onChooseOutput: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenPermissions: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                Text(
                    stringResource(R.string.audio_settings),
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Spacer(Modifier.height(28.dp))
            SectionTitle(stringResource(R.string.devices))
            Spacer(Modifier.height(10.dp))
            SettingCard {
                DeviceSettingRow(
                    label = stringResource(R.string.input_microphone),
                    value = selectedInput?.displayName() ?: stringResource(R.string.no_microphone_available),
                    enabled = devicesEnabled,
                    onClick = onChooseInput,
                )
                DeviceSettingRow(
                    label = stringResource(R.string.audio_output),
                    value = selectedOutput?.productName ?: stringResource(R.string.connect_speaker),
                    enabled = devicesEnabled,
                    onClick = onChooseOutput,
                )
            }
            if (!devicesEnabled) {
                Text(
                    stringResource(R.string.stop_to_change_devices),
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(28.dp))
            SectionTitle(stringResource(R.string.processing))
            Spacer(Modifier.height(10.dp))
            SettingCard {
                StatusSettingRow(stringResource(R.string.echo_cancellation), stringResource(R.string.automatic))
                StatusSettingRow(stringResource(R.string.noise_suppression), stringResource(R.string.automatic))
                StatusSettingRow(stringResource(R.string.audio_buffer), stringResource(R.string.buffer_value))
            }
            Text(
                stringResource(R.string.native_processing_note),
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))
            OutlinedButton(onClick = onOpenBluetooth, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.configure_bluetooth))
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onOpenPermissions, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.open_permissions))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SettingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(horizontal = 18.dp), content = { content() })
    }
}

@Composable
private fun DeviceSettingRow(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium)
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusSettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.labelLarge, color = SignalGreen)
    }
}
