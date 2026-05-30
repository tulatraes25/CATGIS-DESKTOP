/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.XMLEntity
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.simbology;

import com.iver.andami.ConfigurationException;
import com.iver.utiles.XMLEntity;
import com.vividsolutions.jump.util.CollectionMap;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.util.XMLUtils;
import org.saig.jump.lang.I18N;

public class CustomColorSchemesXMLPersistence {
    private static final Logger LOGGER = Logger.getLogger(CustomColorSchemesXMLPersistence.class);
    private static final String XML_CUSTOM_COLOR_SCHEMES = "customColorSchemes.xml";
    private XMLEntity xml = null;
    private XMLEntity schemesXML = null;
    private static final String CUSTOM_COLOR_SCHEME = "custom_color_scheme";
    private static final String CUSTOM_COLOR_SCHEME_NAME = "name";
    private static final String CUSTOM_COLOR_SCHEME_IS_DISCRETE = "discrete";
    private static final String CUSTOM_COLOR_SCHEME_IS_RANGE = "range";
    private static final String COLOR_RED = "red";
    private static final String COLOR_GREEN = "green";
    private static final String COLOR_BLUE = "blue";
    public static Object pluginClassInstance = null;

    public CustomColorSchemesXMLPersistence(Object classInstance) {
        try {
            this.xml = XMLUtils.persistenceFromXML(XML_CUSTOM_COLOR_SCHEMES);
            pluginClassInstance = classInstance;
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyValue(0).equals(CUSTOM_COLOR_SCHEME)) {
                this.schemesXML = this.xml.getChild(child);
            }
            ++child;
        }
        if (this.schemesXML == null) {
            XMLEntity xmlEntity = new XMLEntity();
            xmlEntity.putProperty("groupName", CUSTOM_COLOR_SCHEME);
            this.xml.addChild(xmlEntity);
            this.schemesXML = xmlEntity;
        }
    }

    public CustomColorSchemesXMLPersistence() {
        try {
            this.xml = XMLUtils.persistenceFromXML(XML_CUSTOM_COLOR_SCHEMES);
        }
        catch (ConfigurationException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int child = 0;
        while (child < this.xml.getChildrenCount()) {
            if (this.xml.getChild(child).getPropertyValue(0).equals(CUSTOM_COLOR_SCHEME)) {
                this.schemesXML = this.xml.getChild(child);
            }
            ++child;
        }
        if (this.schemesXML == null) {
            XMLEntity xmlEntity = new XMLEntity();
            xmlEntity.putProperty("groupName", CUSTOM_COLOR_SCHEME);
            this.xml.addChild(xmlEntity);
            this.schemesXML = xmlEntity;
        }
    }

    public void setPersistent() {
        try {
            File xmlFile = new File(XML_CUSTOM_COLOR_SCHEMES);
            File parentDirectory = new File(xmlFile.getAbsolutePath()).getParentFile();
            if (!xmlFile.exists() && parentDirectory.canWrite() || xmlFile.canWrite()) {
                XMLUtils.persistenceToXML(this.xml, XML_CUSTOM_COLOR_SCHEMES);
            } else {
                LOGGER.warn((Object)I18N.getMessage("org.saig.jump.widgets.simbology.CustomColorSchemesXMLPersistence.the-persistence-file-{0}-can-not-be-written", new Object[]{xmlFile.getAbsolutePath()}));
            }
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public void setCustomColorSchemes(CollectionMap schemes, List<String> discreteSchemeNames, List<String> rangeSchemeNames) {
        this.schemesXML.getXmlTag().removeAllXmlTag();
        for (String schemeName : schemes.keySet()) {
            this.schemesXML.addChild(this.schemeColorsToXml(schemeName, discreteSchemeNames.contains(schemeName), rangeSchemeNames.contains(schemeName), schemes.getItems(schemeName)));
        }
    }

    public XMLEntity colorToXml(Color color) {
        XMLEntity xmlEnt = new XMLEntity();
        xmlEnt.putProperty(COLOR_RED, color.getRed());
        xmlEnt.putProperty(COLOR_GREEN, color.getGreen());
        xmlEnt.putProperty(COLOR_BLUE, color.getBlue());
        return xmlEnt;
    }

    public XMLEntity schemeColorsToXml(String schemeName, boolean isDiscrete, boolean isRange, Collection<Color> colors) {
        XMLEntity xmlEnt = new XMLEntity();
        xmlEnt.putProperty(CUSTOM_COLOR_SCHEME_NAME, schemeName);
        xmlEnt.putProperty(CUSTOM_COLOR_SCHEME_IS_DISCRETE, isDiscrete);
        xmlEnt.putProperty(CUSTOM_COLOR_SCHEME_IS_RANGE, isRange);
        for (Color currentColor : colors) {
            xmlEnt.addChild(this.colorToXml(currentColor));
        }
        return xmlEnt;
    }

    public Color xmlToColor(XMLEntity xmlEnt) {
        int red = 0;
        int green = 0;
        int blue = 0;
        red = xmlEnt.getIntProperty(COLOR_RED);
        green = xmlEnt.getIntProperty(COLOR_GREEN);
        blue = xmlEnt.getIntProperty(COLOR_BLUE);
        Color recoveredColor = new Color(red, green, blue);
        return recoveredColor;
    }

    public void xmlToSchemeColors(CollectionMap schemes, List<String> discreteSchemeNames, List<String> rangeSchemeNames, XMLEntity xmlEnt) {
        String name = xmlEnt.getStringProperty(CUSTOM_COLOR_SCHEME_NAME);
        boolean isDiscrete = xmlEnt.getBooleanProperty(CUSTOM_COLOR_SCHEME_IS_DISCRETE);
        boolean isRange = xmlEnt.getBooleanProperty(CUSTOM_COLOR_SCHEME_IS_RANGE);
        int i = 0;
        while (i < xmlEnt.getChildrenCount()) {
            Color color = this.xmlToColor(xmlEnt.getChild(i));
            schemes.addItem(name, color);
            ++i;
        }
        if (isDiscrete) {
            discreteSchemeNames.add(name);
        }
        if (isRange) {
            rangeSchemeNames.add(name);
        }
    }

    public XMLEntity getXml() {
        return this.xml;
    }

    public void setXml(XMLEntity xml) {
        this.xml = xml;
    }

    public Object[] getCustomColorSchemes() {
        Object[] restoring = new Object[3];
        CollectionMap customColorSchemes = new CollectionMap();
        ArrayList<String> discreteSchemeNames = new ArrayList<String>();
        ArrayList<String> rangeSchemeNames = new ArrayList<String>();
        int i = 0;
        while (i < this.schemesXML.getChildrenCount()) {
            this.xmlToSchemeColors(customColorSchemes, discreteSchemeNames, rangeSchemeNames, this.schemesXML.getChild(i));
            ++i;
        }
        restoring[0] = customColorSchemes;
        restoring[1] = discreteSchemeNames;
        restoring[2] = rangeSchemeNames;
        return restoring;
    }
}

