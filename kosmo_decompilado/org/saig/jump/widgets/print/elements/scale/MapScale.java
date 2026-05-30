/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package org.saig.jump.widgets.print.elements.scale;

import com.vividsolutions.jump.util.MathUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.IncrementChooser;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.RoundQuantity;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.JPanel;
import org.saig.core.util.UnitsManager;

public class MapScale
extends JPanel {
    private static final int BAR_HEIGHT = 10;
    private static final Color FILL2 = new Color(255, 204, 204);
    private static final Color FILL1 = Color.white;
    private static final int HORIZONTAL_MARGIN = 3;
    private static final int IDEAL_INCREMENT = 40;
    private static final Color LINE_COLOR = Color.black;
    private static final int TEXT_BOTTOM_MARGIN = 1;
    private static final int UNIT_TEXT_BOTTOM_MARGIN = 1;
    private static final Color TEXT_COLOR = Color.black;
    private static final Color UNIT_TEXT_COLOR = Color.blue;
    private static final int VERTICAL_MARGIN = 3;
    private static final int INCREMENT_COUNT = 3;
    private Font FONT = new Font("Dialog", 0, 10);
    private Font UNIT_FONT = new Font("Dialog", 1, 11);
    private LayerViewPanel panel;
    private int incX = 0;
    private int incY = 0;

    public MapScale(LayerViewPanel panel) {
        this.panel = panel;
        this.setBackground(Color.WHITE);
        this.setOpaque(false);
        FlowLayout layout = new FlowLayout();
        this.setLayout(layout);
        this.removeAll();
    }

    @Override
    public void paint(Graphics g) {
        if (this.panel == null) {
            return;
        }
        if (this.panel.getViewport() != null) {
            this.paint((Graphics2D)g, this.panel.getViewport().getScale());
        }
    }

    public void print(Graphics2D g, int incX, int incY) {
        if (this.panel == null) {
            return;
        }
        this.incX = incX;
        this.incY = incY;
        this.paint(g, this.panel.getViewport().getScale());
        this.incX = 0;
        this.incY = 0;
    }

    public void paint(Graphics2D g, double scale) {
        Collection<Unit<Length>> units = null;
        switch (UnitsManager.getUnitsSystem(this.panel.getMapLengthUnit())) {
            case 1: {
                units = UnitsManager.getNonSILengthUnits();
                break;
            }
            case 2: {
                units = UnitsManager.getSILengthUnits();
            }
        }
        RoundQuantity increment = new IncrementChooser().chooseGoodIncrement(units, 40.0 / scale, this.panel.getMapLengthUnit());
        this.paintIncrements(increment, 3, g, scale);
    }

    private TextLayout createTextLayout(String text, Font font, Graphics2D g) {
        return new TextLayout(text, font, g.getFontRenderContext());
    }

    private Rectangle2D.Double paintIncrement(int i, RoundQuantity increment, int incrementCount, Graphics2D g, double scale) {
        BasicStroke strokeLeyenda = new BasicStroke(1.0f);
        g.setStroke(strokeLeyenda);
        Rectangle2D.Double shape = new Rectangle2D.Double(this.x(i, increment, incrementCount, scale) + (double)this.incX, this.incY, this.x(i + 1, increment, incrementCount, scale) - this.x(i, increment, incrementCount, scale), this.barBottom() - this.barTop());
        g.setColor(i % 2 == 0 ? FILL1 : FILL2);
        g.fill(shape);
        g.setColor(LINE_COLOR);
        g.draw(shape);
        return shape;
    }

    private int barBottom() {
        return this.panel.getHeight() - 3;
    }

    private int barTop() {
        return this.barBottom() - 10;
    }

    private void paintIncrements(RoundQuantity increment, int incrementCount, Graphics2D g, double scale) {
        int i = 0;
        while (i < incrementCount) {
            Rectangle2D.Double rec = this.paintIncrement(i, increment, incrementCount, g, scale);
            this.paintLabel(i, increment, incrementCount, g, scale, rec.getHeight());
            ++i;
        }
    }

    private void paintLabel(int i, RoundQuantity increment, int incrementCount, Graphics2D g, double scale, double height) {
        String text = new RoundQuantity(increment.getMantissa() * (i + 1), increment.getExponent(), increment.getUnit(), this.panel.getMapLengthUnit()).getAmountString();
        Font font = this.FONT;
        g.setColor(TEXT_COLOR);
        boolean textBottomMargin = true;
        if (i == incrementCount - 1) {
            text = increment.getUnit().toString();
            font = this.UNIT_FONT;
            g.setColor(UNIT_TEXT_COLOR);
            textBottomMargin = true;
        }
        TextLayout layout = this.createTextLayout(text, font, g);
        double center = MathUtil.avg(this.x(i, increment, incrementCount, scale), this.x(i + 1, increment, incrementCount, scale));
        layout.draw(g, (float)(center - (double)(layout.getAdvance() / 2.0f)) + (float)this.incX, (float)(height - (double)textBottomMargin) + (float)this.incY);
    }

    private double x(int i, RoundQuantity increment, int incrementCount, double scale) {
        return 3.0 + (double)i * increment.getModelValue() * scale;
    }

    public Component[] getMapLegendComponents() {
        return this.getComponents();
    }
}

