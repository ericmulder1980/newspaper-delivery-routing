---
name: skill-audit
description: Self-improving skill registry - tracks recommended skills, checks for updates, validates TDD status
---

# Skill Audit

Keep the boilerplate's skill ecosystem healthy and current.

## Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    SKILL AUDIT SYSTEM                        │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  Recommended │  │  Boilerplate │  │  Discovery   │       │
│  │  Skills      │  │  Skills      │  │  Queue       │       │
│  │  (external)  │  │  (internal)  │  │  (new finds) │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
│         │                 │                 │                │
│         ▼                 ▼                 ▼                │
│  ┌─────────────────────────────────────────────────────┐    │
│  │              .ai/skills/recommended-skills.json      │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                  │
│                           ▼                                  │
│                   Weekly Annealing                           │
│                   Integration                                │
└─────────────────────────────────────────────────────────────┘
```

## When to Run

| Trigger | Action |
|---------|--------|
| Weekly annealing | Full audit |
| `/audit skills` | Manual full audit |
| New skill added to boilerplate | Add to TDD queue |
| User mentions "skill" + "update" | Check specific skill |

---

## Audit Process

### 1. Check Recommended Skills for Updates

```markdown
For each skill in recommended-skills.json → recommended[]:

1. Check source repo for updates
   - GitHub: fetch releases/commits since last_checked
   - Plugin: check plugin marketplace version

2. If updates found:
   - Log to .ai/feedback/skills/updates.md
   - Add to annealing review queue

3. Update last_validated timestamp
```

**Web search template:**
```
"[skill-name]" OR "[source-repo]" site:github.com updates 2025
```

### 2. Scan Curated Lists for New Skills

```markdown
For each source with type: "curated-list":

1. Fetch current list (web search or fetch)
2. Compare against recommended[] and rejected[]
3. New skills → add to discovery_queue[]
4. Present discoveries during annealing
```

**Discovery criteria:**
- Stars > 100 (for repos)
- Addresses a gap in current skills
- Actively maintained (commits in last 3 months)
- Not already in rejected[]

### 3. Validate Boilerplate Skills TDD Status

```markdown
For each skill in boilerplate_skills[]:

1. Check if .ai/fixtures/[skill-name]/ exists
2. Check for test scenarios:
   - good/ (should pass)
   - bad/ (should catch)
3. Update tdd_status:
   - "validated" — has fixtures, tests pass
   - "needs_fixtures" — no test fixtures
   - "failing" — has fixtures, tests fail
   - "describes_tdd" — describes TDD but isn't tested itself
```

### 4. Prioritize TDD Queue

```markdown
Order boilerplate_skills by:
1. priority: high > medium > low
2. tdd_status: needs_fixtures > failing > describes_tdd
3. Usage frequency (if tracked)

Output: Ranked list for annealing review
```

---

## Audit Report Format

```markdown
# Skill Audit Report: YYYY-MM-DD

## Recommended Skills Status

| Skill | Source | Status | Last Update |
|-------|--------|--------|-------------|
| superpowers:writing-skills | obra/superpowers | ✓ current | 2025-11-28 |
| superpowers:test-driven-development | obra/superpowers | ⚠ update available | 2025-12-01 |

### Updates Available
- **superpowers:test-driven-development**: v2.1.0 → v2.2.0
  - Changelog: Added support for async test patterns
  - Recommendation: Update (non-breaking)

## Boilerplate Skills TDD Status

| Skill | TDD Status | Priority | Action |
|-------|------------|----------|--------|
| guardian-agents | needs_fixtures | high | Create test scenarios |
| boilerplate-sync | needs_fixtures | high | Create merge conflict tests |
| session-management | needs_fixtures | medium | Create greeting edge cases |

### TDD Queue (prioritized)
1. guardian-agents — Core orchestration, highest impact
2. boilerplate-sync — New, untested merge logic
3. session-management — User-facing, edge cases matter

## New Discoveries

Found in curated lists, not yet evaluated:

| Skill | Source | Stars | Description |
|-------|--------|-------|-------------|
| claude-epub-skill | BehiSecc/awesome | 200 | EPUB creation |
| aws-skills | BehiSecc/awesome | 450 | AWS CDK patterns |

### Evaluation Needed
- [ ] claude-epub-skill — Relevant to this project?
- [ ] aws-skills — Infrastructure needed?

