\set ON_ERROR_STOP on
\connect catserver

CREATE OR REPLACE VIEW public.v_catserver_layers AS
WITH catalog(load_order, display_name, schema_name, table_name, theme, load_default, notes) AS (
    VALUES
        (10, 'Barrios', 'catastro', 'barrios_shape_src', 'catastro', true, 'Base barrial municipal'),
        (20, 'Zonificacion', 'planeamiento', 'zonif_shape_src', 'planeamiento', true, 'Zonificacion desde KMZ'),
        (30, 'Parcelas', 'catastro', 'parcelas_shape_src', 'catastro', true, 'Parcelario municipal'),
        (40, 'Circunscripcion y sector', 'catastro', 'circsect_shape_src', 'catastro', true, 'Referencia administrativa'),
        (50, 'CAD barrios - lineas', 'catastro', 'cad_barrios_lineas_ejido', 'cad', true, 'Capas derivadas de DWG filtradas al ejido'),
        (60, 'CAD barrios - rotulos', 'catastro', 'cad_barrios_rotulos_ejido', 'cad', true, 'Rotulos CAD barriales derivados de DWG'),
        (70, 'Infraestructura - lineas', 'infraestructura', 'lineas_shape_operativa', 'infraestructura', true, 'Lineas de infraestructura depuradas para uso operativo'),
        (80, 'Reservorios - poligonos', 'infraestructura', 'reservpol_shape_src', 'hidrologia', true, 'Reservorios poligonales'),
        (90, 'Reservorios - barreras', 'infraestructura', 'reservbar_shape_src', 'hidrologia', true, 'Barreras de reservorios'),
        (100, 'Reservorios - nombres', 'infraestructura', 'reservnomb_shape_src', 'hidrologia', true, 'Nombres de reservorios'),
        (110, 'Drenaje - cuencas principales', 'hidrologia', 'drenaje_cuencas_principales_ejido', 'hidrologia', true, 'Cuencas principales filtradas al ejido'),
        (120, 'Drenaje - cuencas regionales', 'hidrologia', 'drenaje_cuencas_regiones_ejido', 'hidrologia', true, 'Cuencas regionales filtradas al ejido'),
        (130, 'Drenaje - lineas', 'hidrologia', 'drenaje_lineas_ejido', 'hidrologia', true, 'Lineas de drenaje dentro del ejido'),
        (140, 'Drenaje - puentes', 'hidrologia', 'drenaje_puentes_ejido', 'hidrologia', true, 'Puentes y alcantarillas dentro del ejido'),
        (150, 'CAD barrios - control', 'catastro', 'cad_barrios_control_ejido', 'control', false, 'Puntos de control tecnico'),
        (160, 'Ejido urbano - mascara', 'catastro', 'ejido_urbano_mask', 'control', false, 'Mascara de ejido urbano'),
        (170, 'Drenaje - etiquetas fuente', 'hidrologia', 'drenaje_etiquetas_src', 'control', false, 'Etiquetas fuente fuera del ejido')
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
    has_table_privilege(format('%I.%I', c.schema_name, c.table_name), 'INSERT')
        AND has_table_privilege(format('%I.%I', c.schema_name, c.table_name), 'UPDATE')
        AND has_table_privilege(format('%I.%I', c.schema_name, c.table_name), 'DELETE') AS writable
FROM catalog c
JOIN public.geometry_columns gc
    ON gc.f_table_schema = c.schema_name
   AND gc.f_table_name = c.table_name
ORDER BY c.load_order, c.schema_name, c.table_name;

GRANT SELECT ON public.v_catserver_layers TO catserver_admin, catserver_read, catserver_publish;
