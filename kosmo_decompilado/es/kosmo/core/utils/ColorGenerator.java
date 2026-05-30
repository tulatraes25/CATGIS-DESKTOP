/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.utils;

import es.kosmo.core.utils.CollectionUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ColorGenerator {
    protected List<Color> inputColors = new ArrayList<Color>();
    protected Color[] colorArray = null;
    protected int steps = 0;

    public static ColorGenerator getTrafficLightColors(int steps) {
        if (steps >= 3) {
            return new ColorGenerator(steps, Color.GREEN.darker(), Color.YELLOW.brighter(), Color.RED.darker());
        }
        return ColorGenerator.getGreenToRedColors(steps);
    }

    public static ColorGenerator getGreenToRedColors(int steps) {
        return new ColorGenerator(steps, Color.GREEN.darker(), Color.RED.darker());
    }

    public static ColorGenerator getRedToGreenColors(int steps) {
        return new ColorGenerator(steps, Color.RED.darker(), Color.GREEN.darker());
    }

    public static ColorGenerator getReverseTrafficLightColors(int steps) {
        if (steps >= 3) {
            return new ColorGenerator(steps, Color.RED.darker(), Color.YELLOW.brighter(), Color.GREEN.darker());
        }
        return ColorGenerator.getRedToGreenColors(steps);
    }

    public static ColorGenerator getBlueToRedColors(int steps) {
        return new ColorGenerator(steps, Color.BLUE, Color.RED);
    }

    public ColorGenerator() {
    }

    public ColorGenerator(int steps, List<Color> colors) {
        this.steps = steps - 1;
        this.inputColors.addAll(colors);
        this.fillColorArray();
    }

    public ColorGenerator(int steps, Color[] colors) {
        this.steps = steps - 1;
        CollectionUtils.addArrayToList(this.inputColors, colors);
        this.fillColorArray();
    }

    public ColorGenerator(int steps, Color a, Color c) {
        this.steps = steps - 2;
        this.inputColors.add(a);
        this.inputColors.add(c);
        this.fillColorArray();
    }

    public ColorGenerator(int steps, Color a, Color b, Color c) {
        this.steps = steps - 2;
        this.inputColors.add(a);
        this.inputColors.add(b);
        this.inputColors.add(c);
        this.fillColorArray();
    }

    public Color[] getColorArray() {
        return this.colorArray;
    }

    public int getSteps() {
        return this.steps + 2;
    }

    public void setSteps(int steps) {
        this.steps = steps - 2;
        this.fillColorArray();
    }

    public Color getColor(int nr) {
        if (nr < 0 || nr >= this.colorArray.length) {
            return null;
        }
        return this.colorArray[nr];
    }

    protected void setColor(int nr, Color color) {
        if (nr < 0 || nr >= this.colorArray.length) {
            return;
        }
        this.colorArray[nr] = color;
    }

    protected void fillColorArray() {
        ArrayList<Color> colors = new ArrayList<Color>();
        if (this.getSteps() > this.inputColors.size()) {
            int r = 0;
            int g = 0;
            int b = 0;
            int rTarget = 0;
            int gTarget = 0;
            int bTarget = 0;
            double rStep = 0.0;
            double gStep = 0.0;
            double bStep = 0.0;
            double stepsToSwitch = (double)this.steps / (double)(this.inputColors.size() - 1);
            int currentBaseColor = 0;
            Color baseColor = this.inputColors.get(0);
            Color nextColor = this.inputColors.get(1);
            boolean switchR = false;
            boolean switchG = false;
            boolean switchB = false;
            r = baseColor.getRed();
            g = baseColor.getGreen();
            b = baseColor.getBlue();
            rStep = Math.ceil((double)(nextColor.getRed() - baseColor.getRed()) / (double)Math.round(stepsToSwitch));
            gStep = Math.ceil((double)(nextColor.getGreen() - baseColor.getGreen()) / (double)Math.round(stepsToSwitch));
            bStep = Math.ceil((double)(nextColor.getBlue() - baseColor.getBlue()) / (double)Math.round(stepsToSwitch));
            rTarget = nextColor.getRed();
            gTarget = nextColor.getGreen();
            bTarget = nextColor.getBlue();
            colors.add(baseColor);
            int i = 0;
            while (i < this.steps) {
                if (((double)r + rStep >= (double)rTarget && rStep >= 0.0 || (double)r + rStep <= (double)rTarget && rStep < 0.0) && nextColor != null) {
                    switchR = true;
                }
                if (((double)g + gStep >= (double)gTarget && gStep >= 0.0 || (double)g + gStep <= (double)gTarget && gStep < 0.0) && nextColor != null) {
                    switchG = true;
                }
                if (((double)b + bStep >= (double)bTarget && bStep >= 0.0 || (double)b + bStep <= (double)bTarget && bStep < 0.0) && nextColor != null) {
                    switchB = true;
                }
                if (switchR && switchG && switchB) {
                    stepsToSwitch = (double)(this.steps - currentBaseColor) / (double)Math.max(this.inputColors.size() - currentBaseColor, 1);
                    switchR = false;
                    switchG = false;
                    switchB = false;
                    rStep = (long)(nextColor.getRed() - baseColor.getRed()) / Math.round(stepsToSwitch);
                    rTarget = nextColor.getRed();
                    gStep = (long)(nextColor.getGreen() - baseColor.getGreen()) / Math.round(stepsToSwitch);
                    gTarget = nextColor.getGreen();
                    bStep = (long)(nextColor.getBlue() - baseColor.getBlue()) / Math.round(stepsToSwitch);
                    bTarget = nextColor.getBlue();
                    if (++currentBaseColor < this.inputColors.size()) {
                        baseColor = this.inputColors.get(currentBaseColor);
                    }
                    nextColor = currentBaseColor < this.inputColors.size() - 1 ? this.inputColors.get(currentBaseColor + 1) : null;
                }
                r += (int)Math.round(rStep);
                r = Math.max(Math.min(r, 255), 0);
                g += (int)Math.round(gStep);
                g = Math.max(Math.min(g, 255), 0);
                b += (int)Math.round(bStep);
                b = Math.max(Math.min(b, 255), 0);
                colors.add(new Color(r, g, b));
                ++i;
            }
        } else {
            int i = 0;
            while (i < this.getSteps()) {
                colors.add(this.inputColors.get(i));
                ++i;
            }
        }
        colors.add(this.inputColors.get(this.inputColors.size() - 1));
        this.colorArray = colors.toArray(new Color[0]);
    }

    public String toString() {
        return "ColorGenerator";
    }

    public Color[] getInputColorsAsArray() {
        return this.inputColors.toArray(new Color[0]);
    }

    public Collection<Color> getXMLInputColors() {
        return this.inputColors;
    }

    public void addXMLInputColor(Color color) {
        this.inputColors.add(color);
    }

    public int getStepsXML() {
        return this.steps;
    }

    public void setStepsXML(int steps) {
        this.steps = steps;
        this.fillColorArray();
    }

    public Color[] getBlackToGreyColorArray() {
        ArrayList<Color> rampColors = new ArrayList<Color>();
        Color initialColor = this.inputColors.get(0);
        Color finalColor = this.inputColors.get(1);
        rampColors.add(initialColor);
        int r = 255;
        int g = 255;
        int b = 255;
        int rStep = 255 / this.steps;
        int gStep = 255 / this.steps;
        int bStep = 255 / this.steps;
        int i = 1;
        while (i < this.steps) {
            int newR = r - i * rStep;
            int newG = g - i * gStep;
            int newB = b - i * bStep;
            Color newColor = new Color(newR, newG, newB);
            rampColors.add(newColor);
            ++i;
        }
        rampColors.add(finalColor);
        return rampColors.toArray(new Color[0]);
    }

    public Color[] getTwoColorArray() {
        ArrayList<Color> rampColors = new ArrayList<Color>();
        Color initialColor = this.inputColors.get(0);
        Color finalColor = this.inputColors.get(1);
        rampColors.add(initialColor);
        int r = initialColor.getRed();
        int g = initialColor.getGreen();
        int b = initialColor.getBlue();
        int rStep = 255 / this.steps;
        int gStep = 255 / this.steps;
        int bStep = 255 / this.steps;
        int i = 1;
        while (i < this.steps) {
            int newR = r - i * rStep;
            int newG = g - i * gStep;
            int newB = b - i * bStep;
            Color newColor = new Color(newR, newG, newB);
            rampColors.add(newColor);
            ++i;
        }
        rampColors.add(finalColor);
        return rampColors.toArray(new Color[0]);
    }
}

