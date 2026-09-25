---
name: guardian-agents
description: Orchestrates parallel guardian agents for code review, synthesizes results, coordinates with external reviewers (Codex/Gemini)
---

# Guardian Agents Orchestration

Main skill for running code reviews with specialized guardian agents.

## Overview

Spawns applicable guardians in parallel based on changed files. Coordinates with external reviewers. Synthesizes findings. Gates merge on zero issues (or explicit override).

## When to Use

- Pre-commit review
- PR creation review
- Manual `/review` command
- Feature completion workflow

## Execution Flow

### 1. Get Changed Files

```bash
# For uncommitted changes
git status --porcelain | grep -E '^[MARCDU]' | awk '{print $2}'

# For staged changes only
git diff --cached --name-only

# For PR review (vs base branch)
git diff --name-only origin/develop...HEAD
```

Store result as `changed_files` list.

### 2. Detect Applicable Guardians

Read `.claude/settings.json` → `guardians.file_detection`

For each changed file, match against patterns:

```
Pattern Matching:
  *.ts          → typescript
  *.tsx         → typescript, accessibility
  *.sql         → database
  api/**        → api, security
  auth/**       → security
  migrations/** → database
  *.test.*      → test
  *.md          → docs
  docs/**       → docs
```

Build `guardian_map`:
```json
{
  "typescript": ["src/lib/utils.ts", "src/components/Button.tsx"],
  "accessibility": ["src/components/Button.tsx"],
  "security": ["src/api/auth/login.ts", "src/auth/middleware.ts"]
}
```

**Deduplication:** Each guardian appears once with all relevant files.

### 3. Report Plan

Before spawning, report what will run:

```markdown
## Guardian Review Plan

**Changed Files:** 4
**Guardians to Run:** typescript, accessibility, security (3 of 9)
**Guardians Skipped:** architecture, test, performance, docs, api, database (not applicable)

Proceeding with parallel review...
```

### 4. Parallel Dispatch (Quick Swarm)

Launch applicable guardians via Task tool:

```
For each (guardian, files) in guardian_map:
  Task(
    subagent_type=guardian,
    prompt="Review these files for [guardian domain]: [files list]"
  )
```

**Guardian Locations (new domains structure):**
- `.claude/agents/domains/security/guardian.md`
- `.claude/agents/domains/database/guardian.md`
- `.claude/agents/domains/architecture/guardian.md`
- `.claude/agents/domains/api/guardian.md`
- `.claude/agents/domains/typescript/guardian.md`
- `.claude/agents/domains/test/guardian.md`
- `.claude/agents/domains/performance/guardian.md`
- `.claude/agents/domains/accessibility/guardian.md`
- `.claude/agents/domains/docs/guardian.md`

**Important:** Each guardian receives ONLY files relevant to its domain.
- `accessibility-guardian` gets `.tsx` files only
- `database-guardian` gets `.sql` and migration files only
- etc.

### 5. External Review Coordination

