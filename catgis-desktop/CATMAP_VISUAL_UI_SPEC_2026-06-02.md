# CATMAP Visual UI Spec

Fecha: 2026-06-02  
Workspace: `C:\CATGIS\catgis-desktop`  
Objetivo: describir en texto, sin depender de imágenes, los problemas visuales y de UX actualmente observados en CATMAP y el comportamiento esperado para que OpenCode/DeepSeek pueda corregirlos sin ver capturas.

---

## 1. Propósito de este documento

Este documento existe porque el motor usado en OpenCode no interpreta imágenes.

Por lo tanto, todo lo que normalmente se mostraría con capturas queda especificado acá en forma textual:

- qué se ve hoy;
- por qué se ve mal;
- qué parte funciona solo a medias;
- cómo debería comportarse realmente;
- qué se considera aceptable y qué no.

La regla es simple:

Si algo no puede entenderse sin ver la imagen, este documento tiene que decirlo con suficiente detalle para que un agente lo implemente igual.

---

## 2. Resumen de problemas reales actuales

La situación actual de CATMAP no es que “no funcione nada”.
Sí funciona bastante lógica de layout y composición.

El problema real es este:

1. Hay popups contextuales para `Título` y `Leyenda`, pero se ven toscos, inmóviles y pobres.
2. No tienen una UX profesional comparable ni siquiera con un editor básico de texto.
3. El panel derecho sigue mostrando opciones que deberían haber migrado al popup contextual.
4. El panel izquierdo “Elementos del layout” se ve como una lista pobre, con poca capacidad real de organización.
5. El selector de plantillas existe pero no se percibe como una herramienta clara, central y novato-friendly.
6. `Auto-componer` no comunica bien qué hace ni qué variantes ofrece.
7. La simbología sigue siendo pobre y visualmente débil.

En otras palabras:

- la lógica interna puede haber mejorado;
- la UX visible todavía no acompaña ese avance.

---

## 3. Descripción textual de lo que hoy se ve mal

## 3.1. Popup de leyenda

### Estado visual actual

El popup de leyenda aparece como un cuadro pequeño superpuesto dentro del área del layout, con borde azul fino, sin título de ventana visible tipo diálogo estándar del sistema, y con una disposición muy básica de controles.

Contiene algo parecido a:

- campo de `Título`
- combo de `Tam. título`
- combo de `Tam. ítems`
- checkboxes `Fondo`, `Borde`, `Auto alto`
- combo o spinner de `Columnas`
- botón `Actualizar capas`
- botón `Cerrar`

### Problemas observados

1. No se ve como una ventana seria, sino como un cuadrito embebido.
2. No se puede mover libremente como un diálogo normal.
3. No tiene `Aceptar`.
4. No tiene `Cancelar`.
5. `Enter` no confirma.
6. `Esc` no cancela.
7. No permite elegir fuente para el título ni para los ítems.
8. Sigue siendo demasiado pobre para una leyenda cartográfica.
9. Parte de las propiedades sigue existiendo en el panel lateral derecho, duplicando criterio.

### Comportamiento esperado

El editor de leyenda debe comportarse como un diálogo contextual real:

- movible;
- con barra de título clara;
- con `Aceptar`, `Cancelar`, `Cerrar` o equivalente coherente;
- `Enter = Aceptar`;
- `Esc = Cancelar`;
- fuente configurable del título;
- fuente configurable de ítems;
- tamaño del título;
- tamaño de ítems;
- color de texto;
- halo opcional si se implementa;
- fondo sí/no;
- color de fondo;
- opacidad;
- borde sí/no;
- columnas;
- auto alto;
- actualización de capas;
- exclusión de mapas base si aplica.

No debe depender del panel derecho para terminar de editar lo esencial.

---

## 3.2. Popup de título / texto

### Estado visual actual

Aparece otro cuadro pequeño, también superpuesto en el layout, con borde azul fino y aspecto poco profesional.

El popup actual de texto tiene algo parecido a:

- un preview o campo de texto arriba con el contenido
- etiqueta `Formato de texto`
- combo `Fuente`
- combo `Tamaño`
- botones `B`, `/`, `U`
- selector de color
- botón `Cerrar`

### Problemas observados

1. Se ve rudimentario.
2. No se puede mover cómodamente.
3. No tiene `Aceptar`.
4. No tiene `Cancelar`.
5. `Enter` no acepta.
6. `Esc` no cancela.
7. No tiene halo.
8. No tiene fondo/borde si se quisiera enriquecer el texto.
9. No se integra bien con la experiencia del resto del compositor.
10. Solo resuelve una parte de lo pedido.

