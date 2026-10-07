# CATGIS Architecture Debt Register

Status: ACTIVE / NOT_CERTIFIED
Baseline: `e83abc539c1e26c74ae4ab1de490af1560d66179`

Purpose: establish executable no-growth guardrails before architecture burn-down. Entries are not accepted architecture; they are explicit debt that must be reduced or justified before `ARCHITECTURE_STATUS=CLOSED_CERTIFIED`.

## Certified no-growth allowlists

### Swing inside core
- `core/model/DataDefinedOverridesPanel.java`

### Swing / global context inside service
- `service/EventBusInitializer.java`

### Direct ProcessBuilder
- `CartographyToolbar.java`
- `DwgImportSupport.java`
- `ExternalToolService.java`
- `Gdal2TilesService.java`
- `PostgisConnectionStore.java`
- `ProDatasetOpenService.java`
- `ProRasterMaterializationService.java`
- `RasterImageLoader.java`
- `RasterReprojectionService.java`
- `scripting/ScriptEngine.java`

### Direct HttpURLConnection
- `OnlineWmsImageCache.java`
- `StacClient.java`
- `WcsClient.java`
- `WfsCapabilitiesService.java`
- `WmsCapabilitiesService.java`
- `data/online/OnlineTileCache.java`

### Thread.sleep
- `AnalysisConsoleDialog.java`
- `SplashScreenWindow.java`
- `TemporalController.java`

These lists are exact. New usage fails the architecture test. Removal is encouraged and requires reducing the allowlist in the same PR.

## Hotspot ceilings

| File | Ceiling lines | Closure intent |
|---|---:|---|
| MapPanel.java | 3500 | extract rendering/interaction/application coordination |
| MapLayoutComposerDialog.java | 5625 | split composer controllers/panels/export orchestration |
| LayersPanel.java | 2350 | separate tree model, context actions, DnD |
| MapEditingEngine.java | 2525 | isolate edit session/snapping/operations |
| ExportVectorLayerAction.java | 1900 | per-format exporters behind dispatch |
| LayoutPreviewPanel.java | 1810 | separate renderer/selection interaction |
| MainMenuBar.java | 1370 | menu/action composition |
| FloatingVectorEditToolbar.java | 1090 | separate tool groups/state |
| LoadProjectAction.java | 995 | parser/reconstruction/restoration boundaries |

These are ceilings, not targets. Architecture certification requires deliberate reduction of the critical hotspots, not merely staying below the ceiling.

## Coverage baseline and ratchet

GitHub certification run `37554342919` on SHA `710073b8c2a3569b5e8e86765bc60cf49e63f589` established the first measured automated baseline:

| Metric | Covered | Missed | Ratio |
|---|---:|---:|---:|
| Lines | 14,128 | 64,219 | 18.03% |
| Branches | 6,553 | 38,285 | 14.61% |
| Methods | 2,095 | 7,791 | 21.19% |
| Classes | 347 | 614 | 36.11% |

The suite executed 753 tests with 0 failures/errors and 3 reviewed environment skips.

The initial blocking floor is intentionally a **no-regression ratchet**, not a certification target:
- line coverage >= 18%;
- branch coverage >= 14%.

Architecture/product certification is NOT achieved by meeting these low baseline floors. F1/F2/R1 must add behavior-driven tests and raise coverage, especially for currently weak user-critical hotspots. Coverage thresholds must move upward as each domain is certified; lowering them requires an explicit spec decision.

Current large-class evidence shows severe gaps that make runtime certification premature, including near-zero coverage in major Swing/orchestration surfaces such as MapLayoutComposerDialog, MapEditingEngine, MainMenuBar, LayoutPreviewPanel, FloatingVectorEditToolbar and multiple dialogs.

## Test-policy debt

Conditional assumptions are currently restricted to five reviewed test files. `@Disabled` is forbidden. Linux CI starts with a maximum skip budget of 3; Windows certification starts with a maximum skip budget of 6. The budget must ratchet downward as external fixtures become reproducible in GitHub.

## Closure rule

This register can be marked CLOSED_CERTIFIED only after:
- no CRITICAL/HIGH architecture finding remains;
- direct blocking I/O/process/network access is behind explicit adapters or certified exceptions;
- core/domain dependencies are clean;
- hotspot responsibilities have been reduced with characterization coverage;
- Linux + Windows + integration certification workflows pass on the same candidate SHA.
