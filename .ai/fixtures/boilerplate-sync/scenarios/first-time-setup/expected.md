# Expected Output: First-Time Setup

## Terminal Output

```
$ sync pull

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
First-time sync setup detected
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

This appears to be the first time running sync for this project.

Reading configuration...
✓ Source: git@github.com:you/claude-code-boilerplate.git
✓ Managed files: 3 patterns

Checking upstream repository...
✓ Repository is accessible

Setting up boilerplate remote...
✓ Added remote: boilerplate → git@github.com:you/claude-code-boilerplate.git

Fetching from upstream...
✓ Fetched 47 commits from boilerplate/main

Analyzing current state...
  Found 38 managed files locally
  Upstream has 42 files in managed patterns
  4 new files available upstream
  2 local files differ from upstream

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Setup Options
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

How would you like to establish the sync baseline?

  1. Treat current state as customized (SAFE)
     - Keeps all your current files unchanged
     - Marks them as "customized" so future syncs require manual review
     - Best for existing projects with local changes

  2. Sync from upstream (FRESH START)
     - Replaces local managed files with upstream versions
     - Creates backup of current state first
     - Best for new projects or if you want upstream versions

  3. Show differences first (REVIEW)
     - Shows what would change with each option
     - Then choose option 1 or 2

Your choice [1/2/3]:
> 3

Comparing local and upstream versions...

Files that would change with option 2 (sync from upstream):

  Modified files (local differs from upstream):
    1. .claude/agents/security-guardian.md
       - Local: 156 lines
       - Upstream: 178 lines (22 lines added)

    2. .claude/agents/typescript-guardian.md
       - Local: 89 lines
       - Upstream: 95 lines (6 lines added)

  New files (only in upstream):
    3. .claude/agents/performance-guardian.md (new)
    4. .claude/agents/accessibility-guardian.md (new)
    5. .claude/agents/api-guardian.md (new)
    6. .claude/agents/database-guardian.md (new)

View detailed diff for any file? [y/n/skip]
> y

Which file? [1-6 or 'all']
> 1

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Diff: .claude/agents/security-guardian.md
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  ## Focus Areas
  - OWASP Top 10 vulnerabilities
  - Secrets/credentials in code
  - SQL/NoSQL injection
+ - JWT validation and token expiry
+ - API key rotation
+ - Rate limiting and DOS prevention
+ - CORS and CSP configuration
  - XSS and CSRF
  - Authentication/authorization flaws
  - Tenant isolation (multi-tenant apps)

[... more diff lines ...]

View another file? [y/n]
> n

Now choose how to proceed:
  1. Treat current state as customized (keep your changes)
  2. Sync from upstream (adopt upstream versions)

Your choice [1/2]:
> 2

You chose: Sync from upstream (fresh start)

Creating backup of current state...
✓ Backed up to .claude/agents/.backup-first-sync-2025-12-03T17-00-00/

Syncing managed files from upstream...
✓ Updated .claude/agents/security-guardian.md
✓ Updated .claude/agents/typescript-guardian.md
✓ Added .claude/agents/performance-guardian.md
✓ Added .claude/agents/accessibility-guardian.md
✓ Added .claude/agents/api-guardian.md
✓ Added .claude/agents/database-guardian.md

36 files already in sync.

Updating .boilerplate.json...
✓ Set last_sync: 2025-12-03T17:00:00Z
✓ Set last_sync_commit: def456 (boilerplate/main HEAD)

Committing changes...
✓ Committed: chore: initial boilerplate sync (6 files updated/added)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
First-time sync complete!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Baseline established with upstream boilerplate.

Your backup (in case you need to restore):
  .claude/agents/.backup-first-sync-2025-12-03T17-00-00/

Future syncs:
  - Run 'sync pull' to get upstream updates
  - Run 'sync push' to contribute improvements back
  - Run 'sync status' to check current state

Next steps:
  1. Review the synced files to understand what changed
  2. Test guardians: /review
  3. If you need to rollback: git reset --hard HEAD~1
```

## Alternative: User Chooses "Treat as Customized"

```
Your choice [1/2]:
> 1

You chose: Treat current state as customized

This will preserve all your current files and mark them as customized.
Future syncs will require manual review for these files.

Marking managed files as customized...
✓ Added 38 files to customized array

Updating .boilerplate.json...
✓ Set last_sync: 2025-12-03T17:00:00Z
✓ Set last_sync_commit: def456 (boilerplate/main HEAD)
✓ Marked 38 files as customized

No file changes made (all preserved).

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
First-time sync complete!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Baseline established. All current files marked as customized.

Future syncs:
  - 'sync pull' will show upstream changes and ask for review
  - You control what to merge for customized files
  - New upstream files will be added automatically

Upstream changes available now (not applied):
  4 new guardian files available in upstream
  Run 'sync pull' to review and selectively merge
```

## File Changes (Option 2: Sync from Upstream)

### .boilerplate.json (updated)

```json
{
  "source": "git@github.com:you/claude-code-boilerplate.git",
  "last_sync": "2025-12-03T17:00:00Z",
  "last_sync_commit": "def456",
  "managed_files": [
    ".claude/agents/*.md",
    ".claude/skills/**/*.md",
    "CLAUDE.md"
  ],
  "customized": [],
  "first_sync": "2025-12-03T17:00:00Z"
}
```

### Git Remote

```
$ git remote -v
origin      git@github.com:user/project.git (fetch)
origin      git@github.com:user/project.git (push)
boilerplate git@github.com:you/claude-code-boilerplate.git (fetch)
boilerplate git@github.com:you/claude-code-boilerplate.git (push)
```

## File Changes (Option 1: Treat as Customized)

### .boilerplate.json (customized)

```json
{
  "source": "git@github.com:you/claude-code-boilerplate.git",
  "last_sync": "2025-12-03T17:00:00Z",
  "last_sync_commit": "def456",
  "managed_files": [
    ".claude/agents/*.md",
    ".claude/skills/**/*.md",
    "CLAUDE.md"
  ],
  "customized": [
    ".claude/agents/security-guardian.md",
    ".claude/agents/typescript-guardian.md",
    ".claude/agents/test-guardian.md",
    ".claude/agents/architecture-guardian.md",
    ".claude/agents/docs-guardian.md",
    ".claude/skills/guardian-agents/SKILL.md",
    ".claude/skills/feature-completion/SKILL.md",
    "CLAUDE.md"
    // ... all 38 managed files
  ],
  "first_sync": "2025-12-03T17:00:00Z"
}
```

## Behavior Validation

- ✓ First-time setup detected correctly
- ✓ Upstream repo validated before proceeding
- ✓ Remote added successfully
- ✓ Clear options presented with explanations
- ✓ Diff review available before commitment
- ✓ Option 1 (customized): No files changed, all marked customized
- ✓ Option 2 (sync): Upstream adopted, backup created
- ✓ Baseline timestamp established
- ✓ Future sync behavior explained
- ✓ User retains control and safety
- ✓ Can rollback if needed
