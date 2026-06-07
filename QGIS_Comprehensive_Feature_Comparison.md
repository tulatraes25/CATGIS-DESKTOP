# QGIS 3.x Comprehensive Feature Reference

## 1. Processing Toolbox Providers & Algorithm Counts

### Native QGIS Provider (~230 algorithms across 26 categories)
- **3D Tiles**: Export to 3D Tiles
- **Cartography**: atlas features, overview, layout map extraction, smooth/pixelize
- **Database**: execute SQL, import/export PostGIS/GeoPackage
- **File Tools**: unzip, file metadata
- **GPS**: GPSBabel import/export
- **Interpolation**: IDW, TIN interpolation, rasterize
- **Layer Tools**: export to spreadsheet, field statistics, set layer style
- **Mesh**: export mesh to raster, import as overlay, contour generation
- **Modeler Tools**: preconfigured algorithms
- **Network Analysis**: service area (lines/points), shortest path, distance matrix, centroid, travel time
- **Plots**: scatter plot, histogram, bar plot
- **Point Cloud Conversion**: convert format, export, thin, merge, filter, info
- **Point Cloud Data Management**: build VPC, tile, index, info
- **Point Cloud Extraction**: clip, split by class, filter by bounds/attribute
- **Raster Analysis**: cell statistics, zonal stats, proximity, slope, aspect, hillshade, ruggedness, TRI, curvature, reclassify, equalize, sample values
- **Raster Creation**: constant raster, random raster
- **Raster Terrain Analysis**: slope, aspect, hillshade, ruggedness, TRI, roughness, curvature
- **Raster Tools**: clip, merge, warp, proximity, aspect, contour, calculator, tileindex
- **Vector Analysis**: line intersections, nearest hub, sum line lengths, points in polygon, statistics by category, weighted sum, Z statistics
- **Vector Creation**: random points in extent/polygon/layer, regular points, grid, centroids, convert geometry type, merge/split lines, Voronoi/Delaunay polygons, offsets
- **Vector General**: merge layers, join attributes, fields calculator, add field, refactor fields, reorder fields, delete field, subset selection, import HTML, create layer from extent
- **Vector Geometry**: bounding boxes, centroids, concave/convex hull, simplify/densify, smooth, buffer, clip/difference/intersect, union, dissolve, add geometry attributes, check validity, minimum enclosing geometry, move/rotate/scale/flip, reverse line, convert to curved, offset curve, merge/collect lines, split with lines, extract vertices/points/lines/parts, bounding boxes, bounding boxes
- **Vector Overlay**: clip, difference, intersection, union, symmetric difference, extract/choose by location
- **Vector Selection**: select by expression/location/attribute/distance/area/length, random extract
- **Vector Table**: field calculator, add field, delete field, refactor fields, join attributes by location/value, outline/merge attributes, column organizer

### GDAL Provider (~140 algorithms across 8 categories)
- **Raster Analysis**: sieve, proximity, near/extract values, grid data, merge, polygonize, polygon/raster query, Zonal statistics, compute statistics, build overviews/pyramids
- **Raster Conversion**: polygonize, rasterize, rasterize (burn), color map, format convert, polygonize
- **Raster Extraction**: clip by extent/mask/layer/polygon, contour, contour polygon
- **Raster Miscellaneous**: tile index, build overviews/pyramids, merge, build vrt, translate, add alpha band, configure raster metadata, generate XYZ tiles
- **Raster Projections**: warp/reproject, assign projection, extract projection
- **Vector Conversion**: rasterize, polygonize, export to CSV/GeoJSON/SQL/KML, convert from OSM
- **Vector Geoprocessing**: buffer, clip, convex/concave hull, dissolve, intersection, difference, union, symmetrical difference, merge, proximity, single-sided buffer, split by lines/mask
- **Vector Miscellaneous**: save to database, SQL execute, assign projection, export to spreadsheet, import/dump OSM

### GRASS Provider (~350+ algorithms)
Full integration of GRASS GIS modules covering:
- Raster: r.* modules (analysis, algebra, surface, hydrology, etc.)
- Vector: v.* modules (network, topology, overlay, interpolation)
- Imagery: i.* modules
- Temporal: t.* modules
- Database: db.* modules
- General: g.* modules

### SAGA Provider (~200+ algorithms)
- Terrain analysis, hydrology, climate, grid tools, point cloud processing, raster calculus, interpolation, vector processing

### OTB (Orfeo ToolBox) Provider (~100+ algorithms)
- Remote sensing, image segmentation, classification, feature extraction, change detection, spatial filtering

### R Provider (via rpy2)
- Custom R scripts integrated into Processing framework

---

## 2. Expression Functions (300+ across 28 categories)

### Aggregate Functions (23)
aggregate, array_agg, collect, concatenate, concatenate_unique, count, count_distinct, count_missing, iqr, majority, max_length, maximum, mean, median, min_length, minimum, minority, q1, q3, range, relation_aggregate, stdev, sum

