/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.print;

import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class PrintViewPanel
extends LayerViewPanel {
    private static final long serialVersionUID = 1L;

    public PrintViewPanel() {
        this.renderingManager = new RenderingManager(this, false, 10);
    }

    @Override
    public void init(LayerManager layerManager, LayerViewPanelContext context) {
        this.setMinimumSize(new Dimension(100, 100));
        this.context = context;
        this.layerManager = layerManager;
        this.renderingManager = new RenderingManager(this, false, 10);
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PrintViewPanel(LayerManager layerManager, LayerViewPanelContext context, Rectangle bounds) {
        this.context = context;
        this.layerManager = layerManager;
        this.setBounds(bounds);
        this.renderingManager = new RenderingManager(this, false, 10);
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Image createBlankPanelImage() {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            return new BufferedImage(this.getWidth(), this.getHeight(), 2);
        }
        return null;
    }

    protected void jbInit() throws Exception {
        this.setBackground(Color.white);
        this.setLayout(this.borderLayout1);
    }

    @Override
    public void setSize(Dimension dimension) {
        super.setSize(dimension);
    }
}

