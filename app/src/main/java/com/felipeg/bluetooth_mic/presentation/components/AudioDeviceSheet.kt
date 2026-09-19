package com.felipeg.bluetooth_mic.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.felipeg.bluetooth_mic.presentation.main.AudioDeviceUiModel
import com.felipeg.bluetooth_mic.presentation.main.AudioDeviceUiType
import com.felipeg.bluetooth_mic.presentation.theme.AppSpacing
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AudioDeviceSheet(
    title: String,
    emptyMessage: String,
    devices: List<AudioDeviceUiModel>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        AudioDeviceSheetContent(title, emptyMessage, devices, onSelect)
    }
}

@Composable
private fun AudioDeviceSheetContent(
    title: String,
    emptyMessage: String,
    devices: List<AudioDeviceUiModel>,
    onSelect: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg).testTag("device_selector")) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(AppSpacing.md))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        if (devices.isEmpty()) {
            Text(
                emptyMessage,
                modifier = Modifier.padding(vertical = AppSpacing.xl),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            devices.forEach { device ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(device.id) }.padding(vertical = AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = device.selected, onClick = { onSelect(device.id) })
                    Spacer(Modifier.width(AppSpacing.md))
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Text(device.name, style = MaterialTheme.typography.titleMedium)
                        Text(device.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            }
        }
        Spacer(Modifier.height(AppSpacing.lg))
    }
}

@Preview
@Composable
private fun AudioDeviceSheetPreview() {
    BluetoothMicTheme {
        AudioDeviceSheetContent(
            title = "Microphone source",
            emptyMessage = "No microphone available",
            devices = listOf(
                AudioDeviceUiModel(1, "Phone microphone", "Built-in microphone", AudioDeviceUiType.PHONE, true),
                AudioDeviceUiModel(2, "Galaxy Buds Pro", "Bluetooth", AudioDeviceUiType.BLUETOOTH, false),
            ),
            onSelect = {},
        )
    }
}
