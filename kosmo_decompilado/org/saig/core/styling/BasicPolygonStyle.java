/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.FeatureTypeStyleImpl;
import org.saig.core.styling.Fill;
import org.saig.core.styling.FillImpl;
import org.saig.core.styling.PolygonSymbolizerImpl;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StrokeImpl;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleImpl;
import org.saig.core.styling.Symbolizer;

public class BasicPolygonStyle
extends StyleImpl
implements Style {
    public BasicPolygonStyle() {
        this(new FillImpl(), new StrokeImpl());
    }

    public BasicPolygonStyle(Fill fill, Stroke stroke) {
        PolygonSymbolizerImpl polysym = new PolygonSymbolizerImpl();
        polysym.setFill(fill);
        polysym.setStroke(stroke);
        FeatureTypeStyleImpl fts = new FeatureTypeStyleImpl();
        RuleImpl rule = new RuleImpl();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        fts.setRules(new Rule[]{rule});
        this.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
    }

    @Override
    public String getAbstract() {
        return "A simple polygon style";
    }

    @Override
    public String getName() {
        return "Default polygon style";
    }

    @Override
    public String getTitle() {
        return "Default polygon style";
    }
}

