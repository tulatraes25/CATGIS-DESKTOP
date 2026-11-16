# CATGIS / CATMAP Product — Requirements

Status: ACTIVE
Owner: CATGIS
Baseline: `main` after PR #217 (saneamiento T5–T8)
Program: PRODUCT_BUILD -> GITHUB_AUTOMATED_CERTIFICATION -> RUNTIME_E2E -> UX_POLISH -> RELEASE
Directive: el propietario levantó el congelamiento de features para iniciar el producto
CATGIS/CATMAP; A0.7 y F3 siguen siendo la puerta para **declarar** GUI/nativo
verificados.

## Goal

Convertir CATGIS Desktop en un GIS de escritorio profesional y accesible, con un
compositor cartográfico **CATMAP standalone** usable sin abrir CATGIS, análisis de
terreno/hidrología de nivel profesional y una UX tipo "Filmora del GIS": resultado
profesional, flujo guiado, sin el laberinto de QGIS/Kosmo.

Principios:
- **No reescribir** Swing/GeoTools/JTS por moda; se construye sobre la base actual.
- **No copiar código** de ArcGIS (propietario) ni QGIS (GPL); se implementan
  algoritmos publicados y flujos propios.
- **Datos del usuario primero** (archivos locales), OGC estándar y PostGIS
  **opcional**; CATSERVER no es requisito del producto.
- Toda capacidad nueva se prueba deterministicamente en GitHub antes de
  reclamarse; la verificación GUI/nativa sigue bloqueada hasta A0.7/F3.

## REQ-PROD-0 — CATMAP como aplicación independiente real

WHEN el usuario abre CATMAP, THE SYSTEM SHALL iniciar una aplicación propia
(launcher/menú propio) que no requiera la ventana de CATGIS ni fabrique sus
estáticos de UI, con la misma calidad de compositor que el embebido.

Acceptance:
- AC-PROD-0.1: `catmap.Main` arranca sin crear `CatgisDesktopApp`, `MapPanel` ni
  `LayersPanel` estáticos; abre proyectos `.catgis` y documentos `.catmap`.
- AC-PROD-0.2: No hay ciclo `layout` ↔ `catmap` ni dependencia de
  `MapLayoutComposerDialog` desde el motor de layout.
- AC-PROD-0.3: Un test headless demuestra apertura de proyecto y render de una
  página sin la app principal.

## REQ-PROD-1 — Apertura de datos y proyecto en CATMAP

WHEN el usuario trabaja en CATMAP, THE SYSTEM SHALL permitir abrir un proyecto
completo y también "mapa rápido" desde datos sueltos, sin pasar por CATGIS.

Acceptance:
- AC-PROD-1.1: Flujo "mapa rápido" con SHP, GeoJSON, KML/KMZ, GeoPackage,
  GeoTIFF y CSV como mínimo, con CRS explícito y fallo cerrado si no se puede
  determinar (sin inventar `EPSG:4326`).
- AC-PROD-1.2: WFS/WMS/WMTS y PostGIS opcional conectables desde el standalone.
- AC-PROD-1.3: Un proyecto abierto conserva capas, simbología, CRS y orden.

## REQ-PROD-2 — Tamaño de página profesional

WHEN el usuario configura la página, THE SYSTEM SHALL soportar A3, A4, A5,
Carta, Legal, Tabloide y **personalizado en mm**, con orientación y márgenes, y
persistir esa configuración en el documento.

Acceptance:
- AC-PROD-2.1: `LayoutModel` (o el documento) transporta `PageSpec` (tamaño,
  orientación, márgenes) y el serializador lo guarda y restaura.
- AC-PROD-2.2: Preview, export y print usan la misma `PageSpec` (sin 297×210
  hardcodeado).
- AC-PROD-2.3: Test de roundtrip: guardar A3 vertical con márgenes, reabrir y
  exportar produce dimensiones exactas.

## REQ-PROD-3 — Exportación multipágina y paridad vista previa = salida

WHEN el usuario exporta un documento, THE SYSTEM SHALL generar PDF multipágina
(con tamaño por documento), PNG/JPG por página, SVG vectorial real e imprimir
con `PageFormat`, y la salida SHALL coincidir geométricamente con la vista previa.

