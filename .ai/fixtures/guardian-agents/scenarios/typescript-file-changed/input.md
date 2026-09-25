# Scenario: TypeScript File Changed

## Context

Git status shows TypeScript file modifications only:

```
Modified files:
  src/lib/utils.ts
  src/components/Button.tsx
```

Settings configuration:
```json
{
  "guardians": {
    "enabled": ["security", "typescript", "architecture", "test", "performance", "docs", "accessibility", "api", "database"],
    "file_detection": {
      "*.ts": ["typescript"],
      "*.tsx": ["typescript", "accessibility"]
    }
  }
}
```

## Input

User triggers review via:
- Pre-commit hook, OR
- `/review` command, OR
- Feature completion workflow

Changed files:
- `src/lib/utils.ts` (TypeScript only)
- `src/components/Button.tsx` (TypeScript + TSX)

## Expected Trigger

Guardian-agents skill should:
1. Read `.claude/settings.json`
2. Match changed files against `file_detection` patterns
3. Identify applicable guardians: `typescript`, `accessibility` (for .tsx file)
4. Spawn only these guardians in parallel
5. Skip all other guardians (security, api, database, etc.)
