/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.Crs;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.repository.ICrsRepository;

public class UsrRepository
implements ICrsRepository {
    public CRSRepositoryConnection connection = new CRSRepositoryConnection();

    @Override
    public ICrs getCrs(String code) {
        ResultSet result;
        Crs crs;
        String cadWKT;
        block7: {
            cadWKT = "";
            crs = null;
            String sentence = "SELECT usr_code, usr_wkt, usr_proj, usr_geog, usr_datum FROM USR WHERE usr_code = " + code;
            this.connection.setConnectionUsr();
            result = Query.select(sentence, this.connection.getConnection());
            try {
                this.connection.shutdown();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            if (result.next()) break block7;
            return null;
        }
        try {
            cadWKT = result.getString("usr_wkt");
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        cadWKT = String.valueOf(cadWKT.substring(0, cadWKT.length() - 1)) + ", AUTHORITY[\"USR\"," + Integer.parseInt(code) + "]]";
        try {
            crs = new Crs(Integer.parseInt(code), cadWKT);
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
        return crs;
    }

    @Override
    public void close() throws Exception {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.shutdown();
        }
    }
}

