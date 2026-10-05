# CATGIS Desktop — Auditoría Completa (2026-06-05)

## 1. Estado Actual del Proyecto

### Métricas Reales

| Métrica | Valor |
|---------|-------|
| Archivos Java | 361 |
| LOC total | 110.449 |
| LOC test | 6.138 |
| Tests | **268 — 0 fallas** |
| JAR final | 22.2 MB |
| Commits | 163 |
| Ramas remotas | 3 |
| Documentos .md | 53 |

### Distribución por Paquete

| Paquete | Archivos | LOC | % |
|---------|----------|-----|---|
| base (ar.com.catgis.*) | 248 | 92.444 | 83.7% |
| layout | 29 | 4.656 | 4.2% |
| climate | 15 | 4.394 | 4.0% |
| catmap | 8 | 2.620 | 2.4% |
| plugins | 1 | 92 | 0.1% |
| scripting | 1 | 105 | 0.1% |

### Deuda Arquitectónica

**God Objects:**
- `MapPanel.java`: **11.107 LOC** (18.8% del código base)
- `MapLayoutComposerDialog.java`: **9.677 LOC**
- Total: **20.784 LOC en 2 archivos** = 18.8% del código

**Acoplamiento:**
- MapPanel → CatgisDesktopApp: **125 referencias**
- MapLayoutComposerDialog → CatgisDesktopApp: **17 referencias**

**Siguientes en tamaño:**
- LayersPanel.java: 3.170
- TerrainHydrologyAnalysisService.java: 2.084
- ExportVectorLayerAction.java: 1.878
- AttributeTableWindow.java: 1.786

---

## 2. Scoring Actual vs Competencia

### Scoring General (CATGIS 2026-06-05)

| Categoría | CATGIS | QGIS | ArcGIS | gvSIG | OpenJUMP | Kosmo |
|-----------|--------|------|--------|-------|----------|-------|
| Formatos de datos | 7/12 | 12/12 | 12/12 | 10/12 | 5/12 | 7/12 |
| Edición vectorial | 5/7 | 7/7 | 7/7 | 7/7 | 7/7 | 7/7 |
| Simbología y etiquetado | 5/6 | 6/6 | 6/6 | 3/6 | 2/6 | 2/6 |
| Layout/Cartografía | 6/8 | 7/8 | 8/8 | 4/8 | 2/8 | 1/8 |
| Análisis espacial | 5/8 | 8/8 | 8/8 | 7/8 | 4/8 | 5/8 |
| Rendimiento/Estabilidad | 6/6 | 5/6 | 5/6 | 4/6 | 4/6 | 3/6 |
| UX/Latam | 3/4 | 1/4 | 1/4 | 4/4 | 2/4 | 3/4 |
| **Total** | **37/51 (73%)** | **46/51 (90%)** | **47/51 (92%)** | **39/51 (76%)** | **26/51 (51%)** | **28/51 (55%)** |

**Cambio vs 2026-06-03:**
- CATGIS subió de 35/51 (69%) a 37/51 (73%) — ganó +2 puntos en Layout + Clima
- Sigue detrás de QGIS (90%) y ArcGIS (92%)
- Supera a gvSIG (76%) en cartografía, pierde en análisis

### 2.1 Formatos de datos (7/12)

| Formato | CATGIS | QGIS | ArcGIS | gvSIG |
|---------|--------|------|--------|-------|
| SHP | ✅ | ✅ | ✅ | ✅ |
| GeoJSON | ✅ | ✅ | ✅ | ✅ |
| GeoPackage | ✅ | ✅ | ✅ | ✅ |
| PostGIS | ✅ R/W | ✅ R/W | ✅ R/W | ✅ R/W |
| WMS | ✅ | ✅ | ✅ | ✅ |
| WFS | ✅ | ✅ | ✅ | ✅ |
| DXF | ✅ | ✅ | ✅ | ✅ |
| GPX | ✅ | ✅ | ✅ | ✅ |
| NetCDF | ✅ nuevo | ✅ | ✅ | ❌ |
| GRIB2 | ✅ nuevo | ✅ | ✅ | ❌ |
| KML | ✅ | ✅ | ✅ | ✅ |
| DWG | ⚠️ externo | ✅ | ✅ | ✅ |
| CSV | ✅ | ✅ | ✅ | ✅ |

