# CATMAP Standalone — Documentación Final

**Fecha**: 2026-06-03
**Versión**: CATMAP Standalone v1.0
**Estado**: 268/268 tests verdes

---

## 1. Resumen Ejecutivo

CATMAP Standalone es una aplicación independiente de composición cartográfica, separada de CATGIS Desktop. Permite crear, editar y exportar layouts profesionales para mapas técnicos y ambientales.

### Capaz de:
- Crear layouts desde cero
- Abrir proyectos .catgis de CATGIS
- Insertar mapas, leyendas, escalas, nortes, textos, imágenes, tablas
- Exportar a PDF, PNG, JPG
- Guardar/cargar layouts en formato .catmap
- Funcionar sin CATGIS abierto

---

## 2. Arquitectura

### Componentes principales

| Clase | Propósito |
|-------|-----------|
| `Main.java` | Entry point, menú, toolbar, paneles |
| `LayoutPreviewPanel.java` | Preview del layout |
| `LayoutExportEngine.java` | Exportación PDF/PNG/JPG |
| `CatmapSerializer.java` | Persistencia .catmap |
| `LayoutContext.java` | Contexto de acceso a proyecto |

### Dependencias

```
CATMAP Standalone
├── LayoutModel (modelo de layout)
├── LayoutElement (elementos base)
├── LayoutMap (mapa principal)
├── LayoutLegend (leyenda)
├── LayoutScaleBar (escala gráfica)
├── LayoutNorthArrow (norte)
├── LayoutCartouche (datos cartográficos)
├── LayoutLabel (textos)
├── LayoutImage (imágenes)
├── LayoutRectangle/Elipse/Line (formas)
├── LayoutTable (tablas)
└── LayoutRenderContext (contexto de render)
```

---

## 3. Menús

### Archivo
- Nuevo layout
- Abrir layout (.catmap)
- Abrir proyecto (.catgis)
- Guardar / Guardar como
- Exportar PDF / PNG / JPG
- Imprimir
- Salir

### Edición
- Deshacer / Rehacer
- Copiar / Pegar
- Duplicar / Eliminar
- Seleccionar todo
- Bloquear / Desbloquear

### Vista
- Zoom a página / Zoom 100%
- Mostrar/ocultar reglas, grilla, guías
- Mostrar/ocultar paneles

### Insertar
- Texto, Imagen
- Rectángulo, Elipse, Línea
- Mapa, Leyenda, Escala, Norte, Tabla

### Mapa
- Actualizar desde CATGIS
- Sincronizar capas/simbología/etiquetas
- Usar/fijar/ajustar extent
- Refrescar mapa

### Exportar
- PDF, PNG, JPG, SVG
- Configuración DPI

### Ayuda
- Atajos de teclado
- Documentación
- Acerca de CATMAP

---

## 4. Toolbar

| Icono | Acción |
|-------|--------|
| 📂 | Nuevo layout |
| 📁 | Abrir layout |
| 💾 | Guardar |
| 📄 | Exportar PDF |
| 🖼 | Exportar PNG |
| 🖨 | Imprimir |
| ↖ | Seleccionar |
| ✋ | Pan mapa |
| 🔍 | Zoom mapa |
| T | Insertar texto |
| 🖼 | Insertar imagen |
| □ | Insertar rectángulo |
| ○ | Insertar elipse |
| ─ | Insertar línea |
| 🗺 | Insertar mapa |
| 📋 | Insertar leyenda |
| 📏 | Insertar escala |
| ⬆ | Insertar norte |
| 📋 | Duplicar |
| ▲ | Subir |
| ▼ | Bajar |
| 🗑 | Quitar |

---

## 5. Paneles

### Panel izquierdo: Elementos del layout
- Lista de elementos del layout
- Selección por clic

### Panel central: Preview
- Hoja A4/A3 con contenido
- Zoom con rueda del mouse
- Scroll horizontal/vertical

### Panel derecho: Propiedades
- Contextual según elemento seleccionado
- Muestra: posición, tamaño, tipo, propiedades específicas

### Panel inferior: Capas del mapa
- Lista de capas del proyecto
- Botón "Refrescar"

---

## 6. Formato .catmap

