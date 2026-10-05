\set ON_ERROR_STOP on
\connect "CATSERVER"

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS postgis_raster;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE SCHEMA IF NOT EXISTS raw AUTHORIZATION catserver_owner;
CREATE SCHEMA IF NOT EXISTS staging AUTHORIZATION catserver_owner;
CREATE SCHEMA IF NOT EXISTS catastro AUTHORIZATION catserver_owner;
CREATE SCHEMA IF NOT EXISTS infraestructura AUTHORIZATION catserver_owner;
CREATE SCHEMA IF NOT EXISTS planeamiento AUTHORIZATION catserver_owner;
CREATE SCHEMA IF NOT EXISTS hidrologia AUTHORIZATION catserver_owner;
CREATE SCHEMA IF NOT EXISTS admin AUTHORIZATION catserver_owner;

COMMENT ON SCHEMA raw IS 'Aterrizaje inicial de datos fuente.';
COMMENT ON SCHEMA staging IS 'Normalizacion intermedia y control de calidad.';
COMMENT ON SCHEMA catastro IS 'Capas autoritativas catastrales.';
COMMENT ON SCHEMA infraestructura IS 'Capas de infraestructura.';
COMMENT ON SCHEMA planeamiento IS 'Capas normativas y de planeamiento.';
COMMENT ON SCHEMA hidrologia IS 'Capas hidrologicas.';
COMMENT ON SCHEMA admin IS 'Metadata, catalogos, auditoria y control ETL.';
COMMENT ON SCHEMA public IS 'Vistas y objetos listos para publicacion.';

