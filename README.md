# Bluetooth Mic

Bluetooth Mic turns an Android phone into a live wireless microphone. It captures audio from a selected microphone and plays it through a selected Bluetooth output, with separate controls for the input and output routes.

The interface is intentionally focused: choose the devices, press and hold to speak, or keep the microphone open for continuous use.

## Features

- Push-to-talk mode with press feedback and haptics
- Continuous open-microphone mode
- Independent input and output device selection
- Support for built-in, Bluetooth, wired, and USB microphones
- Bluetooth media output selection
- Live microphone level indicator
- Automatic fallback when a selected audio device disconnects
- Native acoustic echo cancellation when supported by the device
- Native noise suppression when supported by the device
- Foreground microphone session with a notification Stop action
- Dark Material 3 interface with accessibility semantics
- Runtime reaction to connected and disconnected audio devices

## How It Works

```text
Selected microphone
        ↓
Android AudioRecord
        ↓
Native voice processing (when available)
        ↓
In-memory 16-bit mono PCM stream
        ↓
Android AudioTrack
        ↓
Selected Bluetooth output
```

Audio is streamed in memory and is not recorded to a file. The application does not require internet access and does not upload audio or device information.

## User Interface

The main screen provides:

- The active Bluetooth output and its connection status
- The selected microphone
- A live input-level meter
- A large push-to-talk control
- A continuous microphone control
- Shortcuts to Bluetooth and audio settings

The settings screen provides a second place to select input and output devices and shows the processing currently managed by the Android audio system.

## Audio Device Routing

Input and output devices are selected independently. This allows combinations such as:

```text
Input:  Phone microphone
Output: JBL PartyBox
```

or:

```text
Input:  Bluetooth headset microphone
Output: Bluetooth speaker
```

Android ultimately controls the effective audio route. Some phones cannot combine a Bluetooth microphone from one device with a Bluetooth media output from another. When a requested combination is unavailable, the app stops the session and reports a routing error instead of silently using an unexpected device.

If the selected microphone disconnects, the app prefers the built-in microphone as its fallback. If the selected output disconnects, it selects another available Bluetooth output when possible.

## Architecture

The project uses a pragmatic single-module architecture. Presentation concerns are separated from Android audio implementation details without introducing additional Gradle modules or a dependency injection framework.

```text
MainActivity
    ↓
MainRoute ── AppNavigation
    ↓
MainViewModel
    ↓
MicrophoneController + AudioDeviceRepository
    ↓
MicrophoneService
    ↓
AudioEngine / AudioLoop
    ↓
AudioRecord + AudioTrack
```

Key design choices:

- `MainUiState` is the single state consumed by Compose.
- `MainUiAction` represents user actions from the main experience.
- Audio models are mapped to presentation-specific models before reaching composables.
- `StateFlow` exposes microphone and device changes.
- `collectAsStateWithLifecycle()` keeps UI collection lifecycle-aware.
- Constructor injection and a small application container provide dependencies.
- Reusable Compose components are extracted only when they have a clear visual responsibility.

More implementation details are available in [ARCHITECTURE.md](ARCHITECTURE.md).

## Technology

- Kotlin
- Jetpack Compose
- Material 3
- Android `AudioRecord` and `AudioTrack`
- Kotlin Coroutines and Flow
- Android foreground services
- Android audio device callbacks
- JUnit and Compose UI Test

## Requirements

- Android 12 or newer (API 31+)
- A physical Android device for meaningful audio-routing validation
- A paired Bluetooth speaker or compatible Bluetooth audio output
- Android Studio with JDK 17 or newer

Bluetooth pairing is handled through Android system settings. The app observes already available audio devices and does not implement its own Bluetooth scanner.

## Getting Started

1. Clone or open the project in Android Studio.
2. Allow Gradle to synchronize the project dependencies.
3. Connect an Android 12+ device.
4. Build and install the `app` configuration.
5. Grant microphone and notification permissions when requested.
6. Pair a Bluetooth speaker in Android settings.
7. Select the desired microphone and output inside the app.
8. Hold the central button to speak, or use **Keep microphone open** for a continuous session.

Keep the phone away from the speaker and begin at a moderate output volume to reduce acoustic feedback.

## Permissions

| Permission | Purpose |
| --- | --- |
| `RECORD_AUDIO` | Captures live microphone audio. |
| `POST_NOTIFICATIONS` | Shows the active-session notification and its Stop action on Android 13+. |
| `FOREGROUND_SERVICE` | Keeps an open microphone session active. |
| `FOREGROUND_SERVICE_MICROPHONE` | Declares microphone use by the foreground service. |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Declares audio playback by the foreground service. |
| `MODIFY_AUDIO_SETTINGS` | Configures communication mode and audio routing. |
| `WAKE_LOCK` | Keeps an active open-microphone session running while the screen is locked. |

## Build and Verification

Build the debug APK:

```bash
./gradlew :app:assembleDebug
```

Run unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Compile the Compose instrumentation tests:

```bash
./gradlew :app:assembleDebugAndroidTest
```

Run instrumentation tests on a connected device:

```bash
./gradlew :app:connectedDebugAndroidTest --no-configuration-cache
```

Run Android lint:

```bash
./gradlew :app:lintDebug
```

The automated tests cover presentation-state mapping, device selection, disconnection fallback, push-to-talk commands, selected-device rendering, the PTT gesture, and device-sheet content.

## Known Limitations

- Bluetooth A2DP introduces noticeable transport latency that the application cannot remove.
- Acoustic echo cancellation quality depends on the phone manufacturer, audio route, speaker volume, and room acoustics.
- Native echo cancellation may have an incomplete playback reference when using an external Bluetooth speaker.
- Android may reject or change a preferred input or output route.
- Samples already buffered by a Bluetooth speaker may continue briefly after the microphone is stopped.
- Reliable latency, routing, feedback, and locked-screen behavior must be evaluated on physical hardware.

## Privacy

Bluetooth Mic processes live audio locally. It does not save recordings, connect to a remote service, or request internet permission. Audio remains in the live capture and playback pipeline for the duration of the microphone session.

## Project Status

The core microphone loop, device routing, foreground session, native voice processing, presentation layer, and primary UI are implemented. Hardware-specific audio behavior continues to be validated across phones, microphones, headsets, and Bluetooth speakers.

## License

This project does not currently include an open-source license.
