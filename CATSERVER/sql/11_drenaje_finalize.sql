\set ON_ERROR_STOP on

CREATE SCHEMA IF NOT EXISTS hidrologia;

DROP TABLE IF EXISTS hidrologia.drenaje_lineas_src;
CREATE TABLE hidrologia.drenaje_lineas_src AS
SELECT
    row_number() OVER ()::bigint AS gid,
    src.source_layer,
    src.name::varchar(120) AS name,
    ST_Multi(ST_Force2D(src.geom))::geometry(MultiLineString, 4326) AS geom
FROM (
    SELECT 'Perdido' AS source_layer, name, wkb_geometry AS geom FROM raw.drenaje_perdido_kmz
    UNION ALL
    SELECT 'Mosconi', name, wkb_geometry FROM raw.drenaje_mosconi_kmz
    UNION ALL
    SELECT 'Quintana', name, wkb_geometry FROM raw.drenaje_quintana_kmz
    UNION ALL
    SELECT 'Cursos Belgrano', name, wkb_geometry FROM raw.drenaje_cursos_belgrano_kmz
    UNION ALL
    SELECT 'Ramon Santos Norte', name, wkb_geometry FROM raw.drenaje_ramon_santos_norte_kmz
    UNION ALL
    SELECT 'Cursos RS sur', name, wkb_geometry FROM raw.drenaje_cursos_rs_sur_kmz
    UNION ALL
    SELECT 'Cursos RT', name, wkb_geometry FROM raw.drenaje_cursos_rt_kmz
    UNION ALL
    SELECT 'Cursos Km 8', name, wkb_geometry FROM raw.drenaje_cursos_km8_kmz
    UNION ALL
    SELECT 'cursos principales', name, wkb_geometry FROM raw.drenaje_cursos_principales_kmz
) AS src;

ALTER TABLE hidrologia.drenaje_lineas_src ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_lineas_src_gix ON hidrologia.drenaje_lineas_src USING gist (geom);
CREATE INDEX drenaje_lineas_src_layer_idx ON hidrologia.drenaje_lineas_src (source_layer);

DROP TABLE IF EXISTS hidrologia.drenaje_lineas_ejido;
CREATE TABLE hidrologia.drenaje_lineas_ejido AS
WITH clipped AS (
    SELECT
        row_number() OVER ()::bigint AS gid,
        s.source_layer,
        s.name,
        ST_Multi(
            ST_CollectionExtract(
                ST_Intersection(s.geom, m.geom),
                2
            )
        )::geometry(MultiLineString, 4326) AS geom
    FROM hidrologia.drenaje_lineas_src s
    JOIN catastro.ejido_urbano_mask m
        ON ST_Intersects(s.geom, m.geom)
)
SELECT *
FROM clipped
WHERE NOT ST_IsEmpty(geom);

ALTER TABLE hidrologia.drenaje_lineas_ejido ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_lineas_ejido_gix ON hidrologia.drenaje_lineas_ejido USING gist (geom);
CREATE INDEX drenaje_lineas_ejido_layer_idx ON hidrologia.drenaje_lineas_ejido (source_layer);

DROP TABLE IF EXISTS hidrologia.drenaje_cuencas_principales_src;
CREATE TABLE hidrologia.drenaje_cuencas_principales_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(120) AS name,
    ST_Multi(
        ST_CollectionExtract(
            ST_MakeValid(ST_Force2D(wkb_geometry)),
            3
        )
    )::geometry(MultiPolygon, 4326) AS geom
FROM raw.drenaje_cuencas_principales_kmz;

ALTER TABLE hidrologia.drenaje_cuencas_principales_src ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_cuencas_principales_src_gix ON hidrologia.drenaje_cuencas_principales_src USING gist (geom);

DROP TABLE IF EXISTS hidrologia.drenaje_cuencas_principales_ejido;
CREATE TABLE hidrologia.drenaje_cuencas_principales_ejido AS
WITH clipped AS (
    SELECT
        row_number() OVER ()::bigint AS gid,
        s.name,
        ST_Multi(
            ST_CollectionExtract(
                ST_MakeValid(ST_Intersection(s.geom, m.geom)),
                3
            )
        )::geometry(MultiPolygon, 4326) AS geom
    FROM hidrologia.drenaje_cuencas_principales_src s
    JOIN catastro.ejido_urbano_mask m
        ON ST_Intersects(s.geom, m.geom)
)
SELECT *
FROM clipped
WHERE NOT ST_IsEmpty(geom);

ALTER TABLE hidrologia.drenaje_cuencas_principales_ejido ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_cuencas_principales_ejido_gix ON hidrologia.drenaje_cuencas_principales_ejido USING gist (geom);

