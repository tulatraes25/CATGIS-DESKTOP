\set ON_ERROR_STOP on

SELECT 'CREATE ROLE catserver_owner NOLOGIN'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'catserver_owner') \gexec

SELECT 'CREATE ROLE catserver_admin NOLOGIN'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'catserver_admin') \gexec

SELECT 'CREATE ROLE catserver_etl NOLOGIN'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'catserver_etl') \gexec

SELECT 'CREATE ROLE catserver_edit NOLOGIN'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'catserver_edit') \gexec

SELECT 'CREATE ROLE catserver_read NOLOGIN'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'catserver_read') \gexec

SELECT 'CREATE ROLE catserver_publish NOLOGIN'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'catserver_publish') \gexec

SELECT 'CREATE DATABASE "CATSERVER" WITH OWNER = catserver_owner ENCODING = ''UTF8'' TEMPLATE = template0'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'CATSERVER') \gexec

COMMENT ON DATABASE "CATSERVER" IS 'Base geoespacial municipal CATSERVER';

-- Usuarios LOGIN sugeridos.
-- Ajustar claves y ejecutar manualmente cuando ya definas operadores reales.
--
-- CREATE ROLE catserver_admin_user LOGIN PASSWORD 'CAMBIAR';
-- GRANT catserver_admin TO catserver_admin_user;
--
-- CREATE ROLE catserver_etl_user LOGIN PASSWORD 'CAMBIAR';
-- GRANT catserver_etl TO catserver_etl_user;
--
-- CREATE ROLE catserver_read_user LOGIN PASSWORD 'CAMBIAR';
-- GRANT catserver_read TO catserver_read_user;

