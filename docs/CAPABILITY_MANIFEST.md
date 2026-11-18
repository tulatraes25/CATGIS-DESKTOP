# Manifiesto de versión y capacidad — CATGIS/CATMAP

Fuente única de verdad para versión y estado de capacidades. Derivado de
`AppVersion`, de las releases de GitHub y de la evidencia de CI al 2026-11-17.
Estados: **SUPPORTED_VERIFIED** (contrato + evidencia CI), **BETA_LIMITED**
(funciona con límites declarados), **REQUIRES_DEPENDENCY** (necesita binario/
servicio externo), **INSPECTION_ONLY** (sólo lectura de metadatos/preview),
**UNSUPPORTED** (no se declara soporte).

## Versión

| Campo | Valor |
|---|---|
| Producto | CATGIS Desktop / CATMAP / CATSERVER / Web viewer |
| Versión | **1.0.0-beta.2** (canal `beta`) |
| Release | `v1.0.0-beta.2` (pre-release, sin firma) |
| CLI | `--version`, `--self-test` (13 checks) |
| `main` de referencia | `c0484a1` (Certification/Runtime/CI/GeoParquet + attestation) |
| Estado GUI interactiva | **NOT_CERTIFIED** (A0.7/F3) |

## Capacidades (Desktop/CATMAP)

| Capacidad | Estado | Límite/evidencia |
|---|---|---|
| Composición CATMAP (página A3/A4/custom, atlas, plantillas) | SUPPORTED_VERIFIED | E2E y contratos CATMAP |
| Export PDF vectorial multipágina / PNG / SVG / GeoTIFF | SUPPORTED_VERIFIED | E2E 13/13 + contratos |
| Impresión física | BETA_LIMITED | sin impresora real certificada |
| Proyecto `.catgis` guardar/abrir | BETA_LIMITED | fail-closed/atómico (#144), codec v2 (#148) y roundtrip por tipo (#146); #149 abierto |
| Shapefile / GeoJSON / KML / GPX / CSV / GeoPackage | BETA_LIMITED | import/export verificados; familia SHP con journal atomico (#155); roundtrip/CRS con #146/#195/#150 |
| WMS / WFS / PostGIS | SUPPORTED_VERIFIED | contratos + PostGIS CI real |
| WMTS (REST/KVP, matrix sets XYZ) | SUPPORTED_VERIFIED | `WmtsSupportContractTest`; otros matrix sets requieren adaptación |
| XYZ/online tiles | SUPPORTED_VERIFIED | contratos de caché/EDT |
| WCS / STAC | BETA_LIMITED | issues de aceptación OGC (#108/#43) |
| FlatGeobuf | BETA_LIMITED | acotado; sin streaming grande (#175) |
| LAS/LAZ | BETA_LIMITED | rechazo >50k, sin LAZ (#176) |
| DWG/DXF | REQUIRES_DEPENDENCY | convertidor externo |
| NetCDF | BETA_LIMITED | CF/grid_mapping pendiente (#162) |
| Curvas de nivel, drenaje, cuencas, inundación, riesgo, viewshed, perfil | BETA_LIMITED | fixes T2 aplicados (#257/#258/#262); restan #162/#139 |
| IDW / volumen / morfometría | BETA_LIMITED | #257/#258 abiertos |
| Calculadora raster (float/NoData, GeoTIFF, preview 8-bit separado) | BETA_LIMITED | #256 API científica + tests; UI exporta GeoTIFF float |
| Catálogo CRS argentino (POSGAR 94, Campo Inchauspe, POSGAR 2007) | SUPPORTED_VERIFIED | validado contra EPSG/GeoTools (#259) |
| Paleta de comandos, temas, workspace, quick data, OGC opcional | SUPPORTED_VERIFIED | contratos + inventarios |
| GUI interactiva / HiDPI real / impresión | NOT_CERTIFIED | A0.7/F3 |
| Instaladores firmados/notarizados | UNSUPPORTED | secretos de firma no configurados |

## Servidor y web

| Capacidad | Estado | Límite |
|---|---|---|
| Pipeline SQL 00→21, políticas T2–T4 (vista canónica, privilegios, destructivo) | SUPPORTED_VERIFIED | PostGIS CI + `_tests/dc6/dc7` |
| DC-5 productor de máscara ejido | UNSUPPORTED | decisión de fuente pendiente |
| Visor web | BETA_LIMITED | sin E2E de navegación pública |

## Cómo verificar

```bash
java -jar catgis-desktop-1.0.0-beta.2.jar --version
java -jar catgis-desktop-1.0.0-beta.2.jar --self-test
sha256sum -c catgis-catmap-beta-linux.tar.gz.sha256
```

Ver `docs/quality/AUDIT_263_RECONCILIATION_20261117.md` para el estado por issue
y los gates de certificación.
