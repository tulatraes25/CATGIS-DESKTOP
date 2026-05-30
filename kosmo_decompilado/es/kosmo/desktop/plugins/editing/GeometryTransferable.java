/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.WKTWriter
 */
package es.kosmo.desktop.plugins.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.AbstractTransferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class GeometryTransferable
extends AbstractTransferable {
    public static final DataFlavor WKT_FLAVOR = new DataFlavor((Class)String.class, "WKT_Geometry"){

        @Override
        public boolean equals(DataFlavor that) {
            return super.equals(that) && this.getHumanPresentableName().equals(that.getHumanPresentableName());
        }
    };
    private static final DataFlavor[] flavors = new DataFlavor[]{DataFlavor.stringFlavor, WKT_FLAVOR};
    private WKTWriter writer = new WKTWriter();
    private Geometry geometry;

    public GeometryTransferable(Geometry geom) {
        super(flavors);
        if (geom != null) {
            this.geometry = (Geometry)geom.clone();
        }
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(WKT_FLAVOR)) {
            return this.writer.write(this.geometry);
        }
        if (flavor.equals(DataFlavor.stringFlavor)) {
            return this.writer.write(this.geometry);
        }
        throw new UnsupportedFlavorException(flavor);
    }
}

