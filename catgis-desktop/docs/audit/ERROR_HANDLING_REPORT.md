# CATGIS Desktop -- Error Handling Report

Audit date: 2026-06-14 | Verified against C:\CATGIS\catgis-desktop\src\ar

---

## Executive Summary

CATGIS Desktop has **significantly improved** its error handling posture since the initial audit. The codebase is **clean of `printStackTrace` and `System.err` usage**. The dominant pattern is now `catch (Exception ...) { CatgisLogger.warn("ClassName: operation failed", ex); }`, which is acceptable for a desktop application. The remaining issues are concentrated and addressable.

---

## 1. Catch Block Inventory

### 1.1 Overall Counts

| Category | Count |
|----------|-------|
| Total `catch` blocks | 764 |
| `catch(Exception)` blocks | 702 |
| Empty `catch` (no action) | 4 |
| With `CatgisLogger.warn()` | ~695 |
| With `CatgisLogger.warn()` + "operation failed" convention | >600 |
| `printStackTrace` | 0 |
| `System.err` | 0 |
| `System.out` | 0 |

### 1.2 Truly Empty Catch Blocks (Priority: Fix)

These are the only 4 catch blocks in the entire codebase with **no logging or action**:

| # | File | Line | Code | Risk | Fix |
|---|------|------|------|------|-----|
| 1 | `catmap/Main.java` | 1790 | `} catch (Exception ignored2) {}` | medio | Add `CatgisLogger.warn("Main: cleanup failed", ignored2)` |
| 2 | `climate/GribLoader.java` | 218 | `} catch (Exception ex) {}` | ALTO | Add `CatgisLogger.warn("GribLoader: band read failed", ex)` |
| 3 | `PostgisConnectionStore.java` | 181 | `} catch (Exception ignored) {}` | bajo | Add `CatgisLogger.warn("PostgisConnectionStore: connection test failed", ignored)` |
| 4 | `PostgisConnectionFactory.java` | 88 | `try { ds.close(); } catch (Exception ignored) {}` | bajo | Add `CatgisLogger.warn("PostgisConnectionFactory: datasource close failed", ignored)` |

### 1.3 Silent parse failures -- CatmapSerializer

| File | Line | Code | Risk | Fix |
|------|------|------|------|-----|
| `catmap/CatmapSerializer.java` | 319 | `try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0; }` | medio -- data corruption | Add `CatgisLogger.warn("CatmapSerializer: invalid double '" + s.trim() + "', defaulting to 0", e)` before `return 0` |
| `catmap/CatmapSerializer.java` | 323 | `try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }` | medio -- data corruption | Add `CatgisLogger.warn("CatmapSerializer: invalid int '" + s.trim() + "', defaulting to 0", e)` before `return 0` |

---

## 2. Console Output Hygiene

| Pattern | Occurrences | Status |
|---------|-------------|--------|
| `printStackTrace()` | 0 | Clean |
| `System.err` | 0 | Clean |
| `System.out` | 0 | Clean |
| `CatgisLogger.warn()` | >600 | Good |
| `CatgisLogger.error()` | Present | Good |
| `CatgisLogger.info()` | Present | Good |

The codebase has been systematically migrated from console output to CatgisLogger. No `printStackTrace` call survives. This is a major improvement.

---

## 3. Return Null Pattern Analysis

### 3.1 Overall Count

| Pattern | Occurrences |
|---------|-------------|
| `return null` | 890 |

### 3.2 Classification

| Category | Estimate | Examples |
|----------|----------|----------|
| Legitimate "not found" | ~60% | `if (layer == null) return null` -- early return for missing data |
| Legitimate "not applicable" | ~20% | `if (feature == null) return null` -- no feature, no result |
| Fallback after catch + warn | ~15% | `catch (Exception ignored) { CatgisLogger.warn(...); return null; }` -- logged, acceptable |
| Masked error (no log) | ~5% | `catch (Exception ex) { return null; }` without logging -- needs audit |

### 3.3 Key Files with High return null Density

| File | return null count | Risk |
|------|-------------------|------|
| `EditingGeometryOperations.java` | ~50 | bajo -- geometry operations returning null for invalid input is correct |
| `CoordinateTransformSupport.java` | ~20 | bajo -- transform failure returning null is documented |
| `ExportVectorLayerAction.java` | ~20 | medio -- export failures should notify user |
| `GeoprocessingAssistantDialog.java` | ~18 | medio -- some null returns may mask geoprocessing failures |
| `NetCdfLoader.java` | ~18 | medio -- loader null returns may indicate missing data vs. read failure |
| `ContourGenerationService.java` | ~9 | bajo -- all within catch blocks that log |

---

## 4. Resource Management

### 4.1 ProcessBuilder Usage

All ProcessBuilder usage has been verified with validation:

| File | Validation Pattern | Status |
|------|-------------------|--------|
| `GdalSupport.java` | `resolve()` checks specific install paths + env var, never PATH | Safe |
| `DwgImportSupport.java` | Absolute paths to ODA Teigha, checked before execution | Safe |
| `ExternalToolService.java` | WhiteboxTools resolution via known paths | Safe |
| `CartographyToolbar.java` | Cartography tools, verified safe | Safe |

