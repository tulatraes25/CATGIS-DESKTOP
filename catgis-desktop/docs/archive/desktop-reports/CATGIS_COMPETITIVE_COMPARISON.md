# CATGIS Desktop — Comparación con Competidores GIS

**Fecha**: 2026-06-03

---

## Tabla Comparativa General

| Programa | Tecnología | Última versión | Licencia | Fuerzas | Debilidades |
|----------|-----------|----------------|----------|---------|-------------|
| **CATGIS** | Java 17/Swing | v1.0 (2026) | Propietaria | Enfoque ambiental, 28 plantillas, PostGIS, labels con colisión | Sin undo/redo, sin PDF vectorial, sin atlas |
| **QGIS** | C++/Python | 4.0 (2026) | GPL v2+ | Estándar industrial, 500+ tools, plugins, atlas | UI compleja, curva de aprendizaje, pesado |
| **ArcGIS Pro** | C++/.NET | 3.6 (2025) | Propietaria ($1500+/año) | Mejor análisis, 3D, enterprise, Python | Caro, solo Windows, vendor lock-in |
| **gvSIG** | Java | 2.6 (2019) | GPL | Multilingüe, 3D, Sextante, mobile | Desarrollo lento, UI envejecida |
| **OpenJUMP** | Java | 2.4.0 (2025) | GPL v2 | Ligero, digitización rápida, JTS, topology | Comunidad pequeña, raster básico |
| **Kosmo Desktop** | Java | 2.0 (2011) | GPL | UI amigable, Sextante, múltiples formatos | Abandonado desde 2011 |
| **SAGA GIS** | C++/wxWidgets | 9.5.1 (2024) | GPL | 700+ módulos, terrain analysis, ligero | UI no estándar, cartografía pobre |
| **GRASS GIS** | C/C++/Python | 8.5.0 (2026) | GPL | 800+ módulos, temporal, científico | Curva de aprendizaje, CLI-first |
| **uDig** | Java/Eclipse | 2.0 (2018) | EPL + BSD | Eclipse plugins, OGC, WMS | Descontinuado desde 2018 |
| **ILWIS** | C++ | 3.8.6 (2020) | GPL v2 | Raster+vector integrado, remote sensing | Solo Windows, inactivo desde 2020 |
| **MapWindow** | C#/C++ | v5 (2020) | MIT | .NET, plugins, HydroDesktop | Solo Windows, comunidad pequeña |

---

## Comparación Detallada por Categoría

### 1. Formatos de datos

| Formato | CATGIS | QGIS | ArcGIS | gvSIG | OpenJUMP |
|---------|--------|------|--------|-------|----------|
| SHP | ✅ | ✅ | ✅ | ✅ | ✅ |
| GeoJSON | ✅ | ✅ | ✅ | ✅ | ✅ |
| GeoPackage | ✅ | ✅ | ✅ | ✅ | ❌ |
| PostGIS | ✅ R/W | ✅ R/W | ✅ R/W | ✅ R/W | ✅ R |
| WMS | ✅ | ✅ | ✅ | ✅ | ✅ |
| WFS | ✅ | ✅ | ✅ | ✅ | ❌ |
| DXF | ✅ | ✅ | ✅ | ✅ | ✅ |
| DWG | ⚠️ externo | ✅ | ✅ | ✅ | ❌ |
| GPX | ✅ | ✅ | ✅ | ✅ | ❌ |
| KML | ✅ | ✅ | ✅ | ✅ | ❌ |
| Raster (GeoTIFF) | ✅ | ✅ | ✅ | ✅ | ✅ |
| CSV/Excel | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Score** | **7/12** | **12/12** | **12/12** | **10/12** | **5/12** |

### 2. Edición vectorial

| Función | CATGIS | QGIS | ArcGIS | gvSIG | OpenJUMP |
|---------|--------|------|--------|-------|----------|
| Crear puntos | ✅ | ✅ | ✅ | ✅ | ✅ |
| Crear líneas | ✅ | ✅ | ✅ | ✅ | ✅ |
| Crear polígonos | ✅ | ✅ | ✅ | ✅ | ✅ |
| Editar vértices | ✅ | ✅ | ✅ | ✅ | ✅ |
| Undo/Redo | ❌ | ✅ | ✅ | ✅ | ✅ |
| Snap | ✅ | ✅ | ✅ | ✅ | ✅ |
| Medición | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Score** | **5/7** | **7/7** | **7/7** | **7/7** | **7/7** |

