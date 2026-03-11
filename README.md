# GPS Tracker

An offline-first GPS tracking app that records the user's 3D movement trajectory (including flight paths) in the background over long periods, saves the data locally, and allows users to export the track as a standard GPX file.

## Features

- **User-Configurable Tracking & Dynamic Frequency:** Adjust tracking frequency (e.g., 1s, 10s, 60s) dynamically without stopping the tracking session.
- **Flight Trajectory & High-Accuracy 3D Tracking:** Captures altitude, speed, and bearing for high-precision flight and hiking paths.
- **Background Service & Doze Mode:** Uses Foreground Service and WakeLocks to ensure tracking survives Doze mode.
- **Offline Capability & GPX Export:** Fully functional without internet. Generates and exports GPX files with complete 3D data (`<ele>` tag).
- **Google Play Compliance (Mar 2026 Strict Rules):** Includes prominent disclosure for background location.

## Tech Stack
- Kotlin
- Jetpack Compose (Material Design 3)
- Clean Architecture + MVVM
- Dagger-Hilt for Dependency Injection
- Room Database for Local Storage
- Jetpack DataStore for Preferences
- Kotlin Coroutines & Flow
- Google Play Services `FusedLocationProviderClient`
- Compile/Target SDK 35

## Automated CI/CD
The project uses GitHub Actions for continuous integration and continuous deployment. It automatically builds the APK and AAB upon pushing to `main` or submitting a pull request.

To enable automated signed releases, configure the following GitHub Secrets:
- `KEYSTORE_BASE64`: Base64 encoded release keystore.
- `KEY_ALIAS`: Alias of the release key.
- `KEY_PASSWORD`: Password for the key.
- `STORE_PASSWORD`: Password for the keystore.

## License
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
