/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import java.util.Collection;
import org.saig.core.filter.Filter;

public interface FidFilter
extends Filter {
    @Override
    public boolean contains(Feature var1);

    public void addFid(String var1);

    public String[] getFids();

    public void addAllFids(Collection<String> var1);

    public void removeAllFids(Collection<String> var1);

    public void removeFid(String var1);
}

