# CATGIS — Roadmap Tecnico para Competencia Vertical Ambiental/Topografica

**Objetivo**: En 18 meses, CATGIS debe ser la primera eleccion para consultores ambientales, topografos y tecnicos en Argentina/Latam que necesitan mapas profesionales sin la complejidad de QGIS/ArcGIS.

**Estrategia**: No competir en funcionalidad bruta. Competir en **velocidad de resultado** para casos de uso ambientales/topograficos.

---

## FASE 1 — Estabilizacion y deuda critica (Mes 1-3)

### 1.1 Fix de bugs bloqueantes
- [ ] Resolver los 3 tests CRS round-trip fallidos (EPSG:22182 vs 3857)
- [ ] Verificar que GDAL native libraries carguen correctamente
- [ ] Auditar y cerrar los 5-10 bugs mas reportados

### 1.2 Undo/Redo en edicion vectorial
**Por que**: Sin undo, no hay edicion profesional real. Cualquier toque accidental rompe el trabajo.
- [ ] Implementar `EditCommand` pattern (AddFeature, DeleteFeature, ModifyGeometry, ModifyAttribute)
- [ ] Pila de undo/redo en `MapPanel`
- [ ] Ctrl+Z / Ctrl+Y funcionales
- [ ] Undo en edicion de vertices

**Esfuerzo**: 2-3 semanas
**Impacto**: ALTO — sin esto, nadie usa la edicion en serio

### 1.3 Motor de colision de etiquetas (basico)
**Por que**: Etiquetas superpuestas = mapa poco profesional. Esto es lo primero que un cartografo nota.
- [ ] Bounding box check por etiqueta
- [ ] Desplazamiento automatico (4 posiciones: centro, arriba, abajo, derecha)
- [ ] Prioridad de etiquetas (campo `labelPriority`)
- [ ] Labels visibles en zoom adecuado (ya tienes minScale/maxScale)

**Esfuerzo**: 2-3 semanas
**Impacto**: ALTO — diferencia visual inmediata

### 1.4 Calidad de vida del build
- [ ] GitHub Actions CI: compile + test en cada push
- [ ] Eliminar los 3 tests CRS fallidos o arreglarlos
- [ ] Cobertura de tests minima: 25% global

**Esfuerzo**: 1 semana
**Impacto**: MEDIO — previene regresiones

---

## FASE 2 — CATMAP profesional (Mes 3-8)

### 2.1 Map frame vectorial real
**Por que**: Este es EL gap mas grande vs QGIS/ArcMap. Un layout sin map frame independiente no es un compositor serio.
- [ ] `LayoutMap` renderiza directamente desde datos geoespaciales (no desde captura del MapPanel)
- [ ] Cada `LayoutMap` tiene su propio extent, escala y CRS
- [ ] Zoom/pan independiente por frame
- [ ] Refresco automatico al cambiar datos

**Esfuerzo**: 6-8 semanas
**Impacto**: MUY ALTO — esto convierte CATMAP en un compositor real

### 2.2 Propiedades contextuales completas en CATMAP
- [ ] Doble clic en cualquier elemento abre popup completo
- [ ] Todos los elementos editables desde popup (ya tienes text, legend, scale, north, cartouche)
- [ ] Snap a elementos existentes
- [ ] Alineacion y distribucion automatica

**Esfuerzo**: 3-4 semanas
**Impacto**: ALTO — flujo de trabajo profesional

### 2.3 Plantillas perfectas
**Por que**: 28 plantillas ya existen. Ahora hay que pulir cada una.
- [ ] Auditar visualmente las 28 plantillas curadas
- [ ] Ajustar margenes, tamanios de fuente, equilibrio visual
- [ ] Eliminar plantillas flojas
- [ ] Agregar 5-8 plantillas nuevas: Perfil topografico, Corte transversal, Plano de localizacion, Plano de emplazamiento, Mapa de riesgo
- [ ] Preview real en el selector (no placeholder gris)

**Esfuerzo**: 3-4 semanas
**Impacto**: ALTO — el usuario ve calidad inmediata

### 2.4 Export profesional
- [ ] PDF vectorial (texto y lineas vectoriales, no raster)
- [ ] PDF geoespacial (coordenadas embebidas)
- [ ] PNG/SVG a resolucion configurable
- [ ] Impresion directa con escala correcta

