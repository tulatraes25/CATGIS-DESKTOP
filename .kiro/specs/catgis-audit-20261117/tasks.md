# CATGIS — Remediación audit #263 — Tasks

Estado: ACTIVE. Base `main` `c0484a1` (2026-11-17).

- [ ] **T0 — Reconciliación y gobernanza**
  - Spec Kiro, manifiesto de capacidad, conciliación de docs/ledger, ruleset
    reforzado, snapshot de runs.
- [ ] **T1 — No perder trabajo**
  - #144 #146 #155 #260 #148 #149 #153 #151 #150 #195 #193 #175 #176.
- [ ] **T2 — Ciencia básica fiable**
  - #258 IDW, #262 ASCII/stats, #257 métrica/signos, #256 raster calculator,
    #259 CRS favoritos, #162 NetCDF.
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
