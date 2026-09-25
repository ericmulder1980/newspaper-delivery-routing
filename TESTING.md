# Testing Guide

Manual testing checklist for the claude-code-boilerplate.

## Pre-deployment Testing

### 1. Test `/setup` Command (New Project Initialization)

**Setup:**
```bash
# Create a test directory
mkdir /tmp/test-new-project
cd /tmp/test-new-project

# Copy boilerplate
cp -r /path/to/claude-code-boilerplate/* .
cp -r /path/to/claude-code-boilerplate/.* .

# Initialize git
git init
git add .
git commit -m "Initial commit: boilerplate"
```

**Test:**
1. Start Claude Code: `claude`
2. Run `/setup`
3. **Expected behavior:**
   - INITIALIZER agent loads
   - Asks about project purpose, features, tech stack, constraints
   - Waits for user responses
   - Creates feature breakdown based on responses
   - Asks for validation of feature breakdown
   - Creates `.claude/memory/features.json` with populated features
   - Creates `.claude/memory/progress.log` with initial entry
   - Creates `.claude/memory/decisions.md` with initial decisions
   - Presents initialization summary

**Verify:**
```bash
# Check files exist and are populated
cat .claude/memory/features.json
cat .claude/memory/progress.log | tail -50
cat .claude/memory/decisions.md
```

**Success criteria:**
- [ ] INITIALIZER asks comprehensive questions
- [ ] Feature breakdown is specific and actionable
- [ ] features.json is valid JSON with correct structure
- [ ] progress.log has initial entry with timestamp
- [ ] decisions.md has at least one decision recorded
- [ ] Initialization summary is clear and actionable

---

### 2. Test `/status` Command

**Prerequisites:** Complete `/setup` test first

**Test:**
1. In the same project, run `/status`
2. **Expected behavior:**
   - Reads `.claude/memory/features.json`
   - Shows current feature, phase, tasks
   - Shows last session summary from progress.log
   - Shows any open blockers

**Success criteria:**
- [ ] Status display is formatted clearly
- [ ] Shows correct feature count breakdown
- [ ] Shows current feature (if any)
- [ ] Shows last session info
- [ ] Handles empty/new project gracefully

---

### 3. Test `/plan` Command

**Prerequisites:** Complete `/setup` test first

**Test:**
1. Run `/plan` and specify a feature from features.json
2. **Expected behavior:**
   - PLANNER agent loads
   - Asks clarifying questions about the feature
   - Creates detailed implementation plan
   - Identifies files to modify/create
   - Considers architectural trade-offs
   - Proposes updates to features.json

**Success criteria:**
- [ ] PLANNER asks relevant questions
- [ ] Plan is detailed and actionable
- [ ] Plan considers existing codebase patterns
- [ ] Plan identifies dependencies
- [ ] Plan includes testing strategy

---

### 4. Test `/migrate` Command (Existing Project)

**Setup Scenario A:** Existing project with no Claude context
```bash
mkdir /tmp/test-migrate-fresh
cd /tmp/test-migrate-fresh

# Create a simple Node.js project
npm init -y
echo "console.log('Hello');" > index.js
git init
git add .
git commit -m "Initial project"

# Copy ONLY the boilerplate structure (not the whole project)
cp -r /path/to/claude-code-boilerplate/.claude .
cp -r /path/to/claude-code-boilerplate/.ai .
cp /path/to/claude-code-boilerplate/CLAUDE.md .
```

**Test:**
1. Start Claude Code: `claude`
2. Run `/migrate`
3. **Expected behavior:**
   - Analyzes existing project structure
   - Detects no existing .claude/memory/
   - Asks about active work, decisions made, known issues
   - Creates memory scaffold
   - Preserves existing code
   - Updates CLAUDE.md with project-specific content

**Verify:**
```bash
# Check memory structure created
ls -la .claude/memory/
cat .claude/memory/features.json
git status  # Should show new files, no modified existing files
```

**Success criteria:**
- [ ] Migration detects project type correctly
- [ ] Memory files created and populated
- [ ] Existing code unchanged
- [ ] CLAUDE.md updated appropriately
- [ ] No breaking changes to existing structure

---

**Setup Scenario B:** Existing project with basic CLAUDE.md
```bash
mkdir /tmp/test-migrate-existing
cd /tmp/test-migrate-existing

# Create project with basic CLAUDE.md
npm init -y
echo "# My Project\n\nCritical Rules:\n- Use TypeScript" > CLAUDE.md
git init
git add .
git commit -m "Initial project with CLAUDE.md"

# Copy boilerplate structure
cp -r /path/to/claude-code-boilerplate/.claude .
cp -r /path/to/claude-code-boilerplate/.ai .
```

**Test:**
1. Run `/migrate`
2. **Expected behavior:**
   - Detects existing CLAUDE.md
   - Merges session protocol into existing CLAUDE.md
   - Preserves project-specific content
   - Creates memory scaffold