DROP TABLE IF EXISTS hidrologia.drenaje_cuencas_regiones_src;
CREATE TABLE hidrologia.drenaje_cuencas_regiones_src AS
SELECT
    row_number() OVER ()::bigint AS gid,
    src.region_group,
    src.name::varchar(120) AS name,
    ST_Multi(
        ST_CollectionExtract(
            ST_MakeValid(ST_Force2D(src.geom)),
            3
        )
    )::geometry(MultiPolygon, 4326) AS geom
FROM (
    SELECT 'Norte' AS region_group, name, wkb_geometry AS geom FROM raw.drenaje_cuencas_norte_kmz
    UNION ALL
    SELECT 'Centro - Belgrano', name, wkb_geometry FROM raw.drenaje_cuencas_centro_belgrano_kmz
    UNION ALL
    SELECT 'Arroyo La Mata', name, wkb_geometry FROM raw.drenaje_cuencas_arroyo_lamata_kmz
    UNION ALL
    SELECT 'Sur', name, wkb_geometry FROM raw.drenaje_cuencas_sur_kmz
) AS src;

ALTER TABLE hidrologia.drenaje_cuencas_regiones_src ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_cuencas_regiones_src_gix ON hidrologia.drenaje_cuencas_regiones_src USING gist (geom);
CREATE INDEX drenaje_cuencas_regiones_src_group_idx ON hidrologia.drenaje_cuencas_regiones_src (region_group);

DROP TABLE IF EXISTS hidrologia.drenaje_cuencas_regiones_ejido;
CREATE TABLE hidrologia.drenaje_cuencas_regiones_ejido AS
WITH clipped AS (
    SELECT
        row_number() OVER ()::bigint AS gid,
        s.region_group,
        s.name,
        ST_Multi(
            ST_CollectionExtract(
                ST_MakeValid(ST_Intersection(s.geom, m.geom)),
                3
            )
        )::geometry(MultiPolygon, 4326) AS geom
    FROM hidrologia.drenaje_cuencas_regiones_src s
    JOIN catastro.ejido_urbano_mask m
        ON ST_Intersects(s.geom, m.geom)
)
SELECT *
FROM clipped
WHERE NOT ST_IsEmpty(geom);

ALTER TABLE hidrologia.drenaje_cuencas_regiones_ejido ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_cuencas_regiones_ejido_gix ON hidrologia.drenaje_cuencas_regiones_ejido USING gist (geom);
CREATE INDEX drenaje_cuencas_regiones_ejido_group_idx ON hidrologia.drenaje_cuencas_regiones_ejido (region_group);

DROP TABLE IF EXISTS hidrologia.drenaje_puentes_src;
CREATE TABLE hidrologia.drenaje_puentes_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(120) AS name,
    ST_Force2D(ST_GeometryN(wkb_geometry, 1))::geometry(Point, 4326) AS geom
FROM raw.drenaje_puentes_alcantarillas_kmz;

ALTER TABLE hidrologia.drenaje_puentes_src ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_puentes_src_gix ON hidrologia.drenaje_puentes_src USING gist (geom);

DROP TABLE IF EXISTS hidrologia.drenaje_puentes_ejido;
CREATE TABLE hidrologia.drenaje_puentes_ejido AS
SELECT
    row_number() OVER ()::bigint AS gid,
    s.name,
    s.geom
FROM hidrologia.drenaje_puentes_src s
JOIN catastro.ejido_urbano_mask m
    ON ST_Intersects(s.geom, m.geom);

ALTER TABLE hidrologia.drenaje_puentes_ejido ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_puentes_ejido_gix ON hidrologia.drenaje_puentes_ejido USING gist (geom);

DROP TABLE IF EXISTS hidrologia.drenaje_etiquetas_src;
CREATE TABLE hidrologia.drenaje_etiquetas_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(120) AS name,
    ST_Force2D(ST_GeometryN(wkb_geometry, 1))::geometry(Point, 4326) AS geom
FROM raw.drenaje_etiquetas_kmz;

ALTER TABLE hidrologia.drenaje_etiquetas_src ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_etiquetas_src_gix ON hidrologia.drenaje_etiquetas_src USING gist (geom);

DROP TABLE IF EXISTS hidrologia.drenaje_etiquetas_ejido;
CREATE TABLE hidrologia.drenaje_etiquetas_ejido AS
SELECT
    row_number() OVER ()::bigint AS gid,
    s.name,
    s.geom
FROM hidrologia.drenaje_etiquetas_src s
JOIN catastro.ejido_urbano_mask m
    ON ST_Intersects(s.geom, m.geom);

ALTER TABLE hidrologia.drenaje_etiquetas_ejido ADD PRIMARY KEY (gid);
CREATE INDEX drenaje_etiquetas_ejido_gix ON hidrologia.drenaje_etiquetas_ejido USING gist (geom);
