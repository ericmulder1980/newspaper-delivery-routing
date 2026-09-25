# Expected Behavior: Continuing Previous Work

## Expected Actions

### 1. Silent Context Gathering
- [ ] Execute `git log --oneline -5` to get recent commits
- [ ] Execute `git branch --show-current` to get current branch
- [ ] Execute `git status` to check for uncommitted work
- [ ] Read `.ai/sessions/session.log` for last session summary
- [ ] List and read `.ai/tasks/backlog/` files
- [ ] List and read `.ai/tasks/parallel/` files
- [ ] List `.ai/annealing/history/` for recent changes
- [ ] Read `.ai/sessions/highlights-shown.log` to avoid repetition
- [ ] Execute `command -v prettier` to check if installed

### 2. Determine Highlights
- [ ] Recognize this is a continuation (session.log exists)
- [ ] Parse last session: "Fix redirect issue after successful authentication"
- [ ] Identify 2 uncommitted files
- [ ] Identify feature/auth-flow branch
- [ ] Note parallel task in progress
- [ ] Check highlights-shown.log shows: guardian-agents, self-annealing, external-review
- [ ] Select a new feature tip not yet shown (e.g., "apprentice-mode", "research-first", or "session-continuity")
- [ ] No prettier offer (already installed)

### 3. Compose and Deliver Greeting

**Expected Greeting Structure:**

```
Good morning! (or time-appropriate greeting)

**Project Status**
- Branch: feature/auth-flow | 2 uncommitted files
- Recent: "Add login component", "Set up auth context"

**Last Session**
Got login form working and connected auth context.
Left off debugging the redirect after successful login.

**What's Next**
- Fix post-login redirect (in progress)
- Add logout functionality (backlog)

**[Feature Tip - e.g., Apprentice Mode]**
I explain the 'why' not just the 'what'—using analogies for new concepts,
defining jargon on first use, and sharing relevant industry context. For
extra-detailed teaching, invoke the apprentice-mode skill.

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
- [ ] Append to `.ai/sessions/highlights-shown.log`: `2025-12-03: [feature-tip-name] - shown`

### 5. Prepare for Session
- [ ] If user says to continue, read the parallel task file for full context
- [ ] If user responds with settings changes, store in `.ai/sessions/current-preferences.json`
- [ ] If user changes direction, be ready to update task files

## Success Criteria

- [ ] Greeting references yesterday's work (not "fresh start")
- [ ] Project status shows correct branch (feature/auth-flow)
- [ ] Uncommitted file count is accurate (2 files)
- [ ] Recent commits are listed (top 2 shown)
- [ ] Last session summary matches session.log entry
- [ ] What's Next includes both parallel (in progress) and backlog items
- [ ] Feature tip is NEW (not guardian-agents, self-annealing, or external-review)
- [ ] No prettier offer (since already installed)
- [ ] Session settings menu is presented
- [ ] Greeting suggests continuing with redirect fix
- [ ] Tone acknowledges continuity ("left off", "pick up where we were")
- [ ] Highlight is tracked to highlights-shown.log
- [ ] No state modifications during context gathering

## Anti-Patterns to Avoid

- [ ] Don't start working on the redirect without user confirmation
- [ ] Don't repeat a feature tip already shown (check highlights-shown.log)
- [ ] Don't ignore uncommitted changes
- [ ] Don't fail to mention the parallel task
- [ ] Don't offer prettier installation (already installed)
- [ ] Don't create new session.log entry (that's for end session)
- [ ] Don't assume user wants to continue with redirect (ask/suggest)
- [ ] Don't skip reading session.log (it has critical context)

## Variance Allowed

- Time-based greeting can vary ("Good morning", "Good afternoon", "Hey", etc.)
- Which feature tip is selected (as long as it's not one already shown)
- Order of recent commits (can show top 1-2)
- Exact phrasing of "What's Next" items