Acceptance:
- AC-PROD-3.1: PDF con N páginas, tamaño exacto en mm, texto vectorial con
  fuentes embebidas cuando sea posible; el verificador deja de exigir 1 página.
- AC-PROD-3.2: PNG/JPG multi-página (archivos o ZIP) y SVG vectorial (solo el
  mapa rasterizado).
- AC-PROD-3.3: Un único `RenderMetrics` convierte pt/mm→px con `dpi/72` para
  TODOS los elementos (labels, escala, leyenda, tabla, cartouche, etiquetas del
  mapa); sin mezclar px fijos con DPI.
- AC-PROD-3.4: Test "preview == export": un layout de referencia renderizado a
  96/150/200 DPI coincide dentro de tolerancia declarada.
- AC-PROD-3.5: Impresión usa el `PageFormat` del documento y soporta N páginas.

## REQ-PROD-4 — Atlas v2

WHEN el usuario genera un atlas, THE SYSTEM SHALL encuadrar cada feature
correctamente (centro + buffer + aspecto del marco), persistir la configuración
y emitir PDF multipágina, PNG o SVG.

Acceptance:
- AC-PROD-4.1: Encuadre corregido (sin reutilizar `zoomFactor` como extent).
- AC-PROD-4.2: Campo de orden/nombre configurable; página dinámica `{page}` y
  `{feature}` en etiquetas.
- AC-PROD-4.3: Configuración de atlas serializada en el documento.
- AC-PROD-4.4: Test con 3 features verifica extensión, orden y PDF multipágina.

## REQ-PROD-5 — Escala cartográfica derivada

WHEN el layout muestra una barra de escala, THE SYSTEM SHALL calcularla desde el
viewport, el tamaño del marco y el CRS (no desde un denominador tipeado a mano).

Acceptance:
- AC-PROD-5.1: `ScaleResolver` compartido por standalone y compositor.
- AC-PROD-5.2: La barra se recalcula al mover/zoomear el mapa y en export.
- AC-PROD-5.3: Test con dos extents conocidos y CRS métrico/geográfico.

## REQ-PROD-6 — Plantillas en el standalone

WHEN el usuario abre la galería de plantillas en CATMAP, THE SYSTEM SHALL
aplicar plantillas (incluye A3/A4 y QPT de QGIS) con su página, orientación y
elementos.

Acceptance:
- AC-PROD-6.1: Galería por categorías reutilizando `TemplateRegistry`.
- AC-PROD-6.2: Importar `.qpt` aplica página y elementos soportados.
- AC-PROD-6.3: Test de plantilla aplicada en `catmap.Main` headless.

## REQ-PROD-7 — GeoTIFF y formatos de salida

WHEN el usuario exporta el mapa, THE SYSTEM SHALL poder producir GeoTIFF
georreferenciado del marco y SVG vectorial, además de PDF/PNG/JPG.

Acceptance:
- AC-PROD-7.1: GeoTIFF con CRS/extent del viewport, legible por `GeoTiffReader`
  (ida/vuelta).
- AC-PROD-7.2: SVG con texto/formas vectoriales y solo el mapa como imagen.
- AC-PROD-7.3: Export GeoPackage/FlatGeobuf y lectura de world files
  (`.tfw/.jgw/.pgw`) evaluados como siguiente iteración documentada.

## REQ-PROD-8 — Análisis de terreno e hidrología profesional

WHEN el usuario analiza un DEM, THE SYSTEM SHALL producir raster analíticos
exportables (float, NoData, unidades) y resultados con morfometría, no solo
imágenes de pantalla.

Acceptance:
- AC-PROD-8.1: Pendiente, aspecto, hillshade, dirección D8 y acumulación se
  generan como coberturas numéricas con GeoTIFF y estadísticas; sin truncar a 8
  bits ni regenerar silenciosamente con parámetros distintos.
- AC-PROD-8.2: Curvas de nivel, drenaje y cuencas a **resolución nativa**
  (sin downsample fijo 256/512/768) con umbrales en área (m²/ha) además de celdas.
- AC-PROD-8.3: Cuencas con morfometría: área, perímetro, elevación/pendiente
  media, orden Strahler, densidad de drenaje.
- AC-PROD-8.4: DEM acondicionado (fill/breach/priority-flood) exportable como
  GeoTIFF.
