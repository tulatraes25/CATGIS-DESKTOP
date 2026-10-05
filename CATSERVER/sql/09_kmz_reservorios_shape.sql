\set ON_ERROR_STOP on

DROP TABLE IF EXISTS infraestructura.reservpol_shape_src;
CREATE TABLE infraestructura.reservpol_shape_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(120) AS name,
    admin.kml_desc_int(description, 'id') AS id_src,
    admin.kml_desc_value(description, 'nombre')::varchar(120) AS nombre,
    admin.kml_desc_int(description, 'numero') AS numero,
    ST_Multi(ST_Force2D(wkb_geometry))::geometry(MultiPolygon, 4326) AS geom
FROM raw.reserv_poly_kmz;

ALTER TABLE infraestructura.reservpol_shape_src ADD PRIMARY KEY (gid);
CREATE INDEX reservpol_shape_src_gix ON infraestructura.reservpol_shape_src USING gist (geom);

DROP TABLE IF EXISTS infraestructura.reservbar_shape_src;
CREATE TABLE infraestructura.reservbar_shape_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(120) AS name,
    admin.kml_desc_int(description, 'id') AS id_src,
    admin.kml_desc_value(description, 'nombre')::varchar(120) AS nombre,
    admin.kml_desc_int(description, 'numero') AS numero,
    ST_Multi(ST_Force2D(wkb_geometry))::geometry(MultiPolygon, 4326) AS geom
FROM raw.reserv_barr_kmz;

ALTER TABLE infraestructura.reservbar_shape_src ADD PRIMARY KEY (gid);
CREATE INDEX reservbar_shape_src_gix ON infraestructura.reservbar_shape_src USING gist (geom);

DROP TABLE IF EXISTS infraestructura.reservnomb_shape_src;
CREATE TABLE infraestructura.reservnomb_shape_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(120) AS name,
    regexp_replace(name, '\s+', ' ', 'g')::varchar(120) AS nombre,
    ST_Multi(ST_Force2D(wkb_geometry))::geometry(MultiPoint, 4326) AS geom
FROM raw.reserv_nomb_kmz;

ALTER TABLE infraestructura.reservnomb_shape_src ADD PRIMARY KEY (gid);
CREATE INDEX reservnomb_shape_src_gix ON infraestructura.reservnomb_shape_src USING gist (geom);
