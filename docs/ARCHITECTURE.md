# Krantenwijk – Architecture

High-level architecture for the Krantenwijk Android app. The full requirements, data model and
rationale live in [`specs/krantenwijk-project-plan.md`](specs/krantenwijk-project-plan.md);
decisions are recorded in [`.claude/memory/decisions.md`](../.claude/memory/decisions.md).

## Overview

A single-user, fully offline Android app. It shows a newspaper deliverer, in walking order, which
addresses get the newspaper, leaflets, both or nothing, based on each mailbox's sticker.

```
┌──────────────────── :app (Android application) ────────────────────┐
│  ui.editor   ui.round   ui.settings   (Compose + Material 3)       │
│        │          │          │                                     │
│        ▼          ▼          ▼                                     │
│             ViewModels  (StateFlow UI state)                       │
│  di/  ── Hilt DataModule: creates DB, DataStore, repositories      │
└─────────────────────────────┬──────────────────────────────────────┘
                              │ depends on
┌─────────────────────────────▼─── :core (Kotlin Multiplatform) ─────┐
│  commonMain                                                        │
│   domain  ── models, rules (deliveryFor, generateRange, …),        │
│              repository interfaces   (no Android, no Room)         │
│   data    ── Room entities/DAOs/database, Room/DataStore           │
│              repository implementations                            │
│  androidMain ── createDatabase(context), createSettingsDataStore   │
│  jvmTest     ── domain + database tests on the host (DEC-013)      │
└────────────────────────────────────────────────────────────────────┘
```

Dependencies point **downward only**. `domain` depends on nothing but Kotlin and coroutines;
`data` implements its repository interfaces. The `jvm` target of `:core` exists only so all domain
and database tests run on the host with the same bundled SQLite as the phone (DEC-013).

## Stack

| Area | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM + domain layer; `:app` + KMP `:core` module (DEC-013) |
| DI | Hilt |
| Async | Coroutines + Flow |
| Persistence | Room 2.8 (KMP, BundledSQLiteDriver; explicit, non-destructive migrations), DataStore |
| Backup | kotlinx.serialization JSON via Storage Access Framework |
| Testing | JUnit Jupiter + Turbine on the JVM (`:core:jvmTest`, in-memory Room), Compose UI tests later |
| Build/CI | Gradle Kotlin DSL + version catalog, GitHub Actions |
| Min / target SDK | 26 / latest stable |

Exact library versions are pinned in `gradle/libs.versions.toml` (created in FND-001).

## Data model (summary)

| Entity | Purpose |
|---|---|
| Route | The single route (name, town) |
| Segment | Part of a street, one side or both, one direction; `position` = walking order |
| Building | Apartment building at a house number (optionally with its own addition, e.g. 8A: schema v3, DEC-031) within a segment |
| Address | House number + addition, sticker, exists flag, exceptions, note; optional `buildingId` |
| CompletedRound | A finished round: start, end, newspaper and leaflet counts (schema v2, DEC-030) |

Delivery results are **computed**, never stored. Round mode shows a live list calculated from the
addresses, without check-offs (DEC-014). Only the round's timer is kept: the active round (start time
and current section) in DataStore, and each finished round in Room, for best times (DEC-030).

## Key constraints

- **Offline & private:** no INTERNET permission, no third-party SDKs, no background work (DEC-003).
- **Outdoor usability:** in round mode, touch targets ≥ 56 dp, text ≥ 20 sp, contrast ≥ 7:1. Status is never shown by colour alone.
- **Reliability:** every write is committed immediately, so route data survives a force-close.
- **Localisation:** English default, Dutch translation, glossary in plan §8.1 (DEC-007).
- **Distribution:** signed APK sideloaded onto the user's phone. The release keystore must be backed up (DEC-008).

## Source layout

```
app/src/main/java/nl/ericmulder/krantenwijk/
├── ui/{editor,round,settings,theme}/   # screens (from FND-002)
└── di/                                 # Hilt modules
core/src/commonMain/kotlin/nl/ericmulder/krantenwijk/
├── domain/{model,rules,repository}/    # pure Kotlin
└── data/{db,repository,settings}/      # Room + DataStore
core/src/androidMain/…/data/            # device file locations
core/src/jvmTest/…                      # all domain + data tests
core/schemas/                           # exported Room schemas (commit every version)
```

*Update this file whenever a structural decision changes (and record it in decisions.md).*
