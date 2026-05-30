/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.print.actions;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.actions.PrintAction;
import org.saig.jump.widgets.print.batch.BatchPrintingOptionsDialog;
import org.saig.jump.widgets.print.util.PrintWaitDialog;

public class BatchPrintingAction
extends PrintAction {
    private static final Logger LOGGER = Logger.getLogger(BatchPrintingAction.class);
    public static final String DEFAULT_DOCUMENT_NAME = I18N.getString("org.saig.jump.widgets.print.actions.BatchPrintingAction.Kosmo-document");

    public BatchPrintingAction(PrintLayoutFrame parent) {
        super(parent);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        BatchPrintingOptionsDialog pad = new BatchPrintingOptionsDialog(this.frame);
        pad.pack();
        pad.setLocationRelativeTo(JUMPWorkbench.getFrameInstance());
        pad.setVisible(true);
        if (pad.wasOkPressed()) {
            this.print(pad);
        }
        pad.dispose();
    }

    public void print(List<Envelope> envelopes, List<String> pageNames) {
        PageFormat pfSinMargenes = new PageFormat();
        Paper paper = new Paper();
        paper.setSize(this.frame.getPageFormat().getPaper().getWidth(), this.frame.getPageFormat().getPaper().getHeight());
        paper.setImageableArea(0.0, 0.0, this.frame.getPageFormat().getPaper().getWidth(), this.frame.getPageFormat().getPaper().getHeight());
        pfSinMargenes.setOrientation(this.frame.getPageFormat().getOrientation());
        pfSinMargenes.setPaper(paper);
        this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().setEnvelopesAndLabels(envelopes, pageNames);
        this.frame.getPrinterJob().setPrintable((Printable)((Object)this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().getPageForPrint()), pfSinMargenes);
        boolean accion = this.frame.getPrinterJob().printDialog();
        if (accion) {
            PrintWaitDialog dialog = new PrintWaitDialog(this.frame);
            dialog.setVisible(true);
        }
    }

    public boolean print(boolean first, String documentName) {
        PageFormat pfSinMargenes = new PageFormat();
        Paper paper = new Paper();
        paper.setSize(this.frame.getPageFormat().getPaper().getWidth(), this.frame.getPageFormat().getPaper().getHeight());
        paper.setImageableArea(0.0, 0.0, this.frame.getPageFormat().getPaper().getWidth(), this.frame.getPageFormat().getPaper().getHeight());
        pfSinMargenes.setOrientation(this.frame.getPageFormat().getOrientation());
        pfSinMargenes.setPaper(paper);
        this.frame.getPrinterJob().setPrintable((Printable)((Object)this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().getPageForPrint()), pfSinMargenes);
        this.frame.getPrinterJob().setJobName(documentName);
        boolean accion = false;
        accion = first ? this.frame.getPrinterJob().printDialog() : true;
        if (accion) {
            PrintWaitDialog dialog = new PrintWaitDialog(this.frame);
            dialog.setVisible(true);
            return dialog.wasCancelRequested();
        }
        return true;
    }

    private void print(BatchPrintingOptionsDialog dialog) {
        Layer layer = dialog.getSelectedLayer();
        String attrName = dialog.getSelectedAttrName();
        boolean documentName = dialog.useDocumentName();
        boolean etqName = dialog.useLabelName();
        boolean allInOneDocument = dialog.printAllInOneDocument();
        boolean printAllLayer = dialog.printWholeLayer();
        if (allInOneDocument) {
            if (printAllLayer) {
                this.printLayer(layer, attrName, etqName);
            } else {
                this.printSelected(layer, attrName, etqName);
            }
        } else if (printAllLayer) {
            this.printMultiDocumentLayer(layer, attrName, documentName, etqName);
        } else {
            this.printMultiDocumentSelected(layer, attrName, documentName, etqName);
        }
    }

    private void printSelected(Layer layer, String attrName, boolean markMap) {
        ArrayList<Envelope> envelopes = new ArrayList<Envelope>();
        ArrayList<String> attributes = null;
        Collection<Feature> selectedItems = this.frame.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(layer);
        Iterator<Feature> it = selectedItems.iterator();
        if (markMap) {
            attributes = new ArrayList<String>();
        }
        while (it.hasNext()) {
            Feature feat = it.next();
            Geometry g = feat.getGeometry();
            Envelope env = g.getEnvelopeInternal();
            envelopes.add(env);
            if (!markMap) continue;
            attributes.add(feat.getAttribute(attrName).toString());
        }
        this.print(envelopes, attributes);
    }

    private void printMultiDocumentSelected(Layer layer, String attrName, boolean nameDocument, boolean markMap) {
        Collection<Feature> selectedItems = this.frame.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(layer);
        Iterator<Feature> it = selectedItems.iterator();
        boolean primero = true;
        boolean cancel = false;
        while (it.hasNext() && !cancel) {
            Feature feat = it.next();
            Geometry g = feat.getGeometry();
            Envelope env = g.getEnvelopeInternal();
            String documentName = nameDocument ? feat.getAttribute(attrName).toString() : DEFAULT_DOCUMENT_NAME;
            if (!markMap) {
                this.frame.getMapElement().changeZoom(env);
            } else {
                documentName = feat.getAttribute(attrName).toString();
                ArrayList<Envelope> envelopes = new ArrayList<Envelope>();
                ArrayList<String> labels = new ArrayList<String>();
                envelopes.add(env);
                labels.add(documentName);
                this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().setEnvelopesAndLabels(envelopes, labels);
            }
            cancel = this.print(primero, documentName);
            primero = false;
        }
    }

    private void printLayer(Layer layer, String attrName, boolean markMap) {
        ArrayList<Envelope> envelopes;
        ArrayList<String> attributes;
        block10: {
            attributes = null;
            FeatureCollection featCol = layer.getUltimateFeatureCollectionWrapper();
            FeatureIterator it = featCol.iterator();
            envelopes = new ArrayList<Envelope>();
            if (markMap) {
                attributes = new ArrayList<String>();
            }
            try {
                try {
                    while (it.hasNext()) {
                        Feature feat = it.next();
                        Envelope env = feat.getGeometry().getEnvelopeInternal();
                        envelopes.add(env);
                        if (!markMap) continue;
                        attributes.add(feat.getAttribute(attrName).toString());
                    }
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (it != null && it instanceof FeatureIterator) {
                        it.close();
                    }
                    break block10;
                }
            }
            catch (Throwable throwable) {
                if (it != null && it instanceof FeatureIterator) {
                    it.close();
                }
                throw throwable;
            }
            if (it != null && it instanceof FeatureIterator) {
                it.close();
            }
        }
        this.print(envelopes, attributes);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void printMultiDocumentLayer(Layer layer, String attrName, boolean nameDocument, boolean markMap) {
        FeatureCollection featCol = layer.getUltimateFeatureCollectionWrapper();
        FeatureIterator it = null;
        try {
            try {
                it = featCol.iterator();
                boolean first = true;
                boolean cancel = false;
                while (it.hasNext()) {
                    if (cancel) {
                        return;
                    }
                    Feature feat = it.next();
                    Envelope env = feat.getGeometry().getEnvelopeInternal();
                    String documentName = nameDocument ? feat.getAttribute(attrName).toString() : DEFAULT_DOCUMENT_NAME;
                    if (!markMap) {
                        this.frame.getMapElement().changeZoom(env);
                    } else {
                        documentName = feat.getAttribute(attrName).toString();
                        ArrayList<Envelope> envelopes = new ArrayList<Envelope>();
                        ArrayList<String> labels = new ArrayList<String>();
                        envelopes.add(env);
                        labels.add(documentName);
                        this.frame.getPrintLayoutPreviewPanel().getPreviewPanel().getPage().setEnvelopesAndLabels(envelopes, labels);
                    }
                    cancel = this.print(first, documentName);
                    first = false;
                }
                return;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                if (it == null) return;
                it.close();
                return;
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }
}

