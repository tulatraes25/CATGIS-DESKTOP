/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.filter.RasterFilterStack
 *  org.cresques.filter.RasterFilterStackManager
 *  org.cresques.filter.bands.PaletteFilter
 *  org.cresques.filter.bands.PaletteStackManager
 *  org.cresques.filter.bands.RasterByteToImageFilter
 *  org.cresques.filter.bands.RasterDoubleToImageFilter
 *  org.cresques.filter.bands.RasterFloatToImageFilter
 *  org.cresques.filter.bands.RasterIntToImageFilter
 *  org.cresques.filter.bands.RasterShortToImageFilter
 *  org.cresques.filter.enhancement.TransparencyRange
 *  org.cresques.geo.Projected
 *  org.cresques.geo.ViewPortData
 *  org.cresques.io.EcwFile
 *  org.cresques.io.GdalFile
 *  org.cresques.io.GeoRasterFile
 *  org.cresques.io.MemoryRasterDriver
 *  org.cresques.io.data.RasterBuf
 *  org.cresques.io.datastruct.Statistic
 *  org.cresques.io.exceptions.SupersamplingNotSupportedException
 *  org.cresques.px.Extent
 *  org.cresques.px.PxObj
 */
package org.cresques.px;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.filter.RasterFilterStack;
import org.cresques.filter.RasterFilterStackManager;
import org.cresques.filter.bands.PaletteFilter;
import org.cresques.filter.bands.PaletteStackManager;
import org.cresques.filter.bands.RasterByteToImageFilter;
import org.cresques.filter.bands.RasterDoubleToImageFilter;
import org.cresques.filter.bands.RasterFloatToImageFilter;
import org.cresques.filter.bands.RasterIntToImageFilter;
import org.cresques.filter.bands.RasterShortToImageFilter;
import org.cresques.filter.enhancement.TransparencyRange;
import org.cresques.geo.Projected;
import org.cresques.geo.ViewPortData;
import org.cresques.io.EcwFile;
import org.cresques.io.GdalFile;
import org.cresques.io.GeoRasterFile;
import org.cresques.io.MemoryRasterDriver;
import org.cresques.io.data.RasterBuf;
import org.cresques.io.datastruct.Statistic;
import org.cresques.io.exceptions.SupersamplingNotSupportedException;
import org.cresques.px.Extent;
import org.cresques.px.PxObj;
import org.saig.jump.lang.I18N;