### Array Functions (36)
array, array_all, array_append, array_cat, array_contains, array_count, array_distinct, array_filter, array_find, array_first, array_foreach, array_get, array_insert, array_intersect, array_last, array_length, array_majority, array_max, array_mean, array_median, array_min, array_minority, array_prepend, array_prioritize, array_remove_all, array_remove_at, array_replace, array_reverse, array_slice, array_sort, array_sum, array_to_string, generate_series, geometries_to_array, regexp_matches, string_to_array

### Color Functions (17)
color_cmyk, color_cmyka, color_grayscale_average, color_hsl, color_hsla, color_hsv, color_hsva, color_mix_rgb, color_part, color_rgb, color_rgba, create_ramp, darker, lighter, project_color, ramp_color, set_color_part

### Conditional Functions (9)
CASE WHEN, coalesce, if, nullif, regexp_match, try, when

### Conversion Functions (9)
array_to_string, date_to_string, format_number, to_date, to_datetime, to_int, to_real, to_string, to_time

### Date and Time Functions (25+)
age, day, day_of_week, day_of_year, epoch, hour, minute, month, now, second, to_date, to_datetime, to_time, week, year, day_of_month, extract_ymd, extract_hms, make_date, make_datetime, make_time, overlay_intersects (with temporal), project_color (temporal-aware)

### Fields and Values (3)
$geometry, $x, $y, attribute, $area, $length, $perimeter, $rownum, $id, $currentfeature, $layer, $scale

### Files and Paths Functions (4)
file_exists, file_name, file_path, is_directory

### Form Functions (4)
current_value, form_value, is_selected, represent_value

### Fuzzy Matching Functions (4)
hamming_distance, levenshtein, longest_common_substring, similar_text

### General Functions (15+)
at, attribute, color_rgb, format_number, get_feature, get_feature_by_id, get_layer_name, get_project_color, get_variable, identifiervar, is_feature, is_select, layer_property, map_get, project_variable, var

### Geometry Functions (60+)
$area, $length, $perimeter, azimuth, boundary, buffer, bounding_box, center_of_mass, centroid, collect, compact_lines, closest_point, combine, concatenate_points, contains, convex_hull, correct_polygons, cross_product, curtain, densify_by_count, densify_by_interval, difference, disjoint, distance, edge_intersections, endpoints, extend, exterior_ring, extrude, force_polygon, force_rhr, geom_from_gml, geom_from_wkt, geom_to_wkt, geom_from地理ML, geometry, geometry_n, geom_num_coordinates, geom_part_to_point, geom_point_n, geom_to_wkt, hausdorff_distance, intersection, intersects, interpolate_point, intersection_point, is_empty, length, lines_interpolate_point, line_interpolate_angle, line_locate_point, line_substring, make_circle, make_ellipse, make_line, make_point, make_point_m, make_point_z, make_polygon, make_rect, make_regular_polygon, make_square, make_triangle, nodes_to_points, num_intersections, num_points, offset_curve, offset_curve, orient_positions, perimeter, point_n, point_on_surface, point_prisms, points_to_table, pole_of_inaccessibility, project, relate, reverse, rotate, roundness, scale, segments_to_lines, simplify, simplify_vw, smooth, smooth_geometry_collection, start_point, sym_difference, symbol, tangent_angle_at_point, to_curve, transform, translate, triangulate_3d, traverse, union, validate

### Layout Functions (4)
layout_id, layout_name, layout_variable, item_variables

### Map Layers (2)
layer_property, map_layers

### Maps Functions (14)
map, map_append, map_canonicalize, map_concat, map_contains, map_delete, map_exists, map_get, map_insert, map_keys, map_length, map_merge, map_circumscribed_cube, map_circumscribed_cube

### Mathematical Functions (23)
abs, ceil, clamp, digits, exp, fact, floor, ln, log, max, min, pi, pow, rand, randf, round, round, scale_exp, scale_linear, sqrt, sum, tanh, x/0, $pi

### Meshes Functions (3)
mesh_vertex_count, mesh_vertex_xy, mesh_vertex_z

### Operators (15)
and, array index [], comparison, concatenate, divide, format, hash, in, logical not, minus, modulo, multiply, not, or, plus, power

### Processing Functions (4)
processing, processing_algorithm_help, processing_model_help, processing_parameters_help

### Rasters Functions (4)
raster_averaged_value, raster_value, raster_x_offset, raster_y_offset

### Record and Attributes Functions (12)
attribute, attribute_name, attributes, $currentfeature, format_map, format_string, get_feature, get_feature_by_id, is_feature, represent_value, $rownum, uuid

### Relations (2)
relation_aggregate, relation_get_feature

### Sensors Functions (3)
sensor_data, sensor_id, sensor_type

### String Functions (30+)
capitalize, contains, left, length, lower, lpad, ltrim, regex_replace, replace, right, rpad, rtrim, strpos, strip_tags, substr, to_upper, to_lower, trim, translate, wordwrap, format, concat, char, format_date, format_number, title, unaccent, initcap, levenshtein, hamming_distance, longest_common_substring, similar_text

### Variables (25+)
@atlas_feature, @atlas_featureid, @atlas_geometry, @layer, @layer_id, @layer_name, @map_crs, @map_scale, @map_extent, @map_rotation, @now, @parent, @project_crs, @project_file_path, @project_title, @row_number, @selection_count, @snapping_distance, @snapping_mode, @symbol_color, @symbol_line_width, @symbol_name, @symbol_point_count, @symbol_size, @symbol_width, @user, @uuid

