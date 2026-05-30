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
import org.gvsig.crs.ogr.Iau2wkt;
import org.gvsig.crs.repository.ICrsRepository;
import org.saig.jump.lang.I18N;

public class Iau2000Repository
implements ICrsRepository {
    private static final Logger LOGGER = Logger.getLogger(Iau2000Repository.class);
    public CRSRepositoryConnection connection = new CRSRepositoryConnection();

    @Override
    public ICrs getCrs(String code) throws CrsException {
        String cadWKT = "";
        Crs crs = null;
        String sentence = "SELECT iau_code, iau_wkt, iau_proj, iau_geog, iau_datum FROM IAU2000 WHERE iau_code = " + code;
        this.connection.setConnectionIAU2000();
        ResultSet result = Query.select(sentence, this.connection.getConnection());
        if (result == null) {
            throw new CrsException(I18N.getMessage("org.gvsig.crs.repository.Iau2000Repository.srs-code-{0}-not-found-in-epsg-repository", new Object[]{code}));
        }
        try {
            this.connection.shutdown();
        }
        catch (SQLException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        try {
            result.next();
            cadWKT = result.getString("iau_wkt");
        }
        catch (SQLException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
        cadWKT = String.valueOf(cadWKT.substring(0, cadWKT.length() - 1)) + ", AUTHORITY[\"IAU2000\"," + Integer.parseInt(code) + "]]";
        if (cadWKT.charAt(0) == 'P') {
            Iau2wkt wk = new Iau2wkt(cadWKT);
            cadWKT = wk.getWkt();
        }
        try {
            crs = new Crs(Integer.parseInt(code), cadWKT);
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
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

