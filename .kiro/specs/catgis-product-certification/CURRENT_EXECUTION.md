# CATGIS - current execution checkpoint

Checkpoint date: 2026-10-15. Status: ACTIVE / A0.7 OPEN.
Observed main: `28f23e272c97580377f87c2e4d70ac022026d526`.
Observed main tree: `8cfe649a48459a17723005b046142a28de92ddce`.

This is a dated execution pointer, not permission to trust an old SHA. At the start of every round, read live main, open PRs/issues and exact-source workflow evidence. This file and tasks.md replace the stale instruction to resume PR #36 against an old base; historical phase entries remain preserved rather than being silently rewritten as current certification.

## Product direction

Issue #109 requires professional capability, scientific correctness, modular architecture and first-class UX together. Ease of use alone is not the goal. Add genuinely missing capabilities and finish partial ones using existing engines and independent tests. Do not inflate feature counts from menus, copy competitor source/assets or claim an exhaustive audit of all GitHub repositories. The dated reference review is `docs/research/GIS_CAPABILITY_REVIEW_2026-10-15.md`.

## Completed this checkpoint: PR #115 batch planning

- Previous main: `48c46e957552d28613c367632fafe7b0a5cd7433`.
- Candidate: `9321b6586d02c90b08a3a5eedb5393320b92903f`.
- CI `38766121444`: SUCCESS; Certification `38766121490`: SUCCESS.
- Normal merge: `28f23e272c97580377f87c2e4d70ac022026d526`.
- Fresh merged-main push CI `38767845106` and Certification `38767844679`, attempt 1: SUCCESS.
- Independent post-main attestation `38769150795`, and reconfirmation `38769953555` / job `116542843512`: SUCCESS. The latter logs explicitly name main `28f23e...`, those push runs and all required suite/package/native jobs.

The verified code tranche validates the entire immutable batch source/destination plan before any callback, including known spelling/hard-link collisions. Twelve genuine filesystem contracts were added; old tests stayed intact. It does not certify all-file transactions, cancellation, interprocess publication, hostile path races, arbitrary callback behavior or dataset sidecars. Issue #114 remains open for those residuals and full UI/provider-specific evidence. Do not recreate the completed plan as a new feature.

## Active code PR: #116 polygon coverage audit

- Branch: `quality/a07-coverage-audit-post115`.
- Exact base: `28f23e272c97580377f87c2e4d70ac022026d526`.
- Latest candidate: `e2792652399fc1137fa7ea5142d14a5fa8b17418`.
- Candidate tree: `80e3a452e8f4e080ec515f3ab4d348d208505685`.
- Verified Linux full-suite checkout: synthetic PR merge `b43034a9f8b7b9814b2b80f1ae4705278e43a4ec`. Git commit metadata proves its tree is exactly `80e3a452...` and parents are the base and candidate above. A PR-associated run is not described as a different checkout.
- Candidate CI `38769953550`: SUCCESS.
- Candidate Certification `38769953541`: STILL IN PROGRESS at checkpoint preparation. Full Linux suite job `116543562147` completed successfully, including coverage and skip accounting; its log lists the existing 15 dependency skips and no failures. Mac/Windows/native jobs must be checked individually to completion before merging. This document does not turn partial workflow progress into success.
- Candidate prior-main attestation `38769953555`: SUCCESS on base `28f23e...`; this is not post-merge evidence for #116.
- PR remains DRAFT / NOT_MERGED at this checkpoint.

The tranche replaces pairwise rectangular-envelope gap heuristics with whole-layer polygon union/shell/difference semantics and adds the explicit-study-area completeness BACKEND. It preserves islands, AOI holes, SRID and input geometry and distinguishes VALID / INVALID / NOT_EVALUATED for these rules. It does not expose a new AOI UI tool.

Review of the first candidate found a further genuine case: valid MultiPolygons can intersect in area plus an independent edge/point. The updated head preserves polygonal overlap rather than rejecting the mixed result. There are now 18 new real JTS/GeoTools contracts across two classes, plus five strengthened old assertions; all 16 original methods remain. Do not merge the earlier `7863f9e...` candidate or reuse its green runs for the current source.

## Exact next gates

1. Re-read #116 head and current main; inspect CI `38769953550`, Certification `38769953541` and current predecessor-main attestation. Require all mandatory jobs, with no new silent test skips. Repair actual failures at root cause; never bypass the remaining Mac/Windows/native jobs.
2. If the exact candidate is fully green and compatible with live main, use a normal merge with expected head SHA. Record the returned actual merge SHA, then require NEW main event=push CI/Certification and independent post-main attestation. Do not claim that a successful PR certifies the merged commit.
3. This continuity change is documentation-only. Do not merge it ahead of or accidentally into the running #116 candidate merely to refresh paperwork. After code integration, reconcile its base/head and checkpoint with live evidence; its green tests cannot certify code missing from its own source tree.
4. Prioritize genuine GeoParquet support #112 next: replace constant/fabricated metadata with a tested bounded format adapter and actual geometry/attribute ingestion. Audit and reuse existing GDAL/data adapters before selecting a new dependency. Driver availability, CRS semantics, real fixtures, malformed inputs and installed-OS behavior require their own acceptance evidence. No GeoParquet implementation change occurred in this checkpoint.
5. Continue #113 integration: MainMenuBar's existing topology calls are synchronous and need a safe background coordinator, explicit evaluation status, progress/cancel semantics and no stale project/selection writes. Its current report consumes issue messages, not missing geometry/feature indices. AOI selection, linked issue layers, export and repair preview remain separate work, not implied by the backend tests.

## Remaining architecture and product constraints

A0.2/A0.3/A0.4/A0.5 and A0.7 remain open. Historical source-import budgets and old phase checkboxes need exact-source reconciliation, not automatic promotion. Core polygon computation uses existing JTS/JDK, with GeoTools schema handling in its adapter; it does not add another engine or move GIS logic into Swing.

Current coverage caps are conservative work limits, not measured heap guarantees. JTS primitive overlay is not hard-preemptible; cancellation is observed between stages/loops. Other legacy topology rules, geodesic conversion, full CRS authority reconciliation and large-data performance remain unverified by this tranche.

No user-PC/GUI/OpenCode run or destructive local change is authorized before architecture prerequisites close. GUI is a separate later evidence layer, not a circular prerequisite for performing deterministic architecture tests. Windows/Linux/Mac Intel/Mac ARM compilation, suite, app image, CLI, JNI and physical GUI evidence must stay distinct. A documentation PR, dependency resolver or packaged app is never proof of a flawless whole product.
