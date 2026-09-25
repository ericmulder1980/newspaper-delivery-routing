# Changelog

All notable changes to the claude-code-boilerplate will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Domain memory integration (features.json, progress.log, decisions.md, issues.md)
- Core agent protocols (INITIALIZER, PLANNER, IMPLEMENTER, REVIEWER)
- Specialists for 6 domains (security, database, architecture, api, test, performance)
- `/setup` command for new project initialization
- `/migrate` command for existing project migration
- `/status` command to show current project status
- `/plan` command to invoke PLANNER for feature planning
- Memory feedback infrastructure (`.ai/feedback/memory/`)
- Session protocol inline in CLAUDE.md
- CHANGELOG.md for version tracking

### Changed
- Moved all guardians to `.claude/agents/domains/` structure
- Updated CLAUDE.md with session protocol (~170 lines)
- Reorganized `.ai/feedback/` to include memory subdirectory
- Updated `/review` command to use REVIEWER orchestration
- Updated documentation to use `/setup` instead of `/init`

### Deprecated
- `session-management` skill (protocol now inline in CLAUDE.md)

### Fixed
- Documentation inconsistencies between README.md and SETUP.md
- Outdated skills list in documentation

## [0.1.0] - 2025-12-03

### Added
- Initial boilerplate structure
- 9 Guardian agents (security, typescript, architecture, test, performance, docs, accessibility, api, database)
- Self-annealing infrastructure
- Apprentice mode
- Feature completion workflow
- External review integration (Codex/Gemini)
- Hooks for auto-formatting and sensitive file blocking
- TDD fixtures for guardian testing
- Guardian builder skill

### Documentation
- README.md with quick start guide
- SETUP.md with detailed setup instructions
- Design document (docs/plans/2025-12-03-claude-code-boilerplate-design.md)
- CLAUDE.md entry point
- AGENTS.md and GEMINI.md for external reviewers

---

## Version Strategy

- **Major (X.0.0)**: Breaking changes to structure, backward-incompatible updates
- **Minor (0.X.0)**: New features, new guardians, new skills
- **Patch (0.0.X)**: Bug fixes, documentation updates, minor improvements

## Upgrade Notes

When upgrading between versions:
1. Check CHANGELOG for breaking changes
2. Use `/migrate update` to pull latest structure
3. Review and merge any CLAUDE.md updates
4. Test with `/status` to verify domain memory intact
