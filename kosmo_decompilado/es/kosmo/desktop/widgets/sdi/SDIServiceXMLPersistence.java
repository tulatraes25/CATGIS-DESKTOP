/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.dom4j.Document
 *  org.dom4j.DocumentException
 *  org.dom4j.DocumentHelper
 *  org.dom4j.Element
 *  org.dom4j.io.OutputFormat
 *  org.dom4j.io.SAXReader
 *  org.dom4j.io.XMLWriter
 */
package es.kosmo.desktop.widgets.sdi;

import es.kosmo.desktop.widgets.sdi.ServerInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class SDIServiceXMLPersistence {
    private static Logger LOGGER = Logger.getLogger(SDIServiceXMLPersistence.class);

    public static List<ServerInfo> readServers(InputStream path) {
        ArrayList<ServerInfo> serverInfoList = new ArrayList<ServerInfo>();
        try {
            SAXReader xmlReader = new SAXReader();
            Document doc = xmlReader.read(path);
            Iterator servidoresIt = doc.getRootElement().elementIterator();
            while (servidoresIt.hasNext()) {
                Element elem = (Element)servidoresIt.next();
                String name = elem.elementText("name");
                String url = elem.elementText("url");
                String description = elem.elementText("description");
                String version = elem.elementText("version");
                boolean isFavourite = Boolean.valueOf(elem.elementText("favorite"));
                ServerInfo nuevoServ = new ServerInfo(name, url, description, version, isFavourite);
                serverInfoList.add(nuevoServ);
            }
        }
        catch (DocumentException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return serverInfoList;
    }

    public static void writeServers(Collection<ServerInfo> serverList, File xmlFile) throws Exception {
        xmlFile.createNewFile();
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("services");
        for (ServerInfo serv : serverList) {
            Element elem = root.addElement("service");
            elem.addElement("name").addText(serv.getName());
            elem.addElement("favorite").addText("" + serv.isFavourite());
            elem.addElement("url").addText(serv.getUrl());
            elem.addElement("description").addText(serv.getDescription());
            if (StringUtils.isNotEmpty((String)serv.getVersion())) {
                elem.addElement("version").addText(serv.getVersion());
                continue;
            }
            elem.addElement("version");
        }
        FileOutputStream out = new FileOutputStream(xmlFile);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("ISO-8859-1");
        XMLWriter xmlWriter = new XMLWriter((OutputStream)out, format);
        xmlWriter.write(document);
        xmlWriter.flush();
        out.close();
        xmlWriter.close();
    }
}

