Show current project status from domain memory.

Read and display:
1. `.claude/memory/features.json` — current feature, phase, tasks
2. Tail of `.claude/memory/progress.log` — last session summary
3. Any open blockers from issues.md

Format as:

```
╔══════════════════════════════════════════════════════════════╗
║                    PROJECT STATUS                             ║
╠══════════════════════════════════════════════════════════════╣
║ Features: X complete │ Y in progress │ Z pending             ║
║ Current:  [FEAT-ID] - [Title]                                ║
║ Phase:    [phase] (task X/Y)                                 ║
║ Blockers: [count]                                            ║
╚══════════════════════════════════════════════════════════════╝

Last session: [summary]
Next recommended: [task]
```

If features.json doesn't exist, prompt for initialization.
