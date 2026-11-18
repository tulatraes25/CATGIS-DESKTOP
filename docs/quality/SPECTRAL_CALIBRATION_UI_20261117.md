# Índices espectrales calibrados — UI explícita (issue #139) — 2026-11-17

Estado: **UI conectada al motor calibrado con contrato explícito**. El motor
float/NoData ya estaba certificado; esta tranche elimina el cálculo silencioso
sobre DN sin calibración.

## Contrato de entrada

`SpectralCalibrationInput.parse(...)` (nuevo):

- Exige que el usuario **declare** que las bandas de entrada ya están calibradas
  a reflectancia; sin declaración → `IllegalArgumentException` accionable, sin
  cálculo ni capa.
- Exige `scale_factor` finito y distinto de cero, `add_offset` finito y NoData
  opcional finito, por banda A/B.
- Construye `SpectralIndexEngine.BandCalibration.reflectance(...)` con
  procedencia (`Declaración manual del usuario` por defecto) y la expone en el
  resultado.

## Flujo UI

- `SpectralIndexDialog` incorpora checkbox de declaración, campos scale/offset/
  NoData para A y B y campo de procedencia.
- El cálculo usa **`SpectralIndexEngine.computeAnalysis`** con dobles calibrados,
  propagate NoData y valida rango de reflectancia; los índices de 3/4 bandas
  siguen bloqueados en este flujo de dos bandas (nunca fórmula simplificada).
- La capa agregada es una **vista de color**; el `AnalysisRaster` float se
  conserva en `LocalRasterData.getAnalytical()` y se puede exportar como
  **GeoTIFF float con NoData/CRS** (`Guardar GeoTIFF analítico...`), sin codificar
  el único resultado como bytes de display.
- Si no hay celdas válidas tras calibración/NoData, no se agrega capa y se
  explica el motivo. Sin calibración declarada no se calcula nada.

## Evidencia de tests

`SpectralCalibrationInputContractTest` (4):

- entrada sin declaración / scale vacío / scale cero / no numérico / offset NaN → rechazo;
- calibración declarada conserva scale, offset, NoData y procedencia;
- **offset aditivo cambia materialmente NDVI** (>0.1) respecto de solo escala;
- NoData declarado se propaga como NoData en el resultado.

Más `SpectralCalibrationContractTest` del motor (casos escala común, offset,
bandas distintas, NoData y diferencia material DN vs calibrado).

## Límites declarados

- La calibración se **declara en la UI**; no se resuelve automáticamente desde
  metadatos de archivos genéricos (esos metadatos no son fiables/portables).
- El resultado analítico vive en memoria de la capa; la persistencia en el
  proyecto de rasters analíticos derivados no se promete en esta tranche; se
  exporta el GeoTIFF analítico.
- A0.7/F3 GUI física siguen `NOT_CERTIFIED`.
