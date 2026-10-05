# CATGIS UI Reorganization — Proposal v2

**Date**: 2026-06-15 | **Status**: Proposal only, no code changes

---

## Table 1: Menu Actual → Menu Nuevo

| Menú Actual | Acción | Menú Nuevo | Submenú |
|---|---|---|---|
| Archivo | Nuevo proyecto | Archivo | — |
| Archivo | Abrir proyecto | Archivo | — |
| Archivo | Guardar | Archivo | — |
| Archivo | Guardar como | Archivo | — |
| Archivo | Recientes | Archivo | — |
| Archivo | Salvar vista | Archivo | — |
| Archivo | Salir | Archivo | — |
| Edicion | Cortar | Editar | — |
| Edicion | Copiar | Editar | — |
| Edicion | Pegar | Editar | — |
| Edicion | Borrar selección | Editar | — |
| Edicion | Deshacer | Editar | — |
| Edicion | Rehacer | Editar | — |
| Edicion | Mover selección | Editar | — |
| Edicion | Cortar geometría | Editar | — |
| Edicion | Unir vértices | Editar | — |
| Edicion | Unir elementos | Editar | — |
| Edicion | Explotar entidades | Editar | — |
| Edicion | Guardar cambios ed. | Editar | — |
| Edicion | Terminar edición | Editar | — |
| Edicion | Cancelar edición | Editar | — |
| Edicion | Copiar a editable | Editar | — |
| Edicion | Pegar en editable | Editar | — |
| Edicion | CAD (submenu 11 items) | Editar | CAD |
| Ver | Zoom +/- | Capas | — |
| Ver | Zoom a capa | Capas | — |
| Ver | Zoom a todo | Capas | — |
| Ver | Vista ant/sig | Capas | — |
| Ver | Traer al frente | Capas | — |
| Ver | Tabla de atributos | Capas | — |
| Ver | Constructor de consultas | Capas | — |
| Datos | Agregar capa | Capas | — |
| Datos | Nueva capa vectorial | Capas | — |
| Datos | Cargar tabla externa | Capas | — |
| Datos | Quitar capa | Capas | — |
| Datos | Propiedades capa | Capas | — |
| Herramientas | Mover | Capas | — |
| Herramientas | Identificar | Capas | — |
| Herramientas | Buscar coordenadas | Capas | — |
| Herramientas | Dibujar punto | Capas | — |
| Herramientas | Dibujar línea | Capas | — |
| Herramientas | Dibujar polígono | Capas | — |
| Herramientas | Medir distancia | Capas | — |
| Herramientas | Medir área | Capas | — |
| Herramientas | Terminar/Cancelar | Capas | — |
| Herramientas | Tabla de atributos | Capas | — |
| Herramientas | Constructor consultas | Capas | — |
| Herramientas | Calculadora campos | Capas | — |
| Herramientas | Asignar valor | Capas | — |
| Herramientas | Conversor CRS | Capas | — |
| Herramientas | CRS del proyecto | Capas | — |
| Herramientas | CRS personalizado | Capas | — |
| Herramientas | Renombrar proyecto | Capas | — |
| Herramientas | Scripting Python | Avanzado | — |
| Analisis | Recortar DEM | Relieve | — |
| Analisis | Curvas de nivel | Relieve | — |
| Analisis | Hillshade | Relieve | — |
| Analisis | Perfil topográfico | Relieve | — |
| Analisis | WhiteboxTools | Relieve | — |
| Analisis | Escorrentías | Drenaje | — |
| Analisis | Análisis topohidrológico | Drenaje | — |
| Analisis | Cuenca desde outlet | Drenaje | — |
| Analisis | Inundación preliminar | Drenaje | — |
| Analisis | Riesgo booleano | Ambiente | — |
| Analisis | Análisis unificado GEO | Ambiente | — |
| Analisis | Índices espectrales | Teledetección | — |
| Analisis | Clasificación ML Smile | Teledetección | — |
| Analisis | Procesamiento x lotes | Teledetección | — |
| Analisis | Redes | Redes | — |
| Analisis | pgRouting | Redes | — |
| Analisis | H3 binning | Redes | — |
| Analisis | Topología (4 validaciones) | Avanzado | — |
| Analisis | WCS | Online | — |
| Analisis | STAC | Online | — |
| Datos | OSM | Online | — |
| Datos | Esri World Imagery | Online | — |
| Datos | DEM online | Online | — |
| Datos | Suelos online | Online | — |
| Datos | Clima online | Online | — |
| Datos | Conectar CATSERVER | Avanzado | — |
| Datos | WFS | Online | — |
| Datos | WMS | Online | — |
| Salida | CATMAP | Mapa Final | — |
| Salida | Simbología capa | Mapa Final | — |
| Salida | Simbología campo | Mapa Final | — |
| Salida | Exportar KML | Mapa Final | — |
| Salida | Exportar SLD | Mapa Final | — |
| Herramientas | Módulos (Plugins) | Avanzado | — |
| — | LAS/LiDAR | Avanzado | — |
| — | DWG/CAD integración | Avanzado | — |
| — | Exportar a PostGIS | Avanzado | — |
| — | Geoprocesamiento (9 ops) | Avanzado | Geoproc |
| — | Pro raster (7 ops) | Relieve | Pro Raster |
| Ayuda | Panel ayuda | Ayuda | — |
| Ayuda | Idioma | Ayuda | — |
| Ayuda | Acerca de | Ayuda | — |

