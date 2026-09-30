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

## Common Patterns

As issues accumulate, document patterns here:

### Environment Issues
*TBD*

### Integration Issues
*TBD*

### Performance Issues
*TBD*
