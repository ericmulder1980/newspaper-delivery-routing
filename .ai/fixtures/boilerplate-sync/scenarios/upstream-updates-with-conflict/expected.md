# Expected Output: Upstream Updates with Conflict

## Terminal Output

```
$ sync pull

Fetching from boilerplate remote...
✓ Fetched 1 new commit from upstream

Analyzing changes to managed files...

⚠️  CONFLICT DETECTED

File: .claude/agents/security-guardian.md
Both local and upstream modified this file in overlapping areas.

┌─ Upstream version (def456) ─────────────────────────────────┐
│ ## Focus Areas                                               │
│ - OWASP Top 10 vulnerabilities                               │
│ - Secrets/credentials in code                                │
│ - SQL/NoSQL injection                                        │
│ - XSS and CSRF                                               │
│ - Authentication/authorization flaws                         │
│ - JWT validation and token expiry                            │ ← Added
│ - Tenant isolation (multi-tenant apps)                       │
└──────────────────────────────────────────────────────────────┘

┌─ Local version (xyz999) ────────────────────────────────────┐
│ ## Focus Areas                                               │
│ - OWASP Top 10 vulnerabilities                               │
│ - Secrets/credentials in code                                │
│ - SQL/NoSQL injection                                        │
│ - XSS and CSRF                                               │
│ - Authentication/authorization flaws                         │
│ - API key rotation and secure storage                        │ ← Added
│ - Tenant isolation (multi-tenant apps)                       │
└──────────────────────────────────────────────────────────────┘

Resolution options:
  1. Take upstream - Replace local changes with upstream version
  2. Keep local - Preserve your local changes, ignore upstream for this file
  3. Merge manually - Open editor to combine both changes
  4. Mark as customized - Keep local, skip future auto-merges for this file

Your choice [1/2/3/4]:
> 3

Opening merge editor...
(Shows both versions with merge markers)

<<<<<<< LOCAL (xyz999)
- API key rotation and secure storage
=======
- JWT validation and token expiry
>>>>>>> UPSTREAM (def456)

Merge completed? [y/n/cancel]
> y

Final merged version:
- Authentication/authorization flaws
- JWT validation and token expiry (from upstream)
- API key rotation and secure storage (from local)
- Tenant isolation (multi-tenant apps)

Looks correct? [y/n]
> y

Creating backup: .claude/agents/.backup-2025-12-03T15-30-00/
✓ Backed up pre-merge state

Applying manual merge...
✓ .claude/agents/security-guardian.md (manually merged)

Updating .boilerplate.json...
✓ Updated last_sync: 2025-12-03T15:30:00Z

Committing changes...
✓ Committed: chore: sync boilerplate with manual merge (security-guardian)

Successfully synced 1 file from upstream (manual merge applied).

Conflict resolution logged to:
.ai/feedback/process/boilerplate-sync.md

Next steps:
- Review merged file: cat .claude/agents/security-guardian.md
- Test security guardian: /review
- If issues: git reset --hard HEAD~1 && cp .claude/agents/.backup-2025-12-03T15-30-00/* .claude/agents/
```

## Alternative: User Chooses "Mark as Customized"

```
Your choice [1/2/3/4]:
> 4

Marking .claude/agents/security-guardian.md as customized.
This file will require manual review for future upstream syncs.

Updating .boilerplate.json...
✓ Added to customized array

File preserved with local changes.
Upstream version saved to: .ai/upstream-diffs/2025-12-03-security-guardian.diff

You can review the upstream changes anytime:
cat .ai/upstream-diffs/2025-12-03-security-guardian.diff

Successfully synced 0 files (1 skipped as customized).
```

## File Changes (Manual Merge Option)

### .boilerplate.json (manual merge)

```json
{
  "source": "git@github.com:you/claude-code-boilerplate.git",
  "last_sync": "2025-12-03T15:30:00Z",
  "managed_files": [
    ".claude/agents/*.md",
    ".claude/skills/**/*.md",
    "CLAUDE.md"
  ],
  "customized": [],
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

## File Changes (Mark as Customized Option)

### .boilerplate.json (customized)

```json
{
  "source": "git@github.com:you/claude-code-boilerplate.git",
  "last_sync": "2025-12-03T15:30:00Z",
  "managed_files": [
    ".claude/agents/*.md",
    ".claude/skills/**/*.md",
    "CLAUDE.md"
  ],
  "customized": [
    ".claude/agents/security-guardian.md"
  ]
}
```

## Behavior Validation

- ✓ Conflict detected and reported clearly
- ✓ NO automatic resolution attempted
- ✓ Side-by-side diff shown
- ✓ All resolution options presented
- ✓ User choice required and respected
- ✓ Manual merge supported with validation
- ✓ Customization flag works correctly
- ✓ Backup created before any changes
- ✓ Conflict resolution logged for feedback
- ✓ No data loss in any scenario