---

## Table 2: Acción → Estado Recomendado

| Acción | Estado | Etiqueta |
|---|---|---|
| Perfil topográfico | Visible | Requiere GDAL |
| Curvas de nivel | Visible | Requiere GDAL |
| Hillshade | Visible | Requiere GDAL |
| Pendiente | Visible | Requiere GDAL |
| Aspecto | Visible | Requiere GDAL |
| Visibilidad/Viewshed | Visible | Requiere GDAL |
| Recortar DEM | Visible | Requiere GDAL |
| Formas del relieve (Geomorphons) | Avanzado | Requiere WhiteboxTools |
| WhiteboxTools | Avanzado | Requiere WhiteboxTools |
| Pro raster (7 ops) | Visible | — |
| Dirección de flujo | Visible | Requiere GDAL |
| Acumulación de flujo | Visible | Requiere GDAL |
| Red de drenaje | Visible | Requiere GDAL |
| Cuenca desde outlet | Visible | Requiere GDAL |
| Orden de cauces | Visible | Requiere GDAL |
| Escorrentía | Visible | Requiere GDAL |
| Inundación preliminar | Visible | Requiere GDAL |
| Conditioning hidrológico | Avanzado | Requiere WhiteboxTools |
| Clima | Visible | Requiere Internet |
| Suelos | Visible | Requiere Internet |
| Riesgo booleano | Visible | Requiere GDAL |
| Estadísticas zonales | Ocultar | No implementado |
| Ocean Color | Avanzado | Requiere Pro raster |
| Análisis por área (GEO) | Visible | — |
| Índices espectrales | Visible | — |
| Clasificación ML Smile | Experimental | — |
| Calculadora raster | Visible | — |
| Reclasificación | Ocultar | Sin UI |
| Reproyección raster | Visible | Requiere GDAL |
| Procesamiento por lotes | Visible | — |
| Ruta más corta | Visible | — |
| Matriz de costo | Visible | — |
| Área de servicio | Visible | — |
| Centralidad | Visible | — |
| Accesibilidad | Visible | — |
| pgRouting | Visible | Requiere PostGIS |
| H3 hexagonal binning | Experimental | — |
| OSM | Visible | Requiere Internet |
| Esri Imagery | Visible | Requiere Internet |
| Esri Topo/Street/Gray/NatGeo | Visible | Requiere Internet |
| WMS | Visible | Requiere Internet |
| WFS | Visible | Requiere Internet |
| DEM online | Visible | Requiere Internet |
| Suelos online | Visible | Requiere Internet |
| Clima online | Visible | Requiere Internet |
| WCS | Experimental | Requiere Internet |
| STAC | Experimental | Requiere Internet |
| CATMAP / Layout | Visible | — |
| Simbología | Visible | — |
| Etiquetas | Visible | — |
| Exportar mapa (KML/SLD) | Visible | — |
| Salvar vista | Visible | — |
| Plugins | Experimental | Requiere opt-in |
| LAS/LiDAR | Experimental | — |
| DWG/CAD integración | Avanzado | Requiere ODA |
| GeoParquet | Ocultar | No implementado |
| PMTiles | Ocultar | No implementado |
| Scripting Python | Avanzado | Requiere Python |
| Topología | Avanzado | — |
| STAC/WCS | Experimental | Requiere Internet |
| Geoprocesamiento (9 ops) | Avanzado | Requiere GDAL |
| CATSERVER/PostGIS | Avanzado | Requiere PostGIS |
| Exportar a PostGIS | Avanzado | Requiere PostGIS |

