# CATSERVER Decision Backlog — Bugfix/Decision Requests


> **Decisión DC-5 (2026-11-19):** sin fuente autoritativa ni rol
> publicador, `catastro.ejido_urbano_mask` se declara `UNSUPPORTED` y se
> retira del alcance beta. Este documento queda como análisis histórico;
> no se implementa productor. Ver `tasks.md` y el manifiesto.

Status: ACTIVE (decision-gated; no production code changes without a recorded decision)
Owner: CATGIS
Baseline: `f965869a8ef73e36afae0de6218cd4191ce8a151` (main after PR #215)
Origin: transferred from `.kiro/specs/repository-audit-remediation/` at its
CLOSED_CERTIFIED closure (2026-11-15). Each item keeps the audit evidence and
adds the decision options and acceptance criteria.
Program: BUGFIX_ONLY until A0.7; no dependency upgrades, no GUI/native claims.

## Goal

Resolve the eight decision-gated defects that a repository-wide audit found in
CATSERVER and repository operations, without inventing data-model, ownership or
policy decisions on the user's behalf. Every item ends in either (a) a recorded
decision plus a certified implementation, or (b) an explicit written decision to
accept the current behavior.

## REQ-AR-16 — Produce the ejido mask automatically (DC-5 producer)

WHEN a CATSERVER installation runs the stage scripts, THE SYSTEM SHALL have a
documented, versioned producer for `catastro.ejido_urbano_mask` instead of
requiring a manual import.

- Current state: `sql/10`, `sql/11` and `sql/18` fail closed with an actionable
  message (`_common/assert_ejido_mask.sql`, certified in PR #212); the full
  `gid`/`geom`/SRID 4326 contract is documented in `CATSERVER/docs/11`.
- Acceptance: a script (for example `CATSERVER/scripts/19_import_ejido_mask.ps1`)
  imports the source shape into the contract with verified CRS, idempotent
  reload, and a PostGIS-job test over a synthetic fixture; docs/11 updated.
- Decision needed: which source file/feature class is authoritative and under
  which repository role the publisher runs.

## REQ-AR-17 — Single canonical circumscription object (DC-6)

WHEN `sql/04` and `sql/18` are applied to the same database, THE SYSTEM SHALL
NOT hit the `catastro.circunscripcion_sector` table/view collision.

- Current state: `sql/04` creates the table; `sql/18` creates a view with the
  same name, so a full 00→21 run aborts at `sql/18`.
- Options: (a) make the view canonical and demote the table (or rename it to a
  staging name), (b) rename the view, (c) keep both in different schemas.
- Acceptance: decision recorded, scripts idempotent in sequence, PostGIS job
  executes the affected scripts in order without the collision.

## REQ-AR-18 — One ownership/privilege model (DC-7)

WHEN any CATSERVER script grants privileges or sets default privileges, THE
SYSTEM SHALL use one documented object-owner role and one documented creator
role, so grants are effective for the objects that actually get created.

- Current state: `sql/02` sets default privileges `FOR ROLE catserver_owner`
  (NOLOGIN, never creates objects); `sql/12` sets them `FOR ROLE postgres`;
  `sql/19` mixes both families; the certification job creates a `postgres` role
  to mirror a default install.
- Options: (a) creator role = `catserver_owner` and run all DDL as that role,
  (b) creator role = the bootstrap superuser and parameterize it, (c) split
  runtime roles from DDL roles explicitly.
- Acceptance: decision recorded, `sql/02` and `sql/12` consistent, PostGIS job
  proves default privileges apply to a table created afterwards by the
  documented role.

## REQ-AR-19 — Destructive-path policy (DC-8)

WHEN a CATSERVER script deletes or overwrites existing output/tables, THE
SYSTEM SHALL require a documented gate (backup, staging, or explicit confirm
flag) instead of unconditional `Remove-Item`/`DROP`/`--overwrite`.

- Current state: unconditional removals exist in `scripts/07`, `10`, `12`,
  `14`, `16` (e.g. `Remove-Item -Recurse -Force`, `DROP TABLE IF EXISTS`) and
  `ogr2ogr --overwrite` guidance in docs.
- Acceptance: a written policy (what may be overwritten, what must be staged),
  implemented as a shared guard/parameter in the affected scripts and covered by
  a static contract test.

## REQ-AR-20 — Branch administration (DC-9)

WHEN the repository is audited, THE SYSTEM SHALL have an explicit, dated branch
inventory with a removal plan for merged historical branches.

- Current state (2026-11-15, `main` `f965869`): **159 remote branches**, of which
  **113 are already contained in main** and **46 are not**. The 46 are historical
  candidate stacks (vector atomic, project-state, CRS, FGB/LAS, UX/mojibake,
  A0 quality, WCS/XYZ, attestation, research) — the same work already rescued or
  superseded; see `design.md` for the full list.
- Options: (a) delete the 113 merged branches now (admin action, reversible via
  SHA), then triage the 46 one by one; (b) keep all and only freeze new branches.
- Acceptance: user decision recorded, inventory re-validated at execution time,
  deletions executed only after explicit approval, with a post-cleanup count.

## REQ-AR-21 — Dev-tooling advisories (DC-10)

WHEN `npm audit` runs on `catserver-web`, THE SYSTEM SHALL have no unaddressed
high-severity advisory in the dependency chain used by CI.

- Current state: 4 advisories (3 moderate, 1 high) from `brace-expansion` and
  `minimatch` through `@eslint/config-array` → `eslint`; dev-only tooling.
- Options: (a) upgrade `eslint`/`minimatch` to patched versions in a dedicated
  maintenance change (requires authorization and re-certification of lint), (b)
  accept the risk in writing while npm advisories persist.
- Acceptance: decision recorded; if upgraded, `npm audit` clean for the eslint
  chain and Web Viewer Lint still PASS.

## REQ-AR-22 — Historical documents reconciliation (DC-11)

WHEN historical certification records are consulted, THE SYSTEM SHALL visibly
distinguish superseded ruleset/repository identifiers from live ones.

- Current state: `requirements.md` and `t10c-support-handoff.md` in older specs
  contain superseded ruleset and repository IDs; only a dated reconciliation
  note was added.
- Acceptance: decision recorded (rewrite, annotate, or archive with pointer to
  the current sources), and every historical document reached by the note either
  carries a dated banner or lives under an `archive/` path.

## REQ-AR-23 — Skip inventory freeze (DC-12)

WHEN the Java suite runs, THE SYSTEM SHALL keep the skip inventory at or below
the certified baseline and SHALL document each skip's dependency gate.

- Current state: 15 skipped tests (14 skip markers) in 6 classes:
  `FlatGeobufRealTest`, `GeoPackageRealTest`, `SpatiaLiteRealTest`,
  `HeavyFormatsIntegrationTest`, `ReleaseFormatCompatibilityTest`,
  `ArchitectureCertificationTest` — all gated on optional native/driver
  availability or architecture policy, not on ignored bugs.
- Acceptance: a documented list with the gate for each skip; new skips require
  the same justification; reducing the inventory is optional separate work.

## Constraints

- Do not rename databases, tables or roles across installations without an
  explicit migration plan and backup.
- Do not delete `C:\catgis` data, user projects, credentials, GIS datasets or
  backups as part of any of these items until GitHub gates close and a separate
  inventory approval exists.
- A0.7 remains NOT_CERTIFIED; this backlog does not change that.
