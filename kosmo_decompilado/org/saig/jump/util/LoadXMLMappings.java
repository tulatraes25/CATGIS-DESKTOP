/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.mapping.MappingException
 */
package org.saig.jump.util;

import java.io.IOException;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;

public class LoadXMLMappings {
    public static final String PROJECT_MAPPINGS_FILE = "projectMappings.xml";
    public static final String PRINT_MAPPINGS_FILE = "printMappings.xml";
    public static final String LAYER_GROUP_MAPPINGS_FILE = "layerGroupMappings.xml";
    public static final String LAYER_SIMBOLOGY_MAPPINGS_FILE = "layerSymbologyMappings.xml";
    public static final String TOPOLOGY_MAPPINGS_FILE = "topologyCheckingMappings.xml";
    public static final String JAVA2XML_MAPPINGS_FILE = "java2XMLMappings.xml";

    public static Mapping loadProjectMappings() throws IOException, MappingException {
        return LoadXMLMappings.loadMappings(PROJECT_MAPPINGS_FILE);
    }

    public static Mapping loadMappings(String mappingFileName) throws IOException, MappingException {
        Mapping mapping = new Mapping();
        mapping.setAllowRedefinitions(true);
        mapping.loadMapping(LoadXMLMappings.class.getResource(mappingFileName));
        return mapping;
    }

    public static Mapping loadPrintMappings() throws IOException, MappingException {
        Mapping mapping = new Mapping();
        mapping.loadMapping(LoadXMLMappings.class.getResource(PRINT_MAPPINGS_FILE));
        return mapping;
    }

    public static Mapping loadLayerGroupMappings() throws IOException, MappingException {
        Mapping mapping = new Mapping();
        mapping.loadMapping(LoadXMLMappings.class.getResource(LAYER_GROUP_MAPPINGS_FILE));
        return mapping;
    }

    public static Mapping loadFileMappingsFromPath(String filePath) throws IOException, MappingException {
        Mapping mapping = new Mapping();
        mapping.loadMapping(filePath);
        return mapping;
    }

    public static Mapping loadLayerSimbologyMappings() throws IOException, MappingException {
        Mapping mapping = new Mapping();
        mapping.loadMapping(LoadXMLMappings.class.getResource(LAYER_SIMBOLOGY_MAPPINGS_FILE));
        return mapping;
    }

    public static Mapping loadTopologyCheckingMappings() throws IOException, MappingException {
        Mapping mapping = new Mapping();
        mapping.loadMapping(LoadXMLMappings.class.getResource(TOPOLOGY_MAPPINGS_FILE));
        return mapping;
    }

    public static Mapping loadJava2XMLMappings() throws IOException, MappingException {
        Mapping mapping = new Mapping();
        mapping.loadMapping(LoadXMLMappings.class.getResource(JAVA2XML_MAPPINGS_FILE));
        return mapping;
    }
}

