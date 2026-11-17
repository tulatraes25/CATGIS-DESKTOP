# CATSERVER Decision Backlog — Tasks

Status: ACTIVE (T2–T4 DECIDED 2026-11-16 option A; implementation pending GitHub CI; T1 still needs decision)
Baseline: `f965869a8ef73e36afae0de6218cd4191ce8a151` (main after PR #215)
Origin: transferred from `.kiro/specs/repository-audit-remediation/` at its
CLOSED_CERTIFIED closure (2026-11-15).

None of these tasks may start with code changes until the corresponding
decision is recorded in this file. Each completed item follows the same
GitHub-first gate as the closed remediation spec: new branch from live main,
exact PR head CI + Certification + Runtime Identity + GeoParquet, merge with
`expected_head_sha`, post-main runs and post-main attestation.

## Pending

> Fichas de decisión preparadas (2026-11-16):
> `docs/quality/SQL_DECISION_RECORDS_20261116.md` con opciones, recomendación,
> impacto y criterios de aceptación para DC-6/DC-7/DC-8. T2–T4 siguen
> bloqueadas hasta que el owner registre la decisión en esta spec.

- [ ] **T1 — DC-5: producer for `catastro.ejido_urbano_mask`**
  - Needs: authoritative source (survey export, CAD/DWG pipeline or GIS service)
    and the role that runs the publisher.
  - Deliverable: import/derive script honoring `gid`/`geom`/SRID 4326, idempotent,
    with a synthetic-fixture test in the PostGIS job; docs/11 updated.
  - Acceptance: REQ-AR-16.

- [ ] **T2 — DC-6: canonical `circunscripcion_sector` object**
  - **Decisión (2026-11-16, owner): A — vista canónica sobre la tabla cruda
    renombrada.**
  - Implementacion (2026-11-17): `sql/04` migra la tabla legada a
    `circunscripcion_sector_raw` y crea la cruda privada; `sql/18` mantiene la
    vista canonica; test PostGIS `_tests/dc6_canonical_view.sql`. Pendiente
    certificacion CI.
  - Needs: table vs view decision (recommended: view canonical, raw table renamed).
  - Deliverable: `sql/04`/`sql/18`/`sql/19`/catalog updated, full 00→21 sequence
    runs in the PostGIS job without collision.
  - Acceptance: REQ-AR-17.

- [ ] **T3 — DC-7: single ownership/privilege model**
  - **Decisión (2026-11-16, owner): A — rol DDL dueño + rol de import con GRANT
    explícitos y ALTER DEFAULT PRIVILEGES.**
  - Implementacion (2026-11-17): `sql/02` aplica defaults para el rol DDL real y
    DML del owner, con grants de import en raw/staging; test PostGIS
    `_tests/dc7_privileges.sql`. Pendiente certificacion CI.
  - Needs: DDL role and import role decided; default-privilege targets confirmed.
  - Deliverable: `sql/02`/`sql/12`/`sql/19` aligned, CI role provisioning
    documented, PostGIS test proving defaults apply to a newly created table.
  - Acceptance: REQ-AR-18.

- [ ] **T4 — DC-8: destructive-path policy**
  - **Decisión (2026-11-16, owner): A — stage+swap obligatorio en reescrituras y
    `--allow-destructive` sólo para borrados.**
  - Implementacion (2026-11-17): helper `DestructiveGuard.ps1`; `07/10/12/14/16`
    con backup/swap y switch; `14` cambia la tabla viva en transaccion; contrato
    `DestructiveScriptPolicyTest`. Pendiente certificacion CI.
  - Needs: policy for overwrite/delete per script (stage+swap vs explicit flag).
  - Deliverable: shared guard/parameter in `scripts/07/10/12/14/16`, docs updated,
    static contract in the safety job.
  - Acceptance: REQ-AR-19.

- [x] **T5 — DC-9: branch administration**
  - Needs: approval to delete the 113 merged branches; triage of the 46 unmerged.
  - Deliverable: re-validated inventory at execution time, deletions with
    before/after counts, per-branch supersede notes for the unmerged set.
  - Acceptance: REQ-AR-20.
  - Evidence (2026-11-15): inventario revalidado con `git ls-remote` y
    `git merge-base --is-ancestor` (160 remotas: 114 contenidas / 46 no); se
    borraron **113** ramas contenidas en `main` (excluyendo `main`) via API, con
    SHA registrado en `docs/quality/BRANCH_INVENTORY_20261115.md`; quedan 47
    (main + 46 historicas sin mergear, conservadas para triage).
- [x] **T6 — DC-10: dev-tooling advisories**
  - Needs: authorization for the eslint/minimatch toolchain upgrade.
  - Deliverable: patched dev dependencies, `npm audit` clean for that chain,
    Web Viewer Lint re-certified (or written acceptance of the dev-only risk).
  - Acceptance: REQ-AR-21.
  - Evidence (2026-11-15): `npm audit fix` en `catserver-web` subio la cadena
    eslint/minimatch dentro de los rangos existentes; `npm audit` = **0
    vulnerabilidades**, `npm run lint` exit 0 y `npm test` **31/31**.
- [x] **T7 — DC-11: historical documents reconciliation**
  - Needs: rewrite vs annotate vs archive decision for superseded records.
  - Deliverable: dated banner or archive path under every historical spec reached
    by the reconciliation note.
  - Acceptance: REQ-AR-22.
  - Evidence (2026-11-15): se anotaron con banner fechado
    `.kiro/specs/repository-sanitization/requirements.md`, `design.md` y
    `t10c-support-handoff.md`, distinguiendo los IDs activos
    (repositorio `1406486908`, ruleset `24546459`) de los históricos; el cuerpo
    historico no se reescribio.
- [x] **T8 — DC-12: skip inventory freeze**
  - Needs: none (documentation-only), but must keep the count frozen.
  - Deliverable: documented skip list with gates; static check that new skips
    carry a documented justification.
  - Acceptance: REQ-AR-23.
  - Evidence (2026-11-15): `docs/quality/SKIP_INVENTORY.md` con las 14 marcas /
    15 casos, su gate y archivo:linea; regla de congelamiento incluida y
    alineada con el test de arquitectura que prohibe `@Disabled`.

## Blockers

- Every task above is blocked on a user decision except T8 (documentation-only,
  ready once the freeze rule is accepted).
- A0.7 remains NOT_CERTIFIED; none of these tasks may claim GUI/native runtime
  correctness.
- No deletions of user data, credentials, GIS datasets or backups are in scope.