## Rejected (won't recommend)

| Skill | Reason | Date |
|-------|--------|------|
| [none yet] | | |

---

*Next audit: YYYY-MM-DD (7 days)*
```

---

## Integration with Annealing

Add to weekly-annealing skill:

```markdown
### Skill Ecosystem Health

During weekly annealing, also run skill-audit:

1. Check for recommended skill updates
2. Scan curated lists for new discoveries
3. Review TDD queue status
4. Present findings for approval:
   - Updates to apply
   - New skills to add to recommended
   - Skills to move from needs_fixtures to building queue
```

**Annealing questions:**

```
## Skill Audit Findings

### Updates Available
- superpowers:tdd v2.2.0 is available (you have v2.1.0)
  - [Apply update] [Skip] [Review changelog]

### New Discoveries
- aws-skills looks useful for infrastructure projects
  - [Add to recommended] [Reject] [Evaluate later]

### TDD Queue
Ready to build fixtures for:
1. guardian-agents (high priority)
   - [Start TDD build] [Defer] [Deprioritize]
```

---

## Building TDD Fixtures

When user approves building fixtures for a skill:

### 1. Invoke guardian-builder Pattern

The guardian-builder skill describes TDD for guardians.
Apply same pattern to skills:

```markdown
### RED Phase
1. Create .ai/fixtures/[skill-name]/
2. Create test scenarios:
   - scenarios/good/ — inputs that should work
   - scenarios/bad/ — inputs that should fail or trigger warnings
3. Document expected behavior for each

### GREEN Phase
4. Run skill against fixtures
5. Verify correct behavior
6. Iterate until 100% accuracy

### REFACTOR Phase
7. Optimize skill prompts
8. Add edge cases discovered
9. Document limitations
```

### 2. Fixture Structure

```
.ai/fixtures/
├── guardian-agents/
│   ├── scenarios/
│   │   ├── single-file-change/
│   │   │   ├── input.md          # Simulated context
│   │   │   └── expected.md       # Expected guardian dispatch
│   │   ├── multi-file-change/
│   │   │   ├── input.md
│   │   │   └── expected.md
│   │   └── no-matching-guardians/
│   │       ├── input.md
│   │       └── expected.md
│   └── README.md                 # Test instructions
├── session-management/
│   ├── scenarios/
│   │   ├── fresh-project/
│   │   ├── continuing-work/
│   │   ├── after-annealing/
│   │   └── missing-session-log/
│   └── README.md
└── ...
```

### 3. Validation Process

```markdown
For each scenario:
1. Load input.md as simulated context
2. Run skill (or subagent with skill)
3. Compare output to expected.md
4. Log results to .ai/fixtures/[skill]/results.md

Pass criteria:
- All good/ scenarios produce expected output
- All bad/ scenarios are handled correctly
- No false positives/negatives
```

---

## Commands

### `/audit skills`

Run full skill audit manually.

### `/audit skills update`

Check and apply recommended skill updates.

### `/audit skills tdd [skill-name]`

Start TDD fixture building for a specific skill.

### `/audit skills discover`

Scan curated lists for new skills.

---

## Self-Annealing

Track audit effectiveness:

```markdown
.ai/feedback/skills/audit-effectiveness.md

### YYYY-MM-DD
**Audit finding:** superpowers:tdd update available
**Action taken:** Applied update
**Result:** Caught 2 additional test issues in next session
**Signal:** Update was valuable

### YYYY-MM-DD
**Audit finding:** aws-skills discovered
**Action taken:** Added to recommended
**Result:** Not used in 30 days
**Signal:** Over-recommended? Move to conditional?
```

---

## Open Source References

This system is informed by:

- [obra/superpowers](https://github.com/obra/superpowers) — Core skills library
- [travisvn/awesome-claude-skills](https://github.com/travisvn/awesome-claude-skills) — Curated list
- [hesreallyhim/awesome-claude-code](https://github.com/hesreallyhim/awesome-claude-code) — Workflows & tools
- [nizos/tdd-guard](https://github.com/nizos/tdd-guard) — TDD enforcement
- [Anthropic skill best practices](https://docs.claude.com/en/docs/agents-and-tools/agent-skills/best-practices)

**Philosophy:** Don't reinvent. Reference, adapt, improve, contribute back.
