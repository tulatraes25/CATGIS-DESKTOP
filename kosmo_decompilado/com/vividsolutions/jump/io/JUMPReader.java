/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;

public interface JUMPReader {
    public FeatureCollection read(DriverProperties var1) throws Exception;
}

