# CATMAP Standalone — UX Report

## 1. Estructura visual final

```
┌─────────────────────────────────────────────────────────────────────┐
│ [Menu Bar] Archivo │ Edición │ Vista │ Insertar │ Mapa │ Ayuda      │
├─────────────────────────────────────────────────────────────────────┤
│ [Toolbar] Nuevo │ Abrir │ Guardar │ Export │ │ Seleccionar │ Pan │  │
│ Zoom │ │ Texto │ Imagen │ Rect │ Elipse │ Linea │ │ Mapa │ Ley │  │
│ Escala │ Norte │ │ Dup │ ↑ │ ↓ │ ✕                                │
├─────────────────────────────────────────────────────────────────────┤
│ [Element List]     │ [Layout Preview]                │ [Properties] │
│                    │                                   │              │
│ • Mapa principal   │   ┌──────────────────────────┐   │ X: 15.0 mm   │
│ • Leyenda          │   │                          │   │ Y: 25.0 mm   │
│ • Escala           │   │       [MAPA VIVO]        │   │ Ancho: 267   │
│ • Norte            │   │                          │   │ Alto: 145    │
│ • Título           │   │                          │   │              │
│                    │   └──────────────────────────┘   │ Grilla: [✓]  │
│                    │   ████████████████████████████    │              │
│                    │   Escala 1:10,000         50m    │              │
├─────────────────────┴───────────────────────────────────┴─────────────┤
│ [Layer Panel] Capas del mapa: 5    [Refrescar]                        │
│ ☑ Inundacion preliminar                                               │
│ ☑ Pozos BV.shp                                                        │
│ ☑ Accesibilidad BV.shp                                                │
│ ☑ Esri World Imagery                                                  │
│ ☐ Terrain Tiles                                                       │
├─────────────────────────────────────────────────────────────────────┤
│ [Status Bar] CATMAP Standalone v1.0 | Proyecto: prueba | EPSG:4326 │
└─────────────────────────────────────────────────────────────────────┘
```

### Layout de la ventana (1400×900)

- **Menú superior** — 7 menús con atajos de teclado
- **Toolbar** — ~25 botones con tooltips, agrupados por función
- **Panel izquierdo (200px)** — Lista de elementos del layout
- **Panel central** — Preview del layout con scroll, zoom, drag & drop
- **Panel derecho (240px)** — Propiedades contextuales del elemento seleccionado
- **Panel inferior (150px)** — Capas del mapa con checkboxes
- **Barra de estado** — Conexión CATGIS, proyecto actual, CRS

---

## 2. Menús

### Archivo
| Item | Atajo | Acción |
|------|-------|--------|
| Nuevo layout | Ctrl+N | Crea layout con elementos por defecto |
| Abrir layout... | Ctrl+O | Abre archivo .catmap |
| Abrir proyecto .catgis... | — | Carga proyecto CATGIS |
| Guardar | Ctrl+S | Guarda .catmap |
| Guardar como... | — | Guarda como nuevo archivo |
| Exportar PDF | — | Exporta a PDF con selector de archivo |
| Exportar PNG | — | Exporta a PNG |
| Exportar SVG | — | Exporta a SVG |
| Exportar imagen... | — | Exporta a imagen (PNG default) |
| Imprimir... | Ctrl+P | Pendiente |
| Salir | Ctrl+X | Cierra la aplicación |

### Edición
| Item | Atajo | Acción |
|------|-------|--------|
| Deshacer | Ctrl+Z | Pendiente |
| Rehacer | Ctrl+Y | Pendiente |
| Copiar | Ctrl+C | Pendiente |
| Pegar | Ctrl+V | Pendiente |
| Duplicar | — | Pendiente |
| Eliminar | Delete | Pendiente |
| Seleccionar todo | Ctrl+A | Pendiente |
| Bloquear elemento | — | Pendiente |
| Desbloquear elemento | — | Pendiente |

### Vista
| Item | Acción |
|------|--------|
| Zoom a página | Ajusta preview al tamaño de página |
| Zoom al ancho | Ajusta al ancho del panel |
| Zoom 100% | Zoom real 1:1 |
| Mostrar reglas | Checkbox toggle |
| Mostrar grilla | Checkbox toggle |
| Mostrar guías | Checkbox toggle |
| Mostrar panel de capas | Checkbox toggle |
| Mostrar panel de propiedades | Checkbox toggle |

### Insertar
| Item | Acción |
|------|--------|
| Texto | Inserta label editable |
| Imagen | File chooser → inserta imagen |
| Rectángulo | Forma rectangular |
| Elipse | Forma elíptica |
| Línea | Línea simple |
| Mapa | Map frame vivo |
| Leyenda | Leyenda auto-height |
| Escala gráfica | Barra de escala |
| Norte | Flecha norte |
| Tabla | Tabla simple |

