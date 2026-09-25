# Scenario: End Session (Normal)

## Context

**Session State:**
- Session started 2025-12-03 09:00
- Current time: 2025-12-03 14:30 (~5.5 hours elapsed)
- Work was completed during session
- Changes exist and need to be committed
- `.ai/sessions/current-preferences.json` exists (user set verbosity to minimal this session)

**Current Preferences:**
```
$ cat .ai/sessions/current-preferences.json
{
  "verbosity": "minimal",
  "guardians": "all",
  "apprentice": "on",
  "annealing": "active",
  "external_review": "enabled",
  "session_docs": "full",
  "sync_check": "skip"
}
```

**Git State:**
```
$ git log --oneline -5
abc1234 Add user endpoints (from previous session)
def5678 Set up database models
789abcd Configure database connection
012cdef Add environment config
345fghi Initial setup

$ git branch --show-current
feature/user-api

$ git status
On branch feature/user-api
Changes not staged for commit:
  modified:   src/api/users.ts
  modified:   src/models/User.ts
  new file:   src/api/users.test.ts
  new file:   src/middleware/validation.ts

no changes added to commit
```

**Work Completed This Session:**
1. Finished CRUD operations for users (users.ts modified)
2. Added validation middleware (validation.ts created)
3. Wrote tests for user endpoints (users.test.ts created)
4. Updated User model with new fields (User.ts modified)

**Observations During Session:**
During the session, there was one moment worth noting for feedback:
- The API guardian flagged a false positive on REST naming (suggested plural when it was already correct)

**Backlog:**
```
$ ls .ai/tasks/backlog/
user-authentication.md
pagination-support.md
```

**Trigger:**
User says: "Let's wrap up for today, end session"

## Input

User explicitly requests to end the session. The session-management skill should execute the end session protocol.

## What the Skill Should Do

The skill should execute the 5-step end session protocol:

### 1. Commit Work
- Stage all relevant changes
- Create a clear commit message
- Push to remote (since it's a feature branch)

### 2. Document Session
- Create `.ai/sessions/2025-12-03-user-crud-api.md` with:
  - Summary of what was accomplished
  - Completed tasks
  - Nothing in progress (work is done)
  - Nothing blocked
  - Next steps (from backlog)
  - Files changed
  - Notes for next session

### 3. Update Session Log
- Append entry to `.ai/sessions/session.log`:
  - End timestamp
  - One-line summary
  - What to do next

### 4. Update Shared Context (if significant)
- This is significant progress (new feature module)
- Update `.ai/SHARED-CONTEXT.md` if needed
- Don't need to update current-world-state.md (no new tools/versions)

### 5. Feedback Capture
- Log the API guardian false positive observation to `.ai/feedback/guardians/api.md`

### Cleanup
- Delete `.ai/sessions/current-preferences.json` (session is ending)
