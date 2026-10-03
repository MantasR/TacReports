# TacReports: project memory

## The idea (owner, 2026-10-03)
An Android app for report templates. Owner's words: "allow in short cuts to set up reports templates".

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

## Design ideas (proposed, not yet agreed)
- Per-mission values set once and reused: Kam, Nuo (callsign), frequency etc. Time (DTG) filled automatically,
  report number auto-incremented. Fields with fixed options (MEDEVAC letters, sizes) as tap-to-pick chips;
  counts as number steppers. Empty optional lines can be dropped from the output.
- Templates editable in the app and shareable (export/import as text or QR) so a team gets the same set.
- Bubble needs the "Display over other apps" permission; clipboard write from it works.
- Could later take the current position / MGRS from SimpleGrid.

## Open questions
1. Output format of the copied text (one line per field like "1. Vieta: ..." vs. compact "1/.../2/...")?
2. Bilingual UI (LT + EN) like SimpleGrid?
3. Same military look as SimpleGrid?

## Notes
- Separate from SimpleGrid on purpose (the owner's decision); may later reuse its look and team relay.
- This repo is PUBLIC. Ask the owner before relying on that, or before copying SimpleGrid code here.
- Nothing is built yet.
