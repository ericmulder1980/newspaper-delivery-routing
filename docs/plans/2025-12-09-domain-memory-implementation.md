# Domain Memory Integration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Integrate domain memory architecture into claude-code-boilerplate, creating a unified system where persistent state (features.json, progress.log, decisions.md, issues.md) enables long-running agent success.

**Architecture:** Domain memory files in `.claude/memory/` provide project state. Guardians move to `.claude/agents/domains/*/guardian.md` with specialists alongside. Session protocol moves from skill to CLAUDE.md for reliability. REVIEWER agent orchestrates guardians during review phase.

**Tech Stack:** Markdown files, JSON (features.json), shell commands, Claude Code skills/commands

---

## Phase 1: Create Domain Memory Scaffold

### Task 1.1: Create memory directory structure

**Files:**
- Create: `.claude/memory/`
- Create: `.claude/templates/`
- Create: `docs/specs/`

**Step 1: Create directories**

```bash
mkdir -p .claude/memory
mkdir -p .claude/templates
mkdir -p docs/specs
```

**Step 2: Verify directories exist**

Run: `ls -la .claude/memory .claude/templates docs/specs`
Expected: Empty directories exist

**Step 3: Commit**

```bash
git add .claude/memory .claude/templates docs/specs
git commit -m "chore: create domain memory directory structure"
```

---

### Task 1.2: Create features.json template

**Files:**
- Create: `.claude/memory/features.json`

**Step 1: Create the file**

```json
{
  "project": "[PROJECT-NAME]",
  "version": "0.1.0",
  "last_updated": null,
  "metadata": {
    "created": null,
    "initialized_by": "user",
    "tech_stack": [],
    "repository": null
  },
  "features": [],
  "phases_legend": {
    "inception": "Feature has been identified but not yet planned",
    "planning": "Requirements and approach being defined",
    "implementation": "Active development",
    "testing": "Code complete, being tested",
    "review": "Tests passing, awaiting code review",
    "merged": "Approved and merged to main branch"
  },
  "status_legend": {
    "pending": "Not started",
    "in_progress": "Currently being worked on",
    "blocked": "Cannot proceed without external input",
    "complete": "Phase finished successfully",
    "skipped": "Phase intentionally skipped"
  }
}
```

**Step 2: Verify JSON is valid**

Run: `cat .claude/memory/features.json | python3 -m json.tool > /dev/null && echo "Valid JSON"`
Expected: "Valid JSON"

**Step 3: Commit**

```bash
git add .claude/memory/features.json
git commit -m "feat(memory): add features.json template"
```

---

### Task 1.3: Create progress.log template

**Files:**
- Create: `.claude/memory/progress.log`

**Step 1: Create the file**

```
# Progress Log
# Append-only entries documenting each work session
# Format: SESSION START/END markers with structured content

================================================================================
[TEMPLATE - DELETE AFTER FIRST REAL ENTRY]

[YYYY-MM-DD HH:MM] SESSION START
Feature: [FEATURE-ID] — [Feature Title]
Task: [TASK-ID] — [Task Description]
Context loaded: features.json, progress.log tail, decisions.md
================================================================================

[HH:MM] ACTION: [What was done]
  - [Details]

[HH:MM] TEST: [Test results]
  - [X/Y passing]

[HH:MM] DECISION: [Decision made] (see DEC-XXX)

[HH:MM] BLOCKER: [Description]

================================================================================
[YYYY-MM-DD HH:MM] SESSION END
Feature: [ID] — [Title]
Task: [TASK-ID] — [Description]

COMPLETED:
- [What was accomplished]

IN PROGRESS:
- [What was started but not finished]

TESTS: [X/Y passing]

DECISIONS: [DEC-XXX — brief description]

FILES CHANGED:
+ [new files]
M [modified files]

BLOCKERS: [Any blockers]

NEXT SESSION:
- [Recommended next actions]
- [Open questions]

CONTEXT FOR NEXT SESSION:
[Free-form notes for handoff]

Commit: [hash] "[message]"
================================================================================

# ============================================================================
# ACTUAL PROGRESS BEGINS BELOW
# ============================================================================

```

**Step 2: Verify file created**

Run: `head -20 .claude/memory/progress.log`
Expected: Shows template header

**Step 3: Commit**

```bash
git add .claude/memory/progress.log
git commit -m "feat(memory): add progress.log template"
```

---

### Task 1.4: Create decisions.md template

**Files:**
- Create: `.claude/memory/decisions.md`

**Step 1: Create the file**

```markdown
# Architectural Decision Records

Track significant decisions. Each decision is immutable once accepted — supersede rather than edit.

---

## Decision Template

### DEC-XXX: [Title]
**Date:** YYYY-MM-DD
**Status:** Proposed | Accepted | Superseded by DEC-YYY | Deprecated
**Deciders:** [Who made this decision]
**Related:** [Feature IDs, Issue IDs, other Decision IDs]

**Context:**
[What is the issue that we're seeing that is motivating this decision?]

**Options Considered:**
1. **[Option A]**
   - Pros: [...]
   - Cons: [...]

2. **[Option B]**
   - Pros: [...]
   - Cons: [...]

**Decision:**
[What is the change that we're proposing and/or doing?]

**Rationale:**
[Why did we choose this option over others?]

**Consequences:**
- [What becomes easier]
- [What becomes harder]
- [What new constraints are introduced]

---

## Active Decisions

*No decisions recorded yet. First decision will be added during project initialization.*

---

## Superseded/Deprecated Decisions

*Decisions that have been replaced or are no longer relevant go here for historical reference.*
```

**Step 2: Verify file created**

Run: `head -30 .claude/memory/decisions.md`
Expected: Shows template with Decision Template section

**Step 3: Commit**

```bash
git add .claude/memory/decisions.md
git commit -m "feat(memory): add decisions.md template"
```

---

### Task 1.5: Create issues.md template

**Files:**
- Create: `.claude/memory/issues.md`

**Step 1: Create the file**

