---
name: boilerplate-migrate
description: Migrate existing projects to use the claude-code-boilerplate structure
---

# Boilerplate Migration

Add boilerplate structure to existing projects without breaking what's already there.

## Overview

```
BEFORE                           AFTER
──────                           ─────
project/                         project/
├── src/                         ├── src/
├── package.json                 ├── package.json
├── .claude/ (maybe)             ├── .claude/
│   └── (existing configs)       │   ├── agents/        ← NEW
└── ...                          │   ├── skills/        ← NEW
                                 │   ├── commands/      ← NEW
                                 │   └── settings.json  ← MERGED
                                 ├── .ai/               ← NEW
                                 │   ├── tasks/
                                 │   ├── sessions/
                                 │   ├── feedback/
                                 │   └── ...
                                 ├── .boilerplate.json  ← NEW
                                 ├── CLAUDE.md          ← MERGED
                                 └── ...
```

## Migration Modes

### `migrate init`

First-time setup. Interactive, safe, preserves existing config.

### `migrate update`

Already migrated, pulling latest boilerplate structure.

### `migrate check`

Dry-run: show what would change without changing anything.

---

## Full Migration Process

### Step 1: Pre-flight Check

```
[ ] Confirm this is a git repository
[ ] Check for uncommitted changes (warn if dirty)
[ ] Detect existing .claude/ directory
[ ] Detect existing CLAUDE.md
[ ] Detect existing .ai/ directory
[ ] Identify project type (Node, Python, etc.)
```

**Output:**
```
Migration Pre-flight Check
──────────────────────────

Git repo: ✓
Working tree: clean (or: 3 uncommitted files - proceed with caution)

Existing structure:
- .claude/: Found (will merge)
- CLAUDE.md: Found (will merge)
- .ai/: Not found (will create)

Project type detected: Node.js + TypeScript

Proceed with migration? [y/n]
```

### Step 2: Create Backup

Before any changes:

```bash
# Create backup branch
git checkout -b pre-boilerplate-backup
git checkout -

# Or stash existing
git stash push -m "Pre-boilerplate migration backup"
```

### Step 3: Fetch Boilerplate

```bash
# Clone boilerplate to temp location
git clone --depth 1 git@github.com:USER/claude-code-boilerplate.git /tmp/boilerplate-source

# Or if already configured:
# Read from .boilerplate.json source
```

### Step 4: Create Directory Structure

Create missing directories:

```
.claude/
├── agents/
├── skills/
└── commands/

.ai/
├── tasks/
│   ├── backlog/
│   ├── parallel/
│   ├── review/
│   └── completed/
├── sessions/
├── feedback/
│   ├── guardians/
│   ├── skills/
│   ├── process/
│   ├── directives/
│   └── upstream-candidates/
│       └── submitted/
├── annealing/
│   ├── weekly/
│   └── history/
├── context/
└── research/
```

### Step 5: Merge or Create CLAUDE.md

**If CLAUDE.md exists:**

```
Existing CLAUDE.md detected.

Options:
1. Merge: Add boilerplate sections, keep your content
2. Append: Add boilerplate reference at end
3. Keep: Don't modify, just add reference file
4. Replace: Use boilerplate version (backup existing)

Choice [1/2/3/4]:
```

**Merge strategy (option 1):**

```markdown
# [Existing Project Name]

[... existing content preserved ...]

---

<!-- Boilerplate sections below - managed by boilerplate-sync -->

## Boilerplate

See @.ai/SHARED-CONTEXT.md for boilerplate documentation.

## Key Skills
- `guardian-agents` — Parallel review orchestration
- `boilerplate-sync` — Sync improvements across instances
- `boilerplate-migrate` — Add boilerplate to existing projects
- `weekly-annealing` — Self-improvement cycle

Note: Session protocol is now inline in CLAUDE.md, not a separate skill.

## Guardians
9 specialized agents. Invoke via `guardian-agents` skill or `/review`.

<!-- End boilerplate sections -->
```

**If CLAUDE.md doesn't exist:**
- Copy boilerplate version
- Prompt user to customize header

### Step 6: Merge settings.json

**If .claude/settings.json exists:**

```
Existing settings.json detected.

Your hooks:
- PreToolUse: 2 hooks
- PostToolUse: 1 hook

Boilerplate hooks:
- SessionStart: 1 hook
- PreToolUse: 1 hook (git commit guardian)
- Stop: 1 hook

Options:
1. Merge: Combine all hooks (yours + boilerplate)
2. Yours first: Your hooks take priority
3. Boilerplate first: Boilerplate hooks take priority
4. Keep: Don't modify settings

Choice [1/2/3/4]:
```

**Merge output:**
```json
{
  "hooks": {
    "SessionStart": [...boilerplate...],
    "PreToolUse": [...yours..., ...boilerplate...],
    "PostToolUse": [...yours...],
    "Stop": [...boilerplate...]
  },
  "guardians": { ...boilerplate... },
  ...
}
```

### Step 7: Copy Boilerplate Components

Copy from source, skip existing:

```
Copying boilerplate components...

.claude/agents/INITIALIZER.md            ✓ created
.claude/agents/PLANNER.md                 ✓ created
.claude/agents/IMPLEMENTER.md             ✓ created
.claude/agents/REVIEWER.md                ✓ created
.claude/agents/domains/security/          ✓ created
.claude/agents/domains/typescript/        ✓ created
.claude/agents/domains/...                ✓ created

.claude/skills/guardian-agents/           ✓ created
.claude/skills/weekly-annealing/          ✓ created
.claude/skills/...                        ✓ created

.claude/commands/review.md                ✓ created
.claude/commands/...                      ✓ created

Skipped (already exist):
- .claude/commands/my-custom-command.md
```

