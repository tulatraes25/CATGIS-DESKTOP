# CATGIS Desktop

## Manual de usuario actualizado 2026

**Versión de referencia:** `1.0.0 Beta final (rev. 14)`  
**Autor:** `Lic. Claudio Alejandro Tula`  
**Colaboradores de revisión:** `Lic. Daniel Warton` y `Geólogo Federico Sanchez`  
**Estado del documento:** `fuente editable en UTF-8`

---

## Nota editorial

Esta versión en Markdown funciona como **fuente editable** del manual.  
La versión visual profesional con iconos reales, portada, tablas y glosario gráfico se encuentra en:

- [CATGIS_Manual_de_Usuario_Actualizado_2026.html](/C:/CATGIS/catgis-desktop/packaging/manual/CATGIS_Manual_de_Usuario_Actualizado_2026.html)
- [CATGIS_Manual_de_Usuario_Actualizado_2026.pdf](/C:/CATGIS/catgis-desktop/packaging/manual/CATGIS_Manual_de_Usuario_Actualizado_2026.pdf)
- [CATGIS_Manual_Integrado_2026.html](/C:/CATGIS/catgis-desktop/src/help/CATGIS_Manual_Integrado_2026.html)

El inventario autogenerado de iconos reales exportados desde la interfaz está disponible en:

- [manual-icon-catalog.md](/C:/CATGIS/catgis-desktop/packaging/manual/generated/manual-icon-catalog.md)
- [manual-icon-catalog.csv](/C:/CATGIS/catgis-desktop/packaging/manual/generated/manual-icon-catalog.csv)

---

## 1. Presentación de CATGIS

CATGIS Desktop es un sistema de información geográfica de escritorio para Windows orientado a trabajo técnico cotidiano. Integra en una misma aplicación:

- gestión de proyectos GIS
- carga de datos vectoriales, raster y tabulares
- edición vectorial con apoyo CAD
- conexiones remotas
- topografía con DEM
- hidrología preliminar
- cartografía mediante CATMAP
- análisis territorial inicial

No es únicamente un visor de capas. Tampoco reemplaza todavía, por sí solo, todos los flujos avanzados de software hidrológico o de modelado especializado. Su fortaleza actual es ofrecer una base GIS sólida, operativa y cada vez más integrada.

## 2. Alcance técnico del software

CATGIS ya puede resolver trabajo real en:

- gestión de proyectos `.catgis`
- visualización y consulta espacial
- edición vectorial
- importación de CAD
- trabajo con CRS
- geoprocesamiento vectorial
- cartografía básica exportable
- análisis DEM
- drenaje, escorrentía y cuencas preliminares
- escenarios preliminares de inundación
- descarga de suelos online
- riesgo booleano preliminar

### Alcance preliminar

Los siguientes bloques deben leerse con honestidad técnica:

- `Inundación preliminar`: aproximación territorial, no hidráulica 1D/2D profesional.
- `Suelos online`: cartografía o modelado global de resolución media, no detalle parcelario.
- `Riesgo booleano preliminar`: combinación simple de reglas, no evaluación geotécnica completa.

## 3. Estructura general de la interfaz

La interfaz principal se organiza en cinco áreas:

1. `Barra de menús`
2. `Barras de herramientas`
3. `Gestor de proyecto`
4. `Vista de mapa`
5. `Barra de estado`

El criterio de orden del gestor es operativo: **arriba = frente** y **abajo = fondo**.

## 4. Menús principales

Los menús visibles concentran el acceso a funciones estructurales:

- `Archivo`: proyectos, apertura de datos y persistencia.
- `Edición`: geometrías, copiar, pegar, borrar y atributos.
- `Vista`: navegación y control del encuadre.
- `Herramientas`: mediciones, consultas y utilidades.
- `Cartografía`: apertura de CATMAP y salida cartográfica.
- `Módulos`: acceso al gestor modular.
- `Ventana` y `Proyecto`: soporte de interfaz y contexto operativo.
- `Ayuda`: panel de ayuda y contenidos integrados.

## 5. Barras de herramientas principales

Las barras superiores y flotantes concentran accesos rápidos para:

- proyecto y archivo
- navegación
- edición vectorial
- CAD
- cartografía
- conexiones online
- topografía e hidrología

La versión HTML/PDF del manual incluye un **glosario visual con iconos reales** agrupados por barra.

## 6. Gestor de proyecto y panel de capas

