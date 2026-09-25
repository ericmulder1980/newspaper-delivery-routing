# Skill Test Fixtures

TDD fixtures for validating boilerplate skills.

## Structure

```
.ai/fixtures/
├── [skill-name]/
│   ├── scenarios/
│   │   ├── [scenario-name]/
│   │   │   ├── input.md      # Simulated context/input
│   │   │   └── expected.md   # Expected behavior/output
│   │   └── ...
│   ├── results.md            # Test run results
│   └── README.md             # Skill-specific test notes
└── README.md                 # This file
```

## TDD Process

### RED Phase
1. Create scenario with input.md and expected.md
2. Run skill against input
3. Verify it fails or behaves incorrectly (proves test is meaningful)

### GREEN Phase
4. Improve skill until it produces expected output
5. Verify all scenarios pass

### REFACTOR Phase
6. Optimize skill prompts
7. Add edge cases discovered during testing
8. Document known limitations

## Running Tests

Use the `skill-audit` skill or `/audit skills tdd [skill-name]` command.

## Priority Queue

From `.ai/skills/recommended-skills.json`:

1. **guardian-agents** (high) — Core orchestration
2. **boilerplate-sync** (high) — Merge conflict handling
3. **session-management** (medium) — Greeting edge cases

## Creating a New Fixture

1. Create directory: `.ai/fixtures/[skill-name]/scenarios/[scenario-name]/`
2. Write `input.md` — The context/input the skill receives
3. Write `expected.md` — What the skill should do/output
4. Run test, verify it fails (RED)
5. Improve skill (GREEN)
6. Add more edge cases (REFACTOR)
