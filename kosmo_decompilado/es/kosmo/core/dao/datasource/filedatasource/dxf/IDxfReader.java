/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import java.io.Reader;
import java.util.Hashtable;
import java.util.Set;

public interface IDxfReader {
    public static final String GENERATE_ONE_FC_BY_LAYER_OPTION = "GENERATE_ONE_FC_BY_LAYER";
    public static final String SCHEMA_ATTRIBUTE_NAMES_TO_UPPERCASE_OPTION = "SCHEMA_ATTRIBUTE_NAMES_TO_UPPERCASE";
    public static final String IGNORE_INSERT_ENTITIES_OPTION = "IGNORE_INSERT_ENTITIES";
    public static final String CLASSIFY_FCS_BY_GEOMETRY_TYPE_OPTION = "CLASSIFY_FCS_BY_GEOMETRY_TYPE_OPTION";
    public static final String OUTPUT_POINT_FC_SUFFIX_OPTION = "OUTPUT_POINT_FC_SUFFIX";
    public static final String OUTPUT_LINE_FC_SUFFIX_OPTION = "OUTPUT_LINE_FC_SUFFIX";
    public static final String OUTPUT_POLYGON_FC_SUFFIX_OPTION = "OUTPUT_POLYGON_FC_SUFFIX";
    public static final String BANNED_LAYER_NAMES_OPTION = "BANNED_LAYER_NAMES_FC_SUFFIX";
    public static final String IGNORE_EMPTY_DXF_LAYERS_OPTION = "IGNORE_EMPTY_DXF_LAYERS";

    public IDxfReader newInstance(Reader var1);

    public IDxfReader newInstance(Reader var1, String var2);

    public boolean supportsVersion(String var1);

    public void load() throws Exception;

    public void setOption(String var1, Object var2);

    public String getDescription();

    public FeatureCollection[] getFeatureCollections() throws Exception;

    public String getAcadVersion();

    public FeatureDataset getFeatureDataset();

    public Hashtable<String, Set<Integer>> getLayerToColor();
}

