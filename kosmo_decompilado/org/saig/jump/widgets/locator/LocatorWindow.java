/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.locator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.cursortool.Animations;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.utils.locator.LocatorPlugIn;
import org.saig.jump.widgets.util.validating.BetweenValidator;

public class LocatorWindow
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LocatorWindow.class);
    private JPanel locatorPanel;
    private JTextField textX;
    private JTextField textY;
    private JButton gotoButton;

    public LocatorWindow() {
        super((Frame)JUMPWorkbench.getFrameInstance(), LocatorPlugIn.NAME);
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.setContentPane(this.getLocatorPanel());
        this.setResizable(false);
        this.pack();
    }

    public JPanel getLocatorPanel() {
        if (this.locatorPanel == null) {
            this.locatorPanel = new JPanel(new GridBagLayout());
            this.initialize();
        }
        return this.locatorPanel;
    }

    private void initialize() {
        JLabel labelX = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.locator.LocatorWindow.coordinate-x")) + " :");
        this.textX = new JTextField();
        this.textX.setColumns(10);
        this.textX.setInputVerifier(new BetweenValidator(this, this.textX, -1.7976931348623157E308, Double.MAX_VALUE));
        this.textX.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (LocatorWindow.this.isInputValid()) {
                    LocatorWindow.this.centerMap();
                }
            }
        });
        FormUtils.addRowInGBL((JComponent)this.locatorPanel, 0, 0, labelX, (JComponent)this.textX);
        JLabel labelY = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.locator.LocatorWindow.coordinate-y")) + " :");
        this.textY = new JTextField();
        this.textY.setColumns(10);
        this.textY.setInputVerifier(new BetweenValidator(this, this.textY, -1.7976931348623157E308, Double.MAX_VALUE));
        FormUtils.addRowInGBL((JComponent)this.locatorPanel, 0, 30, labelY, (JComponent)this.textY);
        this.textY.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (LocatorWindow.this.isInputValid()) {
                    LocatorWindow.this.centerMap();
                }
            }
        });
        this.gotoButton = new JButton(I18N.getString("org.saig.jump.widgets.locator.LocatorWindow.Go"));
        this.gotoButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (LocatorWindow.this.isInputValid()) {
                    LocatorWindow.this.centerMap();
                }
            }
        });
        FormUtils.addRowInGBL(this.locatorPanel, 0, 60, this.gotoButton);
    }

    private void centerMap() {
        double y;
        double x;
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        try {
            x = new Double(this.textX.getText().trim());
        }
        catch (NumberFormatException e1) {
            try {
                x = new Double(this.textX.getText().trim().replaceAll(",", "."));
            }
            catch (NumberFormatException e) {
                return;
            }
        }
        try {
            y = new Double(this.textY.getText().trim());
        }
        catch (NumberFormatException e1) {
            try {
                y = new Double(this.textY.getText().trim().replaceAll(",", "."));
            }
            catch (NumberFormatException e) {
                return;
            }
        }
        Envelope currentEnvelope = context.getLayerViewPanel().getViewport().getEnvelopeInModelCoordinates();
        double width = currentEnvelope.getWidth() / 2.0;
        double height = currentEnvelope.getHeight() / 2.0;
        double minX = x - width;
        double minY = y - height;
        double maxX = x + width;
        double maxY = y + height;
        Envelope envelope = new Envelope(minX, maxX, minY, maxY);
        try {
            context.getLayerViewPanel().getViewport().zoom(envelope, true);
            Animations.drawExpandingRing(context.getLayerViewPanel().getViewport().toViewPoint(new Coordinate(x, y)), false, Color.BLUE, context.getLayerViewPanel(), new float[]{15.0f, 15.0f});
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public boolean isInputValid() {
        boolean solucion = true;
        solucion = this.textX.getInputVerifier().verify(this.textX) && this.textY.getInputVerifier().verify(this.textY);
        return solucion;
    }
}

