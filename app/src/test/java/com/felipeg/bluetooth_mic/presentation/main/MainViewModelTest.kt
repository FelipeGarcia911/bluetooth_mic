package com.felipeg.bluetooth_mic.presentation.main

import com.felipeg.bluetooth_mic.audio.AudioDeviceState
import com.felipeg.bluetooth_mic.audio.AudioInputKind
import com.felipeg.bluetooth_mic.audio.AudioInputOption
import com.felipeg.bluetooth_mic.audio.AudioOutputOption
import com.felipeg.bluetooth_mic.audio.MicrophoneController
import com.felipeg.bluetooth_mic.audio.MicrophoneMode
import com.felipeg.bluetooth_mic.audio.MicrophoneState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `maps selected input and output into presentation models`() = runTest {
        val devices = MutableStateFlow(connectedDevices())
        val viewModel = createViewModel(devices = devices)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals("Phone microphone", viewModel.uiState.value.selectedInput?.name)
        assertEquals("JBL PartyBox", viewModel.uiState.value.selectedOutput?.name)
    }

    @Test
    fun `selection actions target the corresponding device callbacks`() = runTest {
        var inputId: Int? = null
        var outputId: Int? = null
        val viewModel = createViewModel(
            selectInput = { inputId = it },
            selectOutput = { outputId = it },
        )

        viewModel.onAction(MainUiAction.SelectInput(4))
        viewModel.onAction(MainUiAction.SelectOutput(8))

        assertEquals(4, inputId)
        assertEquals(8, outputId)
    }

    @Test
    fun `disconnected selected device is reflected by repository fallback`() = runTest {
        val buds = AudioInputOption(2, "Galaxy Buds", AudioInputKind.BLUETOOTH)
        val phone = AudioInputOption(1, "Built in", AudioInputKind.PHONE)
        val devices = MutableStateFlow(
            connectedDevices().copy(inputs = listOf(phone, buds), selectedInputId = buds.id),
        )
        val viewModel = createViewModel(devices = devices)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals("Galaxy Buds", viewModel.uiState.value.selectedInput?.name)

        devices.value = connectedDevices().copy(inputs = listOf(phone), selectedInputId = phone.id)
        advanceUntilIdle()

        assertEquals("Phone microphone", viewModel.uiState.value.selectedInput?.name)
        assertNull(viewModel.uiState.value.inputDevices.firstOrNull { it.id == buds.id })
    }

    @Test
    fun `push to talk starts hold mode and stops held microphone`() = runTest {
        val controller = FakeMicrophoneController()
        val viewModel = createViewModel(controller = controller)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.updatePermissions(microphoneGranted = true, notificationsGranted = true)
        advanceUntilIdle()

        viewModel.onAction(MainUiAction.StartHold)
        viewModel.onAction(MainUiAction.StopHold)

        assertEquals(listOf(MicrophoneMode.HOLD), controller.startedModes)
        assertEquals(1, controller.stopHoldCalls)
    }

    private fun createViewModel(
        controller: FakeMicrophoneController = FakeMicrophoneController(),
        devices: StateFlow<AudioDeviceState> = MutableStateFlow(connectedDevices()),
        selectInput: (Int) -> Unit = {},
        selectOutput: (Int) -> Unit = {},
    ) = MainViewModel(controller, devices, selectInput, selectOutput)

    private fun connectedDevices() = AudioDeviceState(
        inputs = listOf(AudioInputOption(1, "Built in", AudioInputKind.PHONE)),
        selectedInputId = 1,
        outputs = listOf(AudioOutputOption(9, "JBL PartyBox")),
        selectedOutputId = 9,
    )
}

private class FakeMicrophoneController : MicrophoneController {
    override val state = MutableStateFlow(MicrophoneState())
    val startedModes = mutableListOf<MicrophoneMode>()
    var stopCalls = 0
    var stopHoldCalls = 0

    override fun start(mode: MicrophoneMode) {
        startedModes += mode
    }

    override fun stop() {
        stopCalls++
    }

    override fun stopHeldMicrophone() {
        stopHoldCalls++
    }
}
