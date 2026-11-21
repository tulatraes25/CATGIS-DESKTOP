# CATGIS — Remediación audit #263 — Tasks (estado autoritativo)

**Estado:** fase de remediación cerrada; ejecución verificada sobre `main`
`1756e0b` (código `1d1c27d`), 2026-11-20.
**Evidencia:** [AUDIT_263_FINAL_STATE_20261118.md](../../../docs/quality/AUDIT_263_FINAL_STATE_20261118.md),
[reconciliación](../../../docs/quality/AUDIT_263_RECONCILIATION_20261117.md),
[manifiesto](../../../docs/CAPABILITY_MANIFEST.md),
[F3](../../../docs/quality/F3_RUNTIME_EVIDENCE_20261117.md).

> Este archivo reemplaza las notas cronológicas con estados intermedios
> (“PR en curso”, “parcial”) que quedaron obsoletas. Cualquier discrepancia se
> resuelve con GitHub `main`, los runs exactos y el manifiesto.

## Criterios de aceptación (cumplidos)

- [x] Caso RED reproducido antes del fix (test/oráculo) en cada PR.
- [x] Fix mínimo, compatibilidad y migración versionada cuando aplica.
- [x] CI + Certification (Linux/Windows/macOS Intel+ARM) + Runtime Identity
      17/21 ×4 + GeoParquet + GDAL nativo + PostGIS real.
- [x] Merge con head esperado y **attestation** post-main (última: `44833072706`).
- [x] Manifiesto/documentación/ledger actualizados por tranche.
- [x] Suite local: **1718 tests / 0 fallos / 15 skips**; integridad
      `JAVA_SOURCE_INTEGRITY_PASS files=899`.
- [x] `--version`, `--self-test` 13/13 y `e2eBetaHarness` E2E_OK 13/13.

## T0 — Gobernanza y reconciliación

- [x] Ledger audit y reconciliación por issue/tranche.
- [x] Manifiesto único de versión/capacidad.
- [x] Ruleset activo `24546459` con **25 checks** (incluye nativos, runtime,
      GeoParquet, inventarios y `Verify exact main push certification`).
- [x] Inventarios de superficies/comandos y mapa semántico.

## T1 — No perder trabajo del usuario

- [x] #144 apertura de proyecto fail-closed en dos fases.
- [x] #146 roundtrip por tipo con feature count/source name.
- [x] #148 codec `.catgis` v2 reversible (compatibilidad v1).
- [x] #149 snapshots aislados y restauración de trabajo sin guardar.
- [x] #150 SpatiaLite con CRS/SRID reales.
- [x] #151 FlatGeobuf/LAS sin EPSG:4326 inventado.
- [x] #153 AppContext autoritativo para cambios sin guardar.
- [x] #155 journal crash-recoverable de familias Shapefile.
- [x] #193/#195 provenance de CRS de archivo y roundtrip SHP/GeoJSON/KML/LAS.
- [x] #260 autosave vectorial sin colisiones.
- [x] #176 preview LAS tipado, muestreo estratificado, badge y guards.
- [x] #175 streaming FlatGeobuf por ventanas, cancelación y fail-closed.

## T2 — Ciencia básica fiable

- [x] #256 calculadora raster float/NoData/CRS (no ARGB como único resultado).
- [x] #257 métrica de cuenca y signos cut/fill.
- [x] #258 IDW alineado al norte con centros exactos.
- [x] #259 favoritos CRS argentinos autoritativos.
- [x] #262 ASCII CATMAP falla cerrado si dx≠dy; estadísticas finitas.
- [x] #162 NetCDF CF `grid_mapping`/packaging sin WGS84 asumido.
- [x] #139 índices espectrales con calibración explícita y GeoTIFF analítico.
- [x] #163 normalización explícita separada del default WGS84.

## T3 — Datos, proveedores y formatos

