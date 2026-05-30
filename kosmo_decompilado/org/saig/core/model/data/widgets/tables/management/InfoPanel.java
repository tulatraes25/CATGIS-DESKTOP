/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.saig.jump.lang.I18N;

public class InfoPanel
extends JPanel {
    private JLabel infoLabel;
    private JLabel registrosTotalesLabel;
    private JLabel registrosMostradosLabel;

    public InfoPanel() {
        this.setLayout(new BoxLayout(this, 0));
        this.add(new JLabel(I18N.getString(this.getClass(), "current")));
        this.initializeInfoLabel();
        this.registrosTotalesLabel = new JLabel();
        this.registrosMostradosLabel = new JLabel();
        this.add(this.infoLabel);
        this.add(new JLabel("  -  "));
        this.add(new JLabel(I18N.getString(this.getClass(), "total")));
        this.registrosTotalesLabel = new JLabel();
        this.add(this.registrosTotalesLabel);
        this.add(new JLabel("  -  "));
        this.add(new JLabel(I18N.getString(this.getClass(), "shown")));
        this.registrosMostradosLabel = new JLabel();
        this.add(this.registrosMostradosLabel);
    }

    public void updateRanges(int totalNumRecords, int firstRecordShown, int lastRecordShown) {
        this.registrosTotalesLabel.setText("" + totalNumRecords);
        this.registrosMostradosLabel.setText(String.valueOf(firstRecordShown) + "/" + lastRecordShown);
    }

    public void updateInfoLabel(int firstRecordShown, int actualRecord) {
        int actual = firstRecordShown + actualRecord - 1;
        String info = "" + actual;
        this.infoLabel.setText(info);
    }

    private void initializeInfoLabel() {
        this.infoLabel = new JLabel();
        Dimension dim = new Dimension(60, 25);
        this.infoLabel.setMinimumSize(dim);
        this.infoLabel.setPreferredSize(dim);
        this.infoLabel.setToolTipText(I18N.getString(this.getClass(), "current-record-total-number-of-records"));
        this.infoLabel.setHorizontalAlignment(0);
        this.infoLabel.setText("0/0");
    }
}

