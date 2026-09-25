# Expected Behavior: Prettier Not Installed

## Expected Actions

### 1. Silent Context Gathering
- [ ] Execute `git log --oneline -5` to get recent commits
- [ ] Execute `git branch --show-current` to get current branch
- [ ] Execute `git status` to check for uncommitted work
- [ ] Read `.ai/sessions/session.log` for last session summary
- [ ] List `.ai/tasks/backlog/` files
- [ ] Check if `package.json` exists
- [ ] Execute `command -v prettier` (will return not found)
- [ ] Read `.ai/sessions/highlights-shown.log`

### 2. Determine Highlights
- [ ] Recognize package.json exists (real project)
- [ ] Recognize prettier is not installed
- [ ] Recognize PostToolUse hook expects prettier
- [ ] Check if prettier offer was previously logged in `.ai/sessions/tooling-offered.log`
- [ ] If not previously offered, **prioritize prettier offer over feature tips**
- [ ] Prepare to track the offer (will log after user responds)

### 3. Compose and Deliver Greeting

**Expected Greeting Structure:**

```
Good morning! (or time-appropriate greeting)

**Project Status**
- Branch: feature/user-api | 3 uncommitted files
- Recent: "Add user endpoints", "Set up database models"

**Last Session**
Started the API endpoints for user management.

**What's Next**
- Finish CRUD operations for users
- Add validation middleware (backlog)

**Quick Setup Offer**
I noticed Prettier isn't installed. The boilerplate has an auto-format
hook that runs Prettier on every file I edit—keeps code clean without
thinking about it.

Want me to set it up? Just say "install prettier" and I'll run:
`pnpm add -D prettier` + create a basic .prettierrc

(Or ignore this and we'll continue without it—totally fine.)

---

**Session Settings** (defaults in brackets)

1. Verbosity: [full] / minimal / quiet
2. Guardians: [all] / select / off
3. Apprentice: [on] / detailed / off
4. Annealing: [active] / paused
5. External Review: [enabled] / disabled
6. Session Docs: [full] / light / off
7. Sync Check: [skip] / check

Change with "1 quiet, 3 off" or just tell me what we're working on.

*Tip: When you're done, say "end session" so I can save context for next time.*
```

### 4. Track Tooling Offer (After User Responds)

**If user accepts (says "install prettier" or similar):**
- [ ] Execute prettier installation workflow
- [ ] Create or append to `.ai/sessions/tooling-offered.log`:
  ```
  2025-12-03: prettier - accepted
  ```

**If user declines or ignores:**
- [ ] Create or append to `.ai/sessions/tooling-offered.log`:
  ```
  2025-12-03: prettier - declined
  ```
- [ ] Do NOT offer prettier again in future sessions (check this log)

### 5. Prettier Installation Workflow (If User Accepts)

```bash
# Install prettier
pnpm add -D prettier

# Create .prettierrc config
cat > .prettierrc << 'EOF'
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5"
}
EOF

# Confirm to user
echo "Done! The auto-format hook will now run on every file I edit."
```

- [ ] Execute `pnpm add -D prettier`
- [ ] Create `.prettierrc` with default config
- [ ] Confirm installation to user
- [ ] Log to tooling-offered.log

## Success Criteria

- [ ] Greeting shows correct project status (feature/user-api, 3 uncommitted files)
- [ ] Recent commits are listed
- [ ] Last session summary matches session.log
- [ ] What's Next includes current work and backlog
- [ ] **Prettier offer is included and prioritized** (not a generic feature tip)
- [ ] Offer explains the benefit (auto-format hook)
- [ ] Offer makes acceptance easy ("just say 'install prettier'")
- [ ] Offer makes clear it's optional ("or ignore this")
- [ ] Session settings menu is presented
- [ ] No prettier installation happens until user confirms
- [ ] Tooling offer will be tracked after user responds
- [ ] Future sessions will check tooling-offered.log to avoid re-offering

## Anti-Patterns to Avoid

- [ ] Don't install prettier without user consent
- [ ] Don't nag if user ignores the offer
- [ ] Don't offer prettier again if previously declined (check tooling-offered.log)
- [ ] Don't make the offer if package.json doesn't exist (not a real project)
- [ ] Don't skip the offer if conditions are met (package.json exists, prettier missing, not previously offered)
- [ ] Don't make the offer too pushy or too buried (balance)
- [ ] Don't fail to track the offer result (needed to prevent re-offering)

## Edge Cases

### If tooling-offered.log shows prettier was previously declined:
- [ ] Don't offer again
- [ ] Use a regular feature tip instead
- [ ] Only re-offer if user explicitly asks about formatting

### If prettier installation fails:
- [ ] Show error to user
- [ ] Log to tooling-offered.log as "failed"
- [ ] Don't prevent session from continuing
- [ ] Offer to troubleshoot if user wants

### If user says "yes" or "sure" instead of "install prettier":
- [ ] Interpret as acceptance in context of the offer
- [ ] Proceed with installation
- [ ] Confirm interpretation ("Installing prettier...")

## Variance Allowed

- Exact phrasing of the offer (as long as benefit, ease, and optionality are clear)
- Position of offer in greeting (can be before or after "What's Next")
- Config values in .prettierrc (can vary based on project conventions)
- Time-based greeting variation
