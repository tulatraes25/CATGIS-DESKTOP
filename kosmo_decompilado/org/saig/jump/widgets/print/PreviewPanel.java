/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JPanel;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.elements.GraphicElements;

public class PreviewPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private Page page = null;
    private int unit;
    private int cm;
    private int inch;

    public PreviewPanel(PrintLayoutFrame parent) {
        this.page = new Page(parent);
        this.setUnits(parent);
        this.setLayout(new GridBagLayout());
        this.add((Component)this.page.getPageDrawOnScreen(), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(this.unit, this.unit, this.unit, this.unit), 0, 0));
    }

    public PreviewPanel(PrintLayoutFrame parent, Page page) {
        this.page = page;
        this.setUnits(parent);
        this.setLayout(new GridBagLayout());
        this.add((Component)page.getPageDrawOnScreen(), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, 10, 0, new Insets(this.unit, this.unit, this.unit, this.unit), 0, 0));
    }

    public Page getPage() {
        return this.page;
    }

    public int getCmUnit() {
        return this.cm;
    }

    public int getInchUnit() {
        return this.inch;
    }

    public void dispose() {
        if (this.page != null) {
            if (this.page.getPageDrawOnScreen() != null) {
                this.remove(this.page.getPageDrawOnScreen());
            }
            this.page.dispose();
            this.page = null;
        }
    }

    public void setUnits(PrintLayoutFrame parent) {
        switch (parent.getPageFormat().getOrientation()) {
            case 0: {
                this.cm = Math.round((float)((double)this.page.getPageDrawOnScreen().getWidth() / Conversion.seventyTwoInch_To_Cm(parent.getPageFormat().getWidth())));
                this.inch = Math.round((float)((double)this.page.getPageDrawOnScreen().getWidth() / (double)Conversion.seventyTwoInch_To_Inch(parent.getPageFormat().getWidth())));
                break;
            }
            case 1: {
                this.cm = Math.round((float)((double)this.page.getPageDrawOnScreen().getHeight() / Conversion.seventyTwoInch_To_Cm(parent.getPageFormat().getHeight())));
                this.inch = Math.round((float)((double)this.page.getPageDrawOnScreen().getHeight() / (double)Conversion.seventyTwoInch_To_Inch(parent.getPageFormat().getHeight())));
            }
        }
        this.unit = parent.getPrintLayoutPreviewPanel().isMetric() ? this.cm : this.inch;
    }

    public List<GraphicElements> getGraphicElements() {
        return this.page.getGraphicElements();
    }

    public void setPage(Page page) {
        this.page = page;
    }
}