- AC-PROD-8.5: Viewshed expuesto en UI con observador, radio y capa de resultado.
- AC-PROD-8.6: Perfil con pendiente, curva hipsométrica y perfil 3D expuestos.
- AC-PROD-8.7: Volumen/corte-relleno entre superficies con reporte.
- AC-PROD-8.8: Interpolación desde puntos (IDW propio; kriging evaluado).
- AC-PROD-8.9: Fixtures sintéticos con verificación numérica independiente en
  CI (DEM de pendiente conocida, cuenca de área conocida, red de drenaje
  esperada).

## REQ-PROD-9 — Inundación y riesgo georreferenciados

WHEN el usuario corre escenarios de inundación o riesgo, THE SYSTEM SHALL usar
georreferencia y parámetros reales, persistir la trazabilidad y no presentar
resultados preliminares como hidráulica certificada.

Acceptance:
- AC-PROD-9.1: `RiskAnalysisEngine` deja de asumir 30 m / 111320 m por grado;
  usa CRS y tamaño de celda reales.
- AC-PROD-9.2: Riesgo con clases de susceptibilidad, exposición/vulnerabilidad y
  parámetros persistidos; `WILDFIRE` implementado o retirado explícitamente.
- AC-PROD-9.3: Inundación con modelo declarado (no hidráulica 1D/2D), periodo de
  retorno documentado y GeoTIFF de profundidad verificado.
- AC-PROD-9.4: Trazabilidad: versión de algoritmo, parámetros y CRS en el
  resultado/proyecto.

## REQ-PROD-10 — UX "Filmora del GIS"

WHEN el usuario opera el producto, THE SYSTEM SHALL presentar un espacio de
trabajo guiado (proyecto/datos izquierda, mapa centro, parámetros derecha,
trabajos/resultados abajo), sin cadenas de modales y con búsqueda de comandos.

Acceptance:
- AC-PROD-10.1: Confirmación de cambios sin guardar en TODAS las salidas
  (`Salir` deja de ser `System.exit(0)`), incluido CATMAP.
- AC-PROD-10.2: Cargar N archivos no abre N modales; se usa la infraestructura de
  toasts/notification ya existente.
- AC-PROD-10.3: Paleta de comandos/búsqueda de herramientas por tarea.
- AC-PROD-10.4: HiDPI (escala y variantes) y selector de tema (claro/oscuro).
- AC-PROD-10.5: i18n honesta: sin traducción automática parcial; inglés completo
  o español completo por archivo; cero títulos hardcodeados en diálogos visibles.
- AC-PROD-10.6: Eliminación de UI muerta (toolbars/paneles jamás agregados) y
  duplicados de comando.

## REQ-PROD-11 — Red de seguridad de comportamiento (A0.2)

WHILE se refactorizan `MapPanel`, `MapLayoutComposerDialog` o `LayoutPageRenderer`,
THE SYSTEM SHALL NOT cambiar comportamiento sin evidencia de caracterización.

Acceptance:
- AC-PROD-11.1: Golden masters de render (escenas deterministas, hashes) para
  mapa y layout a DPI fijos.
- AC-PROD-11.2: Contratos de roundtrip de proyecto y documento.
- AC-PROD-11.3: Extracciones en tranches chicos con ratchet como techo, nunca
  como objetivo.

## REQ-PROD-12 — Certificación y límites de reclamo

THE SYSTEM SHALL cumplir el gate GitHub-first en cada tranche y SHALL NOT
declarar GUI/nativo verificado sin evidencia runtime.

Acceptance:
- AC-PROD-12.1: PR desde `main` vivo, CI + Certification + Runtime Identity +
  GeoParquet, merge con expected head SHA y atestación post-main.
- AC-PROD-12.2: Sin secretos, datos de usuario ni credenciales en CI.
- AC-PROD-12.3: A0.7/F3 siguen controlando el claim de runtime; el producto
  avanza con pruebas deterministas mientras tanto.

## Non-goals

- Reescribir la aplicación en otro stack.
- Copiar código de ArcGIS o QGIS.
- Ser QGIS: no competir por cantidad de plugins ni formatos exóticos.
- Prometer hidráulica 1D/2D certificada en esta fase.
- Tocar datos, backups o credenciales del usuario.
