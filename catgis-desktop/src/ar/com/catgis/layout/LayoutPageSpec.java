package ar.com.catgis.layout;

import java.util.Locale;
import java.util.Objects;

/**
 * Document page geometry for CATMAP: standard preset or custom millimetres,
 * orientation and print margins.
 *
 * <p>This is document state (it travels with the layout), unlike the legacy
 * fixed A4/landscape constants that used to live inside each renderer.</p>
 */
public final class LayoutPageSpec {

    public static final double MIN_DIMENSION_MM = 10.0;
    public static final double MAX_DIMENSION_MM = 5000.0;
    public static final String CUSTOM_PRESET = "CUSTOM";

    /** Legacy default used before page geometry became document state. */
    public static final LayoutPageSpec A4_LANDSCAPE =
            new LayoutPageSpec("A4", 210.0, 297.0, PageOrientation.LANDSCAPE, 0, 0, 0, 0);

    private final String presetName;
    private final double portraitWidthMm;
    private final double portraitHeightMm;
    private final PageOrientation orientation;
    private final double marginTopMm;
    private final double marginRightMm;
    private final double marginBottomMm;
    private final double marginLeftMm;

    private LayoutPageSpec(String presetName,
                           double portraitWidthMm,
                           double portraitHeightMm,
                           PageOrientation orientation,
                           double marginTopMm,
                           double marginRightMm,
                           double marginBottomMm,
                           double marginLeftMm) {
        this.presetName = Objects.requireNonNull(presetName, "CATMAP page preset");
        this.portraitWidthMm = requireDimension(portraitWidthMm, "page width");
        this.portraitHeightMm = requireDimension(portraitHeightMm, "page height");
        this.orientation = Objects.requireNonNull(orientation, "CATMAP page orientation");
        this.marginTopMm = requireMargin(marginTopMm, "top margin");
        this.marginRightMm = requireMargin(marginRightMm, "right margin");
        this.marginBottomMm = requireMargin(marginBottomMm, "bottom margin");
        this.marginLeftMm = requireMargin(marginLeftMm, "left margin");
        if (marginLeftMm + marginRightMm >= widthMm()
                || marginTopMm + marginBottomMm >= heightMm()) {
            throw new IllegalArgumentException("CATMAP page margins leave no printable area.");
        }
    }

    public static LayoutPageSpec preset(PageSizePreset preset, PageOrientation orientation) {
        Objects.requireNonNull(preset, "CATMAP page preset");
        return new LayoutPageSpec(preset.name(), preset.widthMm, preset.heightMm,
                orientation, 0, 0, 0, 0);
    }

    /**
     * Build the closest spec for a stored portrait sheet size: a standard preset
     * when the millimetres match it, otherwise a custom sheet.
     */
    public static LayoutPageSpec fromPortraitDimensions(double portraitWidthMm,
                                                        double portraitHeightMm,
                                                        PageOrientation orientation) {
        for (PageSizePreset preset : PageSizePreset.values()) {
            if (Math.abs(preset.widthMm - portraitWidthMm) <= 0.01
                    && Math.abs(preset.heightMm - portraitHeightMm) <= 0.01) {
                return preset(preset, orientation);
            }
        }
        return custom(portraitWidthMm, portraitHeightMm, orientation);
    }

    public static LayoutPageSpec custom(double widthMm, double heightMm,
                                        PageOrientation orientation) {
        return custom(widthMm, heightMm, orientation, 0, 0, 0, 0);
    }

    public static LayoutPageSpec custom(double widthMm, double heightMm,
                                        PageOrientation orientation,
                                        double marginTopMm, double marginRightMm,
                                        double marginBottomMm, double marginLeftMm) {
        return new LayoutPageSpec(CUSTOM_PRESET, widthMm, heightMm, orientation,
                marginTopMm, marginRightMm, marginBottomMm, marginLeftMm);
    }

    public LayoutPageSpec withOrientation(PageOrientation newOrientation) {
        return new LayoutPageSpec(presetName, portraitWidthMm, portraitHeightMm,
                newOrientation, marginTopMm, marginRightMm, marginBottomMm, marginLeftMm);
    }

    public LayoutPageSpec withMargins(double topMm, double rightMm,
                                      double bottomMm, double leftMm) {
        return new LayoutPageSpec(presetName, portraitWidthMm, portraitHeightMm,
                orientation, topMm, rightMm, bottomMm, leftMm);
    }

    public String presetName() {
        return presetName;
    }

    public boolean isCustom() {
        return CUSTOM_PRESET.equals(presetName);
    }

    public PageOrientation orientation() {
        return orientation;
    }

    public boolean isLandscape() {
        return orientation == PageOrientation.LANDSCAPE;
    }

    /** Base portrait width, independent of the selected orientation. */
    public double portraitWidthMm() {
        return portraitWidthMm;
    }

    /** Base portrait height, independent of the selected orientation. */
    public double portraitHeightMm() {
        return portraitHeightMm;
    }

    /** Effective page width after applying the orientation. */
    public double widthMm() {
        return isLandscape() ? portraitHeightMm : portraitWidthMm;
    }

    /** Effective page height after applying the orientation. */
    public double heightMm() {
        return isLandscape() ? portraitWidthMm : portraitHeightMm;
    }

    public double marginTopMm() {
        return marginTopMm;
    }

    public double marginRightMm() {
        return marginRightMm;
    }

    public double marginBottomMm() {
        return marginBottomMm;
    }

    public double marginLeftMm() {
        return marginLeftMm;
    }

    public boolean hasMargins() {
        return marginTopMm > 0 || marginRightMm > 0
                || marginBottomMm > 0 || marginLeftMm > 0;
    }

    private static double requireDimension(double value, String label) {
        if (!Double.isFinite(value) || value < MIN_DIMENSION_MM || value > MAX_DIMENSION_MM) {
            throw new IllegalArgumentException("CATMAP " + label
                    + " must be finite and between " + MIN_DIMENSION_MM
                    + " and " + MAX_DIMENSION_MM + " mm: " + value);
        }
        return value;
    }

    private static double requireMargin(double value, String label) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException("CATMAP " + label
                    + " must be finite and non-negative: " + value);
        }
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LayoutPageSpec spec)) {
            return false;
        }
        return presetName.equals(spec.presetName)
                && Double.compare(portraitWidthMm, spec.portraitWidthMm) == 0
                && Double.compare(portraitHeightMm, spec.portraitHeightMm) == 0
                && orientation == spec.orientation
                && Double.compare(marginTopMm, spec.marginTopMm) == 0
                && Double.compare(marginRightMm, spec.marginRightMm) == 0
                && Double.compare(marginBottomMm, spec.marginBottomMm) == 0
                && Double.compare(marginLeftMm, spec.marginLeftMm) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(presetName, portraitWidthMm, portraitHeightMm, orientation,
                marginTopMm, marginRightMm, marginBottomMm, marginLeftMm);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s %s %.1fx%.1fmm", presetName,
                orientation.name(), widthMm(), heightMm());
    }
}
