/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.ILayerIterator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.saig.core.filter.Expression;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.relations.RelationAttribute;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.jump.widgets.stats.CalculateStatsDialog;

public interface FeatureCollection {
    public FeatureSchema getFeatureSchema();

    public void setFeatureSchema(FeatureSchema var1);

    public Envelope getEnvelope() throws Exception;

    public Envelope getEnvelope(Filter var1) throws Exception;

    public void setEnvelope(Envelope var1);

    public int size() throws Exception;

    public boolean isEmpty() throws Exception;

    public List<Feature> getFeatures();

    public FeatureIterator iterator();

    public ILayerIterator getFullIterator(Envelope var1, Filter var2, String[] var3, boolean var4) throws Exception;

    public List<Feature> query(Envelope var1) throws Exception;

    public List<Feature> query(Envelope var1, Filter var2) throws Exception;

    public List<Feature> query(Filter var1) throws Exception;

    public FeatureIterator queryIterator(Envelope var1);

    public FeatureIterator queryIterator(Filter var1, Envelope var2);

    public FeatureIterator queryOnlyGeometryIterator(Envelope var1, List<String> var2);

    public FeatureIterator queryOnlyGeometryIterator(Filter var1, Envelope var2, List<String> var3);

    public List<Object> getKeys();

    public Feature getByPrimaryKey(Object var1);

    public List<Feature> getByPrimaryKeys(Object[] var1);

    public void add(Feature var1) throws Exception;

    public void addAll(Collection<Feature> var1) throws Exception;

    public void update(Feature var1) throws Exception;

    public void updateAll(Collection<Feature> var1) throws Exception;

    public void remove(Feature var1) throws Exception;

    public void removeAll(Collection<Feature> var1) throws Exception;

    public void removeByPKs(List<Object> var1) throws Exception;

    public void clear() throws Exception;

    public void commit() throws Exception;

    public void rollBack();

    public Collection<Feature> remove(Envelope var1) throws Exception;

    public List<Feature> getByAttribute(String[] var1, Object[] var2);

    public List<Feature> getByAttribute(String[] var1, Object[] var2, String var3);

    public List<Feature> getByAttribute(String[] var1, Object[] var2, String var3, boolean var4);

    public List<Feature> getByAttribute(String[] var1, Object[] var2, String var3, Filter var4);

    public List<Feature> getByAttribute(String[] var1, Object[] var2, String var3, boolean var4, Filter var5);

    public String getName();

    public void setName(String var1);

    public List<Feature> getFeaturesSamples(int var1);

    public List<Object> getSortKeys(String var1, boolean var2, Object[] var3);

    public Set<Object> getDistintsValues(String var1);

    public Set<Object> getDistintsValues(String var1, int var2);

    public Set<Object> getDistintsValues(Expression var1);

    public Set<Object> getDistintsValues(Expression var1, int var2);

    public Object getFieldsValues(String var1, String var2, Object var3);

    public Map<Object, RelationAttribute> getMapFieldsValues(String[] var1, String var2);

    public boolean isCad();

    public void createSpatialIndex();

    public boolean isSpatialIndex();

    public void setTopologyRelations(List<ITopologyRelation> var1);

    public void setEditable(boolean var1);

    public boolean isEditable();

    public void refreshSelection(Collection<Feature> var1);

    public void invertSelection();

    public List<int[]> getIntervalSelection();

    public Filter getLayerFilter();

    public void setLayerFilter(Filter var1);

    public List<Feature> getHistoryOfElement(Object var1, Filter var2) throws Exception;

    public void setTriggers(Set<ITrigger> var1);

    public boolean is3d();

    public void set3d(boolean var1);

    public List<Object[]> queryStats(Map<String, Set<String>> var1, List<String> var2, Object[] var3, List<CalculateStatsDialog.StatPair> var4);

    public Object clone();

    public void dispose();
}

