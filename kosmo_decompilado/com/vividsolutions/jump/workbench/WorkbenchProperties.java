/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench;

import java.util.List;

public interface WorkbenchProperties {
    public List<Class<?>> getPlugInClasses() throws ClassNotFoundException;

    public List<Class<?>> getInputDriverClasses() throws ClassNotFoundException;

    public List<Class<?>> getOutputDriverClasses() throws ClassNotFoundException;

    public List<Class<?>> getConfigurationClasses() throws ClassNotFoundException;
}

