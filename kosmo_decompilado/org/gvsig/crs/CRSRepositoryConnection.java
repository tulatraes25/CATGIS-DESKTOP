/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

public class CRSRepositoryConnection {
    private static final Logger LOGGER = Logger.getLogger(CRSRepositoryConnection.class);
    private Connection connect;

    public CRSRepositoryConnection() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        }
        catch (ClassNotFoundException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public void setConnectionEPSG() {
        try {
            this.connect = DriverManager.getConnection("jdbc:hsqldb:file:crs/db_epsg", "sa", "");
        }
        catch (SQLException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
    }

    public void setConnectionIAU2000() {
        try {
            this.connect = DriverManager.getConnection("jdbc:hsqldb:file:crs/db_iau2000", "sa", "");
        }
        catch (SQLException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
    }

    public void setConnectionEsri() {
        try {
            this.connect = DriverManager.getConnection("jdbc:hsqldb:file:crs/db_esri", "sa", "");
        }
        catch (SQLException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
    }

    public void setConnectionUsr() {
        try {
            this.connect = DriverManager.getConnection("jdbc:hsqldb:file:crs/db_usr", "sa", "");
        }
        catch (SQLException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
    }

    public Connection getConnection() {
        return this.connect;
    }

    public void shutdown() throws SQLException {
        if (this.connect == null) {
            return;
        }
        Statement st = this.connect.createStatement();
        st.execute("SHUTDOWN");
        st.close();
        this.connect.close();
    }

    public boolean isClosed() throws SQLException {
        return this.connect == null || this.connect.isClosed();
    }

    public synchronized void update(String expression) throws SQLException {
        Statement st = null;
        st = this.connect.createStatement();
        int i = st.executeUpdate(expression);
        if (i == -1) {
            System.out.println("db error : " + expression);
        }
        st.close();
    }
}

