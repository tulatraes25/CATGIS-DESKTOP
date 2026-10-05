# Prompt Para Dejar PostGIS Muy Bien Integrado En CATGIS

Quiero que actues como arquitecto GIS + ingeniero Java Swing + especialista GeoTools/PostGIS y trabajes sobre el proyecto `CATGIS` para dejar la integracion con PostGIS realmente solida, usable y coherente con el flujo actual de la aplicacion.

## Objetivo principal

Necesito mejorar `CATGIS` para que la experiencia con PostGIS quede muy bien resuelta dentro del producto, no como un accesorio a medias.

Hoy la aplicacion ya tiene base para:

- conectarse a PostGIS
- listar capas espaciales
- cargar capas PostGIS al proyecto
- abrir `KML/KMZ`
- abrir `DWG/DXF`

Pero la integracion actual de PostGIS parece estar enfocada en modo lectura, y quiero llevarla a un estado mucho mas completo y profesional.

## Contexto real del codigo

Archivos ya identificados en el proyecto:

- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\PostgisBrowserDialog.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\PostgisLoader.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\PostgisDataSourceAction.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\PostgisLayer.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\PostgisConnectionInfo.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\PostgisConnectionStore.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\PostgisErrorSupport.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\AddLayerDialog.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\KmlLoader.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\DwgImportSupport.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\ExportVectorLayerAction.java`

## Hallazgo importante ya verificado

La integracion PostGIS actual carga capas en modo lectura. Quiero mejorar eso de forma concreta.

## Objetivo funcional deseado

Quiero que `CATGIS` permita un flujo PostGIS realmente util para trabajo municipal y catastral:

1. Crear y guardar conexiones PostGIS de forma robusta.
2. Navegar schemas y tablas espaciales de manera clara.
3. Cargar capas PostGIS al proyecto.
4. Exportar capas locales hacia PostGIS.
5. Importar capas PostGIS y poder guardarlas localmente si hace falta.
6. Tener mensajes de error claros y operativos.
7. Mantener una experiencia simple para el usuario final.

## Requisito especial: flujo centrado en Shapefile

Quiero que el flujo de base de datos quede muy orientado a `Shapefile`.

Importante:

- Entiendo que PostGIS no es lo mismo que un `.shp`.
- No quiero una solucion conceptualmente confusa.
- Lo que quiero es que el flujo de trabajo sea muy amigable para usuarios que trabajan en `shape`.

Por eso la propuesta debe resolver esto asi:

- `Shapefile` como formato canonico de intercambio local
- PostGIS como almacenamiento servidor
- importacion facil de `SHP -> PostGIS`
- exportacion facil de `PostGIS -> SHP`
- si hace falta, tambien `SHP -> proyecto CATGIS -> PostGIS`

No propongas reemplazar PostGIS por shapefile, porque eso no es una base de datos real. Quiero una integracion bien pensada entre ambos mundos.

## Lo que necesito que diseñes e implementes

### A. Diagnostico del estado actual

Revisa el codigo existente y explica:

- que ya funciona
- que esta a medio hacer
- que esta ausente
- que partes hoy fuerzan modo lectura

### B. Mejoras concretas para PostGIS

Quiero como minimo:

1. navegador PostGIS mejorado
2. selector de schema mas claro
3. listado de tablas mas util
4. detalle de tipo de geometria y CRS
5. mejor test de conexion
6. mensajes de error mas precisos

### C. Escritura a PostGIS

Implementar o dejar diseñada la capacidad de:

- exportar una capa vectorial del proyecto a PostGIS
- elegir conexion
- elegir schema destino
- elegir nombre de tabla
- decidir si:
  - crear nueva tabla
  - reemplazar tabla existente
  - anexar registros
- registrar CRS correctamente
- manejar tipos de geometria
- manejar errores de clave, schema o permisos

### D. Flujo Shape <-> PostGIS

Quiero que quede muy bien resuelto el flujo:

- abrir shapefile en CATGIS
- trabajar la capa
- enviarla a PostGIS
- volver a traerla desde PostGIS
- exportarla otra vez como shapefile si el usuario lo necesita

### E. Criterios de UX

La experiencia debe sentirse:

- clara
- municipal
- profesional
- sin tecnicismos innecesarios para el usuario comun

### F. Validaciones que quiero

Antes de escribir en PostGIS, validar:

- que la capa tenga geometria
- que el nombre de tabla sea valido
- que el CRS este definido o al menos advertido
- que el tipo geometrico sea consistente
- que la tabla destino no cause conflicto silencioso

### G. Requisitos tecnicos

Si implementas escritura a PostGIS, usa el stack ya presente en el proyecto siempre que sea razonable:

- GeoTools
- JDBC PostgreSQL
- GT JDBC PostGIS

Evita introducir dependencias nuevas si no son necesarias.

## Entregables esperados

Quiero que el trabajo quede organizado asi:

1. analisis del estado actual
2. propuesta de mejora
3. implementacion en codigo
4. pruebas
5. riesgos pendientes

## Nivel de detalle deseado

No quiero una respuesta generica.

Quiero que:

- uses los archivos reales del proyecto
- señales donde estan hoy las limitaciones
- propongas la arquitectura concreta
- implementes lo que falte o dejes bien preparado el esqueleto
- agregues pruebas si corresponde

## Criterio de calidad final

Voy a considerar que quedo "perfecto" si se logra esto:

- CATGIS puede conectarse de forma confiable a PostGIS
- puede leer capas bien
- puede escribir capas bien
- el flujo con shapefiles queda natural
- el usuario no necesita salir de CATGIS para el ciclo normal `shape <-> PostGIS`

## Prioridad de implementacion

Si hay que elegir orden, hacelo asi:

1. robustez de conexion
2. navegador PostGIS
3. exportar capa a PostGIS
4. flujo `shape <-> PostGIS`
5. refinamiento UX

## Importante

Si detectas que hoy CATGIS solo soporta PostGIS en modo lectura, decilo claramente y resuelve esa limitacion como prioridad principal.

