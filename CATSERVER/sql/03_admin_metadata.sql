\set ON_ERROR_STOP on
\connect "CATSERVER"
SET ROLE catserver_owner;

CREATE OR REPLACE FUNCTION admin.set_row_timestamp()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE TABLE IF NOT EXISTS admin.source_asset (
    source_asset_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    source_group text NOT NULL,
    source_label text NOT NULL,
    source_path text NOT NULL UNIQUE,
    source_kind text NOT NULL,
    source_status text NOT NULL DEFAULT 'registered',
    notes text,
    file_size_bytes bigint,
    file_modified_at timestamptz,
    detected_signature text,
    crs_hint text,
    imported_to_schema text,
    imported_table_name text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT source_asset_kind_chk CHECK (source_kind IN ('dwg', 'kmz', 'kml', 'bak', 'folder', 'other')),
    CONSTRAINT source_asset_status_chk CHECK (source_status IN ('registered', 'profiled', 'imported', 'validated', 'rejected'))
);

CREATE TABLE IF NOT EXISTS admin.import_batch (
    import_batch_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    operator_name text,
    tool_name text,
    target_schema text NOT NULL,
    target_table text NOT NULL,
    source_srid integer,
    target_srid integer,
    geometry_type text,
    records_loaded bigint,
    status text NOT NULL DEFAULT 'started',
    notes text,
    started_at timestamptz NOT NULL DEFAULT now(),
    finished_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT import_batch_status_chk CHECK (status IN ('started', 'finished', 'failed', 'cancelled'))
);

CREATE TABLE IF NOT EXISTS admin.layer_catalog (
    layer_catalog_id bigserial PRIMARY KEY,
    schema_name text NOT NULL,
    table_name text NOT NULL,
    logical_name text NOT NULL,
    theme text NOT NULL,
    geometry_kind text NOT NULL,
    canonical_srid integer,
    status text NOT NULL DEFAULT 'planned',
    source_priority text,
    notes text,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT layer_catalog_unique UNIQUE (schema_name, table_name),
    CONSTRAINT layer_catalog_status_chk CHECK (status IN ('planned', 'raw_only', 'staging', 'active', 'retired'))
);

CREATE TABLE IF NOT EXISTS admin.quality_issue (
    quality_issue_id bigserial PRIMARY KEY,
    source_asset_id uuid REFERENCES admin.source_asset(source_asset_id),
    schema_name text NOT NULL,
    table_name text NOT NULL,
    record_identifier text,
    issue_type text NOT NULL,
    severity text NOT NULL,
    issue_message text NOT NULL,
    resolution_status text NOT NULL DEFAULT 'open',
    detected_at timestamptz NOT NULL DEFAULT now(),
    resolved_at timestamptz,
    resolved_by text,
    notes text,
    CONSTRAINT quality_issue_severity_chk CHECK (severity IN ('low', 'medium', 'high', 'critical')),
    CONSTRAINT quality_issue_resolution_chk CHECK (resolution_status IN ('open', 'accepted', 'fixed', 'ignored'))
);

CREATE TABLE IF NOT EXISTS admin.audit_event (
    audit_event_id bigserial PRIMARY KEY,
    event_at timestamptz NOT NULL DEFAULT now(),
    actor_name text NOT NULL DEFAULT current_user,
    event_type text NOT NULL,
    schema_name text,
    table_name text,
    record_identifier text,
    details jsonb NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS source_asset_group_idx
    ON admin.source_asset (source_group);

CREATE INDEX IF NOT EXISTS source_asset_status_idx
    ON admin.source_asset (source_status);

CREATE INDEX IF NOT EXISTS import_batch_status_idx
    ON admin.import_batch (status);

CREATE INDEX IF NOT EXISTS quality_issue_resolution_idx
    ON admin.quality_issue (resolution_status);

DROP TRIGGER IF EXISTS trg_source_asset_updated_at ON admin.source_asset;
CREATE TRIGGER trg_source_asset_updated_at
BEFORE UPDATE ON admin.source_asset
FOR EACH ROW
EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_import_batch_updated_at ON admin.import_batch;
CREATE TRIGGER trg_import_batch_updated_at
BEFORE UPDATE ON admin.import_batch
FOR EACH ROW
EXECUTE FUNCTION admin.set_row_timestamp();

DROP TRIGGER IF EXISTS trg_layer_catalog_updated_at ON admin.layer_catalog;
CREATE TRIGGER trg_layer_catalog_updated_at
BEFORE UPDATE ON admin.layer_catalog
FOR EACH ROW
EXECUTE FUNCTION admin.set_row_timestamp();

CREATE OR REPLACE VIEW admin.v_pending_sources AS
SELECT
    source_asset_id,
    source_group,
    source_label,
    source_path,
    source_kind,
    source_status,
    imported_to_schema,
    imported_table_name,
    file_modified_at
FROM admin.source_asset
WHERE source_status <> 'validated';

GRANT SELECT ON admin.source_asset, admin.import_batch, admin.layer_catalog, admin.quality_issue, admin.audit_event
    TO catserver_admin, catserver_read;

GRANT INSERT, UPDATE, DELETE ON admin.source_asset, admin.import_batch, admin.layer_catalog, admin.quality_issue, admin.audit_event
    TO catserver_admin;

GRANT SELECT ON admin.v_pending_sources TO catserver_admin, catserver_read, catserver_publish;
