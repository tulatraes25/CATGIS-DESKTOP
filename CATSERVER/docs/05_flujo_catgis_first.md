# Flujo CATGIS First

## Decision de trabajo

Si tu operador principal va a ser `CATGIS`, el flujo recomendado cambia a:

- `CATGIS` como cliente GIS principal
- PostgreSQL + PostGIS como motor del servidor `CATSERVER`
- convertidor CAD asistido para DWG cuando haga falta
- utilitarios SQL o ETL solo donde CATGIS hoy todavia no escribe directo a PostGIS

## Lo que CATGIS ya soporta hoy

Verificado en el codigo actual:

- selector de carga con `KML / KMZ` y `CAD DWG / DXF`
- lector local de `KML/KMZ`
- carga asistida de `DWG` via `DXF` gemelo o conversor
- navegador PostGIS para listar capas y agregarlas al proyecto

Archivos relevantes:

- [AddLayerDialog.java](/C:/CATGIS/catgis-desktop/src/ar/com/catgis/AddLayerDialog.java)
- [KmlLoader.java](/C:/CATGIS/catgis-desktop/src/ar/com/catgis/KmlLoader.java)
- [DwgImportSupport.java](/C:/CATGIS/catgis-desktop/src/ar/com/catgis/DwgImportSupport.java)
- [PostgisBrowserDialog.java](/C:/CATGIS/catgis-desktop/src/ar/com/catgis/PostgisBrowserDialog.java)
- [PostgisLoader.java](/C:/CATGIS/catgis-desktop/src/ar/com/catgis/PostgisLoader.java)

## Limitacion actual importante

La integracion PostGIS actual de CATGIS esta implementada en modo lectura.

Eso significa:

- CATGIS ya puede conectarse a `CATSERVER`
- CATGIS ya puede listar tablas espaciales
- CATGIS ya puede agregarlas al proyecto
- CATGIS hoy no aparece preparado para volcar una capa local a PostGIS directamente desde la interfaz actual

## Estrategia recomendada para tu caso

### Operacion diaria

Usar `CATGIS` para:

- abrir `KMZ`
- abrir `DWG/DXF`
- revisar atributos
- validar geometria visualmente
- trabajar capas del proyecto
- conectarse a `CATSERVER` y consumir capas PostGIS

### Bootstrap inicial del servidor

Usar:

- PostgreSQL + PostGIS para crear `CATSERVER`
- nuestros scripts SQL para bootstrap
- y, solo para la carga inicial masiva, una de estas dos opciones:

1. `ogr2ogr` / GDAL como ayuda de ingestion
2. desarrollar en CATGIS una funcion nueva `Exportar a PostGIS`

## Recomendacion honesta

Si queres empezar ya:

- hacemos `CATGIS` como cliente principal
- dejamos fuera a QGIS
- usamos helper ETL solo para el seed inicial

Si queres flujo 100% CATGIS:

- el siguiente desarrollo natural es agregar exportacion directa de capa vectorial a PostGIS

## Flujo propuesto sin QGIS

1. Instalar PostgreSQL.
2. Instalar PostGIS.
3. Tener CATGIS operativo.
4. Instalar convertidor CAD si vas a abrir DWG sin DXF gemelo.
5. Crear `"CATSERVER"` con nuestros SQL.
6. Cargar primero `KMZ` al servidor con helper ETL.
7. Conectarte desde CATGIS a PostGIS para consumir y revisar capas.
8. Usar CATGIS para contraste entre fuente local y capa servida.

## DWG en CATGIS

Segun el codigo actual:

- CATGIS no incorpora lectura nativa directa in-process de DWG
- pero si tiene flujo asistido con convertidor CAD
- y puede usar un `DXF` gemelo con el mismo nombre

Entonces para tus DWG municipales el flujo recomendado es:

1. conservar el DWG como fuente original
2. generar o detectar el DXF equivalente
3. cargar ese DXF en CATGIS
4. validar CRS y capas internas
5. normalizar contra `CATSERVER`

## KMZ en CATGIS

CATGIS ya puede leer `KMZ` porque su loader busca `doc.kml` dentro del zip.

Eso encaja muy bien con tus capas:

- `Parcelas.kmz`
- `Limite de Barrios.kmz`
- `Circunscripciones - Sectores.kmz`
- `Zonificacion por Ordenanza.kmz`
- `Drenaje Comodoro Rivadavia.kmz`

## Siguiente mejora recomendada en CATGIS

Si queres que todo el circuito sea interno a CATGIS, la proxima tarea que conviene implementar es:

- `Exportar capa a PostGIS`

Minimo esperado de esa mejora:

- elegir conexion PostGIS guardada
- elegir esquema destino
- crear tabla si no existe
- insertar features
- registrar CRS
- registrar nombre logico y metadata

## Conclusion operativa

Si, podemos trabajar con `CATGIS` y no con QGIS.

La unica salvedad tecnica actual es que el primer poblado de `CATSERVER` todavia conviene hacerlo con scripts o con una futura mejora de exportacion directa desde CATGIS.

