# CATMAP Standalone — Documentación Final v2

**Fecha**: 2026-06-03
**Versión**: CATMAP Standalone v1.0
**Estado**: 268/268 tests verdes

---

## Resumen Ejecutivo

CATMAP Standalone es una aplicación independiente de composición cartográfica, separada de CATGIS Desktop. Permite crear, editar y exportar layouts profesionales para mapas técnicos y ambientales.

### Capaz de:
- Crear layouts desde cero
- Abrir proyectos .catgis de CATGIS
- Insertar mapas, leyendas, escalas, nortes, textos, imágenes, tablas
- Exportar a PDF, PNG, JPG, SVG
- Generar atlas/map series
- Guardar/cargar layouts en formato .catmap
- Funcionar sin CATGIS abierto
- Comunicarse con CATGIS via socket

---

## Arquitectura

### Componentes principales

| Clase | Propósito |
|-------|-----------|
| `Main.java` | Entry point, menú, toolbar, paneles |
| `LayoutPreviewPanel.java` | Preview con drag & drop y selección |
| `LayoutExportEngine.java` | Exportación PDF/PNG/JPG |
| `SvgExportEngine.java` | Exportación SVG |
| `AtlasEngine.java` | Generación batch de páginas |
| `CatmapSerializer.java` | Persistencia .catmap |
| `CatgisSocketServer.java` | Server en CATGIS |
| `CatmapSocketClient.java` | Client en CATMAP |
| `LayoutContext.java` | Contexto de acceso a proyecto |

---

## Menús

### Archivo
- Nuevo layout (Ctrl+N)
- Abrir layout (Ctrl+O)
- Abrir proyecto .catgis
- Guardar (Ctrl+S) / Guardar como
- Exportar PDF / PNG / SVG
- Imprimir (Ctrl+P)
- Salir

### Edición
- Deshacer (Ctrl+Z) / Rehacer (Ctrl+Y)
- Copiar (Ctrl+C) / Pegar (Ctrl+V)
- Duplicar / Eliminar (Delete)
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
- Refrescar mapa (F5)

### Exportar
- PDF, PNG, JPG, SVG
- Configuración DPI

### Ayuda
- Atajos de teclado
- Documentación
- Acerca de CATMAP

---

## Toolbar

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

## Paneles

### Panel izquierdo: Elementos del layout
- Lista de elementos del layout
- Selección por clic

### Panel central: Preview
- Hoja A4/A3 con contenido
- Zoom con rueda del mouse
- Drag para mover elementos
- Selección con highlight azul + handles

### Panel derecho: Propiedades
- Contextual según elemento seleccionado
- Campos editables (X, Y, Ancho, Alto)
- Propiedades específicas por tipo

### Panel inferior: Capas del mapa
- Lista de capas del proyecto
- Botón "Refrescar"

---

## Formato .catmap

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
ELEMENT|LayoutCartouche|cartouche-1|Datos cartograficos|15|190|267|18|5|true|false|FIELD_Estudio=|FIELD_Proyecto=|FIELD_Empresa=|FIELD_Cartografo=|FIELD_Fuente=|FIELD_Coord.=
# End of layout
```

---

## Exportación

| Formato | Motor | DPI | Notas |
|---------|-------|-----|-------|
| PDF | PDFBox | 150 | A4 landscape, imagen embebida |
| PNG | ImageIO | 150 | Sin pérdida |
| JPG | ImageIO | 150 | Conversión RGB |
| SVG | Embed base64 | 150 | PNG embebido en SVG |

---

## Comunicación CATGIS↔CATMAP

### Protocolo TCP (puerto 8899)

| Comando | Respuesta |
|---------|-----------|
| `PING` | `{"status":"ok","version":"1.0"}` |
| `GET_PROJECT_STATE` | `{"status":"ok","projectName":"...","crs":"...","layers":[...],"extent":{...}}` |
| `GET_LAYERS` | `{"layers":[{"name":"...","type":"...","visible":true,"crs":"..."}]}` |
| `GET_EXTENT` | `{"extent":{"minX":...,"minY":...,"zoomFactor":...}}` |

### Comportamiento
- CATGIS inicia server al arrancar
- CATMAP intenta conectar al iniciar
- Si conecta, sincroniza estado del proyecto
- Si no conecta, trabaja standalone

---

## Keyboard shortcuts

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
| Mouse wheel | Zoom |

---

## Drag & Drop

- Click en elemento → selección
- Drag → mover elemento
- Selection highlight → borde azul + handles
- Propiedades se actualizan al seleccionar

---

## Atlas/Map Series

```java
List<AtlasEngine.AtlasPage> pages = List.of(
    new AtlasEngine.AtlasPage("Página 1", "Subtítulo 1", minX, minY, zoom),
    new AtlasEngine.AtlasPage("Página 2", "Subtítulo 2", minX2, minY2, zoom2)
);
AtlasEngine.generateAndSave(template, pages, outputDir, "mapa", 150);
```

---

## Integración con CATGIS

### Desde CATGIS
- Botón "Abrir CATMAP" → abre CATMAP embebido
- Botón "CATMAP Standalone" → abre como aplicación independiente

### Desde CATMAP
- Archivo → Abrir proyecto → carga .catgis
- Mapa → Actualizar desde CATGIS → sincroniza estado

---

## Limitaciones actuales

### No implementado
- Drag & drop de elementos desde toolbar
- Propiedades con edición en tiempo real (solo al perder foco)
- Undo/Redo visual (funciona con Ctrl+Z/Y pero sin indicador)
- Topology rules
- Python scripting
- Plugin system

---

## Validación

| Comando | Resultado |
|---------|-----------|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 268/268 PASSED |
| `gradlew build` | BUILD SUCCESSFUL |
