# LiDAR — política de preview explícita (2026-11-17)

Issue #176 (audit #263). Estado: **núcleo implementado (PARCIAL)**; quedan
etiquetas de UI/analítica extendidas.

## Problema

El cargador LAS exponía una capa con los primeros 50.000 puntos como si fueran
todo el dataset, con envelope del encabezado completo: selección, estadísticas,
exportes y análisis podían tratar una muestra sesgada como datos completos.

## Solución

- **`LasPreviewPolicy.plan(total, max)`**:
  - `total <= 50.000` → carga completa verificada (`preview=false`);
  - `total > 50.000` → **muestra estratificada** con `stride=ceil(total/max)` y
    mensaje `PARTIAL_PREVIEW: muestra ... de ... puntos; no es el dataset completo`.
- **`LasReader.readSampledPoints`**: lee ≤50.000 registros directamente por
  offset (paso `stride`), sin cargar el archivo completo; el lector secuencial
  existente conserva su límite documentado.
- **`Layer`** gana estado tipado persistente: `partialPreview`,
  `totalSourceFeatures`, `loadedSourceFeatures`; se serializa/restaura en el
  proyecto (`PARTIAL_PREVIEW`, `TOTAL_SOURCE_FEATURES`,
  `LOADED_SOURCE_FEATURES`).
- **Envelope de la muestra**: en preview el extent refleja los puntos cargados,
  no el encabezado completo; los conteos total/cargado quedan como metadata.
- **Guarda de exportación** (`LasPreviewPolicy.requireCompleteForExport`):
  una capa preview no se exporta como dataset completo; el flujo de UI informa
  el motivo.
- LAZ comprimido y formatos de punto 4–10 siguen **fallando cerrados** con guía
  PDAL/GDAL (comportamiento previo).

## Evidencia

`LasPreviewPolicyContractTest` (4): plan completo/preview, muestreo
estratificado y acotado sobre LAS sintético de 100.001 puntos, roundtrip de
estado en la línea de proyecto y guarda de exportación.
`LasHeaderIntegrityContractTest` actualizado al nuevo contrato.

Suite local: 1676 tests, 0 fallos, 15 skips.

## Estado UI/analítica

- **Tabla de atributos**: título, subtítulo y barra de estado muestran
  `PREVIEW - N de TOTAL puntos (muestra estratificada)`; indica que los análisis
  profesionales están bloqueados.
- **Validación topológica**: `requireCompleteForAnalysis` bloquea ejecutar sobre
  una capa preview con aviso al usuario.
- **Exportación**: ya bloqueada (`requireCompleteForExport`).

## Cobertura de guards (cierre funcional)

- **Lista de capas**: sufijo `[PREVIEW]` en el nombre.
- **Tabla de atributos**: título/subtítulo/estado con `PREVIEW - N de TOTAL`.
- **Análisis profesionales bloqueados** con aviso accionable:
  exportación (`requireCompleteForExport`), validación topológica,
  calculadora de campos, geoprocesamiento y clasificación ML
  (`requireCompleteForAnalysis`).
- La guarda es central en `LasPreviewPolicy`: cualquier nuevo flujo profesional
  debe llamarla antes de consumir una capa.
- **Límite**: dataset completo >50k requiere lector indexado; LAZ y formatos de
  punto 4–10 siguen fallando cerrados con guía PDAL/GDAL. No se declara soporte
  de dataset LAS completo >50k.
