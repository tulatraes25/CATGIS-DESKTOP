\set ON_ERROR_STOP on
\connect catserver

CREATE TABLE IF NOT EXISTS control_urbano_operativo.pozos_petroleros_ejido (
    gid bigint PRIMARY KEY,
    gid_fuente bigint,
    area text,
    yacimiento text,
    idpozo bigint,
    sigla text,
    nompropio text,
    vidautil numeric,
    sistextrac text,
    estpozo text,
    tipopozo text,
    clasificacion text,
    subclasificacion text,
    cota numeric,
    profundidad numeric,
    latitud double precision,
    longitud double precision,
    x_posgar94 double precision,
    y_posgar94 double precision,
    x_pcast double precision,
    y_pcast double precision,
    x_inch double precision,
    y_inch double precision,
    ult_civ text,
    fk_area bigint,
    fk_yacim bigint,
    fech_aba text,
    geom geometry(Point, 4326)
);

TRUNCATE TABLE control_urbano_operativo.pozos_petroleros_ejido;

INSERT INTO control_urbano_operativo.pozos_petroleros_ejido (
    gid,
    gid_fuente,
    area,
    yacimiento,
    idpozo,
    sigla,
    nompropio,
    vidautil,
    sistextrac,
    estpozo,
    tipopozo,
    clasificacion,
    subclasificacion,
    cota,
    profundidad,
    latitud,
    longitud,
    x_posgar94,
    y_posgar94,
    x_pcast,
    y_pcast,
    x_inch,
    y_inch,
    ult_civ,
    fk_area,
    fk_yacim,
    fech_aba,
    geom
)
SELECT
    row_number() OVER (ORDER BY COALESCE(p.idpozo, 0), COALESCE(p.gid, 0), COALESCE(p.sigla, ''))::bigint AS gid,
    p.gid::bigint AS gid_fuente,
    nullif(trim(p.area), '') AS area,
    nullif(trim(p.yacimiento), '') AS yacimiento,
    p.idpozo::bigint AS idpozo,
    nullif(trim(p.sigla), '') AS sigla,
    nullif(trim(p.nompropio), '') AS nompropio,
    p.vidautil::numeric AS vidautil,
    nullif(trim(p.sistextrac), '') AS sistextrac,
    nullif(trim(p.estpozo), '') AS estpozo,
    nullif(trim(p.tipopozo), '') AS tipopozo,
    nullif(trim(p.clasificacion), '') AS clasificacion,
    nullif(trim(p.subclasificacion), '') AS subclasificacion,
    p.cota::numeric AS cota,
    p.profundidad::numeric AS profundidad,
    p.latitud::double precision AS latitud,
    p.longitud::double precision AS longitud,
    p.x_posgar94::double precision AS x_posgar94,
    p.y_posgar94::double precision AS y_posgar94,
    p.x_pcast::double precision AS x_pcast,
    p.y_pcast::double precision AS y_pcast,
    p.x_inch::double precision AS x_inch,
    p.y_inch::double precision AS y_inch,
    nullif(trim(p.ult_civ), '') AS ult_civ,
    p.fk_area::bigint AS fk_area,
    p.fk_yacim::bigint AS fk_yacim,
    nullif(trim(p.fech_aba), '') AS fech_aba,
    ST_Force2D(
        CASE
            WHEN GeometryType(ST_MakeValid(p.geom)) = 'POINT' THEN ST_MakeValid(p.geom)
            ELSE ST_PointOnSurface(ST_MakeValid(p.geom))
        END
    )::geometry(Point, 4326)
FROM raw.pozos_petroleros_cr_kml p
JOIN ordenamiento_territorial.planeamiento_ejido_urbano e
  ON ST_Intersects(p.geom, e.geom)
WHERE p.geom IS NOT NULL;

CREATE INDEX IF NOT EXISTS pozos_petroleros_ejido_geom_gix
    ON control_urbano_operativo.pozos_petroleros_ejido
    USING gist (geom);

CREATE INDEX IF NOT EXISTS pozos_petroleros_ejido_sigla_idx
    ON control_urbano_operativo.pozos_petroleros_ejido (sigla);

CREATE INDEX IF NOT EXISTS pozos_petroleros_ejido_idpozo_idx
    ON control_urbano_operativo.pozos_petroleros_ejido (idpozo);

CREATE INDEX IF NOT EXISTS pozos_petroleros_ejido_area_idx
    ON control_urbano_operativo.pozos_petroleros_ejido (area);

COMMENT ON TABLE control_urbano_operativo.pozos_petroleros_ejido IS
    'Pozos petroleros importados desde pozos_cr.kml y filtrados al ejido urbano de Comodoro Rivadavia.';

COMMENT ON COLUMN control_urbano_operativo.pozos_petroleros_ejido.gid_fuente IS
    'Identificador original informado en la fuente KML.';

GRANT SELECT ON control_urbano_operativo.pozos_petroleros_ejido
TO catserver_read, catserver_admin, catserver_publish, catserver_edit;
