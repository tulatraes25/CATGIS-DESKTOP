/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  javax.vecmath.Point2d
 *  javax.vecmath.Tuple2d
 *  org.geotools.math.Line
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point2d;
import javax.vecmath.Tuple2d;
import org.geotools.math.Line;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.ModifySegmentTool;
import org.saig.jump.tools.editing.Utils;
import org.saig.jump.widgets.util.NumberSpinner;

public class ModifySegmentDialog
extends JDialog {
    private boolean exitOk = false;
    private OKCancelPanel okCancelPanel;
    private NumberSpinner incrementDistanceSpinner;
    private NumberSpinner totalSegmentDistanceSpinner;
    private Coordinate modelDestination;
    private boolean updatingSpinners = false;
    private final Coordinate lineStartCoord;
    private final Coordinate lineEndCoord;

    public ModifySegmentDialog(WorkbenchFrame parent, boolean modal, Coordinate modelDestination, Coordinate lineStartCoord, Coordinate lineEndCoord) {
        super((Frame)parent, modal);
        this.modelDestination = modelDestination;
        this.lineStartCoord = lineStartCoord;
        this.lineEndCoord = lineEndCoord;
        this.setTitle(ModifySegmentTool.NAME);
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
        Unit<Length> unidadDeLongitud = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getMapLengthUnit();
        this.updatingSpinners = true;
        JPanel dataPanel = new JPanel(new GridBagLayout());
        JPanel distancePanel = new JPanel(new GridBagLayout());
        distancePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "distance")));
        this.incrementDistanceSpinner = new NumberSpinner(this.calculateIncrementDistance(this.getDestinationCoordinate()), -9.999999999E9, 9.999999999E9, 0.1);
        this.incrementDistanceSpinner.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                if (!ModifySegmentDialog.this.updatingSpinners) {
                    Point2d pInicial = new Point2d(((ModifySegmentDialog)ModifySegmentDialog.this).lineStartCoord.x, ((ModifySegmentDialog)ModifySegmentDialog.this).lineStartCoord.y);
                    Point2d pFinal = new Point2d(((ModifySegmentDialog)ModifySegmentDialog.this).lineEndCoord.x, ((ModifySegmentDialog)ModifySegmentDialog.this).lineEndCoord.y);
                    Point2d pInterpolado = new Point2d();
                    double distance = pInicial.distance(pFinal);
                    pInterpolado.interpolate((Tuple2d)pInicial, (Tuple2d)pFinal, (distance + ModifySegmentDialog.this.incrementDistanceSpinner.getDoubleValue()) / distance);
                    ModifySegmentDialog.this.modelDestination = new Coordinate(pInterpolado.x, pInterpolado.y, ((ModifySegmentDialog)ModifySegmentDialog.this).lineEndCoord.z);
                    ModifySegmentDialog.this.updatingSpinners = true;
                    ModifySegmentDialog.this.totalSegmentDistanceSpinner.setValue(ModifySegmentDialog.this.calculateTotalSegmentDistance(ModifySegmentDialog.this.getDestinationCoordinate()));
                    ModifySegmentDialog.this.updatingSpinners = false;
                }
            }
        });
        this.totalSegmentDistanceSpinner = new NumberSpinner(this.calculateTotalSegmentDistance(this.getDestinationCoordinate()), -9.999999999E9, 9.999999999E9, 0.1);
        this.totalSegmentDistanceSpinner.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                if (!ModifySegmentDialog.this.updatingSpinners) {
                    Point2d pInicial = new Point2d(((ModifySegmentDialog)ModifySegmentDialog.this).lineStartCoord.x, ((ModifySegmentDialog)ModifySegmentDialog.this).lineStartCoord.y);
                    Point2d pFinal = new Point2d(((ModifySegmentDialog)ModifySegmentDialog.this).lineEndCoord.x, ((ModifySegmentDialog)ModifySegmentDialog.this).lineEndCoord.y);
                    Point2d pInterpolado = new Point2d();
                    double distance = pInicial.distance(pFinal);
                    pInterpolado.interpolate((Tuple2d)pInicial, (Tuple2d)pFinal, ModifySegmentDialog.this.totalSegmentDistanceSpinner.getDoubleValue() / distance);
                    ModifySegmentDialog.this.modelDestination = new Coordinate(pInterpolado.x, pInterpolado.y, ((ModifySegmentDialog)ModifySegmentDialog.this).lineEndCoord.z);
                    ModifySegmentDialog.this.updatingSpinners = true;
                    ModifySegmentDialog.this.incrementDistanceSpinner.setValue(ModifySegmentDialog.this.calculateIncrementDistance(ModifySegmentDialog.this.getDestinationCoordinate()));
                    ModifySegmentDialog.this.updatingSpinners = false;
                }
            }
        });
        FormUtils.addRowInGBL((JComponent)distancePanel, 1, 0, String.valueOf(I18N.getString(this.getClass(), "increment")) + " (" + unidadDeLongitud + ")", (JComponent)this.incrementDistanceSpinner);
        FormUtils.addRowInGBL((JComponent)distancePanel, 2, 0, String.valueOf(I18N.getString(this.getClass(), "segment-total")) + " (" + unidadDeLongitud + ")", (JComponent)this.totalSegmentDistanceSpinner);
        FormUtils.addRowInGBL(dataPanel, 2, 0, distancePanel);
        this.updatingSpinners = false;
        return dataPanel;
    }

    private JComponent createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean error = false;
                if (ModifySegmentDialog.this.okCancelPanel.wasOKPressed()) {
                    ModifySegmentDialog.this.exitOk = true;
                    if (!ModifySegmentDialog.this.isInputValid()) {
                        error = true;
                    }
                } else {
                    ModifySegmentDialog.this.exitOk = false;
                }
                if (!ModifySegmentDialog.this.exitOk || ModifySegmentDialog.this.exitOk && !error) {
                    ModifySegmentDialog.this.setVisible(false);
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

    public Coordinate getDestinationCoordinate() {
        return new Coordinate(this.modelDestination.x, this.modelDestination.y, this.lineEndCoord.z);
    }

    private Line getLine() {
        Line line = new Line();
        line.setLine((Point2D)new Point2D.Double(this.lineStartCoord.x, this.lineStartCoord.y), (Point2D)new Point2D.Double(this.lineEndCoord.x, this.lineEndCoord.y));
        return line;
    }

    private double calculateIncrementDistance(Coordinate tmpDestinationCoord) {
        return this.calculateTotalSegmentDistance(tmpDestinationCoord) - this.lineStartCoord.distance(this.lineEndCoord);
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

