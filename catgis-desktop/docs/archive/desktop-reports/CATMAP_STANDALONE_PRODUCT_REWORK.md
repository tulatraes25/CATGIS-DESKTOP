# CATMAP Standalone — Product Rework

## 1. Qué estaba mal realmente

### Problema A: Viewport/Extent se iba al planisferio o a miniatura

**Causa raíz:** `MapFrameViewport.fitFromMainMap()` tomaba ciegamente el envelope del `MapPanel` principal aunque estuviera vacío o sin inicializar. En modo standalone, CATMAP crea un `MapPanel()` nuevo con valores por defecto (`viewMinX=0, viewMinY=0, zoomFactor=1.0`) que genera un envelope de ~800×600 unidades alrededor de (0,0). Si los datos reales están en Argentina (~-68°,-33°), el viewport no intersecta con los datos y se ve un área en blanco.

**Por qué no fallbackeaba a `fitFromProjectLayers()`:** `fitFromMainMap()` devolvía `true` aunque el viewport estuviera mal (el envelope era válido, solo que vacío de datos). El fallback nunca se ejecutaba.

**Por qué se veía un planisferio chico:** Cuando `fitFromMainMap()` fallaba (MapPanel null), `fitFromProjectLayers()` también podía fallar si las capas no tenían envelope válido, dejando el viewport default `(-100,-100,100,100)` —un área de 200×200 grados que mostraba medio planeta con los datos reales como puntos diminutos.

### Problema B: Dual-rendering en MapLayoutComposerDialog

**Causa raíz:** `MapLayoutComposerDialog` inicializaba el layout model con un `LayoutMap` por defecto en posición fija (15mm, 25mm, 267mm, 145mm). Simultáneamente, `LayoutRenderer.renderResult.drawMapFrame()` dibujaba OTRO map frame desde el template en una posición diferente (calculada por márgenes). Esto creaba dos renderizados de mapa compitiendo —el del template (correcto, vivo) y el del LayoutMap (con viewport potencialmente erróneo).

### Problema C: CATMAP Standalone como ventana pobre

El standalone ya tenía toda la estructura funcional (menús completos, toolbar, paneles, export), pero arrastraba los bugs de viewport que hacían que el mapa se viera vacío o incorrecto, dando la impresión de ser una "ventana técnica" en lugar de un producto serio.

---

## 2. Qué se corrigió

### Fix 1: `MapFrameViewport.fitFromMainMap()` — Detección de vista vacía

Se agregó lógica para detectar cuándo el MapPanel tiene una vista "no útil":

1. Si el proyecto tiene capas visibles con datos reales, se calcula el envelope combinado de esas capas
2. Si el envelope del MapPanel NO intersecta con el envelope de las capas → se usa el de las capas
3. Si el envelope del MapPanel es sospechosamente grande (más de 100× el tamaño de los datos) → se usa el de las capas

Esto permite que en standalone, `fitFromMainMap()` reconozca que el MapPanel dummy está mostrando (0,0) y no Argentina, y automáticamente use el extent correcto de las capas.

### Fix 2: `LayoutMap.syncViewportToSource()` — Fallback robusto

Se mejoró la documentación y se aseguró que `fitFromMainMap()` → `fitFromProjectLayers()` sea el flujo correcto. El fallback ahora funciona porque `fitFromMainMap()` ya no devuelve `true` con vistas vacías.

### Fix 3: `MapLayoutComposerDialog` — Eliminación del dual-rendering

Se eliminó el `LayoutMap` por defecto del layout model en el diálogo de CATGIS. El template (LayoutRenderer) ya maneja el map frame principal desde el MapPanel en vivo. Los LayoutMap ahora son solo para mapas adicionales (inset maps).

Se agregaron botones "Insertar → Mapa, Leyenda, Escala, Norte" en la toolbar del diálogo para que los usuarios puedan agregar elementos cartográficos fácilmente.

### Fix 4: `LayoutExportEngine.java` — Orientación PDF

