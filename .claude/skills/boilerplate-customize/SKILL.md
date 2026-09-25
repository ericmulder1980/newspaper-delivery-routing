---
name: boilerplate-customize
description: Customize boilerplate components for project-specific needs
---

# Boilerplate Customization

The boilerplate is a starting point. Your project will evolve. Customize freely.

## Philosophy

```
Boilerplate provides:     You customize for:
─────────────────────     ──────────────────
Structure                 Your workflow
Defaults                  Your preferences
Patterns                  Your domain
Guardians                 Your tech stack
Skills                    Your team's needs
```

**Nothing is sacred.** If something doesn't work for your project, change it.

## When to Offer Customization

Proactively offer customization when:

| Trigger | Offer |
|---------|-------|
| User skips greeting settings 3+ times | "Want me to change the defaults?" |
| User disables a guardian repeatedly | "Should I remove it from this project?" |
| User expresses friction ("this is annoying") | "I can adjust that. What would work better?" |
| After 5+ sessions | "We've been working together a bit. Anything you'd like to customize?" |
| User asks "can I change..." | Immediately help customize |
| Annealing notices pattern | "I've noticed you always X. Want me to make that the default?" |

## Customization Menu

When user wants to customize, offer this menu:

```
**Customize Boilerplate**

What would you like to adjust?

1. **Greeting** — Change format, content, or disable
2. **Session Settings Defaults** — Change what's on by default
3. **Guardians** — Enable/disable, adjust rules, add new
4. **Hooks** — Add project-specific hooks
5. **Skills** — Modify or create project-specific skills
6. **Commands** — Add custom slash commands
7. **Verbosity** — Adjust how much I explain
8. **Sync Behavior** — What syncs, what stays local

Pick a number, or describe what you want to change.
```

---

## 1. Greeting Customization

### Format Options

```
Greeting format options:

a) Full (current) — Status + last session + next + highlight + settings
b) Brief — Status + what's next only
c) Minimal — Just "Ready. What's up?"
d) Custom — Define your own template

Current: [a]
Change to:
```

### Custom Template

```markdown
# .ai/config/greeting-template.md

## My Greeting Template

[time-greeting]

**Status:** [branch] | [uncommitted-count] files
**Last:** [last-commit-message]

[if:tasks]
Next up: [first-task]
[/if]

---
[custom-message: "Let's ship it."]
```

### Disable Greeting

```json
// .boilerplate.json
{
  "customizations": {
    "greeting": {
      "enabled": false,
      "reason": "User prefers to jump straight in"
    }
  }
}
```

---

## 2. Session Settings Defaults

Change what's checked by default at session start:

```json
// .boilerplate.json
{
  "session_defaults": {
    "verbosity": "minimal",      // was: "full"
    "guardians": "all",
    "apprentice": "off",         // was: "on"
    "annealing": "active",
    "external_review": "disabled", // was: "enabled"
    "session_docs": "light",     // was: "full"
    "sync_check": "skip"
  }
}
```

Or skip the menu entirely:
```json
{
  "session_defaults": {
    "skip_menu": true,
    "use_defaults": true
  }
}
```

---

## 3. Guardian Customization

### Disable a Guardian

```json
// .claude/settings.json
{
  "guardians": {
    "enabled": [
      "security",
      "typescript",
      // "accessibility",  ← disabled for CLI-only project
      "test"
    ]
  }
}
```

Mark as customized so sync doesn't re-enable:
```json
// .boilerplate.json
{
  "customized": [
    ".claude/settings.json#guardians.enabled"
  ]
}
```

### Modify Guardian Rules

