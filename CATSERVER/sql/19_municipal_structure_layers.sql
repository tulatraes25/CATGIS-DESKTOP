\set ON_ERROR_STOP on
\connect catserver

CREATE SCHEMA IF NOT EXISTS gobierno_modernizacion;
CREATE SCHEMA IF NOT EXISTS control_urbano_operativo;
CREATE SCHEMA IF NOT EXISTS ordenamiento_territorial;
CREATE SCHEMA IF NOT EXISTS infraestructura_osp;
CREATE SCHEMA IF NOT EXISTS desarrollo_humano_familia;
CREATE SCHEMA IF NOT EXISTS mujer_genero_juventud_diversidad;
CREATE SCHEMA IF NOT EXISTS cultura;
CREATE SCHEMA IF NOT EXISTS salud;
CREATE SCHEMA IF NOT EXISTS recaudacion;
CREATE SCHEMA IF NOT EXISTS economia_finanzas;

COMMENT ON SCHEMA gobierno_modernizacion IS 'Secretaria de Gobierno, Modernizacion y Transparencia. Fuente oficial consultada: Autoridades municipales publicadas el 15-10-2025.';
COMMENT ON SCHEMA control_urbano_operativo IS 'Secretaria de Control Urbano y Operativo. Preparado para futuras capas municipales.';
COMMENT ON SCHEMA ordenamiento_territorial IS 'Secretaria de Ordenamiento Territorial. Acceso operativo para Tierras, Planeamiento, Ambiente y Minas e Hidrocarburos.';
COMMENT ON SCHEMA infraestructura_osp IS 'Secretaria de Infraestructura, Obras y Servicios Publicos. Acceso operativo para obras, drenaje y reservorios.';
COMMENT ON SCHEMA desarrollo_humano_familia IS 'Secretaria de Desarrollo Humano y Familia. Preparado para futuras capas municipales.';
COMMENT ON SCHEMA mujer_genero_juventud_diversidad IS 'Secretaria de la Mujer, Genero, Juventud y Diversidad. Preparado para futuras capas municipales.';
COMMENT ON SCHEMA cultura IS 'Secretaria de Cultura. Preparado para futuras capas municipales.';
COMMENT ON SCHEMA salud IS 'Secretaria de Salud. Preparado para futuras capas municipales.';
COMMENT ON SCHEMA recaudacion IS 'Secretaria de Recaudacion. Preparado para futuras capas municipales.';
COMMENT ON SCHEMA economia_finanzas IS 'Secretaria de Economia, Finanzas y Control de Gestion. Preparado para futuras capas municipales.';

GRANT USAGE ON SCHEMA
    gobierno_modernizacion,
    control_urbano_operativo,
    ordenamiento_territorial,
    infraestructura_osp,
    desarrollo_humano_familia,
    mujer_genero_juventud_diversidad,
    cultura,
    salud,
    recaudacion,
    economia_finanzas
TO catserver_admin, catserver_read, catserver_publish, catserver_edit;

CREATE OR REPLACE VIEW ordenamiento_territorial.tierras_barrios AS
SELECT *
FROM catastro.barrios;

CREATE OR REPLACE VIEW ordenamiento_territorial.tierras_parcelas AS
SELECT *
FROM catastro.parcelas;

CREATE OR REPLACE VIEW ordenamiento_territorial.tierras_circunscripcion_sector AS
SELECT *
FROM catastro.circunscripcion_sector;

CREATE OR REPLACE VIEW ordenamiento_territorial.planeamiento_zonificacion AS
SELECT *
FROM planeamiento.zonificacion;

CREATE OR REPLACE VIEW ordenamiento_territorial.planeamiento_ejido_urbano AS
SELECT *
FROM catastro.ejido_urbano;

CREATE OR REPLACE VIEW ordenamiento_territorial.tierras_cad_barrios_lineas AS
SELECT *
FROM catastro.cad_barrios_lineas;

CREATE OR REPLACE VIEW ordenamiento_territorial.tierras_cad_barrios_rotulos AS
SELECT *
FROM catastro.cad_barrios_rotulos;

CREATE OR REPLACE VIEW ordenamiento_territorial.tierras_cad_barrios_control AS
SELECT *
FROM catastro.cad_barrios_control;

