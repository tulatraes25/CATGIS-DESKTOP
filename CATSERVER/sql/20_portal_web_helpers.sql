\set ON_ERROR_STOP on
\connect catserver

CREATE SCHEMA IF NOT EXISTS portal_web;

COMMENT ON SCHEMA portal_web IS 'Funciones y vistas auxiliares para visor web HTML de CATSERVER.';

GRANT USAGE ON SCHEMA portal_web TO catserver_admin, catserver_read, catserver_publish, catserver_edit;

CREATE OR REPLACE VIEW portal_web.v_catalogo_capas AS
SELECT
    l.load_order,
    l.display_name,
    l.schema_name,
    l.table_name,
    COALESCE(m.secretariat, l.theme) AS secretariat,
    COALESCE(m.subsecretariat, '') AS subsecretariat,
    l.geometry_type,
    l.crs_code,
    l.load_default,
    l.writable,
    l.notes
FROM public.v_catserver_layers_municipal l
LEFT JOIN admin.v_catserver_estructura_municipal m
    ON m.display_name = l.display_name
ORDER BY l.load_order, l.schema_name, l.table_name;

COMMENT ON VIEW portal_web.v_catalogo_capas IS 'Catalogo para el visor web, ya ordenado por secretaria y subsecretaria.';

GRANT SELECT ON portal_web.v_catalogo_capas TO catserver_admin, catserver_read, catserver_publish, catserver_edit;

CREATE OR REPLACE FUNCTION portal_web.buscar_barrios(q text, limit_rows integer DEFAULT 15)
RETURNS TABLE(
    tipo text,
    gid bigint,
    titulo text,
    detalle text,
    schema_name text,
    table_name text,
    center_lon double precision,
    center_lat double precision,
    minx double precision,
    miny double precision,
    maxx double precision,
    maxy double precision
)
LANGUAGE sql
STABLE
AS $$
WITH params AS (
    SELECT
        trim(coalesce(q, '')) AS q,
        greatest(1, least(coalesce(limit_rows, 15), 50)) AS lim
)
SELECT
    'barrio'::text AS tipo,
    b.gid,
    b.barrio::text AS titulo,
    concat_ws(' | ',
        nullif(b.zona, ''),
        CASE WHEN b.pob IS NOT NULL THEN 'Pob. ' || b.pob::text END,
        CASE WHEN b.sup_ha IS NOT NULL THEN 'Sup. ' || trim(to_char(b.sup_ha, 'FM999999990.00')) || ' ha' END,
        nullif(b.riesgo, '')
    )::text AS detalle,
    'ordenamiento_territorial'::text AS schema_name,
    'tierras_barrios'::text AS table_name,
    ST_X(ST_PointOnSurface(b.geom)) AS center_lon,
    ST_Y(ST_PointOnSurface(b.geom)) AS center_lat,
    ST_XMin(b.geom) AS minx,
    ST_YMin(b.geom) AS miny,
    ST_XMax(b.geom) AS maxx,
    ST_YMax(b.geom) AS maxy
FROM ordenamiento_territorial.tierras_barrios b
CROSS JOIN params p
WHERE p.q <> ''
  AND (
        b.barrio ILIKE '%' || p.q || '%'
        OR coalesce(b.etiq, '') ILIKE '%' || p.q || '%'
        OR coalesce(b.zona, '') ILIKE '%' || p.q || '%'
      )
ORDER BY
    CASE
        WHEN upper(b.barrio) = upper(p.q) THEN 0
        WHEN upper(coalesce(b.etiq, '')) = upper(p.q) THEN 1
        WHEN upper(b.barrio) LIKE upper(p.q) || '%' THEN 2
        ELSE 3
    END,
    b.barrio
LIMIT (SELECT lim FROM params);
$$;

COMMENT ON FUNCTION portal_web.buscar_barrios(text, integer) IS 'Busqueda de barrios para el visor web. Devuelve centro y bbox para zoom al resultado.';

