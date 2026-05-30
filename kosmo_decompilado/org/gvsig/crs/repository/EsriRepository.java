/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.Crs;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.repository.ICrsRepository;
import org.saig.jump.lang.I18N;

public class EsriRepository
implements ICrsRepository {
    private static final Logger LOGGER = Logger.getLogger(EsriRepository.class);
    public CRSRepositoryConnection connection = new CRSRepositoryConnection();

    @Override
    public ICrs getCrs(String code) throws CrsException {
        ResultSet result;
        Crs crs;
        String cadWKT;
        block8: {
            cadWKT = "";
            crs = null;
            String sentence = "SELECT esri_code, esri_wkt, esri_proj, esri_geog, esri_datum FROM ESRI WHERE esri_code = " + code;
            this.connection.setConnectionEsri();
            result = Query.select(sentence, this.connection.getConnection());
            if (result == null) {
                throw new CrsException(I18N.getMessage("org.gvsig.crs.repository.EsriRepository.srs-not-found-in-esri-repository-with-code-{0}", new Object[]{code}));
            }
            try {
                this.connection.shutdown();
            }
            catch (SQLException e) {
                LOGGER.error((Object)e);
            }
            if (result.next()) break block8;
            return null;
        }
        try {
            cadWKT = result.getString("esri_wkt");
        }
        catch (SQLException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
        cadWKT = String.valueOf(cadWKT.substring(0, cadWKT.length() - 1)) + ", AUTHORITY[\"ESRI\"," + Integer.parseInt(code) + "]]";
        try {
            crs = new Crs(Integer.parseInt(code), cadWKT);
        }
        catch (CrsException e) {
            LOGGER.error((Object)e);
        }
        return crs;
    }

    @Override
    public void close() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.shutdown();
        }
    }
}

