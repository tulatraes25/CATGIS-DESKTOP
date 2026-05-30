/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.style.AbstractPalettePanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.BorderFactory;

public class GridPalettePanel
extends AbstractPalettePanel {
    private static final int TILE_EXTENT = 26;
    private static final int ROWS = 6;
    private static final int COLUMNS = 5;
    private ColorPanel colorPanel;

    public GridPalettePanel() {
        this.setLayout(new GridLayout(6, 5));
        Iterator k = this.basicStyles().iterator();
        int i = 0;
        while (i < 6 && k.hasNext()) {
            int j = 0;
            while (j < 5 && k.hasNext()) {
                this.add(this.colorPanel((BasicStyle)k.next()));
                ++j;
            }
            ++i;
        }
    }

    @Override
    public void setAlpha(int alpha) {
        int i = 0;
        while (i < this.getComponentCount()) {
            ColorPanel colorPanel = (ColorPanel)this.getComponent(i);
            colorPanel.setFillColor(GUIUtil.alphaColor(colorPanel.getFillColor(), alpha));
            colorPanel.setLineColor(GUIUtil.alphaColor(colorPanel.getLineColor(), alpha));
            ++i;
        }
        this.repaint();
    }

    private Component colorPanel(final BasicStyle basicStyle) {
        ColorPanel colorPanel = new ColorPanel();
        colorPanel.setFillColor(basicStyle.getFillColor());
        colorPanel.setLineColor(basicStyle.getLineColor());
        colorPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        colorPanel.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                GridPalettePanel.this.fireBasicStyleChosen(basicStyle);
            }
        });
        Dimension size = new Dimension(26, 26);
        colorPanel.setMaximumSize(size);
        colorPanel.setMinimumSize(size);
        colorPanel.setPreferredSize(size);
        this.colorPanel = colorPanel;
        return colorPanel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.colorPanel.setEnabled(false);
    }
}

