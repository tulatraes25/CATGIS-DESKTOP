/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.util;

import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.Symbolizer;

public class StyleCloner {
    private StyleFactory styleFactory;

    public StyleCloner(StyleFactory styleFactory) {
        this.styleFactory = styleFactory;
    }

    public Style clone(Style toClone) {
        Style newStyle = this.styleFactory.createStyle();
        newStyle.setAbstract(toClone.getAbstract());
        newStyle.setDefault(toClone.isDefault());
        newStyle.setName(toClone.getName());
        newStyle.setTitle(toClone.getTitle());
        newStyle.setFeatureTypeStyles(this.clone(toClone.getFeatureTypeStyles()));
        return newStyle;
    }

    public FeatureTypeStyle[] clone(FeatureTypeStyle[] toClone) {
        FeatureTypeStyle[] clones = new FeatureTypeStyle[toClone.length];
        int i = 0;
        while (i < toClone.length) {
            clones[i] = this.clone(toClone[i]);
            ++i;
        }
        return clones;
    }

    public FeatureTypeStyle clone(FeatureTypeStyle toClone) {
        FeatureTypeStyle clone = this.styleFactory.createFeatureTypeStyle(this.clone(toClone.getRules()));
        clone.setAbstract(toClone.getAbstract());
        clone.setName(toClone.getName());
        clone.setFeatureTypeName(toClone.getFeatureTypeName());
        clone.setSemantecTypeIdentifiers(toClone.getSemantecTypeIdentifiers());
        clone.setTitle(toClone.getTitle());
        return clone;
    }

    public Rule[] clone(Rule[] toClone) {
        Rule[] clones = new Rule[toClone.length];
        int i = 0;
        while (i < toClone.length) {
            clones[i] = this.clone(toClone[i]);
            ++i;
        }
        return clones;
    }

    public Rule clone(Rule toClone) {
        Rule clone = this.styleFactory.createRule();
        clone.setFilter(toClone.getFilter());
        clone.setAbstract(toClone.getAbstract());
        clone.setName(toClone.getName());
        clone.setTitle(toClone.getTitle());
        clone.setElseFilter(toClone.isElseFilter());
        clone.setLegendGraphic(toClone.getLegendGraphic());
        clone.setMaxScaleDenominator(toClone.getMaxScaleDenominator());
        clone.setMinScaleDenominator(toClone.getMinScaleDenominator());
        clone.setSymbolizers(this.clone(toClone.getSymbolizers()));
        return clone;
    }

    public Symbolizer[] clone(Symbolizer[] toClone) {
        Symbolizer[] clones = new Symbolizer[toClone.length];
        int i = 0;
        while (i < clones.length) {
            clones[i] = this.clone(toClone[i]);
            ++i;
        }
        return clones;
    }

    public Symbolizer clone(Symbolizer s) {
        Symbolizer newS = null;
        if (s != null) {
            if (s instanceof PointSymbolizer) {
                newS = this.clone((PointSymbolizer)s);
            } else if (s instanceof LineSymbolizer) {
                newS = this.clone((LineSymbolizer)s);
            } else if (s instanceof PolygonSymbolizer) {
                newS = this.clone((PolygonSymbolizer)s);
            }
        }
        return newS;
    }

    public PointSymbolizer clone(PointSymbolizer p) {
        PointSymbolizer newP = null;
        if (p != null) {
            newP = this.styleFactory.getDefaultPointSymbolizer();
            newP.setGeometryPropertyName(p.getGeometryPropertyName());
            newP.setGraphic(this.clone(p.getGraphic()));
            newP.setUnitsOfMeasurement(p.getUnitsOfMeasurement());
        }
        return newP;
    }

    public LineSymbolizer clone(LineSymbolizer l) {
        LineSymbolizer newL = null;
        if (l != null) {
            newL = this.styleFactory.getDefaultLineSymbolizer();
            newL.setGeometryPropertyName(l.getGeometryPropertyName());
            newL.setStroke(this.clone(l.getStroke()));
            newL.setUnitsOfMeasurement(l.getUnitsOfMeasurement());
        }
        return newL;
    }

    public PolygonSymbolizer clone(PolygonSymbolizer p) {
        PolygonSymbolizer newP = null;
        if (p != null) {
            newP = this.styleFactory.getDefaultPolygonSymbolizer();
            newP.setGeometryPropertyName(p.getGeometryPropertyName());
            newP.setStroke(this.clone(p.getStroke()));
            newP.setFill(this.clone(p.getFill()));
            newP.setUnitsOfMeasurement(p.getUnitsOfMeasurement());
        }
        return newP;
    }

    public Graphic clone(Graphic g) {
        Graphic newG = null;
        if (g != null) {
            newG = this.styleFactory.getDefaultGraphic();
            newG.setGeometryPropertyName(g.getGeometryPropertyName());
            if (g.getExternalGraphics() != null) {
                newG.setExternalGraphics(g.getExternalGraphics());
            }
            if (g.getMarks() != null) {
                newG.setMarks(this.clone(g.getMarks()));
            }
            newG.setOpacity(g.getOpacity());
            newG.setRotation(g.getRotation());
            newG.setSize(g.getSize());
        }
        return newG;
    }

    public Mark[] clone(Mark[] oldM) {
        Mark[] newM;
        if (oldM == null) {
            newM = null;
        } else {
            newM = new Mark[oldM.length];
            int i = 0;
            while (i < newM.length) {
                newM[i] = this.clone(oldM[i]);
                ++i;
            }
        }
        return newM;
    }

    public Mark clone(Mark oldM) {
        Mark newM = null;
        if (oldM != null) {
            newM = this.styleFactory.getDefaultMark();
            newM.setFill(oldM.getFill());
            newM.setRotation(oldM.getRotation());
            newM.setSize(oldM.getSize());
            newM.setStroke(oldM.getStroke());
            newM.setWellKnownName(oldM.getWellKnownName());
        }
        return newM;
    }

    public Fill clone(Fill oldF) {
        Fill newF = null;
        if (oldF != null) {
            newF = this.styleFactory.getDefaultFill();
            newF.setBackgroundColor(oldF.getBackgroundColor());
            newF.setColor(oldF.getColor());
            newF.setGraphicFill(this.clone(oldF.getGraphicFill()));
            newF.setOpacity(oldF.getOpacity());
        }
        return newF;
    }

    public Stroke clone(Stroke oldS) {
        Stroke newS = null;
        if (oldS != null) {
            newS = this.styleFactory.getDefaultStroke();
            newS.setColor(oldS.getColor());
            newS.setDashArray(this.clone(oldS.getDashArray()));
            newS.setDashOffset(oldS.getDashOffset());
            newS.setGraphicFill(this.clone(oldS.getGraphicFill()));
            newS.setGraphicStroke(this.clone(oldS.getGraphicStroke()));
            newS.setLineCap(oldS.getLineCap());
            newS.setLineJoin(oldS.getLineJoin());
            newS.setOpacity(oldS.getOpacity());
            newS.setWidth(oldS.getWidth());
        }
        return newS;
    }

    public float[] clone(float[] d) {
        float[] newD;
        if (d == null) {
            newD = null;
        } else {
            newD = new float[d.length];
            int i = 0;
            while (i < newD.length) {
                newD[i] = d[i];
                ++i;
            }
        }
        return newD;
    }
}

