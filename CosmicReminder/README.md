# Cosmic Reminder 🌌

A personal Android reminder app with a classic, clean light interface and smooth, modern interactions.

## Features
- Create reminders with date and time
- Choose Personal, Work, or Health categories and Low, Normal, or High priority
- Categories and priority labels
- Mark reminders complete, edit, and delete
- Search and filter reminders
- Local persistence on the device
- Scheduled reminder notifications
- GitHub Actions builds a debug APK automatically

## Build APK on GitHub (phone-friendly)
1. Create a repository named `cosmic-reminder` on GitHub.
2. Upload the contents of this ZIP (upload the files/folders inside it, not the ZIP itself).
3. Open **Actions** and enable workflows if GitHub asks.
4. Select **Build Cosmic Reminder APK** → **Run workflow** (or push a commit).
5. Open the completed workflow run and download the `cosmic-reminder-debug-apk` artifact.
6. Extract the downloaded artifact ZIP to get `app-debug.apk`.

## Notes
- This first build is a debug APK, suitable for personal testing/sideloading.
- Android 13+ asks for notification permission.
- Android may restrict exact alarms depending on device settings. If a reminder notification is late, allow notifications and exact alarms for the app in system settings.
- No account or cloud sync is required; reminder data stays on the device.
