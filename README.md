# AutoMatrix

<p align="center">
  <img src="https://img.shields.io/github/v/release/Shishir-ip/AutoMatrix?style=flat-square&label=Release&color=black" alt="Release" />
  <img src="https://img.shields.io/badge/Platform-Android_7.0%2B-black?style=flat-square&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Kotlin-2.2-black?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-black?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/License-MIT-black?style=flat-square" alt="License" />
</p>

<p align="center"><strong>Task automation and focus modes for Android. Free, offline, and open source.</strong></p>

AutoMatrix turns your phone into an automated, distraction-free environment. Create **routines** (trigger → condition → action) and **focus modes** that reconfigure your device in one tap — silencing notifications, adjusting volume and brightness, enabling Do Not Disturb, and more. Everything runs **100% on your device**. No account, no cloud, no telemetry.

Think of it as the middle ground between iOS Focus Modes and full automation suites like Tasker or MacroDroid — simple enough to set up in a minute, powerful enough for real workflows.

---

## Features

**Focus Modes**
- One-tap device profiles: Work, Study, Sleep, Driving, Gaming and custom modes
- Persistent lock-screen banner with *End Early* and *Mute 30 min* actions
- Quick Settings tile and a home-screen widget for instant switching
- Automatic revert: settings restore when the mode ends

**Routines (Automation)**
- Triggers: time schedules, battery level, charging state, Wi-Fi networks, Bluetooth devices, headphone plug, screen on/off, boot completed, app launch (accessibility), NFC tags, calendar events
- Conditions: combine triggers with All (AND) / Any (OR) logic and priorities
- Actions: ringer mode, DND, media/ring volume, brightness, Wi-Fi, Bluetooth, launch app, notifications, and more — with restore-on-exit support
- Ready-made template gallery: Sleep Mode, Morning Routine, Battery Saver, Commute, Security Guard and others

**Triggers without opening the app**
- Flip to Shhh — place the phone face down to silence and focus
- Shake gesture to run your top routine
- NFC stickers (write them right inside the app)

**Privacy & Design**
- 100% offline — zero analytics, zero trackers, no telemetry
- Monochrome black & white interface with a true-black OLED dark theme
- System / Light / Dark theme modes
- JSON backup export & import — your data is yours

## Download

Grab the latest signed APK from [**Releases**](https://github.com/Shishir-ip/AutoMatrix/releases/latest):

1. Download `AutoMatrix-v1.0.0.apk`
2. Open it and allow "Install unknown apps" for your browser/file manager
3. Done — the app requests permissions progressively as you use features

> The Play Store bundle (`.aab`) is also attached to each release.

## Requirements

- Android 7.0+ (API 24); optimized for Android 13–16
- Optional hardware: NFC (for tag triggers), accelerometer (gestures)
- Optional access: Notification listener, Accessibility (app-launch triggers), calendar — all off by default and explained in-app in the **Permission Center**

## Build from source

```bash
git clone https://github.com/Shishir-ip/AutoMatrix.git
cd AutoMatrix
./gradlew :app:assembleRelease
```

- Requires JDK 17+ and the Android SDK (API 36). `local.properties` or `ANDROID_HOME` must point to your SDK.
- Without a signing key, the release build falls back to debug signing, so `assembleRelease` works out of the box.
- To produce your own signed build: place `automatrix-release.jks` at the project root and provide
  `AUTOMATRIX_STORE_PASSWORD`, `AUTOMATRIX_KEY_ALIAS`, `AUTOMATRIX_KEY_PASSWORD` (environment variables or Gradle properties).

Run the tests:

```bash
./gradlew :app:testDebugUnitTest
```

## Tech stack

| Layer      | Tools                                                        |
|------------|--------------------------------------------------------------|
| Language   | Kotlin 2.2, Coroutines & Flow                                |
| UI         | Jetpack Compose, Material 3, custom monochrome design system |
| Data       | Room (SQLite), DataStore Preferences                         |
| Navigation | Navigation Compose (single-activity)                         |
| Build      | Gradle 9, AGP 9, KSP, R8 minification + resource shrinking   |

```
app/src/main/java/com/shishir/automatrix/
├── automation/        # engine (conditions, actions), receivers, services, scheduler
├── data/              # Room database, repositories, JSON backup
├── domain/            # models and built-in templates
├── permissions/       # permission center logic
├── ui/                # theme, components, screens, view model
└── util/              # NFC manager, refresh-rate helper
```

## Contributing

Issues and pull requests are welcome. For bigger changes, open an issue first to discuss what you would like to change. Run the unit tests before submitting.

## License

Released under the [MIT License](LICENSE). The Inter typeface bundled with the app is licensed under the [SIL Open Font License](INTER-LICENSE.txt).

---

<p align="center">
Developed by <a href="https://github.com/Shishir-ip"><strong>SHISHIR</strong></a>
</p>
