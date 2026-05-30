/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.IllegalParametersException;

public interface JUMPWriter {
    public void write(FeatureCollection var1, DriverProperties var2) throws IllegalParametersException, Exception;
}

