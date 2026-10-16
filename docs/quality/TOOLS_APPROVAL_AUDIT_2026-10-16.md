# CATGIS - auditoria de aprobacion y redundancias de herramientas

Fecha: 2026-10-16. SPEC/Kiro, continuacion de issues #109, #114, #128 y #132.
Base exacta: `e8aba016621ae0265ce090f3a3e5d6f5d37f9167`.
Arbol base: `39c0b868ce2a6cb1487394a57d698479a070e40b`.
Estado: AUDITORIA DE INVENTARIO Y RUTAS SELECCIONADAS; NO APROBACION GLOBAL.
A0.7 sigue OPEN. No se ejecutaron acciones en el PC, OpenCode ni releases.

## 1. Respuesta de producto

No estan todas las herramientas auditadas en profundidad y aprobadas de extremo
a extremo. Existe inventario ejecutable, contratos automatizados e integraciones
reales, pero eso no sustituye la aprobacion por herramienta, sus resultados
cientificos, sus errores/cancelaciones ni el flujo grafico final.

Esta ronda reviso inventarios, requisitos/diseno, registro de modulos, rutas de
raster por lotes y georreferenciacion, obligaciones CI y el diff completo de PR133.
No ejecuto las 541 capacidades individualmente ni una auditoria linea por linea
de todos sus motores. Las issues antiguas y sus titulos pueden quedar atras de
los commits; un titulo OPEN no demuestra que su defecto original siga intacto.

`docs/quality/FEATURE_CERTIFICATION_MATRIX.md` declara 541 identidades semanticas:
293 CORE, 186 BETA, 15 EXPERIMENTAL y 47 EXTERNAL_DEPENDENCY. Son categorias de
inventario, no numeros de herramientas aprobadas. Las filas mantienen
RUNTIME_PENDING antes de A0.7. No presentar un porcentaje global de aprobacion
sin relacionar cada ID con alcance, SHA, datos, entorno y evidencia.

La misma fuente contabiliza 98 accesos suplementarios que son aliases y 13
grupos de normalizacion de nombres estaticos. Son dos clasificaciones distintas;
no sumarlas como 111 motores redundantes ni reducir 541 en esa cantidad.

## 2. Hallazgos confirmados y decisiones

| ID | Evidencia actual | Decision / alcance |
|---|---|---|
| SCI-01 | BatchProcessor.extractBand crea TYPE_BYTE_GRAY; 513 pasa a 1 y 0.5 pasa a 0 en caracterizacion real JDK. | Corregir precision antes de aprobar la ruta de indices por lotes para analisis cientifico. |
| SCI-02 | Math.min(bandIndex, pixel.length - 1) sustituye un indice de banda demasiado alto por la ultima banda. | Rechazar indices inexistentes, no calcular con otra banda silenciosamente. |
| SCI-03 | El parser de applyReclassify separa por guion y no admite el limite negativo -5--1=7. | Parser numerico tipado con signo; pruebas de limites, solapes y reglas invalidas. |
| SCI-04 | applySidecarMetadataIfPresent recrea una cobertura usando los pixeles originales y el envelope/CRS auxiliar aun despues de leer una cobertura. | Definir procedencia y precedencia de metadata; no confundir reasignar CRS/envelope con reproyectar. Sigue #128. |
| OUT-01 | Los wrappers raster por lotes escriben mediante ImageIO, ignoran su booleano y retornan normalmente si el resultado es null. | Resultado explicito, escritor verificable, metadata numerica y publicacion segura. Es hallazgo de fuente, no todos sus fallos reproducidos. |
| DUP-01 | wfs-open y online-basemap-wfs invocan el mismo AddWfsAction::openDialog. Ya comparten ID semantico con el acceso del menu. | Mantener capacidad y accesos utiles; unificar nombre/contrato de comando, no borrar el servicio WFS. |
| DUP-02 | Ocho accesos de geoprocesamiento preseleccionan operaciones del mismo GeoprocessingAssistantDialog. | Reutilizacion correcta; no son ocho implementaciones duplicadas demostradas. |
| DUP-03 | Alias con acentos/puntos suspensivos en Medir area, Curvas de nivel, Perfil topografico, etc. | Canonizar etiquetas y ayuda sin romper atajos ni proyectos. Fase UX tras sus prerrequisitos. |
| IMP-01 | PR122 esta verificado como experimento de lectura de filas GeoParquet, no adaptador de capa integrado. | Completar integracion y limites antes de promocionar soporte; no adoptar librerias solo por un badge verde. |
| TEST-01 | ci.yml ejecuta npm ci y npm run lint del visor; no ejecuta tests funcionales web. | Agregar pruebas reales de API/visor, fallos, permisos y flujo de capas; lint no certifica uso. |