public class PxRaster
extends PxObj
implements Projected {
    private static final Logger LOGGER = Logger.getLogger(PxRaster.class);
    protected GeoRasterFile[] geoFile = null;
    protected ImageObserver component = null;
    protected Vector<Point2D> pts = null;
    protected int rBand = 1;
    protected int gBand = 2;
    protected int bBand = 3;
    public Statistic stats = new Statistic();
    int transparente = 285212544;
    String vName = null;
    protected boolean pintaMarco = false;
    IProjection proj = null;
    protected Extent extentOrig = null;
    ICoordTrans rp = null;
    public RasterFilterStack filterStack = new RasterFilterStack(this.stats);
    private BandSwitch bandSwitch = new BandSwitch();
    private boolean firstPxRaster = true;
    private final double percentFilterInit = 0.02;
    private RasterFilterStackManager stackManager = null;
    private Image geoImage = null;
    private ViewPortData lastViewPort = null;
    private double[] adjustedRotedExtent = null;
    public boolean service = false;

    public PxRaster(ImageObserver component) {
        this.component = component;
    }

    public PxRaster(IProjection proj, String fname, ImageObserver component) {
        this.geoFile = new GeoRasterFile[1];
        this.geoFile[0] = GeoRasterFile.openFile((IProjection)proj, (String)fname);
        this.geoFile[0].setUpdatable((Component)component);
        this.proj = proj;
        this.component = component;
        this.setExtent(this.geoFile[0].getExtent());
        this.setExtentForRequest(this.geoFile[0].getExtentForRequest());
        this.geoFile[0].setView(this.geoFile[0].getExtent());
        this.extentOrig = this.extent;
        this.bandSwitch.addFile(this.geoFile[0]);
        if (this.geoFile[0].getBandCount() >= 3) {
            this.setBand(1, 0);
            this.setBand(2, 1);
            this.setBand(4, 2);
        } else if (this.geoFile[0].getBandCount() == 2) {
            this.setBand(1, 0);
            this.setBand(2, 1);
            this.setBand(4, 1);
        } else if (this.geoFile[0].getBandCount() == 1) {
            this.setBand(1, 0);
            this.setBand(2, 0);
            this.setBand(4, 0);
        }
    }

    public PxRaster(IProjection proj, String[] fnames, ImageObserver component) {
        this.proj = proj;
        this.component = component;
        this.geoFile = new GeoRasterFile[fnames.length];
        int i = 0;
        while (i < this.geoFile.length) {
            this.geoFile[i] = GeoRasterFile.openFile((IProjection)proj, (String)fnames[i]);
            this.geoFile[i].setUpdatable((Component)component);
            this.setExtent(this.geoFile[i].getExtent());
            this.setExtentForRequest(this.geoFile[i].getExtentForRequest());
            this.geoFile[i].setView(this.geoFile[i].getExtent());
            this.bandSwitch.addFile(this.geoFile[i]);
            ++i;
        }
        this.extentOrig = this.extent;
        if (fnames.length >= 3 || this.geoFile[0].getBandCount() > 2) {
            this.setBand(1, 0);
            this.setBand(2, 1);
            this.setBand(4, 2);
        } else {
            this.setBand(7, 0);
        }
    }

    public PxRaster(GeoRasterFile eFile, ImageObserver component, Extent view) {
        this.geoFile = new GeoRasterFile[1];
        this.geoFile[0] = eFile;
        this.geoFile[0].setUpdatable((Component)component);
        this.setProjection(this.geoFile[0].getProjection());
        this.component = component;
        this.setExtent(this.geoFile[0].getExtent());
        this.setExtentForRequest(this.geoFile[0].getExtentForRequest());
        if (view != null) {
            this.geoFile[0].setView(view);
            this.extentOrig = this.extent;
            this.bandSwitch.addFile(eFile);
            if (this.geoFile[0].getBandCount() >= 3) {
                this.setBand(1, 0);
                this.setBand(2, 1);
                this.setBand(4, 2);
            } else if (this.geoFile[0].getBandCount() == 2) {
                this.setBand(1, 0);
                this.setBand(2, 1);
                this.setBand(4, 1);
            } else if (this.geoFile[0].getBandCount() == 1) {
                this.setBand(1, 0);
                this.setBand(2, 0);
                this.setBand(4, 0);
            }
        }
    }

    public GeoRasterFile addFile(String fileName) {
        if (this.geoFile != null) {
            GeoRasterFile[] listFiles = new GeoRasterFile[this.geoFile.length + 1];
            int i = 0;
            while (i < this.geoFile.length) {
                listFiles[i] = this.geoFile[i];
                ++i;
            }
            listFiles[this.geoFile.length] = GeoRasterFile.openFile((IProjection)this.proj, (String)fileName);
            listFiles[this.geoFile.length].setUpdatable((Component)this.component);
            this.setExtent(listFiles[this.geoFile.length].getExtent());
            this.setExtentForRequest(listFiles[this.geoFile.length].getExtentForRequest());
            listFiles[this.geoFile.length].setView(listFiles[this.geoFile.length].getExtent());
            this.bandSwitch.addFile(listFiles[this.geoFile.length]);
            this.geoFile = listFiles;
            return listFiles[this.geoFile.length - 1];
        }
        LOGGER.error((Object)("PxRaster.addFile(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded")));
        return null;
    }

    public Extent testExtentFile(String fileName) {
        GeoRasterFile grf = GeoRasterFile.openFile((IProjection)this.proj, (String)fileName);
        return grf.getExtent();
    }

    public int[] getPixel(double wcx, double wcy) {
        if (this.geoImage != null) {
            int ptox = 0;
            int ptoy = 0;
            try {
                Extent extOrtofoto = this.geoFile[0].getExtent();
                double minx = 0.0;
                double miny = 0.0;
                double maxx = 0.0;
                double maxy = 0.0;
                minx = this.lastViewPort.getExtent().getMin().getX() < extOrtofoto.minX() ? extOrtofoto.minX() : this.lastViewPort.getExtent().getMin().getX();
                maxx = this.lastViewPort.getExtent().getMax().getX() > extOrtofoto.maxX() ? extOrtofoto.maxX() : this.lastViewPort.getExtent().getMax().getX();
                miny = this.lastViewPort.getExtent().getMin().getY() < extOrtofoto.minY() ? extOrtofoto.minY() : this.lastViewPort.getExtent().getMin().getY();
                maxy = this.lastViewPort.getExtent().getMax().getY() > extOrtofoto.maxY() ? extOrtofoto.maxY() : this.lastViewPort.getExtent().getMax().getY();
                if (wcx < minx || wcx > maxx || wcy < miny || wcy > maxy) {
                    int[] res = new int[]{-1, -1, -1, -1};
                    return res;
                }
                int w = ((BufferedImage)this.geoImage).getWidth();
                int h = ((BufferedImage)this.geoImage).getHeight();
                double wcw = maxx - minx;
                double wch = maxy - miny;
                ptox = (int)((wcx - minx) * (double)w / wcw);
                ptoy = (int)((wcy - miny) * (double)h / wch);
                int px = ((BufferedImage)this.geoImage).getRGB(ptox, h - ptoy);
                int[] values = new int[]{(px & 0xFF000000) >> 24, (px & 0xFF0000) >> 16, (px & 0xFF00) >> 8, px & 0xFF};
                return values;
            }
            catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public GeoRasterFile delFile(String fileName) {
        if (this.geoFile != null) {
            Vector<GeoRasterFile> grfTemp = new Vector<GeoRasterFile>();
            GeoRasterFile grfDelete = null;
            int i = 0;
            while (i < this.geoFile.length) {
                if (!fileName.endsWith(this.geoFile[i].getName())) {
                    grfTemp.add(this.geoFile[i]);
                } else {
                    grfDelete = this.geoFile[i];
                }
                ++i;
            }
            GeoRasterFile[] listFiles = new GeoRasterFile[grfTemp.size()];
            int i2 = 0;
            while (i2 < listFiles.length) {
                listFiles[i2] = (GeoRasterFile)grfTemp.get(i2);
                ++i2;
            }
            i2 = 0;
            while (i2 < this.geoFile.length) {
                if (fileName.endsWith(this.geoFile[i2].getName())) {
                    this.bandSwitch.removeFile(this.geoFile[i2]);
                }
                ++i2;
            }
            this.geoFile = listFiles;
            return grfDelete;
        }
        System.err.println("PxRaster.addFile(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return null;
    }

    public int getBlockSize() {
        return this.geoFile[0].getBlockSize();
    }

    public GeoRasterFile[] getGeoFiles() {
        return this.geoFile;
    }

    public GeoRasterFile getGeoFile() {
        return this.geoFile[0];
    }

    public int getBandCount() {
        return this.bandSwitch.getBandCount();
    }

    public int getDataType() {
        if (this.geoFile != null) {
            return this.geoFile[0].getDataType();
        }
        System.err.println("PxRaster.getDataType(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return 0;
    }

    public GeoRasterFile[] getFiles() {
        return this.geoFile;
    }

    public void setBand(int flag, int nBand) {
        this.bandSwitch.setBand(flag, nBand);
    }

    public int getPosFile(int flag) {
        if (flag == 1) {
            return this.bandSwitch.getBandR().getPos();
        }
        if (flag == 2) {
            return this.bandSwitch.getBandG().getPos();
        }
        if (flag == 4) {
            return this.bandSwitch.getBandB().getPos();
        }
        return -1;
    }

    public int getBand(int flag) {
        if (flag == 1) {
            return this.bandSwitch.getBandR().getBand();
        }
        if (flag == 2) {
            return this.bandSwitch.getBandG().getBand();
        }
        if (flag == 4) {
            return this.bandSwitch.getBandB().getBand();
        }
        return -1;
    }

    public void setDrawBorder(boolean pm) {
        this.pintaMarco = pm;
    }

    public String getFName() {
        return this.geoFile[0].getName();
    }

    public String getFName(int i) {
        if (this.geoFile != null) {
            if (i < this.geoFile.length) {
                return this.geoFile[i].getName();
            }
            return null;
        }
        System.err.println("PxRaster.getFName(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return null;
    }

    public String[] getLisName() {
        if (this.geoFile != null) {
            String[] list = new String[this.geoFile.length];
            int i = 0;
            while (i < this.geoFile.length) {
                list[i] = this.geoFile[i].getName();
                ++i;
            }
            return list;
        }
        System.err.println("PxRaster.getListName(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return null;
    }

    public int getFWidth() {
        if (this.geoFile != null) {
            return this.geoFile[0].getWidth();
        }
        System.err.println("PxRaster.getFWidth(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return 0;
    }

    public int getFWidth(int i) {
        if (i < this.geoFile.length) {
            return this.geoFile[i].getWidth();
        }
        System.err.println("PxRaster.getFWidth(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return 0;
    }

    public int getFHeight() {
        if (this.geoFile != null) {
            return this.geoFile[0].getHeight();
        }
        System.err.println("PxRaster.getFHeight(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return 0;
    }

    public int getFHeight(int i) {
        if (i < this.geoFile.length) {
            return this.geoFile[i].getHeight();
        }
        System.err.println("PxRaster.getFHeight(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return 0;
    }

    public int nFiles() {
        if (this.geoFile != null) {
            return this.geoFile.length;
        }
        return 0;
    }

    public void setTransparency(boolean t) {
        if (this.geoFile != null) {
            int i = 0;
            while (i < this.geoFile.length) {
                this.geoFile[i].setTransparency(t);
                ++i;
            }
        } else {
            System.err.println("PxRaster.setTransparency(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
            return;
        }
    }

    public void setTransparency(int t) {
        if (this.geoFile != null) {
            int i = 0;
            while (i < this.geoFile.length) {
                this.geoFile[i].setTransparency(t);
                ++i;
            }
        } else {
            System.err.println("PxRaster.setTransparency(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
            return;
        }
    }

    public int getAlpha() {
        if (this.geoFile != null) {
            return this.geoFile[0].getAlpha();
        }
        System.err.println("PxRaster.getAlpha(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return 0;
    }

    public void setExtent(Extent e) {
        this.extent = e;
        if (e != null && this.proj != null) {
            this.pts = new Vector();
            this.pts.add(this.proj.createPoint(e.minX(), e.minY()));
            this.pts.add(this.proj.createPoint(e.maxX(), e.minY()));
            this.pts.add(this.proj.createPoint(e.maxX(), e.maxY()));
            this.pts.add(this.proj.createPoint(e.minX(), e.maxY()));
        }
    }

    public void setExtentForRequest(Extent e) {
        this.requestExtent = e;
        if (e != null && this.proj != null) {
            this.pts = new Vector();
            this.pts.add(this.proj.createPoint(e.minX(), e.minY()));
            this.pts.add(this.proj.createPoint(e.maxX(), e.minY()));
            this.pts.add(this.proj.createPoint(e.maxX(), e.maxY()));
            this.pts.add(this.proj.createPoint(e.minX(), e.maxY()));
        }
    }

    public void setView(Extent v, String vName) {
        if (this.geoFile != null) {
            int i = 0;
            while (i < this.geoFile.length) {
                this.geoFile[i].setView(v);
                ++i;
            }
        } else {
            System.err.println("PxRaster.setView(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
            return;
        }
        this.vName = vName;
    }

    public double[] getScale(int width, int height) {
        double[] scale = new double[2];
        if (this.geoFile != null) {
            scale[0] = (double)width / this.geoFile[0].getView().width();
            scale[1] = (double)height / this.geoFile[0].getView().height();
            return scale;
        }
        System.err.println("PxRaster.getScale(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        return null;
    }

    private Point2D coordULRotateRaster(double[] v) {
        double[] transf;
        double vx = v[0];
        double vy = v[1];
        double vx2 = v[2];
        double vy2 = v[3];
        if (this.geoFile != null && (transf = this.geoFile[0].getTransform()) != null && (transf[2] != 0.0 || transf[4] != 0.0)) {
            double ptoDesplX = transf[1] > 0.0 ? this.requestExtent.minX() : this.requestExtent.maxX();
            double ptoDesplY = transf[5] < 0.0 ? this.requestExtent.maxY() : this.requestExtent.minY();
            Point2D.Double ul = new Point2D.Double(vx - ptoDesplX, vy2 - ptoDesplY);
            Point2D.Double ur = new Point2D.Double(vx2 - ptoDesplX, vy2 - ptoDesplY);
            Point2D.Double ll = new Point2D.Double(vx - ptoDesplX, vy - ptoDesplY);
            Point2D.Double lr = new Point2D.Double(vx2 - ptoDesplX, vy - ptoDesplY);
            double shearX = 0.0;
            double shearY = 0.0;
            shearX = transf[5] != 0.0 ? transf[2] / transf[5] : transf[2];
            shearY = transf[1] != 0.0 ? transf[4] / transf[1] : transf[4];
            AffineTransform at = new AffineTransform();
            at.setToShear(shearX, shearY);
            at.transform(ul, ul);
            at.transform(ur, ur);
            at.transform(ll, ll);
            at.transform(lr, lr);
            ul = new Point2D.Double(((Point2D)ul).getX() + ptoDesplX, ((Point2D)ul).getY() + ptoDesplY);
            ur = new Point2D.Double(((Point2D)ur).getX() + ptoDesplX, ((Point2D)ur).getY() + ptoDesplY);
            ll = new Point2D.Double(((Point2D)ll).getX() + ptoDesplX, ((Point2D)ll).getY() + ptoDesplY);
            lr = new Point2D.Double(((Point2D)lr).getX() + ptoDesplX, ((Point2D)lr).getY() + ptoDesplY);
            vx2 = Math.max(Math.max(((Point2D)ul).getX(), ((Point2D)ur).getX()), Math.max(((Point2D)ll).getX(), ((Point2D)lr).getX()));
            vy2 = Math.max(Math.max(((Point2D)ul).getY(), ((Point2D)ur).getY()), Math.max(((Point2D)ll).getY(), ((Point2D)lr).getY()));
            vx = Math.min(Math.min(((Point2D)ul).getX(), ((Point2D)ur).getX()), Math.min(((Point2D)ll).getX(), ((Point2D)lr).getX()));
            vy = Math.min(Math.min(((Point2D)ul).getY(), ((Point2D)ur).getY()), Math.min(((Point2D)ll).getY(), ((Point2D)lr).getY()));
            this.adjustedRotedExtent = new double[4];
            this.adjustedRotedExtent[0] = vx;
            this.adjustedRotedExtent[1] = vy;
            this.adjustedRotedExtent[2] = vx2;
            this.adjustedRotedExtent[3] = vy2;
            return ul;
        }
        return null;
    }

    protected double[] calculateNewView(ViewPortData vp) {
        double[] transf;
        Extent sz = vp.getExtent();
        double vx = sz.minX();
        double vy = sz.minY();
        double vx2 = sz.maxX();
        double vy2 = sz.maxY();
        if (this.geoFile != null && (transf = this.geoFile[0].getTransform()) != null && (transf[2] != 0.0 || transf[4] != 0.0)) {
            double ptoDesplX = transf[1] > 0.0 ? this.requestExtent.minX() : this.requestExtent.maxX();
            double ptoDesplY = transf[5] < 0.0 ? this.requestExtent.maxY() : this.requestExtent.minY();
            Point2D.Double ul = new Point2D.Double(vx - ptoDesplX, vy2 - ptoDesplY);
            Point2D.Double ur = new Point2D.Double(vx2 - ptoDesplX, vy2 - ptoDesplY);
            Point2D.Double ll = new Point2D.Double(vx - ptoDesplX, vy - ptoDesplY);
            Point2D.Double lr = new Point2D.Double(vx2 - ptoDesplX, vy - ptoDesplY);
            double shearX = 0.0;
            double shearY = 0.0;
            shearX = transf[5] != 0.0 ? transf[2] / transf[5] : transf[2];
            shearY = transf[1] != 0.0 ? transf[4] / transf[1] : transf[4];
            AffineTransform at = new AffineTransform();
            at.setToShear(shearX, shearY);
            try {
                at.inverseTransform(ul, ul);
                at.inverseTransform(ur, ur);
                at.inverseTransform(ll, ll);
                at.inverseTransform(lr, lr);
            }
            catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }
            ul = new Point2D.Double(((Point2D)ul).getX() + ptoDesplX, ((Point2D)ul).getY() + ptoDesplY);
            ur = new Point2D.Double(((Point2D)ur).getX() + ptoDesplX, ((Point2D)ur).getY() + ptoDesplY);
            ll = new Point2D.Double(((Point2D)ll).getX() + ptoDesplX, ((Point2D)ll).getY() + ptoDesplY);
            lr = new Point2D.Double(((Point2D)lr).getX() + ptoDesplX, ((Point2D)lr).getY() + ptoDesplY);
            vx2 = Math.max(Math.max(((Point2D)ul).getX(), ((Point2D)ur).getX()), Math.max(((Point2D)ll).getX(), ((Point2D)lr).getX()));
            vy2 = Math.max(Math.max(((Point2D)ul).getY(), ((Point2D)ur).getY()), Math.max(((Point2D)ll).getY(), ((Point2D)lr).getY()));
            vx = Math.min(Math.min(((Point2D)ul).getX(), ((Point2D)ur).getX()), Math.min(((Point2D)ll).getX(), ((Point2D)lr).getX()));
            vy = Math.min(Math.min(((Point2D)ul).getY(), ((Point2D)ur).getY()), Math.min(((Point2D)ll).getY(), ((Point2D)lr).getY()));
        }
        if (vx < this.requestExtent.minX()) {
            vx = this.requestExtent.minX();
        }
        if (vy < this.requestExtent.minY()) {
            vy = this.requestExtent.minY();
        }
        if (vx2 > this.requestExtent.maxX()) {
            vx2 = this.requestExtent.maxX();
        }
        if (vy2 > this.requestExtent.maxY()) {
            vy2 = this.requestExtent.maxY();
        }
        if (this.geoFile != null) {
            int i = 0;
            while (i < this.geoFile.length) {
                this.geoFile[i].setView(new Extent(vx, vy, vx2, vy2));
                ++i;
            }
        } else {
            System.err.println("PxRaster.calculateNewView(): " + I18N.getString("org.cresques.px.PxRaster.image-not-loaded"));
        }
        double[] adjustedExtent = new double[]{vx, vy, vx2, vy2};
        return adjustedExtent;
    }

    private void setTransparencyByPixel() {
        if (this.geoFile[0].getMetadata() != null && this.stackManager == null) {
            TransparencyRange[] noData = this.geoFile[0].getMetadata().parserNodataInMetadata();
            if (noData != null) {
                ArrayList<TransparencyRange> entries = new ArrayList<TransparencyRange>();
                int i = 0;
                while (i < noData.length) {
                    entries.add(noData[i]);
                    ++i;
                }
                this.stackManager = new RasterFilterStackManager(this.filterStack);
                this.stackManager.addTransparencyFilter(entries, 0, 255, 255, 255);
            }
            TransparencyRange noDataValue = this.geoFile[0].getMetadata().parserNodataByBand();
            if (noData == null && noDataValue != null) {
                ArrayList<TransparencyRange> entries = new ArrayList<TransparencyRange>();
                entries.add(noDataValue);
                this.stackManager = new RasterFilterStackManager(this.filterStack);
                this.stackManager.addTransparencyFilter(entries, 0, 255, 255, 255);
            }
        }
    }

    public synchronized void draw(Graphics2D g, ViewPortData vp) {
        this.geoImage = null;
        double shearX = 0.0;
        double shearY = 0.0;
        long t1 = new Date().getTime();
        this.lastViewPort = vp;
        if (vp.getExtent().minX() > this.extent.maxX() || vp.getExtent().minY() > this.extent.maxY() || vp.getExtent().maxX() < this.extent.minX() || vp.getExtent().maxY() < this.extent.minY()) {
            return;
        }
        double[] adjustedExtent = this.calculateNewView(vp);
        Point2D p2d = this.coordULRotateRaster(adjustedExtent);
        Extent v = this.geoFile[0].getView();
        double x = v.minX();
        double y = v.minY();
        double w = v.width();
        double h = v.height();
        double scalex = vp.mat.getScaleX();
        double scaley = vp.mat.getScaleY();
        int wImg = (int)Math.round(Math.abs((adjustedExtent[2] - adjustedExtent[0]) * scalex));
        int hImg = (int)Math.round(Math.abs((adjustedExtent[3] - adjustedExtent[1]) * scaley));
        if (wImg <= 0 || hImg <= 0) {
            return;
        }
        int wI = wImg;
        int hI = hImg;
        double[] transf = this.bandSwitch.getBandR().getGeoRasterFile().getTransform();
        Point2D.Double pt = null;
        if (transf != null && (transf[2] != 0.0 || transf[4] != 0.0)) {
            pt = new Point2D.Double(p2d.getX(), p2d.getY());
            wImg = (int)Math.round(Math.abs((this.adjustedRotedExtent[2] - this.adjustedRotedExtent[0]) * scalex));
            hImg = (int)Math.round(Math.abs((this.adjustedRotedExtent[3] - this.adjustedRotedExtent[1]) * scaley));
        } else {
            pt = new Point2D.Double(adjustedExtent[0], adjustedExtent[3]);
        }
        try {
            long t2;
            vp.mat.transform(pt, pt);
            this.setTransparencyByPixel();
            if (this.geoFile != null && (this.geoFile[0] instanceof GdalFile || this.geoFile[0] instanceof MemoryRasterDriver) && this.geoFile[0].getDataType() != 0) {
                RasterBuf raster = null;
                if (this.geoFile.length > 1) {
                    RasterBuf rasterG = null;
                    RasterBuf rasterB = null;
                    raster = ((GdalFile)this.bandSwitch.getBandR().getGeoRasterFile()).getRaster(wImg, hImg, this.rp);
                    rasterG = ((GdalFile)this.bandSwitch.getBandG().getGeoRasterFile()).getRaster(wImg, hImg, this.rp);
                    raster.copyBand(rasterG, 0, 1);
                    rasterB = ((GdalFile)this.bandSwitch.getBandB().getGeoRasterFile()).getRaster(wImg, hImg, this.rp);
                    raster.copyBand(rasterB, 0, 2);
                } else {
                    raster = this.geoFile[0] instanceof MemoryRasterDriver ? ((MemoryRasterDriver)this.geoFile[0]).getRaster(wImg, hImg, this.rp) : ((GdalFile)this.geoFile[0]).getRaster(wImg, hImg, this.rp);
                }
                t2 = new Date().getTime();
                LOGGER.debug((Object)(String.valueOf(I18N.getString("org.cresques.px.PxRaster.drawing-pxraster")) + ": " + (double)(t2 - t1) / 1000.0 + I18N.getString("org.cresques.px.PxRaster.secs-filtering-rendering")));
                t1 = t2;
                this.filterStack.setInitRasterBuf((Object)raster);
                if (this.stackManager == null) {
                    this.stackManager = new RasterFilterStackManager(this.filterStack);
                    this.stackManager.addEnhancedFilter(false, this.geoFile[0].getName());
                    this.stackManager.removeFilter(this.stackManager.getTypeFilter("computeminmax"));
                    this.stackManager.addTailFilter(0.02, 0.0, true);
                }
                if (raster.getBandCount() == 1) {
                    raster.replicateBand(0, 1);
                    raster.replicateBand(0, 2);
                }
                this.geoImage = this.renderizeRaster(raster, vp, v);
                g.drawImage(this.geoImage, (int)pt.getX(), (int)pt.getY(), this.component);
            } else if (!(this.geoFile == null || this.geoFile.length <= 1 || this.bandSwitch.getBandR().getGeoRasterFile().getName().equals(this.bandSwitch.getBandG().getGeoRasterFile().getName()) && this.bandSwitch.getBandR().getGeoRasterFile().getName().equals(this.bandSwitch.getBandB().getGeoRasterFile().getName()) && this.bandSwitch.getBandG().getGeoRasterFile().getName().equals(this.bandSwitch.getBandB().getGeoRasterFile().getName()))) {
                LOGGER.debug((Object)("Dibujando PxRaster (Multifile) ... Bands " + this.geoFile.length));
                if (this.bandSwitch.getBandR().getGeoRasterFile() instanceof EcwFile) {
                    ((EcwFile)this.bandSwitch.getBandR().getGeoRasterFile()).setMultifile(true);
                }
                if (this.bandSwitch.getBandG().getGeoRasterFile() instanceof EcwFile) {
                    ((EcwFile)this.bandSwitch.getBandG().getGeoRasterFile()).setMultifile(true);
                }
                if (this.bandSwitch.getBandB().getGeoRasterFile() instanceof EcwFile) {
                    ((EcwFile)this.bandSwitch.getBandB().getGeoRasterFile()).setMultifile(true);
                }
                if (this.stackManager == null) {
                    this.stackManager = new RasterFilterStackManager(this.filterStack);
                }
                if (!this.filterStack.isActive(this.stackManager.getTypeFilter("sharpening"))) {
                    this.geoImage = this.bandSwitch.getBandR().getGeoRasterFile().updateImage(wImg, hImg, this.rp, null, 0, 0);
                    this.geoImage = this.bandSwitch.getBandG().getGeoRasterFile().updateImage(wImg, hImg, this.rp, this.geoImage, this.bandSwitch.getBandG().getBand(), 2);
                    this.geoImage = this.bandSwitch.getBandB().getGeoRasterFile().updateImage(wImg, hImg, this.rp, this.geoImage, this.bandSwitch.getBandB().getBand(), 4);
                } else {
                    this.geoImage = new BufferedImage(wImg, hImg, 1);
                }
                this.filterStack.setInitRasterBuf((Object)this.geoImage);
                this.geoImage = this.renderizeRaster(this.geoImage, vp, v);
                g.drawImage(this.geoImage, (int)pt.getX(), (int)pt.getY(), this.component);
            } else if (this.geoFile != null) {
                int pxMin;
                this.geoImage = this.bandSwitch.getBandR().getGeoRasterFile().updateImage(wImg, hImg, this.rp, null, 0, 0);
                if (this.stackManager == null) {
                    this.stackManager = new RasterFilterStackManager(this.filterStack);
                }
                PaletteStackManager psm = (PaletteStackManager)this.stackManager.getManagerByClass(PaletteStackManager.class);
                if (this.bandSwitch.getBandR().getGeoRasterFile().getPalette() != null) {
                    PaletteFilter pf = (PaletteFilter)this.filterStack.getByType(13);
                    if (pf == null) {
                        psm.addPaletteFilter(this.bandSwitch.getBandR().getGeoRasterFile().getPalette(), false);
                    } else {
                        int index = this.bandSwitch.getBandR().getGeoRasterFile().getName().lastIndexOf(File.separator);
                        String fname = this.bandSwitch.getBandR().getGeoRasterFile().getName().substring(index + 1);
                        if (fname.startsWith("wmsGetMap") || fname.startsWith("wcsGetMap")) {
                            this.bandSwitch.getBandR().getGeoRasterFile().readPalette();
                            psm.addPaletteFilter(this.bandSwitch.getBandR().getGeoRasterFile().getPalette(), false);
                        }
                    }
                }
                this.filterStack.setInitRasterBuf((Object)this.geoImage);
                this.geoImage = this.renderizeRaster(this.geoImage, vp, v);
                AffineTransform at = new AffineTransform();
                if (transf != null && (transf[2] != 0.0 || transf[4] != 0.0)) {
                    shearX = transf[5] != 0.0 ? transf[2] / transf[5] : transf[2];
                    shearY = transf[1] != 0.0 ? transf[4] / transf[1] : transf[4];
                    at.setToShear(-shearX, -shearY);
                    at.scale((double)wI / (double)wImg, (double)hI / (double)hImg);
                    g.transform(at);
                    at.inverseTransform(pt, pt);
                    pxMin = (int)Math.round(pt.getX());
                    int pyMin = (int)Math.round(pt.getY());
                    int pxMax = pxMin + this.geoImage.getWidth(this.component);
                    int pyMax = pyMin + this.geoImage.getHeight(this.component);
                    LOGGER.debug((Object)(String.valueOf(I18N.getString("org.cresques.px.PxRaster.requesting-image")) + this.geoFile[0].getName() + " XMIN->" + pxMin + " YMIN->" + pyMin + " XMAX->" + pxMax + " YMAX->" + pyMax + " WIDTH->" + (pxMax - pxMin) + " HEIGHT -> " + (pyMax - pyMin)));
                    g.drawImage(this.geoImage, pxMin, pyMin, this.geoImage.getWidth(this.component) + 1, this.geoImage.getHeight(this.component) + 1, this.component);
                    g.transform(at.createInverse());
                } else {
                    pxMin = (int)Math.round(pt.getX());
                    int pyMin = (int)Math.round(pt.getY());
                    int pxMax = pxMin + this.geoImage.getWidth(this.component);
                    int pyMax = pyMin + this.geoImage.getHeight(this.component);
                    LOGGER.debug((Object)(String.valueOf(I18N.getString("org.cresques.px.PxRaster.requesting-image")) + this.geoFile[0].getName() + " XMIN->" + pxMin + " YMIN->" + pyMin + " XMAX->" + pxMax + " YMAX->" + pyMax + " WIDTH->" + (pxMax - pxMin) + " HEIGHT -> " + (pyMax - pyMin)));
                    g.drawImage(this.geoImage, pxMin, pyMin, this.geoImage.getWidth(this.component) + 1, this.geoImage.getHeight(this.component) + 1, this.component);
                }
            } else {
                LOGGER.warn((Object)(String.valueOf(I18N.getString("org.cresques.px.PxRaster.drawing-pxraster")) + ": " + I18N.getString("org.cresques.px.PxRaster.photo-not-loaded")));
            }
            t2 = new Date().getTime();
            LOGGER.debug((Object)(String.valueOf(I18N.getString("org.cresques.px.PxRaster.drawing-pxraster")) + ": " + (double)(t2 - t1) / 1000.0 + ", " + I18N.getString("org.cresques.px.PxRaster.secs")));
        }
        catch (SupersamplingNotSupportedException e) {
            LOGGER.error((Object)I18N.getString("org.cresques.px.PxRaster.supersampling-not-supported"));
            return;
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if (this.pintaMarco) {
            this.drawMarco(g, vp);
        }
    }

    public Image renderizeRaster(RasterBuf raster, ViewPortData vp, Extent e) {
        if (this.filterStack != null) {
            this.filterStack.setViewPortData(vp);
            this.filterStack.setExtent(e);
            this.filterStack.setStep(this.geoFile[0].getStepX(), this.geoFile[0].getStepY());
            raster = this.filterStack.execute(raster);
        }
        LOGGER.debug((Object)"Image renderizeRaster(RasterBuf raster, ViewPortData vp) ");
        RasterByteToImageFilter rti = null;
        if (this.getDataType() == 0) {
            rti = new RasterByteToImageFilter();
        } else if (this.getDataType() == 2) {
            rti = new RasterShortToImageFilter();
        } else if (this.getDataType() == 3) {
            rti = new RasterIntToImageFilter();
        } else if (this.getDataType() == 4) {
            rti = new RasterFloatToImageFilter();
        } else if (this.getDataType() == 5) {
            rti = new RasterDoubleToImageFilter();
        }
        if (this.filterStack.getOutDataType() != -1) {
            rti.addParam("raster", this.filterStack.getResult());
            rti.addParam("alpha", (Object)new Integer(this.getAlpha()));
            rti.execute();
            raster = null;
            return (Image)rti.getResult("raster");
        }
        return (Image)this.filterStack.getResult();
    }

    public Image renderizeRaster(Image image, ViewPortData vp, Extent e) {
        if (this.filterStack != null) {
            this.filterStack.setViewPortData(vp);
            this.filterStack.setExtent(e);
            this.filterStack.setStep(this.geoFile[0].getStepX(), this.geoFile[0].getStepY());
            this.filterStack.execute(image);
        }
        if (this.filterStack.getOutDataType() != -1) {
            RasterByteToImageFilter rti = null;
            if (this.getDataType() == 0) {
                rti = new RasterByteToImageFilter();
            } else if (this.getDataType() == 2) {
                rti = new RasterShortToImageFilter();
            } else if (this.getDataType() == 3) {
                rti = new RasterIntToImageFilter();
            } else if (this.getDataType() == 4) {
                rti = new RasterFloatToImageFilter();
            } else if (this.getDataType() == 5) {
                rti = new RasterDoubleToImageFilter();
            }
            rti.addParam("raster", (Object)((RasterBuf)this.filterStack.getResult()));
            rti.addParam("alpha", (Object)new Integer(this.getAlpha()));
            rti.execute();
            return (Image)rti.getResult("raster");
        }
        return (Image)this.filterStack.getResult();
    }

    public void drawMarco(Graphics2D g, ViewPortData vp) {
        Color color = new Color(128, 128, 128);
        Color fillColor = new Color(255, 220, 220, 32);
        GeneralPath gp = this.newGP(vp);
        g.setColor(fillColor);
        g.fill(gp);
        g.setColor(color);
        g.draw(gp);
    }

    private GeneralPath newGP(ViewPortData vp) {
        GeneralPath gp = new GeneralPath();
        Point2D.Double pt0 = new Point2D.Double(0.0, 0.0);
        Point2D.Double pt1 = new Point2D.Double(0.0, 0.0);
        Point2D.Double pt2 = new Point2D.Double(0.0, 0.0);
        Point2D.Double pt3 = new Point2D.Double(0.0, 0.0);
        vp.mat.transform(this.pts.get(0), pt0);
        vp.mat.transform(this.pts.get(1), pt1);
        vp.mat.transform(this.pts.get(2), pt2);
        vp.mat.transform(this.pts.get(3), pt3);
        gp.moveTo((float)pt0.getX(), (float)pt0.getY());
        gp.lineTo((float)pt2.getX(), (float)pt2.getY());
        gp.moveTo((float)pt1.getX(), (float)pt1.getY());
        gp.lineTo((float)pt3.getX(), (float)pt3.getY());
        gp.moveTo((float)pt0.getX(), (float)pt0.getY());
        gp.lineTo((float)pt1.getX(), (float)pt1.getY());
        gp.lineTo((float)pt2.getX(), (float)pt2.getY());
        gp.lineTo((float)pt3.getX(), (float)pt3.getY());
        if (this.extentOrig != this.extent) {
            gp.lineTo((float)pt0.getX(), (float)pt0.getY());
            Vector<Point2D> pts = new Vector<Point2D>();
            pts.add(this.proj.createPoint(this.extentOrig.minX(), this.extentOrig.minY()));
            pts.add(this.proj.createPoint(this.extentOrig.maxX(), this.extentOrig.minY()));
            pts.add(this.proj.createPoint(this.extentOrig.maxX(), this.extentOrig.maxY()));
            pts.add(this.proj.createPoint(this.extentOrig.minX(), this.extentOrig.maxY()));
            vp.mat.transform((Point2D)pts.get(0), pt0);
            vp.mat.transform((Point2D)pts.get(1), pt1);
            vp.mat.transform((Point2D)pts.get(2), pt2);
            vp.mat.transform((Point2D)pts.get(3), pt3);
            gp.moveTo((float)pt0.getX(), (float)pt0.getY());
            gp.lineTo((float)pt1.getX(), (float)pt1.getY());
            gp.lineTo((float)pt2.getX(), (float)pt2.getY());
            gp.lineTo((float)pt3.getX(), (float)pt3.getY());
        }
        gp.closePath();
        return gp;
    }

    public IProjection getProjection() {
        return this.proj;
    }

    public void setProjection(IProjection p) {
        this.proj = p;
    }

    public void reProject(ICoordTrans rp) {
        this.rp = rp.getInverted();
        LOGGER.debug((Object)"PxRaster: reProject()");
        Vector<Point2D> savePts = this.pts;
        this.pts = new Vector();
        this.extent = new Extent();
        Point2D ptDest = null;
        int i = 0;
        while (i < savePts.size()) {
            ptDest = rp.getPDest().createPoint(0.0, 0.0);
            ptDest = rp.convert(savePts.get(i), ptDest);
            this.pts.add(ptDest);
            this.extent.add(ptDest);
            ++i;
        }
        this.setProjection(rp.getPDest());
    }

    public RasterFilterStackManager getStackManager() {
        return this.stackManager;
    }

    public void setStackManager(RasterFilterStackManager sm) {
        this.stackManager = sm;
    }

    public String getVName() {
        return this.vName;
    }

    public void setVName(String name) {
        this.vName = name;
    }

    public Image getGeoImage() {
        return this.geoImage;
    }

    public ViewPortData getLastViewPort() {
        return this.lastViewPort;
    }

    class BandSwitch {
        private ArrayList<GeoRasterFile> geoFiles = new ArrayList();
        private FileBands[] listBands = new FileBands[3];
        private boolean debug = false;

        BandSwitch() {
            int i = 0;
            while (i < this.listBands.length) {
                this.listBands[i] = new FileBands();
                ++i;
            }
        }

        public void addFile(GeoRasterFile grf) {
            this.geoFiles.add(grf);
            if (this.debug) {
                this.show("addFile");
            }
        }

        public void removeFile(GeoRasterFile grf) {
            int iFile = 0;
            while (iFile < this.geoFiles.size()) {
                if (grf.equals(this.geoFiles.get(iFile))) {
                    this.geoFiles.remove(iFile);
                    --iFile;
                }
                ++iFile;
            }
            if (this.debug) {
                this.show("removeFile");
            }
        }

        public void setBand(int flag, GeoRasterFile grf, int nBand) {
            if ((flag & 1) == 1) {
                this.listBands[0].setBand(nBand);
                this.listBands[0].setGeoRasterFile(grf);
            } else if ((flag & 2) == 2) {
                this.listBands[1].setBand(nBand);
                this.listBands[1].setGeoRasterFile(grf);
            } else if ((flag & 4) == 4) {
                this.listBands[2].setBand(nBand);
                this.listBands[2].setGeoRasterFile(grf);
            } else {
                return;
            }
            grf.setBand(flag, nBand);
        }

        public void setBand(int flag, int nBand) {
            int cont = 0;
            int iGrf = 0;
            while (iGrf < this.geoFiles.size()) {
                int iBand = 0;
                while (iBand < this.geoFiles.get(iGrf).getBandCount()) {
                    if ((flag & 1) == 1 && cont == nBand) {
                        this.listBands[0].setGeoRasterFile(this.geoFiles.get(iGrf));
                        this.listBands[0].setBand(iBand);
                        this.listBands[0].setPos(iGrf);
                        this.geoFiles.get(iGrf).setBand(flag, iBand);
                    } else if ((flag & 2) == 2 && cont == nBand) {
                        this.listBands[1].setGeoRasterFile(this.geoFiles.get(iGrf));
                        this.listBands[1].setBand(iBand);
                        this.listBands[1].setPos(iGrf);
                        this.geoFiles.get(iGrf).setBand(flag, iBand);
                    } else if ((flag & 4) == 4 && cont == nBand) {
                        this.listBands[2].setGeoRasterFile(this.geoFiles.get(iGrf));
                        this.listBands[2].setBand(iBand);
                        this.listBands[2].setPos(iGrf);
                        this.geoFiles.get(iGrf).setBand(flag, iBand);
                    }
                    ++cont;
                    ++iBand;
                }
                ++iGrf;
            }
            if (this.debug) {
                this.show("setBand");
            }
        }

        public int getBandCount() {
            int nbandas = 0;
            int iGrf = 0;
            while (iGrf < this.geoFiles.size()) {
                nbandas += this.geoFiles.get(iGrf).getBandCount();
                ++iGrf;
            }
            return nbandas;
        }

        public FileBands getBandR() {
            return this.listBands[0];
        }

        public FileBands getBandG() {
            return this.listBands[1];
        }

        public FileBands getBandB() {
            return this.listBands[2];
        }

        public void show(String op) {
            String banda = null;
            LOGGER.debug((Object)("** " + op + " **"));
            int i = 0;
            while (i < 3) {
                if (i == 0) {
                    banda = new String(I18N.getString("org.cresques.px.PxRaster.red"));
                } else if (i == 1) {
                    banda = new String(I18N.getString("org.cresques.px.PxRaster.green"));
                } else if (i == 2) {
                    banda = new String(I18N.getString("org.cresques.px.PxRaster.blue"));
                }
                LOGGER.debug((Object)("** BANDA IMAGE=" + banda + " FILEPOS=" + this.listBands[i].getBand() + " BANDA DEL FICHERO=" + this.listBands[i].getBand()));
                ++i;
            }
        }
    }

    class FileBands {
        GeoRasterFile grf;
        int band;
        int filePos;

        FileBands() {
        }

        public int getBand() {
            return this.band;
        }

        public void setBand(int band) {
            this.band = band;
        }

        public int getPos() {
            return this.filePos;
        }

        public void setPos(int pos) {
            this.filePos = pos;
        }

        public GeoRasterFile getGeoRasterFile() {
            return this.grf;
        }

        public void setGeoRasterFile(GeoRasterFile grf) {
            this.grf = grf;
        }
    }
}

