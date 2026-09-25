# Scenario: This IS the Source Repo (Edge Case)

## Context

User runs `sync status` in a repo that IS the boilerplate source:

```json
// .boilerplate.json
{
  "source": {
    "repo": "songbirddigital/claude-code-boilerplate"
  }
}
```

```bash
$ git remote -v
origin  https://github.com/songbirddigital/claude-code-boilerplate.git (fetch)
origin  https://github.com/songbirddigital/claude-code-boilerplate.git (push)
```

The source repo in .boilerplate.json matches the origin remote.

## Input

User invokes `sync status` or `sync pull`.

## Expected Trigger

Skill should:
1. Read .boilerplate.json source
2. Check git remotes
3. Detect that origin == source (this IS the source repo)
4. Report that sync doesn't apply here
5. Explain what to do instead

## Why This Edge Case Matters

- Users might accidentally run sync in the source repo
- Syncing a repo with itself is nonsensical
- Clear guidance prevents confusion
- Source repo has different workflow (direct edits, not sync)
