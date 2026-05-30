/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import java.awt.Color;

public interface SnapPolicy {
    public Coordinate snap(LayerViewPanel var1, Coordinate var2, Geometry var3);

    public Color getColor();

    public String getName();
}

