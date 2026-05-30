/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.print.util;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterJob;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.apache.log4j.Logger;
import org.saig.core.renderer.print.PrintRenderer;
import org.saig.core.util.SwingWorker;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintOptions;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.util.DialogFactory;

public class PrintWaitDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.widgets.print.util.PrintWaitDialog");
    public static boolean canceled = false;
    public static double widthQuality;
    public static double heightQuality;
    public static double wmsWidthQuality;
    public static double wmsHeightQuality;

    public PrintWaitDialog(final PrintLayoutFrame frame) {
        super((Frame)frame, true);
        Page page = frame.getPage();
        Page.PageForPrint pageForPrint = (Page.PageForPrint)page.getPageForPrint();
        for (GraphicElements element : frame.getGraphicElements()) {
            if (!element.getClass().equals(MapFrame.class)) continue;
            MapFrame mapa = (MapFrame)element;
            LayerViewPanel panel = (LayerViewPanel)mapa.getGraphicElementsForPrint();
            int width = panel.getBounds().width;
            int height = panel.getBounds().height;
            double widthMetricGraphic = Conversion.seventyTwoInch_To_Cm(frame.getPageFormat().getWidth()) / (double)pageForPrint.getWidth() * (double)width;
            widthQuality = PrintWaitDialog.calculateQuality(widthMetricGraphic, PrintOptions.printQuality);
            heightQuality = widthQuality * (double)height / (double)width;
            wmsWidthQuality = PrintWaitDialog.calculateQuality(widthMetricGraphic, PrintOptions.wmsPrintQuality);
            wmsHeightQuality = wmsWidthQuality * (double)height / (double)width;
            LOGGER.info((Object)I18N.getMessage("org.saig.jump.widgets.print.util.PrintWaitDialog.print-quality-{0}-{1}", new Object[]{new Double(widthQuality), new Double(heightQuality)}));
        }
        final PrinterJob job = frame.getPrinterJob();
        this.getContentPane().setLayout(new BorderLayout());
        this.setTitle(I18N.getString("org.saig.jump.widgets.print.util.PrintWaitDialog.printing"));
        JLabel label = new JLabel();
        label.setIcon(IconLoader.icon("imprimante-03.gif"));
        label.setHorizontalAlignment(0);
        this.getContentPane().add((Component)label, "Center");
        JButton button = new JButton(I18N.getString("org.saig.jump.widgets.print.util.PrintWaitDialog.cancel"));
        button.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                canceled = true;
                job.cancel();
                PrintWaitDialog.this.setVisible(false);
            }
        });
        this.getContentPane().add((Component)button, "South");
        this.setSize(new Dimension(200, 100));
        GUIUtil.centreOnWindow(this);
        SwingWorker worker = new SwingWorker(){

            @Override
            public Object construct() {
                long t1;
                block5: {
                    t1 = System.currentTimeMillis();
                    try {
                        try {
                            job.print();
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                            DialogFactory.showErrorDialog(frame, I18N.getString("org.saig.jump.widgets.print.util.PrintWaitDialog.the-print-has-been-cancelled-or-an-unexpected-error-has-been-produced"), I18N.getString("org.saig.jump.widgets.print.actions.Print.print-error"));
                            PrintRenderer.eraseCache();
                            canceled = false;
                            break block5;
                        }
                    }
                    catch (Throwable throwable) {
                        PrintRenderer.eraseCache();
                        canceled = false;
                        throw throwable;
                    }
                    PrintRenderer.eraseCache();
                    canceled = false;
                }
                LOGGER.info((Object)I18N.getMessage("org.saig.jump.widgets.print.util.PrintWaitDialog.printing-total-time-{0}-ms", new Object[]{new Integer((int)(System.currentTimeMillis() - t1))}));
                return frame;
            }

            @Override
            public void finished() {
                PrintWaitDialog.this.closeWindow();
            }
        };
        worker.start();
    }

    public void closeWindow() {
        this.dispose();
    }

    public static double calculateQuality(double widthMetricGraphic, int quality) {
        double ppp = widthMetricGraphic / 2.54;
        return (double)quality * ppp;
    }

    public boolean wasCancelRequested() {
        return canceled;
    }
}

