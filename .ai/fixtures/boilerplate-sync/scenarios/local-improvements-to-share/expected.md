# Expected Output: Local Improvements to Share Upstream

## Terminal Output

```
$ sync push

Scanning for upstream candidates...
Found 2 pending candidates in .ai/feedback/upstream-candidates/

Validating generality...
✓ typescript-implicit-any-arrays: General (applies to all TS projects)
✓ test-edge-cases: General (fundamental testing practice)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Candidate 1/2
File: 2025-12-01-typescript-implicit-any-arrays.md
Type: guardian-improvement
Target: typescript-guardian.md
Summary: Catch implicit `any` in array method callbacks
Evidence: Found 2 bugs in user service

This will create a PR to upstream boilerplate.
Include this candidate? [y/n/skip]
> y

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Candidate 2/2
File: 2025-12-02-test-edge-cases.md
Type: guardian-improvement
Target: test-guardian.md
Summary: Detect missing edge case tests
Evidence: 3 production bugs from untested edge cases

This will create a PR to upstream boilerplate.
Include this candidate? [y/n/skip]
> y

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Creating pull requests...

PR 1: typescript-implicit-any-arrays
✓ Created branch: improve/typescript-implicit-any-arrays
✓ Applied changes to .claude/agents/typescript-guardian.md
✓ Committed changes
✓ Pushed to fork
✓ Created PR: https://github.com/you/claude-code-boilerplate/pull/42

PR 2: test-edge-cases
✓ Created branch: improve/test-edge-cases
✓ Applied changes to .claude/agents/test-guardian.md
✓ Committed changes
✓ Pushed to fork
✓ Created PR: https://github.com/you/claude-code-boilerplate/pull/43

Moving candidates to submitted/...
✓ Moved 2025-12-01-typescript-implicit-any-arrays.md → submitted/
✓ Moved 2025-12-02-test-edge-cases.md → submitted/

Updating .boilerplate.json...
✓ Tracked 2 new upstream candidates

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Successfully submitted 2 improvements to upstream!

Pull requests created:
  1. #42: Improve TypeScript guardian - implicit any in array methods
     https://github.com/you/claude-code-boilerplate/pull/42

  2. #43: Add test guardian - edge case detection
     https://github.com/you/claude-code-boilerplate/pull/43

Next steps:
  - PRs are awaiting review
  - Check status: sync status
  - Monitor: gh pr list
  - Once merged, they'll be available in next sync pull
```

## File Changes

### .boilerplate.json (updated)

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
  "upstream_candidates": [
    {
      "file": "2025-12-01-typescript-implicit-any-arrays.md",
      "pr_number": 42,
      "pr_url": "https://github.com/you/claude-code-boilerplate/pull/42",
      "status": "submitted",
      "submitted_date": "2025-12-03T16:00:00Z"
    },
    {
      "file": "2025-12-02-test-edge-cases.md",
      "pr_number": 43,
      "pr_url": "https://github.com/you/claude-code-boilerplate/pull/43",
      "status": "submitted",
      "submitted_date": "2025-12-03T16:00:00Z"
    }
  ]
}
```

### Directory Changes

```
.ai/feedback/upstream-candidates/
├── submitted/
│   ├── 2025-12-01-typescript-implicit-any-arrays.md (moved)
│   └── 2025-12-02-test-edge-cases.md (moved)
└── (empty - all pending moved to submitted/)
```

## GitHub Pull Requests Created

### PR #42

```
Title: Improve TypeScript guardian: detect implicit any in array methods

Description:
## Summary
Catch implicit `any` in array method callbacks (.map, .filter, .reduce, etc.)

## Evidence
Found 2 bugs in user service where `.map(x => x.id)` failed type checking because `x` had implicit `any`.

## Changes
- Added check for implicit any in array method callbacks
- Updated test fixtures with failing examples
- Documented new detection in guardian prompt

## Test Cases
- ✓ `users.map(u => u.id)` where u has no type → flagged
- ✓ `users.map((u: User) => u.id)` → passed

## Generality
High - applies to all TypeScript projects using array methods.

🤖 Generated from local boilerplate instance
Source: .ai/feedback/upstream-candidates/2025-12-01-typescript-implicit-any-arrays.md
```

### PR #43

```
Title: Add test guardian: edge case detection

Description:
## Summary
Detect missing edge case tests for null, undefined, empty values, and boundaries.

## Evidence
3 production bugs traced to untested null/undefined/empty scenarios.

## Changes
- Added edge case detection to test guardian
- Check for null/undefined handling tests
- Check for empty array/string tests
- Check for boundary value tests (0, -1, max)

## Test Cases
Functions tested only with happy path should be flagged.

## Generality
High - fundamental testing practice applicable to all projects.

🤖 Generated from local boilerplate instance
Source: .ai/feedback/upstream-candidates/2025-12-02-test-edge-cases.md
```

## Behavior Validation

- ✓ Candidates scanned and found
- ✓ Generality validated (project-specific would be rejected)
- ✓ Clear summary and confirmation requested
- ✓ PRs created with descriptive titles and bodies
- ✓ Evidence included in PR descriptions
- ✓ Candidates moved to submitted/
- ✓ Tracking added to .boilerplate.json
- ✓ PR URLs reported for easy access
- ✓ Next steps guidance provided
- ✓ User can monitor PR status via sync status