---

## 3. Rendering Options

### Blending Modes (20)
Normal, Lighten, Screen, Dodge, Addition, Darken, Multiply, Burn, Overlay, Soft Light, Hard Light, Difference, Subtract, HSL Hue, HSL Saturation, HSL Color, HSL Luminosity, Source Atop, Source In, Source Out, Source Over

### Data-Defined Overrides
- Every symbology property can be controlled by expressions
- Variables, field values, global/project/layer variables
- Custom Python functions
- Full expression editor for each property

### Layer Effects (Paint Effects)
- **Drop Shadow**: blur, offset, spread, color, opacity
- **Inner Shadow**: blur, offset, spread, color, opacity
- **Outer Glow**: blur, spread, color, opacity
- **Inner Glow**: blur, spread, color, opacity
- **Blur**: blur level, blur type (stack/fast Gaussian)
- **Bilinear Filter**
- **Focal Blur**
- **Shape Burst Fill**: color ramp, blur, distance, use entire polygon
- **Draw Outline Effect**: line color, style, width
- **Transform**: translate, rotate, scale, shear

### Rendering Settings (per layer)
- **Opacity**: 0-100%
- **Blending mode**: per-feature and per-layer
- **Brightness-Contrast**: adjust brightness and contrast
- **Hue-Saturation**: adjust hue, saturation, lightness
- **Color Grayscale**: desaturate
- **Color Inversion**
- **Brightness-Contrast** and **Hue-Saturation** for rasters
- **Resampling**: nearest neighbor, bilinear, cubic, cubic spline, Lanczos
- **Scale dependent visibility**: min/max scale
- **Feature rendering order**: Z-based or custom

### Vector Symbology
- **Single Symbol**, **Categorized**, **Graduated**, **Rule-based**, **Paletted/Unique Values**
- **Inverted Polarity**
- **2.5D Renderer** (building-like extrusion)
- **Heatmap** renderer
- **Point Cluster** renderer
- **Points Displacement**
- **Line Pattern Fill**, **Point Pattern Fill**
- **SVG Marker**, **Font Marker**, **Raster Marker**
- **Interpolated Line** renderer (variable width)

### Raster Symbology
- **Singleband Gray**, **Singleband Pseudocolor**, **Multiband Color**
- **Paletted/Unique Values**, **Hidden**, **hillshade**
- **Color ramp**: gradient, discrete, exact
- **Contrast enhancement**: Stretch to MinMax, Stretch and Clip to MinMax, Clip to MinMax, No Enhancement
- **Histogram**: min/max value, count
- **Resampling**: nearest neighbor, bilinear, cubic, cubic spline, Lanczos

### Labeling
- **No Labels**, **Simple Labels**, **Rule-based Labels**, **Pal** (Proportional Adaptive Labeling)
- **Text rendering**: font, size, color, buffer, shadow, background, mask
- **Callouts**: simple/curved line callouts
- **Placement**: offset, rotation, distance, quadrant, repetition
- **Data-defined**: all properties expression-controllable

---

## 4. Editing Tools

### Basic Digitizing
- Toggle editing mode
- Add point/line/polygon features
- Vertex tool (active layer / all layers)
- Node editing: move, add, delete vertices

### Advanced Digitizing Panel
- **P**: Parallel constraint (distance from last point, angle)
- **D**: Distance constraint
- **A**: Angle constraint
- **X, Y**: X/Y coordinate constraints
- **Z**: Z value constraint
- **M**: M value constraint
- **Lock/Unlock**: lock constraints for repeated use
- **Construction mode**: create construction lines for precise placement
- **Repeating angle/distance**: repeat values across multiple vertices
- **Relative angle/distance**: relative to previous segment
- **Bearing/angle from point**: angle from a reference point

### Geometry Editing Tools
- **Move Feature(s)**: move selected features
- **Copy and Move Feature(s)**: duplicate and move
- **Rotate Feature(s)**: rotate around anchor
- **Scale Feature(s)**: scale from anchor
- **Simplify Feature**: vertex reduction
- **Add Ring**: interior ring (polygon)
- **Add Part**: add geometry part
- **Fill Ring**: create polygon from ring
- **Delete Ring**: remove interior ring
- **Delete Part**: remove geometry part
- **Reshape Features**: reshape boundary
- **Offset Curve**: parallel offset line
- **Split Features**: split with line
- **Split Parts**: split geometry parts
- **Merge Selected Features**: combine features
- **Merge Attributes of Selected Features**: combine attributes only
- **Reverse Line**: flip direction
- **Trim/Extend Feature**: trim or extend to boundary
- **Rotate Point Symbols**: rotate point symbology
- **Offset Point Symbols**: offset point symbology

### Attribute Editing
- **Add Record**: add new feature
- **Delete Selected Features**
- **Modify Attributes of Selected Features**: multi-edit
- **Merge Attributes of Selected Features**
- **Field Calculator**: bulk field updates
- **Cut/Copy/Paste Features**
- **Paste Features as**: new layer or scratch layer

