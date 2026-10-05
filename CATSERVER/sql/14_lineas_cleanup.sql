\set ON_ERROR_STOP on
\connect catserver

DROP TABLE IF EXISTS infraestructura.lineas_shape_cleanup_excluded;
CREATE TABLE infraestructura.lineas_shape_cleanup_excluded AS
WITH short_same_name_candidates AS (
    SELECT
        s.gid,
        s.nombre,
        ST_Length(s.geom::geography) AS len_m,
        COALESCE(
            MAX(
                ST_Length(
                    ST_Intersection(
                        ST_Buffer(o.geom::geography, 6)::geometry,
                        s.geom
                    )::geography
                ) / NULLIF(ST_Length(s.geom::geography), 0)
            ),
            0
        ) AS cover_ratio
    FROM infraestructura.lineas_shape_src s
    LEFT JOIN infraestructura.lineas_shape_src o
        ON s.gid <> o.gid
       AND s.nombre = o.nombre
       AND ST_DWithin(s.geom::geography, o.geom::geography, 6)
    WHERE ST_Length(s.geom::geography) < 2
    GROUP BY s.gid, s.nombre, s.geom
),
generic_specific_coverage AS (
    SELECT
        g.gid,
        ST_Length(g.geom::geography) AS len_m,
        ST_Length(ST_Intersection(g.geom, u.geom)::geography) / NULLIF(ST_Length(g.geom::geography), 0) AS cover_ratio
    FROM infraestructura.lineas_shape_src g
    CROSS JOIN (
        SELECT ST_UnaryUnion(ST_Collect(ST_Buffer(geom::geography, 10)::geometry)) AS geom
        FROM infraestructura.lineas_shape_src
        WHERE nombre <> 'Lineas Electricas'
    ) u
    WHERE g.nombre = 'Lineas Electricas'
),
reasons AS (
    SELECT
        gid,
        'microtramo_redundante_mismo_nombre'::text AS cleanup_reason,
        len_m,
        cover_ratio
    FROM short_same_name_candidates
    WHERE cover_ratio >= 0.95

    UNION ALL

    SELECT
        gid,
        'linea_generica_cubierta_por_lineas_especificas'::text AS cleanup_reason,
        len_m,
        cover_ratio
    FROM generic_specific_coverage
    WHERE cover_ratio >= 0.95
)
SELECT
    s.*,
    r.cleanup_reason,
    round(r.len_m::numeric, 2) AS length_m,
    round(r.cover_ratio::numeric, 3) AS cover_ratio
FROM infraestructura.lineas_shape_src s
JOIN reasons r
    ON r.gid = s.gid
ORDER BY s.gid;

ALTER TABLE infraestructura.lineas_shape_cleanup_excluded
    ADD PRIMARY KEY (gid);

CREATE INDEX lineas_shape_cleanup_excluded_gix
    ON infraestructura.lineas_shape_cleanup_excluded
    USING gist (geom);

COMMENT ON TABLE infraestructura.lineas_shape_cleanup_excluded IS
    'Elementos excluidos de lineas_shape_clean por limpieza operativa: microtramos redundantes y lineas genericas totalmente cubiertas.';

DROP TABLE IF EXISTS infraestructura.lineas_shape_clean;
CREATE TABLE infraestructura.lineas_shape_clean AS
SELECT
    s.gid,
    s.name,
    s.id_src,
    s.nombre,
    s.observ,
    s.geom
FROM infraestructura.lineas_shape_src s
LEFT JOIN infraestructura.lineas_shape_cleanup_excluded x
    ON x.gid = s.gid
WHERE x.gid IS NULL
ORDER BY s.gid;

ALTER TABLE infraestructura.lineas_shape_clean
    ADD PRIMARY KEY (gid);

CREATE INDEX lineas_shape_clean_gix
    ON infraestructura.lineas_shape_clean
    USING gist (geom);

COMMENT ON TABLE infraestructura.lineas_shape_clean IS
    'Version operativa depurada de lineas_shape_src, sin microtramos redundantes ni lineas genericas completamente cubiertas.';
