# Fichas de decisión SQL (T2–T4 / DC-6, DC-7, DC-8)

Estado: **PENDIENTE DE DECISIÓN DEL OWNER**. Según
`.kiro/specs/catserver-decision-backlog/tasks.md`, ninguna de estas tareas puede
tocar código/SQL hasta que la decisión quede registrada aquí. Estas fichas
preparan la decisión (opciones, recomendación, impacto y criterio de aceptación)
para que aprobarla sea un acto explícito y rápido.

## DC-6 — Objeto canónico `circunscripcion_sector` (T2)

| Opción | Descripción | Pro | Contra |
|---|---|---|---|
| A (recomendada) | **Vista canónica** `circunscripcion_sector` sobre la tabla cruda renombrada | una sola fuente de lectura; migraciones futuras detrás de la vista; no duplica datos | requiere renombrar tabla y actualizar `sql/04`, `sql/18`, `sql/19` y catálogo |
| B | Mantener la tabla cruda como canónica y documentar el contrato | cambio mínimo | nombres ambiguos persisten; riesgo de divergencia |
| C | Tabla materializada sincronizada | lecturas rápidas | doble escritura y riesgo de desincronización |

Aceptación (cualquier opción): la secuencia completa `00→21` corre en el job
PostGIS sin colisiones y el catálogo refleja el objeto elegido. REQ-AR-17.

**Decisión owner:** `[ ] A  [ ] B  [ ] C` — fecha / autor: ____________

## DC-7 — Modelo único de ownership/privilegios (T3)

| Opción | Descripción | Pro | Contra |
|---|---|---|---|
| A (recomendada) | Rol DDL dueño del esquema + rol de import con `GRANT` explícitos y `ALTER DEFAULT PRIVILEGES` apuntando al rol DDL | un solo camino auditable; defaults se aplican a tablas nuevas | requiere provisionar ambos roles en CI |
| B | Un único rol para DDL e import | simple | viola mínimo privilegio; import puede alterar esquema |

Aceptación: `sql/02`, `sql/12`, `sql/19` alineados a la opción; CI provisiona los
roles; test PostGIS prueba que una tabla nueva hereda los defaults. REQ-AR-18.

**Decisión owner:** `[ ] A  [ ] B` — fecha / autor: ____________

## DC-8 — Política de rutas destructivas (T4)

| Opción | Descripción | Pro | Contra |
|---|---|---|---|
| A (recomendada) | **Stage + swap** obligatorio para reescrituras y flag explícito `--allow-destructive` sólo para borrados | sin ventanas de datos parciales; borrado auditable | más pasos operativos |
| B | Flag explícito para toda operación destructiva | simple | una reescritura interrumpida deja datos parciales |
| C | Sin guardas (estado actual) | cero trabajo | riesgo de pérdida silenciosa de datos |

Aceptación: guard/parámetro compartido en `scripts/07/10/12/14/16`, docs
actualizados y contrato estático en el job de seguridad. REQ-AR-19.

**Decisión owner:** `[ ] A  [ ] B  [ ] C` — fecha / autor: ____________

## Reglas comunes

- Sin decisión registrada no se abre PR de código para T2–T4.
- Tras la decisión: rama desde `main` vivo, PR con CI + Certification + merge con
  `expected_head_sha`, runs post-main y `main-push-attestation` antes de cerrar.
- Las fichas no modifican datos, credenciales ni el repositorio remoto.
