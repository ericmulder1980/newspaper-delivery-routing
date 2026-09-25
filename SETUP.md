# Project Setup Guide

How to use this boilerplate to start a new project.

## Quick Start

```bash
# 1. Copy boilerplate to new project
cp -r claude-code-boilerplate/ my-new-project/
cd my-new-project/

# 2. Initialize git
git init

# 3. Customize CLAUDE.md for your project
# Edit the [Project Name] and tech stack sections

# 4. Start Claude Code
claude

# 5. Let Claude learn your project
/setup
```

## Step-by-Step Setup

### Step 1: Copy Boilerplate

```bash
cp -r /path/to/claude-code-boilerplate/ /path/to/my-new-project/
cd my-new-project/
```

### Step 2: Initialize Version Control

```bash
git init
git add .
git commit -m "Initial commit: Claude Code boilerplate"
```

### Step 3: Customize CLAUDE.md

Open `CLAUDE.md` and update:

- [ ] Project name (line 1)
- [ ] Tech stack (Quick Reference section)
- [ ] Key commands for your project
- [ ] Any project-specific critical rules

**Keep it lean** — The boilerplate CLAUDE.md is ~60 lines. Add detail to linked files, not here.

### Step 4: Run `/setup`

Start Claude Code and run:

```
/setup
```

**What this does:**
- Invokes the INITIALIZER agent to create domain memory
- Asks about your project purpose, features, and tech stack
- Creates features.json with initial feature breakdown
- Initializes progress.log, decisions.md, issues.md

**Important:** This is an interactive process. Answer the questions thoughtfully - this foundation shapes all future work.

### Step 5: Update Shared Context

Edit `.ai/SHARED-CONTEXT.md`:

- [ ] Project overview
- [ ] Tech stack details
- [ ] Architecture notes
- [ ] Key directories

### Step 6: Update World State

Edit `.ai/context/current-world-state.md`:

- [ ] Verify current date/year
- [ ] Check tool versions match your project
- [ ] Add any project-specific current info

### Step 7: Configure Guardians

Edit `.claude/settings.json`:

- [ ] Enable only relevant guardians
- [ ] Adjust file detection patterns
- [ ] Set up hooks for your workflow

**Example:** If not using a database, disable database-guardian:
```json
"enabled": ["security", "typescript", "architecture", "test", "performance", "docs"]
```

### Step 8: Configure MCP Servers

Edit `.mcp.json`:

- [ ] Update filesystem path
- [ ] Add GITHUB_TOKEN for GitHub integration
- [ ] Enable/disable servers based on your stack

### Step 9: First Session

Start a session with:

```
claude
```

Claude will:
1. Read CLAUDE.md
2. Understand your project structure
3. Be ready to help

**Pro tip:** Start with a planning task:
```
"Let's explore this codebase. Read the main files and
summarize the architecture. Don't write any code yet."
```

---

## Available Tools & Commands

### Slash Commands

| Command | Description |
|---------|-------------|
| `/setup` | Initialize new project with domain memory |
| `/migrate` | Add boilerplate to existing project |
| `/status` | Show current project status |
| `/plan` | Invoke PLANNER for feature planning |
| `/review` | Trigger guardian review + external reviewers |
| `/complete-feature` | Run feature completion checklist |
| `/feedback` | Log feedback for annealing |

### Skills

| Skill | When to Use |
|-------|-------------|
| `guardian-agents` | Run parallel code review |
| `feature-completion` | Complete a feature branch |
| `full-codebase-review` | Review before release |
| `weekly-annealing` | Weekly self-improvement |
| `apprentice-mode` | Detailed teaching mode |
| `guardian-builder` | Create new guardians with TDD |
| `boilerplate-sync` | Sync improvements between instances |
| `boilerplate-migrate` | Add boilerplate to existing projects |

### Guardian Agents

| Guardian | Focus |
|----------|-------|
| security-guardian | OWASP, secrets, injection |
| typescript-guardian | Types, strict mode |
| architecture-guardian | Patterns, modularity |
| test-guardian | Coverage, test quality |
| performance-guardian | N+1, complexity |
| docs-guardian | Documentation currency |
| accessibility-guardian | A11y, WCAG |
| api-guardian | REST conventions |
| database-guardian | Schema, queries |

### Key Directories

```
.claude/          # Configuration
  agents/         # Guardian subagents
  skills/         # Workflow skills
  commands/       # Slash commands
  settings.json   # Hooks, guardian config

.ai/              # Operations
  tasks/          # Work assignments
  sessions/       # Session logs
  feedback/       # Self-annealing data
  annealing/      # Improvement history
  context/        # World state
```

---

## Workflow Tips

### Research Before Coding

```
"Read src/auth/ and explain how authentication works.
Don't write any code yet."
```

Then:
```
"Now make a plan for adding OAuth support."
```

Then:
```
"Implement the plan."
```

### Use Feature Branches

```bash
git checkout -b feature/my-feature
```

Work on feature, then:
```
/complete-feature
```

### Context Management

- **One feature per session** — Keep context focused
- **Use /clear** — When switching tasks
- **Use /resume** — To continue previous work

### The `#` Key

Press `#` during a session to add instructions that get saved to CLAUDE.md:

```
# Always use pnpm, not npm
# Run tests with: pnpm test
```

### Apprentice Mode

If you want detailed explanations:

```
"Explain this like I'm new to React. Use analogies."
```

Or invoke the skill:
```
"Use apprentice-mode skill and teach me about dependency injection."
```

---

## Self-Annealing

The boilerplate improves itself over time:

1. **Log feedback** — Use `/feedback` when something goes wrong
2. **Weekly review** — Run `weekly-annealing` skill to propose improvements
3. **Approve changes** — All improvements require your approval

---

## Checklist: New Project Setup

```
[ ] Copy boilerplate to project directory
[ ] git init
[ ] Customize CLAUDE.md (project name, stack, commands)
[ ] Run /setup to initialize domain memory
[ ] Answer INITIALIZER questions about your project
[ ] Review generated features.json and decisions.md
[ ] Update .ai/SHARED-CONTEXT.md
[ ] Update .ai/context/current-world-state.md
[ ] Configure guardians in .claude/settings.json
[ ] Configure MCP servers in .mcp.json
[ ] First session: explore codebase, don't code yet
[ ] Create first feature branch and start building
```

---

## Resources

- [Claude Code Best Practices](https://www.anthropic.com/engineering/claude-code-best-practices)
- [Using CLAUDE.md Files](https://www.claude.com/blog/using-claude-md-files)
- [Claude Code Common Workflows](https://docs.claude.com/en/docs/claude-code/common-workflows)
- [Hooks Guide](https://docs.claude.com/en/docs/claude-code/hooks-guide)
- [Agent Skills Overview](https://docs.claude.com/en/docs/agents-and-tools/agent-skills/overview)
