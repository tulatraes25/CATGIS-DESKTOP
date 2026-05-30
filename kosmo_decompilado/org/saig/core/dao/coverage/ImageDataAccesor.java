/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.dao.coverage;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Graphics2D;
import org.saig.core.renderer.RendererParameterWrapper;

public interface ImageDataAccesor {
    public static final int IMAGE_ECW = 0;
    public static final int IMAGE_MRSID = 1;
    public static final int IMAGE_OTHERS = 999;
    public static final String ECW_EXT = "ecw";
    public static final String MRSID_EXT = "sid";

    public void getImagen(Graphics2D var1, RendererParameterWrapper var2);

    public Envelope getEnvelope();

    public double getValue(double var1, double var3);

    public void close();

    public int getType();

    public String getImagePath();

    public Object getData(int var1, int var2, int var3);

    public int getHeightInPixels();

    public int getWidthInPixels();

    public double getXCellSize();

    public double getYCellSize();

    public int getNumBands();

    public double getNoDataValue();

    public void setNoDataValue(double var1);

    public int getDataType();
}

