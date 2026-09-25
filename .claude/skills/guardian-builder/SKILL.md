---
name: guardian-builder
description: Create new guardian agents using TDD - write test fixtures first, then build guardian to detect issues
---

# Guardian Builder (TDD)

Create custom guardian agents using test-driven development.

## Overview

Build guardians that actually work by testing them against known cases first. This ensures guardians catch what they're supposed to catch.

## When to Use

- Creating a new specialized guardian
- Improving an existing guardian's accuracy
- Adding domain-specific checks

## TDD Process

### RED Phase: Create Test Fixtures

Before writing the guardian, create test cases:

```
.ai/fixtures/[guardian-name]/
├── bad/                    # Code that SHOULD be flagged
│   ├── issue-1.ts
│   ├── issue-2.ts
│   └── ...
├── good/                   # Code that should PASS
│   ├── correct-1.ts
│   ├── correct-2.ts
│   └── ...
└── expected.md             # What guardian should find
```

#### bad/ files
Code with known issues the guardian should catch:

```typescript
// bad/sql-injection.ts
// EXPECTED: CRITICAL - SQL injection vulnerability

const userId = req.params.id;
const query = `SELECT * FROM users WHERE id = ${userId}`;  // Vulnerable!
```

#### good/ files
Correct code the guardian should NOT flag:

```typescript
// good/parameterized-query.ts
// EXPECTED: Pass - no issues

const userId = req.params.id;
const query = await db.query('SELECT * FROM users WHERE id = $1', [userId]);
```

#### expected.md
Document what the guardian should find:

```markdown
# Expected Findings

## bad/sql-injection.ts
- CRITICAL: SQL injection at line 4

## bad/hardcoded-secret.ts
- HIGH: Hardcoded API key at line 2

## good/parameterized-query.ts
- No issues expected

## good/env-variable.ts
- No issues expected
```

### GREEN Phase: Build the Guardian

Now write the guardian in `.claude/agents/[name]-guardian.md`:

1. Start with basic structure (copy from existing guardian)
2. Define focus areas based on your test cases
3. Specify output format

Test against fixtures:
```
Run guardian on .ai/fixtures/[name]/bad/*
→ Should flag all issues in expected.md

Run guardian on .ai/fixtures/[name]/good/*
→ Should pass all files
```

Iterate until:
- 100% detection on bad/ files
- 0% false positives on good/ files

### REFACTOR Phase: Real-World Testing

Test against actual codebase:

```
[ ] Run on 10 real files from project
[ ] Check each finding manually
[ ] Note any false positives
[ ] Note any missed issues
```

Tune the guardian:
- Too many false positives? Make checks more specific
- Missing issues? Add more patterns to check
- Edge cases? Add to fixtures and refine

### Document Limitations

In the guardian file, add:

```markdown
## Known Limitations
- Does not catch [specific case]
- May false positive on [specific pattern]
- Requires [context] to be accurate
```

## Example: Creating a "Logging Guardian"

### Step 1: Define What to Catch

```markdown
# Logging Guardian Requirements
- Catch console.log in production code
- Catch missing error logging in catch blocks
- Catch sensitive data in logs
- Allow console.log in test files
```

### Step 2: Create Fixtures

```typescript
// bad/console-in-prod.ts
function processPayment(amount: number) {
  console.log('Processing payment:', amount);  // Bad!
  // ...
}

// bad/missing-error-log.ts
try {
  await riskyOperation();
} catch (e) {
  throw e;  // Bad - no logging!
}

// good/proper-logger.ts
function processPayment(amount: number) {
  logger.info('Processing payment', { amount });  // Good
  // ...
}

// good/test-file.test.ts
it('should work', () => {
  console.log('debugging test');  // OK in tests
});
```

### Step 3: Build Guardian

```markdown
---
name: logging-guardian
description: Reviews code for logging best practices
tools: Read, Grep, Glob
model: haiku
---

[Define focus areas based on fixtures...]
```

### Step 4: Verify

```
Fixture Results:
- bad/console-in-prod.ts: CAUGHT ✓
- bad/missing-error-log.ts: CAUGHT ✓
- good/proper-logger.ts: PASSED ✓
- good/test-file.test.ts: PASSED ✓
```

## Self-Annealing Integration

New guardians should include:
- Self-annealing section in prompt
- Initialized feedback file: `.ai/feedback/guardians/[name].md`
- Entry in settings.json under `guardians.enabled`

## Output

When complete:
- `.claude/agents/[name]-guardian.md` — The guardian
- `.ai/fixtures/[name]/` — Test fixtures for future tuning
- `.ai/feedback/guardians/[name].md` — Initialized feedback log
- Updated `.claude/settings.json` — Guardian enabled
