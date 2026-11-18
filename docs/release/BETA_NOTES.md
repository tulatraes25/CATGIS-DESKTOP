# CATMAP / CATGIS Desktop 1.0.0-beta.2 — Notas de la beta

Fecha: 2026-11-16. Canal: `beta` (pre-release).

## Lo nuevo en la beta

- **CATMAP standalone** utilizable sin CATGIS: launcher propio, splash y menu
  **Analisis** con curvas de nivel, drenaje, cuencas, inundacion, riesgo,
  viewshed y perfil.
- **OGC y PostGIS opcional** desde CATMAP (WMS/WFS y **WMTS** por plantilla REST/KVP GetTile para matrix sets compatibles XYZ).
- **Workspace de datos** con simbologia y trabajos/resultados.
- **Beta identity/CLI**: `1.0.0-beta.2`, `--version`, `--self-test` (13 checks
  headless) y "Acerca de" con la version real.
- Capacidades certificadas previamente en esta linea: PDF vectorial multipagina,
  SVG vectorial, GeoTIFF, atlas, plantillas, rasters analiticos, drenaje nativo,
  morfometria, DEM acondicionado, riesgo calibrado, IDW, viewshed, volumen,
  perfil, paleta de comandos, temas/HiDPI, i18n honesta y salida segura.

## Verificacion

- Build local: 1576 tests / 0 fallos / 15 skips.
- CI: PR checks completos (Certification Linux/Windows/macOS Intel+ARM, GDAL
  nativo, PostGIS real, app images, Runtime Identity 17/21, inventarios).
- Post-main: CI + Certification + Runtime Identity + GeoParquet y
  `main-push-attestation` PASS sobre el SHA de main de cada tranche.

## Limitaciones declaradas

El detalle por capacidad vive en `docs/CAPABILITY_MANIFEST.md`. Resumen: WMTS
soportado para matrix sets XYZ; splash simple; instaladores sin firma; A0.5/A0.7/
F3 GUI `NOT_CERTIFIED`; T2–T4 SQL implementados y certificados (DC-5 sigue
pendiente de decisión).

- WMTS soportado (REST o KVP GetTile) para matrix sets compatibles con XYZ; otros requieren adaptacion.
- Splash de CATMAP simple, sin arte dedicado.
- Sin firmas/notarizacion de instaladores.
- A0.5/A0.7/F3 `NOT_CERTIFIED`: no se declara GUI/runtime nativo verificado.
- T2–T4 SQL: decision A/A/A registrada; implementacion en tranche posterior.

## Politicas SQL del servidor (T2-T4)

Las decisiones DC-6/DC-7/DC-8 (vista canónica, ownership/default privileges y
rutas destructivas con stage+swap) estan implementadas en CATSERVER y validadas
por el job PostGIS de CI (`_tests/dc6_*`, `_tests/dc7_*`,
`DestructiveScriptPolicyTest`).

## Firma opcional y checksums

Los artefactos se publican sin firma salvo que el repositorio configure los
secretos de firma (`docs/RELEASE_SIGNING.md`). Cada artefacto incluye su
`.sha256` para verificacion.

## Artefactos

Esta pre-release adjunta app-images de Linux/Windows/macOS Apple Silicon construidos por el
workflow `release-beta.yml`. No se publican binarios firmados.
