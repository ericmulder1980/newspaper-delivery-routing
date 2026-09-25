# Domain Memory Integration Design

**Date:** 2025-12-09
**Status:** Approved for implementation
**Project:** claude-code-boilerplate

---

## Executive Summary

Integrate domain memory architecture from the PM boilerplate into the existing claude-code-boilerplate. This creates a unified system where:

- **Domain memory** (features.json, progress.log, decisions.md, issues.md) provides persistent state across sessions
- **Guardians** provide parallel quality gates during review
- **Specialists** provide deep design assistance during planning
- **Self-annealing** tracks feedback per instance for continuous improvement

The core insight from Anthropic's research: "The magic is in the memory. The magic is in the harness. The magic is not in the personality layer."

---

## Design Decisions

### Primary User
Solo developer on long-running projects. Team-friendly as a bonus.

### Guardians vs REVIEWER
REVIEWER agent orchestrates guardians. Guardians run during the "review" lifecycle phase, invoked by REVIEWER (not automatically on every commit).

### Guardians vs Specialists
Keep both, organized side by side:
- **Guardians** — Fast review, parallel execution, severity-sorted findings
- **Specialists** — Deep design assistance, invoked during planning

### Directory Structure

```
project-root/
├── CLAUDE.md                           # ~170 lines, protocols inline
├── .claude/                            # Configuration
│   ├── memory/                         # Domain memory
│   │   ├── features.json               # Machine-readable backlog
│   │   ├── progress.log                # Append-only history + handoff
│   │   ├── decisions.md                # Architectural Decision Records
│   │   └── issues.md                   # Problems + resolutions
│   ├── agents/                         # Agent protocols
│   │   ├── INITIALIZER.md              # Bootstrap domain memory
│   │   ├── PLANNER.md                  # Feature planning
│   │   ├── IMPLEMENTER.md              # Worker protocol
│   │   ├── REVIEWER.md                 # Pre-merge review orchestration
│   │   └── domains/                    # Domain-specific agents
│   │       ├── security/
│   │       │   ├── guardian.md
│   │       │   └── specialist.md
│   │       ├── database/
│   │       │   ├── guardian.md
│   │       │   └── specialist.md
│   │       ├── architecture/
│   │       │   ├── guardian.md
│   │       │   └── specialist.md
│   │       ├── api/
│   │       │   ├── guardian.md
│   │       │   └── specialist.md
│   │       ├── typescript/
│   │       │   └── guardian.md
│   │       ├── test/
│   │       │   ├── guardian.md
│   │       │   └── specialist.md
│   │       ├── performance/
│   │       │   ├── guardian.md
│   │       │   └── specialist.md
│   │       ├── accessibility/
│   │       │   └── guardian.md
│   │       └── docs/
│   │           └── guardian.md
│   ├── skills/                         # Workflow orchestration
│   ├── commands/                       # Slash commands
│   └── templates/                      # Feature spec templates
├── .ai/                                # Operations
│   ├── sessions/                       # Session logs
│   ├── feedback/                       # Self-annealing observations
│   │   ├── guardians/
│   │   ├── skills/
│   │   ├── process/
│   │   ├── directives/
│   │   └── memory/                     # NEW: Domain memory feedback
│   │       ├── orientation.md
│   │       ├── updates.md
│   │       ├── lifecycle.md
│   │       └── decisions.md
│   └── annealing/                      # Weekly review history
└── docs/
    ├── ARCHITECTURE.md
    └── specs/                          # Feature specifications
```

### CLAUDE.md Structure (~170 lines)

Contents:
- Project quick reference (stack, commands)
- Session protocol (inline, ~15 lines)
- Proactive behaviors (initialization triggers, when to invoke what)
- Domain memory file references
- Agent protocol references
- Guardian mention
- Critical rules (secrets, types, etc.)
- Apprentice mode note
- Links to detailed docs

### Session Protocol (in CLAUDE.md)

```markdown
## Session Protocol

**On start:**
1. Check `.claude/memory/features.json`
   - If exists → read it, show current feature/task/blockers
   - If missing → prompt: "No domain memory found. Describe your project or say 'help me set up'."
2. Read tail of `progress.log` for recent context
3. Announce status, ask what to work on

**Suggest initialization when:**
- features.json missing
- User describes new project or major feature
- User lists multiple things to build
- User seems uncertain what to work on
- Project has code but no features.json

**On end:**
1. Update features.json with progress
2. Append to progress.log (including handoff context)
3. Commit if appropriate
```

### Progress Log Format (replaces SCRATCHPAD.md)

```
================================================================================
[YYYY-MM-DD HH:MM] SESSION END
Feature: [ID] — [Title]
Task: [TASK-ID] — [Description]

COMPLETED:
- [What was accomplished]

IN PROGRESS:
- [What was started but not finished]

TESTS: [X/Y passing]

DECISIONS: [DEC-XXX — brief description]

FILES CHANGED:
+ [new files]
M [modified files]

BLOCKERS: [Any blockers]

NEXT SESSION:
- [Recommended next actions]
- [Open questions]

CONTEXT FOR NEXT SESSION:
[Free-form notes for handoff — what you were thinking, useful links, etc.]

Commit: [hash] "[message]"
================================================================================
```

### Specialists to Add

| Domain | Guardian | Specialist |
|--------|----------|------------|
| security | exists | exists (from PM) |
| database | exists | exists (from PM) |
| architecture | exists | **NEW** |
| api | exists | **NEW** |
| test | exists | **NEW** |
| performance | exists | **NEW** |
| typescript | exists | not needed |
| accessibility | exists | not needed (for now) |
| docs | exists | not needed |

### Skills Changes

