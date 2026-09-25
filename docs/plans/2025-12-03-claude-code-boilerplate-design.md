# Claude Code Boilerplate Design

**Date:** 2025-12-03
**Status:** Approved for implementation
**Project:** `/Users/carmenborrelli/Documents/sites/claude-code-boilerplate`

---

## Executive Summary

A production-ready boilerplate for Claude Code projects featuring:
- **Guardian agents** — 9 specialized reviewers that spawn in parallel
- **Self-annealing** — Continuous improvement with human approval gates
- **Apprentice mode** — Educational, contextual assistance for solo devs
- **Research-first** — Web search before recommendations when currency matters

Based on research from: contractor-system patterns, VoltAgent, wshobson/agents, claude-flow, official Claude Code docs, and industry self-healing AI practices.

---

## 1. Project Structure

```
project-root/
├── .claude/                          # Configuration (what Claude IS)
│   ├── settings.json                 # Hooks, permissions, guardian config
│   ├── agents/                       # Guardian subagents
│   │   ├── security-guardian.md
│   │   ├── typescript-guardian.md
│   │   ├── architecture-guardian.md
│   │   ├── test-guardian.md
│   │   ├── performance-guardian.md
│   │   ├── docs-guardian.md
│   │   ├── accessibility-guardian.md
│   │   ├── api-guardian.md
│   │   └── database-guardian.md
│   ├── skills/
│   │   ├── guardian-agents/          # Main orchestration
│   │   │   └── SKILL.md
│   │   ├── feature-completion/       # Feature branch workflow
│   │   │   └── SKILL.md
│   │   ├── full-codebase-review/     # Develop → main review
│   │   │   └── SKILL.md
│   │   ├── session-management/       # Start/end protocols
│   │   │   └── SKILL.md
│   │   ├── weekly-annealing/         # Self-improvement review
│   │   │   └── SKILL.md
│   │   ├── apprentice-mode/          # Extra-detailed teaching
│   │   │   └── SKILL.md
│   │   └── guardian-builder/         # TDD guardian creation
│   │       └── SKILL.md
│   └── commands/
│       ├── review.md                 # Manual guardian sweep
│       ├── complete-feature.md       # Feature completion checklist
│       ├── feedback.md               # Quick feedback capture
│       └── teach.md                  # Toggle detailed teaching
│
├── .ai/                              # Operations (what Claude DOES)
│   ├── tasks/
│   │   ├── review/                   # Code review assignments + findings
│   │   ├── parallel/                 # Parallel work assignments
│   │   ├── completed/                # Archive
│   │   └── backlog/                  # Future work
│   ├── sessions/                     # Session logs
│   ├── research/                     # Research documents
│   ├── context/
│   │   └── current-world-state.md    # Training cutoff补充
│   ├── feedback/                     # Self-annealing observations
│   │   ├── guardians/                # Guardian accuracy logs
│   │   ├── skills/                   # Skill trigger accuracy
│   │   ├── process/                  # Workflow improvement notes
│   │   └── directives/               # CLAUDE.md refinement suggestions
│   ├── annealing/
│   │   ├── weekly/                   # Weekly review summaries
│   │   └── history/                  # Past annealing decisions + rollbacks
│   └── SHARED-CONTEXT.md             # Detailed project context
│
├── .mcp.json                         # MCP server configuration
├── CLAUDE.md                         # Lean entry point (~50-80 lines)
├── AGENTS.md                         # Codex touchpoint
└── GEMINI.md                         # Gemini touchpoint
```

---

## 2. CLAUDE.md (Lean Root)

~50-80 lines. Critical rules only. Everything else linked.

