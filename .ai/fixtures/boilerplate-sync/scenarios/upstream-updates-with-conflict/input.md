# Scenario: Upstream Updates with Conflict

## Context

**Git State:**
- Current branch: `main`
- Local HEAD: `abc123` (clean working directory)
- No uncommitted changes
- 1 local commit ahead of last sync

**Remotes:**
- `origin`: user's fork (synced)
- `boilerplate`: upstream source (configured)

**Upstream State:**
- Upstream has 1 new commit since last sync:
  - `def456`: "Update security-guardian.md: add JWT validation check"

**Files Changed Upstream:**
- `.claude/agents/security-guardian.md` (modified)
  - Added JWT validation section at line 45

**Local State:**
- Local commit `xyz999`: "Update security-guardian.md: add API key rotation check"
- File changed locally: `.claude/agents/security-guardian.md`
  - Added API key rotation section at line 45 (same location)
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

**Conflict Details:**
Both upstream and local modified the same file at overlapping locations:
- **Upstream**: Added JWT validation check after "Authentication/authorization flaws"
- **Local**: Added API key rotation check at same position

## Input

User runs:
```
sync pull
```

## Expected Behavior

The skill should:

1. **Fetch upstream** without auto-merging
2. **Detect conflict** in security-guardian.md
3. **STOP automatic merge** (critical: don't auto-resolve)
4. **Show side-by-side diff** of conflicting sections
5. **Present resolution options:**
   - Take upstream version
   - Keep local version
   - Merge manually (open editor)
   - Mark as customized (skip this sync, keep local)
6. **Wait for user decision**
7. **Apply chosen resolution**
8. **Update .boilerplate.json** accordingly
9. If marked customized: add to customized[] array

## Success Criteria

- [ ] Fetches from boilerplate remote successfully
- [ ] Detects merge conflict in security-guardian.md
- [ ] DOES NOT auto-resolve or auto-merge
- [ ] Shows clear diff of both versions
- [ ] Presents all resolution options
- [ ] Waits for explicit user choice
- [ ] If "take upstream": applies upstream version, updates last_sync
- [ ] If "keep local": preserves local version, updates last_sync, asks about customized flag
- [ ] If "merge manually": opens editor or shows merge markers
- [ ] If "mark customized": adds to customized[] array, preserves local version
- [ ] Reports conflict resolution method used
- [ ] No data loss occurs
