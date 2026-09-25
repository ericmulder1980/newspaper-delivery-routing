---
name: full-codebase-review
description: Comprehensive review of entire codebase before merging develop to main, or for periodic health checks
---

# Full Codebase Review

Holistic review extending guardian-agents with cross-cutting concerns.

## Overview

Unlike feature reviews (scoped to changed files), this reviews the entire codebase. Use before major releases or for periodic health checks.

## When to Use

- Before merging develop → main
- Scheduled weekly/monthly health check
- Major release preparation
- After significant refactoring
- After multiple features merged (integration check)

## Extended Checks

Beyond standard guardians, full codebase review includes:

### Integration Guardian (Ad-hoc)

Not a separate agent, but additional checks:

```
[ ] Cross-feature compatibility
    - Features merged this cycle work together
    - No conflicting assumptions

[ ] Shared state consistency
    - Global state managed properly
    - No race conditions between features

[ ] Event/messaging coherence
    - Event names consistent
    - Subscribers handle all cases
```

### Coverage Aggregate

```
[ ] Overall test coverage percentage
    - Run: pnpm test --coverage
    - Target: [project-defined, e.g., 80%]

[ ] Coverage gaps by module
    - Identify undertested areas
    - Prioritize critical paths

[ ] Critical paths covered
    - Authentication flows
    - Payment flows
    - Data mutations
```

### Dependency Audit

```
[ ] Outdated packages
    - Run: npm outdated / pnpm outdated
    - Flag major version bumps

[ ] Known vulnerabilities
    - Run: npm audit / pnpm audit
    - Address critical/high severity

[ ] Unused dependencies
    - Run: npx depcheck
    - Remove dead weight
```

### Architecture Drift

```
[ ] Pattern consistency across codebase
    - Are recent additions following patterns?
    - Any divergence from established conventions?

[ ] Coupling assessment
    - Module boundaries respected?
    - Dependency direction correct?

[ ] Naming convention consistency
    - File naming
    - Function/variable naming
    - Component naming
```

## Execution

### Step 1: Run All Guardians on Full Codebase

```
For each guardian in enabled_guardians:
  Task(guardian, "Review all files in src/")
```

This is slower than feature review but comprehensive.

### Step 2: Run Extended Checks

Execute integration, coverage, dependency, and architecture checks.

### Step 3: Generate Health Report

```markdown
# Full Codebase Review: YYYY-MM-DD

## Guardian Findings
| Guardian | Critical | High | Medium | Low |
|----------|----------|------|--------|-----|
| security | 0 | 1 | 3 | 5 |
| ... | | | | |

## Extended Checks

### Integration
[Findings]

### Coverage
- Overall: X%
- Gaps: [list]

### Dependencies
- Outdated: X packages
- Vulnerabilities: Y (Z critical)

### Architecture
[Assessment]

## Recommendations
1. [Priority actions]

## Health Score
[Overall assessment: Healthy / Needs Attention / Critical]
```

### Step 4: External Review

For develop → main merges:
```
[ ] Create comprehensive Codex review task
[ ] Include full health report
[ ] Request sign-off
```

### Step 5: Gate

For develop → main:
- Zero CRITICAL issues
- Explicit sign-off on any HIGH issues
- Documented acceptance of known issues

## Output

Save report to: `.ai/reviews/YYYY-MM-DD-full-codebase-review.md`

## Self-Annealing

After each full review:
- What drifted since last review?
- What checks would have caught issues earlier?
- Adjust guardian sensitivity if needed

Log to `.ai/feedback/process/full-codebase-review.md`
