# CATGIS / CATMAP — Primera beta usable

Estado: **beta** (`1.0.0-beta.2`). A0.5/A0.7/F3 siguen `NOT_CERTIFIED`: esta beta
no declara GUI ni runtime nativo certificados.

## Descarga

Pre-release: https://github.com/tulatraes25/CATGIS-DESKTOP/releases/tag/v1.0.0-beta.2
(Linux tar.gz, Windows zip, macOS Apple Silicon zip; sin firma).

## Requisitos

- Java 21 (los app-images incluyen su propio runtime).
- Windows 10/11, Linux x86_64 o macOS Intel/Apple Silicon.
- No requiere conexion: los datos locales funcionan offline; WMS/WFS/PostGIS son
  opcionales.

## Que incluye

- **CATGIS Desktop**: escritorio completo (capas, mapa, edicion, modulos).
- **CATMAP standalone**: compositor de planchetas sin abrir CATGIS (menu propio
  `CATMAP` en Windows / `bin/CATMAP` en Linux).
- **Cartografia**: pagina A3/A4/personalizada, PDF vectorial multipagina, SVG
  vectorial, PNG/JPG, GeoTIFF del marco de mapa, atlas por entidad, plantillas y
  QPT.
- **Terreno e hidrologia**: rasters analiticos exportables, curvas de nivel,
  redes de drenaje con umbral por area, cuencas y morfometria, DEM acondicionado,
  viewshed, perfil/hipsometria/3D, volumen corte/relleno e IDW.
- **Riesgo**: inundacion preliminar y susceptibilidad/riesgo calibrados (WGS84,
  parametros persistidos y trazables).
- **Datos**: abrir datos sueltos (SHP, GeoJSON, KML, GPKG, FlatGeobuf, GPX,
  GeoTIFF, CSV), WMS/WFS y PostGIS opcional; workspace de datos con simbologia y
  trabajos/resultados.
- **UX**: paleta de comandos, temas claro/oscuro, salida segura con confirmacion,
  toasts de carga masiva y workspace persistente.

## Primeros pasos

1. Abrir `CATGIS Desktop` o el launcher `CATMAP`.
2. En CATMAP: **Archivo → Abrir datos...** o **Mapa → Agregar servicio WMS/WFS**.
3. Componer la plancheta y exportar (**Exportar → PDF/PNG/SVG/imagen**).
4. Para analisis, usar el menu **Analisis** (curvas, drenaje, cuencas,
   inundacion, riesgo, viewshed, perfil).

## Verificacion rapida

```bash
java -jar catgis-desktop-1.0.0-beta.2.jar --version
java -jar catgis-desktop-1.0.0-beta.2.jar --self-test
```

`--self-test` no abre ventanas y verifica version/canal y las capacidades clave.

## Limitaciones declaradas de esta beta

- WMTS soportado como plantilla REST o KVP GetTile para matrix sets compatibles con la grilla XYZ (p. ej. GoogleMapsCompatible/EPSG:3857); otros matrix sets requieren adaptacion del operador.
- Splash propio de CATMAP es simple (sin arte grafico dedicado).
- Sin instaladores firmados/notarizados; artefactos de pre-release sin firma.
- T2–T4 SQL implementados y certificados en CATSERVER (PR #252); ver
  `docs/CAPABILITY_MANIFEST.md` para el estado por capacidad.
- GUI/runtime no certificados (A0.7/F3 `NOT_CERTIFIED`); la evidencia es
  CI/Certification + snapshot Xvfb sin claim.

## Reportar problemas

Incluir version (`--version`), sistema operativo, pasos y el resultado de
`--self-test`. No adjuntar datos personales ni credenciales.

## Manifiesto de capacidad

El estado exacto por capacidad (SUPPORTED_VERIFIED, BETA_LIMITED,
REQUIRES_DEPENDENCY, INSPECTION_ONLY, UNSUPPORTED) y la version vigente se
mantienen en **`docs/CAPABILITY_MANIFEST.md`**; ante cualquier discrepancia,
ese manifiesto prevalece sobre este README.