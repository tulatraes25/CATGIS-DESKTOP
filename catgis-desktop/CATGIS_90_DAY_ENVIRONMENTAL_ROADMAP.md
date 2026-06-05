# CATGIS 90 Day Environmental Roadmap

## 1. Posicionamiento

### Qué es CATGIS
CATGIS es un GIS de escritorio para **consultoría ambiental y cartografía técnica en Latinoamérica**. Está diseñado para que un consultor ambiental pueda abrir datos, simbolizar, etiquetar, maquetar y exportar un entregable final más rápido que en cualquier otra herramienta.

### Para quién
- Consultores ambientales haciendo EIA, línea de base, monitoreo
- Equipos pequeños/medianos en Latam
- Profesionales que hoy usan QGIS para análisis pero terminan en ArcMap para el layout
- Usuarios de Kosmo, gvSIG, OpenJUMP buscando algo más moderno
- Gente que quiere resultados cartográficos profesionales sin aprender 20 menús

### Qué NO intenta ser
- Un clon de QGIS o ArcGIS
- Un SIG de análisis enterprise
- Una plataforma de datos masivos
- Un editor GIS profesional (eso ya son QGIS/ArcGIS)

---

## 2. Estado actual

### Fortalezas
| Frente | Estado | Nota |
|--------|--------|------|
| CATMAP (compositor) | ~75% | Standalone con toolbar completa, export, .catmap, texto dinámico, grouping |
| Plantillas curadas | 28 | Único GIS con plantillas EIA out of box |
| Español nativo | 100% | UI en español, mercado desatendido |
| Simbología | 28+18+18 estilos | Suficiente para 90% de salidas ambientales |
| Etiquetado | Colisión + placement | Mejor que gvSIG/OpenJUMP/Kosmo |
| UX | FlatLaf | Más moderna que cualquier GIS Java |
| PostGIS R/W | Listo | Concurrencia controlada |
| Cartografía temática | 🆕 Riesgo booleano, inundación, hidrología | Diferenciador ambiental fuerte |
| Instalador | jpackage listo | CATGIS + CATMAP en un .exe |

### Debilidades
| Frente | Estado | Impacto |
|--------|--------|---------|
| Edición vectorial | Básica | Sin undo/redo, sin split/merge avanzado |
| PDF | Raster-only | No se puede editar texto en Illustrator |
| Atlas/Map series | Engine existe, UI básica | No integrado en flujo de trabajo |
| Plugin system | Shell | Sin ecosistema |
| Performance | Lento >10K features | Sin spatial index en layers grandes |
| Arquitectura | God Object MapPanel | ~9,690 LOC, riesgo de mantenimiento |
| CATMAP standalone | OK pero mejorable | Sin raster/online independiente |

### Oportunidad concreta
**El consultor ambiental en Latam hoy hace:**
1. Abre QGIS → carga datos → simboliza
2. Abre ArcMap → hace el layout (porque QGIS layout es tosco)
3. Exporta PDF → lo manda al cliente

**Con CATGIS podría:**
1. Abre CATGIS → carga datos → todo en español
2. Abre CATMAP → layout con plantilla EIA pre-cargada
3. Exporta PDF → hecho

**Ahorro: ~40% del tiempo. Sin cambiar de programa. Sin inglés.**

---

## 3. Roadmap 0-30 días — "Fundación sólida"

### Objetivo
Resolver las deudas técnicas que más afectan la calidad percibida y habilitar los flujos ambientales básicos.

### Features

| # | Feature | Esfuerzo | Impacto | Dependencia |
|---|---------|----------|---------|-------------|
| 1 | **Undo/Redo en editor vectorial** | 5 días | 🔴 Alto | Command pattern en MapPanel editing |
| 2 | **PDF vectorial** | 5 días | 🔴 Alto | LayoutExportEngine con PDFBox primitives |
| 3 | **Atlas UI completo** | 3 días | 🟡 Medio | Diálogo con filtros, formato, preview |
| 4 | **5 plantillas EIA nuevas** | 2 días | 🟡 Medio | LayoutTemplateManager |
| 5 | **Spatial index para capas vectoriales** | 3 días | 🟡 Medio | GeoTools R-tree index |
| 6 | **Performance: cache de render** | 2 días | 🟡 Medio | MapPanel tile cache |
| 7 | **Fix LayoutImage en diálogo propiedad** | 1 día | 🟢 Bajo | Propiedades de imagen editable |

