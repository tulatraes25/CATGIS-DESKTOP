/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.dialog;

import java.awt.GridLayout;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.CRSSelectionDialog;

public class CSSelectionDialog
extends CRSSelectionDialog {
    private static final long serialVersionUID = 1L;

    public CSSelectionDialog(ICrs curProj) {
        super(curProj);
        this.init();
    }

    private void init() {
        this.setBounds(0, 0, 600, 400);
        this.setLayout(new GridLayout(0, 1));
    }
}

