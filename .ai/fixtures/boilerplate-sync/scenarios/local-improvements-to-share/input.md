# Scenario: Local Improvements to Share Upstream

## Context

**Git State:**
- Current branch: `main`
- Local HEAD: `abc123` (clean working directory)
- No uncommitted changes
- 2 local commits ahead of upstream

**Remotes:**
- `origin`: user's fork (synced with local)
- `boilerplate`: upstream source (configured and authenticated)

**Local State:**
- Local commits not in upstream:
  - `xyz888`: "Improve typescript-guardian: detect implicit any in array methods"
  - `xyz999`: "Add test-guardian: check for missing edge case tests"

- Upstream candidates ready to share:
  - `.ai/feedback/upstream-candidates/2025-12-01-typescript-implicit-any-arrays.md`
  - `.ai/feedback/upstream-candidates/2025-12-02-test-edge-cases.md`

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
    "customized": [],
    "upstream_candidates": []
  }
  ```

**Candidate Files:**

`.ai/feedback/upstream-candidates/2025-12-01-typescript-implicit-any-arrays.md`:
```markdown
Type: guardian-improvement
Target: .claude/agents/typescript-guardian.md
Summary: Catch implicit `any` in array method callbacks
Evidence: Found 2 bugs in user service where .map(x => x.id) failed type checking
Status: pending
Generality: High (applies to all TypeScript projects)

## Current Behavior
Guardian checks explicit `any` usage but misses implicit `any` in:
- Array.map callbacks
- Array.filter callbacks
- Array.reduce callbacks

## Proposed Change
Add check for implicit any in array method callbacks.

## Test Cases
- `users.map(u => u.id)` where u has no type → should flag
- `users.map((u: User) => u.id)` → should pass
```

`.ai/feedback/upstream-candidates/2025-12-02-test-edge-cases.md`:
```markdown
Type: guardian-improvement
Target: .claude/agents/test-guardian.md
Summary: Detect missing edge case tests
Evidence: 3 production bugs from untested null/undefined/empty scenarios
Status: pending
Generality: High (fundamental testing practice)

## Current Behavior
Guardian checks test coverage % but doesn't flag missing edge cases.

## Proposed Change
Check for common edge cases in test suites:
- null/undefined handling
- empty arrays/strings
- boundary values (0, -1, max)

## Test Cases
Should flag functions tested only with happy path.
```

**GitHub CLI:**
- `gh` is installed and authenticated
- User has write access to upstream repo

## Input

User runs:
```
sync push
```

## Expected Behavior

The skill should:

1. **Scan .ai/feedback/upstream-candidates/** for pending items
2. **Validate each candidate** for generality (not project-specific)
3. **Show summary** of candidates to push
4. **Ask for confirmation** per candidate or batch
5. **Create GitHub PR** for each approved candidate
6. **Move candidates** to `.ai/feedback/upstream-candidates/submitted/`
7. **Track in .boilerplate.json** upstream_candidates array
8. **Report PR URLs** and next steps

## Success Criteria

- [ ] Scans upstream-candidates directory successfully
- [ ] Finds 2 pending candidates
- [ ] Validates both as general (not project-specific)
- [ ] Shows clear summary of each candidate
- [ ] Asks for confirmation before creating PRs
- [ ] Creates PR for typescript-implicit-any-arrays with proper title/description
- [ ] Creates PR for test-edge-cases with proper title/description
- [ ] Moves both files to upstream-candidates/submitted/
- [ ] Updates .boilerplate.json upstream_candidates array with PR numbers
- [ ] Reports both PR URLs
- [ ] Suggests next steps (wait for review, monitor status)
