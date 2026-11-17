# CATSERVER — Políticas SQL T2–T4 — Tasks

- [x] **S1 — DC-6/T2: vista canónica `circunscripcion_sector`**
  - Renombrar tabla cruda a `_raw` con migración idempotente, ajustar índice,
    trigger y grants; vista canónica en `18`; tests PostGIS `_tests/dc6_*`.

- [x] **S2 — DC-7/T3: ownership y default privileges del rol DDL**
  - Defaults para `current_user` en todos los esquemas; grants explícitos a
    `catserver_etl` en `raw`/`staging`; test `_tests/dc7_privileges.sql`.

- [x] **S3 — DC-8/T4: rutas destructivas**
  - Helper `DestructiveGuard.ps1`; switch `-AllowDestructive` en
    `07/10/12/14/16`; stage+swap en reescrituras; contrato estático
    `DestructiveScriptPolicyTest`.

- [x] **S4 — Certificación**
  - PR/CI exactos, PostGIS Real Integration PASS, merge con expected head,
    post-main y attestation; evidencia en `docs/quality/` y en el backlog
    `catserver-decision-backlog`.

## Dependencias

- S2 depende de S1 sólo por orden de scripts (02 antes de 04); pueden ir en el
  mismo PR.
- S4 cierra S1–S3 en main.

## Evidencia local (2026-11-17)

- S1: `04_core_tables.sql` migra una tabla legada a `circunscripcion_sector_raw`,
  crea la cruda privada y ajusta indice/trigger/grants; `18` mantiene la vista
  canonica; `_tests/dc6_canonical_view.sql` valida vista, tabla cruda e
  idempotencia. Job PostGIS ejecuta la prueba.
- S2: `02_roles_and_privileges.sql` agrega `ALTER DEFAULT PRIVILEGES` para el rol
  DDL real (`current_user`) y DML para el owner; grants explicitos a
  `catserver_etl` en raw/staging. `_tests/dc7_privileges.sql` valida herencia y
  ausencia de DDL operativo para el rol de import.
- S3: `scripts/_common/DestructiveGuard.ps1`; `07/10/12/14/16` declaran
  `-AllowDestructive`, usan backup/restauracion o stage+swap, y `14` reemplaza la
  tabla viva en transaccion; `DestructiveScriptPolicyTest` (4) lo verifica.
- Validacion real pendiente del PR CI (PostGIS Real Integration + static tests);
  sin claim de bases reales fuera de CI.
  - Evidence (2026-11-17): PR #252 (head `fdd805e`) con PostGIS Real
    Integration PASS (`_tests/dc6_*`, `_tests/dc7_*`) y checks completos; merge
    `48b008a` con Certification/CI/Runtime Identity/GeoParquet SUCCESS y
    attestation `44075755840`. Sin claim de bases del operador fuera de CI.