/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.ui.ColorChooserPanel;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.TransparencyPanel;
import com.vividsolutions.jump.workbench.ui.ValidatingTextField;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.FillPatternFactory;
import com.vividsolutions.jump.workbench.ui.style.AbstractPalettePanel;
import com.vividsolutions.jump.workbench.ui.style.ListPalettePanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.jump.lang.I18N;

public class BasicStylePanel
extends JPanel {
    protected static final int SLIDER_TEXT_FIELD_COLUMNS = 3;
    protected static final Dimension SLIDER_DIMENSION = new Dimension(130, 49);
    private Paint[] fillPatterns = new FillPatternFactory().createFillPatterns();
    protected JPanel centerPanel = new JPanel();
    private AbstractPalettePanel palettePanel;
    protected JCheckBox fillCheckBox = new JCheckBox();
    protected JCheckBox lineCheckBox = new JCheckBox();
    protected TransparencyPanel transparencyPanel = new TransparencyPanel();
    protected JLabel transparencyLabel = new JLabel();
    protected ColorChooserPanel lineColorChooserPanel = new ColorChooserPanel();
    protected ColorChooserPanel fillColorChooserPanel = new ColorChooserPanel();
    protected JLabel lineWidthLabel = new JLabel();
    protected JCheckBox synchronizeCheckBox = new JCheckBox();
    private JCheckBox linePatternCheckBox = new JCheckBox();
    private JCheckBox fillPatternCheckBox = new JCheckBox();
    private String[] linePatterns = new String[]{"1", "2", "3", "4", "4,2", "5", "5,1", "6", "6,2", "6,6", "7", "7,12", "9", "9,2", "15,6", "20,3"};
    private JComboBox linePatternComboBox = new JComboBox(this.linePatterns){
        {
            ValidatingTextField.Cleaner cleaner = new ValidatingTextField.Cleaner(){

                @Override
                public String clean(String text) {
                    String pattern = "";
                    StringTokenizer tokenizer = new StringTokenizer(StringUtil.replaceAll(text, ",", " "));
                    while (tokenizer.hasMoreTokens()) {
                        pattern = String.valueOf(pattern) + tokenizer.nextToken() + " ";
                    }
                    return StringUtil.replaceAll(pattern.trim(), " ", ",");
                }
            };
            this.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    BasicStylePanel.this.updateControls();
                }
            });
            this.setRenderer(new ListCellRenderer(){
                private JPanel panel = new JPanel(){
                    private int lineWidth = 2;

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D)g;
                        g2.setStroke(new BasicStroke(this.lineWidth, 0, 2, 1.0f, BasicStyle.toArray(linePattern, this.lineWidth), 0.0f));
                        g2.draw(new Line2D.Double(0.0, (double)panel.getHeight() / 2.0, panel.getWidth(), (double)panel.getHeight() / 2.0));
                    }
                };
                private String linePattern;

                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    this.linePattern = (String)value;
                    this.panel.setForeground(UIManager.getColor(isSelected ? "ComboBox.selectionForeground" : "ComboBox.foreground"));
                    this.panel.setBackground(UIManager.getColor(isSelected ? "ComboBox.selectionBackground" : "ComboBox.background"));
                    return this.panel;
                }
            });
        }
    };
    private JComboBox fillPatternComboBox = new JComboBox(this.fillPatterns){
        {
            this.setMaximumRowCount(24);
            this.setEditable(false);
            this.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    BasicStylePanel.this.updateControls();
                }
            });
            this.setRenderer(new ListCellRenderer(){
                private Paint fillPattern;
                private JLabel label = new JLabel(" ");
                private JPanel panel = new JPanel(new BorderLayout()){
                    {
                        label.setPreferredSize(new Dimension(150, (int)label.getPreferredSize().getHeight()));
                        this.add((Component)label, "Center");
                    }

                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        ((Graphics2D)g).setPaint(fillPattern);
                        ((Graphics2D)g).fill(new Rectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight()));
                    }
                };

                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    this.fillPattern = (Paint)value;
                    this.label.setForeground(UIManager.getColor(isSelected ? "ComboBox.selectionForeground" : "ComboBox.foreground"));
                    this.panel.setBackground(UIManager.getColor(isSelected ? "ComboBox.selectionBackground" : "ComboBox.background"));
                    return this.panel;
                }
            });
        }
    };
    protected JSlider lineWidthSlider = new JSlider(){
        {
            this.addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent e) {
                    BasicStylePanel.this.updateControls();
                }
            });
        }
    };
    private Blackboard blackboard;

    public BasicStylePanel() {
        this(null, 22);
    }

    public BasicStylePanel(Blackboard blackboard, int palettePanelVerticalScrollBarPolicy) {
        this.blackboard = blackboard;
        this.palettePanel = new ListPalettePanel(palettePanelVerticalScrollBarPolicy);
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            Assert.shouldNeverReachHere();
        }
        this.transparencyPanel.getSlider().getModel().addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                BasicStylePanel.this.updateControls();
            }
        });
        this.palettePanel.add(new AbstractPalettePanel.Listener(){

            @Override
            public void basicStyleChosen(BasicStyle basicStyle) {
                BasicStyle newBasicStyle = BasicStylePanel.this.getBasicStyle();
                newBasicStyle.setFillColor(basicStyle.getFillColor());
                newBasicStyle.setLineColor(basicStyle.getLineColor());
                newBasicStyle.setLineWidth(basicStyle.getLineWidth());
                newBasicStyle.setRenderingFill(basicStyle.isRenderingFill());
                newBasicStyle.setRenderingLine(basicStyle.isRenderingLine());
                BasicStylePanel.this.setBasicStyle(newBasicStyle, false, false, false);
            }
        });
        this.updateControls();
    }

    private String clean(String linePattern) {
        String pattern = "";
        StringTokenizer tokenizer = new StringTokenizer(StringUtil.replaceAll(linePattern, ",", " "));
        while (tokenizer.hasMoreTokens()) {
            pattern = String.valueOf(pattern) + tokenizer.nextToken() + " ";
        }
        return StringUtil.replaceAll(pattern.trim(), " ", ",");
    }

    void jbInit() throws Exception {
        this.lineWidthSlider.setPreferredSize(SLIDER_DIMENSION);
        this.lineWidthSlider.setPaintLabels(true);
        this.lineWidthSlider.setValue(1);
        this.lineWidthSlider.setLabelTable(this.lineWidthSlider.createStandardLabels(10));
        this.lineWidthSlider.setMajorTickSpacing(5);
        this.lineWidthSlider.setMaximum(30);
        this.lineWidthSlider.setMinorTickSpacing(1);
        this.setLayout(new GridBagLayout());
        this.linePatternCheckBox.setText(I18N.getString("workbench.ui.style.BasicStylePanel.line-pattern"));
        this.fillPatternCheckBox.setText(I18N.getString("workbench.ui.style.BasicStylePanel.fill-pattern"));
        this.linePatternCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                BasicStylePanel.this.linePatternCheckBox_actionPerformed(e);
            }
        });
        this.fillPatternCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                BasicStylePanel.this.fillPatternCheckBox_actionPerformed(e);
            }
        });
        this.add((Component)this.centerPanel, new GridBagConstraints(0, 0, 1, 2, 1.0, 0.0, 10, 2, new Insets(2, 2, 2, 2), 0, 0));
        this.add((Component)new JLabel(I18N.getString("workbench.ui.style.BasicStylePanel.presets")), new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
        this.add((Component)this.palettePanel, new GridBagConstraints(2, 1, 1, 1, 1.0, 0.0, 10, 3, new Insets(0, 0, 0, 0), 0, 0));
        this.centerPanel.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.fillColorChooserPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                BasicStylePanel.this.fillColorChooserPanel_actionPerformed(e);
            }
        });
        this.lineColorChooserPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                BasicStylePanel.this.lineColorChooserPanel_actionPerformed(e);
            }
        });
        this.synchronizeCheckBox.setText(I18N.getString("workbench.ui.style.BasicStylePanel.synchronize-line-colour-with-fill-colour"));
        this.synchronizeCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                BasicStylePanel.this.synchronizeCheckBox_actionPerformed(e);
            }
        });
        this.fillCheckBox.setText(I18N.getString("workbench.ui.style.BasicStylePanel.fill"));
        this.fillCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                BasicStylePanel.this.fillCheckBox_actionPerformed(e);
            }
        });
        this.lineCheckBox.setText(I18N.getString("workbench.ui.style.BasicStylePanel.line"));
        this.lineCheckBox.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                BasicStylePanel.this.lineCheckBox_actionPerformed(e);
            }
        });
        this.centerPanel.add((Component)GUIUtil.createSyncdTextField(this.transparencyPanel.getSlider(), 3), new GridBagConstraints(2, 21, 1, 1, 0.0, 0.0, 10, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.lineWidthSlider, new GridBagConstraints(1, 19, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)GUIUtil.createSyncdTextField(this.lineWidthSlider, 3), new GridBagConstraints(2, 19, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.lineWidthLabel.setText(I18N.getString("workbench.ui.style.BasicStylePanel.line-width"));
        this.transparencyLabel.setText(I18N.getString("workbench.ui.style.BasicStylePanel.transparency"));
        this.centerPanel.add((Component)this.synchronizeCheckBox, new GridBagConstraints(0, 18, 3, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.transparencyLabel, new GridBagConstraints(0, 21, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.fillColorChooserPanel, new GridBagConstraints(1, 5, 2, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.lineColorChooserPanel, new GridBagConstraints(1, 11, 2, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.transparencyPanel, new GridBagConstraints(1, 21, 1, 1, 0.0, 0.0, 10, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.fillCheckBox, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.lineCheckBox, new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.lineWidthLabel, new GridBagConstraints(0, 19, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.linePatternCheckBox, new GridBagConstraints(0, 16, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.fillPatternCheckBox, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.linePatternComboBox, new GridBagConstraints(1, 16, 2, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 2, 2), 0, 0));
        this.centerPanel.add((Component)this.fillPatternComboBox, new GridBagConstraints(1, 7, 2, 1, 0.0, 0.0, 17, 0, new Insets(2, 2, 0, 2), 0, 0));
    }

    public JSlider getTransparencySlider() {
        return this.transparencyPanel.getSlider();
    }

    protected void setAlpha(int alpha) {
        this.transparencyPanel.getSlider().setValue(255 - alpha);
    }

    protected int getAlpha() {
        return 255 - this.transparencyPanel.getSlider().getValue();
    }

    public void setBasicStyle(BasicStyle basicStyle, boolean isLine, boolean isRaster, boolean isPoint) {
        this.addCustomFillPatterns();
        this.fillColorChooserPanel.setColor(basicStyle.getFillColor());
        this.lineColorChooserPanel.setColor(basicStyle.getLineColor());
        this.setAlpha(basicStyle.getAlpha());
        this.fillCheckBox.setSelected(basicStyle.isRenderingFill());
        this.lineCheckBox.setSelected(basicStyle.isRenderingLine());
        this.lineWidthSlider.setValue(basicStyle.getLineWidth());
        this.linePatternCheckBox.setSelected(basicStyle.isRenderingLinePattern());
        this.fillPatternCheckBox.setSelected(basicStyle.isRenderingFillPattern());
        this.linePatternComboBox.setSelectedItem(basicStyle.getLinePattern());
        if (isLine) {
            this.fillCheckBox.setSelected(false);
            this.fillCheckBox.setEnabled(false);
            this.fillPatternCheckBox.setSelected(false);
            this.fillPatternCheckBox.setEnabled(false);
        }
        if (isPoint) {
            this.fillPatternCheckBox.setSelected(false);
            this.fillPatternCheckBox.setEnabled(false);
            this.lineCheckBox.setSelected(false);
            this.lineCheckBox.setEnabled(false);
            this.linePatternCheckBox.setEnabled(false);
            this.lineWidthSlider.setEnabled(false);
            this.synchronizeCheckBox.setEnabled(false);
        }
        if (isRaster) {
            this.fillCheckBox.setSelected(false);
            this.fillCheckBox.setEnabled(false);
            this.fillPatternCheckBox.setSelected(false);
            this.fillPatternCheckBox.setEnabled(false);
            this.lineCheckBox.setSelected(false);
            this.lineCheckBox.setEnabled(false);
            this.linePatternCheckBox.setEnabled(false);
            this.lineWidthSlider.setEnabled(false);
            this.synchronizeCheckBox.setEnabled(false);
            this.palettePanel.setEnabled(false);
        }
        this.updateFillPatternColors();
        Object paint = this.findEquivalentItem(basicStyle.getFillPattern(), this.fillPatternComboBox);
        if (paint != null) {
            this.fillPatternComboBox.setSelectedItem(paint);
        } else {
            this.fillPatternComboBox.setSelectedIndex(0);
        }
        this.updateControls();
    }

    private void addCustomFillPatterns() {
        Object obj = new ArrayList();
        if (this.blackboard != null) {
            obj = this.blackboard.get(FillPatternFactory.CUSTOM_FILL_PATTERNS_KEY, new ArrayList());
        }
        for (Paint fillPattern : (Collection)obj) {
            if (this.findEquivalentItem(fillPattern, this.fillPatternComboBox) != null) continue;
            ((DefaultComboBoxModel)this.fillPatternComboBox.getModel()).addElement(this.cloneIfBasicFillPattern(fillPattern));
        }
    }

    private Object findEquivalentItem(Object item, JComboBox comboBox) {
        if (item != null) {
            int i = 0;
            while (i < comboBox.getItemCount()) {
                if (item.equals(comboBox.getItemAt(i))) {
                    return comboBox.getItemAt(i);
                }
                ++i;
            }
        }
        return null;
    }

    public BasicStyle getBasicStyle() {
        BasicStyle basicStyle = new BasicStyle();
        basicStyle.setFillColor(this.fillColorChooserPanel.getColor());
        basicStyle.setLineColor(this.lineColorChooserPanel.getColor());
        basicStyle.setAlpha(this.getAlpha());
        basicStyle.setRenderingFill(this.fillCheckBox.isSelected());
        basicStyle.setRenderingLine(this.lineCheckBox.isSelected());
        basicStyle.setRenderingLinePattern(this.linePatternCheckBox.isSelected());
        basicStyle.setRenderingFillPattern(this.fillPatternCheckBox.isSelected());
        basicStyle.setLinePattern(this.linePatterns[this.linePatternComboBox.getSelectedIndex()]);
        basicStyle.setFillPattern(this.cloneIfBasicFillPattern((Paint)this.fillPatternComboBox.getSelectedItem()));
        basicStyle.setLineWidth(this.lineWidthSlider.getValue());
        return basicStyle;
    }

    private Paint cloneIfBasicFillPattern(Paint fillPattern) {
        return fillPattern instanceof BasicFillPattern ? (Paint)((BasicFillPattern)fillPattern).clone() : fillPattern;
    }

    protected void setFillColor(Color newColor) {
        this.fillColorChooserPanel.setColor(newColor);
        this.transparencyPanel.setColor(newColor);
    }

    protected void updateControls() {
        this.linePatternComboBox.setEnabled(this.linePatternCheckBox.isSelected());
        this.linePatternComboBox.setMinimumSize(new Dimension(100, 20));
        this.linePatternComboBox.setPreferredSize(new Dimension(150, 20));
        this.fillPatternComboBox.setEnabled(this.fillPatternCheckBox.isSelected());
        this.lineColorChooserPanel.setEnabled(this.lineCheckBox.isSelected());
        this.fillColorChooserPanel.setEnabled(this.fillCheckBox.isSelected());
        this.fillColorChooserPanel.setAlpha(this.getAlpha());
        this.lineColorChooserPanel.setAlpha(this.getAlpha());
        this.palettePanel.setAlpha(this.getAlpha());
        this.transparencyPanel.setColor(this.lineCheckBox.isSelected() && !this.fillCheckBox.isSelected() ? this.lineColorChooserPanel.getColor() : this.fillColorChooserPanel.getColor());
        this.updateFillPatternColors();
        this.fillPatternComboBox.repaint();
    }

    private void updateFillPatternColors() {
        int i = 0;
        while (i < this.fillPatternComboBox.getItemCount()) {
            if (this.fillPatternComboBox.getItemAt(i) instanceof BasicFillPattern) {
                ((BasicFillPattern)this.fillPatternComboBox.getItemAt(i)).setColor(GUIUtil.alphaColor(this.fillColorChooserPanel.getColor(), this.getAlpha()));
            }
            ++i;
        }
    }

    void fillCheckBox_actionPerformed(ActionEvent e) {
        this.updateControls();
    }

    void fillColorChooserPanel_actionPerformed(ActionEvent e) {
        if (this.synchronizeCheckBox.isSelected()) {
            this.syncLineColor();
        }
        this.updateControls();
    }

    private void syncLineColor() {
        this.lineColorChooserPanel.setColor(this.fillColorChooserPanel.getColor().darker());
    }

    void lineColorChooserPanel_actionPerformed(ActionEvent e) {
        if (this.synchronizeCheckBox.isSelected()) {
            this.fillColorChooserPanel.setColor(this.lineColorChooserPanel.getColor().brighter());
        }
        this.updateControls();
    }

    void lineCheckBox_actionPerformed(ActionEvent e) {
        this.updateControls();
    }

    public void setSynchronizingLineColor(boolean newSynchronizingLineColor) {
        this.synchronizeCheckBox.setSelected(newSynchronizingLineColor);
    }

    protected void synchronizeCheckBox_actionPerformed(ActionEvent e) {
        if (this.synchronizeCheckBox.isSelected()) {
            this.syncLineColor();
        }
        this.updateControls();
    }

    public JCheckBox getSynchronizeCheckBox() {
        return this.synchronizeCheckBox;
    }

    void linePatternCheckBox_actionPerformed(ActionEvent e) {
        this.updateControls();
    }

    void fillPatternCheckBox_actionPerformed(ActionEvent e) {
        this.updateControls();
    }
}