### Snapping Options
- **Snap to vertex**, **Snap to segment**, **Snap to intersection**
- **Avoid overlap**
- **Tolerance** in map units or pixels
- **Topological editing**: snap to shared boundaries
- **Intersection snapping**

### Selection Tools
- Select by rectangle, polygon, freehand, radius
- Select by value (form-based)
- Select by expression
- Deselect all, deselect active layer
- Reselect features
- Select all, invert selection

### Annotation Tools
- **Text Annotation**: placed text with formatting
- **Form Annotation**: QGIS form overlay
- **HTML Annotation**: HTML content overlay
- **SVG Annotation**: SVG image overlay
- All annotations support data-defined properties

---

## 5. 3D Capabilities

### 3D Map View
- Multiple simultaneous 3D views
- Dockable/undockable panels
- Theme-based layer visibility

### Terrain Types
- **Flat terrain**
- **DEM (raster layer)**: with vertical scale factor
- **Online elevation service**: Mapzen tiles
- **Mesh layer**: Z values used as terrain
- **Tile resolution** and **Skirt height** configurable
- **Offset** for terrain elevation adjustment

### 3D Vector Layers
- **No Rendering**: hidden in 3D
- **Single Color**: uniform color
- **Color by Category**: classified by attribute
- **Color by Ramp**: gradient mapping
- **Data-defined properties**: expressions for all visual properties
- **Extrusion**: height from field or expression
- **3D Simple Marker**: sphere, cylinder, cone, cube, torus, plane, 3D model
- **3D Simple Line**: polyline with width
- **3D Simple Fill**: extruded polygon
- **3D Model**: OBJ, GLTF, FBX model files

### 3D Scene Configuration
- **General**: 2D map extent clipping, show extent in 2D
- **Terrain**: type, elevation source, vertical scale, tile resolution, skirt height, offset, shading (Phong model)
- **Lights** (up to 12):
  - 8 Point lights (X,Y,Z, color, intensity, attenuation)
  - 4 Directional lights (azimuth, altitude, color, intensity)
- **Shadow**: directional light shadows, max distance, bias, resolution
- **Camera & Skybox**: camera settings, 3D axis (CRS or Cube type), navigation sync (2D↔3D), panoramic/6-face skybox textures
- **Advanced**: map tile resolution, max screen/ground error, zoom levels, show labels/tiles/debug info, Eye Dome Lighting (EDL), Screen-Space Ambient Occlusion (SSAO), debug shadow/depth maps

### 3D Navigation
- Tilt up/down, rotate, pan, zoom
- Camera control, compass widget
- Keyboard shortcuts for all movements
- Measurement tool in 3D

### 3D Animations
- Keyframe-based camera animations
- Interpolation modes: linear, inQuad, outQuad, inCirc, outCirc, inElastic, outElastic, inBounce, outBounce, etc.
- Export animation frames as image series
- Configurable FPS, width, height

### 3D Export
- Export 3D scene as OBJ file (Blender compatible)
- Configurable terrain resolution, texture resolution, model scale
- Smooth edges, normals, textures options

---

## 6. Mesh Data Capabilities

### Mesh Types
- **1D Mesh**: vertices + edges (e.g., drainage networks)
- **2D Mesh**: triangles, quads (structured/unstructured)
- **3D Layered Mesh**: stacked 2D meshes with vertical coordinates

### Supported Formats (via MDAL)
- NetCDF (CF conventions)
- GRIB
- XMS SMS (2DM, 3DM)
- MDAL formats
- Ugrid
- Various hydrological model outputs

### Mesh Properties
- **Information**: vertex/face/edge count, dataset groups
- **Source**: CRS, dataset groups tree, assign extra datasets, static dataset groups
- **Symbology**:
  - **Datasets**: scalar/vector selection, metadata
  - **Contours**: color ramp shader, classification, min/max range, opacity, resampling
  - **Vectors**: arrows, streamlines, traces
    - Arrow options: coloring, filter by magnitude, head options, length (min-max/scale/fixed)
    - Streamlines: seeding method (mesh grid/random), particle count
    - Traces: particle animation effect
  - **Rendering**: native/triangular mesh display, line width/color
  - **Stacked mesh averaging**: depth/height averaging methods
- **3D View**: smooth triangles, wireframe, level of detail, vertical settings (dataset Z values), color ramp shading, arrows in 3D
- **Rendering**: simplify mesh (reduction factor, min triangle size), scale-dependent visibility
- **Temporal**: reference time, dataset matching method, provider time unit
- **Elevation**: scale, offset, profile chart style (line/fill above/fill below)

### Mesh Editing
- Create new mesh layers from scratch
- **Digitize Mesh Elements**: add vertices, faces
- **Select by Polygon** or **by Expression**
- **Transform Vertices Coordinates**: bulk coordinate modification
- **Force by Selected Geometries**: split faces along vector features
- **Reindex Faces and Vertices**
- Z value assignment: default, interpolated from edges/faces, snapped to 3D vectors
- Validity checking for self-intersections, overlaps

### Mesh Calculator
- Arithmetic operations on mesh datasets
- Logical/conditional expressions
- Create virtual datasets
- Process across temporal dimension
- Export results

---

