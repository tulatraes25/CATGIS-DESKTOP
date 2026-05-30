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
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;

public class DBByNextGIDKeyResolver
implements IDBKeyResolver {
    private static final Logger LOGGER = Logger.getLogger(DBByNextGIDKeyResolver.class);
    private Number maxValue;
    private String pkName;
    private String sql;

    public DBByNextGIDKeyResolver(Connection conn, String sql, String pkName) throws Exception {
        this.pkName = pkName;
        this.sql = sql;
        this.maxValue = this.getMaxID(conn);
    }

    public DBByNextGIDKeyResolver(String sql, String pkName) {
        this.pkName = pkName;
        this.sql = sql;
    }

    private Number getMaxID(Connection conn) throws Exception {
        Number pkValue = null;
        Statement statement = null;
        try {
            Object maxPK;
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(this.sql);
            if (resultSet.next() && (maxPK = resultSet.getObject(1)) != null && Number.class.isAssignableFrom(maxPK.getClass())) {
                pkValue = (Number)resultSet.getObject(1);
            }
            if (pkValue == null) {
                pkValue = new Long(0L);
            }
            resultSet.close();
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
        return pkValue;
    }

    @Override
    public Object[] getKey(Connection conn) throws Exception {
        if (this.maxValue == null) {
            this.maxValue = this.getMaxID(conn);
        }
        this.maxValue = this.maxValue.longValue() + 1L;
        return new Object[]{new Object[]{this.pkName, this.maxValue}};
    }

    @Override
    public void setDefaultKey(Object[] defaultKey) {
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
        root.addAttribute("type", "DBByNextGIDKeyResolver");
        root.addAttribute("pk_name", this.pkName);
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
        DBByNextGIDKeyResolver clone = new DBByNextGIDKeyResolver(this.sql, this.pkName);
        return clone;
    }
}

