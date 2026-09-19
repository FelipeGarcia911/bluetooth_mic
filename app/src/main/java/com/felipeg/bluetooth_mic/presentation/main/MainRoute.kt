package com.felipeg.bluetooth_mic.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.felipeg.bluetooth_mic.presentation.navigation.AppNavigation
import com.felipeg.bluetooth_mic.presentation.processing.AudioProcessingViewModel

@Composable
internal fun MainRoute(
    viewModel: MainViewModel,
    processingViewModel: AudioProcessingViewModel,
    onRequestPermissions: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenBluetooth: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AppNavigation(
        state = state,
        processingViewModel = processingViewModel,
        onAction = viewModel::onAction,
        onRequestPermissions = onRequestPermissions,
        onOpenPermissions = onOpenPermissions,
        onOpenBluetooth = onOpenBluetooth,
    )
}
