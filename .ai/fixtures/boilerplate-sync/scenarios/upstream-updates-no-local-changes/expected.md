# Expected Output: Upstream Updates - No Local Changes

## Terminal Output

```
$ sync pull

Fetching from boilerplate remote...
✓ Fetched 3 new commits from upstream

Analyzing changes to managed files...

Files changed upstream:
  1. .claude/agents/security-guardian.md
     - Added SQL injection detection for raw queries
     - Modified: 2025-12-02

  2. .claude/agents/typescript-guardian.md
     - Added implicit any detection in destructuring
     - Modified: 2025-12-02

  3. .claude/agents/performance-guardian.md
     - Added N+1 query detection for ORMs
     - Modified: 2025-12-01

All files can be auto-merged (none are in your customized list).

Apply these changes? [y/n/review]
> y

Creating backup: .claude/agents/.backup-2025-12-03T14-30-00/
✓ Backed up 3 files

Merging changes...
✓ .claude/agents/security-guardian.md
✓ .claude/agents/typescript-guardian.md
✓ .claude/agents/performance-guardian.md

Updating .boilerplate.json...
✓ Updated last_sync: 2025-12-03T14:30:00Z

Committing changes...
✓ Committed: chore: sync boilerplate updates (3 files)

Successfully synced 3 files from upstream.

Next steps:
- Review changes: git show HEAD
- Test guardians: /review
- Rollback if needed: git reset --hard HEAD~1 && cp .claude/agents/.backup-2025-12-03T14-30-00/* .claude/agents/
```

## File Changes

### .boilerplate.json (updated)

```json
{
  "source": "git@github.com:you/claude-code-boilerplate.git",
  "last_sync": "2025-12-03T14:30:00Z",
  "managed_files": [
    ".claude/agents/*.md",
    ".claude/skills/**/*.md",
    "CLAUDE.md"
  ],
  "customized": []
}
```

### Git Commit

```
commit mno345
Author: Claude <noreply@anthropic.com>
Date: 2025-12-03 14:30:00

    chore: sync boilerplate updates (3 files)

    Synced from upstream boilerplate (jkl012)

    Files updated:
    - .claude/agents/security-guardian.md
    - .claude/agents/typescript-guardian.md
    - .claude/agents/performance-guardian.md
```

### Backup Directory Created

```
.claude/agents/.backup-2025-12-03T14-30-00/
├── security-guardian.md (previous version)
├── typescript-guardian.md (previous version)
└── performance-guardian.md (previous version)
```

## Behavior Validation

- ✓ Clean pull without conflicts
- ✓ Automatic merge applied (no manual intervention)
- ✓ Backup created for rollback safety
- ✓ Metadata updated accurately
- ✓ Clear, actionable output
- ✓ Rollback instructions provided
