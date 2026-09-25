# Scenario: Prettier Not Installed

## Context

**Session State:**
- `.ai/sessions/session.log` exists
- Several highlights shown previously
- Work in progress on feature branch
- **Prettier is NOT installed**
- **package.json exists** (PostToolUse hook expects prettier)

**Session Log Content:**
```
Session End: 2025-12-02 19:00
Summary: Started API endpoints for user management
Next: Finish CRUD operations for users
---
```

**Git State:**
```
$ git log --oneline -5
abc1234 Add user endpoints
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
  modified:   src/middleware/validation.ts

no changes added to commit
```

**Package.json:**
```
$ cat package.json
{
  "name": "my-api-project",
  "version": "1.0.0",
  "devDependencies": {
    "typescript": "^5.3.0",
    "@types/node": "^20.0.0"
  },
  "dependencies": {
    "express": "^4.18.0"
  }
}
```

**Prettier Check:**
```
$ command -v prettier
(not found)
```

**Settings.json Hook:**
The PostToolUse hook in .claude/settings.json includes:
```json
{
  "matcher": "Edit:*.ts|Edit:*.tsx|Edit:*.js|Edit:*.jsx|Edit:*.json|Edit:*.css|Edit:*.md",
  "hooks": [
    {
      "type": "command",
      "command": "command -v prettier >/dev/null && prettier --write \"$CLAUDE_FILE_PATH\" 2>/dev/null || true"
    }
  ]
}
```

This hook expects prettier but gracefully handles its absence with `|| true`.

**Backlog:**
```
$ ls .ai/tasks/backlog/
add-validation-middleware.md
user-authentication.md
```

**Highlights Shown:**
```
$ cat .ai/sessions/highlights-shown.log
2025-11-28: guardian-agents - shown
2025-11-30: self-annealing - shown
2025-12-01: external-review - shown
2025-12-02: apprentice-mode - shown
```

**Trigger:**
SessionStart hook fires - user is continuing work on user API feature.

## Input

User opens Claude Code to continue working on the user API. The SessionStart hook executes, triggering the session-management skill.

The skill should detect that:
1. package.json exists (so this is a real project, not just exploration)
2. Prettier is not installed
3. The PostToolUse hook expects prettier for auto-formatting
4. This is a good opportunity to offer installation

## What the Skill Should Do

The skill should:
1. Silently gather context from all sources
2. Recognize package.json exists
3. Execute `command -v prettier` and find it's not installed
4. Compose a greeting that:
   - Shows project status (feature branch, uncommitted files)
   - Summarizes last session
   - Lists what's next
   - **Offers prettier installation** as the highlight (priority over feature tips)
   - Explains the auto-format hook benefit
   - Makes it easy to accept ("just say 'install prettier'")
   - Makes it clear it's optional ("or ignore this")
5. Present session settings menu
6. Track that prettier was offered (so it's not offered again if declined)
