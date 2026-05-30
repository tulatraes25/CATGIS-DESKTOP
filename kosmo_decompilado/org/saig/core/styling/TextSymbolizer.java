/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Color;
import java.util.Map;
import org.saig.core.filter.Expression;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Font;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.ILabelResolver;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.StyleVisitor;
import org.saig.core.styling.Symbolizer;

public interface TextSymbolizer
extends Symbolizer {
    public static final String LINE_TO_LABEL_KEY = "lineToLabel";
    public static final String IGNORE_NULL_GEOMS_KEY = "ignoreLabelIfGeometriesAreNull";
    public static final String LINE_TO_LABEL_COLOR_KEY = "lineToLabelColor";
    public static final Color DEFAULT_LINE_TO_LABEL_COLOR = Color.BLACK;
    public static final String LINE_TO_LABEL_WIDTH_KEY = "lineToLabelWidth";
    public static final double DEFAULT_LINE_TO_LABEL_WIDTH = 1.0;
    public static final String LINE_TO_LABEL_DASH_KEY = "lineToLabelDash";
    public static final float[] DEFAULT_LINE_TO_LABEL_DASH = new float[]{1.0f};
    public static final String LINE_TO_LABEL_ENDING_ANCHOR_KEY = "lineToLabelEndingAnchor";
    public static final LineToLabelEndingAnchorOptions DEFAULT_LINE_TO_LABEL_ENDING_ANCHOR = LineToLabelEndingAnchorOptions.CLOSEST_POINT;
    public static final String GROUP_KEY = "group";
    public static final boolean DEFAULT_GROUP = false;
    public static final String SPACE_AROUND_KEY = "spaceAround";
    public static final int DEFAULT_SPACE_AROUND = 0;
    public static final String MAX_DISPLACEMENT_KEY = "maxDisplacement";
    public static final int DEFAULT_MAX_DISPLACEMENT = 0;
    public static final String MIN_GROUP_DISTANCE_KEY = "minGroupDistance";
    public static final int DEFAULT_MIN_GROUP_DISTANCE = -1;
    public static final String LABEL_REPEAT_KEY = "repeat";
    public static final int DEFAULT_LABEL_REPEAT = 0;
    public static final String LABEL_ALL_GROUP_KEY = "labelAllGroup";
    public static final boolean DEFAULT_LABEL_ALL_GROUP = false;
    public static final String ALLOW_OVERRUNS_KEY = "allowOverruns";
    public static final boolean DEFAULT_ALLOW_OVERRUNS = true;
    public static final String REMOVE_OVERLAPS_KEY = "removeOverlaps";
    public static final boolean DEFAULT_REMOVE_OVERLAPS = false;
    public static final String FOLLOW_LINE_KEY = "followLine";
    public static final boolean DEFAULT_FOLLOW_LINE = false;
    public static final String MAX_ANGLE_DELTA_KEY = "maxAngleDelta";
    public static final double DEFAULT_MAX_ANGLE_DELTA = 22.5;
    public static final String AUTO_WRAP_KEY = "autoWrap";
    public static final int DEFAULT_AUTO_WRAP = 0;
    public static final String FORCE_LEFT_TO_RIGHT_KEY = "forceLeftToRight";
    public static final boolean DEFAULT_FORCE_LEFT_TO_RIGHT = true;
    public static final String CONFLICT_RESOLUTION_KEY = "conflictResolution";
    public static final boolean DEFAULT_CONFLICT_RESOLUTION = true;
    public static final String GOODNESS_OF_FIT_KEY = "goodnessOfFit";
    public static final double DEFAULT_GOODNESS_OF_FIT = 0.5;
    public static final String POLYGONALIGN_KEY = "polygonAlign";
    public static final PolygonAlignOptions DEFAULT_POLYGONALIGN = PolygonAlignOptions.NONE;
    public static final String GRAPHIC_RESIZE_KEY = "graphic-resize";
    public static final GraphicResize DEFAULT_GRAPHIC_RESIZE = GraphicResize.NONE;
    public static final String GRAPHIC_MARGIN_KEY = "graphic-margin";

    public Expression getLabel();

    public void setLabel(Expression var1);

    public Font[] getFonts();

    public void setFonts(Font[] var1);

    public LabelPlacement getLabelPlacement();

    public void setLabelPlacement(LabelPlacement var1);

    public Halo getHalo();

    public void setHalo(Halo var1);

    public Fill getFill();

    public void setFill(Fill var1);

    public String getGeometryPropertyName();

    public void setGeometryPropertyName(String var1);

    public boolean isScale();

    public void setScale(boolean var1);

    public double getScaleMinValue();

    public void setScaleMinValue(double var1);

    public double getScaleMaxValue();

    public void setScaleMaxValue(double var1);

    public void setHeightAttribute(Expression var1);

    public Expression getHeightAttribute();

    @Deprecated
    public Expression getAttributeRotation();

    @Deprecated
    public void setAttributeRotation(Expression var1);

    @Override
    public void accept(StyleVisitor var1);

    public ILabelResolver getLabelResolver();

    public void setLabelResolver(ILabelResolver var1);

    public Object resolveLabel(Feature var1);

    public void setPriority(Expression var1);

    public Expression getPriority();

    public Graphic getGraphic();

    public void setGraphic(Graphic var1);

    public void addToOptions(String var1, String var2);

    public Map<String, String> getOptions();

    public void setOptions(Map<String, String> var1);

    public String getOption(String var1);

    public static enum GraphicResize {
        NONE,
        STRETCH,
        PROPORTIONAL;

    }

    public static enum LineToLabelEndingAnchorOptions {
        CENTROID,
        CENTROID_INSIDE,
        CLOSEST_POINT;

    }

    public static enum PolygonAlignOptions {
        NONE,
        ORTHO,
        MBR;

    }
}

