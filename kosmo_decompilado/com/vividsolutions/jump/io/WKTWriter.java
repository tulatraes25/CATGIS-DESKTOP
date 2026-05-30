/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.io.WKTWriter
 */
package com.vividsolutions.jump.io;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.JUMPWriter;
import java.io.FileWriter;
import java.io.Writer;
import org.saig.core.model.feature.FeatureIterator;

public class WKTWriter
implements JUMPWriter {
    private com.vividsolutions.jts.io.WKTWriter wktWriter = new com.vividsolutions.jts.io.WKTWriter();

    @Override
    public void write(FeatureCollection featureCollection, DriverProperties dp) throws IllegalParametersException, Exception {
        String outputFname = dp.getProperty("File");
        if (outputFname == null) {
            outputFname = dp.getProperty("DefaultValue");
        }
        if (outputFname == null) {
            throw new IllegalParametersException("call to WKTWrite.write() has DataProperties w/o a OutputFile specified");
        }
        FileWriter w = new FileWriter(outputFname);
        this.write(featureCollection, w);
        ((Writer)w).close();
    }

    public void write(FeatureCollection featureCollection, Writer writer) throws Exception {
        FeatureIterator it = null;
        try {
            it = featureCollection.iterator();
            while (it.hasNext()) {
                Feature feature = it.next();
                this.wktWriter.writeFormatted(feature.getGeometry(), writer);
                writer.write("\n\n");
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }
}

