\set ON_ERROR_STOP on
\connect "CATSERVER"

REVOKE ALL ON DATABASE "CATSERVER" FROM PUBLIC;
GRANT CONNECT, TEMPORARY ON DATABASE "CATSERVER" TO catserver_admin, catserver_etl, catserver_edit, catserver_read, catserver_publish;

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

