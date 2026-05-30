/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 *  org.dom4j.Document
 *  org.dom4j.DocumentHelper
 *  org.dom4j.Element
 *  org.dom4j.io.OutputFormat
 *  org.dom4j.io.XMLWriter
 */
package org.saig.jump.widgets.cts;

import com.iver.cit.gvsig.gui.panels.ProjChooserPanel;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.addremove.ButtonCustomAddRemovePanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.InfoSre;
import org.saig.jump.widgets.util.DialogFactory;

public class EPSGSelectionDialog
extends JDialog {
    private static Logger LOGGER = Logger.getLogger(EPSGSelectionDialog.class);
    private static final String CRS_NAME_KEY = "org.saig.jump.widgets.cts.EPSGSelectionDialog - CRS";
    public static final String NO_SRS_DEFINED = I18N.getString("org.saig.jump.widgets.cts.EPSGSelectionDialog.Spatial-reference-system-not-defined");
    public static final String sreXMLFile = "EPSG_Config.xml";
    private static IProjection lastProjection;
    protected Map srePorNombre = new TreeMap();
    protected ButtonCustomAddRemovePanel addRemovePanel = new ButtonCustomAddRemovePanel(true);
    protected DefaultListModel sreListModel;
    protected DefaultListModel favListModel;
    protected String nombreSreSel;
    protected boolean exitOk;
    protected ProjChooserPanel projChooserPanel;

    public EPSGSelectionDialog(JFrame parent, boolean modal, boolean isTask) {
        this(parent, modal, isTask, null);
    }

    public EPSGSelectionDialog(JFrame parent, boolean modal, String title, boolean isTask, IProjection proj) {
        super((Frame)parent, modal);
        this.setTitle(title);
        this.setContentPane(this.getMainPanel(isTask, proj));
        this.pack();
        GUIUtil.centreOnWindow(this);
        this.setVisible(true);
    }

    public EPSGSelectionDialog(JFrame parent, boolean modal, boolean isTask, IProjection proj) {
        this(parent, modal, I18N.getString("org.saig.jump.widgets.cts.EPSGSelectionDialog.Spatial-reference-system-selection"), isTask, proj);
    }

    public static void setLastProjection(IProjection proj) {
        lastProjection = proj;
    }

    private JPanel getMainPanel(boolean isTask, IProjection proj) {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        if (proj == null) {
            if (isTask) {
                lastProjection = JUMPWorkbench.getFrameInstance().getContext().getTask().getProjection();
            } else {
                Layerable[] layers = JUMPWorkbench.getFrameInstance().getContext().getLayerNamePanel().getSelectedLayers();
                lastProjection = layers[0].getProjection();
                if (lastProjection == null) {
                    lastProjection = JUMPWorkbench.getFrameInstance().getContext().getTask().getProjection();
                }
            }
        } else {
            lastProjection = proj;
        }
        this.projChooserPanel = new ProjChooserPanel(lastProjection, isTask);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.projChooserPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getOKCancelPanel());
        FormUtils.addFiller(mainPanel, 2, 0);
        return mainPanel;
    }

    private OKCancelPanel getOKCancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        okCancelPanel.setLayout(gbPaneOKCancel);
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                EPSGSelectionDialog.this.exitOk = okCancelPanel.wasOKPressed();
                EPSGSelectionDialog.this.setVisible(false);
            }
        });
        return okCancelPanel;
    }

    public void guardaEnArchivoXML() {
        try {
            File file = new File(sreXMLFile);
            file.createNewFile();
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("srslist");
            for (InfoSre sre : this.srePorNombre.values()) {
                Element elem = root.addElement("srs");
                elem.addElement("name").addText(sre.getNombre());
                elem.addElement("code").addText(sre.getCodigo());
                elem.addElement("favorite").addText(sre.getFavorito());
            }
            FileOutputStream out = new FileOutputStream(sreXMLFile);
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("ISO-8859-1");
            XMLWriter xmlWriter = new XMLWriter((OutputStream)out, format);
            xmlWriter.write(document);
            xmlWriter.flush();
            out.close();
            xmlWriter.close();
        }
        catch (IOException e) {
            LOGGER.error((Object)e);
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.widgets.cts.EPSGSelectionDialog.Could-not-process-file-{0}-{1}", new Object[]{": ", String.valueOf(System.getProperty("user.dir")) + System.getProperty("file.separator") + sreXMLFile}), I18N.getString("org.saig.jump.widgets.cts.EPSGSelectionDialog.Error"));
        }
    }

    public boolean isOk() {
        return this.exitOk;
    }

    public IProjection getProjection() {
        return this.projChooserPanel.getCurProj();
    }
}

