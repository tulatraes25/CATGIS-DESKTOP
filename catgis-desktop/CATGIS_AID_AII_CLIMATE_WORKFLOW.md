# CATGIS AID/AII Climate Workflow

## Suggested Workflow for Environmental Consultants

This document describes the recommended end-to-end workflow for climate-environmental analysis in CATGIS.

---

## Step 1: Load Climate Data

### Option A: NetCDF Files
1. Go to **Archivo → Abrir capa** (or click the folder icon)
2. In the format dropdown, select **"Datos climáticos (NetCDF)"**
3. Browse to your `.nc` or `.nc4` file
4. In the variable dialog, select the climate variable to visualize (e.g., temperature, precipitation)
5. If the file has multiple time steps, select the desired time step
6. The layer appears in the layer panel and map view

### Option B: GRIB2 Files (via conversion)
1. Open a terminal and convert your GRIB2 file:
   ```
   cdo -f nc4 copy forecast.grib2 forecast.nc4
   ```
2. Load the resulting `.nc4` file as described in Option A

### Option C: Other Raster Formats
Standard raster formats (GeoTIFF, ASC, etc.) can also be used for climate analysis.

---

## Step 2: Define AID/AII Areas

1. **Load or create polygon layers** representing your areas of interest:
   - Use **Herramientas → Dibujar polígono** to digitize areas
   - Or load existing shapefiles/GeoJSON/GPKG containing polygons
2. **Right-click** on a polygon layer in the layer panel
3. Select **"Marcar como área de influencia..."**
4. Choose the area type:
   - **AID** - Área de Influencia Directa (directly affected by the project)
   - **AII** - Área de Influencia Indirecta (indirectly affected)
   - **Ambiental** - Other environmental area

Repeat for all polygon layers that represent influence zones.

---

## Step 3: Apply Climate Visualization

1. **Right-click** on the climate raster layer in the layer panel
2. Go to **Clima y ambiente → Aplicar simbología climática**
3. Select the variable type:
   - Temperatura (-10°C a 45°C): blue→red
   - Precipitación (0-500 mm): white→purple
   - Viento (0-30 m/s): white→red
   - Presión (960-1050 hPa): purple→yellow
   - Personalizado
4. Adjust opacity as needed
5. Click **"Aplicar simbología"**
6. The raster now displays with the professional climate colormap

---

## Step 4: Run Climate-Area Analysis

1. **Right-click** on any raster layer
2. Go to **Clima y ambiente → Análisis climático por áreas (AID/AII)**
3. **Select** the climate raster layer
4. **Select** the AID/AII polygon layer (marked in Step 2)
5. **Choose statistics** to compute:
   - Media (mean)
   - Mínimo (minimum)
   - Máximo (maximum)
   - Desv. estándar (standard deviation)
   - Suma (sum - useful for precipitation totals)
   - Conteo de píxeles (pixel count)
6. Click **"Generar análisis"**
7. Review results in the table

### Export Options
- **Copiar al portapapeles**: Copy text summary
- **Exportar CSV**: Save to file for further analysis in Excel, R, etc.

---

## Step 5: Generate Wind Rose (Optional)

If you have wind data (U and V components):

1. Load two NetCDF files or raster layers with U (eastward) and V (northward) wind components
2. **Right-click** any raster layer
3. Go to **Clima y ambiente → Rosa de los vientos**
4. Select the U component layer and V component layer
5. Optionally toggle frequency/speed/label display
6. Click **"Calcular rosa de vientos"**
7. Export as transparent PNG or copy to clipboard

---

## Step 6: Bring Results to CATMAP

### Export Climate Layer to Layout
1. Climate raster layers (with applied colormaps) render correctly in CATMAP map frames
2. Open CATMAP and add a map frame with your climate layer

### Insert Wind Rose
1. Export the wind rose as transparent PNG from the wind rose dialog
2. In CATMAP, add a **LayoutImage** element with the exported PNG
3. Position the wind rose in the layout (e.g., lower-right corner)

### Export Final Layout
1. In CATMAP, use **Export layout** to generate:
   - PNG (for reports)
   - JPG (for presentations)  
   - PDF (for deliverables)

---

## Example Use Case: Environmental Impact Assessment

**Scenario**: Evaluate temperature and precipitation patterns across a project's influence zones.

1. **Load**: NetCDF with monthly temperature and precipitation data
2. **Mark**: Create polygon for AID (project area) and AII (10km buffer)
3. **Visualize**: Apply temperature colormap to temperature layer
4. **Analyze**: Run Climate-Area Analysis to get mean temperature and precipitation for AID and AII
5. **Export CSV**: Save results for EIA report
6. **Wind Rose**: If wind data available, generate wind rose for AID
7. **CATMAP**: Create final map layout with climate visualization and wind rose

---

## Keyboard Shortcuts & Tips

- **Ctrl+O**: Open layer dialog (includes NetCDF option)
- **Right-click on layer**: Access all climate functions from context menu
- **Opacity slider**: Apply transparency for better overlay visualization
- **AID/AII marking**: Reversible - right-click and select "Quitar marca" to clear

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| NetCDF won't load | Ensure the file contains 2D gridded variables (lat x lon). Verify file integrity with `ncdump -h file.nc` |
| No polygon layers appear in marking dialog | Load or create polygon layers first. Line/point layers are not supported for AID/AII |
| No statistics results | Ensure the climate raster and polygon layer overlap spatially and share the same CRS |
| Wind rose shows no data | The U/V component raster must have climate metadata stored by the loader. Load via NetCdfLoader for automatic metadata |
| GRIB2 file not recognized | Convert to NetCDF using CDO: `cdo -f nc4 copy input.grib2 output.nc4` |
