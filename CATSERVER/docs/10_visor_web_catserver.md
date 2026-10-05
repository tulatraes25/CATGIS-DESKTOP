# Visor Web CATSERVER

## Recomendacion principal

Para `CATSERVER` no te recomiendo empezar con `PHP` como primera opcion en `2026`, salvo que tengas ya un equipo y una base de codigo fuerte en ese stack.

La opcion mas limpia para tu caso hoy es:

- `PostgreSQL + PostGIS` como base maestra
- `pg_tileserv` para servir capas como teselas vectoriales
- `pg_featureserv` para consultas, popups, filtros, descarga y funciones de busqueda
- frontend `HTML + JavaScript`
- visor hecho con `MapLibre GL JS`

## Por que esta arquitectura

### `pg_tileserv`

Sirve capas y vistas de PostGIS como `vector tiles` directamente desde la base.

Eso permite:

- buen rendimiento con muchas parcelas y lineas
- poco mantenimiento
- publicar tablas y vistas sin montar un GIS server pesado
- respetar permisos de PostgreSQL

### `pg_featureserv`

Sirve tablas, vistas y funciones como API web.

Eso te resuelve:

- consultas por atributos
- popup con ficha de objeto
- filtros por `bbox`
- endpoints HTML y JSON
- funciones de busqueda personalizadas

### `MapLibre GL JS`

Es una libreria moderna para mapas web con muy buen rendimiento en `vector tiles`.

Sirve muy bien para:

- capas encendibles y apagables
- panel lateral
- popups
- filtros
- estilos por capa
- vista moderna tipo mapa municipal

## Alternativa valida

Si queres una experiencia mas GIS clasica y menos orientada a teselas vectoriales, `OpenLayers` tambien es muy buena opcion.

Pero para este `CATSERVER` puntual, con bastante dato vectorial municipal y necesidad de buen rendimiento visual, recomiendo arrancar con `MapLibre`.

## Estructura sugerida del visor

### Pestañas o arbol lateral

Un panel izquierdo con estas pestañas:

- `Ordenamiento Territorial`
- `Gobierno, Modernizacion y Transparencia`
- `Infraestructura, Obras y Servicios Publicos`
- `Busqueda`
- `Ficha`

Dentro de cada pestaña:

- lista de capas por subsecretaria
- checkbox de visibilidad
- orden de dibujo
- boton `zoom a capa`
- transparencia

### Busquedas utiles

Minimo recomendable:

- buscar `barrio`
- buscar `partida`
- buscar `circunscripcion/sector/division/parcela`
- buscar `zonificacion`

Opcionales despues:

- buscar por `uso_suelo`
- buscar por `riesgo`
- buscar por `codigo de zona`
- buscar por nombre de red o tendido

### Popup / ficha

Cuando el usuario hace click:

- nombre de capa
- atributos mas utiles
- enlace a ficha completa
- opcion `copiar coordenadas`
- opcion `zoom`

## Lo que ya te deje preparado en la base

En `CATSERVER` ya quedaron listas estas piezas para el futuro visor:

- catalogo web:
  - `portal_web.v_catalogo_capas`

- funciones de busqueda:
  - `portal_web.buscar_barrios(q, limit_rows)`
  - `portal_web.buscar_parcelas(q, limit_rows)`
  - `portal_web.buscar_zonificacion(q, limit_rows)`
  - `portal_web.buscar_general(q, limit_rows)`

Estas funciones ya devuelven:

- tipo de resultado
- identificador
- titulo
- detalle
- capa origen
- centro del objeto
- `bbox`

Eso te sirve directo para:

- armar lista de resultados
- hacer `zoom`
- centrar el mapa

## Como lo haria por fases

### Fase 1

- instalar `pg_tileserv`
- instalar `pg_featureserv`
- publicar `public.v_catserver_layers`
- probar `portal_web.buscar_general`
- armar un visor HTML simple con mapa, panel y popups

### Fase 2

- busqueda rapida con autocompletar
- panel por secretarias y subsecretarias
- leyenda
- filtros por atributos
- descarga GeoJSON / CSV de resultados

### Fase 3

- login municipal
- auditoria de consultas
- favoritos
- impresion PDF
- fichas urbanisticas o catastrales

## Mi recomendacion concreta para vos

Si queres algo mantenible y moderno:

1. `CATSERVER` sigue en `PostGIS`
2. publicamos capas con `pg_tileserv`
3. exponemos consultas con `pg_featureserv`
4. hacemos el visor con `MapLibre GL JS`
5. usamos `portal_web.buscar_general` para el buscador

No pisaria de entrada con `PHP` salvo que ya tengas infraestructura heredada que obligue a eso.

## Fuentes oficiales y tecnicas

- Autoridades municipales de Comodoro Rivadavia:
  - https://www.comodoro.gov.ar/2025/10/15/autoridades/
- Institucional / Investigacion Territorial:
  - https://www.comodoro.gov.ar/miciudad/2025/10/13/institucional/
- `pg_tileserv`:
  - https://access.crunchydata.com/documentation/pg_tileserv/1.0.11/introduction/
  - https://access.crunchydata.com/documentation/pg_tileserv/1.0.11/usage/tiles/
- `pg_featureserv`:
  - https://access.crunchydata.com/documentation/pg_featureserv/1.3.1/
  - https://access.crunchydata.com/documentation/pg_featureserv/1.3.0/usage/api/
  - https://access.crunchydata.com/documentation/pg_featureserv/latest/usage/functions/
  - https://access.crunchydata.com/documentation/pg_featureserv/latest/usage/ui/
- `MapLibre GL JS`:
  - https://maplibre.org/
  - https://maplibre.org/maplibre-gl-js/docs/API/classes/VectorTileSource/
  - https://maplibre.org/maplibre-gl-js/docs/examples/add-a-vector-tile-source/
- `OpenLayers`:
  - https://openlayers.org/doc/quickstart.html
  - https://openlayers.org/doc/
