/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jump.workbench.driver.AbstractDriver;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.BasicFileDriverPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;

public abstract class AbstractOutputDriver
extends AbstractDriver {
    private BasicFileDriverPanel panel;

    public abstract void output(Layer var1) throws Exception;

    @Override
    public AbstractDriverPanel getPanel() {
        return this.panel;
    }

    @Override
    public void initialize(DriverManager driverManager, ErrorHandler errorHandler) {
        super.initialize(driverManager, errorHandler);
        this.panel = driverManager.getSharedSaveBasicFileDriverPanel();
    }
}

