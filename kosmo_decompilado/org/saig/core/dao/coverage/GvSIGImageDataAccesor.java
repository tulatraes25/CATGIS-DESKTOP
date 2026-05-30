/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 *  org.gvsig.raster.RasterLibrary
 *  org.gvsig.raster.buffer.BufferFactory
 *  org.gvsig.raster.dataset.IRasterDataSource
 *  org.gvsig.raster.dataset.MultiRasterDataset
 *  org.gvsig.raster.dataset.NotSupportedExtensionException
 *  org.gvsig.raster.dataset.io.RasterDriverException
 *  org.gvsig.raster.datastruct.Extent
 *  org.gvsig.raster.datastruct.ViewPortData
 *  org.gvsig.raster.grid.Grid
 *  org.gvsig.raster.grid.GridException
 *  org.gvsig.raster.grid.GridPalette
 *  org.gvsig.raster.grid.GridTransparency
 *  org.gvsig.raster.grid.filter.FilterTypeException
 *  org.gvsig.raster.grid.filter.RasterFilterList
 *  org.gvsig.raster.grid.filter.RasterFilterListManager
 *  org.gvsig.raster.grid.filter.bands.ColorTableListManager
 *  org.gvsig.raster.grid.render.Rendering
 */
package org.saig.core.dao.coverage;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.util.FileUtil;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.raster.RasterLibrary;
import org.gvsig.raster.buffer.BufferFactory;
import org.gvsig.raster.dataset.IRasterDataSource;
import org.gvsig.raster.dataset.MultiRasterDataset;
import org.gvsig.raster.dataset.NotSupportedExtensionException;
import org.gvsig.raster.dataset.io.RasterDriverException;
import org.gvsig.raster.datastruct.Extent;
import org.gvsig.raster.datastruct.ViewPortData;
import org.gvsig.raster.grid.Grid;
import org.gvsig.raster.grid.GridException;
import org.gvsig.raster.grid.GridPalette;
import org.gvsig.raster.grid.GridTransparency;
import org.gvsig.raster.grid.filter.FilterTypeException;
import org.gvsig.raster.grid.filter.RasterFilterList;
import org.gvsig.raster.grid.filter.RasterFilterListManager;
import org.gvsig.raster.grid.filter.bands.ColorTableListManager;
import org.gvsig.raster.grid.render.Rendering;
import org.saig.core.dao.coverage.ImageDataAccesor;
import org.saig.core.renderer.RendererParameterWrapper;
import org.saig.jump.lang.I18N;

