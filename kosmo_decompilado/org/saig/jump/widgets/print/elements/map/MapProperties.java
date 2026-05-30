/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.jump.widgets.print.elements.map;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.Conversion;
import org.saig.jump.widgets.print.elements.map.MapFrame;

public class MapProperties
extends JFrame {
    private JPanel scalePanel = null;
    private OKCancelPanel okCancelPanel = new OKCancelPanel();
    private JTextField scaleTextField = null;
    private DecimalFormat formateador = new DecimalFormat("###,###");
    private MapFrame mapFrame;
    private JPanel repaintPanel;
    private JCheckBox repaintBox;

    public MapProperties(MapFrame parentMapFrame) {
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.mapFrame = parentMapFrame;
        this.initialize();
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        this.setName(I18N.getString("org.saig.jump.widgets.print.elements.map.MapProperties.view-properties"));
        this.setTitle(I18N.getString("org.saig.jump.widgets.print.elements.map.MapProperties.view-properties"));
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (MapProperties.this.okCancelPanel.wasOKPressed()) {
                    MapProperties.this.updateRepaint();
                    MapProperties.this.changeCurrentScale();
                    MapProperties.this.termine();
                } else {
                    MapProperties.this.termine();
                }
            }
        });
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getScalePanel());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getRepaintPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.okCancelPanel);
        this.pack();
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }

    private JPanel getScalePanel() {
        if (this.scalePanel == null) {
            this.scalePanel = new JPanel();
            this.scalePanel.setLayout(new BorderLayout());
            this.scalePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.map.MapProperties.view-scale")));
            this.scaleTextField = this.getScaleTextField();
            this.scaleTextField.setText(this.formateador.format(this.mapFrame.getScale()));
            this.scaleTextField.setMinimumSize(new Dimension(150, 25));
            this.scaleTextField.setPreferredSize(new Dimension(150, 25));
            this.scalePanel.add((Component)this.scaleTextField, "Center");
            this.scalePanel.setMinimumSize(new Dimension(300, 50));
            this.scalePanel.setPreferredSize(new Dimension(300, 50));
        }
        return this.scalePanel;
    }

    private JPanel getRepaintPanel() {
        if (this.repaintPanel == null) {
            this.repaintPanel = new JPanel();
            this.repaintPanel.setLayout(new BorderLayout());
            this.repaintPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.print.elements.map.MapProperties.update-view")));
            this.repaintBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.print.elements.map.MapProperties.refresh"), this.mapFrame.isRepaint());
            this.repaintPanel.add((Component)this.repaintBox, "Center");
            this.repaintPanel.setMinimumSize(new Dimension(300, 50));
            this.repaintPanel.setPreferredSize(new Dimension(300, 50));
        }
        return this.repaintPanel;
    }

    private JTextField getScaleTextField() {
        if (this.scaleTextField == null) {
            this.scaleTextField = new ValidatingTextField("", 10, 2, new ValidatingTextField.GreaterThanOrEqualValidator(0.0), ValidatingTextField.DUMMY_CLEANER);
            this.scaleTextField.setToolTipText(I18N.getString("org.saig.jump.widgets.scale.ScalePanel.current-scale"));
            this.scaleTextField.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    MapProperties.this.changeCurrentScale();
                }
            });
        }
        return this.scaleTextField;
    }

    public void updateRepaint() {
        this.mapFrame.updateRepaint(this.repaintBox.isSelected());
    }

    public void changeCurrentScale() {
        double newForPrintHeight;
        double newForPrintWidth;
        Envelope currentViewEnvelope = this.mapFrame.getParent().getTaskFrame().getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
        Envelope currentForPrintEnvelope = ((LayerViewPanel)this.mapFrame.getGraphicElementsForPrint()).getViewport().getEnvelopeInModelCoordinates();
        try {
            newForPrintWidth = this.calculateWidth(this.formateador.parse(this.scaleTextField.getText()).doubleValue());
            newForPrintHeight = currentForPrintEnvelope.getHeight() * newForPrintWidth / currentForPrintEnvelope.getWidth();
        }
        catch (ParseException e1) {
            return;
        }
        double newHeight = 0.0;
        double newWidth = 0.0;
        if (currentViewEnvelope.getHeight() / newForPrintHeight > currentViewEnvelope.getWidth() / newForPrintWidth) {
            newHeight = newForPrintHeight;
            newWidth = currentViewEnvelope.getWidth() * newHeight / currentViewEnvelope.getHeight();
        } else {
            newWidth = newForPrintWidth;
            newHeight = currentViewEnvelope.getHeight() * newWidth / currentViewEnvelope.getWidth();
        }
        double oldCenterX = currentViewEnvelope.getMinX() + currentViewEnvelope.getWidth() / 2.0;
        double oldCenterY = currentViewEnvelope.getMinY() + currentViewEnvelope.getHeight() / 2.0;
        Envelope newEnvelope = new Envelope(oldCenterX - newWidth / 2.0, oldCenterX + newWidth / 2.0, oldCenterY - newHeight / 2.0, oldCenterY + newHeight / 2.0);
        try {
            this.mapFrame.changeViewZoom(newEnvelope);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            // empty catch block
        }
    }

    private double calculateWidth(double scale) {
        double printWidth = this.mapFrame.getGraphicElementsForPrint().getWidth();
        int width = this.mapFrame.getParent().getPage().getWidth();
        double k = Conversion.seventyTwoInch_To_Cm(this.mapFrame.getParent().getPageFormat().getWidth()) / (double)width;
        double newWidth = scale * printWidth * k / 100.0;
        return newWidth;
    }

    private void termine() {
        this.dispose();
    }
}

