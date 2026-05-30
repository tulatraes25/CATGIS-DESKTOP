/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.dom4j.Document
 *  org.dom4j.Element
 *  org.dom4j.io.SAXReader
 */
package org.saig.core.dao.datasource.dbdatasource.keys_resolver;

import java.io.Reader;
import java.io.StringReader;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.DBByNextGIDKeyResolver;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.DBBySQLKeyResolver;
import org.saig.core.dao.datasource.dbdatasource.keys_resolver.IDBKeyResolver;

public class KeyResolverFactory {
    private static final Logger LOGGER = Logger.getLogger(KeyResolverFactory.class);
    public static String DBBySQLKeyResolver = "DBBySQLKeyResolver";
    public static String DBByNextGIDKeyResolver = "DBByNextGIDKeyResolver";

    public static IDBKeyResolver getKeyResolver(String xml) {
        IDBKeyResolver dbKeyResolver = null;
        SAXReader xmlReader = new SAXReader();
        xmlReader.setEncoding("UTF-8");
        StringReader stringReader = new StringReader(xml);
        try {
            Document doc = xmlReader.read((Reader)stringReader);
            Element root = doc.getRootElement();
            String type = root.attribute("type").getValue();
            String sql = ((Element)root.elements().get(0)).getTextTrim();
            if (type.equals(DBBySQLKeyResolver)) {
                dbKeyResolver = new DBBySQLKeyResolver(sql);
            } else if (type.equals(DBByNextGIDKeyResolver)) {
                String pkName = root.attribute("pk_name").getValue();
                dbKeyResolver = new DBByNextGIDKeyResolver(sql, pkName);
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return dbKeyResolver;
    }
}

