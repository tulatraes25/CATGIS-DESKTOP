# Instalacion Windows

## Recomendacion inicial

Para arrancar rapido y con el menor riesgo en Windows:

- PostgreSQL instalado nativamente como servicio Windows
- PostGIS agregado sobre esa instalacion
- CATGIS como cliente GIS principal
- convertidor CAD asistido para DWG cuando haga falta
- GDAL/OGR solo como helper ETL si todavia no implementamos exportacion directa desde CATGIS a PostGIS

## Stack recomendado

Verificado contra fuentes oficiales el 2026-04-13:

- PostgreSQL para Windows: recomiendo PostgreSQL 17 como base estable actual para este proyecto.
- PostGIS para Windows: la pagina oficial de releases Windows publica PostGIS 3.6.2 y el bundle indica compatibilidad con PostgreSQL 14-18.
- CATGIS: el cliente operativo recomendado para tu caso.

## Orden de instalacion

1. Instalar PostgreSQL para Windows con el instalador oficial EDB.
2. Instalar pgAdmin si no viene incluido.
3. Desde StackBuilder o bundle oficial, agregar PostGIS al PostgreSQL instalado.
4. Tener CATGIS disponible en la maquina operativa.
5. Instalar convertidor CAD si vas a trabajar con DWG sin DXF gemelo.
6. Confirmar si `ogr2ogr` y `gdalinfo` quedaron accesibles por PATH en caso de usar helper ETL.
7. Recien despues ejecutar el bootstrap SQL de `CATSERVER`.

## Configuracion de servidor recomendada

- Nombre del equipo o alias operativo: `CATSERVER`
- Base de datos: `"CATSERVER"`
- Puerto PostgreSQL: `5432`
- Directorio de trabajo recomendado:
  - si tenes segundo disco: `D:\CATSERVER`
  - si no, usar `C:\CATSERVER`
- Mantener separados:
  - `incoming`
  - `inventory`
  - `exports`
  - `backups`
  - `logs`

## Lo minimo para manana

- PostgreSQL
- PostGIS
- pgAdmin
- CATGIS

## Lo que puede esperar

- Docker
- GeoServer
- pgRouting
- automatizacion ETL completa
- replica o servidor secundario

## Verificaciones posteriores a la instalacion

En `psql` o pgAdmin:

```sql
SELECT version();
SELECT current_database();
SELECT name, default_version, installed_version
FROM pg_available_extensions
WHERE name IN ('postgis', 'postgis_raster', 'pgcrypto', 'btree_gist');
```

Despues de habilitar PostGIS:

```sql
SELECT postgis_full_version();
```

## Decision practica

Para esta etapa no recomiendo arrancar con Docker porque hoy no esta instalado en la maquina y te suma una capa extra de complejidad. Tampoco hace falta QGIS si tu flujo operativo va a ser con `CATGIS`. Primero estabilicemos `CATSERVER` nativa en Windows y despues, si queres, armamos la mejora de exportacion directa desde CATGIS a PostGIS.
