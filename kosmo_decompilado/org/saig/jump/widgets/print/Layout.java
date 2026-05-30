/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.print;

import java.awt.print.PageFormat;
import java.util.List;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import org.apache.log4j.Logger;
import org.saig.core.renderer.print.PrintRenderer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.MyPrintable;
import org.saig.jump.widgets.print.elements.GraphicElements;

public class Layout {
    private static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.widgets.print.Layout");
    private PrintService[] m_cachePrintServices = null;
    private PrintService m_cachePrintService = null;
    private Doc doc = null;
    private PrintRequestAttributeSet att = null;

    public void showPrintDialog(List<GraphicElements> graphicsElements, PageFormat pageFormat) {
        this.att = this.toPrintAttributes(pageFormat);
        DocFlavor.SERVICE_FORMATTED flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
        if (this.m_cachePrintServices == null) {
            this.m_cachePrintServices = PrintServiceLookup.lookupPrintServices(flavor, null);
        }
        PrintService defaultService = null;
        if (this.m_cachePrintService == null) {
            defaultService = PrintServiceLookup.lookupDefaultPrintService();
        }
        if (defaultService == null && this.m_cachePrintService == null) {
            return;
        }
        this.m_cachePrintService = this.m_cachePrintService == null ? ServiceUI.printDialog(null, 200, 200, this.m_cachePrintServices, defaultService, flavor, this.att) : ServiceUI.printDialog(null, 200, 200, this.m_cachePrintServices, this.m_cachePrintService, flavor, this.att);
        if (this.m_cachePrintService != null) {
            DocPrintJob jobNuevo = this.m_cachePrintService.createPrintJob();
            PrintJobAdapter pjlistener = new PrintJobAdapter(){

                @Override
                public void printDataTransferCompleted(PrintJobEvent e) {
                    LOGGER.info((Object)I18N.getString("org.saig.jump.widgets.print.Layout.printing-end"));
                    PrintRenderer.eraseCache();
                }
            };
            jobNuevo.addPrintJobListener(pjlistener);
            MyPrintable myPrintable = new MyPrintable(graphicsElements);
            this.doc = new SimpleDoc(myPrintable, flavor, null);
            try {
                jobNuevo.print(this.doc, this.att);
            }
            catch (PrintException pe) {
                pe.printStackTrace();
            }
        }
    }

    public PrintRequestAttributeSet toPrintAttributes(PageFormat pageFormat) {
        HashPrintRequestAttributeSet resul = new HashPrintRequestAttributeSet();
        float x = (float)pageFormat.getPaper().getWidth();
        float y = (float)pageFormat.getPaper().getHeight();
        MediaSizeName msn = MediaSize.findMedia(x / 72.0f, y / 72.0f, 25400);
        resul.add(msn);
        int orientation = pageFormat.getOrientation();
        if (orientation == 1) {
            resul.add(OrientationRequested.PORTRAIT);
        } else {
            resul.add(OrientationRequested.LANDSCAPE);
        }
        resul.add(new MediaPrintableArea(0.0f, 0.0f, (float)pageFormat.getWidth(), (float)pageFormat.getHeight(), 1000));
        resul.add(new MediaPrintableArea((float)Conversion.seventyTwoInch_To_Cm(pageFormat.getImageableX()) * 10.0f, (float)Conversion.seventyTwoInch_To_Cm(pageFormat.getImageableY()) * 10.0f, (float)Conversion.seventyTwoInch_To_Cm(pageFormat.getImageableWidth()) * 10.0f, (float)Conversion.seventyTwoInch_To_Cm(pageFormat.getImageableHeight()) * 10.0f, 1000));
        return resul;
    }
}

