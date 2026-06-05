# Changelog — 2026-06-04

## Resumen

Ronda de fixes basados en la auditoría exhaustiva de CATGIS Desktop. Se aplicaron 5 correcciones sobre 7 identificadas, se investigó 1 pendiente de runtime, y 1 ya estaba implementado.

## Modificaciones

### 1. MapPanel.java — paintComponent try/finally (HIGH)

**Archivo:** `catgis-desktop/src/ar/com/catgis/MapPanel.java`  
**Líneas:** ~6024-6105  
**Problema:** El método `paintComponent` creaba un `Graphics2D g2 = (Graphics2D) g.create()` y llamaba a `g2.dispose()` al final, pero sin `try/finally`. Si cualquier excepción ocurría durante el render (entre create y dispose), el objeto Graphics2D nativo nunca se liberaba, causando **memory leak** de recursos gráficos nativos (no GC-collectable).

**Fix:** Se envolvió todo el bloque de render entre `try { ... } finally { g2.dispose(); }`, garantizando que `g2.dispose()` se ejecute incluso si ocurre una excepción.

```java
// Antes:
Graphics2D g2 = (Graphics2D) g.create();
// ... ~80 líneas de render ...
g2.dispose();

// Después:
Graphics2D g2 = (Graphics2D) g.create();
try {
    // ... render ...
} finally {
    g2.dispose();
}
```

**Nota:** La indentación interna quedó desalineada (sin tabulación extra) porque el bloque inner es muy grande. Funcionalmente es correcto.

---

### 2. MapPanel.java — refreshLayerList thread safety (HIGH)

**Archivo:** `catgis-desktop/src/ar/com/catgis/MapPanel.java`  
**Línea:** ~1034  
**Problema:** El método `refreshEditingUi()` llamaba directamente a `CatgisDesktopApp.layersPanel.refreshLayerList()` sin garantía de estar en el Event Dispatch Thread (EDT). Si se invoca desde un background thread, puede corromper el modelo Swing (no thread-safe).

**Fix:** Se encapsuló la llamada en `SwingUtilities.invokeLater()` para asegurar que se ejecute en el EDT:

```java
// Antes:
CatgisDesktopApp.layersPanel.refreshLayerList();

// Después:
javax.swing.SwingUtilities.invokeLater(() -> CatgisDesktopApp.layersPanel.refreshLayerList());
```

---

### 3. MapPanel.java — Null checks en mapPanel (HIGH)

**Archivo:** `catgis-desktop/src/ar/com/catgis/MapPanel.java`  
**Líneas:** ~4482-4483  
**Problema:** En el método `convertPinsToLayer`, se accedía a `CatgisDesktopApp.mapPanel.showOpenedFile()` y `CatgisDesktopApp.mapPanel.repaint()` sin chequear null. Si `mapPanel` no está inicializado (por ejemplo, durante startup o estado parcial), esto lanza un NullPointerException.

**Fix:** Se agregó null check antes de los accesos:

```java
// Antes:
CatgisDesktopApp.layersPanel.addLayer(layer);
CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
CatgisDesktopApp.mapPanel.repaint();

// Después:
CatgisDesktopApp.layersPanel.addLayer(layer);
if (CatgisDesktopApp.mapPanel != null) {
    CatgisDesktopApp.mapPanel.showOpenedFile(layer.getName());
    CatgisDesktopApp.mapPanel.repaint();
}
```

**Nota:** Hay ~300 referencias a `CatgisDesktopApp.mapPanel.` en 45 archivos. El audit original solo señalaba estas dos en MapPanel.java. El problema sistémico se resolverá con la migración a `AppContext` (mencionada en la auditoría de arquitectura).

---

### 4. MainToolBar.java — Labels "OK"/"X" no localizables (MEDIUM)

**Archivo:** `catgis-desktop/src/ar/com/catgis/MainToolBar.java`  
**Línea:** ~142  
**Problema:** Los botones "Terminar" y "Cancelar" tenían texto hardcodeado "OK" y "X" como etiqueta debajo del ícono, sin pasar por `I18n.t()`. Esto impedía la localización.

**Fix:** Se reemplazaron los strings hardcodeados por `I18n.t()`:

```java
// Antes:
add(createLabeledButton(btnTerminar, "OK"));
add(createLabeledButton(btnCancelar, "X"));

// Después:
add(createLabeledButton(btnTerminar, I18n.t("Terminar")));
add(createLabeledButton(btnCancelar, I18n.t("Cancelar")));
```

---

### 5. CatmapSocketClient.java — JSON parser frágil (MEDIUM)

**Archivo:** `catgis-desktop/src/ar/com/catgis/catmap/CatmapSocketClient.java`  
**Problema:** El parser JSON manual tenía varias vulnerabilidades:

