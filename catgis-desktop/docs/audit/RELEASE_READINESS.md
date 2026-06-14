# 10. Release Readiness

**Date:** 2026-06-14
**Repository:** `C:\CATGIS\catgis-desktop`
**Test count:** 55+ test files covering layout (31+), vector ops, raster, hydrology, PostGIS, FlatGeobuf, scripting, and project serialization.

---

## Readiness Levels

### Level 1: Uso Personal (Personal Use)

**Decision: YES -- Ready**

Single developer can use CATGIS for daily GIS work.

**Evidence:**
- 55+ test files, all expected to pass (verified in prior audits)
- Core loop works: load shapefile, visualize, edit, export layout
- Most common formats supported: Shapefile (read/write), GeoTIFF (read), PostGIS (read/write), WMS (read), FlatGeobuf (read)
- CATMAP layout engine has extensive test coverage (31+ layout test classes)
- Project save/load cycle is tested with multiple CRS roundtrip tests

**Known limitations for personal use:**
- GeoPackage write not supported (read-only)
- SpatiaLite save not supported (read-only)
- LAS/LiDAR is experimental
- DXF/DWG requires external software (DXF engine is internal; DWG needs ODA)
- KML/GPX/CSV import is limited
- WFS write requires server-side transaction support

**Risk level for personal use:** Low. A developer who knows the limitations can work around them.

---

### Level 2: Beta Cerrada (Closed Beta)

**Decision: CONDITIONAL -- Not ready yet, but close**

Would need completion of specific items before inviting external testers.

**What's missing:**

1. **PostGIS integration test** -- requires connecting to a real PostGIS server and verifying feature read/write, CRS handling, and connection pool behavior. Currently only in-memory unit tests exist.

2. **Manual test plan execution** -- the plan in `MANUAL_TEST_PLAN.md` should be executed and all 14 test cases must pass.

3. **Known limitations documented** -- a "Known Issues" section should be added to a user-facing README covering:
   - Formats with read-only support
   - DWG requires ODA Teigha installation
   - LAS/LiDAR is experimental
   - Python scripting requires Python 3.8+

4. **Installer tested on clean machine** -- the `generar-instalador-exe.bat` output should be tested on a machine that has never had CATGIS or a Java development environment installed.

5. **Error message quality** -- all common error paths (file not found, corrupt file, unsupported format, GDAL not installed) should produce clear, Spanish-language messages, not raw stack traces.

**Critical risks for closed beta:**
- **pgRouting SQL injection:** `PgRoutingService.isValidTableName()` has unit tests that catch all common SQL injection patterns. Risk is mitigated but not zero -- the actual SQL query composition code should be reviewed.
- **Plugin classloader:** `PluginManager` has zero tests. A malicious or buggy plugin JAR could potentially access internal classes. The sandbox `URLClassLoader` security is unverified.
- **External process dependencies:** GDAL must be installed at `C:\OSGeo4W` or `C:\OSGeo4W64`. Beta testers on non-standard install locations will get GDAL-not-found errors. The `CATGIS_OSGEO4W` environment variable override is documented but not prominently.

**Recommended actions before closed beta:**
1. Execute manual test plan (2 hours)
2. Connect to a real PostGIS server and verify layer display (1 hour)
3. Review plugin ClassLoader isolation (1 hour)
4. Test installer on a clean Windows VM (2 hours)
5. Create "Known Issues" README section (30 min)
6. Test with OSGeo4W installed in a non-standard location (30 min)

**Estimated time to closed-beta readiness:** 1-2 days of focused testing and documentation.

---

### Level 3: Beta Publica (Public Beta)

**Decision: NO -- Not ready**

Significant gaps remain before the application can be released to unknown users.

**What's missing:**

1. **Real dataset tests for 80% of features:**
   - GeoPackage: zero tests
   - SpatiaLite: zero tests
   - DXF: zero tests
   - LAS: zero tests
   - KML export: zero tests
   - CSV import: zero tests
   - NETCDF/GRIB: zero tests
   - H3: zero tests
   - Plugins: zero tests