### Técnica

**1. Undo/Redo en editor (5 días)**
- Implementar memento en `EditingEngine.java`
- Guardar snapshots de geometrías antes de cada operación (add vertex, delete vertex, move, split, merge)
- Ctrl+Z / Ctrl+Y en modo edición
- Limitar pila a 50 operaciones

**2. PDF vectorial (5 días)**
- En `LayoutExportEngine.exportPdf()`, en lugar de renderizar todo a BufferedImage:
  - Dibujar texto como `PDPageContentStream.showText()`
  - Dibujar rectángulos/líneas/elipses como PDF primitives
  - Solo rasterizar el mapa (LayoutMap.render())
  - El fondo blanco como página PDF
- Esto permite que el PDF sea editable en Illustrator, Inkscape, etc.

**3. Atlas UI (3 días)**
- Diálogo completo: seleccionar capa activa, campo de página, rango, filtro
- Preview de páginas generadas
- Export a PDF (multi-page) o PNG (carpeta)
- Cache de páginas generadas

**4. Plantillas EIA (2 días)**
Crear en `LayoutTemplateManager`:
- `EIA_ACCESIBILIDAD` — Mapa de accesos + cartucho
- `EIA_PUNTOS_MUESTREO` — Mapa con tabla de coordenadas
- `EIA_SENSIBILIDAD` — Mapa de zonificación ambiental
- `EIA_LINEA_BASE` — Mapa + perfiles + tabla
- `EIA_IMPACTOS` — Mapa + matriz de impactos

### Impacto esperado
- **Undo/Redo**: elimina el mayor pain point de edición. Sin esto no es profesional.
- **PDF vectorial**: los consultores pueden editar el PDF en Illustrator/Corel para ajustes finales.
- **Atlas**: mapas seriados para capítulos de EIA (un mapa por área de influencia).
- **Plantillas EIA**: el usuario abre y ya tiene el layout casi listo.
- **Spatial index**: proyectos con 50K features dejan de ser lentos.

### Riesgo
- Undo/Redo: toca `MapPanel` (~9,690 LOC), hay que ser cuidadoso con las regresiones.
- PDF vectorial: textos con acentos y caracteres especiales (español) necesitan font embedding.

### Criterio de éxito
- Undo/Redo funciona en 5 escenarios: add vertex, move vertex, delete vertex, split polygon, merge
- PDF exportado se abre en Illustrator sin errores, textos editables
- Atlas genera 20 páginas en <30 segundos
- 5 plantillas EIA disponibles en el selector

---

## 4. Roadmap 31-60 días — "Flujo ambiental completo"

### Objetivo
Construir el flujo de trabajo ambiental completo: desde la carga de datos hasta el entregable final.

### Features

| # | Feature | Esfuerzo | Impacto | Dependencia |
|---|---------|----------|---------|-------------|
| 8 | **Asistente "Nuevo proyecto EIA"** | 3 días | 🔴 Alto | |
| 9 | **Toolbox ambiental (6 tools one-click)** | 4 días | 🔴 Alto | |
| 10 | **Layouts EIA pre-armados (10)** | 3 días | 🔴 Alto | Plantillas del mes 1 |
| 11 | **Plugin system mínimo** | 4 días | 🟡 Medio | |
| 12 | **DXF export mejorado** | 2 días | 🟡 Medio | |
| 13 | **GPX import con atributos** | 2 días | 🟡 Medio | |
| 14 | **CATMAP: render raster sin CATGIS** | 3 días | 🟡 Medio | MapFrameRenderer raster support |

### Técnica

