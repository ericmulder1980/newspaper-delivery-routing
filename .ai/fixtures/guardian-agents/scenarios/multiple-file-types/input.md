# Scenario: Multiple File Types Changed

## Context

Git status shows diverse file type changes across documentation, tests, and code:

```
Modified files:
  src/lib/database.ts
  src/lib/database.test.ts
  docs/api.md
  docs/architecture.md
  README.md
```

Settings configuration:
```json
{
  "guardians": {
    "enabled": ["security", "typescript", "architecture", "test", "performance", "docs", "accessibility", "api", "database"],
    "file_detection": {
      "*.ts": ["typescript"],
      "*.test.*": ["test"],
      "__tests__/**": ["test"],
      "*.md": ["docs"],
      "docs/**": ["docs"]
    }
  }
}
```

## Input

User triggers review via `/review` command after working on database feature with docs and tests.

Changed files:
- `src/lib/database.ts` (matches `*.ts`)
- `src/lib/database.test.ts` (matches both `*.ts` and `*.test.*`)
- `docs/api.md` (matches both `*.md` and `docs/**`)
- `docs/architecture.md` (matches both `*.md` and `docs/**`)
- `README.md` (matches `*.md`)

## Expected Trigger

Guardian-agents skill should:
1. Detect multiple file types requiring different guardians
2. Spawn guardians in parallel: `typescript`, `test`, `docs`
3. Handle pattern overlaps correctly (deduplicate)
4. Provide relevant file context to each guardian
5. Coordinate results from diverse guardian types
