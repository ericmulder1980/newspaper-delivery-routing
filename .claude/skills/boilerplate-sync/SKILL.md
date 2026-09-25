---
name: boilerplate-sync
description: Sync boilerplate improvements between instances and upstream source
---

# Boilerplate Sync

Bidirectional sync between this project and the canonical boilerplate source.

## Overview

```
                    UPSTREAM (source repo)
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
         ▼                 ▼                 ▼
    Project A         Project B         Project C
         │                 │                 │
         └────── upstream-candidates ───────┘
                           │
                           ▼
                    PR to source
```

## Commands

### `sync status`

Show sync state:
- Last sync date
- Files changed locally since sync
- Files changed upstream since sync
- Pending upstream candidates

```bash
# What the skill does:
1. Read .boilerplate.json for source info
2. Check if this IS the source repo (see below)
3. Compare local managed_files against last_sync
4. Fetch upstream (without merging) to compare
5. List upstream-candidates not yet pushed
```

#### Source Repo Detection

Before any sync operation, check if this IS the source repo:

```bash
# Get source from .boilerplate.json
source_repo=$(jq -r '.source.repo' .boilerplate.json)

# Get origin URL
origin_url=$(git remote get-url origin 2>/dev/null)

# Compare (normalize URLs)
if [[ "$origin_url" == *"$source_repo"* ]]; then
  # This IS the source repo
fi
```

**If this IS the source repo, display:**

```
⚠️  This IS the boilerplate source repository

Sync is for projects that USE the boilerplate, not the source itself.

In this repo:
- Edit files directly (they become the canonical version)
- Push to origin (other projects sync FROM here)
- Review PRs from projects using sync push

Commands that work here:
- sync status --instances  (see who uses this boilerplate)
- Direct git operations

Commands that don't apply:
- sync pull  (you ARE upstream)
- sync push  (your changes ARE upstream)
```

**Do NOT proceed with sync operations if this is the source repo.**

### `sync pull`

Fetch and selectively merge upstream changes.

#### First-Time Setup Detection

If no `last_sync` in `.boilerplate.json`, trigger first-time setup:

```
First-time sync setup detected

This appears to be the first time running sync for this project.

Setting up boilerplate remote...
✓ Added remote: boilerplate → [source]

Fetching from upstream...
✓ Fetched [N] commits from boilerplate/main

Setup Options:
  1. Treat current state as customized (SAFE)
     - Keeps all your current files unchanged
     - Marks them as "customized" for manual review
     - Best for existing projects with local changes

  2. Sync from upstream (FRESH START)
     - Replaces local managed files with upstream versions
     - Creates backup first
     - Best for new projects

  3. Show differences first (REVIEW)
     - Shows what would change with each option
     - Then choose option 1 or 2
```

#### Normal Sync Flow

```bash
# What the skill does:
1. git fetch boilerplate (or setup if first time)
2. Diff managed_files between local and upstream
3. For each changed file:
   - If in customized[] → show diff, ask to merge or skip
   - If not customized → auto-merge (with backup)
4. Update .boilerplate.json last_sync and last_sync_commit
```

#### Backup Protocol

**Before ANY file changes:**

```bash
# Create timestamped backup
mkdir -p .claude/agents/.backup-$(date +%Y-%m-%dT%H-%M-%S)/
cp [files-to-be-changed] .claude/agents/.backup-.../
```

Always show rollback instructions after changes:
```
Your backup (in case you need to restore):
  .claude/agents/.backup-2025-12-03T15-30-00/

To rollback: git reset --hard HEAD~1
```

#### Conflict Resolution

When same file modified locally and upstream:

```
⚠️  CONFLICT DETECTED

File: .claude/agents/security-guardian.md
Both local and upstream modified this file.

┌─ Upstream version ──────────────────────────────┐
│ [show relevant diff section]                     │
└──────────────────────────────────────────────────┘

┌─ Local version ─────────────────────────────────┐
│ [show relevant diff section]                     │
└──────────────────────────────────────────────────┘

Resolution options:
  1. Take upstream - Replace with upstream version
  2. Keep local - Preserve your changes
  3. Merge manually - Combine both changes
  4. Mark as customized - Keep local, skip future auto-merges

Your choice [1/2/3/4]:
```

**NEVER auto-resolve conflicts.** Always require user decision.

Log resolution to `.ai/feedback/process/boilerplate-sync.md` and track in `.boilerplate.json`:
```json
{
  "conflict_resolutions": [
    {
      "file": ".claude/agents/security-guardian.md",
      "date": "2025-12-03T15:30:00Z",
      "method": "manual_merge",
      "upstream_commit": "def456",
      "local_commit": "xyz999"
    }
  ]
}
```

If user chooses "Mark as customized", save upstream diff for later review:
```
.ai/upstream-diffs/2025-12-03-security-guardian.diff
```

### `sync push`

Propose local improvements to upstream:

```bash
# What the skill does:
1. Scan .ai/feedback/upstream-candidates/
2. For each candidate:
   - Validate it's general (not project-specific)
   - Format as GitHub issue or PR
3. Use gh CLI to create issue/PR
4. Move candidate to .ai/feedback/upstream-candidates/submitted/
5. Track in .boilerplate.json upstream_candidates[]
```

