# Expected Behavior: No Relevant Files Changed

## Expected Behavior

The guardian-agents skill should gracefully handle configuration-only changes without spawning unnecessary guardians.

### 1. File Detection Phase

- [ ] Read `.claude/settings.json` successfully
- [ ] Parse `guardians.file_detection` configuration
- [ ] Attempt to match each changed file:
  - `.gitignore` → no pattern match
  - `.prettierrc` → no pattern match
  - `package.json` → no pattern match
  - `tsconfig.json` → no pattern match
  - `.env.example` → no pattern match
- [ ] Result: Empty applicable guardians list

### 2. Decision Phase

The skill should make an intelligent decision. Two valid approaches:

**Option A: Skip Review Entirely**
- [ ] Detect zero applicable guardians
- [ ] Notify user: "No code changes detected. Skipping guardian review."
- [ ] Log to session: "Guardian review skipped - config files only"
- [ ] Allow commit/merge to proceed immediately

**Option B: Run Architecture Guardian (Minimal)**
- [ ] Detect configuration changes that may affect architecture
- [ ] Spawn only `architecture-guardian` to review:
  - Dependency changes (package.json)
  - TypeScript config changes (tsconfig.json)
  - Build configuration impacts
- [ ] Provide focused context: "Review configuration changes for architectural impact"

### 3. Communication Phase

- [ ] Clearly communicate decision to user
- [ ] Explain why review was skipped/minimal:
  ```
  Changed files are configuration-only:
  - .gitignore
  - .prettierrc
  - package.json
  - tsconfig.json
  - .env.example

  No code guardians triggered. Proceeding without code review.
  ```
- [ ] If Option B chosen, explain:
  ```
  Configuration changes detected. Running architecture guardian
  to review potential impacts on build/dependencies.
  ```

### 4. Gate Check Phase

- [ ] If Option A (skip): Immediately allow proceed
- [ ] If Option B (minimal): Apply standard gate check after architecture guardian completes

## Success Criteria

**PASS if:**
- Zero or minimal guardians spawned (not all 9)
- User receives clear explanation of why review is skipped/minimal
- Commit/merge not blocked unnecessarily for config changes
- Decision is logged to session

**FAIL if:**
- All guardians spawn for configuration-only changes
- Review blocks unnecessarily
- No communication to user about decision
- Error occurs due to zero applicable guardians

## Edge Cases to Handle

- [ ] Mix of config files and one code file → should trigger appropriate guardians for code file
- [ ] `.env` file (not `.env.example`) → should trigger security warning even if no pattern match
- [ ] `package.json` with dependency changes → consider security guardian for supply chain concerns
- [ ] Empty git diff → should skip review entirely

## Configuration Considerations

Optional enhancement to `file_detection`:

```json
{
  "file_detection": {
    "package.json": ["architecture"],
    "package-lock.json": ["architecture"],
    "tsconfig.json": ["architecture", "typescript"],
    ".env": ["security"]
  }
}
```

If this configuration exists, should trigger appropriate guardians.
If not, should skip gracefully.
