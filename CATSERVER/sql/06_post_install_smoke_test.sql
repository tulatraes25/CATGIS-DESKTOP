\set ON_ERROR_STOP on
\connect "CATSERVER"

SELECT current_database() AS database_name;
SELECT postgis_full_version() AS postgis_version;

SELECT schema_name
FROM information_schema.schemata
WHERE schema_name IN ('raw', 'staging', 'catastro', 'infraestructura', 'planeamiento', 'hidrologia', 'admin', 'public')
ORDER BY schema_name;

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema IN ('admin', 'catastro', 'planeamiento', 'infraestructura', 'hidrologia')
ORDER BY table_schema, table_name;

SELECT source_group, COUNT(*) AS count_rows
FROM admin.source_asset
GROUP BY source_group
ORDER BY source_group;

