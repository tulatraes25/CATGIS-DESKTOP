/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package org.saig.jump.widgets.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.IncrementalMoveVertexTool;
import org.saig.jump.widgets.util.NumberSpinner;

public class IncrementalMoveVertexDialog
extends JDialog {
    private boolean exitOk = false;
    private OKCancelPanel okCancelPanel;
    private NumberSpinner xNumberSpinner;
    private NumberSpinner yNumberSpinner;
    private final Coordinate referenceValue;
    private final Coordinate modelDestination;

    public IncrementalMoveVertexDialog(WorkbenchFrame parent, boolean modal, Coordinate referenceValue, Coordinate modelDestination) {
        super((Frame)parent, modal);
        this.referenceValue = referenceValue;
        this.modelDestination = modelDestination;
        this.setTitle(IncrementalMoveVertexTool.NAME);
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getDataPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.createOKCancelPanel());
        FormUtils.addFiller(mainPanel, 3, 0);
        return mainPanel;
    }

    private JPanel getDataPanel() {
        JPanel dataPanel = new JPanel(new GridBagLayout());
        this.xNumberSpinner = new NumberSpinner(this.modelDestination.x - this.referenceValue.x, -9.999999999E9, 9.999999999E9, 0.1);
        this.yNumberSpinner = new NumberSpinner(this.modelDestination.y - this.referenceValue.y, -9.999999999E9, 9.999999999E9, 0.1);
        Unit<Length> unidadDeLongitud = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getMapLengthUnit();
        FormUtils.addRowInGBL((JComponent)dataPanel, 1, 0, String.valueOf(I18N.getString(this.getClass(), "x-increment")) + " (" + unidadDeLongitud + ")", (JComponent)this.xNumberSpinner);
        FormUtils.addRowInGBL((JComponent)dataPanel, 2, 0, String.valueOf(I18N.getString(this.getClass(), "y-increment")) + " (" + unidadDeLongitud + ")", (JComponent)this.yNumberSpinner);
        return dataPanel;
    }

    private JComponent createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean error = false;
                if (IncrementalMoveVertexDialog.this.okCancelPanel.wasOKPressed()) {
                    IncrementalMoveVertexDialog.this.exitOk = true;
                    if (!IncrementalMoveVertexDialog.this.isInputValid()) {
                        error = true;
                    }
                } else {
                    IncrementalMoveVertexDialog.this.exitOk = false;
                }
                if (!IncrementalMoveVertexDialog.this.exitOk || IncrementalMoveVertexDialog.this.exitOk && !error) {
                    IncrementalMoveVertexDialog.this.setVisible(false);
                }
            }
        });
        return this.okCancelPanel;
    }

    protected boolean isInputValid() {
        return true;
    }

    public boolean isExitOk() {
        return this.exitOk;
    }

    public Coordinate getIncrementedModelDetination() {
        return new Coordinate(this.referenceValue.x + this.xNumberSpinner.getDoubleValue(), this.referenceValue.y + this.yNumberSpinner.getDoubleValue(), this.referenceValue.z);
    }

    public double getXIncrement() {
        return this.xNumberSpinner.getDoubleValue();
    }

    public double getYIncrement() {
        return this.yNumberSpinner.getDoubleValue();
    }
}

