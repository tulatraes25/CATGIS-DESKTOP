/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class ViewBoxSelectingDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ViewBoxSelectingDialog.class);
    private ButtonGroup infoGroup = new ButtonGroup();
    protected JRadioButton selectedViewBoxFromTaskRadioButton;
    protected JRadioButton selectedViewBoxFromLayerRadioButton;
    protected JRadioButton calculateViewBoxRadioButton;
    protected JRadioButton setViewboxRadioButton;
    protected JTextField xMinTextField;
    protected JTextField yMinTextField;
    protected JTextField xMaxTextField;
    protected JTextField yMaxTextField;
    protected JComboBox layerComboBox;
    protected Envelope layerEnvelope;
    protected OKCancelPanel okCancelPanel;

    public ViewBoxSelectingDialog(JFrame parent, boolean modal, String tableName) {
        super((Frame)parent, modal);
        this.setContentPane(this.getMainPanel());
        this.setTitle(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Introduce-layer-view")) + " - " + tableName);
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    public JPanel getMainPanel() {
        Object[] capas = JUMPWorkbench.getFrameInstance().getContext().getAllLayers().toArray();
        boolean activate = capas.length > 0;
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.selectedViewBoxFromTaskRadioButton = new JRadioButton("<html><b>" + I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Assignment-of-the-active-view-envelope") + "</b></html>");
        this.selectedViewBoxFromTaskRadioButton.setToolTipText(I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Recommended-when-all-later-operations-are-about-data-in-such-view"));
        this.selectedViewBoxFromTaskRadioButton.setEnabled(activate);
        this.selectedViewBoxFromTaskRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ViewBoxSelectingDialog.this.layerComboBox.setEnabled(!ViewBoxSelectingDialog.this.selectedViewBoxFromTaskRadioButton.isSelected());
            }
        });
        this.selectedViewBoxFromLayerRadioButton = new JRadioButton("<html><b>" + I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Assignment-from-layer-previously-loaded-in-the-Kosmo-project") + "</b></html>");
        this.selectedViewBoxFromLayerRadioButton.setToolTipText(I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Recommended-when-loading-a-layer-with-many-records-and-having-another-layer-with-the-same-extension-loaded"));
        this.selectedViewBoxFromLayerRadioButton.setEnabled(activate);
        this.selectedViewBoxFromLayerRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ViewBoxSelectingDialog.this.layerComboBox.setEnabled(ViewBoxSelectingDialog.this.selectedViewBoxFromLayerRadioButton.isSelected());
            }
        });
        this.calculateViewBoxRadioButton = new JRadioButton("<html><b>" + I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Direct-calculation-from-layer-Recommended") + "</b></html>");
        this.calculateViewBoxRadioButton.setToolTipText(I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Recommended-If-the-layer-is-big-millions-of-features-it-should-take-long-time-though-you-may-save-the-project-in-kosmo-so-it-will-not-be-necessary-to-recalculate"));
        this.calculateViewBoxRadioButton.setSelected(true);
        this.calculateViewBoxRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ViewBoxSelectingDialog.this.layerComboBox.setEnabled(!ViewBoxSelectingDialog.this.calculateViewBoxRadioButton.isSelected());
            }
        });
        this.setViewboxRadioButton = new JRadioButton("<html><b>" + I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Direct-assignment-by-the-user") + "</b></html>");
        this.setViewboxRadioButton.setToolTipText(I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.The-user-decides-exactly-the-window-on-wich-he-wish-to-work"));
        this.setViewboxRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                ViewBoxSelectingDialog.this.layerComboBox.setEnabled(!ViewBoxSelectingDialog.this.setViewboxRadioButton.isSelected());
                ViewBoxSelectingDialog.this.xMinTextField.setEnabled(ViewBoxSelectingDialog.this.setViewboxRadioButton.isSelected());
                ViewBoxSelectingDialog.this.xMaxTextField.setEnabled(ViewBoxSelectingDialog.this.setViewboxRadioButton.isSelected());
                ViewBoxSelectingDialog.this.yMinTextField.setEnabled(ViewBoxSelectingDialog.this.setViewboxRadioButton.isSelected());
                ViewBoxSelectingDialog.this.yMaxTextField.setEnabled(ViewBoxSelectingDialog.this.setViewboxRadioButton.isSelected());
            }
        });
        this.infoGroup.add(this.selectedViewBoxFromTaskRadioButton);
        this.infoGroup.add(this.selectedViewBoxFromLayerRadioButton);
        this.infoGroup.add(this.calculateViewBoxRadioButton);
        this.infoGroup.add(this.setViewboxRadioButton);
        FormUtils.addRowInGBL(mainPanel, 0, 0, new JLabel("<html>" + I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.In-order-to-load-a-layer-from-a-geodatabase-it-is-necessary-to-know-the-geographic-window-that-its-data-occupy-its-length") + ".<BR>" + I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.This-data-is-important-so-it-is-used-internally-for-differents-checks-and-operations") + ".<BR><BR>" + I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.We-offer-you-some-alternatives-to-supply-it") + ": </html>"));
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.selectedViewBoxFromTaskRadioButton);
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.selectedViewBoxFromLayerRadioButton);
        FormUtils.addRowInGBL(mainPanel, 3, 0, this.calculateViewBoxRadioButton);
        FormUtils.addRowInGBL(mainPanel, 4, 0, this.setViewboxRadioButton);
        this.layerComboBox = new JComboBox<Object>(capas);
        FormUtils.addRowInGBL((JComponent)mainPanel, 5, 0, I18N.getMessage("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Layers-{0}", new Object[]{":"}), (JComponent)this.layerComboBox);
        this.layerComboBox.setEnabled(false);
        FormUtils.addRowInGBL(mainPanel, 6, 0, this.getViewBoxPanel());
        FormUtils.addRowInGBL(mainPanel, 7, 0, this.createOKcancelPanel());
        FormUtils.addFiller(mainPanel, 8, 0);
        return mainPanel;
    }

    public JPanel getViewBoxPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Direct-assignment-parameters")));
        this.xMinTextField = new JTextField();
        FormUtils.addRowInGBL((JComponent)mainPanel, 0, 0, I18N.getMessage("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Minimun-X-coordinate-{0}", new Object[]{":"}), (JComponent)this.xMinTextField);
        this.yMinTextField = new JTextField();
        FormUtils.addRowInGBL((JComponent)mainPanel, 1, 0, I18N.getMessage("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Minimun-Y-coordinate-{0}", new Object[]{":"}), (JComponent)this.yMinTextField);
        this.xMaxTextField = new JTextField();
        FormUtils.addRowInGBL((JComponent)mainPanel, 2, 0, I18N.getMessage("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Maximun-X-coordinate-{0}", new Object[]{":"}), (JComponent)this.xMaxTextField);
        this.yMaxTextField = new JTextField();
        FormUtils.addRowInGBL((JComponent)mainPanel, 3, 0, I18N.getMessage("org.saig.jump.widgets.datasource.ViewBoxSelectingDialog.Maximun-Y-coordinate-{0}", new Object[]{":"}), (JComponent)this.yMaxTextField);
        FormUtils.addFiller(mainPanel, 4, 0);
        this.xMinTextField.setEnabled(false);
        this.xMaxTextField.setEnabled(false);
        this.yMinTextField.setEnabled(false);
        this.yMaxTextField.setEnabled(false);
        return mainPanel;
    }

    protected boolean isInputValid() {
        return true;
    }

    private OKCancelPanel createOKcancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        GridBagLayout gbPaneOKCancel = new GridBagLayout();
        this.okCancelPanel.setLayout(gbPaneOKCancel);
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (ViewBoxSelectingDialog.this.okCancelPanel.wasOKPressed()) {
                    if (ViewBoxSelectingDialog.this.selectedViewBoxFromTaskRadioButton.isSelected()) {
                        ViewBoxSelectingDialog.this.layerEnvelope = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
                    } else if (ViewBoxSelectingDialog.this.selectedViewBoxFromLayerRadioButton.isSelected()) {
                        Layer layer = (Layer)ViewBoxSelectingDialog.this.layerComboBox.getSelectedItem();
                        try {
                            ViewBoxSelectingDialog.this.layerEnvelope = layer.getUltimateFeatureCollectionWrapper().getEnvelope();
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)"", (Throwable)e);
                            ViewBoxSelectingDialog.this.layerEnvelope = new Envelope();
                        }
                    } else if (ViewBoxSelectingDialog.this.setViewboxRadioButton.isSelected()) {
                        if (ViewBoxSelectingDialog.this.isInputValid()) {
                            ViewBoxSelectingDialog.this.layerEnvelope = new Envelope(new Double(ViewBoxSelectingDialog.this.xMinTextField.getText()).doubleValue(), new Double(ViewBoxSelectingDialog.this.xMaxTextField.getText()).doubleValue(), new Double(ViewBoxSelectingDialog.this.yMinTextField.getText()).doubleValue(), new Double(ViewBoxSelectingDialog.this.yMaxTextField.getText()).doubleValue());
                        } else {
                            return;
                        }
                    }
                }
                ViewBoxSelectingDialog.this.setVisible(false);
            }
        });
        return this.okCancelPanel;
    }

    public Envelope getLayerEnvelope() {
        return this.layerEnvelope;
    }
}

