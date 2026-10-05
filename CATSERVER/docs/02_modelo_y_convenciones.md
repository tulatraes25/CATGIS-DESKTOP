# Modelo Y Convenciones

## Convenciones

- Host o alias operativo: `CATSERVER`
- Base: `"CATSERVER"`
- Esquemas: minuscula, `snake_case`
- Tablas: minuscula, `snake_case`
- Vistas publicadas: prefijo `v_`
- Roles: `catserver_*`
- Campos de geometria: `geom`
- PK sinteticas: `bigserial` o `uuid` segun el caso

## Esquemas

- `raw`
  - aterrizaje inicial desde fuente
  - no se edita manualmente
  - ideal para `ogr2ogr`
- `staging`
  - limpieza intermedia
  - renombre de campos
  - validacion de geometria
  - homologacion de tipos
- `catastro`
  - capas autoritativas catastrales
- `planeamiento`
  - zonificaciones, ordenanzas y capas normativas
- `infraestructura`
  - lineas electricas, reservorios y futuras redes
- `hidrologia`
  - drenaje y futuras capas hidricas
- `admin`
  - metadata, catalogo de capas, auditoria, import batches
- `public`
  - vistas o capas listas para publicar

## Roles

- `catserver_owner`
  - propietario tecnico de objetos
- `catserver_admin`
  - administracion funcional y tecnica
- `catserver_etl`
  - importacion a `raw` y `staging`
- `catserver_edit`
  - edicion controlada de capas consolidadas
- `catserver_read`
  - solo lectura
- `catserver_publish`
  - lectura para servicios publicados

## Capas nucleares previstas

- `catastro.ejido_municipal`
  - origen principal esperado: `PAC - Ejido Comodoro Rivadavia - Enero 2025.dwg`
- `catastro.barrio`
  - origen principal esperado: lote de DWG de barrios y `Limite de Barrios.kmz`
- `catastro.parcela`
  - origen principal esperado: `Parcelas.kmz`
- `catastro.circunscripcion_sector`
  - origen principal esperado: `Circunscripciones - Sectores.kmz`
- `planeamiento.zonificacion_ordenanza`
  - origen principal esperado: `Zonificacion por Ordenanza.kmz`
- `infraestructura.linea_electrica`
  - origen principal esperado: `Lineas electricas.kmz`
- `infraestructura.reservorio`
  - origen principal esperado: `Reservorios.kmz`
- `hidrologia.drenaje`
  - origen principal esperado: `Drenaje Comodoro Rivadavia.kmz`

## Punto critico: CRS

Todavia no conviene imponer un SRID final a las capas provenientes de DWG. Antes hay que resolver:

- si los DWG estan en coordenadas locales CAD
- si usan Gauss-Kruger / POSGAR / Campo Inchauspe
- si alguna capa ya esta en UTM
- si el ejido PAC debe quedar en el mismo CRS operativo que parcelas y barrios

Hasta validar eso:

- `raw` preserva lo mas fiel posible
- `staging` agrega metadata de CRS y controles
- las tablas finales quedan listas pero sin restriccion de SRID obligatoria

## Politica de datos

- `raw`: nunca corregir a mano
- `staging`: todas las correcciones deben quedar trazables
- `catastro`, `planeamiento`, `infraestructura`, `hidrologia`: solo datos normalizados y auditados
- `public`: no publicar tablas crudas; publicar vistas o tablas consolidadas

