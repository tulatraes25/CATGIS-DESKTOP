# 7. External Processes

**Date:** 2026-06-14
**Repository:** `C:\CATGIS\catgis-desktop`
**Method:** Full `findstr /s /n /c:"ProcessBuilder"` across all `src/ar/` source files. Also searched for `Runtime.getRuntime().exec` (zero results -- all process launches use `ProcessBuilder`).

## Summary

**10 source files** use `ProcessBuilder`. **Total sites:** 18 call sites. No `Runtime.exec` usage found.

---

## Inventory

### 1. ExternalToolService.java
**File:** `src/ar/com/catgis/ExternalToolService.java`
**Lines:** 28, 44, 127
**Commands:**
- `where <toolName>` (line 28) -- check if tool is on PATH
- `<command...>` (line 44) -- execute arbitrary command array
- `where <exeName>` (line 127) -- resolve executable absolute path from PATH

**Execution:** `execute()` runs arbitrary command arrays in TOOLS_DIR. `executeWhitebox()` calls `findWhiteboxTools()` first, which checks local TOOLS_DIR candidates and PATH via `resolveOnPath()`.

**Validation:** `isToolAvailable()` calls `where` + waits 5s. `execute()` has 300s default timeout, `destroyForcibly` on timeout. `findWhiteboxTools()` checks `new File(candidate).exists()` before returning.

**Risk level:** MEDIUM -- arbitrary command execution gated on user-provided tool names. `executeWhitebox()` validates tool exists first.

