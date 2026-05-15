\set ON_ERROR_STOP on

DROP TABLE IF EXISTS catastro.barrios_shape_src;
CREATE TABLE catastro.barrios_shape_src AS
SELECT
    ogc_fid::bigint AS gid,
    admin.kml_desc_value(description, 'NOMBRE')::varchar(120) AS barrio,
    admin.kml_desc_value(description, 'ZONA')::varchar(40) AS zona,
    admin.kml_desc_int(description, 'POB') AS pob,
    admin.kml_desc_value(description, 'Etiqueta')::varchar(120) AS etiq,
    admin.kml_desc_value(description, 'FUENTE')::varchar(254) AS fuente,
    admin.kml_desc_num(description, 'sup_ha')::numeric(12,2) AS sup_ha,
    admin.kml_desc_value(description, 'ESTIM_RIES')::varchar(40) AS riesgo,
    ST_Multi(ST_Force2D(wkb_geometry))::geometry(MultiPolygon, 4326) AS geom
FROM raw.barrios_kmz;

ALTER TABLE catastro.barrios_shape_src ADD PRIMARY KEY (gid);
CREATE INDEX barrios_shape_src_gix ON catastro.barrios_shape_src USING gist (geom);

DROP TABLE IF EXISTS catastro.parcelas_shape_src;
CREATE TABLE catastro.parcelas_shape_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(20) AS name,
    admin.kml_desc_int(description, 'FID') AS fid_src,
    admin.kml_desc_value(description, 'DEPARTA')::varchar(20) AS departa,
    admin.kml_desc_int(description, 'EJIDO') AS ejido,
    admin.kml_desc_value(description, 'CIRCUN')::varchar(20) AS circun,
    admin.kml_desc_value(description, 'SECTOR')::varchar(20) AS sector,
    admin.kml_desc_int(description, 'CODIGO_DIV') AS cod_div,
    admin.kml_desc_value(description, 'NUMERO_DIV')::varchar(20) AS num_div,
    admin.kml_desc_value(description, 'NUMERO_PAR')::varchar(20) AS num_par,
    admin.kml_desc_value(description, 'Z_COD_EDIF')::varchar(20) AS zcedif,
    admin.kml_desc_int(description, 'PARTIDA') AS partida,
    admin.kml_desc_value(description, 'EST_LEGAL')::varchar(120) AS est_legal,
    admin.kml_desc_value(description, 'USO_SUELO')::varchar(254) AS uso_suelo,
    admin.kml_desc_value(description, 'Estado_c')::varchar(20) AS estado_c,
    admin.kml_desc_value(description, 'CORRED_URB')::varchar(120) AS corred_urb,
    admin.kml_desc_value(description, 'USOS_ADMI')::varchar(254) AS usos_admi,
    admin.kml_desc_value(description, 'USOS_PRO')::varchar(254) AS usos_pro,
    admin.kml_desc_value(description, 'USO_AT-EX')::varchar(254) AS uso_atex,
    admin.kml_desc_value(description, 'IU_FOS')::varchar(60) AS iu_fos,
    admin.kml_desc_value(description, 'IU_H')::varchar(60) AS iu_h,
    admin.kml_desc_value(description, 'IU_LOTE')::varchar(60) AS iu_lote,
    admin.kml_desc_value(description, 'IU_RETIROS')::varchar(120) AS iu_ret,
    admin.kml_desc_value(description, 'IU_ESTAC')::varchar(120) AS iu_estac,
    admin.kml_desc_value(description, 'LINK')::varchar(254) AS link,
    admin.kml_desc_value(description, 'ZONIFIC')::varchar(120) AS zonific,
    admin.kml_desc_value(description, 'CODIGO')::varchar(60) AS codigo,
    admin.kml_desc_value(description, 'REGLAMENT')::varchar(120) AS reglament,
    admin.kml_desc_value(description, 'ZONAS')::varchar(120) AS zonas,
    admin.kml_desc_value(description, 'CARAC_SECT')::varchar(254) AS carac_sect,
    admin.kml_desc_value(description, 'DENS_HAB')::varchar(120) AS dens_hab,
    admin.kml_desc_value(description, 'USO_AD-PRE')::varchar(254) AS uso_adpre,
    admin.kml_desc_value(description, 'USO_COMPLE')::varchar(254) AS uso_compl,
    admin.kml_desc_value(description, 'SUB_SUP_MI')::varchar(120) AS sub_supmi,
    admin.kml_desc_value(description, 'SUB_SUP_MA')::varchar(120) AS sub_supma,
    admin.kml_desc_value(description, 'SUB_LOT-MI')::varchar(120) AS sub_lotmi,
    admin.kml_desc_value(description, 'MED_AMANZ')::varchar(120) AS med_amanz,
    admin.kml_desc_value(description, 'IN-URB_FOS')::varchar(60) AS inurb_fos,
    admin.kml_desc_value(description, 'IN-URB_FOT')::varchar(60) AS inurb_fot,
    admin.kml_desc_value(description, 'IN-URB_HM')::varchar(60) AS inurb_hm,
    admin.kml_desc_value(description, 'IN-URB_RET')::varchar(120) AS inurb_ret,
    admin.kml_desc_value(description, 'PREV-ESTAC')::varchar(120) AS prev_estac,
    admin.kml_desc_value(description, 'EP-A_CALZ')::varchar(120) AS ep_a_calz,
    admin.kml_desc_value(description, 'EP_A-VERED')::varchar(120) AS ep_a_verd,
    admin.kml_desc_value(description, 'EP_BOULEV')::varchar(120) AS ep_boulev,
    admin.kml_desc_value(description, 'OBSERV')::varchar(254) AS observ,
    admin.kml_desc_num(description, 'HA')::numeric(12,2) AS ha,
    admin.kml_desc_value(description, 'Imagen')::varchar(254) AS imagen,
    admin.kml_desc_int(description, 'ALTURAS') AS alturas,
    ST_Multi(ST_Force2D(wkb_geometry))::geometry(MultiPolygon, 4326) AS geom
