# Prompt Inicial Para Armar CATSERVER

Quiero que actues como arquitecto GIS + DevOps + especialista en PostgreSQL/PostGIS y me ayudes a construir desde cero un servidor geoespacial llamado `CATSERVER` para la Municipalidad de Comodoro Rivadavia.

## Objetivo general

Necesito diseñar e implementar una primera version funcional de `CATSERVER` como base geoespacial centralizada para informacion catastral y territorial municipal, usando PostgreSQL + PostGIS. El trabajo debe quedar planteado de manera profesional, ordenada y escalable.

## Contexto

- Trabajo en la Municipalidad de Comodoro Rivadavia.
- Hoy no tengo nada armado del server ni de la base.
- Necesito comenzar desde cero.
- El nombre del servidor, proyecto y base objetivo debe ser `CATSERVER`.
- El enfoque inicial debe priorizar:
  - orden de capas y esquemas
  - carga segura de datos
  - trazabilidad del origen
  - consistencia espacial
  - posibilidad de publicarlo luego por GeoServer, QGIS Server u otro servicio

## Datos disponibles actualmente

### 1. Lote principal de archivos CAD por zonas/barrios

Carpeta:

`C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\2 - DCCION. GRAL. DE CATASTRO ↔ Barrios DWG - A Areas MCR - Enero 2025`

Contiene multiples archivos `.dwg` de barrios y zonas de Comodoro Rivadavia, por ejemplo:

- `Z01 y 16 - Centro y Centro Cívico Gral.Solari.dwg`
- `Z02 - Pietrobelli y Balcón del Paraíso.dwg`
- `Z11 - Gral. E.Mosconi, 25 de Mayo y Divina Providencia.dwg`
- `Z24 - Don Bosco, Std. Sur, Gdor. Fontana, Std.Norte + Restinga Alí.dwg`
- `Z32 - Centenario, Gesta de Malvinas, Dr. R.G. Favaloro + Aeropuerto y C. Chacabuco.dwg`
- `Z40 - Mario Abel Amaya.dwg`

Hay varios `.bak` junto a esos `.dwg`, pero esos `.bak` parecen ser respaldos de AutoCAD y no backups de base de datos.

### 2. Carpeta KMZ

Carpeta:

`C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ`

Contiene al menos:

- `Circunscripciones - Sectores.kmz`
- `Drenaje Comodoro Rivadavia.kmz`
- `Limite de Barrios.kmz`
- `Lineas electricas.kmz`
- `Parcelas.kmz`
- `Reservorios.kmz`
- `Zonificacion por Ordenanza.kmz`

### 3. Archivo PAC principal

- `C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\PAC - Ejido Comodoro Rivadavia - Enero 2025.dwg`
- `C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\PAC - Ejido Comodoro Rivadavia - Enero 2025.bak`

Ese `.bak` tambien debe tratarse inicialmente como backup de AutoCAD, no como backup SQL.

## Restricciones y realidad actual del equipo

- Partimos desde cero.
- En esta maquina hoy no hay Docker, `psql`, `ogr2ogr`, `gdalinfo` ni QGIS instalados.
- Necesito que propongas una estrategia realista para arrancar con lo disponible.
- Si hace falta instalar herramientas, quiero que lo indiques en orden de prioridad.
- Quiero una propuesta pensada para entorno Windows, pero si conviene separar desarrollo local y servidor final, explicalo.

## Lo que necesito que hagas

1. Proponer una arquitectura inicial para `CATSERVER`.
2. Definir una convencion de nombres para:
   - servidor
   - base
   - esquemas
   - tablas
   - vistas
   - usuarios y roles
3. Diseñar un modelo inicial de base para estos datos municipales.
4. Proponer una estrategia ETL para convertir/cargar DWG y KMZ hacia PostGIS.
5. Diferenciar claramente:
   - datos fuente
   - staging
   - datos normalizados
   - publicacion
6. Indicar como manejar:
   - sistema de coordenadas
   - geometria invalida
   - duplicados
   - versionado basico
   - metadatos de origen
7. Preparar un plan de trabajo por fases, desde instalacion hasta primera publicacion.
8. Darme scripts, comandos y estructura de carpetas recomendada.
9. Si alguna decision depende de informacion faltante, no te detengas: hace supuestos razonables y dejalos explicitados.

## Entregables esperados

Quiero que la respuesta quede organizada asi:

### A. Diagnostico inicial

- Que tenemos
- Que falta
- Riesgos principales

### B. Arquitectura recomendada

- Opcion recomendada
- Opcion alternativa
- Pros y contras

### C. Modelo de datos inicial para CATSERVER

Incluir una propuesta de esquemas como minimo del tipo:

- `raw`
- `staging`
- `catastro`
- `infraestructura`
- `planeamiento`
- `hidrologia`
- `public`
- `admin`

Si ves una mejor estructura, proponela con justificacion.

### D. Pipeline de carga

Explicar paso a paso como cargar:

- DWG
- KMZ/KML
- futuras capas SHP, GeoJSON o GPKG

### E. Seguridad y administracion

- roles
- usuarios
- backups
- restauracion
- auditoria basica

### F. Roadmap de implementacion

Dividido en:

- Fase 1: bootstrap tecnico
- Fase 2: inventario y carga inicial
- Fase 3: normalizacion y control de calidad
- Fase 4: publicacion de servicios
- Fase 5: operacion y mantenimiento

### G. Accion inmediata

Cerrá con:

- los primeros 10 pasos concretos para empezar mañana mismo
- la lista minima de software a instalar
- el orden recomendado de ejecucion

## Criterios de respuesta

- Responde en español.
- No des una respuesta generica.
- Usa los nombres reales de los archivos y carpetas que te pasé.
- Priorizá decisiones practicas y ejecutables.
- Si propones scripts o SQL, que sean claros y reutilizables.
- Si hay ambiguedades de CRS/proyeccion en DWG y KMZ, remarcalas como punto critico de validacion.
- Trata el proyecto como una base catastral municipal real, no como ejemplo academico.

## Primer objetivo operativo

Quiero terminar con una base `CATSERVER` lista para:

- recibir capas catastrales
- consultar parcelas, barrios, zonificacion e infraestructura
- integrarse luego con clientes GIS de escritorio y servicios web

