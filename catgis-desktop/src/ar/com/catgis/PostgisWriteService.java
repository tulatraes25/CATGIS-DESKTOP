package ar.com.catgis;
import ar.com.catgis.data.vector.ShapefileData;
import ar.com.catgis.data.vector.VectorLayerUtils;
import ar.com.catgis.core.model.Layer;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class PostgisWriteService {

    private PostgisWriteService() {
    }

    public record WriteRequest(Layer sourceLayer,
                               ShapefileData sourceData,
                               PostgisConnectionInfo connectionInfo,
                               String schemaName,
                               String tableName,
                               PostgisTableWriteMode mode,
                               boolean editableAfterLoad) {
    }

    public record WriteResult(PostgisLayer layer,
                              ShapefileData data,
                              int writtenFeatureCount,
                              boolean createdTable,
                              String typeName) {
    }

    public static String suggestTableName(String text) {
        if (text == null || text.isBlank()) {
            return "capa_vectorial";
        }
        String normalized = text.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_\\s]+", " ")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+", "")
                .replaceAll("_+$", "");
        if (normalized.isBlank()) {
            normalized = "capa_vectorial";
        }
        if (!Character.isLetter(normalized.charAt(0)) && normalized.charAt(0) != '_') {
            normalized = "layer_" + normalized;
        }
        return normalized;
    }

    static String validateIdentifier(String value, String label) {
        String normalized = value != null ? value.trim() : "";
        if (normalized.isBlank()) {
            return "Completa " + label + ".";
        }
        if (!normalized.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return label + " debe empezar con letra o guion bajo y solo puede contener letras, numeros o guion bajo.";
        }
        return "";
    }

    static String findMatchingTypeName(String[] typeNames, String schemaName, String tableName) {
        if (typeNames == null || tableName == null || tableName.isBlank()) {
            return null;
        }
        String normalizedSchema = schemaName != null && !schemaName.isBlank() ? schemaName.trim() : "public";
        for (String typeName : typeNames) {
            if (typeName == null || typeName.isBlank()) {
                continue;
            }
            String resolvedSchema = resolveSchemaName(typeName, normalizedSchema);
            String resolvedTable = resolveTableName(typeName);
            if (normalizedSchema.equalsIgnoreCase(resolvedSchema) && tableName.equalsIgnoreCase(resolvedTable)) {
                return typeName;
            }
        }
        return null;
    }

    public static boolean saveLayerToCurrentPath(PostgisLayer layer,
                                                 ShapefileData data,
                                                 Component parent,
                                                 boolean showSuccessMessage) {
        if (layer == null || data == null) {
            return false;
        }

        PostgisConnectionInfo info = PostgisConnectionStore.applyStoredPassword(layer.toConnectionInfo());
        if (info == null || info.getPassword().isBlank()) {
            info = PostgisConnectionStore.promptForPassword(
                    parent,
                    layer.toConnectionInfo(),
                    "Ingresá la clave para guardar cambios en la tabla PostGIS."
            );
            if (info == null) {
                return false;
            }
        }

        try {
            WriteResult result = writeLayer(new WriteRequest(
                    layer,
                    data,
                    info,
                    layer.getSchemaName(),
                    layer.getTableName(),
                    PostgisTableWriteMode.OVERWRITE_CONTENT,
                    true
            ));
            layer.setSourceCRS(result.layer().getSourceCRS());
            layer.setGeometryTypeLabel(result.layer().getGeometryTypeLabel());
            layer.setFeatureCount(result.data().getFeatureCount());
            layer.setReadOnly(false);
            if (CatgisDesktopApp.mapPanel != null) {
                CatgisDesktopApp.mapPanel.addOrUpdateShapefileLayer(layer, result.data());
                CatgisDesktopApp.mapPanel.refreshMap();
            }
            CatgisDesktopApp.markProjectDirty();
            if (result != null && showSuccessMessage) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Cambios guardados correctamente en PostGIS:\n"
                                + result.layer().getSchemaName() + "." + result.layer().getTableName()
                );
            }
            if (result != null && CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Cambios guardados en PostGIS: " + result.layer().getTableName());
            }
            return result != null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, PostgisErrorSupport.toUserMessage(ex, info), "PostGIS", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static WriteResult writeLayer(WriteRequest request) throws Exception {
        String validationMessage = validateRequest(request);
        if (!validationMessage.isBlank()) {
            throw new IllegalArgumentException(validationMessage);
        }

        Layer sourceLayer = request.sourceLayer();
        ShapefileData sourceData = request.sourceData();
        PostgisConnectionInfo connectionInfo = request.connectionInfo().copy();
        String schemaName = request.schemaName() != null && !request.schemaName().isBlank()
                ? request.schemaName().trim()
                : connectionInfo.getSchema();
        String tableName = request.tableName().trim();
        connectionInfo.setSchema(schemaName);

        DataStore store = PostgisLoader.openDataStore(connectionInfo);
        try {
            String existingTypeName = findMatchingTypeName(store.getTypeNames(), schemaName, tableName);
            SimpleFeatureType existingSchema = existingTypeName != null ? store.getSchema(existingTypeName) : null;
            String targetCrsCode = resolveTargetCrsCode(sourceLayer, sourceData, existingSchema);

            ExportVectorLayerAction.TransformResult transformed = ExportVectorLayerAction.transformFeaturesToTarget(
                    sourceLayer,
                    sourceData,
                    targetCrsCode,
                    "POSTGIS"
            );

            boolean createdTable = false;
            String typeNameForWrite = existingTypeName;
            SimpleFeatureType targetSchema;

            switch (request.mode()) {
                case CREATE_NEW:
                    if (existingTypeName != null) {
                        throw new IllegalArgumentException("La tabla destino ya existe en el schema indicado.");
                    }
                    targetSchema = buildTargetSchema(transformed.featureType, tableName, targetCrsCode);
                    store.createSchema(targetSchema);
                    createdTable = true;
                    typeNameForWrite = findMatchingTypeName(store.getTypeNames(), schemaName, tableName);
                    break;
                case REPLACE_TABLE:
                    if (existingTypeName != null) {
                        store.removeSchema(existingTypeName);
                    }
                    targetSchema = buildTargetSchema(transformed.featureType, tableName, targetCrsCode);
                    store.createSchema(targetSchema);
                    createdTable = true;
                    typeNameForWrite = findMatchingTypeName(store.getTypeNames(), schemaName, tableName);
                    break;
                case APPEND_RECORDS:
                    if (existingTypeName == null || existingSchema == null) {
                        throw new IllegalArgumentException("La tabla destino no existe para anexar registros.");
                    }
                    targetSchema = existingSchema;
                    break;
                case OVERWRITE_CONTENT:
                    if (existingTypeName == null || existingSchema == null) {
                        targetSchema = buildTargetSchema(transformed.featureType, tableName, targetCrsCode);
                        store.createSchema(targetSchema);
                        createdTable = true;
                        typeNameForWrite = findMatchingTypeName(store.getTypeNames(), schemaName, tableName);
                    } else {
                        targetSchema = existingSchema;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Modo de escritura PostGIS no soportado.");
            }

            if (typeNameForWrite == null || typeNameForWrite.isBlank()) {
                typeNameForWrite = schemaName + "." + tableName;
            }
            if (targetSchema == null) {
                targetSchema = store.getSchema(typeNameForWrite);
            }
            if (targetSchema == null) {
                throw new IllegalArgumentException("No se pudo reconstruir el esquema destino de PostGIS.");
            }

            List<SimpleFeature> adaptedFeatures = adaptFeaturesToSchema(transformed.features, targetSchema);
            writeFeatures(store, typeNameForWrite, adaptedFeatures, targetSchema, request.mode());

            PostgisLayer resultLayer = buildResultLayer(sourceLayer, connectionInfo, schemaName, tableName, typeNameForWrite, targetSchema, request.editableAfterLoad());
            ShapefileData loadedData = PostgisLoader.loadLayerData(resultLayer, connectionInfo);
            resultLayer.setFeatureCount(loadedData.getFeatureCount());
            return new WriteResult(resultLayer, loadedData, adaptedFeatures.size(), createdTable, typeNameForWrite);
        } finally {
            store.dispose();
        }
    }

    private static void writeFeatures(DataStore store,
                                      String typeName,
                                      List<SimpleFeature> features,
                                      SimpleFeatureType targetSchema,
                                      PostgisTableWriteMode mode) throws Exception {
        SimpleFeatureSource source = store.getFeatureSource(typeName);
        if (!(source instanceof SimpleFeatureStore featureStore)) {
            throw new IllegalArgumentException("La tabla PostGIS no admite escritura con la conexion actual.");
        }

        Transaction transaction = new DefaultTransaction("catgis-postgis-write");
        try {
            featureStore.setTransaction(transaction);
            if (mode == PostgisTableWriteMode.OVERWRITE_CONTENT) {
                featureStore.removeFeatures(Filter.INCLUDE);
                if (!features.isEmpty()) {
                    featureStore.addFeatures(new ListFeatureCollection(targetSchema, features));
                }
            } else if (mode == PostgisTableWriteMode.APPEND_RECORDS) {
                if (!features.isEmpty()) {
                    featureStore.addFeatures(new ListFeatureCollection(targetSchema, features));
                }
            } else {
                featureStore.removeFeatures(Filter.INCLUDE);
                if (!features.isEmpty()) {
                    featureStore.addFeatures(new ListFeatureCollection(targetSchema, features));
                }
            }
            transaction.commit();
        } catch (Exception ex) {
            transaction.rollback();
            throw ex;
        } finally {
            transaction.close();
        }
    }

    private static PostgisLayer buildResultLayer(Layer sourceLayer,
                                                 PostgisConnectionInfo info,
                                                 String schemaName,
                                                 String tableName,
                                                 String typeName,
                                                 SimpleFeatureType schema,
                                                 boolean editableAfterLoad) {
        String layerName = sourceLayer != null && sourceLayer.getName() != null && !sourceLayer.getName().isBlank()
                ? sourceLayer.getName()
                : tableName;
        PostgisLayer result = new PostgisLayer(layerName);
        result.setConnectionInfo(info);
        result.setSchemaName(schemaName);
        result.setTableName(tableName);
        result.setTypeName(typeName);
        result.setReadOnly(!editableAfterLoad);
        result.setSourceName(info.getDatabase());
        result.setSourceCRS(resolveSchemaCrs(schema));
        result.setGeometryTypeLabel(schema != null && schema.getGeometryDescriptor() != null
                ? VectorLayerUtils.describeGeometryBinding(schema.getGeometryDescriptor().getType().getBinding())
                : "");
        if (sourceLayer != null) {
            result.setLabelsVisible(sourceLayer.isLabelsVisible());
            result.setLabelField(sourceLayer.getLabelField());
            result.setFillColor(sourceLayer.getFillColor());
            result.setBorderColor(sourceLayer.getBorderColor());
            result.setLineColor(sourceLayer.getLineColor());
            result.setLineWidth(sourceLayer.getLineWidth());
            result.setPointColor(sourceLayer.getPointColor());
            result.setPointSize(sourceLayer.getPointSize());
            result.setPointSymbolStyle(sourceLayer.getPointSymbolStyle());
            result.setLineSymbolStyle(sourceLayer.getLineSymbolStyle());
            result.setPolygonFillStyle(sourceLayer.getPolygonFillStyle());
        }
        return result;
    }

    private static String validateRequest(WriteRequest request) {
        if (request == null) {
            return "No se pudo construir la operacion PostGIS.";
        }
        if (request.sourceLayer() == null) {
            return "Seleccioná una capa vectorial para enviar a CATSERVER.";
        }
        if (request.sourceLayer() instanceof RasterLayer) {
            return "La capa seleccionada no es vectorial.";
        }
        if (!ExportVectorLayerAction.hasExportableVectorData(request.sourceData())) {
            return "La capa no tiene datos vectoriales disponibles para enviar a CATSERVER.";
        }
        String connectionValidation = PostgisErrorSupport.validateConnectionInfo(request.connectionInfo(), true);
        if (!connectionValidation.isBlank()) {
            return connectionValidation;
        }
        String schemaValidation = validateIdentifier(request.schemaName(), "El schema");
        if (!schemaValidation.isBlank()) {
            return schemaValidation;
        }
        String tableValidation = validateIdentifier(request.tableName(), "El nombre de tabla");
        if (!tableValidation.isBlank()) {
            return tableValidation;
        }
        if (request.sourceData().getSchema() == null || request.sourceData().getSchema().getGeometryDescriptor() == null) {
            return "La capa necesita una geometria vectorial valida para escribir en PostGIS.";
        }
        return "";
    }

    private static List<SimpleFeature> adaptFeaturesToSchema(List<SimpleFeature> sourceFeatures, SimpleFeatureType targetSchema) {
        List<SimpleFeature> adapted = new ArrayList<>();
        if (targetSchema == null) {
            return adapted;
        }
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(targetSchema);
        int index = 1;
        for (SimpleFeature feature : sourceFeatures) {
            for (AttributeDescriptor descriptor : targetSchema.getAttributeDescriptors()) {
                String attributeName = descriptor.getLocalName();
                Object value = feature != null ? feature.getAttribute(attributeName) : null;
                if (descriptor instanceof GeometryDescriptor geometryDescriptor) {
                    Geometry geometry = value instanceof Geometry ? (Geometry) value : null;
                    Class<?> binding = geometryDescriptor.getType() != null ? geometryDescriptor.getType().getBinding() : Geometry.class;
                    if (geometry != null && binding != null && Geometry.class.isAssignableFrom(binding)) {
                        geometry = VectorLayerUtils.normalizeGeometryForBinding(geometry, binding.asSubclass(Geometry.class));
                    }
                    builder.add(geometry);
                } else {
                    Class<?> binding = descriptor.getType() != null ? descriptor.getType().getBinding() : String.class;
                    builder.add(coerceAttributeValue(value, binding));
                }
            }
            adapted.add(builder.buildFeature(String.valueOf(index++)));
            builder.reset();
        }
        return adapted;
    }

    private static Object coerceAttributeValue(Object value, Class<?> targetBinding) {
        if (value == null || targetBinding == null) {
            return value;
        }
        if (targetBinding.isInstance(value)) {
            return value;
        }
        if (Date.class.isAssignableFrom(targetBinding)) {
            if (value instanceof Date) {
                if (targetBinding == java.sql.Date.class) {
                    return new java.sql.Date(((Date) value).getTime());
                }
                if (targetBinding == Timestamp.class) {
                    return new Timestamp(((Date) value).getTime());
                }
                return value;
            }
            return null;
        }
        if (String.class.isAssignableFrom(targetBinding)) {
            return String.valueOf(value);
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            if (Integer.class.isAssignableFrom(targetBinding) || int.class.isAssignableFrom(targetBinding)) {
                return Integer.parseInt(text);
            }
            if (Long.class.isAssignableFrom(targetBinding) || long.class.isAssignableFrom(targetBinding)) {
                return Long.parseLong(text);
            }
            if (Double.class.isAssignableFrom(targetBinding) || double.class.isAssignableFrom(targetBinding)) {
                return Double.parseDouble(text.replace(',', '.'));
            }
            if (Float.class.isAssignableFrom(targetBinding) || float.class.isAssignableFrom(targetBinding)) {
                return Float.parseFloat(text.replace(',', '.'));
            }
            if (Boolean.class.isAssignableFrom(targetBinding) || boolean.class.isAssignableFrom(targetBinding)) {
                return "true".equalsIgnoreCase(text) || "1".equals(text) || "si".equalsIgnoreCase(text);
            }
        } catch (Exception ignored) {
        }
        return String.class.isAssignableFrom(targetBinding) ? text : null;
    }

    private static SimpleFeatureType buildTargetSchema(SimpleFeatureType baseType, String tableName, String targetCrsCode) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(tableName);
        CoordinateReferenceSystem targetCrs = !targetCrsCode.isBlank()
                ? CRSDefinitions.decode(targetCrsCode, true)
                : null;

        for (AttributeDescriptor descriptor : baseType.getAttributeDescriptors()) {
            if (descriptor instanceof GeometryDescriptor gd) {
                if (targetCrs != null) {
                    builder.setCRS(targetCrs);
                }
                Class<?> binding = gd.getType() != null && gd.getType().getBinding() != null
                        ? gd.getType().getBinding()
                        : Geometry.class;
                builder.add(gd.getLocalName(), binding);
            } else {
                builder.add(descriptor);
            }
        }
        return builder.buildFeatureType();
    }

    private static String resolveTargetCrsCode(Layer sourceLayer, ShapefileData data, SimpleFeatureType existingSchema) {
        String existing = resolveSchemaCrs(existingSchema);
        if (!existing.isBlank()) {
            return existing;
        }
        if (sourceLayer != null && sourceLayer.getSourceCRS() != null && !sourceLayer.getSourceCRS().isBlank()) {
            return CRSDefinitions.normalizeCode(sourceLayer.getSourceCRS());
        }
        if (data != null && data.getSchema() != null) {
            String schemaCode = resolveSchemaCrs(data.getSchema());
            if (!schemaCode.isBlank()) {
                return schemaCode;
            }
        }
        if (CatgisDesktopApp.currentProject != null
                && CatgisDesktopApp.currentProject.getProjectCRS() != null
                && !CatgisDesktopApp.currentProject.getProjectCRS().isBlank()) {
            return CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS());
        }
        return "EPSG:4326";
    }

    private static String resolveSchemaCrs(SimpleFeatureType schema) {
        if (schema == null || schema.getCoordinateReferenceSystem() == null) {
            return "";
        }
        try {
            return CRSDefinitions.normalizeCode(CRS.toSRS(schema.getCoordinateReferenceSystem(), true));
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String resolveSchemaName(String typeName, String fallbackSchema) {
        if (typeName != null) {
            int idx = typeName.indexOf('.');
            if (idx > 0) {
                return typeName.substring(0, idx);
            }
        }
        return fallbackSchema != null && !fallbackSchema.isBlank() ? fallbackSchema : "public";
    }

    private static String resolveTableName(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            return "";
        }
        int idx = typeName.lastIndexOf('.');
        return idx >= 0 ? typeName.substring(idx + 1) : typeName;
    }
}
