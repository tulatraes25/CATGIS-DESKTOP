/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import java.awt.geom.GeneralPath;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.saig.core.renderer.style.DrawnMarkStep;
import org.saig.core.renderer.style.DrawnMarkStepEllipse;
import org.saig.core.renderer.style.DrawnMarkStepLineTo;
import org.saig.core.renderer.style.DrawnMarkStepMoveTo;
import org.saig.core.renderer.style.DrawnMarkStepRectangle;
import org.saig.core.renderer.style.DrawnMarkStepText;

public class DrawnMark {
    private List<DrawnMarkStep> steps = new ArrayList<DrawnMarkStep>();
    public static final String MOVETO = "MOVETO";
    public static final String LINETO = "LINETO";
    public static final String TEXT = "TEXT";
    public static final String ELLIPSE = "ELLIPSE";
    public static final String RECTANGLE = "RECTANGLE";

    public DrawnMark() {
    }

    public DrawnMark(String file) throws Exception {
        BufferedReader in = new BufferedReader(new FileReader("symbol/" + file));
        String line = in.readLine();
        while (line != null) {
            this.parseLine(line);
            line = in.readLine();
        }
        in.close();
    }

    public void parseLine(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        String comand = tokenizer.nextToken();
        if (comand.equalsIgnoreCase(MOVETO)) {
            Float x = new Float(tokenizer.nextToken());
            Float y = new Float(tokenizer.nextToken());
            this.steps.add(new DrawnMarkStepMoveTo(0, new Object[]{x, y}));
        } else if (comand.equalsIgnoreCase(LINETO)) {
            Float x = new Float(tokenizer.nextToken());
            Float y = new Float(tokenizer.nextToken());
            this.steps.add(new DrawnMarkStepLineTo(0, new Object[]{x, y}));
        } else if (comand.equalsIgnoreCase(TEXT)) {
            String text = tokenizer.nextToken();
            Float size = new Float(tokenizer.nextToken());
            Float x = new Float(tokenizer.nextToken());
            Float y = new Float(tokenizer.nextToken());
            this.steps.add(new DrawnMarkStepText(0, new Object[]{text, size, x, y}));
        } else if (comand.equalsIgnoreCase(ELLIPSE)) {
            Float x = new Float(tokenizer.nextToken());
            Float y = new Float(tokenizer.nextToken());
            Float w = new Float(tokenizer.nextToken());
            Float h = new Float(tokenizer.nextToken());
            this.steps.add(new DrawnMarkStepEllipse(0, new Object[]{x, y, w, h}));
        } else if (comand.equalsIgnoreCase(RECTANGLE)) {
            Float x = new Float(tokenizer.nextToken());
            Float y = new Float(tokenizer.nextToken());
            Float w = new Float(tokenizer.nextToken());
            Float h = new Float(tokenizer.nextToken());
            this.steps.add(new DrawnMarkStepRectangle(0, new Object[]{x, y, w, h}));
        }
    }

    public void paint(GeneralPath g) {
        for (DrawnMarkStep step : this.steps) {
            step.paint(g);
        }
    }
}

