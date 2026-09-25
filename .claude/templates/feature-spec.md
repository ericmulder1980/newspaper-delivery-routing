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