```markdown
# Issue Tracker

Track problems and their resolutions. Build institutional knowledge.

---

## Issue Template

### ISS-XXX: [Title]
**Reported:** YYYY-MM-DD
**Status:** Open | Investigating | Blocked | Resolved
**Severity:** Critical | High | Medium | Low
**Related Feature:** [FEATURE-ID]

**Symptoms:**
- [What was observed]
- [Error messages if any]

**Investigation Log:**
- [HH:MM] [What was checked/tried]
- [HH:MM] [Results]

**Root Cause:**
[Once identified — what actually caused the issue]

**Resolution:**
[How it was fixed]

**Prevention:**
- [ ] Added test case
- [ ] Added validation
- [ ] Updated documentation

**Time to Resolution:** [X hours/days]
**Related Commits:** [commit hashes]

---

## Open Issues

*No open issues.*

---

## Resolved Issues

*Resolved issues are moved here with full investigation and resolution notes.*

---

## Common Patterns

As issues accumulate, document patterns here:

### Environment Issues
*TBD*

### Integration Issues
*TBD*

### Performance Issues
*TBD*
```

**Step 2: Verify file created**

Run: `head -30 .claude/memory/issues.md`
Expected: Shows template with Issue Template section

**Step 3: Commit**

```bash
git add .claude/memory/issues.md
git commit -m "feat(memory): add issues.md template"
```

---

### Task 1.6: Create feature-spec.md template

**Files:**
- Create: `.claude/templates/feature-spec.md`

**Step 1: Create the file**

```markdown
# [FEATURE-ID]: [Feature Title]

> Copy this template to docs/specs/[FEATURE-ID]-spec.md when planning a feature.

---

## Overview

[One paragraph description of what this feature does and why it matters]

---

## User Stories

### Primary Story
As a [type of user], I want to [action] so that [benefit].

### Additional Stories
- As a [user], I want to [action] so that [benefit]

---

## Acceptance Criteria

- [ ] [Criterion 1: specific, testable]
- [ ] [Criterion 2: specific, testable]
- [ ] [Criterion 3: specific, testable]

---

## Out of Scope

Explicitly NOT included in this feature:
- [Thing 1]
- [Thing 2]

---

## Technical Design

### Architecture Overview
[How does this fit into the existing system?]

### Data Model Changes
```typescript
// New types or schema changes
```

### API Changes
```
[HTTP Method] /api/[endpoint]
Request:  { ... }
Response: { ... }
```

### UI Changes
[New screens, modified components]

---

## Implementation Tasks

| ID | Task | Estimate | Depends On |
|----|------|----------|------------|
| [FEAT-XXX-T1] | [Task description] | S/M/L | - |
| [FEAT-XXX-T2] | [Task description] | S/M/L | T1 |

---

## Test Plan

### Unit Tests
- [ ] [Component/function]: [What to test]

### Integration Tests
- [ ] [Scenario]: [What to test]

### E2E Tests
- [ ] [User flow]: [What to test]

---

## Rollout Plan

- [ ] Feature flag: `[flag_name]`
- [ ] Metrics to monitor: [metrics]
- [ ] Success criteria: [how to know it's working]
- [ ] Rollback plan: [how to undo]

---

## Open Questions

- [ ] [Question that needs answering]

---

## Related

- Decisions: [DEC-XXX]
- Issues: [ISS-XXX]
- Dependencies: [FEAT-XXX]

---

**Created:** [date]
**Status:** Draft | Ready for Implementation | In Progress | Complete
```

**Step 2: Verify file created**

Run: `wc -l .claude/templates/feature-spec.md`
Expected: ~100 lines

**Step 3: Commit**

```bash
git add .claude/templates/feature-spec.md
git commit -m "feat(templates): add feature-spec.md template"
```

---

## Phase 2: Restructure Agents Directory

### Task 2.1: Create domains directory structure

**Files:**
- Create: `.claude/agents/domains/security/`
- Create: `.claude/agents/domains/database/`
- Create: `.claude/agents/domains/architecture/`
- Create: `.claude/agents/domains/api/`
- Create: `.claude/agents/domains/typescript/`
- Create: `.claude/agents/domains/test/`
- Create: `.claude/agents/domains/performance/`
- Create: `.claude/agents/domains/accessibility/`
- Create: `.claude/agents/domains/docs/`

**Step 1: Create all domain directories**

```bash
mkdir -p .claude/agents/domains/{security,database,architecture,api,typescript,test,performance,accessibility,docs}
```

**Step 2: Verify directories exist**

Run: `ls -la .claude/agents/domains/`
Expected: 9 directories listed

**Step 3: Commit**

```bash
git add .claude/agents/domains
git commit -m "chore: create domain agent directory structure"
```

---

### Task 2.2: Move security guardian to domains

**Files:**
- Move: `.claude/agents/security-guardian.md` → `.claude/agents/domains/security/guardian.md`

**Step 1: Move the file**

```bash
mv .claude/agents/security-guardian.md .claude/agents/domains/security/guardian.md
```

**Step 2: Verify move**

Run: `ls .claude/agents/domains/security/`
Expected: guardian.md

**Step 3: Commit**

```bash
git add .claude/agents/security-guardian.md .claude/agents/domains/security/guardian.md
git commit -m "refactor: move security guardian to domains structure"
```

---

### Task 2.3: Move database guardian to domains

**Files:**
- Move: `.claude/agents/database-guardian.md` → `.claude/agents/domains/database/guardian.md`

**Step 1: Move the file**

```bash
mv .claude/agents/database-guardian.md .claude/agents/domains/database/guardian.md
```

**Step 2: Verify move**

Run: `ls .claude/agents/domains/database/`
Expected: guardian.md

**Step 3: Commit**

```bash
git add .claude/agents/database-guardian.md .claude/agents/domains/database/guardian.md
git commit -m "refactor: move database guardian to domains structure"
```

---

### Task 2.4: Move remaining guardians to domains

