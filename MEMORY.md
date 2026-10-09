# TacReports: project memory

## The idea (owner, 2026-10-03)
An app for report templates. Android first (0.1.0); since 2026-10-05 also iOS (0.2.0). Owner's words: "allow in short cuts to set up reports templates".

Clarified by the owner (2026-10-03):
- Templates change for every mission, so the user must be able to set them up (create/edit) quickly.
- A **floating bubble** (over other apps) opens the template list. Tap a template, fill in ONLY the blanks
  that need input, and the finished text is **automatically copied to the clipboard**, ready to paste
  into whatever messenger is in use.

## Example reports (from the owner's printed card set, Lithuanian)
Every card has a header: Kam (to), Nuo (from), Raportas (type), Laikas (time), Raporto Nr. (number).
- **SITREP**: B Savos pajėgos (1 Vieta, 2 Veiksmai, 3 Statusas: amunicija, sužeistieji, ekipuotė);
  C Priešo pajėgos (1 Dydis, 2 Veiksmai, 3 Vieta); D Vado ketinimai ir pasiūlymai; E Papildoma informacija.
- **CONTREP**: A Pranešimo siuntėjo vieta; B = SALUTE: S Dydis (grandis, skyrius, būrys, kuopa),
  A Priešo veiksmai, L Priešo koordinatės, U Priešo padalinys (žvalgai, pėst., mech.), T Kontakto laikas,
  E Priešo turima įranga; C Savų pajėgų veiksmai; D Papildoma informacija.
- **MEDEVAC (9 eilutės)**: 1 Surinkimo vieta; 2 Dažnis, šaukinys, slaptažodis;
  3 Sužeistųjų sk. pagal pirmenybę: A Neatidėliotini (iki 2 val), B Neatid.-chirurginiai (2 val),
    C Skubūs (per 4 val), D Įprastinė evak. (per 24 val), E Pagal galimybę;
  4 Speciali įranga: A Nereikalinga, B Keltuvai, C Neštuvai, D Dirbt. plaučių ventiliavimas;
  5 Pagal tipus: L Gulintys (neštuvuose), A Ambulatoriniai (sėdintys);
  6 Surinkimo vietos pavojus: N Priešo nėra, P Priešas gali būti, E Priešas rajone, X Priešas rajone, reikia ginkl. palydos;
  7 Pažymėjimas: A Spalvotas ženklas, B Pirotechnika, C Dūmai, D Nėra, E Kitais būdais, F Pasitiks žmogus;
  8 Tautybė/statusas: A LT karys, B LT civilis, C Ne LT karys, D Ne LT civilis, E Priešo karo belaisvis;
  9 MNG (CBRN): A Branduolinis, B Biologinis, C Cheminis, D Nėra.
- **PERSREP**: A Padalinio identifikacija; B table K/P/E/Iš viso (karininkai, puskarininkiai, eiliniai) for
  1 Etatinis pajėgumas (WE), 2 Priskirti, 3 Esamas pajėgumas, 4 Nedarbingi/sužeisti, 5 Žuvę, 6 Karo belaisviai (PW);
  C Laikas (DTG); D Personalo vertinimas (spalva); E Vado vertinimas.

## Decisions (owner, 2026-10-03)
- Output: one point per line ("label: value"). Bilingual UI (LT + EN). Same dark teal military look as SimpleGrid.
- Fields are all custom, so instead of automatic field types every field gets two buttons:
  **MGRS** (current GPS position) and **DTG** in the format `DD HHMM C/B MMM YY`, where C = summer
  time and B = winter time (NATO zone letters for UTC+3 / UTC+2). Implemented as the phone's current
  offset letter; month in English capitals (OCT). Spacing as the owner wrote it: "03 1825 C OCT 26".

## Decisions (owner, 2026-10-05)
- Port to Kotlin Multiplatform + Compose Multiplatform so one code base serves Android and iOS
  (chosen over React Native: the logic and UI were already Kotlin/Compose).
- Briefly renamed "TacRaports" (2026-10-05); the owner reverted that on 2026-10-08: the name stays
  **TacReports**.
- App id `com.mantasr.tacreports` (Android and iOS; owner's Play Console app, 2026-10-09). 0.1.0 is
  `lt.tacreports`, so the two install side by side; templates move over with Share / Import.
- iOS has no overlay bubble; the replacement is a "New report" App Intent (Shortcuts, Action Button,
  Back Tap) plus the `tacreports://` URL. After Copy the user switches back to the chat by hand.

## Repo layout (owner, 2026-10-08)
- One repo: `ProofOfConcept/TacReports` = the Android-only 0.1.0 (kept as is), `app/TacReports` = the
  KMP version (Android + iOS) where new work happens. This MEMORY.md at the root covers both.

## Design ideas (first proposal)
- Per-mission values set once and reused: Kam, Nuo (callsign), frequency etc. Time (DTG) filled automatically,
  report number auto-incremented. Fields with fixed options (MEDEVAC letters, sizes) as tap-to-pick chips;
  counts as number steppers. Empty optional lines can be dropped from the output.
- Templates editable in the app and shareable (export/import as text or QR) so a team gets the same set.
- Bubble needs the "Display over other apps" permission; clipboard write from it works.
- Could later take the current position / MGRS from SimpleGrid.

## Builds
- **0.1.0** (2026-10-03): first version. Bubble, template picker, fill form with MGRS/DTG buttons and
  option chips, copy to clipboard, drafts kept until copied, "keep last value" fields, template editor,
  share/import as JSON text, the four example cards, LT/EN, MGRS digits setting (10/8/6).
  Built by GitHub Actions only (the session couldn't reach dl.google.com); logic tests also run locally.
  The session also can't download Actions artifacts (blob storage blocked), so the owner gets the APK from
  the run's Artifacts section (a zip with app-release.apk). Signed with the DEBUG key (no signing secrets here).
- **0.2.0** (2026-10-05): KMP port in `app/TacReports`. Same features as 0.1.0 on Android; iOS app
  with the same screens, Core Location MGRS, App Intent. Template JSON format unchanged.

## Open questions
1. DTG spacing: "03 1825 C OCT 26" (as written) or the compact "031825COCT26"?
2. Report number auto-increment? (proposed earlier, not answered)
3. PERSREP "spalva" options (žalia/geltona/raudona/juoda?) not added, since the card doesn't list them.

## Notes
- Separate from SimpleGrid on purpose (the owner's decision); may later reuse its look and team relay.
- This repo is PUBLIC.
- iOS open points: Apple Developer account for installing on a phone; Control Center / widget entry
  points (need a Swift widget extension) not built yet.