### 3. Simbología y labeling

| Función | CATGIS | QGIS | ArcGIS | gvSIG | OpenJUMP |
|---------|--------|------|--------|-------|----------|
| Estilos de puntos | ✅ 28 | ✅ 50+ | ✅ 100+ | ✅ 20+ | ✅ 10+ |
| Estilos de líneas | ✅ 18 | ✅ 30+ | ✅ 50+ | ✅ 15+ | ✅ 8+ |
| Estilos de polígonos | ✅ 18 | ✅ 30+ | ✅ 50+ | ✅ 15+ | ✅ 5+ |
| Categorías | ✅ | ✅ | ✅ | ✅ | ✅ |
| Labels con colisión | ✅ básico | ✅ avanzado | ✅ Maplex | ❌ | ❌ |
| Placement modes | ✅ 10 | ✅ 8+ | ✅ 10+ | ❌ | ❌ |
| **Score** | **5/6** | **6/6** | **6/6** | **3/6** | **2/6** |

### 4. Layout/Cartografía

| Función | CATGIS | QGIS | ArcGIS | gvSIG | OpenJUMP |
|---------|--------|------|--------|-------|----------|
| Map frame independiente | ✅ básico | ✅ completo | ✅ completo | ❌ | ❌ |
| 28 plantillas curadas | ✅ | ❌ manual | ❌ manual | ❌ | ❌ |
| Export PDF | ✅ raster | ✅ vectorial | ✅ vectorial | ✅ | ❌ |
| Export imagen | ✅ | ✅ | ✅ | ✅ | ✅ |
| Atlas/map series | ❌ | ✅ | ✅ | ❌ | ❌ |
| Leyenda | ✅ | ✅ | ✅ | ✅ | ❌ |
| Escala gráfica | ✅ | ✅ | ✅ | ✅ | ❌ |
| Norte | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Score** | **5/8** | **7/8** | **8/8** | **3/8** | **1/8** |

### 5. Análisis espacial

| Función | CATGIS | QGIS | ArcGIS | SAGA | GRASS |
|---------|--------|------|--------|------|-------|
| Buffer | ✅ | ✅ | ✅ | ✅ | ✅ |
| Clip/Intersect | ✅ | ✅ | ✅ | ✅ | ✅ |
| Merge/Disjoin | ✅ | ✅ | ✅ | ✅ | ✅ |
| Spatial Join | ✅ | ✅ | ✅ | ✅ | ✅ |
| Topology validation | ✅ básico | ✅ | ✅ | ✅ | ✅ |
| Terrain analysis | ✅ | ✅ | ✅ | ✅ | ✅ |
| Hydrology | ✅ básico | ✅ | ✅ | ✅ | ✅ |
| 700+ módulos | ❌ | ✅ 500+ | ✅ 1000+ | ✅ 700+ | ✅ 800+ |
| **Score** | **5/8** | **8/8** | **8/8** | **8/8** | **8/8** |

### 6. UX y usabilidad

| Función | CATGIS | QGIS | ArcGIS | gvSIG | OpenJUMP |
|---------|--------|------|--------|-------|----------|
| UI moderna | ✅ FlatLaf | ✅ | ✅ | ⚠️ | ⚠️ |
| Spanish-first | ✅ | ⚠️ | ❌ | ✅ | ❌ |
| Toolbar con labels | ✅ | ❌ | ✅ | ❌ | ❌ |
| Drag & drop capas | ✅ | ✅ | ✅ | ✅ | ✅ |
| Context menus | ✅ | ✅ | ✅ | ✅ | ✅ |
| Recent files | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Score** | **5/6** | **5/6** | **5/6** | **4/6** | **2/6** |

### 7. Rendimiento