### Step 8: Create .boilerplate.json

```json
{
  "name": "my-existing-project",
  "version": "0.1.0",
  "source": {
    "type": "github",
    "repo": "USER/claude-code-boilerplate",
    "branch": "main"
  },
  "migrated_from": {
    "date": "2025-12-03",
    "had_claude_dir": true,
    "had_claude_md": true,
    "backup_branch": "pre-boilerplate-backup"
  },
  "last_sync": "2025-12-03",
  "managed_files": [...],
  "customized": [
    ".claude/commands/my-custom-command.md",
    "CLAUDE.md"
  ]
}
```

### Step 9: Initialize .ai/ Files

Create starter files:

```
.ai/sessions/session.log          ← initialized
.ai/context/current-world-state.md ← template
.ai/SHARED-CONTEXT.md              ← template with project detection
.ai/boilerplate-instances.json     ← registered
```

**SHARED-CONTEXT.md auto-detection:**

```markdown
# Shared Project Context

Last updated: 2025-12-03

## Project Overview

[Detected: Node.js project with TypeScript]

## Tech Stack

- **Runtime:** Node.js (detected from package.json)
- **Language:** TypeScript (detected from tsconfig.json)
- **Framework:** [Please specify]
- **Package Manager:** pnpm (detected from pnpm-lock.yaml)

...
```

### Step 10: Register Instance

Add to instances registry (if hub configured):

```
Registering with boilerplate hub...

Instance registered:
- Path: /Users/you/projects/my-project
- Name: my-project
- Version: 0.1.0
```

### Step 11: Post-Migration Summary

```
Migration Complete!
───────────────────

Created:
- .claude/agents/ (9 guardians)
- .claude/skills/ (7 skills)
- .claude/commands/ (3 commands)
- .ai/ (full structure)
- .boilerplate.json

Merged:
- CLAUDE.md (your content + boilerplate sections)
- .claude/settings.json (your hooks + boilerplate hooks)

Preserved:
- .claude/commands/my-custom-command.md (marked as customized)

Backup:
- Branch: pre-boilerplate-backup
- Restore: git checkout pre-boilerplate-backup

Next steps:
1. Review merged CLAUDE.md
2. Customize .ai/SHARED-CONTEXT.md
3. Run `sync status` to verify connection
4. Start a new session to test greeting

Need to undo? Run: git checkout pre-boilerplate-backup
```

---

## Conflict Resolution

### File Exists with Same Name

```
Conflict: .claude/agents/security-guardian.md exists

Your version: 45 lines, modified 2025-11-15
Boilerplate: 62 lines, version 0.1.0

Options:
1. Keep yours (mark as customized)
2. Take boilerplate (backup yours to .bak)
3. View diff
4. Merge manually

Choice [1/2/3/4]:
```

### Hook Conflicts

```
Hook conflict in PreToolUse:

Your hook:
  matcher: "Bash:*"
  command: "echo 'custom check'"

Boilerplate hook:
  matcher: "Bash:git commit"
  command: "echo 'Guardian pre-commit check'"

These don't conflict (different matchers). Merging both.
```

### Incompatible Settings

```
Warning: Your settings.json has deprecated format.

Found:
  "allowedTools": ["Bash", "Read"]

Boilerplate expects:
  "permissions": { "tools": [...] }

Options:
1. Auto-convert to new format
2. Keep old format (may not work with all features)
3. Show me the difference

Choice [1/2/3]:
```

---

## Project Type Detection

Migration adapts to project type:

| Detected | Adjustments |
|----------|-------------|
| Node.js + TypeScript | Enable typescript-guardian, add npm scripts |
| Python | Adjust guardians for Python patterns |
| Rust | Enable systems-focused guardians |
| Monorepo | Detect workspaces, suggest per-package config |
| No package manager | Minimal setup, manual config |

---

## Rollback

If something goes wrong:

```bash
# Full rollback
git checkout pre-boilerplate-backup
git branch -D main  # if you were on main
git checkout -b main

# Partial rollback (keep some changes)
git checkout pre-boilerplate-backup -- .claude/settings.json

# Just remove boilerplate additions
rm -rf .ai/
rm .boilerplate.json
git checkout HEAD -- CLAUDE.md  # restore original
```

---

## Domain Memory Migration

When migrating a project, always add the domain memory scaffold:

### Step 1: Check for existing memory
```bash
ls .claude/memory/ 2>/dev/null || echo "No memory directory"
```

### Step 2: Create memory scaffold
If no memory exists:
```bash
mkdir -p .claude/memory
```

Then create:
- `.claude/memory/features.json` — from template
- `.claude/memory/progress.log` — initialized
- `.claude/memory/decisions.md` — from template
- `.claude/memory/issues.md` — from template

### Step 3: Ask clarifying questions

```
I'm setting up domain memory for your project. A few questions:

1. **Active work:** What are you currently working on?
   (I'll create the first feature entry)

2. **Key decisions already made:** Any architectural choices I should document?

3. **Known issues:** Any bugs or tech debt to track?
```

### Step 4: Populate initial state

Based on user answers:
- Add first feature to features.json
- Record any decisions to decisions.md
- Add any issues to issues.md

### Step 5: Update CLAUDE.md

Merge session protocol into existing CLAUDE.md while preserving project-specific content.

---

## Self-Annealing

Track migration quality:

```
.ai/feedback/process/migration.md

### 2025-12-03
**Project:** my-existing-project
**Issues:**
- CLAUDE.md merge left duplicate sections
- settings.json hook order caused conflict

**Improvements:**
- Add duplicate detection in merge
- Smarter hook ordering logic
```
