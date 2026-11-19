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
- T3 avance 4 (2026-11-17): **#139** corregido en su núcleo (PR en curso):
  API `computeAnalysis` con calibración obligatoria (scale/offset/NoData),
  dominio de reflectancia validado, propagación de NoData y grilla/CRS
  compartidos; el preview 8-bit del diálogo queda marcado no profesional.
  Tests `SpectralCalibrationContractTest` (6); política en
  `docs/quality/SPECTRAL_CALIBRATION_POLICY_20261117.md`.
- T3 avance 5 (2026-11-17): **#163** corregido (PR en curso):
  `normalizeExplicit` conserva vacío y `normalizeCode` mantiene el default
  UI EPSG:4326 delegando en aquel; migradas rutas sensibles (transforms,
  BooleanRisk, vector CRS, FlatGeobuf, declaredCode raster). Tests
  `CrsNormalizationSplitContractTest` (4); política en
  `docs/quality/CRS_NORMALIZATION_POLICY_20261117.md`.
- T4 avance 1 (2026-11-17): **#261** corregido (PR en curso):
  `PostgisCredentialVault` v2 con secreto de instalación aleatorio 32 bytes
  (`~/.catgis/postgis.key`, permisos POSIX), AES-256-GCM por registro;
  migración legacy v1/XOR fail-closed; claves Preferences largas hasheadas.
  Tests `PostgisCredentialsVaultContractTest` (7); política en
  `docs/quality/POSTGIS_CREDENTIALS_SECURITY_20261117.md`. DPAPI/Keychain
  completo queda como mejora futura.
- T4 avance 2 (2026-11-17): **#156** implementado (PR en curso): REPLACE TABLE
  escribe a `<tabla>_catgis_stage_<hex>`, verifica filas y hace DROP+RENAME en
  una transacción JDBC con ROLLBACK y limpieza de staging; identificadores
  validados/citados; modo oculto en UI. Tests de plan (4) y test real en
  `PostgisRealIntegrationTest` (job PostGIS CI). #261 se registra como
  **PARCIAL/MITIGADO**: vault v2 con secreto local, pero DPAPI/Keychain/Secret
  Service completo sigue pendiente.
- T3 avance 6 (2026-11-17): **#175** parcial/mejorado (PR en curso):
  `FlatGeobufFrameReader` transmite frames size-prefixed en ventanas acotadas
  (sin buffer directo del payload completo), completa lecturas cortas, falla
  cerrado ante truncamiento/tamaño inválido/conteo incongruente y cancela por
  interrupción; el loader mantiene límites de 100k features en RAM. Tests
  `FlatGeobufFrameReaderContractTest` (7). **Sigue pendiente** el lector
  indexado para >100k features: #175 no se considera cerrado.
- T3 avance 7 (2026-11-17): **#176** núcleo implementado (PR en curso):
  `LasPreviewPolicy` (completo <=50k, muestra estratificada >50k),
  `LasReader.readSampledPoints`, estado tipado persistente en `Layer`
  (PARTIAL_PREVIEW/TOTAL/LOADED), envelope de muestra y guarda de exportación.
  Tests `LasPreviewPolicyContractTest` (4). **Pendiente** etiquetas UI extendidas
  y bloqueo de todos los flujos profesionales: #176 sigue abierto.
- T4 avance 3 (2026-11-17): **#43** aceptación verificada (PR docs en curso):
  WCS valida firmas TIFF/BigTIFF, ImageIO, dimensiones, CRS/extent, strips y
  muestras de píxel antes del rename atómico; staging/presupuesto/fallback y EDT
  cubiertos por 34+ tests existentes. Evidencia en
  `docs/quality/WCS_PAYLOAD_INTEGRITY_EVIDENCE_20261117.md`. **#108 (ejes WCS2/
  DescribeCoverage) sigue OPEN**; A0.7/F3 NOT_CERTIFIED.
- T4 avance 4 (2026-11-17): **#108** núcleo implementado (PR en curso):
  `WcsDescribeCoverageAxes` parsea DescribeCoverage seguro (<=4 MiB), exige
  srsName y exactamente 2 ejes, mapea x/y por significado (lon/lat/e/n/...) y
  falla cerrado ante ambigüedad; genera SUBSET/SCALESIZE con etiquetas nativas.
  Tests `WcsDescribeCoverageAxesContractTest` (5). **#108 sigue OPEN** hasta
  integrar fetch DescribeCoverage + flujo de descarga/UI. A0.7/F3 NOT_CERTIFIED.
- T4 cierre de #108 (2026-11-17): `DescribeCoverage` integrado en la descarga
  real (`downloadCoverageToFileNegotiated` + WcsDialog), con caché LRU, URL de
  ejes nativos y fallback v2->v1 sin adivinar x/y; tests
  `WcsNegotiatedDownloadContractTest` (3) con URL capturada. A0.7/F3 siguen
  NOT_CERTIFIED.
- T3 avance 8 (2026-11-17): #176 UI/analítica parcial: badge persistente
  `PREVIEW - N de TOTAL` en tabla de atributos (título/subtítulo/estado) y
  bloqueo de validación topológica sobre preview; exportación ya bloqueada.
  Tests `LasPreviewPolicyContractTest` (5). Siguen pendientes calculadora de
  campos/geoprocesamiento/clasificación y simbología: #176 ABIERTO.
