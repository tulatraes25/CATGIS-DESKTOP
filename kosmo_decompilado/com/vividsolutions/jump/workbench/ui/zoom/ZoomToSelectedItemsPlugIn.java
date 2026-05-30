/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.util.Assert
 *  org.cresques.cts.ICoordTrans
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigZoomPanel;

public class ZoomToSelectedItemsPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.zoom.ZoomToSelectedItemsPlugIn.name");
    public static final Icon ICON = IconLoader.icon("ZoomSelected.gif");
    public static final int MAX_NUM_GEOMETRIES_ALLOWED_TO_FLASH = 250;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        this.zoom(context.getLayerViewPanel().getSelectionManager().getSelectedItems(), context.getLayerViewPanel());
        return true;
    }

    public void zoom(Collection<Geometry> geometries, LayerViewPanel panel) throws NoninvertibleTransformException {
        double currentWidth;
        Envelope geomEnvelope = this.envelope(geometries);
        if (geomEnvelope.isNull()) {
            return;
        }
        Envelope proposedEnvelope = EnvelopeUtil.bufferByFraction(geomEnvelope, ConfigZoomPanel.getExtentFraction());
        if (proposedEnvelope.getWidth() == 0.0) {
            proposedEnvelope = EnvelopeUtil.bufferByFraction(geomEnvelope, this.zoomBufferAsExtentFraction(geometries));
        }
        if (panel.getProjection() != null && panel.getProjection().isProjected() && (currentWidth = proposedEnvelope.getWidth()) < 0.5) {
            proposedEnvelope = EnvelopeUtil.expand(proposedEnvelope, 0.5 - currentWidth);
        }
        panel.getViewport().zoom(proposedEnvelope);
    }

    private Envelope envelope(Collection<Geometry> geometries) {
        Envelope envelope = new Envelope();
        for (Geometry geometry : geometries) {
            if (geometry == null) continue;
            envelope.expandToInclude(geometry.getEnvelopeInternal());
        }
        return envelope;
    }

    private double zoomBufferAsExtentFraction(Collection<Geometry> geometries) {
        double zoomBuffer = 2.0 * this.averageExtent(geometries);
        double averageFullExtent = this.averageFullExtent(geometries);
        if (averageFullExtent == 0.0) {
            return 0.0;
        }
        return zoomBuffer / averageFullExtent;
    }

    private double averageExtent(Collection<Geometry> geometries) {
        Assert.isTrue((!geometries.isEmpty() ? 1 : 0) != 0);
        double extentSum = 0.0;
        for (Geometry geometry : geometries) {
            extentSum += geometry.getEnvelopeInternal().getWidth();
            extentSum += geometry.getEnvelopeInternal().getHeight();
        }
        return extentSum / (2.0 * (double)geometries.size());
    }

    private double averageFullExtent(Collection<Geometry> geometries) {
        Envelope envelope = this.envelope(geometries);
        return (envelope.getWidth() + envelope.getHeight()) / 2.0;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1));
    }

    public void flash(Collection<Geometry> geometries, LayerViewPanel panel, ICoordTrans coordTrans) throws NoninvertibleTransformException {
        ArrayList<Geometry> geometriesOk = new ArrayList();
        if (coordTrans != null) {
            for (Geometry currentGeometry : geometries) {
                IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(currentGeometry);
                pathGeom.reProject(coordTrans);
                Geometry geomRepro = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                geometriesOk.add(geomRepro);
            }
        } else {
            geometriesOk = geometries;
        }
        ZoomToSelectedItemsPlugIn.flash(geometriesOk, panel);
    }

    public static void flash(Collection<Geometry> geometries, LayerViewPanel panel) throws NoninvertibleTransformException {
        ArrayList<Geometry> geometriesToFlash = new ArrayList<Geometry>();
        Envelope viewEnvelope = panel.getViewport().getEnvelopeInModelCoordinates();
        for (Geometry currentGeometry : geometries) {
            if (currentGeometry == null || !viewEnvelope.intersects(currentGeometry.getEnvelopeInternal())) continue;
            geometriesToFlash.add(currentGeometry);
        }
        if (geometriesToFlash.isEmpty() || geometriesToFlash.size() > 250) {
            return;
        }
        GeometryCollection gc = ZoomToSelectedItemsPlugIn.toGeometryCollection(geometriesToFlash);
        if (gc.getNumGeometries() <= 250) {
            panel.flash(gc);
        }
    }

    private static GeometryCollection toGeometryCollection(Collection<Geometry> geometries) {
        return new GeometryFactory().createGeometryCollection(geometries.toArray(new Geometry[0]));
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ZoomToSelectedItemsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public String getName() {
        return NAME;
    }
}