### Comportamiento esperado

Debe existir un editor contextual de texto usable para:

- `Título`
- `Subtítulo`
- cualquier `LayoutLabel`
- cualquier texto insertado manualmente

Y debe permitir como mínimo:

- contenido;
- fuente;
- tamaño;
- negrita;
- cursiva;
- subrayado si es rentable;
- color;
- halo opcional;
- color del halo;
- grosor del halo si es simple;
- alineación si aplica;
- fondo opcional;
- opacidad;
- borde opcional.

Debe abrirse con:

- doble clic;
- clic derecho > `Propiedades`;
- y también en el flujo de inserción de texto si corresponde.

---

## 3.3. Panel lateral derecho

### Estado visual actual

Cuando se selecciona un elemento, el panel derecho muestra todavía propiedades como:

- nombre;
- X/Y;
- ancho/alto;
- visible;
- bloqueado;
- contenido;
- fuente;
- tamaño;
- negrita;

### Problema conceptual

Esto contradice la idea del popup contextual rico.

Si ya existe un popup de texto o leyenda, el panel derecho no debería seguir siendo el lugar principal para editar esas propiedades “ricas”.

### Criterio correcto

El panel derecho debería quedar para:

- estado general del elemento;
- posición;
- tamaño;
- visible;
- bloqueado;
- datos rápidos y técnicos;
- ajustes simples del elemento.

Las propiedades ricas de:

- tipografía;
- estilo visual;
- leyenda;
- halo;
- fondo;
- bordes finos;
- contenido detallado;

deben vivir en el popup contextual.

### Regla

No debe haber duplicación confusa entre popup y panel derecho.

---

## 3.4. Panel lateral izquierdo “Elementos del layout”

### Estado visual actual

La barra izquierda muestra:

- encabezado `Elementos del layout`
- botón `+ Elemento`
- botón `Plantillas`
- opciones `Alinear`, `Orden`
- botones `Visible`, `Bloquear`
- una lista de elementos como:
  - Datos cartográficos
  - Norte
  - Escala gráfica
  - Leyenda
  - Mapa principal
  - Subtítulo
  - Título

Cada uno con iconos muy pequeños y escasa diferenciación visual.

### Problemas observados

1. Se ve pobre.
2. Parece más lista estática que panel profesional de composición.
3. No comunica jerarquía ni orden visual con claridad.
4. No transmite bien qué está seleccionado.
5. No parece un panel realmente útil para organizar composición.
6. No se siente parecido a Contents/Items de un compositor GIS serio.

### Comportamiento esperado

El panel izquierdo debe mejorar al menos en:

- claridad de selección;
- orden visual de la pila;
- nombre amigable de elementos;
- visibilidad;
- bloqueo;
- subir / bajar orden;
- sensación de herramienta de organización real.

No hace falta que sea ArcGIS Pro completo, pero sí debe dejar de parecer una lista muerta.

---

## 3.5. Selector de plantillas

### Estado visual actual

Existe un botón o dropdown `Plantillas`, pero no se siente como el punto principal para elegir composiciones.

### Problemas observados

1. No se percibe como una acción importante.
2. No comunica cuántas plantillas existen.
3. No comunica su organización temática.
4. No es especialmente intuitivo para un usuario novato.

### Comportamiento esperado

El selector de plantillas debe ser:

- visible;
- fácil de encontrar;
- claramente entendible;
- agrupado por temática o al menos por prefijos legibles;
- capaz de mostrar qué plantilla está activa;
- capaz de aplicar una plantilla sin ambigüedad.

Si hay muchas plantillas:

- deben aparecer agrupadas;
- no como una lista caótica.

---

## 3.6. Botón Auto-componer

### Estado visual actual

Existe un botón `Auto-componer` en la barra superior.

### Problema

El nombre y la UX no explican bien qué hace.

Para un usuario novato, puede sonar abstracto o misterioso.

### Comportamiento esperado

Debe comunicar mejor:

- qué genera;
- con qué criterio;
- si produce una plantilla base;
- si arma mapa + leyenda + escala + norte + cartucho;
- si tiene variantes o presets.

Si todavía solo tiene una variante:

- igual debe explicarse mejor con tooltip o subtítulo.

Si tiene varias variantes:

- debería ofrecerlas de forma explícita.

---

## 3.7. Simbología

### Estado actual percibido

La simbología general del programa sigue viéndose pobre y limitada.

El usuario la percibe como:

- débil;
- poco profesional;
- demasiado básica;
- muy por debajo de ArcGIS o incluso de un SIG más modesto.

### Problema

