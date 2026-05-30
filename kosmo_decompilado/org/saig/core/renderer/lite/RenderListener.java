/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.feature.Feature
 */
package org.saig.core.renderer.lite;

import org.geotools.feature.Feature;

public interface RenderListener {
    public void featureRenderer(Feature var1);

    public void errorOccurred(Exception var1);
}

