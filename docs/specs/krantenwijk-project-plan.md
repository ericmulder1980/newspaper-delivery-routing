# Krantenwijk – Project & Implementation Plan

*Working title: **Krantenwijk** (Dutch for "paper round"). Android app that gives one newspaper deliverer a clear, street-by-street overview of which addresses do and do not receive the newspaper and/or advertising leaflets.*

| | |
|---|---|
| Document status | Draft v0.4 – source document for the development phase (adds apartment buildings, see sections 3.7 and 4.2) |
| Platform | Android only (iOS and other platforms out of scope) |
| Users | One deliverer, one route, one device |
| Data source | Manual entry in the app; no remote address service |
| Distribution | Direct APK (no Play Store) |
| Language | Built in English, full Dutch translation; app follows the device language |

---

## 1. Purpose and scope

### 1.1 Problem
The deliverer walks a fixed route delivering a free door-to-door newspaper (*huis-aan-huisblad*) and, on some rounds, advertising leaflets. Not every house gets the newspaper, and not every house accepts advertising leaflets (folders); this is determined by the sticker on the mailbox. Remembering this per house is error-prone, especially after changes, and early in the morning in the dark.

### 1.2 Goal
Provide one reliable overview of the route in walking order, where every house number clearly shows one of four states for the current round: newspaper only, leaflets only, both, or nothing. Numbers that do not exist are either removed or clearly marked.

### 1.3 In scope (v1)
Manual management of streets and house numbers (bulk entry by number range and even/odd side), a mailbox sticker per address that determines what is delivered, marking non-existing addresses, apartment buildings with individually managed units, defining the walking order, a "round mode" for use while delivering, local storage, backup/restore to a file for data safety, English and Dutch UI, and distribution as a signed APK.

### 1.4 Out of scope (v1)
iOS and other operating systems; multiple users, multiple routes and multiple newspaper titles; sharing routes with other people or devices; fetching addresses from external services (BAG, PDOK, Google); maps and GPS; accounts and cloud sync; Google Play Store distribution; subscription administration or communication with publishers.

---

## 2. User and context of use

There is one user: the deliverer, who owns the device and maintains the route data. Delivery often happens early in the morning (dark, cold, sometimes rain), with a bag, one hand free, possibly wearing gloves.

This drives the design constraints: large touch targets, high contrast, a dark theme, minimal typing during the round, no dependency on mobile data, and a screen that stays on while delivering.

---

## 3. Domain rules and concepts

### 3.1 Dutch addressing
A Dutch address consists of a street name, a house number and an optional addition (*huisnummertoevoeging*), such as `12A`, `12-2`, `12 bis` or `12 hs`. Most streets have even numbers on one side and odd numbers on the other. Some streets only have houses on one side, and numbering is not always symmetrical (e.g. evens go to 80, odds stop at 33).

### 3.2 Number ranges
Addresses are entered as a range: street, first number, last number, and side (even, odd, or all).

| Input | Result |
|---|---|
| Kerkstraat, 2–24, even | 2, 4, 6, … , 24 (12 addresses) |
| Kerkstraat, 1–23, odd | 1, 3, 5, … , 23 (12 addresses) |
| Molenweg, 1–10, all | 1, 2, 3, … , 10 (10 addresses) |

After generating a range, the user can delete numbers, mark numbers as "does not exist" (section 3.4), add numbers with additions (e.g. 14A, 14B), and add single numbers outside the range. A number that is an apartment building with many units is handled as a building (section 3.7).

### 3.3 Mailbox stickers determine delivery
Every address has exactly one sticker value. The delivery rules are fixed as follows:

| Sticker on mailbox | Newspaper | Leaflets |
|---|---|---|
| No sticker | ✅ Yes | ✅ Yes |
| Circular "JA" (yes) sticker | ✅ Yes | ✅ Yes |
| NEE/JA (no/yes) sticker | ✅ Yes | ❌ No |
| NEE/NEE (no/no) sticker | ❌ No | ❌ No |

"No sticker" and "JA" currently lead to the same result. They are stored as separate values anyway, so the data reflects what is actually on the mailbox and the rules can be changed later without re-surveying the route.

The rules are implemented in exactly one place in the domain layer (`deliveryFor(sticker)`), not spread across the UI.

A per-address **exception** (Should-have, ADR-09) allows the deliverer to override the sticker result for special cases, for example a vacant house or a resident who asked verbally not to receive leaflets.

