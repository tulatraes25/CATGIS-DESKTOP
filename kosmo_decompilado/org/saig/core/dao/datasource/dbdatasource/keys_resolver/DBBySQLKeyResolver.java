/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.dom4j.Document
 *  org.dom4j.DocumentHelper
 *  org.dom4j.Element
 *  org.dom4j.io.OutputFormat
 *  org.dom4j.io.XMLWriter
 */
package org.saig.core.dao.datasource.dbdatasource.keys_resolver;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;

public class DBBySQLKeyResolver
implements IDBKeyResolver {
    private static final Logger LOGGER = Logger.getLogger(DBBySQLKeyResolver.class);
    protected String sql;
    protected Object[] defaultKey;

    public DBBySQLKeyResolver() {
    }

    public DBBySQLKeyResolver(String sql) {
        this.sql = sql;
    }

    @Override
    public synchronized Object[] getKey(Connection conn) throws Exception {
        Object[] key = null;
        Statement st = null;
        try {
            st = conn.createStatement();
            ResultSet res = st.executeQuery(this.sql);
            if (res.next()) {
                ResultSetMetaData rsMetaData = res.getMetaData();
                int n_columns = rsMetaData.getColumnCount();
                key = new Object[n_columns][1];
                int i = 0;
                while (i < key.length) {
                    String columnLabel = rsMetaData.getColumnName(i + 1);
                    Object value = res.getObject(columnLabel);
                    key[i] = new Object[]{columnLabel, value};
                    ++i;
                }
            }
            res.close();
        }
        finally {
            if (st != null) {
                st.close();
            }
        }
        return key != null ? key : this.defaultKey;
    }

    @Override
    public void setDefaultKey(Object[] defaultKey) {
        this.defaultKey = defaultKey;
    }

    @Override
    public String getSql() {
        return this.sql;
    }

    @Override
    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String toXML() {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("KEY_RESOLVER");
        root.addAttribute("type", "DBBySQLKeyResolver");
        Element sqlElm = root.addElement("SQL");
        sqlElm.setText(this.sql);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("ISO-8859-1");
        StringWriter stringWriter = new StringWriter();
        try {
            XMLWriter xmlWriter = new XMLWriter((Writer)stringWriter, format);
            xmlWriter.write(document);
            xmlWriter.flush();
            xmlWriter.close();
        }
        catch (IOException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return stringWriter.toString();
    }

    @Override
    public IDBKeyResolver clone() {
        DBBySQLKeyResolver clone = new DBBySQLKeyResolver(this.sql);
        return clone;
    }
}

