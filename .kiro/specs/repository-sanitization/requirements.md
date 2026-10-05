# Repository Sanitization — Requirements

Status: ACTIVE
Spec type: Security / repository governance
Canonical repository: `tulatraes25/CATGIS-DESKTOP`
Baseline at spec creation: `3d07252ea9b9af62b2ff3230ff76d11d94297313`

## Goal

Complete the final historical sanitation of the public CATGIS repository without losing the current canonical product, then establish a protected Git workflow that prevents the sanitized history from being accidentally reintroduced.

## Current certified state

- Historical rewrite has been published and post-rewrite certified.
- T10B closure was certified against public `main` `592ea1e6d6ba3961b75060b1a414ebae20940669`; the documentary PR that records this state may advance `main` normally through the protected merge workflow.
- Public ref state is exactly 1 branch (`main`) and 0 tags; 0 open PRs were present at T10B certification.
- Known credential-file and decompiled-source history is absent from every unique public branch history checked after rewrite.
- CATSERVER operational inventory CSVs are absent from rewritten public history under the certified rewrite evidence.
- `main` is protected by active ruleset `CATGIS main protection` (ID `24466311`): PR-only integration, three required CATGIS CI jobs, strict up-to-date status checks, force-push blocked, deletion blocked and no bypass actors.
- Canonical Windows worktree `C:\\CATGIS` is a fresh post-rewrite clone and was clean at T10B certification.
- Remaining pre-rewrite material is preserved only as quarantined forensic fragments outside active development; any fragment that remains a valid Git repository has push disabled.
- Repository licensing posture is documented as proprietary/source-available, not open source.
- **T10B is `CLOSED_CERTIFIED`:** live inventory established `postgres` as the only in-scope PostgreSQL LOGIN; that credential was rotated, new authentication passed, the temporary recovery role was removed, the original HBA hash was restored exactly, temporary trust was absent, PostgreSQL remained running, and the DPAPI vault file had a passing file-level ACL. No historical credential was recovered, printed or committed.
- **T10C is `CLOSED_CERTIFIED_BY_REPOSITORY_ISOLATION`:** the original repository is private and preserved as `CATGIS-DESKTOP-FORENSIC-ARCHIVE-20261005` (repository ID `1199177324`); the canonical public repository is a distinct repository (ID `1405554812`) whose baseline is the single parentless root commit `325d3cb71124bc70e43dd6a0e31cb8869917e484`, tree `816795841a35996a45bbfbd4871387a0c4f5f679`. Required CI passed, ruleset `24496251` is active, the known pre-rewrite control SHAs are publicly unreachable, and the canonical Windows worktree was physically cut over to the one-commit clone with the old local clone archived and push-disabled.
- Remaining closure gates are T10D account-level email-privacy confirmation or accepted residual choice, the now non-required historical author-email rewrite decision, and T10E documentary closure.

## Non-goals

- Do not redesign CATGIS Desktop, CATSERVER or CATSERVER Web.
- Do not import historical branches wholesale.
- Do not publish forensic ZIPs, private workstation paths or secret values.
- Do not treat history rewrite as credential rotation.
- Do not rewrite history directly from an old developer clone.

## REQ-1 — Preserve canonical product state

WHEN a historical rewrite is prepared, THE SYSTEM SHALL preserve the current canonical `main` product tree and required repository documentation.

Acceptance:
- AC-1.1: post-rewrite `main` contains `catgis-desktop/`, `CATSERVER/`, `catserver-web/`, `docs/`, `README.md` and `SECURITY.md`.
- AC-1.2: repository integrity passes `git fsck --full`.
- AC-1.3: CATGIS CI passes on rewritten `main`.

## REQ-2 — Remove non-publicable historical paths

WHEN history is rewritten, THE SYSTEM SHALL remove the known credential document, decompiled third-party source and generated CATSERVER inventory CSVs from all rewritten branch/tag history.

Acceptance:
- AC-2.1: no rewritten history contains `CATSERVER/docs/07_datos_conexion_catserver.txt`.
- AC-2.2: no rewritten history contains `kosmo_decompilado/`.
- AC-2.3: no rewritten history contains tracked `CATSERVER/inventory/*.csv`.

## REQ-3 — Keep private historical values out of public artifacts

IF workstation-path or author-email anonymization is requested, THE SYSTEM SHALL receive the historical value only as a local runtime parameter and SHALL NOT commit that private literal to the public repository.