**Esfuerzo**: 3-4 semanas
**Impacto**: ALTO — el output final es lo que el cliente ve

### 2.5 Atlas / Map Series
**Por que**: Un consultor ambiental necesita generar 50 mapas del mismo proyecto (uno por punto de muestreo, uno por parcela, etc.). Hacerlo uno por uno es inaceptable.
- [ ] Campo de agrupacion (por feature o por valor de atributo)
- [ ] Generacion automatica de paginas
- [ ] Numeracion y titulo automatico
- [ ] Export batch a PDF

**Esfuerzo**: 4-5 semanas
**Impacto**: MUY ALTO — ahorra horas de trabajo repetitivo

---

## FASE 3 — Herramientas ambientales/topograficas (Mes 6-12)

### 3.1 Geoprocessing toolbox basico
**Por que**: QGIS tiene 500+ herramientas. Tu necesitas 15-20 que sean EXCELENTES para tu caso de uso, no 500 mediocres.

**Herramientas prioritarias (por orden de impacto):**

#### Hidrologia
1. [ ] **Extraccion de drenaje** (ya existe `DrainageExtractionService`) — pulir y documentar
2. [ ] **Cuencas a partir de punto de vertimiento** (ya existe `BasinFromOutletDialog`) — pulir
3. [ ] **Perfil topografico** (ya existe `TopographicProfileService`) — pulir
4. [ ] **Curvas de nivel** (ya existe `ContourGenerationService`) — pulir
5. [ ] **Zoneado de inundacion** (ya existe `FloodScenarioService`) — pulir

#### Riesgo
6. [ ] **Riesgo booleano** (ya existe `BooleanRiskService`) — pulir
7. [ ] **Clip raster con poligono** (ya existe `DemClipService`) — pulir

#### Muestreo
8. [ ] **Descarga SoilGrids** (ya existe `SoilGridsDownloadService`) — pulir
9. [ ] **Descarga DEM publico** (ya existe `PublicTerrainTilesDemService`) — pulir
10. [ ] **OpenTopography** (ya existe `OpenTopographyDemService`) — pulir

#### Cartografia
11. [ ] **Generacion de mapa de puntos de muestreo** (nueva)
12. [ ] **Generacion de mapa de parcelas** (nueva)
13. [ ] **Generacion de corte transversal** (nueva)
14. [ ] **Calculadora de campos** (ya existe `FieldCalculatorDialog`) — pulir
15. [ ] **Export a PostGIS batch** (nueva)

#### Utilidades
16. [ ] **Reproyeccion de capa** (ya existe `ExportReprojectedLayerDialog`) — pulir
17. [ ] **Conversion de formato** (SHP a GeoJSON, GeoJSON a GeoPackage, etc.)
18. [ ] **Merge de capas** (nueva)
19. [ ] **Buffer** (nueva)
20. [ ] **Interseccion/Clip** (nueva)

**Esfuerzo**: 10-12 semanas (pulir existentes + crear nuevas)
**Impacto**: MUY ALTO — esto es lo que hace que un consultor elija CATGIS

### 3.2 Wizard de proyecto ambiental
**Por que**: Un consultor ambiental no quiere configurar un GIS. Quiere abrir, cargar datos, y generar su mapa.
- [ ] Wizard paso a paso: "Nuevo proyecto ambiental"
- [ ] Seleccion de tipo de estudio (EIA, Monitoreo, Riesgo, etc.)
- [ ] Configuracion automatica de CRS, escala, plantilla
- [ ] Carga guiada de datos (capas base, puntos de muestreo, limites)
- [ ] Generacion automatica de mapa preliminary

**Esfuerzo**: 3-4 semanas
**Impacto**: ALTO — reduce tiempo de setup de 30 min a 5 min

### 3.3 Reporte automatico
- [ ] Generar PDF con mapa + tabla de atributos + metadatos
- [ ] Formato configurable (portrait/landscape, A4/A3)
- [ ] Incluir leyenda, escala, norte, cartucho con datos del proyecto
- [ ] Export batch (un PDF por feature seleccionada)

**Esfuerzo**: 3-4 semanas
**Impacto**: ALTO — el output final es el deliverable del consultor

---

## FASE 4 — Diferenciacion y polish (Mes 12-18)

### 4.1 Integracion PostGIS avanzada
- [ ] Browser de esquemas/tables mejorado
- [ ] Edicion directa en tabla PostGIS
- [ ] Write batch (insertar 1000 features en una transaccion)
- [ ] Espaciales queries desde la interfaz
- [ ] Connection pooling robusto

