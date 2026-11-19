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
| `main` de referencia | `21bd9a6` (Certification/Runtime/CI/GeoParquet + UI snapshot/F3 smoke + attestation `44413954592`) |
| Estado GUI interactiva | **NOT_CERTIFIED** (A0.7/F3); evidencia headless UI snapshot + runtime smoke, sin matriz interactiva humana |

## Capacidades (Desktop/CATMAP)

| Capacidad | Estado | Límite/evidencia |
|---|---|---|
| Composición CATMAP (página A3/A4/custom, atlas, plantillas) | SUPPORTED_VERIFIED | E2E y contratos CATMAP |
| Export PDF vectorial multipágina / PNG / SVG / GeoTIFF | SUPPORTED_VERIFIED | E2E 13/13 + contratos |
| Impresión física | BETA_LIMITED | sin impresora real certificada |
| Proyecto `.catgis` guardar/abrir | BETA_LIMITED | fail-closed/atómico (#144), codec v2 (#148), roundtrip por tipo (#146) y snapshots de recuperación (#149) |
| Shapefile / GeoJSON / KML / GPX / CSV / GeoPackage | BETA_LIMITED | import/export verificados; familia SHP con journal atomico (#155); roundtrip/CRS con #146/#195/#150 |
| WMS / WFS / PostGIS | SUPPORTED_VERIFIED | contratos + PostGIS CI real; credenciales PostGIS con vault v2 + DPAPI/Keychain/Secret Service nativos (evidencia multi-OS; Linux sin secret-tool usa clave local declarada) |
| WMTS (REST/KVP, matrix sets XYZ) | SUPPORTED_VERIFIED | `WmtsSupportContractTest`; otros matrix sets requieren adaptación |
| XYZ/online tiles | SUPPORTED_VERIFIED | contratos de caché/EDT |
| WCS | SUPPORTED_VERIFIED | payload validado (#43) y ejes nativos vía DescribeCoverage con caché/fallback (#108) |
| STAC | BETA_LIMITED | pendiente aceptación OGC |
| DEM/suelo/clima online (OpenTopography, SoilGrids, WorldClim/Open-Meteo) | BETA_LIMITED | TIFF+sidecar atomicos (#161); georreferencia embebida #128 abierto |
| FlatGeobuf | BETA_LIMITED | frames en ventanas acotadas + cancelación (#175 parcial); lector indexado >100k pendiente |
| LAS/LAZ | BETA_LIMITED | rechazo >50k, sin LAZ (#176) |
| DWG/DXF | REQUIRES_DEPENDENCY | convertidor externo |
| NetCDF | BETA_LIMITED | CF grid_mapping/packaging implementado (#162); fixtures .nc reales pendientes |
| Georreferencia raster (GeoKeys vs sidecar) | SUPPORTED_VERIFIED | precedencia y veto de conflicto (#128) |
| Normalización CRS | SUPPORTED_VERIFIED | explícita vs default UI (#163) |
| Curvas de nivel, drenaje, cuencas, inundación, riesgo, viewshed, perfil | BETA_LIMITED | fixes T2 aplicados (#257/#258/#262) |
| Índices espectrales calibrados (NDVI/SAVI/BSI 2 bandas) | SUPPORTED_VERIFIED | UI con calibración explícita scale/offset/NoData y export GeoTIFF float analítico (#139); EVI 3 bandas bloqueado en este flujo |
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