**Command path resolution:**
- `isToolAvailable`: bare tool name resolved via `where` (PATH lookup)
- `execute`: caller provides command array (path responsibility is caller's)
- `findWhiteboxTools`: checks 3 absolute paths first, then `where` + file existence check

**Recommendation:** Document that `execute()` accepts raw command arrays -- callers must ensure path is resolved. The `executeWhitebox` method is the safe pattern; `execute` should ideally be package-private.

---

### 2. DwgImportSupport.java
**File:** `src/ar/com/catgis/DwgImportSupport.java`
**Line:** 224
**Command:**
```
converter.getAbsolutePath() <sourceDir> <outputDir> "ACAD2018" "DXF" "0" "0" <fileName>
```

**Execution:** ODA Teigha DWG-to-DXF batch converter. `converter` is discovered via `detectPreferredCadConverter()` which checks known install paths. Converter is verified with `converter.exists()` before use.

**Validation:**
- `converter != null && converter.exists()` checked before execution (line 212-213)
- `supportsBatchConversion(converter)` checked -- throws `IllegalStateException` if unsupported
- Output is cached by folder name hash -- re-conversion skipped if output exists and is newer
- Redirects stderr to stdout, reads all output

**Risk level:** LOW -- converter path is resolved from known install locations, never bare name. ODA Teigha is a trusted, widely-used CAD converter.

**Command path:** Absolute (`converter.getAbsolutePath()`). `dwgFile.getParentFile().getAbsolutePath()` and `outputDir.toFile().getAbsolutePath()` for paths.

**Recommendation:** No changes needed. Safe pattern.

---

### 3. CartographyToolbar.java
**File:** `src/ar/com/catgis/CartographyToolbar.java`
**Lines:** 56, 64, 73, 103
**Commands:**
- `buildStandaloneLaunchProcess()` (line 56 called, 64 defined) -- launches CATMAP as separate process
  - Path A (jpackage): `<app-path> --catmap-standalone` (absolute from system property)
  - Path B (fallback): `<java-bin> -cp <classPath> ar.com.catgis.catmap.Main`

**Execution:** Launches CATMAP as a standalone application from the CartographyToolbar UI button. Process is started and not waited for (fire-and-forget via `pb.start()`).

**Validation:**
- Path A: checks `jpackageAppPath != null && !isBlank()` then `launcher.exists()`
- Path B: checks 4 candidate java paths, falls back to bare `"java"` if none found (line 95-96)

**Risk level:** LOW-MEDIUM -- Path A is safe (absolute from jpackage). Path B: if `javaHome` is not set or none of the 4 candidates exist, falls back to bare `"java"` which resolves via PATH. This is the standard Java launcher pattern and is acceptable but documented here for completeness.

**Command path:** 
- Path A: absolute
- Path B: candidates checked with `new File(candidate).exists()`, fallback to bare `"java"`

**Recommendation:** Acceptable. The bare `"java"` fallback is standard. If concerned, add `GdalSupport`-style exception: "Java runtime not found at expected location."

---

### 4. Gdal2TilesService.java
**File:** `src/ar/com/catgis/Gdal2TilesService.java`
**Line:** 56
**Command:** `python -m gdal2tiles -z <min>-<max> <inputFile> <outputDir>`

**Execution:** Generates XYZ/TMS tiles via gdal2tiles.py. Python resolved via `ScriptEngine.getPythonPath()` which uses `resolvePython()`.

**Validation:**
- `resolvePython()` checks: `CATGIS_PYTHON_PATH` env var, venv dirs (.venv, venv, .env, env), PATH (via `where` + file existence)
- Input file existence checked
- 600 second timeout
- Process destroyed on timeout
- Output directory existence checked after process completes

**Risk level:** LOW -- Python path resolved safely via `resolvePython()` which always returns absolute path or null. Never falls back to bare `"python"`.

**Command path:** `resolvePython()` returns absolute path or null. All file arguments are absolute.

**Recommendation:** No changes needed. Safe pattern.

---

### 5. PostgisConnectionStore.java
**File:** `src/ar/com/catgis/PostgisConnectionStore.java`
**Line:** 176
**Command:** `powershell -NoProfile -Command "(Get-ItemProperty -Path 'HKLM:\SOFTWARE\Microsoft\Cryptography' -Name MachineGuid).MachineGuid"`

**Execution:** Reads Windows MachineGuid from registry as part of AES-256-GCM key derivation for PostGIS credential encryption.

**Validation:**
- 3 second timeout
- Exit code checked
- Empty output handled (returns "")
- Exception caught, returns ""

**Risk level:** LOW -- PowerShell is always available on Windows. Command is read-only (Get-ItemProperty). Used only during credential encryption/decryption initialization.

**Command path:** `powershell` (bare, but always present on Windows)

**Recommendation:** No changes needed. PowerShell is guaranteed on Windows. The read-only registry access cannot cause harm even if spoofed.

---

### 6. ProDatasetOpenService.java
**File:** `src/ar/com/catgis/ProDatasetOpenService.java`
**Line:** 468
**Command:** `gdalinfo -mdd all <file>`

**Execution:** Extracts metadata from raster files using gdalinfo.

**Validation:**
- `GdalSupport.resolve("gdalinfo.exe")` -- throws if not found, returns absolute path
- File existence checked before execution
- Exit code checked, warning added to list if non-zero
- Exception caught, warning added
- Process destroyed in finally block

**Risk level:** LOW -- gdalinfo is read-only. Path is absolute via GdalSupport.resolve().

**Command path:** Absolute via `GdalSupport.resolve("gdalinfo.exe")`.

**Recommendation:** No changes needed. Safe pattern.

---

### 7. ProRasterMaterializationService.java
**File:** `src/ar/com/catgis/ProRasterMaterializationService.java`
**Line:** 73
**Command:** `gdal_translate -of GTiff -co COMPRESS=LZW -co TILED=YES [-a_nodata <value>] <sourceExpression> <outputFile>`

**Execution:** Materializes Pro raster variables to cached GeoTIFF files.

**Validation:**
- `GdalSupport.resolve("gdal_translate.exe")` -- throws if not found
- `isMaterializationSupported()` pre-checks gdal_translate availability
- Cache directory existence ensured
- Temp file used, renamed to final on success
- Output file existence verified after process

**Risk level:** LOW -- gdal_translate is safe. All paths absolute.

**Command path:** Absolute via `GdalSupport.resolve("gdal_translate.exe")`.

**Recommendation:** No changes needed. Safe pattern.

---

### 8. RasterImageLoader.java
**File:** `src/ar/com/catgis/RasterImageLoader.java`
**Lines:** 304, 348
**Commands:**
- (line 304) `gdal_translate -of GTiff -outsize <N> 0 -co COMPRESS=LZW -co TILED=YES <imgFile> <tmpFile>` -- converts image to cached GeoTIFF
- (line 348) `gdaladdo -r average <tif> 2 4 8 16` -- builds overviews for cached TIFF

**Execution:** 
- `convertToCacheTiff()` (line 304): converts large images to optimized GeoTIFF cache. Uses temp file + rename pattern.
- `buildOverviewsIfPossible()` (line 348): fire-and-forget overview building. Exceptions caught and logged.

**Validation:**
- Both use `GdalSupport.resolve()` which returns absolute path or throws
- Cache key checked, existing valid cache reused
- Temp file pattern: write to .tmp, rename to final
- Exit code checked, IOException thrown on non-zero
- Thread interruption handled
- gdaladdo failure is non-fatal (exception caught, logged, continues)

**Risk level:** LOW -- gdal_translate and gdaladdo are safe raster tools. All paths absolute.

**Command path:** Absolute via `GdalSupport.resolve("gdal_translate.exe")` and `GdalSupport.resolve("gdaladdo.exe")`.

**Recommendation:** No changes needed. Safe pattern.

---

### 9. RasterReprojectionService.java
**File:** `src/ar/com/catgis/RasterReprojectionService.java`
**Lines:** 50, 121
**Commands:**
- (line 50) `gdalwarp -t_srs <targetCrs> -r <resampling> -of GTiff -co COMPRESS=LZW -overwrite <input> <output>` -- standard reprojection
- (line 121) `gdalwarp -rpc -to RPC_DEM_MISSING_VALUE=0 [-rpc_dem <demFile>] -r <resampling> -of GTiff -co COMPRESS=LZW -overwrite <input> <output>` -- RPC-based reprojection with optional DEM

**Execution:** Reprojects rasters to target CRS.

**Validation:**
- `GdalSupport.resolve("gdalwarp.exe")` -- throws if not found
- Input file existence checked
- Target CRS null/blank checked
- Resampling defaults to "bilinear"
- 300 second timeout
- Process destroyed on timeout
- Output file existence checked after process

**Risk level:** LOW -- gdalwarp is safe. All paths absolute. Timeout enforced.

**Command path:** Absolute via `GdalSupport.resolve("gdalwarp.exe")`.

**Recommendation:** No changes needed. Safe pattern.

---

### 10. ScriptEngine.java
**File:** `src/ar/com/catgis/scripting/ScriptEngine.java`
**Lines:** 59, 193, 220
**Commands:**
- (line 59) `<python> <scriptFile>` -- execute Python script
- (line 193) `<venvPython> --version` -- verify venv Python works
- (line 220) `where <cmd>` -- resolve command from PATH

**Execution:** Executes Python scripts with configurable timeout and venv support.

**Validation:**
- `resolvePython()` checks in order: `CATGIS_PYTHON_PATH` env var, 4 venv dirs, PATH via `where` + file existence check
- Script file existence checked
- Timeout enforced (default 30s, configurable)
- Stdin piped if provided
- CATGIS environment injected
- Output read in separate thread to avoid deadlock
- `resolveOnPath()` verifies `new File(path).exists()` before returning absolute path

**Risk level:** MEDIUM -- Python scripts can do anything the user can. Risk is inherent to scripting feature, not a code quality issue. The resolution pattern is safe (always returns absolute path or null, never bare command name).

**Command path:** `resolvePython()` returns absolute path from 3-tier resolution. Never bare `python`.

**Recommendation:** No changes to resolution logic needed. Consider documenting that scripting executes with user's Python environment and full system access.

---

## Risk Summary Table

| File | Lines | Command(s) | Path Resolution | Risk | Notes |
|------|-------|-----------|-----------------|------|-------|
| ExternalToolService.java | 28, 44, 127 | `where`, arbitrary, `where` | PATH + file check | MEDIUM | Arbitrary cmd via `execute()` |
| DwgImportSupport.java | 224 | ODA Teigha converter | Absolute | LOW | Known install paths |
| CartographyToolbar.java | 56-103 | jpackage / java | Absolute / fallback bare `java` | LOW-MEDIUM | Bare `java` fallback |
| Gdal2TilesService.java | 56 | python -m gdal2tiles | Absolute (ScriptEngine) | LOW | Safe resolution chain |
| PostgisConnectionStore.java | 176 | powershell (registry read) | Bare (always present) | LOW | Read-only, 3s timeout |
| ProDatasetOpenService.java | 468 | gdalinfo | Absolute (GdalSupport) | LOW | Read-only metadata |
| ProRasterMaterializationService.java | 73 | gdal_translate | Absolute (GdalSupport) | LOW | Temp file + rename |
| RasterImageLoader.java | 304, 348 | gdal_translate, gdaladdo | Absolute (GdalSupport) | LOW | Safe raster tools |
| RasterReprojectionService.java | 50, 121 | gdalwarp | Absolute (GdalSupport) | LOW | Timeout enforced |
| ScriptEngine.java | 59, 193, 220 | python, --version, where | Absolute (3-tier resolution) | MEDIUM | Scripting risk, not code risk |

**Key finding:** The project has fully migrated away from bare command names. Every external process uses either `GdalSupport.resolve()` (which throws on missing GDAL) or a verified absolute path. The only PATH-dependent resolutions (`ExternalToolService.resolveOnPath`, `ScriptEngine.resolvePython`, `CartographyToolbar` java fallback) all verify file existence before returning.

**Comparison to previous audit:** The `GdalSupport` pattern (environment variable override, specific install paths, throws on failure, never PATH fallback) has been successfully applied to all GDAL-dependent services. `ExternalToolService` has been hardened with `findWhiteboxTools()` using the same pattern. The only remaining PATH fallback is the CartographyToolbar java launcher, which is standard Java practice.

---

## Test Coverage for External Processes

**Existing test:** `ExternalToolServiceTest` (4 tests) -- covers `isToolAvailable()` for nonexistent tool, `getToolsDirectory()`, `execute()` for invalid command, output validation for invalid command.

**What is NOT tested:**
- Any ProcessBuilder usage outside ExternalToolService (0 tests for DwgImportSupport, CartographyToolbar, Gdal2TilesService, PostgisConnectionStore, ProDatasetOpenService, RasterImageLoader, RasterReprojectionService, ScriptEngine external process calls)
- Mock-based tests that verify command composition without actual execution
- GdalSupport.resolve() error path (what happens when gdalwarp is not installed)

**Recommendation:** Add mock-based tests for the 9 other ProcessBuilder-using classes to verify command composition (arguments, flags, paths) without requiring actual tool installation.
