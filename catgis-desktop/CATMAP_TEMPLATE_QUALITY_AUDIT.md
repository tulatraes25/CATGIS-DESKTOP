# CATMAP Template Quality Audit

## 1. Causa raiz del mapa deformado

**Problema**: `LayoutMap.captureMapImage(pw, ph)` capturaba el mapa forzandolo a las dimensiones exactas del frame (pw × ph pixels). Si el mapa tenia diferente aspect ratio, se estiraba.

**Solucion**: `LayoutMap.render()` ahora usa `Math.min(scaleX, scaleY)` para mantener el aspect ratio. La imagen capturada se centra dentro del frame con barras negras en los margenes sobrantes.

## 2. Causa raiz de perdida de leyenda al cambiar plantilla

**Problema**: `applyTemplate()` limpiaba todos los elementos y creaba una leyenda vacia (sin items de capas). No llamaba a `populateLegendFromProject()`.

**Solucion**: `applyTemplate()` ahora auto-popula toda leyenda vacia desde las capas del proyecto, filtrando basemaps.

## 3. Plantillas APTAS (24 con builders especificos)

Todos los templates con builders dedicados funcionan correctamente:
A4_AMBIENTAL, A4_TECNICO, A4_TECNICO_INFERIOR, A4_CATASTRAL, A4_HIDROLOGIA, A4_TOPOGRAFIA, A4_URBANO, A4_PARCELARIO, A4_INFRAESTRUCTURA, A4_VERTICAL, A4_MUESTREO, A4_SATELITAL, A4_REFERENCIA, A4_ACCESIBILIDAD, A4_EMPLAZAMIENTO, A4_PERFIL, A3_TECNICO, A3_AMBIENTAL, A3_CATASTRAL, A3_SATELITAL, A3_PARCELARIO, A3_HIDROLOGIA, A3_TOPOGRAFIA, A3_PRESENTACION

## 4. Plantillas REGULARES (parametricas)

Las 52 plantillas parametricas (buildParametric) producen layouts funcionales pero con variaciones minimas. Comparten la misma estructura base. Son usables pero no optimas.

## 5. Plantillas DEFECTUOSAS

Ninguna. Todas las plantillas producen al menos mapa + titulo + elementos basicos.

## 6. Que se corrigio

- Mapa ya no se estira: mantiene aspect ratio
- Leyenda ya no desaparece: auto-popula desde capas del proyecto
- Cartouche ya tiene popup: doble clic abre editor de campos
- Propiedades del cartouche: desde clic derecho o doble clic

## 7. Pendiente

- Las plantillas parametricas podrian mejorarse con mas variacion
- No hay preview en miniatura de cada plantilla
