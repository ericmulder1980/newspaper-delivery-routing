# Expected Behavior: Guardian Finds Issues

## Expected Behavior

The guardian-agents skill should properly aggregate findings, gate the merge, and guide the user through fix triage.

### 1. File Detection & Guardian Spawning

- [ ] Detect applicable guardians: `security`, `typescript`, `api`
- [ ] Spawn all 3 guardians in parallel
- [ ] Each receives appropriate file context

### 2. Findings Aggregation

- [ ] Receive all findings from guardians
- [ ] Parse and categorize by severity
- [ ] Create findings summary table:

```markdown
## Review Synthesis

### Combined Findings

| Severity | Count | Guardian | Issue |
|----------|-------|----------|-------|
| CRITICAL | 1 | Security | SQL injection at src/api/users/create.ts:42 |
| HIGH | 1 | Security | Hardcoded JWT secret at src/lib/auth.ts:15 |
| MEDIUM | 2 | TypeScript, API | 'any' type usage, Missing input validation |
| LOW | 1 | TypeScript | Missing return type annotation |

**Total Issues:** 5
**Blocking Issues:** 2 (CRITICAL + HIGH)
```

### 3. Gate Check

- [ ] Check gate configuration: `require_zero_issues: true`
- [ ] Detect CRITICAL + HIGH issues exist
- [ ] **BLOCK merge**
- [ ] Present clear blocking message:

```markdown
🚫 Merge Blocked

CRITICAL and HIGH severity issues must be resolved before merge.

Blocking issues:
1. [CRITICAL] SQL injection vulnerability (src/api/users/create.ts:42)
2. [HIGH] Hardcoded JWT secret (src/lib/auth.ts:15)

Options:
1. Fix issues and re-run guardians
2. Override gate (requires explicit approval) - NOT RECOMMENDED for CRITICAL security issues
```

### 4. Fix Triage Suggestion

- [ ] Analyze each finding for isolation
- [ ] Suggest parallel vs sequential fixes:

```markdown
## Suggested Fix Approach

**Isolated Fixes (can run in parallel):**
- [ ] SQL injection fix (src/api/users/create.ts:42) → Isolated to one function
- [ ] Missing return type (src/lib/auth.ts:45) → Isolated to one function
- [ ] Email validation (src/api/users/create.ts:35) → Isolated to one endpoint

**Coupled Fixes (require sequential handling):**
- [ ] Hardcoded JWT secret (src/lib/auth.ts:15) → Affects multiple files, requires environment setup
- [ ] 'any' type usage (src/api/users/create.ts:28) → May require interface definition affecting other files

Recommendation: Fix isolated issues first with parallel agents, then handle coupled issues.
```

### 5. Override Handling (if requested)

If user requests override:

- [ ] Require explicit confirmation
- [ ] Warn about CRITICAL security issues:
  ```
  ⚠️  WARNING: You are about to override a CRITICAL security issue (SQL injection).
  This is strongly discouraged. Proceed only if you have manual verification.

  Type 'OVERRIDE SECURITY GATE' to confirm:
  ```
- [ ] Log override to `.ai/feedback/process/overrides.md`:
  ```markdown
  ### YYYY-MM-DD HH:MM - Security Gate Override
  **Feature:** User creation API
  **Issues Overridden:**
  - CRITICAL: SQL injection at src/api/users/create.ts:42
  - HIGH: Hardcoded JWT secret at src/lib/auth.ts:15
  **Reason:** [User provided reason]
  **Risk:** HIGH - Security vulnerabilities in production code
  ```
- [ ] If override confirmed, allow merge but flag for review

### 6. Re-run After Fixes

If user fixes issues:

- [ ] Allow re-running affected guardians only (not all)
- [ ] If CRITICAL/HIGH issues resolved, re-check gate
- [ ] If new issues found, repeat cycle
- [ ] If zero blocking issues, allow merge

## Success Criteria

**PASS if:**
- All findings properly aggregated and categorized
- Merge correctly blocked due to CRITICAL/HIGH issues
- Clear guidance provided for fix triage
- Override process requires explicit confirmation for security issues
- Override logged to feedback system
- Re-run process re-checks only affected guardians

**FAIL if:**
- Merge allowed despite CRITICAL/HIGH issues
- Findings not properly categorized or presented
- Override allowed without confirmation
- No fix triage guidance provided
- Re-run requires running all guardians again

## Edge Cases to Handle

- [ ] Multiple CRITICAL issues across different guardians
- [ ] Same issue reported by multiple guardians (deduplicate)
- [ ] Issues resolved but new ones introduced in fix
- [ ] User overrides but doesn't provide reason
- [ ] Partial fix (some CRITICAL resolved, others remain)
