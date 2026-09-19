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
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.felipeg.bluetooth_mic.audio.microphoneAudioAttributes
import com.felipeg.bluetooth_mic.presentation.main.MainRoute
import com.felipeg.bluetooth_mic.presentation.main.MainUiAction
import com.felipeg.bluetooth_mic.presentation.main.MainViewModel
import com.felipeg.bluetooth_mic.presentation.processing.AudioProcessingViewModel
import com.felipeg.bluetooth_mic.presentation.theme.BluetoothMicTheme

/** Hosts Android permissions/navigation. The composable only receives state and user actions. */
class MainActivity : ComponentActivity() {
    private val container get() = (application as MicrophoneApplication).container
    private val mainViewModel by viewModels<MainViewModel> {
        MainViewModel.Factory(
            controller = container.controller,
            deviceState = container.devices.state,
            selectInput = container.devices::selectInput,
            selectOutput = container.devices::selectOutput,
            processingSettings = container.processingSettings.settings,
        )
    }
    private val processingViewModel by viewModels<AudioProcessingViewModel> {
        AudioProcessingViewModel.Factory(
            repository = container.processingSettings,
            microphoneState = container.controller.state,
        )
    }
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
            BluetoothMicTheme {
                MainRoute(
                    viewModel = mainViewModel,
                    processingViewModel = processingViewModel,
                    onRequestPermissions = ::requestPermissions,
                    onOpenPermissions = ::openApplicationSettings,
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
        mainViewModel.onAction(MainUiAction.StopHold)
        super.onStop()
    }

    private fun refreshPermissions() {
        mainViewModel.updatePermissions(
            microphoneGranted = isGranted(Manifest.permission.RECORD_AUDIO),
            notificationsGranted = Build.VERSION.SDK_INT < 33 || isGranted(Manifest.permission.POST_NOTIFICATIONS),
        )
    }

    private fun requestPermissions() {
        val permissions = buildList {
            if (!isGranted(Manifest.permission.RECORD_AUDIO)) add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= 33 && !isGranted(Manifest.permission.POST_NOTIFICATIONS)) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (permissions.isNotEmpty()) permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun openApplicationSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