---

## Table 3: Nueva Toolbar Principal (12 botones)

| # | Botón | Tooltip | Icono AppIcons | Acción |
|---|-------|---------|-----------------|--------|
| 1 | Abrir | Abrir proyecto CATGIS | `projectIcon()` | LoadProjectAction |
| 2 | Guardar | Guardar proyecto actual | `saveIcon()` | SaveProjectAction |
| 3 | Capa+ | Agregar capa al proyecto | `addLayerIcon()` | AddLayerAction |
| 4 | − | Acercar mapa | `zoomInIcon()` | zoomIn |
| 5 | + | Alejar mapa | `zoomOutIcon()` | zoomOut |
| 6 | ⌖ | Zoom a todas las capas | `zoomAllIcon()` | zoomToAllLayers |
| 7 | ✋ | Mover mapa (arrastrar) | `panIcon()` | enablePanMode |
| 8 | ℹ | Consultar entidades (clic) | `identifyIcon()` | enableIdentifyMode |
| 9 | • | Dibujar punto | `pointIcon()` | enableDrawPointMode |
| 10 | ∕ | Dibujar línea | `lineIcon()` | enableDrawLineMode |
| 11 | ▭ | Dibujar polígono | `polygonIcon()` | enableDrawPolygonMode |
| 12 | ⊞ | Compositor cartográfico CATMAP | `toolboxIcon()` | open CATMAP |

### Botones eliminados de la toolbar principal (movidos a menú)

| Botón actual | Movido a |
|---|---|
| Nueva capa vectorial | Menú Capas |
| Cargar tabla externa | Menú Capas |
| Guardar como | Menú Archivo |
| Salvar vista | Menú Mapa Final |
| Gestor de módulos | Menú Avanzado |
| Zoom a capa | Menú Capas |
| Vista anterior/siguiente | Menú Capas |
| Buscar coordenadas | Menú Capas |
| Multipunto | Menú Capas |
| Medir distancia/área | Menú Capas |
| Terminar/Cancelar | Aparece solo durante dibujo/medición |
| Tabla de atributos | Menú Capas |
| Conversor coordenadas | Menú Capas |
| CRS proyecto | Menú Capas |
| Quick Style toggle | Menú Capas |
| Raster Calculator | Menú Teledetección |
| Analysis Console | Menú Ambiente (GEO) |

---

## Table 4: Iconos Faltantes