Acceptance:
- AC-3.1: committed scripts/specs/runbooks contain placeholders rather than the private historical source root.
- AC-3.2: the local rewrite can replace both Windows backslash and forward-slash variants.
- AC-3.3: optional author-email rewriting uses a local parameter and leaves no requested historical email in rewritten author/committer metadata.

## REQ-4 — Require a non-destructive local validation gate

BEFORE any remote history mutation, THE SYSTEM SHALL complete the full rewrite in a fresh disposable mirror clone without pushing.

Acceptance:
- AC-4.1: pre-rewrite refs are written to an offline file.
- AC-4.2: pre- and post-rewrite `git fsck --full` succeed.
- AC-4.3: banned-path checks succeed.
- AC-4.4: canonical-tree checks succeed.
- AC-4.5: default helper-script execution performs no remote push.

## REQ-5 — Constrain destructive publication

WHEN the operator explicitly enables remote publication, THE SYSTEM SHALL force-update only intended branch and tag refs during the controlled maintenance window.

Acceptance:
- AC-5.1: normal mode cannot force-push.
- AC-5.2: publication requires an explicit switch.
- AC-5.3: GitHub-managed refs are not mirrored/pushed.
- AC-5.4: branch and tag pushes are deliberate and separately executed.

## REQ-6 — Certify the remote after rewrite

WHEN rewritten history has been published, THE SYSTEM SHALL verify remote exposure, repository integrity and CI before declaring the rewrite complete.

Acceptance:
- AC-6.1: banned paths cannot be browsed through public branch/tag refs.
- AC-6.2: no public operational inventory CSVs remain in rewritten history.
- AC-6.3: CATGIS CI passes on rewritten `main`.
- AC-6.4: the final rewritten `main` SHA is recorded.

## REQ-7 — Prevent reintroduction of old history

AFTER remote rewrite, THE SYSTEM SHALL prevent old developer clones from pushing obsolete objects/branches back into the repository.

Acceptance:
- AC-7.1: every active workstation is re-cloned from rewritten GitHub.
- AC-7.2: pre-rewrite clones are archived/read-only or removed.
- AC-7.3: no normal development resumes from an old SHA.

## REQ-8 — Protect canonical main

AFTER post-rewrite certification, THE REPOSITORY SHALL use branch protection/ruleset controls for `main` suitable for the GitHub-first workflow.

Acceptance:
- AC-8.1: normal changes enter through PRs.
- AC-8.2: CATGIS CI is required before merge.
- AC-8.3: routine force-push/direct development to `main` is blocked.

## REQ-9 — Preserve recovery without republishing it

WHILE sanitation is in progress, THE SYSTEM SHALL retain offline recovery evidence without committing pre-sanitization refs, secrets or forensic bundles back to the public repository.

Acceptance:
- AC-9.1: forensic/recovery files remain outside public GitHub.
- AC-9.2: rollback evidence is usable locally if the rewrite must be investigated.


## REQ-10 — Close the exposure response

BEFORE the sanitation program is declared complete, THE SYSTEM SHALL resolve the security and governance consequences that are not solved by Git history rewriting alone.

Acceptance:
- AC-10.1: all CATSERVER credentials that were historically exposed, reused from exposed material, or cannot be proven out-of-scope are rotated/revoked at the source.
- AC-10.2: the active developer clone uses a GitHub noreply commit email AND account-level email privacy is explicitly confirmed (primary email visibility = private).
- AC-10.3: known pre-rewrite commit objects/cached views SHALL cease to be publicly accessible through one of these certified outcomes:
  - (a) GitHub Support purge/disposition; or
  - (b) repository isolation: the original repository is made private; a distinct new public repository exists; only the sanitized snapshot is published; no old history/refs are imported; and the old SHAs are publicly unreachable.
  - (c) privacy isolation: a superseding public repository is published whose entire reachable history is noreply-only, and the superseded public repository is made private and preserved; no non-noreply commit remains publicly reachable (`PUBLIC_NON_NOREPLY_COMMIT_COUNT=0`).
- AC-10.4: the repository's licensing posture is documented; current decision is proprietary/source-available, not open source.
- AC-10.5: only the intended persistent remote branches remain; current certified target is `main` only.

Status at T10D-R2 closure: `ACCOUNT_PRIMARY_EMAIL_VISIBILITY=PRIVATE`; `PUBLIC_PERSONAL_EMAIL_METADATA=ABSENT`; canonical public repository is `tulatraes25/CATGIS-DESKTOP` (ID `1406486908`) published from a single parentless noreply root commit; the superseded public repository (ID `1405554812`) is a preserved private privacy archive; the original repository (ID `1199177324`) remains the private forensic archive.
