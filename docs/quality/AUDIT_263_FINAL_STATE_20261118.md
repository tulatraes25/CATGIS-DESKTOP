# Audit #263 — Estado final verificable (2026-11-17/18)

Documento de cierre de rondas de remediación. **No certifica GUI física ni
A0.7/F3**: registra exactamente qué issues están corregidos/verificados, qué
queda abierto, y por qué (feature freeze, decisión de owner o gate físico).

## Main de referencia

- `main = 1d1c27da454f985257a691169e6d04f9f9350a02` (2026-11-20 UTC).
- Post-main: **Certification** `44827163743` SUCCESS (Linux/Windows/macOS
  Intel+ARM, GDAL nativo, PostGIS real, app images), **Runtime Identity**
  `44531670539` SUCCESS (8 jobs), **CI**, **GeoParquet**, y **attestation**
  `44534311113` SUCCESS.
- Suite local: **1718 tests / 0 fallos / 15 skips**; `JAVA_SOURCE_INTEGRITY_PASS`.
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
| **#113** | `PolygonCoverageAudit` real, gaps/overlaps honestos, **validación asíncrona con cancelación**, y **cobertura contra AOI** (leyenda/unidades, CRS fail-closed, umbral). | Reparación reversible y validación física/GUI. |
| **#143** | Ledger vivo actualizado con SHAs/evidencia. | No es un defecto; se mantiene como memoria operativa. |
| **#109** | Spec de producto umbrella. | Se cierra con el programa completo/A0.7. |

## Consistencia SPEC/Kiro\n\n- El ledger `.kiro/specs/catgis-audit-20261117/tasks.md` fue reescrito como\n  estado autoritativo (sin “PR en curso”/estados parciales obsoletos) y  \n  `design.md`/`requirements.md` apuntan al estado de ejecución.\n- La decisión owner de **DC-5 (`UNSUPPORTED`)** está reflejada en\n  `.kiro/specs/catserver-decision-backlog/` y en el manifiesto.\n- Guard automatizado `KiroAuditSpecConsistencyContractTest`: falla si reaparecen\n  estados obsoletos, si se pierde la decisión DC-5 o si algún `.md` de\n  `.kiro/specs` está vacío/no es UTF-8.\n\n## Verificación funcional local

- Windows (main `1d1c27d`): build **1718 tests**, `--version` 1.0.0-beta.2,
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
certificada** en `main`: 43 issues cerrados con evidencia, 1718 tests verdes,
ruleset estricto y attestation por push. El **epic #263** queda pendiente únicamente de: 1) certificación A0.7/F3
física (matriz interactiva humana), y 2) decisión de programa sobre los
incrementos residuales #82/#74/#73/#114/#113/#132. Las decisiones DC-5 y
#112/#175 ya fueron tomadas por el owner (2026-11-19). No se declara GUI
interactiva certificada.
