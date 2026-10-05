# CATMAP Advanced Final Report — 2026-06-03

## 1. Qué se mejoró

### Inset maps
- Helper `addInsetMap()` agregado a LayoutTemplateManager
- LayoutMap soporta ownExtent (captura del mapa principal)
- Inset con marco propio, color configurable
- Agregado a plantillas: A4_TECNICO, A4_CATASTRAL, A3_TECNICO

### Grillas
- Distance-based grid con intervalos configurables
- Etiquetas de coordenadas en bordes
- Grosor configurable
- Control de visibilidad de etiquetas

### Tablas
- Header oscuro (#2D3748) con texto blanco
- Separadores de fila sutiles
- Filas alternadas
- Bordes y padding mejorados

### Cartucho
- Barra de título "Datos Cartográficos"
- Fondo semitransparente
- Tipografía reducida (7-9pt)
- Bordes redondeados
- Auto-ajuste de alto

### Plantillas retocadas

| Plantilla | Mejora clave |
|-----------|-------------|
| A4_TECNICO | +Inset map, mapa 190x138 |
| A4_AMBIENTAL | Verde #1B5E20, mapa 273x132 |
| A4_CATASTRAL | +Inset map, naranja #BF360C |
| A4_HIDROLOGIA | Azul #0D47A1, subtítulo |
| A4_TOPOGRAFIA | Verde #33691E, subtítulo |
| A4_INFRAESTRUCTURA | Púrpura #4A148C, cartucho |
| A3_TECNICO | +Inset map, mapa 280x228 |
| A3_AMBIENTAL | Verde #1B5E20, mapa 390x220 |

## 2. Cómo quedaron inset, grilla, tabla, cartucho

- **Inset**: Básico pero funcional. Captura el mapa principal con ownExtent. No tiene rectángulo indicador.
- **Grilla**: Funcional con distance-based grid y etiquetas. No CRS-based.
- **Tabla**: Profesional con header oscuro y separadores. Suficiente para informes.
- **Cartucho**: Profesional con título y fondo. Auto-ajusta alto.

## 3. Qué plantillas quedaron realmente fuertes

- **A4_TECNICO**: Con inset, leyenda con fondo, buenas proporciones
- **A4_AMBIENTAL**: Color diferenciado, mapa dominante
- **A4_CATASTRAL**: Con inset, color diferenciado
- **A3_TECNICO**: Con inset, espacio amplio

## 4. Qué sigue pendiente

- Inset con extent independiente (no solo captura)
- Rectángulo indicador de área del mapa principal
- Grilla CRS-based
- Export PDF vectorial
