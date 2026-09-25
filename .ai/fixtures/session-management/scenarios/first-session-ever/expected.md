# Expected Behavior: First Session Ever

## Expected Actions

### 1. Silent Context Gathering
- [ ] Execute `git log --oneline -5` to get recent commits
- [ ] Execute `git branch --show-current` to get current branch
- [ ] Execute `git status` to check for uncommitted work
- [ ] Try to read `.ai/sessions/session.log` (will not exist)
- [ ] List `.ai/tasks/backlog/` (will be empty)
- [ ] List `.ai/tasks/parallel/` (will be empty)
- [ ] List `.ai/annealing/history/` (will be empty)
- [ ] Execute `command -v prettier` to check if installed
- [ ] Check if `package.json` exists

### 2. Determine Highlights
- [ ] Recognize no previous sessions exist
- [ ] Recognize no annealing history
- [ ] Recognize prettier is not installed but package.json exists
- [ ] Select a feature tip from rotation (e.g., Guardian agents intro)
- [ ] Plan to offer prettier installation

### 3. Compose and Deliver Greeting

**Expected Greeting Structure:**

```
Hey! Starting fresh on my-new-project.

**Project Status**
- Branch: main | Clean working tree
- Recent: Initial commit: Claude Code boilerplate structure

**What's Next**
Backlog is empty—what's the first feature we're building?

**[Feature Tip - e.g., Guardian Agents]**
9 specialized reviewers run in parallel on commit—security, TypeScript,
architecture, test, performance, docs, accessibility, API, and database
guardians. They catch issues before they land.

**Quick Setup Offer**
I noticed Prettier isn't installed. The boilerplate has an auto-format
hook that runs Prettier on every file I edit — keeps code clean without
thinking about it.

Want me to set it up? Just say "install prettier" and I'll run:
`pnpm add -D prettier` + create a basic .prettierrc

(Or ignore this and we'll continue without it — totally fine.)

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
- [ ] Create `.ai/sessions/highlights-shown.log` if it doesn't exist
- [ ] Append entry: `YYYY-MM-DD: [feature-tip-name] - shown`

### 5. Prepare for Settings Response
- [ ] If user responds with settings changes, parse and store in `.ai/sessions/current-preferences.json`
- [ ] If user ignores and states a task, use all defaults and begin work
- [ ] If user says "install prettier", execute prettier installation workflow

## Success Criteria

- [ ] Greeting is delivered as the first message in the session
- [ ] Greeting acknowledges this is a fresh start
- [ ] Project status shows correct branch (main) and clean state
- [ ] Initial commit is mentioned
- [ ] Backlog noted as empty with invitation to define first feature
- [ ] One feature tip is included (rotated from list)
- [ ] Prettier installation offer is included since conditions met
- [ ] Session settings menu is presented with defaults clearly marked
- [ ] Greeting ends with invitation to work or configure
- [ ] Tone is welcoming, not overwhelming
- [ ] No commands are executed that modify state (silent gathering only)
- [ ] Highlight is tracked in highlights-shown.log
- [ ] Length is appropriate (~10-20 lines total)

## Anti-Patterns to Avoid

- [ ] Don't execute work before user responds
- [ ] Don't install prettier without user consent
- [ ] Don't create task files speculatively
- [ ] Don't wait for user to ask "where should I start?"
- [ ] Don't skip the settings menu presentation
- [ ] Don't show multiple feature tips (just one per session start)
- [ ] Don't create session.log entry yet (that's for session end)
