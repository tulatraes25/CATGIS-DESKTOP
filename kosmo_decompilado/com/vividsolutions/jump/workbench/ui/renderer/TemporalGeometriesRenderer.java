/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.AbstractRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.TemporalGeometryItem;
import com.vividsolutions.jump.workbench.ui.renderer.ThreadSafeImage;
import java.awt.Graphics2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TemporalGeometriesRenderer
extends AbstractRenderer {
    public static final String CONTENT_ID = "TEMPORAL_GEOMETRY";
    private static Map<Integer, TemporalGeometryItem> geomItemsMap = new HashMap<Integer, TemporalGeometryItem>();

    public TemporalGeometriesRenderer(LayerViewPanel panel, double factor) {
        super(CONTENT_ID, panel, factor);
    }

    public void add(int geometryId, TemporalGeometryItem temporalGeometryItem) {
        geomItemsMap.put(geometryId, temporalGeometryItem);
    }

    public void remove(int geometryId) {
        geomItemsMap.remove(geometryId);
    }

    public void clearTemporalGeometries() {
        geomItemsMap.clear();
    }

    @Override
    protected void renderHook(ThreadSafeImage image) throws Exception {
        image.draw(new ThreadSafeImage.Drawer(){

            @Override
            public void draw(Graphics2D g) throws Exception {
                ArrayList keys = new ArrayList(geomItemsMap.keySet());
                Collections.sort(keys);
                for (Integer key : keys) {
                    TemporalGeometryItem tgItem = (TemporalGeometryItem)geomItemsMap.get(key);
                    if (tgItem == null || tgItem.getGeometry() == null) continue;
                    g.setColor(tgItem.getColor());
                    g.setStroke(tgItem.getStroke());
                    try {
                        g.draw(TemporalGeometriesRenderer.this.panel.getViewport().getJava2DConverter().toShape(tgItem.getGeometry()));
                    }
                    catch (NoninvertibleTransformException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}

