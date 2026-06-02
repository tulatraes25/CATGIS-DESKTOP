# CATMAP Implemented Fixes - 2026-06-02

## Objetivo de esta ronda
Esta ronda se enfoco en corregir problemas reales de CATMAP que seguian apareciendo en el uso diario:

- el mapa dentro de las plantillas se seguia viendo como captura estirada;
- el acceso a plantillas seguia siendo poco intuitivo;
- varias plantillas visibles eran flojas o rompian la composicion;
- `Datos cartograficos` no quedaba bien integrado al flujo de propiedades;
- la leyenda seguia usando una simbologia demasiado generica;
- los popups de texto y leyenda eran toscos, incomodos y poco utiles.

La idea no fue vender humo ni sumar catalogo por cantidad. La idea fue mejorar el comportamiento real y dejar documentado lo que si cambio y lo que todavia sigue pendiente.

## Archivos modificados
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\MapLayoutComposerDialog.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\MapPanel.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutLegend.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutMap.java`
- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\layout\LayoutTemplateManager.java`

## 1. Cambio importante en el mapa de las plantillas
### Problema anterior
Cuando se aplicaba una plantilla, el mapa del layout tendia a quedar como una captura raster reutilizada y luego reescalada. Eso generaba deformacion visual, especialmente al cambiar de plantilla o al usar marcos con otra proporcion.

### Cambio implementado
Se hicieron tres correcciones coordinadas:

1. `MapPanel` ahora puede renderizar la vista directamente al tamano exacto del marco del layout.
2. `LayoutMap` invalida su cache tambien por ancho y alto, no solo por extent.
3. Al aplicar una plantilla, el `LayoutMap` ya no queda forzado por defecto a una captura congelada del mapa principal.

### Efecto buscado
- reducir stretch del mapa;
- evitar que un bitmap viejo se estire para entrar en otro marco;
- hacer que el mapa del layout siga mejor la vista real del proyecto.

### Limitacion honesta
Esto mejora mucho el comportamiento, pero no convierte todavia a CATMAP en un map frame vectorial independiente como QGIS Layout o ArcMap. Sigue siendo un render del mapa principal llevado al marco del layout, aunque ahora de forma mas coherente y menos deformante.

## 2. Plantillas: acceso mas claro y catalogo curado
### Problema anterior
El acceso a plantillas era confuso y muy parecido a un dropdown poco amigable. Ademas, habia demasiadas variantes flojas visibles al mismo tiempo.

### Cambio implementado
Se reemplazo el acceso principal por una ventana de seleccion con vista previa y descripcion. Ya no depende de un menu simple para elegir la composicion.

Tambien se dejo un conjunto curado de plantillas visibles, priorizando usos tecnicos reales:

- `A4_REFERENCIA`
- `A4_ACCESIBILIDAD`
- `A4_EMPLAZAMIENTO`
- `A4_TECNICO`
- `A4_TECNICO_INFERIOR`
- `A4_AMBIENTAL`
- `A4_CATASTRAL`
- `A4_HIDROLOGIA`
- `A4_TOPOGRAFIA`
- `A4_PERFIL`
- `A4_MUESTREO`
- `A3_TECNICO`
- `A3_AMBIENTAL`
- `A3_CATASTRAL`

Cada una tiene nombre visible y descripcion breve en el selector.

### Efecto buscado
- que el usuario novato vea una ventana clara de plantillas;
- que no se mezclen cien variantes malas con unas pocas utiles;
- que las plantillas visibles tengan mas sentido practico.

### Limitacion honesta
Todavia hay que seguir auditando visualmente cada plantilla. El catalogo visible mejoro, pero no significa que todas hayan alcanzado un nivel profesional definitivo.

## 3. Popups de texto mucho mas utiles
### Problema anterior
Los popups de texto eran muy basicos, feos, con layout manual y sin un flujo serio de aceptar o cancelar.

### Cambio implementado
El popup de `LayoutLabel` se rearmo como dialogo real con:

- `Aceptar`
- `Cancelar`
- `Enter = aceptar`
- `Esc = cancelar`
- ventana normal movible
- edicion de contenido
- fuente
- tamano
- negrita
- cursiva
- subrayado
- color
- halo opcional
- ancho de halo
- color de halo

Tambien se agrego restauracion del estado original si el usuario cancela.

