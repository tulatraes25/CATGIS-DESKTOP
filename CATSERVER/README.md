# CATSERVER Bootstrap

Bootstrap inicial para montar `CATSERVER` como base geoespacial municipal sobre PostgreSQL + PostGIS.

## Alcance

Este paquete deja preparado:

- una guia de instalacion para Windows
- un flujo operativo `CATGIS-first`
- una arquitectura inicial de esquemas y roles
- SQL de bootstrap para la base `"CATSERVER"` sin sembrar rutas ni inventarios propios de una instalación
- tablas de metadata, auditoria e inventario
- tablas nucleares para catastro, planeamiento, infraestructura e hidrologia
- scripts PowerShell para armar carpetas, inventariar fuentes y ejecutar bootstrap con `psql`
- ejemplos de importacion KMZ a PostGIS con `ogr2ogr`

## Hallazgos ya confirmados

- Los archivos `.bak` inspeccionados tienen firma `AC1021` / `AC1032`, consistente con backups de AutoCAD y no con backups SQL.
- Los KMZ inspeccionados contienen `doc.kml` interno.
- `Parcelas.kmz` contiene un `doc.kml` muy grande y su cabecera muestra exportacion desde Google Earth Pro.
- CATGIS soporta carga local de `KML/KMZ` y `DWG/DXF`.
- CATGIS resuelve `DWG` mediante `DXF` gemelo o conversor CAD asistido.
- CATGIS conecta a PostGIS y carga capas en modo lectura.
- El lote de datos actual incluye:
  - una carpeta amplia de DWG de barrios y zonas
  - KMZ de parcelas, drenaje, barrios, sectores, lineas electricas, reservorios y zonificacion
  - un DWG principal del ejido PAC

## Supuestos de trabajo

- El nombre visible del proyecto es `CATSERVER`.
- Para respetar ese pedido, la base se crea como `"CATSERVER"` usando comillas.
- Los roles y esquemas usan `snake_case` en minusculas para evitar friccion operativa.
- El CRS de los DWG todavia no esta validado; por eso las tablas finales quedan listas pero sin fijar un SRID obligatorio.
- Los KMZ se tratan inicialmente como fuente KML/WGS84 hasta validacion final.

## Estructura

- [docs/01_instalacion_windows.md](docs/01_instalacion_windows.md)
- [docs/02_modelo_y_convenciones.md](docs/02_modelo_y_convenciones.md)
- [docs/03_pipeline_etl.md](docs/03_pipeline_etl.md)
- [docs/04_instalacion_paso_a_paso_windows.md](docs/04_instalacion_paso_a_paso_windows.md)
- [docs/05_flujo_catgis_first.md](docs/05_flujo_catgis_first.md)
- [sql/00_cluster_bootstrap.sql](sql/00_cluster_bootstrap.sql)
- [sql/01_extensions_and_schemas.sql](sql/01_extensions_and_schemas.sql)
- [sql/02_roles_and_privileges.sql](sql/02_roles_and_privileges.sql)
- [sql/03_admin_metadata.sql](sql/03_admin_metadata.sql)
- [sql/04_core_tables.sql](sql/04_core_tables.sql)
- [sql/05_seed_source_assets.sql](sql/05_seed_source_assets.sql)
- [sql/06_post_install_smoke_test.sql](sql/06_post_install_smoke_test.sql)
- [scripts/01_create_catserver_tree.ps1](scripts/01_create_catserver_tree.ps1)
- [scripts/02_inventory_sources.ps1](scripts/02_inventory_sources.ps1)
- [scripts/03_run_bootstrap_psql.ps1](scripts/03_run_bootstrap_psql.ps1)
- [scripts/04_import_kmz_examples.ps1](scripts/04_import_kmz_examples.ps1)
- [scripts/05_verify_environment.ps1](scripts/05_verify_environment.ps1)

## Orden recomendado

1. Leer [docs/01_instalacion_windows.md](docs/01_instalacion_windows.md).
2. Crear la estructura fisica con `01_create_catserver_tree.ps1`.
3. Ejecutar el inventario inicial con `02_inventory_sources.ps1`.
4. Instalar PostgreSQL, PostGIS y preparar CATGIS como cliente principal.
5. Ejecutar el bootstrap SQL con `03_run_bootstrap_psql.ps1`.
6. Ejecutar `05_seed_source_assets.sql` si se desea cargar el catálogo temático genérico. Este archivo no registra rutas ni metadatos de una estación de trabajo.
7. Generar inventarios locales fuera de Git cuando sea necesario para ETL/control.
8. Importar primero KML/KMZ y luego resolver el flujo CAD con CRS validado.

## Primer objetivo operativo

Dejar `"CATSERVER"` lista para administrar:

- `catastro.parcela`
- `catastro.barrio`
- `catastro.circunscripcion_sector`
- `catastro.ejido_municipal`
- `planeamiento.zonificacion_ordenanza`
- `infraestructura.linea_electrica`
- `infraestructura.reservorio`
- `hidrologia.drenaje`
