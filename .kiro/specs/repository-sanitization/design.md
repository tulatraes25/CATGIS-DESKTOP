# Repository Sanitization — Design

Status: ACTIVE
Requirements: `requirements.md`
Baseline at spec creation: `3d07252ea9b9af62b2ff3230ff76d11d94297313`

## 1. Current state

The active repository tree, public branch history and workstation cutover have been rewritten/recovered and certified. T10B closure was certified against public `main` `592ea1e6d6ba3961b75060b1a414ebae20940669`.

`main` is hardened by active repository ruleset `CATGIS main protection` (ID `24466311`). T10A reduced the public branch set to exactly `main`, configured local noreply identity, and preserved forensic remnants outside active development.

T10B is now `CLOSED_CERTIFIED`. PostgreSQL live inventory found `postgres` as the only in-scope LOGIN. The credential was rotated, new authentication passed, the temporary recovery role was removed, the original HBA bytes/hash were restored, temporary trust was absent, PostgreSQL remained running, and a DPAPI vault was created with a passing file-level ACL. No historical credential recovery was attempted.

T10C is `CLOSED_CERTIFIED_BY_REPOSITORY_ISOLATION`. The original repository is private and preserved as `CATGIS-DESKTOP-FORENSIC-ARCHIVE-20261005` (repository ID `1199177324`). A distinct public repository named `tulatraes25/CATGIS-DESKTOP` (repository ID `1405554812`) was created from the single parentless root commit `325d3cb71124bc70e43dd6a0e31cb8869917e484`; its tree `816795841a35996a45bbfbd4871387a0c4f5f679` equals the certified sanitized source tree. No historical commits, refs/pull, branches, tags, issues, reflogs, bundles, quarantines or LFS objects were imported. GitHub Support is no longer required for T10C closure. The canonical Windows worktree was physically cut over to the one-commit public clone; the former local clone is archived and push-disabled. T10D is now `PRIVACY_REMEDIATION_IN_PROGRESS`. Finding (T10D-R2): the first public isolation succeeded technically, but subsequent connector/web Git operations introduced **non-noreply** author/committer metadata on the public lineage; **5** of 6 reachable commits from public `main` are affected (1 is noreply-only); the personal address is intentionally omitted; the account primary email visibility has been set to **PRIVATE**. Remediation selected: a **second repository isolation** — the current public repository becomes a private privacy archive (`CATGIS-DESKTOP-PRIVACY-ARCHIVE-20261005`, ID `1405554812`), a private staging repository (`CATGIS-DESKTOP-FINAL-STAGING-20261005`) is prepared from a single parentless noreply root commit, then renamed to `CATGIS-DESKTOP` and made public with a distinct repository ID. Historical-author rewrite for the first pre-rewrite history is `NOT_REQUIRED_FOR_FIRST_PRE_REWRITE_HISTORY`. Licensing is already proprietary/source-available.

Current public source of truth remains GitHub `main`.

## 2. Target state

Final state:

`ACTIVE_TREE_CLEAN -> PUBLIC_REFS_NEUTRALIZED -> LOCAL_REWRITE_VALIDATED -> REMOTE_REWRITE_PUBLISHED -> POST_REWRITE_CERTIFIED -> MAIN_PROTECTED -> CLOSED_CERTIFIED`

The current certified state is:

`T10C_CLOSED_CERTIFIED_BY_REPOSITORY_ISOLATION / T10D_PRIVACY_REMEDIATION_IN_PROGRESS`

## 3. Components

### GitHub

Authoritative remote and final certification surface.

Responsibilities:
- canonical `main`;
- branches/tags;
- PR/CI;
- post-rewrite verification;
- branch protection/ruleset.

### Guarded local rewrite helper

`docs/repository/Invoke-HistorySanitization.ps1`

Responsibilities:
- create a fresh mirror clone;
- capture original refs offline;
- run `git filter-repo`;
- optionally consume private path/email parameters;
- validate rewritten history/tree;
- default to no push;
- publish only with the explicit destructive switch.

### Runbook

`docs/repository/HISTORY_SANITIZATION_RUNBOOK.md`

Human-readable operational contract and recovery procedure.

### Forensic package

Offline-only evidence preserving local/divergent work. It is not part of the public repository.

### Repository isolation (selected T10C design)

Selected design: `PRIVATE_ARCHIVE + NEW_SINGLE_ROOT_PUBLIC_REPOSITORY`.

- the pre-rewrite public repository is converted to private and renamed as a forensic archive; it is never deleted;
- a new, distinct public repository is created with the canonical name and populated with exactly one parentless root commit whose tree equals the last certified sanitized `main` tree;
- no historical history, refs, tags, issues, reflogs, bundles, quarantines or LFS objects are imported;
- the active workstation is re-cloned from the new public repository and physically swapped in an offline cutover.

## 4. Data / control flow