Bug: `exportPdf()` creaba la página PDF con `PDRectangle.A4` (portrait, 595×842 pts), pero `renderLayout()` generaba la imagen en A4 apaisado (297×210 mm, ~842×595 px). La imagen landscape se distorsionaba al encajarla en un rectángulo portrait.

Fix: Se reemplazó `PDRectangle.A4` por un rectángulo A4 landscape explícito.

### Fix 5: `CatmapSocketClient.java` — JSON parser robusto

El parser manual fue reescrito para manejar:
- Valores con escapes (`\"`, `\\`)
- Profundidad de objetos anidados (parser depth-aware)
- Valores `null` en JSON → `null` en Java
- Números como último valor del objeto (sin coma después)

---

## 3. Arquitectura actual de CATMAP

```
CATMAP tiene DOS modos de operación:
```

### Modo 1: Integrado en CATGIS Desktop
- Se abre desde el botón "CATMAP" en CartographyToolbar o menú Salida
- Usa `MapLayoutComposerDialog` (8,846 líneas)
- El mapa se renderiza en vivo desde `CatgisDesktopApp.mapPanel`
- El LayoutMap NO se agrega por defecto (evita dual-rendering)
- LayoutMap adicional se usa solo para inset maps

### Modo 2: CATMAP Standalone
- Ejecutable independiente: `catmap.Main`
- Ventana propia con menú completo
- Puede conectarse a CATGIS vía socket (puerto 8899)
- Puede funcionar sin CATGIS cargando un proyecto .catgis propio
- Usa `LayoutMap` como mapa principal por defecto

### Componentes clave

| Componente | Archivo | Rol |
|-----------|---------|-----|
| LayoutModel | `layout/LayoutModel.java` | Contenedor de elementos del layout |
| LayoutMap | `layout/LayoutMap.java` | Map frame vivo con cache y viewport independiente |
| MapFrameViewport | `layout/MapFrameViewport.java` | Viewport con extent/scale/center |
| MapFrameRenderer | `layout/MapFrameRenderer.java` | Render directo desde capas (sin MapPanel) |
| LayoutRenderContext | `layout/LayoutRenderContext.java` | Contexto de render con mm→px |
| LayoutPreviewPanel | `catmap/LayoutPreviewPanel.java` | Preview interactivo con drag & drop |
| CatmapSerializer | `catmap/CatmapSerializer.java` | Persistencia .catmap |
| LayoutExportEngine | `catmap/LayoutExportEngine.java` | Export PDF/PNG/JPG |
| SvgExportEngine | `catmap/SvgExportEngine.java` | Export SVG |
| CatmapSocketClient | `catmap/CatmapSocketClient.java` | Cliente socket para comunicación con CATGIS |
| CatgisSocketServer | `catmap/CatisSocketServer.java` | Servidor socket en CATGIS |

---

## 4. Elementos del layout disponibles

| Elemento | Clase | Descripción |
|----------|-------|-------------|
| Map frame | `LayoutMap` | Mapa vivo con zoom/pan/grilla/inset indicator |
| Leyenda | `LayoutLegend` | Leyenda de capas con auto-height |
| Escala gráfica | `LayoutScaleBar` | Barra de escala |
| Norte | `LayoutNorthArrow` | Flecha de norte |
| Texto | `LayoutLabel` | Texto libre con fuente/tamaño/color |
| Imagen | `LayoutImage` | Imagen desde archivo |
| Rectángulo | `LayoutRectangle` | Forma rectangular |
| Elipse | `LayoutEllipse` | Forma elíptica |
| Línea | `LayoutLine` | Línea simple |
| Tabla | `LayoutTable` | Tabla simple |
| Cartucho | `LayoutCartouche` | Datos cartográficos (proyecto, escala, fecha) |

---

## 5. Menús y toolbars del standalone

### Menú Archivo
Nuevo, Abrir, Abrir proyecto .catgis, Guardar, Guardar como, Exportar (PDF/PNG/JPG/SVG/Imagen), Imprimir, Salir