**8. Asistente "Nuevo proyecto EIA" (3 días)**
- Diálogo de bienvenida al iniciar CATGIS
- Opciones: "Nuevo proyecto EIA", "Abrir proyecto existente", "Inicio rápido"
- Al elegir "Nuevo proyecto EIA": wizard de 3 pasos
  - Paso 1: Nombre del proyecto, ubicación, cliente
  - Paso 2: Tipo de EIA (accesibilidad, línea de base, impacto, monitoreo)
  - Paso 3: Cargar capas base (suelos, hidrología, infraestructura desde CATSERVER)
- Crea proyecto con plantilla, capas y layout pre-configurado

**9. Toolbox ambiental (4 días)**
Un solo botón que abre un panel con herramientas contextuales:
- **Buffer rápido** — seleccionar capa, distancia, crear
- **Intersección** — seleccionar 2 capas, intersectar
- **Centroide** — generar puntos desde polígonos
- **Vértices** — extraer vértices como puntos
- **Clip por área** — recortar capa por polígono
- **Dissolve por atributo** — unir geometrías por campo

Cada tool: nombre en español, tooltip claro, 1-2 parámetros máximo.

**10. Layouts pre-armados (3 días)**
10 plantillas de layout con:
- Mapa dominante + cartucho inferior (ubicación)
- Mapa + tabla de coordenadas (puntos de muestreo)
- Mapa + perfil topográfico (línea de base)
- Mapa + leyenda lateral + cartucho (sensibilidad)
- Mapa + matriz de impactos (EIA completo)
- Mapa doble (ubicación + detalle)
- Mapa + gráfico de barras (monitoreo)
- Mapa + foto/imagen (registro de campo)
- Mapa A3 extendido (infraestructura)
- Mapa A4 con nota técnica (informe rápido)

**11. Plugin system (4 días)**
- `PluginManager.java` ya existe, mejorarlo:
  - Cargar plugins desde `plugins/` folder
  - API simple: `init()`, `run()`, `getMenuEntry()`
  - Plugin de ejemplo: "Exportar a KMZ"
  - Plugin de ejemplo: "Calcular área de cuenca"

**12-13. DXF/GPX (2 días cada uno)**
- DXF export: preservar colores de capas, simbología simple
- GPX import: leer waypoints como puntos con nombre y descripción

**14. Render raster sin CATGIS (3 días)**
- `MapFrameRenderer.renderIndependent()`: para raster (GeoTIFF, DEM), renderizar usando GeoTools GridCoverage2DRenderer
- Para online tiles (WMS, TileURL): cachear tiles y componer
- Esto hace que CATMAP standalone muestre TODAS las capas, no solo vectoriales

### Impacto esperado
- **Asistente EIA**: reduce onboarding de 20 min a 2 min
- **Toolbox ambiental**: un clic reemplaza 15 pasos en QGIS
- **Layouts pre-armados**: el usuario no diseña el layout, solo completa datos
- **Plugin system**: permite extensiones de terceros
- **Render standalone**: CATMAP funciona 100% sin CATGIS

### Riesgo
- Toolbox ambiental: necesita pruebas con datos reales
- Plugin system: API breaking changes si se diseña mal

### Criterio de éxito
- Asistente EIA guía al usuario desde 0 a layout listo en <5 min
- 6 tools ambientales funcionan con 1-2 clics cada una
- 10 layouts pre-armados disponibles
- Plugin de ejemplo funcional documentado
- CATMAP standalone muestra correctamente raster y tiles

---

## 5. Roadmap 61-90 días — "Producto terminado"

### Objetivo
Pulir, estabilizar y preparar para distribución. CATGIS debe sentirse como un producto terminado, no un beta.

### Features

