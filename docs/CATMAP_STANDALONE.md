# CATMAP standalone

CATMAP es el compositor cartografico de CATGIS Desktop y se puede abrir como
aplicacion propia, sin cargar un proyecto CATGIS.

## Launcher independiente

- Windows: el instalador agrega el launcher **CATMAP Standalone**
  (`--add-launcher` en `generateWindowsInstallerAssets`).
- Linux: el app-image agrega el launcher **CATMAP** (`bin/CATMAP`) con
  `--add-launcher` en `packageLinuxAppImage`; `verifyLinuxAppImage` exige que
  exista y sea ejecutable.
- Ambos launchers usan `main-class=ar.com.catgis.catmap.Main`, por lo que no
  necesitan abrir la ventana de CATGIS.

## Flujo de trabajo sin CATGIS

1. Abrir CATMAP (launcher propio o `--catmap-standalone`).
2. Cargar datos sueltos con **Archivo -> Abrir datos...** (SHP, GeoJSON, KML,
   GPKG, FlatGeobuf, GPX, GeoTIFF, imagen, CSV con CRS explicito o inferido).
3. Componer la plancheta (mapa, leyenda, escala, norte, textos, tablas).
4. Exportar **PDF vectorial multipagina**, PNG/JPG, **SVG vectorial** o
   **GeoTIFF** del marco de mapa; imprimir todas las paginas.
5. Guardar/abrir `.catmap` sin proyecto CATGIS; el mapa se rellena cuando hay
   capas disponibles.

## Capacidades de analisis (P4)

Curvas de nivel, cuencas y morfometria, redes de drenaje con umbral por area,
inundacion preliminar, susceptibilidad/riesgo calibrado (WGS84, parametros
persistidos), viewshed, volumen, perfil/hipsometria/3D e IDW; DEM acondicionado
exportable a GeoTIFF/ASCII.

## Requisitos y limites

- Java 21; los app-images incluyen runtime propio.
- El estado visual/runtime **no** se declara verificado hasta que A0.7/F3 esten
  certificados (ver gates del repositorio); las verificaciones actuales son
  build, tests y empaquetado deterministico.
- Datos y proyectos del usuario no se modifican sin accion explicita.

## Comandos utiles

```bash
cd catgis-desktop
./gradlew build                 # checkstyle + tests + empaquetado configurado
./gradlew packageLinuxAppImage  # app-image Linux con launcher CATMAP
./gradlew verifyLinuxAppImage   # valida launcher CATMAP + runtime embebido
./gradlew uiSnapshot            # snapshot de UI (requiere display/Xvfb)
```
