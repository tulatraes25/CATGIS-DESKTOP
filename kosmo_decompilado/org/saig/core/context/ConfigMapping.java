/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.context;

import java.io.InputStream;
import java.util.Hashtable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.saig.core.context.XMLConfigMapping;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigMapping {
    private static Logger LOGGER = Logger.getLogger(ConfigMapping.class);
    private Hashtable<String, String> metadatos;

    public ConfigMapping(InputStream path) {
        this.parsearFichero(path);
    }

    private void parsearFichero(InputStream path) {
        try {
            this.metadatos = new Hashtable();
            XMLConfigMapping parseador = new XMLConfigMapping(this.metadatos);
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(path, (DefaultHandler)parseador);
        }
        catch (SAXParseException spe) {
            LOGGER.error((Object)("\n** Parsing error, line " + spe.getLineNumber() + ", uri " + spe.getSystemId() + "   " + spe.getMessage()));
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public Hashtable<String, String> getMetadatos() {
        return this.metadatos;
    }
}

