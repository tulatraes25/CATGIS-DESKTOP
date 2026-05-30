/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package es.kosmo.desktop.gui.components;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.cresques.cts.IProjection;
import org.gvsig.crs.ICrs;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.cts.EPSGTransformationSelectionDialog;

public class JSelectTransformationParamsPanel
extends JPanel {
    public static final String LAST_PROJECTION_KEY = String.valueOf(JSelectTransformationParamsPanel.class.getName()) + " - LAST PROJECTION KEY";
    private static final long serialVersionUID = 1L;
    protected EPSGTransformationSelectionDialog dialog;
    protected JTextField selectedProjectionTextField;
    protected JButton selectProjectionButton;
    protected JTextArea selectedProjectionTransParamsTextArea;
    private IProjection selectedProjection;
    private IProjection dataProj;
    private IProjection sourceProj;

    public JSelectTransformationParamsPanel(IProjection srcProj, IProjection targetProj) {
        this.sourceProj = srcProj;
        this.dataProj = targetProj;
        this.initializeGUI();
        this.setProjection(targetProj);
    }

    protected void initializeGUI() {
        this.setLayout(new GridBagLayout());
        JLabel selectedProjectionLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.components.JSelectTransformationParamsPanel.Source-spatial-reference-system")) + ":");
        this.selectedProjectionTextField = new JTextField();
        this.selectedProjectionTextField.setEditable(false);
        this.selectProjectionButton = new JButton("...");
        Dimension dim = new Dimension(30, 20);
        this.selectProjectionButton.setMinimumSize(dim);
        this.selectProjectionButton.setPreferredSize(dim);
        this.selectProjectionButton.setMaximumSize(dim);
        this.selectProjectionButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JSelectTransformationParamsPanel.this.recoverTransformationParams();
            }
        });
        JLabel selectedProjectionTransParamsLabel = new JLabel(String.valueOf(I18N.getString("es.kosmo.desktop.gui.components.JSelectTransformationParamsPanel.Transformation-params")) + ":");
        this.selectedProjectionTransParamsTextArea = new JTextArea();
        this.selectedProjectionTransParamsTextArea.setFont(selectedProjectionLabel.getFont());
        this.selectedProjectionTransParamsTextArea.setEditable(false);
        this.selectedProjectionTransParamsTextArea.setLineWrap(true);
        this.selectedProjectionTransParamsTextArea.setWrapStyleWord(true);
        this.selectedProjectionTransParamsTextArea.setColumns(50);
        this.selectedProjectionTransParamsTextArea.setRows(2);
        FormUtils.addRowInGBL((JComponent)this, 0, 0, selectedProjectionLabel, (JComponent)this.selectedProjectionTextField);
        FormUtils.addRowInGBL(this, 0, 30, this.selectProjectionButton);
        FormUtils.addRowInGBL((JComponent)this, 1, 0, selectedProjectionTransParamsLabel, (JComponent)this.selectedProjectionTransParamsTextArea, true);
    }

    private void recoverTransformationParams() {
        this.dialog = new EPSGTransformationSelectionDialog(JUMPWorkbench.getFrameInstance(), true, this.dataProj, ((ICrs)this.sourceProj).getCode());
        GUIUtil.centreOnWindow(this.dialog);
        this.dialog.setVisible(true);
        if (this.dialog.wasOkPressed()) {
            this.setProjection(this.dialog.getProjection());
        }
    }

    private void setProjection(IProjection proj) {
        this.selectedProjection = proj;
        this.selectedProjectionTextField.setText(GUITranslationsUtils.getCRSDescription(this.selectedProjection));
        this.selectedProjectionTransParamsTextArea.setText(((ICrs)this.selectedProjection).getSourceTransformationParams());
    }

    public IProjection getSelectedProjection() {
        return this.selectedProjection;
    }
}

