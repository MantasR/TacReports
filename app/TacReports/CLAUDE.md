# TacReports: notes for Claude

Android + iOS app for quick military-style text reports (SITREP, CONTREP, MEDEVAC, PERSREP...).
Kotlin Multiplatform + Compose Multiplatform version of the Android-only 0.1.0 in
`ProofOfConcept/TacReports` (same repo).
Personal use; the owner tests every build on their own phone. Read the repo-root `MEMORY.md` for the idea,
decisions and open questions.

Don't copy code from the private SimpleGrid repo here, and don't put personal or unit details in
commits, code or notes.

## What the app does
- Pick a template, fill in only the blanks, tap Copy: the text (one point per line) is on the
  clipboard, ready to paste into a messenger. Empty fields are left out.
- Android: a floating bubble (overlay, foreground service) opens a translucent form over any app.
- iOS: no overlays exist, so a "New report" App Intent (Shortcuts, Action Button, Back Tap) and the
  `tacreports://` URL open the app on the report picker.
- Every field row has an MGRS button (one GPS fix) and a DTG button: `DD HHMM Z MMM YY`,
  Z = NATO zone letter of the phone's offset (LT: C summer, B winter).
- Templates are edited in the app and shared as JSON text (same format as 0.1.0).
- Same app id as 0.1.0 (`lt.tacreports`): on first start `androidApp/.../AndroidStores.kt` copies
  0.1.0's templates and settings into the new store.

## Where things live
| Area | Files |
|---|---|
| Template model, report text, JSON, built-in examples, storage | `shared/src/commonMain/.../model/*` |
| MGRS (pure Kotlin); GPS per platform | `shared/.../geo/Mgrs.kt`, `Locator.kt` + `androidMain/.../geo/OneFix.kt`, `iosMain/.../geo/Locator.ios.kt` |
| All screens (Compose) and HUD style | `shared/src/commonMain/.../ui/*` |
| UI texts EN + LT | `shared/.../Strings.kt` (code, not resources, so the in-app language switch is live) |
| Platform contract (storage, clipboard, share, time) | `shared/.../Platform.kt` + `Platform.android.kt` / `Platform.ios.kt` |
| Android shell: bubble service, activities | `androidApp/src/main/kotlin/lt/tacreports/*` |
| iOS shell: SwiftUI host, App Intent, XcodeGen spec | `iosApp/*`, `shared/src/iosMain/.../MainViewController.kt` |

Look: near-black teal `#020B0D`, cyan `#2EE6D0`, amber `#FFB000`, corner-bracket panels,
Chakra Petch + JetBrains Mono (in `shared/src/commonMain/composeResources/font`).

## Building
- Local (Windows): portable JDK 21 and Android SDK in `C:\Users\Kindziulis\tools`
  (`local.properties` points there, not committed). Set `JAVA_HOME` to `tools\jdk-21`, then
  `./gradlew :shared:testDebugUnitTest :androidApp:assembleDebug`.
- iOS needs macOS: on a Mac, `cd iosApp && xcodegen` then open `TacReports.xcodeproj`; Xcode's build
  phase runs Gradle for the Kotlin framework. CI does the same on a `macos-15` runner.
- `.github/workflows/app.yml` (repo root, runs in this folder): Android tests + `assembleRelease`, published
  as the `android-latest` release (phone link: github.com/MantasR/TacReports/releases/download/android-latest/TacReports.apk); iOS tests +
  unsigned simulator build. Installing on an iPhone needs an Apple Developer account and signing.
- Android signing: `SIGNING_KEYSTORE_B64` + `SIGNING_PASSWORD` (+ optional `SIGNING_ALIAS`) as secrets,
  else the debug key. Never commit a keystore or password.
- Bump `versionCode` / `versionName` in `androidApp/build.gradle.kts` and `MARKETING_VERSION` /
  `CURRENT_PROJECT_VERSION` in `iosApp/project.yml` for every build handed over.

## Conventions
- Shared code first; platform code only where the OS differs. Small focused files, KDoc on
  non-obvious classes, no heavy libraries (no Material icons).
- Commit per feature with a descriptive message.
