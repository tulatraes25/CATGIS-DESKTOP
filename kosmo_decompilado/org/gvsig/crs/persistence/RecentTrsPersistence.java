/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.DateTime
 *  com.iver.utiles.XMLEntity
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.persistence;

import com.iver.andami.ConfigurationException;
import com.iver.utiles.DateTime;
import com.iver.utiles.XMLEntity;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import org.gvsig.crs.persistence.TrData;
import org.saig.core.util.XMLUtils;
import org.saig.jump.lang.I18N;

public class RecentTrsPersistence {
    private XMLEntity xml = null;
    private XMLEntity recentsXml = null;
    private static final String TR_RECENTS = "recentTransformations";
    private static final String TR_AUTHORITY = "authority";
    private static final String TR_NAME = "name";
    private static final String TR_CODE = "code";
    private static final String TR_CRS_SOURCE = "crsSource";
    private static final String TR_CRS_TARGET = "crsTarget";
    private static final String TR_DETAILS = "details";
    private static final String TR_DATE = "date";
    private static final String XMLCRS = "crs.xml";
    private static final Logger LOGGER = Logger.getLogger(RecentTrsPersistence.class);
    public static Object pluginClassInstance = null;

    public RecentTrsPersistence(Object pluginClassInstance) {
        try {
            this.xml = XMLUtils.persistenceFromXML(XMLCRS);
            RecentTrsPersistence.pluginClassInstance = pluginClassInstance;
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyValue(0).equals(TR_RECENTS)) {
                this.recentsXml = this.xml.getChild(child);
            }
            ++child;
        }
        if (this.recentsXml == null) {
            XMLEntity xmlEntity = new XMLEntity();
            xmlEntity.putProperty("groupName", TR_RECENTS);
            this.xml.addChild(xmlEntity);
            this.recentsXml = xmlEntity;
        }
    }

    public RecentTrsPersistence() {
        try {
            this.xml = XMLUtils.persistenceFromXML(XMLCRS);
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyValue(0).equals(TR_RECENTS)) {
                this.recentsXml = this.xml.getChild(child);
            }
            ++child;
        }
        if (this.recentsXml == null) {
            XMLEntity xmlEntity = new XMLEntity();
            xmlEntity.putProperty("groupName", TR_RECENTS);
            this.xml.addChild(xmlEntity);
            this.recentsXml = xmlEntity;
        }
    }

    public void setPersistent() {
        try {
            File xmlFile = new File(XMLCRS);
            File parentDirectory = new File(xmlFile.getAbsolutePath()).getParentFile();
            if (!xmlFile.exists() && parentDirectory.canWrite() || xmlFile.canWrite()) {
                XMLUtils.persistenceToXML(this.xml, XMLCRS);
            } else {
                LOGGER.warn((Object)I18N.getMessage("org.gvsig.crs.persistence.RecentTrsPersistence.the-persistence-file-{0}-can-not-be-written", new Object[]{xmlFile.getAbsolutePath()}));
            }
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public void setArrayOfTrData(TrData[] trs) {
        this.recentsXml.getXmlTag().removeAllXmlTag();
        int i = 0;
        while (i < trs.length) {
            this.recentsXml.addChild(this.trDataToXml(trs[i]));
            ++i;
        }
    }

    public void addTrData(TrData tr) {
        Object[] trs = this.getArrayOfTrData();
        boolean found = false;
        int i = 0;
        while (i < trs.length) {
            if (trs[i].getAuthority().equals(tr.getAuthority()) && ((TrData)trs[i]).getCode() == tr.getCode() && ((TrData)trs[i]).getCrsSource().equals(tr.getCrsSource()) && ((TrData)trs[i]).getCrsTarget().equals(tr.getCrsTarget()) && ((TrData)trs[i]).getDetails().equals(tr.getDetails())) {
                found = true;
                ((TrData)trs[i]).setName(tr.getName());
                ((TrData)trs[i]).setProperies(tr.getProperies());
                ((TrData)trs[i]).updateLastAccess();
                this.setArrayOfTrData((TrData[])trs);
            }
            ++i;
        }
        if (!found) {
            if (trs.length < 10) {
                TrData[] newTrs = new TrData[trs.length + 1];
                System.arraycopy(trs, 0, newTrs, 0, trs.length);
                newTrs[trs.length] = tr;
                this.setArrayOfTrData(newTrs);
            } else {
                Arrays.sort(trs);
                trs[0] = tr;
                this.setArrayOfTrData((TrData[])trs);
            }
        }
    }

    public TrData[] getArrayOfTrData() {
        Object[] trs = new TrData[this.recentsXml.getChildrenCount()];
        int i = 0;
        while (i < this.recentsXml.getChildrenCount()) {
            trs[i] = this.xmlToTrData(this.recentsXml.getChild(i));
            ++i;
        }
        Arrays.sort(trs);
        return trs;
    }

    public XMLEntity trDataToXml(TrData tr) {
        String dFormat = "Y-m-d H:i:s.Z";
        XMLEntity xmlEnt = new XMLEntity();
        xmlEnt.putProperty(TR_AUTHORITY, tr.getAuthority());
        xmlEnt.putProperty(TR_CODE, Integer.toString(tr.getCode()));
        xmlEnt.putProperty(TR_NAME, tr.getName());
        xmlEnt.putProperty(TR_CRS_SOURCE, tr.getCrsSource());
        xmlEnt.putProperty(TR_CRS_TARGET, tr.getCrsTarget());
        xmlEnt.putProperty(TR_DETAILS, tr.getDetails());
        xmlEnt.putProperty(TR_DATE, DateTime.dateToString((Date)tr.getDate(), (String)dFormat));
        Set keys = tr.getProperies().keySet();
        for (String next : keys) {
            xmlEnt.putProperty(next, tr.getProperies().get(next));
        }
        return xmlEnt;
    }

    public TrData xmlToTrData(XMLEntity xmlEnt) {
        String authority = "";
        int code = 0;
        String name = "";
        String crsSource = "";
        String crsTarget = "";
        String details = "";
        Date date = null;
        authority = xmlEnt.getStringProperty(TR_AUTHORITY);
        code = Integer.valueOf(xmlEnt.getStringProperty(TR_CODE));
        name = xmlEnt.getStringProperty(TR_NAME);
        crsSource = xmlEnt.getStringProperty(TR_CRS_SOURCE);
        crsTarget = xmlEnt.getStringProperty(TR_CRS_TARGET);
        details = xmlEnt.getStringProperty(TR_DETAILS);
        date = DateTime.stringToDate((String)xmlEnt.getStringProperty(TR_DATE));
        TrData trData = new TrData(authority, code, name, crsSource, crsTarget, details, date);
        Properties props = new Properties();
        int i = 0;
        while (i < xmlEnt.getPropertyCount()) {
            String property = xmlEnt.getPropertyName(i);
            if (!(property.equals(TR_AUTHORITY) || property.equals(TR_CODE) || property.equals(TR_NAME) || property.equals(TR_CRS_SOURCE) || property.equals(TR_CRS_TARGET) || property.equals(TR_DETAILS) || property.equals(TR_DATE))) {
                props.put(property, xmlEnt.getStringProperty(property));
            }
            ++i;
        }
        trData.setProperies(props);
        return trData;
    }

    public XMLEntity getXml() {
        return this.xml;
    }

    public void setXml(XMLEntity xml) {
        this.xml = xml;
    }
}

