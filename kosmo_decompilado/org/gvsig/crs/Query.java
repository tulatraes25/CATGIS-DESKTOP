/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Query
implements Serializable {
    private static final long serialVersionUID = 1L;

    public static synchronized ResultSet select(String sentence, Connection conn) {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sentence);
            st.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
}

