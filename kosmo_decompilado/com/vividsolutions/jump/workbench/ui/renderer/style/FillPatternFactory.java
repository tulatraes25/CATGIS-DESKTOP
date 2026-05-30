/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jump.workbench.ui.renderer.style.WKTFillPattern;
import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;
import org.saig.core.printing.fill.HatchComposite;

public class FillPatternFactory {
    public static final String CUSTOM_FILL_PATTERNS_KEY = String.valueOf(FillPatternFactory.class.getName()) + " - CUSTOM FILL PATTERNS";
    private static HashMap<String, HatchComposite> fillPatternsMap = new HashMap();

    static {
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 2.0, false, true).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 2, 1350));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 5.0, false, true).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 5, 1350));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 8.0, false, true).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 8, 1350));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 16.0, false, true).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 16, 1350));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 2.0, true, false).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 2, 2250));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 5.0, true, false).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 5, 2250));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 8.0, true, false).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 8, 2250));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 16.0, true, false).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 16, 2250));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 5.0, true, true).getProperties().get("PATTERN WKT"), new HatchComposite(2, Color.BLACK, 5, 1350));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 8.0, true, true).getProperties().get("PATTERN WKT"), new HatchComposite(2, Color.BLACK, 8, 1350));
        fillPatternsMap.put((String)WKTFillPattern.createDiagonalStripePattern(1, 16.0, true, true).getProperties().get("PATTERN WKT"), new HatchComposite(2, Color.BLACK, 16, -200));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 2.0, true, true).getProperties().get("PATTERN WKT"), new HatchComposite(2, Color.BLACK, 2, 0));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 4.0, true, true).getProperties().get("PATTERN WKT"), new HatchComposite(2, Color.BLACK, 4, 0));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 8.0, true, true).getProperties().get("PATTERN WKT"), new HatchComposite(2, Color.BLACK, 8, 0));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 16.0, true, true).getProperties().get("PATTERN WKT"), new HatchComposite(2, Color.BLACK, 16, 0));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 2.0, true, false).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 2, 900));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 4.0, true, false).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 4, 900));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 8.0, true, false).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 8, 900));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 16.0, true, false).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 16, 900));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 2.0, false, true).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 2, 0));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 4.0, false, true).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 4, 0));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 8.0, false, true).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 8, 0));
        fillPatternsMap.put((String)WKTFillPattern.createVerticalHorizontalStripePattern(1, 16.0, false, true).getProperties().get("PATTERN WKT"), new HatchComposite(1, Color.BLACK, 16, 0));
    }

    public static Map<String, HatchComposite> getFillPatternsNameMap() {
        return fillPatternsMap;
    }

    public Paint[] createFillPatterns() {
        return new Paint[]{WKTFillPattern.createDiagonalStripePattern(1, 2.0, false, true), WKTFillPattern.createDiagonalStripePattern(1, 5.0, false, true), WKTFillPattern.createDiagonalStripePattern(1, 8.0, false, true), WKTFillPattern.createDiagonalStripePattern(1, 16.0, false, true), WKTFillPattern.createDiagonalStripePattern(1, 2.0, true, false), WKTFillPattern.createDiagonalStripePattern(1, 5.0, true, false), WKTFillPattern.createDiagonalStripePattern(1, 8.0, true, false), WKTFillPattern.createDiagonalStripePattern(1, 16.0, true, false), WKTFillPattern.createDiagonalStripePattern(1, 5.0, true, true), WKTFillPattern.createDiagonalStripePattern(1, 8.0, true, true), WKTFillPattern.createDiagonalStripePattern(1, 16.0, true, true), WKTFillPattern.createVerticalHorizontalStripePattern(1, 2.0, true, true), WKTFillPattern.createVerticalHorizontalStripePattern(1, 4.0, true, true), WKTFillPattern.createVerticalHorizontalStripePattern(1, 8.0, true, true), WKTFillPattern.createVerticalHorizontalStripePattern(1, 16.0, true, true), WKTFillPattern.createVerticalHorizontalStripePattern(1, 2.0, true, false), WKTFillPattern.createVerticalHorizontalStripePattern(1, 4.0, true, false), WKTFillPattern.createVerticalHorizontalStripePattern(1, 8.0, true, false), WKTFillPattern.createVerticalHorizontalStripePattern(1, 16.0, true, false), WKTFillPattern.createVerticalHorizontalStripePattern(1, 2.0, false, true), WKTFillPattern.createVerticalHorizontalStripePattern(1, 4.0, false, true), WKTFillPattern.createVerticalHorizontalStripePattern(1, 8.0, false, true), WKTFillPattern.createVerticalHorizontalStripePattern(1, 16.0, false, true)};
    }
}

