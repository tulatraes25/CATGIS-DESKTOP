/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.wizard;

import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import java.util.Map;

public interface WizardPanel {
    public void enteredFromLeft(Map<String, Object> var1);

    public void exitingToRight() throws Exception;

    public void add(InputChangedListener var1);

    public void remove(InputChangedListener var1);

    public String getTitle();

    public String getID();

    public String getInstructions();

    public boolean isInputValid();

    public String getNextID();

    public boolean isPanelOk();
}

