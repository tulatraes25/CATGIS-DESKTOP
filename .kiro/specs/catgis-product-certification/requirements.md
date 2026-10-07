# CATGIS Product Certification — Requirements

Status: ACTIVE
Owner: CATGIS
Baseline: `bb5d854d17510a1c2518993a0c2120461f9d92a3`
Program: FUNCTIONAL_E2E -> RELIABILITY -> ARCHITECTURE -> UX -> RELEASE

## Goal

Convert CATGIS Desktop from a feature-rich beta into a product whose visible functions are traceably usable end to end on Windows, whose failures are explicit and recoverable, whose architecture is maintainable/testable, and whose UX is polished only after functionality and architecture are certified.

Historical audit documents are evidence inputs, not current truth. The current repository, exact SHAs, CI, fixtures and Windows runtime evidence are authoritative.

## Non-goals

- Do not redesign the whole product before functional certification.
- Do not hide failing behavior behind skipped tests.
- Do not call a visible feature "working" because its dialog opens.
- Do not require every optional external dependency on every workstation; certify dependency-gated features in declared environments.
- Do not rewrite Java/Swing/GeoTools into another stack merely for fashion.

## REQ-PC-1 — Complete user-facing inventory

WHEN a function is reachable from a menu, toolbar, context action, dialog, shortcut, import/export flow, CATMAP, online catalog or project workflow, THE SYSTEM SHALL have exactly one traceable feature ID in the certification matrix.

Acceptance:
- AC-PC-1.1: Every visible action maps to a feature ID, source entry point and support class.
- AC-PC-1.2: Every row declares CORE, BETA, EXPERIMENTAL or EXTERNAL_DEPENDENCY.
- AC-PC-1.3: No row remains UNKNOWN at program closure.
- AC-PC-1.4: Dead/placeholder UI is either implemented to its declared minimum contract or removed/disabled from normal UI.

## REQ-PC-2 — Definition of Done per function

WHEN a feature is declared usable, THE SYSTEM SHALL have evidence for its complete minimum workflow.

Acceptance:
- invocation/preconditions are valid and understandable;
- success path produces the intended result;
- failure/cancel path is safe and understandable;
- state/persistence is verified where applicable;
- no silent data loss or silent failure;
- no blocking network/disk/external-tool work on the Swing EDT beyond explicitly bounded UI work;
- automated tests cover deterministic core behavior;
- Windows runtime evidence covers user interaction when GUI/native behavior matters.

## REQ-PC-3 — End-to-end professional workflow

WHEN CATGIS is certified, THE SYSTEM SHALL pass a canonical E2E workflow on an exact tested SHA:

1. launch on Windows;
2. create/open project and select CRS;
3. load representative vector and raster data;
4. navigate/zoom/pan/identify;
5. inspect attributes and styling;
6. edit supported vector geometry/attributes and undo/redo;
7. execute representative vector/raster/topography analysis;
8. use an online layer where internet is available;
9. save project;
10. close and reopen with state preserved;
11. compose CATMAP;
12. export at least PNG and PDF;
13. close cleanly without data loss.

## REQ-PC-4 — Format and integration certification

FOR EACH supported import/export/storage/integration surface, THE SYSTEM SHALL have a deterministic contract test and, where an external runtime is required, at least one certified real-environment runtime test.

Priority surfaces include:
Shapefile, GeoPackage, GeoTIFF/DEM, FlatGeobuf, SpatiaLite, CSV, DXF, KML/GPX export, PostGIS, WMS, WFS, online XYZ, CATGIS project save/load and CATMAP export.

Experimental/optional surfaces (DWG, LAS/LiDAR, WCS, STAC, pgRouting, scripting, plugins and other dependency-gated paths) must either pass their declared minimum contract in a certified environment or remain visibly gated/labeled and absent from a "fully supported" claim.

## REQ-PC-5 — Data integrity and recovery

IF an operation fails, is cancelled, receives corrupt input or loses an external dependency, THE SYSTEM SHALL preserve recoverable application/project state and present an actionable error.

Acceptance includes:
- project save/load roundtrip and corrupt/truncated project behavior;
- edit undo/redo and failed-save behavior;
- loader validation for corrupt/unsupported files;
- temporary/atomic output semantics where data loss is possible;
- no swallowed exceptions in critical persistence paths.

## REQ-PC-6 — Performance and responsiveness

WHILE the user navigates or invokes long-running GIS work, THE SYSTEM SHALL remain responsive or clearly expose progress/cancellation.

Acceptance includes:
- P0 pan-lag spec closed before product certification;
- no synchronous network fetch on EDT;
- representative large vector/raster/project benchmarks recorded;
- no repeated UI freezes from avoidable rendering/I/O;
- long-running external operations have progress/error/termination behavior;
- memory/resource release is verified across repeated open/close cycles.

## REQ-PC-7 — Architectural quality

WHEN the functional baseline is stable, THE SYSTEM SHALL enforce maintainable boundaries without changing user-visible semantics unnecessarily.

Acceptance:
- UI event handlers delegate business/GIS work to testable services;
- rendering, persistence, external processes, network and project state have explicit boundaries;
- no new monolithic responsibility growth in known hotspot classes;
- critical dependencies are injectable or replaceable by deterministic test seams;
- architecture rules are executable where practical (tests/static checks), not prose only;
- changes are incremental, behavior-preserving and backed by characterization tests.

## REQ-PC-8 — External dependency truthfulness

IF a feature requires GDAL/OSGeo4W, WhiteboxTools, ODA, PostGIS, internet, Python or another optional component, THE SYSTEM SHALL detect and communicate the dependency before destructive/long work.

A missing dependency must never look like a successful operation.

## REQ-PC-9 — UX is the final product phase

AFTER functional, reliability and architectural blocking gates are certified, THE SYSTEM SHALL undergo a dedicated UX phase.

UX acceptance includes:
- coherent information architecture and progressive disclosure;
- consistent terminology, icons, shortcuts and affordances;
- clear empty/loading/success/error states;
- useful dependency/experimental labels;
- keyboard navigation and focus behavior for core workflows;
- HiDPI/readability checks;
- consistent dialog sizing/layout/action order;
- reduced cognitive load without hiding necessary GIS capability;
- no UX change may regress certified behavior.

## REQ-PC-10 — Installation and clean-machine usability

WHEN a release candidate is produced, THE SYSTEM SHALL install/start on certified Windows environments without requiring source-tree knowledge.

Acceptance:
- clean Windows installation smoke test;
- bundled/required Java runtime contract verified;
- OSGeo4W/optional dependency detection is truthful;
- first launch, CRS selection, project creation and core layer load work;
- uninstall/upgrade behavior does not delete user projects/config unexpectedly.

## REQ-PC-11 — Evidence and exact-SHA traceability

Every certification result SHALL name:
- exact Git SHA;
- test type;
- environment/dependencies;
- result PASS/FAIL/BLOCKED/NOT_APPLICABLE;
- evidence location;
- residual risk.

No feature is certified solely from an old audit report.

## REQ-PC-12 — Release claim discipline

At closure, each feature SHALL be one of:
- SUPPORTED_CERTIFIED
- BETA_CERTIFIED
- EXPERIMENTAL_GATED
- EXTERNAL_DEPENDENCY_CERTIFIED
- DISABLED_NOT_SUPPORTED

No visible function may be left in an ambiguous "probably works" state.