**Esfuerzo**: 4-5 semanas
**Impacto**: ALTO — para equipos que trabajan con datos compartidos

### 4.2 Python scripting (basico)
**Por que**: Los usuarios avanzados quieren automatizar. No necesitas un Python completo, necesitas:
- [ ] Console de scripts integrada
- [ ] Acceso a capas, proyecto, mapa
- [ ] Scripts guardables y reutilizables
- [ ] 10-15 scripts de ejemplo (uno por herramienta ambiental)

**Esfuerzo**: 4-5 semanas
**Impacto**: MEDIO-ALTO — retiene usuarios avanzados

### 4.3 Templates de proyecto
- [ ] Guardar configuracion completa como template
- [ ] Compartir templates entre usuarios
- [ ] Template por defecto para cada tipo de estudio

**Esfuerzo**: 2 semanas
**Impacto**: MEDIO

### 4.4 Mejoras de UX finales
- [ ] Keyboard shortcuts para todo
- [ ] Command palette (Ctrl+Shift+P)
- [ ] Drag and drop de capas mejorado
- [ ] Multi-monitor support
- [ ] Temas de color (claro/oscuro)

**Esfuerzo**: 3-4 semanas
**Impacto**: MEDIO — polish profesional

---

## Metricas de exito por fase

### Fase 1 (Mes 3)
- [ ] 0 tests fallidos
- [ ] Undo/Redo funcional
- [ ] Etiquetas sin superposicion basica
- [ ] CI/CD funcionando

### Fase 2 (Mes 8)
- [ ] CATMAP con map frame vectorial
- [ ] Export PDF vectorial
- [ ] 28 plantillas auditadas y pulidas
- [ ] Atlas funcional (generacion batch)

### Fase 3 (Mes 12)
- [ ] 20 herramientas de geoprocessing funcionales
- [ ] Wizard de proyecto ambiental
- [ ] Reporte automatico a PDF

### Fase 4 (Mes 18)
- [ ] PostGIS avanzado
- [ ] Python scripting basico
- [ ] 50+ usuarios activos (beta)
- [ ] 5+ consultorias ambientales usando CATGIS

---

## Stack tecnico recomendado para acelerar

| Area | Tecnologia actual | Mejora sugerida |
|------|------------------|-----------------|
| UI | Swing + FlatLaf | Mantener (funciona bien) |
| GIS core | GeoTools 34 | Mantener (maduro) |
| Raster | GDAL via GeoTools | Verificar nativo |
| Base de datos | PostGIS | Mantener |
| Layout | CATMAP custom | Refactorizar a map frame real |
| Build | Gradle | Mantener + GitHub Actions |
| Testing | JUnit 5 | Mantener + cobertura minima 25% |
| Packaging | jpackage + Inno Setup | Mantener |

---

## Presupuesto estimado de esfuerzo

| Fase | Semanas | Prioridad |
|------|---------|-----------|
| Fase 1 — Estabilizacion | 6-8 | CRITICA |
| Fase 2 — CATMAP profesional | 19-25 | ALTA |
| Fase 3 — Herramientas ambientales | 16-20 | ALTA |
| Fase 4 — Diferenciacion | 13-16 | MEDIA |
| **TOTAL** | **54-69 semanas** | — |

A ritmo de 1 fase por trimestre: **18 meses para competitividad vertical real.**

---

## Posicionamiento final

```
QGIS:     "Hago todo para todos"     → 10M usuarios
ArcGIS:   "Hago todo para enterprise" → 5M usuarios
CATGIS:   "Hago lo ambiental/topografico rapido" → 50K usuarios target
```

**No necesitas 10M usuarios.** Necesitas 50K consultores ambientales y topografos en Argentina y Latam que digan:
> "Para un EIA, uso CATGIS. Es mas rapido que QGIS y no necesito configurar 200 cosas."

Con 50K usuarios a $50/mes = **$2.5M ARR** = negocio sostenible.

---

## Proximo paso inmediato

**Esta semana**: 
1. Arreglar los 3 tests CRS fallidos
2. Definir el diseño del motor de colision de etiquetas
3. Priorizar las 5 herramientas ambientales mas usadas por tus clientes actuales

¿Quieres que empiece por algun punto especifico?