**Files:**
- Move: `.claude/agents/architecture-guardian.md` → `.claude/agents/domains/architecture/guardian.md`
- Move: `.claude/agents/api-guardian.md` → `.claude/agents/domains/api/guardian.md`
- Move: `.claude/agents/typescript-guardian.md` → `.claude/agents/domains/typescript/guardian.md`
- Move: `.claude/agents/test-guardian.md` → `.claude/agents/domains/test/guardian.md`
- Move: `.claude/agents/performance-guardian.md` → `.claude/agents/domains/performance/guardian.md`
- Move: `.claude/agents/accessibility-guardian.md` → `.claude/agents/domains/accessibility/guardian.md`
- Move: `.claude/agents/docs-guardian.md` → `.claude/agents/domains/docs/guardian.md`

**Step 1: Move all remaining guardians**

```bash
mv .claude/agents/architecture-guardian.md .claude/agents/domains/architecture/guardian.md
mv .claude/agents/api-guardian.md .claude/agents/domains/api/guardian.md
mv .claude/agents/typescript-guardian.md .claude/agents/domains/typescript/guardian.md
mv .claude/agents/test-guardian.md .claude/agents/domains/test/guardian.md
mv .claude/agents/performance-guardian.md .claude/agents/domains/performance/guardian.md
mv .claude/agents/accessibility-guardian.md .claude/agents/domains/accessibility/guardian.md
mv .claude/agents/docs-guardian.md .claude/agents/domains/docs/guardian.md
```

**Step 2: Verify all moves**

Run: `find .claude/agents/domains -name "guardian.md" | wc -l`
Expected: 9

**Step 3: Commit**

```bash
git add .claude/agents/
git commit -m "refactor: move all guardians to domains structure"
```

---

### Task 2.5: Add security specialist from PM boilerplate

**Files:**
- Create: `.claude/agents/domains/security/specialist.md`

**Step 1: Copy specialist from PM boilerplate**

```bash
cp cc-project-context-memory-plan/claude-pm-boilerplate-extracted/boilerplate/.claude/agents/specialists/SECURITY.md .claude/agents/domains/security/specialist.md
```

**Step 2: Verify copy**

Run: `head -20 .claude/agents/domains/security/specialist.md`
Expected: Shows "Security Specialist Agent" header

**Step 3: Commit**

```bash
git add .claude/agents/domains/security/specialist.md
git commit -m "feat(agents): add security specialist from PM boilerplate"
```

---

### Task 2.6: Add database specialist from PM boilerplate

**Files:**
- Create: `.claude/agents/domains/database/specialist.md`

**Step 1: Copy specialist from PM boilerplate**

```bash
cp cc-project-context-memory-plan/claude-pm-boilerplate-extracted/boilerplate/.claude/agents/specialists/DATABASE.md .claude/agents/domains/database/specialist.md
```

**Step 2: Verify copy**

Run: `head -20 .claude/agents/domains/database/specialist.md`
Expected: Shows "Database Specialist Agent" header

**Step 3: Commit**

```bash
git add .claude/agents/domains/database/specialist.md
git commit -m "feat(agents): add database specialist from PM boilerplate"
```

---

### Task 2.7: Create architecture specialist

**Files:**
- Create: `.claude/agents/domains/architecture/specialist.md`

**Step 1: Create the file**

```markdown
# Architecture Specialist Agent

You are an ARCHITECTURE SPECIALIST. You are invoked for system design,
module structure, dependency management, and pattern decisions.

**You are a subagent.** You receive focused context, analyze it, and return
a structured recommendation. You don't modify files or maintain state.

---

## Invocation Format

You will receive:
- Current codebase structure
- New feature requirements
- Specific architecture questions

---

## Areas of Expertise

### System Design
- Component boundaries and responsibilities
- Layer separation (presentation, business, data)
- Service decomposition
- API contract design

### Module Structure
- Directory organization
- Import/export patterns
- Circular dependency prevention
- Feature vs layer organization

### Dependency Management
- Package selection criteria
- Version management strategy
- Dependency injection patterns
- External service abstraction

### Design Patterns
- When to apply patterns (and when not to)
- Pattern trade-offs
- Refactoring toward patterns
- Anti-pattern identification

---

## Review Protocol

### Step 1: Understand Current State

```
## Current Architecture Analysis

### Component Map
[Major components and their responsibilities]

### Dependency Graph
[How components depend on each other]

### Current Patterns
[Patterns already in use]

### Pain Points
[Existing architectural issues]
```

### Step 2: Propose Design

```
## Architecture Proposal

### Recommended Structure
[Directory/module organization]

### Component Responsibilities
| Component | Responsibility | Dependencies |
|-----------|----------------|--------------|
| | | |

### Design Patterns to Apply
| Pattern | Where | Rationale |
|---------|-------|-----------|
| | | |

### Migration Path
[If changing existing architecture, how to get there]
```

### Step 3: Risk Assessment

```
## Architecture Risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Tight coupling | | | |
| Scalability | | | |
| Testability | | | |
```

---

## Output Format

```
===============================================================
              ARCHITECTURE DESIGN REPORT
===============================================================

## Summary
- Components affected: [count]
- New patterns: [list]
- Migration complexity: [Low/Medium/High]

## Proposed Structure
[Directory tree or component diagram]

## Key Design Decisions
| Decision | Rationale | Trade-offs |
|----------|-----------|------------|
| | | |

## Implementation Order
1. [First change]
2. [Second change]

## Requires Human Decision
- [Decisions needing product/business input]

===============================================================
```

---

## You Do NOT:
- Modify any code
- Make final decisions on major refactors
- Ignore existing patterns without justification
- Over-engineer for hypothetical future needs
```

**Step 2: Verify file created**

Run: `wc -l .claude/agents/domains/architecture/specialist.md`
Expected: ~140 lines

**Step 3: Commit**

```bash
git add .claude/agents/domains/architecture/specialist.md
git commit -m "feat(agents): add architecture specialist"
```

---

### Task 2.8: Create API specialist

**Files:**
- Create: `.claude/agents/domains/api/specialist.md`

**Step 1: Create the file**

