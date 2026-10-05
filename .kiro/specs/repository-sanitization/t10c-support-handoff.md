# T10C — GitHub Support Purge Handoff

Status: CLOSED_CERTIFIED_BY_REPOSITORY_ISOLATION (T10C); superseded public lineage under T10D-R2 privacy isolation

This file contains only repository-safe metadata required to hand T10C to GitHub Support. The private evidence bundle remains outside Git and MUST NOT be committed.

## Selected method: repository isolation

T10C was closed by repository isolation rather than by relying on GitHub Support:

- the current public repository is converted to a private forensic archive and renamed (`CATGIS-DESKTOP-FORENSIC-ARCHIVE-20261005`); it is never deleted;
- a distinct new public repository named `tulatraes25/CATGIS-DESKTOP` is created from exactly one parentless root commit whose tree equals the last certified sanitized `main` tree;
- no historical commits, refs/pull, branches, tags, issues, reflogs, bundles, quarantines or LFS objects are imported;
- old pre-rewrite SHAs must be publicly unreachable from the new public repository.

Repository isolation certified successfully. GitHub Support disposition is no longer required for T10C. The Support request/evidence below is retained offline only as historical fallback material.

## Certified repository-isolation result

- Original repository ID: `1199177324`
- Original repository archive: `tulatraes25/CATGIS-DESKTOP-FORENSIC-ARCHIVE-20261005`
- Original repository visibility: PRIVATE
- New canonical public repository ID: `1405554812`
- New public root commit: `325d3cb71124bc70e43dd6a0e31cb8869917e484`
- New public root tree: `816795841a35996a45bbfbd4871387a0c4f5f679`
- Root parents: 0
- Public history count at isolation certification: 1
- Root CI: SUCCESS (run `37295650195`, 3/3 jobs)
- New main ruleset: `24496251`, ACTIVE
- Known pre-rewrite control SHAs: PUBLICLY UNREACHABLE
- Canonical Windows clone: physical cutover PASS; fsck/clean/origin/noreply PASS
- Old local clone: archived and push-disabled
- File copy during cutover: NO
- Remote mutation during physical cutover: 0

## Certified repository state at T10B closure

- Repository: `tulatraes25/CATGIS-DESKTOP`
- T10B certification base: `592ea1e6d6ba3961b75060b1a414ebae20940669`
- Persistent remote branches at certification: 1 (`main`)
- Public tags: 0
- Open PRs: 0
- Forks: 0
- Credentials rotated/revoked: YES
- T10B status: `CLOSED_CERTIFIED`

## Git-filter-repo / Support metadata

- Affected pull refs: 34
- First changed commit: `bd7811e92da2624afbe5fb497696aad9ad119ecf`
- Orphaned LFS objects: NO
- Public branch refs sanitized: YES
- Known stale pre-rewrite direct-SHA/cached reachability: confirmed before Support handoff
- Secrets included in this handoff: NO

## Private bundle boundary

The operator-side Support bundle is intentionally kept outside the repository. The current local filename is:

`%TEMP%\CATGIS_GITHUB_SUPPORT_PURGE_BUNDLE_UPDATED.txt`

Do not commit the private bundle, forensic quarantines, historical credential material, password values, private workstation roots, or tainted secret-bearing content.

## Support request objective

Ask GitHub Support to review stale pre-rewrite objects/cached views that remain reachable after the completed `git-filter-repo` rewrite and public-ref cleanup.

Acceptable T10C closure outcomes:

1. GitHub Support performs server-side dereferencing/cache cleanup/garbage collection and the known stale objects become unreachable; or
2. GitHub Support explicitly states that no further purge is required after the completed credential rotation and repository cleanup.

These Support outcomes are no longer prerequisites because AC-10.3 was satisfied by certified repository isolation.

## Post-Support verification

If Support reports a purge, independently retest the known old pre-rewrite SHAs used during T10 audit. They must no longer be retrievable through the GitHub API/direct commit lookup before T10C is certified as purge-complete.

If Support declines further purge as unnecessary, archive the disposition outside Git and record only the non-sensitive decision in the Kiro spec.

## Remaining closure gates after T10C

- T10D: confirm GitHub account email privacy or document the residual choice; record the optional historical author-email rewrite decision.
- T10E: reconcile issue #28, final repository/spec documentation, protected PR CI, post-main CI, branch cleanup, and final `CLOSED_CERTIFIED` decision.
- P0 remains unauthorized until T10E closes.
