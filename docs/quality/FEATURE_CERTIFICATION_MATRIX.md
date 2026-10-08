# CATGIS Feature Certification Matrix

Status: F1.2_IN_PROGRESS
Program spec: `.kiro/specs/catgis-product-certification/`
Certified source baseline entering F1.2: `d0c13e2c16a25e0aa2c9f0b4b9b543d7c48ff36a`

This document is the feature-level certification ledger. Historical audit matrices are inputs only and never current certification evidence.

## Current F1.2 evidence

The first F1.2 tranche converts visible command discovery into an executable GitHub gate:

- command/toolbar source surfaces tracked: **14/14** from `SOURCE_FEATURE_SURFACE_INVENTORY.tsv`;
- static visible command occurrences: **392**;
- unique source/label pairs: **380**;
- unique static labels: **315**;
- executable guard: `VisibleCommandInventoryCertificationTest`;
- CI evidence artifact: `certification-visible-command-inventory/visible-command-inventory.tsv`.

This is inventory evidence, not a claim that 313 independent product features are certified.

### F1.2 Tranche B1 — UI command-carrier coverage

Filename heuristics do not identify every Java source that can expose a UI command/control. An executable source scan therefore supplements the original surface inventory.

- current production UI command carriers discovered: **106**;
- already covered by filename-derived source-surface categories: **89**;
- supplemental carriers requiring explicit classification: **17**;
- executable guard: `UiCommandCarrierCoverageCertificationTest`;
- evidence manifest: `docs/quality/UI_COMMAND_CARRIER_INVENTORY.tsv`.

B1 closes carrier-classification drift before semantic consolidation.

### F1.2 Tranche B2a — static semantic aliases

The static inventory contains visible-label variants that represent the same product action. B2a makes those aliases explicit instead of treating punctuation/accent differences as separate features.

- normalization-collision groups currently declared: **13**;
- checked-in semantic alias map: `docs/quality/STATIC_COMMAND_ALIAS_MAP.tsv`;
- executable guard: `StaticCommandAliasCertificationTest`;
- every detected static normalization collision must be declared;
- every alias group has one stable `SEM-*` identifier and one canonical current label;
- semantic IDs cannot be reused across unrelated collision groups.

B2a does not claim all 315 static labels are semantically resolved. B2b inventories dynamic/computed commands and B2c performs the complete feature/tier mapping.

### F1.2 Tranche B2b — dynamic/computed commands

B2b treats the menu expression as evidence, not as the feature identity. Runtime labels that vary by selection, state, registry entry or user data must therefore be classified explicitly.

- dynamic/computed menu source-expression rows: **48**;
- concrete source occurrences locked: **55**;
- action-bearing semantic IDs referenced: **25**;
- module registry actions locked independently: **38**;
- dynamic manifest: `docs/quality/DYNAMIC_COMMAND_INVENTORY.tsv`;
- module action manifest: `docs/quality/MODULE_ACTION_INVENTORY.tsv`;
- executable guard: `DynamicCommandInventoryCertificationTest`;
- non-feature expressions are explicitly classified as `DISPLAY_ONLY`, `INDIRECTION_FACTORY`, `MENU_CONTAINER` or `ALIAS_PASSTHROUGH` rather than inflating the feature count;
- action-bearing expressions are classified as `STATE_VARIANT`, `PARAMETERIZED_ACTION`, `COMPUTED_ACTION` or `REGISTRY_ACTION` and reference stable `SEM-*` IDs;
- carrier discovery now recognizes `JCheckBoxMenuItem`, `JRadioButtonMenuItem` and `JMenu`;
- the static scanner now includes literal checkbox/radio menu commands, exposing **Mapa de calor (heatmap)** and **Agrupar puntos (clustering)** that the original Tranche A scanner omitted.

B2b still does not assign tiers or claim behavioral certification.

### F1.2 Tranche B2c — complete semantic feature matrix

B2c materializes one cross-checked semantic catalog across static commands, dynamic actions, module actions and non-command source surfaces.

- semantic feature rows: **476 unique IDs**;
- static command mappings: **380 rows -> 302 semantic IDs**;
- module action mappings: **38**;
- ACTION/DIALOG/LOADER/EXPORT surface mappings: **132**;
- tier distribution: **CORE 291 / BETA 123 / EXPERIMENTAL 15 / EXTERNAL_DEPENDENCY 47**;
- all rows remain **RUNTIME_PENDING** until architecture and Windows runtime gates permit promotion;
- executable guard: `SemanticFeatureMatrixCertificationTest`;
- checked-in contracts:
  - `STATIC_COMMAND_SEMANTIC_MAP.tsv`;
  - `MODULE_ACTION_SEMANTIC_MAP.tsv`;
  - `SURFACE_SEMANTIC_MAP.tsv`;
  - `SEMANTIC_FEATURE_MATRIX.tsv`.

The guard rejects uncovered static commands, dynamic semantic IDs without catalog rows, module actions without mapping, uncovered ACTION/DIALOG/LOADER/EXPORT surfaces, duplicate semantic IDs, invalid tiers and any premature runtime-certified status.

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

F1.2 must populate rows for every current user-facing capability in:

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

## F1.2 closure conditions

- Every visible static command maps to exactly one stable semantic feature ID.
- Dynamic/computed menu labels are explicitly inventoried.
- Dialog-only, loader/export-only and dependency-gated capabilities are linked to a semantic feature row.
- Aliases from menu/toolbar/context surfaces converge on the same feature ID where they represent the same behavior.
- Every row has a declared tier: CORE, BETA, EXPERIMENTAL or EXTERNAL_DEPENDENCY.
- No row receives a certified runtime state before `ARCHITECTURE_STATUS=CLOSED_CERTIFIED`.
- Exact-SHA automated evidence is attached before a row can leave TEST_GAP/RUNTIME_PENDING.
