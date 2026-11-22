# Audit #263 — Estado final verificable (2026-11-17/18)

Documento de cierre de rondas de remediación. **No certifica GUI física ni
A0.7/F3**: registra exactamente qué issues están corregidos/verificados, qué
queda abierto, y por qué (feature freeze, decisión de owner o gate físico).

## Main de referencia

- `main = 68a9f9665e847626e7e01ea88ab4de445ca65134` (2026-11-21 UTC).
- Post-main: **Certification** `44906799689` SUCCESS (Linux/Windows/macOS
  Intel+ARM, GDAL nativo, PostGIS real, app images), **Runtime Identity**
  `44531670539` SUCCESS (8 jobs), **CI**, **GeoParquet**, y **attestation**
  `44534311113` SUCCESS.
- Suite local: **1733 tests / 0 fallos / 15 skips**; `JAVA_SOURCE_INTEGRITY_PASS`.
- Ruleset activo `24546459` exige **25 checks** incl. `Verify exact main push
  certification`, native GDAL, PostGIS, Runtime Identity 17/21 ×4, GeoParquet e
  inventarios (evidencia: reconciliación §2/§8).

## Issues cerrados con evidencia verificada

`#43, #50, #64, #67, #70, #86, #87, #91, #94, #95, #96, #98, #99, #102, #108,
#128, #139, #144, #146, #148, #149, #150, #151, #152, #153, #155, #156, #161,
#162, #163, #164, #167, #174, #176, #193, #195, #256, #257, #258, #259, #260,
#261, #262`

Cada cierre referencia PR mergeado, tests que lo prueban y certificación
post-main con attestation (ver `AUDIT_263_RECONCILIATION_20261117.md` §4–§10).

## Issues abiertos — motivos verificados

| Issue | Estado real | Bloqueo |
|---|---|---|
| **#263** | Epic de este audit. | Se cierra cuando los residuales de abajo sean aceptados/decididos. |
| **#112** | GeoParquet M1 metadatos y M2 inspección off-EDT **certificados**; **M3 ingesta real de filas no implementada**. | **Feature freeze A0.7**: es una capacidad nueva, no un fix. Requiere decisión de producto + diseño multi-ronda (lector Parquet real no trivial). |
| **#175** | FlatGeobuf con streaming por ventanas, cancelación, límites y fail-closed **certificados**; dataset >100k features sin lector indexado. | **Feature freeze A0.7**: lector indexado/lazy es una capacidad nueva. |
| **#82** | Pan/drag sin redibujo completo: código + contrato CI **verificados** (PR #83/#84). | **Medición GUI física** (FPS/memoria/DPI en Windows real) — A0.7/F3. El propio issue prohíbe cerrarlo por CI headless. |
| **#74** | Descubrimiento portable de WhiteboxTools corregido (PR #77). | **Ejecución nativa/resultados científicos** requieren binario WhiteboxTools; no certificado. |
| **#73** | Lease entre procesos + journal de recuperación certificados (PR #75/#76). | Residuales de corte eléctrico, filesystems de red, estados ambiguos y escaneo al inicio. |
| **#132** | `executeCode` sin `deleteOnExit`; **evidencia nativa multi-OS**: 3 syntax-errors con diagnóstico no vacío, salida de 300 KB limitada y **stdin de 10 KB aceptado / >1 MB rechazado**; pasos de CI fallan ante skip. | **Anomalía Windows original** no reproducida; no se declara erradicada. |
| **#114** | Preflight + **transacción all-files con sidecars**, **familia Shapefile completa**, **UI transaccional con cancelación** y **journal durable con recuperación** (restaura previos/elimina nuevos parciales, limpia staging). | Evidencia de performance. |
| **#113** | `PolygonCoverageAudit`, validación **asíncrona**, **cobertura contra AOI** y **propuestas de reparación no destructivas exportables** (CSV/JSON atómicos, UI). | Validación física/GUI y aplicación manual revisada. |
| **#143** | Ledger vivo actualizado con SHAs/evidencia. | No es un defecto; se mantiene como memoria operativa. |
| **#109** | Spec de producto umbrella. | Se cierra con el programa completo/A0.7. |

