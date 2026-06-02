# CATMAP Template, Scale and UX Spec - 2026-06-02

## Objetivo de este documento
Este archivo describe en texto claro los problemas visibles que siguen existiendo en CATMAP y lo que debe corregirse en una nueva ronda.

Esta especificacion esta pensada para motores que no interpretan imagenes. Todo lo importante se describe aca en texto.

## Problemas reales detectados

### 1. Escala grafica mal resuelta
Problemas actuales:
- la escala grafica se ve mal;
- la numeracion no se lee bien;
- no queda presentada como una escala tecnica prolija;
- no es claro como regular la escala desde CATMAP;
- el usuario quiere poder volver a definir la escala manualmente como antes;
- al hacer boton derecho sobre la escala y elegir propiedades, hoy no responde como deberia.

Comportamiento esperado:
- la escala debe tener propiedades editables;
- el popup de propiedades de la escala debe abrir de verdad;
- la escala debe poder mostrar una numeracion limpia y legible;
- la escala tecnica debe poder definirse manualmente por el usuario;
- CATMAP no debe ocultar ni endurecer innecesariamente ese control;
- por defecto debe verse bien, incluso antes de que el usuario la retoque.

### 2. Simbolo de norte mal posicionado
Problema actual:
- el simbolo de norte se va a cualquier lado al aplicar ciertas plantillas;
- aunque se pueda mover despues, por defecto queda mal resuelto.

Comportamiento esperado:
- el norte debe quedar por defecto en el margen superior derecho del marco de mapa;
- debe respetar separacion de borde;
- debe tener un tamaño razonable;
- no debe tapar informacion clave;
- debe seguir siendo editable despues, pero arrancar bien posicionado.

### 3. Tipografia por defecto pobre
Problema actual:
- varios elementos aparecen con tamaños de letra chicos o poco equilibrados;
- el layout por defecto no se ve profesional;
- el usuario termina teniendo que corregir manualmente cosas que deberian venir bien de arranque.

Comportamiento esperado:
- por defecto, titulo, subtitulo, leyenda, escala y cartucho deben quedar legibles;
- el usuario despues puede retocar, pero la base inicial debe verse bien;
- el layout inicial debe tener criterio estetico tecnico.

### 4. Nombres de plantillas poco tecnicos
Problema actual:
- nombres como "Referencia" o "Accesibilidad" no se sienten como nombres tecnicos de plantillas;
- el usuario espera una nomenclatura mas profesional y mas GIS/cartografica.

Comportamiento esperado:
- usar nombres mas tecnicos y claros;
- sugerencia de estilo:
  - Infraestructura · A4 · Ubicacion general
  - Infraestructura · A4 · Acceso operativo
  - Infraestructura · A4 · Emplazamiento tecnico
  - Tecnica · A4 · Leyenda derecha
  - Tecnica · A4 · Cartucho inferior
  - Ambiental · A4 · Muestreo
  - Catastral · A4 · Parcelario
  - Hidrologia · A4 · Drenaje general
  - Topografia · A4 · Curvas de nivel
  - Perfil · A4 · Altimetria tecnica

Regla:
- los nombres deben ayudar al usuario a elegir;
- deben sonar tecnicos y profesionales;
- no deben sonar a placeholders.

### 5. Muy pocas plantillas visibles
Problema actual:
- el selector ahora es mejor visualmente, pero quedaron muy pocas plantillas visibles;
- el usuario quiere mas variedad, siempre que sean buenas.

Comportamiento esperado:
- aumentar la cantidad de plantillas visibles;
- pero solo si son buenas;
- mantener el selector con preview;
- no volver al menu desplegable;
- agregar familias utiles y tecnicas, no clones basura.

### 6. Menu contextual inconsistente
Problema actual:
- al hacer boton derecho sobre algunos elementos, la opcion `Propiedades` no hace nada;
- se detecto explicitamente en la escala;
- puede estar pasando en otros elementos tambien.

Comportamiento esperado:
- todo elemento editable del layout debe responder a `Propiedades`;
- si el elemento tiene configuracion editable, debe abrir popup;
- si no la tiene todavia, debe documentarse y no dejar una accion muerta;
- no debe haber items de menu placebo.

### 7. Selector de plantillas: mantener ventana, no dropdown
Estado deseado:
- el selector de plantillas debe seguir siendo una ventana separada;
- debe mostrar preview;
- debe mostrar descripcion;
- debe ser facil de encontrar para un usuario novato;
- no debe volver a un dropdown.

## Elementos que deben revisarse si o si
- Titulo
- Subtitulo
- Mapa principal
- Leyenda
- Escala grafica
- Norte
- Datos cartograficos / cartucho
- Selector de plantillas
- Menus contextuales y `Propiedades`

## Reglas de calidad para las plantillas
Una plantilla se considera mala o defectuosa si:
- deforma el mapa;
- hace desaparecer la leyenda;
- deja el norte mal ubicado por defecto;
- usa letras demasiado chicas o desequilibradas;
- produce una escala mal numerada o poco legible;
- deja el cartucho inutil o desbalanceado;
- tiene nombre poco tecnico;
- no sirve para mapa tecnico real.

## Reglas de UX
- el usuario debe encontrar rapido el selector de plantillas;
- el selector debe ser ventana con preview;
- las plantillas deben tener nombres tecnicos;
- los elementos deben arrancar bien ubicados por defecto;
- las propiedades por clic derecho deben funcionar;
- la escala debe poder editarse;
- el norte no debe salir volando;
- el layout debe verse razonable incluso antes de editarlo.

## Backlog prioritario sugerido
1. Arreglar `Propiedades` de escala.
2. Restaurar/asegurar control manual de escala tecnica.
3. Reposicionar por defecto el norte arriba a la derecha.
4. Mejorar tipografia por defecto de plantillas.
5. Renombrar plantillas a nomenclatura tecnica.
6. Reampliar el catalogo visible, pero solo con plantillas aptas.
7. Auditar que `Propiedades` funcione en todos los elementos del menu contextual.

## Resultado esperado de la siguiente ronda
- plantilla elegida desde una ventana clara con preview;
- mapa sin deformacion;
- norte bien ubicado por defecto;
- escala legible y editable;
- nombres tecnicos de plantillas;
- mas plantillas visibles buenas;
- `Propiedades` funcionando sobre todos los elementos relevantes;
- CATMAP mas cercano a un compositor GIS serio y novato-friendly.
