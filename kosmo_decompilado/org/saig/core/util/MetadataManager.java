/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.util;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.filedatasource.utils.FileCharsetDetector;
import org.saig.core.model.feature.FeatureCollectionOnDemand;

public class MetadataManager {
    private static final Logger LOGGER = Logger.getLogger(MetadataManager.class);
    private static MetadataManager instance;
    public static final String METADATA_FILE_EXTENSION = ".xml";

    private MetadataManager() {
    }

    public static MetadataManager getInstance() {
        if (instance == null) {
            instance = new MetadataManager();
        }
        return instance;
    }

    public String loadMetadata(FeatureCollection fc, DataSourceQuery dataSourceQuery) {
        String metadata;
        block22: {
            metadata = "";
            BufferedReader br = null;
            FileInputStream fi = null;
            try {
                try {
                    String fileName;
                    File metadataFile;
                    FeatureCollectionOnDemand fcd;
                    if (fc instanceof FeatureCollectionOnDemand && !((fcd = (FeatureCollectionOnDemand)fc).getDataAccesor() instanceof AbstractJDBCDataSource) && (metadataFile = new File(String.valueOf(fileName = dataSourceQuery.getQuery()) + METADATA_FILE_EXTENSION)).exists() && metadataFile.canRead()) {
                        fi = new FileInputStream(metadataFile);
                        String charset = FileCharsetDetector.getCharset(fi);
                        fi = new FileInputStream(metadataFile);
                        br = new BufferedReader(new InputStreamReader((InputStream)fi, charset));
                        StringBuffer sb = new StringBuffer();
                        String line = br.readLine();
                        while (line != null) {
                            sb.append(line);
                            line = br.readLine();
                        }
                        metadata = sb.toString();
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (br != null) {
                        try {
                            br.close();
                        }
                        catch (IOException e2) {
                            LOGGER.error((Object)e2);
                        }
                    }
                    if (fi == null) break block22;
                    try {
                        fi.close();
                    }
                    catch (IOException e3) {
                        LOGGER.error((Object)e3);
                    }
                }
            }
            finally {
                if (br != null) {
                    try {
                        br.close();
                    }
                    catch (IOException e) {
                        LOGGER.error((Object)e);
                    }
                }
                if (fi != null) {
                    try {
                        fi.close();
                    }
                    catch (IOException e) {
                        LOGGER.error((Object)e);
                    }
                }
            }
        }
        return metadata;
    }
}

