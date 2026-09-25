# Session Management Skill - TDD Fixtures

This directory contains RED phase test fixtures for the session-management skill.

## Purpose

These fixtures define the **expected behavior** of the session-management skill across various scenarios. They serve as:

1. **Specification** - What the skill should do in each situation
2. **Test cases** - How to verify the skill works correctly
3. **Documentation** - Examples of proper session management

## Fixture Structure

Each scenario has two files:

- **input.md** - Describes the context, state, and trigger
- **expected.md** - Defines what the skill should do and how to verify it

## Scenarios

### 1. First Session Ever
**Path:** `scenarios/first-session-ever/`

Tests greeting behavior when:
- No session.log exists
- No task history
- Fresh project with initial commit
- Prettier not installed

**Key behaviors:**
- Welcome message for new project
- Feature tip rotation
- Prettier installation offer
- Session settings menu

### 2. Continuing Previous Work
**Path:** `scenarios/continuing-previous-work/`

Tests greeting behavior when:
- session.log has recent entry
- Uncommitted changes exist
- Parallel task in progress
- Previous highlights shown

**Key behaviors:**
- Contextualized greeting from logs
- Summary of last session
- Highlight rotation (avoid duplicates)
- What's Next from tasks

### 3. After Annealing Changes
**Path:** `scenarios/after-annealing-changes/`

Tests greeting behavior when:
- Annealing applied changes within 7 days
- Improvements documented in history
- Clean working tree

**Key behaviors:**
- Prioritize annealing highlights
- Explain what improved and why
- Show evidence from feedback
- Celebrate self-improvement

### 4. Prettier Not Installed
**Path:** `scenarios/prettier-not-installed/`

Tests greeting behavior when:
- package.json exists
- Prettier not installed
- PostToolUse hook expects it

**Key behaviors:**
- Offer prettier installation
- Explain auto-format benefit
- Make acceptance easy
- Track offer to avoid repeating

### 5. End Session (Normal)
**Path:** `scenarios/end-session-normal/`

Tests end session protocol when:
- User requests "end session"
- Work was completed
- Changes need committing

**Key behaviors:**
- Commit work with clear message
- Create session document
- Update session log
- Capture feedback
- Confirm to user

### 6. Context Recovery After Crash
**Path:** `scenarios/context-recovery-after-crash/`

Tests recovery behavior when:
- New chat session (no history)
- But session.log shows interrupted session
- Uncommitted work exists
- Parallel task in progress

**Key behaviors:**
- Detect context loss
- Reconstruct state from logs
- Summarize what was happening
- Ask user to confirm direction

## How to Use These Fixtures

### For Skill Development (GREEN Phase)

1. Read the `input.md` to understand the scenario
2. Implement the skill to handle the scenario
3. Verify behavior matches `expected.md`
4. Check all success criteria
5. Avoid all anti-patterns

### For Testing

1. Set up the environment described in `input.md`
2. Trigger the skill
3. Compare output against `expected.md`
4. Verify all checklist items
5. Ensure no anti-patterns occurred

### For Skill Refinement (REFACTOR Phase)

1. Run skill against all scenarios
2. Document any deviations in `.ai/feedback/skills/session-management.md`
3. Identify patterns in failures
4. Refine skill prompt
5. Re-test until all scenarios pass

## Success Criteria

The session-management skill should:

- ✅ Pass all 6 scenarios
- ✅ Handle edge cases gracefully
- ✅ Avoid all documented anti-patterns
- ✅ Deliver consistent, helpful greetings
- ✅ Execute complete end-session protocol
- ✅ Recover context after interruptions

## Adding New Scenarios

When you identify a new edge case or behavior:

1. Create a new scenario directory: `scenarios/[scenario-name]/`
2. Write `input.md` describing the context and trigger
3. Write `expected.md` defining correct behavior
4. Test the skill against it
5. Update this README with the new scenario

## RED-GREEN-REFACTOR Cycle

**RED (Current Phase):**
- ✅ Fixtures written (this directory)
- ⏸️ Skill exists but may not pass all scenarios

**GREEN (Next Phase):**
- Run skill against each scenario
- Fix skill until all scenarios pass
- Document any changes needed

**REFACTOR (Final Phase):**
- Optimize skill for clarity
- Add examples from real usage
- Fine-tune edge case handling
- Test against real codebase

## Notes

- These fixtures are **prescriptive** - they define how the skill **should** behave
- Variance is allowed where documented (see "Variance Allowed" sections)
- Anti-patterns are explicitly called out (see "Anti-Patterns to Avoid")
- Edge cases have specific guidance (see "Edge Cases" sections)

## Self-Annealing

As the skill runs in real sessions, capture deviations in:
- `.ai/feedback/skills/session-management.md`

During weekly annealing:
- Review feedback against these fixtures
- Identify patterns (skill issue vs fixture issue)
- Propose refinements to skill OR fixtures
- Never auto-apply - always get approval

---

**Next Step:** GREEN phase - implement/refine the skill to pass all scenarios.
