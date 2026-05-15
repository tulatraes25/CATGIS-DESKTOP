\set ON_ERROR_STOP on
\connect catserver

CREATE SCHEMA IF NOT EXISTS ingenieria;

COMMENT ON SCHEMA catastro IS 'Capas catastrales operativas y administrativas.';
COMMENT ON SCHEMA planeamiento IS 'Capas de planeamiento y zonificacion urbana.';
COMMENT ON SCHEMA hidrologia IS 'Capas hidrologicas y drenaje urbano.';
COMMENT ON SCHEMA ingenieria IS 'Capas operativas de ingenieria e infraestructura municipal.';
COMMENT ON SCHEMA infraestructura IS 'Esquema fuente legacy con tablas de infraestructura y salidas tecnicas.';

GRANT USAGE ON SCHEMA catastro, planeamiento, hidrologia, ingenieria TO catserver_admin, catserver_read, catserver_publish, catserver_edit;

CREATE OR REPLACE VIEW catastro.barrios AS
SELECT *
FROM catastro.barrios_shape_src;

CREATE OR REPLACE VIEW catastro.parcelas AS
SELECT *
FROM catastro.parcelas_shape_src;

CREATE OR REPLACE VIEW catastro.circunscripcion_sector AS
SELECT *
FROM catastro.circsect_shape_src;

CREATE OR REPLACE VIEW catastro.cad_barrios_lineas AS
SELECT *
FROM catastro.cad_barrios_lineas_ejido;

CREATE OR REPLACE VIEW catastro.cad_barrios_rotulos AS
SELECT *
FROM catastro.cad_barrios_rotulos_ejido;

CREATE OR REPLACE VIEW catastro.cad_barrios_control AS
SELECT *
FROM catastro.cad_barrios_control_ejido;

CREATE OR REPLACE VIEW catastro.ejido_urbano AS
SELECT *
FROM catastro.ejido_urbano_mask;

CREATE OR REPLACE VIEW planeamiento.zonificacion AS
SELECT *
FROM planeamiento.zonif_shape_src;

CREATE OR REPLACE VIEW ingenieria.lineas AS
SELECT *
FROM infraestructura.lineas_shape_operativa;

CREATE OR REPLACE VIEW hidrologia.reservorios_poligonos AS
SELECT *
FROM infraestructura.reservpol_shape_src;

CREATE OR REPLACE VIEW hidrologia.reservorios_barreras AS
SELECT *
FROM infraestructura.reservbar_shape_src;

CREATE OR REPLACE VIEW hidrologia.reservorios_nombres AS
SELECT *
FROM infraestructura.reservnomb_shape_src;

CREATE OR REPLACE VIEW hidrologia.drenaje_cuencas_principales AS
SELECT *
FROM hidrologia.drenaje_cuencas_principales_ejido;

CREATE OR REPLACE VIEW hidrologia.drenaje_cuencas_regionales AS
SELECT *
FROM hidrologia.drenaje_cuencas_regiones_ejido;

CREATE OR REPLACE VIEW hidrologia.drenaje_lineas AS
SELECT *
FROM hidrologia.drenaje_lineas_ejido;

CREATE OR REPLACE VIEW hidrologia.drenaje_puentes AS
SELECT *
FROM hidrologia.drenaje_puentes_ejido;

CREATE OR REPLACE VIEW hidrologia.drenaje_etiquetas_fuente AS
SELECT *
FROM hidrologia.drenaje_etiquetas_src;

COMMENT ON VIEW catastro.barrios IS 'Vista operativa oficial de barrios para CATSERVER.';
COMMENT ON VIEW catastro.parcelas IS 'Vista operativa oficial de parcelas para CATSERVER.';
COMMENT ON VIEW catastro.circunscripcion_sector IS 'Vista operativa de circunscripcion y sector.';
COMMENT ON VIEW catastro.cad_barrios_lineas IS 'Lineas CAD barriales filtradas al ejido urbano.';
COMMENT ON VIEW catastro.cad_barrios_rotulos IS 'Rotulos CAD barriales filtrados al ejido urbano.';
COMMENT ON VIEW catastro.cad_barrios_control IS 'Puntos de control tecnico de CAD barrial.';
COMMENT ON VIEW catastro.ejido_urbano IS 'Mascara operativa del ejido urbano.';
COMMENT ON VIEW planeamiento.zonificacion IS 'Vista operativa oficial de zonificacion.';
COMMENT ON VIEW ingenieria.lineas IS 'Vista operativa oficial de lineas de ingenieria / infraestructura.';
COMMENT ON VIEW hidrologia.reservorios_poligonos IS 'Vista operativa de reservorios poligonales.';
COMMENT ON VIEW hidrologia.reservorios_barreras IS 'Vista operativa de barreras de reservorios.';
COMMENT ON VIEW hidrologia.reservorios_nombres IS 'Vista operativa de nombres de reservorios.';
COMMENT ON VIEW hidrologia.drenaje_cuencas_principales IS 'Vista operativa de cuencas principales dentro del ejido.';
COMMENT ON VIEW hidrologia.drenaje_cuencas_regionales IS 'Vista operativa de cuencas regionales dentro del ejido.';
COMMENT ON VIEW hidrologia.drenaje_lineas IS 'Vista operativa de lineas de drenaje dentro del ejido.';
COMMENT ON VIEW hidrologia.drenaje_puentes IS 'Vista operativa de puentes y alcantarillas dentro del ejido.';
COMMENT ON VIEW hidrologia.drenaje_etiquetas_fuente IS 'Vista de control con etiquetas fuente de drenaje.';

GRANT SELECT ON
    catastro.barrios,
    catastro.parcelas,
    catastro.circunscripcion_sector,
    catastro.cad_barrios_lineas,
    catastro.cad_barrios_rotulos,
    catastro.cad_barrios_control,
    catastro.ejido_urbano,
    planeamiento.zonificacion,
    ingenieria.lineas,
    hidrologia.reservorios_poligonos,
    hidrologia.reservorios_barreras,
    hidrologia.reservorios_nombres,
    hidrologia.drenaje_cuencas_principales,
    hidrologia.drenaje_cuencas_regionales,
    hidrologia.drenaje_lineas,
    hidrologia.drenaje_puentes,
    hidrologia.drenaje_etiquetas_fuente
TO catserver_read, catserver_admin, catserver_publish, catserver_edit;

REVOKE INSERT, UPDATE, DELETE ON
    catastro.barrios,
    catastro.parcelas,
    catastro.circunscripcion_sector,
    catastro.cad_barrios_lineas,
    catastro.cad_barrios_rotulos,
    catastro.cad_barrios_control,
    catastro.ejido_urbano,
    planeamiento.zonificacion,
    ingenieria.lineas,
    hidrologia.reservorios_poligonos,
    hidrologia.reservorios_barreras,
    hidrologia.reservorios_nombres,
    hidrologia.drenaje_cuencas_principales,
    hidrologia.drenaje_cuencas_regionales,
    hidrologia.drenaje_lineas,
    hidrologia.drenaje_puentes,
    hidrologia.drenaje_etiquetas_fuente
FROM catserver_admin, catserver_publish, catserver_edit;

CREATE OR REPLACE VIEW public.v_catserver_layers AS
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
        (170, 'Drenaje - etiquetas fuente', 'hidrologia', 'drenaje_etiquetas_fuente', 'control', false, false, 'Etiquetas fuente fuera del ejido')
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

GRANT SELECT ON public.v_catserver_layers TO catserver_admin, catserver_read, catserver_publish, catserver_edit;
