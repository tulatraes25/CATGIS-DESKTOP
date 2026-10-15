# CATGIS SPEC/Kiro — A0.3 WCS raster loading outside Swing EDT

Status: DRAFT / GITHUB-FIRST / NOT_IMPLEMENTED / NOT_CERTIFIED
Issue: #91
Protected-main parent: `c748a6eecfefd743df16e501921ec74f104fb500` (PR #92 merged).
Freeze: no new WCS features, no local GUI tests until A0.7 is satisfied.

## Evidence and root cause

`WcsDialog.downloadCoverage` correctly executes the HTTP transfer in
`SwingWorker.doInBackground()`, but `done()` then calls
`loadAsRasterLayer()` synchronously on the Swing Event Dispatch Thread.
This method invokes `RasterImageLoader.loadReal/loadPreview`, which may
perform disk read, GeoTIFF metadata/decode, reprojection and resource
materialization. On large GIS rasters this can stall **all** GUI input and
repaints. That is a source-level EDT violation, not an observed physical
FPS measurement. Further, closing the resolution chooser returns -1
and currently falls into the Preview branch, so cancel unexpectedly
loads a layer. Mutations to project/layer-list occur before map raster
registration and have no compensation on failure.

## Minimum design

1. Preserve successful WCS HTTP download and saved GeoTIFF exactly,
   including original dimensions, CRS and output path.
2. On EDT, after successful download, ask user to load Preview or Full
   and treat cancelled/closed chooser as "downloaded, no layer"; NEVER
   silently treat -1 as Preview.
3. Capture project identity and project CRS on EDT, before worker start.
   Use an injectable `RasterLoadService` in a headless-testable
   coordinator. Dispatch actual `RasterImageLoader.loadPreview/loadReal`
   into a second `SwingWorker.doInBackground()` and retain only tiny
   UI dispatch, layer preparation and project registration in `done()`.
4. Verify worker not cancelled and project identity unchanged before
   publication; no project mutation on loading failure/cancel and
   release disposable raster objects if a background load completes
   after cancellation.
5. A single visible in-flight operation per dialog: disable Download
   until network transfer and raster load finish, allow Cancel of
   the active SwingWorker (network worker interruption delegated to
   #92 core cancellation) and prevent stale completion callbacks
   overwriting the next operation's status.
6. Map/layer/project commit on EDT must compensate removals if map
   registration throws; test unchanged project/layer/model on injected
   failure. Do not claim a full transaction on native exceptions.
   User must still retain the successfully downloaded GeoTIFF on a
   layer-import failure.
7. Preserve native CRS and georeferencing by using the existing
   RasterImageLoader and RasterCoverageSupport implementations.
   Avoid expensive raster decoding in EDT or repaint callbacks.
8. Tests: headless Swing EDT responsiveness using a CountDownLatch
   loader; confirm success callbacks run on EDT and cancellation/error
   don't commit; explicit cancelled chooser; real tiny GeoTIFF
   Preview/Full pixel+CRS fixture with spaces in path. No production
   datasets, screenshot/mocks-only assertions or physical desktop.
9. Exact-head GitHub CI 4/4, Certification 15 mandatory platform jobs
   (Win/Linux/macOS Intel+ARM, GDAL native and PostGIS) and authenticated
   predecessor-main event=push CI+Certification. Normal SHA-checked
   merge, then fresh main CI/Certification and independent attestation.
   Never certify merely because an .app package exists.

## Current next step

This SPEC-only draft may serve as evidence PR for the predecessor main.
Before source changes, authenticate post-PR92 main CI+Certification on
`c748a6ee...`. Then implement and certify the source in this branch.
A0.3, A0.7, #86, #87 and #43 remain OPEN.
