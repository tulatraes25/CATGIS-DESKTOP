# WCS — integridad científica del payload (issue #43) — evidencia 2026-11-17

Estado: **ACEPTACIÓN VERIFICADA** en el código actual (`main` tras #282). El
transporte WCS ya no publica bytes arbitrarios como raster: la validación
científica se ejecuta sobre el archivo en staging antes del rename atómico.

## Requisitos de #43 → evidencia

| Requisito | Implementación | Tests |
|---|---|---|
| Firmas TIFF little/big endian y BigTIFF | `WcsClient.validateStagedTiff` verifica `II*\0`/`MM\0*` y `II+\0`/`MM\0+` | `randomBinaryAdvertisedAsTiffCannotOverwriteExistingCoverage`, `tiffHeaderWithoutImageDirectoryCannotBePublished`, `geoserverTifAliasMustNotBypassTiffSignatureValidation` |
| Decoder confiable antes de reemplazar | `ImageIO` ImageReader del staging con formato TIFF detectado | `malformedV2TiffMustFallBackToGenuineV1CoverageWithoutStaleBytes`, `invalidPixelLayoutInWcs20FallsBackToReadableWcs10WithoutStaleBytes` |
| Rechazar bytes arbitrarios / headers roto | Validación de firma, ImageReader, `TiffPrimaryStorageValidator`, muestras de píxel | `tiffWithValidDimensionsButPixelOffsetsOutsideFileCannotReplaceOldCoverage` |
| Ancho/alto solicitados | Comparación de dimensiones decodificadas contra grilla efectiva | `defaultedZeroGridCannotSilentlyAcceptWrongSizedGeoTiff`, `realTiffWithWrongDimensionsCannotReplaceOldFile` |
| CRS/extent cuando el caller los exige | `validateStagedGeoMetadata` compara BBOX/CRS embebidos | `wrongGeoTiffEnvelopeMustNotReplaceValidRasterEvenWithCorrectPixelGrid`, `wrongEmbeddedGeoTiffCrsMustNotReplaceExistingRaster`, `matchingGeographicCoveragePublishesAndReleasesFileHandlesOnBothOperatingSystems` |
| Preservar salida y limpiar staging en todo fallo | staging privado + `publishStagedCoverage` atómico; `finally` borra `.part` | `failedVersionsPreserveThePreviousOutputAndCleanTemporaryFiles`, `unsupportedAtomicReplacementPreservesPreviousCoverageAndCleansStaging` |
| Streaming/bounded memory | límites de bytes/cabeceras, lecturas chunked y presupuesto | `WcsBoundedInMemoryContractTest` (6 tests) |
| WCS 2.0.1 → 1.0.0 fallback | fallback sólo ante fallo de payload/HTTP, sin reset de presupuesto | `fallsBackToWcs10AndKeepsOnlyTheCompletedRaster`, `failedVersionCannotResetAggregateBudgetForValidFallback` |
| EDT aislado | worker async para descarga/decode | `WcsRasterAsyncLoadContractTest` (5 tests) |
| Formatos no-TIFF explícitos (p.ej. PNG) | `expectsTiff` sólo exige firma cuando el formato pedido es TIFF/GeoTIFF | `explicitPngFormatDoesNotRequireTiffSignature` |

## Evidencia de ejecución

- `WcsAtomicStreamDownloadContractTest` (34 casos) + `WcsBoundedInMemoryContractTest`
  (6) + `WcsRasterAsyncLoadContractTest` (5) + `WcsNamespaceCapabilitiesContractTest`
  (7) + `WcsDimensionsPreflightContractTest` (6) ejecutados en cada PR (Certification
  Linux/Windows/macOS) y en `main` certificado.
- Suite local de referencia: 1676 tests / 0 fallos / 15 skips.

## Límites que NO cierra #43

- **#108** (WCS 2.x): negociación de ejes nativos vía `DescribeCoverage` y orden
  CRS. Sigue **OPEN**; no se declara soporte universal de ejes WCS 2.x.
- La certificación GUI interactiva (A0.7/F3) sigue `NOT_CERTIFIED`.
