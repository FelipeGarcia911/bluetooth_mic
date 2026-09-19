package com.felipeg.bluetooth_mic.presentation.processing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun AudioProcessingRoute(
    viewModel: AudioProcessingViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AudioProcessingScreen(state = state, onAction = viewModel::onAction, onBack = onBack)
}