Aunque esto no es exclusivamente un problema de CATMAP, impacta directamente en la calidad visual del resultado cartográfico.

Un layout serio no se salva si:

- la leyenda es mala;
- los puntos se ven pobres;
- las líneas y polígonos tienen poca expresividad;
- no hay catálogo visual convincente.

### Qué debe quedar claro en texto

La simbología es un frente de trabajo independiente.

No debe mezclarse de forma improvisada con CATMAP, pero sí debe documentarse como deuda visible:

- catálogo de puntos;
- líneas;
- polígonos;
- estilos más profesionales;
- control de etiquetas;
- mejores previews.

---

## 4. Qué comportamiento mínimo debe cumplir cada popup

## 4.1. Reglas comunes

Todo popup contextual de CATMAP debe:

- ser movible;
- abrir centrado o cerca del elemento seleccionado;
- tener título de ventana claro;
- tener `Aceptar`;
- tener `Cancelar` o `Cerrar` coherente;
- `Enter = Aceptar`;
- `Esc = Cancelar/Cerrar`;
- mantener tamaño razonable;
- no salirse de pantalla;
- no tapar todo el layout sin necesidad.

## 4.2. Regla de confirmación

Si el popup edita propiedades:

- debe tener botón `Aceptar`;
- los cambios se aplican con `Aceptar`;
- `Cancelar` descarta cambios si se trabaja con buffer temporal.

Si el sistema usa live preview:

- debe explicitarlo;
- igual tiene que tener un botón de confirmación o cierre claro.

---

## 5. Qué debe migrar fuera del panel derecho

Estas propiedades no deberían seguir viviendo principalmente en la barra lateral derecha:

- fuente de título;
- tamaño de título;
- color tipográfico;
- estilo de fuente;
- propiedades ricas de leyenda;
- fondo / opacidad / borde de leyenda;
- halo tipográfico;
- edición de contenido de textos.

Estas sí pueden seguir en el panel derecho:

- nombre;
- X/Y;
- ancho/alto;
- visible;
- bloqueado;
- ajustes geométricos rápidos.

---

## 6. Aceptación funcional esperada

Se considera aceptable solo si:

1. doble clic en `Título` abre editor contextual movible;
2. doble clic en `Subtítulo` abre editor contextual movible;
3. doble clic en un texto libre abre el mismo tipo de editor;
4. doble clic en `Leyenda` abre editor contextual de leyenda;
5. clic derecho > `Propiedades` abre el mismo editor correcto;
6. `Enter` acepta;
7. `Esc` cancela o cierra;
8. el popup de leyenda permite elegir fuente;
9. el popup de texto permite halo si fue implementado;
10. el panel derecho ya no duplica de forma confusa las propiedades ricas;
11. el botón `Plantillas` permite descubrir y aplicar plantillas con facilidad;
12. `Auto-componer` se entiende mejor para usuario novato.

---

## 7. Qué NO debe considerarse solución válida

No es solución válida:

- dejar el popup como cuadrito embebido inmóvil;
- dejar solo botón `Cerrar`;
- dejar solo panel derecho y llamarlo “editor contextual”;
- dejar plantillas ocultas o poco visibles;
- dejar `Auto-componer` como botón opaco sin explicación;
- dejar la barra izquierda igual y decir que ya organiza.

---

## 8. Backlog visual recomendado

## Prioridad crítica

- popup de texto profesional mínimo;
- popup de leyenda profesional mínimo;
- `Aceptar` / `Cancelar` / `Enter` / `Esc`;
- popups movibles;
- selector de plantillas visible y entendible.

## Prioridad alta

- limpieza del panel derecho;
- mejora del panel izquierdo;
- halo tipográfico;
- mejor narrativa para `Auto-componer`.

## Prioridad media

- toolbar contextual más rica para texto;
- preview dentro de popup de leyenda;
- presets de auto-composición.

## Prioridad alta pero separada

- simbología profesional de puntos/líneas/polígonos.

---

## 9. Instrucción operativa para OpenCode/DeepSeek

Si el agente no puede ver imágenes, debe asumir lo siguiente:

1. Los popups actuales existen pero visualmente son insuficientes.
2. El panel derecho sigue invadiendo territorio que debería ser contextual.
3. El panel izquierdo es funcional pero pobre.
4. El selector de plantillas no está resuelto de forma suficientemente clara.
5. `Auto-componer` necesita mejor UX.
6. La simbología es una deuda visible y grave.

No debe responder con auditoría linda.
Debe responder con:

- wiring UI correcto;
- comportamiento verificable;
- aceptación visual razonable;
- documentación honesta de lo que queda pendiente.

