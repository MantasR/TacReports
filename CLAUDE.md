# TacReports repo: notes for Claude

App for quick military-style text reports (SITREP, CONTREP, MEDEVAC, PERSREP...): pick a template,
fill in the blanks, copy, paste into a messenger. Read `MEMORY.md` (this folder) for the idea,
decisions and open questions; it covers both projects below.

| Folder | What | Status |
|---|---|---|
| `app/TacReports` | Kotlin Multiplatform + Compose Multiplatform app, Android + iOS (0.2.0+) | **Current: new work goes here** |
| `ProofOfConcept/TacReports` | The first, Android-only native app (0.1.0) | Kept for reference; don't change unless asked |

Each folder is its own Gradle project with its own `CLAUDE.md`; run Gradle from inside it.
CI lives at the repo root: `.github/workflows/app.yml` and `proof-of-concept.yml`, each triggered
only by changes in its own folder.

**This repo is PUBLIC.** Don't copy code from the private SimpleGrid repo here, and don't put
personal or unit details in commits, code or notes. Commit per feature with a descriptive message.