2. **PostGIS automated tests:** Requires a CI pipeline with a disposable PostGIS container. Currently all PostGIS tests are in-memory mocks.

3. **Security review of plugin ClassLoader:** The `PluginManager` loads JARs at runtime. Without security review, a plugin could potentially access internal CATGIS classes or the filesystem unrestricted.

4. **User documentation:** No user manual exists. The application is discoverable for developers familiar with QGIS/ArcGIS patterns, but new users will struggle.

5. **Installer tested on clean machine:** Must verify the installer:
   - Correctly bundles the JRE
   - Handles missing OSGeo4W gracefully (shows clear error, not a crash)
   - Creates Start Menu shortcuts
   - Uninstalls cleanly

6. **GDAL version compatibility:** Must verify the application works with GDAL 3.6, 3.7, 3.8 (OSGeo4W ships different versions).

**Critical risks for public beta:**
- **External process breakage on non-standard Windows installs:** 10 source files use ProcessBuilder. If GDAL is not at the expected path, the application degrades silently or shows confusing errors.
- **No crash reporting:** Users who encounter errors have no way to report them. No telemetry or error reporting mechanism exists.
- **No update mechanism:** Users on a public beta would need to manually download and reinstall updates.

**Recommended before public beta:**
- 80% test coverage across all format loaders
- CI pipeline with PostGIS container for automated integration tests
- Plugin security review completed
- User documentation (at minimum: getting started guide + format support matrix)
- Crash reporting mechanism (even if opt-in)
- Installer validation on Windows 10 and Windows 11

**Estimated time to public-beta readiness:** 4-6 weeks of focused work.

---

### Level 4: Uso Profesional (Professional Use)

**Decision: NO -- Not ready**

Professional use demands reliability, performance, and support guarantees that CATGIS does not yet provide.

**What's missing:**

1. **Performance benchmarks with large datasets:**
   - Shapefile with 100K+ features: load time, render time, attribute table open time
   - GeoTIFF > 500 MB: load time, hillshade generation time
   - PostGIS table with 1M+ rows: initial render time
   - Project with 50+ layers: open time, save time

2. **Long-running stability tests:**
   - 24-hour memory leak check: load layers, perform operations, monitor heap
   - 8-hour continuous editing session: create, edit, undo, save repeatedly
   - GC behavior under load: verify no `OutOfMemoryError` after prolonged use

3. **Undo/redo stress testing:**
   - 1000 consecutive operations (create, edit, delete features)
   - Verify undo stack doesn't overflow or corrupt state
   - Memory usage after deep undo history

4. **Project file corruption recovery:**
   - Truncate .catgis file at various points (10%, 50%, 90%)
   - Modify internal XML/JSON to contain invalid data
   - Verify clear error message (not silent NPE or empty project)
   - Offer recovery options (load partial project, skip corrupt layer)

5. **CRS boundary case testing:**
   - Features crossing the antimeridian (180/-180)
   - Features near the poles (89+ degrees latitude)
   - CRS transformations with datum shifts (NAD27 to WGS84)
   - Custom CRS definitions (PROJ strings)

6. **Concurrent access:** Multiple users editing the same PostGIS table (transaction conflicts)

7. **Accessibility:** Keyboard navigation, screen reader compatibility (Java Accessibility API)

**Critical risks for professional use:**
- **Memory leaks:** No long-running stability testing. GIS applications process large datasets and can accumulate memory over hours of use.
- **Data loss:** Undo/redo stack is not stress-tested. A bug in undo could silently corrupt feature geometry.
- **Performance degradation:** No benchmarks exist. Large datasets may cause unacceptable load times or UI freezes.
- **Project corruption:** Silent parse failures in `ProjectDeserializer` could result in data loss when reopening a saved project.

