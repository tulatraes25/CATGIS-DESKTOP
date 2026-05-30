/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 *  org.dom4j.Document
 *  org.dom4j.DocumentException
 *  org.dom4j.Element
 *  org.dom4j.io.SAXReader
 */
package org.saig.core.dao.datasource.filedatasource.textballoon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.renderer.BalloonDrawer;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.saig.core.dao.datasource.AbstractTextBalloonDataSource;
import org.saig.core.model.globes.TextBalloon;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.SaveTextBalloonsAsXMLPlugIn;
import org.saig.jump.widgets.util.DialogFactory;

public class XMLTextBalloonDataSource
extends AbstractTextBalloonDataSource {
    private static final Logger LOGGER = Logger.getLogger(XMLTextBalloonDataSource.class);
    private ArrayList<TextBalloon> balloons = new ArrayList();
    private File xmlFile;

    @Override
    public void addTextBalloon(TextBalloon balloon) {
        super.addTextBalloon(balloon);
        this.balloons.add(balloon);
    }

    @Override
    public boolean commit() {
        if (this.xmlFile == null) {
            return false;
        }
        super.commit();
        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(this.xmlFile));
            output.write(this.toXML());
            output.close();
        }
        catch (IOException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return false;
        }
        return true;
    }

    public void setFileName(String filename) throws Exception {
        File f = new File(filename);
        XMLTextBalloonDataSource ds = XMLTextBalloonDataSource.parseXML(f);
        this.addTextBalloons(ds.getTextBalloons());
        this.setXmlFile(f);
    }

    public String getFileName() throws Exception {
        if (this.getXmlFile() == null) {
            DialogFactory.showWarningDialog(JUMPWorkbench.getFrameInstance(), String.valueOf(I18N.getString(this.getClass(), "text-balloons-has-not-been-saved-to-disc-and-it-is-going-to-be-done")) + ". " + I18N.getString(this.getClass(), "choose-location"), I18N.getString(this.getClass(), "choose-location"));
            XMLTextBalloonDataSource newDs = SaveTextBalloonsAsXMLPlugIn.saveBalloons(JUMPWorkbench.getFrameInstance(), this);
            return newDs.getFileName();
        }
        return this.getXmlFile().getCanonicalPath();
    }

    @Override
    public List<TextBalloon> getTextBalloons() {
        return this.balloons;
    }

    public String toXML() {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>";
        xml = String.valueOf(xml) + "<TextBalloons>";
        for (TextBalloon tb : this.balloons) {
            xml = String.valueOf(xml) + tb.toXML();
        }
        xml = String.valueOf(xml) + "</TextBalloons>";
        return xml;
    }

    public File getXmlFile() {
        return this.xmlFile;
    }

    public void setXmlFile(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    @Override
    public void addTextBalloons(Collection<TextBalloon> balloons) {
        super.addTextBalloons(balloons);
        this.balloons.addAll(balloons);
    }

    public static XMLTextBalloonDataSource parseXML(File file) throws FileNotFoundException {
        XMLTextBalloonDataSource ds = XMLTextBalloonDataSource.parseXML(new FileInputStream(file));
        ds.setXmlFile(file);
        return ds;
    }

    public static XMLTextBalloonDataSource parseXML(InputStream in) {
        SAXReader xmlReader = new SAXReader();
        XMLTextBalloonDataSource ds = null;
        try {
            Document doc = xmlReader.read(in);
            ds = new XMLTextBalloonDataSource();
            Element balloonsElement = doc.getRootElement();
            Iterator it = balloonsElement.elementIterator();
            while (it.hasNext()) {
                Element balloonElement = (Element)it.next();
                TextBalloon tb = TextBalloon.parseFromXMLElement(balloonElement);
                ds.addTextBalloon(tb);
            }
        }
        catch (DocumentException e) {
            e.printStackTrace();
        }
        return ds;
    }

    @Override
    public TextBalloon query(Coordinate c) {
        TextBalloon selectedBalloon = null;
        boolean enc = false;
        BalloonDrawer bd = BalloonDrawer.getInstance();
        Iterator<TextBalloon> it = this.getTextBalloons().iterator();
        while (it.hasNext() && !enc) {
            TextBalloon tb = it.next();
            Shape shp = bd.getBalloonShape(tb);
            if (!shp.contains(new Point2D.Double(c.x, c.y))) continue;
            selectedBalloon = tb;
            enc = true;
        }
        return selectedBalloon;
    }

    @Override
    public List<TextBalloon> query(Envelope c) {
        ArrayList<TextBalloon> list = new ArrayList<TextBalloon>();
        for (TextBalloon tb : this.getTextBalloons()) {
            if (!c.contains(tb.getBalloonEnd()) && !c.contains(tb.getBalloonTextZone())) continue;
            list.add(tb);
        }
        return list;
    }
}

