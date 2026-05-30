/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.JXColorSelectionButton
 *  org.jdesktop.swingx.JXMultiThumbSlider
 *  org.jdesktop.swingx.JXPanel
 *  org.jdesktop.swingx.action.AbstractActionExt
 *  org.jdesktop.swingx.color.ColorUtil
 *  org.jdesktop.swingx.color.GradientThumbRenderer
 *  org.jdesktop.swingx.color.GradientTrackRenderer
 *  org.jdesktop.swingx.multislider.Thumb
 *  org.jdesktop.swingx.multislider.ThumbListener
 *  org.jdesktop.swingx.multislider.ThumbRenderer
 *  org.jdesktop.swingx.multislider.TrackRenderer
 */
package org.jdesktop.swingx;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jdesktop.swingx.JXColorSelectionButton;
import org.jdesktop.swingx.JXMultiThumbSlider;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.color.GradientPreviewPanel;
import org.jdesktop.swingx.color.GradientThumbRenderer;
import org.jdesktop.swingx.color.GradientTrackRenderer;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.multislider.ThumbListener;
import org.jdesktop.swingx.multislider.ThumbRenderer;
import org.jdesktop.swingx.multislider.TrackRenderer;
import org.saig.jump.lang.I18N;

public class JXGradientChooser
extends JXPanel {
    private static final long serialVersionUID = 1L;
    private boolean changeInProgress;
    private JXMultiThumbSlider<Color> slider;
    private JButton deleteThumbButton;
    private JButton addThumbButton;
    private JTextField colorField;
    private JXColorSelectionButton changeColorButton;
    private JSpinner colorLocationSpinner;
    private JSpinner alphaSpinner;
    private JSlider alphaSlider;
    private JComboBox styleCombo;
    private GradientPreviewPanel gradientPreview;
    private JRadioButton noCycleRadio;
    private JRadioButton reflectedRadio;
    private JRadioButton repeatedRadio;
    private JCheckBox reversedCheck;
    private MultipleGradientPaint gradient;
    private boolean thumbsMoving = false;
    private Logger log = Logger.getLogger(JXGradientChooser.class.getName());
    private JPanel topPanel;
    private JPanel previewPanel;

    public JXGradientChooser() {
        this.initComponents2();
    }

    public MultipleGradientPaint getGradient() {
        return this.gradient;
    }

    public void setGradient(MultipleGradientPaint mgrad) {
        if (this.gradient == mgrad) {
            return;
        }
        float[] fracts = mgrad.getFractions();
        Color[] colors = mgrad.getColors();
        if (!this.thumbsMoving) {
            int i;
            if (this.slider.getModel().getThumbCount() != mgrad.getColors().length) {
                while (this.slider.getModel().getThumbCount() > 0) {
                    this.slider.getModel().removeThumb(0);
                }
                i = 0;
                while (i < fracts.length) {
                    this.slider.getModel().addThumb(fracts[i], (Object)colors[i]);
                    ++i;
                }
            } else {
                i = 0;
                while (i < fracts.length) {
                    this.slider.getModel().getThumbAt(i).setObject((Object)colors[i]);
                    this.slider.getModel().getThumbAt(i).setPosition(fracts[i]);
                    ++i;
                }
            }
        } else {
            this.log.fine("not updating because it's moving");
        }
        if (mgrad instanceof RadialGradientPaint) {
            if (this.styleCombo.getSelectedItem() != GradientStyle.Radial) {
                this.styleCombo.setSelectedItem((Object)GradientStyle.Radial);
            }
        } else if (this.styleCombo.getSelectedItem() != GradientStyle.Linear) {
            this.styleCombo.setSelectedItem((Object)GradientStyle.Linear);
        }
        if (mgrad.getCycleMethod() == MultipleGradientPaint.CycleMethod.REFLECT) {
            this.reflectedRadio.setSelected(true);
            this.gradientPreview.setReflected(true);
        }
        if (mgrad.getCycleMethod() == MultipleGradientPaint.CycleMethod.REPEAT) {
            this.repeatedRadio.setSelected(true);
            this.gradientPreview.setRepeated(true);
        }
        this.gradientPreview.setGradient(mgrad);
        MultipleGradientPaint old = this.getGradient();
        this.gradient = mgrad;
        this.firePropertyChange("gradient", old, this.getGradient());
        this.repaint();
    }

    private void recalcGradientFromStops() {
        if (!this.changeInProgress) {
            this.changeInProgress = true;
            this.setGradient(this.gradientPreview.calculateGradient());
            this.changeInProgress = false;
        }
    }

    private void updateFromStop(Thumb<Color> thumb) {
        if (thumb == null) {
            this.updateFromStop(-1, -1.0f, Color.black);
        } else {
            this.updateFromStop(1, thumb.getPosition(), (Color)thumb.getObject());
        }
    }

    private void updateFromStop(int thumb, float position, Color color) {
        this.log.fine("updating: " + thumb + " " + position + " " + color);
        if (thumb == -1) {
            this.colorLocationSpinner.setEnabled(false);
            this.alphaSpinner.setEnabled(false);
            this.alphaSlider.setEnabled(false);
            this.colorField.setEnabled(false);
            this.changeColorButton.setEnabled(false);
            this.changeColorButton.setBackground(Color.black);
            this.deleteThumbButton.setEnabled(false);
        } else {
            this.colorLocationSpinner.setEnabled(true);
            this.alphaSpinner.setEnabled(true);
            this.alphaSlider.setEnabled(true);
            this.colorField.setEnabled(true);
            this.changeColorButton.setEnabled(true);
            this.colorLocationSpinner.setValue((int)(100.0f * position));
            this.colorField.setText(Integer.toHexString(color.getRGB()).substring(2));
            this.alphaSpinner.setValue(color.getAlpha() * 100 / 255);
            this.alphaSlider.setValue(color.getAlpha() * 100 / 255);
            this.changeColorButton.setBackground(color);
            this.deleteThumbButton.setEnabled(true);
        }
        this.updateDeleteButtons();
        this.recalcGradientFromStops();
    }

    private void updateDeleteButtons() {
        if (this.slider.getModel().getThumbCount() <= 2) {
            this.deleteThumbButton.setEnabled(false);
        }
    }

    private void updateGradientProperty() {
        this.firePropertyChange("gradient", null, this.getGradient());
        this.gradientPreview.repaint();
    }

    private void initComponents() {
        this.slider = new JXMultiThumbSlider();
        this.gradientPreview = new GradientPreviewPanel();
        this.gradientPreview.setMultiThumbModel(this.slider.getModel());
        ButtonGroup typeGroup = new ButtonGroup();
        JPanel jPanel1 = new JPanel();
        this.topPanel = new JPanel();
        JPanel jPanel2 = new JPanel();
        JLabel jLabel1 = new JLabel();
        JLabel jLabel5 = new JLabel();
        this.colorField = new JTextField();
        JLabel jLabel2 = new JLabel();
        JLabel jLabel6 = new JLabel();
        this.colorLocationSpinner = new JSpinner();
        JLabel jLabel4 = new JLabel();
        JLabel jLabel7 = new JLabel();
        this.alphaSpinner = new JSpinner();
        this.changeColorButton = new JXColorSelectionButton();
        this.alphaSlider = new JSlider();
        JPanel jPanel4 = new JPanel();
        this.addThumbButton = new JButton();
        this.deleteThumbButton = new JButton();
        this.previewPanel = new JPanel();
        JPanel jPanel3 = new JPanel();
        JLabel jLabel8 = new JLabel();
        this.styleCombo = new JComboBox();
        JLabel jLabel9 = new JLabel();
        this.noCycleRadio = new JRadioButton();
        this.reflectedRadio = new JRadioButton();
        this.repeatedRadio = new JRadioButton();
        this.reversedCheck = new JCheckBox();
        jPanel1.setLayout(new GridBagLayout());
        this.topPanel.setLayout(new GridBagLayout());
        this.topPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Gradient")));
        jPanel2.setLayout(new GridBagLayout());
        jLabel1.setText(String.valueOf(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Color")) + ":");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel2.add((Component)jLabel1, gridBagConstraints);
        jLabel5.setText("#");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(4, 0, 4, 4);
        jPanel2.add((Component)jLabel5, gridBagConstraints);
        this.colorField.setColumns(6);
        this.colorField.setEnabled(false);
        this.colorField.setPreferredSize(null);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = 2;
        jPanel2.add((Component)this.colorField, gridBagConstraints);
        jLabel2.setText(String.valueOf(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Location")) + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel2.add((Component)jLabel2, gridBagConstraints);
        jLabel6.setText("%");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        jPanel2.add((Component)jLabel6, gridBagConstraints);
        this.colorLocationSpinner.setEnabled(false);
        this.colorLocationSpinner.setPreferredSize(null);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = 2;
        jPanel2.add((Component)this.colorLocationSpinner, gridBagConstraints);
        jLabel4.setText(String.valueOf(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Opacity")) + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel2.add((Component)jLabel4, gridBagConstraints);
        jLabel7.setText("%");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel2.add((Component)jLabel7, gridBagConstraints);
        this.alphaSpinner.setEnabled(false);
        this.alphaSpinner.setPreferredSize(null);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = 2;
        jPanel2.add((Component)this.alphaSpinner, gridBagConstraints);
        this.changeColorButton.setText("00");
        this.changeColorButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        jPanel2.add((Component)this.changeColorButton, gridBagConstraints);
        this.alphaSlider.setEnabled(false);
        this.alphaSlider.setPreferredSize(new Dimension(20, 25));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = 2;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add((Component)this.alphaSlider, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        this.topPanel.add((Component)jPanel2, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = 2;
        this.topPanel.add((Component)this.slider, gridBagConstraints);
        jPanel4.setLayout(new GridLayout(1, 0, 2, 0));
        this.addThumbButton.setText(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Add"));
        jPanel4.add(this.addThumbButton);
        this.deleteThumbButton.setText(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Delete"));
        jPanel4.add(this.deleteThumbButton);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        this.topPanel.add((Component)jPanel4, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 2;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add((Component)this.topPanel, gridBagConstraints);
        this.previewPanel.setLayout(new GridBagLayout());
        this.previewPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Preview")));
        jPanel3.setLayout(new GridBagLayout());
        jLabel8.setText(String.valueOf(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Style")) + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel3.add((Component)jLabel8, gridBagConstraints);
        this.styleCombo.setModel(new DefaultComboBoxModel<String>(new String[]{I18N.getString("org.jdesktop.swingx.JXGradientChooser.Linear"), I18N.getString("org.jdesktop.swingx.JXGradientChooser.Radial")}));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel3.add((Component)this.styleCombo, gridBagConstraints);
        jLabel9.setText(String.valueOf(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Type")) + ":");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel3.add((Component)jLabel9, gridBagConstraints);
        typeGroup.add(this.noCycleRadio);
        this.noCycleRadio.setSelected(true);
        this.noCycleRadio.setText(I18N.getString("org.jdesktop.swingx.JXGradientChooser.None"));
        this.noCycleRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.noCycleRadio.setMargin(new Insets(0, 0, 0, 0));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel3.add((Component)this.noCycleRadio, gridBagConstraints);
        typeGroup.add(this.reflectedRadio);
        this.reflectedRadio.setText(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Reflect"));
        this.reflectedRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.reflectedRadio.setMargin(new Insets(0, 0, 0, 0));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel3.add((Component)this.reflectedRadio, gridBagConstraints);
        typeGroup.add(this.repeatedRadio);
        this.repeatedRadio.setText(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Repeat"));
        this.repeatedRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.repeatedRadio.setMargin(new Insets(0, 0, 0, 0));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel3.add((Component)this.repeatedRadio, gridBagConstraints);
        this.reversedCheck.setText(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Reverse"));
        this.reversedCheck.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        this.reversedCheck.setMargin(new Insets(0, 0, 0, 0));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = new Insets(4, 4, 4, 4);
        jPanel3.add((Component)this.reversedCheck, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = 18;
        this.previewPanel.add((Component)jPanel3, gridBagConstraints);
        this.gradientPreview.setBorder(BorderFactory.createEtchedBorder());
        this.gradientPreview.setPreferredSize(new Dimension(130, 130));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 1;
        gridBagConstraints.weightx = 10.0;
        gridBagConstraints.weighty = 10.0;
        this.previewPanel.add((Component)((Object)this.gradientPreview), gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = 1;
        gridBagConstraints.anchor = 11;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add((Component)this.previewPanel, gridBagConstraints);
    }

    private void initComponents2() {
        this.initComponents();
        this.setLayout(new BorderLayout());
        this.add(this.topPanel, "North");
        this.add(this.previewPanel, "Center");
        AddThumbAction addThumbAction = new AddThumbAction();
        DeleteThumbAction deleteThumbAction = new DeleteThumbAction();
        deleteThumbAction.setEnabled(false);
        ActionMap actions = this.getActionMap();
        actions.put("add-thumb", (Action)((Object)addThumbAction));
        actions.put("delete-thumb", (Action)((Object)deleteThumbAction));
        this.addThumbButton.setAction((Action)((Object)addThumbAction));
        this.deleteThumbButton.setAction((Action)((Object)deleteThumbAction));
        this.changeColorButton.addPropertyChangeListener("background", new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                JXGradientChooser.this.selectColorForThumb();
                JXGradientChooser.this.updateGradientProperty();
            }
        });
        this.colorLocationSpinner.addChangeListener(new ChangeLocationListener());
        ChangeAlphaListener changeAlphaListener = new ChangeAlphaListener();
        this.alphaSpinner.addChangeListener(changeAlphaListener);
        this.alphaSlider.addChangeListener(changeAlphaListener);
        RepaintOnEventListener repaintListener = new RepaintOnEventListener();
        this.styleCombo.addItemListener(repaintListener);
        this.styleCombo.setModel(new DefaultComboBoxModel<GradientStyle>(GradientStyle.values()));
        this.noCycleRadio.addActionListener(repaintListener);
        this.reflectedRadio.addActionListener(repaintListener);
        this.repeatedRadio.addActionListener(repaintListener);
        this.reversedCheck.addActionListener(repaintListener);
        this.gradientPreview.picker = this;
        SpinnerNumberModel alpha_model = new SpinnerNumberModel(100, 0, 100, 1);
        this.alphaSpinner.setModel(alpha_model);
        SpinnerNumberModel location_model = new SpinnerNumberModel(100, 0, 100, 1);
        this.colorLocationSpinner.setModel(location_model);
        this.slider.setOpaque(false);
        this.slider.setPreferredSize(new Dimension(100, 35));
        this.slider.getModel().setMinimumValue(0.0f);
        this.slider.getModel().setMaximumValue(1.0f);
        this.slider.getModel().addThumb(0.0f, (Object)Color.black);
        this.slider.getModel().addThumb(0.5f, (Object)Color.red);
        this.slider.getModel().addThumb(1.0f, (Object)Color.white);
        this.slider.setThumbRenderer((ThumbRenderer)new GradientThumbRenderer());
        this.slider.setTrackRenderer((TrackRenderer)new GradientTrackRenderer());
        this.slider.addMultiThumbListener((ThumbListener)new StopListener());
        this.gradientPreview.addPropertyChangeListener("gradient", new PropertyChangeListener(){

            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                JXGradientChooser.this.recalcGradientFromStops();
            }
        });
        this.recalcGradientFromStops();
    }

    private void selectColorForThumb() {
        int index = this.slider.getSelectedIndex();
        if (index >= 0) {
            Color color = this.changeColorButton.getBackground();
            this.slider.getModel().getThumbAt(index).setObject((Object)color);
            this.updateFromStop(index, this.slider.getModel().getThumbAt(index).getPosition(), color);
        }
    }

    public static MultipleGradientPaint showDialog(Component comp, String title, MultipleGradientPaint mgrad) {
        Component root = SwingUtilities.getRoot(comp);
        final JDialog dialog = new JDialog((JFrame)root, title, true);
        JXGradientChooser picker = new JXGradientChooser();
        if (mgrad != null) {
            picker.setGradient(mgrad);
        }
        dialog.add((Component)((Object)picker));
        JPanel panel = new JPanel();
        JButton cancel = new JButton(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Cancel"));
        cancel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
            }
        });
        JButton okay = new JButton(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Ok"));
        okay.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dialog.setVisible(false);
            }
        });
        okay.setDefaultCapable(true);
        GridLayout gl = new GridLayout();
        gl.setHgap(2);
        panel.setLayout(gl);
        panel.add(cancel);
        panel.add(okay);
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = 13;
        gbc.weightx = 1.0;
        p2.add((Component)panel, gbc);
        dialog.add((Component)p2, "South");
        dialog.getRootPane().setDefaultButton(okay);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setVisible(true);
        return picker.getGradient();
    }

    public static String toString(MultipleGradientPaint paint) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(paint.getClass().getName());
        Color[] colors = paint.getColors();
        float[] values = paint.getFractions();
        buffer.append("[");
        int i = 0;
        while (i < colors.length) {
            buffer.append("#").append(Integer.toHexString(colors[i].getRGB()));
            buffer.append(":");
            buffer.append(values[i]);
            buffer.append(", ");
            ++i;
        }
        buffer.append("]");
        return buffer.toString();
    }

    private final class AddThumbAction
    extends AbstractActionExt {
        private static final long serialVersionUID = 1L;

        public AddThumbAction() {
            super(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Add"));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            float pos = 0.2f;
            Color color = Color.black;
            int num = JXGradientChooser.this.slider.getModel().addThumb(pos, (Object)color);
            JXGradientChooser.this.log.fine("new number = " + num);
        }
    }

    private final class ChangeAlphaListener
    implements ChangeListener {
        private ChangeAlphaListener() {
        }

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            if (JXGradientChooser.this.slider.getSelectedIndex() >= 0 && !JXGradientChooser.this.thumbsMoving) {
                Thumb thumb = JXGradientChooser.this.slider.getModel().getThumbAt(JXGradientChooser.this.slider.getSelectedIndex());
                int alpha = changeEvent.getSource() == JXGradientChooser.this.alphaSpinner ? ((Integer)JXGradientChooser.this.alphaSpinner.getValue()).intValue() : JXGradientChooser.this.alphaSlider.getValue();
                Color col = (Color)thumb.getObject();
                col = ColorUtil.setAlpha((Color)col, (int)(alpha * 255 / 100));
                thumb.setObject((Object)col);
                if (changeEvent.getSource() == JXGradientChooser.this.alphaSpinner) {
                    JXGradientChooser.this.alphaSlider.setValue(alpha);
                } else {
                    JXGradientChooser.this.alphaSpinner.setValue(alpha);
                }
                JXGradientChooser.this.recalcGradientFromStops();
            }
        }
    }

    private final class ChangeLocationListener
    implements ChangeListener {
        private ChangeLocationListener() {
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            if (JXGradientChooser.this.slider.getSelectedIndex() >= 0) {
                Thumb thumb = JXGradientChooser.this.slider.getModel().getThumbAt(JXGradientChooser.this.slider.getSelectedIndex());
                thumb.setPosition((float)((Integer)JXGradientChooser.this.colorLocationSpinner.getValue()).intValue() / 100.0f);
                JXGradientChooser.this.updateFromStop((Thumb<Color>)thumb);
                JXGradientChooser.this.updateGradientProperty();
            }
        }
    }

    private final class DeleteThumbAction
    extends AbstractActionExt {
        private static final long serialVersionUID = 1L;

        public DeleteThumbAction() {
            super(I18N.getString("org.jdesktop.swingx.JXGradientChooser.Delete"));
        }

        public void actionPerformed(ActionEvent actionEvent) {
            int index = JXGradientChooser.this.slider.getSelectedIndex();
            if (index >= 0) {
                JXGradientChooser.this.slider.getModel().removeThumb(index);
                JXGradientChooser.this.updateFromStop(-1, -1.0f, null);
            }
        }
    }

    private static enum GradientStyle {
        Linear,
        Radial;

    }

    private final class RepaintOnEventListener
    implements ActionListener,
    ItemListener {
        private RepaintOnEventListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JXGradientChooser.this.gradientPreview.setReflected(JXGradientChooser.this.reflectedRadio.isSelected());
            JXGradientChooser.this.gradientPreview.setReversed(JXGradientChooser.this.reversedCheck.isSelected());
            JXGradientChooser.this.gradientPreview.setRepeated(JXGradientChooser.this.repeatedRadio.isSelected());
            JXGradientChooser.this.recalcGradientFromStops();
            JXGradientChooser.this.gradientPreview.repaint();
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (JXGradientChooser.this.styleCombo.getSelectedItem() == GradientStyle.Radial) {
                JXGradientChooser.this.gradientPreview.setRadial(true);
            } else {
                JXGradientChooser.this.gradientPreview.setRadial(false);
            }
            JXGradientChooser.this.recalcGradientFromStops();
        }
    }

    private class StopListener
    implements ThumbListener {
        public void thumbMoved(int thumb, float pos) {
            JXGradientChooser.this.log.fine("moved: " + thumb + " " + pos);
            Color color = (Color)JXGradientChooser.this.slider.getModel().getThumbAt(thumb).getObject();
            JXGradientChooser.this.thumbsMoving = true;
            JXGradientChooser.this.updateFromStop(thumb, pos, color);
            JXGradientChooser.this.updateDeleteButtons();
            JXGradientChooser.this.thumbsMoving = false;
        }

        public void thumbSelected(int thumb) {
            if (thumb == -1) {
                JXGradientChooser.this.updateFromStop(-1, -1.0f, Color.black);
                return;
            }
            JXGradientChooser.this.thumbsMoving = true;
            float pos = JXGradientChooser.this.slider.getModel().getThumbAt(thumb).getPosition();
            Color color = (Color)JXGradientChooser.this.slider.getModel().getThumbAt(thumb).getObject();
            JXGradientChooser.this.log.fine("selected = " + thumb + " " + pos + " " + color);
            JXGradientChooser.this.updateFromStop(thumb, pos, color);
            JXGradientChooser.this.updateDeleteButtons();
            JXGradientChooser.this.slider.repaint();
            JXGradientChooser.this.thumbsMoving = false;
        }

        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() > 1) {
                JXGradientChooser.this.selectColorForThumb();
            }
        }
    }
}

