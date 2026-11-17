\set ON_ERROR_STOP on
\ir ../_common/assert_catserver_database.sql

-- DC-7/T3: los defaults del rol DDL real (bootstrap) y del owner aplican a
-- tablas nuevas; catserver_etl mantiene raw/staging pero no DDL operativo.
CREATE TABLE catastro.dc7_default_probe (id bigserial PRIMARY KEY, geom geometry(Point, 4326));

DO $dc7_assertions$
BEGIN
    IF NOT has_table_privilege('catserver_read', 'catastro.dc7_default_probe', 'SELECT') THEN
        RAISE EXCEPTION 'DC-7: catserver_read debe heredar SELECT en tablas nuevas';
    END IF;
    IF has_table_privilege('catserver_read', 'catastro.dc7_default_probe', 'INSERT') THEN
        RAISE EXCEPTION 'DC-7: catserver_read no debe heredar INSERT';
    END IF;
    IF NOT has_table_privilege('catserver_edit', 'catastro.dc7_default_probe', 'INSERT') THEN
        RAISE EXCEPTION 'DC-7: catserver_edit debe heredar INSERT en catastro';
    END IF;
    IF NOT has_table_privilege('catserver_admin', 'catastro.dc7_default_probe', 'UPDATE') THEN
        RAISE EXCEPTION 'DC-7: catserver_admin debe heredar UPDATE';
    END IF;
    IF NOT has_schema_privilege('catserver_etl', 'raw', 'CREATE') THEN
        RAISE EXCEPTION 'DC-7: catserver_etl debe poder crear en raw';
    END IF;
    IF has_schema_privilege('catserver_etl', 'catastro', 'CREATE') THEN
        RAISE EXCEPTION 'DC-7: catserver_etl no debe crear en catastro';
    END IF;
END
$dc7_assertions$;

DROP TABLE catastro.dc7_default_probe;
