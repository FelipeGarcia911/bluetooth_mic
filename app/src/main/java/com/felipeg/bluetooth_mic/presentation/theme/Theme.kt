package com.felipeg.bluetooth_mic.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BluetoothMicColors = darkColorScheme(
    primary = TalkOrange,
    onPrimary = AppBackground,
    primaryContainer = TalkOrangeDark,
    onPrimaryContainer = AppOnSurface,
    secondary = SignalGreen,
    onSecondary = AppBackground,
    background = AppBackground,
    onBackground = AppOnSurface,
    surface = AppSurface,
    onSurface = AppOnSurface,
    surfaceVariant = AppSurfaceHigh,
    onSurfaceVariant = AppOnSurfaceMuted,
    outline = AppOutline,
    error = ErrorRed,
)

@Composable
fun BluetoothMicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BluetoothMicColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
