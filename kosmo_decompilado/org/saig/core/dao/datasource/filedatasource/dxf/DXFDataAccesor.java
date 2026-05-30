/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 *  org.opengis.util.Cloneable
 */
package org.saig.core.dao.datasource.filedatasource.dxf;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import es.kosmo.core.dao.datasource.filedatasource.dxf.DxfReaderFactoryFinder;
import es.kosmo.core.dao.datasource.filedatasource.dxf.IDxfReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.opengis.util.Cloneable;
import org.saig.core.dao.datasource.filedatasource.AbstractCadDataSource;
import org.saig.core.dao.datasource.filedatasource.dxf.DXFWriter;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public class DXFDataAccesor
extends AbstractCadDataSource {
    private static final Logger LOGGER = Logger.getLogger(DXFDataAccesor.class);
    public static final String ID_FIELD_ID = "ID";
    public static final String ID_FIELD_FSHAPE = "FShape";
    public static final String ID_FIELD_ENTITY = "Entity";
    public static final String ID_FIELD_LAYER = "Layer";
    public static final String ID_FIELD_COLOR = "Color";
    public static final String ID_FIELD_ELEVATION = "Elevation";
    public static final String ID_FIELD_THICKNESS = "Thickness";
    public static final String ID_FIELD_TEXT = "Text";
    public static final String ID_FIELD_HEIGHTTEXT = "HeightText";
    public static final String ID_FIELD_ROTATIONTEXT = "RotationText";
    public static final String ID_FIELD_BLOCK_NAME = "BlockName";
    private boolean ignoreBlockComponents = false;
    private Hashtable<String, Set<Integer>> layerToColor;
    private File m_Fich;
    private FeatureDataset dataset;

    public DXFDataAccesor() {
    }

    public DXFDataAccesor(File file) throws Exception {
        this(file, false);
    }

    public DXFDataAccesor(File file, boolean ignoreBlockComponents) throws Exception {
        this.m_Fich = file;
        this.ignoreBlockComponents = ignoreBlockComponents;
        this.initialize();
    }

    public void setIgnoreBlockComponents(boolean ignoreBlockComponents) {
        this.ignoreBlockComponents = ignoreBlockComponents;
    }

    private void initialize() throws Exception {
        if (!this.m_Fich.exists() || !this.m_Fich.canRead()) {
            throw new FileNotFoundException(I18N.getMessage(this.getClass(), "dxf-file-{0}-could-not-been-found-or-read", new Object[]{this.m_Fich.getAbsolutePath()}));
        }
        LOGGER.info((Object)(String.valueOf(I18N.getString(this.getClass(), "loading-dxf-file")) + this.m_Fich.getAbsolutePath()));
        String version = "AC1015";
        String encoding = Charset.defaultCharset().name();
        FileReader reader = new FileReader(this.m_Fich);
        IDxfReader dxfReader = DxfReaderFactoryFinder.getReader(version, reader, encoding);
        dxfReader.setOption("BANNED_LAYER_NAMES_FC_SUFFIX", new String[]{"0"});
        dxfReader.load();
        LOGGER.info((Object)("Loaded DXF file version " + dxfReader.getAcadVersion()));
        this.dataset = dxfReader.getFeatureDataset();
        this.set3d(this.dataset.is3d());
        this.layerToColor = dxfReader.getLayerToColor();
    }

    @Override
    public void add(Feature feature) throws Exception {
        this.dataset.addWithNewKey(feature);
    }

    @Override
    public void addAll(Collection<Feature> features) throws Exception {
        this.dataset.addAllWithNewKey(features);
    }

    @Override
    public void createSpatialIndex() throws Exception {
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues) {
        return this.dataset.getByAttribute(atributeNames, atributeValues);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered) {
        return this.dataset.getByAttribute(atributeNames, atributeValues, fieldOrdered);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered, Filter filter) {
        return this.dataset.getByAttribute(atributeNames, atributeValues, fieldOrdered, filter);
    }

    @Override
    public Feature getByPrimaryKey(Object key) {
        return this.dataset.getByPrimaryKey(key);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys) {
        return this.dataset.getByPrimaryKeys(keys);
    }

    @Override
    public List<Feature> getByPrimaryKey(Object[] keys, boolean ignoredUpdate) {
        return this.dataset.getByPrimaryKeys(keys);
    }

    @Override
    public Set<Object> getDistintsValues(String field) {
        return this.dataset.getDistintsValues(field);
    }

    @Override
    public Set<Object> getDistintsValues(String field, int limit) {
        return this.dataset.getDistintsValues(field, limit);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr) {
        return this.dataset.getDistintsValues(expr);
    }

    @Override
    public Set<Object> getDistintsValues(Expression expr, int limit) {
        return this.dataset.getDistintsValues(expr, limit);
    }

    @Override
    public List<Feature> getFeatures() {
        return this.dataset.getFeatures();
    }

    @Override
    public FeatureIterator getFeaturesIterator() {
        return this.dataset.iterator();
    }

    @Override
    public List<Feature> getFeaturesSamples(int n) {
        return this.dataset.getFeaturesSamples(n);
    }

    @Override
    public Object getFieldValue(String field, String fieldKey, Object value) {
        return this.dataset.getFieldsValues(field, fieldKey, value);
    }

    @Override
    public Map<Object, RelationAttribute> getMapFieldsValues(String[] fields, String fieldKey) {
        return this.dataset.getMapFieldsValues(fields, fieldKey);
    }

    @Override
    public List<Object> getOrderedPrimaryKeyList() {
        return this.dataset.getKeys();
    }

    @Override
    public List<Object> getSortKeys(String column, boolean ascending, Object[] values) {
        return this.dataset.getSortKeys(column, ascending, values);
    }

    @Override
    public Envelope getViewBox() {
        return this.dataset.getEnvelope();
    }

    @Override
    public Envelope getViewBox(Filter filter) throws Exception {
        Envelope env = new Envelope();
        FeatureIterator it = null;
        try {
            it = this.dataset.queryIterator(filter, null);
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

    @Override
    public List<Feature> query(Envelope rectangle) {
        return this.dataset.query(rectangle);
    }

    @Override
    public List<Feature> query(Filter filter) {
        return this.dataset.query(filter);
    }

    @Override
    public List<Feature> query(Envelope view, Filter filter) {
        return this.dataset.query(view, filter);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(rectangle, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filtro, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(filtro, rectangle, labels);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields, List<String> labels) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle) {
        return this.dataset.queryIterator(rectangle);
    }

    @Override
    public FeatureIterator queryIterator(Envelope view, Filter filter) {
        return this.dataset.queryIterator(filter, view);
    }

    @Override
    public void removeAll(Collection<Feature> features) throws Exception {
        this.dataset.removeAll(features);
    }

    @Override
    public void removeByPKs(List<Object> pks) throws Exception {
        this.dataset.removeByPKs(pks);
    }

    @Override
    public int size() {
        return this.dataset.size();
    }

    @Override
    public void update(Feature feature) throws Exception {
        if (feature.isUnsaved()) {
            this.add(feature);
        } else {
            this.dataset.update(feature);
        }
    }

    @Override
    public void updateAll(Collection<Feature> features) throws Exception {
        for (Feature feature : features) {
            this.update(feature);
        }
    }

    public File getFile() {
        return this.m_Fich;
    }

    @Override
    public void commit() throws Exception {
        this.inMemory = false;
        if (this.newFeatures.size() > 0) {
            LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.AbstractDataSource.saving-{0}-new-features", new Object[]{new Integer(this.newFeatures.size())}));
            this.addAll(this.newFeatures);
            this.newFeatures.clear();
        }
        if (this.deletedFeatures.size() > 0) {
            ArrayList<Feature> featuresFilter = new ArrayList<Feature>();
            for (Feature element : this.deletedFeatures) {
                if (element.isUnsaved()) continue;
                featuresFilter.add(element);
            }
            LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.AbstractDataSource.deleting-{0}-features", new Object[]{new Integer(featuresFilter.size())}));
            this.removeAll(featuresFilter);
            this.deletedFeatures.clear();
        }
        if (this.updateFeatures.size() > 0) {
            LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.AbstractDataSource.updating-{0}-features", new Object[]{new Integer(this.updateFeatures.size())}));
            this.updateAll(this.updateFeatures);
            this.updateFeatures.clear();
        }
        DXFWriter dxfWriter = new DXFWriter();
        dxfWriter.write(this.dataset, this.m_Fich);
        this.inMemory = true;
    }

    @Override
    public Hashtable<String, Set<Integer>> getLayerToColor() {
        return this.layerToColor;
    }

    @Override
    public FeatureIterator queryIterator(Envelope rectangle, Filter filtro, List<String> orderByFields) {
        return this.queryGeometryIterator(rectangle, filtro, orderByFields, null);
    }

    @Override
    public List<Feature> getHistoryOfElement(Object pkId, Filter filter) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public ILayerIterator getFullIterator(Envelope envelope, Filter filter, String[] fieldsToOrdered, boolean ascending) throws Exception {
        return this.dataset.getFullIterator(envelope, filter, fieldsToOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] atributeNames, Object[] atributeValues, String fieldOrdered, boolean ascending) {
        return this.dataset.getByAttribute(atributeNames, atributeValues, fieldOrdered, ascending);
    }

    @Override
    public List<Feature> getByAttribute(String[] names, Object[] values, String fieldOrdered, boolean ascending, Filter filter) {
        return this.dataset.getByAttribute(names, values, fieldOrdered, ascending, filter);
    }

    @Override
    public FeatureIterator queryGeometryIterator(Envelope rectangle, Filter filter, List<String> orderByFields, boolean ascending, List<String> labels) {
        return this.dataset.queryOnlyGeometryIterator(rectangle, labels);
    }

    @Override
    public List<Object[]> queryStats(Map<String, Set<String>> operatorsByFieldMap, List<String> groupByFields, Object[] keys, List<CalculateStatsDialog.StatPair> resultStatPairs) {
        return this.dataset.queryStats(operatorsByFieldMap, groupByFields, keys, resultStatPairs);
    }

    @Override
    public Object clone() {
        DXFDataAccesor newDXFDataAccesor = new DXFDataAccesor();
        newDXFDataAccesor.ignoreBlockComponents = this.ignoreBlockComponents;
        newDXFDataAccesor.dataset = (FeatureDataset)this.dataset.clone();
        if (this.layerFilter != null) {
            newDXFDataAccesor.setLayerFilter((Filter)((Cloneable)this.getLayerFilter()).clone());
        }
        newDXFDataAccesor.m_Fich = this.m_Fich;
        newDXFDataAccesor.layerToColor = this.layerToColor;
        return newDXFDataAccesor;
    }

    @Override
    public FeatureSchema getSchema() {
        return this.dataset.getFeatureSchema();
    }

    @Override
    public void dispose() {
        if (this.dataset != null) {
            this.dataset.dispose();
            this.dataset = null;
        }
        this.m_Fich = null;
        if (this.layerToColor != null) {
            this.layerToColor.clear();
            this.layerToColor = null;
        }
    }
}

