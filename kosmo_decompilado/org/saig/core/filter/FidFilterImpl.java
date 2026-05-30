/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.opengis.util.Cloneable
 */
package org.saig.core.filter;

import com.vividsolutions.jump.feature.Feature;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.opengis.util.Cloneable;
import org.saig.core.filter.AbstractFilterImpl;
import org.saig.core.filter.FidFilter;
import org.saig.core.filter.FilterVisitor;

public class FidFilterImpl
extends AbstractFilterImpl
implements FidFilter,
Cloneable {
    private Set<String> fids = new HashSet<String>();

    public FidFilterImpl() {
        this.filterType = (short)22;
    }

    public FidFilterImpl(String initialFid) {
        this.filterType = (short)22;
        this.addFid(initialFid);
    }

    @Override
    public final void addFid(String fid) {
        LOGGER.debug((Object)("got fid: " + fid));
        this.fids.add(fid);
    }

    @Override
    public boolean contains(Feature feature) {
        if (feature == null) {
            return false;
        }
        return this.fids.contains(String.valueOf(feature.getID()));
    }

    public String toString() {
        StringBuffer fidFilter = new StringBuffer();
        Iterator<String> fidIterator = this.fids.iterator();
        while (fidIterator.hasNext()) {
            fidFilter.append(fidIterator.next().toString());
            if (!fidIterator.hasNext()) continue;
            fidFilter.append(", ");
        }
        return "[ " + fidFilter.toString() + " ]";
    }

    public boolean equals(Object filter) {
        LOGGER.debug((Object)("condition: " + filter));
        if (filter != null && filter.getClass() == this.getClass()) {
            LOGGER.debug((Object)("condition: " + ((FidFilterImpl)filter).filterType));
            if (((FidFilterImpl)filter).filterType == 22) {
                return this.fids.equals(((FidFilterImpl)filter).getFidsSet());
            }
            return false;
        }
        return false;
    }

    public int hashCode() {
        return this.fids.hashCode();
    }

    @Override
    public String[] getFids() {
        return this.fids.toArray(new String[0]);
    }

    public Set<String> getFidsSet() {
        return this.fids;
    }

    @Override
    public void removeAllFids(Collection<String> fidsToRemove) {
        this.fids.removeAll(fidsToRemove);
    }

    @Override
    public void addAllFids(Collection<String> fidsToAdd) {
        this.fids.addAll(fidsToAdd);
    }

    @Override
    public final void removeFid(String fid) {
        this.fids.remove(fid);
    }

    @Override
    public void accept(FilterVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() {
        FidFilterImpl clone = new FidFilterImpl();
        if (this.fids != null) {
            clone.addAllFids(this.getFidsSet());
        }
        return clone;
    }
}