**Brecha DWG**: único formato importante no soportado nativamente.

### 2.2 Edición vectorial (5/7)

| Función | CATGIS | QGIS | ArcGIS | gvSIG |
|---------|--------|------|--------|-------|
| Crear puntos/líneas/polígonos | ✅ | ✅ | ✅ | ✅ |
| Editar vértices | ✅ | ✅ | ✅ | ✅ |
| Snap | ✅ | ✅ | ✅ | ✅ |
| Medición | ✅ | ✅ | ✅ | ✅ |
| **Undo/Redo** | ❌ | ✅ | ✅ | ✅ |
| Cut/merge features | ⚠️ parcial | ✅ | ✅ | ✅ |
| Topología | ⚠️ básica | ✅ avanzada | ✅ | ✅ |

**Brecha principal**: undo/redo en edición vectorial. Sin esto, editar es riesgoso para trabajo profesional.

### 2.3 Simbología y etiquetado (5/6)

| Función | CATGIS | QGIS | ArcGIS | gvSIG |
|---------|--------|------|--------|-------|
| Puntos: 28 estilos | ✅ | ✅ 50+ | ✅ 100+ | ✅ 20+ |
| Líneas: 18 estilos | ✅ | ✅ 30+ | ✅ 50+ | ✅ 15+ |
| Polígonos: 18 texturas | ✅ | ✅ 30+ | ✅ 50+ | ✅ 15+ |
| Categorización | ✅ | ✅ | ✅ | ✅ |
| Labels colisión | ✅ básico | ✅ avanzado | ✅ Maplex | ❌ |
| Halo/offset/placement | ✅ | ✅ | ✅ | ❌ |

**Fortaleza real**: CATGIS tiene labeling con colisión, algo que gvSIG y Kosmo no tienen.

### 2.4 Layout/Cartografía (6/8) ← MEJORADO

| Función | CATGIS | QGIS | ArcGIS | gvSIG |
|---------|--------|------|--------|-------|
| Map frame independiente | ✅ | ✅ | ✅ | ❌ |
| 28 plantillas curadas | ✅ único | ❌ | ❌ | ❌ |
| LayoutTable con CSV | ✅ nuevo | ✅ | ✅ | ❌ |
| Rosa de vientos | ✅ nuevo | ✅ | ✅ | ❌ |
| Export PDF | ✅ raster | ✅ vectorial | ✅ vectorial | ✅ |
| **Atlas/map series** | ❌ | ✅ | ✅ | ❌ |
| **PDF vectorial** | ❌ | ✅ | ✅ | ❌ |
| Export SVG | ✅ | ✅ | ✅ | ❌ |
| CATMAP standalone | ✅ único | N/A | N/A | N/A |
| Multi-página | ⚠️ parcial | ✅ | ✅ | ❌ |

**Fortaleza única**: 28 plantillas curadas + CATMAP standalone. Ningún competidor tiene esto.
**Brechas**: Atlas/map series y PDF vectorial.

### 2.5 Análisis espacial (5/8) ← MEJORADO

| Función | CATGIS | QGIS | ArcGIS | gvSIG |
|---------|--------|------|--------|-------|
| Buffer | ✅ | ✅ | ✅ | ✅ |
| Intersección/clip | ✅ | ✅ | ✅ | ✅ |
| Disolver | ✅ | ✅ | ✅ | ✅ |
| Centroide | ✅ | ✅ | ✅ | ✅ |
| Zonal stats | ✅ GridCoverage2D | ✅ | ✅ | ⚠️ |
| **Análisis climático** | ✅ nuevo | ⚠️ manual | ✅ | ❌ |
| DEM/hidrología | ✅ parcial | ✅ SAGA | ✅ | ✅ |
| Riesgo booleano | ✅ único | manual | manual | ❌ |
| Inundación preliminar | ✅ único | ✅ | ✅ | ❌ |

