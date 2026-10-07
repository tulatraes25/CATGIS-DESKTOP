# CATGIS Feature Certification Matrix

Status: INVENTORY_PENDING
Program spec: `.kiro/specs/catgis-product-certification/`
Baseline at creation: `bb5d854d17510a1c2518993a0c2120461f9d92a3`

This document becomes the single feature-level certification ledger. Historical audit matrices are inputs only.

## Status vocabulary

- INVENTORY_PENDING
- TEST_GAP
- AUTOMATED_PASS
- RUNTIME_PENDING
- SUPPORTED_CERTIFIED
- BETA_CERTIFIED
- EXPERIMENTAL_GATED
- EXTERNAL_DEPENDENCY_CERTIFIED
- DISABLED_NOT_SUPPORTED
- FAIL
- BLOCKED

## Required row schema

| ID | Domain | Surface | Feature | Entry point | Tier | Preconditions | Automated evidence | Windows runtime | Negative path | Performance | Status | Residual risk |
|---|---|---|---|---|---|---|---|---|---|---|---|---|

## Inventory domains

The F1 gate must populate rows for every current user-facing action in:

- Startup / project / CRS
- Navigation / map canvas
- Layer management
- Vector import/create/export
- Symbology / labels / query / attributes
- Vector editing / snapping / undo-redo
- Raster / GeoTIFF / DEM
- Terrain / relief / hydrology
- Vector geoprocessing / topology / risk
- Online XYZ / WMS / WFS / WCS / STAC / downloads
- PostGIS / pgRouting
- CAD / DXF / DWG
- CSV / KML / GPX / FlatGeobuf / SpatiaLite / LAS
- CATMAP / layout / templates / export
- Batch / scripting / plugins
- Help / preferences / diagnostics
- Installer / first-run / dependency detection

No feature receives a certified status until exact-SHA evidence is linked.
