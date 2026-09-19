package com.felipeg.bluetooth_mic.audio

import android.Manifest
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.felipeg.bluetooth_mic.MicrophoneApplication

/** Android lifecycle adapter. Audio transport and observable state have separate owners. */
class MicrophoneService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val container get() = (application as MicrophoneApplication).container
    private val sessions get() = container.sessions
    private lateinit var manager: AudioManager
    private lateinit var notification: MicrophoneNotification
    private lateinit var resources: AudioSessionResources
    private var engine: AudioEngine? = null
    private var sessionId = INVALID_SESSION
    private var inputId: Int? = null
    private var outputId: Int? = null

    private val devices = object : AudioDeviceCallback() {
        override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) {
            if (removed.any { it.id == inputId || it.id == outputId }) {
                finish(MicrophoneProblem.DEVICE_DISCONNECTED)
            }
        }
    }
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (engine != null) finish(MicrophoneProblem.DEVICE_DISCONNECTED)
        }
    }

    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(AudioManager::class.java)
        notification = MicrophoneNotification(this).also { it.createChannel() }
        resources = AudioSessionResources(manager, getSystemService(PowerManager::class.java), handler)
        manager.registerAudioDeviceCallback(devices, handler)
        ContextCompat.registerReceiver(this, noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            sessions.cancel()
            finish()
            return START_NOT_STICKY
        }
        val requested = intent?.getLongExtra(EXTRA_SESSION, INVALID_SESSION) ?: INVALID_SESSION
        if (!sessions.accepts(requested)) {
            if (engine == null) stopSelfResult(startId)
            return START_NOT_STICKY
        }
        if (engine != null && sessionId == requested) return START_NOT_STICKY
        startSession(requested)
        return START_NOT_STICKY
    }

    private fun startSession(requested: Long) {
        releaseAudio()
        sessionId = requested
        try {
            requireMicrophonePermission()
            startForeground(MicrophoneNotification.ID, notification.build(), FOREGROUND_TYPES)
            val output = container.devices.resolveSelectedOutput()
                ?: throw AudioException(MicrophoneProblem.NO_BLUETOOTH_OUTPUT)
            val input = container.devices.resolveSelectedInput()
                ?: throw AudioException(MicrophoneProblem.NO_AUDIO_INPUT)
            val communicationDevice = if (input.isBluetoothInput()) {
                manager.communicationDeviceFor(input)
                    ?: throw AudioException(MicrophoneProblem.INPUT_ROUTE_UNAVAILABLE)
            } else {
                null
            }
            inputId = input.id
            outputId = output.id
            resources.acquire(communicationDevice) {
                if (sessions.accepts(requested)) finish(MicrophoneProblem.AUDIO_INTERRUPTED)
            }
            engine = container.createEngine(input, output, onLive = {
                handler.post {
                    if (engine != null) sessions.markLive(requested, output.productName.toString())
                }
            }, onLevel = { level ->
                handler.post { sessions.updateLevel(requested, level) }
            }, onFinished = { problem ->
                handler.post { if (sessions.accepts(requested)) finish(problem) }
            }).also { it.start() }
        } catch (exception: Exception) {
            Log.e(TAG, "Cannot start microphone session", exception)
            finish((exception as? AudioException)?.problem ?: MicrophoneProblem.START_FAILED)
        }
    }

    private fun requireMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw AudioException(MicrophoneProblem.PERMISSION_REQUIRED)
        }
    }

    private fun releaseAudio() {
        engine?.stop()
        engine = null
        inputId = null
        outputId = null
        resources.close()
    }

    private fun finish(problem: MicrophoneProblem? = null) {
        releaseAudio()
        sessions.finish(sessionId, problem)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        releaseAudio()
        sessions.finish(sessionId)
        manager.unregisterAudioDeviceCallback(devices)
        unregisterReceiver(noisyReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "MicrophoneService"
        private const val ACTION_START = "com.felipeg.bluetooth_mic.START"
        private const val ACTION_STOP = "com.felipeg.bluetooth_mic.STOP"
        private const val EXTRA_SESSION = "session"
        private const val INVALID_SESSION = -1L
        private const val FOREGROUND_TYPES = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK

        internal fun startIntent(context: Context, sessionId: Long): Intent =
            Intent(context, MicrophoneService::class.java).setAction(ACTION_START).putExtra(EXTRA_SESSION, sessionId)

        internal fun stopIntent(context: Context): Intent =
            Intent(context, MicrophoneService::class.java).setAction(ACTION_STOP)
    }
}
