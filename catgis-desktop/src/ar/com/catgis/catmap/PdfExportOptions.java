package ar.com.catgis.catmap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration options for PDF export.
 */
public class PdfExportOptions {
    String title = "CATGIS Map";
    String author = "";
    String subject = "";
    String creator = "CATGIS Desktop";
    double pageWidthMm = 297;
    double pageHeightMm = 210;
    int dpi = 300;
    boolean pdfACompliant = false;
    String watermarkText = "";
    Map<String, String> customMetadata = new LinkedHashMap<>();

    public PdfExportOptions() {}

    public PdfExportOptions title(String v) { title = v; return this; }
    public PdfExportOptions author(String v) { author = v; return this; }
    public PdfExportOptions subject(String v) { subject = v; return this; }
    public PdfExportOptions pageSize(double wMm, double hMm) { pageWidthMm = wMm; pageHeightMm = hMm; return this; }
    public PdfExportOptions dpi(int v) { dpi = v; return this; }
    public PdfExportOptions pdfA(boolean v) { pdfACompliant = v; return this; }
    public PdfExportOptions watermark(String v) { watermarkText = v; return this; }
    public PdfExportOptions metadata(String key, String val) { customMetadata.put(key, val); return this; }
}
