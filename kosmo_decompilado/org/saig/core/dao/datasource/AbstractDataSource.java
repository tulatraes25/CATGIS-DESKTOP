/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.PrecisionModel
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.LogicFilter;
import org.saig.core.filter.LogicFilterImpl;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public abstract class AbstractDataSource {
    private static final Logger LOGGER = Logger.getLogger(AbstractDataSource.class);
    public static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 0);
    protected String geomColName;
    protected int size = -1;
    protected boolean editable = false;
    protected Envelope envelope;
    protected FeatureSchema schema;
    protected Filter layerFilter;
    protected boolean is3d = false;
    protected Set<Feature> newFeatures;
    protected Set<Feature> updateFeatures;
    protected Set<Feature> deletedFeatures;
    protected BitSet lockedFeatures;
    protected boolean inMemory;

    public abstract void add(Feature var1) throws Exception;

    public abstract void addAll(Collection<Feature> var1) throws Exception;

    public abstract List<Feature> getByAttribute(String[] var1, Object[] var2);

    public abstract List<Feature> getByAttribute(String[] var1, Object[] var2, String var3);

    public abstract List<Feature> getByAttribute(String[] var1, Object[] var2, String var3, boolean var4);

    public abstract List<Feature> getByAttribute(String[] var1, Object[] var2, String var3, Filter var4);

    public abstract List<Feature> getByAttribute(String[] var1, Object[] var2, String var3, boolean var4, Filter var5);

    public abstract List<Feature> getFeatures();

    public abstract FeatureIterator getFeaturesIterator();

    public abstract Envelope getViewBox() throws Exception;

    public abstract Envelope getViewBox(Filter var1) throws Exception;

    public abstract List<Feature> query(Envelope var1) throws Exception;

    public abstract List<Feature> query(Filter var1) throws Exception;

    public abstract List<Feature> query(Envelope var1, Filter var2) throws Exception;

    public abstract FeatureIterator queryIterator(Envelope var1);

    public abstract FeatureIterator queryIterator(Envelope var1, Filter var2);

    public abstract FeatureIterator queryIterator(Envelope var1, Filter var2, List<String> var3);

    public abstract FeatureIterator queryGeometryIterator(Envelope var1, List<String> var2);

    public abstract FeatureIterator queryGeometryIterator(Envelope var1, Filter var2, List<String> var3);

    public abstract FeatureIterator queryGeometryIterator(Envelope var1, Filter var2, List<String> var3, List<String> var4);

    public abstract FeatureIterator queryGeometryIterator(Envelope var1, Filter var2, List<String> var3, boolean var4, List<String> var5);

    public abstract int size() throws Exception;

    public abstract void removeAll(Collection<Feature> var1) throws Exception;

    public abstract void removeByPKs(List<Object> var1) throws Exception;

    public abstract void update(Feature var1) throws Exception;

    public abstract void updateAll(Collection<Feature> var1) throws Exception;

    public void setSchema(FeatureSchema schema) {
        this.schema = schema;
        this.updateFeatures = new HashSet<Feature>();
        this.newFeatures = new HashSet<Feature>();
        this.deletedFeatures = new HashSet<Feature>();
        this.lockedFeatures = new BitSet();
        this.inMemory = true;
    }

    public FeatureSchema getSchema() {
        return this.schema;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setViewBox(Envelope envelope) {
        this.envelope = envelope;
        if (envelope == null) {
            this.size = -1;
            try {
                this.size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
    }

    public String getGeomColName() {
        return this.geomColName;
    }

    public void setGeomColName(String geometryColumnName) {
        this.geomColName = geometryColumnName;
    }

    public void commit() throws Exception {
        this.inMemory = false;
        try {
            if (CollectionUtils.isNotEmpty(this.newFeatures)) {
                LOGGER.info((Object)I18N.getMessage("org.saig.core.dao.datasource.AbstractDataSource.saving-{0}-new-features", new Object[]{new Integer(this.newFeatures.size())}));
                this.addAll(this.newFeatures);
                this.newFeatures.clear();
            }
            if (CollectionUtils.isNotEmpty(this.deletedFeatures)) {
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
        }
        finally {
            this.inMemory = true;
        }
    }

    public void rollback() {
        this.rollback(true);
    }

    public void rollback(boolean deleteAllChanges) {
        if (deleteAllChanges) {
            this.updateFeatures.clear();
            this.deletedFeatures.clear();
            this.newFeatures.clear();
        }
    }

    public Set<Feature> getNewFeatures() {
        return this.newFeatures;
    }

    public Set<Feature> getUpdatedFeatures() {
        return this.updateFeatures;
    }

    public Set<Feature> getDeletedFeatures() {
        return this.deletedFeatures;
    }

    public abstract List<Feature> getFeaturesSamples(int var1);

    public void setInMemory(boolean inMemory) {
        this.inMemory = inMemory;
    }

    public abstract List<Object> getOrderedPrimaryKeyList();

    public abstract Feature getByPrimaryKey(Object var1);

    public abstract List<Feature> getByPrimaryKey(Object[] var1);

    public abstract List<Feature> getByPrimaryKey(Object[] var1, boolean var2);

    public abstract List<Object> getSortKeys(String var1, boolean var2, Object[] var3);

    public List<Object> getKeys(Set<Feature> features) {
        ArrayList<Object> pkList = new ArrayList<Object>(features.size());
        for (Feature element : features) {
            pkList.add(element.getPrimaryKey());
        }
        return pkList;
    }

    public abstract Object getFieldValue(String var1, String var2, Object var3);

    public abstract Set<Object> getDistintsValues(String var1);

    public abstract Set<Object> getDistintsValues(String var1, int var2);

    public abstract Set<Object> getDistintsValues(Expression var1);

    public abstract Set<Object> getDistintsValues(Expression var1, int var2);

    public abstract Map<Object, RelationAttribute> getMapFieldsValues(String[] var1, String var2);

    public abstract void createSpatialIndex() throws Exception;

    public Feature getUpdatedFeature(Feature feat) {
        Feature featResult = null;
        if (this.updateFeatures.contains(feat)) {
            Iterator<Feature> iter = this.updateFeatures.iterator();
            while (iter.hasNext() && featResult == null) {
                Feature element = iter.next();
                if (!element.equals(feat)) continue;
                featResult = element;
            }
        } else {
            featResult = feat;
        }
        return featResult;
    }

    public Feature getRealFeature(Feature feat) {
        Feature featResult = null;
        if (!this.deletedFeatures.contains(feat)) {
            featResult = this.getUpdatedFeature(feat);
        }
        return featResult;
    }

    public Filter getLayerFilter() {
        return this.layerFilter;
    }

    public void setLayerFilter(Filter layerFilter) {
        this.layerFilter = layerFilter;
        this.size = -1;
        this.envelope = null;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void refreshSelection(Collection<Feature> features) {
        throw new I18NUnsupportedOperationException();
    }

    public List<int[]> getIntervalSelection() {
        throw new I18NUnsupportedOperationException();
    }

    public void invertSelection() {
        throw new I18NUnsupportedOperationException();
    }

    public abstract List<Feature> getHistoryOfElement(Object var1, Filter var2);

    public boolean is3d() {
        return this.is3d;
    }

    public void set3d(boolean is3d) {
        this.is3d = is3d;
    }

    public abstract ILayerIterator getFullIterator(Envelope var1, Filter var2, String[] var3, boolean var4) throws Exception;

    public abstract List<Object[]> queryStats(Map<String, Set<String>> var1, List<String> var2, Object[] var3, List<CalculateStatsDialog.StatPair> var4);

    /*
     * Enabled aggressive exception aggregation
     */
    public Filter getFilterByPrimaryKey(Object[] values) {
        if (ArrayUtils.isEmpty((Object[])values)) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource.the-key-set-is-null"));
            return null;
        }
        LogicFilter filter = null;
        try {
            if (AttributeType.isNumeric(this.schema.getPrimaryKey().getType())) {
                List<long[]> ranges = this.getRanges(values);
                if (ranges.isEmpty()) {
                    return null;
                }
                for (long[] range : ranges) {
                    LogicFilterImpl currentFilter = new LogicFilterImpl(2);
                    AttributeExpressionImpl2 pkExpression1 = new AttributeExpressionImpl2(this.schema.getPrimaryKeyName());
                    AttributeExpressionImpl2 pkExpression2 = new AttributeExpressionImpl2(this.schema.getPrimaryKeyName());
                    CompareFilterImpl leftFilter = new CompareFilterImpl(18);
                    leftFilter.addLeftValue(pkExpression1);
                    leftFilter.addRightValue(new LiteralExpressionImpl(range[0]));
                    CompareFilterImpl rightFilter = new CompareFilterImpl(17);
                    rightFilter.addLeftValue(pkExpression2);
                    rightFilter.addRightValue(new LiteralExpressionImpl(range[1]));
                    currentFilter.addFilter(leftFilter);
                    currentFilter.addFilter(rightFilter);
                    if (filter == null) {
                        filter = new LogicFilterImpl(1);
                    }
                    filter.addFilter(currentFilter);
                }
            } else {
                int i = 0;
                while (i < values.length) {
                    AttributeExpressionImpl2 pkExpression1 = new AttributeExpressionImpl2(this.schema.getPrimaryKeyName());
                    CompareFilterImpl currentFilter = new CompareFilterImpl(14);
                    currentFilter.addLeftValue(pkExpression1);
                    currentFilter.addRightValue(new LiteralExpressionImpl("'" + values[i] + "'"));
                    if (filter == null) {
                        filter = new LogicFilterImpl(1);
                    }
                    filter.addFilter(currentFilter);
                    ++i;
                }
            }
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return filter;
    }

    protected List<long[]> getRanges(Object[] values) {
        long value1;
        ArrayList<long[]> ranges = new ArrayList<long[]>();
        if (values.length == 0) {
            return ranges;
        }
        ArrayList<Object> valuesCopyList = new ArrayList<Object>();
        int i = 0;
        while (i < values.length) {
            if (!(values[i] instanceof Feature)) {
                valuesCopyList.add(values[i]);
            }
            ++i;
        }
        Object[] valuesCopy = new Object[valuesCopyList.size()];
        valuesCopyList.toArray(valuesCopy);
        Arrays.sort(valuesCopy);
        long value2 = value1 = ((Number)valuesCopy[0]).longValue();
        int i2 = 1;
        while (i2 < valuesCopy.length) {
            long value = ((Number)valuesCopy[i2]).longValue();
            if (value == value2 + 1L) {
                value2 = value;
            } else {
                ranges.add(new long[]{value1, value2});
                value1 = value;
                value2 = value;
            }
            ++i2;
        }
        ranges.add(new long[]{value1, value2});
        return ranges;
    }

    public abstract Object clone();

    public void setSize(int size) {
        this.size = size;
    }

    public void dispose() {
        if (this.envelope != null) {
            this.envelope = null;
        }
        if (this.layerFilter != null) {
            this.layerFilter = null;
        }
        if (this.schema != null) {
            this.schema = null;
        }
        if (this.newFeatures != null) {
            this.newFeatures.clear();
            this.newFeatures = null;
        }
        if (this.updateFeatures != null) {
            this.updateFeatures.clear();
            this.updateFeatures = null;
        }
        if (this.deletedFeatures != null) {
            this.deletedFeatures.clear();
            this.deletedFeatures = null;
        }
        if (this.lockedFeatures != null) {
            this.lockedFeatures.clear();
            this.lockedFeatures = null;
        }
    }
}

