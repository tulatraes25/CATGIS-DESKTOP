# CATGIS SPEC/Kiro — A0.3 WCS raster loading outside Swing EDT

Status: TRANCHE_A_CODE_CANDIDATE / GITHUB-FIRST / NOT_CERTIFIED
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

## Current implementation tranche A

Source has now been written **only in PR #93 draft**, not merged. The
preceding main push CI has succeeded, but predecessor push Certification
and authenticated attestation must be successful before merge. This
branch is expressly untrusted until exact-head CI and 15-job Certification
on all four OS targets pass.

- WcsDialog keeps download network I/O in its existing SwingWorker and
  asks resolution on EDT after the saved GeoTIFF completes; -1 cancels
  import without deleting the download.
- WcsRasterAsyncLoad<LocalRasterData> moves actual raster decoding into
  doInBackground, then runs the project/layer/map registration callback
  only from SwingWorker.done() on EDT.
- While either stage runs, Download is disabled. A Cancel Download button
  cancels **network transfer only**; full user cancellation of a native
  raster decode is still a separate UI tranche to avoid promising
  immediate interruption of uncooperative native libraries.
- Closing the dialog cancels the worker; an in-flight decode that ignores
  interrupts may finish later but its returned raster is disposed rather
  than registered. Callback ownership is enforced using AtomicReference.
- WCS layer commit uses a compensating rollback if registration throws.
  Failed import preserves the saved TIFF.
- Four headless tests cover EDT responsiveness with blocked background
  decoding, cancellation disposal, simulated publish failure, and
  closing/invalid chooser selection.

## Explicit residuals before claim of full #91 closure

- A new headless test now generates a **real tiny GeoTIFF** with CRS,
  geographic envelope, nonempty pixel grid and a filename containing
  spaces, then loads Preview and Full using real RasterImageLoader from
  the background worker. Its run is **NOT_VERIFIED** until exact-head
  CI; NoData semantics specifically remain untested in this test.
- The WcsDialog import transaction still needs direct injected
  failure/rollback verification, separate from the generic callback
  failure test.
- A cancel button for the *native raster decode* is not enabled by
  this tranche, because it may ignore interruption; user can close and
  the result will not be registered. Robust worker shutdown and
  component lifecycle acceptance remain for tranche B.
- Failure injection into real AppContext project+map commit and verified
  rollback is not yet executable in a headless harness.
- UI runtime on actual Windows/Linux/macOS remains unverified.

## Next gate

Run exact HEAD CI and Certification for the new source revision;
reconcile main post-PR92 event=push evidence, authenticate predecessor,
then normal expected-SHA merge + post-main CI/Certification/attestation.
Until then TRANCHE_A=NOT_CERTIFIED and issue #91/A0.3/A0.7 OPEN.

## First CI root-cause correction

Original source candidate at 2e870e87... failed Java compilation: the
constructor's Cancel-button ActionListener captured final statusLabel
before the latter was definitely assigned. The label is now initialized
before installing that listener (source commit 9e56efad...). No quality
gates have been relaxed; a new exact HEAD is subject to fresh CI.


## Windows native file-lock regression (2026-10-15)

Candidate `99a2a4c24dd64034bce850ac0ae31bfeb1c0ad31` passed Linux CI,
but Windows Certification job `116197026573` failed **one of 988
tests**, with 15 optional skips: `WcsRasterAsyncLoadContractTest.actualSmallGeotiffPreviewAndFullDecodeOnWorkerWithoutBlockingEdt`.
The JUnit XML artifact `11941170845` contains the actual suppressed
`java.nio.file.FileSystemException`: the input
`WCS coverage with spaces.tif` was still in use when JUnit attempted
temp-directory cleanup. This is a genuine Windows file-handle lifetime
bug/regression, not an infrastructure timeout. macOS/Linux passing does
not prove that Windows files close correctly.

The prior `RasterImageLoader.loadWithGeoTools` only invoked
`reader.dispose()`, without disposing its newly read and potentially
reprojected `GridCoverage2D` image graph. The correction explicitly
disposes the distinct display/source/original coverages in reverse
dependency order before the reader (after materializing an independent
BufferedImage for the map), with logged best-effort exceptions.
No change to geo data or on-disk source. The test additionally calls
`Files.delete(inputGeoTiff)` immediately after Preview and Full, and
must succeed on Windows without GC or retries. The corrected source
and test require fresh exact-HEAD CI and full all-platform Certification,
including a successful replacement Windows job. Do **not** relax
temp-directory cleanup, disable the test, or hide file locks with GC.

Remaining risks: the synthetic GeoTiffWriter fixture does not certify
all third-party TIFF codecs, ultra-large native datasets or physical
Windows GIS GUI; verify separately under A0.7.
