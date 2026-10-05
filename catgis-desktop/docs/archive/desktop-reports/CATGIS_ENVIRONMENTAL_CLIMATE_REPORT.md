# CATGIS Environmental & Climate Module - Implementation Report

## 1. What Was Implemented

A complete climate and environmental analysis module integrated into CATGIS Desktop. The module provides:

- **NetCDF data loading** for gridded climate variables (temperature, precipitation, wind, pressure)
- **Climate colormaps** (temperature, precipitation, wind speed, pressure, terrain)
- **Climate visualization dialog** for applying colormaps to raster layers
- **AID/AII area marking** for environmental influence zones
- **Zonal statistics** (climate-area analysis) for marked areas
- **Wind rose generation** from U/V wind components
- **CATMAP integration** for climate layer export
- **Online climate sources** (NOAA, Copernicus, NASA GIBS, OpenWeatherMap, Windy)

## 2. Supported Climate Formats

| Format    | Status | Details |
|-----------|--------|---------|
| NetCDF (.nc, .nc4) | ✅ **Full** | Dual-mode loading: GeoTools gt-netcdf (preferred) + ucar-netcdf fallback. Reads 2D gridded variables with lat/lon georeferencing. |
| NetCDF (.cdf) | ✅ Supported | Via same loader as .nc |
| GRIB2 (.grib, .grib2) | ❌ **Not implemented** | GRIB requires the GeoTools gt-grib module which has complex native dependencies. Workaround: convert to NetCDF using CDO (`cdo -f nc4 copy input.grib2 output.nc4`) or NCO tools. |

## 3. Climate Visualization

### Colormaps
Four predefined colormaps, each with 256-entry Color array:

| Variable | Range | Colors |
|----------|-------|--------|
| Temperature | -10°C to 45°C | blue → cyan → green → yellow → red |
| Precipitation | 0 to 500 mm | white → light blue → blue → dark blue → purple |
| Wind speed | 0 to 30 m/s | white → light green → yellow → orange → red |
| Pressure | 960 to 1050 hPa | dark purple → blue → green → yellow |
| Terrain | Custom | green → brown → white |

### Auto-detection
NetCDF variable names are automatically mapped to climate variable types:
- Temperature: `temp`, `t2m`, `t_2m`, `skt`, `air`, `sst`, `tsoil`
- Precipitation: `precip`, `rain`, `tp`, `prate`, `prcp`, `snow`, `apcp`
- Wind: `wind`, `u10`, `v10`, `ugrd`, `vgrd`, `wspd`
- Pressure: `press`, `psfc`, `mslp`, `pmsl`, `sp`, `prmsl`

### Custom Colormap Support
`MapPanel.RasterStyle` now includes:
- `customColorMap` (Color[]): applied to single-band climate rasters
- `colorMapMin / colorMapMax`: value range for color mapping

## 4. AID/AII Association

### Marking System
Polygon layers can be tagged via right-click context menu:
- `Área de Influencia Directa (AID)`
- `Área de Influencia Indirecta (AII)`
- `Área Ambiental (general)`

### Storage
Metadata stored in `Layer.getUserData()` map with keys:
- `environmentalAreaType`: enum name (AID, AII, AMBIENTAL)
- `environmentalAreaTypeLabel`: human-readable label

### API
`EnvironmentalAreaMarker` provides:
- `markLayer(layer, type)`: tag a polygon layer
- `clearMark(layer)`: remove tag
- `getAreaType(layer)`: query type
- `getMarkedAreaLayers()`: list all marked layers
- `showMarkDialog(owner)`: interactive dialog
- `markSingleLayer(owner, layer)`: quick context menu dialog

## 5. Available Statistics Per Area

The `ClimateAreaAnalysisDialog` computes zonal statistics for each polygon feature in the selected area layer:

| Statistic | Description | Applicable To |
|-----------|-------------|---------------|
| Media | Mean pixel value within polygon | All variables |
| Mínimo | Minimum pixel value | All variables |
| Máximo | Maximum pixel value | All variables |
| Desv. estándar | Standard deviation | All variables |
| Suma | Sum of pixel values | Precipitation, wind |
| Conteo | Number of pixels sampled | All variables |

### Visualization
- Results displayed in a JTable with columns: Área, Variable, Media, Mínimo, Máximo, Desv. estándar, Suma, Píxeles, Observaciones
- Copy to clipboard
- Export to CSV

## 6. Wind Rose Generation

### Components
- **`WindRoseRenderer`**: Swing JComponent that renders a complete wind rose with:
  - 16 cardinal directions (N, NNE, NE, ENE, E, ESE, SE, SSE, S, SSW, SW, WSW, W, WNW, NW, NNW)
  - Frequency petals (blue): show how often wind comes from each direction
  - Speed petals (orange/red): show average wind speed per direction
  - Direction labels
  - Reference circles (25%, 50%, 75%, 100%)
  - Legend (frequency, speed, calm %)
  - Transparent background suitable for CATMAP overlay