CREATE OR REPLACE VIEW ordenamiento_territorial.ambiente_pozos_petroleros AS
SELECT *
FROM control_urbano_operativo.pozos_petroleros_ejido;

CREATE OR REPLACE VIEW gobierno_modernizacion.redes_servicios_publicos_lineas AS
SELECT *
FROM ingenieria.lineas;

CREATE OR REPLACE VIEW infraestructura_osp.obras_reservorios_poligonos AS
SELECT *
FROM hidrologia.reservorios_poligonos;

CREATE OR REPLACE VIEW infraestructura_osp.obras_reservorios_barreras AS
SELECT *
FROM hidrologia.reservorios_barreras;

CREATE OR REPLACE VIEW infraestructura_osp.obras_reservorios_nombres AS
SELECT *
FROM hidrologia.reservorios_nombres;

CREATE OR REPLACE VIEW infraestructura_osp.obras_drenaje_cuencas_principales AS
SELECT *
FROM hidrologia.drenaje_cuencas_principales;

CREATE OR REPLACE VIEW infraestructura_osp.obras_drenaje_cuencas_regionales AS
SELECT *
FROM hidrologia.drenaje_cuencas_regionales;

CREATE OR REPLACE VIEW infraestructura_osp.obras_drenaje_lineas AS
SELECT *
FROM hidrologia.drenaje_lineas;

CREATE OR REPLACE VIEW infraestructura_osp.obras_drenaje_puentes AS
SELECT *
FROM hidrologia.drenaje_puentes;

CREATE OR REPLACE VIEW infraestructura_osp.obras_drenaje_etiquetas_fuente AS
SELECT *
FROM hidrologia.drenaje_etiquetas_fuente;

COMMENT ON VIEW ordenamiento_territorial.tierras_barrios IS 'Barrios publicados bajo Secretaria de Ordenamiento Territorial / Tierras.';
COMMENT ON VIEW ordenamiento_territorial.tierras_parcelas IS 'Parcelas publicadas bajo Secretaria de Ordenamiento Territorial / Tierras.';
COMMENT ON VIEW ordenamiento_territorial.tierras_circunscripcion_sector IS 'Circunscripcion y sector bajo Secretaria de Ordenamiento Territorial / Tierras.';
COMMENT ON VIEW ordenamiento_territorial.planeamiento_zonificacion IS 'Zonificacion publicada bajo Secretaria de Ordenamiento Territorial / Planeamiento.';
COMMENT ON VIEW ordenamiento_territorial.planeamiento_ejido_urbano IS 'Ejido urbano publicado bajo Secretaria de Ordenamiento Territorial / Planeamiento.';
COMMENT ON VIEW ordenamiento_territorial.tierras_cad_barrios_lineas IS 'Lineas CAD barriales bajo Secretaria de Ordenamiento Territorial / Tierras.';
COMMENT ON VIEW ordenamiento_territorial.tierras_cad_barrios_rotulos IS 'Rotulos CAD barriales bajo Secretaria de Ordenamiento Territorial / Tierras.';
COMMENT ON VIEW ordenamiento_territorial.tierras_cad_barrios_control IS 'Puntos de control CAD barriales bajo Secretaria de Ordenamiento Territorial / Tierras.';
COMMENT ON VIEW ordenamiento_territorial.ambiente_pozos_petroleros IS 'Pozos petroleros bajo Secretaria de Ordenamiento Territorial / Subsecretaria de Ambiente / Direccion General de Minas e Hidrocarburos.';
COMMENT ON VIEW gobierno_modernizacion.redes_servicios_publicos_lineas IS 'Lineas de redes y servicios publicos bajo Secretaria de Gobierno, Modernizacion y Transparencia.';
COMMENT ON VIEW infraestructura_osp.obras_reservorios_poligonos IS 'Reservorios poligonales bajo Secretaria de Infraestructura, Obras y Servicios Publicos.';
COMMENT ON VIEW infraestructura_osp.obras_reservorios_barreras IS 'Barreras de reservorios bajo Secretaria de Infraestructura, Obras y Servicios Publicos.';
COMMENT ON VIEW infraestructura_osp.obras_reservorios_nombres IS 'Nombres de reservorios bajo Secretaria de Infraestructura, Obras y Servicios Publicos.';
COMMENT ON VIEW infraestructura_osp.obras_drenaje_cuencas_principales IS 'Cuencas principales bajo Secretaria de Infraestructura, Obras y Servicios Publicos.';
COMMENT ON VIEW infraestructura_osp.obras_drenaje_cuencas_regionales IS 'Cuencas regionales bajo Secretaria de Infraestructura, Obras y Servicios Publicos.';
COMMENT ON VIEW infraestructura_osp.obras_drenaje_lineas IS 'Lineas de drenaje bajo Secretaria de Infraestructura, Obras y Servicios Publicos.';
COMMENT ON VIEW infraestructura_osp.obras_drenaje_puentes IS 'Puentes de drenaje bajo Secretaria de Infraestructura, Obras y Servicios Publicos.';
COMMENT ON VIEW infraestructura_osp.obras_drenaje_etiquetas_fuente IS 'Etiquetas fuente de drenaje bajo Secretaria de Infraestructura, Obras y Servicios Publicos.';