## 7. Point Cloud Capabilities

### Supported Formats
- **LAS/LAZ**: auto-converted to EPT on load
- **EPT (Entwine Point Tile)**: native format, indexed for fast access
- **COPC (Cloud Optimized Point Cloud)**: supported via PDAL

### Virtual Point Cloud (VPC)
- JSON file referencing multiple LAS/LAZ/COPC files
- Single-layer treatment of tiled datasets
- Automatic LOD (level of detail) management
- Created via Processing algorithm or PDAL wrench

### Symbology Renderers
- **Extent Only**: bounding box display
- **Attribute by Ramp**: continuous color gradient by numeric attribute (Z, intensity, etc.)
  - Discrete/Linear/Exact interpolation
  - Classification modes: Continuous, Equal Interval
  - Customizable class values, colors, labels
- **RGB Renderer**: R/G/B attributes mapped to display colors
  - Contrast enhancement options
- **Classification Renderer**: categorical coloring by attribute
  - ASPRS standard point classes
  - Custom class values, colors, legends
  - Checkbox visibility per class

### 3D Rendering Modes
- **No Rendering**
- **Follow 2D Symbology**: sync 3D with 2D settings
- **Single Color**
- **Attribute by Ramp** (same as 2D)
- **RGB** (same as 2D)
- **Classification** (same as 2D)

### 3D Point Symbol
- Point size (pixels)
- Maximum screen space error
- Point budget (rendering cap)
- **Render as surface (Triangulate)**: solid surface with triangle thresholds
- Show bounding boxes

### Rendering Properties
- **Draw order**: Default, Bottom to Top, Top to Bottom
- **Maximum error**: density control (mm/pixels)
- **Opacity**: layer transparency
- **Blending mode**: all standard modes
- **Eye Dome Lighting (EDL)**: strength, distance parameters

### Elevation Properties
- Scale factor and offset for Z values
- Profile chart accuracy (max error)
- Profile chart appearance: point size, circle/square, single color, respect layer coloring, opacity by distance from curve

### Provider Filter
- SQL-like expression filtering at PDAL level
- Query builder with field values, operators
- Visual indicator in Layers Panel

### Statistics Tab
- Attribute statistics: min, max, mean, std dev
- Classification counts and percentages

---

## 8. Temporal Controller Features

### Temporal Navigation Widget
- Play/pause controls
- Time range slider
- Frame step: seconds, minutes, hours, days, months, years
- Loop option
- Cumulative range display

### Layer Temporal Settings
- **No temporal settings**: static display
- **Fixed time range**: start/end time
- **Fixed time step**: repeating intervals
- **Animated**: driven by controller

### Raster Temporal Support
- **Static**: fixed time display
- **Animated**: dynamic temporal control
  - Automatic detection from metadata
  - Custom start/end time
  - Step duration (seconds/minutes/hours/days/months/years)
  - Accumulate features over time
  - Temporal unit alignment

### WMS-T Support
- **Dynamic Temporal Control**: automatic or custom
- **Static WMS-T Temporal Range**:
  - Server default
  - Predefined date/range
  - Follow project temporal range
- **WMS-T Settings**:
  - Time slice mode (whole range, closest match start/end)
  - Ignore time components (dates only)
  - Specific reference time selection

### Mesh Temporal Support
- Reference time detection from data
- Custom reference time override
- Dataset matching: closest before, closest overall
- Provider time unit (seconds/minutes/hours/days)
- Static dataset group option

### Vector Temporal Support
- Timestamps field-based
- Start/end time range from fields
- Fixed time range
- Animated display

### Project Temporal Settings
- Temporal range definition
- Start/end datetime
- Temporal unit
- Frame duration/step
- Cumulative range

---

## 9. Print Layout Items & Options

### Layout Items
- **Map**: scale, extent, rotation, CRS, layers, style presets, overviews, grid
- **3D Map**: 3D view in layout
- **Label**: text with expressions, HTML rendering
- **Legend**: auto/manual, nested groups, layer order sync
- **Scale Bar**: single/linked, numeric, division styles
- **Table**: attribute tables, HTML tables, CSV
- **Marker**: point symbols
- **Picture**: images (SVG, raster), north arrows
- **HTML Frame**: live HTML/web content
- **Shape**: rectangle, ellipse, triangle
- **Elevation Profile**: cross-section view

### Item Common Options
- Position and size (X, Y, width, height)
- Rotation, frame/border
- Background color, opacity
- Map rotation lock
- Follow map theme
- Variables

### Map Item Features
- Map rotation
- Atlas coverage layer
- Overviews (multiple)
- Grid: frame, annotation, cross, ticks
- CRS override
- Layers visibility override
- Extent locked option

### Atlas Generation
- Coverage layer (polygon features)
- Page name field
- Hidden coverage layer
- Feature filtering
- Sort by expression
- Output as PDF/image/SVG series
- Number-based or name-based filenames
- Single file or multiple files

### Report Generation
- Feature-based reports
- Nested report sections
- Summary and header/footer
- PDF output

### Export Options
- **Image**: DPI, resolution, format (PNG, JPG, TIF, BMP)
- **SVG**: layers, text rendering, metadata
- **PDF**: geospatial PDF, OGC Best Practice, multiple formats
- **Print**: direct printer output

