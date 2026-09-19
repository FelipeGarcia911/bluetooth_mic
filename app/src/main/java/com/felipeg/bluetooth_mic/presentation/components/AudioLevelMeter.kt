package com.felipeg.bluetooth_mic.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.felipeg.bluetooth_mic.presentation.theme.InactiveMeter
import com.felipeg.bluetooth_mic.presentation.theme.TalkOrange

@Composable
internal fun AudioLevelMeter(level: Float, modifier: Modifier = Modifier) {
    val animatedLevel by animateFloatAsState(level.coerceIn(0f, 1f), label = "microphoneLevel")
    val heights = listOf(0.30f, 0.52f, 0.75f, 1f, 0.75f, 0.52f, 0.30f)
    Row(
        modifier = modifier.fillMaxWidth().height(34.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        heights.forEachIndexed { index, heightFactor ->
            val threshold = (index.coerceAtMost(heights.lastIndex - index) + 1) / 4f
            Canvas(Modifier.width(5.dp).height((34 * heightFactor).dp)) {
                drawRoundRect(
                    color = if (animatedLevel >= threshold) TalkOrange else InactiveMeter,
                    cornerRadius = CornerRadius(size.width / 2f),
                )
            }
        }
    }
}

@Composable
internal fun MicrophoneGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = size.minDimension * 0.075f
        val bodyWidth = size.width * 0.34f
        val bodyHeight = size.height * 0.48f
        val left = (size.width - bodyWidth) / 2f
        val top = size.height * 0.12f
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(bodyWidth / 2f),
            style = Stroke(stroke),
        )
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.22f, size.height * 0.26f),
            size = Size(size.width * 0.56f, size.height * 0.48f),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawLine(
            color = color,
            start = Offset(size.width / 2f, size.height * 0.74f),
            end = Offset(size.width / 2f, size.height * 0.90f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.35f, size.height * 0.90f),
            end = Offset(size.width * 0.65f, size.height * 0.90f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
