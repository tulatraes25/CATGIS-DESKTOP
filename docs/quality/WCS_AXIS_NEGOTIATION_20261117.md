# WCS 2.x — negociación de ejes DescribeCoverage (issue #108, núcleo) — 2026-11-17

Estado: **INTEGRADO Y VERIFICADO**. El flujo de descarga de la UI usa
`DescribeCoverage` para direccionar los ejes nativos; el parser/planificador y la
URL están cubiertos por tests con fixtures de servidor capturados.

## Problema

`WcsClient.buildGetCoverageUrl20` fijaba `SUBSET=x/y` y `SCALESIZE=x/y`. Los
servidores WCS 2.0.1 pueden exponer etiquetas de eje nativas distintas de `x/y`
y un orden de CRS que no se deduce del EPSG pedido: el resultado podía ser una
extensión geográfica incorrecta con apariencia de éxito.

## Implementación

- **`WcsDescribeCoverageAxes.parse(byte[])`**:
  - XML parseado con `OgcCapabilitiesXml.parse` (sin DOCTYPE/entidades externas)
    y límite de **4 MiB**.
  - Localiza `CoverageDescription` y su `boundedBy/Envelope`; exige `srsName`.
  - Exige **exactamente dos** etiquetas de eje (`axisLabels` del Envelope o del
    `Grid`); mapea ejes horizontales **por significado**:
    - x/lon/long/longitude/e/east/easting
    - y/lat/latitude/n/north/northing
  - Falla cerrado si falta `srsName`, hay 0/1/>2 etiquetas, duplicados o
    etiquetas desconocidas/ambiguas (`i j`, `lon lon`): **nunca adivina orden**.
- **`WcsDescribeCoverageAxes.dimensionParameters(plan, bbox, width, height)`**:
  - Valida BBOX de 4 coordenadas finitas y no invertidas.
  - Genera `SUBSET=<labelX>(min,max)`, `SUBSET=<labelY>(min,max)`,
    `SCALESIZE=<labelX>(width)`, `SCALESIZE=<labelY>(height)` URL-encoded.

## Evidencia

`WcsDescribeCoverageAxesContractTest` (5): `lat lon` y `N E`/`x y` mapeados por
significado, ambigüedad/sin CRS/una sola etiqueta fail-closed, parámetros con
etiquetas nativas y BBOX validado, documentos vacíos/malformados/sobre-dimensionados.

Suite local: 1681 tests, 0 fallos, 15 skips.

## Integración (cierra el alcance funcional de #108)

- `WcsClient.describeCoverageAxes` hace un fetch acotado (máx. 4 MiB) de
  `DescribeCoverage` por `COVERAGEID`, con caché LRU de 64 entradas por
  servicio+cobertura y URL base validada (sin `REQUEST=` previo).
- `downloadCoverageToFileNegotiated` usa la URL WCS 2.0.1 con etiquetas nativas;
  ante fallo/ambigüedad se negocia a WCS 1.0.0 estándar (BBOX/CRS), **nunca** se
  emite un `SUBSET=x/y` adivinado. Presupuesto/deadline/lock/staging y validación
  del GeoTIFF entregado (#43) se comparten con la ruta existente.
- `WcsDialog` usa la ruta negociada para descargas reales.
- `WcsNegotiatedDownloadContractTest` (3): URL capturada con `SUBSET=lon/lat` y
  `SCALESIZE`, caché de DescribeCoverage, ambigüedad → v1 sin V2 GetCoverage,
  DescribeCoverage inválido → v1, fallo total preserva salida previa.
- La UI sigue ejecutando la descarga fuera del EDT (`WcsRasterAsyncLoad`).
- A0.7/F3 siguen `NOT_CERTIFIED` (GUI física residual).
