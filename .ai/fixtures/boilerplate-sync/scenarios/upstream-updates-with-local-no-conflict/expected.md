# Expected Output: Upstream Updates with Local Changes - No Conflict

## Terminal Output

```
$ sync pull

Fetching from boilerplate remote...
✓ Fetched 2 new commits from upstream

Analyzing changes to managed files...

Upstream changes:
  1. .claude/agents/security-guardian.md
     - Added OWASP API Security Top 10 checks
     - Modified: 2025-12-02

  2. .claude/agents/typescript-guardian.md
     - Added generic constraint checking
     - Modified: 2025-12-02

Local changes (will be preserved):
  1. .claude/agents/accessibility-guardian.md
     - Added color contrast checking (local commit xyz999)
     - Modified: 2025-12-01

No conflicts detected (different files modified).

Apply upstream changes? [y/n/review]
> y

Creating backup: .claude/agents/.backup-2025-12-03T15-00-00/
✓ Backed up current state

Merging changes...
✓ .claude/agents/security-guardian.md
✓ .claude/agents/typescript-guardian.md
✓ .claude/agents/accessibility-guardian.md (local, preserved)

Updating .boilerplate.json...
✓ Updated last_sync: 2025-12-03T15:00:00Z

Committing merge...
✓ Committed: chore: sync boilerplate updates (2 upstream, 1 local preserved)

Successfully synced 2 files from upstream.
Your local changes have been preserved (1 file).

Git log shows:
  - HEAD: Merge upstream changes
  - xyz999: Update accessibility-guardian.md (local)
  - ghi789: Update typescript-guardian.md (upstream)
  - def456: Update security-guardian.md (upstream)

Next steps:
- Review merged state: git log --oneline -5
- Test all guardians: /review
```

## File Changes

### .boilerplate.json (updated)

```json
{
  "source": "git@github.com:you/claude-code-boilerplate.git",
  "last_sync": "2025-12-03T15:00:00Z",
  "managed_files": [
    ".claude/agents/*.md",
    ".claude/skills/**/*.md",
    "CLAUDE.md"
  ],
  "customized": []
}
```

### Git History

```
* mno345 (HEAD -> main) chore: sync boilerplate updates (2 upstream, 1 local preserved)
* xyz999 Update accessibility-guardian.md: add color contrast checking
* ghi789 Update typescript-guardian.md: add generic constraint checking
* def456 Update security-guardian.md: add OWASP API Security Top 10
* abc123 (last common commit)
```

### All Files Present

```
.claude/agents/
├── security-guardian.md (upstream version, updated)
├── typescript-guardian.md (upstream version, updated)
├── accessibility-guardian.md (local version, preserved)
├── test-guardian.md (unchanged)
└── ... (other guardians, unchanged)
```

## Behavior Validation

- ✓ Non-conflicting changes merged cleanly
- ✓ Local changes fully preserved
- ✓ Upstream changes fully applied
- ✓ Clear communication about both change sets
- ✓ Metadata updated correctly
- ✓ Git history shows proper merge
- ✓ Backup created for safety
