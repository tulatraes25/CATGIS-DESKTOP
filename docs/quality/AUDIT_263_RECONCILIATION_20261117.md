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
| #175 | **PARCIAL** (lector indexado >100k pendiente) | T1/T3 |
| #195 | **CORREGIDO/VERIFICADO** (SHP/GeoJSON/KML con fixtures reales y roundtrip) | T1 |
| #256, #257, #258, #259, #262 | **CORREGIDO en PRs T2** (#257/#258/#262 en `6cd043d`; #256/#259 en el PR T2-2) | T2 |
| #161 | **CORREGIDO en PR T3-1** (TIFF+sidecar atomicos en proveedores) | T3 |
| #128 | **CORREGIDO en PR T3-2** (GeoKeys > sidecar + veto de conflicto) | T3 |
| #162 | **CORREGIDO en PR T3-3** (CF grid_mapping/packaging, sin WGS84 asumido) | T3 |
| #139 | **CORREGIDO/VERIFICADO** (UI con calibración explícita y export analítico; PR T3-9) | T3 |
| #163 | **CORREGIDO en PR T3-5** (normalización explícita vs default UI) | T3 |
| #261 | **CORREGIDO/VERIFICADO** (vault v2 + DPAPI/Keychain/Secret Service nativos con evidencia CI multi-OS; matriz física bajo A0.7) | T4 |
| #156 | **CORREGIDO en PR T4-2** (staging + swap transaccional; verificación CI PostGIS) | T4 |
| #174 | **CORREGIDO EN T0** (ruleset con 25 checks) | T0 |
| DC-5 / CATSERVER T1 | **RESUELTO POR DECISIÓN** (2026-11-19: `UNSUPPORTED`, retirado del alcance beta sin fuente autoritativa) | T4 |
| #43 | **ACEPTACIÓN VERIFICADA** (evidencia WCS payload) | T4 |
| #108 | **INTEGRADO/VERIFICADO** (DescribeCoverage en descarga real; A0.7 GUI física aparte) | T4 |
| #164 | **CORREGIDO/VERIFICADO** (contrato de encoding) | T1 |
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
## 8. Cierre administrativo de issues (2026-11-17)

Los siguientes issues ya tenían corrección mergeada, tests y `main` certificado;
se cerraron en GitHub con comentario de evidencia apuntando al PR, al test y a
`main = 0acd7052ec9238dd1cdf8a49c1f2de1d7f6adb02` (Certification post-main +
attestation `44356056579`):

`#128, #144, #146, #148, #149, #150, #151, #153, #155, #156, #161, #162, #163, #164, #167,
#174, #176, #193, #195, #256, #257, #258, #259, #260, #261, #262`, y previamente `#43` y `#108`.

### Issues que permanecen ABIERTOS (trabajo real pendiente o decisión)

| Issue | Estado honesto |
|---|---|
| #263 | Epic de auditoría (se cierra sólo con el mapa completo remediado). |
| #112 | GeoParquet: M1/M2 certificados; **M3 ingesta real de filas NO implementada** (feature freeze A0.7). |
| #175 | FlatGeobuf: streaming por ventanas/cancelación certificado; **lector indexado >100k pendiente**. |
| #139 | Núcleo de calibración espectral certificado; **UI de índices calibrados pendiente**. |