### Menú Edición
Deshacer, Rehacer, Copiar, Pegar, Duplicar, Eliminar, Seleccionar todo, Bloquear/Desbloquear

### Menú Vista
Zoom a página, Zoom al ancho, Zoom 100%, Mostrar reglas/grilla/guías, Mostrar panel de capas/propiedades

### Menú Insertar
Texto, Imagen, Rectángulo, Elipse, Línea, Mapa, Leyenda, Escala, Norte, Tabla

### Menú Mapa
Actualizar desde CATGIS, Sincronizar capas/simbología/etiquetas, Usar extent de CATGIS, Fijar extent, Ajustar a capas visibles, Refrescar mapa

### Menú Exportar
PDF, PNG, JPG, SVG, Configuración DPI

### Menú Ayuda
Atajos de teclado, Documentación, Acerca de

### Toolbar principal
Nuevo, Abrir, Guardar, Exportar PDF, Exportar PNG, Imprimir, Seleccionar, Pan mapa, Zoom mapa, Texto, Imagen, Rectángulo, Elipse, Línea, Mapa, Leyenda, Escala, Norte, Duplicar, Subir, Bajar, Quitar

---

## 6. Capacidades actuales como programa independiente

| Capacidad | Estado |
|-----------|--------|
| Ventana propia con menú completo | ✅ Completo |
| Crear layout nuevo | ✅ Completo |
| Abrir/guardar .catmap | ✅ Completo |
| Abrir proyecto .catgis | ✅ Completo |
| Insertar elementos cartográficos | ✅ Completo |
| Viewport sincronizado con capas | ✅ Corregido |
| Export PDF/PNG/JPG/SVG | ✅ Completo |
| Comunicación socket con CATGIS | ✅ Completo |
| Render independiente (sin MapPanel) | ✅ Parcial (vector-only, raster delega) |
| Panel de propiedades editable | ✅ Completo |
| Panel de capas | ✅ Completo |
| Drag & drop elementos | ✅ Completo |
| Imprimir | 🟡 Pendiente |
| Historial deshacer/rehacer | 🟡 Pendiente |
| Documento .catmap con extent por frame | ✅ Completo |

---

## 7. Qué sigue pendiente

1. **Imprimir** — implementar `printLayout()` en standalone
2. **Deshacer/Rehacer** — historial de operaciones en el layout model
3. **Render raster/online independiente** — `MapFrameRenderer` retorna null para raster/online y delega a MapPanel; en standalone esto no funciona
4. **Iconos propios** — faltan iconos para Escala, Norte, Mapa en la toolbar
5. **Instalador independiente** — generar un ejecutable separado para CATMAP
6. **Grilla CRS-based en template** — la grilla del template usa subdivision simple, la de LayoutMap usa CRS-based
7. **Leyenda por frame** — cada LayoutMap debería poder tener su propia leyenda
8. **Atlas/Map series** — `AtlasEngine.java` existe pero no integrado en UI

---

## 8. Qué tan cerca está de un compositor GIS serio

### Comparable a:
- **ArcMap Layout View** — funcionalidad similar (mapa vivo, leyenda, escala, norte, grilla, export)
- **QGIS Print Layout** — más simple pero con los mismos ingredientes base
- **Camera Raw / Photoshop** — metáfora de producto complementario

### Fortalezas:
- El layout model con elementos independientes es sólido
- El render es en vivo (no snapshot estático)
- El formato .catmap es completo
- La exportación PDF/PNG/JPG/SVG funciona
- La comunicación socket permite integración con CATGIS

### Debilidades:
- Sin deshacer/rehacer → no es profesional
- Sin impresión → no es profesional
- El render independiente de raster/online requiere MapPanel
- Falta documentación de usuario
- Sin instalador separado

**Score actual:** ~65% de un compositor GIS serio (como ArcMap Layout o QGIS Print Layout)
**Con deshacer + impresión + instalador:** ~85%