---

## 10. Authentication Methods

### Authentication Database
- SQLite-based (`qgis-auth.db`)
- Separate from QGIS settings
- Config ID (7-char alphanumeric) stored in projects

### Master Password
- Encrypts all credentials
- Cached during session
- Wallet/Keyring integration
- Can be set via environment variable (`QGIS_AUTH_PASSWORD_FILE`)
- Reset with backup option

### Authentication Methods (10+)
1. **Basic HTTP**: username/password
2. **Identity Cert/Certificates**: PKI certificate-based
3. **PKI Paths**: PEM/DER cert/key files
4. **PKI PKCS#12**: PKCS#12 file path
5. **ESRI Token**: ArcGIS token authentication
6. **OAuth2**: full OAuth2 flow with client ID, secret, scopes
7. **Stored Identity**: stored username/password
8. **Header**: custom HTTP header authentication
9. **AWS S3**: AWS signature-based
10. **LDAP**: LDAP bind authentication
11. **Kerberos**: SPNEGO/Kerberos
12. **Custom**: pluggable C++ authentication plugins

### Utility Features
- Clear cached credentials
- Clear network authentication cache
- Erase authentication database
- Wallet/Keyring integration
- Password helper debug log
- Auto-clear on SSL errors

---

## 11. Database Capabilities

### PostgreSQL / PostGIS
- Full read/write support
- Spatial indexing (GiST)
- Views and materialized views
- Foreign tables
- Server-side expression filtering
- Support for all PostgreSQL data types: integer, float, boolean, binary object, varchar, geometry, timestamp, array, hstore, json
- Primary key requirement (int4 or ctid)
- **Select at id** optimization
- DB Manager integration
- Import via shp2pgsql, ogr2ogr, DB Manager
- Layer styles stored in database
- Project storage in PostgreSQL

### SpatiaLite
- Full read/write support
- Editable views
- Spatial indexing
- DB Manager integration
- Create layers from scratch
- All SpatiaLite functions available

### Oracle Spatial
- Full read/write support
- Spatial indexing
- LOB support
- SDO_GEOMETRY type
- Connection pooling
- Schema filtering

### MS SQL Server Spatial
- Full read/write support
- Geography and Geometry types
- Spatial indexing
- Schema filtering
- Primary key support

### SAP HANA Spatial
- Full read/write support
- ST_Geometry type
- Spatial indexing
- Table and view support
- Feature ID requirement (64-bit int mapping)
- Multi-column primary keys
- Updatable view support

### Virtual Layers
- SQL-based virtual layers
- Query any data source
- JOIN across different sources
- Custom CRS
- On-the-fly processing

### OGR-supported databases
- SQLite, MySQL, PostgreSQL, Oracle, SQL Server, Informix, DB2, ODBC

---

## 12. Plugin Categories & Notable Plugins

### Core Plugins (9)
1. **DB Manager**: database interaction, SQL queries, data import/export
2. **Geometry Checker**: geometry validation and repair
3. **Georeferencer GDAL**: geocode rasters and vectors
4. **GPS Tools**: GPX import/export, GPS device communication
5. **GRASS**: full GRASS GIS integration
6. **MetaSearch Catalogue Client**: OGC CSW catalog search
7. **Offline Editing**: offline editing with database sync
8. **Processing**: geospatial analysis framework
9. **Topology Checker**: topology error detection

### External Plugin Categories
- **Analysis & Geoprocessing**: additional algorithms
- **Cartography**: map design tools
- **Data Management**: data import/export, conversion
- **Database**: additional DB support
- **Developer Tools**: debugging, profiling
- **Digitizing**: advanced editing tools
- **File Tools**: file management
- **Georeferencing**: additional georeferencing tools
- **Imagery**: remote sensing tools
- **Import/Export**: format conversion
- **Layers**: layer management
- **Maps**: map creation tools
- **Mesh**: mesh processing
- **Point Cloud**: point cloud tools
- **Raster**: raster analysis
- **Scientific**: research tools
- **Spatial Analysis**: spatial statistics
- **Terrain**: terrain analysis
- **Visualization**: data visualization
- **Web**: web service tools

### Notable External Plugins
- **QuickMapServices**: basemap tiles
- **OpenLayers**: Google/OSM tiles
- **sketcher**: sketching on map
- **Profile Tool**: elevation profiles
- **TimeManager**: temporal data animation
- **Valhalla**: routing engine
- **QNEAT3**: network analysis
- **Sentinel Hub**: Sentinel imagery
- **SCP (Semi-Automatic Classification)**: remote sensing
- **Plugin Builder**: create new plugins
- **QTiles**: generate map tiles

---

## 13. OGC Services Supported

### Client (Consuming)
- **WMS** (1.1, 1.1.1, 1.3): Web Map Service
- **WMTS**: Web Map Tile Service (KVP + RESTful)
- **WMS-C**: Cached WMS
- **WFS** (1.0, 1.1, 2.0): Web Feature Service
- **WFS-T**: Web Feature Service - Transactional (editing)
- **OGC API - Features (OAPIF)**: modern REST API
- **OGC API - Features Part 4**: Create/Replace/Update/Delete
- **WCS** (1.0, 1.1): Web Coverage Service
- **CSW**: Catalog Service for the Web
- **WPS**: Web Processing Service (via Processing framework)
- **GML**: Geography Markup Language (read/write)