- **`WindRoseDialog`**: Interactive dialog with:
  - U/V component layer selection from raster layers
  - Toggle options (frequency, speed, labels)
  - Export as PNG with transparent background
  - Copy to clipboard
  - Demo data generation for preview

### Computation
- Uses U (eastward) and V (northward) wind components
- Meteorological convention: direction = atan2(-u, -v)
- Wind speed = sqrt(u² + v²)
- 16-bin directional histogram

## 7. CATMAP Integration

- **`LayoutExportEngine.createWindRoseLayoutImage()`**: Creates a LayoutImage element from a WindRoseRenderer, ready for insertion into CATMAP layouts
- **`LayoutExportEngine.applyClimateColormapsToLayout()`**: Ensures climate colormaps are preserved during layout rendering
- Climate raster layers with custom colormaps render consistently between map view and export
- Wind rose export: transparent PNG suitable for CATMAP overlay

## 8. Limitations

1. **GRIB2 not supported**: GRIB files require the GeoTools gt-grib module with native GDAL bindings. Current workaround: convert to NetCDF using CDO.
2. **NetCDF 3D/4D**: Time-series and pressure-level data are loaded as single time steps. Full multi-dimensional support requires further work.
3. **Pixel sampling**: Zonal statistics use image-based pixel sampling (2-4 pixel steps), not full GeoTools GridCoverage2D evaluation. Precision is adequate for visualization but may differ from GIS-native zonal stats.
4. **Wind rose demo data**: If no U/V component layers are found, the wind rose dialog generates synthetic demo data for preview.
5. **Online climate sources**: All registered sources are informational. Some require API keys (OpenWeatherMap, Windy). Others may change endpoints (NOAA GFS, Copernicus).
6. **No automatic CRS reprojection for analysis**: Zonal stats assume raster and polygon layers share the same CRS. Manual CRS alignment is recommended.
7. **Large file handling**: Very large NetCDF files (> 500MB) may cause memory issues during full-raster pixel extraction.

## 9. Next Steps

1. **GRIB2 support**: Add `org.geotools:gt-grib:34.0` dependency and implement GRIB reader similar to NetCdfLoader.
2. **Multi-temporal analysis**: Extend zonal statistics to compute trends across multiple time steps.
3. **Full GeoTools GridCoverage2D evaluation**: Replace pixel-sampling with GeoTools coverage grid evaluation for precise statistics.
4. **Terrain analysis**: Extend colormaps with slope, aspect, and hillshade support.
5. **ERA5 integration**: Add direct Copernicus CDS API access for ERA5 climate reanalysis data.
6. **PDF generation**: Add wind rose directly into PDF exports without intermediate image files.

## Files Created/Modified

### New files (package `ar.com.catgis.climate`):
- `ClimateColormaps.java` - Predefined climate colormaps
- `NetCdfLoader.java` - NetCDF file loading with variable/time selection
- `GribLoader.java` - GRIB documentation and conversion workflow
- `ClimateVisualizationDialog.java` - Climate symbology application dialog
- `EnvironmentalAreaMarker.java` - AID/AII area marking system
- `ClimateAreaAnalysisDialog.java` - Zonal statistics analysis
- `WindRoseRenderer.java` - Wind rose rendering component
- `WindRoseDialog.java` - Wind rose generation dialog

### Modified files:
- `build.gradle` - Added NetCDF and CSV dependencies
- `Layer.java` - Added `getUserData()` / `putUserData()` property map
- `MapPanel.java` - Added `customColorMap` / `colorMapMin/Max` to RasterStyle
- `AddLayerDialog.java` - Added "Datos climáticos (NetCDF)" option
- `OpenFileAction.java` - Added NetCDF/GRIB file handling
- `LayersPanel.java` - Added climate context menu entries
- `OnlineMapCatalog.java` - Added climate data sources
- `catmap/LayoutExportEngine.java` - Added wind rose layout image support

### Documentation:
- `CATGIS_ENVIRONMENTAL_CLIMATE_REPORT.md` (this file)
- `CATGIS_AID_AII_CLIMATE_WORKFLOW.md`

## 10. Gap Closure Status (2026-06-05)

### ✅ Closed Gaps
| Gap | Status | Details |
|-----|--------|---------|
| Table→CATMAP flow | ✅ Closed | `--import-table`, `pendingCatmapTable`, LayoutTable.createFromData |
| Period presets | ✅ Closed | Último mes, Último año, Normal climática, Personalizado |
| Provider honesty | ✅ Closed | Only WorldClim + Open-Meteo listed as real providers |
| GRIB honesty | ✅ Closed | Documented as unsupported with CDO conversion workflow |
| Area analysis honesty | ✅ Closed | Labeled as "muestreo aproximado" with disclaimer |

### ❌ Remaining Gaps
| Gap | Priority | Notes |
|-----|----------|-------|
| GRIB native support | Low | Requires gt-grib + GDAL native deps |
| Full GridCoverage2D zonal stats | Medium | Would replace pixel sampling |
| Multi-temporal analysis | Low | Currently single-period per layer |
| Direct ERA5/Copernicus download | Low | Requires CDS API key |