**For security-sensitive changes (auth/**, api/**):**

Create Codex task file: `.ai/tasks/review/YYMMDD-codex-[feature].md`

```markdown
# Codex Review Task: [Feature Name]

**Type:** security-sensitive-review
**Branch:** [branch-name]
**Created:** YYYY-MM-DD

## Files to Review
- src/auth/middleware.ts
- src/api/users/route.ts

## Focus Areas
- Authentication flows
- Authorization checks
- Tenant isolation
- OWASP Top 10

## Deliverables
Save findings to: .ai/tasks/review/YYMMDD-codex-[feature]-findings.md
```

Notify: "Branch ready for Codex review. Waiting for external review before synthesis."

### 6. Synthesis

Wait for:
- All guardians to complete
- Codex review (if applicable) — this is the sync point

Aggregate findings with deduplication:

```markdown
## Review Synthesis

### Guardians Run
- typescript: 2 files → 1 issue
- accessibility: 1 file → 0 issues
- security: 2 files → 2 issues

### External Review
- Codex: Completed → 1 additional finding

### Combined Findings (Deduplicated)

| Severity | Count | Guardian | File:Line | Issue |
|----------|-------|----------|-----------|-------|
| CRITICAL | 1 | Security | src/api/users.ts:42 | SQL injection |
| HIGH | 1 | Security | src/auth/login.ts:15 | Hardcoded secret |
| MEDIUM | 1 | TypeScript | src/lib/utils.ts:28 | 'any' type usage |
| LOW | 0 | — | — | — |

**Total Issues:** 3
**Blocking Issues:** 2 (CRITICAL + HIGH)
```

**Deduplication:** If same issue reported by multiple guardians, keep one entry and note "(also found by: X)".

### 7. Gate Check

Default: require zero CRITICAL and HIGH issues.

```
If CRITICAL > 0 or HIGH > 0:
  🚫 Merge Blocked

  Blocking issues:
  1. [CRITICAL] SQL injection (src/api/users.ts:42)
  2. [HIGH] Hardcoded secret (src/auth/login.ts:15)

  Options:
  1. Fix issues and re-run review
  2. Override gate (requires explicit approval)
```

**For CRITICAL security issues, require explicit confirmation:**

```
⚠️  WARNING: You are about to override CRITICAL security issues.
This is strongly discouraged. Proceed only with manual verification.

To confirm, type: OVERRIDE SECURITY GATE
```

Log all overrides to `.ai/feedback/process/overrides.md`:

```markdown
### YYYY-MM-DD HH:MM - Gate Override

**Feature:** [feature name]
**Branch:** [branch]
**Issues Overridden:**
- CRITICAL: SQL injection at src/api/users.ts:42
- HIGH: Hardcoded secret at src/auth/login.ts:15

**User Reason:** [provided reason]
**Risk Level:** HIGH
```

### 8. Fix Triage

For each finding, assess isolation:

```markdown
## Suggested Fix Approach

**Isolated Fixes (can run in parallel):**
- [ ] SQL injection fix (src/api/users.ts:42) → One function, no dependencies
- [ ] Missing return type (src/lib/utils.ts:45) → One function

**Coupled Fixes (handle sequentially):**
- [ ] Hardcoded secret (src/auth/login.ts:15) → Requires env setup, affects multiple files
- [ ] 'any' type (src/api/types.ts:28) → May need interface affecting other files

Recommendation: Fix isolated issues with parallel agents first, then handle coupled issues.
```

### 9. Re-run After Fixes

After user fixes issues:

```
1. Identify which guardians are affected by the fix
2. Re-run ONLY affected guardians (not all 9)
3. Synthesis: merge new results with previous passing results
4. Re-check gate
5. If new issues found, repeat cycle
6. If zero blocking issues, allow merge
```

Example:
```
Fixed: SQL injection in src/api/users.ts

Re-running: security (affected)
Skipping: typescript, accessibility (already passed, files unchanged)
```

## Configuration

In `.claude/settings.json`:

```json
{
  "guardians": {
    "enabled": ["security", "typescript", ...],
    "gates": {
      "require_zero_issues": true,
      "allow_override": true
    },
    "file_detection": {
      "*.ts": ["typescript"],
      "*.tsx": ["typescript", "accessibility"],
      "api/**": ["api", "security"],
      "auth/**": ["security"]
    }
  }
}
```

## Self-Annealing

After each review cycle, log:

1. **Guardian accuracy:**
   - False positives (issues that weren't real)
   - False negatives (issues guardians missed)
   - Log to `.ai/feedback/guardians/[name].md`

2. **Override patterns:**
   - Frequency of overrides
   - Common reasons
   - Whether overridden issues caused problems later

3. **File detection accuracy:**
   - Files that should have triggered a guardian but didn't
   - Guardians spawned for irrelevant files
