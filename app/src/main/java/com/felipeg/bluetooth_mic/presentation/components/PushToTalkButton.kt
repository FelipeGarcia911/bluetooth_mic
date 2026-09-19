package com.felipeg.bluetooth_mic.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.R
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme
import com.felipeg.bluetooth_mic.presentation.theme.TalkOrange
import com.felipeg.bluetooth_mic.presentation.theme.TalkOrangeDark

@Composable
internal fun PushToTalkButton(
    active: Boolean,
    enabled: Boolean,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val start by rememberUpdatedState(onHoldStart)
    val end by rememberUpdatedState(onHoldEnd)
    val haptics = LocalHapticFeedback.current
    val background by animateColorAsState(if (active) TalkOrangeDark else MaterialTheme.colorScheme.surfaceVariant, label = "pttColor")
    val scale by animateFloatAsState(if (active) 0.96f else 1f, label = "pttScale")
    val accessibilityState = stringResource(
        when {
            !enabled -> R.string.ptt_disabled
            active -> R.string.ptt_transmitting
            else -> R.string.ptt_ready
        },
    )
    val description = stringResource(R.string.push_to_talk)

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val diameter = minOf(maxWidth, 228.dp)
        Surface(
            modifier = Modifier.size(diameter).scale(scale).alpha(if (enabled) 1f else 0.48f)
                .testTag("push_to_talk")
                .semantics {
                    role = Role.Button
                    contentDescription = description
                    stateDescription = accessibilityState
                    if (!enabled) disabled()
                    onClick(label = accessibilityState) {
                        if (enabled) {
                            if (active) end() else start()
                        }
                        enabled
                    }
                }
                .pointerInput(enabled) {
                    if (enabled) detectTapGestures(onPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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

@Preview(name = "Ready", showBackground = true)
@Composable
private fun PushToTalkReadyPreview() {
    BluetoothMicTheme {
        PushToTalkButton(active = false, enabled = true, onHoldStart = {}, onHoldEnd = {})
    }
}

@Preview(name = "Transmitting", showBackground = true)
@Composable
private fun PushToTalkActivePreview() {
    BluetoothMicTheme {
        PushToTalkButton(active = true, enabled = true, onHoldStart = {}, onHoldEnd = {})
    }
}