### Fuentes exactas inspeccionadas

- Inventario: `docs/quality/FEATURE_CERTIFICATION_MATRIX.md`, blob
  `a0ecf8c255b667b541ea35ffad1e92f55768609a`.
- Aliases: `docs/quality/STATIC_COMMAND_ALIAS_MAP.tsv`, blob
  `6b68519b402641a7315dd0a85e89035fe04b3d68`.
- Registro: `catgis-desktop/src/ar/com/catgis/ModuleRegistry.java`, blob
  `0e6bf210a389e6352cbf6544c15dbf065865a573`.
- Lotes: `catgis-desktop/src/ar/com/catgis/BatchProcessor.java`, blob
  `4c231ffb567af7f7b3fdb4e8800acce1094e9c05`.
- Metadata: `catgis-desktop/src/ar/com/catgis/data/raster/RasterCoverageSupport.java`,
  blob `1449e37f5143cffea39f45296ace315606d1338c`.
- CI web: `.github/workflows/ci.yml`, blob
  `2c836f6beb2180c0193b54f142f61a06df1ac450`.

Estas fuentes se leyeron en main4454 o en main e8aba; el merge PR133 cambia
solamente scripting, sus pruebas y dos SPEC, no las fuentes anteriores.
El detalle del experimento esta registrado en
https://github.com/tulatraes25/CATGIS-DESKTOP/issues/114#issuecomment-6215886737 .

## 3. Reproduccion aislada de precision y seleccion

El archivo acompanante `tools/qa/probes/BatchRasterPrecisionProbe.java` contiene
el cuerpo exacto del helper de bandas y una caracterizacion del prefijo del
parser. No depende de GeoTools ni de clases simuladas de CATGIS.

Ejecucion observada: Linux, javac --release 17, JVM real21.0.12.1,
java.awt.headless=true. No afirmar que fue ejecutado sobre un runtime17.

```
USHORT: 513.0 -> 1.0
FLOAT: 0.5 -> 0.0
Indice99 en imagen de3bandas: ultima banda, valor33.0
Intervalo -5--1=7: NumberFormatException
```

Son cuatro casos en tres clases de defecto. Dos valores prueban la misma
perdida de precision; no contar cada pixel como un defecto independiente.
El caso del parser ejecuta su prefijo real, no una reclasificacion GIS completa.
No es prueba end-to-end de GeoTIFF ni nueva certificacion Windows/Mac.
El probe caracteriza defectos conocidos; NO se agrega como test de aprobacion
que convierta su persistencia en un requisito del producto. Al corregirlos,
los contratos de produccion deben exigir valores numericos correctos y el
probe historico se conserva solo como evidencia de esta base.

## 4. Que mantener, completar y consolidar

Mantener los motores y capacidades existentes que tienen un cometido propio:
CAD, topologia, calculadora/indices raster, hidrologia, consultas, PostGIS,
CATMAP, importadores y procesamiento por lotes. No crear una segunda calculadora,
un segundo batch o un nuevo lector GeoParquet paralelo para aumentar el conteo.

La diferencia entre un acceso rapido y una duplicacion de implementacion debe
probarse siguiendo la llamada y el resultado, no por parecido de nombres.
Asistente, toolbox y accesos de operaciones pueden compartir servicios. El
compositor simple y CATMAP requieren comparacion de funciones/persistencia/
exportacion antes de cualquier migracion o retiro; esta ronda no demuestra
que uno pueda eliminarse sin perdida.

