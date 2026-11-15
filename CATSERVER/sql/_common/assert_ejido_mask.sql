-- CATSERVER: prerrequisito de datos catastro.ejido_urbano_mask (DC-5).
--
-- sql/10, sql/11 y sql/18 consumen esta mascara, pero ningun script del repo la
-- crea: hay que importarla antes (ver CATSERVER/docs/11, seccion 3). Este guard
-- convierte una instalacion sin mascara (o con una mascara incompatible) en un
-- error accionable al inicio del script, en lugar de un "relation does not
-- exist" tardio a mitad de una carga.

DO $catgis_ejido_mask_guard$
DECLARE
    mask_geom_type text;
    mask_srid integer;
BEGIN
    IF to_regclass('catastro.ejido_urbano_mask') IS NULL THEN
        RAISE EXCEPTION
            'CATSERVER: falta catastro.ejido_urbano_mask. Importar la mascara del ejido antes de este script con columnas gid y geom (EPSG:4326); ver CATSERVER/docs/11 seccion 3.';
    END IF;

    SELECT type, srid INTO mask_geom_type, mask_srid
    FROM geometry_columns
    WHERE f_table_schema = 'catastro'
      AND f_table_name = 'ejido_urbano_mask'
      AND f_geometry_column = 'geom';

    IF mask_geom_type IS NULL THEN
        RAISE EXCEPTION
            'CATSERVER: catastro.ejido_urbano_mask no tiene columna de geometria llamada geom (requerida en SRID 4326).';
    END IF;

    IF mask_srid <> 4326 THEN
        RAISE EXCEPTION
            'CATSERVER: catastro.ejido_urbano_mask.geom debe estar en SRID 4326 (SRID actual: %). Reprojectar con ogr2ogr -t_srs EPSG:4326.',
            mask_srid;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'catastro'
          AND table_name = 'ejido_urbano_mask'
          AND column_name = 'gid'
    ) THEN
        RAISE EXCEPTION
            'CATSERVER: catastro.ejido_urbano_mask necesita una columna gid porque sql/13 y sql/18 la publican como capa del visor.';
    END IF;
END
$catgis_ejido_mask_guard$;
