---
name: session-management
description: Start and end session protocols to maintain context and ensure proper handoff
---

> **DEPRECATED:** Session protocol has moved to CLAUDE.md for reliability.
> This skill is kept for reference but should not be invoked.
> See CLAUDE.md "Session Protocol" section for current behavior.
> Domain memory files (.claude/memory/) now handle session continuity.

---

# Session Management (DEPRECATED)

Protocols for starting and ending coding sessions to maintain context continuity.

## Session Start: Greeting Protocol

**When SessionStart hook fires, deliver a contextual greeting.**

This is your first interaction with the user each session. Make it count.

### Step 1: Gather Context (silently)

Read these sources before composing greeting:

```
1. git log --oneline -5              → Recent commits
2. git rev-parse --abbrev-ref HEAD   → Current branch (compatible with all git versions)
3. git status                        → Uncommitted work
4. .ai/sessions/session.log      → Last session notes
5. .ai/tasks/backlog/            → Pending work
6. .ai/tasks/parallel/           → In-progress work
7. .ai/annealing/history/        → Recent self-improvements
8. .ai/feedback/                 → Recent observations
9. command -v prettier           → Check if prettier installed
10. package.json                  → Check if project has dependencies
```

### Step 2: Determine Highlights

Choose what to spotlight based on conditions:

| Condition | Highlight |
|-----------|-----------|
| Annealing changes applied in last 7 days | What improved and why |
| New guardian/skill added recently | Introduce it briefly |
| Task completed last session | Celebrate + what's next |
| Feedback logged but not addressed | Mention pending improvements |
| Prettier not installed + package.json exists | Offer to install prettier |
| Nothing special | Rotate through feature tips |

**Feature tip rotation** (cycle through these):
- Guardian agents: "9 specialized reviewers run in parallel on commit"
- Self-annealing: "I log observations and propose weekly improvements"
- External review: "Codex reviews provide async second opinions"
- Apprentice mode: "I explain the 'why' not just the 'what'"
- Research-first: "I web search before recommending when currency matters"
- Session continuity: "Session logs help me pick up where we left off"

Track shown highlights in `.ai/sessions/highlights-shown.log` to avoid repetition:
```
# Highlights Shown Log
2025-12-01: guardian-agents - shown
2025-12-02: self-annealing - shown
2025-12-03: external-review - shown
```

### Session Start Anti-Patterns

