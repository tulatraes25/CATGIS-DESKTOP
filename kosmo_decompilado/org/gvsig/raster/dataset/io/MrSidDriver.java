/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  es.gva.cit.jmrsid.MrSIDException
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.gvsig.raster.dataset.BandAccessException
 *  org.gvsig.raster.dataset.BandList
 *  org.gvsig.raster.dataset.FileNotOpenException
 *  org.gvsig.raster.dataset.GeoInfo
 *  org.gvsig.raster.dataset.IBuffer
 *  org.gvsig.raster.dataset.InvalidSetViewException
 *  org.gvsig.raster.dataset.RasterDataset
 *  org.gvsig.raster.dataset.io.MrSidNative
 *  org.gvsig.raster.dataset.io.RasterDriverException
 *  org.gvsig.raster.dataset.io.rmf.ParsingException
 *  org.gvsig.raster.dataset.properties.DatasetColorInterpretation
 *  org.gvsig.raster.datastruct.Extent
 *  org.gvsig.raster.datastruct.Transparency
 *  org.gvsig.raster.process.RasterTask
 *  org.gvsig.raster.process.RasterTaskQueue
 *  org.gvsig.raster.util.extensionPoints.ExtensionPoint
 */
package org.gvsig.raster.dataset.io;

import es.gva.cit.jmrsid.MrSIDException;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.raster.dataset.BandAccessException;
import org.gvsig.raster.dataset.BandList;
import org.gvsig.raster.dataset.FileNotOpenException;
import org.gvsig.raster.dataset.GeoInfo;
import org.gvsig.raster.dataset.IBuffer;
import org.gvsig.raster.dataset.InvalidSetViewException;
import org.gvsig.raster.dataset.RasterDataset;
import org.gvsig.raster.dataset.io.MrSidNative;
import org.gvsig.raster.dataset.io.RasterDriverException;
import org.gvsig.raster.dataset.io.rmf.ParsingException;
import org.gvsig.raster.dataset.properties.DatasetColorInterpretation;
import org.gvsig.raster.datastruct.Extent;
import org.gvsig.raster.datastruct.Transparency;
import org.gvsig.raster.process.RasterTask;
import org.gvsig.raster.process.RasterTaskQueue;
import org.gvsig.raster.util.extensionPoints.ExtensionPoint;

