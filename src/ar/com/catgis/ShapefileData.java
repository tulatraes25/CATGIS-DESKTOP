package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.locationtech.jts.geom.Envelope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShapefileData {
    private final List<SimpleFeature> features;
    private final Envelope envelope;
    private final String sourceName;
    private final int featureCount;
    private final String message;
    private final SimpleFeatureType schema;
    private final SimpleFeatureCollection featureCollection;

    public ShapefileData(SimpleFeatureCollection featureCollection, String sourceName, int featureCount) {
        this(featureCollection, sourceName, featureCount, "Shapefile cargado correctamente.");
    }

    public ShapefileData(SimpleFeatureCollection featureCollection, String sourceName, int featureCount, String message) {
        this.schema = featureCollection != null ? featureCollection.getSchema() : null;
        this.sourceName = sourceName;
        this.featureCount = featureCount;
        this.message = message != null ? message : "";
        this.envelope = featureCollection != null ? featureCollection.getBounds() : null;
        this.features = new ArrayList<>();

        if (featureCollection != null) {
            try (org.geotools.feature.FeatureIterator<SimpleFeature> it = featureCollection.features()) {
                while (it.hasNext()) {
                    this.features.add(it.next());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        this.featureCollection = buildFeatureCollection(this.features, this.schema);
    }

    public ShapefileData(List<SimpleFeature> features, Envelope envelope, String sourceName, int featureCount, String message) {
        this(features, envelope, sourceName, featureCount, message, inferSchemaStatic(features));
    }

    public ShapefileData(List<SimpleFeature> features,
                         Envelope envelope,
                         String sourceName,
                         int featureCount,
                         String message,
                         SimpleFeatureType schema) {
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
        this.envelope = envelope;
        this.sourceName = sourceName;
        this.featureCount = featureCount;
        this.message = message != null ? message : "";
        this.schema = schema != null ? schema : inferSchemaStatic(this.features);
        this.featureCollection = buildFeatureCollection(this.features, this.schema);
    }

    public ShapefileData(List<?> rawFeatures, Object envelopeObj, String sourceName, int featureCount, String message) {
        this.features = new ArrayList<>();
        if (rawFeatures != null) {
            for (Object obj : rawFeatures) {
                if (obj instanceof SimpleFeature) {
                    this.features.add((SimpleFeature) obj);
                }
            }
        }

        if (envelopeObj instanceof Envelope) {
            this.envelope = (Envelope) envelopeObj;
        } else {
            this.envelope = null;
        }

        this.sourceName = sourceName;
        this.featureCount = featureCount;
        this.message = message != null ? message : "";
        this.schema = inferSchemaStatic(this.features);
        this.featureCollection = buildFeatureCollection(this.features, this.schema);
    }

    private SimpleFeatureCollection buildFeatureCollection(List<SimpleFeature> features, SimpleFeatureType schema) {
        if (schema != null) {
            return new ListFeatureCollection(schema, features != null ? features : Collections.emptyList());
        }

        DefaultFeatureCollection collection = new DefaultFeatureCollection();
        if (features != null) {
            for (SimpleFeature f : features) {
                if (f != null) {
                    collection.add(f);
                }
            }
        }
        return collection;
    }

    private static SimpleFeatureType inferSchemaStatic(List<SimpleFeature> features) {
        if (features == null) {
            return null;
        }
        for (SimpleFeature feature : features) {
            if (feature != null && feature.getFeatureType() != null) {
                return feature.getFeatureType();
            }
        }
        return null;
    }

    public List<SimpleFeature> getFeatures() {
        return features;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public String getSourceName() {
        return sourceName;
    }

    public int getFeatureCount() {
        return featureCount;
    }

    public String getMessage() {
        return message;
    }

    public SimpleFeatureType getSchema() {
        return schema;
    }

    public SimpleFeatureCollection getFeatureCollection() {
        return featureCollection;
    }

    public List<String> getAttributeNames() {
        List<String> names = new ArrayList<>();

        SimpleFeatureType currentSchema = schema != null
                ? schema
                : featureCollection != null ? featureCollection.getSchema() : null;

        if (currentSchema == null) {
            return names;
        }

        currentSchema.getAttributeDescriptors().forEach(descriptor -> {
            String name = descriptor.getLocalName();
            if (!"the_geom".equalsIgnoreCase(name) && !"geom".equalsIgnoreCase(name)) {
                names.add(name);
            }
        });

        return names;
    }
}
