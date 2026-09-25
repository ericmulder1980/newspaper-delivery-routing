# Migrate to Boilerplate

Add domain memory to an existing project.

Invoke the `boilerplate-migrate` skill with full onboarding flow:

## Process

1. **Analyze existing context**
   - Read CLAUDE.md, .claude/, .ai/, docs/
   - Identify what already exists

2. **Present findings**
   - Show what was found
   - Propose migration plan

3. **Ask clarifying questions**
   - Active work?
   - Key decisions already made?
   - Known issues?

4. **Create memory scaffold**
   - Create .claude/memory/ with features.json, progress.log, decisions.md, issues.md
   - Merge session protocol into existing CLAUDE.md
   - Preserve all existing project-specific content

5. **Confirm**
   - List what was created
   - Ready to continue

## Scenarios

This handles:
- **Scenario A:** Existing codebase, no Claude context
- **Scenario B:** Existing codebase with basic CLAUDE.md

## Usage

- `/migrate` — Interactive migration with analysis
- `/migrate check` — Dry run, show what would change

## Options

- `/migrate init` — Full interactive migration (default)
- `/migrate check` — Dry run, show what would change
- `/migrate update` — Update existing migration to latest

## After Migration

1. Review merged CLAUDE.md
2. Verify domain memory files created
3. Run `/status` to confirm memory working
4. Start new session to test session protocol

## Rollback

If needed: `git checkout pre-boilerplate-backup`
