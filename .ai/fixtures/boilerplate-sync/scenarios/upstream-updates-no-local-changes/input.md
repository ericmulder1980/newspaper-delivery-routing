# Scenario: Upstream Updates - No Local Changes

## Context

**Git State:**
- Current branch: `main`
- Local HEAD: `abc123` (clean working directory)
- No uncommitted changes
- No local commits ahead of last sync

**Remotes:**
- `origin`: user's fork (synced)
- `boilerplate`: upstream source (configured)

**Upstream State:**
- Upstream has 3 new commits since last sync:
  - `def456`: "Improve security guardian: add SQL injection detection"
  - `ghi789`: "Update typescript guardian: catch implicit any in destructuring"
  - `jkl012`: "Add new performance guardian check for N+1 queries"

**Files Changed Upstream:**
- `.claude/agents/security-guardian.md` (modified)
- `.claude/agents/typescript-guardian.md` (modified)
- `.claude/agents/performance-guardian.md` (modified)

**Local State:**
- `.boilerplate.json`:
  ```json
  {
    "source": "git@github.com:you/claude-code-boilerplate.git",
    "last_sync": "2025-11-28T10:00:00Z",
    "managed_files": [
      ".claude/agents/*.md",
      ".claude/skills/**/*.md",
      "CLAUDE.md"
    ],
    "customized": []
  }
  ```
- No files in customized array
- All managed files match upstream as of last sync

## Input

User runs:
```
sync pull
```

## Expected Behavior

The skill should:

1. **Fetch upstream** without auto-merging
2. **Detect changes** in 3 guardian files
3. **Classify changes** as auto-mergeable (none are customized)
4. **Show summary** of what will change
5. **Request confirmation** before applying
6. **Auto-merge all files** (with backup)
7. **Update .boilerplate.json** with new last_sync timestamp
8. **Report success** with commit references and file summary

## Success Criteria

- [ ] Fetches from boilerplate remote successfully
- [ ] Detects all 3 changed files in managed_files
- [ ] Shows diff summary for each file
- [ ] None are flagged as needing manual review (not customized)
- [ ] Creates backup before merging (.claude/agents/.backup-TIMESTAMP/)
- [ ] Merges all 3 files cleanly
- [ ] Updates .boilerplate.json last_sync to latest upstream commit date
- [ ] Commits changes with message: "chore: sync boilerplate updates (3 files)"
- [ ] Reports: "Successfully synced 3 files from upstream"
- [ ] No conflicts or errors reported
