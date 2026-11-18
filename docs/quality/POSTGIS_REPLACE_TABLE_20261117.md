# PostGIS — reemplazo de tabla transaccional (2026-11-17)

Issue #156 (audit #263). Estado: **implementado; verificación real en el job
PostGIS de CI**.

## Problema

El modo "Replace Table" hacía `DROP` antes de crear/escribir y estaba bloqueado
por seguridad: cualquier fallo podía destruir la última tabla válida del usuario.

## Solución

- El dataset nuevo se escribe **completo en una tabla temporal**
  `<tabla>_catgis_stage_<8hex>` creada por GeoTools en el mismo schema.
- `PostgisReplaceSwap.swap(...)` abre una conexión JDBC y, **en una sola
  transacción**:
  1. verifica `count(*)` de la tabla temporal contra las filas escritas;
  2. `DROP TABLE IF EXISTS` de la tabla destino;
  3. `ALTER TABLE ... RENAME TO` del staging a la tabla destino;
  4. verifica `count(*)` de la tabla publicada;
  5. `COMMIT`.
  Cualquier error hace `ROLLBACK` (la tabla anterior sobrevive) y el staging se
  elimina con `DROP TABLE IF EXISTS` best-effort.
- Identificadores schema/tabla/staging se validan con `^[A-Za-z_][A-Za-z0-9_]{0,62}$`
  y se citan; staging ≤63 chars. No hay concatenación de entrada sin validar.
- `REPLACE_TABLE` sigue **oculto en el diálogo de exportación** (pendiente de
  certificación de UI); la API de servicio ya lo soporta.
- Si la tabla destino no existe, el modo se comporta como creación nueva
  (no hay nada que perder).

## Evidencia

- `PostgisReplaceSwapPlanContractTest` (4): orden del plan (drop→rename→count),
  staging único/validado, identificadores inseguros rechazados y modo oculto en
  diálogo.
- `PostgisRealIntegrationTest.replaceTableIsTransactionalAndKeepsPreviousDataOnFailure`:
  crea tabla (2 filas), intenta un swap fallido (filas esperadas incorrectas) y
  verifica que la tabla conserva 2 filas; luego reemplaza por la API y confirma
  filas y ausencia de staging. Se ejecuta en el job **PostGIS Real Integration**
  (PostGIS 16-3.4) de CI.
- Suite local: 1665 tests, 0 fallos, 15 skips (sin DB real).

## Límites

- La UI no expone todavía el modo; queda para T5/UI.
- Si existieran vistas dependientes de la tabla, el `DROP` falla y el rollback
  conserva la tabla (fail-closed); no se hace `CASCADE` automático.
