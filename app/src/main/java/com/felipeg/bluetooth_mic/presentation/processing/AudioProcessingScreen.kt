package com.felipeg.bluetooth_mic.presentation.processing

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingPreset
import com.felipeg.bluetooth_mic.audio.processing.AudioProcessingSettings
import com.felipeg.bluetooth_mic.audio.processing.AudioSourceProfile
import com.felipeg.bluetooth_mic.audio.processing.settingsForPreset
import com.felipeg.bluetooth_mic.presentation.theme.AppSpacing
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme
import kotlin.math.roundToInt

private enum class ProcessingSheet { PRESET, CUTOFF, AUDIO_PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AudioProcessingScreen(
    state: AudioProcessingUiState,
    onAction: (AudioProcessingUiAction) -> Unit,
    onBack: () -> Unit,
) {
    var visibleSheet by rememberSaveable { mutableStateOf<ProcessingSheet?>(null) }
    val settings = state.settings

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.fillMaxWidth().widthIn(max = 600.dp)) {
                ScreenTitle(onBack)
                Spacer(Modifier.height(AppSpacing.lg))

                SectionTitle(stringResource(R.string.preset))
                Spacer(Modifier.height(AppSpacing.sm))
                SelectionRow(
                    title = stringResource(R.string.preset),
                    value = state.preset.displayName(),
                    onClick = { visibleSheet = ProcessingSheet.PRESET },
                )

                Spacer(Modifier.height(AppSpacing.lg))
                SectionTitle(stringResource(R.string.microphone_section))
                Spacer(Modifier.height(AppSpacing.sm))
                SettingsGroup {
                    SliderSetting(
                        title = stringResource(R.string.input_gain),
                        supportingText = stringResource(R.string.input_gain_description),
                        valueLabel = formatDb(settings.inputGainDb),
                        value = settings.inputGainDb,
                        range = -18f..0f,
                        steps = 17,
                        onValueChange = { onAction(AudioProcessingUiAction.SetInputGain(it.roundToInt().toFloat())) },
                    )
                }

                Spacer(Modifier.height(AppSpacing.lg))
                SectionTitle(stringResource(R.string.filtering))
                Spacer(Modifier.height(AppSpacing.sm))
                SettingsGroup {
                    SwitchSetting(
                        title = stringResource(R.string.high_pass_filter),
                        supportingText = stringResource(R.string.high_pass_description),
                        checked = settings.highPassEnabled,
                        onCheckedChange = { onAction(AudioProcessingUiAction.SetHighPassEnabled(it)) },
                    )
                    AnimatedVisibility(settings.highPassEnabled) {
                        SelectionRow(
                            title = stringResource(R.string.cutoff_frequency),
                            value = "${settings.highPassCutoffHz.roundToInt()} Hz",
                            grouped = true,
                            onClick = { visibleSheet = ProcessingSheet.CUTOFF },
                        )
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg))
                SectionTitle(stringResource(R.string.echo_noise_control))
                Spacer(Modifier.height(AppSpacing.sm))
                SettingsGroup {
                    SwitchSetting(
                        title = stringResource(R.string.expander),
                        supportingText = stringResource(R.string.expander_description),
                        checked = settings.expanderEnabled,
                        onCheckedChange = { onAction(AudioProcessingUiAction.SetExpanderEnabled(it)) },
                    )
                    AnimatedVisibility(settings.expanderEnabled) {
                        Column {
                            SliderSetting(
                                title = stringResource(R.string.threshold),
                                valueLabel = formatDb(settings.expanderThresholdDb),
                                value = settings.expanderThresholdDb,
                                range = -60f..-20f,
                                steps = 39,
                                onValueChange = {
                                    onAction(AudioProcessingUiAction.SetExpanderThreshold(it.roundToInt().toFloat()))
                                },
                            )
                            SliderSetting(
                                title = stringResource(R.string.ratio),
                                valueLabel = formatRatio(settings.expanderRatio),
                                value = settings.expanderRatio,
                                range = 1f..6f,
                                steps = 9,
                                onValueChange = {
                                    onAction(AudioProcessingUiAction.SetExpanderRatio((it * 2f).roundToInt() / 2f))
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(AppSpacing.lg))
                SectionTitle(stringResource(R.string.android_processing))
                Spacer(Modifier.height(AppSpacing.sm))
                SettingsGroup {
                    SwitchSetting(
                        title = stringResource(R.string.echo_cancellation),
                        supportingText = stringResource(R.string.echo_cancellation_description),
                        checked = settings.echoCancellationEnabled,
                        onCheckedChange = { onAction(AudioProcessingUiAction.SetAecEnabled(it)) },
                    )
                    SwitchSetting(
                        title = stringResource(R.string.noise_suppression),
                        supportingText = stringResource(R.string.noise_suppression_description),
                        checked = settings.noiseSuppressionEnabled,
                        onCheckedChange = { onAction(AudioProcessingUiAction.SetNoiseSuppressionEnabled(it)) },
                    )
                    SelectionRow(
                        title = stringResource(R.string.audio_profile),
                        value = settings.audioSourceProfile.displayName(),
                        supportingText = stringResource(R.string.audio_profile_restart),
                        grouped = true,
                        onClick = { visibleSheet = ProcessingSheet.AUDIO_PROFILE },
                    )
                }

                Spacer(Modifier.height(AppSpacing.lg))
                OutlinedButton(
                    onClick = { onAction(AudioProcessingUiAction.ResetPreset) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text(stringResource(R.string.reset_preset_defaults))
                }
                Spacer(Modifier.height(AppSpacing.lg))
            }
        }
    }

    when (visibleSheet) {
        ProcessingSheet.PRESET -> OptionSheet(
            title = stringResource(R.string.preset),
            options = AudioProcessingPreset.entries.filterNot { it == AudioProcessingPreset.CUSTOM },
            selected = state.preset,
            label = { it.displayName() },
            onSelect = {
                onAction(AudioProcessingUiAction.SelectPreset(it))
                visibleSheet = null
            },
            onDismiss = { visibleSheet = null },
        )
        ProcessingSheet.CUTOFF -> OptionSheet(
            title = stringResource(R.string.cutoff_frequency),
            options = listOf(80f, 100f, 120f, 150f),
            selected = settings.highPassCutoffHz,
            label = { "${it.roundToInt()} Hz" },
            onSelect = {
                onAction(AudioProcessingUiAction.SetHighPassCutoff(it))
                visibleSheet = null
            },
            onDismiss = { visibleSheet = null },
        )
        ProcessingSheet.AUDIO_PROFILE -> OptionSheet(
            title = stringResource(R.string.audio_profile),
            options = AudioSourceProfile.entries,
            selected = settings.audioSourceProfile,
            label = { it.displayName() },
            onSelect = {
                onAction(AudioProcessingUiAction.SetAudioSourceProfile(it))
                visibleSheet = null
            },
            onDismiss = { visibleSheet = null },
        )
        null -> Unit
    }
}

@Composable
private fun ScreenTitle(onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.back))
        }
        Text(stringResource(R.string.audio_processing), style = MaterialTheme.typography.titleLarge)
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
private fun SliderSetting(
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    supportingText: String? = null,
) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(valueLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        supportingText?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range, steps = steps)
    }
}