### 3.4 Non-existing addresses
When a range generates a number that does not exist, the user can either **delete** it or **mark it as "does not exist"**. Marking is useful when a number is missing in the sequence, because it confirms the gap is known rather than forgotten. Marked addresses never receive anything, are shown with strikethrough in the editor, and are hidden in round mode by default (with a setting to show them dimmed).

### 3.5 Walking order
A route is an ordered list of **segments**. A segment is a part of a street, walked in one direction, covering one side or both sides.

| Walking pattern | How it is modelled |
|---|---|
| Walk up the evens, come back along the odds | Segment 1: Kerkstraat even, ascending. Segment 2: Kerkstraat odd, descending |
| Zigzag across a quiet street | Segment 1: Kerkstraat all, ascending |
| Do part of a street, return later from the other end | Two segments of the same street, at different positions in the route |

### 3.6 What a round contains
Not every round includes leaflets (and possibly not every round includes the newspaper). When starting a round the user indicates what is being delivered today. The per-address result is then the combination of the round contents and the address's sticker (and exception, if set).

### 3.7 Apartment buildings
Some apartment buildings have a single house number with suffixes per apartment, such as 12A to 12Z. Entering and viewing these as 26 separate lines in the street list would make the route hard to read and hard to maintain, so the app treats them as a **building**:

The building appears in the street list as **one row** at its house number (e.g. "12 · 18 apartments"), with a summary of stickers or deliveries. The user can **zoom in** on the building to see all apartments as a grid of tiles, similar to the bank of mailboxes in the entrance hall. Each tile shows the suffix and the sticker, so stickers can be recorded per apartment while standing in front of the mailboxes.

Each apartment is a normal address (house number + suffix) with its own sticker, "does not exist" flag, exceptions and note. All delivery rules in section 3.3 apply per apartment, and all counts (take-along totals, progress) count apartments, not buildings.

Units are created with a suffix range. Letter ranges (A–Z) are the main case; numeric suffixes (12-1 to 12-20) are also supported because they are common in Dutch flats. Letters skipped by the building (e.g. no 12I) are deleted or marked "does not exist", just like house numbers.

Two houses sharing a number (e.g. a split house 14A and 14B) can remain standalone addresses; a number is only a building when the user creates or groups it as one. Suffixes are sorted naturally: A, B, … Z and 1, 2, … 10 (not 1, 10, 2).

---

## 4. Functional requirements

Priorities follow MoSCoW: **M** = Must (v1), **S** = Should (v1 if time allows, otherwise v1.1), **C** = Could (later).

### 4.1 Route and address management

| ID | Prio | Requirement | Acceptance criteria |
|---|---|---|---|
| ADR-01 | M | Guided first launch: ask the user's name or nickname, then the route name and (optional) town, then go straight into adding street sections. | Four steps with a progress indicator: name → route → street sections (add + check numbers, repeat) → walking order. "Next" is disabled until required fields are filled. Name, route name and town can be edited later in settings. |
| ADR-11 | M | While adding a street section, show a live preview of the generated numbers and a warning when the range does not fit the chosen side. | "2 → 24, even" shows "12 addresses · 2, 4, 6 … 24"; entering 23 on the even side shows a warning that odd numbers are skipped; a swap button reverses the walking direction. |
| ADR-12 | M | After the preview, a number check screen where each number can be marked "does not exist", turned into an apartment building, or deleted, and extra numbers (with additions) can be added. | Tapping a number opens these options; the address count updates immediately; saving adds the section to the route. |
| ADR-02 | M | Add a segment by street name, number range and side (even/odd/all). | "Kerkstraat, 2, 24, even" creates exactly 12 addresses (2…24 even). Validation rejects first > last and warns when an endpoint does not match the chosen side. A preview is shown before saving. |
| ADR-03 | M | Edit a segment's addresses: delete numbers, add single numbers, add numbers with additions. | User can delete 10, add 14A and 14B, and add 26; list stays sorted by number, then addition. |
| ADR-04 | M | Mark an address as "does not exist" and unmark it. | Marked address is struck through in the editor, excluded from all delivery counts, and hidden in round mode by default. |
| ADR-05 | M | Reorder segments within the route. | Drag-and-drop; order persists after restart. |
| ADR-06 | M | Set walking direction per segment (ascending/descending). | Changing direction immediately changes the order in the editor and in round mode. |
| ADR-07 | M | Street name auto-completion from streets already in the route. | Typing "Ker" suggests "Kerkstraat" if it exists. |
| ADR-08 | S | Duplicate detection. | Adding an address that already exists in another segment shows a warning. |
| ADR-09 | S | Per-address exception overriding the sticker result (no newspaper and/or no leaflets). | Exception is clearly marked in editor and round mode; delivery counts respect it. |
| ADR-10 | S | Free-text note per address (e.g. "mailbox at side door", "dog"). | Note icon in round mode; full text on tap. |

