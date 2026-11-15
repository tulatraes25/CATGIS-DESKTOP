# Orden de ejecucion y prerrequisitos del pipeline CATSERVER

Este documento completa el [README](../README.md) y
[03_pipeline_etl.md](03_pipeline_etl.md): dice **que script corre cuando**, contra
**que base** y **que datos tiene que existir antes**. Los prefijos numericos son
por carpeta (`sql/` y `scripts/`) y no expresan un orden unico global.

## 1. Nombre fisico de la base (leer antes de ejecutar)

El bootstrap crea la base como `"CATSERVER"` (con mayusculas y comillas, ver
[00_cluster_bootstrap.sql](../sql/00_cluster_bootstrap.sql)). En cambio, los
scripts de las etapas posteriores asumen una base llamada `catserver`
(minusculas):

| Conjunto | Nombre de base que asume |
| --- | --- |
| `sql/00` a `sql/06` | `"CATSERVER"` (literal en `\connect` y en `GRANT ... ON DATABASE`) |
| `scripts/03_run_bootstrap_psql.ps1` | se conecta a `postgres` para crear la base |
| `scripts/04_import_kmz_examples.ps1` | `-Database "CATSERVER"` por defecto |
| `sql/12` a `sql/21` | `catserver` (literal en `\connect` y en `GRANT ... ON DATABASE`) |
| `scripts/06, 07, 14, 15, 17, 18` | `-Database "catserver"` por defecto |

Consecuencia: **en una instalacion nueva hecha con `docs/04`, los SQL 12 a 21
fallan de entrada** con `database "catserver" does not exist`, porque el
`\connect catserver` de cada archivo pisa el `-d` de la invocacion. Y a la
inversa, sobre una base existente `catserver` no deben correrse los SQL 00 a 04
(crearian otra base y apuntarian a `"CATSERVER"`).

Antes de cualquier etapa, verificar la base real:

```sql
SELECT datname FROM pg_database ORDER BY 1;
```

Reglas practicas:

- No renombrar una instalacion existente como parte de una limpieza (ver
  [03_pipeline_etl.md](03_pipeline_etl.md), seccion 7). Normalizar el nombre
  fisico es una migracion aparte, con backup y verificacion.
- Elegir un escenario por instalacion:
  - **A. Instalacion nueva:** ejecutar solo `sql/00` a `sql/06` mas los
    `scripts/*` que no fijen base. Para usar las etapas 07-21 hay que crear
    ademas una base `catserver` o hacer la migracion de nombre.
  - **B. Base existente `catserver`:** no ejecutar `sql/00` a `sql/06`; correr
    las etapas 07-21 y los scripts con `-Database catserver`.
- `psql -d CATSERVER` funciona con el nombre exacto (el argumento no se
  normaliza), pero los scripts que tienen `\connect` adentro ignoran el `-d`.

## 2. Orden SQL base (instalacion nueva)

1. `sql/00_cluster_bootstrap.sql` (como superusuario, base `postgres`).
2. `sql/01_extensions_and_schemas.sql`.
3. `sql/02_roles_and_privileges.sql`.
4. `sql/03_admin_metadata.sql`.
5. `sql/04_core_tables.sql`.
6. `sql/05_seed_source_assets.sql` (opcional, catalogo tematico generico).
7. `sql/06_post_install_smoke_test.sql`.

`scripts/03_run_bootstrap_psql.ps1` automatiza solo los pasos 1 a 5.

**Colision de nombres a resolver antes de unificar bases:** `sql/04` crea la
tabla `catastro.circunscripcion_sector` y `sql/18` crea una vista con el mismo
nombre. Si ambos scripts se ejecutan sobre la misma base, `sql/18` falla
(`"circunscripcion_sector" is a table`). Definir primero cual es el objeto
canonico; no se corrige en este repo porque implica cambio de modelo de datos.

## 3. Prerrequisito que no provee ningun SQL: `catastro.ejido_urbano_mask`

`sql/10`, `sql/11` y `sql/18` filtran contra `catastro.ejido_urbano_mask`, pero
**ningun archivo de `sql/` crea esa tabla**. Hay que importar la mascara del
ejido antes de `sql/10`. Debe estar en EPSG:4326 (los JOIN usan
`ST_Intersects` contra geometrias 4326) y con la columna de geometria llamada
`geom` (los SQL 10 y 11 usan `m.geom` explicito):