| # | Feature | Esfuerzo | Impacto | Dependencia |
|---|---------|----------|---------|-------------|
| 15 | **Refactor MapPanel (AppContext migration)** | 10 días | 🔴 Alto | |
| 16 | **CATMAP: deshacer/rehacer en propiedades** | 3 días | 🟡 Medio | LayoutModel snapshots |
| 17 | **CATMAP: múltiples páginas** | 3 días | 🟡 Medio | LayoutModel page support |
| 18 | **Documentación de usuario (ES)** | 5 días | 🟡 Medio | |
| 19 | **Testing: 50 escenarios EIA** | 5 días | 🟡 Medio | |
| 20 | **Instalador pulido (logo, firmas, WIX)** | 2 días | 🟢 Bajo | |
| 21 | **Bug bounties / estabilización** | 7 días | 🔴 Alto | |

### Técnica

**15. Refactor MapPanel (10 días)**
El más riesgoso de toda la hoja de ruta.
- Extraer `EditorEngine` completo (hoy repartido entre `MapPanel` y `EditingEngine`)
- Migrar referencias estáticas a `AppContext`
- `MapPanel` pasa de ~9,690 LOC a ~5,000
- No romper funcionalidad existente

**16. CATMAP undo en propiedades (3 días)**
- `saveSnapshot()` antes de cada cambio en propiedades contextuales
- Ctrl+Z en el diálogo de propiedades
- Indicador visual de "deshacer disponible"

**17. Múltiples páginas en CATMAP (3 días)**
- `LayoutModel` ya soporta páginas (`getCurrentPage()`, `addPage()`)
- UI: botones "Agregar página", "Eliminar página", navegación
- Export multi-page PDF

**18. Documentación (5 días)**
- Manual de usuario en español: 20-30 páginas
- Guía rápida: "De 0 a tu primer mapa ambiental en 10 minutos"
- Video tutorial (script + capturas)

**19. Testing (5 días)**
- 50 escenarios de uso ambiental
- Tests de regresión automatizados
- Pruebas con datasets reales (10K, 50K, 100K features)

**20. Instalador pulido (2 días)**
- Logo 512x512 en instalador
- Firma digital (si aplica)
- Página de inicio en el instalador
- Desinstalador limpio

### Impacto esperado
- CATGIS se siente como un producto terminado
- Los bugs se reducen significativamente
- La documentación permite onboarding autónomo
- El instalador genera confianza profesional

### Riesgo
- Refactor MapPanel: **ALTO**. Puede romper funcionalidad existente. Hacerlo con tests de regresión.
- 50 escenarios de prueba requiere datos reales.

### Criterio de éxito
- MapPanel < 5,000 LOC sin funcionalidad perdida
- 268+ tests verdes (tests nuevos incluidos)
- Manual de usuario de 20+ páginas en español
- Instalador .exe con logo, firmado, que instala CATGIS + CATMAP
- < 5 bugs críticos conocidos

---

## 6. Lo que no conviene hacer ahora

| Feature | Razón |
|---------|-------|
| **Edición 3D/TIN** | Demasiado complejo para el valor. QGIS tiene years de desarrollo en esto |
| **Servidor de mapas web** | Ya hay CATSERVER + catserver-web. No duplicar |
| **App mobile** | Mercado saturando, equipo pequeño, distrae |
| **Machine learning / AI** | Hype, no demanda real en consultoría ambiental Latam |
| **Rediseño UI completo** | FlatLaf ya es moderno. Cambiar por cambiar no suma |
| **Soporte de bases de datos enterprise (Oracle, SQL Server)** | PostGIS cubre 95% de casos Latam |
| **Export a CAD nativo (DWG)** | DXF es suficiente, DWG es formato cerrado |
| **Render 3D (WebGL/OpenGL)** | No es un visor 3D, es un GIS para producción de mapas |
| **Repositorio de plugins online** | Primero que haya plugins, después repositorio |
| **Colaboración en tiempo real** | Google Docs para mapas no es un caso de uso real hoy |

---

## 7. Quick wins / Medium bets / Strategic bets

### Quick wins (0-10 días, bajo esfuerzo, alto impacto)
| Item | Días | Por qué |
|------|------|---------|
| ✅ Instalador unificado | 0 (listo) | Ya configurado |
| 5 plantillas EIA nuevas | 2 | Diferenciador inmediato |
| Fix LayoutImage props | 1 | Bug reportado |
| GPX import atributos | 2 | Reemplaza Mapsource |
| DXF export mejorado | 2 | Formato de intercambio más usado |