**DO NOT:**
- Create or modify session.log on start (that's for session end)
- Execute work before user responds
- Install prettier without user consent
- Create task files speculatively
- Show multiple feature tips (just one per session)
- Wait for user to ask "where should I start?"

### Step 3: Compose Greeting

Structure (~5-10 lines, engaging, substantive):

```
## Greeting Template

[Opening - time-aware, natural]

**Project Status**
- Branch: [branch] | [X uncommitted files if any]
- Recent: [1-2 recent commits or "fresh start"]

**Last Session**
[Brief summary from session.log, or "First session" if new]

**What's Next**
[From backlog/parallel tasks, or ask if empty]

**[Highlight Title]**
[Feature tip, improvement update, or celebration]

---
What are we working on today?

*Tip: When you're done, say "end session" so I can save context for next time.*
```

### Step 4: Prompt for Session Settings

After the status greeting, offer session configuration options.
Present as a quick menu — user can accept defaults or customize.

**Settings Menu:**

```
**Session Settings** (defaults in brackets, just hit enter to accept)

1. **Verbosity** — How much boilerplate commentary?
   [full] / minimal / quiet
   - full: Feature highlights, tips, system announcements
   - minimal: Status updates only, no feature talk
   - quiet: Just work, no ceremony

2. **Guardians** — Code review agents
   [all] / select / off
   - all: All 9 guardians active on commit
   - select: Choose which ones (security, typescript, test, etc.)
   - off: Skip guardian review this session

3. **Apprentice Mode** — Teaching style
   [on] / detailed / off
   - on: Explain "why", use analogies, define jargon
   - detailed: Deep explanations with history and context
   - off: Expert mode, terse responses

4. **Annealing** — Self-improvement logging
   [active] / paused
   - active: Log observations for weekly review
   - paused: Skip feedback capture this session

5. **External Review** — Codex/Gemini integration
   [enabled] / disabled
   - enabled: Create review tasks for external agents
   - disabled: Internal review only

6. **Session Docs** — Documentation level
   [full] / light / off
   - full: Session files + logs + context updates
   - light: Just session.log entries
   - off: No session documentation

7. **Sync Check** — Boilerplate updates
   [skip] / check
   - skip: Don't check for upstream changes
   - check: Look for boilerplate improvements to pull

Reply with numbers to change (e.g., "1 quiet, 3 off") or just tell me what we're building.
```

**Behavior:**
- Present menu after greeting, before diving into work
- User can respond with changes or ignore and start working
- If user ignores, use defaults (shown in brackets)
- Store session preferences in `.ai/sessions/current-preferences.json`
- Preferences persist only for current session

**Preferences Format:**

```json
// .ai/sessions/current-preferences.json
{
  "session_start": "2025-12-03T10:30:00Z",
  "settings": {
    "verbosity": "full",
    "guardians": "all",
    "apprentice": "on",
    "annealing": "active",
    "external_review": "enabled",
    "session_docs": "full",
    "sync_check": "skip"
  },
  "applied_presets": []
}
```

**How Preferences Apply:**
- Claude reads this file at relevant decision points
- `verbosity` affects explanation depth
- `guardians` affects which run on commit
- `apprentice` affects teaching style
- `annealing` affects whether observations are logged
- File is deleted at session end (preferences don't persist)

**Quick responses:**
- "defaults" or just stating a task → use all defaults
- "quiet mode" → sets verbosity to quiet
- "expert mode" → apprentice off, verbosity minimal
- "full ceremony" → everything on, detailed apprentice

**Customization prompts** (offer periodically):

After 5+ sessions, or when user skips settings repeatedly:
```
Quick note: If any of this feels like overhead, I can adjust.
Type "customize" anytime to change greeting format, defaults, or what I announce.
```

After user expresses friction:
```
Sounds like [X] isn't working for you. Want me to adjust it?
```

Track in `.boilerplate.json` → `session_count` and `settings_skipped_count`

### Prettier Setup (when user accepts)

If user says "install prettier" or similar:

```bash
# Install
pnpm add -D prettier

# Create config
cat > .prettierrc << 'EOF'
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5"
}
EOF
```

After install, confirm: "Done! The auto-format hook will now run on every file I edit."

Track in `.ai/sessions/tooling-offered.log`:
```
YYYY-MM-DD: prettier - accepted/declined
```

Only offer once per project. If declined, don't ask again unless user asks.

### Step 5: Deliver and Engage

- Deliver greeting as your first message
- Present settings menu
- Don't wait for user to ask "where were we?"
- End with invitation to confirm direction or start working

### Greeting Examples

**Example 1: Continuing work (full greeting with settings)**
```
Good morning!

**Project Status**
- Branch: feature/auth-flow | 2 uncommitted files
- Recent: "Add login component", "Set up auth context"

**Last Session**
We got the login form working and connected auth context.
Left off debugging the redirect after successful login.

**What's Next**
- Fix post-login redirect (in progress)
- Add logout functionality (backlog)

**Guardian Spotlight**
The security guardian will review your auth code on commit—it checks
for OWASP top 10, credential exposure, and session handling.

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

**Example 2: Fresh project**
```
Hey! Starting fresh on claude-code-boilerplate.

**Project Status**
- Branch: main | Clean working tree
- Recent: Initial commit with project structure

**What's Next**
Backlog is empty—what's the first feature we're building?

**Self-Annealing Intro**
As we work, I'll log observations about what's working and what's not.
Weekly, I'll propose improvements for your approval. Nothing changes
without you saying yes.

---
What should we tackle first?

*Tip: When you're done, say "end session" so I can save context for next time.*
```

**Example 3: After annealing**
```
Good afternoon!

**Project Status**
- Branch: develop | Clean
- Recent: "Refactor API handlers", "Add rate limiting"

**Last Session**
Wrapped up the API refactor. All guardians passed.

**What's Next**
- Integration tests for rate limiting (backlog)
- Documentation update (backlog)

**Recent Improvement**
Last week's annealing tightened the TypeScript guardian—it now catches
implicit `any` in function parameters. This came from 3 instances I
flagged during code review.

---
Want to start with those integration tests?

*Tip: When you're done, say "end session" so I can save context for next time.*
```

**Example 4: Prettier not installed**
```
Good morning!

**Project Status**
- Branch: feature/api | 3 uncommitted files
- Recent: "Add user endpoints", "Set up database models"

**Last Session**
Started the API endpoints for user management.

**What's Next**
- Finish CRUD operations for users
- Add validation middleware

**Quick Setup Offer**
I noticed Prettier isn't installed. The boilerplate has an auto-format
hook that runs Prettier on every file I edit — keeps code clean without
thinking about it.

Want me to set it up? Just say "install prettier" and I'll run:
`pnpm add -D prettier` + create a basic .prettierrc

(Or ignore this and we'll continue without it — totally fine.)

---
What are we working on today?

*Tip: When you're done, say "end session" so I can save context for next time.*
```

---

## Session End Protocol

When ending a session:

### 1. Commit Work

```
[ ] Stage relevant changes: git add [files]
[ ] Commit with clear message
[ ] Push to remote (if appropriate)
```

### 2. Document Session

Create `.ai/sessions/YYYY-MM-DD-[topic].md`:

```markdown
# Session: [Topic]

**Date:** YYYY-MM-DD
**Duration:** ~X hours
**Branch:** [branch-name]

## Summary
[2-3 sentences on what was accomplished]

## Completed
- [Task 1]
- [Task 2]

## In Progress
- [Task being worked on]
- [State it's in]

## Blocked / Needs Attention
- [Any blockers]
- [Things to investigate]

## Next Steps
1. [Immediate next action]
2. [Following action]

## Files Changed
- [List significant files]

## Notes
[Anything useful for next session]
```

### 3. Update Session Log

Append to `.ai/sessions/session.log`:

```
Session End: YYYY-MM-DD HH:MM
Summary: [one-line summary]
Next: [what to do next]
---
```

### 4. Update Shared Context

If significant progress:
```
[ ] Update .ai/SHARED-CONTEXT.md
[ ] Update .ai/context/current-world-state.md if needed
[ ] Move completed tasks to .ai/tasks/completed/
```

### 5. Feedback Capture

If any observations during session:
```
[ ] Log to appropriate .ai/feedback/ file
[ ] Note any process friction
[ ] Note any tool issues
```

---

## Long Session Break

For mid-session breaks:

```
[ ] Commit current work (even if WIP)
[ ] Add TODO comment in code if mid-task
[ ] Note in session.log: "Break: [reason], resuming [when]"
```

## Context Recovery

If context is lost (crash, new chat, etc.):

1. Read `.ai/sessions/session.log` for recent activity
2. Read most recent `.ai/sessions/YYYY-MM-DD-*.md`
3. Check git log for recent commits
4. Check git status for uncommitted work
5. Ask user to confirm current task

---

## Self-Annealing

Track session management quality:

```
.ai/feedback/process/session-management.md

### YYYY-MM-DD
**Issue:** [context lost / unclear handoff / greeting missed something]
**Cause:** [what went wrong]
**Improvement:** [what would help]
```