```markdown
# API Specialist Agent

You are an API SPECIALIST. You are invoked for endpoint design,
REST conventions, versioning strategy, and API documentation.

**You are a subagent.** You receive focused context, analyze it, and return
a structured recommendation. You don't modify files or maintain state.

---

## Invocation Format

You will receive:
- Current API structure (if exists)
- New feature requirements
- Specific API design questions

---

## Areas of Expertise

### Endpoint Design
- RESTful resource modeling
- URL structure and naming
- HTTP method selection
- Query parameter vs path parameter

### Request/Response Design
- Payload structure
- Error response format
- Pagination patterns
- Filtering and sorting

### Versioning Strategy
- URL versioning vs header versioning
- Breaking change management
- Deprecation policies

### Documentation
- OpenAPI/Swagger specs
- Example requests/responses
- Error code documentation

---

## Review Protocol

### Step 1: Understand Requirements

```
## API Requirements Analysis

### Resources to Expose
[What entities/operations need API access]

### Consumer Types
[Who will use this API: frontend, mobile, third-party]

### Existing Patterns
[Current API conventions in the codebase]
```

### Step 2: Propose API Design

```
## API Design Proposal

### Endpoints

#### [Resource Name]

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/[resources] | List all |
| GET | /api/v1/[resources]/:id | Get one |
| POST | /api/v1/[resources] | Create |
| PUT | /api/v1/[resources]/:id | Update |
| DELETE | /api/v1/[resources]/:id | Delete |

#### Request/Response Examples

**GET /api/v1/[resources]**
```json
// Request
GET /api/v1/resources?page=1&limit=20&sort=created_at:desc

// Response 200
{
  "data": [...],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 100
  }
}
```

**POST /api/v1/[resources]**
```json
// Request
{
  "field": "value"
}

// Response 201
{
  "data": { "id": "...", "field": "value" }
}
```

### Error Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Human readable message",
    "details": [...]
  }
}
```
```

---

## Output Format

```
===============================================================
              API DESIGN REPORT
===============================================================

## Summary
- New endpoints: [count]
- Modified endpoints: [count]
- Breaking changes: [yes/no]

## Endpoint Specifications
[Detailed endpoint designs]

## Error Codes
| Code | HTTP Status | Description |
|------|-------------|-------------|
| | | |

## Versioning Recommendation
[How to version this API]

## Documentation Needs
[What docs should be created/updated]

## Requires Human Decision
- [Decisions needing product input]

===============================================================
```

---

## You Do NOT:
- Implement endpoints
- Make breaking changes without flagging
- Deviate from existing API patterns without justification
- Skip error handling design
```

**Step 2: Verify file created**

Run: `wc -l .claude/agents/domains/api/specialist.md`
Expected: ~150 lines

**Step 3: Commit**

```bash
git add .claude/agents/domains/api/specialist.md
git commit -m "feat(agents): add API specialist"
```

---

### Task 2.9: Create test specialist

**Files:**
- Create: `.claude/agents/domains/test/specialist.md`

**Step 1: Create the file**

```markdown
# Test Specialist Agent

You are a TEST SPECIALIST. You are invoked for test strategy,
coverage planning, mocking approach, and test architecture decisions.

**You are a subagent.** You receive focused context, analyze it, and return
a structured recommendation. You don't modify files or maintain state.

---

## Invocation Format

You will receive:
- Current test structure (if exists)
- Feature to be tested
- Specific testing questions

---

## Areas of Expertise

### Test Strategy
- Unit vs integration vs E2E balance
- What to test and what not to test
- Test pyramid/trophy recommendations
- Critical path identification

### Mocking Strategy
- When to mock vs use real implementations
- Mock boundaries
- Test doubles selection (mock, stub, spy, fake)

### Test Architecture
- Test file organization
- Shared fixtures and factories
- Test utilities and helpers
- CI/CD integration

### Coverage Strategy
- Meaningful coverage targets
- Critical vs nice-to-have coverage
- Edge case identification

---

## Review Protocol

### Step 1: Understand Testing Needs

```
## Test Requirements Analysis

### Feature Under Test
[What needs to be tested]

### Critical Paths
[User journeys that must work]

### Edge Cases
[Boundary conditions, error states]

### Dependencies
[External services, databases, APIs]
```

### Step 2: Propose Test Strategy

```
## Test Strategy Proposal

### Test Distribution

| Type | Count | Coverage Target | Rationale |
|------|-------|-----------------|-----------|
| Unit | X | 80% | Core logic |
| Integration | Y | Key paths | API + DB |
| E2E | Z | Critical flows | User journeys |

### Mocking Boundaries

| Dependency | Approach | Rationale |
|------------|----------|-----------|
| Database | Real (test container) | Data integrity |
| External API | Mock | Reliability |
| Time | Fake | Determinism |

### Test Cases

#### Unit Tests
- [ ] [Function]: [What to verify]
- [ ] [Function]: [Edge case]

#### Integration Tests
- [ ] [Flow]: [What to verify]

#### E2E Tests
- [ ] [User journey]: [What to verify]
```

---

## Output Format

```
===============================================================
              TEST STRATEGY REPORT
===============================================================

## Summary
- Recommended test count: [X unit, Y integration, Z E2E]
- Coverage target: [X%]
- Estimated test development: [time]

## Test Architecture
[File organization and patterns]

## Critical Tests
[Tests that MUST exist]

## Mocking Strategy
[What to mock and why]

## Edge Cases to Cover
[Specific edge cases identified]

## Requires Human Decision
- [Coverage vs speed trade-offs]
- [Real vs mocked dependencies]

===============================================================
```

---

## You Do NOT:
- Write test implementations (only strategy)
- Recommend 100% coverage without justification
- Mock everything (or nothing)
- Skip edge case analysis
```

**Step 2: Verify file created**

Run: `wc -l .claude/agents/domains/test/specialist.md`
Expected: ~140 lines

**Step 3: Commit**

```bash
git add .claude/agents/domains/test/specialist.md
git commit -m "feat(agents): add test specialist"
```

---

### Task 2.10: Create performance specialist

**Files:**
- Create: `.claude/agents/domains/performance/specialist.md`

**Step 1: Create the file**

```markdown
# Performance Specialist Agent

You are a PERFORMANCE SPECIALIST. You are invoked for optimization strategy,
caching decisions, profiling guidance, and performance architecture.

