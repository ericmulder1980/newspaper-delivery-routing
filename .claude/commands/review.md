---
description: Trigger guardian review with REVIEWER agent orchestration
---

Read @.claude/agents/REVIEWER.md and perform a code review.

Usage: /review [FEATURE-ID or file paths]

The REVIEWER will:
1. Load feature context from features.json
2. Run automated checks (tests, lint, types)
3. Check specification compliance
4. Spawn parallel guardian reviews based on files changed
5. Create Codex review task (async, ~15 min)
6. Synthesize all findings
7. Present verdict: APPROVED / CHANGES REQUESTED

Guardian file detection:
- *.sql, migrations/**, *.prisma → database guardian
- api/**, routes/** → api + security guardians
- auth/** → security guardian
- *.ts, *.tsx → typescript guardian
- *.tsx, *.jsx, *.css → accessibility guardian
- *.test.*, *.spec.* → test guardian
- *.md, docs/** → docs guardian

All findings are aggregated and prioritized by severity.

## Quick Review (no FEATURE-ID)

Reviews uncommitted changes:
- `git diff --name-only` for scope
- Spawns guardians based on detected files

## Full Review (--full flag)

Invokes `full-codebase-review` skill:
- All guardians on all files
- Extended checks (coverage, deps, architecture)