### 4.2 Resource Cleanup

| Pattern | Status |
|---------|--------|
| `try-with-resources` for I/O | Present in loaders and exporters |
| `removeNotify()` for Swing cleanup | Present in dialog/panel lifecycles |
| `dispose()` for dialog cleanup | Present |
| `BufferedImage.flush()` | Present in layout/golden image code |
| `DataStore.dispose()` | Present in GeoTools integration |

---

## 5. Recommendations by Critical Zone

### 5.1 Data Loaders

| Loader | Current State | Recommendation |
|--------|--------------|----------------|
| FlatGeobufLoader | validateFile() + magic bytes + UnsupportedFormatException | Complete -- no action needed |
| SpatiaLiteLoader | Throws on connection failure | Complete -- no action needed |
| ShapefileData | Catches and logs | Adequate |
| GribLoader | 6 catch(Exception) blocks, 1 empty, returns null silently | **Add validateFile() pre-check. Add CatgisLogger.warn to empty catch. File feature request to implement proper GRIB parsing** |
| NetCdfLoader | Returns null with CatgisLogger.warn on most paths | Adequate for beta |
| DwgImportSupport | Catches and logs | **Add pre-validation: check ODA installation exists before attempting import** |

### 5.2 PostGIS

| Area | Current State | Recommendation |
|------|--------------|----------------|
| Connection encryption | AES-256-GCM + PBKDF2 (100k iterations) | Complete |
| Connection pooling | HikariCP 5.1.0 by fingerprint | Complete |
| Connection validation | No explicit validateFile() test | **Add connection test query (e.g., SELECT 1) in PostgisConnectionStore.validateFile()** |
| Error on failed connection | Logged via CatgisLogger | Adequate |
| SQL injection (pgRouting) | Table name interpolation without validation | **Apply safe-sql-table-validation pattern** |

### 5.3 Export Operations

| Area | Current State | Recommendation |
|------|--------------|----------------|
| ExportVectorLayerAction | 1674 lines, 20 catch blocks, all logged | **Split by format first, then add per-format pre-export validation** |
| LayoutPageRenderer | Handles PDF/PNG/SVG | **Add pre-export validation: check output path is writable, content is renderable** |
| KmlExportEngine | Simple, focused | Adequate |

### 5.4 CRS Transform

| Area | Current State | Recommendation |
|------|--------------|----------------|
| reprojectGeometryIfNeeded | Now logs with CatgisLogger.warn | **Fixed. Monitor log output. Consider user notification for critical operations** |
| CoordinateTransformSupport | Returns null on failure | Adequate -- null is documented behavior |

### 5.5 Project Save/Load

| Area | Current State | Recommendation |
|------|--------------|----------------|
| CatmapSerializer.parseDouble | `return 0` without log | **Add CatgisLogger.warn before return 0** |
| CatmapSerializer.parseInt | `return 0` without log | **Add CatgisLogger.warn before return 0** |
| .catgis XML validation | No schema validation | **Add version marker and minimal XSD/schema validation on load** |

---

## 6. Error Handling Pattern Reference

### 6.1 Recommended Pattern (already dominant in codebase)

```java
try {
    // risky operation
} catch (Exception ignored) {
    CatgisLogger.warn("ClassName: operation failed", ignored);
}
```

### 6.2 Anti-Patterns Still Present

```java
// Anti-pattern: empty catch
} catch (Exception ex) {}  // GribLoader:218, Main:1790

// Anti-pattern: silent default
} catch (Exception e) { return 0; }  // CatmapSerializer:319,323
```

### 6.3 For Loaders

```java
// Recommended loader pattern (as seen in FlatGeobufLoader)
public Layer load(File file) {
    validateFile(file);  // throws UnsupportedFormatException with details
    try {
        // actual loading
    } catch (Exception e) {
        CatgisLogger.error("LoaderName: load failed for " + file.getName(), e);
        throw new UnsupportedFormatException("Failed to load: " + e.getMessage(), e);
    }
}
```

---

## 7. Trend Summary

| Metric | Initial Audit | Current | Trend |
|--------|--------------|---------|-------|
| catch(Exception) blocks | ~183 | 702 | Expanded (new code added) |
| Empty catch (no action) | ~10 | 4 | Improved |
| catch with CatgisLogger.warn | ~100 | ~695 | Major improvement |
| printStackTrace | ~5 | 0 | Fixed |
| System.err/out in prod code | ~3 | 0 | Fixed |
| CatmapSerializer silent failures | 2 | 2 | Unchanged |
| ProcessBuilder without validation | 1 | 0 | Fixed |
| SQL injection vectors | 1 | 1 (pgRouting) | Unchanged |
| Loaders without validateFile | 3 | 1 (GribLoader) | Improved |

The error handling culture in the codebase has shifted from "catch and forget" to "catch, log, and notify". The 698+ CatgisLogger.warn calls represent a significant investment in observability.
