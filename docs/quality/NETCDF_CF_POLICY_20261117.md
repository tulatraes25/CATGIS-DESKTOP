# NetCDF CF — política de CRS y packaging (2026-11-17)

Issue #162 (audit #263). Estado: **implementado y certificado**.

## Problema

El fallback UCAR (`NetCdfLoader.loadWithUcarNetCdf`) asignaba
`String crsCode = "EPSG:4326"` de forma incondicional y describía valores
empaquetados sin `scale_factor`/`add_offset` ni `_FillValue`, ignorando el
`grid_mapping` CF.

## Política

Orden de resolución del CRS (`NetCdfCfSupport.resolveCrs`):

1. `epsg_code` en la variable de `grid_mapping` (verificado decodable).
2. `spatial_ref` WKT decodable (autoridad EPSG resultante).
3. `grid_mapping_name=latitude_longitude` → **EPSG:4326** con provenance
   `CF latitude_longitude`.
4. Sin `grid_mapping`, coordenadas con unidades de grados
   (`degrees_east/north/...`) → **EPSG:4326** con provenance
   `CF degree coordinates`.
5. Cualquier otro `grid_mapping` (rotado/proyectado) sin CRS decodable →
   **falla cerrado**: no se etiqueta como WGS84; el loader informa y no crea la
   capa.

Reglas adicionales:

- Unidades de coordenadas: radianes → grados; longitudes 0..360 → -180..180.
- Packaging: `scale_factor`, `add_offset`, `_FillValue`, `valid_min/max` se
  aplican antes de calcular min/max y de la rampa visual; el fill se compara en
  el dominio empaquetado.
- Provenance expuesta en `climateCrsProvenance` de la capa.

## Evidencia

`NetCdfCfSupportContractTest` (9): epsg_code, latitude_longitude, rotado
fail-closed, grados sin grid_mapping, radianes, normalización de longitudes,
packaging/fill/valid range y defaults.

Suite local: 1644 tests, 0 fallos, 15 skips.

## Límites

- La lectura de atributos CF es por reflexión sobre `ucar.nc2` (dependencia
  existente); el fixture NetCDF real requiere la librería nativa y queda cubierto
  por la matriz de integración cuando se disponga de un `.nc` de referencia.
- Paquetes con CRS proyectado CF (`grid_mapping_name=lambert_azimuthal...`) sin
  `epsg_code`/`spatial_ref` siguen fail-closed; agregar el atributo es el camino
  soportado.
