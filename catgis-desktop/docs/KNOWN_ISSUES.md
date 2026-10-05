# CATGIS Desktop — Known Issues

**HEAD**: `d0934b7` | **Fecha**: 2026-06-14

---

## Riesgos abiertos del Risk Register

| ID | Riesgo | Severidad | Estado |
|---|---|---|---|
| R-01 | Plugin ClassLoader sin sandbox | 🔴 ALTA | Documentado (experimental). Sin mitigación técnica. |
| R-03 | GribLoader catch vacío | 🟡 MEDIA | Corregido (CatgisLogger.error). Pendiente prueba manual con fixture GRIB real. |
| R-04 | CatmapSerializer parseos silenciosos | 🟡 MEDIA | Corregido (CatgisLogger.warn + save atómico). 10 tests. |
| R-05 | 4 catch(Exception) vacíos | 🟢 BAJA | Corregidos 2. 2 benignos mantenidos (LookAndFeel, ds.close). |
| R-09 | Tests sin datasets reales | 🔴 ALTA | DXF ✅. GeoPackage/FlatGeobuf ⏸️ (andamiados, bloqueados por dependencias de build). |
| R-10 | Memoria de rasters | 🟡 MEDIA | Corregido. dispose() en clearAllLayers y removeLayer. Pendiente prueba manual. |
| R-12 | Corrupción de archivo de proyecto | 🟡 MEDIA | Corregido. Save atómico (.tmp → rename + .bak). |

---

## Features sin datasets reales

| Feature | Estado | Bloqueante |
|---|---|---|
| GeoPackage | ⏸️ Tests @Disabled | GeoTools Geometries enum API mismatch |
| FlatGeobuf | ⏸️ Tests @Disabled | Falta flatbuffers-java en test dependencies |
| PostGIS | 📋 Sin tests automatizados | Requiere servidor externo |
| pgRouting | 📋 Sin tests | Requiere servidor + tablas de ruteo |
| DXF (completo) | 📋 Solo fixture mínimo | Pendiente DXF con layers, texto, bloques |
| DWG | 📋 Sin tests | Requiere ODA Teigha |
| LAS/LiDAR | 📋 Sin tests | Experimental |
| WMS/WFS | 📋 Sin tests | Requiere servidor externo |
| Plugins | 📋 Sin tests | SPI discovery sin validar |
| Scripting | 📋 Sin tests de integración | Requiere Python instalado |

---

## Formatos experimentales

| Formato | Limitación |
|---|---|
| DWG | Solo import vía ODA Teigha. Windows-only. Binario externo. Sin tests. |
| LAS/LiDAR | Solo point formats 2/3 con RGB. Coordinate scaling funciona, sin tests. |
| pgRouting | Interpolación de nombre de tabla protegida (regex + catálogo + PreparedStatement). Sin tests de integración. |
| SLD | Solo reglas básicas (stroke, fill, symbol). Sin soporte OGC completo. |
| KML import | No implementado. Solo export. |
| GPX import | No implementado. |
| CSV import | Solo puntos (x, y, nombre). Sin soporte WKT geometry. |
| Plugins | ClassLoader sin sandbox. Sin UI de gestión. Carga automática sin confirmación. |

---

## Operaciones que pueden tardar

| Operación | Tiempo estimado | Causa |
|---|---|---|
| Carga de GeoTIFF grande (>500 MB) | 30s - 2min | Decodificación de imagen en modo "real" |
| Geoprocesamiento (buffer, clip) | 1-5 min por capa | Ejecución externa vía GDAL |
| Hidrología (flow accumulation) | 2-10 min | Ejecución externa vía WhiteboxTools |
| Exportación CATMAP a PDF (alta resolución) | 5-30s | Renderizado de página completa a 300 DPI |
| Conexión PostGIS inicial | 5-15s | Pool HikariCP + verificación de conexión |
| Golden image tests | 1-2 min | 11 tests de renderizado de layout |

---

## Casos no soportados

| Caso | Razón |
|---|---|
| CRS sin código EPSG | GeoTools requiere identificador de autoridad. CRS personalizados necesitan WKT. |
| Shapefile sin .prj | Se asume EPSG:4326. Puede causar desplazamiento si el archivo está en otra proyección. |
| Raster con proyección no conforme | Algunas proyecciones locales pueden fallar en la reproyección. |
| PostGIS con SSL/TLS obligatorio | No configurado por defecto. Requiere ajuste manual de parámetros JDBC. |
| DWG 3D solids | Solo se importan geometrías 2D (líneas, arcos, círculos, polilíneas). |
| Múltiples instancias de CATGIS | No testeado. Puede haber conflictos en archivos de configuración o puertos. |
| Ejecución desde path con espacios | Algunas rutas de OSGeo4W pueden fallar si contienen espacios. Usar `C:\OSGeo4W64`. |
| HiDPI / 4K displays | Escalado puede ser incorrecto. Java2D no maneja bien HiDPI en algunas configuraciones. |