| Acción | Icono sugerido | Nota |
|---|---|---|
| Curvas de nivel | ❌ faltante | Crear SVG contour |
| Pendiente | ❌ faltante | Crear SVG slope |
| Aspecto | ❌ faltante | Crear SVG aspect |
| Visibilidad | ❌ faltante | Crear SVG viewshed |
| Formas relieve | ❌ faltante | Crear SVG geomorphons |
| Dir. flujo | ❌ faltante | Crear SVG flow-dir |
| Acum. flujo | ❌ faltante | Crear SVG flow-acc |
| Red drenaje | ❌ faltante | Crear SVG drainage |
| Orden cauces | ❌ faltante | Crear SVG stream-order |
| Escorrentía | ❌ faltante | Crear SVG runoff |
| Inundación | ❌ faltante | Crear SVG flood |
| Riesgo booleano | ❌ faltante | Crear SVG risk |
| Índices espectrales | ❌ faltante | Crear SVG spectral |
| Clasificación ML | ❌ faltante | Crear SVG ml |
| Reclasificación | ❌ faltante | Crear SVG reclass |
| Reproyección | ❌ faltante | Crear SVG reproject |
| Ruta más corta | ❌ faltante | Crear SVG route |
| Matriz costo | ❌ faltante | Crear SVG matrix |
| Área servicio | ❌ faltante | Crear SVG service-area |
| Centralidad | ❌ faltante | Crear SVG centrality |
| Accesibilidad | ❌ faltante | Crear SVG accessibility |
| PgRouting | ❌ faltante | Crear SVG pgrouting |
| WMS | ❌ faltante | existe `wmsIcon` ✅ |
| WFS | ❌ faltante | Crear SVG wfs |
| WCS | ❌ faltante | Crear SVG wcs |
| STAC | ❌ faltante | Crear SVG stac |
| DEM online | ❌ faltante | Crear SVG dem-download |
| Suelos online | ❌ faltante | Crear SVG soil |
| Clima online | ❌ faltante | Crear SVG climate |
| Etiquetas | ❌ faltante | existe `labelsIcon` ✅ |
| Leyenda | ❌ faltante | Crear SVG legend |
| Escala | ❌ faltante | Crear SVG scale |
| Norte | ❌ faltante | Crear SVG north |
| Exportar mapa | ❌ faltante | existe `exportIcon` ✅ |
| Plugins | ❌ faltante | Crear SVG plugin |
| LAS/LiDAR | ❌ faltante | Crear SVG las |
| DWG | ❌ faltante | Crear SVG dwg |
| GeoParquet | ❌ faltante | Crear SVG parquet |
| PMTiles | ❌ faltante | Crear SVG pmtiles |
| Scripting Python | ❌ faltante | Crear SVG python |
| Topología | ❌ faltante | Crear SVG topology |
| Geoprocesamiento | ❌ faltante | Crear SVG geoproc |

---

## Table 5: Dependencia → Acciones a deshabilitar

| Dependencia | Cómo detectar | Acciones afectadas |
|---|---|---|
| GDAL | `GdalSupport.resolve("gdal_translate")` no nulo | 15 acciones en Relieve y Drenaje |
| PostGIS | `PostgisConnectionFactory` sin conexión | CATSERVER, Exportar PostGIS, pgRouting |
| ODA Teigha | `DwgImportSupport.isAvailable()` | DWG import |
| WhiteboxTools | `whitebox_tools.exe` en PATH/OSGeo4W | WhiteboxTools, Geomorphons, Cond. hidro |
| Python | `python --version` exit 0 | Scripting |
| Internet | `InetAddress.getByName("tile.openstreetmap.org")` reachable | 17 acciones Online |
| OpenTopography key | `System.getProperty("opentopography.key")` | DEM online (OpenTopography) |
| Plugins | `"true".equals(System.getProperty("catgis.plugins.enabled"))` | Plugins |

---

## Summary

| Métrica | Valor |
|---|---|
| Menús totales | 7 (Archivo, Editar, Capas, Análisis:6 submenús, Online, Mapa Final, Avanzado, Ayuda) |
| Toolbar principal | 31 → 18 botones |
| Acciones Visibles | ~75 |
| Acciones Avanzado | ~20 |
| Acciones Experimentales | ~12 |
| Acciones Ocultar | ~5 (no implementadas) |
| Iconos faltantes | ~38 SVGs nuevos necesarios |
| Dependencias detectadas | 8 |
