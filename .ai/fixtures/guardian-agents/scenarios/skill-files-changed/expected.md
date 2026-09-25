# Expected Behavior: Skill Files Changed

## Expected Behavior

The guardian-agents skill should recognize skill files warrant both documentation AND architecture review.

### 1. File Detection Phase

- [ ] Read `.claude/settings.json` successfully
- [ ] Match `.claude/skills/**/*.md` pattern (more specific than `*.md`)
- [ ] Identify applicable guardians: `docs`, `architecture`
- [ ] Recognize skill files are system-defining, not just documentation

### 2. Guardian Spawning Phase

- [ ] Spawn exactly 2 guardians: `docs` and `architecture`
- [ ] `docs-guardian` receives all 3 skill files for documentation review
- [ ] `architecture-guardian` receives all 3 skill files for architecture review
- [ ] Use parallel dispatch pattern

### 3. Architecture Guardian Focus

When reviewing skill files, architecture guardian should check:

- [ ] **Skill structure consistency**: All skills follow same format
- [ ] **Integration points**: Skills reference each other correctly
- [ ] **No circular dependencies**: Skill A doesn't require B which requires A
- [ ] **Aligned with CLAUDE.md**: Skills mentioned in CLAUDE.md actually exist
- [ ] **Configuration alignment**: Skills work with current settings.json
- [ ] **Naming conventions**: Skill names follow pattern

### 4. Docs Guardian Focus

When reviewing skill files, docs guardian should check:

- [ ] **Completeness**: All sections present (Overview, When to Use, Process, etc.)
- [ ] **Clarity**: Instructions are unambiguous
- [ ] **Examples**: Concrete examples provided
- [ ] **Cross-references**: Links to other docs are valid

### 5. Synthesis

- [ ] Combine findings from both guardians
- [ ] Categorize by severity
- [ ] Architecture issues may be HIGH (affect system behavior)
- [ ] Documentation issues typically MEDIUM/LOW

## Success Criteria

**PASS if:**
- Both `docs` and `architecture` guardians are spawned
- Skill files recognized as system-defining (not just regular .md)
- Architecture guardian provides structural feedback
- Findings are properly categorized

**FAIL if:**
- Only `docs` guardian runs (misses architecture review)
- Skill files treated same as regular markdown
- No structural/integration checks performed

## Configuration Required

To enable this behavior, add to `.claude/settings.json`:

```json
{
  "guardians": {
    "file_detection": {
      "*.md": ["docs"],
      ".claude/skills/**/*.md": ["docs", "architecture"],
      ".claude/agents/*.md": ["docs", "architecture"]
    }
  }
}
```

More specific patterns should take precedence over general patterns.

## Edge Cases

- [ ] New skill added (no previous version to compare)
- [ ] Skill renamed (should check if old references updated)
- [ ] Skill deleted (should check nothing depends on it)
- [ ] Multiple skills modified that interact with each other
