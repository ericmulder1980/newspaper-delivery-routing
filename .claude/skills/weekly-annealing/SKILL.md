---
name: weekly-annealing
description: Weekly review of accumulated feedback, propose improvements for human approval. Never auto-apply changes.
---

# Weekly Annealing Review

Aggregate feedback. Identify patterns. Propose refinements. ALL proposals require explicit human approval.

## Core Principle

```
NEVER auto-apply changes.
Observe → Log → Analyze → Propose → (Human Approves) → Apply
```

## When to Use

- Weekly scheduled review
- When feedback logs are substantial
- After multiple feature completions
- When patterns emerge from reviews

## Process

### Step 1: Gather Feedback

Read all `.ai/feedback/` logs from past week:

```
.ai/feedback/
├── guardians/          # False positives, missed issues
│   ├── security.md
│   ├── typescript.md
│   └── ...
├── skills/             # Invocation accuracy
├── process/            # Workflow friction, overrides
└── directives/         # CLAUDE.md suggestions
```

### Step 2: Pattern Analysis

For each category, identify:

- **Recurring issues**: Same problem appearing multiple times
- **Frequent overrides**: Gates overridden often → too strict?
- **Accuracy rates**: Guardian precision and recall
- **Trigger mismatches**: Skills invoked when not needed, or needed but not invoked

### Step 3: Metaprompting (Self-Review)

For agents/skills with issues:

```
1. Read current prompt
2. Read feedback about failures
3. Ask: "Why is this failing?"
4. Generate improved version
5. Explain reasoning
```

### Step 4: Propose Changes

Format each proposal clearly:

```markdown
## Proposal: [Title]

### What
[Specific file and change]

### Why
[Evidence from feedback logs]

### Evidence
- [Date]: [Issue]
- [Date]: [Issue]
- [Date]: [Issue]

### Impact
- Positive: [What improves]
- Risk: [What could break]

### Diff
\`\`\`diff
- Old line
+ New line
\`\`\`

### Recommendation
[APPLY / DEFER / INVESTIGATE MORE]
```

### Step 5: Human Decision

Present all proposals to human. For each:

- **Approved**: Apply change, log to `.ai/annealing/history/`
- **Rejected**: Note reasoning, don't re-propose without new evidence
- **Deferred**: Move to backlog for later consideration

### Step 6: Apply Approved Changes

For each approved change:

```
1. Make the change
2. Log to .ai/annealing/history/YYYY-MM-DD-applied.md:
   - What changed
   - Why (evidence)
   - Rollback instructions
3. Test if possible
4. Verify no regression
```

### Step 7: Skill Ecosystem Audit

Run skill-audit as part of annealing:

```
[ ] Check recommended skills for updates
[ ] Scan curated lists for new discoveries
[ ] Review TDD queue for boilerplate skills
[ ] Present findings for approval
```

Read `.ai/skills/recommended-skills.json` and:

1. **Updates Available**
   - Web search for updates to recommended skills
   - Present changelog/diff if found
   - Options: [Apply] [Skip] [Review]

2. **New Discoveries**
   - Check curated lists for skills not in recommended or rejected
   - Evaluate relevance to project
   - Options: [Add to recommended] [Reject] [Evaluate later]

3. **TDD Queue**
   - List boilerplate skills needing fixtures
   - Prioritize by impact and usage
   - Options: [Start TDD build] [Defer]

4. **Update Tracking**
   - Set last_audit timestamp
   - Log findings to .ai/feedback/skills/

### Step 8: Update Current World State

Review and update `.ai/context/current-world-state.md`:

```
[ ] Current date/year correct
[ ] AI model versions current
[ ] Tool versions current (Node, pnpm, etc.)
[ ] Any recent announcements relevant
```

## Rollback Protocol

If an annealing change causes problems:

```
1. Check .ai/annealing/history/ for rollback instructions
2. Revert the change
3. Log the failure to .ai/feedback/
4. Mark proposal as "failed" (don't re-propose)
```

