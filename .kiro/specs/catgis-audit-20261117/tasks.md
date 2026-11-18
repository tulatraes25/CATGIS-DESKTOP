# CATGIS — Remediación audit #263 — Tasks

Estado: ACTIVE. Base `main` `c0484a1` (2026-11-17).

- [ ] **T0 — Reconciliación y gobernanza**
  - Spec Kiro, manifiesto de capacidad, conciliación de docs/ledger, ruleset
    reforzado, snapshot de runs.
- [x] **T1 — No perder trabajo** (cerrado)
  - #144 #146 #155 #260 #148 #149 #153 #151 #150 #195 #193 #175 #176.
- [x] **T2 — Ciencia básica fiable (PRs #266 + este)**
  - #258 IDW, #262 ASCII/stats, #257 métrica/signos, #256 raster calculator,
    #259 CRS favoritos (todos corregidos con oráculos). #162 NetCDF y #139/#163
    se reasignan a T3 (datos/proveedores).
- [ ] **T3 — Datos y proveedores**
  - #128 georreferencia embebida, #161 sidecar atómico, #139 calibración
    espectral, #112 GeoParquet real, FGB/LAS escalable.
- [ ] **T4 — Integración y seguridad**
  - #261 credential store, #156 PostGIS replace transaccional, OGC/WCS/WMTS,
    DC-5, #174 ruleset, web auth/proxy.
- [ ] **T5 — A0.7/F3 y release**
  - Gates de arquitectura + GUI interactiva + firma/notarización; sin cerrar
    A0.7 por snapshots/E2E.

## Progreso

- T0 completado y certificado en `main` `1a51ac0` (PR #265, ruleset 25 checks,
  manifiesto y reconciliación).
- T2 parcial (PR en curso): **#258** IDW con norte alineado y centros exactos;
  **#262** ASCII falla cerrada si dx!=dy y stats ignoran no finitos; **#257**
  escala métrica por CRS (`MetricScale`) y convención corte/relleno corregida en
  `VolumeCalculator`/`BasinMorphometry` con oráculos grados/metros. Pendientes
  T2: #256 raster calculator, #259 CRS favoritos, #162 NetCDF.

- T2 cierre (2026-11-17): #258/#262/#257 certificados en `main` `6cd043d`
  (PR #266); #256 (calculadora raster científica float/NoData + GeoTIFF y
  preview separado) y #259 (favoritos POSGAR 94 / Campo Inchauspe / POSGAR 2007
  validados contra la base EPSG de GeoTools) en el PR T2-2. Suite local **1602
  tests / 0 fallos / 15 skips**.

- T1 avance (2026-11-17): **#260** corregido (PR en curso): preflight de todas
  las salidas de capas vectoriales transitorias con reserva de nombre físico
  único (`a_b.shp`, `a_b_2.shp`, ...), sin sobreescribir archivos ajenos y
  reutilizando el archivo propio de la capa. Tests
  `VectorAutosaveCollisionContractTest` (4). Restan #144/#146/#148/#149/#155.
- T1 avance 2 (2026-11-17): **#144** corregido (PR en curso): apertura en dos
  fases, `stageProject` detached (raster/vector contra el proyecto candidato,
  remoto pendiente explícito) y commit atómico; un fallo obligatorio deja la
  sesión previa intacta y no hay éxito parcial. Tests
  `ProjectOpenAtomicCommitContractTest` (2: raster corrupto conserva sesión,
  proyecto válido commitea raster adjunto). Restan #146/#148/#149/#155.
- T1 avance 3 (2026-11-17): **#148** corregido (PR en curso): formato `.catgis`
  v2 con marcador `PROJECT_FORMAT|2` y codec reversible (`%`, `|`, CR/LF
  escapados; Unicode y backslashes verbatim) en guardado/carga canonicos,
  `ProjectOpenService.inspect`, grupos y codec legacy; archivos v1 siguen
  abriendo. Tests `ProjectCodecRoundTripContractTest` (4). Restan
  #146/#149/#155.
- T1 avance 4 (2026-11-17): **#155** corregido (PR en curso): publicacion de
  familias Shapefile con journal (`.base.catgis-publish.journal`), commit point
  por borrado del journal, recuperacion al siguiente guardado, backups huerfanos
  limpiados, lock de identidad con deteccion de stale (15 min) y seam de crash
  inyectado. Tests `VectorFamilyJournalContractTest` (7). Solo resta #146/#149.
- T1 avance 5 (2026-11-17): **#146** corregido (PR en curso): featureCount y
  sourceName se persisten/restauran (solo se escribe FEATURE_COUNT si >0 para
  mantener la forma legacy), el payload keyed usa el codec v2 (pipes preservados)
  y el decodificador legacy ya no interpreta sufijos KEY=value como campos
  posicionales. Tests `ProjectLayerRoundtripByTypeContractTest` (GPX, GeoPackage,
  SpatiaLite, Vector, PostGIS y WFS con tipo/path/CRS/count/source) y ajustes v2
  en `SpecializedProjectLayerPayloadContractTest`. Solo resta #149.
- T1 cierre (2026-11-17): **#149** corregido (PR en curso): `ProjectRecoveryJournal`
  con snapshots aislados/versionados en `~/.catgis/recovery` (override
  `-Dcatgis.recovery.root`), sidecars Shapefile de capas en memoria, manifiesto
  SHA-256, retención 5, restauración no destructiva vía flujo normal de apertura,
  rechazo de snapshots corruptos, autosnapshot EDT cada 60 s (headless no-op) y
  prompt de recuperación al inicio; al guardar se descartan los snapshots del
  proyecto. Tests `ProjectRecoveryJournalContractTest` (5). T1 completo:
  #144/#146/#148/#149/#155/#260.
- T3 avance 1 (2026-11-17): **#161** corregido (PR en curso):
  `RasterBundlePublisher` publica TIFF+sidecar atomicos (staged, backup de ambos,
  verificacion de imagen/sidecar, restauracion ante fallo, deletion de sidecar
  obsoleto si no hay georreferencia) y se usa en OpenTopography, SoilGrids y
  Climate (WorldClim/Open-Meteo). Tests `RasterBundlePublisherContractTest` (6).
- T3 avance 2 (2026-11-17): **#128** corregido (PR en curso): precedencia
  GeoKeys embebidas > sidecar, veto de conflicto con warning de provenance,
  sidecar sólo si no hay GeoKeys y declara CRS decodable, y
  `declaredCode()` evita inventar EPSG:4326 en decisiones de georreferencia
  (incluido el fallback por CRS de capa). Tests
  `GeoreferencePrecedenceContractTest` (4) con oracle de pixeles; decisión en
  `docs/quality/GEOREFERENCE_PRECEDENCE_20261117.md`.
- T3 avance 3 (2026-11-17): **#162** corregido (PR en curso): `NetCdfCfSupport`
  resuelve CRS por `grid_mapping` (`epsg_code`/`spatial_ref`/
  `latitude_longitude`/grados) y falla cerrado en grids rotados sin CRS; el
  fallback UCAR ya no asume EPSG:4326, aplica packaging CF
  (scale/offset/fill/valid) y convierte unidades (radianes, 0..360).
  `NetCdfCfSupportContractTest` (9); politica en
  `docs/quality/NETCDF_CF_POLICY_20261117.md`.