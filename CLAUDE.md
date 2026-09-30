# Krantenwijk

Android app giving one newspaper deliverer a street-by-street, walking-order overview of which
addresses get the newspaper and/or leaflets, based on mailbox stickers. Offline, single user, APK only.

## Quick Reference
- Stack: Kotlin · Jetpack Compose + Material 3 · MVVM + domain layer · Room · DataStore · Hilt · Coroutines/Flow
- Commands: `./gradlew assembleDebug` · `./gradlew test` (all tests incl. `:core`, via an alias task) · `./gradlew lint` · `./gradlew assembleRelease`
- Modules: `:app` (Android UI, Hilt) → `:core` (KMP: `domain` + `data`, android + jvm targets; DEC-013)
- Supported Android: 12 (API 31) to 17 (API 37), rolling "newest minus 5" (DEC-027)
- Toolchain (local, no Android Studio): prefix Gradle with `export JAVA_HOME=$(ls -d ~/Library/Java/JavaVirtualMachines/jdk-21*/Contents/Home)`; SDK at `~/Library/Android/sdk` (`local.properties`). Intel Mac, 2 cores, so builds are slow and the first run takes ~5 min
- Test builds: the user installs APKs by file transfer (no adb). Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Branch: `git branch --show-current`
- Memory: `.claude/memory/features.json`
- Spec (source of truth): `docs/specs/krantenwijk-project-plan.md`. Work one feature (plan requirement IDs in `plan_refs`) at a time
- Wireframes (approved): `reference/prototype/*.dc.html`
- Roles: Claude builds; the user tests on device and approves
- Always: EN + NL strings for every UI text (glossary in plan §8.1); `domain` package has no `android.*` imports; no INTERNET permission

---

## 🔍 STAYING CURRENT (Critical!)

**You MUST research before making ANY technical recommendations.**

### When to Search
**ALWAYS search before suggesting:**
- Libraries, frameworks, or tools
- Best practices or design patterns
- Technology choices or architectural decisions
- Any recommendation that could be outdated

### How to Search

1. **Check Current Date/Time First**
   - Read `@.ai/context/current-world-state.md` for date context
   - Your knowledge cutoff is January 2025 — assume you're outdated

2. **Search Pattern: "best [month year] [topic]"**
   ```
   Examples:
   - "best December 2025 React state management"
   - "best December 2025 AI video generators"
   - "best December 2025 TypeScript testing frameworks"
   ```

3. **For Patterns: Search GitHub First**
   ```
   - "GitHub [pattern name] 2025"
   - "GitHub react hooks patterns stars:>1000"
   - Sort by: Recently updated
   ```

4. **For Breaking Changes**
   ```
   - "[library] breaking changes 2025"
   - "[library] migration guide v[X] to v[Y]"
   - Check official changelog/release notes
   ```

### Search Examples by Category

| Category | Search Query Template |
|----------|---------------------|
| **State Management** | "best [month year] [framework] state management" |
| **Testing** | "best [month year] [language] testing framework" |
| **AI Tools** | "best [month year] AI [use case]" |
| **Database** | "best [month year] database for [use case]" |
| **Deployment** | "best [month year] hosting platform for [stack]" |
| **Security** | "[topic] security best practices 2025" |

### Red Flags (Stop and Search!)

If you're about to recommend:
- ❌ "Redux is the standard for React state" → SEARCH FIRST
- ❌ "Use Create React App" → SEARCH FIRST
- ❌ "Jest is the best testing framework" → SEARCH FIRST
- ❌ Any tool you last saw in training data → SEARCH FIRST

### After Searching

Present findings with:
- ✅ Source links (with dates when available)
- ✅ "As of [month year], the current approach is..."
- ✅ Trade-offs between current options
- ✅ Migration path if recommending change

**Example Response:**
> "I searched for 'best December 2025 React state management' and found:
> - Zustand and Jotai are now preferred over Redux for most use cases
> - React Server Components (stable in Next.js 15) reduce client state needs
> - Sources: [link1], [link2] (Dec 2025)
>
> For your use case, I recommend [X] because [reasons]."

---

## Session Protocol

**On start:**
1. Check `.claude/memory/features.json`
   - If exists → read it, show current feature/task/blockers, ask what to work on
   - If missing → prompt: "No domain memory found. Describe your project or say 'help me set up'."
2. Read tail of `.claude/memory/progress.log` for recent context
3. Announce status, ask what to work on

**Suggest initialization when:**
- features.json missing
- User describes new project or major feature
- User lists multiple things to build
- User seems uncertain what to work on
- Project has code but no features.json

**On end:**
1. Update features.json with progress
2. Append to progress.log (including handoff context)
3. Commit if appropriate

---

## Proactive Behaviors

**Follow IMPLEMENTER protocol when:**
- Working on any implementation task
- Orient → Plan → Implement → Verify → Update Memory

**Invoke PLANNER when:**
- Feature is in inception phase and needs planning
- User wants to break down a new feature

**Invoke guardians when:**
- Feature enters review phase
- User requests code review
- Before merge to develop/main

---

## Critical Rules
1. **Research ALWAYS** — Web search before ANY technical recommendation (see "STAYING CURRENT" section)
2. **No secrets in code** — Environment variables only
3. **Explicit types** — No `any` without justification
4. **Tests before commit** — All tests written and passing
5. **Memory updates** — Always update features.json and progress.log at session end
6. **Human approval** — Never auto-apply changes; all proposals require explicit approval

---

## Mode: Apprentice
Explain "why" not just "what" · Use analogies · Define jargon · Share industry context
For extra detail: invoke `apprentice-mode` skill.

---

## Domain Memory

| File | Purpose |
|------|---------|
| `.claude/memory/features.json` | Machine-readable backlog with lifecycle |
| `.claude/memory/progress.log` | Append-only session history + handoff |
| `.claude/memory/decisions.md` | Architectural Decision Records |
| `.claude/memory/issues.md` | Problem tracking with resolutions |

---

## Agents

| Agent | Use When |
|-------|----------|
| `@.claude/agents/INITIALIZER.md` | Setting up a new project |
| `@.claude/agents/PLANNER.md` | Planning a new feature |
| `@.claude/agents/IMPLEMENTER.md` | Implementing tasks (default) |
| `@.claude/agents/REVIEWER.md` | Code review before merge |

### Domain Agents (guardians + specialists)
Located at `.claude/agents/domains/[domain]/guardian.md` and `specialist.md`

Domains: security · database · architecture · api · typescript · test · performance · accessibility · docs

---

## Review Flow
1. **Guardians** (parallel) — Domain-specific reviewers invoked by REVIEWER
2. **Codex** (async) — External review via `.ai/tasks/review/` (~15 min)
3. **Synthesis** — REVIEWER aggregates findings, gates merge on zero critical issues

---

## Commands
`/setup` (new project) · `/status` (current state) · `/plan` (feature planning)
`/review` (guardian + codex) · `/complete-feature` (completion checklist)
`/migrate` (add to existing project) · `/feedback` (quick capture)

---

## Self-Annealing
Log observations → `.ai/feedback/` · Weekly review proposes improvements
Memory feedback tracked in `.ai/feedback/memory/`

---

## Context (load when relevant)
@.ai/context/current-world-state.md · @.ai/SHARED-CONTEXT.md · @docs/ARCHITECTURE.md
Spec (read when working on a feature, not auto-loaded): `docs/specs/krantenwijk-project-plan.md`

---

## See Also
@docs/plans/2025-12-09-domain-memory-integration-design.md · @.claude/settings.json

---
*Boilerplate by [Songbird Digital](https://github.com/songbirddigital)*
