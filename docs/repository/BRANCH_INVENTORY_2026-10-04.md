# Inventario de ramas — cierre de saneamiento 2026-10-04

Este documento registra el estado final de refs públicas después de la reescritura histórica y la limpieza administrativa T10A.

## Nota de vigencia (auditoría 2026-11-15)

Este documento es un registro de certificación fechado (T10A, 2026-10-04). El remoto ya no coincide con el estado certificado:

- `git ls-remote origin` (2026-11-15) devuelve **150 `refs/heads`** y **0 tags**, con `refs/heads/main` = `aecb4d20…`;
- el clon local `C:\CATGIS`, tras `git fetch --prune` (2026-11-15), tiene `origin/main` alineado en `aecb4d20…` y sólo un subconjunto de ramas remotas como refs locales;
- por lo tanto `main` sigue siendo la rama canónica, pero **no** es la única rama remota existente.

La afirmación de "1 rama remota" corresponde al cierre T10A y no debe citarse como estado actual.

## Fuente canónica

### `main`

**Estado:** ACTIVA / CANÓNICA / PROTEGIDA

Todo desarrollo nuevo debe:

1. partir del HEAD vigente de `main`;
2. usar una rama corta;
3. volver mediante Pull Request;
4. pasar los checks CATGIS obligatorios;
5. fusionarse por merge normal.

## Estado remoto certificado

Después de T10A:

- ramas remotas: **1**;
- única rama: **`main`**;
- tags públicos: **0**;
- forks: **0**;
- PRs abiertos al momento de la certificación: **0**;
- ruleset `CATGIS main protection` activo;
- force-push y borrado de `main` bloqueados;
- no existen bypass actors.

Durante T10A se eliminaron administrativamente **44 ramas remotas obsoletas** después de comprobar que no existían PRs abiertos y que `main` era la única línea canónica.

## Qué ocurrió con las ramas históricas

Las antiguas ramas `master`, `chore/*`, `fix/*`, `docs/*`, `test/*`, `feat/*`, `codex/*`, `emdash/*` y otras refs usadas durante saneamiento fueron previamente:

- auditadas;
- neutralizadas o reescritas según correspondía;
- preservadas localmente cuando existía valor forense;
- retiradas del remoto una vez cerrado su propósito.

No deben recrearse para continuar desarrollo histórico. Si una capacidad antigua vuelve a ser necesaria, debe reimplementarse o recuperarse deliberadamente desde evidencia forense bajo un nuevo PR y contra el `main` actual.

## Historia sensible

La reescritura publicada eliminó de las ramas/tags activos:

- el archivo histórico CATSERVER con credenciales;
- `kosmo_decompilado/`;
- inventarios operativos `CATSERVER/inventory/*.csv`;
- rutas privadas de workstation incluidas en el alcance.

Sin embargo, GitHub todavía puede servir algunos objetos pre-rewrite por SHA o vistas cacheadas. El cierre posterior se resolvió por aislamiento de repositorio (T10C/T10D): el repositorio original y la primera réplica pública quedaron privados y la línea pública final se publicó desde un único root commit saneado. GitHub Support resultó innecesario para el cierre (`SUPPORT_NOT_REQUIRED`); ver `.kiro/specs/repository-sanitization/t10c-support-handoff.md` y AC-10.3 en `.kiro/specs/repository-sanitization/requirements.md`.

## Clones locales

El checkout operativo Windows `C:\CATGIS` fue reemplazado por un clon limpio post-rewrite y validado con `git fsck --full` y baseline Gradle.

Los restos pre-rewrite se conservan únicamente como evidencia forense fuera del flujo de desarrollo. Ningún desarrollo normal puede continuar desde esos restos.

## Regla operativa

- `main` es la única rama persistente.
- Ramas nuevas: cortas y temporales.
- PR enfocado.
- CI obligatorio.
- Merge normal.
- Borrar la rama temporal después de integrar.
- Nunca empujar desde material pre-rewrite.

## Pendientes de cierre T10 (histórico — resueltos)

Los pendientes registrados en esta sección quedaron cerrados en T10B–T10E (ver `.kiro/specs/repository-sanitization/tasks.md`):

1. rotar/revocar las credenciales CATSERVER históricamente expuestas — `T10B = CLOSED_CERTIFIED`;
2. disposición de purge/caché de GitHub Support — no requerida: cerrado por aislamiento de repositorio (`T10C = CLOSED_CERTIFIED_BY_REPOSITORY_ISOLATION`);
3. privacidad/noreply a nivel de cuenta GitHub — `ACCOUNT_PRIMARY_EMAIL_VISIBILITY=PRIVATE` (`T10D`);
4. cierre de issue #28 y paso del spec a `CLOSED_CERTIFIED` — `T10E = CLOSED_CERTIFIED`; `SANITATION_PROGRAM=CLOSED_CERTIFIED`.

La licencia del repositorio fue decidida como **propietaria** y se documenta en `LICENSE`.
