# Spec/Kiro rules for CATGIS

## Purpose

Use this reference when creating or updating a CATGIS spec.

Kiro Specs are intent artifacts. GitHub commits and CI remain the source of implementation truth.

## Artifact set

### Feature / engineering program

`.kiro/specs/<slug>/requirements.md`

`.kiro/specs/<slug>/design.md`

`.kiro/specs/<slug>/tasks.md`

### Bugfix

`.kiro/specs/<slug>/bugfix.md`

`.kiro/specs/<slug>/design.md`

`.kiro/specs/<slug>/tasks.md`

## requirements.md template

```markdown
# <Spec name> — Requirements

Status: ACTIVE
Owner: CATGIS
Baseline: <main SHA or TO_VERIFY>

## Goal
<observable outcome>

## Non-goals
- ...

## REQ-1 — <name>
WHEN <event>, THE SYSTEM SHALL <behavior>.

Acceptance:
- AC-1.1 ...
- AC-1.2 ...

## Constraints
- ...
```

Requirements should describe externally testable behavior. Keep implementation choices in design unless they are true constraints.

## design.md template

```markdown
# <Spec name> — Design

Status: ACTIVE
Baseline: <verified SHA>

## Current state
...

## Target state
...

## Architecture / components
...

## Sequence
1. ...
2. ...

## Security and privacy
...

## Validation
...

## Rollback
...

## Risks / alternatives
...
```

For destructive changes, include a dry-run state that cannot mutate the remote.

## tasks.md template

```markdown
# <Spec name> — Tasks

- [ ] T1 — <atomic outcome>
  - Requirements: REQ-1
  - Evidence: <test/diff/CI/SHA/runtime proof>
  - Executor: GitHub | Kiro/OpenCode local | Manual admin

- [ ] T2 — ...
```

A task is complete only when its evidence exists.

## Autonomous phase progression

When the user's intent is already clear, do not pause after generating requirements or design merely to ask for approval. Self-review:

1. requirement completeness and contradictions;
2. design coverage of all requirements;
3. task traceability to requirements;
4. rollback/security gaps;
5. then execute pending tasks.

Pause only for a genuinely unresolved product choice, missing destructive authorization, or unavailable required environment.

## Traceability

Prefer explicit mappings:

`REQ-x -> design section -> Tx -> evidence`

When implementation reveals a requirement/design change, update the spec before or with the implementation PR.

## Spec closure

A spec closes only when:

- all blocking tasks are `[x]`;
- required PRs are merged;
- post-main CI is green;
- required local validation is complete;
- operational/manual follow-ups are either complete or explicitly moved to a separate spec/issue.