## Selector de CRS (fajas argentinas)\n\n- `EPSG:22182` se mostraba como faja 1 porque la tabla de meridianos tenía\n  `-72` duplicado y faltaba `-54`. Corregido a\n  `-72/-69/-66/-63/-60/-57/-54`; el panel ya informa **faja 2 (-69°W)**.\n- Planisferio **regenerado desde Natural Earth** (equirectangular 2:1, 1800×900, ±90°): el\n  área de uso y el marcador caen en Argentina, no en Brasil.\n- `CRSArgentinaBoundsContractTest` (3) protege ambas regresiones.\n\n## GeoJSON sin tipado\n\n- `GeoJsonLoader` fallaba con FeatureCollections sin esquema de geometría\n  (export OSM): las entidades quedaban con `getDefaultGeometry()==null`.\n- Fix: fallback con parser limpio antes de fallar cerrado; no se publica una capa\n  sin geometrías. Test de regresión `GeoJsonLoaderContractTest` (2): LineString y\n  Polygon conservan geometría/envelope.\n- Evidencia de uso real: mapa de Comodoro Rivadavia (OSM) exportado con CATMAP.\n\n## Release readiness\n\n- Informe exacto: `docs/quality/RELEASE_READINESS_20261121.md`.\n- Guard `ReleaseReadinessContractTest`: versión consistente en AppVersion/build.gradle/\n  manifiesto/BETA_README, release beta con pre-release y 3 app-images, y\n  A0.7/F3 explícitamente `NOT_CERTIFIED`.\n- Performance smoke del lote transaccional: 40 archivos, cota 120 s, marker\n  `BATCH_TX_PERF`.\n\n## Consistencia SPEC/Kiro\n\n- El ledger `.kiro/specs/catgis-audit-20261117/tasks.md` fue reescrito como\n  estado autoritativo (sin “PR en curso”/estados parciales obsoletos) y  \n  `design.md`/`requirements.md` apuntan al estado de ejecución.\n- La decisión owner de **DC-5 (`UNSUPPORTED`)** está reflejada en\n  `.kiro/specs/catserver-decision-backlog/` y en el manifiesto.\n- Guard automatizado `KiroAuditSpecConsistencyContractTest`: falla si reaparecen\n  estados obsoletos, si se pierde la decisión DC-5 o si algún `.md` de\n  `.kiro/specs` está vacío/no es UTF-8.\n\n## Verificación funcional local

- Windows (main `68a9f96`): build **1733 tests**, `--version` 1.0.0-beta.2,
  `--self-test` **13/13** y `e2eBetaHarness` **E2E_OK checks=13**
  (proyecto/SHP/GeoTIFF/terrain/PNG/PDF/SVG/layout/workspace). Evidencia en
  `F3_RUNTIME_EVIDENCE_20261117.md`.

## Bloqueos por decisión del owner

- **DC-5 / CATSERVER T1 — RESUELTO 2026-11-19**: el owner decidió declarar
  `UNSUPPORTED` y retirar el productor de `catastro.ejido_urbano_mask` del
  alcance beta (sin fuente autoritativa/rol publicador no se inventan datos).
- **#112 / #175 — RESUELTO 2026-11-19**: el owner decidió mantener estas
  capacidades nuevas congeladas hasta la certificación A0.7; se documentan como
  `INSPECTION_ONLY` / `BETA_LIMITED` y no se implementan ahora.

## A0.7 / F3 — lo que sí hay y lo que falta

Evidencia headless/CI vigente:
- `ui-snapshot.yml` `44414255394` (3/3 Linux Xvfb/Windows/macOS ARM).
- `f3-runtime-smoke.yml` `44414260382` (3/3 app-image + launcher + E2E).
- Evidencia nativa de scripting `*_SCRIPT_LIFECYCLE_EVIDENCE tests=1 skipped=0`
  y de OS-backed credentials `*_NATIVE_*_EVIDENCE tests=1 skipped=0` (Linux,
  Windows, macOS Intel+ARM) en Certification.
- Certification multi-OS + Runtime Identity 17/21 ×4 + native GDAL + PostGIS.
- Evidencia nativa OS-backed de credenciales: Windows DPAPI, Linux Secret
  Service/unsupported, macOS Keychain (Intel+ARM), con pasos que fallan ante skip.

**No certificado** (A0.7/F3 `NOT_CERTIFIED`): matriz interactiva humana
(F3.2/F3.3/F3.4), HiDPI/foco reales, gestos del owner, impresión física,
mediciones de FPS/memoria en Windows real y aislamiento físico multi-sesión.

## Conclusión

La remediación de código/CI del audit está **sustancialmente completa y
certificada** en `main`: 43 issues cerrados con evidencia, 1733 tests verdes,
ruleset estricto y attestation por push. El **epic #263** queda pendiente únicamente de: 1) certificación A0.7/F3
física (matriz interactiva humana), y 2) decisión de programa sobre los
incrementos residuales #82/#74/#73/#114/#113/#132. Las decisiones DC-5 y
#112/#175 ya fueron tomadas por el owner (2026-11-19). No se declara GUI
interactiva certificada.
