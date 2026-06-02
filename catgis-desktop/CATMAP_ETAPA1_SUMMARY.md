# CATMAP — Etapa 1: Escala, Norte, Propiedades (2026-06-02)

## Escala grafica

### Antes
- Segmentos sin borde claro
- Labels pegados a los segmentos
- Sin indicador de relacion 1:N
- Popup de propiedades basico

### Ahora
- `LayoutScaleBar.render()` reescrito:
  - Segmentos alternados con borde gris fino
  - Labels centrados debajo de cada segmento
  - Label total a la derecha
  - Ratio "1:25,000" debajo de la barra, centrado, gris tenue
  - Anti-aliasing de texto activado
  - Barra minima 20px (antes 5px)
- `showScalePopup()` mejorado:
  - Campo "Escala 1:" para control manual de escala real
  - Aceptar/Cancelar funcionales
  - Enter = Aceptar, Esc = Cancelar
  - Segmentos, Color y Unidad editables

### Archivos tocados
- `src/ar/com/catgis/layout/LayoutScaleBar.java`
- `src/ar/com/catgis/MapLayoutComposerDialog.java`

## Norte

### Antes
- Posicion hardcodeada en cada plantilla (mal ubicado)
- Plantillas viejas con norte en lugares raros

### Ahora
- `addNorthAuto()` calcula posicion automaticamente:
  - X = mapa.right - size - 4
  - Y = mapa.top + 4
- 22 plantillas usan auto-posicion
- Posicion consistente: top-right del marco del mapa

### Archivos tocados
- `src/ar/com/catgis/layout/LayoutTemplateManager.java`

## Propiedades contextuales

### Antes
- Solo Titulo, Leyenda y Cartucho tenian popup
- Escala, Norte y Mapa no respondian

### Ahora
- 6/6 elementos con popup funcional:
  1. Titulo → showTextPopup (fuente, tamano, B/I/U, color, halo)
  2. Leyenda → showLegendPopup (titulo, fuente, tamanos, fondo/borde, columnas)
  3. Cartucho → showCartouchePopup (6 campos editables)
  4. Escala → showScalePopup (segmentos, color, unidad, escala 1:N)
  5. Norte → showNorthPopup (X, Y, tamano)
  6. Mapa → showMapPropsPopup (escala manual + grilla)

- Todos con Aceptar/Cancelar + Enter/Esc
- Acceso: doble clic o clic derecho > Propiedades

### Archivos tocados
- `src/ar/com/catgis/MapLayoutComposerDialog.java`

## Tests y build
- 268 tests, 0 failures
- BUILD SUCCESSFUL
- Instalador: CATGIS Desktop Review-1.0.0.exe
