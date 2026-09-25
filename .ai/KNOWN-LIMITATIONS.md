# Known Limitations

Discovered during TDD REFACTOR phase. These are documented for transparency and future improvement.

## Guardian Agents

### 1. Docs Guardian False Positives
**Issue:** Docs guardian may report files as missing without checking file system.
**Example:** Claimed `/customize` command didn't exist when it did.
**Mitigation:** Always verify file existence with Glob before claiming missing.
**Logged:** `.ai/feedback/guardians/docs.md`

### 2. Pattern Matching Order
**Issue:** More specific patterns must be listed before general patterns in `file_detection`.
**Example:** `.claude/skills/**/*.md` must come before `*.md`.
**Status:** Fixed in settings.json, documented in skill.

## Boilerplate Sync

### 1. Source Repo Detection
**Issue:** Skill didn't detect when running in the source repo itself.
**Impact:** Could confuse users trying to sync repo with itself.
**Status:** Fixed - now detects and provides appropriate guidance.

### 2. No Real Sync Testing
**Issue:** Can't fully test sync in the source repo.
**Mitigation:** Need separate test project to validate sync pull/push.

## Session Management

### 1. Git Version Compatibility
**Issue:** `git branch --show-current` requires Git 2.22+.
**Impact:** Fails on older git versions (e.g., 2.4.1).
**Status:** Fixed - now uses `git rev-parse --abbrev-ref HEAD`.

### 2. Start Protocol Untestable Mid-Session
**Issue:** Session start greeting can only be tested at actual session start.
**Mitigation:** Fixtures define expected behavior; manual verification needed.

### 3. Settings Menu Complexity
**Issue:** 7-option settings menu may be overkill for most users.
**Status:** Documented. `/customize` can simplify defaults.

## External Review (Codex/Gemini)

### 1. No Automatic Integration
**Issue:** Codex review requires manual setup and task file monitoring.
**Impact:** External review is async and requires human coordination.
**Future:** Could add webhook or polling for Codex completion.

### 2. Timeout Handling
**Issue:** No defined behavior if Codex review never completes.
**Mitigation:** Don't block indefinitely; allow proceed after timeout.

## General

### 1. Self-Annealing Not Yet Validated
**Issue:** Trust escalation benchmarks defined but not tested over time.
**Status:** Need 8+ weeks of usage to validate thresholds.

### 2. Error Handling Sparse
**Issue:** Skills don't comprehensively document failure scenarios.
**Future:** Add "Error Cases" section to each skill.

---

## Edge Cases Discovered & Fixed

| Skill | Edge Case | Status |
|-------|-----------|--------|
| guardian-agents | Skill files need architecture review | Fixed |
| boilerplate-sync | Source repo detection | Fixed |
| session-management | Old git version compatibility | Fixed |

---

*Last updated: 2025-12-03*
*Update this file during weekly annealing reviews.*
