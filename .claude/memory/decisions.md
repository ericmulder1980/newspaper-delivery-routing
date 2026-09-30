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
**Status:** Superseded by DEC-014
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

---

### DEC-013: Domain and data in a Kotlin Multiplatform `:core` module (android + jvm)
**Date:** 2026-09-29
**Status:** Accepted (supersedes the "single app module" part of DEC-001)
**Deciders:** User (chose this over Robolectric and emulator-only CI tests)
**Related:** FND-004, DEC-001, DEC-010

**Context:** Plan §10 asks for in-memory Room tests. The user installs APKs by file transfer (no adb), so device tests aren't practical. Google's Room testing guide says: "We don't recommend Android local unit tests with Robolectric. Use local JVM tests using Room KMP instead." That requires the database in a multiplatform module with a JVM target.

**Options Considered:**
1. **Robolectric in the single module:** simplest, but advised against by Google, and it tests against a different SQLite.
2. **KMP `:core` module:** Google's recommended setup. Chosen.
3. **Instrumented tests on a CI emulator only:** realistic, but a slow iteration loop.

**Decision:**
- `:core` uses `org.jetbrains.kotlin.multiplatform` + `com.android.kotlin.multiplatform.library`, with targets `android` and `jvm`. The jvm target exists only for tests and isn't shipped.
- `commonMain` holds `domain` (models, rules, repository interfaces) and `data` (Room entities/DAOs/database, repositories, DataStore settings). `androidMain` creates the database and settings file on the device. `:app` keeps UI, ViewModels and Hilt wiring.
- Room 2.8.5 in KMP mode with `BundledSQLiteDriver`, so the phone and the host tests use the same SQLite build. Transactions use `@Transaction` DAO methods.
- **`androidx.sqlite` is pinned to 2.6.2**, the version Room 2.8.5 is built against. `sqlite-bundled` 2.7.0+ dropped the macOS Intel (x86_64) host binary, and this dev machine is an Intel Mac.

**Consequences:**
- The compiler now keeps `domain` Android-free. `DomainPurityTest` still guards against androidx and Dagger imports in `domain`.
- KMP creates no `test` task, so `:core` registers a `test` alias for `jvmTest`. Plain `./gradlew test` runs everything.
- The APK grew to about 5.9 MB, mostly bundled SQLite for 4 ABIs. Restricting to ARM ABIs would save about 2.4 MB (optional).
- **Future risk:** moving to Room 3 / sqlite 2.7+ breaks local DB tests on this Intel Mac. They'd then run in Linux CI only, or on a newer Mac.
- The Room plugin refuses to overwrite an exported schema with different content for the same version, so schema changes can't slip in without a version bump. Running only `:core:jvmTest` after an entity change can leave a stale Android schema copy; a full build (`assemble`) fixes it.

---

### DEC-014: No round persistence and no check-offs: round mode is a live list
**Date:** 2026-09-29
**Status:** Accepted (supersedes DEC-005)
**Deciders:** User
**Related:** RND-A, RND-B, BLD-06, FND-004; plan §4.4, §5

**Context:** Plan §5 has Round and RoundItem tables: a snapshot at round start plus per-address check-offs. That makes edits during a round apply only from the next round, and it drives RND-05/06/07/09, BLD-07, RND-11 and RND-12. The user reviewed this: the snapshot adds no value, and progress doesn't need to be tracked per house per round.

**Options Considered:**
1. **Frozen snapshot + check-offs** (plan): rejected, because corrections made mid-round don't apply and it adds complexity.
2. **Live list + saved check-offs:** rejected by the user.
3. **Live list, no check-offs:** chosen.

**Decision:**
- There are no `round` or `round_item` tables. Schema v1 is `route`, `segment`, `building`, `address`.
- A round is only today's contents (`RoundContents`: newspaper and/or leaflets), held in the round screen's state. Android restores it after process death.
- The round list and the take-along counts (RND-02, via `deliverySummary`) are always calculated from the live addresses, so every edit applies immediately.
- Dropped from v1: RND-05 (check-off/undo), RND-06 (progress), RND-07 (round state survives restart), RND-09 (focus next delivery), BLD-07 (check off per apartment / "Building done"), RND-11 (end-of-round summary), RND-12 (round history).

**Consequences:**
- Simpler schema and round mode. M2's exit criterion becomes "a full round can be walked with the live list".
- RND-10 ("next up" view: current street + next 3 deliveries) depended on knowing where you are. It needs redefining (for example scroll-position based) or dropping. That's open for the user.
- The plan (v0.4) still describes check-offs; update it at its next revision.

---

### DEC-015: Theme from the prototype, contrast-corrected; "dimmed" means style, not low contrast
**Date:** 2026-09-29
**Status:** Accepted
**Deciders:** User (approved colour table and dimming approach)
**Related:** FND-002, RND-B, RND-04, NFR-03, NFR-04, SET-01

**Context:** The approved prototype (dark only) had four colour pairs below the 7:1 text / 3:1 edge targets of NFR-03. RND-04 asks for skipped houses to be "dimmed", which conflicts with 7:1 if that means lower contrast.