### Estructura
```
# CATMAP Layout v1
PAGE_SIZE=A4
PAGE_ORIENTATION=LANDSCAPE
ELEMENT|LayoutLabel|header-title|Titulo|12|6|273|14|0|true|false|TEXT=Mapa|FONT_FAMILY=SansSerif|FONT_SIZE=18|FONT_STYLE=1|COLOR=26,36,52
ELEMENT|LayoutMap|main-map|Mapa principal|15|25|267|145|1|true|false|SHOW_GRID=false|FRAME_COLOR=74,85,104|FRAME_WIDTH=0.8
ELEMENT|LayoutLegend|main-legend|Leyenda|155|55|75|40|2|true|false|AUTO_HEIGHT=true|SHOW_BACKGROUND=false|TITLE=Leyenda
ELEMENT|LayoutScaleBar|scale-1|Escala|145|175|95|10|3|true|false|SCALE_DENOMINATOR=10000.0|SEGMENTS=4
ELEMENT|LayoutNorthArrow|north-1|Norte|250|30|16|16|4|true|false
# End of layout
```

### Propiedades soportadas por tipo

| Tipo | Propiedades |
|------|------------|
| LayoutLabel | TEXT, FONT_FAMILY, FONT_SIZE, FONT_STYLE, COLOR |
| LayoutMap | SHOW_GRID, FRAME_COLOR, FRAME_WIDTH, OWN_EXTENT, VIEW_MIN_X/Y, ZOOM_FACTOR |
| LayoutLegend | AUTO_HEIGHT, SHOW_BACKGROUND, TITLE |
| LayoutScaleBar | SCALE_DENOMINATOR, SEGMENTS |
| LayoutNorthArrow | (sin propiedades extra) |
| LayoutCartouche | FIELD_* (campos dinámicos) |

---

## 7. Exportación

### PDF
- Motor: Apache PDFBox
- Formato: A4 landscape
- Resolución: 150 DPI
- Método: Render a BufferedImage → embed en PDF

### PNG
- Resolución: 150 DPI
- Formato: PNG sin pérdida

### JPG
- Resolución: 150 DPI
- Formato: JPG con conversión a RGB

---

## 8. Keyboard shortcuts

| Atajo | Acción |
|-------|--------|
| Ctrl+N | Nuevo layout |
| Ctrl+O | Abrir layout |
| Ctrl+S | Guardar |
| Ctrl+Z | Deshacer |
| Ctrl+Y | Rehacer |
| Ctrl+C | Copiar |
| Ctrl+V | Pegar |
| Delete | Eliminar |
| F5 | Refrescar mapa |
| Ctrl+P | Imprimir |

---

## 9. Integración con CATGIS

### Desde CATGIS
- Botón "Abrir CATMAP" → abre CATMAP embebido
- Botón "CATMAP Standalone" → abre CATMAP como aplicación independiente

### Desde CATMAP
- Archivo → Abrir proyecto → carga .catgis
- Mapa → Actualizar desde CATGIS → sincroniza estado

### Formato de intercambio
- .catgis → proyecto CATGIS (capas, CRS, estilos)
- .catmap → layout CATMAP (elementos, posiciones, estilos)

---

## 10. Limitaciones actuales

### No implementado
- Comunicación socket CATGIS↔CATMAP (pendiente)
- Panel de propiedades con edición en tiempo real
- Drag & drop de elementos
- Undo/Redo funcional (solo estructura en menú)
- SVG export
- Atlas/map series
- Inset maps con extent independiente

### Pendiente para Etapa 7
- Comunicación socket para sincronización en tiempo real
- Panel de propiedades con campos editables
- Drag & drop de elementos
- Undo/Redo completo
- Keyboard shortcuts para acciones de edición

---

## 11. Validación

| Comando | Resultado |
|---------|-----------|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 268/268 PASSED |
| `gradlew build` | BUILD SUCCESSFUL |

---

## 12. Commits de la sesión

```
afe8cfd feat: CATMAP Standalone Etapa 5 - Export PDF/PNG/JPG funcional
4468c8a feat: CATMAP Standalone Etapa 4 - Panel propiedades contextual
0aa6096 feat: CATMAP Standalone Etapa 3 - .catmap format + save/load
f93a4e9 feat: CATMAP Standalone Etapa 2 - Toolbar con iconos + hover effects
cdc8817 feat: CATMAP Standalone Etapa 1 - Menú completo + Fix viewport
8e894a9 fix: CATMAP viewport/extent fix
86e5c14 feat: Hover shadow effect on toolbar buttons
e927966 fix: CATMAP Standalone path + empty map fallback
55c62b7 fix: Tab icons + Installer cleanup + Geometry tools
```
