/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.geotools.data.DataSourceException
 */
package org.geotools.data.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.geotools.data.DataSourceException;

class FIDSequence {
    private int current;

    FIDSequence(Connection conn, String tablename, String fidColumn) throws DataSourceException {
        block12: {
            ResultSet resultSet = null;
            try {
                try {
                    String query = "SELECT max(" + fidColumn + ") FROM " + tablename;
                    Statement statement = conn.createStatement();
                    resultSet = statement.executeQuery(query);
                    if (resultSet.next()) {
                        Number number = (Number)resultSet.getObject(1);
                        this.current = number.intValue();
                        break block12;
                    }
                    throw new DataSourceException("Could not get max for " + fidColumn);
                }
                catch (SQLException e) {
                    throw new DataSourceException("SQL Error occured when generating unique key", (Throwable)e);
                }
                catch (ClassCastException e) {
                    throw new DataSourceException("Error casting fid column to a number", (Throwable)e);
                }
            }
            finally {
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
                catch (SQLException sQLException) {}
            }
        }
    }

    int getNext() {
        ++this.current;
        return this.current;
    }
}

