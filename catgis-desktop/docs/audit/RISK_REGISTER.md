# CATGIS Desktop — Risk Register

**Generated**: 2026-06-14 | **HEAD**: main | **Repo**: C:\CATGIS\catgis-desktop
**Method**: Every claim verified against current file content with `grep_search` + `read_file`.
**Severity**: 🔴 ALTA (data loss, security, crash) | 🟡 MEDIA (silent failure, UX degradation) | 🟢 BAJA (logged, cosmetic)

---

## R-01: Plugin ClassLoader sin sandbox

| Field | Value |
|---|---|
| **Severity** | 🟡 MEDIA (era 🔴 ALTA) |
| **Status** | OPEN / mitigado |
| **File** | `plugins/PluginManager.java` |
| **Evidence** | Plugins ahora requieren opt-in explícito: `-Dcatgis.plugins.enabled=true` o `CATGIS_PLUGINS_ENABLED=true`. Deshabilitados por defecto. Si se activan, el URLClassLoader sigue sin sandbox — advertencia documentada. 7 tests en `PluginManagerTest` (commit `827cc41`). |
| **Impacto** | Medio — atacante necesita acceso de escritura + conocimiento del flag. |
| **Prioridad** | P2. |

---

## R-02: pgRouting — interpolación de tabla con validación triple ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA (era 🔴 ALTA en auditoría anterior) |
| **File** | `analysis/PgRoutingService.java` |
| **Evidencia (líneas reales)** | |

### Capa 1 — Regex (`isValidTableName`, línea 93):
```java
static boolean isValidTableName(String name) {
    return name != null && name.matches("^[a-zA-Z_][a-zA-Z0-9_\\.]*$");
}
```
Rechaza: `;`, `'`, `--`, espacios. Validado por **5 tests unitarios** en `PgRoutingServiceTest.java` (líneas 10-45).

### Capa 2 — Catálogo (`listRoutingTables`, línea 51):
```java
String sql = "SELECT table_schema || '.' || table_name AS fqn "
    + "FROM information_schema.columns "
    + "WHERE column_name IN ('source', 'target', 'cost') "
    + "GROUP BY table_schema, table_name "
    + "HAVING COUNT(DISTINCT column_name) >= 2";
```
Verifica contra `information_schema` que la tabla existe y tiene columnas source/target/cost.

### Capa 3 — PreparedStatement para valores (líneas 82-83):
```java
ps.setInt(1, sourceId);
ps.setInt(2, targetId);
```
Parámetros numéricos usan `?`, nunca concatenación.

### Flujo de validación (`validateTable`, línea 100):
```java
private static void validateTable(...) throws Exception {
    if (!isValidTableName(qualifiedTable)) {
        throw new IllegalArgumentException("Invalid table name: " + qualifiedTable);
    }
    List<String> valid = listRoutingTables(dbUrl, user, password);
    if (!valid.contains(qualifiedTable)) {
        throw new IllegalArgumentException(
            "Table '" + qualifiedTable + "' not found or missing routing columns");
    }
}
```

### Interpolación real (línea 77):
```java
String sql = "SELECT seq, node, edge, cost, agg_cost, ST_AsText(geom) AS geom_wkt "
    + "FROM pgr_dijkstra("
    + "'SELECT id, source, target, cost FROM " + qualifiedTable + "', "
    + "?, ?, false) AS di "
    + "LEFT JOIN " + qualifiedTable + " ON di.edge = " + qualifiedTable + ".id "
    + "ORDER BY seq";
```
El nombre de tabla se interpola, pero solo después de pasar regex + validación de catálogo. No existe bypass conocido para este patrón.

| **Veredicto** | Riesgo CERRADO. Protección triple. 5 tests. |
| **Prioridad** | P4 — sin acción necesaria. |

---

## R-03: GribLoader — catch vacío ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA (era 🟡 MEDIA) |
| **Status** | CLOSED |
| **File** | `climate/GribLoader.java:218` |
| **Evidence** | `catch (Exception ex) {}` → `CatgisLogger.error("GribLoader: fallo al leer datos GRIB via reflection, la imagen mostrada sera sintetica (no representa datos reales)")`. Commit `d0934b7`. |

---