### Server (Publishing)
- **WMS**: Web Map Service
- **WMTS**: Web Map Tile Service
- **WFS**: Web Feature Service
- **WFS-T**: Web Feature Service - Transactional
- **WCS**: Web Coverage Service
- **WPS**: Web Processing Service
- **OGC API - Features**: modern REST API
- **CSW**: Catalog Service
- **SFS**: Simple Features for SQL

### Additional Standards
- **GeoJSON**: read/write
- **KML/KMZ**: read/write
- **GPKG**: OGC GeoPackage
- **XYZ Tiles**: custom tile services
- **TMS**: Tile Map Service
- **ArcGIS REST**: MapServer, ImageServer, FeatureServer

---

## 14. Import/Export Formats

### Vector Formats
- **GeoPackage** (default): GPKG
- **ESRI Shapefile**: SHP (+ SHZ/ZIP compressed)
- **GeoJSON**: RFC 7946 support
- **KML/KMZ**: Google Earth
- **CSV/TSV**: delimited text
- **DXF/DWG**: CAD formats
- **MapInfo Tab/MIF**: TAB, MIF
- **SDTS**: SDTS
- **GML**: Geography Markup Language
- **FileGDB**: ESRI File Geodatabase
- **Personal GDB**: ESRI Personal Geodatabase
- **SQL**: database-specific SQL dumps
- **GPX**: GPS Exchange Format
- **DGN**: MicroStation
- **BNA**: Atlas BNA
- **DXF**: AutoCAD DXF
- **IDRISI Vector**: VEC
- **MapInfo MIF**: MIF
- **PDS**: Planetary Data System
- **S-57**: Electronic Navigational Charts
- **S-101**: S-100 product format
- **UK National Grid**: NTf2
- **TIGER**: Census Bureau
- **WAsP**: Wind Atlas
- **XLSX**: Excel spreadsheets
- **ODS**: OpenDocument spreadsheets

### Raster Formats
- **GeoTIFF** (+ BigTIFF): TIF/TIFF
- **JPEG**: JPG (+ JPEG2000)
- **PNG**: PNG
- **BMP**: BMP
- **GIF**: GIF
- **ERDAS Imagine**: IMG
- **ArcInfo ASCII GRID**: ASC
- **ArcInfo Binary Grid**: FLT
- **GRASS**: GRASS raster
- **HFA**: Erdas Imagine
- **MRF**: Meta Raster Format
- **MBTiles**: map tiles
- **NetCDF**: Network Common Data Form
- **GRIB**: meteorological
- **RST**: Idrisi raster
- **SRTM**: Shuttle Radar Topography Mission
- **VRT**: GDAL Virtual Raster
- **WMS/WMTS**: web map tiles
- **WCS**: web coverage service
- **EPT**: Entwine Point Tile (point clouds)
- **LAS/LAZ**: LiDAR formats
- **COPC**: Cloud Optimized Point Cloud

### 3D Formats
- **OBJ**: Wavefront OBJ
- **GLTF/GLB**: GL Transmission Format
- **FBX**: Autodesk FBX
- **3D Tiles**: OGC 3D Tiles

### Other Formats
- **Project files**: QGS, QGZ
- **Style files**: QML, SLD
- **Layer definition files**: QLR
- **Processing models**: GGM
- **Print layouts**: QPT

---

## 15. QGIS Server Capabilities

### Services
- **WMS**: Web Map Service (1.1, 1.1.1, 1.3)
- **WMTS**: Web Map Tile Service
- **WFS**: Web Feature Service (1.0, 1.1, 2.0)
- **WFS-T**: Web Feature Service - Transactional
- **WCS**: Web Coverage Service (1.0)
- **WPS**: Web Processing Service
- **OGC API - Features**: REST API
- **CSW**: Catalog Service for the Web

### Server Features
- Project-based configuration
- Multiple project support
- Access control (per-layer ACLs)
- Print capabilities
- GetProjectSettings
- GetPrint (Atlas support)
- GetFeatureInfo with GML output
- SVG/PIXMAP symbols
- External WMS layers
- Authentication support (LDAP, Basic, OAuth2)
- Style management (SLD, QML)
- Data-defined overrides in WMS
- Temporal support (WMS-T)
- 3D rendering in WMS
- OGR SQL queries
- OWS service properties per layer
- GetLegendGraphic
- Contextual legend

### Server Plugins
- C++ and Python server plugins
- Custom API endpoints
- Request filtering
- Logging
- Performance monitoring

### Deployment
- Apache (mod_fcgid/mod_proxy_fcgi)
- Nginx (fcgiwrap)
- IIS (FastCGI)
- Apache httpd on Windows
- Linux package repositories
- Docker images

---

## 16. QGIS Cloud/Online Features

### QGIS Cloud (qgiscloud.com)
- Free hosting for QGIS Server
- Publish maps online
- Cloud storage for projects
- WMS/WMTS/WFS endpoints
- Auto-configuration with QGIS

