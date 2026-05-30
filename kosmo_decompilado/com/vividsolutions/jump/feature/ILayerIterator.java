/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.feature;

import com.vividsolutions.jump.feature.Feature;
import java.util.List;

public interface ILayerIterator {
    public Feature next() throws Exception;

    public Feature prior() throws Exception;

    public Feature first() throws Exception;

    public Feature last() throws Exception;

    public Feature absolute(int var1) throws Exception;

    public Feature relative(int var1) throws Exception;

    public Feature forward() throws Exception;

    public List<Feature> forward(int var1) throws Exception;

    public List<Feature> forward_all() throws Exception;

    public Feature backward() throws Exception;

    public List<Feature> backward(int var1) throws Exception;

    public List<Feature> backward_all() throws Exception;

    public void close() throws Exception;

    public void open() throws Exception;

    public long size() throws Exception;
}

