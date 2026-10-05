package ar.com.catgis.catmap;

/**
 * Configuration for PDF export metadata and layout.
 */
public class PdfExportOptions {
    String title = "";
    String author = "";
    String subject = "";
    double pageWidthMm = 297;
    double pageHeightMm = 210;
    int dpi = 300;
    String watermark = "";

    public PdfExportOptions() {}

    public PdfExportOptions title(String v) { title = v; return this; }
    public PdfExportOptions author(String v) { author = v; return this; }
    public PdfExportOptions subject(String v) { subject = v; return this; }
    public PdfExportOptions pageSize(double wMm, double hMm) { pageWidthMm = wMm; pageHeightMm = hMm; return this; }
    public PdfExportOptions dpi(int v) { dpi = v; return this; }
    public PdfExportOptions watermark(String v) { watermark = v; return this; }
}
