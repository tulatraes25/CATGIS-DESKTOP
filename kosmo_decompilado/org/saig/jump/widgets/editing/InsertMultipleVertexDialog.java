/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package org.saig.jump.widgets.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.InsertMultipleVertexPlugIn;
import org.saig.jump.widgets.util.NumberSpinner;

public class InsertMultipleVertexDialog
extends JDialog {
    public static final int OPCION_UN_PUNTO = 1;
    public static final int OPCION_N_PUNTOS = 2;
    public static final int OPCION_TODOS_PUNTOS = 3;
    private JRadioButton unPuntoRadioButton;
    private JRadioButton nPuntosRadioButton;
    private JRadioButton todosPuntosRadioButton;
    private ButtonGroup buttonGroup;
    private JLabel puntosLabel;
    private JLabel distanciaLabel;
    private NumberSpinner puntosNumberSpinner;
    private NumberSpinner distanciaNumberSpinner;
    private OKCancelPanel okCancelPanel;
    private boolean exitOk = false;

    public InsertMultipleVertexDialog(JFrame parent, boolean modal) {
        super((Frame)parent, modal);
        this.setTitle(InsertMultipleVertexPlugIn.NAME);
        this.setContentPane(this.getMainPanel());
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(mainPanel, 1, 0, this.getMetodosPanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.getOpcionesPanel());
        FormUtils.addRowInGBL(mainPanel, 3, 0, this.createOKCancelPanel());
        FormUtils.addFiller(mainPanel, 4, 0);
        return mainPanel;
    }

    private JPanel getMetodosPanel() {
        JPanel metodosPanel = new JPanel(new GridBagLayout());
        metodosPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "methods")));
        this.unPuntoRadioButton = new JRadioButton(I18N.getString(this.getClass(), "one-point-to-distance-x-from-selected-vertex"));
        this.unPuntoRadioButton.setSelected(true);
        this.unPuntoRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (InsertMultipleVertexDialog.this.unPuntoRadioButton.isSelected()) {
                    InsertMultipleVertexDialog.this.puntosNumberSpinner.setValue(1);
                    InsertMultipleVertexDialog.this.puntosNumberSpinner.setEnabled(false);
                    InsertMultipleVertexDialog.this.puntosLabel.setEnabled(false);
                }
            }
        });
        this.nPuntosRadioButton = new JRadioButton(I18N.getString(this.getClass(), "n-point-each-x-distance"));
        this.nPuntosRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (InsertMultipleVertexDialog.this.nPuntosRadioButton.isSelected()) {
                    InsertMultipleVertexDialog.this.puntosNumberSpinner.setValue(1);
                    InsertMultipleVertexDialog.this.puntosNumberSpinner.setEnabled(true);
                    InsertMultipleVertexDialog.this.puntosLabel.setEnabled(true);
                }
            }
        });
        this.todosPuntosRadioButton = new JRadioButton(I18N.getString(this.getClass(), "all-that-fits-into-geometry-each-x-distance"));
        this.todosPuntosRadioButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (InsertMultipleVertexDialog.this.todosPuntosRadioButton.isSelected()) {
                    InsertMultipleVertexDialog.this.puntosNumberSpinner.setValue(1);
                    InsertMultipleVertexDialog.this.puntosNumberSpinner.setEnabled(false);
                    InsertMultipleVertexDialog.this.puntosLabel.setEnabled(false);
                }
            }
        });
        this.buttonGroup = new ButtonGroup();
        this.buttonGroup.add(this.unPuntoRadioButton);
        this.buttonGroup.add(this.nPuntosRadioButton);
        this.buttonGroup.add(this.todosPuntosRadioButton);
        FormUtils.addRowInGBL(metodosPanel, 1, 0, this.unPuntoRadioButton);
        FormUtils.addRowInGBL(metodosPanel, 2, 0, this.nPuntosRadioButton);
        FormUtils.addRowInGBL(metodosPanel, 3, 0, this.todosPuntosRadioButton);
        return metodosPanel;
    }

    private JPanel getOpcionesPanel() {
        JPanel opcionesPanel = new JPanel(new GridBagLayout());
        opcionesPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString(this.getClass(), "options")));
        this.puntosNumberSpinner = new NumberSpinner(1, 1, 9999999, 1);
        this.distanciaNumberSpinner = new NumberSpinner(1.0, 0.0, 9999999.0, 0.1);
        Unit<Length> unidadDeLongitud = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel().getMapLengthUnit();
        this.puntosLabel = new JLabel(I18N.getString(this.getClass(), "number-of-points"));
        this.distanciaLabel = new JLabel(String.valueOf(I18N.getString(this.getClass(), "distance")) + " (" + unidadDeLongitud + ")");
        this.puntosNumberSpinner.setEnabled(false);
        this.puntosLabel.setEnabled(false);
        FormUtils.addRowInGBL((JComponent)opcionesPanel, 1, 0, (JComponent)this.puntosLabel, false, false);
        FormUtils.addRowInGBL((JComponent)opcionesPanel, 2, 0, (JComponent)this.puntosNumberSpinner, false, false);
        FormUtils.addRowInGBL(opcionesPanel, 1, 2, this.distanciaLabel);
        FormUtils.addRowInGBL(opcionesPanel, 2, 2, this.distanciaNumberSpinner);
        return opcionesPanel;
    }

    public boolean isExitOk() {
        return this.exitOk;
    }

    private JComponent createOKCancelPanel() {
        this.okCancelPanel = new OKCancelPanel();
        this.okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                boolean error = false;
                if (InsertMultipleVertexDialog.this.okCancelPanel.wasOKPressed()) {
                    if (!error) {
                        InsertMultipleVertexDialog.this.exitOk = true;
                        InsertMultipleVertexDialog.this.setVisible(false);
                    }
                } else {
                    InsertMultipleVertexDialog.this.exitOk = false;
                    InsertMultipleVertexDialog.this.okCancelPanel.setOKPressed(false);
                    InsertMultipleVertexDialog.this.setVisible(false);
                }
            }
        });
        return this.okCancelPanel;
    }

    public int getSelectedOption() {
        if (this.unPuntoRadioButton.isSelected()) {
            return 1;
        }
        if (this.nPuntosRadioButton.isSelected()) {
            return 2;
        }
        return 3;
    }

    public int getNPuntos() {
        return this.puntosNumberSpinner.getIntValue();
    }

    public double getDistancia() {
        return this.distanciaNumberSpinner.getDoubleValue();
    }
}