### 4.2 Apartment buildings

| ID | Prio | Requirement | Acceptance criteria |
|---|---|---|---|
| BLD-01 | M | Create an apartment building in a segment: house number plus a suffix range (letters, e.g. A–Z, or numbers with a separator, e.g. 12-1 to 12-20). | "Kerkstraat 12, units A–L" creates one building with 12 apartments 12A…12L, shown at the position of number 12 in the segment. A preview is shown before saving. |
| BLD-02 | M | Show a building as one collapsed row in the segment list, with apartment count and sticker summary. | Row reads e.g. "12 · 12 apartments · 3 NEE/JA · 2 NEE/NEE"; non-existing apartments are not counted. |
| BLD-03 | M | Building detail ("zoom in"): all apartments as a grid of tiles showing suffix and sticker. | Grid fits 26 tiles on one phone screen without scrolling in portrait; tapping a tile opens a quick sticker picker. |
| BLD-04 | M | Bulk edit within a building: multi-select apartments or "select all", then set sticker or mark "does not exist". | Selecting 12A–12F and setting NEE/NEE updates exactly those 6. |
| BLD-05 | M | Add or delete individual apartments in an existing building. | Adding 12M to an A–L building places it after 12L. |
| BLD-06 | M | Round mode: building row shows what to deliver for this round (e.g. "12 · 9 newspapers · 5 leaflets") and expands inline to the per-apartment list or grid. | Counts equal the sum of the apartments' deliveries; apartments that receive nothing are dimmed. |
| BLD-07 | M | Round mode: check off per apartment, or "Building done" for all apartments at once (with undo). | Building shows done when all deliverable apartments are checked; progress counts apartments. |
| BLD-08 | S | Optional building name (e.g. "Flat De Linde") shown with the number. | Name shown in editor and round mode. |
| BLD-09 | S | Group existing standalone addresses with the same number into a building, and dissolve a building into standalone addresses. | Stickers, flags and notes are preserved in both directions. |
| BLD-10 | C | Arrange tiles to match the physical mailbox layout (columns × rows). | Grid dimensions configurable per building. |

### 4.3 Sticker and delivery status

| ID | Prio | Requirement | Acceptance criteria |
|---|---|---|---|
| STK-01 | M | Set the sticker per address: none, JA, NEE/JA, NEE/NEE. Default for new addresses: none. | Each value produces exactly the result in the table in section 3.3. |
| STK-02 | M | Bulk edit: select multiple addresses and set the sticker in one action. | Setting NEE/NEE on 5 selected addresses updates all 5. |
| STK-03 | M | Show the resulting delivery (newspaper / leaflets) next to each address in the editor. | Changing the sticker updates the indicators immediately. |
| STK-04 | S | Overview of sticker counts per route (e.g. 180 none, 12 JA, 40 NEE/JA, 25 NEE/NEE). | Counts are shown in the route overview and exclude non-existing addresses. |

### 4.4 Round mode (delivering)

| ID | Prio | Requirement | Acceptance criteria |
|---|---|---|---|
| RND-01 | M | Start a round by choosing what is delivered: newspaper (default on) and leaflets (default off). | At least one must be on; the choice is shown at the top of the round screen. |
| RND-02 | M | Before starting, show how many newspapers and leaflets to take along. | Counts match the number of existing addresses whose sticker/exception allows each item. |
| RND-03 | M | List all addresses in walking order, grouped by segment, with a clear indicator per address: newspaper, leaflets, both, or nothing. | The four states are distinguishable by icon **and** colour (never colour alone), readable at arm's length. |
| RND-04 | M | Addresses that receive nothing in this round are shown dimmed (not hidden) by default, with an option to hide them. | Toggle "Show skipped houses"; default "show". Non-existing addresses follow ADR-04. |
| RND-05 | M | Tap to mark an address as done; undo possible. | Check mark appears; undo via tapping again or a snackbar. |
| RND-06 | M | Progress indicator (e.g. 34 / 61). | Updates on every check-off. |
| RND-07 | M | Round state survives app closure, phone lock and restart. | Force-closing mid-round and reopening restores all check marks. |
| RND-08 | M | Screen stays on in round mode. | No screen timeout while round mode is open (can be disabled in settings). |
| RND-09 | M | Focus on the next address that needs delivery. | After a check-off, the next delivery is highlighted and scrolled into view. |
| RND-10 | S | Compact "next up" view: current street in large type plus the next 3 deliveries. | Toggle between full list and compact view. |
| RND-11 | S | End-of-round summary (duration, delivered, skipped). | Shown when all deliveries are checked or the round is ended manually. |
| RND-12 | C | Round history (last 30 rounds). | List with date, contents, duration, completion. |

