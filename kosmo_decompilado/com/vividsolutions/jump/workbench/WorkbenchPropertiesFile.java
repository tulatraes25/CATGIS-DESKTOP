/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdom.Document
 *  org.jdom.Element
 *  org.jdom.input.SAXBuilder
 */
package com.vividsolutions.jump.workbench;

import com.vividsolutions.jump.workbench.WorkbenchProperties;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class WorkbenchPropertiesFile
implements WorkbenchProperties {
    private ErrorHandler errorHandler;
    private Element root;

    public WorkbenchPropertiesFile(File file, ErrorHandler errorHandler) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(file);
        this.root = document.getRootElement();
        this.errorHandler = errorHandler;
    }

    @Override
    public List<Class<?>> getPlugInClasses() {
        ArrayList plugInClasses = new ArrayList();
        for (Element plugInElement : this.root.getChildren("plug-in")) {
            try {
                plugInClasses.add(Class.forName(plugInElement.getTextTrim()));
            }
            catch (ClassNotFoundException e) {
                this.errorHandler.handleThrowable(e);
            }
        }
        return plugInClasses;
    }

    @Override
    public List<Class<?>> getInputDriverClasses() throws ClassNotFoundException {
        ArrayList inputDriverClasses = new ArrayList();
        for (Element inputDriverElement : this.root.getChildren("input-driver")) {
            inputDriverClasses.add(Class.forName(inputDriverElement.getTextTrim()));
        }
        return inputDriverClasses;
    }

    @Override
    public List<Class<?>> getOutputDriverClasses() throws ClassNotFoundException {
        ArrayList outputDriverClasses = new ArrayList();
        for (Element outputDriverElement : this.root.getChildren("output-driver")) {
            outputDriverClasses.add(Class.forName(outputDriverElement.getTextTrim()));
        }
        return outputDriverClasses;
    }

    @Override
    public List<Class<?>> getConfigurationClasses() throws ClassNotFoundException {
        ArrayList getConfigurationClasses = new ArrayList();
        for (Element configurationElement : this.root.getChildren("extension")) {
            getConfigurationClasses.add(Class.forName(configurationElement.getTextTrim()));
        }
        return getConfigurationClasses;
    }
}

