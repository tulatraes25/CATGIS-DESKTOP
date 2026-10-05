# CATMAP Pro Visual Final Report — 2026-06-03

## 1. Qué estaba mal

### Auto-componer
- El botón "Auto-componer" en el toolbar generaba composiciones poco útiles
- El norte quedaba mal ubicado en algunos casos
- No generaba variantes confiables
- Efecto placebo: el usuario hacía clic y no obtenía algo usable

### Plantillas
- Las plantillas tenían márgenes inconsistentes (15mm vs 12mm)
- Falta de subtítulos en varias plantillas clave
- Jerarquía tipográfica inconsistente
- Colores de título genéricos (mismo #1A2434 para todos)
- Mapas con proporciones subóptimas

### Mapa vivo
- El mapa en el layout era una captura estática (cache sin invalidación por cambios de contenido)
- Ya resuelto en ronda anterior (500ms cache refresh)

## 2. Cómo quedó el mapa vivo redimensionable

- LayoutMap invalida cache cada 500ms para contenido vivo
- Resize del marco funciona via 8-handle drag en el compositor
- El contenido se re-renderiza al nuevo tamaño manteniendo proporción
- No hay deformación artificial del mapa

## 3. Cómo quedó el comportamiento del resize

- Drag handles en 8 puntos (N/S/E/W + esquinas)
- Conversión correcta de píxeles a mm
- Mínimo 5mm de tamaño
- Cache se invalida automáticamente al cambiar dimensiones

## 4. Qué se hizo con Auto-componer

**DECISIÓN: Ocultado del toolbar visible.**

Razón: No genera composiciones confiables. Es mejor usar plantillas predefinidas que han sido pulidas visualmente.

El método `autoComposeLayout()` sigue existiendo en el código pero no es accesible desde la interfaz. Se puede reactivar en el futuro cuando se implemente un auto-componer realmente útil.

## 5. Qué plantillas se mejoraron

| Plantilla | Mejoras |
|-----------|---------|
| A4_TECNICO | Márgenes uniformes (12mm), mapa más grande (190x138), leyenda con fondo, mejor proporción |
| A4_AMBIENTAL | Color verde (#1B5E20) para título, subtítulo, mapa más alto (132mm), leyenda mejor posicionada |
| A4_CATASTRAL | Color naranja (#BF360C) para título, subtítulo, mapa más grande (195x138), leyenda con fondo |
| A4_HIDROLOGIA | Color azul (#0D47A1) para título, subtítulo, mapa más alto, leyenda mejor posicionada |
| A4_TOPOGRAFIA | Color verde oscuro (#33691E) para título, subtítulo, mapa más alto, leyenda mejor posicionada |
| A4_INFRAESTRUCTURA | Color púrpura (#4A148C) para título, subtítulo, cartucho agregado |
| A3_TECNICO | Márgenes uniformes (15mm), mapa más grande (280x228), leyenda con fondo |
| A3_AMBIENTAL | Color verde, mapa más grande, mejor proporción |

## 6. Qué plantillas se ocultaron o retiraron

Ninguna plantilla fue retirada del catálogo. Se mejoraron las 8 plantillas más usadas.

## 7. Cómo quedó el norte por defecto

- `addNorthAuto()` calcula posición: esquina superior derecha del marco del mapa
- Fórmula: `nx = mapX + mapWidth - size - 4`, `ny = mapY + 4`
- Tamaño fijo según plantilla (14-20px)
- Funciona correctamente en todas las plantillas mejoradas

## 8. Qué sigue pendiente

### Prioridad ALTA
- Map frame vectorial real (no render del MapPanel global)
- Atlas/map series
- Export PDF vectorial (texto y líneas vectoriales)

### Prioridad MEDIA
- Auto-componer realmente útil (3-5 variantes confiables)
- Scale bar dinámico (enlazado automáticamente a la escala del LayoutMap)
- Grilla geoespacial real (CRS-based, no aritmética)

### Prioridad BAJA
- SVG export funcional
- Multi-page layout
- Importación MXD/QGS

## 9. Validación ejecutada

| Comando | Resultado |
|---------|-----------|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 268/268 PASSED |
| `gradlew build` | BUILD SUCCESSFUL |

### Archivos tocados

| Archivo | Cambio |
|---------|--------|
| `MapLayoutComposerDialog.java` | Auto-componer ocultado del toolbar |
| `LayoutTemplateManager.java` | 8 plantillas mejoradas (A4: TECNICO, AMBIENTAL, CATASTRAL, HIDROLOGIA, TOPOGRAFIA, INFRAESTRUCTURA; A3: TECNICO, AMBIENTAL) |
