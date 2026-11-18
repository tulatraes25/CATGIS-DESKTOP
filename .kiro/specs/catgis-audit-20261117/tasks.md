# CATGIS — Remediación audit #263 — Tasks

Estado: ACTIVE. Base `main` `c0484a1` (2026-11-17).

- [ ] **T0 — Reconciliación y gobernanza**
  - Spec Kiro, manifiesto de capacidad, conciliación de docs/ledger, ruleset
    reforzado, snapshot de runs.
- [ ] **T1 — No perder trabajo** (en curso)
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