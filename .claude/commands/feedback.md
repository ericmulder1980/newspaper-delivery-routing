---
description: Quick capture of feedback for self-annealing
---

# Quick Feedback

Capture observations for self-annealing review.

## Usage

```
/feedback                  # Interactive mode
/feedback guardian security "false positive on utility function"
/feedback process "review took too long due to X"
```

## Categories

- **guardian** — Guardian accuracy (false positives, misses)
- **skill** — Skill invocation issues
- **process** — Workflow friction points
- **directive** — CLAUDE.md improvement suggestions

## Interactive Mode

Prompts for:
1. Category (guardian/skill/process/directive)
2. Subcategory (which guardian, which skill, etc.)
3. What happened
4. What should have happened
5. Suggested improvement (optional)

## Log Format

Appends to appropriate `.ai/feedback/[category]/[subcategory].md`:

```markdown
### YYYY-MM-DD HH:MM
**Type:** false_positive | false_negative | friction | suggestion
**Context:** [what was happening]
**Expected:** [what should have happened]
**Actual:** [what did happen]
**Suggestion:** [improvement idea if any]
```

## Review

Feedback is reviewed in weekly annealing sessions.
Use `/feedback` liberally — small observations lead to meaningful improvements.

## Examples

```
/feedback guardian typescript "flagged any on line 45 but it was justified with comment"

/feedback process "feature completion took 3 rounds because test guardian kept finding new issues"

/feedback directive "add reminder about checking branch before starting work"
```
