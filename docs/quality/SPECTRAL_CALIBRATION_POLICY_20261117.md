# Índices espectrales — política de calibración (2026-11-17)

Issue #139 (audit #263). Estado: **núcleo certificado**; la vista previa 8-bit del
diálogo queda marcada como no profesional.

## Problema

`SpectralIndexEngine` calculaba NDVI/EVI/BSI sobre los valores crudos del raster
(o del preview 8-bit del diálogo) sin `scale_factor`/`add_offset`, sin NoData y
sin validar que los valores estuvieran en dominio de reflectancia. Cualquier
"índice profesional" así calculado podía ser numéricamente inválido.

## Política

- Los índices profesionales se calculan con
  `SpectralIndexEngine.computeAnalysis(...)` y devuelven un
  `AnalysisRaster` **float** con CRS/extent de origen.
- Se exige `BandCalibration.reflectance(scale, offset, noData, source)` para
  cada banda; sin calibración explícita el cálculo **falla cerrado** con mensaje
  accionable (no se asume identidad).
- Se aplica primero `raw * scale + offset`; luego se validan:
  - `NoData` del raster y `_FillValue`/`noData` declarado → NoData;
  - no finitos → NoData;
  - reflectancia fuera de `[-0.05, 1.05]` → NoData (tolerancia explícita).
- Las bandas deben compartir grilla, dimensiones y CRS; no hay resample
  implícito.
- La vista previa 8-bit del diálogo (`computeIndex` legacy) se mantiene para
  inspección visual pero **no** se presenta como índice profesional: el diálogo
  lo advierte explícitamente.

## Evidencia

`SpectralCalibrationContractTest` (6):

1. NDVI con `scale=1e-4`: (0.8−0.2)/(0.8+0.2)=0.6 exacto.
2. Bandas sin calibración rechazadas con motivo.
3. `_FillValue` → NoData (no índice 0).
4. Reflectancia > 1 → NoData.
5. EVI de 3 bandas con oráculo numérico.
6. Grillas/CRS incompatibles fallan cerrado.

Suite local: 1650 tests, 0 fallos, 15 skips.

## Límite declarado

- La ruta GUI actual sigue mostrando una vista previa de visualización; el flujo
  de escritorio que conecte bandas GeoTIFF calibradas al API profesional queda
  como trabajo de UI (T5), sin declarar que el preview sea un producto
  profesional.