### QGIS Online Integration
- Save projects to cloud storage
- Share maps via URL
- Web client for map viewing

### Tile Services
- Custom XYZ tile generation
- MBTiles output
- TMS endpoints
- QTiles plugin for tile generation

---

## 17. Mobile Capabilities

### QField (qfield.qgis.org)
- Official QGIS mobile app
- Android/iOS/Windows
- Field data collection
- GPS/GNSS integration
- Offline data editing
- Sync with QGIS projects
- QR code project sharing
- Sketching and photo capture
- Form-based data entry
- Background GPS tracking
- Cloud sync (QFieldCloud)

### QFieldCloud
- Cloud synchronization service
- Version control for GIS projects
- Multi-user collaboration
- Offline/online workflow
- Automatic project packaging
- API for custom integrations

### Input (opengis.ch)
- Alternative mobile app
- Android/iOS
- Form-based data collection
- GPS/GNSS support
- QGIS project support
- WebDAV sync
- Cloud sync

### QGIS on Tablets
- Touch-optimized interface
- Stylus support
- Pressure-sensitive digitizing
- Touch gestures for navigation

---

## 18. Performance Features

### Parallel Rendering
- Multi-threaded map rendering
- Configurable number of rendering threads
- Progress indication per layer
- Rendering in background threads

### Cached Layers
- WMS/WMTS tile caching (24h default)
- WMTS pre-generated tile sets
- On-disk cache for WFS features
- Local caching for WCS

### Spatial Indexing
- R-tree spatial index (built-in)
- Automatic indexing for GeoPackage/PostGIS
- Manual index creation
- GRASS spatial index

### Rendering Optimization
- Feature simplification (local and global)
- Simplify on-the-fly for large datasets
- Mesh level-of-detail rendering
- Point cloud LOD management
- Scale-dependent rendering
- Feature-based rendering order

### Memory Management
- Virtual layers for on-the-fly processing
- Temporary scratch layers
- In-memory data caching

### Background Processing
- All processing algorithms run in background
- Progress reporting
- Cancel support
- Result caching

### Project Performance
- Layer subset caching
- Style caching
- Expression caching
- Projection caching

---

## 19. Accessibility Features

### Keyboard Navigation
- Full keyboard navigation of interface
- Customizable keyboard shortcuts
- Tab/arrow key navigation
- Access keys for menus

### Screen Reader Support
- ARIA labels for interface elements
- Role descriptions
- State announcements
- Live region updates

### High Contrast
- High contrast color schemes
- Scalable interface elements
- Color-independent information

### Touch Interface
- Touch gesture support
- Pinch-to-zoom
- Touch-optimized buttons
- Stylus support

### Font and Display
- Configurable font sizes
- DPI scaling
- Multi-monitor support
- Full screen mode

### Color Vision
- Color blindness simulation tools (Protanopia, Deuteranopia, Tritanopia)
- Color ramp design considerations
- Pattern fills as alternatives

---

## 20. Testing Framework

### QGIS Testing Tools
- **PyQGIS**: Python scripting and testing
- **Processing tests**: algorithm unit tests
- **Server tests**: WMS/WFS/WCS testing
- **GUI tests**: interface testing
- **Integration tests**: end-to-end workflows

### Test Categories
- **Unit tests**: C++ and Python
- **Rendering tests**: map output comparison
- **Database tests**: PostGIS/SpatiaLite
- **Provider tests**: data source access
- **Expression tests**: expression function validation
- **Processing tests**: algorithm verification

### CI/CD
- Travis CI / GitHub Actions
- Automated test suites
- Platform-specific testing (Linux, Windows, macOS)
- Docker-based testing

### Quality Tools
- **Geometry Checker**: validate geometry integrity
- **Topology Checker**: detect topology errors
- **QGIS Validation Tool**: OGC standard compliance

### Documentation
- Interactive documentation (Sphinx)
- Training manual with exercises
- PyQGIS Developer Cookbook
- API documentation (Doxygen)

---

## Key Differentiator Summary

| Feature | QGIS 3.x | Competitor |
|---------|-----------|------------|
| Price | Free/Open Source | Paid License |
| Processing Algorithms | 800+ (native+GDAL+GRASS+SAGA+OTB) | Varies |
| Expression Functions | 300+ | Varies |
| 3D View | Full-featured with animations | Basic |
| Mesh Support | Native with editing | Limited |
| Point Cloud | LAS/LAZ/EPT/COPC with VPC | Limited |
| Temporal | Full controller with WMS-T | Basic |
| Authentication | 12+ methods | Varies |
| Databases | PostGIS, SpatiaLite, Oracle, SQL Server, SAP HANA | Varies |
| OGC Services | WMS, WMTS, WFS, WCS, WPS, OAPIF | Varies |
| Server | Full WMS/WFS/WCS/WPS server | Varies |
| Mobile | QField + Input | Varies |
| Plugin Ecosystem | 1800+ plugins | Varies |
| Platform | Windows, macOS, Linux | Varies |
| Scripting | Python (PyQGIS) | Varies |
| Print Layout | Atlas, Reports, 3D Map items | Basic |
