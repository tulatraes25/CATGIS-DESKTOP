package ar.com.catgis.renderer;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Mapnik-style compositing and blending modes for cartographic effects.
 * Multiply, screen, overlay, soft-light, and other blend modes.
 */
public final class CompositingRenderer {

    private CompositingRenderer() {}

    /**
     * Blend two images using a specified mode.
     *
     * @param base     base layer (bottom)
     * @param overlay  overlay layer (top)
     * @param mode     blend mode
     * @param opacity  overlay opacity (0..1)
     * @return blended image (same size as base)
     */
    public static BufferedImage blend(BufferedImage base, BufferedImage overlay,
                                       BlendMode mode, float opacity) {
        if (base == null || overlay == null) return base;
        int w = Math.min(base.getWidth(), overlay.getWidth());
        int h = Math.min(base.getHeight(), overlay.getHeight());
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int[] basePx = new int[w];
        int[] overPx = new int[w];

        for (int y = 0; y < h; y++) {
            base.getRGB(0, y, w, 1, basePx, 0, w);
            overlay.getRGB(0, y, w, 1, overPx, 0, w);
            for (int x = 0; x < w; x++) {
                int b = basePx[x];
                int o = overPx[x];
                int blended = blendPixel(b, o, mode, opacity);
                result.setRGB(x, y, blended);
            }
        }
        return result;
    }

    private static int blendPixel(int base, int overlay, BlendMode mode, float opacity) {
        int ba = (base >> 24) & 0xFF;
        int br = (base >> 16) & 0xFF;
        int bg = (base >> 8) & 0xFF;
        int bb = base & 0xFF;

        int oa = (overlay >> 24) & 0xFF;
        int or = (overlay >> 16) & 0xFF;
        int og = (overlay >> 8) & 0xFF;
        int ob = overlay & 0xFF;

        float oaf = (oa / 255f) * opacity;
        if (oaf <= 0) return base;

        int fr, fg, fb;
        float fbFloat = br / 255f, fgFloat = bg / 255f, foFloat;

        switch (mode) {
            case MULTIPLY:
                fr = (int) (br * (or / 255f * oaf + (1 - oaf)));
                fg = (int) (bg * (og / 255f * oaf + (1 - oaf)));
                fb = (int) (bb * (ob / 255f * oaf + (1 - oaf)));
                break;
            case SCREEN:
                fr = 255 - (int) ((255 - br) * ((255 - or) / 255f * oaf + (1 - oaf)));
                fg = 255 - (int) ((255 - bg) * ((255 - og) / 255f * oaf + (1 - oaf)));
                fb = 255 - (int) ((255 - bb) * ((255 - ob) / 255f * oaf + (1 - oaf)));
                break;
            case OVERLAY:
                foFloat = or / 255f;
                fr = fbFloat < 0.5f
                        ? (int) (2 * br * (foFloat * oaf + (1 - oaf)))
                        : (int) (255 - 2 * (255 - br) * ((1 - foFloat) * oaf + (1 - oaf)));
                fbFloat = br / 255f;
                foFloat = og / 255f;
                fgFloat = bg / 255f;
                fg = fgFloat < 0.5f
                        ? (int) (2 * bg * (foFloat * oaf + (1 - oaf)))
                        : (int) (255 - 2 * (255 - bg) * ((1 - foFloat) * oaf + (1 - oaf)));
                fbFloat = bb / 255f;
                foFloat = ob / 255f;
                fb = fbFloat < 0.5f
                        ? (int) (2 * bb * (foFloat * oaf + (1 - oaf)))
                        : (int) (255 - 2 * (255 - bb) * ((1 - foFloat) * oaf + (1 - oaf)));
                break;
            case DARKEN:
                fr = (int) (Math.min(br, or * oaf + br * (1 - oaf)));
                fg = (int) (Math.min(bg, og * oaf + bg * (1 - oaf)));
                fb = (int) (Math.min(bb, ob * oaf + bb * (1 - oaf)));
                break;
            case LIGHTEN:
                fr = (int) (Math.max(br, or * oaf + br * (1 - oaf)));
                fg = (int) (Math.max(bg, og * oaf + bg * (1 - oaf)));
                fb = (int) (Math.max(bb, ob * oaf + bb * (1 - oaf)));
                break;
            case ADD:
                fr = Math.min(255, (int) (br + or * oaf));
                fg = Math.min(255, (int) (bg + og * oaf));
                fb = Math.min(255, (int) (bb + ob * oaf));
                break;
            case SOFT_LIGHT:
                fr = softLight(br, or, oaf);
                fg = softLight(bg, og, oaf);
                fb = softLight(bb, ob, oaf);
                break;
            case DIFFERENCE:
                fr = (int) (Math.abs(br - or * oaf) + (1 - oaf) * br);
                fg = (int) (Math.abs(bg - og * oaf) + (1 - oaf) * bg);
                fb = (int) (Math.abs(bb - ob * oaf) + (1 - oaf) * bb);
                break;
            default: // NORMAL
                fr = (int) (br * (1 - oaf) + or * oaf);
                fg = (int) (bg * (1 - oaf) + og * oaf);
                fb = (int) (bb * (1 - oaf) + ob * oaf);
                break;
        }

        int fa = Math.min(255, ba + (int) (oa * opacity));
        fr = clamp(fr);
        fg = clamp(fg);
        fb = clamp(fb);

        return (fa << 24) | (fr << 16) | (fg << 8) | fb;
    }

    private static int softLight(int base, int blend, float opacity) {
        float b = base / 255f;
        float s = blend / 255f;
        float result;
        if (s < 0.5f) {
            result = b - (1 - 2 * s) * b * (1 - b);
        } else {
            result = b + (2 * s - 1) * ((b <= 0.25f
                    ? ((16 * b - 12) * b + 4) * b
                    : (float) Math.sqrt(b)) - b);
        }
        return clamp((int) (result * 255 * opacity + base * (1 - opacity)));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /**
     * Blend modes matching Mapnik/Photoshop conventions.
     */
    public enum BlendMode {
        NORMAL, MULTIPLY, SCREEN, OVERLAY, DARKEN, LIGHTEN, ADD, SOFT_LIGHT, DIFFERENCE
    }

    /**
     * Create a relief shading overlay from a hillshade.
     * Blends hillshade with terrain color using the MULTIPLY mode.
     */
    public static BufferedImage reliefShading(BufferedImage terrainColors, BufferedImage hillshade, float opacity) {
        return blend(terrainColors, hillshade, BlendMode.MULTIPLY, opacity);
    }
}
