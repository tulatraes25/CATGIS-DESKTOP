# CATGIS Symbology Gap Report — Junio 2026

## Estado anterior
- 9 estilos de punto (basic shapes)
- 4 estilos de linea (SOLID, DASHED, DOTTED, DASH_DOT)
- 5 estilos de poligono (SOLID, DIAGONAL, CROSS, DOTS, OUTLINE)

## Estado actual (post-expansion + Codex)
- 28 estilos de punto con render unificado via PointSymbolRenderer
- 18 estilos de linea (enumerados en Layer.java)
- 18 estilos de poligono (enumerados en Layer.java)
- PointSymbolRenderer compartido entre mapa, preview, categorizada y leyenda

## Lo que falta
### Lineas
- Los 18 estilos existen en el enum pero la diferenciacion visual depende del dash pattern
- Estilos como PATH_PRIMARY vs SOLID se ven casi igual si no hay diferencia de grosor o color
- Faltan previews en el dialogo de propiedades que muestren claramente cada estilo
- buildLineStroke() necesita mapear cada estilo a un patron visual unico

### Poligonos
- Los 18 estilos existen pero muchos usan el mismo relleno solido
- TRANSPARENT vs OUTLINE_ONLY se ven igual (sin relleno)
- ENVIRONMENTAL, WATER, VEGETATION no tienen textura real (son solidos con distinto color por defecto)
- Faltan previews con patron visual en el dialogo

### Recomendacion para proxima ronda
1. Crear LineSymbolRenderer (mismo patron que PointSymbolRenderer)
2. Crear PolygonSymbolRenderer con texturas reales (BufferedImage con hatch patterns)
3. Conectar ambos a los 4 paths de render (mapa, preview, categorizada, leyenda)
4. Agregar previews visuales en LayerPropertiesDialog para lineas y poligonos
