# TacReports: notes for Claude

Android app for quick military-style text reports (SITREP, CONTREP, MEDEVAC, PERSREP...).
Same owner as SimpleGrid; personal use; the owner tests every build on their own phone.
Read `MEMORY.md` for the idea, decisions and open questions.

**This repo is PUBLIC.** Don't copy code from the private SimpleGrid repo here, and don't put
personal or unit details in commits, code or notes.

## What the app does
- A floating bubble (overlay, foreground service) opens a translucent form over any app.
- Pick a template, fill in only the blanks, tap Copy: the text (one point per line) is on the
  clipboard, ready to paste into a messenger. Empty fields are left out.
- Every field row has an MGRS button (one GPS fix, platform LocationManager, no Play Services) and
  a DTG button: `DD HHMM Z MMM YY`, Z = NATO zone letter of the phone's offset (LT: C summer, B winter).
- Templates are edited in the app (Fill in / Fixed / Section fields, tap-to-pick options,
  "keep last value") and shared as JSON text (Share, then Import from clipboard or share-to-app).

## Where things live
| Area | Files |
|---|---|
| Template model, report text, JSON, built-in examples, storage | `model/*` |
| MGRS (UTM + 100 km letters), one-shot GPS fix | `geo/Mgrs.kt`, `geo/OneFix.kt` |
| Bubble overlay service | `bubble/*` |
| Screens (Compose) and HUD style | `ui/*`, `MainActivity.kt`, `FillActivity.kt` |

Look: near-black teal `#020B0D`, cyan `#2EE6D0`, amber `#FFB000`, corner-bracket panels,
Chakra Petch + JetBrains Mono (both cover Lithuanian letters). Bilingual: every string in
`values/strings.xml` (EN) and `values-lt/strings.xml` (LT).

## Building
- `.github/workflows/android.yml` runs unit tests and `assembleRelease` on every push to `main`
  and uploads the APK as a workflow artifact (download needs a GitHub login).
- In a cloud session the Android SDK host `dl.google.com` may be blocked. The pure-Kotlin logic
  (`model/`, `geo/`) and its tests can still be run with a throwaway Kotlin/JVM Gradle project that
  points its source sets at those folders (Maven Central is reachable).
- Signing: `SIGNING_KEYSTORE_B64` + `SIGNING_PASSWORD` (+ optional `SIGNING_ALIAS`) as secrets,
  else the debug key. Never commit a keystore or password.
- Bump `versionCode` / `versionName` in `app/build.gradle.kts` for every APK handed over.

## Conventions
- Small focused files, KDoc on non-obvious classes, no heavy libraries (no Material icons).
- Commit per feature with a descriptive message; push to `main`.