**You are a subagent.** You receive focused context, analyze it, and return
a structured recommendation. You don't modify files or maintain state.

---

## Invocation Format

You will receive:
- Current performance concerns
- Feature requirements with performance needs
- Specific optimization questions

---

## Areas of Expertise

### Optimization Strategy
- Identifying bottlenecks
- Prioritizing optimizations (impact vs effort)
- Premature optimization avoidance
- Measurable performance targets

### Caching Strategy
- What to cache (and what not to)
- Cache invalidation patterns
- Cache layers (browser, CDN, application, database)
- Cache key design

### Database Performance
- Query optimization
- Index strategy
- Connection pooling
- Read replicas and sharding

### Frontend Performance
- Bundle optimization
- Lazy loading strategy
- Image optimization
- Core Web Vitals

---

## Review Protocol

### Step 1: Understand Performance Requirements

```
## Performance Requirements Analysis

### Current State
[Baseline metrics if available]

### Target Metrics
| Metric | Current | Target |
|--------|---------|--------|
| Response time | | |
| Throughput | | |
| Memory usage | | |

### Constraints
[Budget, infrastructure, timeline]
```

### Step 2: Identify Bottlenecks

```
## Bottleneck Analysis

### Suspected Bottlenecks
| Area | Symptom | Evidence |
|------|---------|----------|
| | | |

### Profiling Recommendations
[What to measure and how]
```

### Step 3: Propose Optimizations

```
## Optimization Proposal

### Quick Wins (Low effort, High impact)
1. [Optimization] - [Expected improvement]

### Medium Term
1. [Optimization] - [Expected improvement]

### Architecture Changes (If needed)
1. [Change] - [Expected improvement] - [Trade-offs]

### Caching Strategy
| Layer | What to Cache | TTL | Invalidation |
|-------|---------------|-----|--------------|
| | | | |
```

---

## Output Format

```
===============================================================
              PERFORMANCE OPTIMIZATION REPORT
===============================================================

## Summary
- Bottlenecks identified: [count]
- Quick wins available: [count]
- Estimated improvement: [X%]

## Optimization Priority
| Priority | Optimization | Impact | Effort |
|----------|--------------|--------|--------|
| 1 | | High | Low |
| 2 | | Medium | Medium |

## Caching Recommendations
[What to cache, where, how]

## Monitoring Needs
[Metrics to track]

## Requires Human Decision
- [Cost vs performance trade-offs]
- [Architecture changes]

===============================================================
```

---

## You Do NOT:
- Implement optimizations
- Recommend without measurement
- Optimize prematurely
- Ignore trade-offs (memory vs speed, complexity vs performance)
```

**Step 2: Verify file created**

Run: `wc -l .claude/agents/domains/performance/specialist.md`
Expected: ~140 lines

**Step 3: Commit**

```bash
git add .claude/agents/domains/performance/specialist.md
git commit -m "feat(agents): add performance specialist"
```

---

## Phase 3: Add Core Agents

### Task 3.1: Add INITIALIZER agent

**Files:**
- Create: `.claude/agents/INITIALIZER.md`

**Step 1: Copy from PM boilerplate**

```bash
cp cc-project-context-memory-plan/claude-pm-boilerplate-extracted/boilerplate/.claude/agents/INITIALIZER.md .claude/agents/INITIALIZER.md
```

**Step 2: Verify copy**

Run: `head -10 .claude/agents/INITIALIZER.md`
Expected: Shows "Initializer Agent" header

**Step 3: Commit**

```bash
git add .claude/agents/INITIALIZER.md
git commit -m "feat(agents): add INITIALIZER agent from PM boilerplate"
```

---

### Task 3.2: Add PLANNER agent

**Files:**
- Create: `.claude/agents/PLANNER.md`

**Step 1: Copy from PM boilerplate**

```bash
cp cc-project-context-memory-plan/claude-pm-boilerplate-extracted/boilerplate/.claude/agents/PLANNER.md .claude/agents/PLANNER.md
```

**Step 2: Verify copy**

Run: `head -10 .claude/agents/PLANNER.md`
Expected: Shows "Planner Agent" header

**Step 3: Commit**

```bash
git add .claude/agents/PLANNER.md
git commit -m "feat(agents): add PLANNER agent from PM boilerplate"
```

---

### Task 3.3: Add IMPLEMENTER agent

**Files:**
- Create: `.claude/agents/IMPLEMENTER.md`

**Step 1: Copy from PM boilerplate and modify**

```bash
cp cc-project-context-memory-plan/claude-pm-boilerplate-extracted/boilerplate/.claude/agents/IMPLEMENTER.md .claude/agents/IMPLEMENTER.md
```

**Step 2: Modify to remove SCRATCHPAD.md references (we merged into progress.log)**

Edit `.claude/agents/IMPLEMENTER.md`:
- Remove references to `docs/SCRATCHPAD.md`
- Update "Phase 5: UPDATE MEMORY" to only mention features.json and progress.log
- Update session summary to remove SCRATCHPAD.md from checklist

**Step 3: Verify modifications**

Run: `grep -c "SCRATCHPAD" .claude/agents/IMPLEMENTER.md`
Expected: 0

**Step 4: Commit**

```bash
git add .claude/agents/IMPLEMENTER.md
git commit -m "feat(agents): add IMPLEMENTER agent (modified to use progress.log for handoff)"
```

---

### Task 3.4: Add REVIEWER agent with guardian orchestration

**Files:**
- Create: `.claude/agents/REVIEWER.md`

**Step 1: Copy from PM boilerplate**

```bash
cp cc-project-context-memory-plan/claude-pm-boilerplate-extracted/boilerplate/.claude/agents/REVIEWER.md .claude/agents/REVIEWER.md
```

**Step 2: Add guardian orchestration section**

Append to `.claude/agents/REVIEWER.md`:

```markdown

---

## Guardian Orchestration

The REVIEWER agent orchestrates parallel guardian reviews during Phase 4.

### Invoke Guardians

Based on files changed, spawn relevant guardians in parallel:

```
Invoking guardian reviews for [FEATURE-ID]...

Files to review: [count]
[List files]

Spawning guardians:
- Security Guardian (auth/, api/ files detected)
- TypeScript Guardian (*.ts files detected)
- Test Guardian (*.test.* files detected)
- [etc based on file patterns]

Waiting for results...
```

### Guardian File Detection

| File Pattern | Guardians to Invoke |
|--------------|---------------------|
| `*.sql`, `migrations/**`, `*.prisma` | database |
| `api/**`, `routes/**` | api, security |
| `auth/**` | security |
| `*.ts`, `*.tsx` | typescript |
| `*.tsx`, `*.jsx`, `*.css` | accessibility |
| `*.test.*`, `*.spec.*` | test |
| `*.md`, `docs/**` | docs |

### Synthesize Results

After all guardians complete:

```
## Guardian Review Results

| Guardian | Findings | Critical | High | Medium | Low |
|----------|----------|----------|------|--------|-----|
| Security | 2 | 0 | 1 | 1 | 0 |
| TypeScript | 0 | 0 | 0 | 0 | 0 |
| Test | 1 | 0 | 0 | 1 | 0 |

### All Findings (by severity)

[Aggregate all guardian findings, sorted by severity]
```

### External Review (Codex)

After guardian review:

```
Creating Codex review task...

Task file: .ai/tasks/review/[FEATURE-ID]-codex-review.md
Branch pushed: [branch-name]

Codex will review asynchronously (~15 minutes).
You may continue working. I'll incorporate Codex findings when available.
```
```

**Step 3: Verify file has guardian section**

Run: `grep -c "Guardian Orchestration" .claude/agents/REVIEWER.md`
Expected: 1

**Step 4: Commit**

```bash
git add .claude/agents/REVIEWER.md
git commit -m "feat(agents): add REVIEWER agent with guardian orchestration"
```

---

## Phase 4: Rewrite CLAUDE.md

### Task 4.1: Create new CLAUDE.md with domain memory integration

**Files:**
- Modify: `CLAUDE.md`

**Step 1: Read current CLAUDE.md for reference**

Run: `cat CLAUDE.md`

**Step 2: Write new CLAUDE.md**

```markdown
# [Project Name] Boilerplate

## Quick Reference
- Stack: [Define your tech stack here]
- Commands: `pnpm dev` · `pnpm test` · `pnpm build`
- Branch: `git branch --show-current`
- Memory: `.claude/memory/features.json`

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
1. **Research first** — Web search before recommending when currency matters
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

---

## See Also
@docs/plans/2025-12-09-domain-memory-integration-design.md · @.claude/settings.json

