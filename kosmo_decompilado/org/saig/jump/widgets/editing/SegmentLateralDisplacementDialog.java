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
import java.awt.geom.Point2D;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.SegmentLateralDisplacementTool;
import org.saig.jump.tools.editing.Utils;
import org.saig.jump.widgets.util.NumberSpinner;

public class SegmentLateralDisplacementDialog
extends JDialog {
    private boolean exitOk = false;
    private OKCancelPanel okCancelPanel;
    private NumberSpinner xNumberSpinner;
    private NumberSpinner yNumberSpinner;
    private NumberSpinner incrementDistanceSpinner;
    private boolean updatingSpinners = false;
    private final Coordinate lineStartCoord;
    private final Coordinate lineEndCoord;
    private JCheckBox withMouseCheckBox;
    private JCheckBox perpendicularCheckBox;

    public SegmentLateralDisplacementDialog(WorkbenchFrame parent, boolean modal, Coordinate lineStartCoord, Coordinate lineEndCoord) {
        super((Frame)parent, modal);
        this.lineStartCoord = lineStartCoord;
        this.lineEndCoord = lineEndCoord;
        this.setTitle(SegmentLateralDisplacementTool.NAME);
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
        this.refreshEnabled();
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getDataPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.createOKCancelPanel());
        FormUtils.addFiller(mainPanel, 3, 0);
        return mainPanel;
    }

    private JPanel getDataPanel() {
        Unit<Length> unidadDeLongitud = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getMapLengthUnit();
        this.updatingSpinners = true;
        JPanel dataPanel = new JPanel(new GridBagLayout());
        JPanel checkPanel = new JPanel(new GridBagLayout());
        checkPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "options")));
        this.withMouseCheckBox = new JCheckBox(I18N.getString(this.getClass(), "with-mouse"));
        this.withMouseCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SegmentLateralDisplacementDialog.this.refreshEnabled();
            }
        });
        this.perpendicularCheckBox = new JCheckBox(I18N.getString(this.getClass(), "perpendicular"));
        this.perpendicularCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SegmentLateralDisplacementDialog.this.refreshEnabled();
                if (SegmentLateralDisplacementDialog.this.perpendicularCheckBox.isSelected()) {
                    SegmentLateralDisplacementDialog.this.refreshXAndY(SegmentLateralDisplacementDialog.this.incrementDistanceSpinner.getDoubleValue());
                }
            }
        });
        this.perpendicularCheckBox.setEnabled(!this.lineStartCoord.equals2D(this.lineEndCoord));
        FormUtils.addRowInGBL((JComponent)checkPanel, 1, 0, (JComponent)this.withMouseCheckBox, true, false);
        FormUtils.addRowInGBL((JComponent)checkPanel, 2, 0, (JComponent)this.perpendicularCheckBox, true, false);
        JPanel coordPanel = new JPanel(new GridBagLayout());
        ChangeListener refreshDistanceChangeListener = new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                if (!SegmentLateralDisplacementDialog.this.updatingSpinners) {
                    double distance = Math.sqrt(Math.pow(SegmentLateralDisplacementDialog.this.xNumberSpinner.getDoubleValue(), 2.0) + Math.pow(SegmentLateralDisplacementDialog.this.yNumberSpinner.getDoubleValue(), 2.0));
                    SegmentLateralDisplacementDialog.this.updatingSpinners = true;
                    SegmentLateralDisplacementDialog.this.incrementDistanceSpinner.setValue(distance);
                    SegmentLateralDisplacementDialog.this.updatingSpinners = false;
                }
            }
        };
        coordPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "increment")));
        this.xNumberSpinner = new NumberSpinner(0.0, -9.999999999E9, 9.999999999E9, 0.1);
        this.xNumberSpinner.addChangeListener(refreshDistanceChangeListener);
        this.yNumberSpinner = new NumberSpinner(0.0, -9.999999999E9, 9.999999999E9, 0.1);
        this.yNumberSpinner.addChangeListener(refreshDistanceChangeListener);
        FormUtils.addRowInGBL((JComponent)coordPanel, 1, 0, I18N.getMessage(this.getClass(), "x-{0}", new Object[]{unidadDeLongitud}), (JComponent)this.xNumberSpinner);
        FormUtils.addRowInGBL((JComponent)coordPanel, 2, 0, I18N.getMessage(this.getClass(), "y-{0}", new Object[]{unidadDeLongitud}), (JComponent)this.yNumberSpinner);
        JPanel distancePanel = new JPanel(new GridBagLayout());
        distancePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "distance")));
        this.incrementDistanceSpinner = new NumberSpinner(0.0, -9.999999999E9, 9.999999999E9, 0.1);
        this.incrementDistanceSpinner.setEnabled(false);
        this.incrementDistanceSpinner.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                if (!SegmentLateralDisplacementDialog.this.updatingSpinners) {
                    SegmentLateralDisplacementDialog.this.refreshXAndY(SegmentLateralDisplacementDialog.this.incrementDistanceSpinner.getDoubleValue());
                }
            }
        });
        FormUtils.addRowInGBL((JComponent)distancePanel, 1, 0, I18N.getMessage(this.getClass(), "increment-{0}", new Object[]{unidadDeLongitud}), (JComponent)this.incrementDistanceSpinner);
        FormUtils.addRowInGBL(dataPanel, 0, 0, checkPanel);
        FormUtils.addRowInGBL(dataPanel, 1, 0, coordPanel);
        FormUtils.addRowInGBL(dataPanel, 2, 0, distancePanel);
        this.updatingSpinners = false;
        return dataPanel;
    }

    private void refreshXAndY(double distance) {
        Point2D.Double perpendicularIncrementAt = SegmentLateralDisplacementTool.getPerpendicularIncrementAt(this.lineStartCoord, this.lineEndCoord, distance);
        this.updatingSpinners = true;
        this.xNumberSpinner.setValue(perpendicularIncrementAt.x);
        this.yNumberSpinner.setValue(perpendicularIncrementAt.y);
        this.updatingSpinners = false;
    }

    private void refreshEnabled() {
        this.xNumberSpinner.setEnabled(!this.withMouseCheckBox.isSelected() && !this.perpendicularCheckBox.isSelected());
        this.yNumberSpinner.setEnabled(!this.withMouseCheckBox.isSelected() && !this.perpendicularCheckBox.isSelected());
        this.incrementDistanceSpinner.setEnabled(!this.withMouseCheckBox.isSelected() && this.perpendicularCheckBox.isSelected());
    }

    private JComponent createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean error = false;
                if (SegmentLateralDisplacementDialog.this.okCancelPanel.wasOKPressed()) {
                    SegmentLateralDisplacementDialog.this.exitOk = true;
                    if (!SegmentLateralDisplacementDialog.this.isInputValid()) {
                        error = true;
                    }
                } else {
                    SegmentLateralDisplacementDialog.this.exitOk = false;
                }
                if (!SegmentLateralDisplacementDialog.this.exitOk || SegmentLateralDisplacementDialog.this.exitOk && !error) {
                    SegmentLateralDisplacementDialog.this.setVisible(false);
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

    public boolean isWithMouseSelected() {
        return this.withMouseCheckBox.isSelected();
    }

    public boolean isPerpendicularSelected() {
        return this.perpendicularCheckBox.isSelected();
    }

    public double getIncrX() {
        return this.xNumberSpinner.getDoubleValue();
    }

    public double getIncrY() {
        return this.yNumberSpinner.getDoubleValue();
    }

    private double calculateTotalSegmentDistance(Coordinate tmpDestinationCoord) {
        double angleNew;
        double distance = this.lineStartCoord.distance(tmpDestinationCoord);
        double angleLine = Utils.getAngle(this.lineStartCoord.x, this.lineStartCoord.y, this.lineEndCoord.x, this.lineEndCoord.y);
        double diffAngle = Math.abs(angleLine - (angleNew = Utils.getAngle(this.lineStartCoord.x, this.lineStartCoord.y, tmpDestinationCoord.x, tmpDestinationCoord.y)));
        if (diffAngle < 1.0 && diffAngle > -1.0) {
            return distance;
        }
        return -distance;
    }
}

