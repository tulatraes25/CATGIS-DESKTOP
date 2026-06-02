# CATMAP — Escala y Templates: Estado final (2026-06-02)

## Escala grafica
- Popup contextual: clic derecho > Propiedades sobre la escala abre editor con segmentos, color y unidad
- Escala tecnica: clic derecho > Propiedades sobre el mapa abre popup con campo "Escala 1:N" y boton Aplicar
- El usuario puede ingresar cualquier valor (ej: 1235, 18750, 4320) sin redondeo forzado
- La escala grafica se conecta a la escala real del mapa via mapScaleDenominator

## Norte
- Posicion por defecto: top-right del marco del mapa (~268mm X, ~42mm Y en A4)
- Corregido en 4 plantillas principales (Referencia, Accesibilidad, Emplazamiento, Perfil)
- Popup contextual: clic derecho > Propiedades permite editar X, Y y tamano

## Plantillas curadas (22 visibles)
### A4 (15)
- Infraestructura: Ubicacion general, Acceso operativo, Emplazamiento tecnico, General
- Tecnica: Leyenda derecha, Leyenda inferior, Cartucho inferior
- Ambiental: Estandar, Muestreo
- Hidrologia: General
- Topografia: Curvas de nivel
- Catastral: Estandar, Parcelario con tabla
- Satelital: Estandar
- Perfil: Altimetria tecnica

### A3 (7)
- Tecnica, Ambiental, Catastral, Satelital, Hidrologia, Topografia, Presentacion

### Dialogo de plantillas
- Ventana con lista + preview + descripcion
- Agrupado por A4 y A3 con headers visuales
- Doble clic o boton Aplicar
- A3 cambia automaticamente pagina a 420x297mm

## Propiedades contextuales
Funcionan en 6/6 elementos: Titulo, Leyenda, Cartucho, Escala, Norte, Mapa

## Pendiente
- Norte top-right en TODAS las plantillas (solo 4/22 corregidas)
- Mas variacion visual entre plantillas del mismo tipo
- Linea/poligono: preview visual en dialogo de propiedades
