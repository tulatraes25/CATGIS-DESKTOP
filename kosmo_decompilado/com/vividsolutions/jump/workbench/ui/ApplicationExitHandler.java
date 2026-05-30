/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import javax.swing.JFrame;
import org.saig.jump.util.ApplicationExitListener;

public interface ApplicationExitHandler {
    public void exitApplication(JFrame var1);

    public void addExitListener(ApplicationExitListener var1);

    public void removeExitListener(ApplicationExitListener var1);

    public boolean fireExitingApplication();
}

