/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.hardcode.gdbms.engine.values.NullValue
 *  com.iver.cit.gvsig.fmap.spatialindex.IPersistentSpatialIndex
 *  com.iver.cit.gvsig.fmap.spatialindex.ISpatialIndex
 *  com.iver.cit.gvsig.fmap.spatialindex.QuadtreeJts
 *  com.iver.cit.gvsig.fmap.spatialindex.SpatialIndexException
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.collections.list.GrowthList
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package org.saig.core.dao.datasource.filedatasource.shape;

import com.hardcode.gdbms.engine.values.NullValue;
import com.iver.cit.gvsig.fmap.spatialindex.IPersistentSpatialIndex;
import com.iver.cit.gvsig.fmap.spatialindex.ISpatialIndex;
import com.iver.cit.gvsig.fmap.spatialindex.QuadtreeGt2;
import com.iver.cit.gvsig.fmap.spatialindex.QuadtreeJts;
import com.iver.cit.gvsig.fmap.spatialindex.SpatialIndexException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.model.IQueryable;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.list.GrowthList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.SortedAttribute;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeConnection;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.dao.datasource.filedatasource.shape.iterators.ShapeEditionIterator;
import org.saig.core.dao.datasource.filedatasource.shape.iterators.ShapeFullIterator;
import org.saig.core.dao.datasource.filedatasource.shape.iterators.ShapeIterator;
import org.saig.core.dao.datasource.filedatasource.shape.writers.SHP;
import org.saig.core.dao.datasource.memory.CollectionIterator;
import org.saig.core.dao.datasource.memory.FeatureDatasetIterator;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.LogicFilterImpl;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.AttributeCalculate;
import org.saig.core.model.feature.DummyFeatureIterator;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.stats.StatsOperatorsFactory;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class ShapeFileDataSource
extends AbstractDataSource
implements Cloneable,
IQueryable {
    private static final Logger LOGGER = Logger.getLogger(ShapeFileDataSource.class);
    private static final GeometryFactory geomFact = new GeometryFactory();
    protected static final Set<String> AVAILABLE_SHAPE_OPERATORS = new HashSet<String>();
    public static final Charset DEFAULT_STRING_CHARSET;
    private static final Number NULL_NUMBER;
    private static final String NULL_STRING = "";
    private static final String NULL_DATE = "        ";
    private ISpatialIndex spatialIndex;
    private File file;
    private File dbfFile;
    private int type;
    private int numReg;
    private int contador;
    private Rectangle2D extent;
    private String shapePath;
    private BitSet updatedKeys;
    private BitSet deletedKeys;
    private BitSet newKeys;
    private BitSet selectionKeys;
    private Feature[] modFeats;
    private GrowthList newFeats;
    private Feature firstFeature;
    private Charset dbfCharset;

    static {
        AVAILABLE_SHAPE_OPERATORS.add("OP_AVG");
        AVAILABLE_SHAPE_OPERATORS.add("OP_COUNT");
        AVAILABLE_SHAPE_OPERATORS.add("OP_COUNT_NO");
        AVAILABLE_SHAPE_OPERATORS.add("OP_COUNT_YES");
        AVAILABLE_SHAPE_OPERATORS.add("OP_FIRST");
        AVAILABLE_SHAPE_OPERATORS.add("OP_LAST");
        AVAILABLE_SHAPE_OPERATORS.add("OP_MAX");
        AVAILABLE_SHAPE_OPERATORS.add("OP_MIN");
        AVAILABLE_SHAPE_OPERATORS.add("OP_STANDARD_DEVIANCE");
        AVAILABLE_SHAPE_OPERATORS.add("OP_SUM");
        AVAILABLE_SHAPE_OPERATORS.add("OP_VARIANCE");
        DEFAULT_STRING_CHARSET = Charset.forName("ISO-8859-1");
        NULL_NUMBER = new Integer(0);
    }

    public ShapeFileDataSource() {
        this(DEFAULT_STRING_CHARSET);
    }

    public ShapeFileDataSource(Charset selectedCharset) {
        this.dbfCharset = selectedCharset;
    }

    @Override
    public void add(Feature feature) throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        this.addAll(features);
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        if (this.inMemory) {
            for (Feature element : features) {
                if (element.isUnsaved()) {
                    Integer newKey = new Integer(this.contador);
                    element.setAttribute(this.schema.getPrimaryKeyIndex(), (Object)newKey);
                    int index = this.contador - this.numReg;
                    this.newFeats.set(index, (Object)element);
                    this.newKeys.set(index);
                    ++this.contador;
                    continue;
                }
                int index = element.getPrimaryKeyAsInt();
                if (index >= this.numReg) {
                    this.newFeats.set(index -= this.numReg, (Object)element);
                    this.newKeys.set(index);
                    continue;
                }
                if (this.deletedKeys.get(index)) {
                    this.deletedKeys.clear(index);
                    this.modFeats[index] = null;
                }
                this.updatedKeys.set(index);
                this.modFeats[index] = element;
            }
            return;
        }
    }

    @Override
    public List<Feature> getByAttribute(String[] attributeNames, Object[] attributeValues) {
        return this.getByAttribute(attributeNames, attributeValues, null, null);
    }

    @Override
    public List<Feature> getFeatures() {
        ArrayList<Feature> result = new ArrayList<Feature>();
        FeatureIterator itFeats = null;
        try {
            try {
                itFeats = this.getFeaturesIterator();
                while (itFeats.hasNext()) {
                    Feature feat = itFeats.next();
                    result.add(feat);
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
                if (itFeats != null) {
                    itFeats.close();
                }
            }
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
        }
        return result;
    }

    @Override
    public FeatureIterator getFeaturesIterator() {
        return this.getFeaturesIterator(null, null);
    }

    public FeatureIterator getFeaturesIterator(Envelope env, Filter filter) {
        FeatureIterator iterator = null;
        try {
            iterator = this.editable ? new ShapeEditionIterator(this, filter, env) : new ShapeIterator(this, filter, env);
            return iterator;
        }
        catch (Exception e) {
            LOGGER.error((Object)NULL_STRING, (Throwable)e);
            if (iterator != null) {
                iterator.close();
            }
            return new DummyFeatureIterator();
        }
    }

    @Override
    public Envelope getViewBox() {
        block18: {
            if (this.envelope == null) {
                ShapeConnection con = this.getConnection();
                try {
                    try {
                        con.open();
                        con.initialize();
                        Rectangle2D rec = con.getFullExtent();
                        this.envelope = new Envelope(rec.getMinX(), rec.getMaxX(), rec.getMinY(), rec.getMaxY());
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)NULL_STRING, (Throwable)e);
                        if (con != null) {
                            try {
                                con.close();
                            }
                            catch (Exception e2) {
                                LOGGER.error((Object)NULL_STRING, (Throwable)e2);
                            }
                        }
                        break block18;
                    }
                }
                catch (Throwable throwable) {
                    if (con != null) {
                        try {
                            con.close();
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)NULL_STRING, (Throwable)e);
                        }
                    }
                    throw throwable;
                }
                if (con != null) {
                    try {
                        con.close();
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)NULL_STRING, (Throwable)e);
                    }
                }
            }
        }
        Envelope fullEnvelope = new Envelope(this.envelope);
        if (this.inMemory) {
            Feature feat;
            int i = this.newKeys.nextSetBit(0);
            while (i >= 0) {
                feat = (Feature)this.newFeats.get(i);
                fullEnvelope.expandToInclude(feat.getGeometry().getEnvelopeInternal());
                i = this.newKeys.nextSetBit(i + 1);
            }
            i = this.updatedKeys.nextSetBit(0);
            while (i >= 0) {
                feat = this.modFeats[i];
                fullEnvelope.expandToInclude(feat.getGeometry().getEnvelopeInternal());
                i = this.updatedKeys.nextSetBit(i + 1);
            }
        }
        return fullEnvelope;
    }

    @Override
    public Envelope getViewBox(Filter filter) throws Exception {
        Envelope env = new Envelope();
        FeatureIterator it = null;
        try {
            it = this.getFeaturesIterator(null, filter);
            while (it.hasNext()) {
                Feature feat = it.next();
                env.expandToInclude(feat.getGeometry().getEnvelopeInternal());
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        return env;
    }

    public boolean hasEditableFeatures() {
        return !this.deletedKeys.isEmpty() || !this.updatedKeys.isEmpty() || !this.newKeys.isEmpty();
    }

    public List<Feature> getRemovableFeatures() {
        ArrayList<Feature> editableFeatures = new ArrayList<Feature>();
        int i = this.newKeys.nextSetBit(0);
        while (i >= 0) {
            editableFeatures.add((Feature)this.newFeats.get(i));
            i = this.newKeys.nextSetBit(i + 1);
        }
        i = this.updatedKeys.nextSetBit(0);
        while (i >= 0) {
            editableFeatures.add(this.modFeats[i]);
            i = this.updatedKeys.nextSetBit(i + 1);
        }
        i = this.deletedKeys.nextSetBit(0);
        while (i >= 0) {
            editableFeatures.add(this.modFeats[i]);
            i = this.deletedKeys.nextSetBit(i + 1);
        }
        return editableFeatures;
    }

    public List<Feature> getEditableFeatures() {
        ArrayList<Feature> editableFeatures = new ArrayList<Feature>();
        int i = this.newKeys.nextSetBit(0);
        while (i >= 0) {
            editableFeatures.add((Feature)this.newFeats.get(i));
            i = this.newKeys.nextSetBit(i + 1);
        }
        i = this.updatedKeys.nextSetBit(0);
        while (i >= 0) {
            editableFeatures.add(this.modFeats[i]);
            i = this.updatedKeys.nextSetBit(i + 1);
        }
        return editableFeatures;
    }

    @Override
    public List<Feature> query(Envelope rectangle) throws Exception {
        return this.query(rectangle, null);
    }

    @Override
    public List<Feature> query(Filter filter) throws Exception {
        return this.query(null, filter);
    }

    @Override
    public List<Feature> query(Envelope view, Filter filter) throws Exception {
        ArrayList<Feature> result = new ArrayList<Feature>();
        if (view != null && !view.intersects(this.getViewBox())) {
            return result;
        }
        FeatureIterator itFeats = null;
        try {
            itFeats = this.getFeaturesIterator(view, filter);
            while (itFeats.hasNext()) {
                result.add(itFeats.next());
            }
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
        }
        return result;
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle) {
        return this.queryIterator(rectangle, null);
    }

    @Override
    public FeatureIterator queryIterator(Envelope view, Filter filter) {
        if (view != null && !view.intersects(this.getViewBox())) {
            return new DummyFeatureIterator();
        }
        try {
            return this.getFeaturesIterator(view, filter);
        }
        catch (Exception e) {
            LOGGER.error((Object)NULL_STRING, (Throwable)e);
            return new DummyFeatureIterator();
        }
    }

    @Override
    public int size() {
        return this.numReg + this.newKeys.cardinality() - this.deletedKeys.cardinality();
    }

    public int iterableRows() {
        return this.numReg + this.newKeys.length();
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
        if (this.inMemory) {
            for (Feature object : features) {
                int index = object.getPrimaryKeyAsInt();
                if (index < this.numReg) {
                    this.updatedKeys.set(index);
                    this.modFeats[index] = object;
                    this.deletedKeys.clear(index);
                    continue;
                }
                this.newFeats.set(index -= this.numReg, (Object)object);
            }
            return;
        }
    }

    @Override
    public void removeAll(Collection<Feature> features) {
        if (this.inMemory) {
            for (Feature element : features) {
                int index = element.getPrimaryKeyAsInt();
                if (index < this.numReg) {
                    this.updatedKeys.clear(index);
                    this.modFeats[index] = element;
                    this.deletedKeys.set(index);
                    continue;
                }
                this.newFeats.set(index -= this.numReg, null);
                this.newKeys.clear(index);
            }
        }
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void update(Feature feature) throws Exception {
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        this.updateAll(features);
    }

    @Override
    public void commit() throws Exception {
        if (!this.hasEditableFeatures()) {
            return;
        }
        Collection<Relation<?>> relations = this.schema.recoverRelations();
        try {
            SHP.createShapeFile(this.schema, ShapeGeometryConverter.jts_to_igeometry(this.getSampleGeometry()), this.getFeaturesIterator(), new File(this.shapePath), false, false, true, this.dbfCharset);
            this.setFile(new File(this.shapePath));
            if (relations != null) {
                this.schema.addRelations(relations);
            }
            this.envelope = null;
            if (this.spatialIndex != null) {
                this.deleteSpatialIndex();
                this.createSpatialIndex();
            }
        }
        catch (Exception ex) {
            LOGGER.error((Object)NULL_STRING, (Throwable)ex);
            throw ex;
        }
        this.initialize();
    }

    public void commit(FeatureCollection fc) throws Exception {
        FeatureSchema schema = fc.getFeatureSchema();
        Collection<Relation<?>> relations = schema.recoverRelations();
        SHP.createShapeFile(schema, ShapeGeometryConverter.jts_to_igeometry(fc.getFeaturesSamples(1).get(0).getGeometry()), fc.iterator(), new File(this.shapePath), false, false, true, this.dbfCharset);
        this.setFile(new File(this.shapePath));
        if (relations != null) {
            schema.addRelations(relations);
        }
        this.envelope = null;
        if (this.spatialIndex != null) {
            this.deleteSpatialIndex();
            this.createSpatialIndex();
        }
        this.initialize();
    }

    public void commit(Collection<Feature> fc, FeatureSchema schema) throws Exception {
        Collection<Relation<?>> relations = schema.recoverRelations();
        SHP.createShapeFile(schema, ShapeGeometryConverter.jts_to_igeometry(fc.iterator().next().getGeometry()), (FeatureIterator)new CollectionIterator(fc), new File(this.shapePath), false, false, true, this.dbfCharset);
        this.setFile(new File(this.shapePath));
        if (relations != null) {
            schema.addRelations(relations);
        }
        this.envelope = null;
        if (this.spatialIndex != null) {
            this.deleteSpatialIndex();
            this.createSpatialIndex();
        }
        this.initialize();
    }

    public static void toShape(FeatureCollection fc, String shapePath, Charset charset) throws Exception {
        SHP.createShapeFile(fc.getFeatureSchema(), ShapeGeometryConverter.jts_to_igeometry(fc.getFeaturesSamples(1).get(0).getGeometry()), fc.iterator(), new File(shapePath), false, false, false, charset);
    }

    public static void toShape(FeatureCollection fc, String shapePath, boolean savedCalculate, boolean savePk, Charset charset) throws Exception {
        SHP.createShapeFile(fc.getFeatureSchema(), ShapeGeometryConverter.jts_to_igeometry(fc.getFeaturesSamples(1).get(0).getGeometry()), fc.iterator(), new File(shapePath), savePk, savedCalculate, false, charset);
    }

    public static void toShape(FeatureCollection fc, String shapePath, long maxNumberOfFeatures) throws Exception {
        SHP.createShapeFile(fc.getFeatureSchema(), ShapeGeometryConverter.jts_to_igeometry(fc.getFeaturesSamples(1).get(0).getGeometry()), fc.iterator(), new File(shapePath), maxNumberOfFeatures, DEFAULT_STRING_CHARSET);
    }

    public static void toShape(FeatureCollection fc, String shapePath, boolean savedCalculate, boolean savePrimaryKey) throws Exception {
        SHP.createShapeFile(fc.getFeatureSchema(), ShapeGeometryConverter.jts_to_igeometry(fc.getFeaturesSamples(1).get(0).getGeometry()), fc.iterator(), new File(shapePath), savePrimaryKey, savedCalculate, false, DEFAULT_STRING_CHARSET);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public List<Feature> getFeaturesSamples(int n) {
        ArrayList<Feature> results = new ArrayList<Feature>();
        if (n == 1 && this.firstFeature != null) {
            results.add(this.firstFeature);
            return results;
        }
        int maxSamples = Math.min(n, this.getShapeCount());
        FeatureIterator itFeats = null;
        try {
            try {
                itFeats = this.getFeaturesIterator();
                int numSamples = 0;
                while (numSamples < maxSamples) {
                    if (!itFeats.hasNext()) {
                        return results;
                    }
                    results.add(itFeats.next());
                    ++numSamples;
                }
                return results;
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
                if (itFeats == null) return results;
                itFeats.close();
                return results;
            }
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
        }
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        ArrayList<Object> orderedPkList = new ArrayList<Object>(this.size());
        int i = 0;
        while (i < this.getShapeCount()) {
            if (!this.deletedKeys.get(i)) {
                orderedPkList.add(new Integer(i));
            }
            ++i;
        }
        i = this.newKeys.nextSetBit(0);
        while (i >= 0) {
            orderedPkList.add(((Feature)this.newFeats.get(i)).getPrimaryKey());
            i = this.newKeys.nextSetBit(i + 1);
        }
        return orderedPkList;
    }

    public void setFile(File file) throws Exception {
        try {
            LOGGER.info((Object)I18N.getMessage(this.getClass(), "loading-shp-file-{0}", new Object[]{file.getAbsolutePath()}));
            this.file = file;
            this.shapePath = file.getAbsolutePath();
            this.dbfFile = SHP.getDbfFile(file);
            this.initialize();
        }
        catch (Exception e) {
            LOGGER.error((Object)file.getAbsolutePath(), (Throwable)e);
            throw e;
        }
    }

    public Feature readFeature(int recordNumber, ShapeGeometry pathGeom, boolean loadJTSGeometry, ShapeConnection con) throws IOException, Exception {
        return con.readFeature(recordNumber, pathGeom, loadJTSGeometry, this.getSchema());
    }

    public Feature getRealFeature(int index, ShapeConnection con, boolean ignored) throws Exception {
        Feature solucion = null;
        if (!ignored) {
            if (this.deletedKeys.get(index)) {
                return null;
            }
            if (this.updatedKeys.get(index)) {
                solucion = this.modFeats[index];
            }
        }
        if (solucion == null) {
            int row = index;
            if (row < this.getShapeCount()) {
                solucion = con.readFeature(row, con.getShape(row), true, this.getSchema());
            } else {
                return (Feature)this.newFeats.get(row -= this.getShapeCount());
            }
        }
        return solucion;
    }

    public int getShapeCount() {
        return this.numReg;
    }

    public Geometry getSampleGeometry() {
        switch (this.type) {
            case 1: {
                return geomFact.createPoint(new Coordinate(0.0, 0.0));
            }
            case 11: {
                return geomFact.createPoint(new Coordinate(0.0, 0.0, 0.0));
            }
            case 3: {
                return geomFact.createLineString(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(1.0, 0.0)});
            }
            case 13: {
                return geomFact.createLineString(new Coordinate[]{new Coordinate(0.0, 0.0, 0.0), new Coordinate(1.0, 0.0, 0.0)});
            }
            case 5: {
                return geomFact.createPolygon(geomFact.createLinearRing(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(0.0, 1.0), new Coordinate(-1.0, -1.0), new Coordinate(-1.0, 0.0), new Coordinate(0.0, 0.0)}), null);
            }
            case 15: {
                return geomFact.createPolygon(geomFact.createLinearRing(new Coordinate[]{new Coordinate(0.0, 0.0, 0.0), new Coordinate(0.0, 1.0, 0.0), new Coordinate(-1.0, -1.0, 0.0), new Coordinate(-1.0, 0.0, 0.0), new Coordinate(0.0, 0.0, 0.0)}), null);
            }
            case 8: {
                return geomFact.createMultiPoint(new Coordinate[]{new Coordinate(0.0, 0.0), new Coordinate(1.0, 1.0)});
            }
            case 18: {
                return geomFact.createMultiPoint(new Coordinate[]{new Coordinate(0.0, 0.0, 0.0), new Coordinate(1.0, 1.0, 0.0)});
            }
        }
        return null;
    }

    public void initialize() throws Exception {
        ShapeConnection con = this.getConnection();
        try {
            con.initialize();
            this.extent = con.getFullExtent();
            this.setNumReg(con.getShapeCount());
            this.type = con.getShapeType();
            this.set3d(con.is3d());
            this.contador = con.getShapeCount();
            this.schema = con.getShapefileSchema();
            this.updatedKeys = new BitSet(con.getShapeCount());
            this.deletedKeys = new BitSet(con.getShapeCount());
            this.newKeys = new BitSet(con.getShapeCount());
            this.modFeats = new Feature[con.getShapeCount()];
            this.newFeats = new GrowthList(con.getShapeCount());
            this.selectionKeys = new BitSet(con.getShapeCount());
            this.inMemory = true;
            if (con.getShapeCount() > 0) {
                this.firstFeature = this.getRealFeature(0, con, false);
            }
        }
        finally {
            con.close();
        }
    }

    public Rectangle2D getFullExtent() throws IOException {
        return this.extent;
    }

    public boolean accept(File f) {
        return f.getName().toUpperCase().endsWith("SHP");
    }

    public int getShapeType() {
        return this.type;
    }

    public File getFile() {
        return this.file;
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        if (key == null) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.PathShapeDataSource.primary-key-null"));
            return null;
        }
        if (key instanceof Feature) {
            return (Feature)key;
        }
        ShapeConnection con = this.getConnection();
        try {
            con.open();
            if (key == null || key instanceof Feature) {
                Feature feature = (Feature)key;
                return feature;
            }
            Feature feature = this.getRealFeature(((Number)key).intValue(), con, false);
            return feature;
        }
        catch (Exception e) {
            LOGGER.error((Object)NULL_STRING, (Throwable)e);
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
            }
        }
        return null;
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        TreeSet<SortedAttribute> sort;
        block16: {
            Object key;
            Object value;
            Feature element;
            boolean isString = this.schema.getAttribute(column).getType().toJavaClass().equals(String.class);
            sort = new TreeSet<SortedAttribute>();
            if (values == null) {
                FeatureIterator iter = null;
                try {
                    try {
                        iter = this.getFeaturesIterator();
                        while (iter.hasNext()) {
                            element = iter.next();
                            value = element.getAttribute(column);
                            key = element.getPrimaryKey();
                            if (key == null) {
                                sort.add(new SortedAttribute(value, element, ascending, isString));
                                continue;
                            }
                            sort.add(new SortedAttribute(value, key, ascending, isString));
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)NULL_STRING, (Throwable)e);
                        if (iter == null) break block16;
                        iter.close();
                        break block16;
                    }
                }
                catch (Throwable throwable) {
                    if (iter != null) {
                        iter.close();
                    }
                    throw throwable;
                }
                if (iter != null) {
                    iter.close();
                }
            } else {
                int i = 0;
                while (i < values.length) {
                    element = null;
                    element = values[i] instanceof Feature ? (Feature)values[i] : this.getByPrimaryKey(values[i]);
                    if (element != null) {
                        value = element.getAttribute(column);
                        key = element.getPrimaryKey();
                        if (key == null) {
                            sort.add(new SortedAttribute(value, element, ascending, isString));
                        } else {
                            sort.add(new SortedAttribute(value, key, ascending, isString));
                        }
                    }
                    ++i;
                }
            }
        }
        ArrayList<Object> result = new ArrayList<Object>();
        for (SortedAttribute element : sort) {
            result.add(element.getRecordNumber());
        }
        sort = null;
        System.gc();
        return result;
    }

    private Object getRealField(int index, int indexColumn, ShapeConnection con) throws Exception {
        Object value = null;
        if (this.deletedKeys.get(index)) {
            return null;
        }
        if (this.updatedKeys.get(index)) {
            value = this.modFeats[index].getAttribute(indexColumn);
        }
        if (value == null) {
            int row = index;
            if (row < this.getShapeCount()) {
                value = this.schema.getAttribute(indexColumn).isPrimaryKey() ? new Integer(index) : con.readField(row, indexColumn);
            } else {
                return ((Feature)this.newFeats.get(row -= this.getShapeCount())).getAttribute(indexColumn);
            }
        }
        return value;
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys) {
        return this.getByPrimaryKey(keys, false);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys, boolean ignoredCache) {
        if (ArrayUtils.isEmpty((Object[])keys)) {
            return new ArrayList<Feature>();
        }
        ShapeConnection con = this.getConnection();
        Arrays.sort(keys);
        ArrayList<Feature> result = new ArrayList<Feature>(this.size());
        try {
            try {
                con.open();
                int i = 0;
                while (i < keys.length) {
                    if (!ignoredCache) {
                        if (keys[i] == null) {
                            result.add((Feature)keys[i]);
                        }
                        result.add(this.getRealFeature(((Number)keys[i]).intValue(), con, false));
                    } else {
                        result.add(con.readFeature(((Number)keys[i]).intValue(), con.getShape(((Number)keys[i]).intValue()), true, this.getSchema()));
                    }
                    ++i;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
                try {
                    con.close();
                }
                catch (Exception e2) {
                    LOGGER.error((Object)NULL_STRING, (Throwable)e2);
                }
            }
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
            }
        }
        return result;
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.getDistintsValues(field, Integer.MAX_VALUE);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        TreeSet<Object> values = new TreeSet<Object>();
        if (!this.schema.hasAttribute(field)) {
            return values;
        }
        Attribute attr = this.schema.getAttribute(field);
        if (attr instanceof AttributeCalculate) {
            AttributeCalculate attrCal = (AttributeCalculate)attr;
            return attrCal.getDistintsValues(attrCal.getRelationFieldName());
        }
        ShapeConnection con = this.getConnection();
        int index = this.schema.getAttributeIndex(field);
        try {
            try {
                con.open();
                int i = 0;
                while (i < this.size()) {
                    if (values.size() >= limit) {
                        return values;
                    }
                    Object value = this.getRealField(i, index, con);
                    if (value != null) {
                        values.add(value);
                    }
                    ++i;
                }
                return values;
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
                try {
                    con.close();
                    return values;
                }
                catch (Exception e2) {
                    LOGGER.error((Object)NULL_STRING, (Throwable)e2);
                    return values;
                }
            }
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
            }
        }
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.getDistintsValues(expr, Integer.MAX_VALUE);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        TreeSet<Object> values = new TreeSet<Object>();
        FeatureIterator itFeatures = null;
        try {
            try {
                itFeatures = this.getFeaturesIterator();
                while (itFeatures.hasNext()) {
                    if (values.size() >= limit) {
                        return values;
                    }
                    Object value = expr.getValue(itFeatures.next());
                    if (value == null) continue;
                    values.add(value);
                }
                return values;
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
                if (itFeatures == null) return values;
                itFeatures.close();
                return values;
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
    }

    @Override
    public Object getFieldValue(String field, String fieldKey, Object value) {
        Object result = null;
        if (!this.schema.hasAttribute(field) || !this.schema.hasAttribute(fieldKey)) {
            return result;
        }
        Attribute attr = this.schema.getAttribute(field);
        if (attr instanceof AttributeCalculate) {
            return ((AttributeCalculate)attr).getRelation().getFieldValue(field, value);
        }
        ShapeConnection con = this.getConnection();
        int index = this.schema.getAttributeIndex(field);
        int pkIndex = this.schema.getAttributeIndex(fieldKey);
        try {
            try {
                con.open();
                int i = 0;
                while (i < this.size()) {
                    Object pkValue = this.getRealField(i, pkIndex, con);
                    if (pkValue.equals(value)) {
                        result = this.getRealField(i, index, con);
                        break;
                    }
                    ++i;
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
                try {
                    con.close();
                }
                catch (Exception e2) {
                    LOGGER.error((Object)NULL_STRING, (Throwable)e2);
                }
            }
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
            }
        }
        return result;
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        HashMap<Object, RelationAttribute> values = new HashMap<Object, RelationAttribute>();
        int i = 0;
        while (i < fields.length) {
            String field = fields[i];
            if (!this.schema.hasAttribute(field)) {
                return values;
            }
            ++i;
        }
        if (!this.schema.hasAttribute(fieldKey)) {
            return values;
        }
        ArrayList<AttributeCalculate> attrCalculate = new ArrayList<AttributeCalculate>();
        ArrayList<Attribute> attrNoCalculate = new ArrayList<Attribute>();
        int i2 = 0;
        while (i2 < fields.length) {
            String field = fields[i2];
            Attribute attr = this.schema.getAttribute(field);
            if (attr instanceof AttributeCalculate) {
                attrCalculate.add((AttributeCalculate)attr);
            } else {
                attrNoCalculate.add(attr);
            }
            ++i2;
        }
        int[] indexes = new int[attrNoCalculate.size()];
        int pkIndex = this.schema.getAttributeIndex(fieldKey);
        int i3 = 0;
        while (i3 < attrNoCalculate.size()) {
            Attribute attr = (Attribute)attrNoCalculate.get(i3);
            indexes[i3] = this.schema.getAttributeIndex(attr.getName());
            ++i3;
        }
        ShapeConnection con = this.getConnection();
        try {
            try {
                con.open();
                if (!this.schema.getAttribute(fieldKey).isPrimaryKey()) {
                    int i4 = 0;
                    while (i4 < this.size()) {
                        Object[] values_ = con.readField(i4);
                        RelationAttribute ra = new RelationAttribute();
                        int j = 0;
                        while (j < indexes.length) {
                            Object value = values_[indexes[j]];
                            ra.setFieldValue(((Attribute)attrNoCalculate.get(j)).getName(), value);
                            ++j;
                        }
                        values.put(values_[pkIndex], ra);
                        ++i4;
                    }
                } else {
                    int i5 = 0;
                    while (i5 < this.size()) {
                        Object[] values_ = con.readField(i5);
                        RelationAttribute ra = new RelationAttribute();
                        int j = 0;
                        while (j < indexes.length) {
                            Object value = values_[indexes[j]];
                            ra.setFieldValue(((Attribute)attrNoCalculate.get(j)).getName(), value);
                            ++j;
                        }
                        values.put(i5, ra);
                        ++i5;
                    }
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
                try {
                    con.close();
                }
                catch (Exception e2) {
                    LOGGER.error((Object)NULL_STRING, (Throwable)e2);
                }
            }
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
            }
        }
        return values;
    }

    @Override
    public void rollback() {
        super.rollback();
        this.deletedKeys.clear();
        this.updatedKeys.clear();
        this.newKeys.clear();
        this.newFeats = new GrowthList(this.getShapeCount());
        this.modFeats = new Feature[this.getShapeCount()];
        this.contador = this.numReg;
    }

    public void deleteSpatialIndex() {
        if (this.spatialIndex != null && this.spatialIndex instanceof IPersistentSpatialIndex) {
            try {
                ((IPersistentSpatialIndex)this.spatialIndex).close();
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
            }
        }
        this.spatialIndex = null;
        System.gc();
    }

    @Override
    public void createSpatialIndex() {
        this.createSpatialIndex(false);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void createSpatialIndex(boolean optimizeMemoryResources) {
        if (this.spatialIndex != null) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource.the-spatial-index-already-exists"));
            return;
        }
        if (optimizeMemoryResources) {
            LOGGER.info((Object)("Loading " + this.file.getAbsolutePath() + " without spatial index"));
            return;
        }
        String fileName = FileUtil.nameWithoutExtension(this.file.getAbsolutePath());
        QuadtreeGt2 localCopy = null;
        File qixFile = new File(String.valueOf(fileName) + ".qix");
        boolean load = qixFile != null && qixFile.exists() && qixFile.length() != 0L && this.file.lastModified() <= qixFile.lastModified();
        try {
            localCopy = new QuadtreeGt2(fileName, "NM", this.getExtent(), this.numReg, !load);
            localCopy.query(this.getExtent());
        }
        catch (SpatialIndexException e1) {
            String directoryName = System.getProperty("java.io.tmpdir");
            File newFile = new File(String.valueOf(directoryName) + File.separator + this.file.getName());
            String newFileName = newFile.getName();
            try {
                load = true;
                localCopy = new QuadtreeGt2(newFileName, "NM", this.getExtent(), this.numReg, true);
                localCopy.query(this.getExtent());
            }
            catch (SpatialIndexException e) {
                localCopy = new QuadtreeJts();
                load = false;
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)NULL_STRING, (Throwable)e);
        }
        if (!load) {
            LOGGER.info((Object)("Generating qix spatial index file for the shapefile " + this.file.getAbsolutePath()));
            ShapeConnection con = this.getConnection();
            try {
                try {
                    con.open();
                    int i = 0;
                    while (i < this.numReg) {
                        Rectangle2D r = con.getShapeBounds(i);
                        if (r != null) {
                            localCopy.insert(r, i);
                        }
                        ++i;
                    }
                    if (localCopy instanceof IPersistentSpatialIndex) {
                        ((IPersistentSpatialIndex)localCopy).flush();
                    }
                    this.spatialIndex = localCopy;
                    return;
                }
                catch (Exception e) {
                    LOGGER.error((Object)NULL_STRING, (Throwable)e);
                    try {
                        if (con == null) return;
                        con.close();
                        return;
                    }
                    catch (Exception e2) {
                        LOGGER.error((Object)NULL_STRING, (Throwable)e2);
                    }
                }
                return;
            }
            finally {
                try {
                    if (con != null) {
                        con.close();
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)NULL_STRING, (Throwable)e);
                }
            }
        }
        this.spatialIndex = localCopy;
    }

    @Override
    public List<Feature> getByAttribute(String[] attributeNames, Object[] attributeValues, String fieldOrdered) {
        return this.getByAttribute(attributeNames, attributeValues, fieldOrdered, null);
    }

    @Override
    public List<Feature> getByAttribute(String[] attributeNames, Object[] attributeValues, String fieldOrdered, Filter filter) {
        return this.getByAttribute(attributeNames, attributeValues, fieldOrdered, true, filter);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, List<String> labels) {
        return this.queryGeometryIterator(rectangle, null, null, true, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> labels) {
        return this.queryGeometryIterator(rectangle, filter, null, true, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, List<String> labels) {
        return this.queryGeometryIterator(rectangle, filter, orderByFields, true, labels);
    }

    public Rectangle2D getExtent() {
        return this.extent;
    }

    public void setExtent(Rectangle2D extent) {
        this.extent = extent;
    }

    public int getNumReg() {
        return this.numReg;
    }

    public void setNumReg(int numReg) {
        this.numReg = numReg;
    }

    public ShapeConnection getConnection() {
        return new ShapeConnection(this.file, this.dbfCharset);
    }

    public static int getFieldType(int i, FeatureSchema schema) {
        Attribute attr = schema.getAttribute(i);
        AttributeType type = attr.getType();
        if (type.equals(AttributeType.GEOMETRY)) {
            return -1;
        }
        if (type.equals(AttributeType.CHAR) || type.equals(AttributeType.LONGVARCHAR) || type.equals(AttributeType.STRING) || type.equals(AttributeType.VARCHAR) || type.equals(AttributeType.TEXT) || type.equals(AttributeType.OBJECT)) {
            return 5;
        }
        if (type.equals(AttributeType.DECIMAL) || type.equals(AttributeType.BIGDECIMAL) || type.equals(AttributeType.BIGINT) || type.equals(AttributeType.LONG) || type.equals(AttributeType.NUMERIC)) {
            return 2;
        }
        if (type.equals(AttributeType.BIT) || type.equals(AttributeType.INTEGER) || type.equals(AttributeType.TINYINT) || type.equals(AttributeType.SMALLINT)) {
            return 1;
        }
        if (type.equals(AttributeType.DOUBLE) || type.equals(AttributeType.FLOAT) || type.equals(AttributeType.REAL)) {
            return 4;
        }
        if (type.equals(AttributeType.BOOLEAN)) {
            return 0;
        }
        if (type.equals(AttributeType.DATE) || type.equals(AttributeType.TIME) || type.equals(AttributeType.TIMESTAMP)) {
            return 6;
        }
        return 0;
    }

    public static Object getFieldValue(Object obj, int fieldType) {
        if (obj != null) {
            return obj;
        }
        if (fieldType == 6) {
            return NULL_DATE;
        }
        if (fieldType == 0) {
            return new NullValue();
        }
        if (fieldType == 4 || fieldType == 3 || fieldType == 1 || fieldType == 2) {
            return NULL_NUMBER;
        }
        if (fieldType == 5) {
            return NULL_STRING;
        }
        return null;
    }

    public static String[] getFieldNames(FeatureSchema schema) {
        String[] fields = new String[schema.getAttributeCount() - 1];
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Attribute attr = schema.getAttribute(i);
            AttributeType type = attr.getType();
            if (!type.equals(AttributeType.GEOMETRY)) {
                fields[i] = attr.getName();
            }
            ++i;
        }
        return fields;
    }

    public int getFieldCount() {
        FeatureSchema schema = this.getSchema();
        return schema.getAttributeCount() - 1;
    }

    public boolean isUpdatedOrDeleted(int index) {
        return this.deletedKeys.get(index) || this.updatedKeys.get(index);
    }

    public ISpatialIndex getSpatialIndex() {
        return this.spatialIndex;
    }

    @Override
    public void refreshSelection(Collection<Feature> features) {
        this.selectionKeys.clear();
        for (Feature element : features) {
            this.selectionKeys.set(element.getPrimaryKeyAsInt());
        }
    }

    @Override
    public List<int[]> getIntervalSelection() {
        ArrayList<int[]> intervalos = new ArrayList<int[]>();
        int i0 = -1;
        int last = -1;
        int i = this.selectionKeys.nextSetBit(0);
        while (i >= 0) {
            if (i0 == -1) {
                i0 = i;
                last = i;
            } else if (i == last + 1) {
                last = i;
            } else {
                intervalos.add(new int[]{i0, last});
                i0 = i;
                last = i;
            }
            i = this.selectionKeys.nextSetBit(i + 1);
        }
        if (i0 != -1) {
            intervalos.add(new int[]{i0, last});
        }
        return intervalos;
    }

    @Override
    public void invertSelection() {
        this.selectionKeys.flip(0, this.numReg);
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields) {
        return this.queryGeometryIterator(rectangle, filtro, orderByFields, null);
    }

    @Override
    public Object clone() {
        ShapeFileDataSource clone = new ShapeFileDataSource();
        try {
            clone.setFile(this.getFile());
            clone.setCharset(this.getCharset());
            if (this.layerFilter != null) {
                clone.setLayerFilter((Filter)((Cloneable)this.getLayerFilter()).clone());
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)NULL_STRING, (Throwable)e);
        }
        return clone;
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        throw new UnsupportedOperationException(I18N.getString("org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource.opperation-not-supported"));
    }

    public void setCharset(Charset selectedCharset) {
        this.dbfCharset = selectedCharset;
    }

    public Charset getCharset() {
        return this.dbfCharset;
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        return new ShapeFullIterator(this);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered, boolean ascending) {
        return this.getByAttribute(atributeNames, atributeValues, fieldOrdered, ascending, null);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        FeatureIterator it = null;
        try {
            LogicFilterImpl fieldsFilter = new LogicFilterImpl(2);
            int i = 0;
            while (i < names.length) {
                CompareFilterImpl compareFilter = new CompareFilterImpl(14);
                compareFilter.addLeftValue(new AttributeExpressionImpl2(names[i]));
                compareFilter.addRightValue(new LiteralExpressionImpl(values[i]));
                fieldsFilter.and(compareFilter);
                ++i;
            }
            if (filter == null) {
                filter = fieldsFilter;
            } else if (fieldsFilter != null) {
                filter = filter.and(fieldsFilter);
            }
            if (fieldOrdered != null) {
                it = this.getFeaturesIterator(null, filter);
                List<Feature> list = this.orderedIterator(fieldOrdered, ascending, it);
                return list;
            }
            List<Feature> list = this.query(filter);
            return list;
        }
        catch (Exception ex) {
            LOGGER.error((Object)NULL_STRING, (Throwable)ex);
            ArrayList<Feature> arrayList = new ArrayList<Feature>();
            return arrayList;
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, boolean ascending, List<String> labels) {
        if (rectangle != null && !rectangle.intersects(this.getViewBox())) {
            return new DummyFeatureIterator();
        }
        FeatureIterator iterator = null;
        try {
            iterator = this.getFeaturesIterator(rectangle, filter);
            if (CollectionUtils.isNotEmpty(orderByFields)) {
                List<Feature> result = this.orderedIterator(orderByFields.get(0), ascending, iterator);
                FeatureDataset dataSet = new FeatureDataset(result, this.getSchema());
                return new FeatureDatasetIterator(dataSet, null, null);
            }
            return iterator;
        }
        catch (Exception e) {
            LOGGER.error((Object)NULL_STRING, (Throwable)e);
            if (iterator != null) {
                iterator.close();
            }
            return new DummyFeatureIterator();
        }
    }

    private List<Feature> orderedIterator(String fieldOrdered, boolean ascending, FeatureIterator it) throws Exception {
        boolean isString = this.schema.getAttribute(fieldOrdered).getType().toJavaClass().equals(String.class);
        TreeSet<SortedAttribute> sort = new TreeSet<SortedAttribute>();
        while (it.hasNext()) {
            Feature element = it.next();
            Object value = element.getAttribute(fieldOrdered);
            Object key = element.getPrimaryKey();
            if (key == null) {
                sort.add(new SortedAttribute(value, element, ascending, isString));
                continue;
            }
            sort.add(new SortedAttribute(value, key, ascending, isString));
        }
        ArrayList<Feature> result = new ArrayList<Feature>();
        ShapeConnection con = null;
        try {
            con = this.getConnection();
            con.open();
            for (SortedAttribute element : sort) {
                int index = ((Number)element.getRecordNumber()).intValue();
                result.add(con.readFeature(index, con.getShape(index), true, this.getSchema()));
            }
        }
        finally {
            if (con != null) {
                con.close();
            }
        }
        sort = null;
        System.gc();
        return result;
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        FeatureIterator itFeats = null;
        List<Object[]> results = new ArrayList<Object[]>();
        try {
            try {
                Filter filter = this.getFilterByPrimaryKey(keys);
                itFeats = this.getFeaturesIterator(null, filter);
                results = StatsOperatorsFactory.getInstance().queryStats(operatorsByFieldMap, groupByFields, resultStatPairs, AVAILABLE_SHAPE_OPERATORS, this.size(), itFeats, this);
            }
            catch (Exception e) {
                LOGGER.error((Object)NULL_STRING, (Throwable)e);
                if (itFeats != null) {
                    itFeats.close();
                }
            }
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
        }
        return results;
    }
}

