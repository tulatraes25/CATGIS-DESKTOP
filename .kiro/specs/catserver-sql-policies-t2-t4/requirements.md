# CATSERVER — Políticas SQL T2–T4 — Requisitos

Estado: ACTIVE. Decisiones del owner registradas el 2026-11-16
(`docs/quality/SQL_DECISION_RECORDS_20261116.md`): **DC-6 = A**, **DC-7 = A**,
**DC-8 = A**. Esta spec implementa esas decisiones.

## REQ-SQL-1 — Objeto canónico `circunscripcion_sector` (DC-6 / T2)

THE SYSTEM SHALL exponer `catastro.circunscripcion_sector` como **vista
canónica** sobre la tabla cruda privada `catastro.circunscripcion_sector_raw`,
sin colisión de nombres, y el pipeline `00→21` SHALL poder ejecutarse de forma
idempotente.

Acceptance:

- Tras `00→04`, `catastro.circunscripcion_sector_raw` existe como tabla y
  `catastro.circunscripcion_sector` no existe como tabla.
- La vista canónica creada por `18` es idempotente (`CREATE OR REPLACE VIEW`) y
  es legible por `catserver_read`/`catserver_publish`.
- Una instalación legada con tabla canónica se migra renombrando a `_raw` antes
  de crear la vista.

## REQ-SQL-2 — Ownership/privilegios únicos (DC-7 / T3)

THE SYSTEM SHALL aplicar `ALTER DEFAULT PRIVILEGES` para el **rol DDL real**
(`current_user` del bootstrap, típicamente superusuario o `catserver_owner`), de
modo que tablas nuevas hereden los `GRANT` por rol, y `catserver_etl` (import) no
pueda crear objetos en esquemas operativos.

Acceptance:

- Una tabla nueva en `catastro` hereda SELECT para `catserver_read`/`publish` y
  DML para `catserver_edit`/`admin`, sin INSERT para `catserver_read`.
- `catserver_etl` conserva USAGE/CREATE en `raw`/`staging` y no CREATE en
  `catastro`/`infraestructura`/`planeamiento`/`hidrologia`.
- Test PostGIS `dc7_privileges.sql` verde en el job `PostGIS Real Integration`.

## REQ-SQL-3 — Rutas destructivas (DC-8 / T4)

THE SYSTEM SHALL impedir reescrituras/borrados silenciosos en los scripts
`07`, `10`, `12`, `14`, `16`: reescrituras con **stage+swap** y borrados sólo con
`--allow-destructive`/`-AllowDestructive`, con contrato estático en el job de
seguridad.

## No objetivos

- Cambiar el nombre de la base o romper instalaciones existentes.
- Firmar/notarizar, GUI ni runtime (A0.5/A0.7/F3 siguen `NOT_CERTIFIED`).
