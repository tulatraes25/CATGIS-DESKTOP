# CATGIS — A0.3 Tranche D: XYZ tile disk-cache isolation

Status: CANDIDATE / NOT_CERTIFIED
Parent main: `22961526117bf2e090450cd837042241f23af64b`
Branch: `quality/a0-xyz-tile-cache-off-edt`
Architecture overall: `NOT_CERTIFIED` until A0.7.

## Objective

Keep all synchronous filesystem metadata checks and PNG/JPEG decode out of
`OnlineTileCache.getTile`, which is called from live map rendering on Swing EDT.

## Root cause

Before this tranche, a memory-cache miss performed `tileFile.isFile()` and
`ImageIO.read(tileFile)` inline on the rendering thread. Class initialization
also called `CACHE_DIR.mkdirs()`. A slow cache directory or large/corrupt
tile could freeze pan/zoom despite network fetch being asynchronous.

## Requirements and constraints

- Return a cached in-memory tile immediately with the same image data.
- On a disk-cache hit, return `null` immediately, decode in the existing
  bounded six-worker pool, warm memory cache and post repaint on Swing EDT.
- Deduplicate pending requests, clear pending markers on success/failure,
  and handle rejected executor submissions during shutdown.
- Keep unavailable-provider placeholder recognition and retry logic.
- Avoid any new library, real internet request, committed PNG, or GUI session.
- Do not treat this partial EDT gate as A0.3 or A0.7 closure.

## Acceptance

`OnlineTileDiskCacheEdtContractTest` creates a real PNG at a unique XYZ
cache address and uses `SwingUtilities.invokeAndWait`. Its first `getTile`
call must return `null`, an asynchronous EDT repaint must occur, and the
next call must reproduce the pixel-exact image from warmed memory.

## Certification

PR CI and Certification (Linux, Windows, PostGIS, architecture, inventories)
must both be SUCCESS on the exact candidate SHA; then a PR merge with lease
must receive new SUCCESS runs for CI and Certification on the exact main SHA.
Do not start OpenCode or desktop runtime before A0.7.
