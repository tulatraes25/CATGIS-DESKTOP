# CATSERVER Decision Backlog — Design


> **Decisión DC-5 (2026-11-19):** sin fuente autoritativa ni rol
> publicador, `catastro.ejido_urbano_mask` se declara `UNSUPPORTED` y se
> retira del alcance beta. Este documento queda como análisis histórico;
> no se implementa productor. Ver `tasks.md` y el manifiesto.

Status: ACTIVE (decision-gated)
Baseline: `f965869a8ef73e36afae0de6218cd4191ce8a151` (main after PR #215)
Inventory date: 2026-11-15

This design records the evidence gathered during the `repository-audit-remediation`
closure so each decision can be made with facts instead of re-auditing. All
counts and SHAs are snapshots at the baseline above.

## 1. DC-5 producer — what must be produced

Required contract (already documented and fail-closed in
`CATSERVER/sql/_common/assert_ejido_mask.sql`, certified in PR #212):

| Requisito | Detalle |
|---|---|
| Tabla | `catastro.ejido_urbano_mask` |
| Geometria | columna `geom`, SRID 4326 |
| Identidad | columna `gid` (publicada como capa del visor por `sql/13`/`sql/18`) |
| Consumidores | `sql/10` (3 JOIN), `sql/11` (5 JOIN), `sql/18` (view `catastro.ejido_urbano`) |

Source referenced by docs and the delivery package: `ejido\ejido_urbano_mask.shp`
(`scripts/16_build_shape_delivery_package.ps1` uses the same `SourceShape`).
Open question: does that file come from the ejido survey export, from the CAD
DWG pipeline, or from the municipal GIS service? That determines whether the
producer is an `ogr2ogr` import script or a derived step in `sql/10`.

## 2. DC-6 — collision analysis

| Object | Script | Statement |
|---|---|---|
| Table `catastro.circunscripcion_sector` | `sql/04` | `CREATE TABLE IF NOT EXISTS` (line 47), spatial index (111), trigger (132), grants (185/191) |
| View `catastro.circunscripcion_sector` | `sql/18` | `CREATE OR REPLACE VIEW` (line 23), `COMMENT ON VIEW` (85), catalog grants (104/124/147) |
| Consumer | `sql/19` | view `ordenamiento_territorial.tierras_circunscripcion_sector` selects from it (47) |

A full 00→21 run reaches `sql/18` with the table already created by `sql/04` and
aborts with `"circunscripcion_sector" is a table`. The operational catalog
(`sql/13` line 147 in `sql/18`) publishes the object as a layer, so whichever
object wins must keep `gid`-style identity for the viewer.

Options: (a) view canonical + rename the base table to `circunscripcion_sector_raw`
(staging), (b) separate schemas, (c) rename the view and update catalog/consumers.
Recommended: (a) if the view is the operational object; it keeps the published
layer name stable and leaves the raw table for ETL.

## 3. DC-7 — role/default-privilege matrix

| Script | Default privileges for | Observations |
|---|---|---|
| `sql/02` | `catserver_owner` in 8 schemas | role is NOLOGIN and never creates objects in the documented pipeline, so its defaults never fire |
| `sql/12` | `postgres` in 8 schema groups | matches the documentation that imports run as `postgres`, but diverges from `sql/02` and from `catserver_owner` ownership of the database |
| `sql/19` | mixed families | grants on existing objects, no defaults |

The certification PostGIS job currently creates a `postgres` role to mirror a
default installation (documented in `certification.yml`). Options: (a) all DDL
runs as `catserver_owner` (change the documented pipeline and CI to `SET ROLE`),
(b) parameterize the creator role and keep `postgres` for compatibility,
(c) split DDL role (`catserver_owner`) from import role (`postgres`) and set
defaults for both explicitly.

## 4. DC-8 — destructive-path inventory (sample)

| Script | Operation | Risk |
|---|---|---|
| `scripts/07_export_shape.ps1:33` | `Get-ChildItem ... \| Remove-Item -Force` | deletes previous export before writing |
| `scripts/10_export_dxf_thematic_shapes.ps1:36,69` | wildcard `Remove-Item -Force` | same, twice |
| `scripts/12_clip_shapefile_to_mask.ps1:26,46` | wildcard `Remove-Item -Force` | same |
| `scripts/14_import_cad_shapes_postgis.ps1:36` | `DROP TABLE IF EXISTS $TargetTable` | drops live PostGIS table before load |
| `scripts/16_build_shape_delivery_package.ps1:63` | `Remove-Item -Recurse -Force` | deletes package directory |
| docs `03_pipeline_etl.md` / `11_...` | `ogr2ogr --overwrite` guidance | documented in-place overwrite |

Options: (a) fail-closed `-Force` style confirmation parameter, (b) stage to a
temp directory and swap on success, (c) backup suffix before delete. Recommended:
(b) where a writer exists, (a) for table-level operations.

## 5. DC-9 — branch inventory (estado inicial 160: 114 mergeadas, 46 sin mergear; ejecutado)

**Ejecutado el 2026-11-15:** se borraron las **113** ramas contenidas en `main`
(excluyendo `main`), con SHA registrado en
`docs/quality/BRANCH_INVENTORY_20261115.md`; quedan **47** ramas (main + 46
históricas sin mergear, conservadas para triage). La lista original de las 46 no
contenidas al momento del inventario:

```
audit/fgb-crs-post-main-20261017          fix/vector-singlefile-atomic-post-las-20261017
docs/kiro-current-execution-post115       fix/wfs-unknown-crs-current-main-20261017
fix/fgb-header-crs-verified-20261017      fix/wfs-unspecified-request-crs
fix/fgb-source-crs-truth-staging          fix/wfs-wcs-utf8-current-main-20261017
fix/fgb-unknown-crs-guard-20261017        perf/p0-pan-lag
fix/las-header-integrity-containment      quality/a0-gdal2tiles-autozoom-bounded-process
fix/local-file-crs-after-wfs-20261017     quality/a0-linux-gdal-native-path-portability-post55
fix/project-live-state-post-ux-20261017   quality/a0-main-push-certification-attestation
fix/project-specialized-layer-codec-post136 quality/a0-map-render-state-characterization
fix/project-state-authority               quality/a0-unix-gdal-explicit-native-bin
fix/project-unsaved-appcontext-post-las-20261017 quality/a0-wcs-atomic-stream-download
fix/shp-geojson-kml-source-crs-roundtrip-20261017 quality/a0-wcs-bounded-pixel-integrity-post77
fix/source-crs-truth-fgb-las-post142      quality/a0-wcs-tiff-structural-publication-guard
fix/specialized-layer-payload-post-las-20261017 quality/a0-wms-disk-cache-off-edt
fix/ui-source-utf8-literals-20261017      quality/a0-xyz-bounded-image-decoding
fix/ux-mojibake-post160                    quality/a0-xyz-portable-persistent-cache-root
fix/vector-atomic-post-wfs-20261017       quality/a0-xyz-source-cache-isolation
fix/vector-single-file-atomic-post142     quality/a0-xyz-tile-cache-off-edt
fix/vector-singlefile-atomic-current-main-20261017 quality/a03-catmap-atlas-isolated-page-output
fix/vector-singlefile-atomic-post-las-20261017 quality/a04-wcs-tiff-pixel-integrity
quality/attest-main-pr51-exact            quality/attest-main-pr59
quality/f1-ui-command-carrier-coverage    quality/tool-approval-audit-post133
research/geoparquet-rows-probe-post121    test/wcs-deadline-cold-start
test/wcs-shared-deadline-stable-20261017  quality/attest-main-pr59
```

Categorization used for the removal decision: historical candidate stacks for
already-merged work (vector atomic, project state/payload, CRS/FGB/LAS, UX
mojibake), A0/quality experiments whose evidence lives in the certification
specs, attestation attempts, and two research branches. The 113 merged branches
are safe to delete after approval; the 46 need a per-branch check (unique trees
versus the merged rescue content).

## 6. DC-10 — advisories detail

```
brace-expansion: HIGH  (6 advisories: DoS/OOM/quadratic/recursion)
minimatch:       MODERATE (globparts amplification)
@eslint/config-array: MODERATE (depends on minimatch)
eslint:          MODERATE (depends on minimatch)
```

Chain: `eslint` → `@eslint/config-array` → `minimatch` → `brace-expansion`.
Dev-only: not part of the shipped web bundle. `npm audit fix` would upgrade the
lint toolchain; that is a dependency change outside the bugfix program and needs
its own authorization plus a Web Viewer Lint re-certification.

## 7. DC-12 — skip inventory (15 skipped, 14 markers)

| Test class | Gate |
|---|---|
| `FlatGeobufRealTest` (2) | optional FlatGeobuf native/driver availability |
| `GeoPackageRealTest` (2) | optional GeoPackage driver availability |
| `SpatiaLiteRealTest` (2) | optional SpatiaLite driver availability |
| `HeavyFormatsIntegrationTest` (2) | heavy format fixture availability |
| `ReleaseFormatCompatibilityTest` (1) | release-format fixture availability |
| `ArchitectureCertificationTest` (2) | architecture policy assertions |

These are environment gates, not ignored defects. The freeze rule: the count may
not grow without an equivalent documented gate.

## Validation approach (per item)

- DC-5/DC-6/DC-7: executable PostgreSQL evidence in the certification PostGIS
  job (fresh bootstrap on `"CATSERVER"`), plus static contract checks.
- DC-8: static contract over the scripts plus a documented policy; no execution
  of destructive paths against user data.
- DC-9: `git ls-remote`-based inventory re-validated at execution; deletions
  only after explicit approval, with before/after counts recorded.
- DC-10: `npm audit` output plus Web Viewer Lint run.
- DC-11/DC-12: inventory documents with dated evidence; no behavior claims.
