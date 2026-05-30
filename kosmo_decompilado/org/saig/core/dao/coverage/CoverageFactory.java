/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 *  org.gvsig.raster.RasterLibrary
 *  org.gvsig.raster.dataset.RasterDataset
 */
package org.saig.core.dao.coverage;

import com.vividsolutions.jump.util.FileUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Arrays;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.raster.RasterLibrary;
import org.gvsig.raster.dataset.RasterDataset;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.dao.coverage.GridCoverage;
import org.saig.core.dao.coverage.GridCoverageCollection;
import org.saig.core.dao.coverage.ImageDataAccesorFactory;
import org.saig.jump.lang.I18N;

public class CoverageFactory {
    private static final Logger LOGGER = Logger.getLogger(CoverageFactory.class);
    private static CoverageFactory instance;

    private CoverageFactory() {
    }

    public static CoverageFactory getInstance() {
        if (instance == null) {
            instance = new CoverageFactory();
        }
        return instance;
    }

    public Coverage buildCoverageFromPath(String path, IProjection proj) throws Exception {
        File file = new File(path);
        GridCoverage coverage = null;
        if (!file.exists() || !file.canRead()) {
            throw new FileNotFoundException(I18N.getMessage("org.saig.core.dao.coverage.CoverageFactory.image-file-or-directory-{0}-was-not-found-or-could-not-be-read", new Object[]{file.getAbsolutePath()}));
        }
        if (file.isDirectory()) {
            LOGGER.info((Object)("Loading raster file collection from directory " + file.getAbsolutePath()));
            Object[] files = file.listFiles(new FilenameFilter(){

                @Override
                public boolean accept(File dir, String name) {
                    return CoverageFactory.this.isSupported(name);
                }
            });
            if (ArrayUtils.isEmpty((Object[])files)) {
                throw new Exception(I18N.getMessage("org.saig.core.dao.coverage.CoverageFactory.image-directory-{0}-has-no-supported-images-types", new Object[]{file.getAbsolutePath()}));
            }
            LOGGER.info((Object)(String.valueOf(files.length) + " supported files found"));
            GridCoverageCollection collection = new GridCoverageCollection(ImageDataAccesorFactory.createDataAccesor((File[])files, proj));
            int i = 0;
            while (i < files.length) {
                GridCoverage cov = new GridCoverage(ImageDataAccesorFactory.createDataAccesor((File)files[i], proj));
                collection.addGridCoverage(cov);
                ++i;
            }
            coverage = collection;
        } else {
            LOGGER.info((Object)("Loading raster file " + file.getAbsolutePath()));
            coverage = new GridCoverage(ImageDataAccesorFactory.createDataAccesor(file, proj));
        }
        return coverage;
    }

    public boolean isSupported(String imageFilePath) {
        RasterLibrary.wakeUp();
        return !"rmf".equals(FileUtil.getExtension(imageFilePath)) && RasterDataset.fileIsSupported((String)imageFilePath);
    }

    public String[] getSupportedExtensions() {
        RasterLibrary.wakeUp();
        Object[] supportedExtensions = RasterDataset.getExtensionsSupported();
        Arrays.sort(supportedExtensions);
        supportedExtensions = (String[])ArrayUtils.removeElement((Object[])supportedExtensions, (Object)"rmf");
        return supportedExtensions;
    }
}

