# Scenario: No Relevant Files Changed

## Context

Git status shows only configuration and infrastructure file changes:

```
Modified files:
  .gitignore
  .prettierrc
  package.json
  tsconfig.json
  .env.example
```

Settings configuration:
```json
{
  "guardians": {
    "enabled": ["security", "typescript", "architecture", "test", "performance", "docs", "accessibility", "api", "database"],
    "file_detection": {
      "*.sql": ["database"],
      "*.ts": ["typescript"],
      "*.tsx": ["typescript", "accessibility"],
      "*.test.*": ["test"],
      "*.md": ["docs"],
      "api/**": ["api", "security"],
      "auth/**": ["security"]
    }
  }
}
```

## Input

User triggers review via pre-commit hook after updating project configuration.

Changed files:
- `.gitignore` (no matching pattern)
- `.prettierrc` (no matching pattern)
- `package.json` (no matching pattern)
- `tsconfig.json` (no matching pattern)
- `.env.example` (no matching pattern)

## Expected Trigger

Guardian-agents skill should:
1. Attempt to match changed files against `file_detection` patterns
2. Find zero matching patterns
3. Decide whether to skip review entirely OR run minimal architecture guardian
4. Handle gracefully without spawning unnecessary guardians
5. Provide clear feedback to user about why review was skipped/minimal
