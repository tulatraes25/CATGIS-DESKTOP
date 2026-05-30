/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.legend;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.wms.MapStyle;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.widgets.print.elements.legend.PrintFont;

public class MapLegend
extends JLabel {
    private static final long serialVersionUID = 1L;
    private Icon symbol;
    private PrintFont printFont;
    private Symbolizer[] symbols;
    private String title;
    private boolean visible = true;
    private boolean isWMS = false;

    public MapLegend() {
        this.setBackground(Color.WHITE);
        this.setOpaque(false);
        this.setIconTextGap(8);
    }

    public MapLegend(Layerable layer, Symbolizer[] symbols, String title) {
        this.setBackground(Color.WHITE);
        this.setOpaque(false);
        this.setIconTextGap(8);
        if (layer instanceof Layer) {
            this.symbols = symbols;
            this.title = title;
            this.setText(title);
        } else if (layer instanceof WMSLayer) {
            List<MapStyle> layerStyles = ((WMSLayer)layer).getLayerStyles();
            if (!layerStyles.isEmpty()) {
                MapStyle style = layerStyles.iterator().next();
                this.symbol = style.getLegendIcon();
            }
            this.isWMS = true;
        }
    }

    public MapLegend(MapLegend ml) {
        this(null, ml.symbols, ml.title);
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public PrintFont getPrintFont() {
        return this.printFont;
    }

    public void setPrintFont(PrintFont font) {
        int size;
        this.printFont = font;
        this.setFont(this.printFont.getFont());
        this.setForeground(this.printFont.getColor());
        if (this.printFont.isUnderline()) {
            this.setText("<html><u>" + this.getText() + "</u></html>");
        } else {
            this.setText(this.getText().replaceAll("(?i)<html><u>", ""));
            this.setText(this.getText().replaceAll("(?i)</u></html>", ""));
        }
        this.setBackground(Color.WHITE);
        this.setOpaque(false);
        if (!this.isWMS && (size = font.getFontSize()) > 0) {
            this.symbol = LegendIconMaker.makeLegendIcon(font.getFontSize(), font.getFontSize(), new Color(0, 0, 0, 0), this.symbols, null, true);
        }
        this.setIcon(this.symbol);
        this.repaint();
    }

    public Icon getSymbol() {
        return this.symbol;
    }

    public boolean isWMSLegend() {
        return this.isWMS;
    }

    public void paintGoodIcon(Graphics g, int x, int y, int size) {
        if (!this.isWMS) {
            LegendIconMaker.reallyMakeLegendIcon(size, size, new Color(0, 0, 0, 0), this.symbols, (Graphics2D)g, x, y);
        } else {
            g.drawImage(((ImageIcon)this.getSymbol()).getImage(), x, y, null);
        }
    }
}