```powershell
& "C:\Program Files\PostgreSQL\18\bin\ogr2ogr.exe" `
  -f PostgreSQL "PG:host=localhost port=5432 dbname=catserver user=postgres" `
  "C:\CATSERVER_SOURCE_ROOT\ejido\ejido_urbano_mask.shp" `
  -nln catastro.ejido_urbano_mask -nlt PROMOTE_TO_MULTI `
  -lco GEOMETRY_NAME=geom -t_srs EPSG:4326
```

El `SourceShape` de esa mascara es el mismo que usa
`scripts/16_build_shape_delivery_package.ps1` (`ejido\ejido_urbano_mask.shp`).

## 4. Contrato de importacion KMZ a `raw`

Los SQL 07 a 11 esperan **nombres de tabla fijos** y columnas con el criterio
por defecto de `ogr2ogr` PostgreSQL: `ogc_fid`, `wkb_geometry`, `name`,
`description`. No cambiar `-lco FID=` ni `-lco GEOMETRY_NAME=` en estas cargas.

| Tabla `raw` esperada | Fuente | Consumidor |
| --- | --- | --- |
| `raw.barrios_kmz` | Limite de Barrios.kmz (o DWG barrios) | `sql/08` |
| `raw.parcelas_kmz` | Parcelas.kmz | `sql/08` |
| `raw.circ_sect_kmz` | Circunscripciones - Sectores.kmz | `sql/08` |
| `raw.zonif_kmz` | Zonificacion por Ordenanza.kmz | `sql/08` |
| `raw.lineas_kmz` | Lineas electricas.kmz | `sql/08` |
| `raw.reserv_poly_kmz`, `raw.reserv_barr_kmz`, `raw.reserv_nomb_kmz` | capas de Reservorios.kmz | `sql/09` |
| `raw.drenaje_*_kmz` (16 tablas) | Drenaje Comodoro Rivadavia.kml | `scripts/15` importa, `sql/11` consume |
| `raw.pozos_petroleros_cr_kml` | pozos_cr.kml | `scripts/18` importa, `sql/21` consume |

Importacion de una capa suelta con el criterio correcto:

```powershell
& "C:\Program Files\PostgreSQL\18\bin\ogr2ogr.exe" `
  -f PostgreSQL "PG:host=localhost port=5432 dbname=catserver user=postgres" `
  "C:\CATSERVER_SOURCE_ROOT\KMZ\Limite de Barrios.kmz" `
  -nln raw.barrios_kmz -nlt PROMOTE_TO_MULTI
