\set ON_ERROR_STOP on
\ir ../_common/assert_catserver_database.sql

-- DC-6/T2: la tabla canonica legada debe ser privada (_raw) y la vista
-- canonica debe crearse sin colision e idempotentemente.
CREATE TABLE IF NOT EXISTS catastro.circsect_shape_src (
    gid bigint,
    circunscripcion text,
    sector text,
    geom geometry(MultiPolygon, 4326)
);

CREATE OR REPLACE VIEW catastro.circunscripcion_sector AS
SELECT * FROM catastro.circsect_shape_src;
CREATE OR REPLACE VIEW catastro.circunscripcion_sector AS
SELECT * FROM catastro.circsect_shape_src;

DO $dc6_assertions$
DECLARE
    view_kind "char";
    raw_exists boolean;
BEGIN
    SELECT c.relkind INTO view_kind
    FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'catastro' AND c.relname = 'circunscripcion_sector';
    IF view_kind IS DISTINCT FROM 'v' THEN
        RAISE EXCEPTION 'DC-6: circunscripcion_sector debe ser vista (relkind actual: %)', view_kind;
    END IF;

    SELECT EXISTS (
        SELECT 1
        FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE n.nspname = 'catastro'
          AND c.relname = 'circunscripcion_sector_raw'
          AND c.relkind = 'r'
    ) INTO raw_exists;
    IF NOT raw_exists THEN
        RAISE EXCEPTION 'DC-6: falta la tabla cruda circunscripcion_sector_raw';
    END IF;

    IF NOT has_table_privilege('catserver_read', 'catastro.circunscripcion_sector', 'SELECT') THEN
        RAISE EXCEPTION 'DC-6: catserver_read debe poder leer la vista canonica';
    END IF;
    IF has_table_privilege('catserver_read', 'catastro.circunscripcion_sector_raw', 'SELECT') THEN
        RAISE EXCEPTION 'DC-6: la tabla cruda no debe exponerse a catserver_read';
    END IF;
END
$dc6_assertions$;

DROP VIEW IF EXISTS catastro.circunscripcion_sector;
DROP TABLE IF EXISTS catastro.circsect_shape_src;
DROP TABLE IF EXISTS catastro.circunscripcion_sector_raw CASCADE;
