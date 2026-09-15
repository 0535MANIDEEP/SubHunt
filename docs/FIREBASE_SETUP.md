# Firebase Setup Guide

## Quick Setup (5 minutes)

1. Go to https://console.firebase.google.com
2. Click "Create a project" → name it "SubHunt"
3. Skip Google Analytics (add later)
4. Click "Continue"

## Register Android App

1. Click Android icon (</>)
2. Package name: `com.subhunt.app`
3. App nickname: SubHunt
4. Click "Register app"
5. Download `google-services.json`
6. Place it in: `app/google-services.json`

## Enable Services

1. **Analytics**: Firebase Console → Analytics → Get Started
2. **Crashlytics**: Firebase Console → Crashlytics → Enable
3. **Cloud Messaging**: Already enabled by default

## Add to Build

After placing `google-services.json`, uncomment these in build files:

### Root build.gradle.kts — add plugins:
```kotlin
alias(libs.plugins.google.services) apply false
alias(libs.plugins.firebase.crashlytics) apply false
```

### app/build.gradle.kts — add plugins:
```kotlin
alias(libs.plugins.google.services)
alias(libs.plugins.firebase.crashlytics)
```

### gradle/libs.versions.toml — add Firebase deps:
```toml
[versions]
googleServices = "4.4.2"
firebaseBom = "33.7.0"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics" }
firebase-crashlytics-ktx = { group = "com.google.firebase", name = "firebase-crashlytics-ktx" }

[plugins]
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version.ref = "firebaseCrashlytics" }
```

### app/build.gradle.kts — add dependencies:
```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.analytics)
implementation(libs.firebase.crashlytics.ktx)
```

## Optional: Test

After setup, verify in Firebase Console:
- Realtime Database → Should show app connection
- Analytics → Should show first open event
- Crashlytics → Should show "No crashes yet"
