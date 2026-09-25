# Expected Behavior: All Guardians Pass

## Expected Behavior

The guardian-agents skill should properly handle clean code reviews, coordinate with external reviewers, and allow merge to proceed smoothly.

### 1. File Detection & Guardian Spawning

- [ ] Detect applicable guardians from file patterns:
  - `*.ts` → `typescript`
  - `*.test.*` → `test`
  - `*.md` + `docs/**` → `docs`
- [ ] Deduplicate: `[typescript, test, docs]`
- [ ] Spawn all 3 guardians in parallel
- [ ] Provide appropriate file context to each

### 2. External Review Coordination

- [ ] Read external_reviewers configuration
- [ ] Create Codex task file: `.ai/tasks/review/YYMMDD-codex-validators.md`
- [ ] Task file includes:
  ```markdown
  # Review Task: Validators Feature

  **Files to Review:**
  - src/lib/validators.ts
  - src/lib/validators.test.ts
  - docs/api/validators.md

  **Focus Areas:**
  - Code quality and maintainability
  - Test coverage adequacy
  - Documentation accuracy

  **Deliverables:**
  Save findings to: .ai/tasks/review/YYMMDD-codex-validators-findings.md
  ```
- [ ] Notify user: "Branch ready for Codex review"

### 3. Findings Collection

- [ ] Wait for all guardians to complete
- [ ] Collect reports:
  - TypeScript Guardian: No issues
  - Test Guardian: No issues
  - Docs Guardian: No issues
- [ ] Wait for Codex completion (sync point)
- [ ] Collect Codex report: APPROVED with optional enhancement

### 4. Synthesis Phase

- [ ] Aggregate all findings
- [ ] Present synthesis:

```markdown
## Review Synthesis: Validators Feature

### Guardian Results
✅ TypeScript Guardian: PASSED (0 issues)
✅ Test Guardian: PASSED (0 issues, 100% coverage)
✅ Docs Guardian: PASSED (0 issues)

### External Review
✅ Codex Review: APPROVED

### Combined Findings

| Severity | Count | Issues |
|----------|-------|--------|
| CRITICAL | 0 | None |
| HIGH | 0 | None |
| MEDIUM | 0 | None |
| LOW | 0 | None |

**Total Issues:** 0
**Optional Enhancements:** 1 (JSDoc comments for IDE support)

### Verdict
✅ All checks passed. Ready to merge.
```

### 5. Gate Check

- [ ] Check gate configuration: `require_zero_issues: true`
- [ ] Verify zero CRITICAL/HIGH/MEDIUM/LOW issues
- [ ] **ALLOW merge to proceed**
- [ ] Present success message:

```markdown
✅ Review Complete - All Checks Passed

All guardians and external reviewers approved this change.
No blocking issues found.

You may proceed with merge to develop.

Optional enhancements (non-blocking):
- Add JSDoc comments for better IDE support
```

### 6. Session Logging

- [ ] Log successful review to `.ai/sessions/session.log`:
  ```
  YYYY-MM-DD HH:MM - Guardian review completed: validators feature
  Guardians run: typescript, test, docs
  External reviewers: codex
  Result: PASSED (0 issues)
  Merge: ALLOWED
  ```

### 7. Self-Annealing Feedback

- [ ] Log successful review pattern to `.ai/feedback/process/success-patterns.md`:
  ```markdown
  ### YYYY-MM-DD - Validators Feature Review
  **Pattern:** Feature with code + tests + docs
  **Guardians:** typescript, test, docs
  **Result:** Zero issues found
  **Key Success Factors:**
  - Explicit type annotations throughout
  - 100% test coverage with edge cases
  - Documentation matched implementation
  - All tests passing before review

  **Takeaway:** This is an example of well-structured feature development.
  ```

### 8. Optional Enhancement Handling

- [ ] Distinguish between blocking issues and optional enhancements
- [ ] Present optional enhancements separately
- [ ] Don't gate merge on optional items
- [ ] Allow user to defer enhancements to future work

## Success Criteria

**PASS if:**
- All guardians spawn and complete successfully
- External review coordinated properly
- Zero issues correctly allows merge
- Clear success message presented
- Session logged with success details
- Success pattern logged for self-annealing

**FAIL if:**
- Merge blocked despite zero issues
- External review not coordinated
- Success not properly logged
- Optional enhancements treated as blocking issues
- No clear "ready to merge" message

## Edge Cases to Handle

- [ ] Optional enhancements should not block merge
- [ ] If Codex is slow, should notify user and allow proceeding without waiting (based on `require_approval: false`)
- [ ] Multiple optional enhancements from different guardians should be aggregated
- [ ] Success should be celebrated (positive reinforcement for good practices)

## User Experience Validation

The successful review should:
- [ ] Feel quick and efficient (parallel execution)
- [ ] Provide confidence (detailed verification)
- [ ] Celebrate success (positive messaging)
- [ ] Capture learnings (success pattern logged)
- [ ] Not create friction (immediate merge allowed)
