package com.felipeg.bluetooth_mic.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingPreset
import com.felipeg.bluetooth_mic.presentation.main.AudioDeviceUiModel
import com.felipeg.bluetooth_mic.presentation.main.AudioDeviceUiType
import com.felipeg.bluetooth_mic.presentation.main.MainUiState
import com.felipeg.bluetooth_mic.presentation.theme.AppSpacing
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme

@Composable
internal fun SettingsScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onChooseInput: () -> Unit,
    onChooseOutput: () -> Unit,
    onOpenBluetooth: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAudioProcessing: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.fillMaxWidth().widthIn(max = 600.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.back))
                    }
                    Text(stringResource(R.string.audio_settings), style = MaterialTheme.typography.titleLarge)
                }

                Spacer(Modifier.height(AppSpacing.lg))
                SectionTitle(stringResource(R.string.devices))
                Spacer(Modifier.height(AppSpacing.sm))
                SettingsGroup {
                    DeviceSettingRow(
                        icon = R.drawable.ic_mic,
                        label = stringResource(R.string.input_microphone),
                        value = state.selectedInput?.name ?: stringResource(R.string.no_microphone_available),
                        enabled = !state.isActive,
                        onClick = onChooseInput,
                    )
                    DeviceSettingRow(
                        icon = R.drawable.ic_volume_up,
                        label = stringResource(R.string.audio_output),
                        value = state.selectedOutput?.name ?: stringResource(R.string.connect_speaker),
                        enabled = !state.isActive,
                        onClick = onChooseOutput,
                    )
                }
                if (state.isActive) {
                    Text(
                        stringResource(R.string.stop_to_change_devices),
                        modifier = Modifier.padding(top = AppSpacing.sm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(AppSpacing.lg))
                SectionTitle(stringResource(R.string.processing))
                Spacer(Modifier.height(AppSpacing.sm))
                SettingsGroup {
                    DeviceSettingRow(
                        icon = R.drawable.ic_settings,
                        label = stringResource(R.string.audio_processing),
                        value = state.audioProcessingPreset.displayName(),
                        enabled = true,
                        onClick = onOpenAudioProcessing,
                    )
                }

                Spacer(Modifier.height(AppSpacing.lg))
                SectionTitle(stringResource(R.string.connections_and_permissions))
                Spacer(Modifier.height(AppSpacing.sm))
                OutlinedButton(onClick = onOpenBluetooth, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(painterResource(R.drawable.ic_bluetooth), contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.configure_bluetooth), modifier = Modifier.padding(start = AppSpacing.sm))
                }
                Spacer(Modifier.height(AppSpacing.sm))
                OutlinedButton(onClick = onOpenPermissions, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(painterResource(R.drawable.ic_settings), contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(stringResource(R.string.open_permissions), modifier = Modifier.padding(start = AppSpacing.sm))
                }
                Spacer(Modifier.height(AppSpacing.lg))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(horizontal = AppSpacing.md), content = { content() })
    }
}

@Composable
private fun DeviceSettingRow(icon: Int, label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(Modifier.weight(1f).padding(start = AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
        Icon(
            painterResource(R.drawable.ic_chevron_right),
            contentDescription = stringResource(R.string.choose_device),
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AudioProcessingPreset.displayName(): String = stringResource(
    when (this) {
        AudioProcessingPreset.NATURAL -> R.string.natural
        AudioProcessingPreset.ECHO_REDUCTION -> R.string.echo_reduction
        AudioProcessingPreset.AGGRESSIVE -> R.string.aggressive
        AudioProcessingPreset.CUSTOM -> R.string.custom
    },
)

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    val input = AudioDeviceUiModel(1, "Phone microphone", "Built-in microphone", AudioDeviceUiType.PHONE, true)
    val output = AudioDeviceUiModel(2, "JBL PartyBox 310", "Bluetooth output", AudioDeviceUiType.OUTPUT, true)
    BluetoothMicTheme {
        SettingsScreen(
            state = MainUiState(selectedInput = input, selectedOutput = output),
            onBack = {},
            onChooseInput = {},
            onChooseOutput = {},
            onOpenBluetooth = {},
            onOpenPermissions = {},
            onOpenAudioProcessing = {},
        )
    }
}