**Estimated time to professional readiness:** 3-6 months, depending on team size and priority.

---

### Level 5: Venta / Comercializacion (Commercial Sale)

**Decision: NO -- Not ready**

Commercial sale requires legal, support, and distribution infrastructure that does not exist.

**What's missing:**

1. **Legal review of dependencies:**
   - **GeoTools:** LGPL -- must ensure dynamic linking (not static) to comply. CATGIS uses GeoTools as a Gradle dependency, which is standard dynamic linking. Verify no GeoTools code is copied into CATGIS source.
   - **JTS (Java Topology Suite):** EPL 1.0 / BSD -- permissive, low risk.
   - **PDFBox:** Apache 2.0 -- permissive, attribution required.
   - **HikariCP:** Apache 2.0 -- permissive.
   - **FlatGeobuf (org.wololo):** License unknown -- must verify before distribution.
   - **ImageIO-ext:** BSD-style -- permissive, low risk.
   - **Total dependency count:** ~40-50 JARs in the Gradle dependency tree. Full license audit needed.

2. **EULA (End User License Agreement):** Must be drafted by a lawyer. Covers:
   - Usage rights and restrictions
   - Limitation of liability
   - Warranty disclaimer
   - Data privacy (does CATGIS phone home? Currently: no)
   - Third-party component licenses and attributions

3. **Support SLA (Service Level Agreement):**
   - Response time guarantees (e.g., 24h for critical bugs)
   - Support channels (email, ticket system, phone)
   - Update frequency commitment

4. **Automated installer:**
   - OSGeo4W dependency checking (prompt to install if missing)
   - JRE bundling (currently manual via jpackage)
   - Silent/unattended installation mode
   - Version upgrade (install over previous version without data loss)

5. **License activation system:**
   - License key validation
   - Hardware binding (machine-locked or floating)
   - Trial period management
   - License expiration and renewal

6. **Update mechanism:**
   - Check for updates on startup
   - Download and install updates
   - Rollback on failed update
   - Changelog display

7. **User documentation (comprehensive):**
   - PDF manual or online documentation
   - Tutorial videos
   - FAQ
   - Troubleshooting guide

8. **Data export guarantees:**
   - Documented export formats with verified fidelity
   - No vendor lock-in (all data accessible in standard formats)
   - Migration path to/from QGIS and ArcGIS

9. **Support infrastructure:**
   - Bug tracker (GitHub Issues or dedicated system)
   - Knowledge base
   - Community forum

**Estimated time to commercial readiness:** 6-12 months, requiring a dedicated team (legal, support, development).

---

## Summary Table

| Level | Decision | Missing Critical Items | Estimated Time |
|-------|----------|------------------------|----------------|
| Personal Use | **YES** | None (known limitations are acceptable) | 0 days |
| Closed Beta | **CONDITIONAL** | PostGIS integration test, manual test plan, installer test, known issues doc | 1-2 days |
| Public Beta | **NO** | Format test coverage, PostGIS CI, plugin security, user docs, installer validation | 4-6 weeks |
| Professional Use | **NO** | Performance benchmarks, stability tests, CRS boundary tests, corruption recovery | 3-6 months |
| Commercial Sale | **NO** | Legal review, EULA, support SLA, activation system, update mechanism, comprehensive docs | 6-12 months |

---

## Immediate Priority Recommendations

For the fastest path to Closed Beta (the next achievable milestone):

1. **Execute MANUAL_TEST_PLAN.md** -- 14 test cases, ~2 hours
2. **Connect to a real PostGIS server** -- verify layer load and attribute display
3. **Test the installer on a clean Windows VM** -- verify JRE bundling and OSGeo4W detection
4. **Review plugin ClassLoader isolation** -- verify sandbox security
5. **Create Known Issues documentation** -- format support limitations, GDAL path requirements

These 5 items represent the minimum viable Closed Beta. Everything beyond this is Public Beta territory and requires significantly more investment.
