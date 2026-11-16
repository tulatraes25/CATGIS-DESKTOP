package ar.com.catgis.catmap.print;

import ar.com.catgis.layout.LayoutLabel;
import ar.com.catgis.layout.LayoutModel;
import ar.com.catgis.layout.LayoutPageSpec;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Printing follows the document sheet, orientation and margins for every page. */
class LayoutPrintServiceContractTest {

    private static LayoutModel pageModel(LayoutPageSpec spec, int pages) {
        LayoutModel model = new LayoutModel();
        model.setPageSpec(spec);
        for (int page = 0; page < pages; page++) {
            if (page > 0) {
                model.addPage();
            }
            model.addElement(new LayoutLabel("p" + page, "PAGINA " + (page + 1),
                    12, 14 + page * 40, 200, 20));
        }
        return model;
    }

    private static double pt(double millimetres) {
        return millimetres * 72.0 / 25.4;
    }

    private static byte[] digest(BufferedImage image) throws Exception {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0,
                image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocate(pixels.length * 4);
        for (int pixel : pixels) {
            buffer.putInt(pixel);
        }
        return MessageDigest.getInstance("SHA-256").digest(buffer.array());
    }

    @Test
    void bookHasOnePagePerDocumentPageWithDocumentSheetAndMargins() {
        LayoutModel model = pageModel(LayoutPageSpec.preset(PageSizePreset.A3,
                PageOrientation.PORTRAIT).withMargins(10, 5, 10, 5), 3);
        Book book = LayoutPrintService.buildBook(model);
        assertEquals(3, book.getNumberOfPages());
        for (int page = 0; page < 3; page++) {
            PageFormat format = book.getPageFormat(page);
            assertNotNull(book.getPrintable(page));
            assertEquals(PageFormat.PORTRAIT, format.getOrientation());
            assertEquals(pt(297.0), format.getWidth(), 0.5);
            assertEquals(pt(420.0), format.getHeight(), 0.5);
            assertEquals(pt(5.0), format.getImageableX(), 0.5);
            assertEquals(pt(10.0), format.getImageableY(), 0.5);
            assertEquals(pt(287.0), format.getImageableWidth(), 0.5);
            assertEquals(pt(400.0), format.getImageableHeight(), 0.5);
        }
    }

    @Test
    void landscapeSheetUsesLandscapeOrientationAndSwappedSheet() {
        LayoutModel model = pageModel(LayoutPageSpec.A4_LANDSCAPE, 1);
        PageFormat format = LayoutPrintService.buildBook(model).getPageFormat(0);
        assertEquals(PageFormat.LANDSCAPE, format.getOrientation());
        assertEquals(pt(297.0), format.getWidth(), 0.5);
        assertEquals(pt(210.0), format.getHeight(), 0.5);
    }

    @Test
    void customSheetUsesMillimetres() {
        LayoutPageSpec spec = LayoutPageSpec.custom(200.0, 300.0, PageOrientation.PORTRAIT);
        PageFormat format = LayoutPrintService.pageFormatFor(spec);
        assertEquals(pt(200.0), format.getWidth(), 0.5);
        assertEquals(pt(300.0), format.getHeight(), 0.5);
        assertEquals(PageFormat.PORTRAIT, format.getOrientation());
    }

    @Test
    void printableRendersEveryPageAndRejectsOutOfRange() throws Exception {
        LayoutModel model = pageModel(LayoutPageSpec.A4_LANDSCAPE, 2);
        LayoutPrintable printable = new LayoutPrintable(model);
        PageFormat format = LayoutPrintService.pageFormatFor(model.getPageSpec());

        BufferedImage first = new BufferedImage(900, 700, BufferedImage.TYPE_INT_ARGB);
        BufferedImage second = new BufferedImage(900, 700, BufferedImage.TYPE_INT_ARGB);
        Graphics2D firstGraphics = first.createGraphics();
        Graphics2D secondGraphics = second.createGraphics();
        try {
            assertEquals(Printable.PAGE_EXISTS, printable.print(firstGraphics, format, 0));
            assertEquals(Printable.PAGE_EXISTS, printable.print(secondGraphics, format, 1));
            assertEquals(Printable.NO_SUCH_PAGE, printable.print(firstGraphics, format, 2));
        } finally {
            firstGraphics.dispose();
            secondGraphics.dispose();
        }
        assertFalse(java.util.Arrays.equals(digest(first), digest(second)),
                "each printed page must render its own elements");
        first.flush();
        second.flush();
    }

    @Test
    void printableWorksThroughTheBookPage() throws Exception {
        LayoutModel model = pageModel(LayoutPageSpec.A4_LANDSCAPE, 2);
        Book book = LayoutPrintService.buildBook(model);
        BufferedImage canvas = new BufferedImage(900, 700, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            assertEquals(Printable.PAGE_EXISTS,
                    book.getPrintable(1).print(graphics, book.getPageFormat(1), 1));
        } finally {
            graphics.dispose();
            canvas.flush();
        }
    }

    @Test
    void printRejectsNullModel() {
        assertThrows(NullPointerException.class, () -> LayoutPrintService.buildBook(null));
        assertThrows(NullPointerException.class, () -> new LayoutPrintable(null));
    }

    @Test
    void zeroMarginsKeepTheWholeSheetImageable() {
        PageFormat format = LayoutPrintService.pageFormatFor(LayoutPageSpec.A4_LANDSCAPE);
        assertTrue(format.getImageableWidth() > pt(290.0));
        assertTrue(format.getImageableHeight() > pt(200.0));
    }
}
