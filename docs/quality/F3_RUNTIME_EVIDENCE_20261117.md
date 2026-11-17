# F3 — Evidencia runtime (2026-11-17)

Estado: **F3 PARCIAL**. A0.7/F3 completo siguen `NOT_CERTIFIED`; esta evidencia
no los sustituye ni declara GUI interactiva certificada.

## Lo ejecutado

| Evidencia | Cómo | Resultado |
|---|---|---|
| Launcher empaquetado `--version`/`--self-test` en Linux/Windows/macOS | `f3-runtime-smoke.yml` (app-image jpackage + ejecución del launcher) | 3/3 jobs SUCCESS (run `44080222592`) |
| UI real CATMAP en Linux Xvfb | `ui-snapshot.yml` → `uiSnapshot` (ventana Swing pintada a PNG) | SUCCESS (run `43960374643`) |
| E2E canónico headless CATMAP/CATGIS | `BetaE2EHarness` (`e2eBetaHarness`): proyecto guardado/reabierto con CRS y metadatos, lectura de SHP+GeoTIFF representativos, pendiente/hillshade + GeoTIFF/ASCII, CATMAP A3 con PNG/PDF/SVG/GeoTIFF reabiertos y roundtrip `.catmap` con hash | **E2E_OK checks=13** (local; CI en `f3-runtime-smoke.yml`) |

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
