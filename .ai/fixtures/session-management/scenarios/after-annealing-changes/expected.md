# Expected Behavior: After Annealing Changes

## Expected Actions

### 1. Silent Context Gathering
- [ ] Execute `git log --oneline -5` to get recent commits
- [ ] Execute `git branch --show-current` to get current branch
- [ ] Execute `git status` to check for uncommitted work
- [ ] Read `.ai/sessions/session.log` for last session summary
- [ ] List and check `.ai/annealing/history/` for recent changes (within 7 days)
- [ ] Read the most recent annealing history file (2025-11-29-applied.md)
- [ ] List `.ai/tasks/backlog/` files
- [ ] Read `.ai/sessions/highlights-shown.log`
- [ ] Execute `command -v prettier`

### 2. Determine Highlights
- [ ] Recognize annealing changes applied on 2025-11-29 (4 days ago, within 7-day window)
- [ ] Parse annealing history for improvements:
  - TypeScript guardian now catches implicit `any` in function parameters
  - Security guardian reduced false positives on "key" variable names
- [ ] This takes priority over feature tip rotation
- [ ] No prettier offer needed (already installed)
- [ ] No uncommitted changes
- [ ] Clean working tree on develop branch

### 3. Compose and Deliver Greeting

**Expected Greeting Structure:**

```
Good afternoon! (or time-appropriate greeting)

**Project Status**
- Branch: develop | Clean
- Recent: "Refactor API handlers", "Add rate limiting middleware"

**Last Session**
Wrapped up the API refactor. All guardians passed.

**What's Next**
- Integration tests for rate limiting (backlog)
- Update API documentation (backlog)

**Recent Improvements**
Last week's annealing tightened the TypeScript guardian—it now catches
implicit `any` in function parameters. This came from 3 instances that
slipped through during previous code reviews.

The security guardian also got smarter: fewer false positives on variables
with "key" in the name (it now checks context, not just the substring).

These changes make reviews more accurate while reducing noise.

---

**Session Settings** (defaults in brackets)

1. Verbosity: [full] / minimal / quiet
2. Guardians: [all] / select / off
3. Apprentice: [on] / detailed / off
4. Annealing: [active] / paused
5. External Review: [enabled] / disabled
6. Session Docs: [full] / light / off
7. Sync Check: [skip] / check

Change with "1 quiet, 3 off" or just tell me what we're working on.

*Tip: When you're done, say "end session" so I can save context for next time.*
```

### 4. Track Shown Highlights
- [ ] Append to `.ai/sessions/highlights-shown.log`: `2025-12-03: annealing-improvements - shown`

### 5. Prepare for Session
- [ ] If user responds with settings changes, store in `.ai/sessions/current-preferences.json`
- [ ] If user selects a task, begin work

## Success Criteria

- [ ] Greeting acknowledges annealing changes (priority over feature tips)
- [ ] Improvements section explains WHAT changed (implicit any detection, key false positives)
- [ ] Improvements section explains WHY (3 instances slipped through, 5 false positives)
- [ ] Improvements section explains IMPACT (more accurate, less noise)
- [ ] Tone is positive about self-improvement (celebratory but not boastful)
- [ ] Project status shows correct branch (develop) and clean state
- [ ] Last session summary matches session.log
- [ ] Backlog items are listed in What's Next
- [ ] Session settings menu is presented
- [ ] Greeting suggests tackling integration tests or docs
- [ ] Annealing highlight is tracked to highlights-shown.log
- [ ] Information is accurate (from annealing history file)
- [ ] No speculation about improvements (only what's documented)

## Anti-Patterns to Avoid

- [ ] Don't use generic feature tip when annealing changes exist
- [ ] Don't mention annealing changes if they're older than 7 days
- [ ] Don't fabricate or guess what improved (read from history file)
- [ ] Don't skip mentioning WHY changes were made (evidence from feedback)
- [ ] Don't make it too verbose (keep improvements section concise)
- [ ] Don't highlight trivial changes (only meaningful improvements)
- [ ] Don't fail to show that improvements came from accumulated observations
- [ ] Don't make it sound like the system is "learning" autonomously (human approved)

## Variance Allowed

- Which improvements to highlight if multiple exist (can focus on most impactful)
- Exact phrasing of improvements explanation
- Whether to mention both changes or focus on one (if space is limited)
- Time-based greeting variation

## Edge Cases

- If annealing history exists but is older than 7 days, don't highlight it
- If multiple annealing files exist within 7 days, use the most recent
- If annealing history file is malformed, gracefully fall back to feature tip
