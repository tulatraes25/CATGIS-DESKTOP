# CATGIS — SPEC/Kiro: durable XYZ cache root on Windows and Linux

Status: CANDIDATE / NOT_CERTIFIED
Base main: `47b4b890dbb313bc33c61ebf94fb793e17d4bc8a`
Branch: `quality/a0-xyz-portable-persistent-cache-root`
Related: A0.3 online cache, A0.7 certification OPEN.

## Confirmed root cause

After the previously certified provider-fingerprint and 128 MiB weighted LRU
work, `OnlineTileCache.CACHE_DIR` still used Windows `%APPDATA%\CATGIS`
only when the `APPDATA` environment variable happened to exist. Without
APPDATA (normal Linux Mint) it fell back to `java.io.tmpdir/catgis-online-tiles`.
Many Linux systems clear /tmp on reboot, defeating persistent offline basemaps.

## Requirements

- Windows: preserve **exact** former %APPDATA%/CATGIS/online-tile-cache
  path so existing caches and source-safe v2 namespaces remain usable.
- Linux: use absolute `XDG_CACHE_HOME/CATGIS/online-tile-cache` when defined,
  otherwise `user.home/.cache/CATGIS/online-tile-cache`.
- Reject relative XDG_CACHE_HOME, which violates the XDG location contract.
- macOS: prefer user.home/Library/Caches/CATGIS/online-tile-cache.
- User accounts without an available home use the existing explicit temp
  fallback, never throw during static class initialization.
- No mkdir/probing/cache decoding on Swing EDT; pure resolver computation
  alone is allowed at class initialization.
- Keep source-fingerprint folder names and existing 200 MiB disk eviction,
  128 MiB in-memory tile budget, and 8 MiB HTTP guards intact.
- Never delete/move legacy temporary directories silently. A one-time Linux
  cache warmup is acceptable; user data beyond CATGIS cache must not change.

## Acceptance

- Deterministic pure resolver tests simulate Windows 10/11, Linux Mint with
  and without XDG_CACHE_HOME, relative XDG rejected, macOS and headless fallback.
- Existing offline, provider-switch, disk hit, EDT, bounded HTTP and weighted
  LRU tests still pass.
- Cross-platform Java Desktop Build + Linux Full Suite + Windows Full Suite,
  PostGIS real integration, architecture and source inventories must PASS on
  the exact PR head, then again on the exact merged main SHA with authenticated
  push-run evidence.
- No local Windows/OpenCode execution before architecture A0.7 gates close.

## Residual risks

Persistence depends on operating-system cleanup policy and available home
storage. Symlink/junction and disk-eviction recursion need their own safety
assessment; this change neither follows nor cleans old temporary cache roots.
