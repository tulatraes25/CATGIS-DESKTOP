\set ON_ERROR_STOP on
\connect "CATSERVER"
SET ROLE catserver_owner;

CREATE TABLE IF NOT EXISTS catastro.ejido_municipal (
    ejido_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    import_batch_id bigint REFERENCES admin.import_batch(import_batch_id),
    nombre text NOT NULL DEFAULT 'Ejido Comodoro Rivadavia',
    version_fuente text,
    observaciones text,
    geom geometry(MultiPolygon),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS catastro.barrio (
    barrio_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    import_batch_id bigint REFERENCES admin.import_batch(import_batch_id),
    zona_codigo text,
    barrio_nombre text NOT NULL,
    source_identifier text,
    observaciones text,
    geom geometry(MultiPolygon),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS catastro.parcela (
    parcela_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    import_batch_id bigint REFERENCES admin.import_batch(import_batch_id),
    source_identifier text,
    nomenclatura text,
    circunscripcion text,
    sector text,
    manzana text,
    parcela text,
    partida text,
    observaciones text,
    geom geometry(MultiPolygon),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS catastro.circunscripcion_sector (
    circunscripcion_sector_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    import_batch_id bigint REFERENCES admin.import_batch(import_batch_id),
    circunscripcion text,
    sector text,
    descripcion text,
    geom geometry(MultiPolygon),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS planeamiento.zonificacion_ordenanza (
    zonificacion_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    import_batch_id bigint REFERENCES admin.import_batch(import_batch_id),
    ordenanza text,
    zona_codigo text,
    zonificacion text,
    descripcion text,
    geom geometry(MultiPolygon),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS infraestructura.linea_electrica (
    linea_electrica_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    import_batch_id bigint REFERENCES admin.import_batch(import_batch_id),
    nombre text,
    categoria text,
    observaciones text,
    geom geometry(MultiLineString),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS infraestructura.reservorio (
    reservorio_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    import_batch_id bigint REFERENCES admin.import_batch(import_batch_id),
    nombre text,
    categoria text,
    observaciones text,
    geom geometry(Geometry),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS hidrologia.drenaje (
    drenaje_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    import_batch_id bigint REFERENCES admin.import_batch(import_batch_id),
    nombre text,
    categoria text,
    observaciones text,
    geom geometry(MultiLineString),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ejido_geom_gix ON catastro.ejido_municipal USING gist (geom);
CREATE INDEX IF NOT EXISTS barrio_geom_gix ON catastro.barrio USING gist (geom);
CREATE INDEX IF NOT EXISTS parcela_geom_gix ON catastro.parcela USING gist (geom);
CREATE INDEX IF NOT EXISTS circ_sector_geom_gix ON catastro.circunscripcion_sector USING gist (geom);
CREATE INDEX IF NOT EXISTS zonificacion_geom_gix ON planeamiento.zonificacion_ordenanza USING gist (geom);
CREATE INDEX IF NOT EXISTS linea_electrica_geom_gix ON infraestructura.linea_electrica USING gist (geom);
CREATE INDEX IF NOT EXISTS reservorio_geom_gix ON infraestructura.reservorio USING gist (geom);
CREATE INDEX IF NOT EXISTS drenaje_geom_gix ON hidrologia.drenaje USING gist (geom);

DROP TRIGGER IF EXISTS trg_ejido_updated_at ON catastro.ejido_municipal;
CREATE TRIGGER trg_ejido_updated_at
BEFORE UPDATE ON catastro.ejido_municipal
FOR EACH ROW EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_barrio_updated_at ON catastro.barrio;
CREATE TRIGGER trg_barrio_updated_at
BEFORE UPDATE ON catastro.barrio
FOR EACH ROW EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_parcela_updated_at ON catastro.parcela;
CREATE TRIGGER trg_parcela_updated_at
BEFORE UPDATE ON catastro.parcela
FOR EACH ROW EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_circ_sector_updated_at ON catastro.circunscripcion_sector;
CREATE TRIGGER trg_circ_sector_updated_at
BEFORE UPDATE ON catastro.circunscripcion_sector
FOR EACH ROW EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_zonificacion_updated_at ON planeamiento.zonificacion_ordenanza;
CREATE TRIGGER trg_zonificacion_updated_at
BEFORE UPDATE ON planeamiento.zonificacion_ordenanza
FOR EACH ROW EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_linea_electrica_updated_at ON infraestructura.linea_electrica;
CREATE TRIGGER trg_linea_electrica_updated_at
BEFORE UPDATE ON infraestructura.linea_electrica
FOR EACH ROW EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_reservorio_updated_at ON infraestructura.reservorio;
CREATE TRIGGER trg_reservorio_updated_at
BEFORE UPDATE ON infraestructura.reservorio
FOR EACH ROW EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_drenaje_updated_at ON hidrologia.drenaje;
CREATE TRIGGER trg_drenaje_updated_at
BEFORE UPDATE ON hidrologia.drenaje
FOR EACH ROW EXECUTE FUNCTION admin.set_row_timestamp();

CREATE OR REPLACE VIEW public.v_catalogo_capas AS
SELECT
    lc.layer_catalog_id,
    lc.schema_name,
    lc.table_name,
    lc.logical_name,
    lc.theme,
    lc.geometry_kind,
    lc.canonical_srid,
    lc.status,
    lc.source_priority,
    lc.notes
FROM admin.layer_catalog lc;

CREATE OR REPLACE VIEW public.v_fuentes_registradas AS
SELECT
    sa.source_asset_id,
    sa.source_group,
    sa.source_label,
    sa.source_path,
    sa.source_kind,
    sa.source_status,
    sa.file_size_bytes,
    sa.file_modified_at,
    sa.imported_to_schema,
    sa.imported_table_name
FROM admin.source_asset sa;

GRANT SELECT ON catastro.ejido_municipal, catastro.barrio, catastro.parcela, catastro.circunscripcion_sector
    TO catserver_admin, catserver_read, catserver_publish;

GRANT SELECT ON planeamiento.zonificacion_ordenanza, infraestructura.linea_electrica, infraestructura.reservorio, hidrologia.drenaje
    TO catserver_admin, catserver_read, catserver_publish;

GRANT INSERT, UPDATE, DELETE ON catastro.ejido_municipal, catastro.barrio, catastro.parcela, catastro.circunscripcion_sector
    TO catserver_admin, catserver_edit;

GRANT INSERT, UPDATE, DELETE ON planeamiento.zonificacion_ordenanza, infraestructura.linea_electrica, infraestructura.reservorio, hidrologia.drenaje
    TO catserver_admin, catserver_edit;

GRANT SELECT ON public.v_catalogo_capas, public.v_fuentes_registradas
    TO catserver_admin, catserver_read, catserver_publish;
