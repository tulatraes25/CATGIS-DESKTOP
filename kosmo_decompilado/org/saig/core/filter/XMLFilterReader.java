/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.geotools.gml.GMLFilterDocument
 *  org.geotools.gml.GMLFilterGeometry
 *  org.geotools.gml.GMLHandlerGeometry
 *  org.geotools.gml.GMLHandlerJTS
 */
package org.saig.core.filter;

import java.io.IOException;
import java.io.Reader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.geotools.gml.GMLFilterDocument;
import org.geotools.gml.GMLFilterGeometry;
import org.geotools.gml.GMLHandlerGeometry;
import org.geotools.gml.GMLHandlerJTS;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFilter;
import org.saig.core.filter.FilterHandlerImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.ParserAdapter;

public class XMLFilterReader {
    protected static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.model.sld.filter.XMLFilterReader");

    public static Filter readFilter(Reader reader) throws Exception {
        InputSource requestSource = new InputSource(reader);
        FilterHandlerImpl contentHandler = new FilterHandlerImpl();
        FilterFilter filterParser = new FilterFilter(contentHandler, null);
        GMLFilterGeometry geometryFilter = new GMLFilterGeometry((GMLHandlerJTS)filterParser);
        GMLFilterDocument documentFilter = new GMLFilterDocument((GMLHandlerGeometry)geometryFilter);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ParserAdapter adapter = new ParserAdapter(parser.getParser());
            adapter.setContentHandler((ContentHandler)documentFilter);
            adapter.parse(requestSource);
            LOGGER.debug((Object)("just parsed: " + requestSource));
        }
        catch (SAXException e) {
            throw new Exception("XML getFeature request SAX parsing error", e);
        }
        catch (IOException e) {
            throw new Exception("XML get feature request input error", e);
        }
        catch (ParserConfigurationException e) {
            throw new Exception("Some sort of issue creating parser", e);
        }
        LOGGER.debug((Object)("passing filter: " + contentHandler.getFilter()));
        return contentHandler.getFilter();
    }
}