@Composable
private fun SwitchSetting(
    title: String,
    supportingText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = AppSpacing.md), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(supportingText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SelectionRow(
    title: String,
    value: String,
    onClick: () -> Unit,
    supportingText: String? = null,
    grouped: Boolean = false,
) {
    val content = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                supportingText?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(value, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(
                painterResource(R.drawable.ic_keyboard_arrow_down),
                contentDescription = stringResource(R.string.select_option),
                modifier = Modifier.padding(start = AppSpacing.sm).size(24.dp),
            )
        }
    }
    if (grouped) content() else Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> OptionSheet(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = AppSpacing.lg)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(AppSpacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            options.forEach { option ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelect(option) }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = option == selected, onClick = { onSelect(option) })
                    Text(label(option), modifier = Modifier.padding(start = AppSpacing.md), style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(AppSpacing.lg))
        }
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

@Composable
private fun AudioSourceProfile.displayName(): String = stringResource(
    when (this) {
        AudioSourceProfile.VOICE_COMMUNICATION -> R.string.voice_communication
        AudioSourceProfile.VOICE_RECOGNITION -> R.string.voice_recognition
    },
)

private fun formatDb(value: Float) = "${value.roundToInt()} dB"
private fun formatRatio(value: Float): String = if (value % 1f == 0f) {
    "${value.roundToInt()}:1"
} else {
    "${(value * 10).roundToInt() / 10f}:1"
}

@Preview(name = "Echo reduction", showBackground = true)
@Composable
private fun EchoReductionPreview() = ProcessingPreview(AudioProcessingPreset.ECHO_REDUCTION)

@Preview(name = "Natural", showBackground = true)
@Composable
private fun NaturalPreview() = ProcessingPreview(AudioProcessingPreset.NATURAL)

@Preview(name = "Aggressive", showBackground = true)
@Composable
private fun AggressivePreview() = ProcessingPreview(AudioProcessingPreset.AGGRESSIVE)

@Preview(name = "Custom", showBackground = true)
@Composable
private fun CustomPreview() = ProcessingPreview(
    AudioProcessingPreset.CUSTOM,
    AudioProcessingSettings(inputGainDb = -5f),
)

@Preview(name = "Expander off", showBackground = true)
@Composable
private fun ExpanderOffPreview() = ProcessingPreview(
    AudioProcessingPreset.CUSTOM,
    AudioProcessingSettings(expanderEnabled = false),
)

@Composable
private fun ProcessingPreview(
    preset: AudioProcessingPreset,
    settings: AudioProcessingSettings = settingsForPreset(preset),
) {
    BluetoothMicTheme {
        AudioProcessingScreen(AudioProcessingUiState(preset, settings), {}, {})
    }
}
