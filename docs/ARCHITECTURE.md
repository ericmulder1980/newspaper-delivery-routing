# Krantenwijk – Architecture

High-level architecture for the Krantenwijk Android app. The full requirements, data model and
rationale live in [`specs/krantenwijk-project-plan.md`](specs/krantenwijk-project-plan.md);
decisions are recorded in [`.claude/memory/decisions.md`](../.claude/memory/decisions.md).

## Overview

A single-user, fully offline Android app. It shows a newspaper deliverer, in walking order, which
addresses get the newspaper, leaflets, both or nothing, based on each mailbox's sticker.

```
┌──────────────────────────── app module ────────────────────────────┐
│                                                                    │
│  ui.editor   ui.round   ui.settings   (Compose + Material 3)       │
│        │          │          │                                     │
│        ▼          ▼          ▼                                     │
│             ViewModels  (StateFlow UI state)                       │
│                     │                                              │
│                     ▼                                              │
│  domain   ── use cases + pure rules (no android.*)                 │
│              deliveryFor · generateRange · generateUnits           │
│              naturalSort · buildingSummary                         │
│                     │                                              │
│                     ▼                                              │
│  data     ── repositories ─┬─ Room (SQLite)                        │
│                            ├─ DataStore (settings)                 │
│                            └─ Backup (kotlinx.serialization + SAF) │
└────────────────────────────────────────────────────────────────────┘
```

Dependencies point **downward only**. The `domain` package must not import Android classes, so
all delivery rules run as plain JVM unit tests.

## Stack

| Area | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM + domain layer, single module |
| DI | Hilt |
| Async | Coroutines + Flow |
| Persistence | Room (explicit, non-destructive migrations), DataStore |
| Backup | kotlinx.serialization JSON via Storage Access Framework |
| Testing | JUnit 5, Turbine, Compose UI tests, in-memory Room |
| Build/CI | Gradle Kotlin DSL + version catalog, GitHub Actions |
| Min / target SDK | 26 / latest stable |

Exact library versions are pinned in `gradle/libs.versions.toml` (created in FND-001).

## Data model (summary)

| Entity | Purpose |
|---|---|
| Route | The single route (name, town) |
| Segment | Part of a street, one side or both, one direction; `position` = walking order |
| Building | Apartment building at a house number within a segment |
| Address | House number + addition, sticker, exists flag, exceptions, note; optional `buildingId` |
| Round | One delivery round: what's included, start/finish time |
| RoundItem | Snapshot per address taken at round start, plus `doneAt` |

Delivery results are **computed**, never stored, except in the RoundItem snapshot (DEC-004, DEC-005).

## Key constraints

- **Offline & private:** no INTERNET permission, no third-party SDKs, no background work (DEC-003).
- **Outdoor usability:** in round mode, touch targets ≥ 56 dp, text ≥ 20 sp, contrast ≥ 7:1. Status is never shown by colour alone.
- **Reliability:** every write is committed immediately, and round state survives a force-close.
- **Localisation:** English default, Dutch translation, glossary in plan §8.1 (DEC-007).
- **Distribution:** signed APK sideloaded onto the user's phone. The release keystore must be backed up (DEC-008).

## Package layout (planned)

```
app/src/main/java/.../krantenwijk/
├── domain/        # models, rules, use cases (pure Kotlin)
├── data/          # Room entities/DAOs, repositories, DataStore, backup
├── ui/
│   ├── editor/    # onboarding, route/segment/building editors
│   ├── round/     # round start, round mode
│   ├── settings/
│   └── theme/
└── di/            # Hilt modules
```

*Update this file whenever a structural decision changes (and record it in decisions.md).*
