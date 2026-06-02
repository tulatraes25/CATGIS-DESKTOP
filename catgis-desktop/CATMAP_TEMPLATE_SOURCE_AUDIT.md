# CATMAP Template Source Audit

Fecha: 2026-06-01  
Workspace auditado: `C:\CATGIS\catgis-desktop`

## Objetivo

Dejar una auditoría operativa para OpenCode sobre **fuentes externas de plantillas cartográficas** que sirvan para ampliar CATMAP con layouts profesionales, sin confundir:

- fuentes de **plantillas reales** importables o reinterpretables;
- fuentes de **referencia visual** útiles para copiar estructura;
- fuentes que **no sirven** como base de layout aunque estén dentro del ecosistema GIS.

Este documento está pensado para que OpenCode lo use como guía de trabajo y priorización.

---

## Resumen ejecutivo

### Conclusión principal

CATMAP **sí puede nutrirse de fuentes externas**, pero no todas sirven del mismo modo.

La prioridad correcta es:

1. **Plantillas QGIS `.qpt`** y layouts públicos equivalentes.
2. **Galerías visuales** de mapas terminados para copiar estructura editorial.
3. **PDFs reales de producción** del usuario como estándar visual local.
4. **Plantillas CATMAP nativas** construidas a partir de esas referencias.

### Regla crítica

No tomar como fuente principal:

