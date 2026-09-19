package com.felipeg.bluetooth_mic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.felipeg.bluetooth_mic.audio.MicrophoneMode
import com.felipeg.bluetooth_mic.audio.microphoneAudioAttributes
import com.felipeg.bluetooth_mic.ui.MicrophoneControls
import com.felipeg.bluetooth_mic.ui.theme.Bluetooth_MicTheme

/** Hosts Android permissions/navigation. The composable only receives state and user actions. */
class MainActivity : ComponentActivity() {
    private val container get() = (application as MicrophoneApplication).container
    private val controller get() = container.controller
    private var microphoneGranted by mutableStateOf(false)
    private var notificationsGranted by mutableStateOf(false)
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        refreshPermissions()
        // A permission response must never restart a press that has already been released.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = microphoneAudioAttributes.volumeControlStream
        refreshPermissions()
        enableEdgeToEdge()
        setContent {
            val state by controller.state.collectAsState()
            val devices by container.devices.state.collectAsState()
            Bluetooth_MicTheme {
                MicrophoneControls(
                    state = state,
                    inputs = devices.inputs,
                    selectedInputId = devices.selectedInputId,
                    outputs = devices.outputs,
                    selectedOutputId = devices.selectedOutputId,
                    microphoneGranted = microphoneGranted,
                    notificationsGranted = notificationsGranted,
                    onSelectInput = container.devices::selectInput,
                    onSelectOutput = container.devices::selectOutput,
                    onRequestPermissions = ::requestPermissions,
                    onOpenPermissions = ::openApplicationSettings,
                    onHoldStart = { controller.start(MicrophoneMode.HOLD) },
                    onHoldEnd = controller::stopHeldMicrophone,
                    onOpenMicrophone = { controller.start(MicrophoneMode.OPEN) },
                    onStop = controller::stop,
                    onOpenBluetooth = { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
    }

    override fun onStop() {
        controller.stopHeldMicrophone()
        super.onStop()
    }

    private fun refreshPermissions() {
        microphoneGranted = isGranted(Manifest.permission.RECORD_AUDIO)
        notificationsGranted = Build.VERSION.SDK_INT < 33 || isGranted(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun requestPermissions() {
        val permissions = buildList {
            if (!microphoneGranted) add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= 33 && !notificationsGranted) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun openApplicationSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
