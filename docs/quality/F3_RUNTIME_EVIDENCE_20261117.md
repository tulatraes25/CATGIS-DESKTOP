# F3 — Evidencia runtime (2026-11-17)

Estado: **F3 PARCIAL**. A0.7/F3 completo siguen `NOT_CERTIFIED`; esta evidencia
no los sustituye ni declara GUI interactiva certificada.

## Lo ejecutado

| Evidencia | Cómo | Resultado |
|---|---|---|
| Launcher empaquetado `--version`/`--self-test` en Linux/Windows/macOS | `f3-runtime-smoke.yml` (app-image jpackage + ejecución del launcher) | 3/3 jobs SUCCESS (run `44080222592`) |
| UI real CATMAP en Linux Xvfb | `ui-snapshot.yml` → `uiSnapshot` (ventana Swing pintada a PNG) | SUCCESS (run `43960374643`) |
| E2E canónico headless CATMAP/CATGIS | `BetaE2EHarness` (`e2eBetaHarness`): proyecto guardado/reabierto con CRS y metadatos, lectura de SHP+GeoTIFF representativos, pendiente/hillshade + GeoTIFF/ASCII, CATMAP A3 con PNG/PDF/SVG/GeoTIFF reabiertos y roundtrip `.catmap` con hash | **E2E_OK checks=13** (local y CI Linux; jobs Windows/macOS también SUCCESS) |

## Evidencia CI (main `6e4b077`)

- `ui-snapshot.yml` run **`44200187527`**: 3/3 jobs SUCCESS (Linux Xvfb,
  Windows, macOS Apple Silicon); artefactos `catmap-ui-snapshot-*` con la
  ventana real de CATMAP pintada a PNG.
- `f3-runtime-smoke.yml` run **`44200192526`**: 3/3 jobs SUCCESS; artefactos
  `f3-runtime-*` con `--version`, `--self-test` y el E2E. Reporte Linux
  descargado: **`E2E_OK checks=13`** con `plancheta.png/pdf/svg`,
  `plancheta-mapa.tif`, `pendiente.tif`, `hillshade.asc`, `e2e.catgis`,
  `layout.catmap`, `checksums.sha256`.

## Cobertura REQ-PC-3

Cubierto por el E2E: **2** (proyecto + CRS), **3** (vector/raster), **7**
(análisis de terreno), **9/10** (guardar y reabrir proyecto con estado), **11/12**
(CATMAP + export PNG/PDF, más SVG/GeoTIFF como extra).

Pendiente (requiere sesión GUI interactiva): **1** (launch visible), **4**
(navegar/zoom/pan/identify con interacción), **5** (inspección/styling por UI),
**6** (edición + undo/redo por UI), **8** (capa online en vivo), **13** (cierre
sin pérdida verificado por UI).

## Snapshots multiplataforma

`ui-snapshot.yml` ahora es una matriz Linux (Xvfb), Windows y macOS Apple
Silicon que pinta la ventana real de CATMAP a PNG. Si un runner no permite
crear ventanas, el job falla de forma visible y no se declara evidencia.

## Límites

- Evidencia de ejecución, no certificación de GUI: A0.7 exige los gates de
  arquitectura y una matriz CORE ejecutada.
- El E2E headless omite interacción humana; no reemplaza F3.2/F3.3/F3.4.
## Refresh 2026-11-17 (main `21bd9a6`)

- `ui-snapshot.yml` run **`44414255394`**: **3/3 jobs SUCCESS** (Linux Xvfb,
  Windows, macOS Apple Silicon) — la ventana real de CATMAP se pintó a PNG en
  cada runner.
- `f3-runtime-smoke.yml` run **`44414260382`**: **3/3 jobs SUCCESS**
  (app-image jpackage + launcher `--version`/`--self-test` + E2E) en Linux,
  Windows y macOS Apple Silicon.
- Esto **no** certifica A0.7/F3: falta la matriz interactiva humana (F3.2/F3.3/
  F3.4), HiDPI/foco reales, gestos del owner y validación física de impresión.
  `NOT_CERTIFIED` se mantiene.