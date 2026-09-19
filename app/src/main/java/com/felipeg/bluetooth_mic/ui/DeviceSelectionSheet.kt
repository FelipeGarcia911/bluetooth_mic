package com.felipeg.bluetooth_mic.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.audio.AudioInputKind
import com.felipeg.bluetooth_mic.audio.AudioInputOption
import com.felipeg.bluetooth_mic.audio.AudioOutputOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InputDeviceSheet(
    devices: List<AudioInputOption>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    DeviceSheet(
        title = stringResource(R.string.microphone_source),
        emptyMessage = stringResource(R.string.no_microphone_available),
        devices = devices.map { device ->
            DeviceChoice(device.id, device.displayName(), device.typeName(), device.id == selectedId)
        },
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OutputDeviceSheet(
    devices: List<AudioOutputOption>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    DeviceSheet(
        title = stringResource(R.string.audio_output),
        emptyMessage = stringResource(R.string.connect_speaker),
        devices = devices.map { device ->
            DeviceChoice(
                id = device.id,
                name = device.productName,
                type = stringResource(R.string.bluetooth_output),
                selected = device.id == selectedId,
            )
        },
        onSelect = onSelect,
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceSheet(
    title: String,
    emptyMessage: String,
    devices: List<DeviceChoice>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            if (devices.isEmpty()) {
                Text(
                    emptyMessage,
                    modifier = Modifier.padding(vertical = 32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                devices.forEach { device ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(device.id) }.padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = device.selected, onClick = { onSelect(device.id) })
                        Spacer(Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(device.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                device.type,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

private data class DeviceChoice(
    val id: Int,
    val name: String,
    val type: String,
    val selected: Boolean,
)

@Composable
internal fun AudioInputOption.displayName(): String = when (kind) {
    AudioInputKind.PHONE -> stringResource(R.string.phone_microphone)
    AudioInputKind.BLUETOOTH -> productName
    AudioInputKind.WIRED -> productName
    AudioInputKind.USB -> productName
    AudioInputKind.EXTERNAL -> productName
}

@Composable
private fun AudioInputOption.typeName(): String = stringResource(
    when (kind) {
        AudioInputKind.PHONE -> R.string.built_in_device
        AudioInputKind.BLUETOOTH -> R.string.bluetooth_device
        AudioInputKind.WIRED -> R.string.wired_device
        AudioInputKind.USB -> R.string.usb_device
        AudioInputKind.EXTERNAL -> R.string.external_device
    },
)