### 4.5 Data, language and settings

| ID | Prio | Requirement | Acceptance criteria |
|---|---|---|---|
| DATA-01 | M | All data stored locally; app works fully offline. | Works in airplane mode; no internet permission requested. |
| DATA-02 | M | Export the full route to a backup file (JSON) and restore it. Intended for the user's own data safety and phone replacement, not for sharing. | Export → uninstall → reinstall → import yields an identical route. |
| DATA-03 | S | Backup reminder after significant changes or monthly. | Reminder appears; can be dismissed. |
| DATA-04 | C | CSV import for bulk entry (street, number, addition, sticker). | 200 rows import with preview and an error report. |
| LANG-01 | M | English and Dutch UI; app follows the device language, English as fallback. | On a Dutch device every screen is fully Dutch; no hard-coded strings (lint check). |
| LANG-02 | M | Per-app language choice on Android 13+ via the system app-language setting. | App appears in system settings → Apps → Language, with English and Dutch. |
| SET-01 | M | Theme: light, dark, follow system. | Dark theme meets NFR-03. |
| SET-02 | M | Show app version and build number in settings. | Needed to check which APK is installed (see section 11). |

---

## 5. Data model

Starting point for the Room database. Delivery results are computed, not stored (except in the round snapshot).

| Entity | Key fields | Notes |
|---|---|---|
| **Route** | id, name, town, createdAt | Exactly one row in v1. |
| *Settings (DataStore)* | nickname, onboardingCompleted, theme, display options | Not in Room; the nickname is used only for greetings in the app. |
| **Segment** | id, routeId, streetName, side (EVEN / ODD / ALL), rangeFrom, rangeTo, direction (ASC / DESC), position | `position` defines walking order. |
| **Building** | id, segmentId, houseNumber (int), name (nullable), suffixType (LETTER / NUMBER), separator (e.g. "" or "-") | Occupies the position of its house number within the segment. |
| **Address** | id, segmentId, buildingId (nullable), houseNumber (int), addition (nullable), exists (bool, default true), sticker (NONE / JA / NEE_JA / NEE_NEE), exceptionNoNewspaper (bool), exceptionNoLeaflets (bool), note (nullable) | Sort key within a segment: houseNumber, then natural sort of addition; direction applied on read. Apartments have a buildingId; standalone addresses have none. Exceptions are S (ADR-09). |
| **Round** | id, date, includesNewspaper (bool), includesLeaflets (bool), startedAt, finishedAt (nullable) | One active round at a time. |
| **RoundItem** | roundId, addressId, deliverNewspaper (bool), deliverLeaflets (bool), doneAt (nullable) | Snapshot taken at round start, so edits during a round do not change the list mid-walk. |

**Domain rules** (pure Kotlin functions, fully unit-tested):

`deliveryFor(sticker)` returns (newspaper, leaflets) exactly according to the table in section 3.3.

`deliveryFor(address, round)`: if `!address.exists` → nothing. Otherwise start from `deliveryFor(sticker)`, remove items blocked by exceptions, then remove items not included in the round.

`generateRange(from, to, side)` returns the list of numbers; for EVEN/ODD it starts at the first number in the range matching the side.

`generateUnits(fromSuffix, toSuffix, type)` returns the suffixes of a building (A…L, or 1…20).

`buildingSummary(building, round?)` aggregates sticker counts (editor) or delivery counts (round) over the building's existing apartments.

---

## 6. Screens and key flows