### Elementos cubiertos
Este flujo sirve para textos del layout, incluido el `Titulo` cuando se logra seleccionar como `LayoutLabel` real.

## 4. Popup de leyenda rearmado
### Problema anterior
La leyenda tenia un popup muy flojo, poco movible, sin flujo serio y sin permitir editar cuestiones tipograficas basicas.

### Cambio implementado
El popup de `LayoutLegend` se rearmo como dialogo real con:

- `Actualizar capas`
- `Aceptar`
- `Cancelar`
- `Enter = aceptar`
- `Esc = cancelar`
- titulo
- fuente del titulo
- tamano del titulo
- fuente de items
- tamano de items
- color del titulo
- color de items
- fondo activar/desactivar
- color de fondo
- opacidad
- borde activar/desactivar
- color de borde
- columnas
- auto alto

### Efecto buscado
Que la leyenda deje de depender solo del panel lateral derecho y tenga una edicion contextual mucho mas parecida a lo que el usuario espera de un compositor serio.

## 5. Datos cartograficos / cartucho
### Problema anterior
`Datos cartograficos` muchas veces no entraba bien en el flujo de propiedades desde la estructura del layout.

### Cambio implementado
Se mejoro el enrutamiento desde la estructura izquierda para que:

- `Leyenda` abra la leyenda real del `LayoutModel`;
- `Datos cartograficos` intente abrir el `LayoutCartouche` real;
- `Titulo` busque un `LayoutLabel` con nombre coherente y abra el popup de texto.

Ademas, las plantillas curadas ahora intentan poblar el cartucho con datos utiles del proyecto:

- Estudio
- Proyecto
- Empresa
- Cartografo
- Fuente
- Coord.

### Limitacion honesta
Si una plantilla vieja o externa no genera `LayoutCartouche` real, el flujo puede seguir siendo incompleto. La integracion esta mejor, pero todavia depende de que la plantilla haya sido construida con elementos reales y no con rastros hardcodeados.

## 6. Leyenda con simbologia menos generica
### Problema anterior
La leyenda venia mostrando simbolos demasiado genericos, lo que empobrecía el mapa incluso si la capa tenia mejor intencion de estilo.

### Cambio implementado
`LayoutLegend` ahora conserva y usa mas informacion de simbologia:

- `catalogSymbolId`
- `pointSymbolStyle`
- `lineSymbolStyle`
- `polygonFillStyle`
- `strokeColor`

Y el render de simbolos de leyenda se amplio para representar mejor:

- puntos
- lineas
- poligonos
- rellenos y borde

### Efecto buscado
Acercar la leyenda a la simbologia real de las capas y dejar de mostrar siempre marcas demasiado planas o equivalentes.

### Limitacion honesta
Esto mejora la leyenda, pero no resuelve por completo la pobreza general del catalogo de simbologia de capas de CATGIS. La deuda de simbologia global sigue existiendo y requiere una ronda especifica sobre `LayerPropertiesDialog`, previews y catalogos de estilos mas ricos.

## 7. Simbologia de puntos mas coherente entre mapa, dialogos y leyenda
### Problema anterior
Habia varios lugares dibujando puntos de forma distinta:

- el mapa principal;
- la vista previa de propiedades de capa;
- la vista previa de simbologia categorizada;
- la vista previa de leyenda en CATMAP.

Eso hacia que muchos estilos aparecieran con nombres distintos, pero en pantalla terminaran viendose demasiado parecidos o directamente equivocados.

### Cambio implementado
Se agrego un renderer compartido:

- `C:\CATGIS\catgis-desktop\src\ar\com\catgis\PointSymbolRenderer.java`

Y se conecto a:

- `MapPanel`
- `LayerPropertiesDialog`
- `CategorizedSymbologyDialog`
- `MapLayoutComposerDialog`

Ahora las variantes de puntos usan una base de dibujo comun para estilos como:

- circulo
- cuadrado
- rombo
- triangulo
- triangulo invertido
- objetivo
- pin
- bandera
- estrella 5
- estrella 6
- pozo
- cruz
- cruz diagonal
- hexagono
- pentagono
- flecha arriba
- flecha abajo
- camara
- torre
- anillo
- doble circulo
- rectangulo horizontal
- rectangulo vertical
- alerta
- ubicacion
- muestreo
- control
- acceso

