# CATMAP Competitive Audit (CATGIS Desktop)

Fecha: 2026-05-31  
Repositorio auditado: `C:\CATGIS\catgis-desktop`  
Commit de referencia: `ba849ad`  
Alcance: **auditoría + comparación** (sin implementar cambios en este documento).

---

## 1) Resumen ejecutivo

CATMAP avanzó mucho respecto de builds anteriores (selección, z-order, export, leyenda más configurable), pero **todavía no está al nivel de un compositor GIS profesional** tipo QGIS Layout / ArcMap Layout View.

Estado actual:
- Madurez funcional: **media**.
- Madurez de UX profesional: **media-baja**.
- Confiabilidad para informes básicos ambientales: **sí, con límites**.
- Competencia real con QGIS/ArcGIS para layout avanzado: **todavía no**.

Problema estructural principal:
- CATMAP aún conserva una arquitectura híbrida (elementos del `layoutModel` + elementos fijos/render legacy), lo que sigue generando incoherencias de edición y expectativas de usuario.

---

## 2) Evidencia interna (código real auditado)

Archivos revisados:
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\MapLayoutComposerDialog.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutModel.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutElement.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutMap.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutLegend.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutScaleBar.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutNorthArrow.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutLabel.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutImage.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutRectangle.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutTemplateManager.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\QgisQptImporter.java`

Hallazgos técnicos verificables:
- `openElementProperties(...)` todavía muestra un cuadro informativo simple (no editor contextual rico).
- Insertar rectángulo/elipse/línea sigue pasando por `addCatmapItem(...)` + diálogo previo, no por “draw-first” directo en canvas.
- `drawLayoutModelOverlay(...)` omite `LayoutMap` cuando hay template (`templateHasMap = true`), lo que confirma arquitectura híbrida.
- Exportación (`exportImage/exportPdf/printLayout`) usa `renderLayout(...)` con composición mejorada, pero no todo está 100% unificado como en motores layout maduros.
- `LayoutMap` tiene soporte de grilla y escala objetivo (`gridByDistance`, `gridIntervalX/Y`, `targetScaleDenominator`), pero la UX de control no es aún equivalente a compositores líderes.
- `LayoutLegend` ya incluye columnas, opacidad, padding y exclusión parcial de basemap por nombre, pero aún le falta robustez de “legend item management” profesional.

---

## 3) Comparación funcional (CATMAP vs mercado)

Escala usada:
- ✅ = completo y maduro
- ◑ = parcial / funcional pero limitado
- ❌ = ausente

| Feature | CATMAP | QGIS Layout | ArcMap | ArcGIS Pro | gvSIG | OpenJUMP/Kosmo | uDig | MapWindow/DotSpatial | Prioridad CATMAP |
|---|---|---|---|---|---|---|---|---|---|
| Modelo de layout unificado | ◑ | ✅ | ✅ | ✅ | ◑ | ◑ | ◑ | ◑ | Crítico |
| Todo visible = item editable | ◑ | ✅ | ✅ | ✅ | ◑ | ◑ | ◑ | ◑ | Crítico |
| Selección/mover/resize confiable | ◑ | ✅ | ✅ | ✅ | ◑ | ◑ | ◑ | ◑ | Crítico |
| Z-order/bloqueo/visibilidad | ◑ | ✅ | ✅ | ✅ | ◑ | ◑ | ◑ | ◑ | Alto |
| Panel de items profesional | ◑ | ✅ | ✅ | ✅ | ◑ | ◑ | ◑ | ◑ | Alto |
| Map frame vivo (no snapshot) | ◑ | ✅ | ✅ | ✅ | ◑ | ◑ | ◑ | ◑ | Crítico |
| Pan/zoom interno de marco | ◑ | ✅ | ✅ | ✅ | ◑ | ❌ | ◑ | ◑ | Alto |
| Grilla cartográfica avanzada | ◑ | ✅ | ✅ | ✅ | ◑ | ❌ | ◑ | ◑ | Alto |
| Leyenda avanzada (items/grupos/orden) | ◑ | ✅ | ✅ | ✅ | ◑ | ❌ | ◑ | ◑ | Alto |
| Tipografía contextual rica (Word-like) | ❌ | ✅ | ✅ | ✅ | ◑ | ❌ | ❌ | ❌ | Alto |
| Escala gráfica robusta | ◑ | ✅ | ✅ | ✅ | ◑ | ❌ | ◑ | ◑ | Alto |
| Plantillas de layout | ◑ | ✅ | ✅ | ✅ | ◑ | ❌ | ❌ | ◑ | Medio |
| Importar plantillas QPT | ◑ | ✅ (nativo) | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Medio |
| Export PDF WYSIWYG confiable | ◑ | ✅ | ✅ | ✅ | ◑ | ◑ | ◑ | ◑ | Crítico |
| Export SVG profesional | ❌ | ✅ | ◑ | ✅ | ◑ | ❌ | ❌ | ❌ | Medio |
| Atlas / map series | ❌ | ✅ | ◑ | ✅ | ◑ | ❌ | ❌ | ❌ | Medio |
| Tablas dinámicas en layout | ◑ (`LayoutTable`) | ✅ | ✅ | ✅ | ◑ | ❌ | ◑ | ◑ | Alto |
| Geospatial PDF | ❌ | ◑ | ◑ | ✅ | ❌ | ❌ | ❌ | ❌ | Bajo |

---

## 4) Brecha (gap) priorizada

### Crítico
1. Cerrar arquitectura híbrida (legacy fijo + layoutModel) hacia una fuente única de verdad.
2. Garantizar que todo lo visible sea seleccionable/editable sin excepciones.
3. Convertir `LayoutMap` en marco de mapa plenamente operativo en edición.
4. Asegurar exportación 1:1 preview (WYSIWYG real) en PDF/imagen/impresión.

### Alto
1. Propiedades contextuales ricas (tipografía, color, fondo, borde, opacidad) para título/leyenda/textos.
2. Inserción de formas por dibujo directo (arrastrar en canvas) y edición posterior.
3. Leyenda con administración fina de ítems (orden manual, renombrar, agrupación simple, wrap estable).
4. Grid cartográfica con control real por intervalo/estilo/etiquetado.

### Medio
1. Importador QPT más robusto.
2. Mejoras del panel de elementos (íconos, estados, jerarquía clara, drag reordenado).
3. SVG export y atlas/map-series básico.

### Bajo
1. Geospatial PDF.
2. Pulido visual temático y presets avanzados.

---

## 5) Comparación honesta (preguntas directas)

### ¿CATMAP hoy puede competir con QGIS Layout?
**No.** Está por debajo en arquitectura de ítems, panel de propiedades, leyenda avanzada, map frame y export avanzado.

### ¿CATMAP hoy puede competir con ArcMap Layout View?
**No todavía.** Puede resolver mapas técnicos básicos, pero faltan controles de layout de nivel producción.

### ¿CATMAP hoy puede competir con ArcGIS Pro Layout?
**No.** ArcGIS Pro está varios niveles arriba en sistema de composición y automatización.

### ¿CATMAP sirve para informes ambientales básicos?
**Sí, parcialmente.** Para entregables simples y rápidos, sí. Para cartografía editorial/profesional compleja, aún no.

### ¿Qué copiar primero?
1. Modelo de item + panel de propiedades estilo QGIS (conceptualmente).
2. Flujo de map frame (extent/scale/grid) tipo ArcMap/QGIS.
3. Leyenda y tipografía contextual robustas.

### ¿Qué no conviene intentar todavía?
1. Importación MXD/PAGX nativa completa.
2. Funciones enterprise complejas (atlas avanzado, geospatial PDF completo) antes de estabilizar base.

---

## 6) Recomendación de arquitectura objetivo

Objetivo recomendado:
- `LayoutModel` = única fuente de verdad.
- Todo visible es `LayoutElement` (sin render fantasma ni ramas paralelas).
- `LayoutRenderer` único para preview/export/print.
- `LayoutMap` como map frame vivo.
- `LayoutLegend`, `LayoutScaleBar`, `LayoutNorthArrow`, `LayoutLabel`, `LayoutImage`, `LayoutTable`, `LayoutRectangle` con propiedades completas.
- `LayoutTemplateManager` para persistencia `.catmap`.
- `QgisQptImporter` como importador parcial pragmático (no prometer 100% fidelidad).

---

## 7) Roadmap por fases

### Fase A — Estabilización base
- Selección/move/resize 100% confiables.
- Eliminar remanentes de render no-model.
- Pipeline único preview/export/print.

### Fase B — Elementos cartográficos pro
- Leyenda final robusta.
- Escala gráfica final.
- Norte y grilla con estilos.
- Map frame editable completo.

### Fase C — Plantillas y persistencia
- Plantillas técnicas/ambientales firmes.
- Guardado/carga `.catmap` consistente.
- Reapertura sin pérdidas de estilo.

### Fase D — Interoperabilidad
- QPT básico estable.
- QPT avanzado incremental.
- Importadores externos experimentales (no críticos).

### Fase E — Profesionalización
- Atlas/map-series.
- SVG export.
- Biblioteca simbológica avanzada.

---

## 8) MXD / ArcMap (viabilidad)

Conclusión realista:
- Soporte directo MXD completo es técnicamente y legalmente complejo.
- No hay librería OSS simple y confiable para “paridad ArcMap layout”.
- Recomendación: priorizar QPT y formatos abiertos (PDF/SVG/GeoPackage/SLD cuando aplique), más un importador externo limitado si se evalúa luego.

---

## 9) Bugs y riesgos de UX aún visibles

1. Propiedades contextuales de texto/leyenda insuficientes para edición profesional rápida.
2. Inserción de formas aún no sigue flujo de dibujo directo esperado por usuarios GIS/CAD.
3. Panel de elementos todavía no alcanza claridad operativa de un compositor maduro.
4. Ajustes de map frame/grilla/escala no están aún en un flujo único e intuitivo.
5. Simbología y etiquetado siguen por debajo de lo esperado para producción cartográfica exigente.

---

## 10) Conclusión final

CATMAP **va en buen camino**, pero hoy está en etapa de transición: ya supera un editor básico y todavía no llega a un compositor GIS profesional comparable con QGIS/ArcMap.

Recomendación estratégica:
- **No reescribir desde cero ahora**.
- Sí hacer refactor por fases con foco en:
  1) arquitectura unificada,
  2) map frame real,
  3) leyenda/tipografía profesional,
  4) export WYSIWYG sólido.

Esfuerzo estimado:
- Estabilizar base: **medio**.
- Alcanzar nivel QGIS básico usable en producción: **grande**.
- Competir con ArcGIS Pro en layout: **enorme** (no objetivo de corto plazo).

---

## Referencias funcionales de comparación (alto nivel)

- QGIS Documentation – Print Layout / Layout Items: https://docs.qgis.org/
- ArcGIS Pro – Layouts and Map Frames: https://pro.arcgis.com/
- ArcMap – Layout View basics: https://desktop.arcgis.com/
- gvSIG Desktop docs: https://docs.gvsig.org/
- uDig project/docs: https://udig.github.io/
- MapWindow GIS docs: https://www.mapwindow.org/