Los tres controles CATMAP de sincronizacion de capas/simbologia/etiquetas ya
figuran como DISABLED_UNSUPPORTED en el inventario: no presentarlos como
funciones aprobadas ni afirmar que se deshabilitaron recien en esta ronda.
No se elimina ninguna herramienta en este cambio de evidencia.

## 5. Proxima implementacion con criterio de aceptacion

Prioridad inmediata #114: una frontera numerica comun para raster individual,
lotes y futuro modelador. Preservar tipos y valores nativos, validar bandas,
tratar NoData explicitamente, separar calculo de visualizacion y verificar CRS,
transformacion de grilla, resolucion, error y archivo final antes de publicar.
Retener el preflight completo de BatchOutputPlan ya implementado; no reintroducir
colisiones/sobrescrituras ni llamarlo una capacidad ausente.

Prioridad #128: establecer metadata autoritativa versus auxiliar, detectar
contradicciones y comprobar que cambiar una etiqueta no simule una reproyeccion.
Usar fixtures pequenos con coordenadas y valores esperados independientes.

Consolidar los contratos/ejecucion de operadores existentes: precondiciones,
parametros tipados, unidades, progreso/cancelacion, resultado y procedencia.
Luego agregar historial reproducible, encadenamiento/modelador y una presentacion
unificada de resultados QA sobre esos mismos operadores, no otros motores.
Son direcciones propuestas, NO nuevas funcionalidades implementadas aqui.

GeoParquet debe avanzar desde metadata/experimento a capa real solo tras resolver
lectura acotada, CRS, schema/atributos, memoria, fallos y distribucion de dependencias.
La integracion grafica de topologia, CATMAP, servicios online, dependencias nativas
y el visor conserva sus criterios separados; no declarar aprobados todos estos
frentes por pruebas aisladas. UX/redisenos visibles y runtime local siguen tras
los prerrequisitos del programa, no se elimina A0.7 para facilitar el cierre.

## 6. Continuacion real en esta ronda

PR133 se fusiono normalmente, con expected head
`5d9e0ff0180953205cc18a950af6406a84748eea`, en main
`e8aba016621ae0265ce090f3a3e5d6f5d37f9167`.
El arbol fusionado coincide exactamente con el candidato39c0b868.

Gates del candidato: CI38897054224 SUCCESS4/4;
Certification38897054204 SUCCESS15/15; metadata38897054180 SUCCESS;
RuntimeIdentity38897054156 SUCCESS8/8. Jobs individuales inspeccionados y diff
completo revisado. Log Windows21/116960162863 leido completo: compilador y test21,
1200casos,0fallos,0errores,15omisiones externas anteriores,32verificadores Python
+15contratos de atestacion, empaquetado/PE y fuente sin cambios. No es GUI.

Atestacion autenticada38897054280 attempt2/job116977248417 confirma base-main4454
con CI38895610121, Certification38895610254, metadata38895610106 y
RuntimeIdentity38895610090, todos push/main SUCCESS. Solo se repitio ese requisito
cuando termino Certification, no pruebas de aplicacion fallidas. Esa evidencia
permite cerrar #130 de politica Java, no #132 completo ni todas las herramientas.

Main e8aba requiere sus NUEVAS ejecuciones push y atestacion: al escribir este
snapshot no se declaran terminadas. Un PR de documentacion que atestigua su base
no certifica sus propios cambios ni convierte los defects raster en corregidos.
Las futuras observaciones se agregan al PR/issues con SHA y runIDs exactos.

Se preservan el primer fallo Windows del candidato de scripts y el incidente
anterior de salida vacia/error de sintaxis bajo #132; ningun replay exitoso prueba
su causa. Solo se integraron las correcciones acotadas de EOF, full-duplex y
limpieza propia. Procesos descendientes, cuotas, descubrimiento Python y GUI
siguen separados. Ninguna release, dataset, credencial ni archivo del usuario
se modifico por esta auditoria.