public class GvSIGImageDataAccesor
implements ImageDataAccesor {
    private static final Logger LOGGER = Logger.getLogger(GvSIGImageDataAccesor.class);
    protected IRasterDataSource dataset = null;
    private Envelope envelope;
    private String filePath;
    private IProjection projection;
    protected Rendering render = null;
    private BufferFactory bufferFactory;

    static {
        RasterLibrary.wakeUp();
    }

    public GvSIGImageDataAccesor(String path, IProjection proj) throws Exception {
        this.filePath = path;
        this.projection = proj;
        if (this.projection == null) {
            throw new Exception(I18N.getString("org.saig.core.dao.coverage.UniversalImageDataAccesor.null-projection"));
        }
        File imageFile = new File(path);
        if (!imageFile.exists() || !imageFile.canRead()) {
            throw new FileNotFoundException(I18N.getMessage("org.saig.core.dao.coverage.UniversalImageDataAccesor.file-not-found-{0}", new Object[]{imageFile.getAbsolutePath()}));
        }
        try {
            this.dataset = MultiRasterDataset.open((IProjection)proj, (Object)this.filePath);
        }
        catch (NotSupportedExtensionException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if (this.dataset != null) {
            this.init();
        }
    }

    public GvSIGImageDataAccesor(String[] paths, IProjection proj) throws Exception {
        this.filePath = new File(paths[0]).getParent();
        this.projection = proj;
        if (this.projection == null) {
            throw new Exception(I18N.getString("org.saig.core.dao.coverage.UniversalImageDataAccesor.null-projection"));
        }
        String[] stringArray = paths;
        int n = paths.length;
        int n2 = 0;
        while (n2 < n) {
            String currentPath = stringArray[n2];
            File imageFile = new File(currentPath);
            if (!imageFile.exists() || !imageFile.canRead()) {
                throw new FileNotFoundException(I18N.getMessage("org.saig.core.dao.coverage.UniversalImageDataAccesor.file-not-found-{0}", new Object[]{imageFile.getAbsolutePath()}));
            }
            ++n2;
        }
        try {
            this.dataset = MultiRasterDataset.open((IProjection)proj, (Object)paths);
        }
        catch (NotSupportedExtensionException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if (this.dataset != null) {
            this.init();
        }
    }

    private void init() {
        this.bufferFactory = new BufferFactory(this.dataset);
        this.render = new Rendering(this.bufferFactory);
        this.initFilters();
    }

    private void initFilters() {
        RasterFilterList filterList = new RasterFilterList();
        filterList.addEnvParam("IStatistics", (Object)this.dataset.getStatistics());
        filterList.addEnvParam("MultiRasterDataset", (Object)this.dataset);
        if (this.dataset == null) {
            return;
        }
        this.dataset.resetNoDataValue();
        this.applyNoData();
        GridTransparency gridTransparency = new GridTransparency(this.dataset.getTransparencyFilesStatus());
        filterList.setInitDataType(this.getDataType());
        RasterFilterListManager filterManager = new RasterFilterListManager(filterList);
        try {
            if (this.dataset.getColorTables()[0] != null) {
                GridPalette p = new GridPalette(this.dataset.getColorTables()[0]);
                ColorTableListManager ctm = new ColorTableListManager(filterManager);
                ctm.addColorTableFilter(p);
            }
            this.render.setFilterList(filterList);
            this.render.setLastTransparency(gridTransparency);
        }
        catch (FilterTypeException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public void close() {
        this.dataset.close();
        this.dataset = null;
        this.bufferFactory.free();
        this.bufferFactory = null;
        this.render.free();
        this.render = null;
        this.projection = null;
        this.envelope = null;
        this.filePath = null;
    }

    public void applyNoData() {
        Double noDataValue = this.getNoDataValue();
        this.dataset.getTransparencyFilesStatus().setNoData(noDataValue.doubleValue());
    }

    @Override
    public Object getData(int x, int y, int band) {
        Object value = null;
        try {
            value = this.dataset.getData(x, y, band);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if (value == null) {
            return this.getNoDataValue();
        }
        return value;
    }

    @Override
    public Envelope getEnvelope() {
        if (this.envelope == null) {
            Extent extent = this.dataset.getExtent();
            this.envelope = new Envelope(extent.minX(), extent.maxX(), extent.minY(), extent.maxY());
        }
        return this.envelope;
    }

    @Override
    public int getType() {
        String ext = FileUtil.getExtension(this.filePath);
        if (ext.equalsIgnoreCase("ecw")) {
            return 0;
        }
        if (ext.equalsIgnoreCase("sid")) {
            return 1;
        }
        return 999;
    }

    @Override
    public String getImagePath() {
        return this.filePath;
    }

    @Override
    public int getHeightInPixels() {
        return (int)this.dataset.getHeight();
    }

    @Override
    public int getWidthInPixels() {
        return (int)this.dataset.getWidth();
    }

    @Override
    public void getImagen(Graphics2D g2d, RendererParameterWrapper renderPS) {
        Envelope env = renderPS.getOriginalEnvelope();
        Extent e = new Extent(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY());
        Dimension rectangle = new Dimension();
        rectangle.setSize(renderPS.getWidth(), renderPS.getHeight());
        ViewPortData viewPortData = new ViewPortData(this.projection, e, (Dimension2D)rectangle);
        viewPortData.zoom(e);
        try {
            this.setTransparency(renderPS.getAlpha());
            this.render.draw(g2d, viewPortData);
        }
        catch (Exception e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
    }

    @Override
    public double getValue(double xCoord, double yCoord) {
        return 0.0;
    }

    @Override
    public double getXCellSize() {
        Envelope env = this.getEnvelope();
        double dCellsize = (env.getMaxX() - env.getMinX()) / (double)this.getWidthInPixels();
        return dCellsize;
    }

    @Override
    public double getYCellSize() {
        Envelope env = this.getEnvelope();
        double dCellsize = (env.getMaxY() - env.getMinY()) / (double)this.getHeightInPixels();
        return dCellsize;
    }

    @Override
    public int getNumBands() {
        return this.dataset.getBandCount();
    }

    @Override
    public double getNoDataValue() {
        if (this.dataset == null) {
            return RasterLibrary.defaultNoDataValue;
        }
        return this.dataset.getNoDataValue();
    }

    @Override
    public void setNoDataValue(double nd) {
        if (this.bufferFactory != null) {
            this.bufferFactory.setNoDataToFill(nd);
        }
        if (this.dataset != null) {
            this.dataset.setNoDataValue(nd);
        }
    }

    @Override
    public int getDataType() {
        return this.dataset.getDataType()[0];
    }

    public void setTransparency(int trans) {
        try {
            this.getRenderTransparency().setOpacity(trans);
            this.getRenderTransparency().activeTransparency();
        }
        catch (NullPointerException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public GridTransparency getRenderTransparency() {
        return this.getRender().getLastTransparency();
    }

    public Rendering getRender() {
        if (this.render == null) {
            this.render = new Rendering(this.bufferFactory);
        }
        return this.render;
    }

    public Grid getReadOnlyFullGrid(boolean interpolated) throws GridException, InterruptedException {
        BufferFactory bf = new BufferFactory(this.dataset.newDataset());
        bf.setReadOnly(true);
        bf.clearDrawableBand();
        bf.setAllDrawableBands();
        try {
            bf.setAreaOfInterest();
        }
        catch (RasterDriverException e) {
            throw new GridException("Error reading buffer");
        }
        return new Grid(bf, interpolated);
    }
}

