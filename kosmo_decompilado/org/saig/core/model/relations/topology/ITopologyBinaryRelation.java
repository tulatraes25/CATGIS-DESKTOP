/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.relations.topology;

import org.saig.core.filter.Filter;
import org.saig.core.model.relations.topology.ITopologyRelation;

public interface ITopologyBinaryRelation
extends ITopologyRelation {
    public Filter getEntryTargetFilter();

    public void setEntryTargetFilter(Filter var1);

    public String getTargetLayerName();

    public void setTargetLayerName(String var1);
}

