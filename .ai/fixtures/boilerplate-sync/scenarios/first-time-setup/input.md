# Scenario: First-Time Setup

## Context

**Git State:**
- Current branch: `main`
- Local HEAD: `abc123`
- Clean working directory
- Project initialized from boilerplate but never synced

**Remotes:**
- `origin`: user's project repo (exists)
- `boilerplate`: **NOT CONFIGURED** (this is the key)

**Local State:**
- `.boilerplate.json` exists but minimal:
  ```json
  {
    "source": "git@github.com:you/claude-code-boilerplate.git",
    "managed_files": [
      ".claude/agents/*.md",
      ".claude/skills/**/*.md",
      "CLAUDE.md"
    ],
    "customized": []
  }
  ```
  - Note: No `last_sync` field (never synced)
  - No `last_sync_commit` field

**User Intent:**
- User wants to set up sync for the first time
- May have made local changes since initial copy
- Needs to establish baseline sync relationship

**Upstream State:**
- Upstream repo exists and is accessible
- Has commits since project was initialized

## Input

User runs:
```
sync pull
```

Or explicitly:
```
sync setup
```

## Expected Behavior

The skill should:

1. **Detect first-time setup** (no last_sync in .boilerplate.json or no boilerplate remote)
2. **Validate source repo** from .boilerplate.json
3. **Add remote** if missing: `git remote add boilerplate <source>`
4. **Fetch from remote** for first time
5. **Detect current state** of managed files
6. **Ask user** how to establish baseline:
   - Option A: "Treat current state as customized" (safe for existing projects)
   - Option B: "Sync from upstream" (fresh start, may overwrite)
   - Option C: "Show differences first" (review before deciding)
7. **Execute chosen option**
8. **Set last_sync** timestamp and commit
9. **Report success** and next steps

## Success Criteria

- [ ] Detects missing boilerplate remote or missing last_sync
- [ ] Reads source from .boilerplate.json
- [ ] Validates source repo is accessible (git ls-remote)
- [ ] Adds boilerplate remote if missing
- [ ] Fetches from remote successfully
- [ ] Presents clear setup options
- [ ] If "treat as customized": adds all managed files to customized[]
- [ ] If "sync from upstream": performs clean pull (may overwrite local)
- [ ] If "show differences": displays diff, then asks for option A or B
- [ ] Sets last_sync timestamp after setup
- [ ] Sets last_sync_commit to upstream HEAD
- [ ] Reports setup complete with guidance
- [ ] Explains what happens on future sync pull commands
