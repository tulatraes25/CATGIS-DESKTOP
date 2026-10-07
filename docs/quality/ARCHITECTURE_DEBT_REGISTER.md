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

## Test-policy debt

Conditional assumptions are currently restricted to five reviewed test files. `@Disabled` is forbidden. Linux CI starts with a maximum skip budget of 3; Windows certification starts with a maximum skip budget of 6. The budget must ratchet downward as external fixtures become reproducible in GitHub.

## Closure rule

This register can be marked CLOSED_CERTIFIED only after:
- no CRITICAL/HIGH architecture finding remains;
- direct blocking I/O/process/network access is behind explicit adapters or certified exceptions;
- core/domain dependencies are clean;
- hotspot responsibilities have been reduced with characterization coverage;
- Linux + Windows + integration certification workflows pass on the same candidate SHA.
