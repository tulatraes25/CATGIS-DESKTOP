/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jump.workbench.driver.DriverManager;
import com.vividsolutions.jump.workbench.ui.AbstractDriverPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;

public abstract class AbstractDriver {
    protected DriverManager driverManager;
    protected ErrorHandler errorHandler;

    public void initialize(DriverManager driverManager, ErrorHandler errorHandler) {
        this.driverManager = driverManager;
        this.errorHandler = errorHandler;
    }

    public abstract String toString();

    public abstract AbstractDriverPanel getPanel();
}

