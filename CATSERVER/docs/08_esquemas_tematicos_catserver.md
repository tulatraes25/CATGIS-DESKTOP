# Esquemas Tematicos CATSERVER

## Criterio

En `CATSERVER` conviene separar dos cosas:

- esquemas de negocio: lo que usa la gente para trabajar en CATGIS
- esquemas tecnicos: importaciones crudas, staging, control y soporte interno

Con este criterio, la base queda mas parecida a una organizacion por carpetas de `shape`, pero con ventajas de `PostGIS`.

## Esquemas de negocio

- `catastro`
  - `barrios`
  - `parcelas`
  - `circunscripcion_sector`
  - `cad_barrios_lineas`
  - `cad_barrios_rotulos`
  - `cad_barrios_control`
  - `ejido_urbano`

- `planeamiento`
  - `zonificacion`

- `ingenieria`
  - `lineas`

- `hidrologia`
  - `reservorios_poligonos`
  - `reservorios_barreras`
  - `reservorios_nombres`
  - `drenaje_cuencas_principales`
  - `drenaje_cuencas_regionales`
  - `drenaje_lineas`
  - `drenaje_puentes`
  - `drenaje_etiquetas_fuente`

## Esquemas tecnicos

- `raw`: cargas crudas desde `KMZ`
- `staging`: consolidaciones y pasos intermedios
- `infraestructura`: tablas fuente legacy y salidas tecnicas de limpieza
- `admin`: control, metadatos y seguridad

## Idea principal

Las tablas fuente originales no se eliminan.

Para trabajo diario se publican vistas limpias y cortas, por ejemplo:

- `catastro.barrios` en vez de `catastro.barrios_shape_src`
- `planeamiento.zonificacion` en vez de `planeamiento.zonif_shape_src`
- `ingenieria.lineas` en vez de `infraestructura.lineas_shape_operativa`

Eso permite:

- mantener trazabilidad tecnica
- exponer nombres prolijos al usuario
- seguir exportando a `shape` cuando haga falta
- reorganizar CATGIS sin romper los datos fuente

## Edicion

Las vistas operativas tematicas se publican como `solo lectura` en `CATGIS`.

Esto es intencional: sirven para navegar, consultar, simbolizar y exportar, sin exponer tablas fuente con nombres tecnicos.

Si mas adelante queres un circuito de edicion formal, conviene definirlo sobre tablas maestras puntuales o sobre un workflow de escritura controlado, no directamente sobre todas las vistas tematicas.

## Carga en CATGIS

`CATGIS` sigue entrando por `public.v_catserver_layers`.

Esa vista ahora apunta a las vistas operativas de negocio, no a las tablas fuente con sufijos como `_src`, `_shape` o `_ejido`.

## Futuro

Si mas adelante aparecen nuevas areas, se recomienda seguir el mismo patron:

- `ecosistemas.flora`
- `ecosistemas.fauna`
- `ecosistemas.transectas`
- `ingenieria.accesibilidad`
- `ingenieria.locacion`
- `ingenieria.linea_conduccion`

La idea es que el esquema represente el area tematica y la vista represente la capa operativa oficial.