### Mapa
| Item | Acción |
|------|--------|
| Actualizar desde CATGIS | Socket → sync proyecto y capas |
| Sincronizar capas visibles | Refresca visibilidad |
| Sincronizar simbología | Refresca estilos |
| Sincronizar etiquetas | Refresca labels |
| Usar extent actual de CATGIS | Toma viewport del MapPanel |
| Fijar extent del mapa | Congela extent actual |
| Ajustar a capas visibles | Fit a visible layers |
| Refrescar mapa | F5 — invalida cache y renderiza |

### Ayuda
| Item | Acción |
|------|--------|
| Atajos de teclado | Muestra popup con shortcuts |
| Documentación | Pendiente (link a docs) |
| Acerca de CATMAP | Versión, descripción, copyright |

---

## 3. Toolbar

### Grupo Documento
Nuevo (icono: null), Abrir (openIcon), Guardar (saveIcon), Exportar PDF (exportIcon), Exportar PNG (exportIcon), Imprimir (projectIcon)

### Grupo Trabajo
Seleccionar (moveFeatureIcon), Pan mapa (panIcon), Zoom mapa (zoomInIcon)

### Grupo Insertar
Mapa (genericLayerIcon), Leyenda (tableIcon), Escala (null), Norte (null), Texto (attrEditIcon), Imagen (imageryIcon), Rectángulo (rectangleIcon), Elipse (circleIcon), Línea (lineIcon)

### Grupo Editar
Editar (propertiesIcon), Duplicar (attrCopyIcon), Subir (upIcon), Bajar (downIcon), Quitar (removeIcon)

### Grupo Alinear
Izquierda, Centro, Derecha, Arriba, Medio, Abajo

### Grupo Organizar
Visible (visibleIcon), Bloquear (lockIcon, pending), Eliminar (removeIcon)

---

## 4. Paneles

### Panel izquierdo: Elementos del layout
- Lista de todos los elementos en orden Z
- Incluye nombre, tipo, visibilidad
- Selección → panel derecho muestra propiedades

### Panel derecho: Propiedades
- Contextual según tipo de elemento
- Campos editables: X, Y, Ancho, Alto (mm)
- LayoutMap: toggle grilla
- LayoutLabel: texto, fuente, tamaño, negrita, cursiva
- LayoutLegend: título, auto-alto
- LayoutScaleBar: escala 1:N, segmentos
- LayoutImage/LayoutNorthArrow/LayoutCartouche: campos específicos

### Panel inferior: Capas del mapa
- Lista de capas con checkbox de visibilidad
- Botón "Refrescar" para sync desde CATGIS
- Muestra nombre, tipo y CRS de cada capa

### Barra de estado
- Conexión a CATGIS (verde: conectado, gris: standalone)
- Nombre del proyecto actual
- CRS del proyecto
- Mensajes de operación (exportando, guardando, etc.)

---

## 5. Flujo de uso típico

### Desde CATGIS Desktop
1. Abrir CATGIS
2. Cargar proyecto con capas
3. Menú Salida → CATMAP (o toolbar)
4. El layout se abre con el mapa vivo del proyecto
5. Ajustar título, leyenda, escala
6. Exportar PDF/PNG
7. Cerrar CATMAP → vuelve a CATGIS

### Desde CATMAP Standalone (con CATGIS)
1. Abrir CATMAP directamente
2. Se conecta automáticamente a CATGIS (puerto 8899)
3. Sync proyecto y capas
4. Crear/editar layout
5. Exportar PDF/PNG
6. Guardar .catmap

### Desde CATMAP Standalone (sin CATGIS)
1. Abrir CATMAP
2. "Sin conexión a CATGIS" en barra de estado
3. Archivo → Abrir proyecto .catgis
4. El layout usa extent de capas visibles
5. Crear/editar layout
6. Exportar y guardar

---

## 6. Limitaciones reales

| Limitación | Impacto | Plan |
|-----------|---------|------|
| Sin deshacer/rehacer | Alto: operaciones destructivas son permanentes | Implementar Command pattern en LayoutModel |
| Sin impresión | Medio: usuario no puede enviar a impresora directa | Integrar Printable en LayoutPreviewPanel |
| Render raster/online depende de MapPanel | Medio: standalone no muestra tiles ni DEM | Implementar render directo de raster en MapFrameRenderer |
| Sin instalador separado | Medio: no hay acceso directo desde menú inicio | Configurar jpackage en build.gradle |
| Sin iconos propios para Escala/Norte | Bajo: botones sin ícono | Agregar SVGs a AppIcons |
| Sin documentación de usuario | Medio: onboarding difícil | Agregar manual de usuario |
| LayoutImage no se serializa en .catmap | Medio: imágenes se pierden al guardar/cargar | Fix en CatmapSerializer |
| Grilla del template es simple (no CRS) | Bajo: afecta template, no LayoutMap | Migrar template a usar LayoutMap |
