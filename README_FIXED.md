# ShowTime Android project — patched

## What changed
- Login queries now close their SQLite cursor safely, normalize email input, route based on the database `is_admin` flag, and log unexpected failures rather than silently crashing.
- The admin dashboard no longer opens the Add Event screen. Admins are directed to select an existing event and use **Edit Showtime** to update its date and time.
- The admin event adapter updates an existing showtime only; it will not create a new event/showtime from the edit dialog.
- AdminAddEventActivity was removed from the manifest so it cannot be launched as an app screen.
- Admin reports now display zero instead of `null` for revenue when no bookings exist.

## Demo login (fresh app database)
- Admin: `admin@showtime.com` / `admin123`
- User: `user@showtime.com` / `password123`

These demo accounts are seeded when Android creates the app database for the first time. If an old install has an inconsistent database, uninstall the app from the test device (this deletes local app data) and install/run again, or clear the app's storage before testing. Back up any data you need first.

## Open and run
1. Extract this ZIP.
2. Open the `miniP` folder in Android Studio.
3. Let Android Studio sync Gradle and install any SDK components it requests.
4. Run on an emulator or connected Android phone.

## Build verification limitation
XML files were parsed successfully. A Gradle build could not be completed in this environment because the Gradle 9.5.0 distribution is not cached and network access to `services.gradle.org` is unavailable. Please run **Build > Make Project** in Android Studio and share the first Build Output error if any remains. This ZIP is a patched source project, not a verified APK.
