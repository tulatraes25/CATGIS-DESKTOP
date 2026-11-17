\set ON_ERROR_STOP on
\ir _common/assert_catserver_database.sql

DO $catgis_db_privileges$
BEGIN
    EXECUTE format('REVOKE ALL ON DATABASE %I FROM PUBLIC', current_database());
    EXECUTE format('GRANT CONNECT, TEMPORARY ON DATABASE %I TO catserver_admin, catserver_etl, catserver_edit, catserver_read, catserver_publish', current_database());
END
$catgis_db_privileges$;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;

GRANT USAGE ON SCHEMA admin TO catserver_admin, catserver_etl, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA raw TO catserver_admin, catserver_etl, catserver_read;
GRANT USAGE ON SCHEMA staging TO catserver_admin, catserver_etl, catserver_edit, catserver_read;
GRANT USAGE ON SCHEMA catastro TO catserver_admin, catserver_edit, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA infraestructura TO catserver_admin, catserver_edit, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA planeamiento TO catserver_admin, catserver_edit, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA hidrologia TO catserver_admin, catserver_edit, catserver_read, catserver_publish;
GRANT USAGE ON SCHEMA public TO catserver_admin, catserver_read, catserver_publish;

GRANT CREATE ON SCHEMA raw TO catserver_etl, catserver_admin;
GRANT CREATE ON SCHEMA staging TO catserver_etl, catserver_admin;
GRANT CREATE ON SCHEMA catastro TO catserver_admin;
GRANT CREATE ON SCHEMA infraestructura TO catserver_admin;
GRANT CREATE ON SCHEMA planeamiento TO catserver_admin;
GRANT CREATE ON SCHEMA hidrologia TO catserver_admin;
GRANT CREATE ON SCHEMA admin TO catserver_admin;

ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA admin
    GRANT SELECT ON TABLES TO catserver_admin, catserver_read;

ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA raw
    GRANT SELECT ON TABLES TO catserver_admin, catserver_read;

ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA staging
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit;

ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA catastro
    GRANT SELECT ON TABLES TO catserver_read, catserver_publish;

ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA infraestructura
    GRANT SELECT ON TABLES TO catserver_read, catserver_publish;

ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA planeamiento
    GRANT SELECT ON TABLES TO catserver_read, catserver_publish;

ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA hidrologia
    GRANT SELECT ON TABLES TO catserver_read, catserver_publish;

ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA public
    GRANT SELECT ON TABLES TO catserver_read, catserver_publish;

-- DC-7 (T3): el bootstrap puede crear objetos como superusuario o como
-- catserver_owner. Los ALTER DEFAULT PRIVILEGES anteriores solo cubren a
-- catserver_owner; este bloque aplica la misma politica al rol DDL REAL para que
-- las tablas nuevas hereden los GRANT por rol en cualquier instalacion.
DO $catgis_ddl_role_defaults$
DECLARE
    ddl_role text := current_user;
BEGIN
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA catastro GRANT SELECT ON TABLES TO catserver_read, catserver_publish', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA catastro GRANT INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA infraestructura GRANT SELECT ON TABLES TO catserver_read, catserver_publish', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA infraestructura GRANT INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA planeamiento GRANT SELECT ON TABLES TO catserver_read, catserver_publish', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA planeamiento GRANT INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA hidrologia GRANT SELECT ON TABLES TO catserver_read, catserver_publish', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA hidrologia GRANT INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA raw GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_etl', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA staging GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit, catserver_etl', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA raw GRANT USAGE, SELECT ON SEQUENCES TO catserver_admin, catserver_etl', ddl_role);
    EXECUTE format('ALTER DEFAULT PRIVILEGES FOR ROLE %I IN SCHEMA staging GRANT USAGE, SELECT ON SEQUENCES TO catserver_admin, catserver_edit, catserver_etl', ddl_role);
END
$catgis_ddl_role_defaults$;

-- Rol de import: lectura/escritura en raw/staging, sin DDL en esquemas operativos.
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA raw, staging TO catserver_etl;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA raw, staging TO catserver_etl;

-- DC-7: los scripts 03/04 crean objetos con SET ROLE catserver_owner; los defaults
-- del owner deben cubrir tambien DML por rol (no solo SELECT).
ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA catastro
    GRANT INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit;
ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA infraestructura
    GRANT INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit;
ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA planeamiento
    GRANT INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit;
ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA hidrologia
    GRANT INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit;
ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA raw
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_etl;
ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA staging
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO catserver_admin, catserver_edit, catserver_etl;
ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA raw
    GRANT USAGE, SELECT ON SEQUENCES TO catserver_admin, catserver_etl;
ALTER DEFAULT PRIVILEGES FOR ROLE catserver_owner IN SCHEMA staging
    GRANT USAGE, SELECT ON SEQUENCES TO catserver_admin, catserver_edit, catserver_etl;