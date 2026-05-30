/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  es.gva.cit.jgdal.Gdal
 *  es.gva.cit.jgdal.GdalBuffer
 *  es.gva.cit.jgdal.GdalException
 *  es.gva.cit.jgdal.GdalRasterBand
 *  es.gva.cit.jgdal.GeoTransform
 *  org.gvsig.raster.RasterLibrary
 *  org.gvsig.raster.dataset.BandList
 *  org.gvsig.raster.dataset.IBuffer
 *  org.gvsig.raster.dataset.properties.DatasetColorInterpretation
 *  org.gvsig.raster.dataset.properties.DatasetMetadata
 *  org.gvsig.raster.datastruct.ColorTable
 *  org.gvsig.raster.datastruct.Extent
 *  org.gvsig.raster.datastruct.Transparency
 *  org.gvsig.raster.process.RasterTask
 *  org.gvsig.raster.process.RasterTaskQueue
 *  org.gvsig.raster.util.RasterUtilities
 */
package org.gvsig.raster.dataset.io;

import es.gva.cit.jgdal.Gdal;
import es.gva.cit.jgdal.GdalBuffer;
import es.gva.cit.jgdal.GdalException;
import es.gva.cit.jgdal.GdalRasterBand;
import es.gva.cit.jgdal.GeoTransform;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import org.gvsig.raster.RasterLibrary;
import org.gvsig.raster.dataset.BandList;
import org.gvsig.raster.dataset.IBuffer;
import org.gvsig.raster.dataset.properties.DatasetColorInterpretation;
import org.gvsig.raster.dataset.properties.DatasetMetadata;
import org.gvsig.raster.datastruct.ColorTable;
import org.gvsig.raster.datastruct.Extent;
import org.gvsig.raster.datastruct.Transparency;
import org.gvsig.raster.process.RasterTask;
import org.gvsig.raster.process.RasterTaskQueue;
import org.gvsig.raster.util.RasterUtilities;

