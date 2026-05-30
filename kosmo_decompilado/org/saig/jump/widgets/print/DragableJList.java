/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.elements.geometry.GeometryFrame;
import org.saig.jump.widgets.print.elements.geometry.LineElement;
import org.saig.jump.widgets.print.elements.image.ImageFrame;
import org.saig.jump.widgets.print.elements.legend.LegendFrame;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.print.elements.north.NorthFrame;
import org.saig.jump.widgets.print.elements.scale.ScaleFrame;
import org.saig.jump.widgets.print.elements.text.GraphicText;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class DragableJList
extends JList {
    private static final long serialVersionUID = 1L;
    PrintLayoutFrame plf;
    private int index;
    private boolean mousePressed = false;

    public DragableJList(PrintLayoutFrame plf) {
        this.plf = plf;
        MouseMotionListener mouseListener = new MouseMotionListener(){

            @Override
            public void mouseDragged(MouseEvent e) {
                DragableJList.this.index = DragableJList.this.locationToIndex(e.getPoint());
                DragableJList.this.mousePressed = true;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                DragableJList.this.mousePressed = false;
            }
        };
        this.addMouseMotionListener(mouseListener);
        this.setCellRenderer(new MyCellRenderer());
    }

    public int getPressedIndex() {
        return this.index;
    }

    public void orderChanged() {
        this.plf.orderLayerableElements();
    }

    class MyCellRenderer
    extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        private ImageIcon imageIcon = PrintIconLoader.icon("addImage.gif");
        private ImageIcon legendIcon = PrintIconLoader.icon("addLegend.gif");
        private ImageIcon mapIcon = PrintIconLoader.icon("addMap.gif");
        private ImageIcon northIcon = PrintIconLoader.icon("addNorth.gif");
        private ImageIcon scaleIcon = PrintIconLoader.icon("addScale.gif");
        private ImageIcon textIcon = PrintIconLoader.icon("addText.gif");
        private ImageIcon geometryIcon = PrintIconLoader.icon("addGeometry.gif");

        MyCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf) {
            super.getListCellRendererComponent((JList<?>)list, value, index, iss, chf);
            if (value instanceof ImageFrame) {
                this.setIcon(this.imageIcon);
            } else if (value instanceof LegendFrame) {
                this.setIcon(this.legendIcon);
            } else if (value instanceof MapFrame) {
                this.setIcon(this.mapIcon);
            } else if (value instanceof NorthFrame) {
                this.setIcon(this.northIcon);
            } else if (value instanceof ScaleFrame) {
                this.setIcon(this.scaleIcon);
            } else if (value instanceof GraphicText) {
                this.setIcon(this.textIcon);
            } else if (value instanceof GeometryFrame) {
                this.setIcon(this.geometryIcon);
            } else if (value instanceof LineElement) {
                this.setIcon(this.geometryIcon);
            }
            if (index % 2 == 0) {
                this.setBackground(Color.WHITE);
            } else {
                this.setBackground(new Color(255, 255, 170));
            }
            if (iss) {
                this.setBackground(Color.BLUE);
            }
            return this;
        }
    }
}

