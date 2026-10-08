# CATGIS Feature Certification Matrix

Status: F2.2_IN_PROGRESS
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

### F1.2 Tranche B2d — supplemental carrier commands

A follow-up audit proved that the original static/dynamic scanners did not expand literal command call-sites hidden behind UI helper factories in the 17 supplemental command carriers.

- supplemental carrier command occurrences: **201**;
- unique source/kind/label rows: **191**;
- unique labels: **147**;
- executable inventory: `SUPPLEMENTAL_VISIBLE_COMMAND_INVENTORY.tsv`;
- executable guard: `SupplementalVisibleCommandInventoryCertificationTest`;
- the guard rejects active `addMenuItem(... -> {})` empty handlers;
- CATMAP commands for selective visible-layer, symbology and label synchronization are disabled until the CATMAP protocol exposes real behavior.

B2d1 locks this newly discovered command universe.

### F1.2 Tranche B2d2 — supplemental semantic mapping

B2d2 classifies every supplemental command row by behavior rather than label alone.

- supplemental semantic rows covered: **191/191**;
- aliases to existing semantic features: **98**;
- action rows: **74**, converging on **65 new semantic IDs**;
- subordinate controls: **14**;
- display-only rows: **2**;
- disabled/unsupported CATMAP sync rows: **3**;
- executable guard: `SupplementalCommandSemanticCertificationTest`;
- checked-in map: `SUPPLEMENTAL_COMMAND_SEMANTIC_MAP.tsv`;
- semantic feature matrix candidate: **541 unique IDs**;
- candidate tier distribution: **CORE 293 / BETA 186 / EXPERIMENTAL 15 / EXTERNAL_DEPENDENCY 47**;
- all new semantic action rows remain **RUNTIME_PENDING** before A0.7.

Controls, display-only entries and disabled unsupported commands are explicitly `NON_FEATURE`; they do not inflate the feature count.

### F1.3 — visible dead/placeholder reconciliation

F1.3 converts placeholder/dead-action review into an executable gate.

- visible command carriers are scanned for empty lambda handlers;
- active visible commands with empty behavior are forbidden;
- deliberately unsupported visible commands must be explicitly classified as `DISABLED_UNSUPPORTED` and remain visibly gated;
- current unsupported baseline is exactly **3** CATMAP synchronization entries: visible layers, symbology and labels;
- executable guard: `VisiblePlaceholderActionCertificationTest`.

Contextual disablement (for example a Paste command when there is nothing to paste) is not classified as dead behavior.

F1.3 CLOSED_CERTIFIED evidence: PR #21 head `09b678bdd765b335c412b8bb490d8505b21c0846`, CATGIS CI `37856236790` SUCCESS, Certification `37856236813` SUCCESS; merge SHA `4be1e284c789cff0b16e200e42cb5f303843cd41`; post-main CATGIS CI `37858849967` SUCCESS and Certification `37858849921` SUCCESS.

### F2.1 — core project / CRS / save-load contracts

F2.1 starts behavioral certification with the minimum project persistence boundary.

- default project CRS must be stable and CRS assignments normalized;
- a minimal project must round-trip CRS and core metadata;
- invalid or truncated project input must not replace live project state;
- a failed save must not repoint the current project file or rename the project;
- executable contract: `CoreProjectPersistenceContractTest`;
- candidate branch baseline: `4be1e284c789cff0b16e200e42cb5f303843cd41`.

The initial F2.1 audit found and repairs two negative-path defects: header-only project files were accepted as empty projects, and failed save attempts could mutate project identity before the write succeeded.

F2.1 CLOSED_CERTIFIED evidence: PR #22 head `11a7753a9b55ab24caebbba00de1177bd092a7fb`; PR CATGIS CI `37859335916` SUCCESS and Certification `37859335855` SUCCESS; merge `d5f6db956bf0086d6e63db840ea0993a94b1b93d`; post-main CATGIS CI `37859737265` SUCCESS and Certification `37859737268` SUCCESS.

### F2.2 Tranche A — vector edit history contract

The current repository already contains real or deterministic coverage for several vector-format surfaces that historical audits marked as gaps. F2.2 therefore starts with a confirmed current gap rather than duplicating those tests.

- Shapefile export/reload roundtrip already exists in `ReleaseVectorInteropTest`;
- CSV and DXF have deterministic inline-fixture suites;
- FlatGeobuf, GeoPackage and SpatiaLite have real-file suites with explicit external GDAL/compatibility gating where required;
- `SnapManager` has deterministic context/unit tests;
- `UndoRedoManager` exposes an explicit `UndoRedoContext` seam but previously had no direct behavioral contract;
- new executable contract: `UndoRedoManagerContractTest`;
- required behavior: snapshot A -> edit B -> undo restores A -> redo restores B, selection is restored, new edits invalidate redo, and history remains bounded to 20 snapshots;
- exact Tranche A baseline: `d5f6db956bf0086d6e63db840ea0993a94b1b93d`.

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