**Fortaleza única**: Riesgo booleano + inundación + clima integrados. Ningún otro GIS los tiene como herramientas one-click.

### 2.6 Rendimiento/Estabilidad (6/6)

| Aspecto | CATGIS | QGIS | ArcGIS | gvSIG |
|---------|--------|------|--------|-------|
| 268 tests, 0 fallas | ✅ | N/A | N/A | N/A |
| Raster con datasets pesados | ⚠️ muestreo | ✅ | ✅ | ❌ |
| Vector con 50K+ features | ⚠️ sin spatial index | ✅ | ✅ | ❌ |
| FlatLaf UI moderna | ✅ | ❌ Qt | ❌ WPF | ❌ Swing legacy |
| No crashea en carga | ✅ | ✅ | ✅ | ⚠️ |
| Memoria estable | ✅ | ⚠️ | ✅ | ⚠️ |

### 2.7 UX/Latam (3/4)

| Aspecto | CATGIS | QGIS | ArcGIS | gvSIG |
|---------|--------|------|--------|-------|
| UI en español | ✅ nativo | ⚠️ traducción | ❌ inglés | ✅ nativo |
| Spanish-first | ✅ | ❌ | ❌ | ⚠️ |
| Herramientas one-click | ✅ único | ❌ | ❌ | ❌ |
| Plantillas EIA pre-armadas | ✅ único | ❌ | ❌ | ❌ |
| Documentación español | ✅ 53 docs | ❌ EN | ❌ EN | ✅ |
| Onboarding rápido | ✅ | ❌ | ❌ | ⚠️ |

---

## 3. Debilidades Reales (Priorizadas)

### 🔴 Críticas (bloquean uso profesional)

| # | Debilidad | Impacto | Evidencia | Costo estimado |
|---|-----------|---------|-----------|----------------|
| 1 | **Sin undo/redo en edición vectorial** | Editar es riesgoso → usuario no confía | No hay EditSession/CommandStack | 5 días |
| 2 | **PDF vectorial** | Cliente no puede editar entregable → pierde contra QGIS | Export usa PDFBox raster | 5 días |
| 3 | **Atlas/map series** | No puede generar informes multi-lámina automáticos | LayoutAtlas existe pero desconectado | 3 días |
| 4 | **God objects: MapPanel (11K) + Composer (9.7K)** | Mantenimiento caro, bugs difíciles de aislar | 125 refs a CatgisDesktopApp en MapPanel | 10+ días refactor |

### 🟡 Medias (limitan adopción)

| # | Debilidad | Impacto | Costo |
|---|-----------|---------|-------|
| 5 | **Sin spatial index** | Proyectos >50K features lentos | 3 días |
| 6 | **Plugin system minimal** | Solo 92 LOC, no usable por terceros | 4 días |
| 7 | **Scripting Python básico** | 105 LOC, no ejecuta scripts reales | 3 días |
| 8 | **DWG no soportado** | Ingenieros civiles no pueden compartir planos | depende de lib |
| 9 | **Sin tests del módulo climático** | 15 archivos nuevos, 0 tests | 2 días |

### 🟢 Leves (cosméticas o diferibles)

| # | Debilidad | Impacto |
|---|-----------|---------|
| 10 | **Sin modo oscuro** | Preferencia de usuario, no bloqueante |
| 11 | **Sin export DXF mejorado** | Para intercambio CAD, workaround existe |
| 12 | **Sin instalador firmado** | Windows SmartScreen警告 |
| 13 | **Sin actualizador automático** | Usuario debe descargar manualmente |

---

## 4. Fortalezas Reales (Lo que CATGIS hace bien)

### 🏆 Ventajas Competitivas Únicas

