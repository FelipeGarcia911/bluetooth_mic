package com.felipeg.bluetooth_mic.audio

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioInputKind { PHONE, BLUETOOTH, WIRED, USB, EXTERNAL }

data class AudioInputOption(
    val id: Int,
    val productName: String,
    val kind: AudioInputKind,
)

data class AudioOutputOption(
    val id: Int,
    val productName: String,
)

data class AudioDeviceState(
    val inputs: List<AudioInputOption> = emptyList(),
    val selectedInputId: Int? = null,
    val outputs: List<AudioOutputOption> = emptyList(),
    val selectedOutputId: Int? = null,
) {
    val selectedInput: AudioInputOption?
        get() = inputs.firstOrNull { it.id == selectedInputId }
    val selectedOutput: AudioOutputOption?
        get() = outputs.firstOrNull { it.id == selectedOutputId }
    val outputName: String?
        get() = selectedOutput?.productName
}

/** Process-scoped source of connected audio devices and the user's input selection. */
@MainThread
internal class AudioDeviceRepository(private val manager: AudioManager) {
    private val mutableState = MutableStateFlow(AudioDeviceState())
    val state = mutableState.asStateFlow()

    private val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>) = refresh()
        override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) = refresh()
    }

    init {
        manager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        refresh()
    }

    fun selectInput(id: Int) {
        if (state.value.inputs.none { it.id == id }) return
        mutableState.value = state.value.copy(selectedInputId = id)
    }

    fun selectOutput(id: Int) {
        if (state.value.outputs.none { it.id == id }) return
        mutableState.value = state.value.copy(selectedOutputId = id)
    }

    fun resolveSelectedInput(): AudioDeviceInfo? {
        val availableInputs = manager.selectableInputs()
        return availableInputs.firstOrNull { it.id == state.value.selectedInputId }
            ?: availableInputs.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC }
            ?: availableInputs.firstOrNull()
    }

    fun resolveSelectedOutput(): AudioDeviceInfo? {
        val availableOutputs = manager.bluetoothOutputs()
        return availableOutputs.firstOrNull { it.id == state.value.selectedOutputId }
            ?: availableOutputs.firstOrNull()
    }

    private fun refresh() {
        val inputs = manager.selectableInputs()
        val outputs = manager.bluetoothOutputs()
        val selectedInputId = state.value.selectedInputId
            ?.takeIf { id -> inputs.any { it.id == id } }
            ?: inputs.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC }?.id
            ?: inputs.firstOrNull()?.id
        val selectedOutputId = state.value.selectedOutputId
            ?.takeIf { id -> outputs.any { it.id == id } }
            ?: outputs.firstOrNull()?.id
        mutableState.value = AudioDeviceState(
            inputs = inputs.map(AudioDeviceInfo::toInputOption),
            selectedInputId = selectedInputId,
            outputs = outputs.map(AudioDeviceInfo::toOutputOption),
            selectedOutputId = selectedOutputId,
        )
    }
}

private fun AudioDeviceInfo.toInputOption(): AudioInputOption = AudioInputOption(
    id = id,
    productName = productName.toString(),
    kind = when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> AudioInputKind.PHONE
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_BLE_HEADSET,
        -> AudioInputKind.BLUETOOTH
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> AudioInputKind.WIRED
        AudioDeviceInfo.TYPE_USB_ACCESSORY,
        AudioDeviceInfo.TYPE_USB_DEVICE,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        -> AudioInputKind.USB
        else -> AudioInputKind.EXTERNAL
    },
)

private fun AudioDeviceInfo.toOutputOption(): AudioOutputOption = AudioOutputOption(
    id = id,
    productName = productName.toString(),
)
