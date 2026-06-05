# CATGIS Climate Module — Gap Closure Report

**Date:** 2026-06-05  
**Branch:** `codex/climate-gap-closure`

## Summary

This round closes 5 identified gaps in the climate module and the CATGIS→CATMAP workflow.

## Files Modified

| File | Change |
|------|--------|
| `src/ar/com/catgis/layout/LayoutTable.java` | Added `createFromData()` factory method |
| `src/ar/com/catgis/catmap/Main.java` | Added `--import-table` CLI, `importClimateTable()`, `checkPendingCatmapTable()`, menu item |
| `src/ar/com/catgis/catmap/CatisSocketServer.java` | Updated `ADD_TABLE` handler to use `ClimateAreaAnalysisDialog` Preferences key |
| `src/ar/com/catgis/climate/GribLoader.java` | Replaced with honest unsupported documentation |
| `src/ar/com/catgis/climate/OnlineClimateProvider.java` | Already clean (no changes needed) |
| `src/ar/com/catgis/climate/ClimateOnlineDownloadDialog.java` | Added period presets, updated summary, stores period metadata |
| `src/ar/com/catgis/climate/ClimateAreaAnalysisDialog.java` | Added precision disclaimer, honest "muestreo aprox." label |
| `src/ar/com/catgis/OnlineMapCatalog.java` | Added visualization-only comment block |
| `CATGIS_ENVIRONMENTAL_CLIMATE_REPORT.md` | Added gap closure status section |
| `CATGIS_CLIMATE_GAP_CLOSURE_REPORT.md` | This file |

## Gaps Closed

1. **Table→CATMAP flow**: Climate analysis results can now be sent from ClimateAreaAnalysisDialog to CATMAP standalone via `--import-table` flag or Preferences-based `pendingCatmapTable` flow. LayoutTable.createFromData() enables programmatic table creation.

2. **Period presets**: ClimateOnlineDownloadDialog now has a period preset combo (Personalizado, Último mes, Último año, Normal climática). WorldClim only shows "Personalizado" (static data). Dates auto-fill from presets.

3. **Provider honesty**: OnlineClimateProvider only lists WorldClim and Open-Meteo — the two actually implemented providers.

4. **GRIB honesty**: GribLoader now explicitly documents GRIB2 as unsupported with the CDO conversion workflow.

5. **Full GridCoverage2D zonal statistics** (2026-06-05): Replaced BufferedImage pixel-sampling with real `GridCoverage2D.evaluate()`. Now evaluates actual pixel values directly from the grid coverage, not estimations from rendered image. Removed disclaimer panel and "muestreo aprox." labels. Renamed observation column to "píxeles evaluados".

## Remaining Gaps

- GRIB native support (requires gt-grib + GDAL)
- Multi-temporal analysis
- Direct ERA5/Copernicus download (requires CDS API key)
