# Scenario: Upstream Updates with Local Changes - No Conflict

## Context

**Git State:**
- Current branch: `main`
- Local HEAD: `abc123` (clean working directory)
- No uncommitted changes
- 1 local commit ahead of last sync (not yet pushed to upstream)

**Remotes:**
- `origin`: user's fork (synced)
- `boilerplate`: upstream source (configured)

**Upstream State:**
- Upstream has 2 new commits since last sync:
  - `def456`: "Update security-guardian.md: add OWASP API Security Top 10"
  - `ghi789`: "Update typescript-guardian.md: add generic constraint checking"

**Files Changed Upstream:**
- `.claude/agents/security-guardian.md` (modified)
- `.claude/agents/typescript-guardian.md` (modified)

**Local State:**
- Local commit `xyz999`: "Update accessibility-guardian.md: add color contrast checking"
- File changed locally: `.claude/agents/accessibility-guardian.md`
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

**Key Detail:**
- No file overlap: upstream changed security & typescript, local changed accessibility
- No merge conflicts possible (different files)

## Input

User runs:
```
sync pull
```

## Expected Behavior

The skill should:

1. **Fetch upstream** without auto-merging
2. **Detect changes** in 2 upstream files + 1 local file
3. **Recognize no conflicts** (different files modified)
4. **Show summary** of both upstream and local changes
5. **Request confirmation**
6. **Merge upstream changes** cleanly
7. **Preserve local changes** (accessibility-guardian.md)
8. **Update .boilerplate.json**
9. **Report success** showing both sets of changes

## Success Criteria

- [ ] Fetches from boilerplate remote successfully
- [ ] Detects 2 upstream changed files + 1 local changed file
- [ ] Identifies that files don't overlap (no conflict)
- [ ] Shows clear summary: "2 upstream changes, 1 local change (no conflicts)"
- [ ] Merges upstream files without touching local file
- [ ] Creates backup of pre-merge state
- [ ] Updates .boilerplate.json last_sync
- [ ] Git history shows both upstream and local commits
- [ ] Reports: "Successfully synced 2 files. Your local changes preserved."
- [ ] No merge conflicts or errors
