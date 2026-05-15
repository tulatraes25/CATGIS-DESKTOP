# Instalacion Paso A Paso En Windows

Guia operativa para dejar lista una maquina Windows para `CATSERVER`.

## Versiones recomendadas

Verificadas el 2026-04-13:

- PostgreSQL: rama 17 soportada en Windows por el instalador EDB
- PostGIS: 3.6.2 para PostgreSQL 14-18 en Windows
- CATGIS: cliente operativo principal para tu flujo de trabajo

Fuentes oficiales:

- [PostgreSQL Windows](https://www.postgresql.org/download/windows/)
- [PostGIS Windows Releases](https://postgis.net/documentation/getting_started/install_windows/released_versions/)

## A. Instalar PostgreSQL

### 1. Descargar

1. Abrir [PostgreSQL Windows](https://www.postgresql.org/download/windows/).
2. Entrar al instalador de EDB.
3. Elegir PostgreSQL 17 x64 para Windows.

### 2. Instalar

Durante el instalador:

1. Dejar seleccionado:
   - PostgreSQL Server
   - pgAdmin
   - StackBuilder
2. Elegir directorio de instalacion.
   - Recomendado: `C:\Program Files\PostgreSQL\17`
3. Elegir directorio de datos.
   - Recomendado: `C:\CATSERVER\pgdata`
   - Si luego tenes otro disco, migramos a `D:\CATSERVER\pgdata`
4. Definir una clave fuerte para el usuario `postgres`.
5. Puerto: `5432`
6. Locale: dejar por defecto si no hay politica interna distinta.
7. Completar la instalacion.

### 3. Validar al terminar

Abrir PowerShell y correr:

```powershell
psql --version
```

Si `psql` no entra por PATH, probar con:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" --version
```

## B. Instalar PostGIS

La recomendacion oficial de PostGIS para Windows es usar StackBuilder sobre la instalacion EDB de PostgreSQL.

### 1. Abrir StackBuilder

1. Buscar `StackBuilder` en el menu Inicio.
2. Elegir la instalacion PostgreSQL 17 recien instalada.

### 2. Seleccionar paquete

Dentro de StackBuilder:

1. Ir a `Spatial Extensions`.
2. Elegir el paquete PostGIS mas reciente compatible.
   - Al 2026-04-13, la referencia oficial publicada es `PostGIS 3.6.2`.
3. Completar el asistente de instalacion.

### 3. Habilitar extensiones en la base

Esto ya lo hace nuestro bootstrap SQL cuando exista la base `"CATSERVER"`.

Si queres verificar manualmente luego:

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_raster;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;
SELECT postgis_full_version();
```

## C. Preparar CATGIS

### 1. Objetivo

No necesitas QGIS si vas a trabajar con `CATGIS`.

Necesitas que CATGIS pueda:

- abrir `KMZ`
- abrir `DWG/DXF`
- conectarse a PostGIS

### 2. Estado funcional verificado

En el codigo actual de CATGIS ya estan contemplados:

- `KML / KMZ`
- `CAD DWG / DXF`
- `Origen de datos PostGIS`

### 3. Si vas a abrir DWG

Para `DWG`, CATGIS hoy trabaja con flujo asistido:

- `DXF` gemelo
- o convertidor CAD detectado

### 4. Convertidor recomendado para DWG

Si no tenes `DXF` gemelo por archivo, instala un convertidor compatible como `ODA File Converter`.

### 5. Validar helper ETL

Abrir una nueva consola y correr:

```powershell
ogr2ogr --version
gdalinfo --version
```

Si no aparecen en PATH, no bloquean el uso de CATGIS como cliente. Solo afectan el helper ETL para poblar PostGIS en esta primera etapa.

## D. Verificacion rapida de entorno

Una vez instalado todo:

```powershell
powershell -ExecutionPolicy Bypass -File C:\CATGIS\CATSERVER\scripts\05_verify_environment.ps1
```

Ese script te dira si estan visibles:

- `psql`
- `pg_restore`
- `pg_dump`
- `ogr2ogr`
- `gdalinfo`
- `CATGIS`
- convertidor CAD

## E. Crear la base CATSERVER

Con PostgreSQL y PostGIS listos:

```powershell
powershell -ExecutionPolicy Bypass -File C:\CATGIS\CATSERVER\scripts\03_run_bootstrap_psql.ps1
```

Si `psql` no esta en PATH:

```powershell
powershell -ExecutionPolicy Bypass -File C:\CATGIS\CATSERVER\scripts\03_run_bootstrap_psql.ps1 `
  -PsqlPath "C:\Program Files\PostgreSQL\17\bin\psql.exe"
```

## F. Crear usuarios login reales

El bootstrap crea roles base sin login. Despues de eso hay que crear usuarios reales.

Plantilla recomendada:

```sql
CREATE ROLE catserver_admin_user LOGIN PASSWORD 'CAMBIAR_AQUI';
GRANT catserver_admin TO catserver_admin_user;

CREATE ROLE catserver_etl_user LOGIN PASSWORD 'CAMBIAR_AQUI';
GRANT catserver_etl TO catserver_etl_user;

CREATE ROLE catserver_read_user LOGIN PASSWORD 'CAMBIAR_AQUI';
GRANT catserver_read TO catserver_read_user;
```

## G. Smoke test de base

Una vez creada `"CATSERVER"`:

```powershell
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -d CATSERVER -f C:\CATGIS\CATSERVER\sql\06_post_install_smoke_test.sql
```

## H. Primera importacion recomendada

Arrancar por KMZ:

1. `Parcelas.kmz`
2. `Limite de Barrios.kmz`
3. `Circunscripciones - Sectores.kmz`
4. `Zonificacion por Ordenanza.kmz`

Comando base:

```powershell
powershell -ExecutionPolicy Bypass -File C:\CATGIS\CATSERVER\scripts\04_import_kmz_examples.ps1 `
  -User catserver_etl_user `
  -Password TU_CLAVE
```

## I. Que revisar antes de tocar los DWG

No conviene importar los DWG a ciegas sin resolver esto:

1. CRS real de dibujo
2. Layers CAD utiles vs ruido tecnico
3. Unidades del dibujo
4. Si las polilineas estan cerradas o no
5. Si el ejido PAC y los barrios coinciden espacialmente con los KMZ

## J. Primer checklist de manana

1. Instalar PostgreSQL 17.
2. Confirmar `psql`.
3. Instalar PostGIS 3.6.2 por StackBuilder.
4. Tener CATGIS disponible.
5. Instalar convertidor CAD si hace falta para DWG.
6. Confirmar `ogr2ogr` solo si vas a usar helper ETL.
7. Ejecutar `05_verify_environment.ps1`.
8. Ejecutar `03_run_bootstrap_psql.ps1`.
9. Crear usuarios login reales.
10. Ejecutar `06_post_install_smoke_test.sql`.
