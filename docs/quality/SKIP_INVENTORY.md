# Inventario de tests saltados (skip inventory)

Status: registro de ejecucion (congelamiento T8 del backlog de decisiones).
Baseline: `main` `a64de322e028de2f9194ae33f48535d886886da5` (2026-11-15).

## Regla

- El inventario **no puede crecer** sin un gate documentado en este archivo y
  aprobacion explicita.
- Los saltos son **condicionales por entorno** (`Assumptions.assumeTrue`), no
  `@Disabled`: la suite prohíbe `@Disabled` y verifica que las suposiciones
  condicionales queden en una lista explicita
  (`ArchitectureCertificationTest.conditionalAssumptionsRemainExplicitlyAllowlisted`).
- En el runner de CI la mayoria de estos tests **se ejecutan** (GDAL nativo esta
  provisto en Linux/macOS); el conteo de "skipped" del reporte local refleja el
  entorno de quien corre la suite.

## Inventario (14 marcas / 15 casos salteados)

| Clase | Caso | Gate | Evidencia |
|---|---|---|---|
| `FlatGeobufRealTest` | `assumeOgr2ogrAvailable` | `ogr2ogr` no disponible en el entorno | `src/test/java/ar/com/catgis/FlatGeobufRealTest.java:33` |
| `FlatGeobufRealTest` | `assumeGdalCompatible` | GDAL incompatible con el reader FlatGeobuf | `FlatGeobufRealTest.java:58` |
| `GeoPackageRealTest` | `assumeOgr2ogrAvailable` | `ogr2ogr` no disponible | `GeoPackageRealTest.java:26` |
| `GeoPackageRealTest` | `assumeGdalCompatible` | GDAL con cambio de formato SQLite >= 3.13 | `GeoPackageRealTest.java:38` |
| `SpatiaLiteRealTest` | `assumeOgr2ogrAvailable` | `ogr2ogr` no disponible | `SpatiaLiteRealTest.java:23` |
| `SpatiaLiteRealTest` | `assumeGdalCompatible` | GDAL con cambio de formato SQLite >= 3.13 | `SpatiaLiteRealTest.java:35` |
| `HeavyFormatsIntegrationTest` | `flatgeobufRoundTripWorksWhenOgr2ogrIsAvailable` | `ogr2ogr` ausente | `HeavyFormatsIntegrationTest.java:111` |
| `HeavyFormatsIntegrationTest` | `flatgeobufRoundTripWorksWhenOgr2ogrIsAvailable` | GDAL >= 3.13 incompatible | `HeavyFormatsIntegrationTest.java:112` |
| `HeavyFormatsIntegrationTest` | `spatialiteRoundTripWorksWhenOgr2ogrIsAvailable` | `ogr2ogr` ausente | `HeavyFormatsIntegrationTest.java:133` |
| `HeavyFormatsIntegrationTest` | `spatialiteRoundTripWorksWhenOgr2ogrIsAvailable` | GDAL >= 3.13 incompatible | `HeavyFormatsIntegrationTest.java:134` |
| `ReleaseFormatCompatibilityTest` | `resolvesDwgWithAssistedConverterWhenAvailable` | convertidor CAD local ausente | `ReleaseFormatCompatibilityTest.java:209` |
| `ArchitectureCertificationTest` | `junitDisabledAnnotationsAreForbidden` | regla de arquitectura (no es un skip de entorno) | `ArchitectureCertificationTest.java:136-137` |
| `ArchitectureCertificationTest` | `conditionalAssumptionsRemainExplicitlyAllowlisted` | regla de arquitectura (no es un skip de entorno) | `ArchitectureCertificationTest.java:149` |

## Consecuencia para el programa

- Reducir el inventario **no** es trabajo de feature: depende de disponer de
  GDAL/conversores en el entorno de ejecucion, ya cubiertos por los jobs nativos
  de certificacion.
- Cualquier test nuevo debe correr en CI o registrar su gate aqui; no se acepta
  `@Disabled`.
