# CATGIS Desktop — Beta Cerrada

**Versión**: main @ `d0934b7` | **Fecha**: 2026-06-14
**Estado**: Preparación para beta cerrada (5-10 testers de confianza).

---

## Qué está incluido

| Funcionalidad | Estado |
|---|---|
| Carga de Shapefile (.shp) | ✅ Estable |
| Carga de GeoPackage (.gpkg) | ✅ Estable |
| Carga de FlatGeobuf (.fgb) | ✅ Estable, validación estricta |
| Carga de SpatiaLite (.sqlite/.db) | ✅ Estable, validación de header SQLite |
| Carga de DXF (ASCII) | ✅ Básico |
| Carga de GeoTIFF / raster | ✅ Estable, 3 modos (preview/virtual/real) |
| DEM / Hillshade | ✅ Algoritmo Horn 1981 |
| WMS / WFS | ✅ Cliente básico |
| Servicios de teselas online (XYZ/TMS) | ✅ Cache LRU 200MB |
| PostGIS (lectura/escritura) | ✅ AES-256-GCM, HikariCP pooling |
| Edición vectorial (mover/añadir/eliminar vértices, cortar, unir, merge/explode) | ✅ Estable |
| Geoprocesamiento (buffer, clip, intersect, union, dissolve) | ✅ Vía GDAL |
| Hidrología / topografía (flow accumulation, watershed, contour, viewshed, geomorphons) | ✅ Vía WhiteboxTools |
| Índices espectrales (NDVI, EVI, SAVI, BSI, NDWI, etc.) | ✅ |
| Raster calculator (215+ funciones de expresión) | ✅ |
| Exportación CATMAP a PDF/PNG/SVG | ✅ Layout A4/A3 landscape/portrait |
| Plantillas CATMAP (76+) | ✅ Biblioteca masiva |
| Guardar / cargar proyecto (.catgis) | ✅ Save atómico con backup |
| Sistema de notificaciones (Toast + diálogos modales) | ✅ 97% JOptionPane migradas |
| Golden image testing | ✅ 11 tests con PNGs de referencia |

---

## Qué está excluido (no en beta cerrada)

| Funcionalidad | Razón |
|---|---|
| pgRouting | Experimental — requiere base PostGIS con tablas de ruteo. Sin tests automatizados. |
| LAS/LiDAR | Experimental — lectura parcial (formatos 2/3), sin tests. |
| DWG import | Requiere ODA Teigha (Windows-only, binario externo). Sin tests. |
| DWG export | No implementado. |
| KML import | No implementado. |
| GPX import | No implementado. |
| CSV import (completo) | Solo puntos. Sin tests. |
| SLD completo | Solo reglas básicas. Sin tests. |
| 3D terrain | Experimental. No validado. |
| Plugins | **Experimental — sin sandbox de seguridad.** Los plugins se ejecutan con permisos completos. Solo instalar de fuentes confiables. |
| Scripting Python | Básico. Console existe pero sin tests de integración. |
| WFS transacciones | Básico. Insert/update simple, sin tests. |
| Análisis de redes | Experimental vía pgRouting. |

---

## Requisitos de instalación

### Mínimo

| Componente | Versión | Notas |
|---|---|---|
| Windows | 10 / 11 (64-bit) | No testeado en Windows 7/8 |
| Java | 17 o superior | `java -version` debe mostrar 17+ |
| OSGeo4W | Cualquier versión reciente | Instalar en `C:\OSGeo4W64` o `C:\OSGeo4W` |
| GDAL | Incluido con OSGeo4W | Verificar: `gdalinfo --version` |
| RAM | 4 GB mínimo | 8 GB recomendado para rasters grandes |
| Espacio disco | 500 MB | ~120K líneas de código, dependencias Maven |

### Opcional

| Componente | Para |
|---|---|
| PostgreSQL 14+ + PostGIS 3+ | Capas PostGIS, pgRouting |
| Python 3.8+ | Consola de scripting |
| WhiteboxTools | Hidrología avanzada |
| ODA Teigha | Importación DWG |

---

## Limitaciones conocidas

1. **Windows-only**: No hay soporte para Linux o macOS. Dependencias de OSGeo4W y AutoCAD ODA son Windows-only.
2. **Sin tests PostGIS automatizados**: Conexión a base de datos requiere servidor externo. Probado manualmente.
3. **Sin tests GeoPackage/FlatGeobuf con datos reales**: Tests existen pero usan datos sintéticos. La validación estricta de headers reduce el riesgo.
4. **Memoria de rasters**: Los rasters grandes (>500 MB) pueden consumir mucha memoria. El modo "virtual" reduce el uso pero la UI puede trabarse durante la carga inicial.
5. **Idioma**: La interfaz está en español. Algunos mensajes de error están en español. No hay soporte i18n completo (traducciones parciales).
6. **Renderizado**: Depende de Java2D. Anti-aliasing varía entre JDK/OS. Los golden tests usan tolerancia de 1% + delta de 8 por canal ARGB.
7. **Plugins**: Sin sandbox. El classloader de plugins (URLClassLoader) NO aísla el código — cualquier JAR tiene acceso completo a archivos, red y memoria.
8. **CatmapLayoutItems legacy**: El sistema de CatmapLayoutItems (modelo de lista Swing) coexiste con LayoutElements. Algunas operaciones de UI usan el sistema legacy.
9. **Ramas divergidas**: `main` está 354 commits ahead y 7 behind de `origin/main`. Requiere reconciliación antes de release pública.

---

## Cómo reportar errores

1. **Logs**: Los logs de CATGIS se escriben vía Log4j2 (configuración en `src/main/resources/log4j2.xml`). Por defecto van a consola y archivo.
2. **Enviar**: Adjuntar el archivo de log completo, captura de pantalla del error, y pasos para reproducir.
3. **Formato del reporte**:
   - Qué hiciste (pasos exactos)
   - Qué esperabas que pase
   - Qué pasó realmente
   - Archivo/log adjunto
   - Versión de Windows y Java
4. **No incluir**: Contraseñas de PostGIS, archivos de proyecto con credenciales, datos personales.

---

## Advertencia sobre plugins

> **EXPERIMENTAL**: Los plugins se ejecutan con los mismos permisos que la aplicación. Pueden leer/escribir archivos, acceder a la red y ejecutar código arbitrario. CATGIS no aplica sandbox ni restringe el código de plugins. Solo instalar plugins de fuentes confiables.

Los plugins se cargan desde el directorio `plugins/` al inicio. No hay confirmación de usuario. No hay verificación de firma digital.
