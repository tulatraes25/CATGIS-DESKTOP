/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.wizard;

import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.wizard.WizardPanel;
import java.util.Map;
import javax.swing.JPanel;

public abstract class AbstractWizardPanel
extends JPanel
implements WizardPanel {
    private static final long serialVersionUID = 1L;
    protected InputChangedFirer inputChangedFirer = new InputChangedFirer();
    protected Map<String, Object> dataMap;

    @Override
    public void add(InputChangedListener listener) {
        this.inputChangedFirer.add(listener);
    }

    @Override
    public void remove(InputChangedListener listener) {
        this.inputChangedFirer.remove(listener);
    }
}

