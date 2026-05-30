/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.control;

import org.saig.core.model.data.widgets.tables.management.control.AdvancedDBControlPanel;

public class AdvancedNotEditableDBControlPanel
extends AdvancedDBControlPanel {
    @Override
    public void evaluateButtons() {
        this.buttonDelete.setEnabled(false);
        this.buttonCommit.setEnabled(false);
        this.buttonRollback.setEnabled(false);
        this.buttonInsert.setEnabled(false);
        this.buttonDelete.setEnabled(false);
    }
}