| Screen | Purpose |
|---|---|
| First-launch setup | Four guided steps: (1) name or nickname, (2) route name and town, (3) add street section (street with suggestions of streets already used, side, from/to in walking order with swap, live preview) followed by a number check grid (does not exist / apartment building / delete / add number), repeated per section, (4) walking order with move up/down. Ends on the home screen. |
| Home | Route name, sticker overview, big "Start round" button, entry to route editor. |
| Route editor | Ordered segments with drag handles, direction toggle, "Add segment". |
| Add/edit segment | Street (autocomplete), from, to, side, direction, preview of generated numbers. |
| Segment detail | Addresses with sticker selector and resulting delivery icons; multi-select for bulk sticker change, delete, or "does not exist". |
| Building detail | "Zoomed-in" grid of apartment tiles (suffix + sticker), multi-select, add/delete apartments, building name. |
| Address detail | Sticker, exists flag, exceptions, note (for standalone addresses and individual apartments). |
| Round start | "Newspaper" and "Leaflets" toggles, counts to take along, "Start". |
| Round mode | Walking list or compact view, check-off, progress; buildings expand inline with per-apartment check-off and "Building done". |
| Settings | Theme, show/hide non-existing and skipped addresses, keep screen on, backup/restore, version info. Language via system settings. |

**Setting up the route:** enter nickname → name the route → add segment "Kerkstraat 2–24 even, ascending" → preview → save → delete or mark non-existing numbers, add 14A → add building 12 with apartments A–L, zoom in and set each apartment's sticker from the mailbox bank → multi-select houses with a NEE/JA sticker → set sticker → repeat for next segments → reorder segments into walking order → export a first backup.

**Delivering:** open app → Start round → choose newspaper/leaflets → check counts, pack bag → walk and tap each house when done → finish → summary.

---

