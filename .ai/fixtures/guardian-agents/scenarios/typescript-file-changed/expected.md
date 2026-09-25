# Expected Behavior: TypeScript File Changed

## Expected Behavior

The guardian-agents skill should demonstrate correct file detection and targeted guardian spawning.

### 1. File Detection Phase

- [ ] Read `.claude/settings.json` successfully
- [ ] Parse `guardians.file_detection` configuration
- [ ] Match `src/lib/utils.ts` against pattern `*.ts` → identifies `typescript`
- [ ] Match `src/components/Button.tsx` against pattern `*.tsx` → identifies `typescript`, `accessibility`
- [ ] Deduplicate guardian list: `[typescript, accessibility]`

### 2. Guardian Spawning Phase

- [ ] Spawn exactly 2 guardians (not all 9)
- [ ] Spawn `typescript-guardian` with context: both files
- [ ] Spawn `accessibility-guardian` with context: only `Button.tsx`
- [ ] Use parallel dispatch (Task tool or subagent pattern)
- [ ] Do NOT spawn: security, architecture, test, performance, docs, api, database

### 3. Result Synthesis Phase

- [ ] Wait for both guardians to complete
- [ ] Aggregate findings by severity (CRITICAL, HIGH, MEDIUM, LOW)
- [ ] Present combined findings in table format
- [ ] Note which guardians were run and which were skipped

### 4. Gate Check Phase

- [ ] If zero CRITICAL/HIGH issues → allow proceed
- [ ] If CRITICAL/HIGH issues exist → block and offer override option
- [ ] Log any overrides to `.ai/feedback/process/overrides.md`

## Success Criteria

**PASS if:**
- Only `typescript` and `accessibility` guardians are spawned
- No irrelevant guardians are invoked
- Guardians receive only relevant files in their context
- Findings are properly aggregated and categorized

**FAIL if:**
- All guardians are spawned (incorrect file detection)
- Wrong guardians are spawned
- Guardians receive files outside their domain
- No guardians are spawned when changes exist

## Edge Cases to Handle

- [ ] Empty file changes → skip review
- [ ] Multiple files of same type → deduplicate guardians
- [ ] Files matching multiple patterns → combine guardian lists
