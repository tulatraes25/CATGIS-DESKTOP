# CATMAP — Resumen de cambios (2026-06-02) para Codex

## 3 issues resueltos

### #1 Norte top-right en TODAS las plantillas
- **Archivo**: `LayoutTemplateManager.java`
- `addMap` ahora guarda x/y/w/h en z[1-4]
- `addNorthAuto(m, id, size, z)` calcula posicion: mapa right - size - 4, mapa top + 4
- 22 plantillas usan `addNorthAuto` (reemplazo global con regex)
- `int[] z = {0}` → `int[] z = {0,0,0,0,0}` en todos los builders

### #2 Preview sin proyecto + Auto-componer con variantes
- **Archivo**: `LayoutMap.java`, `MapLayoutComposerDialog.java`
- `LayoutMap.render()`: si cachedImage es null, dibuja recuadro gris con "Mapa" centrado
- `autoComposeLayout()`: ahora muestra menu popup con 5 variantes (Ubicacion, Emplazamiento, Tecnica, Ambiental, Perfil) + acceso a galeria completa
- Ya no aplica una sola plantilla fija

### #3 Preview visual linea/poligono en dialogos de propiedades
- **Archivo**: `LayerPropertiesDialog.java`
- `LineStyleRenderer`: icono 80x16 con el patron de linea real (dash/dot/double/bold etc)
- `PolygonStyleRenderer`: icono 24x16 con textura real (hatch/dots/lineas)
- Ambos conectados a `lineStyleCombo.setRenderer()` y `polygonStyleCombo.setRenderer()`

## Commits en esta sesion
```
ba702c3 fix: Norte auto-posicionado top-right en TODAS las plantillas
7f63fe0 feat: Preview placeholder + Auto-componer 5 variantes
5cd0ace feat: Preview visual linea y poligono en combo propiedades
```

## Estado final del repo
- 268 tests, 0 failures
- BUILD SUCCESSFUL
- 22 plantillas curadas con nombres tecnicos
- A3 cambia pagina a 420x297mm automatico
- Propiedades en 6/6 elementos (Titulo, Leyenda, Cartucho, Escala, Norte, Mapa)
- 28 estilos punto, 18 linea, 18 poligono con preview visual
- Norte top-right automatico
- Instalador: `CATGIS Desktop Review-1.0.0.exe`
