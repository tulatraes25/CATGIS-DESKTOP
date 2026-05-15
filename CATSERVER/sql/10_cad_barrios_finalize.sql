\set ON_ERROR_STOP on

CREATE SCHEMA IF NOT EXISTS staging;

DROP TABLE IF EXISTS catastro.cad_barrios_lineas_ejido;
CREATE TABLE catastro.cad_barrios_lineas_ejido AS
WITH clipped AS (
    SELECT
        row_number() OVER ()::bigint AS gid,
        s.source_file,
        s.barrio_key,
        s.export_group,
        s.layer,
        s.paperspace,
        s.subclasses,
        s.linetype,
        s.entityhand,
        s.text,
        ST_Multi(
            ST_CollectionExtract(
                ST_Intersection(s.geom, m.geom),
                2
            )
        )::geometry(MultiLineString, 4326) AS geom
    FROM staging.cad_barrios_lineas_src s
    JOIN catastro.ejido_urbano_mask m
        ON ST_Intersects(s.geom, m.geom)
)
SELECT *
FROM clipped
WHERE NOT ST_IsEmpty(geom);

ALTER TABLE catastro.cad_barrios_lineas_ejido ADD PRIMARY KEY (gid);
CREATE INDEX cad_barrios_lineas_ejido_gix ON catastro.cad_barrios_lineas_ejido USING gist (geom);
CREATE INDEX cad_barrios_lineas_ejido_barrio_idx ON catastro.cad_barrios_lineas_ejido (barrio_key);

DROP TABLE IF EXISTS catastro.cad_barrios_rotulos_ejido;
CREATE TABLE catastro.cad_barrios_rotulos_ejido AS
SELECT
    row_number() OVER ()::bigint AS gid,
    s.source_file,
    s.barrio_key,
    s.export_group,
    s.layer,
    s.paperspace,
    s.subclasses,
    s.linetype,
    s.entityhand,
    s.text,
    ST_Force2D(s.geom)::geometry(Point, 4326) AS geom
FROM staging.cad_barrios_rotulos_src s
JOIN catastro.ejido_urbano_mask m
    ON ST_Intersects(s.geom, m.geom);

ALTER TABLE catastro.cad_barrios_rotulos_ejido ADD PRIMARY KEY (gid);
CREATE INDEX cad_barrios_rotulos_ejido_gix ON catastro.cad_barrios_rotulos_ejido USING gist (geom);
CREATE INDEX cad_barrios_rotulos_ejido_barrio_idx ON catastro.cad_barrios_rotulos_ejido (barrio_key);

DROP TABLE IF EXISTS catastro.cad_barrios_control_ejido;
CREATE TABLE catastro.cad_barrios_control_ejido AS
SELECT
    row_number() OVER ()::bigint AS gid,
    s.source_file,
    s.barrio_key,
    s.export_group,
    s.layer,
    s.paperspace,
    s.subclasses,
    s.linetype,
    s.entityhand,
    s.text,
    ST_Force2D(s.geom)::geometry(Point, 4326) AS geom
FROM staging.cad_barrios_control_src s
JOIN catastro.ejido_urbano_mask m
    ON ST_Intersects(s.geom, m.geom);

ALTER TABLE catastro.cad_barrios_control_ejido ADD PRIMARY KEY (gid);
CREATE INDEX cad_barrios_control_ejido_gix ON catastro.cad_barrios_control_ejido USING gist (geom);
CREATE INDEX cad_barrios_control_ejido_barrio_idx ON catastro.cad_barrios_control_ejido (barrio_key);