- T3 cierre de #176 (2026-11-17): badge `[PREVIEW]` en lista de capas y estado
  `PREVIEW - N de TOTAL` en tabla; guards centrales bloquean exportacion,
  topologia, calculadora de campos, geoprocesamiento y clasificacion ML sobre
  previews. Tests de politica (5). Dataset completo >50k y LAZ siguen fuera de
  alcance declarado.
- Gobernanza (2026-11-17): cierre administrativo de issues con evidencia
  (PR+test+main certificado): #128/#144/#146/#148/#149/#155/#156/#161/#162/
  #163/#176/#256/#257/#258/#259/#260/#262 (+#43/#108 previos). Quedan abiertos
  #112 (M3 ingesta), #175 (indexado), #139 (UI), #261 (OS vault), #150/#151/
  #153/#193/#195, #164 y gates CATMAP/A0.7. Ver reconciliacion seccion 8.
- Gobernanza (2026-11-17, cont.): cierre verificado adicional de #174
  (ruleset activo exige 25 checks incl. Verify exact main push certification),
  #193 (LocalVectorSourceCrsProvenance + LasSourceCrsProjectRoundTripContractTest)
  y #164 (UserVisibleTextEncodingContractTest sin sentinelas). #195 sigue
  parcial (SHP cubierto; GeoJSON/KML pendientes).
- T3 cierre de #139 (2026-11-17): `SpectralCalibrationInput` exige declaracion
  explicita scale/offset/NoData; `SpectralIndexDialog` calcula via
  computeAnalysis calibrado, conserva AnalysisRaster float en LocalRasterData y
  permite exportar GeoTIFF analitico; entrada ambigua/raw falla cerrado sin capa.
  Tests `SpectralCalibrationInputContractTest` (4). 1689 tests ok.
- T1 cierre de #150/#151/#153 (2026-11-17): SpatiaLite usa CRS del schema
  real (`requireSourceCrs`, SRID real), FlatGeobuf exige CRS verificado del
  header y LAS exige CRS declarado (sin default 4326); `hasUnsavedProjectChanges`
  lee el proyecto autoritativo de AppContext. Tests
  `SpatiaLiteSourceCrsTruthContractTest`, `FlatGeobufCrsFailClosedContractTest`,
  `LasSourceCrsProjectRoundTripContractTest`, `ProjectAuthorityContractTest`.
  1690 tests ok.
- T1/T3 cierres (2026-11-17): #195 provenance source-vs-display para SHP/
  GeoJSON/KML con fixtures reales, roundtrip de proyecto, bytes originales
  intactos, CRS-null y path-change invalidation; #167 contrato WCS shared
  deadline con headroom determinista (1500 ms vs stream 3 s, requests==1 sin
  fallback). Ambos verificados en main certificado (attestation 44384865579).
- T5 avance (2026-11-17): cierre verificado de gates CATMAP A0.x con tests
  certificados: #95 (save atómico/backup), #99 (exports atómicos + SVG vectorial),
  #98 (multipágina/v2), #96 (Atlas no muta/streaming), #102 (edición/undo por
  página). A0.7/F3 GUI física siguen NOT_CERTIFIED; no se sobrepromete.
- CI/QA (2026-11-17): estabilizado `ExternalToolTimeoutContractTest`
  veryLargeOutput... — ExternalToolService espera EOF del drain con latch
  (5 s + cierre forzado) y falla cerrado si no drena, en lugar de snapshot tras
  join(1 s) (causa del flake MacIntel21). Test repite 5 veces y verifica marcador
  de truncado y ausencia de "could not be fully drained".
- T4/T5 avance (2026-11-17): cierre verificado de gates WCS (#86/#87/#91/#94),
  XYZ/GDAL (#50/#64/#67/#70) y SpatiaLite (#152) con tests certificados en main.
  Quedan abiertos #263, #261, #175, #143, #132, #114, #113, #112, #109, #82,
  #74 y #73; A0.7/F3 NOT_CERTIFIED.
- T5 evidencia (2026-11-17, main 21bd9a6): refresco real de `ui-snapshot.yml`
  (run 44414255394, 3/3 SUCCESS: Linux Xvfb/Windows/macOS ARM) y
  `f3-runtime-smoke.yml` (run 44414260382, 3/3 SUCCESS: app-image + launcher +
  E2E). Manifiesto/F3 doc actualizados; A0.7/F3 siguen NOT_CERTIFIED por falta
  de matriz interactiva humana/HiDPI/foco/impresión física.
- T4 avance (2026-11-17): #261 `OsKeyProtector` envuelve el secreto de
  instalacion con DPAPI (Windows, PowerShell stdin) o Secret Service (Linux
  secret-tool), formato de archivo versionado C G O S 1, migracion unica del
  key raw, fail-closed ante tamper/backend distinto/ausente; ejecucion via
  ExternalToolService.runBounded (sin ProcessBuilder nuevo). Tests
  OsKeyProtectorContractTest (5) + vault (7). 1695 tests ok. #261 SIGUE
  ABIERTO por Keychain macOS y matriz nativa de sesiones.
- T4 avance (2026-11-17): #132 `executeCode` ya no usa `deleteOnExit`; el
  script temporal se elimina en `finally` y se registra un warning si falla.
  Nuevo `ScriptTempCleanupContractTest` (sin fugas). #132 SIGUE ABIERTO por la
  anomalia Windows original y la matriz nativa de lifecycle.