GRANT SELECT ON
    ordenamiento_territorial.tierras_barrios,
    ordenamiento_territorial.tierras_parcelas,
    ordenamiento_territorial.tierras_circunscripcion_sector,
    ordenamiento_territorial.planeamiento_zonificacion,
    ordenamiento_territorial.planeamiento_ejido_urbano,
    ordenamiento_territorial.tierras_cad_barrios_lineas,
    ordenamiento_territorial.tierras_cad_barrios_rotulos,
    ordenamiento_territorial.tierras_cad_barrios_control,
    ordenamiento_territorial.ambiente_pozos_petroleros,
    gobierno_modernizacion.redes_servicios_publicos_lineas,
    infraestructura_osp.obras_reservorios_poligonos,
    infraestructura_osp.obras_reservorios_barreras,
    infraestructura_osp.obras_reservorios_nombres,
    infraestructura_osp.obras_drenaje_cuencas_principales,
    infraestructura_osp.obras_drenaje_cuencas_regionales,
    infraestructura_osp.obras_drenaje_lineas,
    infraestructura_osp.obras_drenaje_puentes,
    infraestructura_osp.obras_drenaje_etiquetas_fuente
TO catserver_read, catserver_admin, catserver_publish, catserver_edit;

REVOKE INSERT, UPDATE, DELETE ON
    ordenamiento_territorial.tierras_barrios,
    ordenamiento_territorial.tierras_parcelas,
    ordenamiento_territorial.tierras_circunscripcion_sector,
    ordenamiento_territorial.planeamiento_zonificacion,
    ordenamiento_territorial.planeamiento_ejido_urbano,
    ordenamiento_territorial.tierras_cad_barrios_lineas,
    ordenamiento_territorial.tierras_cad_barrios_rotulos,
    ordenamiento_territorial.tierras_cad_barrios_control,
    ordenamiento_territorial.ambiente_pozos_petroleros,
    gobierno_modernizacion.redes_servicios_publicos_lineas,
    infraestructura_osp.obras_reservorios_poligonos,
    infraestructura_osp.obras_reservorios_barreras,
    infraestructura_osp.obras_reservorios_nombres,
    infraestructura_osp.obras_drenaje_cuencas_principales,
    infraestructura_osp.obras_drenaje_cuencas_regionales,
    infraestructura_osp.obras_drenaje_lineas,
    infraestructura_osp.obras_drenaje_puentes,
    infraestructura_osp.obras_drenaje_etiquetas_fuente
FROM catserver_admin, catserver_publish, catserver_edit;

