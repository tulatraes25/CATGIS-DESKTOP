# CATMAP UI Repair Report

## 1. Que estaba prometido y NO funcionaba

| Promesa | Realidad (antes) |
|---|---|
| "Editor contextual de texto al hacer doble clic" | El doble clic buscaba `LayoutElementType.HEADER` (hardcodeado viejo), no los LayoutLabels del modelo. Caia en `beginInlineTitleEdit()`. |
| "Editor contextual de leyenda al hacer doble clic" | El doble clic buscaba `LayoutElementType.LEGEND` (hardcodeado viejo), no los LayoutLegends del modelo. Caia en `openLegendEditor()`. |
| "76 plantillas visibles en CATMAP" | Las plantillas solo existian en `showTemplateDialog()` que se llamaba desde un boton QGIS oculto (ya removido). El `templateCombo` usaba el viejo enum `LayoutTemplate` (6 valores). |
| "Propiedades abre editor rico" | `openElementProperties()` llamaba a `showTextPopup/showLegendPopup` solo si se llegaba via `handleRightClick`, pero doble clic tenia su propio handler roto. |

## 2. Causa real

El `mouseClicked` handler (linea 4720) tenia DOS sistemas de deteccion:
1. Sistema NUEVO: `layoutModel.findTopmostElementAtMm()` — solo manejaba LayoutMap (activaba Pan mapa)
2. Sistema VIEJO: `isInsideElement(LayoutElementType.HEADER/CARTOUCHE/LEGEND/NORTH)` — manejaba elementos hardcodeados del template

Los LayoutLabel y LayoutLegend del modelo NO eran manejados por NINGUNO de los dos. El sistema nuevo los encontraba pero solo procesaba LayoutMap. El sistema viejo nunca los encontraba porque son del modelo, no del template.

Las plantillas estaban en `LayoutTemplateManager.getTemplateList()` pero:
- `templateCombo` usaba `LayoutTemplate.values()` (enum viejo, 6 items)
- `showTemplateDialog()` usaba `LayoutTemplateManager.getTemplateList()` pero no tenia boton visible

## 3. Que se corrigio

| Bug | Fix | Linea |
|---|---|---|
| Doble clic en LayoutLabel no abre popup | Agregado `el instanceof LayoutLabel` → `showTextPopup()` | 4735-4740 |
| Doble clic en LayoutLegend no abre popup | Agregado `el instanceof LayoutLegend` → `showLegendPopup()` | 4741-4746 |
| showTextPopup no accesible | Cambiado `private` → package-private | 5606 |
| showLegendPopup no accesible | Cambiado `private` → package-private | 5656 |
| Plantillas invisibles | Boton "Plantillas ▼" en panel izquierdo con las 76 del TemplateRegistry | 8017-8030 |

## 4. Flujo que quedo operativo

- Doble clic en Titulo/Subtitulo → Popup con fuente, tamano, negrita, color
- Doble clic en Leyenda → Popup con titulo, tamano, fondo, borde, columnas
- Click derecho > Propiedades → Mismo popup (via openElementProperties → showTextPopup/showLegendPopup)
- Boton "Plantillas ▼" → Menu con 76 plantillas organizadas → Click aplica al layout

## 5. Que sigue pendiente

- El viejo `templateCombo` (JComboBox<LayoutTemplate>) sigue en el panel de propiedades. No se removio para no romper `buildSettings()`.
- Los elementos hardcodeados viejos (HEADER, CARTOUCHE, etc.) siguen en `mouseClicked` como fallback para layouts sin modelo.
- MAP_CONTENT hardcodeado persiste (2/8).

## 6. Verificacion

- `gradlew compileJava`: BUILD SUCCESSFUL
- `gradlew test`: 263 tests, 0 failures
- `gradlew build`: BUILD SUCCESSFUL