- [QGIS Hub Models](https://hub.qgis.org/models/)

Porque allí hay **modelos de procesamiento**, no composiciones cartográficas de impresión/layout.

### Estado actual de CATGIS Desktop auditado

En el código actual existen piezas útiles para esta estrategia:

- `LayoutTemplateManager.java`: catálogo y aplicación de plantillas nativas CATMAP.
- `QgisQptImporter.java`: importación parcial de layouts QGIS.
- `LayoutModel.java`: modelo base de elementos.
- `LayoutMap.java`, `LayoutLegend.java`, `LayoutScaleBar.java`, `LayoutNorthArrow.java`, `LayoutLabel.java`, `LayoutImage.java`, `LayoutRectangle.java`, `LayoutLine.java`, `LayoutTable.java`.
- Soporte de tablas ODS ya presente en:
  - `OdsTableReader.java`
  - `TableDataSupport.java`

### Lo importante

CATMAP ya tiene base para:

- copiar estilos de layout;
- reinterpretar plantillas QGIS parcialmente;
- construir plantillas nativas robustas;
- combinar mapa, leyenda, escala, norte, textos, tablas e imágenes.

Lo que **no** conviene prometer:

- importar MXD de ArcMap como si fuera una plantilla abierta;
- convertir cualquier layout externo automáticamente y sin pérdida;
- descargar layouts de cualquier sitio sin revisar licencia, estructura y formato.

---

## Auditoría interna previa de CATMAP

### Plantillas nativas actuales detectadas

En `LayoutTemplateManager.java` hoy existen estas familias:

- `A4_TECNICO`
- `A4_TECNICO_INFERIOR`
- `A4_TOPOGRAFIA`
- `A4_VERTICAL`
- `A4_AMBIENTAL`
- `A4_HIDROLOGIA`
- `A4_MUESTREO`
- `A4_CATASTRAL`
- `A4_PARCELARIO`
- `A4_URBANO`
- `A4_SATELITAL`
- `A4_REFERENCIA`
- `A4_ACCESIBILIDAD`
- `A4_EMPLAZAMIENTO`
- `A4_INFRAESTRUCTURA`
- `A4_PERFIL`
- `A3_TECNICO`
- `A3_AMBIENTAL`
- `A3_CATASTRAL`
- `A3_SATELITAL`
- `A3_PARCELARIO`
- `A3_HIDROLOGIA`
- `A3_TOPOGRAFIA`
- `A3_PRESENTACION`

### Importador QGIS detectado

`QgisQptImporter.java` hoy muestra soporte parcial para:

- labels
- maps
- legends
- scale bars
- north arrows
- rectangles

Eso alcanza para una estrategia realista:

- **importación parcial si hay `.qpt` buenos**
- **reconstrucción nativa** cuando el importador no alcance

### Limitación real

No hay evidencia de un sistema robusto para:

- importar ArcMap `.mxd`
- importar ArcGIS Pro `.pagx`
- convertir layouts propietarios de ESRI de forma segura

Por eso la estrategia correcta es:

- tomar QGIS `.qpt` y layouts públicos como base abierta;
- usar PDFs reales como referencia editorial;
- dejar lo de ESRI como benchmarking visual, no como pipeline de importación.

---

## Fuentes externas auditadas

## 1. Fuentes primarias — plantillas/layouts reales

### 1.1. QGIS Print Layout — documentación oficial

URL: [QGIS Print Layout Overview](https://docs.qgis.org/latest/en/docs/user_manual/print_layout/overview_layout.html)

#### Valor

Fuente oficial para entender:

- estructura del layout;
- items del layout;
- templates;
- exportación;
- comportamiento esperado del compositor.

#### Qué sirve para CATMAP

- taxonomía de elementos;
- reglas de diseño;
- comportamiento de map frames;
- leyendas, escalas y norte;
- conceptos de template `.qpt`.

#### Qué no da por sí sola

- una gran biblioteca descargable de plantillas terminadas.

#### Prioridad

**Crítica.** Debe usarse como referencia funcional base.

---

### 1.2. Layout Hub

URL: [layout-hub.github.io](https://layout-hub.github.io/)

#### Valor

Fuente comunitaria orientada específicamente a layouts/plantillas.

#### Qué puede aportar

- ejemplos de composición;
- posibles plantillas listas o semilistas;
- estructuras reutilizables;
- ideas de categorías de mapas.

#### Riesgo / límite

Hay que verificar por caso:

- si ofrece archivos descargables reales;
- si el formato es `.qpt`, imagen, mockup o solo galería;
- licencia de reutilización;
- nivel de mantenimiento.

#### Uso recomendado

- usarla como **fuente de muestra prioritaria**;
- descargar únicamente plantillas claras y reutilizables;
- si no hay archivo utilizable, tomarla como **referencia visual**.

#### Prioridad

**Muy alta.** Es una de las mejores candidatas de muestra para OpenCode.

---

### 1.3. Repositorios públicos con `.qpt`

Búsqueda recomendada:

- GitHub code search por `*.qpt`
- búsquedas tipo:
  - `QGIS print layout template .qpt`
  - `site:github.com .qpt qgis layout`
  - `QGIS A4 template qpt`

#### Valor

Los `.qpt` son el formato más cercano a una fuente reutilizable para CATMAP.

#### Qué sirve

- plantillas A4/A3;
- layouts ambientales;
- layouts institucionales;
- layouts con cartucho, norte, leyenda, escala;
- estructuras de páginas repetibles.

#### Riesgos

- calidad muy variable;
- muchas plantillas viejas;
- estilos acoplados a datasets o paths locales;
- elementos no soportados completamente por el importador actual.

#### Reglas para OpenCode

- preferir `.qpt` simples y limpios;
- evitar plantillas sobrecargadas;
- tomar primero layouts con:
  - 1 map frame
  - 1 leyenda
  - 1 escala
  - 1 norte
  - labels y rectángulos simples
- registrar fuente y licencia en comentarios o documentación.

#### Prioridad

**Muy alta.** Es la vía más práctica para crecer rápido y con base abierta.

---

## 2. Fuentes secundarias — inspiración visual profesional

### 2.1. QGIS Hub Map Gallery

URL: [QGIS Hub Map Gallery](https://hub.qgis.org/map-gallery/)

#### Valor

Excelente para ver mapas terminados y detectar:

- jerarquía visual;
- composición;
- ubicación de leyendas;
- uso de espacio;
- cartuchos;
- escalas;
- estética editorial.

#### Qué no da

- no es necesariamente una fuente de plantilla importable;
- muchas veces es resultado final, no archivo fuente.

#### Uso recomendado

- tomar como **benchmark visual**;
- clasificar ejemplos por familia:
  - ambiental
  - técnico
  - satelital
  - catastral
  - presentación

#### Prioridad

**Alta.**

---

### 2.2. QGIS Hub Projects

URL: [QGIS Hub Projects](https://hub.qgis.org/projects/)

#### Valor

Puede contener proyectos completos con layouts reutilizables o replicables.

#### Qué sirve

- estructura real de proyectos;
- configuración de capas;
- estilos;
- layouts asociados.

#### Riesgos

- datasets incompletos;
- paths rotos;
- dependencias externas;
- licencias variables.

#### Uso recomendado

- buscar proyectos pequeños y limpios;
- priorizar los que incluyan layout o composición clara;
- usar más como referencia estructural que como importación directa.

#### Prioridad

**Media-alta.**

---

### 2.3. PDFs reales del usuario

Fuente local:

- PDFs técnicos y ambientales aportados por el usuario

#### Valor

Son la **referencia más importante** para CATMAP si el objetivo es competir en la práctica del usuario.

#### Qué aportan

- estándar visual real de producción;
- proporciones editoriales;
- ubicación de cartucho;
- escalas;
- estilos de referencia;
- equilibrio de blancos, márgenes y bloques.

#### Uso recomendado

- tomarlos como “gold standard” local;
- crear familias de plantillas CATMAP a partir de ellos;
- usar QGIS y otras fuentes para completar variaciones, no para reemplazar el estándar local.

#### Prioridad

**Crítica.**

---

## 3. Fuentes que NO deben usarse como base de layout

### 3.1. QGIS Hub Models

URL: [QGIS Hub Models](https://hub.qgis.org/models/)

#### Diagnóstico

No es una biblioteca de layouts.

#### Contenido real

- modelos de procesamiento;
- cadenas de algoritmos;
- automatización geoprocesal.

#### Regla

No usar como fuente de plantillas CATMAP.

#### Prioridad

**Descartar para layouts.**

---

### 3.2. ArcMap `.mxd`

#### Diagnóstico

No es fuente viable para pipeline directo de CATMAP.

#### Problemas

- formato propietario;
- dependencia histórica de ArcObjects;
- importación frágil;
- baja mantenibilidad.

#### Regla

Usar solo como referencia visual externa, nunca como fuente primaria de importación.

---

### 3.3. ArcGIS `.style`

#### Diagnóstico

Sirve para auditoría de simbología, no para layouts.

#### Regla

No mezclar biblioteca de estilos con catálogo de plantillas.

---

## Matriz comparativa de fuentes

| Fuente | Tipo | Reutilización directa | Valor para CATMAP | Riesgo | Prioridad |
|---|---|---:|---|---|---:|
| QGIS Print Layout Docs | Documentación oficial | Media | Base conceptual y funcional | Bajo | Crítica |
| Layout Hub | Comunidad/layouts | Media | Plantillas y ejemplos concretos | Medio | Muy alta |
| Repos GitHub con `.qpt` | Plantillas reales | Alta | Importación parcial o reconstrucción nativa | Medio | Muy alta |
| QGIS Hub Map Gallery | Galería visual | Baja | Inspiración editorial | Bajo | Alta |
| QGIS Hub Projects | Proyectos completos | Media | Casos reales y layouts asociados | Medio/alto | Media-alta |
| PDFs reales del usuario | Referencia visual local | Baja/importable | Estándar estético local | Bajo | Crítica |
| QGIS Hub Models | Modelos de procesamiento | Nula | No aplica a layouts | Bajo | Descartar |
| MXD de ArcMap | Propietario | Muy baja | Solo benchmark externo | Alto | Descartar como importación |

---

## Qué debe hacer OpenCode con estas fuentes

## Fase A — Recolección y clasificación

1. Reunir fuentes en tres grupos:
   - `importable`
   - `reference_visual`
   - `discard`

2. Para cada fuente externa registrar:
   - URL
   - licencia visible si existe
   - formato
   - tema
   - si tiene `.qpt`
   - si CATMAP podría reconstruirla nativamente

3. Priorizar plantillas por caso de uso:
   - ambiental
   - accesibilidad
   - emplazamiento
   - catastral
   - topografía
   - satelital
   - muestreo
   - hidrología
   - infraestructura
   - perfil/tabla
   - presentación institucional

---

## Fase B — Importación o reconstrucción

### Si la fuente tiene `.qpt`

- intentar `QgisQptImporter` primero;
- si entra parcialmente, convertir el resultado en plantilla CATMAP nativa;
- si el `.qpt` tiene demasiados elementos no soportados, usarlo como guía y reconstruir manualmente.

### Si la fuente es solo visual

- no forzar importación;
- reconstruir layout nativo con:
  - `LayoutMap`
  - `LayoutLegend`
  - `LayoutScaleBar`
  - `LayoutNorthArrow`
  - `LayoutLabel`
  - `LayoutRectangle`
  - `LayoutLine`
  - `LayoutImage`
  - `LayoutTable`

---

## Fase C — Curado de plantillas

OpenCode no debe agregar plantillas por cantidad solamente.

Debe dejar una biblioteca amplia, pero coherente.

### Familias mínimas sugeridas

- A4 horizontal técnico
- A4 horizontal ambiental
- A4 horizontal accesibilidad
- A4 horizontal emplazamiento
- A4 horizontal catastral
- A4 horizontal satelital
- A4 horizontal hidrología
- A4 horizontal topografía
- A4 horizontal muestreo
- A4 horizontal infraestructura
- A4 vertical técnico
- A4 perfil/altimetría
- A3 técnico
- A3 ambiental
- A3 presentación
- A3 parcelario
- A3 satelital

### Regla editorial

Cada plantilla debe pasar este filtro:

- se entiende a primera vista;
- no está sobrecargada;
- la leyenda entra bien;
- el cartucho no compite con el mapa;
- el mapa principal domina la página;
- escala y norte no molestan;
- el layout exportado a PDF sigue viéndose profesional.

---

## Qué copiar primero

## Prioridad 1

- layouts QGIS simples con un solo mapa y leyenda clara;
- layouts ambientales sobrios;
- layouts técnicos con cartucho inferior;
- layouts satelitales limpios;
- layouts A4/A3 con jerarquía editorial fuerte.

## Prioridad 2

- layouts con tabla integrada;
- layouts de perfil o altimetría;
- layouts de presentación institucional;
- layouts con inset maps o mapa de ubicación.

## Prioridad 3

- layouts muy decorativos;
- layouts complejos con atlas;
- layouts con muchos items no soportados;
- layouts dependientes de plugins o expresiones avanzadas.

---

## Qué NO conviene hacer

- No descargar plantillas al azar solo para inflar el catálogo.
- No meter plantillas que dependan de datasets que CATGIS no puede resolver.
- No prometer compatibilidad total con QGIS.
- No intentar copiar ArcMap MXD directamente.
- No mezclar simbología de capas con plantillas de layout.
- No usar QGIS Models como si fueran layouts.

---

## Propuesta concreta de salida para OpenCode

OpenCode debería crear o actualizar:

- catálogo de fuentes de muestra;
- biblioteca ampliada de plantillas CATMAP;
- notas de procedencia por plantilla;
- agrupación por familia en el selector de plantillas;
- mejoras parciales del importador QPT solo si son rentables.

### Artefactos sugeridos

- `CATMAP_TEMPLATE_SOURCE_AUDIT.md` (este documento)
- `CATMAP_TEMPLATE_BACKLOG.md`
- `CATMAP_TEMPLATE_LICENSE_NOTES.md`

---

## Checklist mínimo antes de incorporar una fuente

- ¿Es realmente una plantilla/layout?
- ¿Tiene licencia clara o uso razonable como referencia?
- ¿Tiene `.qpt` o solo imagen?
- ¿El importador actual la podría absorber parcialmente?
- ¿Vale la pena reconstruirla nativamente?
- ¿Sirve para un caso de uso real del usuario?
- ¿Se exporta bien en PDF?
- ¿No rompe la simplicidad operativa de CATMAP?

---

## Recomendación final

La mejor estrategia para CATMAP no es “bajar todo lo que exista”, sino hacer esto:

1. tomar **Layout Hub** como fuente comunitaria prioritaria;
2. tomar **QGIS docs** como referencia oficial funcional;
3. tomar **QGIS Map Gallery** y **QGIS Projects** como benchmarking visual;
4. tomar los **PDFs reales del usuario** como estándar local;
5. convertir ese conjunto en una **biblioteca nativa CATMAP**, coherente y profesional.

En síntesis:

- **QPT y layouts públicos** = fuente de estructura
- **galerías y PDFs** = fuente de estética
- **CATMAP nativo** = producto final estable

No conviene depender de fuentes propietarias o de formatos no soportados.

CATMAP debe crecer por **reconstrucción nativa bien curada**, no por importación masiva e inestable.
