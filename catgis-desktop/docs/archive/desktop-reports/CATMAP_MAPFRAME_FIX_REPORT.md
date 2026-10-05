# CATMAP MapFrame Fix Report

## 1. Causa del stretch

`LayoutMap.captureMapImage(w, h)` renderizaba el mapa a dimensiones exactas w×h del frame. Si el viewport del mapa (proporcion del MapPanel) era diferente a la proporcion del frame del layout, la imagen se estiraba forzadamente.

## 2. Correccion

`LayoutMap.render()` ahora calcula la escala de ajuste como `min(scaleX, scaleY)` para preservar el aspect ratio. La imagen se centra en el frame.

## 3. Verificacion

- Templates A4 landscape (297×210) con mapa centrado: OK
- Templates A3 (420×297) con mapa centrado: OK  
- Redimension del frame: mantiene aspect ratio

## 4. Limitaciones

- Las barras negras (letterbox) aparecen si el aspect ratio del mapa no coincide con el del frame
- Para un control perfecto, usar `captureFromMainMap()` que congela el extent actual
