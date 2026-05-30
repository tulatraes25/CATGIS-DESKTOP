/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.WKTWriter;
import com.vividsolutions.jump.workbench.driver.AbstractOutputDriver;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.io.File;

public class WKTFileOutputDriver
extends AbstractOutputDriver {
    private WKTWriter writer = new WKTWriter();

    @Override
    public String toString() {
        return GUIUtil.wktDesc;
    }

    @Override
    public void output(Layer layer) throws Exception {
        File selectedFile = this.driverManager.getSharedSaveBasicFileDriverPanel().getSelectedFile();
        String fname = selectedFile.getAbsolutePath();
        DriverProperties dp = new DriverProperties();
        dp.set("File", fname);
        this.writer.write((FeatureCollection)layer.getFeatureCollectionWrapper(), dp);
    }
}