CREATE OR REPLACE FUNCTION portal_web.buscar_parcelas(q text, limit_rows integer DEFAULT 20)
RETURNS TABLE(
    tipo text,
    gid bigint,
    titulo text,
    detalle text,
    schema_name text,
    table_name text,
    center_lon double precision,
    center_lat double precision,
    minx double precision,
    miny double precision,
    maxx double precision,
    maxy double precision
)
LANGUAGE sql
STABLE
AS $$
WITH params AS (
    SELECT
        trim(coalesce(q, '')) AS q,
        greatest(1, least(coalesce(limit_rows, 20), 50)) AS lim
),
parcelas AS (
    SELECT
        p.*,
        concat_ws('-',
            nullif(p.circun, ''),
            nullif(p.sector, ''),
            nullif(p.num_div, ''),
            nullif(p.num_par, '')
        ) AS nomenclatura_simple
    FROM ordenamiento_territorial.tierras_parcelas p
)
SELECT
    'parcela'::text AS tipo,
    p.gid,
    (
        CASE
            WHEN p.partida IS NOT NULL AND p.partida > 0 THEN 'Partida ' || p.partida::text
            ELSE 'Parcela ' || coalesce(nullif(p.nomenclatura_simple, ''), p.name, p.gid::text)
        END
    )::text AS titulo,
    concat_ws(' | ',
        CASE WHEN p.circun IS NOT NULL OR p.sector IS NOT NULL THEN 'Circ. ' || coalesce(p.circun, '') || ' Sec. ' || coalesce(p.sector, '') END,
        CASE WHEN p.num_div IS NOT NULL THEN 'Div. ' || p.num_div END,
        CASE WHEN p.num_par IS NOT NULL THEN 'Parc. ' || p.num_par END,
        CASE WHEN p.zonific IS NOT NULL AND p.zonific <> '' THEN 'Zonif. ' || p.zonific END,
        CASE WHEN p.uso_suelo IS NOT NULL AND p.uso_suelo <> '' THEN p.uso_suelo END,
        CASE WHEN p.ha IS NOT NULL THEN trim(to_char(p.ha, 'FM999999990.0000')) || ' ha' END
    )::text AS detalle,
    'ordenamiento_territorial'::text AS schema_name,
    'tierras_parcelas'::text AS table_name,
    ST_X(ST_PointOnSurface(p.geom)) AS center_lon,
    ST_Y(ST_PointOnSurface(p.geom)) AS center_lat,
    ST_XMin(p.geom) AS minx,
    ST_YMin(p.geom) AS miny,
    ST_XMax(p.geom) AS maxx,
    ST_YMax(p.geom) AS maxy
FROM parcelas p
CROSS JOIN params x
WHERE x.q <> ''
  AND (
        p.partida::text = x.q
        OR p.partida::text ILIKE x.q || '%'
        OR coalesce(p.nomenclatura_simple, '') ILIKE '%' || x.q || '%'
        OR coalesce(p.codigo, '') ILIKE '%' || x.q || '%'
        OR coalesce(p.name, '') ILIKE '%' || x.q || '%'
      )
ORDER BY
    CASE
        WHEN p.partida::text = x.q THEN 0
        WHEN p.partida::text ILIKE x.q || '%' THEN 1
        WHEN upper(coalesce(p.nomenclatura_simple, '')) = upper(x.q) THEN 2
        ELSE 3
    END,
    p.partida NULLS LAST,
    p.gid
LIMIT (SELECT lim FROM params);
$$;

COMMENT ON FUNCTION portal_web.buscar_parcelas(text, integer) IS 'Busqueda de parcelas para visor web. Prioriza partida exacta y luego coincidencias parciales.';

CREATE OR REPLACE FUNCTION portal_web.buscar_zonificacion(q text, limit_rows integer DEFAULT 15)
RETURNS TABLE(
    tipo text,
    gid bigint,
    titulo text,
    detalle text,
    schema_name text,
    table_name text,
    center_lon double precision,
    center_lat double precision,
    minx double precision,
    miny double precision,
    maxx double precision,
    maxy double precision
)
LANGUAGE sql
STABLE
AS $$
WITH params AS (
    SELECT
        trim(coalesce(q, '')) AS q,
        greatest(1, least(coalesce(limit_rows, 15), 50)) AS lim
)
SELECT
    'zonificacion'::text AS tipo,
    z.gid,
    coalesce(nullif(z.nombre, ''), nullif(z.codigo, ''), nullif(z.cod, ''), 'Zona sin nombre')::text AS titulo,
    concat_ws(' | ',
        CASE WHEN z.codigo IS NOT NULL AND z.codigo <> '' THEN 'Codigo ' || z.codigo END,
        nullif(z.reglament, ''),
        CASE WHEN z.sup_ha IS NOT NULL THEN trim(to_char(z.sup_ha, 'FM999999990.00')) || ' ha' END
    )::text AS detalle,
    'ordenamiento_territorial'::text AS schema_name,
    'planeamiento_zonificacion'::text AS table_name,
    ST_X(ST_PointOnSurface(z.geom)) AS center_lon,
    ST_Y(ST_PointOnSurface(z.geom)) AS center_lat,
    ST_XMin(z.geom) AS minx,
    ST_YMin(z.geom) AS miny,
    ST_XMax(z.geom) AS maxx,
    ST_YMax(z.geom) AS maxy
FROM ordenamiento_territorial.planeamiento_zonificacion z
CROSS JOIN params p
WHERE p.q <> ''
  AND (
        coalesce(z.nombre, '') ILIKE '%' || p.q || '%'
        OR coalesce(z.codigo, '') ILIKE '%' || p.q || '%'
        OR coalesce(z.cod, '') ILIKE '%' || p.q || '%'
      )
ORDER BY
    CASE
        WHEN upper(coalesce(z.nombre, '')) = upper(p.q) THEN 0
        WHEN upper(coalesce(z.codigo, '')) = upper(p.q) THEN 1
        WHEN upper(coalesce(z.nombre, '')) LIKE upper(p.q) || '%' THEN 2
        ELSE 3
    END,
    coalesce(z.nombre, z.codigo, z.cod, z.gid::text)
LIMIT (SELECT lim FROM params);
$$;

COMMENT ON FUNCTION portal_web.buscar_zonificacion(text, integer) IS 'Busqueda de zonas y codigos de zonificacion para visor web.';

