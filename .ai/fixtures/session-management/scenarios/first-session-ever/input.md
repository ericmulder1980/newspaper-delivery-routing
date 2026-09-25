# Scenario: First Session Ever

## Context

**Session State:**
- `.ai/sessions/session.log` does not exist
- `.ai/sessions/` directory is empty
- `.ai/tasks/backlog/` is empty
- `.ai/tasks/parallel/` is empty
- `.ai/annealing/history/` is empty
- `.ai/feedback/` is empty
- No `.ai/sessions/highlights-shown.log`
- No `.ai/sessions/current-preferences.json`

**Git State:**
```
$ git log --oneline -5
abc1234 Initial commit: Claude Code boilerplate structure

$ git branch --show-current
main

$ git status
On branch main
nothing to commit, working tree clean
```

**Project State:**
```
$ ls package.json
package.json

$ cat package.json
{
  "name": "my-new-project",
  "version": "1.0.0",
  "devDependencies": {}
}

$ command -v prettier
(not found)
```

**Trigger:**
SessionStart hook fires - this is the very first session in this project.

## Input

User opens Claude Code for the first time in this fresh project. The SessionStart hook executes, triggering the session-management skill.

## What the Skill Should Do

The skill should:
1. Silently gather context from all sources (git, logs, tasks)
2. Recognize this is the first session (no session.log)
3. Compose a welcoming greeting that:
   - Acknowledges fresh start
   - Shows project status (main branch, clean tree, initial commit)
   - Notes the backlog is empty
   - Selects a feature tip from rotation (since nothing special to highlight)
   - Offers to install prettier (since package.json exists but prettier is missing)
4. Present the session settings menu with defaults
5. Ask what to build first
6. Track the shown highlight to avoid repetition
