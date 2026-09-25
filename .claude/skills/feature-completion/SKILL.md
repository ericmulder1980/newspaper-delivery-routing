---
name: feature-completion
description: Complete workflow for finishing a feature branch - tests, review, fixes, merge gates
---

# Feature Completion Workflow

End-to-end process for completing a feature and merging to develop.

## Overview

Structured checklist ensuring tests, reviews, and gates are satisfied before merge. Use this when a feature is functionally complete and ready for integration.

## Prerequisites

- Working on a `feature/*` branch
- Feature is functionally complete
- Ready to integrate to develop

## Phase 1: Self-Validation

Before requesting review, verify locally:

```
[ ] All new code has corresponding tests
[ ] Tests passing: pnpm test (or equivalent)
[ ] No TypeScript errors: pnpm typecheck
[ ] Code formatted: pnpm format
[ ] No linting errors: pnpm lint
```

If any fail, fix before proceeding.

## Phase 2: Guardian Review

Invoke guardian-agents skill:

```
[ ] Determine applicable guardians from changed files
[ ] Spawn guardians in parallel
[ ] Collect all findings
[ ] Review each finding
```

### Handling Findings

For each finding:
1. Is it valid? (not a false positive)
2. Is it critical/high? (must fix)
3. Is it isolated? (parallel agent) or coupled? (fix directly)

Log any false positives to `.ai/feedback/guardians/[name].md`

## Phase 3: External Review

```
[ ] Create Codex review task in .ai/tasks/review/
    - YYMMDD-codex-[feature-name].md
[ ] Push branch to remote
[ ] Notify that review is ready
[ ] Wait for Codex findings
[ ] (Optional) Create Gemini review task
```

### Task File Template

```markdown
# Review Task: [Feature Name]

**Type:** review
**Assigned:** codex
**Branch:** feature/[name]
**Created:** YYYY-MM-DD

## Context
[What this feature does, any areas of concern]

## Files to Review
- [list of key files]

## Focus Areas
- [specific concerns]

## Deliverables
Save findings to: .ai/tasks/review/YYMMDD-codex-[feature]-findings.md
```

## Phase 4: Synthesis & Fix

```
[ ] Aggregate all findings (guardians + Codex + Gemini)
[ ] Prioritize by severity
[ ] Fix all CRITICAL issues
[ ] Fix all HIGH issues
[ ] Review MEDIUM issues (fix or defer with justification)
[ ] LOW issues: fix if quick, otherwise log to backlog
```

### After Fixes

```
[ ] Re-run affected guardians on fixed code
[ ] Verify zero CRITICAL/HIGH remaining
[ ] Update any affected tests
```

## Phase 5: Merge Gate

Final checklist:

```
[ ] Zero CRITICAL issues
[ ] Zero HIGH issues
[ ] All tests passing
[ ] Branch up to date with develop
[ ] Documentation updated (if applicable)
```

If all pass: proceed to merge.

If blocked and need override:
```
[ ] Document reason in PR/commit
[ ] Log to .ai/feedback/process/overrides.md
[ ] Get explicit human approval
```

## Phase 6: Merge & Cleanup

```
[ ] Merge to develop branch
[ ] Delete feature branch (if policy)
[ ] Move task to .ai/tasks/completed/
[ ] Write session summary to .ai/sessions/
[ ] Note any annealing observations
```

### Session Log Template

```markdown
# Session: [Feature Name]
**Date:** YYYY-MM-DD
**Branch:** feature/[name]

## Summary
[What was accomplished]

## Files Changed
[List of significant files]

## Review Findings
[Summary of what was caught and fixed]

## Observations
[Anything notable for future reference or annealing]
```

## Lifecycle Phase Verification

Before completing a feature, verify lifecycle phases in features.json:

### Phase Checklist

```bash
# Read feature status
cat .claude/memory/features.json | grep -A 50 "[FEATURE-ID]"
```

Verify:
- [ ] `lifecycle.inception.status` = "complete"
- [ ] `lifecycle.planning.status` = "complete"
- [ ] `lifecycle.implementation.status` = "complete" (all tasks done)
- [ ] `lifecycle.testing.status` = "complete"
- [ ] `lifecycle.review.status` = "complete"

### Update Feature Status

On completion:
```javascript
// In features.json, update:
{
  "status": "complete",
  "phase": "merged",
  "lifecycle": {
    "merged": {
      "status": "complete",
      "merge_date": "[date]",
      "pr_url": "[if applicable]"
    }
  }
}
```

### Update Progress Log

```
================================================================================
[TIMESTAMP] FEATURE COMPLETE
Feature: [ID] - [Title]
Total tasks: [X]
Total sessions: [Y]
Decisions recorded: [Z]
================================================================================
```

---

## Self-Annealing

After each feature completion:
- Did any gate overrides happen? Why?
- What issues were caught late that could be caught earlier?
- Any friction in the process?

Log to `.ai/feedback/process/feature-completion.md`