1. Operator starts from a normal current clone only to invoke the committed helper.
2. Helper creates a new disposable `--mirror` clone.
3. Helper records original refs outside the mirror.
4. Helper verifies original object integrity.
5. `git filter-repo` removes banned paths from all reachable rewritten refs.
6. Optional private local parameters rewrite workstation paths and/or author email.
7. Helper verifies banned paths are absent.
8. Helper verifies required canonical files/directories.
9. Helper runs `git fsck --full`.
10. In default mode, execution stops with `LOCAL_REWRITE_VALIDATED`.
11. During a maintenance window, operator repeats with explicit push authorization.
12. Helper force-pushes branches and tags only.
13. GitHub state is re-read and CI is run/inspected.
14. Branch protection/ruleset is enabled.
15. Workstations are re-cloned.
16. Obsolete remote branch names are removed.
17. Exposed/reused CATSERVER credentials are rotated or revoked and T10B is independently certified.
18. A sanitized T10C handoff is prepared while the private Support evidence bundle remains outside Git.
19. Repository isolation is certified: private forensic archive, distinct one-root public repository, old control SHAs publicly unreachable, CI/protection green, and canonical workstation cut over.
20. Account/local commit-email privacy posture is reconciled.
21. Repository licensing posture is documented.
22. Spec reaches `CLOSED_CERTIFIED`.

## 5. Security and privacy

- No historical credential value belongs in this spec.
- No private workstation root belongs in this spec.
- No historical private email must be added merely to support rewrite.
- Private values are supplied only at runtime on the operator workstation.
- Public helper defaults must be neutral.
- Exposed database credentials remain compromised until rotated/revoked independently.
- Because the historical credential document is not reopened during closure, any operational CATSERVER login that may have reused an exposed password is treated as in-scope until verified otherwise.
- Current public repository license posture is proprietary/source-available; public visibility does not grant reuse rights.

## 6. Remote mutation boundary

The destructive boundary is the explicit `-PushRewrittenHistory` switch.

Everything before that switch must be locally repeatable and non-mutating to GitHub.

Publication uses:

- `git push --force origin --all`
- `git push --force origin --tags`

Do not use `--mirror` for the remote push because GitHub-managed refs must not be rewritten.

## 7. Validation strategy

### Before remote push

- fresh mirror clone;
- `git show-ref` captured offline;
- `git fsck --full`;
- banned path history checks;
- optional private-literal checks;
- canonical-tree existence checks;
- second `git fsck --full`.

### After remote push

- enumerate branch/tag state;
- verify banned paths cannot be browsed;
- verify no public CATSERVER operational CSVs;
- run and inspect CATGIS CI;
- record rewritten `main` SHA.

### After workstation resynchronization

If the local automation host itself keeps the pre-rewrite clone directory open, resynchronization is executed in two phases rather than weakening the quarantine requirement.

**Phase T9A — logical quarantine and clean clone validation**

- keep the old worktree contents intact;
- disable its push URL before any new development resumes;
- create a fresh clone at a different path from rewritten GitHub only;
- verify `HEAD == origin/main == certified SHA`;
- run `git fsck --full` and the Windows Gradle baseline;
- do not copy files, commits or Git objects from the old clone.

**Phase T9B — offline physical cutover**

- after OpenCode and any process holding the old root are closed, execute the final rename/swap from an external PowerShell session;
- move the old clone to a clearly named quarantine path without deleting it;
- move the already validated clean clone into the canonical `C:\CATGIS` path;
- re-verify identity, clean status and disabled push on the quarantined clone;
- never use the old clone as a source of implementation code.

This split preserves REQ-7 while avoiding unsafe attempts to terminate the process that is hosting the migration itself.

## 8. Rollback / recovery

History rewriting changes SHAs. The rollback artifact is the offline pre-rewrite ref map plus the pre-existing forensic package.

Do not publish that rollback material.

If remote publication fails partially:

1. stop all development;
2. inspect remote refs vs offline ref map;
3. restore or complete the intended ref update deliberately;
4. do not let old workstations push until the remote state is certified.

## 9. Risks

### R1 — Old clone reintroduces history

Mitigation: mandatory re-clone and branch protection after rewrite. If the host process prevents physical rename, immediately apply logical quarantine by disabling push, validate a separate clean clone, then complete the physical swap from an external process after the host exits.

### R2 — Over-broad filter deletes product files

Mitigation: explicit path list plus canonical-tree verification before push.

### R3 — GitHub-managed refs are damaged

Mitigation: push branches/tags only, never remote `--mirror`.

### R4 — Credential is assumed safe after purge

Mitigation: maintain rotation/revocation as a separate acceptance condition.

### R5 — Private values leak into public automation

Mitigation: runtime parameters only; public docs use placeholders.

## 10. Rejected alternatives

- **Delete only from current `main`:** already insufficient because historical objects remain.
- **Delete old branch names without rewrite:** reduces discoverability but does not purge history.
- **Force-push directly from an existing workstation clone:** rejected because stale/local refs may contaminate the rewrite.
- **Reinitialize the repository with no history:** rejected for the sanitized lineage because it destroys legitimate provenance unnecessarily. Adopted only as the T10C repository-isolation design, where the original repository is preserved as a private forensic archive and a brand-new public repository is published from a single sanitized root commit.