| Escenario | CATGIS | QGIS | ArcGIS | OpenJUMP |
|-----------|--------|------|--------|----------|
| 10 capas vectoriales | ✅ rápido | ✅ | ✅ | ✅ rápido |
| 1000 features | ✅ | ✅ | ✅ | ✅ |
| 10000 features | ⚠️ lento | ✅ | ✅ | ✅ |
| Raster grande | ⚠️ | ✅ | ✅ | ⚠️ |
| **Score** | **3/4** | **4/4** | **4/4** | **3/4** |

---

## Puntuación Total

| Programa | Formatos | Edición | Symb/Labels | Layout | Análisis | UX | Rendimiento | **TOTAL** |
|----------|----------|---------|-------------|--------|----------|-----|-------------|-----------|
| **CATGIS** | 7/12 | 5/7 | 5/6 | 5/8 | 5/8 | 5/6 | 3/4 | **35/51 (69%)** |
| **QGIS** | 12/12 | 7/7 | 6/6 | 7/8 | 8/8 | 5/6 | 4/4 | **49/51 (96%)** |
| **ArcGIS** | 12/12 | 7/7 | 6/6 | 8/8 | 8/8 | 5/6 | 4/4 | **50/51 (98%)** |
| **gvSIG** | 10/12 | 7/7 | 3/6 | 3/8 | 5/8 | 4/6 | 3/4 | **37/51 (73%)** |
| **OpenJUMP** | 5/12 | 7/7 | 2/6 | 1/8 | 5/8 | 2/6 | 3/4 | **28/51 (55%)** |
| **SAGA** | 4/12 | 2/7 | 2/6 | 1/8 | 8/8 | 2/6 | 4/4 | **23/51 (45%)** |
| **GRASS** | 4/12 | 3/7 | 2/6 | 1/8 | 8/8 | 2/6 | 4/4 | **24/51 (47%)** |

---

## Posicionamiento de CATGIS

```
QGIS:      ████████████████████ 96%  (Estándar industrial)
ArcGIS:    ████████████████████ 98%  (Líder enterprise)
gvSIG:     ██████████████░░░░░░ 73%  (Similar a CATGIS)
CATGIS:    █████████████░░░░░░░ 69%  (Enfoque vertical)
OpenJUMP:  ███████████░░░░░░░░░ 55%  (Ligero, digitización)
SAGA:      █████████░░░░░░░░░░░ 45%  (Análisis raster)
GRASS:     █████████░░░░░░░░░░░ 47%  (Análisis científico)
```

---

## Ventaja Competitiva de CATGIS

**NO compite en funcionalidad bruta contra QGIS/ArcGIS.**

**SÍ compite en:**

1. **Verticalidad ambiental**: Plantillas curadas para EIA, monitoreo, riesgo
2. **Velocidad de resultado**: Un botón resuelve problemas complejos
3. **Mercado Latam**: UI en español, contexto local
4. **PostGIS integrado**: Read/write sin configuración
5. **Plantillas listas**: 28 plantillas vs 0 en QGIS/ArcGIS

---

## Lo que CATGIS necesita para ser competitivo

### Corto plazo (1-3 meses)
1. ✅~~Motor de colisión de etiquetas~~ (HECHO)
2. ~~Export PDF vectorial~~ (PENDIENTE)
3. ~~Atlas/map series~~ (PENDIENTE)
4. ~~Undo/Redo en atributos~~ (PENDIENTE)

### Mediano plazo (3-6 meses)
5. ~~Leyenda por frame~~ (PENDIENTE)
6. ~~Scale bar enlazada~~ (PENDIENTE)
7. ~~Spatial bookmarks~~ (PENDIENTE)
8. ~~Query wizard~~ (PENDIENTE)

### Largo plazo (6-12 meses)
9. ~~Plugin system~~ (PENDIENTE)
10. ~~Python scripting~~ (PENDIENTE)
11. ~~Topology rules avanzadas~~ (PENDIENTE)

---

## Conclusión

CATGIS está en el **69%** de funcionalidad vs QGIS (96%) y ArcGIS (98%).

**Pero no necesita llegar al 100%.** Necesita llegar al **80%** en su vertical ambiental/topográfica para ser la primera elección de consultores en Latam.

Con las mejoras pendientes (PDF vectorial, atlas, undo/redo), CATGIS alcanza el **75-80%** y se posiciona como:
> "El GIS más rápido para informes ambientales en Argentina y Latam"
