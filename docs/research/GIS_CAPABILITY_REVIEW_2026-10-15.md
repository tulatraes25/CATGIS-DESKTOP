# CATGIS: capacidad profesional, arquitectura y UX

Fecha de consulta: 2026-10-15. Direccion de producto: issue #109.
Base de la auditoria de capacidades: `ea715cdfd3ee528cf76bd2a7d1e1f367d23d435c`.
Base de esta propuesta: `48c46e957552d28613c367632fafe7b0a5cd7433` (incluye PR #111 de PDF).
Estado: investigacion y plan de implementacion; NO certificacion integral del producto.

## 1. Decision de producto

La facilidad de uso no es el criterio principal aislado. CATGIS debe combinar cuatro condiciones: capacidad profesional util, resultados cientificamente correctos y recuperables, arquitectura mantenible y verificable, y UX coherente para usuarios nuevos y expertos. Un modo simple puede mostrar menos parametros inicialmente, pero nunca reducir la precision ni ejecutar otro motor. Una herramienta avanzada no se considera terminada porque aparezca en un menu.

El objetivo es incorporar herramientas que falten y completar las parciales. Las prioridades se deciden por el trabajo profesional que permiten resolver, gravedad de los errores actuales, reutilizacion del motor, evidencia disponible y costo de mantenerlas en todas las plataformas. El numero bruto de comandos o estrellas de un repositorio no decide calidad.

## 2. Alcance y metodo del relevamiento

Se combinaron busquedas actuales en la web, repositorios oficiales y documentacion de sus responsables con lectura de codigo CATGIS en SHA exacto. Se realizo tambien una busqueda amplia de repositorios GitHub `desktop GIS`, con una primera pagina de 25 candidatos; se separaron proyectos canonicos, espejos, ejemplos, tutoriales y proyectos archivados. Esta es una muestra amplia y dirigida, NO un censo de todos los repositorios existentes.

Niveles de evidencia: **D** descubrimiento; **P** documentacion primaria/README consultado; **C** codigo seleccionado consultado; **R** ejecucion real verificada. Los competidores de esta revision no fueron instalados ni sometidos a un benchmark propio: ninguna fila siguiente equivale a R. Una busqueda sin resultados de una palabra no prueba que una funcion no exista. Las descripciones comerciales se tratan como declaraciones del proveedor, no como mediciones independientes.

## 3. Fuentes primarias y decisiones de aprovechamiento

| Referencia | Evidencia y material | Idea para CATGIS; limite de la comparacion |
|---|---|---|
| QGIS | P/C: [model designer](https://docs.qgis.org/3.44/en/docs/user_manual/processing/modeler.html), [repositorio](https://github.com/qgis/QGIS), `src/core/processing/qgsprocessingalgorithm.h` lineas 535-660, blob consultado `70ce1e5a06ac4053735f82e5d037c95311eaa4ae` | Definiciones de entradas/salidas, proveedor, validacion y preparacion por contexto, feedback/cancelacion, modelos reutilizables. Inspiracion contractual; no copiar su implementacion ni afirmar auditoria de todo QGIS. |
| ArcGIS Pro | P: [ModelBuilder](https://doc.esri.com/en/arcgis-pro/latest/help/analysis/geoprocessing/modelbuilder/what-is-modelbuilder-.html), [historial](https://doc.esri.com/en/arcgis-pro/latest/help/analysis/geoprocessing/basics/geoprocessing-history.html) | Historial persistente de parametros, entorno, entradas/salidas y errores que sirve para repetir y encadenar procesos. Benchmark funcional propietario, no fuente para reutilizar codigo. |
| GRASS | P: [OSGeo/grass](https://github.com/OSGeo/grass) | Motor analitico raster/vector, terreno, hidrologia y series temporales. Adoptar flujos y oraculos de comparacion antes de decidir una integracion opcional. |
| SAGA GIS | P: [saga-gis/saga-gis](https://github.com/saga-gis/saga-gis) | Referencia de familias de analisis de terreno y geociencia. El repositorio se presenta como espejo oficial de SourceForge; no contarlo como un segundo producto. |
| WhiteboxTools | P: [jblindsay/whitebox-tools](https://github.com/jblindsay/whitebox-tools) | Herramientas externas de terreno/hidrologia y sus parametros. CATGIS ya tiene trabajo de descubrimiento del ejecutable: faltan pruebas de resultados nativos, no otro detector duplicado. Separar licencia de fuente, binarios y extensiones. |
| OpenJUMP | P: [openjump-gis/openjump](https://github.com/openjump-gis/openjump) | Edicion vectorial y extensibilidad en una aplicacion Java. Comparar semantica de seleccion, acciones y operaciones reversibles; no importar codigo por compartir lenguaje. |
| uDig | P: [locationtech/udig-platform](https://github.com/locationtech/udig-platform) | Separacion de catalogo, herramientas y plataforma de escritorio. Referencia arquitectonica/historica: no mandato de migrar CATGIS a Eclipse RCP. |
| OrbisGIS | P: [orbisgis/orbisgis](https://github.com/orbisgis/orbisgis) | Referencia para explorar trabajo GIS orientado a datos y consultas; evaluacion arquitectonica mas profunda pendiente. No se certifico su estado de mantenimiento o rendimiento. |
| GeoLibre | P: [opengeos/GeoLibre](https://github.com/opengeos/GeoLibre), README consultado por conector GitHub | Espacio de trabajo local, paneles y vistas sincronizadas, herramientas accesibles desde un contexto comun. Su stack Tauri/React/MapLibre no justifica reescribir Swing; los conteos publicitados de herramientas no se validaron ejecutando cada una. |
| Mapshaper | P: [mbloch/mapshaper](https://github.com/mbloch/mapshaper) | Flujos de limpieza y simplificacion vectorial con comandos reproducibles. Comparar tolerancias y conservacion de geometria, no solo aspecto visual. |
| GDAL | P: [Parquet](https://gdal.org/en/stable/drivers/vector/parquet.html), [COG](https://gdal.org/en/stable/drivers/raster/cog.html), [GeoPackage](https://gdal.org/en/stable/drivers/vector/gpkg.html) | Completar adaptadores reales usando capacidades verificadas del motor existente. La disponibilidad del ejecutable no prueba que incluya un driver particular. |
| PDAL | P: [repositorio](https://github.com/PDAL/PDAL), [stages](https://pdal.org/en/latest/stages/stages.html) | Lectores/filtros/escritores para nubes de puntos. LAZ/COPC y generacion de superficies requieren driver, pipeline, CRS, precision y recursos comprobados; no todas las etapas son streaming. |
| DuckDB Spatial | P: [extension espacial](https://duckdb.org/docs/stable/core_extensions/spatial/overview), [R-tree](https://duckdb.org/docs/stable/core_extensions/spatial/r-tree_indexes) | Candidato para consultas analiticas locales y datos columnares. Evaluar frente a PostGIS/GeoTools ya existentes; no agregar otra base obligatoria sin un caso medido. La documentacion actual incluye R-tree; evitar decisiones basadas en README antiguos. |
| GeoServer | P: [geoserver/geoserver](https://github.com/geoserver/geoserver) | Contratos de publicacion e interoperabilidad OGC para CATSERVER/web, permisos y pruebas cliente-servidor. No convertir el escritorio en microservicios por imitar un servidor. |
| gvSIG Online | P: [gvSIGAssociation/gvsig-online](https://github.com/gvSIGAssociation/gvsig-online) | Organizacion de publicacion/catalogo/permisos. Esta referencia es gvSIG Online, NO una auditoria del codigo de gvSIG Desktop. |
| Global Mapper | P: [sitio del proveedor](https://try.bluemarblegeo.com/global-mapper/), [analisis](https://www.bluemarblegeo.com/global-mapper/) | Comparador comercial de flujos integrados de datos/terreno. Validar ediciones, costos y licencias antes de cualquier dependencia; no se midio su rendimiento aqui. |
| JTS y GeoParquet | P: [CoverageValidator](https://locationtech.github.io/jts/javadoc/org/locationtech/jts/coverage/CoverageValidator.html), [GeoParquet 1.1.0](https://geoparquet.org/releases/v1.1.0/) | Referencias semanticas, no aplicaciones competidoras. Diferenciar cobertura valida de completitud dentro de un AOI; distinguir CRS omitido/default de CRS explicitamente desconocido. |

Fuentes descubiertas para ampliacion, sin auditoria de codigo en esta ronda: SharpMap, MapWindow5, rgis, Mapton, MeteoInfoMap, HydroDesktop y UrsulaGIS. Tutorials, forks redundantes y deegree2-desktop archivado no se cuentan como nuevos productos modernos examinados. Antes de adoptar componentes se deben fijar version/commit, licencia y obligaciones, mantenimiento, seguridad, empaquetado y pruebas; no se transfirieron fuentes o recursos graficos de competidores.

## 4. Brechas verificadas en CATGIS

Las rutas de esta seccion son relativas a `catgis-desktop/src/ar/com/catgis/` en el SHA de auditoria. Los informes historicos no sustituyen el codigo.

| Area | Evidencia actual | Mejora exigida y seguimiento |
|---|---|---|
| Procesamiento por lotes | `BatchProcessor.java`, blob `1b9dd5e5ae399a9686c39afc9227fa650519b43c`: salidas derivadas solo de `input.getName()` dentro del bucle; `BatchProcessorDialog` ya usa SwingWorker | Plan completo seguro de entradas/salidas antes de ejecutar (#114); luego descriptores tipados, cancelacion, recibo de ejecucion y modelo de procesos. No anunciar que batch entero faltaba. |
| GeoParquet | `GeoParquetReader.java`, blob `7497dceb9a0aa69de8440492008d3b92f3cac4e2`: constantes Point/EPSG:4326/columnas/bbox mundial y filas aproximadas por bytes | Lectura autentica de metadata y geometria/atributos, con streaming y errores claros (#112). Detectar PAR1 no acredita que sea GeoParquet. |
| Calidad topologica | `TopologyValidator.java`, blob `14bd62e749e78418bd77fddc46b97afbd772e06e`: resta union de pares a un rectangulo exterior; excepciones pueden terminar en `valid=true` | Reglas de cobertura correctas, estado ERROR/NO_EVALUADO, y nueva auditoria de completitud dentro de un AOI explicito (#113). |
| Raster por lotes | `BatchProcessor.extractBand` crea TYPE_BYTE_GRAY; helpers escriben TIFF con ImageIO y no comprueban su retorno | Preservar datos numericos, indices de banda, NoData, CRS y transformacion; no usar una imagen de visualizacion como verdad cientifica. Requiere regresiones numericas independientes. |
| Calculo espacial existente | Entradas positivas para ZonalStatisticsEngine, calculadoras raster/campos, red/terreno y modulos de analisis | Inventariar y probar primero; ampliar lo que falta, no duplicar clases porque una busqueda por otro nombre no las encontro. |
| CATMAP | Exportaciones y atlas ya existen; PR #111 corrige posicion multilinea PDF | Cerrar #99 por flujos reales: paridad previa/exportacion, pagina, leyenda/escala, formatos fieles. No confundir arreglo multilinea con toda la cartografia certificada. |
| OGC e interoperabilidad | Cliente WCS existe; PR #107 conserva API y valida inputs, pero ejes WCS2 especificos siguen abiertos | DescribeCoverage, ejes/CRS/formato del servicio y pruebas reales de protocolo (#108), sin suponer x/y para todos los servidores. |
| Modelador y trazabilidad | No se identifico en las fuentes revisadas un flujo completo equivalente al modelador e historial de las referencias; hay templates batch parciales | Auditoria de existencia por flujos/clases antes de crear un modulo. Completar o agregar un DAG persistente sobre servicios existentes, no un segundo motor. |

Ejemplo causal de topologia: dos poligonos adyacentes formando una L tienen union de area 3 sin hueco interior; su rectangulo exterior tiene area 4. Restar el rectangulo crea una falsa zona de area 1 fuera del dominio. Esto es un contraejemplo geometrico de la formula observada, no una ejecucion certificada de CATGIS. La prueba JTS y el AOI explicito estan especificados en #113.

En GeoParquet 1.1.0, el bloque `geo` describe columnas de geometria y su referencia; CRS ausente implica OGC:CRS84, mientras null significa desconocido. Esos casos no autorizan inventar una geometria Point, filas o bbox. El driver GDAL Parquet requiere las bibliotecas correspondientes en su compilacion. Estos detalles se deben verificar al decidir el adaptador (#112).

## 5. Herramientas y flujos a incorporar

**Pista A - Centro de procesos y modelador reutilizable.** Un registro de operaciones tipadas reutiliza los servicios actuales y los registros de acciones ya presentes. Cada operacion declara entradas, salidas, CRS/unidades, dependencias, parametros, estimacion de recursos y politica de reemplazo. La misma definicion alimenta un formulario individual, un lote y un modelo grafico. Ejemplo de flujo objetivo: reparar geometria -> reproyectar -> zona de influencia -> interseccion -> estadisticas -> mapa/informe. Los parametros, datos y resultados quedan registrados para repetirlo con otro conjunto. Aceptacion: dependencias validas, ciclos rechazados, mismos resultados individual/lote/modelo, cancelacion y reinicio controlado, sin publicar resultados parciales como finales. #114 es solo su primera defensa, no el modelador terminado.

**Pista B - Centro de calidad geografica.** Capas de errores seleccionables en el mapa, reglas guardables, restricciones de atributos, duplicados, geometria invalida, solapamientos y cobertura dentro del area de estudio. Permitir revisar propuestas de reparacion y comparar antes/despues sin destruir el original. Aceptacion: falsos positivos/negativos conocidos resueltos con datos pequenos verificables, identificadores estables, unidades explicitadas y diferencia entre invalido y no evaluado. La nueva herramienta AOI de #113 agrega capacidad real mas alla de corregir el viejo validador.

**Pista C - Raster y terreno de trabajo profesional.** Alinear grillas de forma explicita; preservar float/entero, NoData y bandas; mostrar resolucion, unidades y metodo de remuestreo. Consolidar estadisticas zonales, perfiles de elevacion, pendientes, curvas y operaciones de volumen/corte-relleno; auditar cuales ya existen antes de ampliar. Para drenaje/cuencas, aprovechar proveedores nativos aprobados y controlar sumideros, celda de salida, bordes y CRS. Aceptacion: salidas numericas con tolerancia justificada y comparacion independiente; una captura bonita o exit code 0 no alcanza.

**Pista D - Catalogo de datos real y moderno.** Completar GeoParquet (#112); contrastar soporte existente de GeoPackage, COG y PMTiles con importacion/consulta/visualizacion verdaderas. Evaluar LAZ/COPC mediante PDAL solo cuando el flujo aporte valor y su dependencia sea verificable en cada OS. Catalogo con esquema, extension, CRS, bandas, campos, fecha de fuente y estado de dependencia. Aceptacion: abrir, inspeccionar, seleccionar, analizar y guardar/reabrir preservando geometria/atributos; no encabezados ficticios. SQL espacial local es una opcion evaluable, no otro requisito de instalacion automatico.

**Pista E - Entregables profesionales y evidencia.** Completar CATMAP y paquetes de proyecto portables con fuentes referenciadas, estilos y procesos realizados. Atlas y plantillas deben producir salidas que coincidan con la vista previa y distingan exportacion vectorial de raster. Incluir registro de CRS, parametros, versiones y advertencias en una ficha reproducible; no divulgar paths privados o credenciales al compartirla. Aceptacion: paginas, escalas/leyendas y contenido visible medidos; comparacion de renders y apertura independiente; errores de codificacion nunca destruyen un entregable previo.

**Pista F - UX experta y accesible.** Un espacio de trabajo coherente puede organizar proyecto/datos a la izquierda, mapa al centro, parametros contextuales a la derecha y trabajos/resultados abajo, con paneles ajustables. Debe evitar cadenas de ventanas modales, preservar foco/seleccion y permitir busqueda de herramientas por tarea. Los parametros avanzados siguen accesibles. Agregar comparacion y vistas sincronizadas solo despues de contrastar capacidades existentes. Aceptacion: operar con teclado, lectores de accesibilidad donde proceda, distintos escalados/HiDPI, nombres largos y errores recuperables; cero ambiguedad entre seleccion y todas las entidades. Evaluar eficiencia experta, no solo el primer mapa de un principiante.

## 6. Arquitectura objetivo y decisiones descartadas

Mantener una aplicacion de escritorio modular con limites comprobables, no una reescritura total ni un sistema distribuido artificial. La direccion de dependencias es interfaz -> coordinadores de casos de uso -> dominio/puertos GIS -> adaptadores GeoTools/JTS/GDAL/PostGIS/archivos/red. Las decisiones sobre un toolkit visual alternativo requieren un prototipo con evidencia; cambiar Swing por una tecnologia de moda no demuestra mejor producto.

Separar registro de acciones, definicion del algoritmo, estado del proyecto, ejecucion y render. La interfaz consume eventos/resultados inmutables y no aloja operaciones largas; los adaptadores no deben depender de widgets. Antes de extraer responsabilidades de MapPanel se necesitan pruebas de caracterizacion de render, seleccion, edicion y recursos. Una clase mas pequena no es por si sola una arquitectura correcta.

El ciclo de trabajo propuesto es: validar parametros y datos -> planificar salidas y recursos -> ejecutar con progreso/cancelacion -> validar resultados -> publicar de forma segura -> registrar recibo y actualizar la interfaz. Cada transicion tiene condiciones de error. Un resultado cancelado o sin validar no se etiqueta exitoso. La previsualizacion y ejecucion deben compartir semantica de CRS, unidades y seleccion. El procesamiento por bloques y los limites de memoria se prueban; no se infieren de que una API diga streaming.

Evitar un registro paralelo para cada menu, un motor distinto para el modelador, procesos shell con argumentos concatenados y dependencias obligatorias de varios motores solapados. Integraciones opcionales se describen con capacidad/driver/version/arquitectura y error accionable cuando faltan. Los plugins, si se amplian, necesitan contrato versionado, permisos e aislamiento, no carga libre de codigo no confiable.

## 7. Calidad, UX y criterio de cierre

Los siguientes son objetivos de aceptacion, NO resultados ya medidos:

- Cero bloqueos criticos/altos conocidos de datos, exactitud o arquitectura en el alcance liberado. No prometer matematicamente la inexistencia de todo error futuro.
- Fixtures genuinas pequenas, tablas de resultados esperados, pruebas de propiedades como invariancia al orden, comparacion diferencial con motores aprobados y tolerancias ligadas al CRS/resolucion. Fuzzing de inputs malformados y mutacion en contratos criticos cuando resulte aplicable.
- Cancelacion, fallos de disco/red, salida truncada, concurrencia, handles nativos y reemplazo seguro. La cobertura de lineas no sustituye haber probado esas conductas.
- Rendimiento medido con datasets y equipos documentados: latencia de interaccion/EDT, tiempo de carga/proceso, memoria maxima y disco temporal. Fijar presupuestos por tarea despues del baseline; no inventar FPS ni tiempos actuales.
- UX observada por tareas: importar, encontrar una herramienta, configurar, detectar un error, recuperar, comparar, guardar y exportar. Medir cumplimiento, pasos innecesarios y recuperacion tanto en novatos como en expertos; no equiparar tema visual nuevo con UX certificada.
- Windows, Linux, Mac Intel y Apple Silicon: separar compilacion, suite, paquete, driver/CLI, JNI y GUI. Cada capa tiene evidencia propia. CATSERVER necesita integracion aislada; el visor web necesita pruebas de navegador/contrato, no solamente lint.

A0.7 y la frontera sin pruebas prematuras en la PC permanecen vigentes. Investigacion, especificacion y fundamentos seguros avanzan ahora. Exponer nuevas capacidades requiere cerrar sus dependencias tecnicas y comprobar su flujo completo; el congelamiento no debe convertirse en excusa para no disenar las capacidades faltantes.

## 8. Incremento ejecutable de esta ronda

Se especificaron #112 GeoParquet, #113 calidad/AOI y #114 batch seguro, y se actualizo #109 con este criterio conjunto. La implementacion candidata de #114 incorpora un plan inmutable de salidas y doce pruebas de archivos reales, sin agregar dependencias ni modificar pruebas anteriores. El helper fue verificado suplementariamente con 18 comprobaciones en JVM21 compilado con --release 17; la certificacion Java17/OS exige las ejecuciones GitHub del SHA exacto.

La lectura de codigo ya encontro problemas que una revision solo estetica omitiria. Esta propuesta no da por realizadas las nuevas herramientas, los benchmarks ni las pruebas GUI. Cada proxima entrega debe identificar capacidad ganada, bug corregido, PR/SHA, evidencia observada, limites y siguiente condicion de aceptacion.
