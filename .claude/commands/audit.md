# Audit Skills

Invoke the `skill-audit` skill to check the health of the skill ecosystem.

## Usage

- `/audit skills` — Full audit (updates, discoveries, TDD queue)
- `/audit skills update` — Check for updates to recommended skills
- `/audit skills discover` — Scan curated lists for new skills
- `/audit skills tdd` — Review TDD queue status
- `/audit skills tdd [skill-name]` — Start TDD fixture building

## What It Checks

1. **Recommended Skills** — Are external skills up to date?
2. **Curated Lists** — Any new skills worth adding?
3. **Boilerplate Skills** — Do they have TDD fixtures?
4. **Open Source References** — Are we using the best available?

## Sources Tracked

- [obra/superpowers](https://github.com/obra/superpowers) — Core skills library
- [travisvn/awesome-claude-skills](https://github.com/travisvn/awesome-claude-skills) — Curated list
- [hesreallyhim/awesome-claude-code](https://github.com/hesreallyhim/awesome-claude-code) — Workflows & tools
- [Anthropic best practices](https://docs.claude.com/en/docs/agents-and-tools/agent-skills/best-practices)

## Also Runs During

- Weekly annealing (automatic)
- When skill issues are logged to feedback

## Registry

See `.ai/skills/recommended-skills.json` for full tracking.
