package com.felipeg.bluetooth_mic.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.felipeg.bluetooth_mic.presentation.main.AudioDeviceUiModel
import com.felipeg.bluetooth_mic.presentation.main.AudioDeviceUiType
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme
import org.junit.Rule
import org.junit.Test

class AudioDeviceSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun deviceSelectorShowsAvailableDevices() {
        composeRule.setContent {
            BluetoothMicTheme {
                AudioDeviceSheet(
                    title = "Microphone source",
                    emptyMessage = "No microphone available",
                    devices = listOf(
                        AudioDeviceUiModel(1, "Phone microphone", "Built-in microphone", AudioDeviceUiType.PHONE, true),
                        AudioDeviceUiModel(2, "Galaxy Buds", "Bluetooth", AudioDeviceUiType.BLUETOOTH, false),
                    ),
                    onSelect = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Phone microphone").assertIsDisplayed()
        composeRule.onNodeWithText("Galaxy Buds").assertIsDisplayed()
    }
}
