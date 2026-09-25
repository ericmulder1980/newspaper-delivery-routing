# Expected Behavior: Security Files Changed

## Expected Behavior

The guardian-agents skill should correctly detect security-critical changes and invoke appropriate guardians with proper external review coordination.

### 1. File Detection Phase

- [ ] Read `.claude/settings.json` successfully
- [ ] Match `src/auth/middleware.ts`:
  - Pattern `*.ts` → `typescript`
  - Pattern `auth/**` → `security`
- [ ] Match `src/api/users/route.ts`:
  - Pattern `*.ts` → `typescript`
  - Pattern `api/**` → `api`, `security`
- [ ] Match `src/api/auth/login.ts`:
  - Pattern `*.ts` → `typescript`
  - Pattern `api/**` → `api`, `security`
  - Pattern `auth/**` → `security`
- [ ] Deduplicate guardian list: `[typescript, security, api]`

### 2. Guardian Spawning Phase

- [ ] Spawn exactly 3 guardians: `typescript`, `security`, `api`
- [ ] All guardians receive context of all 3 changed files
- [ ] Use parallel dispatch pattern
- [ ] Security guardian receives priority flag (if applicable)
- [ ] Do NOT spawn: architecture, test, performance, docs, accessibility, database

### 3. External Review Coordination

Because security-sensitive files changed:

- [ ] Create Codex task file: `.ai/tasks/review/YYMMDD-codex-[feature].md`
- [ ] Task file includes:
  - List of changed files
  - Focus areas: "Security: authentication flows, authorization, tenant isolation"
  - Deliverables path
- [ ] Notify user that branch is ready for Codex review
- [ ] If Gemini configured, create secondary task file

### 4. Synthesis Phase

- [ ] Wait for all guardians to complete
- [ ] Wait for Codex completion (sync point)
- [ ] Aggregate findings from:
  - TypeScript guardian
  - Security guardian
  - API guardian
  - Codex external review
- [ ] Present combined findings with severity categorization

### 5. Gate Check Phase

- [ ] If CRITICAL or HIGH security issues → BLOCK merge
- [ ] Require explicit override for security issues
- [ ] Log security override to `.ai/feedback/process/overrides.md` with extra detail
- [ ] Recommend security team review if override requested

## Success Criteria

**PASS if:**
- Correct 3 guardians spawned (typescript, security, api)
- External review coordination initiated
- Security issues properly flagged and gated
- All relevant files included in each guardian's context

**FAIL if:**
- Security guardian not invoked despite auth/ and api/ changes
- No external review coordination for security-sensitive changes
- Security issues not properly gated
- Wrong guardians invoked

## Edge Cases to Handle

- [ ] Auth files in nested directories (e.g., `src/api/v2/auth/`)
- [ ] Multiple overlapping patterns should deduplicate correctly
- [ ] Security guardian should receive all security-relevant files