**Decision:**
- Dark theme uses the prototype colours, with these raised: secondary text on cards `#A7A9AE` → `#B1B3B8`; NEE/NEE text `#9A9DA3` → `#AEB1B6`; "does not exist" text `#7D8087` → `#AAADB3`; NEE/NEE tile edge `#4A4D53` → `#6E7178`.
- Light theme: off-white `#F4F1EA` page, near-black text, olive `#3A4000` as the accent for text and icons. Yellow `#E8FF00` is kept for filled accents (Start round, "both" tiles) with an olive edge.
- **Dimming by style:** houses that get nothing keep text ≥ 7:1 and are shown dimmed through an outline-only tile (no fill) and lighter weight. "Does not exist" also gets a dashed edge and strikethrough.
- Fonts: Bebas Neue (headings) and Barlow (text), bundled with their OFL licences in `assets/licenses`.
- Delivery-state colours are separate theme roles (`KrantenwijkTheme.colors.both`/`newspaperOnly`/`nothing`/`doesNotExist`).

**Consequences:** `ThemeContrastTest` checks every text/background pair (≥ 7:1) and every tile or control edge (≥ 3:1) in both themes. It already caught one case the manual table missed ("does not exist" text on cards). Any future colour change must pass it.

---

### DEC-016: Number check edits the saved section (save first, then check)
**Date:** 2026-09-30
**Status:** Accepted
**Deciders:** Claude (presented to the user with ADR-B)
**Related:** ADR-B (ADR-12, ADR-03), ADR-A, ONB-A

**Context:** In the prototype, numbers are checked in a draft before the section is saved. Plan ADR-03 also needs the same editing on existing sections.

**Decision:** "Check numbers" saves the section immediately and opens the street-section screen. The same screen serves the number check after adding (ADR-12) and later edits (ADR-03). Each change is written straight away (NFR-05). Navigation replaces the add form with the number check, so back returns to the route.

**Consequences:** One screen and one set of tests instead of two. Abandoning the check leaves a saved section, which can be deleted with "Delete street section" (with confirmation).

---

### DEC-017: "Leaflets only" is not a valid delivery state
**Date:** 2026-09-30
**Status:** Accepted
**Deciders:** User
**Related:** STK-A, RND-A (RND-01), ADR-09, plan §1.2, §3.6

**Context:** Plan §1.2 lists four states (newspaper only, leaflets only, both, nothing), and §3.6 / RND-01 allow a round without the newspaper. STK-A added `DeliveryKind.LEAFLETS_ONLY` and proposed a colour for it later.

**Decision:** An address only ever shows one of three states: **nothing**, **newspaper only**, or **newspaper + leaflets**. No colour or UI is needed for leaflets only.

**Follow-up (resolved with the user, 2026-09-30):**
- NEE/NEE means nothing (unchanged).
- **A round always includes the newspaper**, and leaflets are optional and vary per week. `RoundContents` now only holds `leaflets`, and `newspaper` is always true. The app needs no leaflet schedule, because the choice is made at round start (RND-01).
- **The "no newspaper" exception (ADR-09) means nothing at all**, since leaflets never go without the newspaper. This follows from DEC-017; the user may still refine it.
- `DeliveryKind.LEAFLETS_ONLY` is removed. A test checks every combination of sticker, exists, exceptions and round and asserts that leaflets are never delivered without the newspaper.

---

### DEC-018: Street sections are ordered by creation; "next up" is the next section
**Date:** 2026-09-30
**Status:** Accepted
**Deciders:** User
**Related:** ADR-C (ADR-05), RND-10, FND-004

**Context:** RND-10 ("next up": current street plus the next 3 deliveries) assumed check-offs, which were dropped (DEC-014). ADR-05 (drag-and-drop reordering) is a Must in the plan.

**Decision:**
- Street sections have a sort value, which is their creation order by default. `segment.position` already works this way: new sections are appended and deleting closes the gap.
- **"Next up" means the next street section in that order.** RND-10 is redefined accordingly.
- **Changing the order by drag-and-drop is a nice-to-have** (the user's wording), so ADR-05 is downgraded from Must to Should.

**Consequences:** The reorder repository method and its tests already exist (FND-004). Only the drag-and-drop UI is deferred.


---

### DEC-019: No round contents choice; the list always shows newspaper + leaflets
**Date:** 2026-09-30
**Status:** Accepted (refines DEC-017)
**Deciders:** User (on Claude's recommendation)
**Related:** RND-A (RND-01, RND-02), RND-B, DEC-014, DEC-017

**Context:** DEC-017 kept a per-round "leaflets included?" choice, which would change the take-along counts and turn "newspaper + leaflets" houses into "newspaper only" for that round. The user noted that the deliverer simply ignores the leaflet information in weeks without leaflets.

**Options Considered:**
1. **Toggle at round start (plan RND-01):** the list adapts per week, at the cost of an extra choice every round and an extra screen.
2. **No toggle; always show full information:** chosen.

**Decision:**
- **RND-01 is dropped.** There's no round-start choice. "Start round" goes straight to the walking list.
- Round mode and the take-along counts always use the full view, newspaper + leaflets (`FullRound`), just like the editor. Counts show both numbers ("57 newspapers · 41 leaflets") and the deliverer uses what applies.
- Nothing about a round is stored or chosen (see also DEC-014).

**Consequences:**
- RND-A shrinks to "Start round → live list with both counts at the top". The `RoundStart` placeholder screen and the `RoundMode(newspaper, leaflets)` navigation key are simplified when RND-A is built.
- `RoundContents` remains only as the rules' parameter, always `FullRound`. It can be simplified away later if nothing else needs it.
