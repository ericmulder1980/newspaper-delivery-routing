# Expected Behavior: Context Recovery After Crash

## Expected Actions

### 1. Detect Context Loss
- [ ] Recognize this is a fresh chat session (no conversation history)
- [ ] Read `.ai/sessions/session.log`
- [ ] Identify last entry is "Session Start: 2025-12-03 09:00" without "Session End"
- [ ] Conclude: previous session was interrupted/crashed

### 2. Gather Recovery Context
- [ ] Read most recent session doc: `.ai/sessions/2025-12-02-database-migrations.md`
- [ ] Execute `git log --oneline -5`
- [ ] Execute `git branch --show-current`
- [ ] Execute `git status` (find 2 files modified/added)
- [ ] List `.ai/tasks/parallel/` and find `user-table-indexes.md`
- [ ] Read `user-table-indexes.md` to understand current state

### 3. Reconstruct State
From gathered information:
- Last complete session: 2025-12-02, set up migrations, user schema
- Interrupted session: Started 2025-12-03 09:00, working on indexes/constraints
- Current files:
  - `prisma/schema.prisma` modified
  - `prisma/migrations/20251203_add_indexes.sql` created
- Parallel task shows:
  - Migration file created but not tested
  - Next step: test migration, verify no conflicts

### 4. Compose Recovery Greeting

**Expected Greeting Structure:**

```
Hey! Looks like we lost context from earlier today—picking up where we left off.

**Recovered State**
- Branch: feature/database-setup | 2 uncommitted files
- Last clean session: 2025-12-02 (database migrations setup)
- Interrupted session: Started this morning at 09:00

**What Was Happening**
Based on logs and git status, we were working on adding indexes and
constraints to the user table. Migration file is drafted but not tested yet.

**Current Work**
- Modified: prisma/schema.prisma
- Created: prisma/migrations/20251203_add_indexes.sql
- Task: .ai/tasks/parallel/user-table-indexes.md (in progress)

**From the Task File:**
Migration file created, need to:
1. Test migration on dev database
2. Add foreign key constraints
3. Run performance benchmarks

**Confirm Direction**
Is that still what we're working on, or did something change?

---

**Session Settings** (defaults in brackets)

1. Verbosity: [full] / minimal / quiet
2. Guardians: [all] / select / off
3. Apprentice: [on] / detailed / off
4. Annealing: [active] / paused
5. External Review: [enabled] / disabled
6. Session Docs: [full] / light / off
7. Sync Check: [skip] / check

Change with "1 quiet, 3 off" or confirm we should continue with testing the migration.

*Tip: When you're done, say "end session" so I can save context for next time.*
```

### 5. Update Session Log (After Recovery)

**Append to:** `.ai/sessions/session.log`

**Entry:**
```
Session Recovered: 2025-12-03 [current-time]
Previous session interrupted at: 09:00
Recovered task: Adding indexes and constraints to user table
---
```

**Checklist:**
- [ ] Log the recovery event
- [ ] Note when previous session was interrupted
- [ ] Document what task was recovered

### 6. Wait for User Confirmation

**Do NOT:**
- [ ] Automatically continue work
- [ ] Assume the task is still correct
- [ ] Make changes without confirmation

**Do:**
- [ ] Wait for user to confirm direction
- [ ] Offer to continue OR change direction
- [ ] Be ready to read parallel task file again for full context

## Success Criteria

- [ ] Greeting acknowledges context loss ("lost context", "picking up")
- [ ] Recovered state section shows what was reconstructed
- [ ] Last clean session is identified (2025-12-02)
- [ ] Interrupted session start time is noted (09:00)
- [ ] Current uncommitted work is listed accurately
- [ ] Task file contents are summarized (what's done, what's next)
- [ ] User is asked to confirm direction (not assumed)
- [ ] Session settings menu is presented
- [ ] Session log is updated with recovery event
- [ ] Tone is confident but not presumptuous (we recovered state, but verify)
- [ ] No work proceeds until user confirms

## Anti-Patterns to Avoid

- [ ] Don't pretend context wasn't lost (acknowledge it)
- [ ] Don't start working without user confirmation
- [ ] Don't assume the recovered state is 100% accurate (ask to confirm)
- [ ] Don't fail to read the parallel task file (critical context)
- [ ] Don't ignore uncommitted changes in git status
- [ ] Don't skip the session log recovery entry
- [ ] Don't overwhelm user with too much detail (be concise but thorough)
- [ ] Don't make user piece together what was happening (do that work for them)

## Edge Cases

### If no parallel task file exists:
- [ ] Rely on session.log, git status, and most recent session doc
- [ ] Be more tentative: "Looks like we were working on [X based on files], can you confirm?"

### If session.log has no "Session Start" without "Session End":
- [ ] Still acknowledge context loss
- [ ] Use most recent session doc + git status to infer state
- [ ] Be even more tentative about what was happening

### If uncommitted changes don't match any recent task:
- [ ] Show the uncommitted files
- [ ] Ask user what they were working on
- [ ] Offer to create a new task file based on user input

### If multiple parallel tasks exist:
- [ ] List all of them
- [ ] Ask which one is current
- [ ] Offer to show details of each

### If git status shows clean tree but session wasn't closed:
- [ ] Note that work may have been committed
- [ ] Check recent commits
- [ ] Ask if there's more to do or if it was complete

## Variance Allowed

- Exact phrasing of recovery acknowledgment
- Level of detail in "What Was Happening" (concise vs verbose)
- How task file contents are summarized
- Order of presenting recovered information
- Whether to include recent commits in the summary

## Context Recovery vs Fresh Start

**This is NOT a fresh start because:**
- Session.log shows recent activity
- Git status shows uncommitted work
- Parallel task exists in progress
- Recent session docs exist

**Key difference:**
- Fresh start: "What should we build?"
- Context recovery: "Here's what we were doing, confirm?"

## Recovery Quality Check

After user confirms and work resumes:
- [ ] Read parallel task file fully for all context
- [ ] Verify uncommitted changes align with task
- [ ] If anything seems misaligned, ask before proceeding
- [ ] Update task file as work progresses
- [ ] Ensure proper session end protocol when finishing
