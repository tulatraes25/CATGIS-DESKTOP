/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.io.ParseException
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.GMLWriter;
import com.vividsolutions.jump.workbench.driver.AbstractOutputDriver;
import com.vividsolutions.jump.workbench.model.Layer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class JMLFileOutputDriver
extends AbstractOutputDriver {
    private GMLWriter gmlWriter = new GMLWriter();

    @Override
    public void output(Layer layer) throws FileNotFoundException, IOException, com.vividsolutions.jump.io.ParseException, ParseException, Exception {
        File selectedFile = this.driverManager.getSharedSaveBasicFileDriverPanel().getSelectedFile();
        String fname = selectedFile.getAbsolutePath();
        DriverProperties dp = new DriverProperties();
        dp.set("File", fname);
        this.gmlWriter.write((FeatureCollection)layer.getFeatureCollectionWrapper(), dp);
    }

    @Override
    public String toString() {
        return "JCS GML";
    }
}