FROM raw.parcelas_kmz;

ALTER TABLE catastro.parcelas_shape_src ADD PRIMARY KEY (gid);
CREATE INDEX parcelas_shape_src_gix ON catastro.parcelas_shape_src USING gist (geom);

DROP TABLE IF EXISTS catastro.circsect_shape_src;
CREATE TABLE catastro.circsect_shape_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(120) AS name,
    admin.kml_desc_int(description, 'id') AS id_src,
    admin.kml_desc_value(description, 'Depart')::varchar(40) AS depart,
    admin.kml_desc_int(description, 'Ejido') AS ejido,
    admin.kml_desc_int(description, 'Circunsc') AS circun,
    admin.kml_desc_int(description, 'Sector') AS sector,
    admin.kml_desc_value(description, 'Codigo')::varchar(30) AS codigo,
    admin.kml_desc_value(description, 'Fuente')::varchar(254) AS fuente,
    admin.kml_desc_value(description, 'Referencia')::varchar(120) AS refer,
    ST_Multi(ST_Force2D(wkb_geometry))::geometry(MultiPolygon, 4326) AS geom
FROM raw.circ_sect_kmz;

ALTER TABLE catastro.circsect_shape_src ADD PRIMARY KEY (gid);
CREATE INDEX circsect_shape_src_gix ON catastro.circsect_shape_src USING gist (geom);

DROP TABLE IF EXISTS planeamiento.zonif_shape_src;
CREATE TABLE planeamiento.zonif_shape_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(40) AS name,
    admin.kml_desc_int(description, 'id') AS id_src,
    admin.kml_desc_value(description, 'Depart')::varchar(40) AS depart,
    admin.kml_desc_int(description, 'Ejido') AS ejido,
    admin.kml_desc_value(description, 'Nombre')::varchar(120) AS nombre,
    admin.kml_desc_value(description, 'Codigo')::varchar(30) AS codigo,
    admin.kml_desc_value(description, 'Fuente')::varchar(254) AS fuente,
    admin.kml_desc_value(description, 'COD')::varchar(30) AS cod,
    admin.kml_desc_value(description, 'REGLAMENT')::varchar(120) AS reglament,
    admin.kml_desc_value(description, 'ZONAS')::varchar(120) AS zonas,
    admin.kml_desc_value(description, 'CARAC_SEC')::varchar(254) AS carac_sec,
    admin.kml_desc_value(description, 'DENS_HAB')::varchar(120) AS dens_hab,
    admin.kml_desc_value(description, 'USO_AD-PRE')::varchar(254) AS uso_adpre,
    admin.kml_desc_value(description, 'USO_COMPLE')::varchar(254) AS uso_compl,
    admin.kml_desc_value(description, 'SUB_SUP_MI')::varchar(120) AS sub_supmi,
    admin.kml_desc_value(description, 'SUB_SUP_MA')::varchar(120) AS sub_supma,
    admin.kml_desc_value(description, 'SUB_LOT-MI')::varchar(120) AS sub_lotmi,
    admin.kml_desc_value(description, 'MED_AMANZ')::varchar(120) AS med_amanz,
    admin.kml_desc_value(description, 'IN-URB_FOS')::varchar(60) AS inurb_fos,
    admin.kml_desc_value(description, 'IN-URB_FOT')::varchar(60) AS inurb_fot,
    admin.kml_desc_value(description, 'IN-URB_HM')::varchar(60) AS inurb_hm,
    admin.kml_desc_value(description, 'IN-URB_RET')::varchar(120) AS inurb_ret,
    admin.kml_desc_value(description, 'PREV-ESTAC')::varchar(120) AS prev_estac,
    admin.kml_desc_value(description, 'EP-A_CALZ')::varchar(120) AS ep_a_calz,
    admin.kml_desc_value(description, 'EP_A-VERED')::varchar(120) AS ep_a_verd,
    admin.kml_desc_value(description, 'EP_BOULEV')::varchar(120) AS ep_boulev,
    admin.kml_desc_value(description, 'OBSERV')::varchar(254) AS observ,
    admin.kml_desc_num(description, 'SUP(HA)')::numeric(12,2) AS sup_ha,
    ST_Multi(ST_Force2D(wkb_geometry))::geometry(MultiPolygon, 4326) AS geom
FROM raw.zonif_kmz;

ALTER TABLE planeamiento.zonif_shape_src ADD PRIMARY KEY (gid);
CREATE INDEX zonif_shape_src_gix ON planeamiento.zonif_shape_src USING gist (geom);

DROP TABLE IF EXISTS infraestructura.lineas_shape_src;
CREATE TABLE infraestructura.lineas_shape_src AS
SELECT
    ogc_fid::bigint AS gid,
    name::varchar(120) AS name,
    admin.kml_desc_int(description, 'id') AS id_src,
    admin.kml_desc_value(description, 'Nombre')::varchar(120) AS nombre,
    admin.kml_desc_value(description, 'Observ')::varchar(254) AS observ,
    ST_Multi(ST_Force2D(wkb_geometry))::geometry(MultiLineString, 4326) AS geom
FROM raw.lineas_kmz;

ALTER TABLE infraestructura.lineas_shape_src ADD PRIMARY KEY (gid);
CREATE INDEX lineas_shape_src_gix ON infraestructura.lineas_shape_src USING gist (geom);
