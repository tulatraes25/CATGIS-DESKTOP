package ar.com.catgis.catmap.print;

import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Objects;

/**
 * Print service for CATMAP documents: one {@link Book} page per document page,
 * using the document sheet size, orientation and margins (never the legacy fixed
 * A4 landscape raster).
 */
public final class LayoutPrintService {

    private LayoutPrintService() {}

    /** Build the printable book (pure, testable headlessly). */
    public static Book buildBook(LayoutModel model) {
        Objects.requireNonNull(model, "CATMAP layout model");
        LayoutPageSpec spec = model.getPageSpec();
        PageFormat pageFormat = pageFormatFor(spec);
        LayoutPrintable printable = new LayoutPrintable(model);
        Book book = new Book();
        for (int page = 0; page < model.getPageCount(); page++) {
            book.append(printable, pageFormat);
        }
        return book;
    }

    /** Page format for one document: portrait paper with the document margins. */
    public static PageFormat pageFormatFor(LayoutPageSpec spec) {
        Objects.requireNonNull(spec, "CATMAP page spec");
        double paperWidthMm = spec.portraitWidthMm();
        double paperHeightMm = spec.portraitHeightMm();
        Paper paper = new Paper();
        paper.setSize(mmToPt(paperWidthMm), mmToPt(paperHeightMm));
        double imageableX = mmToPt(spec.marginLeftMm());
        double imageableY = mmToPt(spec.marginTopMm());
        double imageableWidth = mmToPt(paperWidthMm - spec.marginLeftMm() - spec.marginRightMm());
        double imageableHeight = mmToPt(paperHeightMm - spec.marginTopMm() - spec.marginBottomMm());
        paper.setImageableArea(imageableX, imageableY,
                Math.max(1.0, imageableWidth), Math.max(1.0, imageableHeight));
        PageFormat pageFormat = new PageFormat();
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(spec.orientation() == PageOrientation.LANDSCAPE
                ? PageFormat.LANDSCAPE : PageFormat.PORTRAIT);
        return pageFormat;
    }

    /**
     * Show the print dialog (when requested) and print every document page.
     *
     * @return true when the job was sent, false when the user cancelled the dialog
     */
    public static boolean print(LayoutModel model, boolean showDialog) throws PrinterException {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("CATMAP - Layout");
        job.setPageable(buildBook(model));
        if (showDialog && !job.printDialog()) {
            return false;
        }
        job.print();
        return true;
    }

    private static double mmToPt(double millimetres) {
        return millimetres * 72.0 / 25.4;
    }
}
