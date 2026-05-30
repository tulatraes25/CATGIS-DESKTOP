/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.jump.plugin.editing.ZMouseWheelManager;
import org.saig.jump.util.KosmoDesktopUtils;
import org.saig.jump.widgets.editing.ZJPanel;
import org.saig.jump.widgets.util.NumberSpinner;

public class ZManager {
    private ZJPanel panel = new ZJPanel();
    private static ZManager instance;
    public static final String KEY;
    public static final String USE_Z_EDITING;
    public static final String Z_STEP;
    private boolean forceZUse = false;
    private boolean defaultZValueActivated = false;
    private double defaultZValue = 0.0;
    private boolean zEditionActive;
    private ZMouseWheelManager mouseManager;

    static {
        KEY = ZManager.class.getName();
        USE_Z_EDITING = String.valueOf(KEY) + " - USE Z EDITING";
        Z_STEP = String.valueOf(KEY) + " - Z STEP";
    }

    private ZManager() {
        this.panel.getzSpinner().setStep(ZManager.getZStep());
        this.panel.getzSpinner().setEnabled(this.isEditingZUseActive());
        this.panel.getzStepJSpinner().setValue(ZManager.getZStep());
        this.panel.getzStepJSpinner().setEnabled(this.isEditingZUseActive());
        this.panel.getzStepJSpinner().addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext().getBlackboard()).put(Z_STEP, ZManager.this.panel.getzStepJSpinner().getValue());
                ZManager.this.panel.getzSpinner().setModel(new SpinnerNumberModel(ZManager.getActiveZ(), -1.7976931348623157E308, Double.MAX_VALUE, ZManager.getZStep()));
            }
        });
        this.panel.getUseZCoordinateCheckBox().setSelected(this.isEditingZUseActive());
        this.panel.getUseZCoordinateCheckBox().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext().getBlackboard()).put(USE_Z_EDITING, ZManager.this.panel.getUseZCoordinateCheckBox().isSelected());
                ZManager.this.panel.getzSpinner().setEnabled(ZManager.isZUseActive());
                ZManager.this.panel.getzStepJSpinner().setEnabled(ZManager.isZUseActive());
            }
        });
        this.mouseManager = new ZMouseWheelManager(){

            @Override
            public boolean isMouseWheelIntercepted() {
                return ZManager.this.isEditingZUseActive() && KosmoDesktopUtils.getEditableLayer() != null;
            }

            @Override
            public void adjustZToNotches(int notches) {
                NumberSpinner spinner = ZManager.this.panel.getzSpinner();
                Double newVal = (Double)(notches < 0 ? spinner.getNextValue() : spinner.getPreviousValue());
                spinner.setValue(newVal);
            }
        };
    }

    public static ZManager getInstance() {
        if (instance == null) {
            instance = new ZManager();
        }
        return instance;
    }

    public ZJPanel getPanel() {
        return this.panel;
    }

    public double getZ() {
        Container ancestor = this.panel.getTopLevelAncestor();
        if (this.isDefaultZValueActivated() && ancestor != null && !ancestor.isVisible()) {
            return this.defaultZValue;
        }
        return this.panel.getzSpinner().getDoubleValue();
    }

    public boolean isEditingZUseActive() {
        if (this.forceZUse) {
            return true;
        }
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(USE_Z_EDITING, false);
    }

    public static boolean isZUseActive() {
        return ZManager.getInstance().isEditingZUseActive();
    }

    public boolean isMouseWheelIntercepted() {
        return this.mouseManager.isMouseWheelIntercepted();
    }

    public void adjustZToNotches(int notches) {
        this.mouseManager.adjustZToNotches(notches);
    }

    public static double getZStep() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(Z_STEP, 1.0);
    }

    public static double getActiveZ() {
        return ZManager.getInstance().getZ();
    }

    public void setForceZUse(boolean forceZUse) {
        this.forceZUse = forceZUse;
        this.panel.getUseZCoordinateCheckBox().setSelected(forceZUse);
        if (forceZUse) {
            this.panel.getUseZCoordinateCheckBox().setEnabled(false);
            this.panel.getzSpinner().setEnabled(true);
            this.panel.getzStepJSpinner().setEnabled(true);
        } else {
            this.panel.getUseZCoordinateCheckBox().setEnabled(true);
            this.panel.getzSpinner().setEnabled(this.isEditingZUseActive());
            this.panel.getzStepJSpinner().setEnabled(this.isEditingZUseActive());
        }
    }

    public boolean isForceZUse() {
        return this.forceZUse;
    }

    public void activateDefaultZValue(double zVal) {
        this.defaultZValueActivated = true;
        this.defaultZValue = zVal;
    }

    public boolean isDefaultZValueActivated() {
        return this.defaultZValueActivated;
    }

    public void deactivateDefaultZValue() {
        this.defaultZValueActivated = false;
    }

    public void setzEditionActive(boolean zEditionActive) {
        this.zEditionActive = zEditionActive;
    }

    public boolean iszEditionActive() {
        return this.zEditionActive;
    }

    public ZMouseWheelManager getMouseManager() {
        return this.mouseManager;
    }

    public void setMouseManager(ZMouseWheelManager mouseManager) {
        this.mouseManager = mouseManager;
    }
}

