# Audit #263 — Reconciliación T0 (2026-11-17)

Base: `main` `c0484a1b5da48b28146e88941f14122e1865c64f`. Release vigente:
`v1.0.0-beta.2` (pre-release, sin firma). Fuente: issue **#263**.

## 1. Estado real de `main` (snapshot)

| Gate | Run | Estado |
|---|---|---|
| CATGIS CI | 44203593791 | SUCCESS |
| CATGIS Certification | 44203592792 | SUCCESS |
| CATGIS Runtime Identity | 44203592844 | SUCCESS |
| CATGIS GeoParquet Metadata | 44203592897 | SUCCESS |
| CATGIS Main Push Attestation | 44205724219 | SUCCESS |

Evidencia runtime previa: UI snapshots run `44200187527` (Linux Xvfb/Windows/
macOS) y launcher+E2E run `44200192526` (`E2E_OK checks=13`). **A0.7/F3 GUI
interactiva siguen `NOT_CERTIFIED`**; snapshots y E2E no los sustituyen.

## 2. Gobernanza — ruleset #174

Ruleset activo `24546459 CATGIS main protection`, target `branch`
(`~DEFAULT_BRANCH`), enforcement `active`, reglas `pull_request`,
`required_status_checks`, `non_fast_forward`, `deletion`.

| Momento | Required status checks |
|---|---|
| Antes (snapshot `docs/quality/ruleset-before-20261117.json`) | 3: `Java Desktop Build & Test`, `Web Viewer Lint`, `Repository Safety Checks` |
| Después (este PR, vía API PUT) | **25**: los 3 anteriores + Certification Linux/Windows/macOS Intel+ARM, Real Native GDAL Linux/macOS Intel+ARM, PostGIS, app images Linux/Windows, Runtime Identity 17/21 de las 4 plataformas, inventarios de superficies y comandos, GeoParquet y `Verify exact main push certification` |

El `strict_required_status_checks_policy` permanece `true`. Rollback disponible:
`gh api --method PUT repos/tulatraes25/CATGIS-DESKTOP/rulesets/24546459 --input <snapshot>`
(el snapshot no incluye campos generados por el servidor; usar el JSON de
`.ruleset-before-20261117.json` como referencia de decisión).

## 3. Documentación reconciliada

- Nuevo **`docs/CAPABILITY_MANIFEST.md`**: única fuente de versión y estado de
  capacidad (SUPPORTED_VERIFIED / BETA_LIMITED / REQUIRES_DEPENDENCY /
  INSPECTION_ONLY / UNSUPPORTED).
- `docs/BETA_README.md` pasa a `1.0.0-beta.2`, comandos correctos y T2–T4 SQL
  marcado como implementado/certificado (PR #252).
- `docs/release/BETA_NOTES.md` mantiene la versión beta.2 y delega las
  limitaciones al manifiesto.

## 4. Issues del audit — estado real y tranche

| Issue(s) | Estado en `main` | Tranche |
|---|---|---|
| #144 | **CORREGIDO en PR T1-2** (staging detached + commit atómico) | T1 |
| #148 | **CORREGIDO en PR T1-3** (codec v2 reversible con compatibilidad v1) | T1 |
| #155 | **CORREGIDO en PR T1-4** (journal crash-recoverable + lock de identidad) | T1 |
| #146 | **CORREGIDO en PR T1-5** (roundtrip por tipo + feature count/source name) | T1 |
| #149 | **CORREGIDO en PR T1-6** (snapshots aislados/versionados + restauración) | T1 |
| #260 | **CORREGIDO en PR T1-1** (pendiente gates) | T1 |
| #150, #151, #195, #193, #175, #176, #153 | **PARCIAL** (CRS fuente/preview/streaming) | T1 |
| #256, #257, #258, #259, #262 | **CORREGIDO en PRs T2** (#257/#258/#262 en `6cd043d`; #256/#259 en el PR T2-2) | T2 |
| #161 | **CORREGIDO en PR T3-1** (TIFF+sidecar atomicos en proveedores) | T3 |
| #128 | **CORREGIDO en PR T3-2** (GeoKeys > sidecar + veto de conflicto) | T3 |
| #162 | **CORREGIDO en PR T3-3** (CF grid_mapping/packaging, sin WGS84 asumido) | T3 |
| #139 | **CORREGIDO núcleo en PR T3-4** (calibración obligatoria; UI calibrada pendiente) | T3 |
| #163 | **CORREGIDO en PR T3-5** (normalización explícita vs default UI) | T3 |
| #156, #261 | **CONFIRMADO** (PostGIS/credenciales) | T4 |
| #174 | **CORREGIDO EN T0** (ruleset con 25 checks) | T0 |
| DC-5 / CATSERVER T1 | **BLOQUEADO POR DECISIÓN** (fuente de máscara ejido) | T4 |
| #164, #167 | **PENDIENTE** (mojibake/WCS race) | T1/T3 |
| #95–#102, #82/#113/#114/#132, web | **PARCIAL** (CATMAP/GUI/web) | T5 |

## 5. T0 — criterios cumplidos

- [x] Spec Kiro `.kiro/specs/catgis-audit-20261117/` (requirements/design/tasks).
- [x] Manifiesto único de versión/capacidad.
- [x] Reconciliación de `BETA_README`/`BETA_NOTES` y ledger.
- [x] Ruleset revalidado y endurecido (25 contextos).
- [x] Snapshot de runs de `main` y evidencia runtime.
- [x] T1 completo (6 PRs) y T2 completo.

## 6. Límites

- Los conteos de tests (1588 locales) y los 15 skips declarados **no** son PASS
  de GUI/nativo; ver `docs/quality/SKIP_INVENTORY.md`.
- No se ejecutaron sesiones GUI interactivas ni ensayos con datos municipales.
