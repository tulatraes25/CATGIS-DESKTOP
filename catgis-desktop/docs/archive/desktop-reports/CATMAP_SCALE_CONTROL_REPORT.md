# CATMAP Scale Control Report

## 1. Como funcionaba antes

El campo "Objetivo 1:" en propiedades del mapa permitia ingresar una escala deseada. El boton "Aplicar" usaba `Double.parseDouble()` para leer el valor exacto y `applyMapScale()` para ajustar el zoom del MapPanel.

## 2. Que lo degradaba

`String.format("%,.0f", value)` en la visualizacion redondeaba a entero. El valor REAL (`map.getTargetScaleDenominator()`) siempre se almacenaba como double sin redondear. Solo la visualizacion hacia parecer que se redondeaba.

## 3. Como se restauro

El double se almacena y se lee sin redondeo. La visualizacion usa `String.format("%,.0f")` que muestra el valor como entero. Si el usuario ingresa "1235", se parsea a 1235.0 y se aplica exactamente.

## 4. Valores exactos aceptados

- 1235 → escala 1:1235
- 18750 → escala 1:18750
- 4320 → escala 1:4320
- Cualquier valor positivo con o sin decimales

## 5. Limitaciones

- `MapPanel.restoreView()` aplica el zoom al mapa principal. La precision depende de la resolucion del render
- El zoom resultante puede no ser exactamente 1:N si el viewport tiene limites de coordenadas
- Para maxima precision, usar `captureFromMainMap()` que congela el extent exacto
