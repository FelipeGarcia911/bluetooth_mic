# Bluetooth Microphone Architecture

An Android application with a single module and manual dependency composition. The Compose UI provides a focused push-to-talk screen, device selection sheets, and lightweight audio settings. Minimum Android version: 12 (API 31), based on the original configuration.

## Responsibilities

| Component | Responsibility |
| --- | --- |
| `MainActivity` | Host Compose, handle permissions, and launch Android settings. |
| `MainViewModel` | Combine microphone, device, and permission state and handle UI actions. |
| `MainRoute` | Collect UI state with lifecycle awareness and connect it to navigation. |
| `AppNavigation` | Coordinate the main/settings destinations and device sheets. |
| `MainScreen` | Render output/input status, live input level, and microphone controls from presentation models. |
| `SettingsScreen` | Present device and platform-processing settings from presentation models. |
| `presentation/components` | Hold the reusable device card/sheet, level, permission, status, and PTT controls. |
| `MicrophoneController` | Define the control contract consumed by the screen. |
| `AndroidMicrophoneController` | Translate user actions into service commands. |
| `MicrophoneSessionStore` | Act as the single owner of observable state and session identifiers. |
| `MicrophoneService` | Adapt the Android service lifecycle to the audio session. |
| `AudioSessionResources` | Acquire and release audio focus and the session's partial CPU wake lock. |
| `MicrophoneNotification` | Build the notification channel and notification with a Stop action. |
| `AudioEngine` | Define the engine start and stop contract. |
| `AudioLoop` | Transfer samples, verify routing, and handle cancellation. |
| `PcmStream` / `PcmStreamFactory` | Define focused contracts for PCM transport and creation. |
| `AndroidPcmStreamFactory` | Configure and build AudioRecord/AudioTrack while cleaning up partial creations. |
| `AndroidPcmStream` | Adapt the capture/playback APIs and release their resources. |
| `VoiceProcessingEffects` | Attach and own the optional platform AEC and noise-suppression effects. |
| `AudioDeviceRepository` | Observe available Bluetooth outputs and input microphones, and own both user selections. |
| `MicrophoneContainer` | Compose dependencies and share a serial worker across sessions. |

## Design Decisions

- Responsibilities are separated and dependencies are provided through constructors. `MainViewModel` depends on the existing controller, device state, and selection functions; composables only consume `MainUiState` and emit `MainUiAction`. The engine depends on `PcmStreamFactory`, rather than Android device constructors.
- Contracts are specific to each consumer; no interface forces a component to manage UI, notifications, and audio together.
- The implementation uses composition, adapters, and a simple factory. It does not introduce inheritance hierarchies or a dependency injection framework.
- [Extract Class](https://refactoring.guru/extract-class) and [Extract Method](https://refactoring.guru/extract-method) are applied: native construction lives outside the loop, while routing checks, transfer, and cancellation use focused methods with clear names.
- `MicrophoneProblem` provides typed errors. The presentation layer maps them to Android resources, while technical details are written to Logcat.
- Session state lives at application scope and survives Activity recreation. The notification and controller finish the same session.
- `AudioInputOption`, `AudioOutputOption`, and `MicrophoneState` are mapped at the presentation boundary. Compose receives only `AudioDeviceUiModel` and neutral UI enums.
- The app uses a small explicit two-destination navigator instead of a navigation library. This keeps current needs simple while making the destinations visible and replaceable later.
- Material 3 colors, typography, shapes, and spacing live in the presentation theme. Secondary icons are local vector drawables; the PTT microphone remains a custom Canvas glyph.

## Audio and Concurrency

Built-in microphone → platform voice processing → in-memory 16-bit mono PCM → AudioTrack → Bluetooth A2DP or LE Audio output. The app does not save recordings or use the internet. Android handles pairing; the app does not scan for devices and therefore does not request location permissions.

Each session uses `MODE_IN_COMMUNICATION`, `VOICE_COMMUNICATION` capture, and `USAGE_VOICE_COMMUNICATION` playback. The capture source lets the device apply its voice-processing pipeline. The app also attempts to attach `AcousticEchoCanceler` and `NoiseSuppressor` to the AudioRecord session when the device implements them; unavailable or rejected effects do not prevent the microphone from working. Both effects are released with their recording session. AudioTrack receives the platform's low-latency performance hint, while the existing 10 ms application chunks remain unchanged.

The user explicitly selects a connected Bluetooth media output. For input, the user can select the phone microphone or a connected Bluetooth, wired, or USB microphone. The repository keeps both selections by Android audio-device ID for the process lifetime. If a selected device disappears, output falls back to another available Bluetooth device and input falls back to the built-in microphone. Each session resolves both selected devices again and verifies the effective routes before sending voice audio. It sends silence during startup. Route changes, input/output disconnection, system-muted microphone, or audio-focus loss stop the session; reconnecting does not reactivate it.

For the phone, wired, and USB inputs, routing uses `AudioRecord.setPreferredDevice()`. A Bluetooth SCO or LE headset input additionally selects its matching communication output with `setCommunicationDevice()`, because Android activates the corresponding Bluetooth capture route as a pair. `AudioTrack` still requests the connected Bluetooth media speaker and the app verifies the actual input and output. Android devices that cannot combine a microphone and speaker from different Bluetooth devices stop with a clear routing error; using the phone microphone or the same Bluetooth device for input and output remains the compatible fallback. The communication-device request is cleared when the session ends.

The state store and commands run on the main thread. Each request has an identifier: releasing the button invalidates a pending start, and a response from an earlier session cannot modify the next one. Native resource creation, transfer, and release are serialized on one application worker. Non-blocking PCM operations permit cancellation; a short lock protects muting from concurrent release.

While a session is live, the audio worker calculates a normalized RMS level at a limited update rate and publishes it through `MicrophoneState`. Compose only renders that neutral value; it does not access `AudioRecord`, `AudioTrack`, or Android routing objects. Input and output selection use separate modal sheets and are locked while a session is active.

The engine accepts one start per instance and an idempotent stop, including a stop issued before startup. Permissions are checked when the service starts; granting a permission never activates the microphone automatically. The service uses `START_NOT_STICKY` to prevent restarts without a new user action.

Open mode continues while the screen is locked through a microphone/mediaPlayback service and a partial CPU wake lock, which is released when the service ends or is destroyed. The notification provides a Stop action. Hold mode ends when the user releases the control, cancels the gesture, or leaves the Activity. Android 13+ requires notification permission to show the control in the notification panel; denying it does not prevent microphone use from within the app.

## Limits and Verification

Build and static analysis: `./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug`. Unit verification uses `./gradlew :app:testDebugUnitTest` and covers state mapping, input/output selection, device fallback reflection, and PTT commands. Three Compose instrumentation tests cover selected-device rendering, the PTT gesture, and device-sheet content.

Latency, locked-screen stability, feedback, and native AEC effectiveness require evaluation with a real phone and speaker. AEC support and quality depend on the phone vendor and its ability to use Bluetooth playback as an echo reference. The roughly 500 ms observed on A2DP is mainly transport buffering and is not an application delay. The app does not promise zero latency or feedback elimination. Android can change routes asynchronously; stopping after detecting a change cannot guarantee that zero local samples play during the transition. After releasing the button, samples already buffered by the speaker may still play.

For the first physical check: use moderate volume, separate the phone from the speaker, speak in both modes, stop from the notification, and disconnect the speaker while broadcasting. Also confirm that a quick press does not leave the microphone active and that another app taking audio focus stops the session.
