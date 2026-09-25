# Expected Behavior: End Session (Normal)

## Expected Actions

### 1. Commit Work

**Commands:**
```bash
# Stage all relevant changes
git add src/api/users.ts src/api/users.test.ts src/models/User.ts src/middleware/validation.ts

# Commit with clear message
git commit -m "$(cat <<'EOF'
Complete user CRUD API with validation and tests

- Finish CRUD operations (create, read, update, delete)
- Add validation middleware for request sanitization
- Write comprehensive test suite for user endpoints
- Update User model with additional fields

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
EOF
)"

# Push to remote
git push origin feature/user-api
```

**Checklist:**
- [ ] All changed files staged (4 files)
- [ ] Commit message is clear and describes what was accomplished
- [ ] Commit message follows project conventions
- [ ] Push to remote succeeds
- [ ] Confirm commit with `git status` (should show clean tree)

### 2. Document Session

**Create file:** `.ai/sessions/2025-12-03-user-crud-api.md`

**Expected content:**
```markdown
# Session: User CRUD API with Validation

**Date:** 2025-12-03
**Duration:** ~5.5 hours
**Branch:** feature/user-api

## Summary
Completed the user CRUD API endpoints with comprehensive validation middleware
and test coverage. All four operations (create, read, update, delete) are
implemented and tested. Updated the User model to support new fields.

## Completed
- Finish CRUD operations for users (GET, POST, PUT, DELETE)
- Add validation middleware for request sanitization
- Write tests for user endpoints (users.test.ts)
- Update User model with additional fields

## In Progress
(None - session concluded with work complete)

## Blocked / Needs Attention
(None)

## Next Steps
1. Implement user authentication (backlog)
2. Add pagination support for list endpoints (backlog)
3. Consider rate limiting for user creation

## Files Changed
- src/api/users.ts (modified)
- src/models/User.ts (modified)
- src/api/users.test.ts (new)
- src/middleware/validation.ts (new)

## Notes
- All tests passing
- Guardians reviewed code (one false positive noted in feedback)
- Ready to continue with authentication next session
```

**Checklist:**
- [ ] File created in `.ai/sessions/` directory
- [ ] Filename includes date and topic
- [ ] All sections populated accurately
- [ ] Duration calculated from session start/end
- [ ] Completed tasks match what was actually done
- [ ] Next steps pulled from backlog
- [ ] Files changed list is accurate

### 3. Update Session Log

**Append to:** `.ai/sessions/session.log`

**Expected entry:**
```
Session End: 2025-12-03 14:30
Summary: Completed user CRUD API with validation middleware and tests
Next: Implement user authentication
---
```

**Checklist:**
- [ ] Appended to session.log (not overwritten)
- [ ] Timestamp matches actual end time
- [ ] Summary is one clear line
- [ ] Next action identified from backlog
- [ ] Separator line included

### 4. Update Shared Context

**Check if update needed:** Yes (significant new feature)

**Update:** `.ai/SHARED-CONTEXT.md`

**Expected changes:**
- Update "Current Status > Active Work" section
- Move "User CRUD API" from active to "Recent Completions"
- Update "Next" to "User Authentication"

**Checklist:**
- [ ] Read current SHARED-CONTEXT.md
- [ ] Update Active Work section (remove completed items)
- [ ] Update Recent Completions (add today's work)
- [ ] Update Pending/Next (authentication is next)
- [ ] Don't need to update tech stack or architecture sections

**Skip updates:**
- [ ] `.ai/context/current-world-state.md` - no new tools/versions discovered
- [ ] Task files - none moved to completed (no formal tasks were tracked)

### 5. Feedback Capture

**Create/append to:** `.ai/feedback/guardians/api.md`

**Expected entry:**
```markdown
### 2025-12-03 14:15
**Type:** false_positive
**Source:** api-guardian
**Context:** Review of src/api/users.ts endpoint naming
**Expected:** Should pass - endpoints already use correct REST plural conventions
**Actual:** Guardian suggested changing endpoint names to plural, but they were already plural (/users, not /user)
**Suggested Fix:** Improve guardian's detection of existing plural forms before suggesting changes
```

**Checklist:**
- [ ] Feedback file created if doesn't exist
- [ ] Entry follows standard template
- [ ] Type is correct (false_positive)
- [ ] Context explains what was being reviewed
- [ ] Expected vs Actual is clear
- [ ] Suggested fix is actionable

### 6. Cleanup

**Delete:** `.ai/sessions/current-preferences.json`

**Checklist:**
- [ ] Session preferences file deleted (ephemeral, only for this session)

### 7. Confirmation to User

**Expected message to user:**
```
Session wrapped up! Here's what I saved:

**Committed:**
- User CRUD API with validation and tests
- Pushed to feature/user-api

**Documented:**
- Session notes: .ai/sessions/2025-12-03-user-crud-api.md
- Session log updated
- Shared context updated

**Feedback logged:**
- API guardian false positive on plural naming

**Next session:**
You'll see a summary of today's work and a suggestion to start on user authentication.

See you next time!
```

**Checklist:**
- [ ] User receives confirmation
- [ ] Key outcomes summarized
- [ ] What was logged is mentioned
- [ ] Preview of next session provided
- [ ] Tone is friendly and conclusive

## Success Criteria

- [ ] All 4 changed files are committed
- [ ] Commit message is clear and follows conventions
- [ ] Changes pushed to remote successfully
- [ ] Session document created with accurate information
- [ ] Session log updated with timestamp and summary
- [ ] Shared context updated to reflect completion
- [ ] Feedback captured for API guardian issue
- [ ] Current preferences file deleted
- [ ] User receives confirmation of what was saved
- [ ] No uncommitted changes remain (verified with git status)
- [ ] Session is ready for next time (context preserved)

## Anti-Patterns to Avoid

- [ ] Don't skip committing work (even if tests haven't run)
- [ ] Don't create an empty commit if no changes exist
- [ ] Don't leave session.log without an update
- [ ] Don't forget to track feedback observations
- [ ] Don't update shared context with trivial changes
- [ ] Don't leave current-preferences.json (it's ephemeral)
- [ ] Don't fail to confirm to user what was saved
- [ ] Don't push to main/master (this is feature branch - ok to push)
- [ ] Don't create session doc if absolutely nothing was done

## Edge Cases

### If git push fails (e.g., network issue):
- [ ] Still complete other steps
- [ ] Note push failure in session doc
- [ ] Remind user to push manually later

### If no changes exist to commit:
- [ ] Skip commit step gracefully
- [ ] Note in session doc: "No code changes (discussion/planning session)"
- [ ] Still update session.log and create session doc

### If no feedback to capture:
- [ ] Skip feedback step
- [ ] Don't create empty feedback entries

### If shared context doesn't need update:
- [ ] Skip that step
- [ ] Not every session requires shared context update

## Variance Allowed

- Exact commit message wording (as long as it's clear and accurate)
- Session doc filename (topic name can vary)
- Duration precision (approximate is fine)
- Order of steps (can vary slightly as long as all execute)
- Confirmation message phrasing to user