#### extractJsonValue
- **Antes:** Buscaba `"key":"` y luego el siguiente `"`, lo que se rompe con valores que contienen escapes (`\"`) o comillas internas.
- **Después:** Itera carácter por carácter manejando escapes (`\\` → `\`, `\"` → `"`) correctamente.

#### extractJsonDouble
- **Antes:** Buscaba la siguiente coma `,` como delimitador, fallando si el valor era el último del objeto.
- **Después:** Avanza hasta encontrar `,`, `}`, `]` o espacio, manejando `null` y whitespace.

#### extractJsonNullValue (NUEVO)
- Helper que distingue entre valores string, numéricos y `null`, retornando `null` Java cuando corresponde.

#### parseLayers
- **Antes:** Split por `},{` que se rompe con objetos vacíos, anidamiento, o formato variado.
- **Después:** Parser con contador de profundidad (`depth++`/`depth--`) que extrae objetos correctamente aunque tengan anidamiento interno.

---

### 6. MainMenuBar.java — Items vacíos (MEDIUM) — VERIFICADO

**Archivo:** `catgis-desktop/src/ar/com/catgis/MainMenuBar.java`  
**Estado:** ✅ Ya estaba implementado. Todos los items del menú Edición tienen action listeners:
- Cortar, Copiar, Pegar, Copiar a capa editable, Borrar
- Deshacer, Rehacer
- Mover selección, Cortar geometría, Unir vértices
- Unir elementos, Explotar
- Guardar/Terminar/Cancelar edición
- Submenú CAD con 8 herramientas

Posiblemente el audit se basó en una versión anterior o hubo un falso positivo.

---

### 7. LayoutExportEngine.java — PDF image position (HIGH) — FIXEADO

**Archivo:** `catgis-desktop/src/ar/com/catgis/catmap/LayoutExportEngine.java`  
**Estado:** ✅ Fix aplicado.

**Problema:** La clase `LayoutExportEngine` (standalone, usada por CATMAP Standalone) renderiza el layout en **A4 apaisado** (297×210 mm) pero creaba la página PDF con `PDRectangle.A4` que es **A4 vertical** (210×297 mm, 595.28×841.89 pts).

Al hacer `cs.drawImage(pdfImg, 0, 0, rect.getWidth(), rect.getHeight())`, PDFBox escala la imagen landscape para que quepa en el rectángulo portrait — la imagen se distorsiona (stretch vertical + squash horizontal) y las posiciones relativas de los elementos quedan incorrectas.

**Fix:** Se reemplazó `PDRectangle.A4` (portrait) por un rectángulo A4 landscape equivalente:

```java
// Antes:
org.apache.pdfbox.pdmodel.common.PDRectangle rect = org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
// → portrait: 595.28 x 841.89 pts

// Después:
org.apache.pdfbox.pdmodel.common.PDRectangle rect = new org.apache.pdfbox.pdmodel.common.PDRectangle(
    org.apache.pdfbox.pdmodel.common.PDRectangle.A4.getHeight(),
    org.apache.pdfbox.pdmodel.common.PDRectangle.A4.getWidth()
);
// → landscape: 841.89 x 595.28 pts
```

**Nota:** El `MapLayoutComposerDialog` (compositor dentro de CATGIS) ya manejaba correctamente la orientación usando `toPdfRectangle(orientation)`. El bug era solo del export standalone.

**Verificado:** compileJava BUILD SUCCESSFUL.

---

## SEGUNDA RONDA — CATMAP Fix y Refactor

### 8. MapFrameViewport.java — Detección de vista vacía en fitFromMainMap (CRÍTICO)

**Archivo:** `layout/MapFrameViewport.java`  
**Problema:** `fitFromMainMap()` tomaba el envelope del `MapPanel` aunque estuviera vacío (standalone crea un MapPanel nuevo con view `(0,0, zoom=1)`, envelope ~800×600 alrededor de (0,0)). Los datos reales están en Argentina (~-68,-33), el viewport no intersectaba y se veía en blanco. El método devolvía `true`, por lo que el fallback `fitFromProjectLayers()` nunca se ejecutaba.

**Fix:**
- Si el proyecto tiene capas visibles con datos, se calcula el envelope combinado
- Si el MapPanel envelope NO intersecta con los datos → se usa el de las capas
- Si el MapPanel envelope es sospechosamente grande (>100× los datos) → se usa el de las capas

### 9. MapLayoutComposerDialog — Eliminación de LayoutMap redundante (CRÍTICO)

**Archivo:** `MapLayoutComposerDialog.java`  
**Problema:** El dialog agregaba un `LayoutMap` por defecto en posición fija (15mm,25mm,267mm,145mm). El template (`LayoutRenderer`) también dibujaba un map frame en posición diferente calculada por márgenes. Esto creaba DOS mapas compitiendo.

**Fix:** Se eliminó el `LayoutMap` por defecto del layout model. El template ya maneja el map frame vivo desde el MapPanel. LayoutMap ahora es solo para mapas adicionales (inset maps).

### 10. MapLayoutComposerDialog — Toolbar Insertar ampliada

**Archivo:** `MapLayoutComposerDialog.java`  
**Cambio:** Se agregaron botones en la toolbar para insertar Mapa, Leyenda, Escala, Norte al layout.

### 11. LayoutMap.java — Fallback robusto en syncViewportToSource

**Archivo:** `layout/LayoutMap.java`  
**Cambio:** Se mejoró la documentación y se aseguró que el flujo `fitFromMainMap()` → `fitFromProjectLayers()` funcione correctamente ahora que `fitFromMainMap()` ya no devuelve `true` con vistas vacías.

### Documentación creada
- `CATMAP_STANDALONE_PRODUCT_REWORK.md` — Qué estaba mal, qué se corrigió, arquitectura actual, capacidades, pendientes
- `CATMAP_STANDALONE_UX_REPORT.md` — Estructura visual, menús, toolbars, paneles, flujo de uso, limitaciones
- `CATMAP_FIX_PLAN.md` — Plan de fases

## Build & Tests

```
compileJava   → BUILD SUFFUL in 3s
test          → 268/268 PASSED in 44s
jacocoTestReport → OK
build -x checkstyleMain -x checkstyleTest → BUILD SUCCESSFUL in 8s
```
