# Expected Behavior: This IS the Source Repo

## Expected Behavior

The boilerplate-sync skill should detect when the current repo IS the source and provide appropriate guidance.

### 1. Detection Phase

- [ ] Read `.boilerplate.json` → get source repo
- [ ] Run `git remote -v` → get origin URL
- [ ] Compare source repo with origin
- [ ] Detect match: this IS the source repo

### 2. Report Phase

- [ ] Display clear message that this is the source repo
- [ ] Explain why sync doesn't apply
- [ ] Provide guidance on what to do instead

**Expected Output:**

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️  This IS the boilerplate source repository
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Sync is designed for projects that USE the boilerplate, not the source itself.

In this repo, you can:
- Edit boilerplate files directly (they become the canonical version)
- Push changes to origin (other projects sync FROM here)
- Review incoming PRs from projects using `sync push`

Commands that work here:
- `sync status --instances` — See which projects use this boilerplate
- Direct git operations — Edit, commit, push as normal

Commands that don't apply:
- `sync pull` — You ARE the upstream
- `sync push` — Your changes ARE the upstream

For testing sync, create a separate project:
  mkdir test-project && cd test-project
  git init
  # Copy boilerplate structure
  # Update .boilerplate.json to point here
  # Then test sync pull
```

### 3. No Harmful Actions

- [ ] Do NOT attempt to sync
- [ ] Do NOT create unnecessary backups
- [ ] Do NOT modify any files
- [ ] Do NOT add unnecessary remotes

## Success Criteria

**PASS if:**
- Source repo correctly detected
- Clear, helpful message displayed
- No sync operations attempted
- Alternative guidance provided

**FAIL if:**
- Attempts to sync repo with itself
- Creates confusing errors
- Modifies files unnecessarily
- Doesn't detect the edge case

## Related Scenarios

- First-time setup with no .boilerplate.json
- Source repo URL doesn't match (different fork)
- Multiple remotes configured