Desde el gestor de proyecto se puede:

- activar o desactivar capas
- cambiar su orden visual
- agrupar capas
- seleccionar la capa de trabajo
- identificar si se trata de vector, raster o mapa base
- revisar información resumida como geometría, cantidad de elementos y CRS

Es el punto de control principal antes de editar, consultar o analizar.

## 7. Navegación y visualización

Las funciones de navegación actuales incluyen:

- acercar y alejar
- desplazar mapa
- zoom a capa seleccionada
- zoom a todas las capas
- vista anterior y siguiente
- buscar por coordenadas
- identificar entidades

Estas herramientas no alteran los datos; modifican únicamente el modo de lectura y exploración.

## 8. Carga de datos

### Formatos vectoriales y espaciales

- SHP
- GeoPackage
- GeoJSON / JSON
- GPX
- KML / KMZ
- DXF
- DWG

### Raster e imagen

- GeoTIFF / TIFF
- IMG
- ASC
- JPG / JPEG
- PNG
- BMP
- GIF

### Tablas

- CSV
- XLSX
- XLS
- ODS
- DBF

También admite arrastrar y soltar capas y proyectos.

## 9. CRS y coordenadas

CATGIS puede:

- definir el CRS del proyecto
- asignar CRS a capas cuando corresponde
- trabajar con un CRS operativo coherente
- convertir coordenadas
- exportar capas vectoriales reproyectadas
- sostener mejor el round-trip espacial al guardar y reabrir proyectos

En flujos DEM e hidrológicos, la política actual más importante es esta: **el CRS operativo del proyecto y del DEM base manda sobre los derivados**.

## 10. Edición vectorial

La edición vectorial actual incluye:

- puntos, multipuntos, líneas y polígonos
- rectángulo y círculo
- mover entidades
- mover, insertar y borrar vértices
- dividir líneas y polígonos
- crear agujeros
- aumentar y disminuir superficie
- unir elementos
- explotar entidades
- copiar y pegar
- ver o editar atributos
- deshacer y rehacer
- guardado de sesión
- SNAP

## 11. Herramientas CAD

CATGIS incorpora un bloque CAD liviano pero útil para:

- continuar líneas
- generar rectángulos y círculos
- extender o acortar líneas
- crear paralelas y perpendiculares
- georreferenciar CAD
- trabajar con entidades importadas desde DXF y DWG

No es un CAD completo, pero sí un soporte práctico dentro del flujo GIS.

## 12. Geoprocesamiento

Operaciones disponibles:

- Buffer
- Dissolve
- Clip
- Intersección
- Merge
- Diferencia
- Spatial Join
- Unión geométrica

Las salidas suelen generarse como nuevas capas vectoriales dentro del proyecto.

## 13. Topología y validación

El sistema ya incluye validaciones para:

- geometrías inválidas
- duplicados
- extremos colgantes
- superposiciones
- huecos simples

Estas comprobaciones ayudan a mejorar calidad de datos antes de análisis y cartografía.

## 14. Cartografía / CATMAP

CATMAP es el compositor cartográfico integrado. Permite:

- abrir un diseño cartográfico
- insertar mapa, escala, norte y título
- preparar salida para impresión o exportación
- combinar capas del proyecto con mapas base online

Es un bloque de salida cartográfica simple, práctico y directamente integrado con el proyecto.

## 15. DEM, relieve y topografía

Funciones actuales:

- DEM online
- carga de DEM local
- recorte de DEM
- curvas de nivel
- hillshade
- pendiente
- aspecto
- perfil topográfico
- controles de visualización raster
- exclusión de cotas `<= 0` para curvas cuando corresponde

El recorte de DEM es una pieza clave para trabajo costero o de áreas parciales.

## 16. Hidrología, escorrentía y cuencas

CATGIS ya puede generar:

- escorrentías
- drenajes
- dirección de flujo
- acumulación de flujo
- cuencas
- subcuencas básicas
- outlets
- flechas de flujo
- cuenca por outlet
- corridas por lote en múltiples outlets

El criterio operativo actual es claro: los resultados derivados deben quedar alineados al DEM de trabajo y al CRS operativo del proyecto.

## 17. Inundación preliminar

El bloque de inundación preliminar permite:

- definir lluvia en milímetros
- correr escenarios simples
- comparar escenarios
- exportar resultados raster a GeoTIFF

