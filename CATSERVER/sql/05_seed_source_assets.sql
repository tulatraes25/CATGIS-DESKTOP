\set ON_ERROR_STOP on
\connect "CATSERVER"
SET ROLE catserver_owner;

-- Este seed público no registra admin.source_asset.
-- Las rutas, tamaños, fechas y demás metadatos de fuentes son específicos
-- de cada instalación y deben cargarse desde inventarios locales no versionados.
--
-- El archivo conserva únicamente un catálogo temático genérico para dejar
-- preparada la estructura funcional sin publicar detalles operativos.

INSERT INTO admin.layer_catalog (
    schema_name,
    table_name,
    logical_name,
    theme,
    geometry_kind,
    canonical_srid,
    status,
    source_priority,
    notes
)
VALUES
('catastro', 'ejido_municipal', 'Ejido municipal', 'catastro', 'MULTIPOLYGON', NULL, 'planned', 'fuente CAD validada', 'Validar CRS antes de fijar SRID.'),
('catastro', 'barrio', 'Barrios', 'catastro', 'MULTIPOLYGON', NULL, 'planned', 'fuentes territoriales validadas', 'Cruzar fuentes y validar CRS antes de normalizar.'),
('catastro', 'parcela', 'Parcelas', 'catastro', 'MULTIPOLYGON', 4326, 'planned', 'fuente vectorial validada', 'Importar primero a raw y luego normalizar.'),
('catastro', 'circunscripcion_sector', 'Circunscripciones y sectores', 'catastro', 'MULTIPOLYGON', 4326, 'planned', 'fuente vectorial validada', 'Homologar atributos administrativos.'),
('planeamiento', 'zonificacion_ordenanza', 'Zonificacion por ordenanza', 'planeamiento', 'MULTIPOLYGON', 4326, 'planned', 'fuente normativa validada', 'Revisar campos de norma y codigo.'),
('infraestructura', 'linea_electrica', 'Lineas electricas', 'infraestructura', 'MULTILINESTRING', 4326, 'planned', 'fuente vectorial validada', 'Clasificar por categoria cuando exista atributo.'),
('infraestructura', 'reservorio', 'Reservorios', 'infraestructura', 'GEOMETRY', 4326, 'planned', 'fuente vectorial validada', 'Validar tipo geometrico antes de normalizar.'),
('hidrologia', 'drenaje', 'Drenaje', 'hidrologia', 'MULTILINESTRING', 4326, 'planned', 'fuente hidrologica validada', 'Validar estructura y simplificar si hace falta.')
ON CONFLICT (schema_name, table_name) DO NOTHING;
