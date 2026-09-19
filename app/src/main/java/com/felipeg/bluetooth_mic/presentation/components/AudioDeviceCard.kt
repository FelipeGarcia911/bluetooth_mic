package com.felipeg.bluetooth_mic.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.presentation.theme.AppSpacing
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme
import com.felipeg.bluetooth_mic.presentation.theme.SignalGreen

@Composable
internal fun AudioDeviceCard(
    title: String,
    deviceName: String,
    status: DeviceStatus?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val statusColor = if (status == DeviceStatus.CONNECTED) SignalGreen else MaterialTheme.colorScheme.error
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.invoke()
            Column(
                modifier = Modifier.weight(1f).padding(start = if (leadingContent == null) 0.dp else AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(deviceName, style = MaterialTheme.typography.titleMedium)
            }
            if (status != null) {
                StatusIndicator(
                    label = stringResource(if (status == DeviceStatus.CONNECTED) R.string.connected else R.string.not_connected),
                    color = statusColor,
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                contentDescription = stringResource(R.string.choose_device),
                modifier = Modifier.padding(start = AppSpacing.sm).size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun AudioDeviceCardPreview() {
    BluetoothMicTheme {
        AudioDeviceCard(
            title = "Audio output",
            deviceName = "JBL PartyBox 310",
            status = DeviceStatus.CONNECTED,
            enabled = true,
            onClick = {},
        )
    }
}