CREATE OR REPLACE VIEW public.v_catserver_layers_tematicas AS
WITH catalog(load_order, display_name, schema_name, table_name, theme, load_default, writable, notes) AS (
    VALUES
        (10, 'Barrios', 'catastro', 'barrios', 'catastro', true, false, 'Vista operativa oficial de barrios'),
        (20, 'Zonificacion', 'planeamiento', 'zonificacion', 'planeamiento', true, false, 'Vista operativa oficial de zonificacion'),
        (30, 'Parcelas', 'catastro', 'parcelas', 'catastro', true, false, 'Vista operativa oficial de parcelas'),
        (40, 'Circunscripcion y sector', 'catastro', 'circunscripcion_sector', 'catastro', true, false, 'Vista operativa administrativa'),
        (50, 'CAD barrios - lineas', 'catastro', 'cad_barrios_lineas', 'catastro', true, false, 'Lineas CAD barriales dentro del ejido'),
        (60, 'CAD barrios - rotulos', 'catastro', 'cad_barrios_rotulos', 'catastro', true, false, 'Rotulos CAD barriales dentro del ejido'),
        (70, 'Infraestructura - lineas', 'ingenieria', 'lineas', 'ingenieria', true, false, 'Lineas operativas de ingenieria / infraestructura'),
        (80, 'Reservorios - poligonos', 'hidrologia', 'reservorios_poligonos', 'hidrologia', true, false, 'Reservorios poligonales'),
        (90, 'Reservorios - barreras', 'hidrologia', 'reservorios_barreras', 'hidrologia', true, false, 'Barreras de reservorios'),
        (100, 'Reservorios - nombres', 'hidrologia', 'reservorios_nombres', 'hidrologia', true, false, 'Nombres de reservorios'),
        (110, 'Drenaje - cuencas principales', 'hidrologia', 'drenaje_cuencas_principales', 'hidrologia', true, false, 'Cuencas principales filtradas al ejido'),
        (120, 'Drenaje - cuencas regionales', 'hidrologia', 'drenaje_cuencas_regionales', 'hidrologia', true, false, 'Cuencas regionales filtradas al ejido'),
        (130, 'Drenaje - lineas', 'hidrologia', 'drenaje_lineas', 'hidrologia', true, false, 'Lineas de drenaje dentro del ejido'),
        (140, 'Drenaje - puentes', 'hidrologia', 'drenaje_puentes', 'hidrologia', true, false, 'Puentes y alcantarillas dentro del ejido'),
        (150, 'CAD barrios - control', 'catastro', 'cad_barrios_control', 'control', false, false, 'Puntos de control tecnico'),
        (160, 'Ejido urbano - mascara', 'catastro', 'ejido_urbano', 'control', false, false, 'Mascara de ejido urbano'),
        (170, 'Drenaje - etiquetas fuente', 'hidrologia', 'drenaje_etiquetas_fuente', 'control', false, false, 'Etiquetas fuente fuera del ejido'),
        (180, 'Pozos petroleros - ejido', 'ordenamiento_territorial', 'ambiente_pozos_petroleros', 'ambiente', false, false, 'Pozos petroleros del ejido bajo Ambiente / Minas e Hidrocarburos')
)
SELECT
    c.load_order,
    c.display_name,
    c.schema_name,
    c.table_name,
    c.theme,
    c.load_default,
    c.notes,
    COALESCE(NULLIF(gc.type, ''), 'GEOMETRY') AS geometry_type,
    CASE WHEN gc.srid > 0 THEN 'EPSG:' || gc.srid ELSE '' END AS crs_code,
    c.writable
FROM catalog c
JOIN public.geometry_columns gc
    ON gc.f_table_schema = c.schema_name
   AND gc.f_table_name = c.table_name
ORDER BY c.load_order, c.schema_name, c.table_name;

