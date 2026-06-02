# CATMAP Backlog Post Audit

## 1. Estado de partida (segun CATMAP_CLAIM_AUDIT.md)

**Real**: 263 tests, 13 LayoutElements, 76 plantillas, leyenda profesional, WYSIWYG parcial
**Parcial**: Atlas (modelo sin UI), WYSIWYG (2/8 hardcodeados), biblioteca parametrica
**Placeholder**: Snap toggles (checkboxes sin logica)
**Mal nombrado**: "Geospatial PDF" (era sidecar .txt), porcentajes QGIS/ArcMap

**Corregido en ronda anterior**: Snap toggles documentados, PDF sidecar renombrado

## 2. Backlog priorizado

### CRITICO
| ID | Problema | Estado | Archivo | Accion | Resultado |
|---|---|---|---|---|---|
| C1 | Snap toggles son placeholders | **CORREGIDO** | MapLayoutComposerDialog | Conectar toggles a snapToGrid/snapToElements booleans en mouseDragged | Toggles controlan snap real |
| C2 | 2/8 hardcodeados persisten (MAP_CONTENT, CATMAP_ITEM) | PENDIENTE | LayoutRenderer interno | Migrar a LayoutElements o integrar al pipeline | WYSIWYG completo |

### ALTO
| ID | Problema | Estado | Archivo | Accion | Resultado |
|---|---|---|---|---|---|
| A1 | Atlas renderer no integrado a UI | PENDIENTE | LayoutAtlasRenderer, MapLayoutComposerDialog | Conectar PageRenderer con export UI | Atlas funcional |
| A2 | PDF sidecar mal documentado | **CORREGIDO** | MapLayoutComposerDialog | Renombrado a "referencia de coordenadas" | Nombre honesto |
| A3 | Porcentajes QGIS/ArcMap sin base | **DOCUMENTADO** | CATMAP_CLAIM_AUDIT.md | Usar wording honesto | Menos marketing |

### MEDIO
| ID | Problema | Estado | Archivo | Accion | Resultado |
|---|---|---|---|---|---|
| M1 | Plantillas parametricas inflan conteo | DOCUMENTADO | TemplateCatalog | Diferenciar disenos unicos (24) de variaciones (52) | Conteo honesto |
| M2 | Test count reportado como 268 (real 263) | DOCUMENTADO | Tests XML | Usar 263 | Precision |

## 3. Fase ejecutada en esta ronda

**Corregido**:
- C1: Snap toggles ahora controlan comportamiento real en mouseDragged
- A2: PDF sidecar renombrado honestamente
- Snap booleans (snapToGrid, snapToElements) agregados a LayoutPreviewPanel

**Pendiente**:
- C2: 2/8 hardcodeados (MAP_CONTENT, CATMAP_ITEM) - requieren refactor del template renderer
- A1: Atlas UI - requiere diseno de interfaz
- M1, M2: Solo documentacion

## 4. Claims honestos posteriores

Recomendado para uso publico:
- "CATMAP: compositor cartografico con 13 tipos de elementos, 263 tests, 76 plantillas"
- "Snap a grid y elementos durante arrastre (configurable)"
- "PDF con referencia de coordenadas del mapa"
- "Modelo de atlas para exportacion por paginas"
- "Acercamiento funcional a compositores GIS profesionales"
