# Issue Tracker

Track problems and their resolutions. Build institutional knowledge.

---

## Issue Template

### ISS-XXX: [Title]
**Reported:** YYYY-MM-DD
**Status:** Open | Investigating | Blocked | Resolved
**Severity:** Critical | High | Medium | Low
**Related Feature:** [FEATURE-ID]

**Symptoms:**
- [What was observed]
- [Error messages if any]

**Investigation Log:**
- [HH:MM] [What was checked/tried]
- [HH:MM] [Results]

**Root Cause:**
[Once identified — what actually caused the issue]

**Resolution:**
[How it was fixed]

**Prevention:**
- [ ] Added test case
- [ ] Added validation
- [ ] Updated documentation

**Time to Resolution:** [X hours/days]
**Related Commits:** [commit hashes]

---

## Open Issues

### ISS-001: From/To placeholder shows even example numbers for every side
**Reported:** 2026-09-30
**Status:** Open
**Severity:** Low
**Related Feature:** ADR-A

**Symptoms:**
- On "Add street", the empty From/To fields show the example "2" and "24", also when Odd or Both is selected (reported by the user on the phone, v0.4.0).

**Investigation Log:**
- Cause is known: `NumberFields` in `app/src/main/java/nl/ericmulder/krantenwijk/ui/route/AddSectionScreen.kt` passes fixed hints "2" and "24".

**Root Cause:**
Hard-coded placeholder text, independent of the selected side.

**Resolution:**
To do (user: pick up at a later stage). Suggested: Even → 2 / 24, Odd → 1 / 23, Both → 1 / 10, plus a ViewModel or UI test.

**Prevention:**
- [ ] Added test case

---


---

## Resolved Issues

*Resolved issues are moved here with full investigation and resolution notes.*

---

### ISS-002: Converting 8B into a building crashes; 8A became building "8"
**Reported:** 2026-10-07
**Status:** Resolved (awaiting device test)
**Severity:** High
**Related Feature:** BLD-01

**Symptoms:**
- Field test on v1.3.0: two buildings 8A and 8B. "Make building" on 8A created a building listed under 8, and 8A stayed as a separate number. Doing the same on 8B crashed the app.

**Root Cause:**
A building was identified by house number only. `SegmentDetailViewModel.createBuilding` passed `address.houseNumber` and dropped the addition, so 8A became building 8 (replacing a plain 8, if any). For 8B the insert hit the unique index (segmentId, houseNumber) on `building`; only `BuildingConflictException` was caught, so the SQLite constraint exception crashed the app.

**Resolution:**
DEC-031: building gets its own addition (schema v3, backup format 3), lettered buildings get numbered mailboxes 8A-1…, a duplicate building is reported (BuildingExistsException), and any other failure shows "Couldn't create the building" instead of crashing.

**Prevention:**
- [x] Added test case (repository, ViewModel, migration 2→3, backup round trip)
- [x] Added validation (existing-building check in the same transaction)
- [x] Updated documentation (plan §3.7, ARCHITECTURE)

**Time to Resolution:** same day
**Related Commits:** (pending)

---

## Common Patterns

As issues accumulate, document patterns here:

### Environment Issues
*TBD*

### Integration Issues
*TBD*

### Performance Issues
*TBD*
