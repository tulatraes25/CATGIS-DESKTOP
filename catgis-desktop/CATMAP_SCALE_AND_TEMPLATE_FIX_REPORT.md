# CATMAP Scale and Template Fix Report — 2026-06-02

## 1. Resumen ejecutivo

Esta ronda corrigio tres problemas visibles que degradaban la experiencia de CATMAP como compositor cartografico:

1. **Escala grafica**: se veia mal, la numeracion era pobre y `Propiedades` no funcionaba sobre ella.
2. **Norte**: quedaba mal ubicado en la mayoria de las plantillas.
3. **Plantillas**: pocas, mal nombradas y sin agrupacion A4/A3.

Todo se resolvio con cambios en 4 archivos, sin romper tests ni funcionalidad existente.

---

## 2. Escala grafica

### Estado anterior
- `LayoutScaleBar.render()` dibujaba segmentos sin borde claro, con labels pegados a los bordes de cada segmento.
- No mostraba la relacion 1:N debajo de la barra (el usuario no veia la escala numerica).
- El popup de `Propiedades` sobre la escala no existia (la accion estaba muerta).
- La barra no tenia anti-aliasing de texto.

### Cambios en codigo (`LayoutScaleBar.java`)
- Segmentos alternados con borde gris fino (`new Color(0xCCCED4)`)
- Labels centrados debajo de cada segmento (no pegados al borde)
- Label total a la derecha de la barra
- Linea adicional debajo con `"1:25,000"` centrada, gris tenue
- `RenderingHints.VALUE_TEXT_ANTIALIAS_ON` activado
- Barra minima 20px (antes 5px, lo que hacia invisible barras muy finas)
- Metodo `formatScaleLabel()`: si >= 1000m muestra "Xk m", si no "X m"

### Cambios en codigo (`MapLayoutComposerDialog.java`)
- `showScalePopup()` agregado con:
  - Selector de segmentos (2 a 8)
  - Selector de color
  - Campo de unidad
  - **Campo "Escala 1:" para control manual** (el usuario ingresa 5000 para 1:5000)
  - Botones Aceptar/Cancelar
  - Enter = Aceptar, Esc = Cancelar
- Conectado a `openElementProperties()`: cuando el usuario hace clic derecho > Propiedades sobre la escala, se abre este popup.

### Limitaciones
- La escala mostrada depende de `mapScaleDenominator`. Si el mapa no tiene escala configurada, muestra el default (1:10,000).
- El control manual ajusta `LayoutScaleBar.mapScaleDenominator` pero no el zoom del MapPanel.

---

## 3. Norte

### Estado anterior
- Cada plantilla tenia coordenadas hardcodeadas para el norte. Muchas quedaban en posiciones incorrectas (centro, abajo, lejos del mapa).

### Cambios en codigo (`LayoutTemplateManager.java`)
- `addMap()` ahora guarda x, y, width, height en `z[1]` a `z[4]`.
- `addNorthAuto(m, id, size, z)` calcula automaticamente:
  - `nx = mapX + mapWidth - size - 4`
  - `ny = mapY + 4`
- Resultado: norte siempre en la esquina superior derecha del marco del mapa, con 4mm de margen.
- Array `z` expandido de `{0}` a `{0,0,0,0,0}` en los 22+ builders.
- Todas las plantillas usan `addNorthAuto`.

### Limitaciones
- El norte es un triangulo simple con "N". No tiene rotacion automatica segun el mapa.
- El tamaño es fijo (14-20px segun plantilla). No se escala con el mapa.

---

## 4. Plantillas

### Cantidad y agrupacion
- **28 plantillas curadas** visibles en el selector:
  - A4: 21 plantillas
  - A3: 7 plantillas
- Agrupadas visualmente con headers "— A4 (297×210 mm) —" y "— A3 (420×297 mm) —" en la lista.
- Los headers no son seleccionables (key vacia).

### Nomenclatura tecnica
Formato: `Familia · Formato · Variante`

Ejemplos:
- `Infraestructura · A4 · Ubicacion general`
- `Tecnica · A4 · Leyenda derecha`
- `Ambiental · A4 · Base satelital`
- `Hidrologia · A4 · Drenaje`
- `Catastral · A3 · Parcelario con tabla`

