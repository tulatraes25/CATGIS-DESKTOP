# CATGIS Desktop — Tested Environments

**Fecha**: 2026-06-14 | **HEAD**: `d0934b7`

---

## Entornos probados

| Windows | Java | OSGeo4W / GDAL | PostgreSQL / PostGIS | Pantalla | Resultado | Notas |
|---|---|---|---|---|---|---|
| Windows 11 23H2 | OpenJDK 17.0.9 | OSGeo4W64 (GDAL 3.8.4) | — | 1920x1080 (100% scaling) | ✅ BUILD SUCCESSFUL, 647 tests | Entorno de desarrollo principal |
| *(pendiente)* | | | | | ⬜ No verificado | Testear en Windows 10 |
| *(pendiente)* | | | | | ⬜ No verificado | Testear con Java 21 |
| *(pendiente)* | | | | | ⬜ No verificado | Testear con HiDPI (150% scaling) |
| *(pendiente)* | | | | | ⬜ No verificado | Testear con PostgreSQL 14 + PostGIS 3.4 |

---

## Dependencias verificadas en build

| Dependencia | Versión | Gradle |
|---|---|---|
| GeoTools | 34.0 | `org.geotools:gt-main:34.0` |
| JTS | 1.20.0 | `org.locationtech.jts:jts-core:1.20.0` |
| PDFBox | 3.0.3 | `org.apache.pdfbox:pdfbox:3.0.3` |
| FlatLaf | 3.4.1 | `com.formdev:flatlaf:3.4.1` |
| Log4j2 | 2.23.1 | `org.apache.logging.log4j:log4j-core:2.23.1` |
| JUnit Jupiter | 5.10.2 | `org.junit.jupiter:junit-jupiter:5.10.2` |
| HikariCP | 5.1.0 | `com.zaxxer:HikariCP:5.1.0` |
| FlatGeobuf (wololo) | 3.26.2 | `org.wololo:flatgeobuf:3.26.2` |
| H3 (Uber) | 4.1.1 | `com.uber:h3:4.1.1` |
| Jackson | 2.17.2 | `com.fasterxml.jackson.core:jackson-databind:2.17.2` |
| Checkstyle | 10.18.0 | `com.puppycrawl.tools:checkstyle:10.18.0` |
| JaCoCo | 0.8.12 | `org.jacoco:org.jacoco.agent:0.8.12` |

---

## Entorno de build

| Componente | Versión / Config |
|---|---|
| Gradle | 8.7 (wrapper) |
| Java target | 17 |
| Encoding | UTF-8 |
| Headless tests | `java.awt.headless=true` |
| OS | Windows 11 23H2 (amd64) |

---

## Instrucciones para testers

Para agregar una fila a esta tabla:

1. Instalar CATGIS según `BETA_CERRADA.md`
2. Ejecutar `probar-catgis.bat`
3. Realizar las pruebas del `MANUAL_TEST_PLAN.md`
4. Reportar:
   - Versión exacta de Windows (`winver`)
   - Versión de Java (`java -version`)
   - Versión de GDAL (`gdalinfo --version`)
   - Resolución y scaling de pantalla
   - Resultado general (✅ / ⚠️ / ❌)
   - Notas sobre cualquier problema
