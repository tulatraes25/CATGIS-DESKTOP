\set ON_ERROR_STOP on
\connect "CATSERVER"
SET ROLE catserver_owner;

INSERT INTO admin.source_asset (
    source_group,
    source_label,
    source_path,
    source_kind,
    source_status,
    notes,
    file_size_bytes,
    file_modified_at,
    detected_signature
)
VALUES
(
    'barrios_dwg',
    'Carpeta barrios DWG enero 2025',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\2 - DCCION. GRAL. DE CATASTRO ↔ Barrios DWG - A Areas MCR - Enero 2025',
    'folder',
    'registered',
    'Lote principal de DWG por zonas y barrios.',
    NULL,
    '2026-04-13 11:00:28-03',
    NULL
),
(
    'kmz',
    'Carpeta KMZ municipal',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ',
    'folder',
    'registered',
    'Lote de KMZ con capas territoriales listas para primer ETL.',
    NULL,
    '2026-04-13 14:23:31-03',
    NULL
),
(
    'pac',
    'PAC - Ejido Comodoro Rivadavia - Enero 2025.dwg',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\PAC - Ejido Comodoro Rivadavia - Enero 2025.dwg',
    'dwg',
    'registered',
    'Fuente principal esperada para ejido municipal.',
    36732041,
    '2025-04-26 20:59:48-03',
    'AC1032'
),
(
    'pac_backup',
    'PAC - Ejido Comodoro Rivadavia - Enero 2025.bak',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\PAC - Ejido Comodoro Rivadavia - Enero 2025.bak',
    'bak',
    'registered',
    'Backup de AutoCAD detectado por firma de cabecera.',
    36264224,
    '2025-04-26 20:19:50-03',
    'AC1021'
),
(
    'kmz',
    'Circunscripciones - Sectores.kmz',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Circunscripciones - Sectores.kmz',
    'kmz',
    'registered',
    'Fuente candidata a catastro.circunscripcion_sector.',
    48303,
    '2026-04-13 11:23:28-03',
    NULL
),
(
    'kmz',
    'Drenaje Comodoro Rivadavia.kmz',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Drenaje Comodoro Rivadavia.kmz',
    'kmz',
    'registered',
    'Fuente candidata a hidrologia.drenaje.',
    2860016,
    '2026-04-13 11:24:22-03',
    NULL
),
(
    'kmz',
    'Limite de Barrios.kmz',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Limite de Barrios.kmz',
    'kmz',
    'registered',
    'Fuente candidata a catastro.barrio.',
    37824,
    '2026-04-13 11:23:36-03',
    NULL
),
(
    'kmz',
    'Lineas electricas.kmz',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Lineas electricas.kmz',
    'kmz',
    'registered',
    'Fuente candidata a infraestructura.linea_electrica.',
    130219,
    '2026-04-13 11:23:24-03',
    NULL
),
(
    'kmz',
    'Parcelas.kmz',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Parcelas.kmz',
    'kmz',
    'registered',
    'Fuente candidata a catastro.parcela. Contiene doc.kml interno muy grande.',
    8299561,
    '2026-04-13 11:23:56-03',
    NULL
),
(
    'kmz',
    'Reservorios.kmz',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Reservorios.kmz',
    'kmz',
    'registered',
    'Fuente candidata a infraestructura.reservorio.',
    7223,
    '2026-04-13 11:23:20-03',
    NULL
),
(
    'kmz',
    'Zonificacion por Ordenanza.kmz',
    'C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Zonificacion por Ordenanza.kmz',
    'kmz',
    'registered',
    'Fuente candidata a planeamiento.zonificacion_ordenanza.',
    129110,
    '2026-04-13 11:22:50-03',
    NULL
)
ON CONFLICT (source_path) DO NOTHING;

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
('catastro', 'ejido_municipal', 'Ejido municipal', 'catastro', 'MULTIPOLYGON', NULL, 'planned', 'PAC DWG', 'Validar CRS antes de fijar SRID.'),
('catastro', 'barrio', 'Barrios', 'catastro', 'MULTIPOLYGON', NULL, 'planned', 'DWG barrios + KMZ limite de barrios', 'Cruzar DWG por zonas con KMZ municipal.'),
('catastro', 'parcela', 'Parcelas', 'catastro', 'MULTIPOLYGON', 4326, 'planned', 'KMZ Parcelas', 'Importar primero a raw y luego normalizar.'),
('catastro', 'circunscripcion_sector', 'Circunscripciones y sectores', 'catastro', 'MULTIPOLYGON', 4326, 'planned', 'KMZ Circunscripciones - Sectores', 'Homologar atributos de circunscripcion y sector.'),
('planeamiento', 'zonificacion_ordenanza', 'Zonificacion por ordenanza', 'planeamiento', 'MULTIPOLYGON', 4326, 'planned', 'KMZ Zonificacion por Ordenanza', 'Revisar campos de norma y codigo.'),
('infraestructura', 'linea_electrica', 'Lineas electricas', 'infraestructura', 'MULTILINESTRING', 4326, 'planned', 'KMZ Lineas electricas', 'Clasificar por categoria si existe atributo.'),
('infraestructura', 'reservorio', 'Reservorios', 'infraestructura', 'GEOMETRY', 4326, 'planned', 'KMZ Reservorios', 'Puede venir como punto o poligono.'),
('hidrologia', 'drenaje', 'Drenaje', 'hidrologia', 'MULTILINESTRING', 4326, 'planned', 'KMZ Drenaje Comodoro Rivadavia', 'Validar estructura y simplificar si hace falta.')
ON CONFLICT (schema_name, table_name) DO NOTHING;
