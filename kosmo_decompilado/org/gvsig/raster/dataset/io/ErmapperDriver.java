/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ermapper.ecw.JNCSException
 *  com.ermapper.ecw.JNCSFile
 *  com.ermapper.ecw.JNCSFileNotOpenException
 *  com.ermapper.ecw.JNCSInvalidSetViewException
 *  com.ermapper.ecw.JNCSProgressiveUpdate
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.gvsig.raster.dataset.BandAccessException
 *  org.gvsig.raster.dataset.BandList
 *  org.gvsig.raster.dataset.FileNotOpenException
 *  org.gvsig.raster.dataset.GeoInfo
 *  org.gvsig.raster.dataset.IBuffer
 *  org.gvsig.raster.dataset.InvalidSetViewException
 *  org.gvsig.raster.dataset.NotSupportedExtensionException
 *  org.gvsig.raster.dataset.RasterDataset
 *  org.gvsig.raster.dataset.io.RasterDriverException
 *  org.gvsig.raster.dataset.io.rmf.ParsingException
 *  org.gvsig.raster.dataset.properties.DatasetColorInterpretation
 *  org.gvsig.raster.datastruct.Extent
 *  org.gvsig.raster.datastruct.Transparency
 *  org.gvsig.raster.process.RasterTask
 *  org.gvsig.raster.process.RasterTaskQueue
 *  org.gvsig.raster.util.RasterUtilities
 *  org.gvsig.raster.util.extensionPoints.ExtensionPoint
 */
package org.gvsig.raster.dataset.io;

import com.ermapper.ecw.JNCSException;
import com.ermapper.ecw.JNCSFile;
import com.ermapper.ecw.JNCSFileNotOpenException;
import com.ermapper.ecw.JNCSInvalidSetViewException;
import com.ermapper.ecw.JNCSProgressiveUpdate;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.Vector;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.raster.dataset.BandAccessException;
import org.gvsig.raster.dataset.BandList;
import org.gvsig.raster.dataset.FileNotOpenException;
import org.gvsig.raster.dataset.GeoInfo;
import org.gvsig.raster.dataset.IBuffer;
import org.gvsig.raster.dataset.InvalidSetViewException;
import org.gvsig.raster.dataset.NotSupportedExtensionException;
import org.gvsig.raster.dataset.RasterDataset;
import org.gvsig.raster.dataset.io.RasterDriverException;
import org.gvsig.raster.dataset.io.rmf.ParsingException;
import org.gvsig.raster.dataset.properties.DatasetColorInterpretation;
import org.gvsig.raster.datastruct.Extent;
import org.gvsig.raster.datastruct.Transparency;
import org.gvsig.raster.process.RasterTask;
import org.gvsig.raster.process.RasterTaskQueue;
import org.gvsig.raster.util.RasterUtilities;
import org.gvsig.raster.util.extensionPoints.ExtensionPoint;