```markdown
# .claude/agents/typescript-guardian.md (customized)

[... boilerplate content ...]

---
## Project-Specific Rules

### Allow `any` in migration files
Files matching `migrations/*.ts` may use `any` for legacy compatibility.

### Stricter on API boundaries
Files in `src/api/` require explicit return types on all exported functions.
```

### Add a Custom Guardian

```markdown
# .claude/agents/domain-guardian.md

---
name: domain-guardian
description: Reviews code for domain-specific patterns (e-commerce)
tools: Read, Grep, Glob
model: haiku
---

You are the Domain Guardian for an e-commerce platform.

## Focus Areas
- Price calculations use Decimal, not float
- Currency is always explicit
- Tax calculations are isolated
- Inventory checks are atomic

## Output Format
[standard guardian format]
```

Register in settings:
```json
{
  "guardians": {
    "enabled": [..., "domain"],
    "custom": ["domain-guardian"]
  }
}
```

---

## 4. Hook Customization

### Add Project-Specific Hooks

```json
// .claude/settings.json
{
  "hooks": {
    "PreToolUse": [
      // ... boilerplate hooks ...
      {
        "matcher": "Bash:npm publish",
        "hooks": [{
          "type": "command",
          "command": "echo 'Remember to update CHANGELOG!'"
        }]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Write:src/api/*",
        "hooks": [{
          "type": "command",
          "command": "echo 'API file changed - update OpenAPI spec?'"
        }]
      }
    ]
  }
}
```

### Hook Ideas by Project Type

| Project Type | Useful Hooks |
|--------------|--------------|
| Library | Pre-publish changelog reminder |
| API | Post-write OpenAPI sync check |
| Monorepo | Pre-commit affected packages check |
| Database-heavy | Post-migration seed data reminder |
| Deployed service | Pre-commit env var check |

---

## 5. Skill Customization

### Modify Existing Skill

1. Copy skill to project
2. Mark as customized
3. Edit freely

```bash
# Skill is now project-local
.claude/skills/session-management/SKILL.md  # ← your version
```

```json
// .boilerplate.json
{
  "customized": [
    ".claude/skills/session-management/SKILL.md"
  ]
}
```

### Create Project-Specific Skill

```markdown
# .claude/skills/deploy-workflow/SKILL.md

---
name: deploy-workflow
description: Project-specific deployment process
---

# Deploy Workflow

## Pre-Deploy Checklist
[ ] All tests passing
[ ] CHANGELOG updated
[ ] Version bumped
[ ] Migrations reviewed

## Deploy Steps
1. ...
```

---

## 6. Command Customization

### Add Custom Commands

```markdown
# .claude/commands/deploy.md

Run the deploy workflow for this project.

Steps:
1. Run tests
2. Build production bundle
3. Run deploy-workflow skill checklist
4. Push to production branch
```

```markdown
# .claude/commands/standup.md

Generate a standup summary:
- What I did yesterday (from git log)
- What's planned (from .ai/tasks/)
- Any blockers (from session notes)
```

---

## 7. Verbosity Customization

### Global Verbosity Setting

```json
// .boilerplate.json
{
  "customizations": {
    "verbosity": {
      "default": "minimal",
      "apprentice_mode": false,
      "feature_highlights": false,
      "guardian_announcements": false,
      "annealing_mentions": false
    }
  }
}
```

### Context-Aware Verbosity

```json
{
  "customizations": {
    "verbosity": {
      "default": "minimal",
      "on_error": "detailed",      // explain more when things break
      "on_new_pattern": "full",    // explain new concepts
      "on_request": "full"         // "explain this" triggers detail
    }
  }
}
```

---

## 8. Sync Customization

### What Syncs vs Stays Local

```json
// .boilerplate.json
{
  "sync": {
    "pull": {
      "agents": true,           // get guardian updates
      "skills": true,           // get skill updates
      "commands": false,        // keep my commands
      "settings_hooks": false   // keep my hooks
    },
    "push": {
      "auto_candidate": false,  // don't auto-suggest upstream
      "require_confirmation": true
    }
  }
}
```

---

## Tracking Customizations

All customizations tracked in `.boilerplate.json`:

```json
{
  "customized": [
    "CLAUDE.md",
    ".claude/agents/typescript-guardian.md",
    ".claude/settings.json#guardians.enabled",
    ".claude/skills/session-management/SKILL.md"
  ],
  "customizations": {
    "greeting": { "format": "brief" },
    "session_defaults": { "verbosity": "minimal" },
    "verbosity": { "default": "minimal" }
  },
  "custom_components": {
    "guardians": ["domain-guardian"],
    "skills": ["deploy-workflow"],
    "commands": ["deploy", "standup"]
  }
}
```

---

## Proactive Customization Prompts

Claude should notice patterns and offer:

### After Repeated Behavior

```
I've noticed you disable the accessibility guardian every session.

Options:
1. Disable it by default for this project
2. Keep asking (maybe you'll need it sometimes)
3. Remove it entirely from this project

What works best?
```

### After Friction

```
You mentioned the greeting is "too much."

I can make it:
- Brief (just status + next task)
- Minimal (just "Ready.")
- Off (no greeting, just wait for you)

Or tell me what you'd prefer.
```

### Periodic Check-in

```
We've been working together for 2 weeks now.

Anything about how I work that you'd like to adjust?
- Greeting format
- How much I explain
- Which guardians run
- Something else

Or say "all good" and we'll keep going.
```

---

## Reset to Defaults

If customization goes wrong:

```bash
# Reset single file
git checkout boilerplate/main -- .claude/agents/typescript-guardian.md

# Reset all customizations
# 1. Clear customized array in .boilerplate.json
# 2. Run sync pull --force
```

Or in conversation:
```
"Reset the greeting to boilerplate defaults"
"Undo my typescript guardian customizations"
"Start fresh with boilerplate settings"
```