- [x] #128 georreferencia embebida con veto de sidecar.
- [x] #161 publicación atómica TIFF+sidecar (DEM/suelo/clima).
- [x] #162 climate CF sin relabel.
- [x] #163 CRS explícito.
- [x] #139/#176 calibración espectral y LiDAR preview.
- [x] #175 FlatGeobuf acotado y fail-closed.
- [x] #112 M1 metadatos reales + M2 inspección off-EDT **certificados**.
- [ ] #112 M3 ingesta real de filas — **congelada por decisión del owner**
      (feature freeze A0.7; manifiesto `INSPECTION_ONLY`).
- [ ] #175 lector indexado >100k — **congelado por decisión del owner**
      (manifiesto `BETA_LIMITED`).
- [ ] LAZ / LAS formatos 4–10 — `REQUIRES_DEPENDENCY` (PDAL/decoder dedicado).

## T4 — Integración y seguridad

- [x] #156 PostGIS Replace Table transaccional con CI PostGIS real.
- [x] #261 vault v2 + DPAPI/Keychain/Secret Service con evidencia nativa
      multi-OS (`tests=1 skipped=0`).
- [x] #43 WCS payload validado antes de publicar.
- [x] #108 WCS 2.x `DescribeCoverage` con ejes nativos, caché y fallback.
- [x] #174 ruleset con certificación nativa y attestation.
- [x] DC-5/CATSERVER: decisión owner → **UNSUPPORTED**, retirado del alcance
      beta (sin fuente autoritativa no se inventan datos).
- [x] OGC/WCS gates cerrados con evidencia CI multi-OS.

## T5 — A0.7/F3 y evidencia

- [x] Gates de implementación CATMAP: #95/#96/#98/#99/#102 con tests
      (save/exports atómicos, multipágina, Atlas, undo por página).
- [x] WCS/XYZ/GDAL: #50/#64/#67/#70/#86/#87/#91/#94.
- [x] UI snapshot y runtime smoke multi-OS (última: `44545814882` /
      `44545819822`).
- [x] Verificación funcional local: `--version`, `--self-test` 13/13,
      `e2eBetaHarness` E2E_OK 13/13.
- [x] Copy batch de familia Shapefile completa (#114), transacción con
      rollback/cancelación y UI migrada.
- [x] Topología asíncrona con cancelación y **cobertura contra AOI** (#113).
- [ ] **A0.7/F3 físico** — `NOT_CERTIFIED`: falta matriz interactiva humana,
      HiDPI/foco reales, gestos del owner, impresión física, FPS/memoria y
      multi-sesión. No se certifica desde CI headless.
- [ ] #132 anomalía Windows original de scripting — no reproducida en 40+
      rondas; no se declara erradicada (evidencia nativa de lifecycle y stdin
      acotado certificada).
- [ ] #114 rollback ante crash y evidencia de performance — incrementos
      separados.
- [x] #113 propuestas de reparación no destructivas exportables (CSV/JSON\n      atómicos, UI, tests) — sin aplicar cambios automáticamente.\n- [ ] #113 validación física/GUI de la aplicación manual — gate externo.
- [ ] #82 medición GUI de pan/drag en Windows real — gate físico.
- [ ] #74 ejecución nativa de WhiteboxTools — depende de binario externo.
- [ ] #73 residuos de corte eléctrico/filesystems de red — gate físico.

## Issues de seguimiento

- **#263** epic: pendiente de la certificación física A0.7/F3 y de decisión de
  programa sobre residuales.
- **#143** ledger vivo.
- **#109** spec de producto umbrella.

## Guard de consistencia\n\n- [x] `KiroAuditSpecConsistencyContractTest` (3) protege contra estados\n      obsoletos, pérdida de la decisión DC-5 y specs vacías/no UTF-8.\n- [x] `catgis-catmap-beta-1` y `catserver-sql-policies-t2-t4` sin pendientes\n      contradictorios.\n\n## Historial de cierre por fases

- T0/T1/T2 cerrados y certificados en `main`.
- T3/T4/T5 con gates de implementación cerrados; capacidades nuevas congeladas
  por decisión owner.
- Suites y attestation registradas en el manifiesto y la reconciliación; este
  archivo ya no contiene estados “en curso”.
