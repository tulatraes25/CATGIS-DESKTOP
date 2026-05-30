/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

import com.vividsolutions.jump.feature.FeatureCollection;
import java.io.Writer;
import java.util.List;

public interface IDxfWriter {
    public static final String DEFAULT_TEXT_HEIGHT = "1.5";
    public static final double DEFAULT_TEXT_X_DISPLACEMENT = 0.7;
    public static final double DEFAULT_TEXT_Y_DISPLACEMENT = 0.7;
    public static final String GEOMETRY_AS_BLOCK_OPTION = "GEOMETRY_AS_BLOCK";
    public static final String WRITE_FEATURE_ATTRS_AS_XDATA_OPTION = "WRITE_FEATURE_ATTRS_AS_XDATA";
    public static final String MAX_NUMBER_FEATURES_OPTION = "MAX_NUMBER_FEATURES";
    public static final String WRITE_POINT_FCS_AS_INSERTS_WITH_ATTRS_OPTION = "WRITE_POINT_FCS_AS_INSERTS_WITH_ATTRS";
    public static final String LAYER_COLORS = "LAYER_COLORS";
    public static final String LAYER_LABEL_ATTRIBUTE_NAMES = "LAYER_LABEL_ATTRIBUTE_NAMES";
    public static final String LAYER_ROTATION_ATTRIBUTE_NAMES = "LAYER_ROTATION_ATTRIBUTE_NAMES";
    public static final String LAYER_BLOCK_NAMES = "LAYER_BLOCK_NAMES";
    public static final String DXF_INSUNITS_VALUE_OPTION = "DXF_INSUNITS_VALUE";
    public static final String HANDSEED_VALUE_TO_REPLACE = "%MAX_HANDLER_VALUE%";

    public IDxfWriter newInstance(Writer var1);

    public IDxfWriter newInstance(Writer var1, String var2);

    public boolean supportsVersion(String var1);

    public void write(List<FeatureCollection> var1, String var2) throws Exception;

    public void preProcess(List<FeatureCollection> var1) throws Exception;

    public void postProcess() throws Exception;

    public void setOption(String var1, Object var2);

    public String getDescription();
}