### Medium bets (10-20 días, esfuerzo medio, alto impacto)
| Item | Días | Por qué |
|------|------|---------|
| Undo/Redo en editor | 5 | Habilidad más pedida |
| PDF vectorial | 5 | Diferenciador frente a Kosmo/gvSIG |
| Atlas UI completo | 3 | Mapas seriados para EIA |
| Toolbox ambiental | 4 | El "one-click" que prometemos |
| Render raster standalone | 3 | CATMAP 100% independiente |

### Strategic bets (20+ días, inversión alta, diferenciación a largo plazo)
| Item | Días | Por qué |
|------|------|---------|
| Refactor MapPanel | 10 | Deuda técnica que limita todo |
| Documentación en español | 5 | Adopción en Latam |
| Plugin system | 4 | Ecosistema a futuro |
| Asistente EIA | 3 | Experiencia de onboarding |

### Orden de entrega recomendado

**Semana 1:** Instalador + plantillas EIA + fix LayoutImage → **demo comercial listo**
**Semana 2-3:** Undo/Redo + PDF vectorial + DXF/GPX → **edición profesional**
**Semana 4:** Atlas + Toolbox ambiental → **flujo ambiental completo**
**Semana 5-6:** Asistente EIA + Layouts pre-armados + Plugin system → **producto**
**Semana 7-8:** Render standalone + Documentación → **independencia**
**Semana 9-12:** Refactor MapPanel + Testing + Bug bounties → **estabilidad**

---

## 8. KPI sugeridos

| KPI | Hoy | Target 90 días | Cómo medir |
|-----|-----|----------------|------------|
| Tiempo para mapa ambiental final | ~20 min | **<5 min** | Cronómetro: abrir CATGIS → layout → export PDF |
| Pasos para layout/export | ~15 | **<5 clics** | Contar clicks desde proyecto abierto a PDF |
| Tiempo de onboarding | ~2 hs | **<20 min** | Usuario nuevo produce su primer layout |
| Crashes/errores conocidos | ~8 críticos | **<2 críticos** | Bug tracker |
| Tiempo abrir proyecto (50 capas) | ~15 seg | **<5 seg** | Cronómetro |
| Tiempo export PDF A4 | ~8 seg | **<3 seg** | Cronómetro |
| Acciones manuales evitadas | — | **>15** | Comparar pasos CATGIS vs QGIS para misma tarea |
| Tests verdes | 268 | **>320** | `gradlew test` |
| Instalador funcional | Beta | **Release** | Prueba en 3 PCs distintas |

---

## 9. Feature Matrix

Feature matrix detallada en archivo separado: `CATGIS_ENVIRONMENTAL_FEATURE_MATRIX.md`

---

## 10. Recomendación final

### El posicionamiento correcto
CATGIS no compite con QGIS. Lo complementa.

El consultor ambiental usa QGIS para análisis pesado (hidrología avanzada, modelado 3D, procesamiento LiDAR). Usa CATGIS para **la salida final**: el mapa que va en el informe, la lámina que ve el cliente, el entregable que firma.

### La trampa a evitar
"No hagamos todo, hagamos esto bien."

CATGIS gana si:
1. **Abrir proyecto + layout + export** toma menos de 5 minutos
2. **El PDF vectorial** es editable en Illustrator
3. **Las plantillas EIA** están listas para usar
4. **La UI es en español** y los tooltips son claros
5. **No crashea.** Punto.

### La métrica que importa
> *"¿Cuánto tiempo pasa entre que abro CATGIS y entrego el PDF al cliente?"*

Si esa métrica es menor que con cualquier otra herramienta, CATGIS gana.

### Veredicto final
90 días es suficiente para pasar de **52/90 a ~65/90** —no para alcanzar a QGIS (76/90), sino para ser **el mejor GIS ambiental práctico de Latinoamérica**. Ese es el nicho. Ahí no compite nadie.
