/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing.controllers;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.EditOptionsPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.editing.MultiClickToolTraceDialog;
import org.saig.jump.widgets.util.DialogFactory;

public class MultiClickToolTraceController
implements ChangeListener,
ActionListener {
    private static final Logger LOGGER = Logger.getLogger(MultiClickToolTraceController.class);
    private static MultiClickToolTraceController instance;
    private MultiClickToolTraceDialog dialog = new MultiClickToolTraceDialog(JUMPWorkbench.getFrameInstance());
    private MultiClickTool mcTool;
    private boolean listenersEnabled = false;
    private double acum;

    public static MultiClickToolTraceController getInstance() {
        if (instance == null) {
            instance = new MultiClickToolTraceController();
        }
        return instance;
    }

    private MultiClickToolTraceController() {
        this.dialog.setTitle(I18N.getString("org.saig.jump.plugin.editing.controllers.MultiClickToolTraceController.Last-segment"));
        this.dialog.getJSpinnerAngle().setModel(new SpinnerNumberModel(0.0, -360.0, 360.0, 1.0E-4));
        this.dialog.getJSpinnerLenght().setModel(new SpinnerNumberModel(0.0, 0.0, Double.MAX_VALUE, 1.0E-8));
        this.dialog.getJSpinnerAngle().addChangeListener(this);
        this.dialog.getJSpinnerLenght().addChangeListener(this);
        this.dialog.getJButtonFinishGesture().addActionListener(this);
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        LayerViewPanel lvp = context.getLayerViewPanel();
        Dimension localSize = this.dialog.getSize();
        if (lvp != null) {
            this.dialog.setLocation(lvp.getLocation().x + lvp.getSize().width - localSize.width, lvp.getLocation().y + lvp.getSize().height);
        }
    }

    public void reset() {
        this.acum = 0.0;
    }

    public void refresh(MultiClickTool tool, double angle, double length) {
        this.listenersEnabled = false;
        this.acum += length;
        this.dialog.getJSpinnerAngle().setValue(angle * 180.0 / Math.PI);
        this.dialog.getJSpinnerLenght().setValue(length);
        this.dialog.getjTextFieldAcum().setText(this.format(this.acum));
        this.mcTool = tool;
        this.listenersEnabled = true;
    }

    private String format(double d) {
        DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance(I18N.getLocale());
        formatter.setGroupingSize(3);
        formatter.setGroupingUsed(true);
        formatter.setMaximumFractionDigits(3);
        return formatter.format(d);
    }

    public void show() {
        if (!this.dialog.isVisible() && EditOptionsPanel.isShowAngleAndLenght()) {
            this.acum = 0.0;
            this.dialog.setVisible(true);
        }
    }

    public void hide() {
        this.dialog.setVisible(false);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (this.mcTool != null && this.listenersEnabled) {
            this.mcTool.changeLastSegment((Double)this.dialog.getJSpinnerAngle().getValue() * Math.PI / 180.0, (Double)this.dialog.getJSpinnerLenght().getValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.dialog.getJButtonFinishGesture()) {
            this.finishGesture();
        }
    }

    private void finishGesture() {
        try {
            this.mcTool.finishGesture();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), e.getMessage(), I18N.getString("org.saig.jump.plugin.editing.controllers.MultiClickToolTraceController.Error"));
        }
    }
}

