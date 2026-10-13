# GitHub certification and authenticated transfer

Read before opening, merging or attesting CATGIS work.

1. Fetch main, record base SHA; read the actual CI workflows on that SHA. Find conflicting PRs.
2. Create one short branch from exact base, edit only the necessary source, tests and SPEC. Audit complete diff, secrets/licensing.
3. Open draft PR; inspect its exact head SHA. Require CATGIS CI and CATGIS Certification pull_request runs SUCCESS for **that SHA**, with all mandatory jobs. Inspect JUnit counts/skips and real native raster evidence, not only check badges.
4. Require CATGIS Main Push Attestation verifying the *prior main* event=push CI and Certification, with exact base SHA and all required jobs. If pre-main CI still runs, wait for completed evidence within current available work and rerun attestation on identical SHA. Do not merge while BLOCKED.
5. Mark ready only after success, then normal merge with expected_head_sha (no force-push, squash or rebase by default).
6. Fetch actual merged main SHA; demand fresh event=push CATGIS CI and Certification **on that merged SHA**. Obtain authenticated post-main attestation, even if it requires an evidence-only non-merged PR as designed by workflow.
7. Only declare CLOSED_CERTIFIED for the specific gate after post-main evidence and relevant native/GUI requirements. Maintain unresolved issues explicitly.

## Evidence record
- BASE_SHA: 40 hex, observed
- PR: # and HEAD_SHA
- PR_CI_RUN / PR_CERT_RUN / BASE_PUSH_ATTEST_RUN: IDs + conclusions
- MERGE_SHA
- POST_MAIN_CI_RUN / POST_MAIN_CERT_RUN / POST_MAIN_ATTEST_RUN: IDs + conclusions
- REQUIRED_JOBS / SKIPS / PLATFORM_NATIVE_EVIDENCE
- STATUS: CLOSED_CERTIFIED / PARTIAL / BLOCKED / NOT_CERTIFIED
- RESIDUAL_ISSUES and next exact task

## Intended workload families
Review .github/workflows/{ci,certification,main-push-attestation}.yml on the candidate revision. As applicable, require Java17/Gradle tests, Checkstyle/coverage, repository safety, web lint, Windows+Linux+macOS Intel+Apple Silicon suites and app images, PostGIS real integration, native GDAL Linux/Mac actual GIS roundtrips, architecture and source/visible command inventories. Real Windows OSGeo4W/JNI/GUI require separate physical evidence until implemented.

A green PR is not a certified post-merge main. An optional skipped storage-cleanup step is not the same as a silently skipped GIS test. Review exact SHA, triggered event and individual job conclusions. Treat genuine infrastructure flakes as such only after logs confirm.
