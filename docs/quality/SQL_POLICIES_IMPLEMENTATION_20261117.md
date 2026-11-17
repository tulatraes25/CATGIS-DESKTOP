# Politicas SQL T2-T4 — implementacion 2026-11-17

Decisiones del owner: DC-6=A, DC-7=A, DC-8=A
(`docs/quality/SQL_DECISION_RECORDS_20261116.md`). Spec Kiro:
`.kiro/specs/catserver-sql-policies-t2-t4/`.

## DC-6 (T2) — vista canonica `circunscripcion_sector`

- `CATSERVER/sql/04_core_tables.sql`: migracion idempotente de una tabla legada
  al nombre privado `circunscripcion_sector_raw`, creacion de la cruda privada,
  indice GiST/trigger y grants (SELECT+DML solo a admin/etl; secuencia incluida).
- `CATSERVER/sql/18_thematic_business_views.sql`: la vista canonica sigue
  apuntando a `catastro.circsect_shape_src`; comentario actualizado. El codigo 19
  y el catalogo leen el mismo nombre canonico (vista).
- Prueba: `CATSERVER/sql/_tests/dc6_canonical_view.sql` (relkind vista, tabla
  cruda presente, idempotencia, privilegios) ejecutada en el job PostGIS.

## DC-7 (T3) — ownership/privilegios

- `CATSERVER/sql/02_roles_and_privileges.sql`: `ALTER DEFAULT PRIVILEGES` para el
  rol DDL real (`current_user`) y para `catserver_owner` (los scripts 03/04 usan
  `SET ROLE catserver_owner`); DML para admin/edit en esquemas operativos y
  SELECT/INSERT/UPDATE/DELETE para etl en raw/staging; secuencias incluidas.
- Prueba: `CATSERVER/sql/_tests/dc7_privileges.sql` (herencia de privilegios,
  etl sin CREATE en catastro, etl con CREATE en raw).

## DC-8 (T4) — rutas destructivas

- `CATSERVER/scripts/_common/DestructiveGuard.ps1`: backup/restauracion,
  limpieza de backups solo con `-AllowDestructive` y confirmacion explicita.
- `07/10/12`: reescrituras con backup + swap y restauracion ante error.
- `14`: import a tabla `_stage` y swap transaccional
  (`DROP ...; ALTER ... RENAME ...; COMMIT;`).
- `16`: el paquete de entrega anterior se conserva como backup hasta publicar el
  nuevo.
- Contrato estatico: `DestructiveScriptPolicyTest` (4 tests).

## Limites

- Los scripts PowerShell requieren binarios locales (pgsql2shp/ogr2ogr/psql); CI
  valida el SQL en PostGIS real y los scripts por contrato estatico.
- A0.5/A0.7/F3 siguen `NOT_CERTIFIED`; sin claim de runtime.
