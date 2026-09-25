---
description: Run through the feature completion checklist to prepare for merge
---

# Complete Feature

Run the feature completion workflow to prepare for merge to develop.

## Usage

```
/complete-feature          # Start completion checklist
```

## Process

Invokes `feature-completion` skill and walks through:

1. **Self-Validation**
   - Tests passing?
   - TypeScript clean?
   - Code formatted?

2. **Guardian Review**
   - Spawn applicable guardians
   - Review findings
   - Fix issues

3. **External Review**
   - Create Codex task
   - Wait for review (or proceed if not configured)

4. **Gate Check**
   - Zero critical/high issues
   - All tests passing
   - Documentation updated

5. **Merge**
   - Merge to develop
   - Cleanup and logging

## Interactive Mode

Will prompt for:
- Confirmation at each phase
- How to handle findings
- Override approval if needed

## Output

On success:
```
Feature [name] ready for merge.
- All checks passed
- No blocking issues
- Session logged to .ai/sessions/

Proceeding with merge to develop...
```

On failure:
```
Feature [name] blocked.
- [X] critical issues remaining
- [Y] high issues remaining

Fix these issues and run /complete-feature again.
```
