# Organizacion Municipal de CATSERVER

## Criterio aplicado

En `CATSERVER` quedaron dos formas de navegar las capas:

- organizacion tematica: para trabajo tecnico GIS
- organizacion municipal: para trabajo alineado con la estructura del Municipio

La vista que usa `CATGIS` por defecto ahora es la municipal:

- `public.v_catserver_layers`

Tambien quedan disponibles:

- `public.v_catserver_layers_tematicas`
- `public.v_catserver_layers_municipal`
- `admin.v_catserver_estructura_municipal`

## Estructura municipal usada

La organizacion se tomo de la pagina oficial de autoridades de la Municipalidad de Comodoro Rivadavia, publicada el `15-10-2025`.

Fuente oficial:

- https://www.comodoro.gov.ar/2025/10/15/autoridades/

Se uso tambien la presentacion institucional de la Direccion General de Modernizacion e Investigacion Territorial, donde se indica su dependencia de la Subsecretaria de Modernizacion y Transparencia dentro de la Secretaria de Gobierno, Modernizacion y Transparencia.

Fuente oficial:

- https://www.comodoro.gov.ar/miciudad/2025/10/13/institucional/

## Esquemas municipales preparados

Quedaron creados estos esquemas:

- `gobierno_modernizacion`
- `control_urbano_operativo`
- `ordenamiento_territorial`
- `infraestructura_osp`
- `desarrollo_humano_familia`
- `mujer_genero_juventud_diversidad`
- `cultura`
- `salud`
- `recaudacion`
- `economia_finanzas`

No todos tienen capas todavia. Los que hoy quedaron poblados con vistas GIS son:

- `ordenamiento_territorial`
- `gobierno_modernizacion`
- `infraestructura_osp`

## Mapeo actual de capas

### Ordenamiento Territorial

- `tierras_barrios`
- `tierras_parcelas`
- `tierras_circunscripcion_sector`
- `planeamiento_zonificacion`
- `planeamiento_ejido_urbano`
- `tierras_cad_barrios_lineas`
- `tierras_cad_barrios_rotulos`
- `tierras_cad_barrios_control`
- `ambiente_pozos_petroleros`

Sububicacion institucional aplicada para esa capa:

- secretaria: `Secretaria de Ordenamiento Territorial`
- subsecretaria: `Subsecretaria de Ambiente`
- direccion general: `Direccion General de Minas e Hidrocarburos`

### Gobierno, Modernizacion y Transparencia

- `redes_servicios_publicos_lineas`

### Infraestructura, Obras y Servicios Publicos

- `obras_reservorios_poligonos`
- `obras_reservorios_barreras`
- `obras_reservorios_nombres`
- `obras_drenaje_cuencas_principales`
- `obras_drenaje_cuencas_regionales`
- `obras_drenaje_lineas`
- `obras_drenaje_puentes`
- `obras_drenaje_etiquetas_fuente`

## Importante

Las capas municipales se publican como vistas `solo lectura`.

Eso permite:

- ordenar la base segun la estructura institucional
- no duplicar datos fisicos
- mantener la base tematica original
- seguir exportando a `shape`
- evitar que se editen por error vistas institucionales pensadas para consulta

## Recomendacion de uso

- usar `public.v_catserver_layers` para el dia a dia en `CATGIS`
- usar `public.v_catserver_layers_tematicas` para tareas tecnicas GIS
- usar `admin.v_catserver_estructura_municipal` para documentacion, auditoria y capacitacion interna
