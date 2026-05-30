/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.styling;

import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.FeatureTypeStyleImpl;
import org.saig.core.styling.LineSymbolizerImpl;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.StrokeImpl;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleImpl;
import org.saig.core.styling.Symbolizer;

public class BasicLineStyle
extends StyleImpl
implements Style {
    public BasicLineStyle() {
        this(new StrokeImpl());
    }

    public BasicLineStyle(Stroke stroke) {
        LineSymbolizerImpl linesym = new LineSymbolizerImpl();
        linesym.setStroke(stroke);
        FeatureTypeStyleImpl fts = new FeatureTypeStyleImpl();
        RuleImpl rule = new RuleImpl();
        rule.setSymbolizers(new Symbolizer[]{linesym});
        fts.setRules(new Rule[]{rule});
        this.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
    }

    @Override
    public String getAbstract() {
        return "A simple line style";
    }

    @Override
    public String getName() {
        return "Default line style";
    }

    @Override
    public String getTitle() {
        return "Default line style";
    }
}

