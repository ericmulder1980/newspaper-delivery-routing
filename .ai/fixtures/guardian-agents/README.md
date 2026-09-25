# Guardian-Agents Skill: TDD Fixtures

Test-Driven Development fixtures for the `guardian-agents` skill.

## Purpose

These fixtures define expected behavior BEFORE implementing the skill (RED phase). They serve as:
- **Specification**: What the skill must do
- **Test Cases**: Scenarios to validate against
- **Documentation**: Examples of correct behavior
- **Regression Prevention**: Guard against future breakage

## Fixture Structure

Each scenario directory contains:
- `input.md`: Context, changed files, settings, and trigger conditions
- `expected.md`: Expected behavior, success criteria, and edge cases

## Test Scenarios

### 1. TypeScript File Changed
**Location:** `scenarios/typescript-file-changed/`

Tests basic file detection and targeted guardian spawning.

**Key Validations:**
- Only relevant guardians spawn (typescript, accessibility)
- File detection pattern matching works correctly
- Guardians receive appropriate file context
- Irrelevant guardians are NOT spawned

### 2. Security Files Changed
**Location:** `scenarios/security-files-changed/`

Tests security-critical file detection and external review coordination.

**Key Validations:**
- Security-sensitive patterns detected (auth/, api/)
- Multiple overlapping patterns deduplicate correctly
- External review (Codex) coordination triggered
- Security guardians receive all relevant files

### 3. Multiple File Types
**Location:** `scenarios/multiple-file-types/`

Tests diverse file type handling and parallel guardian execution.

**Key Validations:**
- Multiple guardians spawn in parallel (not sequential)
- Each guardian receives only relevant files
- Pattern overlaps handled correctly (e.g., test files)
- Findings aggregated from diverse guardian types

### 4. No Relevant Files
**Location:** `scenarios/no-relevant-files/`

Tests graceful handling of configuration-only changes.

**Key Validations:**
- Zero guardians spawn for config-only changes
- Clear communication about why review skipped
- No unnecessary blocking
- Decision logged appropriately

### 5. Guardian Finds Issues
**Location:** `scenarios/guardian-finds-issues/`

Tests issue detection, gate checking, and fix triage.

**Key Validations:**
- Findings properly aggregated and categorized
- Merge blocked for CRITICAL/HIGH issues
- Override process requires explicit confirmation
- Fix triage suggestions provided (isolated vs coupled)
- Override logged to feedback system

### 6. All Guardians Pass
**Location:** `scenarios/all-guardians-pass/`

Tests clean review flow and success handling.

**Key Validations:**
- External review coordination works
- Zero issues allow merge
- Success properly logged for self-annealing
- Optional enhancements don't block merge
- Positive user experience maintained

## Usage

### RED Phase (Current)
1. Read `input.md` for scenario context
2. Read `expected.md` for success criteria
3. Use as specification when implementing skill

### GREEN Phase (Next)
1. Implement the guardian-agents skill
2. Test each scenario manually or via test harness
3. Verify all success criteria met
4. Iterate until 100% of scenarios pass

### REFACTOR Phase (Future)
1. Test against real codebase samples
2. Add new scenarios for edge cases discovered
3. Update expected behavior based on learnings
4. Tune skill for false positive reduction

## Testing the Skill

To validate the skill against these fixtures:

1. **Manual Testing:**
   ```bash
   # Simulate changed files from a scenario
   # Invoke guardian-agents skill
   # Compare actual behavior to expected.md
   ```

2. **Automated Testing (Future):**
   - Create test harness that reads input.md
   - Invokes skill with simulated context
   - Validates output against expected.md
   - Reports pass/fail for each criterion

## Adding New Scenarios

When adding new test scenarios:

1. Create new directory: `scenarios/[scenario-name]/`
2. Write `input.md`:
   - Context section (git status, settings)
   - Input section (what triggers the skill)
   - Expected trigger section (what should happen)
3. Write `expected.md`:
   - Expected behavior by phase
   - Success criteria checklist
   - Edge cases to handle
4. Update this README with scenario description

## Success Metrics

The guardian-agents skill passes TDD validation when:

- [ ] All 6 base scenarios pass
- [ ] All success criteria met for each scenario
- [ ] All edge cases handled gracefully
- [ ] No false positives in guardian spawning
- [ ] No false negatives in issue detection
- [ ] User experience is smooth and non-blocking for clean code

## Next Steps

1. **GREEN Phase**: Implement the skill to pass these tests
2. **Real-World Testing**: Test against actual codebase
3. **Iteration**: Add scenarios for discovered edge cases
4. **Self-Annealing**: Log accuracy and improve over time

---

*Created: 2025-12-03*
*Phase: RED (Test-First)*
*Status: Specification Complete, Implementation Pending*
