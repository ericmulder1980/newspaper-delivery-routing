# Architectural Decision Records

Track significant decisions. Each decision is immutable once accepted — supersede rather than edit.

---

## Decision Template

### DEC-XXX: [Title]
**Date:** YYYY-MM-DD
**Status:** Proposed | Accepted | Superseded by DEC-YYY | Deprecated
**Deciders:** [Who made this decision]
**Related:** [Feature IDs, Issue IDs, other Decision IDs]

**Context:**
[What is the issue that we're seeing that is motivating this decision?]

**Options Considered:**
1. **[Option A]**
   - Pros: [...]
   - Cons: [...]

2. **[Option B]**
   - Pros: [...]
   - Cons: [...]

**Decision:**
[What is the change that we're proposing and/or doing?]

**Rationale:**
[Why did we choose this option over others?]

**Consequences:**
- [What becomes easier]
- [What becomes harder]
- [What new constraints are introduced]

---

## Active Decisions

> DEC-001 to DEC-009 were recorded at initialization (2026-09-25) from the project plan
> (`docs/specs/krantenwijk-project-plan.md`, §8, §11 and §12), which the user had already decided.
> See the plan for full context; these entries capture the rationale for future sessions.

### DEC-001: Native Android with Kotlin, Compose and MVVM + domain layer
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User (project plan §8)
**Related:** FND-001, FND-002, FND-003

**Context:** One user, Android only, must be usable outdoors in the dark with one hand.

**Decision:** Kotlin, Jetpack Compose + Material 3, MVVM (UI → ViewModel → UseCases → Repository → Room), Hilt for DI, Coroutines + Flow. A single app module with packages `data`, `domain`, `ui.editor`, `ui.round`, `ui.settings`.

**Rationale:** Android standard stack; Compose gives good dark-theme and accessibility support. A multi-module setup (NowInAndroid style) is overkill for a one-user app.

**Consequences:**
- `domain` must not import any `android.*` class, so delivery rules are testable on the JVM.
- If the codebase grows, packages can be promoted to modules later.

---

### DEC-002: Room + DataStore, never-destructive migrations
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User (plan §5, §8, §11.3)
**Related:** FND-004

**Decision:** Relational data in Room (SQLite) with exported schemas and explicit migrations; simple settings in Jetpack DataStore.

**Rationale:** Reliable offline relational storage; losing the route means re-surveying it on foot.

**Consequences:**
- `fallbackToDestructiveMigration` is forbidden.
- Every schema version gets a migration test.

---

### DEC-003: Fully offline, no third-party SDKs
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User (plan NFR-01, NFR-06, NFR-09)
**Related:** FND-001, DATA-A, REL-A

**Decision:** No INTERNET permission, no analytics/tracking/crash SDKs, no background work or location. Data leaves the device only via user-initiated export.

**Rationale:** Addresses combined with sticker preferences are personal data under AVG/GDPR; the app must also work without mobile data at 5 a.m.

**Consequences:**
- Crash reporting is a local log file the user exports manually.
- Any dependency that adds INTERNET to the merged manifest must be rejected.

---

### DEC-004: Delivery rules live in exactly one domain function; stickers stored as observed
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User (plan §3.3, decision log #3)
**Related:** FND-003, STK-A, RND-A

**Decision:** Store the physical sticker (NONE / JA / NEE_JA / NEE_NEE) per address. Compute what to deliver only in `deliveryFor(sticker)` / `deliveryFor(address, round)`. Delivery results are never stored except in the round snapshot.

**Rationale:** NONE and JA currently behave the same, but storing what's actually on the mailbox lets the rules change without re-surveying the route.

**Consequences:** UI code never interprets stickers directly.

---

### DEC-005: Rounds work from a snapshot
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User (plan §5 RoundItem)
**Related:** RND-A, RND-C

**Decision:** Starting a round writes one RoundItem per deliverable address (deliverNewspaper, deliverLeaflets, doneAt). Round mode reads RoundItems, not live addresses.

**Rationale:** Editing a sticker mid-walk must not reshuffle the list the deliverer is following.

**Consequences:** Edits during a round take effect from the next round.

---

### DEC-006: Apartment buildings are a separate entity; counts are per apartment
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User (plan §3.7, decision log #9)
**Related:** BLD-A, BLD-B, RND-D

**Decision:** A `Building` groups addresses sharing a house number with suffixes. It's shown as one row, and you zoom in to a tile grid to see the apartments. Each apartment is a normal Address with its own sticker, flags and note. All counts are per apartment.

**Rationale:** 26 rows for 12A–12Z would make the street list unreadable. The grid mirrors the bank of mailboxes in the entrance hall.

**Consequences:** Two houses sharing a number (14A/14B) stay standalone unless the user explicitly makes them a building.

---

### DEC-007: English default resources, full Dutch translation
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User (plan LANG-01/02, NFR-08, §8.1, decision log #6)
**Related:** FND-002

**Decision:** English in `values/`, Dutch in `values-nl/`. The app follows the device language, with per-app language on Android 13+ via `locales_config`. Domain terms follow the glossary in plan §8.1. Code uses English names.

**Rationale:** English gives a fallback for any device language. The user's daily language is Dutch.

**Consequences:** No hard-coded strings (lint enforced), and plural forms use plural resources.

---

### DEC-008: Direct APK distribution
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User (plan §11, decision log #7)
**Related:** FND-001, REL-A, milestone M4

**Decision:** Distribute as a signed release APK, installed manually, and never through the Play Store. Register a free limited-distribution developer verification account and the package name before the 2027 global rollout.

**Rationale:** One user, one device. The Play Store adds overhead without benefit.

**Consequences:**
- Losing the release keystore blocks updates, so it must be backed up in two places and checked yearly.
- `versionCode` is incremented for every APK.
- The user exports a backup before every update.

---

### DEC-009: Adapt boilerplate to an Android/Gradle project
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** User + Claude (initialization)
**Related:** CLAUDE.md

**Decision:** Replace the boilerplate's `pnpm` commands with Gradle wrapper commands. Keep domain memory, guardians and the review flow. Claude builds; the user tests on device and approves. `docs/SCRATCHPAD.md` is not used because working notes go in `progress.log`, following the domain-memory design doc.

**Consequences:**
- Guardian file detection in `.claude/settings.json` targets TS/JS and may need `*.kt` patterns added later.
- The Prettier post-edit hook doesn't apply to Kotlin.

---

### DEC-010: Toolchain and library versions for v1 (Room 2.8, Navigation 3)
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** Claude (researched), user (approved FND-001)
**Related:** FND-001, FND-002, FND-004

**Context:** The plan names libraries but not versions or a navigation library. Versions were checked against Google Maven / Maven Central on 2026-09-25.

**Decision:**
- AGP 9.4.1 using its built-in Kotlin (no `kotlin-android` plugin applied), with Kotlin pinned to 2.4.20 at the root. KSP 2.3.12, Gradle 9.8.0, JDK 21 to run the build, Java 17 bytecode.
- compileSdk/targetSdk 37 (Android 17), minSdk 26.
- **Room 2.8.5** rather than Room 3.0, which is still alpha and uses new `androidx.room3` coordinates.
- **Navigation 3 (1.2.0)**, stable since 2026-09-23 and Google's recommended Compose navigation, rather than Navigation Compose 2.x.
- Compose BOM 2026.09.00, Hilt/Dagger 2.60.1, androidx.hilt 1.4.0.
- JUnit Jupiter 6.1.3 on the JUnit Platform (API-compatible with the plan's "JUnit 5") via `useJUnitPlatform()`. The mannodermaus plugin will only be added if instrumented JUnit tests are needed.
- No AppCompat: the per-app language comes from `generateLocaleConfig` (Android 13+). Older devices follow the device language, which LANG-01/02 allow.

**Consequences:**
- Room 2.x is in maintenance mode, so migrate to Room 3 after it's stable (post-v1). Schemas are exported, so a migration can be verified.
- targetSdk 37 ignores orientation locks on large screens (≥600dp). Phones in portrait (NFR-07) are unaffected.

---

### DEC-011: Disable Android cloud backup and device transfer
**Date:** 2026-09-25
**Status:** Accepted
**Deciders:** Claude (derived from NFR-06), user (approved FND-001)
**Related:** FND-001, DATA-A

**Context:** Android auto-backup copies app data to the user's Google Drive by default. NFR-06 requires that data leaves the device only through a user-initiated export.

**Decision:** Set `allowBackup="false"` and add `data_extraction_rules.xml` excluding every domain from cloud backup and device transfer.

**Consequences:** Replacing a phone relies entirely on the JSON export (DATA-A), which makes DATA-A and the backup reminder (DATA-03) more important.

---

## Superseded/Deprecated Decisions

*Decisions that have been replaced or are no longer relevant go here for historical reference.*

---

### DEC-012: House number ranges are entered low to high; reverse walking is a segment option
**Date:** 2026-09-29
**Status:** Accepted
**Deciders:** User (overrode Claude's first proposal, which followed the prototype's swap button)
**Related:** FND-003, ADR-A (ADR-02, ADR-11), ADR-C (ADR-06)

**Context:** The plan conflicts with itself: ADR-02 says "validation rejects first > last", while ADR-11 and §6 describe from/to "in walking order" with a swap button. The approved prototype followed ADR-11 (Kerkstraat odd 23 → 1).

**Options Considered:**
1. **Enter the range in walking order** (prototype): `from > to` means walk descending. Rejected: entering numbers high to low is unnatural.
2. **Always enter low to high, with walking order as a separate option:** chosen.

**Decision:**
- Ranges are always entered low to high. `generateRange()` returns ascending numbers and rejects `from > to`. `checkRange()` reports this as the blocking issue `FromAfterTo`.
- Walking direction is a per-segment option, a simple "walk in reverse order" checkbox (`Direction.DESCENDING`), applied with `inWalkingOrder()`.
- As in the prototype, a section holds at most 300 numbers (`MAX_RANGE_SIZE`).

**Consequences:**
- The prototype's swap button in the "add street section" screen becomes a "walk in reverse order" checkbox (ADR-A/ADR-11). ADR-02's validation stands as written.
- ADR-06 (change direction per segment) edits that same flag.
