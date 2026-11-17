# CATSERVER — Políticas SQL T2–T4 — Design

## DC-6 (T2) — vista canónica

- `sql/04_core_tables.sql`:
  - Bloque DO de migración: si existe una **tabla** `catastro.circunscripcion_sector`
    (instalación legada), se renombra a `catastro.circunscripcion_sector_raw`.
  - `CREATE TABLE IF NOT EXISTS catastro.circunscripcion_sector_raw (...)`.
  - Índice GiST y trigger `updated_at` sobre `_raw`.
  - Grants: SELECT a `catserver_admin, catserver_etl`; DML a
    `catserver_admin, catserver_etl`; secuencia a ambos.
- `sql/18_thematic_business_views.sql` crea `CREATE OR REPLACE VIEW
  catastro.circunscripcion_sector` sobre `catastro.circsect_shape_src` y conserva
  el GRANT SELECT a `admin/read/publish`. `19` y el catálogo siguen leyendo el
  nombre canónico (vista).
- La tabla `_raw` queda privada: el acceso público pasa siempre por la vista.

## DC-7 (T3) — ownership/privilegios

- `sql/02_roles_and_privileges.sql` agrega un bloque DO que emite
  `ALTER DEFAULT PRIVILEGES FOR ROLE <current_user>` para todos los esquemas
  operativos, porque el bootstrap puede correr como superusuario y los defaults
  previos sólo apuntaban a `catserver_owner` (nunca aplicaban en CI).
- Grants explícitos para `catserver_etl` en `raw`/`staging` (tablas y secuencias).
- Test `_tests/dc7_privileges.sql`: crea una tabla probe como rol DDL, verifica
  herencia de privilegios con `has_table_privilege` y `has_schema_privilege`, y
  limpia la tabla.

## DC-8 (T4) — rutas destructivas

- Helper PowerShell `scripts/_common/DestructiveGuard.ps1` con:
  - `Assert-CatserverOverwriteAllowed` (stage+swap para salidas),
  - `Assert-CatserverDropAllowed` (`-AllowDestructive` para DROP/borrado).
- Scripts `07/10/12/14/16` importan el helper y exigen el switch para pisar
  resultados; las reescrituras escriben en staging y publican con swap.
- Contrato estático Java (`DestructiveScriptPolicyTest`) que exige la marca del
  helper y el switch en cada script listado y prohíbe `Remove-Item -Recurse -Force`
  sin él.

## Pruebas

- Job `PostGIS Real Integration`: ejecuta `_tests/dc6_canonical_view.sql` y
  `_tests/dc7_privileges.sql` sobre la base `CATSERVER` del bootstrap.
- Job de arquitectura: `DestructiveScriptPolicyTest` + tests existentes.
- Los scripts PowerShell no se ejecutan en CI (requieren PostgreSQL/GDAL local),
  por eso el contrato estático más la validación psql del SQL.