```markdown
# [Project Name]

## Quick Reference
- Stack: [brief tech stack]
- Commands: `pnpm dev`, `pnpm test`, `pnpm build`
- Branch: Check with `git branch --show-current`

## Critical Rules
1. Research before recommending (web search when currency matters)
2. No secrets in code — environment variables only
3. Explicit TypeScript types — no `any` without justification
4. Tests required before commits

## Mode
Apprentice mode active (explain why, use analogies, share context).
For extra detail: invoke apprentice-mode skill.

## Context (load when relevant)
@.ai/context/current-world-state.md
@.ai/SHARED-CONTEXT.md

## Guardians
Configured in @.claude/settings.json
Invoke via guardian-agents skill or /review command

## See Also
- Architecture: @docs/architecture.md
- Patterns: @docs/patterns.md
- API design: @docs/api.md
```

---

## 3. Guardian Agents

### 3.1 Agent List

| Agent | Focus | Model | Tools | File Triggers |
|-------|-------|-------|-------|---------------|
| security-guardian | OWASP, secrets, injection, auth, tenant isolation | sonnet | Read, Grep, Glob | `*.ts`, `*.js`, `api/**`, `auth/**` |
| typescript-guardian | Strict types, no `any`, type safety | haiku | Read, Grep, Glob | `*.ts`, `*.tsx` |
| architecture-guardian | Patterns, modularity, dependencies, coupling | sonnet | Read, Grep, Glob | All code files |
| test-guardian | Coverage, test quality, edge cases | haiku | Read, Grep, Glob, Bash | `*.test.*`, `*.spec.*` |
| performance-guardian | N+1 queries, bundle size, complexity | sonnet | Read, Grep, Glob | `*.ts`, `*.sql`, `api/**` |
| docs-guardian | Documentation currency, organization | haiku | Read, Write, Glob, WebFetch | `*.md`, `docs/**` |
| accessibility-guardian | A11y, ARIA, keyboard nav, contrast | sonnet | Read, Grep, Glob | `*.tsx`, `*.jsx`, `*.css` |
| api-guardian | Endpoint design, REST conventions, versioning | sonnet | Read, Grep, Glob | `api/**`, `routes/**` |
| database-guardian | Schema, migrations, query optimization | sonnet | Read, Grep, Glob | `*.sql`, `migrations/**`, `prisma/**` |

### 3.2 Agent Format

```markdown
---
name: security-guardian
description: Reviews code for security vulnerabilities, OWASP top 10, secrets exposure, and tenant isolation
tools: Read, Grep, Glob
model: sonnet
---

You are the Security Guardian. Your role is to review code for security issues.

# Overview
[~100 tokens - loads with metadata]

## Focus Areas
- OWASP Top 10 vulnerabilities
- Secrets/credentials in code
- SQL/NoSQL injection
- XSS and CSRF
- Tenant isolation (multi-tenant apps)
- Authentication/authorization flaws

## Output Format
Report findings as:
- CRITICAL: [issue] at [file:line] - [explanation]
- HIGH: [issue] at [file:line] - [explanation]
- MEDIUM: [issue] at [file:line] - [explanation]
- LOW: [issue] at [file:line] - [explanation]

If no issues: "No security issues found in reviewed files."

## Self-Annealing
After review, if you suspect a false positive or missed something:
Log to .ai/feedback/guardians/security.md with reasoning.
```

### 3.3 Model Allocation

- **Haiku** — Fast, deterministic checks (typescript, test, docs)
- **Sonnet** — Complex reasoning needed (security, architecture, performance, accessibility, api, database)

---

## 4. Settings Configuration

`.claude/settings.json`:

```json
{
  "hooks": {
    "SessionStart": [
      {
        "type": "command",
        "command": "echo 'Session started at $(date)' >> .ai/sessions/current.log"
      }
    ],
    "PreToolUse": [
      {
        "matcher": "Bash:git commit",
        "hooks": [
          {
            "type": "command",
            "command": "echo 'Pre-commit guardian check triggered'"
          }
        ]
      }
    ]
  },
  "guardians": {
    "enabled": [
      "security",
      "typescript",
      "architecture",
      "test",
      "performance",
      "docs",
      "accessibility",
      "api",
      "database"
    ],
    "triggers": {
      "pre-commit": true,
      "pr-creation": true,
      "manual": true
    },
    "gates": {
      "require_zero_issues": true,
      "allow_override": true
    },
    "file_detection": {
      "*.sql": ["database"],
      "*.prisma": ["database"],
      "migrations/**": ["database"],
      "api/**": ["api", "security"],
      "auth/**": ["security"],
      "*.tsx": ["accessibility", "typescript"],
      "*.jsx": ["accessibility"],
      "*.test.*": ["test"],
      "*.spec.*": ["test"],
      "*.md": ["docs"],
      "docs/**": ["docs"]
    }
  },
  "external_reviewers": {
    "primary": "codex",
    "secondary": "gemini",
    "require_approval": false
  },
  "annealing": {
    "weekly_review": true,
    "auto_apply": false,
    "rollback_enabled": true
  },
  "apprentice_mode": {
    "base_enabled": true,
    "detailed_skill": "apprentice-mode"
  }
}
```

---

## 5. Core Skills

### 5.1 guardian-agents (Main Orchestrator)

```markdown
---
name: guardian-agents
description: Orchestrates parallel guardian agents for code review, synthesizes results, coordinates with external reviewers
---

# Overview
Spawns applicable guardians in parallel based on file detection.
Coordinates with Codex/Gemini. Synthesizes all findings.
Gates merge on zero issues (or explicit override).

## Execution Flow

### 1. Detect Applicable Guardians
Read .claude/settings.json → guardians.file_detection
Match against changed files
Spawn only relevant guardians

### 2. Parallel Dispatch (Quick Swarm Pattern)
Launch all applicable guardians via Task tool
Each operates in isolated context
Stream results as they complete

### 3. External Review Coordination
Prepare summary for Codex (primary)
Create task file in .ai/tasks/review/
Optionally trigger Gemini (secondary)

### 4. Synthesis (Wait for Codex as sync point)
Aggregate all findings
Categorize: CRITICAL, HIGH, MEDIUM, LOW

### 5. Fix Triage
Assess isolation of each fix:
- Isolated → spawn parallel coding agents
- Coupled → Claude handles directly

### 6. Gate Check
Default: zero issues to proceed
User can override with explicit approval
Log overrides to .ai/feedback/process/overrides.md
```

### 5.2 feature-completion

```markdown
---
name: feature-completion
description: Complete workflow for finishing a feature branch - tests, review, fixes, merge gates
---

# Overview
End-to-end checklist for completing a feature.
Ensures tests, review, and gates before merge.

## Phases

### Phase 1: Self-Validation
- [ ] All tests written
- [ ] Tests passing (`pnpm test`)
- [ ] No TypeScript errors
- [ ] Code formatted

### Phase 2: Guardian Review
- [ ] Invoke guardian-agents skill
- [ ] All applicable guardians complete
- [ ] Review all findings

### Phase 3: External Review
- [ ] Create Codex task in .ai/tasks/review/
- [ ] Push branch
- [ ] Wait for Codex completion
- [ ] (Optional) Gemini review

### Phase 4: Fix & Verify
- [ ] Triage fixes (parallel vs coupled)
- [ ] Apply fixes
- [ ] Re-run affected guardians
- [ ] Confirm zero issues

### Phase 5: Merge
- [ ] Merge to develop
- [ ] Archive task to .ai/tasks/completed/
- [ ] Write session log
- [ ] Log annealing observations
```

### 5.3 full-codebase-review

```markdown
---
name: full-codebase-review
description: Comprehensive review of entire codebase before merging develop to main
---

# Overview
Holistic review extending guardian-agents.
Runs all guardians on full codebase plus integration checks.

## When to Use
- Before develop → main merge
- Weekly/monthly health check
- Major release prep
- Post-significant-refactor

## Additional Checks

### Integration Guardian
- Cross-feature compatibility
- Shared state conflicts
- Event/messaging consistency

### Coverage Aggregate
- Overall test coverage %
- Coverage gaps across modules
- Critical paths without tests

### Dependency Audit
- Outdated packages (npm outdated)
- Known vulnerabilities (npm audit)
- Unused dependencies

### Architecture Drift
- Pattern violations
- Coupling creep
- Naming convention drift

## Output
.ai/reviews/YYYY-MM-DD-full-codebase-review.md
```

