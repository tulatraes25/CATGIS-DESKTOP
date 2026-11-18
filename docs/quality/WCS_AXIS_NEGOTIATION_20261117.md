# WCS 2.x — negociación de ejes DescribeCoverage (issue #108, núcleo) — 2026-11-17

Estado: **NÚCLEO IMPLEMENTADO (PARCIAL)**. El parser/planificador de ejes ya
existe y está certificado por tests; la integración en el flujo de descarga y UI
queda para el siguiente tramo. **#108 sigue OPEN**.

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

## Pendiente para cerrar #108

- `DescribeCoverage` real por `COVERAGEID` (fetch acotado) y caché por
  cobertura/servicio.
- Integración en `downloadCoverageToFile`/UI con fallback v2→v1 coherente y
  validación del GeoTIFF entregado (ya existe en #43).
- Fixtures de servidor que capturen la URL real de GetCoverage.
- A0.7/F3 siguen `NOT_CERTIFIED`.