**Success criteria:**
- [ ] Existing CLAUDE.md content preserved
- [ ] Session protocol added
- [ ] No duplicate content
- [ ] User can review merge before accepting

---

### 5. Test Session Protocol

**Test Start:**
1. In a project with domain memory, start a new Claude Code session
2. **Expected behavior:**
   - Checks `.claude/memory/features.json`
   - Shows current feature/task/blockers
   - Reads tail of progress.log
   - Presents status and asks what to work on

**Success criteria:**
- [ ] Session starts with context from memory
- [ ] Status is accurate
- [ ] Handoff context preserved from previous session

**Test End:**
1. At end of a session, say "end session" or complete a significant milestone
2. **Expected behavior:**
   - Updates features.json with progress
   - Appends to progress.log with session summary
   - Includes handoff context for next session

**Success criteria:**
- [ ] features.json updated with current state
- [ ] progress.log has new entry with timestamp
- [ ] Handoff context is clear and actionable

---

### 6. Test `/review` Command

**Prerequisites:** Project with some code changes

**Test:**
1. Make some code changes across multiple files
2. Run `/review`
3. **Expected behavior:**
   - REVIEWER agent orchestrates guardians
   - Relevant guardians run in parallel (based on file types)
   - External review task created (if configured)
   - Findings aggregated and presented
   - Severity-sorted results

**Success criteria:**
- [ ] Guardians detect relevant issues
- [ ] No false positives (verify manually)
- [ ] Results are actionable
- [ ] External review task created (check `.ai/tasks/review/`)

---

### 7. Test Memory Feedback

**Test:**
1. Use the boilerplate for a full feature lifecycle
2. Check `.ai/feedback/memory/` for observations
3. **Expected behavior:**
   - orientation.md tracks session start effectiveness
   - updates.md tracks session end reliability
   - lifecycle.md tracks phase transition issues
   - decisions.md tracks ADR usefulness

**Success criteria:**
- [ ] Feedback files populated during normal usage
- [ ] Observations are specific and actionable
- [ ] No false negatives (memory works but no feedback recorded)

---

## Integration Testing

### Full Feature Lifecycle

**Test the complete flow:**

1. `/setup` - Initialize project
2. `/status` - Verify initial state
3. `/plan` - Plan first feature
4. Work on feature (implementation)
5. `/review` - Guardian review
6. `/complete-feature` - Completion checklist
7. Commit and merge
8. `/status` - Verify updated state

**Success criteria:**
- [ ] Smooth flow with no blockers
- [ ] Memory updated at each step
- [ ] Handoff context preserved
- [ ] All commands work as expected

---

## Performance Testing

### Memory File Size

**Test:**
- Create project with 50+ features
- Check features.json size and load time
- **Expected:** Should load quickly, no performance degradation

### Guardian Parallel Execution

**Test:**
- Make changes affecting multiple domains
- Time the `/review` command
- **Expected:** Guardians run in parallel, total time < sum of individual times

---

## Edge Cases

### Empty Project
- [ ] Test `/setup` on completely empty directory
- [ ] Test `/status` before initialization

### Large Project
- [ ] Test `/migrate` on project with 100+ files
- [ ] Verify analysis doesn't time out

### Corrupted Memory
- [ ] Test behavior when features.json has invalid JSON
- [ ] Test behavior when progress.log is missing
- [ ] Should fail gracefully with clear error messages

### Concurrent Sessions
- [ ] Test two Claude sessions simultaneously
- [ ] Verify no race conditions on memory updates

---

## Regression Testing

After any changes to:
- `.claude/agents/` - Re-run agent tests
- `.claude/memory/` templates - Re-run memory tests
- CLAUDE.md - Re-run session protocol tests
- `/commands/` - Re-run command tests

---

## Manual Validation Checklist

Before marking as production-ready:

- [ ] All commands (`/setup`, `/status`, `/plan`, `/review`, `/migrate`, `/complete-feature`) tested
- [ ] Session protocol works (start and end)
- [ ] Memory files created and updated correctly
- [ ] Documentation accurate (README, SETUP, CLAUDE.md)
- [ ] No references to deprecated features (e.g., `/init`, `session-management`)
- [ ] CHANGELOG.md updated
- [ ] All guardians functional
- [ ] External review integration works
- [ ] Boilerplate-sync works (test on second instance)
- [ ] No breaking changes for existing users

---

## Automated Testing (Future)

Considerations for future automation:
- Fixture-based testing for agents (like guardian-builder TDD approach)
- CLI testing framework for slash commands
- Memory file schema validation
- Performance benchmarks

---

## Reporting Issues

When you find issues during testing:
1. Log to `.ai/feedback/` in the appropriate category
2. Create an entry in `.claude/memory/issues.md`
3. Open a GitHub issue if it affects multiple instances
4. Document in CHANGELOG.md under "Known Issues" if critical
