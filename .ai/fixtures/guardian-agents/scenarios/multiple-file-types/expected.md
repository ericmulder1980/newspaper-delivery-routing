# Expected Behavior: Multiple File Types Changed

## Expected Behavior

The guardian-agents skill should correctly detect diverse file types and spawn appropriate guardians with properly scoped contexts.

### 1. File Detection Phase

- [ ] Read `.claude/settings.json` successfully
- [ ] Match `src/lib/database.ts`:
  - Pattern `*.ts` → `typescript`
- [ ] Match `src/lib/database.test.ts`:
  - Pattern `*.ts` → `typescript`
  - Pattern `*.test.*` → `test`
- [ ] Match `docs/api.md`:
  - Pattern `*.md` → `docs`
  - Pattern `docs/**` → `docs`
- [ ] Match `docs/architecture.md`:
  - Pattern `*.md` → `docs`
  - Pattern `docs/**` → `docs`
- [ ] Match `README.md`:
  - Pattern `*.md` → `docs`
- [ ] Deduplicate guardian list: `[typescript, test, docs]`

### 2. Guardian Spawning Phase

- [ ] Spawn exactly 3 guardians in parallel
- [ ] Spawn `typescript-guardian` with context:
  - `src/lib/database.ts`
  - `src/lib/database.test.ts`
- [ ] Spawn `test-guardian` with context:
  - `src/lib/database.test.ts`
  - Corresponding source: `src/lib/database.ts` (for coverage checking)
- [ ] Spawn `docs-guardian` with context:
  - `docs/api.md`
  - `docs/architecture.md`
  - `README.md`
- [ ] Use parallel dispatch (all guardians run simultaneously)
- [ ] Do NOT spawn: security, architecture, performance, accessibility, api, database

### 3. Guardian-Specific Validation

**TypeScript Guardian should:**
- [ ] Check type safety in both `.ts` and `.test.ts` files
- [ ] Verify no `any` types without justification
- [ ] Check import/export consistency

**Test Guardian should:**
- [ ] Verify test coverage for `database.ts`
- [ ] Check test quality (edge cases, assertions)
- [ ] Validate test naming conventions
- [ ] Ensure tests are runnable

**Docs Guardian should:**
- [ ] Check documentation currency
- [ ] Verify links are not broken
- [ ] Ensure API documentation matches implementation
- [ ] Validate markdown formatting

### 4. Result Synthesis Phase

- [ ] Wait for all 3 guardians to complete
- [ ] Aggregate findings by guardian type:
  ```markdown
  ### From Guardians
  - TypeScript: [findings summary]
  - Test: [findings summary]
  - Docs: [findings summary]
  ```
- [ ] Combine and categorize by severity
- [ ] Cross-reference findings (e.g., undocumented API changes)

### 5. Gate Check Phase

- [ ] If zero CRITICAL/HIGH issues → allow proceed
- [ ] If CRITICAL/HIGH issues exist → block merge
- [ ] Present issues grouped by guardian for clarity

## Success Criteria

**PASS if:**
- Correct 3 guardians spawned (typescript, test, docs)
- Each guardian receives only relevant files
- Guardians run in parallel (not sequentially)
- Findings properly aggregated and categorized
- Cross-cutting concerns identified (e.g., code change without doc update)

**FAIL if:**
- All guardians spawned (not filtered by file detection)
- Sequential execution instead of parallel
- Wrong file context provided to guardians
- No synthesis of findings across guardians

## Edge Cases to Handle

- [ ] Test files should be reviewed by both test guardian AND typescript guardian
- [ ] Docs in multiple locations should all go to docs guardian
- [ ] Pattern overlaps (e.g., `*.md` and `docs/**`) should deduplicate correctly