## 7. Non-functional requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Offline | All functionality works without network; no internet permission in the manifest. |
| NFR-02 | Performance | Round list scrolls smoothly with 1,000 addresses (including apartments); cold start under 2 s on a mid-range device. |
| NFR-03 | Outdoor usability | Round mode touch targets at least 56 dp, primary text at least 20 sp, contrast ratio at least 7:1 in both themes. |
| NFR-04 | Accessibility | Status never by colour alone; TalkBack labels on all indicators; system font scaling up to 200%. |
| NFR-05 | Reliability | No data loss on app kill, restart or battery shutdown mid-round (writes committed immediately). |
| NFR-06 | Privacy (AVG/GDPR) | Addresses combined with preferences are personal data. No names or contact details of residents stored (only the user's own nickname, locally); no analytics, tracking or third-party SDKs; data leaves the device only via user-initiated export. |
| NFR-07 | Compatibility | Minimum Android 8.0 (API 26); target SDK kept at the latest stable level (see section 11); portrait, 5" to 6.8" phones. |
| NFR-08 | Localisation | All user-facing text in string resources; English in `values/`, Dutch in `values-nl/`; plurals via plural resources ("1 krant" / "12 kranten"); a fixed glossary for domain terms (section 8.1). |
| NFR-09 | Battery | No background work, no location, no polling. |

---

## 8. Architecture and technology

| Area | Choice | Rationale |
|---|---|---|
| Language | Kotlin | Android standard. |
| UI | Jetpack Compose + Material 3 | Declarative, good dark theme and accessibility support. |
| Architecture | MVVM with a domain layer (UI → ViewModel → UseCases → Repository → Room) | Delivery rules testable without Android. |
| Persistence | Room (SQLite) with explicit migrations | Reliable offline relational storage. |
| Settings | Jetpack DataStore | Modern replacement for SharedPreferences. |
| Dependency injection | Hilt | Standard, low boilerplate. |
| Async | Coroutines + Flow | Reactive updates from database to UI. |
| Serialization | kotlinx.serialization | Backup file format. |
| File access | Storage Access Framework | Export/import without storage permissions. |
| Localisation | Android string resources + `locales_config` for per-app language (Android 13+), AppCompat per-app locale API for older versions | Follows Android localisation guidelines; English default ensures a fallback for any device language. |
| Testing | JUnit 5, Turbine, Compose UI tests, in-memory Room | See section 10. |
| Build/CI | Gradle Kotlin DSL + version catalog; CI (e.g. GitHub Actions) runs lint, unit tests and builds a signed-release APK artefact | Reproducible builds and APKs. |

A single app module with clear packages (`data`, `domain`, `ui.editor`, `ui.round`, `ui.settings`) is sufficient for a one-user app; the `domain` package must not depend on Android classes.

### 8.1 Terminology glossary (EN → NL)

| English (in code and default strings) | Dutch (UI) |
|---|---|
| Route | Wijk |
| Round | Ronde |
| Newspaper | Krant (huis-aan-huisblad) |
| Leaflets | Folders |
| Segment | Straatdeel |
| Sticker: none / JA / NEE/JA / NEE/NEE | Geen sticker / JA-sticker / NEE/JA-sticker / NEE/NEE-sticker |
| Does not exist | Bestaat niet |
| Apartment building | Flat / appartementencomplex |
| Apartment | Appartement |
| Building done | Flat klaar |
| House number / addition | Huisnummer / toevoeging |
| Even / odd side | Even / oneven kant |
| Ascending / descending | Oplopend / aflopend |

---

## 9. Implementation phases

Estimates assume one experienced Android developer, in developer days. Revisit after Phase 1.

| Phase | Content | Deliverables | Exit criteria | Estimate |
|---|---|---|---|---|
| **0. Preparation** | ✅ All open decisions resolved (section 12). Remaining: wireframes, walk the real route once to note stickers and walking order. | Wireframes, backlog. | Wireframes approved by the user. | 1–2 |
| **1. Foundation** | Project setup, CI, release signing config, theming, navigation skeleton, string resources EN/NL from day one, Room schema v1, domain functions (`generateRange`, `deliveryFor`) with tests. | Running skeleton, green CI, domain tests. | Sticker table from 3.3 fully covered by tests. | 3–4 |
| **2. Address management** | ADR-01…07, ADR-11/12, BLD-01…05, STK-01…03, SET-01. | Route editor, segment editor with preview, building grid, bulk sticker edit, "does not exist". | The real route, including all buildings, can be entered in under 45 minutes. | 7–10 |
| **3. Round mode** | RND-01…09, BLD-06/07. | Round start, delivery list, check-off, persistence. | A full round can be walked; state survives force-close. | 6–8 |
| **4. Backup & release pipeline** | DATA-01/02, SET-02, signed release APK, install instructions. | JSON export/import; installable release APK. | Export/uninstall/reinstall/import round trip passes; APK installs and updates over itself on the user's phone. | 2–3 |
| **5. Field test** | User runs the app on the real route for 2 weeks alongside the current method. Fixes. | Bug list, fixes. | Two consecutive weeks without a wrong delivery caused by the app. | 3–5 |
| **6. Should-haves** | ADR-08…10, BLD-08/09, STK-04, RND-10/11, DATA-03 – prioritised by field-test findings. | v1.0 feature set. | Accepted Should-haves meet their criteria. | 5–7 |
| **7. Release v1.0** | Complete Dutch translation review, final signed APK, developer verification registration (section 11.2). | v1.0 APK installed on the user's phone. | No crashes in the first week of use. | 1–2 |
| | | | **Total** | **28–41** |

**Milestones:** M1 = end of Phase 1, M2 = end of Phase 3 (MVP: a round can be walked), M3 = end of Phase 5 (validated in the field), M4 = v1.0.

---

## 10. Testing strategy

**Unit tests (domain):** every row of the sticker table; non-existing address always gives nothing; exceptions override stickers; round without leaflets gives no leaflets anywhere; ranges (2–24 even, 1–24 even starts at 2, from = to, single-sided streets); sorting with additions (12, 12A, 12B, 13); descending direction; unit generation (A–L gives 12, A–Z gives 26, 1–20 numeric); natural suffix sorting (1, 2, 10); building summaries excluding non-existing apartments; "Building done" checks only deliverable apartments and undo restores the previous state.

**Database tests:** in-memory Room; a migration test for every schema version from v1 onward.

**UI tests (Compose):** the two key flows in section 6; four delivery states render distinct icons; a 26-unit building grid fits one screen and bulk sticker edit works; app launches in Dutch with a Dutch locale and in English with an unsupported locale (e.g. German).

**Manual and field tests:** darkness and rain, one-handed, with gloves; force-close mid-round; 500+ addresses; oldest supported Android version; installing an update APK over the existing installation without losing data.

---

## 11. Distribution and maintenance

### 11.1 Direct APK distribution
The app is distributed as a signed release APK and installed manually on the user's phone (allow "Install unknown apps" for the file manager or browser used). Consequences:

| Topic | Approach |
|---|---|
| Signing key | One release keystore, created in Phase 1. **Back it up in two places** (e.g. password manager + offline copy). Updates only install over the existing app if signed with the same key; losing it means uninstalling and restoring from a backup file. |
| Versioning | Increment `versionCode` for every APK; `versionName` follows MAJOR.MINOR.PATCH. Version visible in settings (SET-02). |
| Updates | No automatic updates. The user installs a new APK over the old one; data is kept. Always export a backup before updating. |
| Crash reporting | No Play Console vitals and no third-party SDKs. Record crashes to a local log file that the user can export and send. |
| Target SDK | Not enforced by a store, but still raised yearly to the latest stable API level to keep system behaviour and security current. |

### 11.2 Android developer verification
Google is introducing mandatory developer verification for apps installed on certified Android devices, including sideloaded APKs. Enforcement starts on 30 September 2026 in Brazil, Indonesia, Singapore and Thailand, with a global rollout (including the Netherlands) planned for 2027. For this project the relevant option is a **limited distribution account**: free, no government ID required, for up to 20 devices, intended for hobbyists and small closed groups. Register it and the app's package name before the global rollout so updates keep installing normally. Unverified apps can still be installed via ADB or an "advanced" flow with a waiting period, but that is not a workable update path for a daily-use app. Check the current status in Phase 7, as details may still change.

### 11.3 Maintenance activities

| Activity | Frequency | Description |
|---|---|---|
| Dependency updates | Monthly | Via version catalog; CI must stay green. |
| Target SDK update | Yearly | Raise to latest stable API level, re-test. |
| Database migrations | Every schema change | Never destructive; migration test per version. |
| Translations | Every release | Lint check for missing Dutch strings; glossary kept in sync. |
| Keystore check | Yearly | Verify the backups of the signing key are still readable. |
| Release | As needed | Changelog in the repository; backup before installing. |

---

## 12. Decisions log

| # | Question | Decision |
|---|---|---|
| 1 | Number of users | One user, one route, one device. |
| 2 | Newspaper titles | One title. |
| 3 | Leaflet rules | Determined by the mailbox sticker per the table in section 3.3: no sticker → newspaper + leaflets; circular JA → newspaper + leaflets; NEE/JA → newspaper only; NEE/NEE → nothing. |
| 4 | Non-existing addresses | Can be deleted or marked "does not exist" (ADR-04). |
| 5 | Sharing routes | Out of scope. Backup export is kept for data safety only. |
| 6 | Language | Build in English (default resources), full Dutch translation; app follows device language, per-app language on Android 13+. |
| 7 | Distribution | Direct APK; limited-distribution developer verification account before the 2027 global rollout. |
| 8 | Nature of the newspaper | Confirmed: a free door-to-door paper (*huis-aan-huisblad*), delivered to every existing address unless the sticker blocks it. There are no subscriptions. |
| 9 | Apartment buildings | One house number with suffixes (A–Z) is a building: one row in the list, zoom-in grid per apartment, sticker per apartment (section 3.7, BLD-01…10). |

---

## 13. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Loss of the signing keystore | High – no updates possible over the installed app | Two independent backups; yearly check. |
| Data loss (phone lost, reset, app removed) | High – route must be re-entered | Backup export (Must), backup reminder, backup before every update. |
| Developer verification blocks installation after the 2027 rollout | Medium | Register a limited distribution account in time (11.2). |
| Entering the route takes too long | Medium | Range entry with preview, bulk sticker edit; measured in Phase 2 exit criteria. |
| Unreadable outdoors / in the dark | Medium | High contrast, large targets, dark theme, field test. |
| Stickers change without the app being updated | Medium | Stickers can be corrected quickly in the street and building screens (tap an address, choose a sticker); the deliverer checks for changed stickers while walking. |
| Incomplete or inconsistent Dutch translation | Low | Glossary, lint check for missing translations, review in Phase 7. |
| Scope creep | Medium | MoSCoW discipline; extensions only after v1.0. |

**Possible future extensions:** map view (OpenStreetMap), optional address lookup via BAG/PDOK, round history and statistics, CSV import.

---

## 14. Using this plan with an AI coding assistant

Hand over one requirement ID (e.g. `ADR-02`) at a time as a work package. Include the relevant context from this plan (domain rules in section 3, data model in section 5, tech stack and glossary in section 8), state the requirement and its acceptance criteria, and ask for the implementation plus unit tests and both English and Dutch strings. Working in phase order keeps every step small and reviewable.
