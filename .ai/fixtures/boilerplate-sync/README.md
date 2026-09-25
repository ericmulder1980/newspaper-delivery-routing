# Boilerplate Sync Skill - TDD Fixtures

**Status:** RED phase (fixtures defined, skill not yet implemented)

This directory contains test fixtures for the `boilerplate-sync` skill following TDD methodology.

## RED Phase: Test Fixtures

Each scenario directory contains:
- `input.md`: Context, git state, remotes, and trigger conditions
- `expected.md`: Expected behavior, output, and file changes

## Scenarios

### 1. upstream-updates-no-local-changes
**Purpose:** Clean pull when upstream has new commits and local has no changes.

**Key Validations:**
- Detects upstream changes
- Auto-merges non-customized files
- Creates backups
- Updates metadata

**Success Criteria:** Clean automatic merge with no conflicts.

---

### 2. upstream-updates-with-local-no-conflict
**Purpose:** Merge when both upstream and local have changes to different files.

**Key Validations:**
- Detects changes in both locations
- Recognizes no file overlap
- Merges cleanly
- Preserves all changes from both sources

**Success Criteria:** Both upstream and local changes coexist without conflict.

---

### 3. upstream-updates-with-conflict
**Purpose:** Handle conflicts when same file modified in both locations.

**Key Validations:**
- CRITICAL: Does NOT auto-resolve conflicts
- Shows side-by-side diff
- Presents resolution options
- Respects user choice
- Supports manual merge and customization flag

**Success Criteria:** No data loss, user maintains full control, all resolution paths work.

---

### 4. no-upstream-updates
**Purpose:** Fast path when already up to date.

**Key Validations:**
- Detects no changes available
- Reports status clearly
- Makes no modifications
- Fast execution

**Success Criteria:** Idempotent, informative, no unnecessary operations.

---

### 5. local-improvements-to-share
**Purpose:** Contribute local improvements back to upstream via PR.

**Key Validations:**
- Scans upstream candidates
- Validates generality (not project-specific)
- Creates PRs with evidence
- Tracks submission status
- Moves to submitted/

**Success Criteria:** Smooth contribution workflow, tracking works, PRs well-formatted.

---

### 6. first-time-setup
**Purpose:** Initial sync setup for new or existing projects.

**Key Validations:**
- Detects first-time condition
- Validates upstream repo
- Adds remote if missing
- Presents safe options for baseline
- Explains consequences clearly

**Success Criteria:** Both "fresh start" and "treat as customized" paths work safely.

---

## GREEN Phase (Next)

Once fixtures are defined:
1. Implement `boilerplate-sync` skill
2. Create test harness to run skill against each scenario
3. Verify actual output matches expected output
4. Iterate until all scenarios pass

## REFACTOR Phase (Future)

After passing all fixtures:
1. Test against real git repositories
2. Add edge cases discovered
3. Tune for false positives/negatives
4. Document limitations

## Running Tests (Future)

```bash
# Test all scenarios
.ai/fixtures/boilerplate-sync/test-runner.sh

# Test specific scenario
.ai/fixtures/boilerplate-sync/test-runner.sh upstream-updates-with-conflict
```

## Fixture Format

### input.md Structure
```markdown
# Scenario: [Name]

## Context
[Git state, remotes, local changes, upstream state]

## Input
[What triggers the skill]

## Expected Behavior
[What the skill should do]

## Success Criteria
- [ ] Criterion 1
- [ ] Criterion 2
```

### expected.md Structure
```markdown
# Expected Output: [Name]

## Terminal Output
[Exact expected console output]

## File Changes
[Expected file modifications]

## Behavior Validation
- ✓ Validation 1
- ✓ Validation 2
```

---

**Created:** 2025-12-03
**Skill:** `.claude/skills/boilerplate-sync/SKILL.md`
**Status:** Awaiting GREEN phase implementation
