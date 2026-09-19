package com.felipeg.bluetooth_mic.presentation.main

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.down
import androidx.compose.ui.test.up
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mainScreenRendersSelectedDevices() {
        composeRule.setContent {
            BluetoothMicTheme { MainScreen(connectedState(), {}, {}, {}, {}, {}, {}, {}) }
        }

        composeRule.onNodeWithText("Phone microphone").assertIsDisplayed()
        composeRule.onNodeWithText("JBL PartyBox").assertIsDisplayed()
    }

    @Test
    fun pushToTalkPressSendsStartAndStopActions() {
        val actions = mutableListOf<MainUiAction>()
        composeRule.setContent {
            BluetoothMicTheme { MainScreen(connectedState(), actions::add, {}, {}, {}, {}, {}, {}) }
        }

        composeRule.onNodeWithTag("push_to_talk").performTouchInput {
            down(Offset(centerX, centerY))
            up()
        }

        assertEquals(listOf(MainUiAction.StartHold, MainUiAction.StopHold), actions)
    }

    private fun connectedState(): MainUiState {
        val input = AudioDeviceUiModel(1, "Phone microphone", "Built-in microphone", AudioDeviceUiType.PHONE, true)
        val output = AudioDeviceUiModel(2, "JBL PartyBox", "Bluetooth output", AudioDeviceUiType.OUTPUT, true)
        return MainUiState(
            inputDevices = listOf(input),
            outputDevices = listOf(output),
            selectedInput = input,
            selectedOutput = output,
            microphoneGranted = true,
            notificationsGranted = true,
        )
    }
}
