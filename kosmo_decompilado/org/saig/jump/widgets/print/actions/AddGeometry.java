/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.elements.geometry.GeometryFrame;
import org.saig.jump.widgets.print.elements.geometry.LineElement;

public class AddGeometry
extends PrintAction {
    private static final String strLinea = I18N.getString("org.saig.jump.widgets.print.actions.AddGeometry.line");
    private static final String strCuadrado = I18N.getString("org.saig.jump.widgets.print.actions.AddGeometry.square");
    private static final String strRectangulo = I18N.getString("org.saig.jump.widgets.print.actions.AddGeometry.rectangle");
    private static final String strCirculo = I18N.getString("org.saig.jump.widgets.print.actions.AddGeometry.circle");
    private static final String strElipse = I18N.getString("org.saig.jump.widgets.print.actions.AddGeometry.ellipse");
    private JPopupMenu popup = new JPopupMenu();
    private JMenuItem m1 = new JMenuItem(strLinea);
    private JMenuItem m2 = new JMenuItem(strCuadrado);
    private JMenuItem m3 = new JMenuItem(strRectangulo);
    private JMenuItem m4 = new JMenuItem(strCirculo);
    private JMenuItem m5 = new JMenuItem(strElipse);

    public AddGeometry(PrintLayoutFrame plf) {
        super(plf);
        this.popup.add(this.m1);
        this.popup.add(this.m2);
        this.popup.add(this.m3);
        this.popup.add(this.m4);
        this.popup.add(this.m5);
        ActionListener actLis = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem it = (JMenuItem)e.getSource();
                String geom = it.getText();
                if (geom.equals(strLinea)) {
                    AddGeometry.this.frame.setGraphic(new LineElement(AddGeometry.this.frame));
                    ((Page.PageDrawOnScreen)AddGeometry.this.frame.getPage().getPageDrawOnScreen()).getCenter().getMouseListeners()[0].mousePressed(new MouseEvent(AddGeometry.this.frame.getPage().getPageDrawOnScreen(), 0, 0L, 0, 0, 0, 1, false));
                    AddGeometry.this.frame.setGraphic(new LineElement(AddGeometry.this.frame));
                    ((Page.PageDrawOnScreen)AddGeometry.this.frame.getPage().getPageDrawOnScreen()).getCenter().getMouseListeners()[0].mouseReleased(new MouseEvent(AddGeometry.this.frame.getPage().getPageDrawOnScreen(), 0, 0L, 0, 0, 0, 1, false));
                } else if (geom.equals(strCuadrado)) {
                    AddGeometry.this.frame.setGraphic(new GeometryFrame(AddGeometry.this.frame, 4));
                } else if (geom.equals(strRectangulo)) {
                    AddGeometry.this.frame.setGraphic(new GeometryFrame(AddGeometry.this.frame, 1));
                } else if (geom.equals(strCirculo)) {
                    AddGeometry.this.frame.setGraphic(new GeometryFrame(AddGeometry.this.frame, 5));
                } else if (geom.equals(strElipse)) {
                    AddGeometry.this.frame.setGraphic(new GeometryFrame(AddGeometry.this.frame, 2));
                }
            }
        };
        this.m1.addActionListener(actLis);
        this.m2.addActionListener(actLis);
        this.m3.addActionListener(actLis);
        this.m4.addActionListener(actLis);
        this.m5.addActionListener(actLis);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Component but = (Component)e.getSource();
        this.popup.show(but, 0, 0);
    }
}