| Skill | Action |
|-------|--------|
| `session-management` | **Deprecate** — protocol moves to CLAUDE.md |
| `guardian-agents` | **Keep** — REVIEWER invokes this |
| `feature-completion` | **Update** — integrate lifecycle phases |
| `full-codebase-review` | **Keep** |
| `weekly-annealing` | **Keep** |
| `apprentice-mode` | **Keep** |
| `guardian-builder` | **Keep** |
| `boilerplate-sync` | **Keep** |
| `boilerplate-migrate` | **Update** — full onboarding flow with memory scaffold |
| `boilerplate-customize` | **Keep** |
| `skill-audit` | **Keep** |

### Commands

| Command | Purpose | Status |
|---------|---------|--------|
| `/setup` | New project — create boilerplate + run INITIALIZER | **NEW** |
| `/migrate` | Existing project — analyze + add memory scaffold | **UPDATE** |
| `/status` | Show current feature, task, blockers | **NEW** |
| `/plan` | Invoke PLANNER for a feature | **NEW** |
| `/review` | REVIEWER + guardians + Codex | **UPDATE** |
| `/complete-feature` | Feature completion checklist | **UPDATE** |
| `/feedback` | Quick feedback capture | **KEEP** |
| `/customize` | Adjust settings | **KEEP** |
| `/audit` | Ecosystem health | **KEEP** |

### Review Workflow

```
Feature enters review phase
    │
    ├─→ Spawn guardian subagents (parallel, fast)
    │   └─→ Security, TypeScript, Architecture, etc.
    │
    ├─→ Create Codex task (async, ~15 min)
    │   └─→ Push branch, Codex reviews in background
    │
    ├─→ Guardian results stream in
    │
    └─→ Codex completes
        │
        ▼
    REVIEWER synthesizes all findings
        │
        ▼
    Present unified review to user
```

### Migration Flow (Scenario B: Existing project with CLAUDE.md)

**Step 1: Analyze existing context**
- Read CLAUDE.md, .claude/, .ai/, docs/
- Identify what exists

**Step 2: Present findings**
- Show what was found
- Propose migration plan

**Step 3: Ask clarifying questions**
- Active work?
- Key decisions already made?
- Known issues?

**Step 4: Create memory scaffold**
- Create .claude/memory/ files
- Merge session protocol into CLAUDE.md
- Preserve existing project-specific content

**Step 5: Confirm**
- List what was created
- Ready to continue

### Self-Annealing Per Instance

Each project instance tracks its own feedback:

```
.ai/feedback/
├── guardians/           # Guardian accuracy
├── skills/              # Skill trigger accuracy
├── process/             # Workflow friction
├── directives/          # CLAUDE.md refinements
└── memory/              # Domain memory feedback
    ├── orientation.md   # Session start effectiveness
    ├── updates.md       # Session end reliability
    ├── lifecycle.md     # Phase transition issues
    └── decisions.md     # ADR usefulness
```

Weekly annealing reviews instance feedback and proposes improvements:
- Instance-specific improvements
- Boilerplate-wide improvements (via boilerplate-sync)

### External Review

Keep Codex integration. Valuable for solo dev (~15 min async review with different perspective). Optional but recommended.

### Apprentice Mode

Keep as-is. Naturally explains domain memory system when relevant:
- When working with features.json → explain lifecycle phases
- When making decisions → explain why we record them
- When updating progress.log → explain handoff pattern

---

## What's NOT Changing

- Guardian agents (content preserved, moved to domains/ structure)
- Self-annealing infrastructure
- Hooks (auto-format, sensitive file blocking)
- TDD fixtures for skill testing
- External reviewer integration
- Apprentice mode behavior

---

## Implementation Order

### Phase 1: Directory Restructure
1. Create `.claude/memory/` with template files
2. Move guardians to `.claude/agents/domains/*/guardian.md`
3. Add specialists from PM boilerplate
4. Create new specialists (architecture, api, test, performance)

### Phase 2: Agent Integration
5. Add INITIALIZER.md, PLANNER.md, IMPLEMENTER.md, REVIEWER.md
6. Update REVIEWER to orchestrate guardians

### Phase 3: CLAUDE.md Rewrite
7. Merge PM boilerplate CLAUDE.md with current
8. Add session protocol inline
9. Add initialization triggers
10. Add proactive behaviors

### Phase 4: Skills Update
11. Deprecate session-management (move to CLAUDE.md)
12. Update boilerplate-migrate with memory scaffold flow
13. Update feature-completion with lifecycle integration

### Phase 5: Commands
14. Add `/setup` command
15. Add `/status` command
16. Add `/plan` command
17. Update `/review` command
18. Update `/migrate` command

### Phase 6: Feedback Infrastructure
19. Add `.ai/feedback/memory/` structure
20. Update weekly-annealing to include memory feedback

### Phase 7: Documentation
21. Update README
22. Update any existing docs

### Phase 8: Testing
23. Test `/setup` flow on new project
24. Test `/migrate` flow on existing project
25. Test full feature lifecycle

---

## Sources

- Anthropic: Context Engineering for AI Agents (Sept 2025)
- Anthropic: Building Agents with Claude Agent SDK (Sept 2025)
- Anthropic: Building Effective Agents (Dec 2024)
- Nate B Jones: Domain Memory analysis (YouTube)
- PM Boilerplate: cc-project-context-memory-plan/claude-pm-boilerplate.zip
- Current boilerplate: claude-code-boilerplate design doc

---

## Open Questions (Resolved)

| Question | Resolution |
|----------|------------|
| Guardians vs Specialists | Keep both, side by side |
| SCRATCHPAD.md | Merged into progress.log |
| Session reliability | Protocol in CLAUDE.md, not dependent on skills |
| /init command | Use /setup instead (conflicts with built-in) |
| Migration approach | Full onboarding flow with analysis |

---

*Design approved: 2025-12-09*
