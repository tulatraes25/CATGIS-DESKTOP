/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.digester.Digester
 *  org.apache.commons.digester.xmlrules.DigesterLoader
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management.definition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;
import org.saig.core.model.data.widgets.tables.management.definition.DefinitionMapping;
import org.saig.core.model.data.widgets.tables.management.definition.TableDef;
import org.xml.sax.SAXException;

public class DefinitionMappingHelper {
    public static final Logger LOGGER = Logger.getLogger(DefinitionMappingHelper.class);
    private DefinitionMapping definitionMapping = null;
    private String definitionMappingLocation = "org/saig/core/model/data/widgets/tables/management/definition/definitionMapping.xml";
    private InputStream definitionMappingXml;

    public DefinitionMapping getDefinitionMapping() {
        if (this.definitionMapping == null) {
            this.definitionMapping = this.loadDefinitionMapping();
        }
        return this.definitionMapping;
    }

    private DefinitionMapping loadDefinitionMapping() {
        try {
            ClassLoader classLoader = DefinitionMappingHelper.class.getClassLoader();
            if (this.definitionMappingXml == null) {
                this.definitionMappingXml = classLoader.getResourceAsStream(this.definitionMappingLocation);
            }
            URL digesterRules = classLoader.getResource("org/saig/core/model/data/widgets/tables/management/definition/definitionMappingRules.xml");
            Digester digester = DigesterLoader.createDigester((URL)digesterRules);
            return (DefinitionMapping)digester.parse(this.definitionMappingXml);
        }
        catch (IOException ioEx) {
            LOGGER.error((Object)"", (Throwable)ioEx);
            return null;
        }
        catch (SAXException saxEx) {
            LOGGER.error((Object)"", (Throwable)saxEx);
            return null;
        }
    }

    public TableDef getPage(String name) {
        DefinitionMapping refrescoMapping = this.getDefinitionMapping();
        return refrescoMapping.getTable(name);
    }

    public static void main(String[] args) {
        DefinitionMappingHelper.testLoadXML();
    }

    private static void testLoadXML() {
        try {
            DefinitionMappingHelper definitionMappingHelper = new DefinitionMappingHelper();
            DefinitionMapping definitionMapping = definitionMappingHelper.getDefinitionMapping();
            TableDef o = definitionMapping.getTable("Municipios");
            boolean bl = false;
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public void setDefinitionMappingLocation(String definitionMappingLocation) {
        this.definitionMappingLocation = definitionMappingLocation;
    }

    public void setDefinitionMappingXml(InputStream definitionMappingXml) {
        this.definitionMappingXml = definitionMappingXml;
    }
}

