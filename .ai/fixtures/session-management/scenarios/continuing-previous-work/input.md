# Scenario: Continuing Previous Work

## Context

**Session State:**
- `.ai/sessions/session.log` exists with recent entry
- `.ai/sessions/2025-12-02-auth-flow.md` exists (yesterday's session doc)
- `.ai/tasks/backlog/` has 1 task file
- `.ai/tasks/parallel/auth-redirect-fix.md` exists (in-progress task)
- `.ai/annealing/history/` is empty (no recent changes)
- `.ai/sessions/highlights-shown.log` exists with 3 previous highlights
- Prettier is already installed

**Session Log Content:**
```
Session End: 2025-12-02 18:30
Summary: Got login form working, connected auth context, debugging post-login redirect
Next: Fix redirect issue after successful authentication
---
```

**Git State:**
```
$ git log --oneline -5
def5678 Add login component
abc4567 Set up auth context
789abcd Add routing structure
456def0 Initial auth setup
123abc9 Initial commit

$ git branch --show-current
feature/auth-flow

$ git status
On branch feature/auth-flow
Changes not staged for commit:
  modified:   src/components/LoginForm.tsx
  modified:   src/context/AuthContext.tsx

no changes added to commit
```

**Backlog Task:**
```
$ cat .ai/tasks/backlog/logout-functionality.md
# Task: Add Logout Functionality

Implement logout button and clear auth state.
```

**Parallel Task:**
```
$ cat .ai/tasks/parallel/auth-redirect-fix.md
# Task: Fix Post-Login Redirect

Status: In Progress
Started: 2025-12-02

Issue: After successful login, redirect to /dashboard fails.
Currently investigating router configuration.
```

**Prettier Check:**
```
$ command -v prettier
/usr/local/bin/prettier
```

**Highlights Shown:**
```
$ cat .ai/sessions/highlights-shown.log
2025-11-28: guardian-agents - shown
2025-11-30: self-annealing - shown
2025-12-01: external-review - shown
```

**Trigger:**
SessionStart hook fires - user is continuing work from yesterday.

## Input

User opens Claude Code to continue working on the auth flow feature. The SessionStart hook executes, triggering the session-management skill.

## What the Skill Should Do

The skill should:
1. Silently gather context from all sources
2. Read session.log to get the last session summary
3. Recognize uncommitted changes exist (2 files modified)
4. Identify current branch (feature/auth-flow)
5. Read the in-progress parallel task
6. Compose a greeting that:
   - Acknowledges continuation from yesterday
   - Shows project status with uncommitted file count
   - Summarizes last session from session.log
   - Lists what's next (parallel task + backlog)
   - Selects a NEW feature tip (not one of the 3 already shown)
7. Present session settings menu
8. Ask if user wants to continue with the redirect fix
9. Track the new highlight shown
