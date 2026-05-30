/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLReader;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPReader;

public class JMLReader
implements JUMPReader {
    @Override
    public FeatureCollection read(DriverProperties dp) throws IllegalParametersException, Exception {
        String inputFname = dp.getProperty("File");
        if (inputFname == null) {
            inputFname = dp.getProperty("DefaultValue");
        }
        if (inputFname == null) {
            throw new IllegalParametersException("call to JMLReader.read() has DataProperties w/o a InputFile specified");
        }
        GMLReader gmlReader = new GMLReader();
        return gmlReader.read(dp);
    }
}