CREATE OR REPLACE VIEW public.v_catserver_layers_municipal AS
WITH catalog(load_order, display_name, schema_name, table_name, secretariat, subsecretariat, direction_general, load_default, writable, notes) AS (
    VALUES
        (10, 'Barrios', 'ordenamiento_territorial', 'tierras_barrios', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', true, false, 'Acceso municipal oficial a barrios'),
        (20, 'Parcelas', 'ordenamiento_territorial', 'tierras_parcelas', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', true, false, 'Acceso municipal oficial a parcelas'),
        (30, 'Circunscripcion y sector', 'ordenamiento_territorial', 'tierras_circunscripcion_sector', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', true, false, 'Referencia administrativa territorial'),
        (40, 'Zonificacion', 'ordenamiento_territorial', 'planeamiento_zonificacion', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Planeamiento', '', true, false, 'Zonificacion urbana'),
        (50, 'Ejido urbano - mascara', 'ordenamiento_territorial', 'planeamiento_ejido_urbano', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Planeamiento', '', false, false, 'Mascara del ejido urbano'),
        (60, 'CAD barrios - lineas', 'ordenamiento_territorial', 'tierras_cad_barrios_lineas', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', true, false, 'Lineas CAD barriales'),
        (70, 'CAD barrios - rotulos', 'ordenamiento_territorial', 'tierras_cad_barrios_rotulos', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', true, false, 'Rotulos CAD barriales'),
        (80, 'CAD barrios - control', 'ordenamiento_territorial', 'tierras_cad_barrios_control', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', false, false, 'Puntos de control tecnico CAD'),
        (90, 'Infraestructura - lineas', 'gobierno_modernizacion', 'redes_servicios_publicos_lineas', 'Secretaria de Gobierno, Modernizacion y Transparencia', 'Subsecretaria de Redes y Servicios Publicos', '', true, false, 'Lineas de redes y servicios publicos'),
        (100, 'Reservorios - poligonos', 'infraestructura_osp', 'obras_reservorios_poligonos', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Infraestructura', '', true, false, 'Reservorios poligonales'),
        (110, 'Reservorios - barreras', 'infraestructura_osp', 'obras_reservorios_barreras', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Infraestructura', '', true, false, 'Barreras de reservorios'),
        (120, 'Reservorios - nombres', 'infraestructura_osp', 'obras_reservorios_nombres', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Infraestructura', '', true, false, 'Nombres de reservorios'),
        (130, 'Drenaje - cuencas principales', 'infraestructura_osp', 'obras_drenaje_cuencas_principales', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Planificacion', '', true, false, 'Cuencas principales'),
        (140, 'Drenaje - cuencas regionales', 'infraestructura_osp', 'obras_drenaje_cuencas_regionales', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Planificacion', '', true, false, 'Cuencas regionales'),
        (150, 'Drenaje - lineas', 'infraestructura_osp', 'obras_drenaje_lineas', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Planificacion', '', true, false, 'Lineas de drenaje'),
        (160, 'Drenaje - puentes', 'infraestructura_osp', 'obras_drenaje_puentes', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Vialidad Urbana', '', true, false, 'Puentes y alcantarillas'),
        (170, 'Drenaje - etiquetas fuente', 'infraestructura_osp', 'obras_drenaje_etiquetas_fuente', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Planificacion', '', false, false, 'Etiquetas fuente de control'),
        (180, 'Pozos petroleros - ejido', 'ordenamiento_territorial', 'ambiente_pozos_petroleros', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Ambiente', 'Direccion General de Minas e Hidrocarburos', false, false, 'Pozos petroleros ubicados dentro del ejido urbano')
)
SELECT
    c.load_order,
    c.display_name,
    c.schema_name,
    c.table_name,
    c.secretariat AS theme,
    c.load_default,
    CASE
        WHEN nullif(c.direction_general, '') IS NOT NULL THEN c.subsecretariat || ' / ' || c.direction_general || '. ' || c.notes
        WHEN nullif(c.subsecretariat, '') IS NOT NULL THEN c.subsecretariat || '. ' || c.notes
        ELSE c.notes
    END AS notes,
    COALESCE(NULLIF(gc.type, ''), 'GEOMETRY') AS geometry_type,
    CASE WHEN gc.srid > 0 THEN 'EPSG:' || gc.srid ELSE '' END AS crs_code,
    c.writable
FROM catalog c
JOIN public.geometry_columns gc
    ON gc.f_table_schema = c.schema_name
   AND gc.f_table_name = c.table_name
ORDER BY c.load_order, c.schema_name, c.table_name;

CREATE OR REPLACE VIEW public.v_catserver_layers AS
SELECT *
FROM public.v_catserver_layers_municipal;

CREATE OR REPLACE VIEW admin.v_catserver_estructura_municipal AS
WITH mapping(display_name, secretariat, subsecretariat, direction_general, municipal_schema, municipal_table, thematic_schema, thematic_table, notes) AS (
    VALUES
        ('Barrios', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', 'ordenamiento_territorial', 'tierras_barrios', 'catastro', 'barrios', 'Mapeo institucional por tierras'),
        ('Parcelas', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', 'ordenamiento_territorial', 'tierras_parcelas', 'catastro', 'parcelas', 'Mapeo institucional por tierras'),
        ('Circunscripcion y sector', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', 'ordenamiento_territorial', 'tierras_circunscripcion_sector', 'catastro', 'circunscripcion_sector', 'Referencia territorial administrativa'),
        ('Zonificacion', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Planeamiento', '', 'ordenamiento_territorial', 'planeamiento_zonificacion', 'planeamiento', 'zonificacion', 'Mapeo institucional por planeamiento'),
        ('Ejido urbano - mascara', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Planeamiento', '', 'ordenamiento_territorial', 'planeamiento_ejido_urbano', 'catastro', 'ejido_urbano', 'Control territorial'),
        ('CAD barrios - lineas', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', 'ordenamiento_territorial', 'tierras_cad_barrios_lineas', 'catastro', 'cad_barrios_lineas', 'CAD barrial institucional'),
        ('CAD barrios - rotulos', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', 'ordenamiento_territorial', 'tierras_cad_barrios_rotulos', 'catastro', 'cad_barrios_rotulos', 'CAD barrial institucional'),
        ('CAD barrios - control', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Tierras', '', 'ordenamiento_territorial', 'tierras_cad_barrios_control', 'catastro', 'cad_barrios_control', 'Control tecnico CAD'),
        ('Infraestructura - lineas', 'Secretaria de Gobierno, Modernizacion y Transparencia', 'Subsecretaria de Redes y Servicios Publicos', '', 'gobierno_modernizacion', 'redes_servicios_publicos_lineas', 'ingenieria', 'lineas', 'Redes y servicios publicos'),
        ('Reservorios - poligonos', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Infraestructura', '', 'infraestructura_osp', 'obras_reservorios_poligonos', 'hidrologia', 'reservorios_poligonos', 'Obras hidraulicas'),
        ('Reservorios - barreras', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Infraestructura', '', 'infraestructura_osp', 'obras_reservorios_barreras', 'hidrologia', 'reservorios_barreras', 'Obras hidraulicas'),
        ('Reservorios - nombres', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Infraestructura', '', 'infraestructura_osp', 'obras_reservorios_nombres', 'hidrologia', 'reservorios_nombres', 'Obras hidraulicas'),
        ('Drenaje - cuencas principales', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Planificacion', '', 'infraestructura_osp', 'obras_drenaje_cuencas_principales', 'hidrologia', 'drenaje_cuencas_principales', 'Planificacion hidrica'),
        ('Drenaje - cuencas regionales', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Planificacion', '', 'infraestructura_osp', 'obras_drenaje_cuencas_regionales', 'hidrologia', 'drenaje_cuencas_regionales', 'Planificacion hidrica'),
        ('Drenaje - lineas', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Planificacion', '', 'infraestructura_osp', 'obras_drenaje_lineas', 'hidrologia', 'drenaje_lineas', 'Planificacion hidrica'),
        ('Drenaje - puentes', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Vialidad Urbana', '', 'infraestructura_osp', 'obras_drenaje_puentes', 'hidrologia', 'drenaje_puentes', 'Infraestructura vial'),
        ('Drenaje - etiquetas fuente', 'Secretaria de Infraestructura, Obras y Servicios Publicos', 'Subsecretaria de Obras / Planificacion', '', 'infraestructura_osp', 'obras_drenaje_etiquetas_fuente', 'hidrologia', 'drenaje_etiquetas_fuente', 'Control tecnico'),
        ('Pozos petroleros - ejido', 'Secretaria de Ordenamiento Territorial', 'Subsecretaria de Ambiente', 'Direccion General de Minas e Hidrocarburos', 'ordenamiento_territorial', 'ambiente_pozos_petroleros', 'ordenamiento_territorial', 'ambiente_pozos_petroleros', 'Pozos petroleros dentro del ejido urbano')
)
SELECT
    display_name,
    secretariat,
    subsecretariat,
    municipal_schema,
    municipal_table,
    thematic_schema,
    thematic_table,
    notes,
    direction_general
FROM mapping
ORDER BY secretariat, subsecretariat, display_name;

GRANT SELECT ON
    public.v_catserver_layers_tematicas,
    public.v_catserver_layers_municipal,
    public.v_catserver_layers,
    admin.v_catserver_estructura_municipal
TO catserver_admin, catserver_read, catserver_publish, catserver_edit;
