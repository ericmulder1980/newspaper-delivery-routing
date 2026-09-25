Read @.claude/agents/PLANNER.md and help me plan a feature.

Usage: /plan [feature description or FEATURE-ID]

If a FEATURE-ID is provided, load that feature from features.json.
If a description is provided, this may be a new feature to add.

The PLANNER will:
1. Load context from features.json and decisions.md
2. Clarify requirements with user
3. Create technical design
4. Break into implementation tasks
5. Create spec file in docs/specs/
6. Update features.json with tasks

Guide the user through this process interactively.
