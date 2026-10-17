# SPEC/Kiro #172 — Main attestation bound to live GitHub ref

BASE_SHA: `228e30c60c330eedf8f918d3a4ffae9e660c953d`
STATUS: CANDIDATE / NOT_CERTIFIED

## Root cause
The workflow used `github.event.pull_request.base.sha` from the event snapshot.
When PR branches were resynchronized onto a newer main, original pull_request
base SHA could remain stale and an attestation would PASS against the old main
rather than the live protected branch.

## Requirements and design
- Read live /git/ref/heads/main via the read-only actions token; reject an event
  base or actual PR base that does not exactly equal that live SHA.
- Fetch /pulls/{number} and verify event/current head SHA identity, main target,
  open PR number, and compare behind_by==0 to prove live-main ancestry.
- Keep existing independent exact-main workflow/job evidence checks unchanged.
- Repeat live main/head/base checks immediately before reporting PASS to reduce
  TOCTOU risk.
- Allow workflow_dispatch only on live refs/heads/main + exact GITHUB_SHA, so an
  unchanged SHA can be reattested after pending native jobs complete.
- Never checkout/execute untrusted PR code in the attestation workflow and never
  expand token permissions beyond read-only.
- Add offline tests extracting actual inline production policy from the YAML
  and covering stale event/base/head, wrong ancestry, dispatch and eligible runs.

## Non-goals and residual
Passing attestation does not imply a GUI E2E, real Windows OSGeo4W/JNI, or
scientific raster acceptance. PR branch heads that are updated after a workflow
event require a new PR event/attempt. A green prior main has no transfer value
to a new SHA. Full candidate CI/native and post-merge push evidence remain
mandatory; A0.7 OPEN.

## Evidence-only reattestation of unchanged branch head

GitHub rerun-failed replays the original pull_request event JSON; its old
base.sha stays stale after main moves. Passing all current main jobs cannot
make that historical event current. Enable only the read-only attestation
workflow's `pull_request.edited` event, preserving opened/synchronize/reopened.
Editing PR description is an auditable no-source-change way to create fresh
event payload and recheck live main, current PR base and exact current head.
Other full native matrices keep their default events and do not re-run solely
for an edited description; no new credentials or unsafe checkout.

Acceptance: tests assert edited trigger exists while stale SHA snapshots remain
fail-closed. Reverify actual edited event run on unchanged PR head AFTER native
main certification, do not reuse original failed run. No merged/release claim.