public class ErmapperDriver
extends RasterDataset
implements JNCSProgressiveUpdate {
    private JNCSFile file = null;
    protected Transparency fileTransparency = null;
    private Extent viewRequest = null;
    private DatasetColorInterpretation colorInterpr = null;

    public static void register() {
        ExtensionPoint point = ExtensionPoint.getExtensionPoint((String)"RasterReader");
        point.register("ecw", ErmapperDriver.class);
        point.register("jp2", ErmapperDriver.class);
    }

    public ErmapperDriver(IProjection proj, Object param) throws NotSupportedExtensionException {
        super(proj, (Object)((String)param));
        this.setParam(param);
        try {
            if (!new File((String)param).exists() && !((String)param).startsWith("ecwp:")) {
                throw new NotSupportedExtensionException("Extension not supported");
            }
            this.file = new JNCSFile((String)param, false);
            this.load();
            this.bandCount = this.file.numBands;
            this.getTransparencyDatasetStatus();
            int[] dt = new int[this.bandCount];
            int i = 0;
            while (i < this.bandCount) {
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
            throw new NotSupportedExtensionException("Extension not supported");
        }
    }

    public GeoInfo load() {
        this.ownTransformation = new AffineTransform(this.file.cellIncrementX, 0.0, 0.0, this.file.cellIncrementY, this.file.originX, this.file.originY);
        this.externalTransformation = (AffineTransform)this.ownTransformation.clone();
        return this;
    }

    public void close() {
        if (this.file != null) {
            this.file.close(true);
            this.file = null;
        }
    }

    public Transparency getTransparencyDatasetStatus() {
        if (this.fileTransparency == null) {
            this.fileTransparency = new Transparency();
        }
        return this.fileTransparency;
    }

    public int getWidth() {
        return this.file.width;
    }

    public int getHeight() {
        return this.file.height;
    }

    public Extent getView() {
        return this.viewRequest;
    }

    public void setView(Extent e) {
        this.viewRequest = new Extent(e);
    }

    public IBuffer getWindowRaster(double ulx, double uly, double lrx, double lry, BandList bandList, IBuffer rasterBuf) throws InterruptedException, RasterDriverException {
        Point2D.Double p1 = new Point2D.Double(ulx, uly);
        Point2D.Double p2 = new Point2D.Double(lrx, lry);
        try {
            this.externalTransformation.inverseTransform(p1, p1);
            this.externalTransformation.inverseTransform(p2, p2);
            this.ownTransformation.transform(p1, p1);
            this.ownTransformation.transform(p2, p2);
        }
        catch (NoninvertibleTransformException e) {
            throw new RasterDriverException("Noninvertible transform");
        }
        Extent selectedExtent = new Extent(((Point2D)p1).getX(), ((Point2D)p1).getY(), ((Point2D)p2).getX(), ((Point2D)p2).getY());
        this.setView(selectedExtent);
        int wPx = rasterBuf.getWidth();
        int hPx = rasterBuf.getHeight();
        int[] nArray = new int[4];
        nArray[2] = wPx;
        nArray[3] = hPx;
        int[] stpBuffer = nArray;
        this.loadBuffer(this.viewRequest, wPx, hPx, rasterBuf, bandList, stpBuffer);
        return rasterBuf;
    }

    public IBuffer getWindowRaster(double ulx, double uly, double w, double h, BandList bandList, IBuffer rasterBuf, boolean adjustToExtent) throws InterruptedException, RasterDriverException {
        Extent ext = this.getExtent();
        Point2D pInit = this.rasterToWorld(new Point2D.Double(0.0, 0.0));
        Point2D pEnd = this.rasterToWorld(new Point2D.Double(this.getWidth(), this.getHeight()));
        double wRaster = Math.abs(pEnd.getX() - pInit.getX());
        double hRaster = Math.abs(pEnd.getY() - pInit.getY());
        double lrx = ext.getULX() - wRaster > ext.maxX() || ext.getULX() - wRaster < ext.minX() ? ulx + w : ulx - w;
        double lry = ext.getULY() - hRaster > ext.maxY() || ext.getULY() - hRaster < ext.minY() ? uly + h : uly - h;
        Point2D.Double p1 = new Point2D.Double(ulx, uly);
        Point2D.Double p2 = new Point2D.Double(lrx, lry);
        try {
            this.externalTransformation.inverseTransform(p1, p1);
            this.externalTransformation.inverseTransform(p2, p2);
            this.ownTransformation.transform(p1, p1);
            this.ownTransformation.transform(p2, p2);
        }
        catch (NoninvertibleTransformException e) {
            throw new RasterDriverException("Noninvertible transform");
        }
        Extent selectedExtent = new Extent(((Point2D)p1).getX(), ((Point2D)p1).getY(), ((Point2D)p2).getX(), ((Point2D)p2).getY());
        this.setView(selectedExtent);
        int wPx = rasterBuf.getWidth();
        int hPx = rasterBuf.getHeight();
        int[] nArray = new int[4];
        nArray[2] = wPx;
        nArray[3] = hPx;
        int[] stpBuffer = nArray;
        this.loadBuffer(this.viewRequest, wPx, hPx, rasterBuf, bandList, stpBuffer);
        return rasterBuf;
    }

    public IBuffer getWindowRaster(int x, int y, int w, int h, BandList bandList, IBuffer rasterBuf) throws InterruptedException, RasterDriverException {
        Point2D init = this.rasterToWorld(new Point2D.Double(x, y));
        Point2D end = this.rasterToWorld(new Point2D.Double(x + w, y + h));
        Extent selectedExtent = new Extent(init.getX(), init.getY(), end.getX(), end.getY());
        this.setView(selectedExtent);
        int[] nArray = new int[4];
        nArray[2] = w;
        nArray[3] = h;
        int[] stpBuffer = nArray;
        this.loadBuffer(this.viewRequest, w, h, rasterBuf, bandList, stpBuffer);
        return rasterBuf;
    }

    public IBuffer getWindowRaster(double ulx, double uly, double lrx, double lry, int bufWidth, int bufHeight, BandList bandList, IBuffer rasterBuf, boolean adjustToExtent) throws InterruptedException, RasterDriverException {
        Point2D.Double p1 = new Point2D.Double(ulx, uly);
        Point2D.Double p2 = new Point2D.Double(lrx, lry);
        try {
            this.externalTransformation.inverseTransform(p1, p1);
            this.externalTransformation.inverseTransform(p2, p2);
            this.ownTransformation.transform(p1, p1);
            this.ownTransformation.transform(p2, p2);
        }
        catch (NoninvertibleTransformException e) {
            throw new RasterDriverException("Noninvertible transform");
        }
        Extent selectedExtent = new Extent((Point2D)p1, (Point2D)p2);
        this.setView(selectedExtent);
        int[] nArray = new int[4];
        nArray[2] = bufWidth;
        nArray[3] = bufHeight;
        int[] stpBuffer = nArray;
        this.loadBuffer(this.viewRequest, bufWidth, bufHeight, rasterBuf, bandList, stpBuffer);
        return rasterBuf;
    }

    public IBuffer getWindowRaster(int x, int y, int w, int h, int bufWidth, int bufHeight, BandList bandList, IBuffer rasterBuf) throws InterruptedException, RasterDriverException {
        Point2D init = this.rasterToWorld(new Point2D.Double(x, y));
        Point2D end = this.rasterToWorld(new Point2D.Double(x + w, y + h));
        Extent selectedExtent = new Extent(init.getX(), init.getY(), end.getX(), end.getY());
        this.setView(selectedExtent);
        int[] nArray = new int[4];
        nArray[2] = bufWidth;
        nArray[3] = bufHeight;
        int[] stpBuffer = nArray;
        this.loadBuffer(this.viewRequest, bufWidth, bufHeight, rasterBuf, bandList, stpBuffer);
        return rasterBuf;
    }

    private void loadBuffer(Extent selectedExtent, int bufWidth, int bufHeight, IBuffer rasterBuf, BandList bandList, int[] stpBuffer) throws InterruptedException, RasterDriverException {
        try {
            RasterTask task = RasterTaskQueue.get((String)Thread.currentThread().toString());
            int[] readBandsFromECW = new int[Math.max(this.file.numBands, 3)];
            int[] readBands = new int[Math.max(this.file.numBands, 3)];
            int i = 0;
            while (i < readBandsFromECW.length) {
                readBands[i] = -1;
                ++i;
            }
            int cont = 0;
            int i2 = 0;
            while (i2 < this.file.numBands) {
                int[] bandsToDraw = bandList.getBand(i2).getBufferBandListToDraw();
                if (bandsToDraw != null) {
                    int j = 0;
                    while (j < bandsToDraw.length) {
                        readBandsFromECW[cont] = i2;
                        readBands[cont] = i2;
                        ++cont;
                        ++j;
                    }
                }
                ++i2;
            }
            if (task.getEvent() != null) {
                task.manageEvent(task.getEvent());
            }
            if ((double)bufWidth > Math.abs(selectedExtent.width() / this.file.cellIncrementX)) {
                bufWidth = (int)Math.round(Math.abs(selectedExtent.width() / this.file.cellIncrementX));
            }
            if ((double)bufHeight > Math.abs(selectedExtent.height() / this.file.cellIncrementY)) {
                bufHeight = (int)Math.round(Math.abs(selectedExtent.height() / this.file.cellIncrementY));
            }
            this.file.setView(this.file.numBands, readBandsFromECW, selectedExtent.minX(), selectedExtent.maxY(), selectedExtent.maxX(), selectedExtent.minY(), bufWidth, bufHeight);
            int[] pRGBArray = new int[bufWidth];
            this.drawRGB(rasterBuf, pRGBArray, readBandsFromECW, bandList, task);
        }
        catch (JNCSInvalidSetViewException exc) {
            throw new RasterDriverException("Error setting coords", (Exception)((Object)exc));
        }
        catch (JNCSFileNotOpenException exc) {
            throw new RasterDriverException("Error opening file", (Exception)((Object)exc));
        }
        catch (JNCSException exc) {
            throw new RasterDriverException("Error reading data", (Exception)((Object)exc));
        }
    }

    private void drawRGB(IBuffer rasterBuf, int[] pRGBArray, int[] readBands, BandList bandList, RasterTask task) throws JNCSException, InterruptedException {
        int line;
        int[] b;
        int i;
        int bandB;
        int bandR = readBands[0];
        int bandG = readBands.length > 1 ? readBands[1] : -1;
        int n = bandB = readBands.length > 2 ? readBands[2] : -1;
        if (this.file.numBands == 3 && bandList.getDrawableBandsCount() < 3) {
            i = 0;
            while (i < 3) {
                b = bandList.getBand(i).getBufferBandListToDraw();
                if (b != null) {
                    bandG = 1;
                    bandR = 0;
                    bandB = 2;
                }
                ++i;
            }
        }
        if (this.file.numBands == 3 && bandR == bandG && bandG == bandB) {
            i = 0;
            while (i < 3) {
                b = bandList.getBand(i).getBufferBandListToDraw();
                if (b != null) {
                    if (i == 0) {
                        int line2 = 0;
                        while (line2 < rasterBuf.getHeight()) {
                            try {
                                this.file.readLineRGBA(pRGBArray);
                                int col = 0;
                                while (col < pRGBArray.length) {
                                    rasterBuf.setElem(line2, col, bandR, (byte)((pRGBArray[col] & 0xFF0000) >> 16));
                                    rasterBuf.setElem(line2, col, bandG, (byte)((pRGBArray[col] & 0xFF0000) >> 16));
                                    rasterBuf.setElem(line2, col, bandB, (byte)((pRGBArray[col] & 0xFF0000) >> 16));
                                    ++col;
                                }
                            }
                            catch (JNCSException exc) {
                                System.err.println("Error: fuera de rango en la petici\u00f3n de l\u00ednea");
                            }
                            ++line2;
                        }
                        return;
                    }
                    if (i == 1) {
                        int line3 = 0;
                        while (line3 < rasterBuf.getHeight()) {
                            try {
                                this.file.readLineRGBA(pRGBArray);
                                int col = 0;
                                while (col < pRGBArray.length) {
                                    rasterBuf.setElem(line3, col, bandR, (byte)((pRGBArray[col] & 0xFF00) >> 8));
                                    rasterBuf.setElem(line3, col, bandG, (byte)((pRGBArray[col] & 0xFF00) >> 8));
                                    rasterBuf.setElem(line3, col, bandB, (byte)((pRGBArray[col] & 0xFF00) >> 8));
                                    ++col;
                                }
                            }
                            catch (JNCSException exc) {
                                System.err.println("Error: fuera de rango en la petici\u00f3n de l\u00ednea");
                            }
                            ++line3;
                        }
                        return;
                    }
                    if (i == 2) {
                        int line4 = 0;
                        while (line4 < rasterBuf.getHeight()) {
                            try {
                                this.file.readLineRGBA(pRGBArray);
                                int col = 0;
                                while (col < pRGBArray.length) {
                                    rasterBuf.setElem(line4, col, bandR, (byte)(pRGBArray[col] & 0xFF));
                                    rasterBuf.setElem(line4, col, bandG, (byte)(pRGBArray[col] & 0xFF));
                                    rasterBuf.setElem(line4, col, bandB, (byte)(pRGBArray[col] & 0xFF));
                                    ++col;
                                }
                            }
                            catch (JNCSException exc) {
                                System.err.println("Error: fuera de rango en la petici\u00f3n de l\u00ednea");
                            }
                            ++line4;
                        }
                        return;
                    }
                }
                if (task.getEvent() != null) {
                    task.manageEvent(task.getEvent());
                }
                ++i;
            }
        }
        if (bandR >= 0 && bandG >= 0 && bandB >= 0) {
            line = 0;
            while (line < rasterBuf.getHeight()) {
                try {
                    this.file.readLineRGBA(pRGBArray);
                    int col = 0;
                    while (col < pRGBArray.length) {
                        rasterBuf.setElem(line, col, bandR, (byte)((pRGBArray[col] & 0xFF0000) >> 16));
                        rasterBuf.setElem(line, col, bandG, (byte)((pRGBArray[col] & 0xFF00) >> 8));
                        rasterBuf.setElem(line, col, bandB, (byte)(pRGBArray[col] & 0xFF));
                        ++col;
                    }
                }
                catch (JNCSException col) {
                    // empty catch block
                }
                ++line;
            }
            return;
        }
        if (task.getEvent() != null) {
            task.manageEvent(task.getEvent());
        }
        if (bandR >= 0 && bandG >= 0) {
            line = 0;
            while (line < rasterBuf.getHeight()) {
                try {
                    this.file.readLineRGBA(pRGBArray);
                    int col = 0;
                    while (col < pRGBArray.length) {
                        rasterBuf.setElem(line, col, bandR, (byte)((pRGBArray[col] & 0xFF0000) >> 16));
                        rasterBuf.setElem(line, col, bandG, (byte)((pRGBArray[col] & 0xFF00) >> 8));
                        ++col;
                    }
                }
                catch (JNCSException col) {
                    // empty catch block
                }
                ++line;
            }
            return;
        }
        if (task.getEvent() != null) {
            task.manageEvent(task.getEvent());
        }
        if (bandR >= 0) {
            line = 0;
            while (line < rasterBuf.getHeight()) {
                try {
                    this.file.readLineRGBA(pRGBArray);
                    int col = 0;
                    while (col < pRGBArray.length) {
                        rasterBuf.setElem(line, col, bandR, (byte)((pRGBArray[col] & 0xFF0000) >> 16));
                        ++col;
                    }
                }
                catch (JNCSException jNCSException) {
                    // empty catch block
                }
                ++line;
            }
            return;
        }
        if (task.getEvent() != null) {
            task.manageEvent(task.getEvent());
        }
    }

    public void reProject(ICoordTrans rp) {
    }

    public int getBlockSize() {
        return 0;
    }

    public Object readCompleteLine(int line, int band) throws InvalidSetViewException, FileNotOpenException, RasterDriverException {
        block24: {
            if (line < 0 || line >= this.file.height || band < 0 || band >= this.getBandCount()) {
                throw new InvalidSetViewException("Request out of grid");
            }
            Point2D begin = this.rasterToWorld(new Point2D.Double(0.0, line));
            Point2D end = this.rasterToWorld(new Point2D.Double(this.file.width, line + 1));
            int[] readBandsFromECW = new int[this.file.numBands];
            if (this.file.numBands <= 3) {
                int i = 0;
                while (i < this.file.numBands) {
                    readBandsFromECW[i] = i;
                    ++i;
                }
            } else {
                readBandsFromECW[0] = band;
            }
            Extent e = new Extent(begin.getX(), begin.getY(), end.getX(), end.getY());
            try {
                int[] value = new int[this.file.width];
                this.file.setView(this.file.numBands, readBandsFromECW, e.minX(), e.maxY(), e.maxX(), e.minY(), this.file.width, 1);
                this.file.readLineRGBA(value);
                if (this.file.numBands <= 3) {
                    switch (this.getDataType()[0]) {
                        case 0: {
                            byte[] b = new byte[this.file.width];
                            switch (band) {
                                case 0: {
                                    int i = 0;
                                    while (i < this.file.width) {
                                        b[i] = (byte)((value[i] & 0xFF0000) >> 16 & 0xFF);
                                        ++i;
                                    }
                                    break;
                                }
                                case 1: {
                                    int i = 0;
                                    while (i < this.file.width) {
                                        b[i] = (byte)((value[i] & 0xFF00) >> 8 & 0xFF);
                                        ++i;
                                    }
                                    break;
                                }
                                case 2: {
                                    int i = 0;
                                    while (i < this.file.width) {
                                        b[i] = (byte)(value[i] & 0xFF & 0xFF);
                                        ++i;
                                    }
                                    break;
                                }
                            }
                            return b;
                        }
                    }
                    break block24;
                }
                switch (this.getDataType()[0]) {
                    case 0: {
                        byte[] b = new byte[this.file.width];
                        int i = 0;
                        while (i < this.file.width) {
                            b[i] = (byte)((value[i] & 0xFF0000) >> 16 & 0xFF);
                            ++i;
                        }
                        break;
                    }
                }
            }
            catch (JNCSFileNotOpenException e1) {
                throw new FileNotOpenException("Error en jecw: JNCSFileNotOpenException");
            }
            catch (JNCSInvalidSetViewException e1) {
                throw new FileNotOpenException("Error en jecw: JNCSInvalidSetViewException");
            }
            catch (JNCSException e1) {
                throw new RasterDriverException("Error la lectura de datos ecw");
            }
        }
        return null;
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
        int[] readBandsFromECW = new int[this.file.numBands];
        int i = 0;
        while (i < this.file.numBands) {
            readBandsFromECW[i] = i;
            ++i;
        }
        byte[][][] buf = new byte[this.file.numBands][blockHeight][this.file.width];
        Extent e = new Extent(begin.getX(), begin.getY(), end.getX(), end.getY());
        e = RasterUtilities.calculateAdjustedView((Extent)this.getExtent(), (Extent)e);
        try {
            int[] value = new int[this.file.width];
            this.file.setView(this.file.numBands, readBandsFromECW, e.minX(), e.maxY(), e.maxX(), e.minY(), this.file.width, blockHeight);
            if (this.file.numBands <= 3) {
                int row = 0;
                while (row < blockHeight) {
                    this.file.readLineRGBA(value);
                    switch (this.getDataType()[0]) {
                        case 0: {
                            int col = 0;
                            while (col < this.file.width) {
                                buf[0][row][col] = (byte)((value[col] & 0xFF0000) >> 16 & 0xFF);
                                buf[1][row][col] = (byte)((value[col] & 0xFF00) >> 8 & 0xFF);
                                buf[2][row][col] = (byte)(value[col] & 0xFF & 0xFF);
                                ++col;
                            }
                            break;
                        }
                    }
                    ++row;
                }
                if (task.getEvent() != null) {
                    task.manageEvent(task.getEvent());
                }
            }
        }
        catch (JNCSFileNotOpenException e1) {
            throw new FileNotOpenException("Error en jecw: JNCSFileNotOpenException");
        }
        catch (JNCSInvalidSetViewException e1) {
            throw new FileNotOpenException("Error en jecw: JNCSInvalidSetViewException");
        }
        catch (JNCSException e1) {
            throw new RasterDriverException("Error la lectura de datos ecw");
        }
        return buf;
    }

    public Object getData(int x, int y, int band) throws InvalidSetViewException, FileNotOpenException, RasterDriverException {
        if (x < 0 || y < 0 || x >= this.file.width || y >= this.file.height) {
            throw new InvalidSetViewException("Request out of grid");
        }
        Point2D.Double begin = new Point2D.Double(x, y);
        Point2D.Double end = new Point2D.Double(x + 1, y + 1);
        this.ownTransformation.transform(begin, begin);
        this.ownTransformation.transform(end, end);
        int[] readBandsFromECW = new int[this.file.numBands];
        if (this.file.numBands <= 3) {
            int i = 0;
            while (i < this.file.numBands) {
                readBandsFromECW[i] = i;
                ++i;
            }
        } else {
            readBandsFromECW[0] = band;
        }
        Extent e = new Extent(((Point2D)begin).getX(), ((Point2D)begin).getY(), ((Point2D)end).getX(), ((Point2D)end).getY());
        try {
            int[] value = new int[1];
            this.file.setView(this.file.numBands, readBandsFromECW, e.minX(), e.maxY(), e.maxX(), e.minY(), 1, 1);
            this.file.readLineRGBA(value);
            if (this.file.numBands <= 3) {
                switch (band) {
                    case 0: {
                        return new Integer((value[0] & 0xFF0000) >> 16 & 0xFFFFFFFF);
                    }
                    case 1: {
                        return new Integer((value[0] & 0xFF00) >> 8 & 0xFFFFFFFF);
                    }
                    case 2: {
                        return new Integer(value[0] & 0xFF & 0xFFFFFFFF);
                    }
                }
            }
            return new Integer((value[0] & 0xFF0000) >> 16 & 0xFFFFFFFF);
        }
        catch (JNCSFileNotOpenException e1) {
            throw new FileNotOpenException("Error en jecw: JNCSFileNotOpenException");
        }
        catch (JNCSInvalidSetViewException e1) {
            throw new FileNotOpenException("Error en jecw: JNCSInvalidSetViewException");
        }
        catch (JNCSException e1) {
            throw new RasterDriverException("Error reading ecw data");
        }
    }

    public void refreshUpdate(int arg0, int arg1, double arg2, double arg3, double arg4, double arg5) {
    }

    public void refreshUpdate(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
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

    public int getOverviewCount(int band) throws BandAccessException, RasterDriverException {
        if (band >= this.getBandCount()) {
            throw new BandAccessException("Wrong band");
        }
        return 0;
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

    public String getStringProjection() throws RasterDriverException {
        return this.file.projection;
    }

    public String getWktProjection() {
        return null;
    }

    class Contour
    extends Vector {
        private static final long serialVersionUID = 0L;
        public double minX = Double.MAX_VALUE;
        public double minY = Double.MAX_VALUE;
        public double maxX = -1.7976931348623157E308;
        public double maxY = -1.7976931348623157E308;

        public void add(Point2D pt) {
            super.add(pt);
            if (pt.getX() > this.maxX) {
                this.maxX = pt.getX();
            }
            if (pt.getX() < this.minX) {
                this.minX = pt.getX();
            }
            if (pt.getY() > this.maxY) {
                this.maxY = pt.getY();
            }
            if (pt.getY() < this.minY) {
                this.minY = pt.getY();
            }
        }
    }
}

