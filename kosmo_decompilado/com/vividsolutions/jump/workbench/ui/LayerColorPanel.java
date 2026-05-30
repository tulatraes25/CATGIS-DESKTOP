/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.ColorPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.DummyStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Iterator;
import javax.swing.JPanel;

public class LayerColorPanel
extends JPanel {
    private ColorPanel oneColorPanel = new ColorPanel();
    private FourColorPanel fourColorPanel = new FourColorPanel();

    public LayerColorPanel() {
        this.setLayout(new BorderLayout());
        this.add(this.oneColorPanel);
        this.setMaximumSize(new Dimension(10, 10));
        this.setMinimumSize(new Dimension(10, 10));
        this.setPreferredSize(new Dimension(10, 10));
    }

    public LayerColorPanel init(Layer layer, boolean selected, Color backgroundColor, Color selectionBackgroundColor) {
        ColorThemingStyle colorThemingStyle;
        if (layer.getBasicStyle().isEnabled()) {
            if (this.getComponents()[0] == this.fourColorPanel) {
                this.remove(this.fourColorPanel);
                this.add(this.oneColorPanel);
            }
            this.color(this.oneColorPanel, layer.getBasicStyle(), selected, backgroundColor, selectionBackgroundColor);
        }
        if ((colorThemingStyle = (ColorThemingStyle)layer.getStyle(ColorThemingStyle.class)) != null && colorThemingStyle.isEnabled()) {
            if (this.getComponents()[0] == this.oneColorPanel) {
                this.remove(this.oneColorPanel);
                this.add(this.fourColorPanel);
            }
            Iterator<BasicStyle> styles = colorThemingStyle.getAttributeValueToBasicStyleMap().values().iterator();
            this.color(this.fourColorPanel.panel1, colorThemingStyle.getDefaultStyle(), selected, backgroundColor, selectionBackgroundColor);
            this.color(this.fourColorPanel.panel2, styles.hasNext() ? (Style)styles.next() : DummyStyle.instance(), selected, backgroundColor, selectionBackgroundColor);
            this.color(this.fourColorPanel.panel3, styles.hasNext() ? (Style)styles.next() : DummyStyle.instance(), selected, backgroundColor, selectionBackgroundColor);
            this.color(this.fourColorPanel.panel4, styles.hasNext() ? (Style)styles.next() : DummyStyle.instance(), selected, backgroundColor, selectionBackgroundColor);
        }
        return this;
    }

    private void color(ColorPanel colorPanel, Style style, boolean selected, Color backgroundColor, Color selectionBackgroundColor) {
        colorPanel.setLineColor(style instanceof BasicStyle && ((BasicStyle)style).isRenderingLine() ? GUIUtil.alphaColor(((BasicStyle)style).getLineColor(), ((BasicStyle)style).getAlpha()) : (selected ? selectionBackgroundColor : backgroundColor));
        colorPanel.setFillColor(style instanceof BasicStyle && ((BasicStyle)style).isRenderingFill() ? GUIUtil.alphaColor(((BasicStyle)style).getFillColor(), ((BasicStyle)style).getAlpha()) : (selected ? selectionBackgroundColor : backgroundColor));
    }

    public static class FourColorPanel
    extends JPanel {
        public ColorPanel panel1 = new ColorPanel();
        public ColorPanel panel2 = new ColorPanel();
        public ColorPanel panel3 = new ColorPanel();
        public ColorPanel panel4 = new ColorPanel();

        public FourColorPanel() {
            GridLayout gridLayout = new GridLayout(2, 2);
            this.setLayout(gridLayout);
            this.add(this.panel1);
            this.add(this.panel2);
            this.add(this.panel3);
            this.add(this.panel4);
        }
    }
}

