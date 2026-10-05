# Git history sanitization runbook

This runbook describes the final destructive phase of the 2026-10 CATGIS repository sanitation.

It must be executed only from a disposable fresh clone after all useful local/divergent work has been preserved.

## Scope

Remove from all reachable Git history:

- `CATSERVER/docs/07_datos_conexion_catserver.txt`
- `kosmo_decompilado/`
- generated operational inventories under `CATSERVER/inventory/*.csv`

Also replace historical workstation-specific source roots with a neutral placeholder.

No credential values are recorded in this runbook.

## Preconditions

1. All important local work is backed up outside the repository.
2. `main` is green in GitHub Actions.
3. No active work is based on old commit SHAs.
4. Database credentials historically committed to Git are already rotated/revoked.
5. The operator has repository administration/push permission.
6. Install `git-filter-repo`.

## Fresh mirror clone

```bash
git clone --mirror https://github.com/tulatraes25/CATGIS-DESKTOP.git CATGIS-DESKTOP-sanitize.git
cd CATGIS-DESKTOP-sanitize.git
```

Record the pre-rewrite refs:

```bash
git show-ref > ../CATGIS_PRE_REWRITE_REFS.txt
git fsck --full
```

## Remove sensitive / third-party paths from all history

```bash
git filter-repo --force \
  --path CATSERVER/docs/07_datos_conexion_catserver.txt \
  --path kosmo_decompilado \
  --path-glob 'CATSERVER/inventory/*.csv' \
  --invert-paths
```

## Anonymize historical workstation paths

Do not record the old workstation path in this repository.

Use the guarded PowerShell helper and pass the historical source root privately at execution time:

```powershell
powershell -ExecutionPolicy Bypass -File .\docs\repository\Invoke-HistorySanitization.ps1 -HistoricalSourceRoot "<PRIVATE_OLD_SOURCE_ROOT>"
```

The script builds the replacement file locally and rewrites both backslash and forward-slash variants to `<SOURCE_ROOT>`.

## Verification before push

The following searches must return no tracked historical matches:

```bash
git log --all -- CATSERVER/docs/07_datos_conexion_catserver.txt
git log --all -- kosmo_decompilado
git log --all -- 'CATSERVER/inventory/*.csv'
# Workstation-path verification is performed by Invoke-HistorySanitization.ps1
# when -HistoricalSourceRoot is supplied privately.
```

Also verify repository integrity:

```bash
git fsck --full
git show-ref
```

Review the rewritten `main` tree and confirm that these remain present:

- `catgis-desktop/`
- `CATSERVER/`
- `catserver-web/`
- `docs/`
- `README.md`
- `SECURITY.md`

## Push rewritten history

This step rewrites commit SHAs and therefore requires an explicit maintenance window.

```bash
git push --force origin --all
git push --force origin --tags
```

If GitHub branch protection/rulesets are enabled, temporarily permit the controlled rewrite and restore protection immediately afterward.

## Post-push verification

1. Confirm the old credential file cannot be browsed from any branch/tag.
2. Confirm `kosmo_decompilado/` cannot be browsed from any branch/tag.
3. Confirm generated CATSERVER inventory CSVs are absent from public history.
4. Search GitHub for historical workstation paths.
5. Run CI on rewritten `main`.
6. Re-enable/verify branch protection.
7. Re-clone every active developer/workstation checkout. Do not keep using old clones.

## Old clones

All pre-rewrite clones contain obsolete object history. They must not push old branches back to GitHub.

Preferred action:

```bash
git remote -v
```

Record any unpushed work, then archive/delete the old clone and make a fresh clone from the rewritten repository.

## Rollback evidence

`CATGIS_PRE_REWRITE_REFS.txt` is an offline recovery map only. Do not commit it back to the public repository if it references pre-sanitization objects.

## Important

History rewriting is not credential rotation. Any credential that was ever public must remain considered compromised even after the Git objects are removed from normal GitHub reachability.
