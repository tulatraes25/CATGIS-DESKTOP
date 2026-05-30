/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.DragTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class PanTool
extends DragTool {
    public static final String NAME = I18N.getString("workbench.ui.zoom.PanTool.name");
    public static final Icon ICON = IconLoader.icon("BigHand.gif");
    public static final Cursor CURSOR = PanTool.createCursor(IconLoader.icon("Hand.gif").getImage());
    protected boolean dragging = false;
    protected Image image;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public Cursor getCursor() {
        return CURSOR;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        try {
            if (!this.dragging) {
                this.dragging = true;
                this.getPanel().getRenderingManager().setPaintingEnabled(false);
                this.cacheImage();
            }
            this.getPanel().erase((Graphics2D)this.getPanel().getGraphics());
            this.drawImage(e.getPoint());
            super.mouseDragged(e);
        }
        catch (Throwable t) {
            this.getPanel().getContext().handleThrowable(t);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!this.dragging) {
            return;
        }
        this.getPanel().getRenderingManager().setPaintingEnabled(true);
        this.dragging = false;
        super.mouseReleased(e);
        this.image = null;
    }

    @Override
    protected Shape getShape(Point2D source, Point2D destination) {
        return null;
    }

    @Override
    protected void gestureFinished() throws NoninvertibleTransformException {
        this.reportNothingToUndoYet();
        double xDisplacement = this.getModelDestination().x - this.getModelSource().x;
        double yDisplacement = this.getModelDestination().y - this.getModelSource().y;
        Envelope oldEnvelope = this.getPanel().getViewport().getEnvelopeInModelCoordinates();
        this.getPanel().getViewport().zoom(new Envelope(oldEnvelope.getMinX() - xDisplacement, oldEnvelope.getMaxX() - xDisplacement, oldEnvelope.getMinY() - yDisplacement, oldEnvelope.getMaxY() - yDisplacement));
    }

    private void cacheImage() {
        this.image = this.getPanel().createBlankPanelImage();
        this.getPanel().paint(this.image.getGraphics());
    }

    private void drawImage(Point p) throws NoninvertibleTransformException {
        double dx = p.getX() - this.getViewSource().getX();
        double dy = p.getY() - this.getViewSource().getY();
        this.getPanel().getGraphics().drawImage(this.image, (int)dx, (int)dy, this.getPanel());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        return solucion;
    }
}