## Output

Weekly summary to `.ai/annealing/weekly/YYYY-MM-DD-review.md`:

```markdown
# Weekly Annealing Review: YYYY-MM-DD

## Feedback Reviewed
- Guardian feedback: X entries
- Skill feedback: Y entries
- Process feedback: Z entries

## Proposals Made
1. [Title] — [APPROVED/REJECTED/DEFERRED]
2. [Title] — [APPROVED/REJECTED/DEFERRED]

## Changes Applied
- [List of applied changes]

## Rejected (with reasons)
- [Proposal]: [Reason]

## Observations
[Trends, concerns, notes for next week]
```

## Constraints

1. **NEVER auto-apply** — All changes require explicit approval (unless trust-escalated)
2. **NEVER re-propose rejected** — Unless new compelling evidence
3. **Always show evidence** — No proposals without logged feedback
4. **Maintain rollback** — Every change must be reversible
5. **One week minimum** — Don't propose changes on thin evidence

---

## Trust Escalation (Graduation to Autonomous)

After sustained approval history, low-risk changes may graduate to auto-apply with notification.

### Benchmarks for Escalation

| Metric | Threshold | Why |
|--------|-----------|-----|
| Consecutive approvals | 10+ in a row | Demonstrates consistent good judgment |
| Approval rate | 95%+ over 8 weeks | Sustained accuracy, not luck |
| Zero rollbacks | 4+ weeks | Changes actually work in practice |
| Time in monitored mode | 8+ weeks minimum | Enough data to trust patterns |

### Risk Categories

Only **low-risk** changes are eligible for autonomous application:

| Risk Level | Examples | Auto-apply eligible? |
|------------|----------|---------------------|
| **Low** | Typo fixes, formatting, comment updates, documentation | Yes (after graduation) |
| **Medium** | Guardian prompt tweaks, skill description changes, config adjustments | No — always require approval |
| **High** | Critical rules, gate logic, new skills, security-related | No — always require approval |

### Escalation Process

```
1. System proposes escalation during weekly review
   "Category X has met all benchmarks. Recommend trust escalation."

2. Human explicitly approves escalation
   - Must specify which category
   - Can set additional constraints

3. Log escalation to .ai/annealing/trust-levels.json:
   {
     "escalated": {
       "documentation_typos": {
         "escalated_date": "YYYY-MM-DD",
         "approved_by": "human",
         "constraints": "none",
         "approval_history": { "approved": 12, "rejected": 0, "rollbacks": 0 }
       }
     },
     "monitored": ["guardian_prompts", "skill_descriptions"],
     "always_manual": ["critical_rules", "security", "gate_logic"]
   }

4. Auto-applied changes still logged to .ai/annealing/history/
   - Marked as "auto-applied (trust level: low)"
   - Rollback instructions included
   - Notification shown to user (not silent)
```

### De-escalation

If an auto-applied change causes issues:

```
1. Immediate rollback
2. Category reverts to manual approval
3. Reset approval counter to 0
4. Log failure reason
5. Cannot re-escalate for 4+ weeks with clean record
```

### Tracking in Weekly Review

Add to weekly summary:

```markdown
## Trust Escalation Status

### Currently Autonomous
- documentation_typos (escalated YYYY-MM-DD, 0 issues)

### Approaching Threshold
- formatting_fixes: 8/10 consecutive approvals, 4 more weeks needed

### Recently De-escalated
- [none]

### Recommendation
[ESCALATE category X / MAINTAIN current levels / DE-ESCALATE category Y]
```

### Human Controls

- **Pause all autonomous**: `"pause_autonomous": true` in settings
- **Revoke specific category**: Remove from `trust-levels.json`
- **Review auto-applied**: Check `.ai/annealing/history/` for `auto-applied` entries
- **Override**: Human can always manually reject an auto-applied change (triggers rollback)