### 5.4 weekly-annealing

```markdown
---
name: weekly-annealing
description: Weekly review of accumulated feedback, propose improvements for human approval
---

# Overview
Aggregate feedback. Identify patterns. Propose refinements.
ALL proposals require explicit human approval. Never auto-apply.

## Process

### 1. Gather Feedback
Read all .ai/feedback/ logs from past week:
- guardians/*.md (false positives/negatives)
- skills/*.md (invocation accuracy)
- process/*.md (workflow friction, overrides)
- directives/*.md (CLAUDE.md suggestions)

### 2. Pattern Analysis
- Recurring issues
- Frequent overrides (why?)
- Guardian accuracy rates
- Skill trigger mismatches

### 3. Metaprompting (Self-Review)
For skills/agents with issues:
- Read current prompt
- Analyze failures
- Generate improved version
- Explain reasoning

### 4. Propose Changes
Format each proposal:
- WHAT: Specific change
- WHY: Evidence from logs
- IMPACT: What improves
- RISK: What could break
- DIFF: Before/after

### 5. Human Decision
- Approved → Apply, log to .ai/annealing/history/
- Rejected → Note reasoning, don't re-propose without new evidence
- Deferred → Move to backlog

### 6. Update Current World State
Review .ai/context/current-world-state.md
Update if needed (model versions, tool versions, etc.)

## Constraints
- NEVER auto-apply
- NEVER re-propose rejected without new evidence
- Always show evidence trail
- Maintain rollback capability
```

### 5.5 apprentice-mode (Detailed Teaching)

```markdown
---
name: apprentice-mode
description: Extra-detailed teaching mode with deep explanations, analogies, and step-by-step breakdowns
---

# Overview
Enhanced teaching for developers new to systematic development.
Goes beyond base apprentice mode with deeper explanations.

## When Invoked
- User requests detailed explanation
- Complex concept being introduced
- User seems confused
- First time using a pattern

## Teaching Techniques

### Analogies
Connect technical concepts to everyday things:
- Dependency injection → "Hiring a contractor: you provide tools"
- Git branches → "Parallel universes for your code"
- Middleware → "Security checkpoint at airport"

### Step-by-Step
Break complex operations into numbered steps.
Explain what each step does and why.

### Historical Context
Why does this exist? What problem did it solve?
"Before React hooks, you had to use classes because..."

### Common Mistakes
What do beginners get wrong?
"A common mistake here is... because..."

### Real Examples
Reference actual codebases, case studies:
"Netflix uses this pattern because..."

### Industry Context
Current stats, adoption, trends:
"As of 2025, 80% of enterprises use..."
```

### 5.6 guardian-builder (TDD)

```markdown
---
name: guardian-builder
description: Create new guardian agents using TDD - write test fixtures first, then build guardian to detect them
---

# Overview
TDD for guardian creation. Ensures guardians catch what they should.

## Process

### RED Phase
1. Create test fixtures with known issues
   - .ai/fixtures/[guardian-name]/bad/*.ts (should catch)
   - .ai/fixtures/[guardian-name]/good/*.ts (should pass)
2. Document expected findings for each bad file

### GREEN Phase
3. Write guardian prompt in .claude/agents/[name]-guardian.md
4. Run guardian against fixtures
5. Verify: catches all bad, passes all good
6. Iterate until 100% accuracy on fixtures

### REFACTOR Phase
7. Test against real codebase samples
8. Tune for false positive reduction
9. Add edge cases to fixtures
10. Document any known limitations

## Output
- .claude/agents/[name]-guardian.md
- .ai/fixtures/[name]/ (test fixtures)
- .ai/feedback/guardians/[name].md (initialized)
```