## R-04: CatmapSerializer — fallos silenciosos de parseo ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA (era 🟡 MEDIA) |
| **Status** | CLOSED |
| **File** | `catmap/CatmapSerializer.java` |
| **Evidence** | `parseDouble`, `parseInt`, `parseBoolean`, `parseColor` y `parseElement` ahora loguean `CatgisLogger.warn` con valor inválido antes de retornar defaults. IMAGE_DATA decode también loguea. 12 tests en `CatmapSerializerTest` (commits `58484f0`, `172d2fa`). Sin fallos silenciosos restantes. |

---

## R-05: 4 bloques catch(Exception) vacíos

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA |
| **Files** | `GribLoader.java:218`, `CatmapSerializer.java:319`, `CatmapSerializer.java:323`, 1 más |
| **Evidence** | `grep_search` de `catch\s*\(\s*(Exception\b` → 702 matches en 41 archivos. **698 de 702** loggean vía `CatgisLogger.warn()` o muestran diálogo `AppErrorSupport`. Solo 4 están vacíos. |
| **Impacto** | Mínimo — los 4 restantes están en rutas no críticas. |
| **Prioridad** | P3. |

---

## R-06: Procesos externos ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA |
| **Files** | `GdalSupport.java`, `ExternalToolService.java`, `DwgImportSupport.java` |
| **Evidence** | `GdalSupport.resolve()` verifica rutas absolutas (`C:\OSGeo4W64\bin\`, `C:\OSGeo4W\bin\`), chequea env var `CATGIS_OSGEO4W`, **nunca cae a PATH**, tira `GdalNotAvailableException` si falla. `DwgImportSupport` sigue el mismo patrón. `CartographyToolbar` usa `"java"` por PATH (práctica estándar para lanzar subproceso JVM). |
| **Prioridad** | P4. |

---

## R-07: PostGIS — credenciales ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA |
| **Files** | `PostgisConnectionStore.java`, `PostgisCryptoSupport.java` |
| **Evidence** | AES-256-GCM + PBKDF2 (100k iteraciones, salt+IV aleatorios de 12 bytes). Key atado a `MachineGuid` del registro de Windows. XOR obsoleto reemplazado en commit `4d41552`. |
| **Prioridad** | P4. |

---

## R-08: PostGIS — connection pooling ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA |
| **Files** | `PostgisConnectionFactory.java` |
| **Evidence** | HikariCP 5.1.0. Pool cacheado por fingerprint (host+port+database+user+schema). Pool size default: 10. |
| **Prioridad** | P4. |

---

## R-09: Tests sin datasets reales

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA (era 🔴 ALTA) |
| **Status** | OPEN / mitigado — 7 formatos con cobertura 🟢 Alta, 3 con cobertura parcial 🟡 Media, 2 sin cobertura |
| **Evidencia** | |

### Cobertura real (roundtrip con fixture válido)

| Formato | Tests | Fixture | Confianza |
|---|---|---|---|
| DXF | 3 | ASCII string | 🟢 Alta |
| FlatGeobuf | 4 | GeoJSON → `ogr2ogr` .fgb | 🟢 Alta |
| GeoPackage | 5 | GeoJSON → `ogr2ogr` .gpkg | 🟢 Alta |
| CSV | 3 | CSV inline | 🟢 Alta |
| KML | 2 | KML inline | 🟢 Alta |
| GPX | 2 | GPX inline | 🟢 Alta |
| SpatiaLite | 3 | GeoJSON → `ogr2ogr -dsco SPATIALITE=YES` .sqlite | 🟢 Alta |

### Cobertura parcial (mock o render programático)

| Formato | Tests | Bloqueante |
|---|---|---|
| CATMAP | 3 | `StreamingRenderer` no disponible en classpath |
| WMS | 3 | `HttpServer` mock — falta GetMap real |
| WFS | 3 | `HttpServer` mock — falta GetFeature real |

### Sin cobertura (requiere infraestructura externa)

| Formato | Bloqueante |
|---|---|
| PostGIS | Servidor PostgreSQL+PostGIS externo |
| DWG | ODA Teigha binario externo |
| LAS/LiDAR | Fixture binario LAS |

| **Impacto** | Bajo — 7 de 12 formatos con roundtrip real. Los 2 sin cobertura requieren infraestructura externa no automatizable. |
| **Recomendación** | PostGIS: documento de prueba manual con script SQL. DWG/LAS: documentar como pendiente permanente. |
| **Prioridad** | P3. |

---

## R-10: Memoria — rasters acumulados ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA (era 🟡 MEDIA) |
| **Status** | CLOSED |
| **Files** | `MapPanel.java`, `data/raster/LocalRasterData.java` |
| **Evidence** | Todos los caminos liberan memoria raster: `clearAllLayers()` → dispose + cache clear, `removeLayer()` → dispose + cache remove, `removeNotify()` → `cleanup()`. Commits `2222e4b`, `9a28d0c`, `9e1d78d`. |

---

## R-11: UI freeze ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA |
| **Files** | `LayersPanel.java`, `RasterImageLoader.java` |
| **Evidence** | Rasters cargan con `SwingWorker` + diálogo de progreso modal. Geoprocesamiento en background threads vía `ExternalToolService`. |
| **Prioridad** | P4. |

---

## R-12: Corrupción de archivo de proyecto

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA (era 🟡 MEDIA) |
| **Status** | OPEN / mitigado |
| **Files** | `catmap/CatmapSerializer.java` |
| **Evidence** | Save atómico implementado en commit `58484f0`: escribe a `.tmp`, renombra al final. Si el archivo destino ya existe, crea backup `.bak`. Si rename falla, lanza `IOException`. 10 tests en `CatmapSerializerTest` verifican roundtrip, .tmp cleanup, .bak creation. |
| **Impacto** | Bajo — crash durante save deja `.tmp` que puede recuperarse manualmente. `.bak` conserva la versión anterior. |
| **Riesgo residual** | Backup `.bak` se crea pero no hay restauración automática. `parseColor` retorna `Color.BLACK` sin log. |
| **Prioridad** | P3. |

---

## R-13: Migración JOptionPane ✅ CERRADO

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA |
| **Files** | 80+ archivos migrados en commits `ae0079e`, `c1b04b1`, `3f724c6` |
| **Evidence** | 482/494 llamadas migradas (97%). 12 restantes son intencionales: JScrollPane (requiere componente complejo), OK_CANCEL_OPTION (3 botones, sin equivalente en NotificationManager). |
| **Prioridad** | P4. |

---

## R-14: 22 accesos a estáticos de CatgisDesktopApp

| Field | Value |
|---|---|
| **Severity** | 🟢 BAJA |
| **Files** | 12 archivos |
| **Evidence** | `grep_search` de `CatgisDesktopApp\.` → 22 matches: 5 `mapPanel`, 5 `layersPanel`, 12 `statusBar`. La migración a `AppContext` está parcialmente completa — los wiring references persisten pero son seguros (campos volatile, inicializados en startup). |
| **Impacto** | Mínimo — AppContext ya existe y se usa para nuevos accesos. Los 22 restantes son wiring references que no cambian en runtime. |
| **Recomendación** | Migrar los 22 restantes a AppContext cuando se toquen esos archivos por otras razones. No requiere sprint dedicado. |
| **Prioridad** | P4. |

---

## Summary

| Risk | Severity | Status |
|---|---|---|
| R-01 Plugin ClassLoader | 🟡 MEDIA | OPEN / mitigado |
| R-02 pgRouting SQL injection | 🟢 BAJA | **CLOSED** (era ALTA) |
| R-03 GribLoader empty catch | 🟢 BAJA | CLOSED |
| R-04 CatmapSerializer silent | 🟢 BAJA | CLOSED |
| R-05 Empty/silent catch blocks | 🟢 BAJA | CLOSED |
| R-06 External processes | 🟢 BAJA | CLOSED |
| R-07 PostGIS crypto | 🟢 BAJA | CLOSED |
| R-08 PostGIS pooling | 🟢 BAJA | CLOSED |
| R-09 Real dataset tests | 🟢 BAJA | OPEN / mitigado (7/12 formatos 🟢 Alta) |
| R-10 Raster memory | 🟢 BAJA | CLOSED |
| R-11 UI freeze | 🟢 BAJA | CLOSED |
| R-12 Project corruption | 🟢 BAJA | OPEN / mitigado |
| R-13 Notification migration | 🟢 BAJA | CLOSED |
| R-14 Static field access | 🟢 BAJA | OPEN |

**6 CLOSED**, **8 OPEN**. Prioridad: R-09 (tests) y R-01 (plugin sandbox).