public class GdalNative
extends Gdal {
    private String fileName = null;
    private String shortName = "";
    public GeoTransform trans = null;
    public int width = 0;
    public int height = 0;
    public double originX = 0.0;
    public double originY = 0.0;
    public String version = "";
    protected int rBandNr = 1;
    protected int gBandNr = 2;
    protected int bBandNr = 3;
    protected int aBandNr = 4;
    private int[] dataType = null;
    protected DatasetMetadata metadata = null;
    protected boolean georeferenced = true;
    public int[] stepArrayX = null;
    public int[] stepArrayY = null;
    protected GdalRasterBand[] gdalBands = null;
    private double lastReadLine = -1.0;
    private int currentFullWidth = -1;
    private int currentFullHeight = -1;
    private int currentViewWidth = -1;
    private int currentViewHeight = -1;
    private double currentViewX = 0.0;
    private double viewportScaleX = 0.0;
    private double viewportScaleY = 0.0;
    private double stepX = 0.0;
    private double stepY = 0.0;
    public boolean isSupersampling = false;
    protected Transparency fileTransparency = null;
    protected ColorTable palette = null;
    protected DatasetColorInterpretation colorInterpr = null;
    protected AffineTransform ownTransformation = null;
    protected AffineTransform externalTransformation = new AffineTransform();
    int currentOverview = -1;
    int lastY = -1;

    public GdalNative(String fName) throws GdalException, IOException {
        this.init(fName);
    }

    private void init(String fName) throws GdalException, IOException {
        this.fileName = fName;
        this.open(fName, GA_ReadOnly);
        if (this.getPtro() == -1L) {
            throw new GdalException("Error en la apertura del fichero. El fichero no tiene un formato v\u00e1lido.");
        }
        this.width = this.getRasterXSize();
        this.height = this.getRasterYSize();
        int[] dt = new int[this.getRasterCount()];
        int i = 0;
        while (i < this.getRasterCount()) {
            dt[i] = this.getRasterBand(i + 1).getRasterDataType();
            if (dt[i] == GDT_Byte) {
                System.out.println("*** DEBUG: changed band " + i + " to type GDT_Int16");
                dt[i] = GDT_Int16;
            }
            ++i;
        }
        this.setDataType(dt);
        this.shortName = this.getDriverShortName();
        this.fileTransparency = new Transparency();
        this.colorInterpr = new DatasetColorInterpretation();
        this.metadata = new DatasetMetadata(this.getMetadata(), this.colorInterpr);
        this.colorInterpr.initColorInterpretation(this.getRasterCount());
        this.metadata.initNoDataByBand(this.getRasterCount());
        i = 0;
        while (i < this.getRasterCount()) {
            GdalRasterBand rb = this.getRasterBand(i + 1);
            String colorInt = this.getColorInterpretationName(rb.getRasterColorInterpretation());
            this.metadata.setNoDataValue(i, rb.getRasterNoDataValue() == -9999.0 ? RasterLibrary.defaultNoDataValue : rb.getRasterNoDataValue());
            this.metadata.setNoDataEnabled(rb.existsNoDataValue());
            this.colorInterpr.setColorInterpValue(i, colorInt);
            if (colorInt.equals("Alpha")) {
                this.fileTransparency.setTransparencyBand(i);
            }
            if (rb.getRasterColorTable() != null && this.palette == null) {
                this.palette = new ColorTable();
                this.palette.createPaletteFromGdalColorTable(rb.getRasterColorTable());
            }
            ++i;
        }
        this.fileTransparency.setTransparencyByPixelFromMetadata(this.metadata);
        try {
            this.trans = this.getGeoTransform();
            boolean isCorrect = false;
            int i2 = 0;
            while (i2 < this.trans.adfgeotransform.length) {
                if (this.trans.adfgeotransform[i2] != 0.0) {
                    isCorrect = true;
                }
                ++i2;
            }
            if (!isCorrect) {
                throw new GdalException("");
            }
            this.ownTransformation = new AffineTransform(this.trans.adfgeotransform[1], this.trans.adfgeotransform[4], this.trans.adfgeotransform[2], this.trans.adfgeotransform[5], this.trans.adfgeotransform[0], this.trans.adfgeotransform[3]);
            this.externalTransformation = (AffineTransform)this.ownTransformation.clone();
            this.currentFullWidth = this.width;
            this.currentFullHeight = this.height;
            this.georeferenced = true;
        }
        catch (GdalException exc) {
            this.ownTransformation = new AffineTransform(1.0f, 0.0f, 0.0f, -1.0f, 0.0f, this.height);
            this.externalTransformation = (AffineTransform)this.ownTransformation.clone();
            this.currentFullWidth = this.width;
            this.currentFullHeight = this.height;
            this.georeferenced = false;
        }
    }

    public boolean existsNoDataValue() throws GdalException {
        int i = 0;
        while (i < this.getRasterCount()) {
            GdalRasterBand rb = this.getRasterBand(i + 1);
            if (rb.existsNoDataValue()) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public boolean existsNoDataValue(int band) throws GdalException {
        GdalRasterBand rb = this.getRasterBand(band + 1);
        return rb.existsNoDataValue();
    }

    public double getNoDataValue() {
        if (this.metadata == null) {
            return RasterLibrary.defaultNoDataValue;
        }
        if (this.metadata.getNoDataValue().length == 0) {
            return RasterLibrary.defaultNoDataValue;
        }
        return this.metadata.getNoDataValue()[0];
    }

    public void setDataType(int[] dt) {
        this.dataType = dt;
    }

    public int[] getDataType() {
        return this.dataType;
    }

    public Point2D worldToRasterWithoutRot(Point2D pt) {
        Point2D.Double p = new Point2D.Double();
        AffineTransform at = new AffineTransform(this.externalTransformation.getScaleX(), 0.0, 0.0, this.externalTransformation.getScaleY(), this.externalTransformation.getTranslateX(), this.externalTransformation.getTranslateY());
        try {
            at.inverseTransform(pt, p);
        }
        catch (NoninvertibleTransformException e) {
            return pt;
        }
        return p;
    }

    public Point2D worldToRaster(Point2D pt) {
        Point2D.Double p = new Point2D.Double();
        try {
            this.externalTransformation.inverseTransform(pt, p);
        }
        catch (NoninvertibleTransformException e) {
            return pt;
        }
        return p;
    }

    public Point2D rasterToWorld(Point2D pt) {
        Point2D.Double p = new Point2D.Double();
        this.externalTransformation.transform(pt, p);
        return p;
    }

    private void calcOverview(Point2D tl, Point2D br) throws GdalException {
        this.gdalBands[0] = this.getRasterBand(1);
        this.currentOverview = -1;
        if (this.gdalBands[0].getOverviewCount() > 0) {
            GdalRasterBand ovb = null;
            int i = this.gdalBands[0].getOverviewCount() - 1;
            while (i > 0) {
                ovb = this.gdalBands[0].getOverview(i);
                if ((double)ovb.getRasterBandXSize() > (double)this.getRasterXSize() * this.viewportScaleX) {
                    this.currentOverview = i;
                    this.viewportScaleX *= (double)this.width / (double)ovb.getRasterBandXSize();
                    this.viewportScaleY *= (double)this.height / (double)ovb.getRasterBandYSize();
                    this.stepX = 1.0 / this.viewportScaleX;
                    this.stepY = 1.0 / this.viewportScaleY;
                    this.currentFullWidth = ovb.getRasterBandXSize();
                    this.currentFullHeight = ovb.getRasterBandYSize();
                    this.currentViewX = Math.min(tl.getX(), br.getX());
                    this.lastReadLine = Math.min(tl.getY(), br.getY());
                    break;
                }
                --i;
            }
        }
    }

    public void setView(double dWorldTLX, double dWorldTLY, double dWorldBRX, double dWorldBRY, int nWidth, int nHeight) throws GdalException {
        this.currentFullWidth = this.width;
        this.currentFullHeight = this.height;
        Point2D tl = this.worldToRaster(new Point2D.Double(dWorldTLX, dWorldTLY));
        Point2D br = this.worldToRaster(new Point2D.Double(dWorldBRX, dWorldBRY));
        this.currentViewWidth = nWidth;
        this.currentViewHeight = nHeight;
        this.currentViewX = Math.min(tl.getX(), br.getX());
        this.viewportScaleX = (double)this.currentViewWidth / (br.getX() - tl.getX());
        this.viewportScaleY = (double)this.currentViewHeight / (br.getY() - tl.getY());
        this.stepX = 1.0 / this.viewportScaleX;
        this.stepY = 1.0 / this.viewportScaleY;
        this.lastReadLine = Math.min(tl.getY(), br.getY());
        this.gdalBands = new GdalRasterBand[4];
        this.calcOverview(tl, br);
    }

    public void selectGdalBands(int nbands) throws GdalException {
        this.gdalBands = new GdalRasterBand[nbands];
        this.gdalBands[0] = this.getRasterBand(1);
        int i = 0;
        while (i < nbands) {
            this.gdalBands[i] = this.gdalBands[0];
            ++i;
        }
        this.assignDataTypeFromGdalRasterBands(this.gdalBands);
        i = 2;
        while (i <= nbands) {
            if (this.getRasterCount() >= i) {
                this.gdalBands[i - 1] = this.getRasterBand(i);
                int j = i;
                while (j < nbands) {
                    this.gdalBands[j] = this.gdalBands[i - 1];
                    ++j;
                }
            }
            ++i;
        }
        if (this.currentOverview > 0) {
            this.gdalBands[0] = this.gdalBands[0].getOverview(this.currentOverview);
            i = 2;
            while (i <= nbands) {
                if (this.getRasterCount() >= i) {
                    this.gdalBands[i - 1] = this.gdalBands[i - 1].getOverview(this.currentOverview);
                }
                ++i;
            }
        }
    }

    private void readLine(byte[][] line, double initOffset, GdalBuffer[] gdalBuffer) {
        double j = 0.0;
        int i = 0;
        int iBand = 0;
        while (iBand < gdalBuffer.length) {
            i = 0;
            j = initOffset;
            while (i < this.currentViewWidth && j < (double)gdalBuffer[0].getSize()) {
                line[iBand][i] = gdalBuffer[iBand].buffByte[(int)j];
                ++i;
                j += this.stepX;
            }
            ++iBand;
        }
    }

    private void readLine(short[][] line, double initOffset, GdalBuffer[] gdalBuffer) {
        double j = 0.0;
        int i = 0;
        int iBand = 0;
        while (iBand < gdalBuffer.length) {
            i = 0;
            j = initOffset;
            while (i < this.currentViewWidth && j < (double)gdalBuffer[0].getSize()) {
                line[iBand][i] = (short)(gdalBuffer[iBand].buffShort[(int)j] & 0xFFFF);
                ++i;
                j += this.stepX;
            }
            ++iBand;
        }
    }

    private void readLine(int[][] line, double initOffset, GdalBuffer[] gdalBuffer) {
        double j = 0.0;
        int i = 0;
        int iBand = 0;
        while (iBand < gdalBuffer.length) {
            i = 0;
            j = initOffset;
            while (i < this.currentViewWidth && j < (double)gdalBuffer[0].getSize()) {
                line[iBand][i] = gdalBuffer[iBand].buffInt[(int)j] & 0xFFFFFFFF;
                ++i;
                j += this.stepX;
            }
            ++iBand;
        }
    }

    private void readLine(float[][] line, double initOffset, GdalBuffer[] gdalBuffer) {
        double j = 0.0;
        int i = 0;
        int iBand = 0;
        while (iBand < gdalBuffer.length) {
            i = 0;
            j = initOffset;
            while (i < this.currentViewWidth && j < (double)gdalBuffer[0].getSize()) {
                line[iBand][i] = gdalBuffer[iBand].buffFloat[(int)j];
                ++i;
                j += this.stepX;
            }
            ++iBand;
        }
    }

    private void readLine(double[][] line, double initOffset, GdalBuffer[] gdalBuffer) {
        double j = 0.0;
        int i = 0;
        int iBand = 0;
        while (iBand < gdalBuffer.length) {
            i = 0;
            j = initOffset;
            while (i < this.currentViewWidth && j < (double)gdalBuffer[0].getSize()) {
                line[iBand][i] = gdalBuffer[iBand].buffDouble[(int)j];
                ++i;
                j += this.stepX;
            }
            ++iBand;
        }
    }

    public Object readCompleteLine(int nLine, int band) throws GdalException {
        GdalRasterBand gdalBand = super.getRasterBand(band + 1);
        GdalBuffer gdalBuf = null;
        gdalBuf = gdalBand.readRaster(0, nLine, this.getRasterXSize(), 1, this.getRasterXSize(), 1, this.dataType[band]);
        if (this.dataType[band] == GDT_Byte) {
            return gdalBuf.buffByte;
        }
        if (this.dataType[band] == GDT_Int16 || this.dataType[band] == GDT_UInt16) {
            return gdalBuf.buffShort;
        }
        if (this.dataType[band] == GDT_Int32 || this.dataType[band] == GDT_UInt32) {
            return gdalBuf.buffInt;
        }
        if (this.dataType[band] == GDT_Float32) {
            return gdalBuf.buffFloat;
        }
        if (this.dataType[band] == GDT_Float64) {
            return gdalBuf.buffDouble;
        }
        if (this.dataType[band] == GDT_CInt16 || this.dataType[band] == GDT_CInt32 || this.dataType[band] == GDT_CFloat32 || this.dataType[band] == GDT_CFloat64) {
            return null;
        }
        return null;
    }

    public Object readBlock(int pos, int blockHeight) throws GdalException, InterruptedException {
        this.bBandNr = super.getRasterCount();
        int nX = this.getRasterXSize();
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        GdalRasterBand[] gdalBand = new GdalRasterBand[this.bBandNr];
        int iBand = 0;
        while (iBand < gdalBand.length) {
            gdalBand[iBand] = super.getRasterBand(iBand + 1);
            ++iBand;
        }
        GdalBuffer[] gdalBuf = new GdalBuffer[this.bBandNr];
        if (this.dataType[0] == GDT_Byte) {
            byte[][][] buf = new byte[this.bBandNr][blockHeight][this.getRasterXSize()];
            int iBand2 = 0;
            while (iBand2 < gdalBuf.length) {
                gdalBuf[iBand2] = gdalBand[iBand2].readRaster(0, pos, nX, blockHeight, nX, blockHeight, this.dataType[0]);
                int iRow = 0;
                while (iRow < blockHeight) {
                    int iCol = 0;
                    while (iCol < nX) {
                        buf[iBand2][iRow][iCol] = gdalBuf[iBand2].buffByte[iRow * nX + iCol];
                        ++iCol;
                    }
                    if (task.getEvent() != null) {
                        task.manageEvent(task.getEvent());
                    }
                    ++iRow;
                }
                ++iBand2;
            }
            return buf;
        }
        if (this.dataType[0] == GDT_CInt16 || this.dataType[0] == GDT_Int16 || this.dataType[0] == GDT_UInt16) {
            short[][][] buf = new short[this.bBandNr][blockHeight][this.getRasterXSize()];
            int iBand3 = 0;
            while (iBand3 < gdalBuf.length) {
                gdalBuf[iBand3] = gdalBand[iBand3].readRaster(0, pos, nX, blockHeight, nX, blockHeight, this.dataType[0]);
                int iRow = 0;
                while (iRow < blockHeight) {
                    int iCol = 0;
                    while (iCol < nX) {
                        buf[iBand3][iRow][iCol] = gdalBuf[iBand3].buffShort[iRow * nX + iCol];
                        ++iCol;
                    }
                    if (task.getEvent() != null) {
                        task.manageEvent(task.getEvent());
                    }
                    ++iRow;
                }
                ++iBand3;
            }
            return buf;
        }
        if (this.dataType[0] == GDT_CInt32 || this.dataType[0] == GDT_Int32 || this.dataType[0] == GDT_UInt32) {
            int[][][] buf = new int[this.bBandNr][blockHeight][this.getRasterXSize()];
            int iBand4 = 0;
            while (iBand4 < gdalBuf.length) {
                gdalBuf[iBand4] = gdalBand[iBand4].readRaster(0, pos, nX, blockHeight, nX, blockHeight, this.dataType[0]);
                int iRow = 0;
                while (iRow < blockHeight) {
                    int iCol = 0;
                    while (iCol < nX) {
                        buf[iBand4][iRow][iCol] = gdalBuf[iBand4].buffInt[iRow * nX + iCol];
                        ++iCol;
                    }
                    if (task.getEvent() != null) {
                        task.manageEvent(task.getEvent());
                    }
                    ++iRow;
                }
                ++iBand4;
            }
            return buf;
        }
        if (this.dataType[0] == GDT_Float32 || this.dataType[0] == GDT_CFloat32) {
            float[][][] buf = new float[this.bBandNr][blockHeight][this.getRasterXSize()];
            int iBand5 = 0;
            while (iBand5 < gdalBuf.length) {
                gdalBuf[iBand5] = gdalBand[iBand5].readRaster(0, pos, nX, blockHeight, nX, blockHeight, this.dataType[0]);
                int iRow = 0;
                while (iRow < blockHeight) {
                    int iCol = 0;
                    while (iCol < nX) {
                        buf[iBand5][iRow][iCol] = gdalBuf[iBand5].buffFloat[iRow * nX + iCol];
                        ++iCol;
                    }
                    if (task.getEvent() != null) {
                        task.manageEvent(task.getEvent());
                    }
                    ++iRow;
                }
                ++iBand5;
            }
            return buf;
        }
        if (this.dataType[0] == GDT_Float64 || this.dataType[0] == GDT_CFloat64) {
            double[][][] buf = new double[this.bBandNr][blockHeight][this.getRasterXSize()];
            int iBand6 = 0;
            while (iBand6 < gdalBuf.length) {
                gdalBuf[iBand6] = gdalBand[iBand6].readRaster(0, pos, nX, blockHeight, nX, blockHeight, this.dataType[0]);
                int iRow = 0;
                while (iRow < blockHeight) {
                    int iCol = 0;
                    while (iCol < nX) {
                        buf[iBand6][iRow][iCol] = gdalBuf[iBand6].buffDouble[iRow * nX + iCol];
                        ++iCol;
                    }
                    if (task.getEvent() != null) {
                        task.manageEvent(task.getEvent());
                    }
                    ++iRow;
                }
                ++iBand6;
            }
            return buf;
        }
        return null;
    }

    public void readLine(Object line) throws GdalException {
        int w = (int)(Math.ceil((double)this.currentViewWidth * this.stepX) + 1.0);
        int x = (int)this.currentViewX;
        int y = (int)this.lastReadLine;
        GdalBuffer r = null;
        GdalBuffer g = null;
        GdalBuffer b = null;
        GdalBuffer a = new GdalBuffer();
        while (y >= this.gdalBands[0].getRasterBandYSize()) {
            --y;
        }
        if (x + w > this.gdalBands[0].getRasterBandXSize()) {
            w = this.gdalBands[0].getRasterBandXSize() - x;
        }
        if (this.gdalBands[0].getRasterColorTable() != null) {
            this.palette = new ColorTable();
            this.palette.createPaletteFromGdalColorTable(this.gdalBands[0].getRasterColorTable());
            r = this.gdalBands[0].readRaster(x, y, w, 1, w, 1, this.dataType[0]);
        } else {
            a.buffByte = new byte[w];
            g = b = (r = this.gdalBands[0].readRaster(x, y, w, 1, w, 1, this.dataType[0]));
            if (this.getRasterCount() > 1 && this.gdalBands[1] != null) {
                g = this.gdalBands[1].readRaster(x, y, w, 1, w, 1, this.dataType[0]);
            }
            if (this.getRasterCount() > 2 && this.gdalBands[2] != null) {
                b = this.gdalBands[2].readRaster(x, y, w, 1, w, 1, this.dataType[0]);
            }
        }
        this.lastReadLine += this.stepY;
        double initOffset = Math.abs(this.currentViewX - (double)((int)this.currentViewX));
        GdalBuffer[] bands = new GdalBuffer[]{r, g, b};
        if (this.dataType[0] == GDT_Byte) {
            this.readLine((byte[][])line, initOffset, bands);
        } else if (this.dataType[0] == GDT_CInt16 || this.dataType[0] == GDT_Int16 || this.dataType[0] == GDT_UInt16) {
            this.readLine((short[][])line, initOffset, bands);
        } else if (this.dataType[0] == GDT_CInt32 || this.dataType[0] == GDT_Int32 || this.dataType[0] == GDT_UInt32) {
            this.readLine((int[][])line, initOffset, bands);
        } else if (this.dataType[0] == GDT_Float32 || this.dataType[0] == GDT_CFloat32) {
            this.readLine((float[][])line, initOffset, bands);
        } else if (this.dataType[0] == GDT_Float64 || this.dataType[0] == GDT_CFloat64) {
            this.readLine((double[][])line, initOffset, bands);
        }
    }

    private int[] calcStepBuffer(Extent dataExtent, int nWidth, int nHeight, int[] stpBuffer) {
        Extent imageExtent = this.getExtentWithoutRot();
        Extent ajustDataExtent = RasterUtilities.calculateAdjustedView((Extent)dataExtent, (Extent)imageExtent);
        if (!RasterUtilities.compareExtents((Extent)dataExtent, (Extent)ajustDataExtent)) {
            Point2D p1 = this.worldToRasterWithoutRot(new Point2D.Double(ajustDataExtent.minX(), ajustDataExtent.maxY()));
            Point2D p2 = this.worldToRasterWithoutRot(new Point2D.Double(ajustDataExtent.maxX(), ajustDataExtent.minY()));
            Point2D p3 = this.worldToRasterWithoutRot(new Point2D.Double(dataExtent.minX(), dataExtent.maxY()));
            int w = (int)Math.abs(Math.ceil(p2.getX()) - Math.floor(p1.getX()));
            int h = (int)Math.abs(Math.floor(p1.getY()) - Math.ceil(p2.getY()));
            stpBuffer[0] = (int)(p1.getX() + -p3.getX());
            stpBuffer[1] = (int)(p1.getY() + -p3.getY());
            stpBuffer[2] = stpBuffer[0] + w;
            stpBuffer[3] = stpBuffer[1] + h;
            return new int[]{w, h};
        }
        return new int[]{nWidth, nHeight};
    }

    public void readWindow(IBuffer buf, BandList bandList, double ulx, double uly, double lrx, double lry, int nWidth, int nHeight, boolean adjustToExtent) throws GdalException, InterruptedException {
        Extent petExtent = new Extent(ulx, uly, lrx, lry);
        this.setView(ulx, uly, lrx, lry, nWidth, nHeight);
        Point2D tl = this.worldToRaster(new Point2D.Double(ulx, uly));
        Point2D br = this.worldToRaster(new Point2D.Double(lrx, lry));
        br.setLocation(br.getX() - 1.0, br.getY() - 1.0);
        if (this.gdalBands.length == 0) {
            return;
        }
        this.selectGdalBands(buf.getBandCount());
        int x = (int)Math.round(Math.min(tl.getX(), br.getX()));
        int y = (int)Math.round(Math.min(tl.getY(), br.getY()));
        int[] nArray = new int[4];
        nArray[2] = buf.getWidth();
        nArray[3] = buf.getHeight();
        int[] stpBuffer = nArray;
        if (!adjustToExtent) {
            int[] wh = this.calcStepBuffer(petExtent, nWidth, nHeight, stpBuffer);
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            this.readData(buf, bandList, x, y, wh[0], wh[1], wh[0], wh[1], 0, 0, stpBuffer);
            return;
        }
        this.readData(buf, bandList, x, y, nWidth, nHeight, nWidth, nHeight, 0, 0, stpBuffer);
    }

    public void readWindow(IBuffer buf, BandList bandList, double ulx, double uly, double lrx, double lry, double nWidth, double nHeight, int bufWidth, int bufHeight, boolean adjustToExtent) throws GdalException, InterruptedException {
        Extent petExtent = new Extent(ulx, uly, lrx, lry);
        this.setView(ulx, uly, lrx, lry, bufWidth, bufHeight);
        Point2D tl = this.worldToRaster(new Point2D.Double(ulx, uly));
        Point2D br = this.worldToRaster(new Point2D.Double(lrx, lry));
        br.setLocation(br.getX() - 1.0, br.getY() - 1.0);
        this.adjustPoints(tl, br);
        if (this.gdalBands.length == 0) {
            return;
        }
        this.selectGdalBands(buf.getBandCount());
        int x = (int)Math.min(tl.getX(), br.getX());
        int y = (int)Math.min(tl.getY(), br.getY());
        int stpX = 0;
        int stpY = 0;
        nWidth = nWidth * (double)this.currentFullWidth / (double)this.width;
        nHeight = nHeight * (double)this.currentFullHeight / (double)this.height;
        x = x * this.currentFullWidth / this.width;
        y = y * this.currentFullHeight / this.height;
        int[] nArray = new int[4];
        nArray[2] = buf.getWidth();
        nArray[3] = buf.getHeight();
        int[] stpBuffer = nArray;
        if (!adjustToExtent) {
            int[] wh = this.calcStepBuffer(petExtent, bufWidth, bufHeight, stpBuffer);
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            stpBuffer[0] = (int)((double)(stpBuffer[0] * bufWidth) / nWidth);
            stpBuffer[1] = (int)((double)(stpBuffer[1] * bufHeight) / nHeight);
            stpBuffer[2] = (int)((double)(stpBuffer[2] * bufWidth) / nWidth);
            stpBuffer[3] = (int)((double)(stpBuffer[3] * bufHeight) / nHeight);
            bufWidth = Math.abs(stpBuffer[2] - stpBuffer[0]);
            bufHeight = Math.abs(stpBuffer[3] - stpBuffer[1]);
            this.readData(buf, bandList, x, y, wh[0], wh[1], bufWidth, bufHeight, 0, 0, stpBuffer);
            return;
        }
        if ((double)x + nWidth > (double)this.gdalBands[0].getRasterBandXSize()) {
            nWidth = this.gdalBands[0].getRasterBandXSize() - x;
        }
        if ((double)y + nHeight > (double)this.gdalBands[0].getRasterBandYSize()) {
            nHeight = this.gdalBands[0].getRasterBandYSize() - y;
        }
        this.readData(buf, bandList, x, y, (int)nWidth, (int)nHeight, bufWidth, bufHeight, stpX, stpY, stpBuffer);
    }

    private void adjustPoints(Point2D ul, Point2D lr) {
        double a = ul.getX() - (double)((int)ul.getX());
        double b = ul.getY() - (double)((int)ul.getY());
        ul.setLocation(a > 0.95 || a < 0.05 ? (double)Math.round(ul.getX()) : ul.getX(), b > 0.95 || b < 0.05 ? (double)Math.round(ul.getY()) : ul.getY());
        lr.setLocation(a > 0.95 || a < 0.05 ? (double)Math.round(lr.getX()) : lr.getX(), b > 0.95 || b < 0.05 ? (double)Math.round(lr.getY()) : lr.getY());
    }

    public void readWindow(IBuffer buf, BandList bandList, int x, int y, int w, int h) throws GdalException, InterruptedException {
        this.gdalBands = new GdalRasterBand[this.getRasterCount()];
        this.isSupersampling = false;
        if (this.gdalBands.length == 0) {
            return;
        }
        this.gdalBands[0] = this.getRasterBand(1);
        int iBand = 1;
        while (iBand < this.gdalBands.length) {
            this.gdalBands[iBand] = this.getRasterBand(iBand + 1);
            ++iBand;
        }
        this.assignDataTypeFromGdalRasterBands(this.gdalBands);
        int yMax = y + h;
        this.readDataByLine(buf, bandList, x, y, w, yMax);
    }

    public void readWindow(IBuffer buf, BandList bandList, int x, int y, int w, int h, int bufWidth, int bufHeight) throws GdalException, InterruptedException {
        this.gdalBands = new GdalRasterBand[this.getRasterCount()];
        if (this.gdalBands.length == 0) {
            return;
        }
        this.gdalBands[0] = this.getRasterBand(1);
        int iBand = 1;
        while (iBand < this.gdalBands.length) {
            this.gdalBands[iBand] = this.getRasterBand(iBand + 1);
            ++iBand;
        }
        this.assignDataTypeFromGdalRasterBands(this.gdalBands);
        int[] nArray = new int[4];
        nArray[2] = buf.getWidth();
        nArray[3] = buf.getHeight();
        int[] stpBuffer = nArray;
        this.readData(buf, bandList, x, y, w, h, bufWidth, bufHeight, 0, 0, stpBuffer);
    }

    private void assignDataTypeFromGdalRasterBands(GdalRasterBand[] gdalBands) throws GdalException {
        int[] dt = new int[gdalBands.length];
        int i = 0;
        while (i < gdalBands.length) {
            if (gdalBands[i] != null) {
                dt[i] = gdalBands[i].getRasterDataType();
                if (dt[i] == GDT_Byte) {
                    System.out.println("*** DEBUG: changed band " + i + " to type GDT_Int16");
                    dt[i] = GDT_Int16;
                }
            }
            ++i;
        }
        this.setDataType(dt);
    }

    private void readData(IBuffer buf, BandList bandList, int x, int y, int w, int h, int bufWidth, int bufHeight, int stpX, int stpY, int[] stepBuffer) throws GdalException, InterruptedException {
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        GdalBuffer gdalBuf = null;
        int iBand = 0;
        while (iBand < this.gdalBands.length) {
            int[] drawableBands = bandList.getBufferBandToDraw(this.fileName, iBand);
            if (drawableBands != null && (drawableBands.length != 1 || drawableBands[0] != -1)) {
                int col;
                int line;
                int init;
                int pos = init = bufWidth * stpY + stpX;
                gdalBuf = this.gdalBands[iBand].readRaster(x, y, w, h, bufWidth, bufHeight, this.dataType[iBand]);
                if (this.dataType[iBand] == Gdal.GDT_Byte) {
                    line = stepBuffer[1];
                    while (line < stepBuffer[3]) {
                        pos = bufWidth * (line - stepBuffer[0]) + init;
                        col = stepBuffer[0];
                        while (col < stepBuffer[2]) {
                            buf.setElem(line, col, iBand, gdalBuf.buffByte[pos]);
                            ++pos;
                            ++col;
                        }
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                } else if (this.dataType[iBand] == Gdal.GDT_UInt16 || this.dataType[iBand] == Gdal.GDT_Int16 || this.dataType[iBand] == Gdal.GDT_CInt16) {
                    line = stepBuffer[1];
                    while (line < stepBuffer[3]) {
                        pos = bufWidth * (line - stepBuffer[0]) + init;
                        col = stepBuffer[0];
                        while (col < stepBuffer[2]) {
                            buf.setElem(line, col, iBand, gdalBuf.buffShort[pos]);
                            ++pos;
                            ++col;
                        }
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                } else if (this.dataType[iBand] == Gdal.GDT_UInt32 || this.dataType[iBand] == Gdal.GDT_Int32 || this.dataType[iBand] == Gdal.GDT_CInt32) {
                    line = stepBuffer[1];
                    while (line < stepBuffer[3]) {
                        pos = bufWidth * (line - stepBuffer[0]) + init;
                        col = stepBuffer[0];
                        while (col < stepBuffer[2]) {
                            buf.setElem(line, col, iBand, gdalBuf.buffInt[pos]);
                            ++pos;
                            ++col;
                        }
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                } else if (this.dataType[iBand] == Gdal.GDT_Float32) {
                    line = stepBuffer[1];
                    while (line < stepBuffer[3]) {
                        pos = bufWidth * (line - stepBuffer[0]) + init;
                        col = stepBuffer[0];
                        while (col < stepBuffer[2]) {
                            buf.setElem(line, col, iBand, gdalBuf.buffFloat[pos]);
                            ++pos;
                            ++col;
                        }
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                } else if (this.dataType[iBand] == Gdal.GDT_Float64) {
                    line = stepBuffer[1];
                    while (line < stepBuffer[3]) {
                        pos = bufWidth * (line - stepBuffer[0]) + init;
                        col = stepBuffer[0];
                        while (col < stepBuffer[2]) {
                            buf.setElem(line, col, iBand, gdalBuf.buffDouble[pos]);
                            ++pos;
                            ++col;
                        }
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                }
            }
            ++iBand;
        }
    }

    private void readDataByLine(IBuffer buf, BandList bandList, int x, int y, int w, int yMax) throws GdalException, InterruptedException {
        GdalBuffer gdalBuf = null;
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        int iBand = 0;
        while (iBand < this.gdalBands.length) {
            int[] drawableBands = bandList.getBufferBandToDraw(this.fileName, iBand);
            if (drawableBands != null && (drawableBands.length != 1 || drawableBands[0] != -1)) {
                int rasterBufLine;
                int line;
                if (this.dataType[iBand] == Gdal.GDT_Byte) {
                    line = y;
                    while (line < yMax) {
                        gdalBuf = this.gdalBands[iBand].readRaster(x, line, w, 1, w, 1, this.dataType[iBand]);
                        rasterBufLine = line - y;
                        buf.setLineInBandByte(gdalBuf.buffByte, rasterBufLine, iBand);
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                } else if (this.dataType[iBand] == Gdal.GDT_UInt16 || this.dataType[iBand] == Gdal.GDT_Int16 || this.dataType[iBand] == Gdal.GDT_CInt16) {
                    line = y;
                    while (line < yMax) {
                        gdalBuf = this.gdalBands[iBand].readRaster(x, line, w, 1, w, 1, this.dataType[iBand]);
                        rasterBufLine = line - y;
                        buf.setLineInBandShort(gdalBuf.buffShort, rasterBufLine, iBand);
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                } else if (this.dataType[iBand] == Gdal.GDT_UInt32 || this.dataType[iBand] == Gdal.GDT_Int32 || this.dataType[iBand] == Gdal.GDT_CInt32) {
                    line = y;
                    while (line < yMax) {
                        gdalBuf = this.gdalBands[iBand].readRaster(x, line, w, 1, w, 1, this.dataType[iBand]);
                        rasterBufLine = line - y;
                        buf.setLineInBandInt(gdalBuf.buffInt, rasterBufLine, iBand);
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                } else if (this.dataType[iBand] == Gdal.GDT_Float32) {
                    line = y;
                    while (line < yMax) {
                        gdalBuf = this.gdalBands[iBand].readRaster(x, line, w, 1, w, 1, this.dataType[iBand]);
                        rasterBufLine = line - y;
                        buf.setLineInBandFloat(gdalBuf.buffFloat, rasterBufLine, iBand);
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                } else if (this.dataType[iBand] == Gdal.GDT_Float64) {
                    line = y;
                    while (line < yMax) {
                        gdalBuf = this.gdalBands[iBand].readRaster(x, line, w, 1, w, 1, this.dataType[iBand]);
                        rasterBufLine = line - y;
                        buf.setLineInBandDouble(gdalBuf.buffDouble, rasterBufLine, iBand);
                        if (task.getEvent() != null) {
                            task.manageEvent(task.getEvent());
                        }
                        ++line;
                    }
                }
            }
            ++iBand;
        }
    }

    public Object[] getData(int x, int y) {
        try {
            Object[] data = new Object[this.getRasterCount()];
            int i = 0;
            while (i < this.getRasterCount()) {
                GdalRasterBand rb = this.getRasterBand(i + 1);
                GdalBuffer r = rb.readRaster(x, y, 1, 1, 1, 1, this.dataType[i]);
                switch (this.dataType[i]) {
                    case 0: {
                        break;
                    }
                    case 1: {
                        data[i] = new Integer(r.buffByte[0]);
                        break;
                    }
                    case 2: 
                    case 3: {
                        data[i] = new Integer(r.buffShort[0]);
                        break;
                    }
                    case 4: 
                    case 5: {
                        data[i] = new Integer(r.buffInt[0]);
                        break;
                    }
                    case 6: {
                        data[i] = new Float(r.buffFloat[0]);
                        break;
                    }
                    case 7: {
                        data[i] = new Double(r.buffDouble[0]);
                    }
                }
                ++i;
            }
            return data;
        }
        catch (GdalException e) {
            return null;
        }
    }

    public int getBlockSize() {
        return this.getBlockSize();
    }

    public AffineTransform getOwnTransformation() {
        return this.ownTransformation;
    }

    public Extent getExtentWithoutRot() {
        AffineTransform at = new AffineTransform(this.externalTransformation.getScaleX(), 0.0, 0.0, this.externalTransformation.getScaleY(), this.externalTransformation.getTranslateX(), this.externalTransformation.getTranslateY());
        Point2D.Double p1 = new Point2D.Double(0.0, 0.0);
        Point2D.Double p2 = new Point2D.Double(this.width, this.height);
        at.transform(p1, p1);
        at.transform(p2, p2);
        return new Extent((Point2D)p1, (Point2D)p2);
    }

    public void setExternalTransform(AffineTransform t) {
        this.externalTransformation = t;
    }

    public String getGdalShortName() {
        return this.shortName;
    }
}

