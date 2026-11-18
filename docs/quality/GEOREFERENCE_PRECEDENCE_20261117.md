# Georreferencia raster — decisión de precedencia (2026-11-17)

Issue #128 (audit #263). Estado: **implementado y certificado**.

## Problema

`RasterCoverageSupport.applySidecarMetadataIfPresent` reconstruía la cobertura
desde el sidecar **siempre**, incluso cuando el GeoTIFF traía GeoKeys embebidas.
Además `CRSDefinitions.normalizeCode("")` devuelve `EPSG:4326`, por lo que un
sidecar sin CRS podía terminar mostrando un CRS inventado (WGS84) y el extent
del sidecar reemplazaba al real.

## Decisión

| Situación | Georreferencia usada | Acción |
|---|---|---|
| GeoKeys embebidas y sidecar ausente | **Embebida** | Cobertura intacta |
| GeoKeys embebidas y sidecar coincide | **Embebida** | Cobertura intacta |
| GeoKeys embebidas y sidecar **contradice** CRS | **Embebida** | Sidecar **vetado** + warning de provenance; píxeles y extent no cambian |
| Sin GeoKeys, sidecar con CRS declarado decodable | **Sidecar** | Cobertura reconstruida con el extent del sidecar |
| Sin GeoKeys, sidecar sin CRS o no decodable | **Ninguna** | No se inventa CRS; cobertura intacta |

Reglas adicionales:

- Un CRS **vacío nunca** se normaliza a EPSG:4326 en decisiones de
  georreferencia (`declaredCode()` preserva vacío).
- El fallback de lectura por CRS de capa tampoco inventa 4326.
- `decideGeoreference()` expone `GeoreferenceSource` (EMBEDDED_GEOTIFF,
  SIDECAR, CONFLICT_KEPT_EMBEDDED, NONE), `conflict` y detalle legible.

## Evidencia

`GeoreferencePrecedenceContractTest` (4):

1. sidecar conflictivo vetado, CRS/extent embebidos preservados y **oracle de
   píxeles** idéntico al GeoTIFF leído directamente.
2. sidecar aplicado sólo cuando el TIFF no tiene GeoKeys.
3. sin sidecar ni GeoKeys: decisión `NONE`, sin CRS inventado.
4. sidecar coincidente no reemplaza GeoKeys.

Suite local: 1635 tests, 0 fallos, 15 skips.

## Límites

- Prioridad y veto cubren CRS + extent del sidecar; validaciones de checksum de
  datos remotos pertenecen a #161/#112.
- La normalización global de CRS (#163) mantiene el default 4326 para UI; sólo
  las decisiones de georreferencia usan `declaredCode`.
