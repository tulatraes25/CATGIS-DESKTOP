# GitHub Storage Optimization — Design

Status: ACTIVE
Baseline: `963133c6d585cfde8bc2ea377a35af34b031321b`

## Scope

This gate targets GitHub Actions storage, not product behavior.

The storage audit shows that the Git repository itself is small relative to the reported quota concern. The current tree is approximately 15.85 MB and one runtime-referenced DOCX accounts for roughly half of that tree. Removing product resources would therefore create risk with negligible quota benefit.

The dominant controllable source is Actions storage:
- repeated Gradle cache writes on post-main runs;
- repeated JaCoCo/test HTML artifacts;
- 90-day default artifact retention.

## Non-goals

- no Git history rewrite;
- no removal of runtime-referenced manuals/resources;
- no removal of tests or fixtures;
- no reduction in test coverage requirements;
- no disabling of Linux, Windows, PostGIS or architecture certification;
- no OpenCode/local runtime work.

## Workflow changes

### CATGIS CI

- Set `cache-disabled: true` on `gradle/actions/setup-gradle`.
- Keep npm cache because its key is content-addressed by `package-lock.json` and observed size is small.
- Upload Java test diagnostics only on failure.
- Upload JaCoCo report only on `push` to `main`.
- Set explicit artifact retention to 7 days.

### CATGIS Certification

- Set `cache-disabled: true` on every Gradle setup step.
- Keep all certification jobs and test commands unchanged.
- Upload evidence only when:
  - the run is a `push` to `main`, or
  - the job is failing and diagnostics are useful.
- Set explicit retention to 7 days.

## Storage maintenance workflow

Create `.github/workflows/storage-maintenance.yml`.

Triggers:
- weekly schedule;
- manual `workflow_dispatch`;
- `push` to `main`, but the cleanup job executes on push only when the head commit message contains `[storage-cleanup]`.

Permissions:
- `contents: read`;
- `actions: write`.

Bootstrap behavior:
- delete every existing Actions cache;
- delete every existing Actions artifact;
- preserve workflow run records and logs.

Scheduled/manual behavior:
- delete every Actions cache;
- delete artifacts older than 7 days.

Implementation uses GitHub CLI REST calls with the repository-scoped `GITHUB_TOKEN`.

## Evidence model after cleanup

Certification authority remains:
- exact Git SHA;
- PR and workflow run ID;
- workflow/job/step status;
- source-controlled inventories/specs;
- short-lived artifacts where useful.

Artifacts are supplementary evidence, not the sole evidence source.

## Risk

### Risk: slower CI
Disabling Gradle cache can increase setup/build time.

Mitigation:
- correctness gates remain unchanged;
- future cache reintroduction requires an explicit bounded-storage design.

### Risk: artifact purge removes convenient historical downloads
Mitigation:
- all artifacts are reproducible from exact SHAs;
- workflow run logs remain;
- source-controlled certification manifests remain;
- only Actions artifacts/caches are deleted.

### Risk: cleanup accidentally touches repository data
Mitigation:
- maintenance workflow only calls `actions/caches` and `actions/artifacts` delete endpoints;
- no Git refs/content APIs are used for deletion.

## Rollback

If build time becomes unacceptable:
1. revert only the cache-disabled workflow changes;
2. introduce a new cache policy with explicit bounded keys/retention;
3. do not restore deleted historical artifacts because they are reproducible from exact SHAs.