---

## 6. Self-Annealing Infrastructure

### 6.1 Feedback Capture

`.ai/feedback/` structure with standardized format:

```markdown
# [Category] Feedback Log

## Entry Template
### YYYY-MM-DD HH:MM
**Type:** false_positive | false_negative | friction | suggestion
**Source:** [guardian/skill/process]
**Context:** [what was happening]
**Expected:** [what should have happened]
**Actual:** [what did happen]
**Suggested Fix:** [if any]
```

### 6.2 Version Tracking with Rollback

`.ai/annealing/history/YYYY-MM-DD-applied.md`:

```markdown
# Annealing Changes Applied: YYYY-MM-DD

## Change 1
**File:** .claude/agents/security-guardian.md
**Type:** Prompt refinement
**Reason:** 3 false positives on tenant isolation in utility functions
**Before:** [hash or snippet]
**After:** [hash or snippet]
**Rollback:** git checkout [commit] -- .claude/agents/security-guardian.md

## Verification
- [ ] Change applied
- [ ] Tested against previous false positives
- [ ] No regression on true positives
```

### 6.3 Trust Escalation (Future)

After N consecutive successful annealing cycles:
- Low-risk changes could be auto-applied with notification
- Still logged and rollback-able
- Requires explicit trust escalation approval

---

## 7. MCP Configuration

`.mcp.json`:

```json
{
  "servers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-server-filesystem"],
      "status": "recommended",
      "notes": "File operations"
    },
    "git": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-server-git"],
      "status": "recommended",
      "notes": "Git operations"
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-server-github"],
      "env": {"GITHUB_TOKEN": "${GITHUB_TOKEN}"},
      "status": "recommended",
      "notes": "PR workflows"
    },
    "fetch": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-server-fetch"],
      "status": "recommended",
      "notes": "Web fetching for research"
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-server-postgres"],
      "status": "if-applicable",
      "notes": "Database access"
    },
    "puppeteer": {
      "command": "npx",
      "args": ["-y", "@anthropic/mcp-server-puppeteer"],
      "status": "if-applicable",
      "notes": "Browser automation, UI testing"
    }
  }
}
```

---

## 8. External Reviewer Integration

### 8.1 Codex (Primary)

Task file format in `.ai/tasks/review/`:

```markdown
# Review Task: [Feature Name]

**Type:** review
**Assigned:** codex
**Branch:** feature/[name]
**Created:** YYYY-MM-DD
**Status:** pending

## Context
[Current state, what to focus on]

## Files to Review
- src/path/to/file.ts (lines X-Y)
- src/other/file.ts

## Focus Areas
- Security: [specific concerns]
- Architecture: [specific concerns]
- Performance: [specific concerns]

## Deliverables
Save findings to: .ai/tasks/review/YYMMDD-codex-[feature]-findings.md

## Constraints
- Stay in scope
- Don't refactor beyond task
- Follow existing patterns
```

### 8.2 Review Flow

```
Claude completes feature
    │
    ├─→ Spawns guardian agents (Quick Swarm)
    │     ├── Security Guardian
    │     ├── TypeScript Guardian
    │     └── [relevant guardians]
    │
    ├─→ Creates Codex task file
    │     └── Pushes branch for access
    │
    ├─→ Streams guardian results as they complete
    │
    └─→ Waits for Codex (sync point)
          │
          ▼
    Synthesizes all findings
          │
          ├── Isolated fixes → Parallel agents
          └── Coupled fixes → Claude direct
          │
          ▼
    Gate check (zero issues or override)
          │
          ▼
    Merge to develop
```

---

## 9. Apprentice Mode Base Directives

Always active in CLAUDE.md (brief). Skill for detailed mode.

### Base Directives (Always On)
- Explain "why" not just "what"
- Use analogies when introducing concepts
- Define jargon on first use
- Share relevant industry context
- Warn about common mistakes