---
*Boilerplate by [Songbird Digital](https://github.com/songbirddigital)*
```

**Step 3: Verify line count**

Run: `wc -l CLAUDE.md`
Expected: ~120-140 lines

**Step 4: Commit**

```bash
git add CLAUDE.md
git commit -m "feat: rewrite CLAUDE.md with domain memory integration"
```

---

## Phase 5: Update Commands

### Task 5.1: Create /setup command

**Files:**
- Create: `.claude/commands/setup.md`

**Step 1: Create the command**

```markdown
Read @.claude/agents/INITIALIZER.md and help me set up this project with domain memory.

Context: User wants to initialize a new project or set up domain memory for the first time.

The INITIALIZER will:
1. Ask about project purpose, features, tech stack, constraints
2. Create features.json with initial feature breakdown
3. Initialize progress.log, decisions.md, issues.md
4. Set up docs/ARCHITECTURE.md

Guide the user through this process interactively.
```

**Step 2: Verify file created**

Run: `cat .claude/commands/setup.md`
Expected: Shows command content

**Step 3: Commit**

```bash
git add .claude/commands/setup.md
git commit -m "feat(commands): add /setup command for project initialization"
```

---

### Task 5.2: Create /status command

**Files:**
- Create: `.claude/commands/status.md`

**Step 1: Create the command**

```markdown
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
```

**Step 2: Verify file created**

Run: `cat .claude/commands/status.md`
Expected: Shows command content

**Step 3: Commit**

```bash
git add .claude/commands/status.md
git commit -m "feat(commands): add /status command for project status display"
```

---

### Task 5.3: Create /plan command

**Files:**
- Create: `.claude/commands/plan.md`

**Step 1: Create the command**

```markdown
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
```

**Step 2: Verify file created**

Run: `cat .claude/commands/plan.md`
Expected: Shows command content

**Step 3: Commit**

```bash
git add .claude/commands/plan.md
git commit -m "feat(commands): add /plan command for feature planning"
```

---

### Task 5.4: Update /review command

**Files:**
- Modify: `.claude/commands/review.md`

**Step 1: Read current command**

Run: `cat .claude/commands/review.md`

**Step 2: Update the command**

```markdown
Read @.claude/agents/REVIEWER.md and perform a code review.

Usage: /review [FEATURE-ID or file paths]

The REVIEWER will:
1. Load feature context from features.json
2. Run automated checks (tests, lint, types)
3. Check specification compliance
4. Spawn parallel guardian reviews based on files changed
5. Create Codex review task (async, ~15 min)
6. Synthesize all findings
7. Present verdict: APPROVED / CHANGES REQUESTED

Guardian file detection:
- *.sql, migrations/**, *.prisma → database guardian
- api/**, routes/** → api + security guardians
- auth/** → security guardian
- *.ts, *.tsx → typescript guardian
- *.tsx, *.jsx, *.css → accessibility guardian
- *.test.*, *.spec.* → test guardian
- *.md, docs/** → docs guardian

All findings are aggregated and prioritized by severity.
```

**Step 3: Verify update**

Run: `grep -c "REVIEWER" .claude/commands/review.md`
Expected: >= 1

**Step 4: Commit**

```bash
git add .claude/commands/review.md
git commit -m "feat(commands): update /review to use REVIEWER agent with guardian orchestration"
```

---

### Task 5.5: Update /migrate command

**Files:**
- Modify: `.claude/commands/migrate.md`

**Step 1: Read current command**

Run: `cat .claude/commands/migrate.md`

**Step 2: Update the command**

```markdown
Add domain memory to an existing project.

Invoke the boilerplate-migrate skill with full onboarding flow:

1. **Analyze existing context**
   - Read CLAUDE.md, .claude/, .ai/, docs/
   - Identify what already exists

2. **Present findings**
   - Show what was found
   - Propose migration plan

3. **Ask clarifying questions**
   - Active work?
   - Key decisions already made?
   - Known issues?

4. **Create memory scaffold**
   - Create .claude/memory/ with features.json, progress.log, decisions.md, issues.md
   - Merge session protocol into existing CLAUDE.md
   - Preserve all existing project-specific content

5. **Confirm**
   - List what was created
   - Ready to continue

This handles:
- Scenario A: Existing codebase, no Claude context
- Scenario B: Existing codebase with basic CLAUDE.md
```

**Step 3: Verify update**

Run: `grep -c "memory scaffold" .claude/commands/migrate.md`
Expected: >= 1

**Step 4: Commit**

```bash
git add .claude/commands/migrate.md
git commit -m "feat(commands): update /migrate with domain memory onboarding flow"
```

---

## Phase 6: Add Memory Feedback Infrastructure

### Task 6.1: Create memory feedback directory

**Files:**
- Create: `.ai/feedback/memory/`

**Step 1: Create directory and files**

```bash
mkdir -p .ai/feedback/memory
```

**Step 2: Create orientation.md**

```markdown
# Memory Orientation Feedback

Track effectiveness of session start orientation from domain memory.

---

## Feedback Template

### YYYY-MM-DD
**Type:** helpful | confusing | incomplete | wrong
**Context:** [What was happening]
**Expected:** [What should have happened]
**Actual:** [What did happen]
**Suggested Fix:** [If any]

---

## Entries

*No entries yet.*
```

**Step 3: Create updates.md**

```markdown
# Memory Update Feedback

Track reliability of session end memory updates.

---

## Feedback Template

### YYYY-MM-DD
**Type:** missed_update | incorrect_update | partial_update
**File:** [features.json | progress.log | decisions.md | issues.md]
**Context:** [What was happening]
**Expected:** [What should have been updated]
**Actual:** [What was/wasn't updated]
**Suggested Fix:** [If any]

---

## Entries

*No entries yet.*
```

**Step 4: Create lifecycle.md**

```markdown
# Lifecycle Feedback

Track issues with feature lifecycle phase transitions.

---

## Feedback Template

### YYYY-MM-DD
**Type:** wrong_phase | stuck_phase | skipped_phase
**Feature:** [FEATURE-ID]
**Context:** [What was happening]
**Expected:** [Expected phase transition]
**Actual:** [What happened]
**Suggested Fix:** [If any]

---

## Entries

*No entries yet.*
```

**Step 5: Create decisions-feedback.md (to avoid conflict with decisions.md)**

```markdown
# Decision Record Feedback

Track usefulness of ADRs when referenced in later sessions.

---

## Feedback Template

### YYYY-MM-DD
**Type:** helpful | outdated | unclear | missing
**Decision:** [DEC-XXX]
**Context:** [When was it referenced]
**Issue:** [What was the problem]
**Suggested Fix:** [If any]

---

## Entries

*No entries yet.*
```

**Step 6: Verify files created**

Run: `ls .ai/feedback/memory/`
Expected: orientation.md, updates.md, lifecycle.md, decisions-feedback.md

**Step 7: Commit**

```bash
git add .ai/feedback/memory/
git commit -m "feat(feedback): add memory feedback infrastructure"
```

---

## Phase 7: Update Skills

### Task 7.1: Deprecate session-management skill

**Files:**
- Modify: `.claude/skills/session-management/SKILL.md`

**Step 1: Add deprecation notice to top of file**

Prepend to `.claude/skills/session-management/SKILL.md`:

```markdown
> **DEPRECATED:** Session protocol has moved to CLAUDE.md for reliability.
> This skill is kept for reference but should not be invoked.
> See CLAUDE.md "Session Protocol" section for current behavior.

---

```

**Step 2: Verify deprecation notice added**

Run: `head -5 .claude/skills/session-management/SKILL.md`
Expected: Shows DEPRECATED notice

**Step 3: Commit**

```bash
git add .claude/skills/session-management/SKILL.md
git commit -m "chore: deprecate session-management skill (moved to CLAUDE.md)"
```

---

### Task 7.2: Update boilerplate-migrate skill

**Files:**
- Modify: `.claude/skills/boilerplate-migrate/SKILL.md`

**Step 1: Read current skill**

Run: `cat .claude/skills/boilerplate-migrate/SKILL.md`

**Step 2: Update skill with full onboarding flow**

Add section to skill for domain memory scaffold creation:

```markdown

---

## Domain Memory Migration

When migrating a project, always add the domain memory scaffold:

### Step 1: Check for existing memory
```bash
ls .claude/memory/ 2>/dev/null || echo "No memory directory"
```

### Step 2: Create memory scaffold
If no memory exists:
```bash
mkdir -p .claude/memory
```

Then create:
- `.claude/memory/features.json` — from template
- `.claude/memory/progress.log` — initialized
- `.claude/memory/decisions.md` — from template
- `.claude/memory/issues.md` — from template

### Step 3: Ask clarifying questions

```
I'm setting up domain memory for your project. A few questions:

1. **Active work:** What are you currently working on?
   (I'll create the first feature entry)

2. **Key decisions already made:** Any architectural choices I should document?

3. **Known issues:** Any bugs or tech debt to track?
```

### Step 4: Populate initial state

Based on user answers:
- Add first feature to features.json
- Record any decisions to decisions.md
- Add any issues to issues.md

### Step 5: Update CLAUDE.md

Merge session protocol into existing CLAUDE.md while preserving project-specific content.
```

**Step 3: Verify update**

Run: `grep -c "Domain Memory Migration" .claude/skills/boilerplate-migrate/SKILL.md`
Expected: 1

**Step 4: Commit**

```bash
git add .claude/skills/boilerplate-migrate/SKILL.md
git commit -m "feat(skills): update boilerplate-migrate with domain memory scaffold"
```

---

### Task 7.3: Update guardian-agents skill for new paths

**Files:**
- Modify: `.claude/skills/guardian-agents/SKILL.md`

**Step 1: Read current skill**

Run: `cat .claude/skills/guardian-agents/SKILL.md`

**Step 2: Update guardian paths**

Find and replace all references to `.claude/agents/[name]-guardian.md` with `.claude/agents/domains/[name]/guardian.md`

Example replacements:
- `.claude/agents/security-guardian.md` → `.claude/agents/domains/security/guardian.md`
- `.claude/agents/database-guardian.md` → `.claude/agents/domains/database/guardian.md`
- etc.

**Step 3: Verify update**

Run: `grep -c "domains/" .claude/skills/guardian-agents/SKILL.md`
Expected: >= 9 (one for each guardian)

**Step 4: Commit**

```bash
git add .claude/skills/guardian-agents/SKILL.md
git commit -m "refactor(skills): update guardian-agents paths to new domains structure"
```

---

### Task 7.4: Update feature-completion skill with lifecycle integration

**Files:**
- Modify: `.claude/skills/feature-completion/SKILL.md`

**Step 1: Read current skill**

Run: `cat .claude/skills/feature-completion/SKILL.md`

**Step 2: Add lifecycle phase checking**

Add section:

```markdown

---

## Lifecycle Phase Verification

Before completing a feature, verify lifecycle phases in features.json:

### Phase Checklist

```bash
# Read feature status
cat .claude/memory/features.json | grep -A 50 "[FEATURE-ID]"
```

Verify:
- [ ] `lifecycle.inception.status` = "complete"
- [ ] `lifecycle.planning.status` = "complete"
- [ ] `lifecycle.implementation.status` = "complete" (all tasks done)
- [ ] `lifecycle.testing.status` = "complete"
- [ ] `lifecycle.review.status` = "complete"

### Update Feature Status

On completion:
```javascript
// In features.json, update:
{
  "status": "complete",
  "phase": "merged",
  "lifecycle": {
    "merged": {
      "status": "complete",
      "merge_date": "[date]",
      "pr_url": "[if applicable]"
    }
  }
}
```

### Update Progress Log

```
================================================================================
[TIMESTAMP] FEATURE COMPLETE
Feature: [ID] - [Title]
Total tasks: [X]
Total sessions: [Y]
Decisions recorded: [Z]
================================================================================
```
```

**Step 3: Verify update**

Run: `grep -c "Lifecycle Phase" .claude/skills/feature-completion/SKILL.md`
Expected: >= 1

**Step 4: Commit**

```bash
git add .claude/skills/feature-completion/SKILL.md
git commit -m "feat(skills): update feature-completion with lifecycle integration"
```

---

## Phase 8: Final Verification

### Task 8.1: Verify directory structure

**Step 1: Check complete structure**

Run: `find .claude -type f -name "*.md" -o -name "*.json" | sort`

Expected files (minimum):
```
.claude/agents/IMPLEMENTER.md
.claude/agents/INITIALIZER.md
.claude/agents/PLANNER.md
.claude/agents/REVIEWER.md
.claude/agents/domains/accessibility/guardian.md
.claude/agents/domains/api/guardian.md
.claude/agents/domains/api/specialist.md
.claude/agents/domains/architecture/guardian.md
.claude/agents/domains/architecture/specialist.md
.claude/agents/domains/database/guardian.md
.claude/agents/domains/database/specialist.md
.claude/agents/domains/docs/guardian.md
.claude/agents/domains/performance/guardian.md
.claude/agents/domains/performance/specialist.md
.claude/agents/domains/security/guardian.md
.claude/agents/domains/security/specialist.md
.claude/agents/domains/test/guardian.md
.claude/agents/domains/test/specialist.md
.claude/agents/domains/typescript/guardian.md
.claude/commands/feedback.md
.claude/commands/migrate.md
.claude/commands/plan.md
.claude/commands/review.md
.claude/commands/setup.md
.claude/commands/status.md
.claude/memory/decisions.md
.claude/memory/features.json
.claude/memory/issues.md
.claude/memory/progress.log
.claude/settings.json
.claude/templates/feature-spec.md
```

**Step 2: Commit final structure**

```bash
git add -A
git commit -m "chore: verify complete domain memory integration structure"
```

---

### Task 8.2: Update settings.json with new paths

**Files:**
- Modify: `.claude/settings.json`

**Step 1: Read current settings**

Run: `cat .claude/settings.json`

**Step 2: Update guardian paths in file_detection**

Update the `guardians` section to reflect new paths. The file detection patterns stay the same, but any path references should use the new `domains/` structure.

**Step 3: Commit**

```bash
git add .claude/settings.json
git commit -m "chore: update settings.json guardian paths"
```

---

### Task 8.3: Create final integration commit

**Step 1: Verify all changes are committed**

Run: `git status`
Expected: Working tree clean

**Step 2: Create summary commit if needed**

If there are any uncommitted changes:
```bash
git add -A
git commit -m "feat: complete domain memory integration

- Add .claude/memory/ with features.json, progress.log, decisions.md, issues.md
- Restructure guardians to .claude/agents/domains/*/guardian.md
- Add specialists alongside guardians
- Add core agents: INITIALIZER, PLANNER, IMPLEMENTER, REVIEWER
- Rewrite CLAUDE.md with session protocol inline
- Add /setup, /status, /plan commands
- Update /review, /migrate commands
- Add memory feedback infrastructure
- Update skills for new structure"
```

---

## Execution Notes

- Each task is designed to be completed in 2-5 minutes
- Commit after each task for safe incremental progress
- If a task fails, the previous commits provide rollback points
- Test file paths before writing to ensure directories exist
- Verify JSON validity after creating features.json

---

**Plan complete and saved to `docs/plans/2025-12-09-domain-memory-implementation.md`.**

Two execution options:

**1. Subagent-Driven (this session)** — I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** — Open new session in worktree, batch execution with checkpoints

Which approach?
