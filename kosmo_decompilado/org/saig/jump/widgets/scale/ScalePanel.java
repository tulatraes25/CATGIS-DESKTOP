/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.gvsig.gui.beans.swing.JButton
 */
package org.saig.jump.widgets.scale;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import org.gvsig.gui.beans.swing.JButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.ScaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.scale.CategoryPopupFactory;

public class ScalePanel
extends JPanel
implements ViewportListener {
    private static final long serialVersionUID = 1L;
    private JLabel scaleLabel;
    private ValidatingTextField scaleTextField;
    private DecimalFormat formateador = new DecimalFormat("###,###");
    private Envelope currentEnvelope;
    private WorkbenchContext context;
    private double layerViewPanelWidth;
    public static double currentScale = 0.0;
    private LayerViewPanel layerViewPanel;

    public ScalePanel(WorkbenchContext workbenchContext) {
        super(new GridBagLayout());
        this.context = workbenchContext;
        this.initialize();
    }

    private void initialize() {
        ImageIcon icon = IconLoader.icon("folder.png");
        JButton categoriesButton = new JButton((Icon)icon);
        categoriesButton.setPreferredSize(new Dimension(icon.getIconWidth() + 10, icon.getIconHeight() + 10));
        categoriesButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                CategoryPopupFactory categoryPopupFactory = new CategoryPopupFactory();
                JPopupMenu popupMenu = categoryPopupFactory.createPopupMenu();
                JButton source = (JButton)e.getSource();
                Rectangle bounds = source.getBounds();
                popupMenu.show((Component)source, bounds.x, bounds.y);
            }
        });
        this.scaleLabel = new JLabel();
        this.scaleLabel.setText(String.valueOf(I18N.getString("org.saig.jump.widgets.scale.ScalePanel.scale")) + " 1:");
        this.scaleLabel.setToolTipText(I18N.getString("org.saig.jump.widgets.scale.ScalePanel.current-scale"));
        FormUtils.addRowInGBL((JComponent)this, 0, 0, (JComponent)categoriesButton, false, true);
        FormUtils.addRowInGBL((JComponent)this, 0, 30, this.scaleLabel, (JComponent)this.getScaleTextField());
    }

    private JTextField getScaleTextField() {
        if (this.scaleTextField == null) {
            this.scaleTextField = new ValidatingTextField("", 10, 2, new ValidatingTextField.GreaterThanOrEqualValidator(0.0), ValidatingTextField.DUMMY_CLEANER);
            this.scaleTextField.setToolTipText(I18N.getString("org.saig.jump.widgets.scale.ScalePanel.current-scale"));
            this.scaleTextField.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    ScalePanel.this.changeCurrentScale();
                }
            });
        }
        return this.scaleTextField;
    }

    @Override
    public void zoomChanged(Envelope modelEnvelope) {
        this.currentEnvelope = modelEnvelope;
        if (this.layerViewPanel == null && this.context.getLayerViewPanel() != null) {
            this.layerViewPanel = this.context.getLayerViewPanel();
        }
        if (this.context != null && this.context.getLayerViewPanel() != null) {
            this.layerViewPanelWidth = this.layerViewPanel.getWidth();
            double newScale = this.generateScaleValue(this.currentEnvelope.getMaxX(), this.currentEnvelope.getMinX(), this.layerViewPanelWidth);
            if (new Double(newScale).isNaN()) {
                newScale = 0.0;
            }
            this.scaleTextField.setText(this.formateador.format(newScale));
        }
    }

    public void changeCurrentScale() {
        double value;
        try {
            value = this.formateador.parse(this.scaleTextField.getText()).doubleValue();
            this.layerViewPanelWidth = this.layerViewPanel.getWidth();
        }
        catch (ParseException e1) {
            return;
        }
        Envelope smEnvelope = ScaleManager.getInstance().generateNewEnvelopeValue(this.currentEnvelope, this.layerViewPanel.getWidth(), this.layerViewPanel.getHeight(), value, this.context.getTask().getProjection(), this.layerViewPanel.getMapLengthUnit());
        try {
            this.layerViewPanel.getViewport().zoom(smEnvelope);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            // empty catch block
        }
    }

    private double generateScaleValue(double maxX, double minX, double width) {
        double scale;
        currentScale = scale = (maxX - minX) * (ScaleManager.dpi / 2.54 * 100.0) / width * this.context.getLayerViewPanel().getFactor();
        double newScale = ScaleManager.getInstance().generateScaleValue(maxX, minX, width, this.layerViewPanel.getProjection(), this.layerViewPanel.getMapLengthUnit());
        return newScale;
    }
}