Tambien se corrigio el fallback del catalogo para que, si llega un id no reconocido, no termine usando un simbolo arbitrario del catalogo por accidente.

### Efecto buscado
- menos sensacion de que "todo son puntos con distinto nombre";
- mas consistencia entre mapa, preview y leyenda;
- base mas fuerte para una ronda futura de simbologia profesional completa.

## 8. Flags hardcodeados y sincronizacion con LayoutModel
### Problema anterior
Parte de CATMAP seguia mezclando elementos del `LayoutModel` con banderas o renderizados viejos, lo que podia producir duplicaciones o confusiones.

### Cambio implementado
Se agrego una sincronizacion para que, si ya existe en el modelo un:

- `LayoutMap`
- `LayoutLegend`
- `LayoutCartouche`
- u otro elemento equivalente

se apaguen las banderas hardcodeadas correspondientes y no se dibuje doble.

### Efecto buscado
Reducir elementos fantasma y hacer que el `LayoutModel` mande mas que los caminos heredados.

## 9. Validacion tecnica ejecutada
Se ejecutaron estos comandos:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
.\gradlew.bat build -x checkstyleMain -x checkstyleTest
```

### Resultado
- `compileJava`: OK
- `test`: OK
- `build -x checkstyleMain -x checkstyleTest`: OK

### Nota honesta
En ejecuciones paralelas hubo un lock del archivo:

- `build\test-results\test\binary\output.bin`

Eso no fue un error de logica del codigo sino un problema de archivo tomado al correr validaciones superpuestas. Ejecutado en secuencia, el build termino correctamente.

## 10. Lo que si mejoro de verdad
- el mapa de las plantillas ya no queda fijado por defecto a una captura congelada;
- el render al marco del layout ahora usa tamano exacto de salida;
- el selector de plantillas paso a una ventana con preview y descripcion;
- el popup de texto paso a ser un dialogo util y editable;
- el popup de leyenda ahora tiene mucha mas edicion contextual;
- el cartucho se integra mejor al flujo real del modelo;
- la leyenda representa mejor la simbologia de las capas;
- la simbologia de puntos ahora es mucho mas coherente entre mapa, previews y leyenda;
- se redujo la duplicacion entre elementos del modelo y caminos hardcodeados.

## 11. Lo que sigue pendiente
Esto es importante para no vender humo.

### Pendiente A - calidad definitiva de plantillas
Aunque el acceso y el catalogo visible mejoraron, todavia hace falta seguir puliendo varias plantillas en uso real. No conviene asumir que todas ya quedaron a la altura de un compositor GIS profesional.

### Pendiente B - simbologia global de capas
La leyenda y la simbologia de puntos mejoraron, pero la simbologia general del software sigue necesitando una ronda fuerte aparte:

- mas estilos de puntos reales
- mas estilos de lineas
- mas estilos de poligonos
- mejores previews
- mejor integracion entre simbolo de capa y simbolo de leyenda

### Pendiente C - map frame profesional completo
El comportamiento es mejor y menos deforme, pero CATMAP sigue sin ser un compositor con map frame completamente independiente tipo QGIS/ArcMap.

### Pendiente D - limpieza total de la UI lateral
El panel derecho todavia existe y conserva redundancias. Esta mejor que antes porque los popups ya absorben la edicion rica, pero aun no esta completamente depurado.

## 12. Recomendacion para la siguiente ronda
Si se quiere seguir fortaleciendo CATMAP, el mejor orden es:

1. Auditar visualmente las plantillas visibles y retirar cualquier composicion floja.
2. Hacer una ronda fuerte dedicada solo a simbologia de capas.
3. Seguir limpiando caminos hardcodeados de CATMAP.
4. Mejorar el comportamiento del map frame como objeto cada vez mas independiente.

## 13. Resumen honesto
Esta ronda no convierte automaticamente a CATMAP en ArcMap o QGIS Layout.
Pero si deja mejoras concretas y visibles:

- mejor relacion entre plantilla y mapa real;
- mejor selector de plantillas;
- mejor popup de texto;
- mejor popup de leyenda;
- mejor integracion del cartucho;
- mejor simbologia de leyenda.

Lo correcto es tomar esto como una mejora real, no como cierre definitivo del compositor.
