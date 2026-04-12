# CATGIS Desktop - Matriz Manual de Release Beta Final Funcional

Uso previsto: validacion manual corta sobre la build empaquetada mas reciente antes de confirmar su salida como `beta final funcional` para distribucion controlada.

Reglas:
- Ejecutar sobre la build empaquetada mas reciente.
- Marcar cada caso como `OK`, `FALLA` o `N/A`.
- Registrar evidencia breve cuando falle: paso, mensaje y archivo usado.
- Si la maquina bloquea binarios sin firma, dejar evidencia y repetir la corrida en un equipo apto para distribucion controlada.

## Datos de la corrida

| Campo | Completar |
| --- | --- |
| Build |  |
| Fecha |  |
| Operador |  |
| Equipo |  |
| Instalador / app-image |  |

## Matriz

| ID | Area | Pasos minimos | Resultado esperado | Estado | Evidencia |
| --- | --- | --- | --- | --- | --- |
| REL-01 | Instalador | Ejecutar el `.exe`, revisar nombre, licencia, carpeta sugerida y accesos directos. | Branding CATGIS Desktop, licencia de beta final funcional y fin de instalacion sin error en un equipo que permita binarios sin firma. |  |  |
| REL-02 | Primer arranque | Abrir CATGIS desde acceso directo o carpeta instalada. | La app inicia sin mensajes heredados de pre-release ni errores visibles de carga. |  |  |
| REL-03 | Proyecto nuevo | Crear proyecto nuevo y revisar CRS inicial. | Proyecto creado y CRS visible/usable desde el selector. |  |  |
| REL-04 | Guardar / abrir proyecto | Guardar `.catgis`, cerrar y reabrir el proyecto. | Proyecto reabre con capas, vista y configuracion persistidas. |  |  |
| REL-05 | SHP | Cargar un shapefile vectorial y revisar atributos. | Capa visible, atributos legibles y CRS respetado o informado. |  |  |
| REL-06 | GeoJSON | Cargar un GeoJSON y revisar atributos/geometria. | Carga correcta sin perder campos ni geometria. |  |  |
| REL-07 | GPKG | Cargar una capa GeoPackage. | Apertura correcta y capa usable en mapa/tabla. |  |  |
| REL-08 | KML | Cargar KML y revisar geometria/atributos. | Carga correcta y capa visible. |  |  |
| REL-09 | KMZ | Cargar KMZ directo. | Se abre sin descomprimir manualmente y la capa queda usable. |  |  |
| REL-10 | Raster clave | Cargar GeoTIFF o raster soportado y revisar visualizacion. | Raster visible, sin error de carga y con georreferenciacion razonable. |  |  |
| REL-11 | DXF | Cargar DXF como referencia CAD. | Carga correcta, capa visible y atributos CAD basicos presentes. |  |  |
| REL-12 | DWG asistido | Cargar DWG con convertidor detectado. | Resolucion automatica o guiada, sin dejar al usuario bloqueado. |  |  |
| REL-13 | Grupos de capas | Crear grupo, mover capas dentro y alternar visibilidad. | Estructura persistente y visibilidad efectiva correcta. |  |  |
| REL-14 | Edicion basica | Crear o editar una entidad simple y guardar cambios. | Edicion estable, sin cortar flujo ni perder geometria. |  |  |
| REL-15 | Tabla / atributos | Abrir tabla y usar calculadora de campos simple. | Tabla estable, calculo aplicado y valores persistidos en sesion. |  |  |
| REL-16 | Exportacion vectorial | Exportar a SHP o GeoJSON y reabrir el resultado. | Geometria y atributos coherentes en el archivo exportado. |  |  |
| REL-17 | KML/KMZ con etiquetas | Exportar una capa a KML o KMZ con campo etiqueta. | El visor externo muestra geometria y nombre/etiqueta esperados. |  |  |
| REL-18 | Geoprocesos clave | Probar clip y union/interseccion simple con datos chicos. | Resultados coherentes, sin geometria rota ni atributos vacios. |  |  |
| REL-19 | CATMAP base | Abrir CATMAP, insertar mapa y exportar PDF o imagen si aplica. | Flujo base usable y exportacion sin error visible. |  |  |
| REL-20 | Cierre / reapertura | Cerrar la app y volver a abrir ultimo proyecto. | Reapertura estable sin corrupcion del proyecto ni errores de inicio. |  |  |

## Criterio de salida

- Beta final funcional:
  - Sin fallas bloqueantes en `REL-01` a `REL-12`.
  - Sin fallas repetibles en guardado, apertura y exportacion.
  - Sin textos rotos visibles en el flujo principal.
  - Con alcance de distribucion controlada claramente documentado.
- No liberar ni distribuir fuera de alcance controlado:
  - Si falla instalacion, primer arranque, apertura de proyecto, formatos clave, CAD asistido o exportaciones base.
  - Si el entorno objetivo exige firma digital obligatoria y la build no puede instalarse por esa politica.

## Notas

- `REL-12` puede marcarse `N/A` si la maquina no tiene convertidor DWG configurado y esa ausencia esta documentada.
- `REL-19` puede limitarse al flujo base de composicion; refinamientos esteticos no bloquean por si solos una beta final funcional.
- Esta matriz asume distribucion controlada. La firma digital del instalador queda pendiente para una etapa comercial posterior.
