/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLWriter;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPWriter;

public class JMLWriter
implements JUMPWriter {
    @Override
    public void write(FeatureCollection featureCollection, DriverProperties dp) throws IllegalParametersException, Exception {
        String outputFname = dp.getProperty("File");
        if (outputFname == null) {
            outputFname = dp.getProperty("DefaultValue");
        }
        if (outputFname == null) {
            throw new IllegalParametersException("call to JMLWriter.write() has DataProperties w/o a OutputFile specified");
        }
        GMLWriter gmlWriter = new GMLWriter();
        gmlWriter.write(featureCollection, dp);
    }
}

