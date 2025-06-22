# PGPAndy

Placeholder Android app with navigation drawer, forms for storing PGP contacts, a message creator, and theme toggle.

## Building

This project uses Gradle 8.5 with Android Gradle Plugin 8.4.1 and requires the Android SDK. Run:

```bash
./gradlew assembleDebug
```

## Tests

No tests are provided.

## Database setup

The app initializes its SQLite database automatically when first launched. The
database contains `preferences` and `keys` tables for storing settings and PGP
key information. No manual setup is required.
