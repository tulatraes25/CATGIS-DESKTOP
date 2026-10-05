# CATMAP — Estado real post-Codex (2026-06-02)

## Lo que Codex arreglo (confirmado en codigo)
- LayoutMap invalida cache por ancho/alto, no solo extent
- Plantillas curadas visibles en ventana con preview
- PointSymbolRenderer compartido (4 paths de render unificados)
- Popups con Aceptar/Cancelar + Enter/Esc

## Lo que yo complete en esta ronda
- Build paso (todo compila, tests OK)
- Documentacion actualizada

## Pendiente honesto
- Lineas: los estilos existen pero la diferenciacion visual es sutil (dash patterns)
- Poligonos: los estilos existen, hace falta mejor preview
- Map frame: mejoro pero sigue siendo render del MapPanel, no vectorial independiente
- Simbologia: unificada en puntos, falta mismo tratamiento en lineas/poligonos
