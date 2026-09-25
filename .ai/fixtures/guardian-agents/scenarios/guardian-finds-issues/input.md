# Scenario: Guardian Finds Issues

## Context

Git status shows TypeScript and API changes:

```
Modified files:
  src/api/users/create.ts
  src/lib/auth.ts
```

Settings configuration:
```json
{
  "guardians": {
    "enabled": ["security", "typescript", "api"],
    "gates": {
      "require_zero_issues": true,
      "allow_override": true
    },
    "file_detection": {
      "*.ts": ["typescript"],
      "api/**": ["api", "security"]
    }
  }
}
```

## Input

User completes feature and triggers review via feature-completion workflow.

Changed files:
- `src/api/users/create.ts` (API endpoint for user creation)
- `src/lib/auth.ts` (Authentication utility)

## Simulated Guardian Findings

**Security Guardian:**
```markdown
CRITICAL: SQL injection vulnerability at src/api/users/create.ts:42
- Direct string interpolation in SQL query
- Recommendation: Use parameterized queries

HIGH: Hardcoded JWT secret at src/lib/auth.ts:15
- Secret should be in environment variable
- Recommendation: Use process.env.JWT_SECRET
```

**TypeScript Guardian:**
```markdown
MEDIUM: Use of 'any' type at src/api/users/create.ts:28
- Function parameter lacks type annotation
- Recommendation: Define UserCreateParams interface

LOW: Missing return type at src/lib/auth.ts:45
- Function should explicitly declare return type
- Recommendation: Add ': Promise<AuthToken>'
```

**API Guardian:**
```markdown
MEDIUM: Missing input validation at src/api/users/create.ts:35
- Email format not validated before database insert
- Recommendation: Add email validation middleware
```

## Expected Trigger

Guardian-agents skill should:
1. Spawn security, typescript, and api guardians
2. Receive findings from all guardians
3. Synthesize and categorize findings by severity
4. Block merge due to CRITICAL and HIGH issues
5. Offer override option
6. Suggest fix triage (isolated vs coupled)
