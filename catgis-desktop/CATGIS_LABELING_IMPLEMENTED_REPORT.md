# CATGIS Labeling Implemented Report — 2026-06-02

## 1. Estado anterior
- Solo `labelsVisible` (checkbox) y `labelField` (combo de campo) en Layer
- Sin control de fuente, color, halo ni offset
- Sin render de etiquetas en el mapa

## 2. Nuevos campos/configuraciones en Layer.java
12 propiedades nuevas:
- labelFontFamily (String, default "SansSerif")
- labelFontSize (int, default 10)
- labelBold / labelItalic (boolean)
- labelColor (Color, default BLACK)
- labelHaloEnabled (boolean)
- labelHaloColor (Color, default white 200 alpha)
- labelHaloWidth (float, default 2)
- labelOffsetX / labelOffsetY (int)
- labelPlacement (String, default "AUTO")

## 3. Dialogo de propiedades (LayerPropertiesDialog)
Pestana "Etiquetas" expandida con:
- Campo (combo de atributos)
- Fuente (combo con todas las del sistema)
- Tamano (spinner 6-72)
- Bold/Italic (toggle buttons)
- Color texto (JColorChooser)
- Halo checkbox + color + grosor
- Offset X/Y (spinners -50 a +50)

## 4. Render de etiquetas (MapPanel.drawLabelForFeature)
- Puntos: etiqueta centrada sobre el punto con offset
- Lineas: etiqueta en el punto medio de la linea
- Poligonos: etiqueta en el centroide del poligono
- Halo: render en 8 direcciones con grosor configurable
- Soporta bold, italic, color

## 5. Persistencia (SaveProjectAction / LoadProjectAction)
Formato keyed suffix compatible hacia atras:
- LABEL_FONT=, LABEL_SIZE=, LABEL_BOLD=, LABEL_ITALIC=
- LABEL_COLOR=, LABEL_HALO=, LABEL_HALO_COLOR=, LABEL_HALO_WIDTH=
- LABEL_OFFSET_X=, LABEL_OFFSET_Y=

## 6. Pendiente
- No hay motor de colision de etiquetas (pueden superponerse)
- LabelPlacement no se usa en el render (siempre centrado)
- No hay minScale/maxScale

## 7. Limitaciones vs QGIS/ArcGIS
- CATGIS tiene etiquetas basicas funcionales. QGIS tiene motor de posicionamiento avanzado, expresiones, multi-linea, y reglas de visibilidad por escala.
- Suficiente para mapas tecnicos y ambientales basicos.
