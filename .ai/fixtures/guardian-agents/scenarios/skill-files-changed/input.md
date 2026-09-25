# Scenario: Skill Files Changed (Edge Case)

## Context

Git status shows skill file modifications:

```
Modified files:
  .claude/skills/guardian-agents/SKILL.md
  .claude/skills/session-management/SKILL.md
  .claude/skills/boilerplate-sync/SKILL.md
```

Settings configuration:
```json
{
  "guardians": {
    "enabled": ["security", "typescript", "architecture", "test", "performance", "docs", "accessibility", "api", "database"],
    "file_detection": {
      "*.md": ["docs"],
      ".claude/skills/**/*.md": ["docs", "architecture"]
    }
  }
}
```

## Input

User triggers review via `/review` command.

Changed files:
- `.claude/skills/guardian-agents/SKILL.md`
- `.claude/skills/session-management/SKILL.md`
- `.claude/skills/boilerplate-sync/SKILL.md`

## Expected Trigger

Guardian-agents skill should:
1. Recognize these are skill definition files (not just regular markdown)
2. Trigger BOTH `docs` and `architecture` guardians
3. Docs guardian checks documentation quality
4. Architecture guardian checks:
   - Skill structure consistency
   - Proper integration with other skills
   - No circular dependencies between skills
   - Alignment with overall system architecture

## Why This Edge Case Matters

Skill files define Claude's behavior. Changes to skills can:
- Break workflows
- Introduce inconsistencies
- Create conflicts between skills
- Change system architecture

Regular `*.md` → `docs` pattern misses this. Need special pattern for skill files.