```

Para un KMZ con varias capas (Reservorios) hay que indicar la capa de origen
segun el nombre que tenga dentro del KMZ:

```powershell
& ogr2ogr -f PostgreSQL "PG:..." "Reservorios.kmz" <capa_origen> -nln raw.reserv_poly_kmz -nlt PROMOTE_TO_MULTI
```

`scripts/06_import_kml_via_pgdump.ps1` hace lo mismo por capa
(`-LayerName` + `-Table`) y conserva `ogc_fid`/`wkb_geometry`; es la via
recomendada para las capas de drenaje (la usa `scripts/15`).

**No usar la salida de `scripts/04_import_kmz_examples.ps1` como entrada de
`sql/08`/`sql/09`/`sql/11`:** ese script es un ejemplo y crea tablas con nombre
fechado (`kmz_parcelas_<fecha>`) y con `-lco FID=gid`/`GEOMETRY_NAME=geom`, que
no coinciden con lo que esperan esos SQL. Tambien usa `-overwrite`, que
reemplaza la tabla destino sin confirmacion.

## 5. Orden de las etapas posteriores (base `catserver`)

Las etapas 1 y 2 son independientes entre si; la 3 y 4 son posteriores.

**Etapa 1 - Catastro/planeamiento desde KMZ**

1. `sql/07_kml_html_helpers.sql` (funciones `admin.kml_desc_*`).
2. Cargar a `raw` las tablas de la seccion 4.
3. `sql/08_kmz_shape_layers.sql`.
4. `sql/09_kmz_reservorios_shape.sql`.

**Etapa 2 - CAD barrial (DWG -> DXF -> shape -> staging)**

1. `scripts/08_convert_dwg_safe.ps1` y/o `scripts/09_batch_convert_dwgs_safe.ps1`
   (DWG a DXF).
2. `scripts/10_export_dxf_thematic_shapes.ps1` y/o
   `scripts/11_batch_export_dxf_thematic_shapes.ps1` (DXF a shapes con sufijo
   `_22182` y `_4326`; el SRID de origen es parametro, por defecto 22182 y
   **no esta validado** para todos los DWG).
3. `scripts/12_clip_shapefile_to_mask.ps1` y/o
   `scripts/13_batch_clip_shapes_to_ejido.ps1` (recorte al ejido).
4. `scripts/14_import_cad_shapes_postgis.ps1` (carga `staging.cad_barrios_*_src`).
5. `sql/10_cad_barrios_finalize.sql`.
6. Exportar las capas finales y armar la entrega con `scripts/16`.

**Etapa 3 - Drenaje**

1. `scripts/15_import_drenaje_layers.ps1` (carga `raw.drenaje_*_kmz` via
   `scripts/06`).
2. `sql/11_drenaje_finalize.sql`.

**Etapa 4 - Seguridad, limpieza de lineas y publicacion**

1. `sql/12_security_roles_current.sql` (roles y privilegios; ejecutar antes de
   las vistas para que apliquen los default privileges sobre las tablas nuevas).
2. `sql/13_catserver_layers_view.sql` (vista tecnica previa; `sql/18` la
   reemplaza despues).
3. `sql/14_lineas_cleanup.sql`.
4. `sql/15_lineas_refined_cleanup.sql`.
5. `sql/18_thematic_business_views.sql` (vistas de negocio).
6. `sql/19_municipal_structure_layers.sql` (vistas por secretaria y catalogo
   municipal).
7. `sql/20_portal_web_helpers.sql` (catalogo y funciones del visor web).
8. `scripts/18_import_pozos_petroleros_ejido.ps1` + `sql/21_pozos_petroleros_ejido.sql`
   (pozos; el script importa el KML y luego ejecuta el SQL). `sql/21` crea
   `ordenamiento_territorial.ambiente_pozos_petroleros`, que antes se creaba en
   `sql/19` y fallaba por dependencia con la tabla.

Notas de la etapa 4:

- `sql/12` revoca el `USAGE` sobre `admin` a `catserver_read`. Por eso el
  `GRANT SELECT` de `sql/19` sobre `admin.v_catserver_estructura_municipal` para
  `catserver_read` existe pero no es utilizable sin volver a otorgar el
  `USAGE` (no se agrega por diseno: los visores no deben ver `admin`, ver
  [06_usuarios_y_permisos.md](06_usuarios_y_permisos.md)).
- `sql/13` define `public.v_catserver_layers` con la version tecnica; `sql/18`
  y luego `sql/19` la reemplazan por la version municipal. Correr los tres en
  orden deja la version final de `sql/19`.
- Las funciones de `sql/20` referencian `control_urbano_operativo.pozos_petroleros_ejido`;
  PostgreSQL valida esas referencias al ejecutarlas, no al crearlas, pero
  fallaran si `sql/21` no se ejecuto.

## 6. Contrasenas en linea de comandos

`scripts/04`, `06`, `07`, `14`, `15`, `17` y `18` aceptan `-Password` (y `17`
tambien `-BootstrapPassword`). Pasarlas como argumento las deja visibles en el
historial de PowerShell y en la lista de procesos mientras corre el comando.
Recomendacion operativa:

- usar `PGPASSWORD` en la sesion o un archivo `.pgpass`/`.pg_service.conf`,
- o ejecutar interactivamente y no guardar el comando en el historial,
- no dejar claves en archivos versionados (ver
  [07_datos_conexion_catserver.example.txt](07_datos_conexion_catserver.example.txt)).

`scripts/07_export_shape.ps1` ademas pasa la clave a `pgsql2shp` con `-P`, que
es el unico mecanismo que acepta ese binario; por eso tambien conviene usar
`.pgpass`/`PGPASSWORD` en vez de `-Password`.

## 7. Diferencias de version de herramientas

La documentacion de instalacion recomienda PostgreSQL 17
([04_instalacion_paso_a_paso_windows.md](04_instalacion_paso_a_paso_windows.md)),
pero los scripts que fijan rutas absolutas apuntan a
`C:\Program Files\PostgreSQL\18\bin` y `C:\OSGeo4W\bin`. Ajustar el parametro
correspondiente (`-PsqlPath`, `-Ogr2OgrPath`, `-OgrInfoPath`, `-Shp2PgsqlPath`,
`-Pgsql2ShpPath`, `-OdaPath`) o corregir las rutas por defecto segun la
instalacion real antes de ejecutar.
