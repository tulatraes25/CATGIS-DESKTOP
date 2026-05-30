/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.dom4j.Document
 *  org.dom4j.DocumentException
 *  org.dom4j.Element
 *  org.dom4j.io.SAXReader
 */
package org.saig.jump.widgets.cts;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.InfoSre;

public class SreXMLReader {
    private static Logger LOGGER = Logger.getLogger(SreXMLReader.class);
    private List sre;

    public SreXMLReader(InputStream path) {
        this.extraeInfoSre(path);
    }

    private void extraeInfoSre(InputStream path) {
        this.sre = new ArrayList();
        ((ArrayList)this.sre).ensureCapacity(1100);
        try {
            SAXReader xmlReader = new SAXReader();
            Document doc = xmlReader.read(path);
            Iterator srsIt = doc.getRootElement().elementIterator();
            while (srsIt.hasNext()) {
                Element elem = (Element)srsIt.next();
                InfoSre nuevoSrs = new InfoSre();
                nuevoSrs.setNombre(elem.elementText("name"));
                nuevoSrs.setCodigo(elem.elementText("code"));
                nuevoSrs.setFavorito(elem.elementText("favorite"));
                this.sre.add(nuevoSrs);
            }
        }
        catch (DocumentException e) {
            LOGGER.error((Object)I18N.getString("org.saig.jump.widgets.cts.SreXMLReader.An-error-was-produced-with-the-information-of-the-spatial-reference-systems"));
        }
    }

    public List getSre() {
        return this.sre;
    }

    public void imprimeSre() {
        for (InfoSre srs : this.sre) {
            System.out.println(String.valueOf(I18N.getString("org.saig.jump.widgets.cts.SreXMLReader.Name")) + srs.getNombre());
            System.out.println(String.valueOf(I18N.getString("org.saig.jump.widgets.cts.SreXMLReader.Code")) + srs.getCodigo());
            System.out.println(String.valueOf(I18N.getString("org.saig.jump.widgets.cts.SreXMLReader.Favourite")) + srs.getFavorito());
        }
    }

    public Iterator iterator() {
        return this.sre.iterator();
    }
}