CREATE OR REPLACE FUNCTION portal_web.buscar_pozos_petroleros(q text, limit_rows integer DEFAULT 20)
RETURNS TABLE(
    tipo text,
    gid bigint,
    titulo text,
    detalle text,
    schema_name text,
    table_name text,
    center_lon double precision,
    center_lat double precision,
    minx double precision,
    miny double precision,
    maxx double precision,
    maxy double precision
)
LANGUAGE sql
STABLE
AS $$
WITH params AS (
    SELECT
        trim(coalesce(q, '')) AS q,
        greatest(1, least(coalesce(limit_rows, 20), 50)) AS lim
)
SELECT
    'pozo_petrolero'::text AS tipo,
    p.gid,
    coalesce(nullif(p.sigla, ''), nullif(p.nompropio, ''), CASE WHEN p.idpozo IS NOT NULL THEN 'Pozo ' || p.idpozo::text END, 'Pozo sin identificacion')::text AS titulo,
    concat_ws(' | ',
        CASE WHEN p.idpozo IS NOT NULL THEN 'ID ' || p.idpozo::text END,
        nullif(p.area, ''),
        nullif(p.yacimiento, ''),
        nullif(p.tipopozo, ''),
        nullif(p.estpozo, ''),
        nullif(p.clasificacion, ''),
        nullif(p.subclasificacion, '')
    )::text AS detalle,
    'control_urbano_operativo'::text AS schema_name,
    'pozos_petroleros_ejido'::text AS table_name,
    ST_X(p.geom) AS center_lon,
    ST_Y(p.geom) AS center_lat,
    ST_X(p.geom) AS minx,
    ST_Y(p.geom) AS miny,
    ST_X(p.geom) AS maxx,
    ST_Y(p.geom) AS maxy
FROM control_urbano_operativo.pozos_petroleros_ejido p
CROSS JOIN params x
WHERE x.q <> ''
  AND (
        coalesce(p.sigla, '') ILIKE '%' || x.q || '%'
        OR coalesce(p.nompropio, '') ILIKE '%' || x.q || '%'
        OR coalesce(p.area, '') ILIKE '%' || x.q || '%'
        OR coalesce(p.yacimiento, '') ILIKE '%' || x.q || '%'
        OR coalesce(p.tipopozo, '') ILIKE '%' || x.q || '%'
        OR coalesce(p.estpozo, '') ILIKE '%' || x.q || '%'
        OR coalesce(p.idpozo::text, '') ILIKE '%' || x.q || '%'
      )
ORDER BY
    CASE
        WHEN upper(coalesce(p.sigla, '')) = upper(x.q) THEN 0
        WHEN upper(coalesce(p.nompropio, '')) = upper(x.q) THEN 1
        WHEN coalesce(p.idpozo::text, '') = x.q THEN 2
        WHEN upper(coalesce(p.sigla, '')) LIKE upper(x.q) || '%' THEN 3
        ELSE 4
    END,
    coalesce(p.sigla, p.nompropio, p.idpozo::text, p.gid::text)
LIMIT (SELECT lim FROM params);
$$;

COMMENT ON FUNCTION portal_web.buscar_pozos_petroleros(text, integer) IS 'Busqueda de pozos petroleros publicados dentro del ejido urbano.';

CREATE OR REPLACE FUNCTION portal_web.buscar_general(q text, limit_rows integer DEFAULT 20)
RETURNS TABLE(
    tipo text,
    gid bigint,
    titulo text,
    detalle text,
    schema_name text,
    table_name text,
    center_lon double precision,
    center_lat double precision,
    minx double precision,
    miny double precision,
    maxx double precision,
    maxy double precision
)
LANGUAGE sql
STABLE
AS $$
WITH params AS (
    SELECT greatest(1, least(coalesce(limit_rows, 20), 50)) AS lim
),
results AS (
    SELECT * FROM portal_web.buscar_barrios(q, limit_rows)
    UNION ALL
    SELECT * FROM portal_web.buscar_parcelas(q, limit_rows)
    UNION ALL
    SELECT * FROM portal_web.buscar_zonificacion(q, limit_rows)
    UNION ALL
    SELECT * FROM portal_web.buscar_pozos_petroleros(q, limit_rows)
)
SELECT *
FROM results
ORDER BY
    CASE tipo
        WHEN 'parcela' THEN 0
        WHEN 'barrio' THEN 1
        WHEN 'zonificacion' THEN 2
        WHEN 'pozo_petrolero' THEN 3
        ELSE 9
    END,
    titulo
LIMIT (SELECT lim FROM params);
$$;

COMMENT ON FUNCTION portal_web.buscar_general(text, integer) IS 'Busqueda general para el visor web municipal.';

GRANT EXECUTE ON FUNCTION
    portal_web.buscar_barrios(text, integer),
    portal_web.buscar_parcelas(text, integer),
    portal_web.buscar_zonificacion(text, integer),
    portal_web.buscar_pozos_petroleros(text, integer),
    portal_web.buscar_general(text, integer)
TO catserver_admin, catserver_read, catserver_publish, catserver_edit;
