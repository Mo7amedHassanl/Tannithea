# Tannithea

A modern Android application for monitoring and controlling sensor data with real-time Firebase integration.

## Features

- **Real-time Sensor Monitoring**: View and track sensor data in real-time
- **Control System**: Manage and control connected devices
- **Data Visualization**: Charts and graphs for sensor readings
- **Offline Capability**: Continues to function when offline with Firebase persistence
- **Modern UI**: Built with Material 3 and Jetpack Compose

## Technical Overview

### Architecture
- **MVVM Architecture** with repository pattern
- **Jetpack Compose** for UI
- **Hilt** for dependency injection
- **Firebase Realtime Database** for data storage and synchronization
- **Navigation Compose** for in-app navigation

### Tech Stack
- Kotlin
- Jetpack Compose
- Firebase Realtime Database
- Dagger Hilt
- Vico Compose Charts
- Material 3 Components

## Requirements

- Android Studio Giraffe (2023.1.1) or newer
- JDK 11
- Android SDK 35
- Minimum SDK: 24
- Target SDK: 35

## Getting Started

1. Clone the repository
2. Open the project in Android Studio
3. Connect to your Firebase project:
   - Create a Firebase project at [firebase.google.com](https://firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download the `google-services.json` file and place it in the app directory
4. Build and run the application

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/m7md7sn/tannithea/
│   │   │   ├── data/         # Data layer (models, repositories)
│   │   │   ├── di/           # Dependency injection modules
│   │   │   └── ui/           # UI components
│   │   │       ├── app/      # App-level UI components
│   │   │       ├── navigation/ # Navigation components
│   │   │       ├── screen/   # Screens (home, control, monitoring, etc.)
│   │   │       └── theme/    # UI theme
│   │   └── res/              # Resources
├── build.gradle.kts          # App-level build configuration
└── google-services.json      # Firebase configuration (you need to add this)
```

## License

[Include license information here]

## Contact

[Your contact information] 