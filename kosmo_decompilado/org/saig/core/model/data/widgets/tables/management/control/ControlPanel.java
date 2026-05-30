/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.control;

import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.saig.core.model.data.widgets.tables.management.TableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.operations.OperationsManager;

public interface ControlPanel {
    public void setTablePanel(TableManagementPanel var1);

    public void addButton(JComponent var1);

    public void addActionListenerToButtons(ActionListener var1);

    public void evaluateButtons();

    public void setManager(OperationsManager var1);
}

