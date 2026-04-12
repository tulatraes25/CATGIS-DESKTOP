package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DxfLoader {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final String GEOMETRY_FIELD = "the_geom";

    private DxfLoader() {
    }

    public static ShapefileData load(String path) throws Exception {
        return load(new File(path));
    }

    public static ShapefileData load(File file) throws Exception {
        if (file == null) {
            throw new RuntimeException("Archivo DXF nulo.");
        }
        if (!file.exists()) {
            throw new RuntimeException("No existe el archivo: " + file.getAbsolutePath());
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".dxf")) {
            throw new RuntimeException("El archivo no es DXF: " + file.getAbsolutePath());
        }

        byte[] bytes = Files.readAllBytes(file.toPath());
        if (isBinaryDxf(bytes)) {
            throw new RuntimeException("El DXF parece binario. Exportalo como DXF ASCII para cargarlo en CATGIS.");
        }

        DxfParseResult parseResult = parseDocument(readPairs(decodeContent(bytes)));
        if (parseResult.records().isEmpty()) {
            throw new RuntimeException("El DXF no contiene entidades CAD soportadas para cargar como referencia.");
        }

        List<Geometry> geometries = new ArrayList<>();
        Envelope envelope = null;
        for (DxfRecord record : parseResult.records()) {
            if (record.geometry() == null || record.geometry().isEmpty()) {
                continue;
            }
            geometries.add(record.geometry());
            if (envelope == null) {
                envelope = new Envelope(record.geometry().getEnvelopeInternal());
            } else {
                envelope.expandToInclude(record.geometry().getEnvelopeInternal());
            }
        }
        if (geometries.isEmpty()) {
            throw new RuntimeException("El DXF no genero geometria valida para dibujar.");
        }

        LinkedHashMap<String, Class<?>> attributes = new LinkedHashMap<>();
        attributes.put("entity_type", String.class);
        attributes.put("cad_layer", String.class);
        attributes.put("cad_handle", String.class);
        attributes.put("cad_color", Integer.class);
        attributes.put("cad_ltype", String.class);
        attributes.put("text", String.class);
        attributes.put("closed", Boolean.class);
        attributes.put("elev_z", Double.class);

        Class<? extends Geometry> geometryBinding =
                VectorLayerUtils.resolveConcreteGeometryBinding(geometries, Geometry.class);
        SimpleFeatureType featureType = buildFeatureType(file, geometryBinding, attributes);
        List<SimpleFeature> features = buildFeatures(parseResult.records(), featureType, attributes);

        String unitsLabel = parseResult.insUnitsLabel() != null && !parseResult.insUnitsLabel().isBlank()
                ? parseResult.insUnitsLabel()
                : "sin definir";
        String message = "DXF CAD cargado: " + file.getName()
                + " | entidades: " + features.size()
                + " | unidades CAD: " + unitsLabel
                + " | CRS embebido: no disponible";
        return new ShapefileData(features, envelope, file.getName(), features.size(), message, featureType);
    }

    private static boolean isBinaryDxf(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }
        String header = new String(bytes, 0, Math.min(bytes.length, 24), StandardCharsets.ISO_8859_1);
        return header.startsWith("AutoCAD Binary DXF");
    }

    private static String decodeContent(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        long replacementCount = utf8.chars().filter(ch -> ch == '\uFFFD').count();
        if (replacementCount > 0) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }
        return utf8;
    }

    private static List<DxfPair> readPairs(String content) {
        List<DxfPair> pairs = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return pairs;
        }
        String[] lines = content.split("\\R");
        for (int i = 0; i + 1 < lines.length; i += 2) {
            String code = lines[i] != null ? lines[i].trim() : "";
            String value = lines[i + 1] != null ? lines[i + 1].stripTrailing() : "";
            if (!code.isBlank()) {
                pairs.add(new DxfPair(code, value));
            }
        }
        return pairs;
    }

    private static DxfParseResult parseDocument(List<DxfPair> pairs) {
        List<DxfRecord> records = new ArrayList<>();
        String currentSection = "";
        String currentHeaderVariable = "";
        Integer insUnitsCode = null;

        for (int i = 0; i < pairs.size(); i++) {
            DxfPair pair = pairs.get(i);
            if ("0".equals(pair.code()) && "SECTION".equalsIgnoreCase(pair.value())) {
                currentSection = findSectionName(pairs, i + 1);
                continue;
            }
            if ("0".equals(pair.code()) && "ENDSEC".equalsIgnoreCase(pair.value())) {
                currentSection = "";
                currentHeaderVariable = "";
                continue;
            }
            if ("HEADER".equalsIgnoreCase(currentSection)) {
                if ("9".equals(pair.code())) {
                    currentHeaderVariable = pair.value() != null ? pair.value().trim() : "";
                    continue;
                }
                if ("$INSUNITS".equalsIgnoreCase(currentHeaderVariable) && "70".equals(pair.code())) {
                    insUnitsCode = parseInteger(pair.value(), null);
                }
                continue;
            }
            if (!"ENTITIES".equalsIgnoreCase(currentSection) || !"0".equals(pair.code())) {
                continue;
            }
            if ("ENDSEC".equalsIgnoreCase(pair.value())) {
                currentSection = "";
                continue;
            }
            ParsedEntity parsed = parseEntity(pairs, i);
            if (parsed != null) {
                if (parsed.record() != null && parsed.record().geometry() != null && !parsed.record().geometry().isEmpty()) {
                    records.add(parsed.record());
                }
                i = Math.max(i, parsed.nextIndex() - 1);
            }
        }

        return new DxfParseResult(records, describeUnits(insUnitsCode));
    }

    private static String findSectionName(List<DxfPair> pairs, int fromIndex) {
        for (int i = fromIndex; i < Math.min(pairs.size(), fromIndex + 4); i++) {
            DxfPair pair = pairs.get(i);
            if ("2".equals(pair.code())) {
                return pair.value() != null ? pair.value().trim() : "";
            }
            if ("0".equals(pair.code())) {
                break;
            }
        }
        return "";
    }

    private static ParsedEntity parseEntity(List<DxfPair> pairs, int index) {
        DxfPair start = pairs.get(index);
        String entityType = start.value() != null ? start.value().trim().toUpperCase(Locale.ROOT) : "";
        return switch (entityType) {
            case "LINE" -> parseLine(pairs, index);
            case "POINT" -> parsePoint(pairs, index);
            case "TEXT" -> parseText(pairs, index, false);
            case "MTEXT" -> parseText(pairs, index, true);
            case "CIRCLE" -> parseCircle(pairs, index);
            case "ARC" -> parseArc(pairs, index);
            case "LWPOLYLINE" -> parseLightweightPolyline(pairs, index);
            case "POLYLINE" -> parsePolyline(pairs, index);
            default -> skipUnknownEntity(pairs, index);
        };
    }

    private static ParsedEntity parseLine(List<DxfPair> pairs, int index) {
        EntityBase base = new EntityBase("LINE");
        Double x1 = null;
        Double y1 = null;
        Double z1 = null;
        Double x2 = null;
        Double y2 = null;
        Double z2 = null;
        int next = findNextEntityIndex(pairs, index + 1);
        for (int i = index + 1; i < next; i++) {
            DxfPair pair = pairs.get(i);
            base.accept(pair);
            switch (pair.code()) {
                case "10" -> x1 = parseDouble(pair.value(), x1);
                case "20" -> y1 = parseDouble(pair.value(), y1);
                case "30" -> z1 = parseDouble(pair.value(), z1);
                case "11" -> x2 = parseDouble(pair.value(), x2);
                case "21" -> y2 = parseDouble(pair.value(), y2);
                case "31" -> z2 = parseDouble(pair.value(), z2);
                default -> {
                }
            }
        }
        if (x1 == null || y1 == null || x2 == null || y2 == null) {
            return new ParsedEntity(null, next);
        }
        LineString geometry = GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                new Coordinate(x1, y1, z1 != null ? z1 : 0d),
                new Coordinate(x2, y2, z2 != null ? z2 : 0d)
        });
        return new ParsedEntity(base.toRecord(geometry, false), next);
    }

    private static ParsedEntity parsePoint(List<DxfPair> pairs, int index) {
        EntityBase base = new EntityBase("POINT");
        Double x = null;
        Double y = null;
        Double z = null;
        int next = findNextEntityIndex(pairs, index + 1);
        for (int i = index + 1; i < next; i++) {
            DxfPair pair = pairs.get(i);
            base.accept(pair);
            switch (pair.code()) {
                case "10" -> x = parseDouble(pair.value(), x);
                case "20" -> y = parseDouble(pair.value(), y);
                case "30" -> z = parseDouble(pair.value(), z);
                default -> {
                }
            }
        }
        if (x == null || y == null) {
            return new ParsedEntity(null, next);
        }
        Point geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(x, y, z != null ? z : 0d));
        return new ParsedEntity(base.toRecord(geometry, false), next);
    }

    private static ParsedEntity parseText(List<DxfPair> pairs, int index, boolean multiline) {
        EntityBase base = new EntityBase(multiline ? "MTEXT" : "TEXT");
        Double x = null;
        Double y = null;
        Double z = null;
        StringBuilder text = new StringBuilder();
        int next = findNextEntityIndex(pairs, index + 1);
        for (int i = index + 1; i < next; i++) {
            DxfPair pair = pairs.get(i);
            base.accept(pair);
            switch (pair.code()) {
                case "10" -> x = parseDouble(pair.value(), x);
                case "20" -> y = parseDouble(pair.value(), y);
                case "30" -> z = parseDouble(pair.value(), z);
                case "1", "3" -> {
                    String chunk = normalizeText(pair.value());
                    if (!chunk.isBlank()) {
                        if (!text.isEmpty()) {
                            text.append(' ');
                        }
                        text.append(chunk);
                    }
                }
                default -> {
                }
            }
        }
        if (x == null || y == null) {
            return new ParsedEntity(null, next);
        }
        base.text = text.toString().trim();
        Point geometry = GEOMETRY_FACTORY.createPoint(new Coordinate(x, y, z != null ? z : 0d));
        return new ParsedEntity(base.toRecord(geometry, false), next);
    }

    private static ParsedEntity parseCircle(List<DxfPair> pairs, int index) {
        EntityBase base = new EntityBase("CIRCLE");
        Double centerX = null;
        Double centerY = null;
        Double radius = null;
        int next = findNextEntityIndex(pairs, index + 1);
        for (int i = index + 1; i < next; i++) {
            DxfPair pair = pairs.get(i);
            base.accept(pair);
            switch (pair.code()) {
                case "10" -> centerX = parseDouble(pair.value(), centerX);
                case "20" -> centerY = parseDouble(pair.value(), centerY);
                case "40" -> radius = parseDouble(pair.value(), radius);
                default -> {
                }
            }
        }
        if (centerX == null || centerY == null || radius == null || radius <= 0d) {
            return new ParsedEntity(null, next);
        }
        LineString geometry = buildArcGeometry(centerX, centerY, radius, 0d, 360d, true);
        return new ParsedEntity(base.toRecord(geometry, true), next);
    }

    private static ParsedEntity parseArc(List<DxfPair> pairs, int index) {
        EntityBase base = new EntityBase("ARC");
        Double centerX = null;
        Double centerY = null;
        Double radius = null;
        Double startAngle = null;
        Double endAngle = null;
        int next = findNextEntityIndex(pairs, index + 1);
        for (int i = index + 1; i < next; i++) {
            DxfPair pair = pairs.get(i);
            base.accept(pair);
            switch (pair.code()) {
                case "10" -> centerX = parseDouble(pair.value(), centerX);
                case "20" -> centerY = parseDouble(pair.value(), centerY);
                case "40" -> radius = parseDouble(pair.value(), radius);
                case "50" -> startAngle = parseDouble(pair.value(), startAngle);
                case "51" -> endAngle = parseDouble(pair.value(), endAngle);
                default -> {
                }
            }
        }
        if (centerX == null || centerY == null || radius == null || radius <= 0d || startAngle == null || endAngle == null) {
            return new ParsedEntity(null, next);
        }
        LineString geometry = buildArcGeometry(centerX, centerY, radius, startAngle, endAngle, false);
        return new ParsedEntity(base.toRecord(geometry, false), next);
    }

    private static ParsedEntity parseLightweightPolyline(List<DxfPair> pairs, int index) {
        EntityBase base = new EntityBase("LWPOLYLINE");
        List<Coordinate> coordinates = new ArrayList<>();
        Double currentX = null;
        boolean closed = false;
        int next = findNextEntityIndex(pairs, index + 1);
        for (int i = index + 1; i < next; i++) {
            DxfPair pair = pairs.get(i);
            base.accept(pair);
            switch (pair.code()) {
                case "10" -> currentX = parseDouble(pair.value(), currentX);
                case "20" -> {
                    Double currentY = parseDouble(pair.value(), null);
                    if (currentX != null && currentY != null) {
                        coordinates.add(new Coordinate(currentX, currentY));
                        currentX = null;
                    }
                }
                case "38" -> base.elevation = parseDouble(pair.value(), base.elevation);
                case "70" -> {
                    Integer flags = parseInteger(pair.value(), 0);
                    closed = flags != null && (flags & 1) == 1;
                }
                default -> {
                }
            }
        }
        Geometry geometry = buildPolylineGeometry(coordinates, closed, base.elevation);
        return new ParsedEntity(geometry != null ? base.toRecord(geometry, closed) : null, next);
    }

    private static ParsedEntity parsePolyline(List<DxfPair> pairs, int index) {
        EntityBase base = new EntityBase("POLYLINE");
        List<Coordinate> coordinates = new ArrayList<>();
        boolean closed = false;
        int i = index + 1;
        for (; i < pairs.size(); i++) {
            DxfPair pair = pairs.get(i);
            if ("0".equals(pair.code())) {
                String value = pair.value() != null ? pair.value().trim().toUpperCase(Locale.ROOT) : "";
                if ("VERTEX".equals(value)) {
                    ParsedVertex vertex = parseVertex(pairs, i);
                    if (vertex.coordinate() != null) {
                        coordinates.add(vertex.coordinate());
                    }
                    i = vertex.nextIndex() - 1;
                    continue;
                }
                if ("SEQEND".equals(value)) {
                    i = i + 1;
                    break;
                }
                break;
            }
            base.accept(pair);
            if ("70".equals(pair.code())) {
                Integer flags = parseInteger(pair.value(), 0);
                closed = flags != null && (flags & 1) == 1;
            }
        }
        Geometry geometry = buildPolylineGeometry(coordinates, closed, base.elevation);
        return new ParsedEntity(geometry != null ? base.toRecord(geometry, closed) : null, i);
    }

    private static ParsedVertex parseVertex(List<DxfPair> pairs, int index) {
        Double x = null;
        Double y = null;
        Double z = null;
        int next = findNextEntityIndex(pairs, index + 1);
        for (int i = index + 1; i < next; i++) {
            DxfPair pair = pairs.get(i);
            switch (pair.code()) {
                case "10" -> x = parseDouble(pair.value(), x);
                case "20" -> y = parseDouble(pair.value(), y);
                case "30" -> z = parseDouble(pair.value(), z);
                default -> {
                }
            }
        }
        Coordinate coordinate = (x != null && y != null) ? new Coordinate(x, y, z != null ? z : 0d) : null;
        return new ParsedVertex(coordinate, next);
    }

    private static ParsedEntity skipUnknownEntity(List<DxfPair> pairs, int index) {
        return new ParsedEntity(null, findNextEntityIndex(pairs, index + 1));
    }

    private static int findNextEntityIndex(List<DxfPair> pairs, int fromIndex) {
        for (int i = fromIndex; i < pairs.size(); i++) {
            if ("0".equals(pairs.get(i).code())) {
                return i;
            }
        }
        return pairs.size();
    }

    private static Geometry buildPolylineGeometry(List<Coordinate> coordinates, boolean closed, Double elevation) {
        if (coordinates == null || coordinates.size() < 2) {
            return null;
        }
        Coordinate[] prepared = applyElevation(coordinates, elevation);
        if (closed && prepared.length >= 3) {
            Coordinate[] shell = ensureClosed(prepared);
            try {
                Polygon polygon = GEOMETRY_FACTORY.createPolygon(shell);
                if (polygon != null && !polygon.isEmpty()) {
                    return polygon;
                }
            } catch (Exception ignored) {
            }
            try {
                return GEOMETRY_FACTORY.createLineString(shell);
            } catch (Exception ignored) {
                return null;
            }
        }
        return GEOMETRY_FACTORY.createLineString(prepared);
    }

    private static Coordinate[] applyElevation(List<Coordinate> coordinates, Double elevation) {
        Coordinate[] prepared = new Coordinate[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            Coordinate source = coordinates.get(i);
            double z = source != null && Double.isFinite(source.getZ()) ? source.getZ() : (elevation != null ? elevation : 0d);
            prepared[i] = source != null ? new Coordinate(source.x, source.y, z) : new Coordinate();
        }
        return prepared;
    }

    private static Coordinate[] ensureClosed(Coordinate[] coordinates) {
        if (coordinates == null || coordinates.length == 0) {
            return new Coordinate[0];
        }
        Coordinate first = coordinates[0];
        Coordinate last = coordinates[coordinates.length - 1];
        if (first.equals2D(last)) {
            return coordinates;
        }
        Coordinate[] closed = new Coordinate[coordinates.length + 1];
        System.arraycopy(coordinates, 0, closed, 0, coordinates.length);
        closed[closed.length - 1] = new Coordinate(first);
        return closed;
    }

    private static LineString buildArcGeometry(double centerX, double centerY, double radius, double startAngle, double endAngle, boolean closed) {
        double start = normalizeAngle(startAngle);
        double end = normalizeAngle(endAngle);
        if (closed || end <= start) {
            end += 360d;
        }
        double sweep = Math.max(4d, end - start);
        int segments = Math.max(12, Math.min(96, (int) Math.round(sweep / 7.5d)));
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i <= segments; i++) {
            double factor = i / (double) segments;
            double angle = Math.toRadians(start + (sweep * factor));
            coordinates.add(new Coordinate(centerX + (radius * Math.cos(angle)), centerY + (radius * Math.sin(angle))));
        }
        Coordinate[] sequence = coordinates.toArray(new Coordinate[0]);
        if (closed) {
            sequence = ensureClosed(sequence);
        }
        return GEOMETRY_FACTORY.createLineString(sequence);
    }

    private static double normalizeAngle(double angle) {
        double normalized = angle % 360d;
        return normalized < 0d ? normalized + 360d : normalized;
    }

    private static SimpleFeatureType buildFeatureType(File file, Class<? extends Geometry> geometryBinding, LinkedHashMap<String, Class<?>> attributes) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(safeTypeName(stripExtension(file.getName())));
        builder.add(GEOMETRY_FIELD, geometryBinding != null ? geometryBinding : Geometry.class);
        for (Map.Entry<String, Class<?>> entry : attributes.entrySet()) {
            builder.add(entry.getKey(), entry.getValue() != null ? entry.getValue() : String.class);
        }
        return builder.buildFeatureType();
    }

    private static List<SimpleFeature> buildFeatures(List<DxfRecord> records, SimpleFeatureType featureType, LinkedHashMap<String, Class<?>> attributes) {
        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
        int index = 1;
        for (DxfRecord record : records) {
            if (record == null || record.geometry() == null || record.geometry().isEmpty()) {
                continue;
            }
            builder.set(GEOMETRY_FIELD, record.geometry());
            for (String attribute : attributes.keySet()) {
                builder.set(attribute, record.attributes().get(attribute));
            }
            features.add(builder.buildFeature("dxf." + index++));
            builder.reset();
        }
        return features;
    }

    private static String stripExtension(String name) {
        if (name == null || name.isBlank()) {
            return "DXF";
        }
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(0, idx) : name;
    }

    private static String safeTypeName(String text) {
        String name = text != null ? text.trim().replaceAll("[^A-Za-z0-9_]+", "_") : "";
        if (name.isBlank()) {
            name = "dxf_layer";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "dxf_" + name;
        }
        return name;
    }

    private static String describeUnits(Integer code) {
        if (code == null) {
            return "";
        }
        return switch (code) {
            case 0 -> "unitless";
            case 1 -> "pulgadas";
            case 2 -> "pies";
            case 3 -> "millas";
            case 4 -> "milimetros";
            case 5 -> "centimetros";
            case 6 -> "metros";
            case 7 -> "kilometros";
            case 8 -> "microinches";
            case 9 -> "mils";
            case 10 -> "yardas";
            default -> "codigo " + code;
        };
    }

    private static Double parseDouble(String value, Double fallback) {
        try {
            return value != null ? Double.parseDouble(value.trim()) : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static Integer parseInteger(String value, Integer fallback) {
        try {
            return value != null ? Integer.parseInt(value.trim()) : fallback;
        } catch (Exception ex) {
            return fallback;
        }
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\P", " ").replace("\\p", " ").trim().replaceAll("\\s+", " ");
    }

    private record DxfPair(String code, String value) {
    }

    private record DxfParseResult(List<DxfRecord> records, String insUnitsLabel) {
    }

    private record ParsedEntity(DxfRecord record, int nextIndex) {
    }

    private record ParsedVertex(Coordinate coordinate, int nextIndex) {
    }

    private record DxfRecord(Geometry geometry, LinkedHashMap<String, Object> attributes) {
    }

    private static final class EntityBase {
        private final String entityType;
        private String cadLayer = "0";
        private String handle = "";
        private Integer colorIndex = null;
        private String lineType = "";
        private String text = "";
        private Double elevation = null;

        private EntityBase(String entityType) {
            this.entityType = entityType;
        }

        private void accept(DxfPair pair) {
            if (pair == null) {
                return;
            }
            switch (pair.code()) {
                case "5" -> handle = pair.value() != null ? pair.value().trim() : "";
                case "6" -> lineType = pair.value() != null ? pair.value().trim() : "";
                case "8" -> cadLayer = pair.value() != null && !pair.value().isBlank() ? pair.value().trim() : "0";
                case "38" -> elevation = parseDouble(pair.value(), elevation);
                case "62" -> colorIndex = parseInteger(pair.value(), colorIndex);
                default -> {
                }
            }
        }

        private DxfRecord toRecord(Geometry geometry, boolean closed) {
            LinkedHashMap<String, Object> attributes = new LinkedHashMap<>();
            attributes.put("entity_type", entityType);
            attributes.put("cad_layer", cadLayer);
            attributes.put("cad_handle", handle);
            attributes.put("cad_color", colorIndex);
            attributes.put("cad_ltype", lineType);
            attributes.put("text", text != null && !text.isBlank() ? text : null);
            attributes.put("closed", closed);
            attributes.put("elev_z", elevation);
            return new DxfRecord(geometry, attributes);
        }
    }
}
