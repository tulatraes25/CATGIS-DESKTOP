/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.XMLEntity
 *  com.iver.utiles.xmlEntity.generate.XmlTag
 *  org.exolab.castor.xml.MarshalException
 *  org.exolab.castor.xml.ValidationException
 */
package org.saig.core.util;

import com.iver.andami.ConfigurationException;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.xmlEntity.generate.XmlTag;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

public class XMLUtils {
    public static XMLEntity persistenceFromXML(String pluginsPersistencePath) throws ConfigurationException {
        File xml = new File(pluginsPersistencePath);
        if (xml.exists()) {
            try {
                FileReader reader = new FileReader(xml);
                XmlTag tag = (XmlTag)XmlTag.unmarshal((Reader)reader);
                return new XMLEntity(tag);
            }
            catch (Exception e) {
                throw new ConfigurationException(e);
            }
        }
        return new XMLEntity();
    }

    public static void persistenceToXML(XMLEntity entity, String path) throws ConfigurationException {
        File xml = new File(path);
        try {
            FileWriter writer = new FileWriter(xml);
            entity.getXmlTag().marshal((Writer)writer);
        }
        catch (FileNotFoundException e) {
            throw new ConfigurationException(e);
        }
        catch (MarshalException e) {
            throw new ConfigurationException(e);
        }
        catch (ValidationException e) {
            throw new ConfigurationException(e);
        }
        catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }
}

