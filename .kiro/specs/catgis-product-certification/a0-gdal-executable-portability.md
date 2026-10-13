# CATGIS — SPEC/Kiro A0.7 external GDAL native command portability

Status: SOURCE + CONTRACT TESTS STAGED / NOT CERTIFIED
Base protected main: `34d73ed47c6720e03ef49d49715f77b81d86a70c`
Branch: `quality/a0-unix-gdal-explicit-native-bin`
Related: issue #64; macOS Intel + Apple Silicon package PR #63.

## Confirmed root cause

The former GdalSupport was Windows-only: it tried .exe files in
`C:\\OSGeo4W\\bin` and `C:\\OSGeo4W64\\bin` and never mapped
`gdal_translate.exe`, `gdalwarp.exe`, `gdalinfo.exe` to Unix names.
Real macOS/Linux GDAL binaries therefore stayed inaccessible even if installed.
Consumers include RasterImageLoader, RasterReprojectionService,
ProDatasetOpenService and ProRasterMaterializationService.

## Scope

1. Retain current Windows OSGeo4W variable overrides and default lookup order;
   resolve the same approved .exe commands without breaking Windows callers.
2. On Mac, look in explicit configured absolute `CATGIS_GDAL_BIN` first, then
   `/opt/homebrew/bin` (Apple Silicon), `/usr/local/bin` (Intel Homebrew),
   `/opt/local/bin` (MacPorts), `/usr/bin`.
3. On Linux, look in configured absolute bin first, then `/usr/bin`,
   `/usr/local/bin`, `/opt/bin`. **Do not trust PATH or cwd**.
4. Allow only the three actually reviewed commands
   `gdal_translate`, `gdalwarp`, `gdalinfo` (with optional .exe suffix
   accepted as caller syntax). Reject traversal, slashes and arbitrary
   shell/user programs at the input boundary.
5. Resolve to an absolute real regular executable file. macOS Homebrew
   symlinks are permitted, subject to a regular executable target.
6. Include deterministic tests simulating Mac Intel/ARM, Linux, Windows
   explicit roots/extension normalization, fake tool executables,
   no PATH/cwd fallback, invalid env dirs, missing dependency diagnostics.
7. No OS binaries, C libraries or additional dependencies in git. Keep the
   Java ProcessBuilder argument list; never construct a shell command.
8. Full exact-head CI and Certification Windows/Linux/macOS Intel/ARM plus
   post-main authenticated push checks before declaring integrated.
   First rebase/replay this isolated change on exact main after #63.
9. Real package integration test with native Mac/Linux GDAL and end-to-end
   GeoTIFF conversion still required for full issue #64 closure.

## Important exclusions

- This source change only resolves native CLI tools. It does NOT install GDAL,
  fix ImageIO/JNI `.dylib/.so/.dll` loading, deliver ODA DWG on macOS, sign
  the Mac .app, certify GUI output or run a real GDAL raster conversion.
- Original private Windows documentation remains historical evidence only;
  do not claim the complete GIS is Mac-ready from JVM + jpackage success.