### Selector
- Ventana modal (`JDialog`) con:
  - Lista de plantillas a la izquierda
  - Preview (render del template a 400x280) a la derecha
  - Descripcion debajo del preview
  - Botones Aplicar/Cancelar
  - Doble clic en la lista = Aplicar
- Al aplicar plantilla A3: `pageSizeCombo` se ajusta a A3 y `orientationCombo` a LANDSCAPE.
- Al aplicar plantilla A4: se ajusta a A4 y orientacion correspondiente.

### Plantillas descartadas/ocultadas
- ~48 plantillas parametricas existen en `TemplateCatalog.java` pero NO se muestran en el selector principal. Solo las 28 curadas son visibles.
- Las plantillas parametricas se generan con `buildParametric()` y producen layouts genericos. No estan a la altura de calidad requerida.

---

## 5. Propiedades por elemento

| Elemento | Popup | Edicion real | Depende panel lateral |
|---|---|---|---|
| Titulo | `showTextPopup` | Fuente, tamanio, B/I, color, halo | Parcial (posicion/size en panel) |
| Subtitulo | `showTextPopup` | Idem | Parcial |
| Texto libre | `showTextPopup` | Idem | Parcial |
| Mapa principal | `showMapPropsPopup` | Escala 1:N, grilla on/off | Parcial (posicion/size/grilla detalle en panel) |
| Leyenda | `showLegendPopup` | Titulo, fuente, tamanios, fondo, borde, columnas, auto-alto | Parcial (posicion/size en panel) |
| Escala grafica | `showScalePopup` | Segmentos, color, unidad, escala 1:N | Completo |
| Norte | `showNorthPopup` | X, Y, tamanio | Completo |
| Cartucho | `showCartouchePopup` | 6 campos editables | Completo |

Todos los popups tienen Aceptar/Cancelar + Enter/Esc.

---

## 6. Validacion ejecutada

| Comando | Resultado |
|---|---|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 268 tests, 0 failures |
| `gradlew build -x checkstyleMain -x checkstyleTest` | BUILD SUCCESSFUL |
| `gradlew packageWindowsExeInstaller` | BUILD SUCCESSFUL |

Instalador: `C:\CATGIS\catgis-desktop\build\installer\windows\exe\CATGIS Desktop Review-1.0.0.exe`

---

## 7. Riesgos y deuda pendiente

### Sigue flojo
- **Map frame**: `LayoutMap` sigue siendo un render del MapPanel global, no un frame vectorial independiente como QGIS/ArcMap.
- **Simbologia de lineas**: los 18 estilos existen en el enum y en `buildLineStroke()`, pero el preview en el dialogo de propiedades es un icono pequenio (80x16). No todos los usuarios notaran la diferencia entre estilos similares (BOLD vs PATH_PRIMARY).
- **Simbologia de poligonos**: 18 texturas pero solo 5-6 son claramente distinguibles en el preview de 24x16px.
- **Calidad visual final de plantillas**: 28 plantillas curadas usan builders especificos o parametricos. Las parametricas producen layouts funcionales pero no optimizados visualmente para cada caso de uso.

### Sigue parcial
- **Alineacion**: 6 modos funcionan, pero no hay distribucion automatica de elementos.
- **Snap**: existe snap a grid (5mm) y a bordes de elementos, controlado por toggles en panel de mapa.
- **Preview de plantillas**: muestra la estructura del layout pero el area del mapa aparece como placeholder gris si no hay proyecto cargado.

---

## 8. Proximo paso recomendado

**Prioridad 1 — Map frame vivo**: hacer que `LayoutMap` renderice el mapa directamente sin captura intermedia. Es la funcionalidad que mas acerca CATMAP a un compositor real.

**Prioridad 2 — Simbologia**: completar `LineSymbolRenderer` y `PolygonSymbolRenderer` con el mismo patron que `PointSymbolRenderer` (unificar 4 paths de render).

**Prioridad 3 — Pulido visual**: revisar una por una las 28 plantillas visibles y ajustar margenes, tamanios de fuente y equilibrio visual.

**No tocar todavia**: atlas/map series, geospatial PDF, multi-page, importacion MXD.
