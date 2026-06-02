# CATMAP DeepSeek Execution Prompt

Usar este prompt en OpenCode cuando el motor no pueda interpretar imágenes.

---

## Prompt

```text
Tarea para OpenCode — Reparación real de CATMAP basada en especificación textual, sin depender de imágenes

Workspace:
C:\CATGIS\catgis-desktop

Documento guía obligatorio:
C:\CATGIS\catgis-desktop\CATMAP_VISUAL_UI_SPEC_2026-06-02.md

Objetivo:
Usar la especificación textual del archivo `CATMAP_VISUAL_UI_SPEC_2026-06-02.md` para corregir la UX visible de CATMAP, sin depender de capturas y sin inventar interpretación visual.

No quiero más auditorías optimistas.
No quiero más “cumple” si visualmente se sigue viendo mal.
Quiero corregir de verdad los problemas descritos en ese documento.

REGLA CENTRAL
El archivo `CATMAP_VISUAL_UI_SPEC_2026-06-02.md` es la fuente de verdad para esta ronda.
Si una conducta o expectativa está ahí escrita, debe tratarse como requisito funcional/visual real.

ÁREAS OBLIGATORIAS A CORREGIR

1. Popup contextual de texto
- Debe ser movible
- Debe tener Aceptar
- Debe tener Cancelar o Cerrar coherente
- Enter = Aceptar
- Esc = Cancelar/Cerrar
- Debe servir para:
  - Título
  - Subtítulo
  - LayoutLabel
  - Texto libre insertado
- Debe permitir:
  - contenido
  - fuente
  - tamaño
  - negrita
  - cursiva
  - subrayado si es rentable
  - color
  - halo opcional
  - color de halo
  - grosor de halo si es simple
  - fondo/opacidad/borde si aplica

2. Popup contextual de leyenda
- Debe ser movible
- Debe tener Aceptar
- Debe tener Cancelar o Cerrar coherente
- Enter = Aceptar
- Esc = Cancelar/Cerrar
- Debe abrirse por doble clic y por clic derecho > Propiedades
- Debe permitir:
  - título
  - fuente título
  - tamaño título
  - fuente ítems
  - tamaño ítems
  - color texto
  - fondo sí/no
  - color fondo
  - opacidad
  - borde sí/no
  - columnas
  - auto alto
  - actualizar capas
  - exclusión de mapas base si ya está implementada

3. Panel derecho
- No debe duplicar las propiedades ricas que ya se editan en popups
- Debe quedar para:
  - nombre
  - X/Y
  - ancho/alto
  - visible
  - bloqueado
  - datos rápidos del elemento

4. Panel izquierdo
- Debe seguir listando elementos del layout
- Pero debe ser más útil para:
  - selección
  - orden
  - visibilidad
  - bloqueo
  - claridad del stack visual
- No hace falta rehacer todo, pero sí mejorar su usabilidad

5. Plantillas
- El botón Plantillas debe ser fácil de entender
- Debe mostrar el catálogo real disponible
- Debe permitir aplicar una plantilla con claridad
- Debe comunicar mejor qué se está eligiendo

6. Auto-componer
- Debe comunicar mejor qué hace
- Si solo hay una variante, explicarlo con mejor UX
- Si hay varias, ofrecerlas mejor
- No debe parecer un botón misterioso

7. Simbología
- No resolver toda la simbología ArcGIS en esta ronda
- Sí auditar su estado y dejarla documentada como deuda visible si sigue siendo pobre
- Si hay una mejora pequeña y segura, hacerla
- Si no, documentar backlog honesto

ARCHIVOS A REVISAR
- src/ar/com/catgis/MapLayoutComposerDialog.java
- src/ar/com/catgis/layout/LayoutLabel.java
- src/ar/com/catgis/layout/LayoutLegend.java
- src/ar/com/catgis/layout/LayoutModel.java
- src/ar/com/catgis/layout/LayoutTemplateManager.java
- src/ar/com/catgis/layout/TemplateCatalog.java
- src/ar/com/catgis/layout/TemplateRegistry.java
- cualquier helper UI asociado

ENTREGABLE OBLIGATORIO
Crear o actualizar:
C:\CATGIS\catgis-desktop\CATMAP_UI_UX_GAP_REPORT.md

Debe decir:
1. qué estaba mal realmente
2. qué se corrigió
3. qué popup quedó operativo
4. qué salió del panel derecho
5. cómo quedó Plantillas
6. cómo quedó Auto-componer
7. qué sigue pendiente en simbología

VALIDACIÓN OBLIGATORIA
Ejecutar:
- gradlew compileJava
- gradlew test
- gradlew build -x checkstyleMain -x checkstyleTest

Además validar manualmente:
- doble clic en título
- doble clic en subtítulo
- doble clic en texto libre
- doble clic en leyenda
- clic derecho > Propiedades
- mover popup con mouse
- Enter acepta
- Esc cancela/cierra
- selector de fuente en leyenda
- halo en texto si se implementa
- botón Plantillas usable
- Auto-componer entendible

CRITERIO FINAL
No dar la tarea por resuelta si los popups siguen viéndose como cuadros toscos sin Aceptar o si las plantillas siguen siendo poco intuitivas.
```

---

## Uso recomendado

Este prompt debe usarse junto con:

- `CATMAP_VISUAL_UI_SPEC_2026-06-02.md`
- `CATMAP_CLAIM_AUDIT.md`
- `CATMAP_BACKLOG_POST_AUDIT.md`
- `CATMAP_UI_REPAIR_REPORT.md`

La prioridad para DeepSeek/OpenCode debe ser:

1. corregir la UX visible;
2. no volver a sobreprometer;
3. dejar CATMAP más intuitivo y novato-friendly;
4. documentar con honestidad lo pendiente.

