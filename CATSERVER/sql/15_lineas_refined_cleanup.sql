\set ON_ERROR_STOP on
\connect catserver

DROP TABLE IF EXISTS infraestructura.lineas_shape_overlap_review;
CREATE TABLE infraestructura.lineas_shape_overlap_review AS
WITH src AS (
    SELECT
        gid,
        nombre,
        ST_Transform(ST_LineMerge(geom), 22182) AS geom_22182
    FROM infraestructura.lineas_shape_clean
),
pairs AS (
    SELECT
        a.gid AS gid_a,
        b.gid AS gid_b,
        a.nombre AS nombre_a,
        b.nombre AS nombre_b,
        ST_Length(a.geom_22182) AS len_a_m,
        ST_Length(b.geom_22182) AS len_b_m,
        ST_HausdorffDistance(a.geom_22182, b.geom_22182) AS haus_m,
        GREATEST(
            ST_Length(ST_Intersection(ST_Buffer(a.geom_22182, 8), b.geom_22182)) / NULLIF(ST_Length(b.geom_22182), 0),
            ST_Length(ST_Intersection(ST_Buffer(b.geom_22182, 8), a.geom_22182)) / NULLIF(ST_Length(a.geom_22182), 0)
        ) AS cover_ratio
    FROM src a
    JOIN src b
        ON a.gid < b.gid
       AND ST_DWithin(a.geom_22182, b.geom_22182, 8)
),
flagged AS (
    SELECT
        gid_a,
        gid_b,
        nombre_a,
        nombre_b,
        len_a_m,
        len_b_m,
        haus_m,
        cover_ratio,
        CASE
            WHEN nombre_a = nombre_b AND haus_m <= 2 AND cover_ratio >= 0.95
                THEN 'duplicado_mismo_nombre_casi_coincidente'
            WHEN nombre_a <> nombre_b AND haus_m <= 2 AND cover_ratio >= 0.95
                THEN 'superposicion_distinto_nombre_revisar'
            ELSE 'cercania_visual_revisar'
        END AS review_type
    FROM pairs
    WHERE (haus_m <= 2 AND cover_ratio >= 0.95)
       OR (haus_m <= 4 AND cover_ratio >= 0.80)
)
SELECT
    row_number() OVER (ORDER BY review_type, gid_a, gid_b)::bigint AS review_id,
    gid_a,
    gid_b,
    nombre_a,
    nombre_b,
    round(len_a_m::numeric, 1) AS len_a_m,
    round(len_b_m::numeric, 1) AS len_b_m,
    round(haus_m::numeric, 2) AS haus_m,
    round(cover_ratio::numeric, 3) AS cover_ratio,
    review_type
FROM flagged
ORDER BY review_type, gid_a, gid_b;

ALTER TABLE infraestructura.lineas_shape_overlap_review
    ADD PRIMARY KEY (review_id);

COMMENT ON TABLE infraestructura.lineas_shape_overlap_review IS
    'Pares de lineas con cercania visual alta en la capa limpia. Sirve para revision de posibles superposiciones o duplicados.';

DROP TABLE IF EXISTS infraestructura.lineas_shape_refined_excluded;
CREATE TABLE infraestructura.lineas_shape_refined_excluded AS
WITH same_name_duplicates AS (
    SELECT
        gid_a,
        gid_b,
        len_a_m,
        len_b_m,
        CASE
            WHEN len_a_m < len_b_m THEN gid_a
            WHEN len_b_m < len_a_m THEN gid_b
            ELSE GREATEST(gid_a, gid_b)
        END AS remove_gid,
        CASE
            WHEN len_a_m < len_b_m THEN gid_b
            WHEN len_b_m < len_a_m THEN gid_a
            ELSE LEAST(gid_a, gid_b)
        END AS keep_gid
    FROM infraestructura.lineas_shape_overlap_review
    WHERE review_type = 'duplicado_mismo_nombre_casi_coincidente'
),
dedup AS (
    SELECT DISTINCT
        remove_gid,
        keep_gid
    FROM same_name_duplicates
)
SELECT
    s.*,
    'duplicado_mismo_nombre_casi_coincidente'::text AS cleanup_reason,
    d.keep_gid
FROM infraestructura.lineas_shape_clean s
JOIN dedup d
    ON d.remove_gid = s.gid
ORDER BY s.gid;

ALTER TABLE infraestructura.lineas_shape_refined_excluded
    ADD PRIMARY KEY (gid);

CREATE INDEX lineas_shape_refined_excluded_gix
    ON infraestructura.lineas_shape_refined_excluded
    USING gist (geom);

COMMENT ON TABLE infraestructura.lineas_shape_refined_excluded IS
    'Elementos excluidos en la segunda pasada de limpieza de lineas: duplicados casi coincidentes con el mismo nombre.';

DROP TABLE IF EXISTS infraestructura.lineas_shape_operativa;
CREATE TABLE infraestructura.lineas_shape_operativa AS
SELECT
    s.gid,
    s.name,
    s.id_src,
    s.nombre,
    s.observ,
    s.geom
FROM infraestructura.lineas_shape_clean s
LEFT JOIN infraestructura.lineas_shape_refined_excluded x
    ON x.gid = s.gid
WHERE x.gid IS NULL
ORDER BY s.gid;

ALTER TABLE infraestructura.lineas_shape_operativa
    ADD PRIMARY KEY (gid);

CREATE INDEX lineas_shape_operativa_gix
    ON infraestructura.lineas_shape_operativa
    USING gist (geom);

COMMENT ON TABLE infraestructura.lineas_shape_operativa IS
    'Version operativa final de lineas: limpieza base mas depuracion de duplicados casi coincidentes con mismo nombre.';