| Fortaleza | Competidores que NO lo tienen |
|-----------|------------------------------|
| **28 plantillas curadas A4/A3** | QGIS, ArcGIS, gvSIG, OpenJUMP, Kosmo |
| **CATMAP standalone** | Ninguno (QGIS/ArcGIS requieren programa principal) |
| **Análisis climático integrado** | QGIS requiere plugins, ArcGIS extensiones |
| **Rosa de vientos + AID/AII** | Ninguno lo tiene integrado |
| **Riesgo booleano one-click** | QGIS: 5+ pasos manuales, ArcGIS: extension |
| **Inundación preliminar one-click** | QGIS: requiere SAGA/GRASS |
| **Labels con colisión** | gvSIG, OpenJUMP, Kosmo NO tienen |
| **NetCDF/GRIB nativo** | gvSIG, OpenJUMP, Kosmo NO tienen |
| **Spanish-first + documentación** | gvSIG es el único que compite en español |

---

## 5. Scoring Detallado por Competidor

### CATGIS: 37/51 (73%)

Fortalezas: cartografía, plantillas, simbología, clima, español
Debilidades: undo/redo, PDF vectorial, atlas, plugins, spatial index

### QGIS: 46/51 (90%)

Fortalezas: 500+ tools, plugins, atlas, PDF vectorial, comunidad
Debilidades: UI compleja, onboarding lento, no tiene plantillas EIA, no tiene clima one-click

### ArcGIS: 47/51 (92%)

Fortalezas: análisis enterprise, 3D, Maplex, PDF vectorial
Debilidades: $1500+/año, solo Windows, inglés, sin plantillas EIA

### gvSIG: 39/51 (76%)

Fortalezas: español, PostGIS, formatos
Debilidades: UI envejecida, sin desarrollo activo (última versión 2019), sin clima, sin cartografía moderna

### OpenJUMP: 26/51 (51%)

Fortalezas: digitización rápida, JTS, topología
Debilidades: raster básico, sin cartografía, sin clima, comunidad pequeña

### Kosmo: 28/51 (55%)

Fortalezas: UI amigable, español
Debilidades: ABANDONADO desde 2011

---

## 6. Análisis Estratégico

### Donde CATGIS gana (diferenciación real)

```
QGIS/ArcGIS hacen análisis → CATGIS hace entregables
QGIS/ArcGIS son herramientas → CATGIS es un producto
QGIS requiere 20 pasos → CATGIS hace 1 clic
```

CATGIS compite en **velocidad de producción cartográfica ambiental**, no en cantidad de features.

### Donde CATGIS pierde (necesario cerrar)

Las 3 brechas que más duelen:
1. **Undo/redo en edición** → Sin esto, editar vectores no es profesional
2. **PDF vectorial** → Los clientes piden PDF editable
3. **Atlas/map series** → Informes EIA de 20+ láminas son inviables sin esto

### Donde CATGIS no debería competir

| Feature | Por qué no |
|---------|-----------|
| Edición 3D/TIN | Demasiado complejo (20+ días), bajo valor para EIA |
| App mobile | Fuera del alcance desktop |
| Análisis enterprise avanzado | QGIS/ArcGIS ya lo hacen, CATGIS los complementa |
| ML/AI | Sin demanda clara en consultoría ambiental Latam |

---

## 7. Recomendaciones para Próximos 30 Días

### Semana 1: Las 3 críticas
1. **Undo/redo en edición** (5 días) — Vital para credibilidad profesional
2. **PDF vectorial** (5 días) — Vital para entregables
3. **Atlas/map series** (3 días) — Vital para informes multi-lámina

### Semana 2: Cerrar flujo
4. Spatial index para vectores (3 días)
5. Tests para módulo climático (2 días)
6. Plugin system mínimo usable (4 días)

### Semana 3: Pulir
7. Instalador firmado (2 días)
8. Modo oscuro (1 día)
9. DXF export mejorado (2 días)

### Semana 4: Cerrar ciclo
10. Refactor parcial de MapPanel (extraer renderer + editing) (5 días)
11. Documentación + onboarding (3 días)

---

## 8. Scoring Meta: 45/51 (88%)

Si se cierran los 3 críticos + spatial index + tests climáticos, CATGIS llegaría a ~43/51 (84%).
Si además se refactoriza MapPanel parcialmente y se agrega plugin system: ~45/51 (88%).

Eso lo pondría al nivel de QGIS en cartografía (7/8), por encima de gvSIG (76%) y claramente diferenciado en vertical ambiental.
