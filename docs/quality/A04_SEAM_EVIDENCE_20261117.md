# A0.4 — Evidencia de seams deterministas (2026-11-17)

Estado: **inventario completo con evidencia ejecutable en CI**. A0.7/F3 siguen
`NOT_CERTIFIED`: este documento no los declara.

A0.4 exige que las operaciones de red, PostGIS, GDAL y filesystem tengan
**seams deterministas** con aserciones reales en los límites. Inventario:

## Filesystem / publicación atómica

| Seam | Evidencia |
|---|---|
| Export CATMAP atómico (stage + publish) | `CatmapAtomicExportContractTest`, `CatmapSvgAtomicExportContractTest` |
| Guardado de documento CATMAP atómico | `CatmapAtomicSaveContractTest` |
| Publicación de proyecto `.catgis` | `ProjectFilePublisherContractTest` |
| Export vectorial publicado atómicamente | `VectorExportPublisherContractTest` |
| Lectura de proyecto con presupuesto/límites | `ProjectOpenReadBudgetContractTest`, `ProjectOpenRecordIntegrityContractTest` |
| Commit de apertura de proyecto (sin efectos parciales) | `ProjectOpenChooserCommitBoundaryContractTest`, `ProjectOpenServiceTest` |

## Red / HTTP

| Seam | Evidencia |
|---|---|
| Caché de tiles online portable + raíz configurable | `OnlineTilePortableCacheRootContractTest`, `OnlineTileDiskCacheEdtContractTest` |
| WMS: acceso a disco fuera de EDT y PNG exacto | `OnlineWmsDiskCacheEdtContractTest` |
| WCS: descarga atómica con límites | `WcsAtomicStreamDownloadContractTest` |

## GDAL / procesos externos

| Seam | Evidencia |
|---|---|
| Resolución portable de GDAL (sin inventar binario) | `GdalSupportPortableResolverContractTest` |
| Resolución de ejecutables externos (Whitebox) | `WhiteboxExecutableResolverTest` |

## PostGIS

| Seam | Evidencia |
|---|---|
| Roundtrip real PostGIS en CI (servicio PostGIS 16-3.4) | `postgisIntegrationTest` + job **PostGIS Real Integration** |
| Pipeline SQL canónico, políticas T2–T4 y fail-closed | job PostGIS: `_tests/dc6_*`, `_tests/dc7_*`, guards de base/máscara |

## Criterio

Todas las fronteras listadas tienen un contrato determinista (unitario o de
integración real en CI). Las dependencias opcionales (GDAL nativo, DWG, LAS,
WCS/STAC) están gated/labeled y no se declaran "fully supported" sin runtime
certificado. Los seams de GUI interactiva pertenecen a F3/A0.7 y no se cubren
aquí.

## Evidencia de ejecución

- Los contratos corren en cada PR (Certification Linux/Windows/macOS) y en
  `postgisIntegrationTest` dentro del job PostGIS.
- Cierre certificado en PR #255 (head `020dd69`) → `main` `6e4b077` con
  Certification/CI/Runtime Identity/GeoParquet SUCCESS y attestation
  `44200096397`. **A0.7/F3 siguen `NOT_CERTIFIED`.**
- Build local de referencia: 1588 tests / 0 fallos / 15 skips (pre-harness E2E).
