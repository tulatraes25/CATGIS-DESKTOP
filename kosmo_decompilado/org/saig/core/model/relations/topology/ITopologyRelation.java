/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations.topology;

import com.vividsolutions.jump.feature.Feature;
import es.kosmo.core.model.relations.topology.OperationType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.trigger.ITrigger;

public interface ITopologyRelation {
    public String getId();

    public String getName();

    public String getDescription();

    public boolean checkAll();

    public boolean check(Feature var1, Collection<Feature> var2) throws Exception;

    public List<Feature> obtainErrors();

    public String getSourceLayerName();

    public void setSourceLayerName(String var1);

    public Filter getEntrySourceFilter();

    public void setEntrySourceFilter(Filter var1);

    public Filter getAlphanumericFilter();

    public void setAlphanumericFilter(Filter var1);

    public boolean isEnabled();

    public void setEnabled(boolean var1);

    public void success(Collection<Feature> var1) throws Exception;

    public void failure(Feature var1, Collection<Feature> var2) throws Exception;

    public void setSuccessTriggers(Set<ITrigger> var1);

    public Set<ITrigger> getSuccessTriggers();

    public void setFailureTriggers(Set<ITrigger> var1);

    public Set<ITrigger> getFailureTriggers();

    public int getCheckStrategy();

    public void setCheckStrategy(int var1);

    public boolean checkValidGeometryType(int var1);

    public OperationType getOperationType();

    public void setOperationType(OperationType var1);
}

