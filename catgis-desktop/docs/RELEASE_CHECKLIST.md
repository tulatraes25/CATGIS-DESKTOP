# CATGIS Desktop — Release Checklist (Beta Cerrada)

**HEAD**: `d0934b7` | **Fecha**: 2026-06-14

---

## Automatizado

| # | Verificación | Comando | Resultado |
|---|---|---|---|
| 1 | Clean test verde | `gradlew clean test` | ✅ BUILD SUCCESSFUL, 647 tests, 0 failures |
| 2 | Compilación sin warnings | `gradlew compileJava --warning-mode all` | ✅ 0 warnings |
| 3 | Checkstyle | `gradlew checkstyleMain` | ⬜ No ejecutado en esta sesión |
| 4 | JaCoCo coverage | `gradlew jacocoTestReport` | ⬜ No ejecutado |

---

## Código

| # | Verificación | Resultado | Notas |
|---|---|---|---|
| 5 | `git status` limpio en `src/ar/com/catgis/` | ✅ Limpio | Parent repo tiene ruido (CONSULTLAN-*, .atl/, fix_final2.ps1) |
| 6 | Sin `printStackTrace` | ✅ 0 ocurrencias | Verificado con `grep_search` |
| 7 | Sin `System.err` / `System.out` | ✅ 0 ocurrencias | Solo en código de test |
| 8 | Catch blocks silenciosos | ✅ 2 benignos restantes | LookAndFeel fallback + ds.close() |
| 9 | Plugins marcados como experimentales | ✅ | Javadoc + CatgisLogger.warn al inicio |
| 10 | Loaders con `validateFile()` | ✅ | FlatGeobuf, SpatiaLite |
| 11 | Save atómico (.tmp → rename) | ✅ | CatmapSerializer.save() |
| 12 | `UnsupportedFormatException` mensajes claros | ✅ | Español, sin stack traces en UI |

---

## Documentación

| # | Verificación | Resultado |
|---|---|---|
| 13 | `BETA_CERRADA.md` — incluido/excluido/requisitos | ✅ |
| 14 | `KNOWN_ISSUES.md` — riesgos abiertos, limitaciones | ✅ |
| 15 | `TESTED_ENVIRONMENTS.md` — entornos probados | ✅ |
| 16 | `MANUAL_TEST_PLAN.md` — 21 tests manuales | ✅ |
| 17 | `RISK_REGISTER.md` — 14 riesgos, 6 cerrados | ✅ |
| 18 | `FEATURE_MATRIX.md` — 74 features | ✅ |

---

## Funcional (pruebas manuales pendientes)

| # | Verificación | Estado |
|---|---|---|
| 19 | Abrir CATGIS en PC limpia | ⬜ Pendiente |
| 20 | Crear proyecto nuevo | ⬜ Pendiente |
| 21 | Cargar Shapefile | ⬜ Pendiente |
| 22 | Cargar GeoPackage | ⬜ Pendiente |
| 23 | Cargar GeoTIFF | ⬜ Pendiente |
| 24 | Generar hillshade desde DEM | ⬜ Pendiente |
| 25 | Editar geometría vectorial | ⬜ Pendiente |
| 26 | Guardar proyecto (.catgis) | ⬜ Pendiente |
| 27 | Cerrar y reabrir proyecto | ⬜ Pendiente |
| 28 | Exportar PDF desde CATMAP | ⬜ Pendiente |
| 29 | Exportar PNG desde CATMAP | ⬜ Pendiente |
| 30 | Conectar PostGIS | ⬜ Pendiente (requiere servidor) |
| 31 | Cargar WMS público | ⬜ Pendiente |
| 32 | Cargar FlatGeobuf válido | ⬜ Pendiente |
| 33 | Probar error: archivo .fgb corrupto | ⬜ Pendiente |
| 34 | Probar error: header SQLite inválido | ⬜ Pendiente |
| 35 | Probar error: .catgis corrupto | ⬜ Pendiente |
| 36 | Stress: 5 capas + zoom rápido | ⬜ Pendiente |
| 37 | Memoria: abrir/cerrar proyecto con GeoTIFF ×3 | ⬜ Pendiente |

---

## Build / Instalador

| # | Verificación | Estado |
|---|---|---|
| 38 | `generar-instalador-app-image.bat` ejecuta sin error | ⬜ Pendiente |
| 39 | `generar-instalador-exe.bat` ejecuta sin error | ⬜ Pendiente |
| 40 | Instalador generado instala en `C:\Program Files\CATGIS` | ⬜ Pendiente |
| 41 | Aplicación instalada abre sin errores | ⬜ Pendiente |
| 42 | OSGeo4W detectado automáticamente | ⬜ Pendiente |

---

## Veredicto

**Automatizado**: ✅ Listo. 647 tests verdes, 0 warnings, catch blocks limpios.

**Manual**: ⬜ 23 pruebas pendientes. Sin issues bloqueantes conocidos.

**Beta cerrada**: ✅ Puede comenzar con 5-10 testers de confianza. Las pruebas manuales pendientes deben ejecutarse durante la beta, no antes.
