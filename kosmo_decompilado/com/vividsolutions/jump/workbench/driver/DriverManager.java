/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.driver;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.WorkbenchProperties;
import com.vividsolutions.jump.workbench.driver.AbstractDriver;
import com.vividsolutions.jump.workbench.driver.AbstractInputDriver;
import com.vividsolutions.jump.workbench.driver.AbstractOutputDriver;
import com.vividsolutions.jump.workbench.driver.FMEFileInputDriver;
import com.vividsolutions.jump.workbench.driver.FMEFileOutputDriver;
import com.vividsolutions.jump.workbench.driver.GMLFileInputDriver;
import com.vividsolutions.jump.workbench.driver.GMLFileOutputDriver;
import com.vividsolutions.jump.workbench.driver.JMLFileInputDriver;
import com.vividsolutions.jump.workbench.driver.JMLFileOutputDriver;
import com.vividsolutions.jump.workbench.driver.WKTFileInputDriver;
import com.vividsolutions.jump.workbench.driver.WKTFileOutputDriver;
import com.vividsolutions.jump.workbench.ui.BasicFileDriverPanel;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.saig.jump.lang.I18N;

public class DriverManager {
    private ArrayList inputDrivers = new ArrayList();
    private ArrayList outputDrivers = new ArrayList();
    private List builtInInputDriverClasses = Arrays.asList(FMEFileInputDriver.class, GMLFileInputDriver.class, JMLFileInputDriver.class, WKTFileInputDriver.class);
    private List builtInOutputDriverClasses = Arrays.asList(FMEFileOutputDriver.class, GMLFileOutputDriver.class, JMLFileOutputDriver.class, WKTFileOutputDriver.class);
    private BasicFileDriverPanel sharedOpenBasicFileDriverPanel;
    private BasicFileDriverPanel sharedSaveBasicFileDriverPanel;
    private ErrorHandler errorHandler;

    public DriverManager(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        this.sharedOpenBasicFileDriverPanel = new SharedFileDriverPanel(I18N.getString("workbench.driver.DriverManager.file-to-open"), errorHandler);
        this.sharedSaveBasicFileDriverPanel = new SharedFileDriverPanel(I18N.getString("workbench.driver.DriverManager.file-to-save"), errorHandler);
        this.sharedOpenBasicFileDriverPanel.setFileMustExist(true);
        this.sharedSaveBasicFileDriverPanel.setFileMustExist(false);
    }

    public List getInputDrivers() {
        return this.inputDrivers;
    }

    public List getOutputDrivers() {
        return this.outputDrivers;
    }

    public BasicFileDriverPanel getSharedOpenBasicFileDriverPanel() {
        return this.sharedOpenBasicFileDriverPanel;
    }

    public BasicFileDriverPanel getSharedSaveBasicFileDriverPanel() {
        return this.sharedSaveBasicFileDriverPanel;
    }

    public void loadDrivers(WorkbenchProperties properties) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ArrayList newInputDriverClasses = new ArrayList();
        newInputDriverClasses.addAll(this.builtInInputDriverClasses);
        newInputDriverClasses.addAll(properties.getInputDriverClasses());
        this.loadDrivers(newInputDriverClasses, this.inputDrivers);
        ArrayList newOutputDriverClasses = new ArrayList();
        newOutputDriverClasses.addAll(this.builtInOutputDriverClasses);
        newOutputDriverClasses.addAll(properties.getOutputDriverClasses());
        this.loadDrivers(newOutputDriverClasses, this.outputDrivers);
    }

    public void loadInputDriver(AbstractInputDriver driver) {
        driver.initialize(this, this.errorHandler);
        this.inputDrivers.add(driver);
    }

    public void loadOutputDriver(AbstractOutputDriver driver) {
        driver.initialize(this, this.errorHandler);
        this.outputDrivers.add(driver);
    }

    private void loadDrivers(List driverClasses, List drivers) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for (Class driverClass : driverClasses) {
            AbstractDriver driver = (AbstractDriver)driverClass.newInstance();
            driver.initialize(this, this.errorHandler);
            drivers.add(driver);
        }
    }

    private static class SharedFileDriverPanel
    extends BasicFileDriverPanel {
        public SharedFileDriverPanel(String description, ErrorHandler errorHandler) {
            super(errorHandler);
            this.fileNamePanel.setUpperDescription(description);
        }

        @Override
        public void setFileDescription(String description) {
            Assert.shouldNeverReachHere((String)I18N.getString("workbench.driver.DriverManager.panel-is-shared-thus-description-cannot-be-changed"));
        }
    }
}

