# CATGIS Label Placement Report — 2026-06-02

## 1. Estado anterior

- Etiquetas siempre centradas sobre el anchor point (centroide/interior point)
- Sin deteccion de superposicion
- Sin prioridad entre etiquetas
- Placement declarado en el dialogo pero no implementado en el render
- Resultado: etiquetas superpuestas en capas densas, sin importar la geometria

## 2. Placement implementado por geometria

### Puntos (`POINT_ABOVE`, `POINT_BELOW`, `POINT_LEFT`, `POINT_RIGHT`, `POINT_CENTER`, `AUTO`)

| Modo | Comportamiento |
|------|---------------|
| `POINT_ABOVE` | Etiqueta arriba del punto, centrada horizontalmente |
| `POINT_BELOW` | Etiqueta abajo del punto |
| `POINT_LEFT` | Etiqueta a la izquierda del punto |
| `POINT_RIGHT` | Etiqueta a la derecha del punto |
| `POINT_CENTER` | Etiqueta centrada sobre el punto |
| `AUTO` | Prueba derecha, izquierda, arriba, abajo, centro (en ese orden) y elige la primera sin colision |

### Lineas (`LINE_CENTER`, `LINE_FOLLOW`, `AUTO`)

| Modo | Comportamiento |
|------|---------------|
| `LINE_CENTER` | Etiqueta en el punto medio de la linea, centrada |
| `LINE_FOLLOW` | Prueba arriba, centro, abajo del punto medio |
| `AUTO` | Prueba arriba, centro, abajo del punto medio |

### Poligonos (`POLYGON_CENTROID`, `POLYGON_INTERIOR`, `AUTO`)

| Modo | Comportamiento |
|------|---------------|
| `POLYGON_CENTROID` | Etiqueta en el centroide del poligono |
| `POLYGON_INTERIOR` | Etiqueta en el interior point (fallback a centroide) |
| `AUTO` | Prueba centro, arriba, abajo del centroide/interior point |

## 3. Estrategia de colision

### Algoritmo
1. Por cada capa, se colectan todas las etiquetas候选 con su posicion anchor
2. Se ordenan por prioridad (menor numero = mayor prioridad)
3. Para cada etiqueta, se generan candidate positions segun el modo de placement
4. Se verifica bounding box contra las cajas ya usadas
5. Se elige la primera posicion sin colision
6. Si todas colisionan, se usa la primera candidata (mejor overlap que nada)

### Bounding box
- Ancho: textWidth + 8px padding
- Alto: textHeight + 4px padding
- Altura adicional: ascent del font

### Collision entre capas
- Las cajas de colision se acumulan globalmente entre todas las capas
- La prioridad se respeta: etiquetas con prioridad 1 se colocan antes que prioridad 10

## 4. Prioridad implementada

- Campo `labelPriority` en Layer (1-10, default 5)
- 1 = mayor prioridad (se renderiza primero)
- 10 = menor prioridad (se renderiza ultimo)
- La prioridad determina el orden de colocacion: prioridad 1 siempre gana contra prioridad 5
- Persiste en proyecto con campo `LABEL_PRIORITY=`

## 5. Que casos mejora

| Caso | Antes | Ahora |
|------|-------|-------|
| 5 puntos cercanos | 5 etiquetas superpuestas | 3-5 etiquetas sin superposicion (depende de densidad) |
| Linea con puntos cercanos | Etiqueta en centro, puede chocar | Etiqueta busca posicion libre arriba/abajo |
| Poligono pequeno | Etiqueta centrada, puede salirse | Etiqueta centrada con fallback |
| Capas con prioridades | Todas iguales | Etiquetas prioritarias sobreviven |
| Mapa con 10+ capas etiquetadas | Sopa de texto | Colision global entre capas |

## 6. Limitaciones que siguen

### No resueltos en esta ronda
- **No hay rotation de etiquetas** para lineas (el texto siempre es horizontal)
- **No hay placement curvilíneo** (follow line es simplificado)
- **No hay smart collision** (simulated annealing, force-directed)
- **No hay label leader lines** (linea conectora cuando la etiqueta se desplaza)
- **No hay masking** (poligono semitransparente detras de la etiqueta)
- **No hay auto-resize** de fuente segun densidad
- **No hay expression-based labeling** (solo campo simple)

### Rendimiento
- Para capas con >1000 features, el placement puede ser lento
- El algoritmo es O(n*m) donde n=features y m=candidate positions
- Para uso tipico (100-500 features por capa) es acceptable

## 7. Comparacion honesta con QGIS/ArcGIS

| Caracteristica | CATGIS | QGIS | ArcGIS Pro |
|---------------|--------|------|------------|
| Point placement modes | 6 | 8+ | 10+ |
| Line placement | Basico | Curvilíneo | Curvilíneo + rotacion |
| Polygon placement | 2 modos | 3+ modos | 5+ modos |
| Collision detection | BBox basico | BBox + avoidance | Simulated annealing |
| Priority | 1-10 por capa | Por capa + expresion | Por capa + expresion + reglas |
| Label engine | Basico | PAL (robust) | Maplex (avanzado) |
| Performance (10K features) | Lento | Rapido | Muy rapido |
| Masking | No | Si | Si |
| Leader lines | No | Si | Si |

### Nivel actual de CATGIS
**50% de lo que un cartografo necesita para mapas basicos.**
Suficiente para mapas ambientales/tecnicos con 1-5 capas etiquetadas.
No suficiente para mapas complejos con 10+ capas densas.

### Para llegar al 75%
- Rotation de etiquetas en lineas
- Smart collision con priority global
- Performance para >1000 features
- Masking basico

## 8. Archivos tocados

| Archivo | Cambio |
|---------|--------|
| `Layer.java` | +LabelPlacementMode enum (10 modos), +labelPriority, +labelCollisionAvoid |
| `LabelPlacementEngine.java` | **NUEVO** - Motor de placement y colision |
| `MapPanel.java` | drawAllLabels() batch con colision, drawResolvedLabel(), drawLabelForFeature() → no-op |
| `LayerPropertiesDialog.java` | Selector de placement real, spinner prioridad, checkbox colision |
| `SaveProjectAction.java` | +LABEL_PLACEMENT_MODE, +LABEL_PRIORITY, +LABEL_COLLISION_AVOID |
| `LoadProjectAction.java` | +deserialization de 3 campos nuevos |
| `VectorLayerUtils.java` | +copia de 3 campos nuevos |
