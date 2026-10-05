# Inventario de ramas — cierre de saneamiento 2026-10-04

Este documento registra el estado final de refs públicas después de la reescritura histórica y la limpieza administrativa T10A.

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

Sin embargo, GitHub todavía puede servir algunos objetos pre-rewrite por SHA o vistas cacheadas. La remoción server-side de esos objetos/vistas permanece pendiente de GitHub Support.

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

## Pendientes de cierre T10

1. rotar/revocar las credenciales CATSERVER históricamente expuestas;
2. obtener la disposición de purge/caché de GitHub Support;
3. confirmar privacidad/noreply a nivel de cuenta GitHub;
4. cerrar issue #28 y pasar el spec a `CLOSED_CERTIFIED`.

La licencia del repositorio fue decidida como **propietaria** y se documenta en `LICENSE`.
