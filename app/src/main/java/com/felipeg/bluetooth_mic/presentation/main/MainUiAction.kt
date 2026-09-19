package com.felipeg.bluetooth_mic.presentation.main

internal sealed interface MainUiAction {
    data object StartHold : MainUiAction
    data object StopHold : MainUiAction
    data object StartOpenMic : MainUiAction
    data object Stop : MainUiAction
    data class SelectInput(val id: Int) : MainUiAction
    data class SelectOutput(val id: Int) : MainUiAction
}
