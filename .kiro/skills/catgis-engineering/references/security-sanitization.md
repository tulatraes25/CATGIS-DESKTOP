# CATGIS security and repository sanitation

## Public repository assumptions

Treat `tulatraes25/CATGIS-DESKTOP` as public.

Never place secret values or private workstation identifiers into:

- source;
- docs;
- Kiro specs;
- issues;
- PR descriptions;
- helper scripts committed to the repository.

Use neutral placeholders and pass private historical values only as local runtime parameters.

## Historical sanitation

Known sanitation classes include:

- committed credential documents;
- decompiled third-party source;
- generated operational CATSERVER inventories;
- workstation-specific paths;
- optionally historical author email when the owner chooses to anonymize it.

The canonical helper is:

`docs/repository/Invoke-HistorySanitization.ps1`

The canonical procedure is:

`docs/repository/HISTORY_SANITIZATION_RUNBOOK.md`

Do not duplicate private values into a new helper or spec.

## Required destructive gate

Before publishing rewritten history:

1. fresh disposable mirror clone;
2. offline pre-rewrite ref map;
3. `git fsck --full`;
4. local `git filter-repo` rewrite;
5. banned-path verification;
6. canonical-tree verification;
7. second `git fsck --full`;
8. operator review;
9. controlled force-push of branches/tags only;
10. remote verification and CI;
11. branch protection restored/enabled;
12. all active workstations re-cloned.

## Credential rule

Removing a secret from Git is not rotation. Any credential ever publicly committed remains compromised until revoked/rotated at the source.

## Forensic package

Local forensic exports and recovery patches are recovery material, not public repository content. Keep them offline/private and do not use them as a source for broad bulk re-imports.
