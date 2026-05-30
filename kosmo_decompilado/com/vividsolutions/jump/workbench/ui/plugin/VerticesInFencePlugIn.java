/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.PointLocator
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryComponentFilter
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.io.WKTWriter
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.algorithm.PointLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.util.Fmt;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TextFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.WKTDisplayHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class VerticesInFencePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.VerticesInFencePlugIn.name");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private WKTWriter wktWriter = new WKTWriter();
    private WKTDisplayHelper helper = new WKTDisplayHelper();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return VerticesInFencePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        TextFrame textFrame = new TextFrame(context.getWorkbenchFrame());
        textFrame.setTitle(NAME);
        textFrame.clear();
        textFrame.setText(this.description(context));
        textFrame.setSize(550, 300);
        context.getWorkbenchFrame().addInternalFrame(textFrame);
        return true;
    }

    private String description(PlugInContext context) throws Exception {
        FenceLayerFinder fenceLayerFinder = new FenceLayerFinder(context);
        StringBuffer description = new StringBuffer();
        description.append("<html><body>");
        Iterator<Layer> i = context.getLayerManager().iterator();
        while (i.hasNext()) {
            Layer layer = i.next();
            if (!layer.isVisible() || layer == fenceLayerFinder.getLayer()) continue;
            description.append(this.description(layer, context));
        }
        description.append("</body></html>");
        return description.toString();
    }

    public static Collection<Coordinate> verticesInFence(Collection<Geometry> geometries, Geometry fence, boolean skipClosingVertex) {
        ArrayList<Coordinate> verticesInFence = new ArrayList<Coordinate>();
        for (Geometry geometry : geometries) {
            verticesInFence.addAll(VerticesInFencePlugIn.verticesInFence(geometry, fence, skipClosingVertex).getCoordinates());
        }
        return verticesInFence;
    }

    public static VerticesInFence verticesInFence(Geometry geometry, final Geometry fence, final boolean skipClosingVertex) {
        final ArrayList coordinates = new ArrayList();
        final ArrayList indices = new ArrayList();
        final PointLocator pointLocator = new PointLocator();
        final IntWrapper index = new IntWrapper(-1);
        geometry.apply(new GeometryComponentFilter(){

            public void filter(Geometry geometry) {
                if (geometry instanceof GeometryCollection || geometry instanceof Polygon) {
                    return;
                }
                Coordinate[] component = geometry.getCoordinates();
                int j = 0;
                while (j < component.length) {
                    ++index.value;
                    if (!(skipClosingVertex && component.length > 1 && j == component.length - 1 && component[j].equals((Object)component[0]) || pointLocator.locate(component[j], fence) == 2)) {
                        coordinates.add(component[j]);
                        indices.add(new Integer(index.value));
                    }
                    ++j;
                }
            }
        });
        return new VerticesInFence(){

            @Override
            public List<Coordinate> getCoordinates() {
                return coordinates;
            }

            @Override
            public int getIndex(int i) {
                return (Integer)indices.get(i);
            }
        };
    }

    private String description(Layer layer, PlugInContext context) throws Exception {
        boolean foundVertices = false;
        String description = "<Table width=100%><tr><td colspan=2 valign=top><i>Layer: </i><font color='#3300cc'><b>" + layer.getName() + "</b></font></td></tr>";
        String bgcolor = "darkgrey";
        for (Feature feature : layer.getFeatureCollectionWrapper().query(context.getLayerViewPanel().getFence().getEnvelopeInternal())) {
            VerticesInFence verticesInFence = VerticesInFencePlugIn.verticesInFence(feature.getGeometry(), context.getLayerViewPanel().getFence(), true);
            if (verticesInFence.getCoordinates().isEmpty()) continue;
            bgcolor = bgcolor.equals("#faebd7") ? "darkgrey" : "#faebd7";
            foundVertices = true;
            description = String.valueOf(description) + "<tr bgcolor=" + bgcolor + "><td width=10% valign=top><font size='-1'><i>Feature ID: </i></font><font size='-1' color='#3300cc'><b>" + feature.getID() + "</b></font><td>";
            description = String.valueOf(description) + this.description(verticesInFence, feature.getGeometry());
            description = String.valueOf(description) + "</td></tr>";
        }
        description = String.valueOf(description) + "</table>";
        return foundVertices ? description : "";
    }

    private String description(VerticesInFence verticesInFence, Geometry geometry) {
        StringBuffer description = new StringBuffer();
        int i = 0;
        while (i < verticesInFence.getCoordinates().size()) {
            description.append(GUIUtil.escapeHTML("[" + Fmt.fmt(this.helper.annotation(geometry, verticesInFence.getCoordinates().get(i)), 10) + "] ", true));
            description.append("<code>" + GUIUtil.escapeHTML(this.wktWriter.write((Geometry)geomFac.createPoint(verticesInFence.getCoordinates().get(i))), true) + "</code><br>");
            ++i;
        }
        return description.toString();
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createFenceMustBeDrawnCheck());
    }

    private static class IntWrapper {
        public int value;

        public IntWrapper(int value) {
            this.value = value;
        }
    }

    public static interface VerticesInFence {
        public List<Coordinate> getCoordinates();

        public int getIndex(int var1);
    }
}