public class MrSidDriver
extends RasterDataset {
    protected MrSidNative file = null;
    private Extent viewRequest = null;
    private DatasetColorInterpretation colorInterpr = null;
    protected Transparency fileTransparency = null;

    public static void register() {
        ExtensionPoint point = ExtensionPoint.getExtensionPoint((String)"RasterReader");
        point.register("sid", MrSidDriver.class);
    }

    public MrSidDriver(IProjection proj, Object param) {
        super(proj, (Object)((String)param));
        this.setParam(param);
        try {
            this.file = new MrSidNative((String)param);
            this.load();
            this.bandCount = this.file.nbands;
            int[] dt = new int[this.bandCount];
            int i = 0;
            while (i < dt.length) {
                dt[i] = 0;
                ++i;
            }
            this.setDataType(dt);
            super.init();
            try {
                this.loadFromRmf(this.getRmfBlocksManager());
            }
            catch (ParsingException parsingException) {}
        }
        catch (Exception e) {
            System.out.println("Error en constructor de MrSID");
            e.printStackTrace();
            this.file = null;
        }
    }

    public GeoInfo load() {
        this.ownTransformation = this.file.getOwnTransformation();
        this.externalTransformation = (AffineTransform)this.ownTransformation.clone();
        return this;
    }

    public void close() {
        if (this.file != null) {
            this.file.close();
            this.file = null;
        }
    }

    public void setView(Extent e) {
        this.viewRequest = new Extent(e);
    }

    public Extent getView() {
        return this.viewRequest;
    }

    public int getWidth() {
        return this.file.width;
    }

    public int getHeight() {
        return this.file.height;
    }

    public void reProject(ICoordTrans rp) {
    }

    protected void setRGBLine(BufferedImage image, int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
        image.setRGB(startX, startY, w, h, rgbArray, offset, scansize);
    }

    public Object getData(int x, int y, int band) throws InvalidSetViewException, FileNotOpenException, RasterDriverException {
        if (this.file != null) {
            if (x < 0 || y < 0 || x >= this.file.width || y >= this.file.height) {
                throw new InvalidSetViewException("Request out of grid");
            }
            Object[] data = this.file.getData(x, y);
            return data[band];
        }
        throw new FileNotOpenException("MrSIDNative not exist");
    }

    public int getBlockSize() {
        return this.file.blocksize;
    }

    public boolean isSupersampling() {
        return this.file.isSupersampling;
    }

    public IBuffer getWindowRaster(double ulx, double uly, double lrx, double lry, BandList bandList, IBuffer rasterBuf) throws InterruptedException, RasterDriverException {
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        int width = rasterBuf.getWidth();
        int height = rasterBuf.getHeight();
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        this.setView(new Extent(ulx, uly, lrx, lry));
        this.file.setView(this.viewRequest.getULX(), this.viewRequest.getULY(), this.viewRequest.getLRX(), this.viewRequest.getLRY(), width, height);
        int[] pRGBArray = new int[width * height];
        try {
            this.file.readScene(pRGBArray, task);
            int wBuf = rasterBuf.getWidth();
            int bandCount = bandList.getBandCount();
            int row = 0;
            while (row < rasterBuf.getHeight()) {
                int col = 0;
                while (col < wBuf) {
                    if (bandCount == 1) {
                        rasterBuf.setElem(row, col, 0, (byte)(pRGBArray[row * wBuf + col] & 0xFF));
                    } else {
                        rasterBuf.setElem(row, col, 0, (byte)((pRGBArray[row * wBuf + col] & 0xFF0000) >> 16));
                        rasterBuf.setElem(row, col, 1, (byte)((pRGBArray[row * wBuf + col] & 0xFF00) >> 8));
                        rasterBuf.setElem(row, col, 2, (byte)(pRGBArray[row * wBuf + col] & 0xFF));
                    }
                    ++col;
                }
                if (task.getEvent() != null) {
                    task.manageEvent(task.getEvent());
                }
                ++row;
            }
        }
        catch (MrSIDException e) {
            throw new RasterDriverException("Error reading data");
        }
        return rasterBuf;
    }

    public IBuffer getWindowRaster(double ulx, double uly, double w, double h, BandList bandList, IBuffer rasterBuf, boolean adjustToExtent) throws InterruptedException, RasterDriverException {
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        Extent ext = this.getExtent();
        Point2D pInit = this.rasterToWorld(new Point2D.Double(0.0, 0.0));
        Point2D pEnd = this.rasterToWorld(new Point2D.Double(this.getWidth(), this.getHeight()));
        double wRaster = Math.abs(pEnd.getX() - pInit.getX());
        double hRaster = Math.abs(pEnd.getY() - pInit.getY());
        double lrx = ext.getULX() - wRaster > ext.maxX() || ext.getULX() - wRaster < ext.minX() ? ulx + w : ulx - w;
        double lry = ext.getULY() - hRaster > ext.maxY() || ext.getULY() - hRaster < ext.minY() ? uly + h : uly - h;
        int width = rasterBuf.getWidth();
        int height = rasterBuf.getHeight();
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        this.setView(new Extent(ulx, uly, lrx, lry));
        this.file.setView(this.viewRequest.minX(), this.viewRequest.maxY(), this.viewRequest.maxX(), this.viewRequest.minY(), width, height);
        int[] pRGBArray = new int[width * height];
        try {
            this.file.readScene(pRGBArray, task);
            int wBuf = rasterBuf.getWidth();
            int bandCount = bandList.getBandCount();
            int row = 0;
            while (row < rasterBuf.getHeight()) {
                int col = 0;
                while (col < wBuf) {
                    if (bandCount == 1) {
                        rasterBuf.setElem(row, col, 0, (byte)(pRGBArray[row * wBuf + col] & 0xFF));
                    } else {
                        rasterBuf.setElem(row, col, 0, (byte)((pRGBArray[row * wBuf + col] & 0xFF0000) >> 16));
                        rasterBuf.setElem(row, col, 1, (byte)((pRGBArray[row * wBuf + col] & 0xFF00) >> 8));
                        rasterBuf.setElem(row, col, 2, (byte)(pRGBArray[row * wBuf + col] & 0xFF));
                    }
                    ++col;
                }
                if (task.getEvent() != null) {
                    task.manageEvent(task.getEvent());
                }
                ++row;
            }
        }
        catch (MrSIDException e) {
            throw new RasterDriverException("Error reading data");
        }
        return rasterBuf;
    }

    public IBuffer getWindowRaster(double ulx, double uly, double lrx, double lry, int bufWidth, int bufHeight, BandList bandList, IBuffer rasterBuf, boolean adjustToExtent) throws InterruptedException, RasterDriverException {
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        if (bufWidth <= 0) {
            bufWidth = 1;
        }
        if (bufHeight <= 0) {
            bufHeight = 1;
        }
        this.setView(new Extent(ulx, uly, lrx, lry));
        this.file.setView(this.viewRequest.getULX(), this.viewRequest.getULY(), this.viewRequest.getLRX(), this.viewRequest.getLRY(), bufWidth, bufHeight);
        int[] pRGBArray = new int[bufWidth * bufHeight];
        try {
            int col;
            int row;
            this.file.readScene(pRGBArray, task);
            int w = rasterBuf.getWidth();
            if (this.getBandCount() >= 3) {
                row = 0;
                while (row < rasterBuf.getHeight()) {
                    col = 0;
                    while (col < w) {
                        rasterBuf.setElem(row, col, 0, (byte)((pRGBArray[row * w + col] & 0xFF0000) >> 16));
                        rasterBuf.setElem(row, col, 1, (byte)((pRGBArray[row * w + col] & 0xFF00) >> 8));
                        rasterBuf.setElem(row, col, 2, (byte)(pRGBArray[row * w + col] & 0xFF));
                        ++col;
                    }
                    if (task.getEvent() != null) {
                        task.manageEvent(task.getEvent());
                    }
                    ++row;
                }
            }
            if (this.getBandCount() == 1) {
                row = 0;
                while (row < rasterBuf.getHeight()) {
                    col = 0;
                    while (col < w) {
                        rasterBuf.setElem(row, col, 0, (byte)(pRGBArray[row * w + col] & 0xFF));
                        ++col;
                    }
                    if (task.getEvent() != null) {
                        task.manageEvent(task.getEvent());
                    }
                    ++row;
                }
            }
        }
        catch (MrSIDException e) {
            throw new RasterDriverException("Error reading data");
        }
        return rasterBuf;
    }

    public IBuffer getWindowRaster(int x, int y, int w, int h, int bufWidth, int bufHeight, BandList bandList, IBuffer rasterBuf) throws InterruptedException, RasterDriverException {
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        if (bufWidth <= 0) {
            bufWidth = 1;
        }
        if (bufHeight <= 0) {
            bufHeight = 1;
        }
        Point2D begin = this.rasterToWorld(new Point2D.Double(x, y));
        Point2D end = this.rasterToWorld(new Point2D.Double(x + w, y + h));
        this.file.setView(begin.getX(), begin.getY(), end.getX(), end.getY(), bufWidth, bufHeight);
        int[] pRGBArray = new int[bufWidth * bufHeight];
        try {
            this.file.readScene(pRGBArray, task);
            int bandCount = bandList.getBandCount();
            int row = 0;
            while (row < bufHeight) {
                int col = 0;
                while (col < bufWidth) {
                    if (bandCount == 1) {
                        rasterBuf.setElem(row, col, 0, (byte)(pRGBArray[row * bufWidth + col] & 0xFF));
                    } else {
                        rasterBuf.setElem(row, col, 0, (byte)((pRGBArray[row * bufWidth + col] & 0xFF0000) >> 16));
                        rasterBuf.setElem(row, col, 1, (byte)((pRGBArray[row * bufWidth + col] & 0xFF00) >> 8));
                        rasterBuf.setElem(row, col, 2, (byte)(pRGBArray[row * bufWidth + col] & 0xFF));
                    }
                    ++col;
                }
                if (task.getEvent() != null) {
                    task.manageEvent(task.getEvent());
                }
                ++row;
            }
        }
        catch (MrSIDException e) {
            throw new RasterDriverException("Error reading data");
        }
        return rasterBuf;
    }

    public Object readBlock(int pos, int blockHeight) throws InvalidSetViewException, FileNotOpenException, RasterDriverException, InterruptedException {
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        if (pos < 0) {
            throw new InvalidSetViewException("Request out of grid");
        }
        if (pos + blockHeight > this.file.height) {
            blockHeight = Math.abs(this.file.height - pos);
        }
        Point2D begin = this.rasterToWorld(new Point2D.Double(0.0, pos));
        Point2D end = this.rasterToWorld(new Point2D.Double(this.file.width, pos + blockHeight));
        int w = this.file.width;
        this.file.setView(begin.getX(), begin.getY(), end.getX(), end.getY(), w, blockHeight);
        int[] pRGBArray = new int[this.file.width * blockHeight];
        try {
            this.file.readScene(pRGBArray, task);
            byte[][][] buf = new byte[3][blockHeight][w];
            int row = 0;
            while (row < blockHeight) {
                int col = 0;
                while (col < w) {
                    if (this.bandCount == 1) {
                        buf[0][row][col] = (byte)(pRGBArray[row * w + col] & 0xFF);
                    } else {
                        buf[0][row][col] = (byte)((pRGBArray[row * w + col] & 0xFF0000) >> 16);
                        buf[1][row][col] = (byte)((pRGBArray[row * w + col] & 0xFF00) >> 8);
                        buf[2][row][col] = (byte)(pRGBArray[row * w + col] & 0xFF);
                    }
                    ++col;
                }
                if (task.getEvent() != null) {
                    task.manageEvent(task.getEvent());
                }
                ++row;
            }
            return buf;
        }
        catch (MrSIDException e) {
            throw new RasterDriverException("Error reading data");
        }
    }

    public Object readCompleteLine(int line, int band) throws InvalidSetViewException, FileNotOpenException, RasterDriverException {
        RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
        if (line > this.getHeight() || band > this.getBandCount()) {
            throw new InvalidSetViewException("Request out of grid");
        }
        try {
            Extent extent = this.getExtent();
            Point2D pt = this.rasterToWorld(new Point2D.Double(extent.minX(), line));
            this.file.setView(extent.minX(), pt.getY(), extent.maxX(), pt.getY(), this.getWidth(), 1);
            int[] pRGBArray = new int[this.getWidth()];
            this.file.readScene(pRGBArray, task);
            return pRGBArray;
        }
        catch (MrSIDException e) {
            throw new RasterDriverException("Error reading data from MrSID library");
        }
        catch (InterruptedException interruptedException) {
            return null;
        }
    }

    public IBuffer getWindowRaster(int x, int y, int w, int h, BandList bandList, IBuffer rasterBuf) throws InterruptedException, RasterDriverException {
        try {
            this.file.readWindow(rasterBuf, bandList, x, y, w, h);
        }
        catch (MrSIDException e) {
            throw new RasterDriverException("Error reading data");
        }
        return rasterBuf;
    }

    public Transparency getTransparencyDatasetStatus() {
        if (this.fileTransparency == null) {
            this.fileTransparency = new Transparency();
        }
        return this.fileTransparency;
    }

    public DatasetColorInterpretation getColorInterpretation() {
        if (this.colorInterpr == null) {
            this.colorInterpr = new DatasetColorInterpretation();
            this.colorInterpr.initColorInterpretation(this.getBandCount());
            if (this.getBandCount() == 1) {
                this.colorInterpr.setColorInterpValue(0, "Gray");
            }
            if (this.getBandCount() >= 3) {
                this.colorInterpr.setColorInterpValue(0, "Red");
                this.colorInterpr.setColorInterpValue(1, "Green");
                this.colorInterpr.setColorInterpValue(2, "Blue");
            }
        }
        return this.colorInterpr;
    }

    public void setColorInterpretation(DatasetColorInterpretation colorInterpretation) {
        this.colorInterpretation = colorInterpretation;
    }

    public String getWktProjection() {
        return null;
    }

    public void setAffineTransform(AffineTransform t) {
        super.setAffineTransform(t);
        this.file.setExternalTransform(t);
    }

    public int getOverviewCount(int band) throws BandAccessException, RasterDriverException {
        if (band >= this.getBandCount()) {
            throw new BandAccessException("Wrong band");
        }
        try {
            return this.file.getNumLevels();
        }
        catch (MrSIDException e) {
            throw new RasterDriverException("");
        }
    }

    public int getOverviewWidth(int band, int overview) throws BandAccessException, RasterDriverException {
        if (band >= this.getBandCount()) {
            throw new BandAccessException("Wrong band");
        }
        return 0;
    }

    public int getOverviewHeight(int band, int overview) throws BandAccessException, RasterDriverException {
        if (band >= this.getBandCount()) {
            throw new BandAccessException("Wrong band");
        }
        return 0;
    }

    public boolean overviewsSupport() {
        return false;
    }
}

