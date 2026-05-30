/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jump.workbench.driver.AbstractDriver;
import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.BasicFileDriverPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;

public abstract class AbstractInputDriver
extends AbstractDriver {
    private BasicFileDriverPanel panel;

    public abstract void input(LayerManager var1, String var2) throws Exception;

    @Override
    public AbstractDriverPanel getPanel() {
        return this.panel;
    }

    @Override
    public void initialize(DriverManager driverManager, ErrorHandler errorHandler) {
        super.initialize(driverManager, errorHandler);
        this.panel = driverManager.getSharedOpenBasicFileDriverPanel();
    }
}