### Research-First Directive
Before recommending technologies, patterns, or tools:
- Web search when currency matters
- Check current versions/adoption
- Note recent developments

### Contextual Intelligence
When relevant, share:
- Industry statistics
- Adoption trends
- Case studies
- Cautionary tales

---

## 10. Quick Swarm vs Hive-Mind

### Quick Swarm (Guardians)
- Isolated, temporary tasks
- No memory between invocations
- Minimal context (files to review + prompt)
- Fire, execute, return, done

### Hive-Mind (Main Session)
- Persistent across feature development
- Uses session logs for continuity
- Shared context via .ai/SHARED-CONTEXT.md
- Self-annealing feedback accumulates

### Hybrid Pattern
```
Hive-Mind (Main Claude)
  ├── Quick Swarm: Security Guardian
  ├── Quick Swarm: TypeScript Guardian
  ├── Quick Swarm: Test Guardian
  └── Quick Swarm: Codex Review

  ← All results return to Hive-Mind for synthesis
```

---

## 11. Future Enhancements

Remind user when relevant:

| Enhancement | Trigger | Complexity |
|-------------|---------|------------|
| SPO for guardians | After 10+ approved annealing cycles | Medium |
| Promptbreeder evolution | When guardian accuracy plateaus | High |
| DSPy integration | When example collection is robust | High |
| Automatic trust escalation | After consistent approval rate | Medium |
| Cross-project learning | When 3+ projects use boilerplate | High |
| MCP marketplace scanning | New official MCPs announced | Low |

---

## 12. Self-Annealing Opportunities Log

Active opportunities identified during design:

| # | Opportunity | Location | Status |
|---|-------------|----------|--------|
| 1 | Guardian accuracy tracking | Guardian agents | Designed |
| 2 | Hook effectiveness metrics | Hook configuration | Designed |
| 3 | CLAUDE.md directive refinement | Directives | Designed |
| 4 | Skill trigger accuracy | Skill descriptions | Designed |
| 5 | Review finding patterns | All context files | Designed |
| 6 | Current world state maintenance | .ai/context/ | Designed |
| 7 | Promptbreeder-style evolution | Guardians | Future |
| 8 | Proactive research before recs | CLAUDE.md directive | Designed |

---

## 13. Implementation Order

1. Create directory structure
2. Write CLAUDE.md (lean, ~50-80 lines)
3. Write .claude/settings.json
4. Create guardian agents (start with security, typescript, test)
5. Create core skills (guardian-agents, feature-completion)
6. Set up .ai/ feedback infrastructure
7. Create .mcp.json
8. Create AGENTS.md and GEMINI.md
9. Write remaining guardians
10. Write remaining skills
11. Test with sample workflow
12. Document in README.md

---

## Sources

- [Claude Code Best Practices](https://www.anthropic.com/engineering/claude-code-best-practices)
- [Using CLAUDE.md Files](https://www.claude.com/blog/using-claude-md-files)
- [Claude Code Hooks Guide](https://docs.claude.com/en/docs/claude-code/hooks-guide)
- [Agent Skills Overview](https://docs.claude.com/en/docs/agents-and-tools/agent-skills/overview)
- [Subagents Documentation](https://code.claude.com/docs/en/sub-agents)
- [VoltAgent/awesome-claude-code-subagents](https://github.com/VoltAgent/awesome-claude-code-subagents)
- [wshobson/agents](https://github.com/wshobson/agents)
- [ruvnet/claude-flow](https://github.com/ruvnet/claude-flow)
- [Self-Evolving Agents Cookbook](https://cookbook.openai.com/examples/partners/self_evolving_agents/autonomous_agent_retraining)
- [Promptbreeder Paper](https://arxiv.org/abs/2309.16797)
- [SuperAGI Self-Healing AI](https://superagi.com/future-of-self-healing-ai-trends-tools-and-techniques-shaping-autonomous-systems-in-2025-and-beyond/)
- contractor-system (.ai/ patterns, guardian agents concept)
