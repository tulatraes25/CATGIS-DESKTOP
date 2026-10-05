package ar.com.catgis.layout;

import java.awt.Dimension;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Standard page size presets for layout composition.
 */
public enum PageSizePreset {
    A3("A3", 297d, 420d),
    A4("A4", 210d, 297d),
    A5("A5", 148d, 210d),
    LETTER("Carta", 216d, 279d),
    LEGAL("Legal", 216d, 356d),
    TABLOID("Tabloide", 279d, 432d);

    private final String label;
    public final double widthMm;
    public final double heightMm;

    PageSizePreset(String label, double widthMm, double heightMm) {
        this.label = label;
        this.widthMm = widthMm;
        this.heightMm = heightMm;
    }

    public Dimension pixelSize(PageOrientation orientation, int dpi) {
        double widthInches = (orientation == PageOrientation.LANDSCAPE ? heightMm : widthMm) / 25.4d;
        double heightInches = (orientation == PageOrientation.LANDSCAPE ? widthMm : heightMm) / 25.4d;
        int width = Math.max(600, (int) Math.round(widthInches * dpi));
        int height = Math.max(600, (int) Math.round(heightInches * dpi));
        return new Dimension(width, height);
    }

    public PDRectangle toPdfRectangle(PageOrientation orientation) {
        float widthPoints = (float) ((orientation == PageOrientation.LANDSCAPE ? heightMm : widthMm) / 25.4d * 72d);
        float heightPoints = (float) ((orientation == PageOrientation.LANDSCAPE ? widthMm : heightMm) / 25.4d * 72d);
        return new PDRectangle(widthPoints, heightPoints);
    }

    @Override
    public String toString() {
        return label;
    }
}
