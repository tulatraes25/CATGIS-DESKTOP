# CATMAP / CATGIS Desktop 1.0.0-beta.1 — Notas de la primera beta

Fecha: 2026-11-16. Canal: `beta` (pre-release).

## Lo nuevo en la beta

- **CATMAP standalone** utilizable sin CATGIS: launcher propio, splash y menu
  **Analisis** con curvas de nivel, drenaje, cuencas, inundacion, riesgo,
  viewshed y perfil.
- **OGC y PostGIS opcional** desde CATMAP (WMS/WFS; WMTS declarado no soportado).
- **Workspace de datos** con simbologia y trabajos/resultados.
- **Beta identity/CLI**: `1.0.0-beta.1`, `--version`, `--self-test` (13 checks
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

- WMTS no soportado; WMS/WFS/PostGIS si.
- Splash de CATMAP simple, sin arte dedicado.
- Sin firmas/notarizacion de instaladores.
- A0.5/A0.7/F3 `NOT_CERTIFIED`: no se declara GUI/runtime nativo verificado.
- T2–T4 SQL: decision A/A/A registrada; implementacion en tranche posterior.

## Artefactos

Esta pre-release adjunta app-images de Linux/Windows/macOS construidos por el
workflow `release-beta.yml`. No se publican binarios firmados.
