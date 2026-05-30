/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.dom4j.Document
 *  org.dom4j.DocumentException
 *  org.dom4j.DocumentFactory
 *  org.dom4j.Element
 *  org.dom4j.io.OutputFormat
 *  org.dom4j.io.SAXReader
 *  org.dom4j.io.XMLWriter
 */
package es.kosmo.desktop.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class Dom4JUtils {
    public static void writeXmlPrettyPrint(Element root, OutputStream out) throws IOException {
        Document doc = DocumentFactory.getInstance().createDocument();
        doc.setRootElement(root);
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter(out, format);
        writer.write(doc);
        writer.flush();
    }

    public static void writeXml(Element root, OutputStream out) throws IOException {
        Document doc = DocumentFactory.getInstance().createDocument();
        doc.setRootElement(root);
        XMLWriter writer = new XMLWriter(out);
        writer.write(doc);
        writer.flush();
    }

    public static Element readXML(File xmlFile) throws FileNotFoundException, DocumentException {
        FileInputStream is = new FileInputStream(xmlFile);
        SAXReader reader = new SAXReader(false);
        reader.setValidation(false);
        Document document = reader.read((InputStream)is);
        Element root = document.getRootElement();
        return root;
    }

    public static Element readXml(String encoding, String xml) throws UnsupportedEncodingException, DocumentException {
        ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(encoding));
        SAXReader reader = new SAXReader(false);
        reader.setValidation(false);
        Document document = reader.read((InputStream)is);
        Element root = document.getRootElement();
        return root;
    }

    public static String changeCharset(String name, String charset) {
        String nameutf8 = null;
        try {
            nameutf8 = new String(name.getBytes(), charset);
            nameutf8 = nameutf8.trim();
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return nameutf8;
    }

    public static Double getDouble(Element element, String tagName) {
        String elementText = element.elementText(tagName);
        if (elementText != null && !(elementText = elementText.trim()).isEmpty()) {
            return Double.parseDouble(elementText);
        }
        return 0.0;
    }

    public static Integer getInteger(Element element, String tagName) {
        String elementText = element.elementText(tagName);
        if (elementText != null && !elementText.isEmpty()) {
            return Integer.parseInt(elementText);
        }
        return 0;
    }
}

