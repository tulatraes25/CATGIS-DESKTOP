\set ON_ERROR_STOP on
\connect catserver

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

REVOKE ALL ON DATABASE catserver FROM PUBLIC;
GRANT CONNECT ON DATABASE catserver TO catserver_admin, catserver_etl, catserver_edit, catserver_read, catserver_publish;

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA admin FROM PUBLIC;
REVOKE ALL ON SCHEMA raw FROM PUBLIC;
REVOKE ALL ON SCHEMA staging FROM PUBLIC;
REVOKE ALL ON SCHEMA catastro FROM PUBLIC;
REVOKE ALL ON SCHEMA infraestructura FROM PUBLIC;
REVOKE ALL ON SCHEMA planeamiento FROM PUBLIC;
REVOKE ALL ON SCHEMA hidrologia FROM PUBLIC;

REVOKE ALL ON SCHEMA admin, raw, staging FROM catserver_read;

GRANT USAGE ON SCHEMA public TO catserver_admin, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA admin TO catserver_admin, catserver_etl;
GRANT USAGE ON SCHEMA raw TO catserver_admin, catserver_etl;
GRANT USAGE ON SCHEMA staging TO catserver_admin, catserver_etl, catserver_edit;
GRANT USAGE ON SCHEMA catastro TO catserver_admin, catserver_edit, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA infraestructura TO catserver_admin, catserver_edit, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA planeamiento TO catserver_admin, catserver_edit, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA hidrologia TO catserver_admin, catserver_edit, catserver_read, catserver_publish;

GRANT CREATE ON SCHEMA public TO catserver_admin;
GRANT CREATE ON SCHEMA admin TO catserver_admin;
GRANT CREATE ON SCHEMA raw TO catserver_admin, catserver_etl;
GRANT CREATE ON SCHEMA staging TO catserver_admin, catserver_etl;
GRANT CREATE ON SCHEMA catastro TO catserver_admin;
GRANT CREATE ON SCHEMA infraestructura TO catserver_admin;
GRANT CREATE ON SCHEMA planeamiento TO catserver_admin;
GRANT CREATE ON SCHEMA hidrologia TO catserver_admin;

GRANT SELECT ON ALL TABLES IN SCHEMA public TO catserver_admin, catserver_read, catserver_publish;
GRANT SELECT ON ALL TABLES IN SCHEMA catastro, infraestructura, planeamiento, hidrologia
    TO catserver_admin, catserver_read, catserver_publish;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA admin, raw, staging, catastro, infraestructura, planeamiento, hidrologia
    TO catserver_admin;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA staging
    TO catserver_edit;

GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA admin, raw, staging, catastro, infraestructura, planeamiento, hidrologia
    TO catserver_admin;

GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA staging
    TO catserver_edit;

REVOKE ALL ON ALL TABLES IN SCHEMA admin, raw, staging FROM catserver_read;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA admin, raw, staging FROM catserver_read;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
    GRANT SELECT ON TABLES TO catserver_admin, catserver_read, catserver_publish;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA admin, raw, staging, catastro, infraestructura, planeamiento, hidrologia
    GRANT ALL PRIVILEGES ON TABLES TO catserver_admin;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA admin, raw, staging, catastro, infraestructura, planeamiento, hidrologia
    GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO catserver_admin;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA catastro, infraestructura, planeamiento, hidrologia
    GRANT SELECT ON TABLES TO catserver_read, catserver_publish;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA staging
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO catserver_edit;

ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA staging
    GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO catserver_edit;
