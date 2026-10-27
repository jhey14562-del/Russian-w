# Offline Runner — native Android project

This project contains a native Kotlin Android endless-runner game. Gameplay works offline; the best score is stored locally on the device.

## Build APK with GitHub Actions
1. Push the contents of this ZIP into your repository root.
2. Open the repository's **Actions** tab.
3. Select **Build Offline Runner APK** and choose **Run workflow**.
4. When the run succeeds, open the run and download the `offline-runner-debug-apk` artifact.
5. Extract the artifact ZIP to get `app-debug.apk`.

## Build locally
Use JDK 17, Android SDK Platform 35, and Gradle 8.9:
`gradle --no-daemon :app:assembleDebug`

Output: `app/build/outputs/apk/debug/app-debug.apk`.
