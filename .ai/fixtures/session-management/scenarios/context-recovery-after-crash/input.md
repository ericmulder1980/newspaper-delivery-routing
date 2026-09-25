# Scenario: Context Recovery After Crash

## Context

**Session State:**
- Previous session crashed or was interrupted unexpectedly
- No conversation history available in current session
- User is starting a fresh chat session
- BUT: session.log and git status show work was in progress

**Session Log Content:**
```
$ cat .ai/sessions/session.log
Session Start: 2025-12-03 09:00
Session working...

Session End: 2025-12-02 18:00
Summary: Set up database migrations, started user schema
Next: Add indexes and constraints to user table
---

Session End: 2025-12-01 17:30
Summary: Initial database setup, configured Prisma
Next: Create user table migration
---
```

**Note:** Last session (2025-12-03) started but never ended properly (no "Session End" entry).

**Git State:**
```
$ git log --oneline -5
abc1234 Add user table migration
def5678 Configure Prisma client
789abcd Set up database connection
012cdef Add environment variables
345fghi Initial commit

$ git branch --show-current
feature/database-setup

$ git status
On branch feature/database-setup
Changes not staged for commit:
  modified:   prisma/schema.prisma
  new file:   prisma/migrations/20251203_add_indexes.sql

Untracked files:
  .ai/tasks/parallel/user-table-indexes.md

no changes added to commit
```

**Parallel Task (Left in progress):**
```
$ cat .ai/tasks/parallel/user-table-indexes.md
# Task: Add Indexes and Constraints to User Table

**Status:** In Progress
**Started:** 2025-12-03 09:15

## Goal
Add performance indexes and data integrity constraints to user table.

## Progress
- [x] Identify fields needing indexes (email, username, created_at)
- [x] Draft migration file
- [ ] Test migration on dev database
- [ ] Add foreign key constraints
- [ ] Run performance benchmarks

## Current State
Migration file created (20251203_add_indexes.sql) but not tested yet.
Schema file modified with constraint definitions.

## Next
Test the migration, verify no conflicts.
```

**Most Recent Session Doc:**
```
$ cat .ai/sessions/2025-12-02-database-migrations.md
# Session: Database Migrations Setup

**Date:** 2025-12-02
**Duration:** ~3 hours
**Branch:** feature/database-setup

## Summary
Set up Prisma migrations workflow and created initial user table schema.

## Completed
- Configure Prisma schema file
- Create user table migration
- Test migration rollback

## In Progress
(None - wrapped cleanly)

## Next Steps
1. Add indexes and constraints to user table
2. Create relationships for user roles
```

**Trigger:**
User starts a fresh session (new chat) after a crash/interruption. SessionStart hook fires but there's no conversation history from the interrupted session.

## Input

User opens Claude Code in a new chat session after the previous session crashed or was interrupted. The SessionStart hook executes, triggering the session-management skill.

The skill needs to detect:
1. No conversation history (fresh chat)
2. BUT session.log shows a session started but never ended
3. Git status shows uncommitted work
4. A parallel task exists in progress
5. This is context recovery, not a fresh start

## What the Skill Should Do

The skill should execute the **Context Recovery** protocol:

1. Read `.ai/sessions/session.log` for recent activity
2. Notice last entry is "Session Start: 2025-12-03 09:00" without matching "Session End"
3. Read most recent session doc (2025-12-02)
4. Check git log and git status
5. Read parallel task file to understand current state
6. Compose a greeting that:
   - Acknowledges context loss/recovery
   - Summarizes what was happening (from parallel task and git status)
   - Shows current uncommitted work
   - Asks user to confirm the current task
   - Offers to continue or change direction
7. Present session settings menu
8. Wait for user confirmation before proceeding