| #139 | Cerrado con UI calibrada explícita (ver `docs/quality/SPECTRAL_CALIBRATION_UI_20261117.md`). |
| #143, #132, #114, #113 | #132 temporal + lifecycle nativo; #114 transaccion, familia SHP, UI, cancelacion y recovery de crash; #113 worker async + cobertura AOI; reparacion/fisico residuales. |
| A0.x CATMAP (#82..#102) | Gates de arquitectura/GUI CATMAP. |
## 9. CATMAP A0.x — cierres de implementación (2026-11-17)

Cerrados en GitHub con evidencia de tests en `main = 3e07760` (Certification +
Runtime Identity post-main SUCCESS; attestation `44391736517`):

| Issue | Evidencia headless/CI |
|---|---|
| #95 save atómico .catmap + backup | `CatmapAtomicSaveContractTest` (ATOMIC_MOVE sin fallback, staging único, fallo preserva destino/backup, symlink/directorio). |
| #99 exports PDF/PNG/SVG atómicos y SVG vectorial honesto | `CatmapAtomicExportContractTest`, `SvgVectorExporterContractTest`, `CatmapSvgAtomicExportContractTest`; UI usa `SvgVectorExporter`. |
| #98 multipágina persistente + imágenes | `CatmapMultipageImageContractTest` (v2 todas las páginas, v1 compatible, encoder error fail-closed), `CatmapPageGeometryContractTest`. |
| #96 Atlas sin mutar template y memoria acotada | `AtlasEngineSafetyContractTest` (repetibilidad, límites, streaming, publisher falla sin publicar). |
| #102 edición/undo por página activa | `CatmapActivePageEditingContractTest` (edición/undo/hit-test por página, roundtrip, drag). |

**Importante:** estos cierres son de implementación y tests headless/CI. **A0.7/F3
(interacción GUI física, HiDPI, focus, owner gestures) siguen NOT_CERTIFIED.**
## 10. Gates WCS / XYZ / GDAL cerrados (2026-11-17)

Cerrados en GitHub con evidencia en `main = f4d8067` (attestation `44407049092`):

| Issue | Evidencia |
|---|---|
| #86 presupuesto bytes/tiempo WCS | `WcsAtomicStreamDownloadContractTest`: presupuesto configurable, Content-Length temprano, agregado v2->v1, deadline compartido, abort/limpieza. |
| #87 publicacion atomica/concurrencia WCS | `publishStagedCoverage` solo ATOMIC_MOVE, fallo cerrado sin fallback, symlink/directorio rechazados, lock de destino. |
| #91 WCS decode fuera del EDT | `WcsRasterAsyncLoadContractTest` (worker async, cancelacion unica, fallo de callback, cierre sin import). |
| #94 dimensiones/preflight WCS | `WcsDimensionsPreflightContractTest` (6): sin clamp 512, overflow, presupuesto real, mensajes. |
| #50 cache XYZ con presupuesto de bytes | `OnlineTileCache` 128 MiB + contabilidad; `OnlineTileByteBudgetIntegrityContractTest` y suites asociadas. |
| #64 GDAL portable macOS/Linux | `GdalSupport` resolucion confiable por plataforma; `GdalSupportPortableResolverContractTest` (9). |
| #67 gdal2tiles moderno/XYZ-TMS | `Gdal2TilesCommandContractTest` (7) + `Gdal2TilesRealXyzIntegrationTest` real. |
| #70 publicacion transaccional de tiles GDAL | `GdalTileOutputTransactionContractTest` (14) + lock/journal. |
| #152 SpatiaLite sin primera tabla automatica | `SpatiaLiteSourceCrsTruthContractTest.selectedTableMustExistAndCannotBeSubstitutedByFirstAvailable`. |

**Alcance:** evidencia headless/CI; **A0.7/F3 (GUI/runtime fisico) sigue NOT_CERTIFIED**.
## 11. Estado final verificable (2026-11-18/19)

Ver `docs/quality/AUDIT_263_FINAL_STATE_20261118.md`: `main` `bf671b4`
certificado (Certification `44531670621`, Runtime `44531670539`, attestation
`44534311113`), 43 issues cerrados con evidencia, 1707 tests. Abiertos: #263
epic, #112/#175 (feature freeze A0.7), #82/#74/#73 (físicos), #132/#114/#113
(residuales), #143 (ledger), #109 (spec). Decisiones del owner 2026-11-19:
DC-5 `UNSUPPORTED`; #112/#175 congelados hasta A0.7. A0.7/F3 siguen NOT_CERTIFIED.