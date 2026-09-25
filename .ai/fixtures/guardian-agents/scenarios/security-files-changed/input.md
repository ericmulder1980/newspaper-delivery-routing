# Scenario: Security Files Changed

## Context

Git status shows security-sensitive file modifications:

```
Modified files:
  src/auth/middleware.ts
  src/api/users/route.ts
  src/api/auth/login.ts
```

Settings configuration:
```json
{
  "guardians": {
    "enabled": ["security", "typescript", "architecture", "test", "performance", "docs", "accessibility", "api", "database"],
    "file_detection": {
      "*.ts": ["typescript"],
      "auth/**": ["security"],
      "api/**": ["api", "security"]
    }
  }
}
```

## Input

User triggers review via feature completion workflow.

Changed files:
- `src/auth/middleware.ts` (matches both `*.ts` and `auth/**`)
- `src/api/users/route.ts` (matches both `*.ts` and `api/**`)
- `src/api/auth/login.ts` (matches `*.ts`, `api/**`, and `auth/**`)

## Expected Trigger

Guardian-agents skill should:
1. Detect overlapping patterns for security-critical files
2. Identify applicable guardians from all matching patterns
3. Deduplicate and spawn: `typescript`, `security`, `api`
4. Prioritize security review for auth-related changes
5. Coordinate with external reviewers (Codex) for security changes
