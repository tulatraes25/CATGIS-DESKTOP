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
import org.gvsig.crs.persistence.CrsData;
import org.saig.core.util.XMLUtils;
import org.saig.jump.lang.I18N;

public class RecentCRSsPersistence {
    private static final Logger LOGGER = Logger.getLogger(RecentCRSsPersistence.class);
    private static final String XMLCRS = "crs.xml";
    private XMLEntity xml = null;
    private XMLEntity recentsXml = null;
    private static final String CRS_RECENTS = "recentCrss";
    private static final String CRS_AUTHORITY = "authority";
    private static final String CRS_NAME = "name";
    private static final String CRS_CODE = "code";
    private static final String CRS_DATE = "date";
    public static Object pluginClassInstance = null;

    public RecentCRSsPersistence(Object pluginClassInstance) {
        try {
            this.xml = XMLUtils.persistenceFromXML(XMLCRS);
            RecentCRSsPersistence.pluginClassInstance = pluginClassInstance;
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyValue(0).equals(CRS_RECENTS)) {
                this.recentsXml = this.xml.getChild(child);
            }
            ++child;
        }
        if (this.recentsXml == null) {
            XMLEntity xmlEntity = new XMLEntity();
            xmlEntity.putProperty("groupName", CRS_RECENTS);
            this.xml.addChild(xmlEntity);
            this.recentsXml = xmlEntity;
        }
    }

    public RecentCRSsPersistence() {
        try {
            this.xml = XMLUtils.persistenceFromXML(XMLCRS);
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyValue(0).equals(CRS_RECENTS)) {
                this.recentsXml = this.xml.getChild(child);
            }
            ++child;
        }
        if (this.recentsXml == null) {
            XMLEntity xmlEntity = new XMLEntity();
            xmlEntity.putProperty("groupName", CRS_RECENTS);
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
                LOGGER.warn((Object)I18N.getMessage("org.gvsig.crs.persistence.RecentCRSsPersistence.the-persistence-file-{0}-can-not-be-written", new Object[]{xmlFile.getAbsolutePath()}));
            }
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public void setArrayOfCrsData(CrsData[] crss) {
        this.recentsXml.getXmlTag().removeAllXmlTag();
        int i = 0;
        while (i < crss.length) {
            this.recentsXml.addChild(this.crsDataToXml(crss[i]));
            ++i;
        }
    }

    public void addCrsData(CrsData crs) {
        Object[] crss = this.getArrayOfCrsData();
        boolean found = false;
        int i = 0;
        while (i < crss.length) {
            if (crss[i].getAuthority().equals(crs.getAuthority()) && ((CrsData)crss[i]).getCode() == crs.getCode()) {
                found = true;
                ((CrsData)crss[i]).setName(crs.getName());
                ((CrsData)crss[i]).setProperies(crs.getProperies());
                ((CrsData)crss[i]).updateLastAccess();
                this.setArrayOfCrsData((CrsData[])crss);
            }
            ++i;
        }
        if (!found) {
            if (crss.length < 10) {
                CrsData[] newCrss = new CrsData[crss.length + 1];
                System.arraycopy(crss, 0, newCrss, 0, crss.length);
                newCrss[crss.length] = crs;
                this.setArrayOfCrsData(newCrss);
            } else {
                Arrays.sort(crss);
                crss[0] = crs;
                this.setArrayOfCrsData((CrsData[])crss);
            }
        }
    }

    public CrsData[] getArrayOfCrsData() {
        Object[] crss = new CrsData[this.recentsXml.getChildrenCount()];
        int i = 0;
        while (i < this.recentsXml.getChildrenCount()) {
            crss[i] = this.xmlToCrsData(this.recentsXml.getChild(i));
            ++i;
        }
        Arrays.sort(crss);
        return crss;
    }

    public XMLEntity crsDataToXml(CrsData crs) {
        String dFormat = "Y-m-d H:i:s.Z";
        XMLEntity xmlEnt = new XMLEntity();
        xmlEnt.putProperty(CRS_AUTHORITY, crs.getAuthority());
        xmlEnt.putProperty(CRS_CODE, Integer.toString(crs.getCode()));
        xmlEnt.putProperty(CRS_NAME, crs.getName());
        xmlEnt.putProperty(CRS_DATE, DateTime.dateToString((Date)crs.getDate(), (String)dFormat));
        Set keys = crs.getProperies().keySet();
        for (String next : keys) {
            xmlEnt.putProperty(next, crs.getProperies().get(next));
        }
        return xmlEnt;
    }

    public CrsData xmlToCrsData(XMLEntity xmlEnt) {
        String authority = "";
        int code = 0;
        String name = "";
        Date date = null;
        authority = xmlEnt.getStringProperty(CRS_AUTHORITY);
        code = Integer.valueOf(xmlEnt.getStringProperty(CRS_CODE));
        name = xmlEnt.getStringProperty(CRS_NAME);
        date = DateTime.stringToDate((String)xmlEnt.getStringProperty(CRS_DATE));
        CrsData crsData = new CrsData(authority, code, name, date);
        Properties props = new Properties();
        int i = 0;
        while (i < xmlEnt.getPropertyCount()) {
            String property = xmlEnt.getPropertyName(i);
            if (!(property.equals(CRS_AUTHORITY) || property.equals(CRS_CODE) || property.equals(CRS_NAME) || property.equals(CRS_DATE))) {
                props.put(property, xmlEnt.getStringProperty(property));
            }
            ++i;
        }
        crsData.setProperies(props);
        return crsData;
    }

    public XMLEntity getXml() {
        return this.xml;
    }

    public void setXml(XMLEntity xml) {
        this.xml = xml;
    }
}

