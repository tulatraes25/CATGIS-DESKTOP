/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultiColorsGenerator {
    private static final double MINIMUN_HSB_DISTANCE = 40.0;
    private static final boolean CHECK_MIN_DISTANCE = true;
    private static final double MINIMUN_S = 0.3;
    private static final double MINIMUN_B = 0.3;
    private static final boolean CHECK_MAX_SB = true;
    private static final double MAXIMUN_S = 0.98;
    private static final double MAXIMUN_B = 0.98;
    private static final boolean CHECK_MIN_SB = true;
    private static final int MAX_ATTEMPT_BEFORE_RESET = 10000;
    private static final int MAX_DARK_SCALE = 4;
    private static MultiColorsGenerator instance;
    private static final Color[] BASE_COLORS_ARRAY;
    private List<Color> generatedColorsList;
    private Random randomNumberGenerator;
    private int currentDarkScale;

    static {
        BASE_COLORS_ARRAY = new Color[]{new Color(0, 0, 240), new Color(240, 0, 0), new Color(0, 200, 0), new Color(128, 0, 255), new Color(255, 128, 0), new Color(0, 240, 240), new Color(128, 255, 0), new Color(255, 0, 255), new Color(150, 75, 0), new Color(200, 200, 0), new Color(0, 0, 0)};
    }

    private MultiColorsGenerator() {
        this.initialize();
    }

    public static MultiColorsGenerator getInstance() {
        if (instance == null) {
            instance = new MultiColorsGenerator();
        }
        return instance;
    }

    private void initialize() {
        this.generatedColorsList = new ArrayList<Color>();
        this.randomNumberGenerator = new Random();
        this.currentDarkScale = 1;
    }

    public Color generateColor() {
        Color color = Color.BLACK;
        if (this.generatedColorsList.size() < BASE_COLORS_ARRAY.length) {
            color = BASE_COLORS_ARRAY[this.generatedColorsList.size()];
        } else {
            int cont;
            boolean validColor = false;
            while (this.currentDarkScale <= 4 && !validColor) {
                cont = 0;
                while (cont < BASE_COLORS_ARRAY.length && !validColor) {
                    Color darkColor = BASE_COLORS_ARRAY[cont];
                    int dCount = 0;
                    while (dCount < this.currentDarkScale) {
                        darkColor = darkColor.darker();
                        ++dCount;
                    }
                    validColor = this.isValidColor(darkColor);
                    if (validColor) {
                        color = darkColor;
                    }
                    ++cont;
                }
                ++this.currentDarkScale;
            }
            if (!validColor) {
                cont = 0;
                while (cont < 10000 && !validColor) {
                    Color randomColor = new Color(this.randomNumberGenerator.nextInt(256), this.randomNumberGenerator.nextInt(256), this.randomNumberGenerator.nextInt(256));
                    validColor = this.isValidColor(randomColor);
                    if (validColor) {
                        color = randomColor;
                    }
                    ++cont;
                }
            }
            if (!validColor) {
                this.initialize();
                color = this.generateColor();
            }
        }
        this.generatedColorsList.add(new Color(color.getRed(), color.getGreen(), color.getBlue()));
        return color;
    }

    private boolean isValidColor(Color color) {
        if (color == null) {
            return false;
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
        if ((double)hsb[1] < 0.3 || (double)hsb[2] < 0.3) {
            return false;
        }
        if ((double)hsb[1] > 0.98 || (double)hsb[2] > 0.98) {
            return false;
        }
        for (Color genColor : this.generatedColorsList) {
            if (genColor.equals(color)) {
                return false;
            }
            float[] genHsb = Color.RGBtoHSB(genColor.getRed(), genColor.getGreen(), genColor.getBlue(), new float[3]);
            if (!((double)Math.abs(genHsb[0] - hsb[0]) * 255.0 + (double)Math.abs(genHsb[1] - hsb[1]) * 100.0 + (double)(Math.abs(genHsb[2] - hsb[2]) * 100.0f) < 40.0)) continue;
            return false;
        }
        return true;
    }
}