**Candidate format:**
```markdown
# .ai/feedback/upstream-candidates/2025-12-03-typescript-implicit-any.md

Type: guardian-improvement
Target: .claude/agents/typescript-guardian.md
Summary: Catch implicit `any` in arrow function parameters
Evidence: Found 3 bugs in auth module that slipped through
Status: pending

## Current Behavior
[What the guardian does now]

## Proposed Change
[What it should do]

## Diff
```diff
- Check for explicit `any` usage
+ Check for explicit `any` usage
+ Check for implicit `any` in arrow function parameters: (x) => vs (x: Type) =>
```

## Test Cases
- `const fn = (x) => x.foo` → should flag
- `const fn = (x: User) => x.foo` → should pass
```

### `sync register`

Register this instance with the boilerplate ecosystem:

```bash
# What the skill does:
1. Read .boilerplate.json
2. Add this project to registry (local file or remote)
3. Enable tracking for federated improvements
```

**Registry file (.ai/boilerplate-instances.json):**
```json
{
  "instances": [
    {
      "path": "/Users/you/projects/project-a",
      "name": "project-a",
      "registered": "2025-12-03",
      "last_active": "2025-12-03"
    }
  ]
}
```

---

## Workflow: Making a Local Improvement Upstream-Ready

### 1. During Annealing

When weekly-annealing identifies an improvement:

```markdown
## Improvement Identified

**File:** .claude/agents/security-guardian.md
**Change:** Add tenant isolation check for shared database queries
**Evidence:** Missed 2 cross-tenant data leaks in Project B

**Is this project-specific or general?**
- [ ] Project-specific → apply locally only
- [x] General → create upstream candidate
```

### 2. Create Candidate

If general, skill creates candidate file:

```bash
.ai/feedback/upstream-candidates/2025-12-03-security-tenant-isolation.md
```

### 3. Push to Upstream

When ready:

```
> sync push

Found 1 upstream candidate:
- security-tenant-isolation: Add tenant isolation check

Create GitHub issue for this? [y/n]
```

### 4. Track Status

```
> sync status

Upstream candidates:
- security-tenant-isolation: submitted (PR #42, pending review)
- typescript-implicit-any: approved (merged, available in v0.2.0)
```

---

## Workflow: Pulling Upstream Improvements

### 1. Check for Updates

```
> sync status

Upstream changes available (last sync: 2025-11-28):
- .claude/agents/typescript-guardian.md (modified)
- .claude/skills/weekly-annealing/SKILL.md (modified)
- .claude/agents/perf-guardian.md (new)

1 file in your customized list has upstream changes.
```

### 2. Pull Changes

```
> sync pull

Pulling from your-username/claude-code-boilerplate...

typescript-guardian.md:
  [View diff] [Take upstream] [Keep local] [Merge] [Mark customized]

weekly-annealing/SKILL.md:
  Auto-merged (not in customized list)

perf-guardian.md:
  New file added

Updated .boilerplate.json (last_sync: 2025-12-03)
```

---

## Community Scaling

### Personal Multi-Project

For syncing across your own projects:

1. Keep boilerplate as separate repo (source of truth)
2. Each project has `.boilerplate.json` pointing to it
3. Run `sync pull` periodically in each project
4. `sync push` improvements from any project to source

### Team/Org

For shared boilerplate within a team:

1. Boilerplate repo with branch protection
2. `sync push` creates PRs requiring review
3. Team reviews and merges good improvements
4. All projects `sync pull` merged changes

### Community (Future)

For open-source shared learning:

1. Public boilerplate repo
2. Contributions via PR with evidence
3. Voting/approval mechanism for changes
4. Versioned releases for stability
5. Telemetry opt-in: anonymous improvement signals

**Trust levels:**
- **Verified**: Changes that improved measurable outcomes
- **Community**: Changes with multiple project endorsements
- **Experimental**: New changes pending validation

---

## Implementation Notes

### Git Setup

For sync to work, project needs upstream remote:

```bash
git remote add boilerplate git@github.com:you/claude-code-boilerplate.git
git fetch boilerplate
```

Or for first-time setup:

```bash
# Skill handles this automatically on first sync pull
```

### GitHub CLI

Push requires `gh` CLI authenticated:

```bash
gh auth status  # Check auth
gh auth login   # If needed
```

### File Tracking

The `managed_files` array in `.boilerplate.json` uses glob patterns.
Files matching these patterns are candidates for sync.
Files in `customized` array require manual merge decisions.

---

## Self-Annealing

Track sync effectiveness:

```
.ai/feedback/process/boilerplate-sync.md

### 2025-12-03
**Action:** Pulled typescript-guardian update
**Result:** Caught 2 additional type issues in next commit
**Signal:** Upstream improvement validated

### 2025-12-02
**Action:** Pushed tenant-isolation candidate
**Result:** Merged to upstream after review
**Signal:** Local improvement benefited ecosystem
```
