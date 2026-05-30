/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.dao.datasource.dbdatasource.utils;

import com.vividsolutions.jump.util.FileUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.dbdatasource.utils.EpsgToOracleCodeNotFoundException;
import org.saig.core.dao.datasource.dbdatasource.utils.OracleToEpsgCodeNotFoundException;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.dbf.DBFRecordDataSource;

public class EpsgToOracleCodeConverter {
    private static final Logger LOGGER = Logger.getLogger(EpsgToOracleCodeConverter.class);
    protected static EpsgToOracleCodeConverter _instance;
    private TableRecordDataSource oraEPSGTable;
    private static final String DEFAULT_DBF_FILE_PATH = "resources/dao/oracle/ORA_EPSG.DBF";
    private static final String FIELD_EPSG = "EPSG";
    private static final String FIELD_ORACLE = "ORACLE";

    private EpsgToOracleCodeConverter() {
        try {
            File dbfFile = new File(DEFAULT_DBF_FILE_PATH);
            if (dbfFile.exists() && dbfFile.canRead()) {
                LOGGER.info((Object)("Loading Oracle to EPSG conversion table from " + dbfFile.getAbsolutePath()));
            } else {
                int len;
                LOGGER.info((Object)"Oracle to EPSG conversion table not found, using internal one");
                InputStream dfbStream = EpsgToOracleCodeConverter.class.getResourceAsStream("ORA_EPSG.DBF");
                dbfFile = FileUtil.createTemporalFile("ORA_EPSG", "DBF");
                FileOutputStream out = new FileOutputStream(dbfFile);
                byte[] buf = new byte[1024];
                while ((len = dfbStream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                dfbStream.close();
                out.close();
            }
            this.oraEPSGTable = new DBFRecordDataSource(dbfFile.getAbsolutePath());
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public static EpsgToOracleCodeConverter getInstance() {
        if (_instance == null) {
            _instance = new EpsgToOracleCodeConverter();
        }
        return _instance;
    }

    public int oracleCodeToEPSGCode(int oracleCode) throws OracleToEpsgCodeNotFoundException {
        List<Record> records = this.oraEPSGTable.getByAttribute(new String[]{FIELD_ORACLE}, new Object[]{new Double(oracleCode)});
        if (CollectionUtils.isEmpty(records)) {
            throw new OracleToEpsgCodeNotFoundException(oracleCode);
        }
        return ((Number)records.get(0).getAttribute(FIELD_EPSG)).intValue();
    }

    public int epsgCodeToOracleCode(int epsgCode) throws EpsgToOracleCodeNotFoundException {
        List<Record> records = this.oraEPSGTable.getByAttribute(new String[]{FIELD_EPSG}, new Object[]{new Double(epsgCode)});
        if (records.isEmpty()) {
            throw new EpsgToOracleCodeNotFoundException(epsgCode);
        }
        return ((Number)records.get(0).getAttribute(FIELD_ORACLE)).intValue();
    }
}