Debe interpretarse como una lectura territorial preliminar, no como simulación hidráulica profesional completa.

## 18. Suelos online

CATGIS integra actualmente `SoilGrids` como fuente principal de suelos online.

Capas iniciales integradas:

- arcilla
- arena
- limo
- carbono orgánico

Se descargan por área y se incorporan al proyecto como raster utilizable en análisis posteriores.

## 19. Riesgo booleano preliminar

El bloque de riesgo booleano preliminar combina:

- un DEM base
- una regla de pendiente
- un raster de suelos
- una regla de suelo
- lógica `AND / OR`

Puede generar:

- máscara de pendiente
- máscara de suelo
- raster de riesgo preliminar
- vectorización positiva opcional
- resumen técnico del resultado

## 20. Exportación y persistencia

El sistema permite:

- guardar y reabrir proyectos `.catgis`
- exportar cartografía desde CATMAP
- reutilizar capas raster y vectoriales derivadas
- exportar GeoTIFF en ciertos flujos, como inundación preliminar

La persistencia ha mejorado especialmente en el bloque DEM/hidrológico, donde antes era más frecuente perder coherencia espacial.

## 21. Limitaciones y buenas prácticas

Buenas prácticas recomendadas:

- revisar el CRS antes de análisis DEM e hidrológicos
- recortar el DEM cuando el área útil es menor que el raster completo
- no exigir microdetalle a insumos globales
- separar resultados preliminares de modelado especializado
- guardar versiones del proyecto en hitos relevantes

## 22. Glosario visual de iconos

El glosario visual profesional completo con **iconos reales exportados desde la interfaz** está en:

- [CATGIS_Manual_de_Usuario_Actualizado_2026.html](/C:/CATGIS/catgis-desktop/packaging/manual/CATGIS_Manual_de_Usuario_Actualizado_2026.html)
- [CATGIS_Manual_de_Usuario_Actualizado_2026.pdf](/C:/CATGIS/catgis-desktop/packaging/manual/CATGIS_Manual_de_Usuario_Actualizado_2026.pdf)

Incluye agrupación por:

- proyecto y archivo
- navegación
- selección y edición vectorial
- CAD
- topografía
- cartografía
- conexiones online
- sesión / guardar / confirmar / cancelar
- contextos raster y gestor de proyecto

## 23. Flujos de trabajo recomendados

### 23.1 Abrir y organizar un proyecto

1. Crear o abrir proyecto.
2. Definir CRS del proyecto si corresponde.
3. Cargar capas y tablas.
4. Ordenar visualmente el gestor.
5. Guardar el estado inicial.

### 23.2 Editar y digitalizar

1. Elegir o crear una capa editable.
2. Activar SNAP cuando sea necesario.
3. Dibujar o modificar geometrías.
4. Revisar atributos.
5. Guardar cambios.

### 23.3 Trabajar con DEM e hidrología

1. Cargar o descargar DEM.
2. Recortar DEM si corresponde.
3. Generar relieve o hidrología.
4. Verificar CRS y coincidencia espacial.
5. Guardar proyecto.

### 23.4 Trabajar con suelos y riesgo preliminar

1. Descargar suelos online por área.
2. Verificar cruce real con el DEM.
3. Definir umbrales.
4. Ejecutar `AND` u `OR`.
5. Revisar raster y vectorización resultante.

---

## Resumen por módulo

| Módulo | Estado actual | Tipo de uso |
|---|---|---|
| Proyectos y navegación | Sólido | Operativo diario |
| Carga de datos | Sólido | Operativo diario |
| Edición vectorial | Sólido | Operativo diario |
| CAD integrado | Útil | Apoyo técnico |
| Geoprocesamiento | Sólido | Trabajo vectorial general |
| Cartografía / CATMAP | Útil | Salida cartográfica |
| DEM y topografía | Sólido | Análisis territorial |
| Hidrología preliminar | Sólido / en evolución | Cuencas, drenaje, relieve |
| Inundación preliminar | Preliminar | Lectura territorial |
| Suelos online | Útil | Insumo analítico |
| Riesgo booleano preliminar | Preliminar útil | Cruce inicial pendiente + suelo |

## Cierre

Esta fuente editable acompaña un manual visual mucho más profesional, con portada, iconos reales, glosario gráfico y versión integrada al HelpCenter. Para publicación, capacitación o entrega formal, las versiones recomendadas son la HTML y la PDF.
