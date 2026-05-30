/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Polygon
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class DrawnMarkUtils {
    public static void generateDrawnMarkSymbolFile(String symbolFilePath, FeatureCollection fcToSave, boolean allowOverwrite) throws Exception {
        File drawnMarkSymbolFile = new File(symbolFilePath);
        if (!allowOverwrite && drawnMarkSymbolFile.exists()) {
            throw new IOException(I18N.getMessage(DrawnMarkUtils.class, "symbol-file-{0}-already-exists", new Object[]{drawnMarkSymbolFile.getAbsolutePath()}));
        }
        FileOutputStream output = new FileOutputStream(drawnMarkSymbolFile);
        PrintWriter out = new PrintWriter(output);
        Coordinate center = null;
        FeatureIterator it = null;
        Envelope env = fcToSave.getEnvelope();
        center = env.centre();
        double factor = 1.0 / (env.getWidth() / 2.0);
        try {
            it = fcToSave.iterator();
            while (it.hasNext()) {
                Feature feature = it.next();
                Geometry g = feature.getGeometry();
                if (g instanceof Polygon) {
                    Polygon geom = (Polygon)feature.getGeometry();
                    Coordinate[] coords = geom.getExteriorRing().getCoordinates();
                    DrawnMarkUtils.writeSequence(out, center, factor, coords);
                    int k = 0;
                    while (k < geom.getNumInteriorRing()) {
                        LineString interiorRing = geom.getInteriorRingN(k);
                        coords = interiorRing.getCoordinates();
                        DrawnMarkUtils.writeSequence(out, center, factor, coords);
                        ++k;
                    }
                    continue;
                }
                Coordinate[] coords = g.getCoordinates();
                DrawnMarkUtils.writeSequence(out, center, factor, coords);
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
            out.close();
            output.close();
        }
    }

    private static void writeSequence(PrintWriter out, Coordinate center, double factor, Coordinate[] coords) {
        int i = 0;
        while (i < coords.length) {
            if (i == 0) {
                out.print("MOVETO ");
            } else {
                out.print("LINETO ");
            }
            out.print(String.valueOf((coords[i].x - center.x) * factor) + " ");
            out.println((coords[i].y - center.y) * factor);
            ++i;
        }
    }
}

