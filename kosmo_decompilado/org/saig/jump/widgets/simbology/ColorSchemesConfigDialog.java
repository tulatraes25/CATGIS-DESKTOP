/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.simbology;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.addremove.ButtonCustomAddRemovePanel;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.PreMoveElementBetweenListsListener;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorSchemeListCellRenderer;
import es.kosmo.core.utils.ColorGenerator;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.util.NumberSpinner;
import org.saig.jump.widgets.util.validating.NullTextFieldValidator;

public class ColorSchemesConfigDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final String TITLE = I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.color-schemes-configuration");
    private static final int MAX_COLOR_COLUMNS_PER_ROW = 6;
    protected static final Color[] DEFAULTCOLORS = new Color[]{Color.BLUE, Color.YELLOW, Color.RED};
    protected static final Color DEFAULTCOLOR = Color.BLACK;
    protected static final int DEFAULT_MIN_COLOR_NUMBER = 2;
    private JPanel schemeSelectionPanel;
    private JCheckBox filterDefaultColorSchemesCheckBox;
    private ButtonCustomAddRemovePanel<String> discreteColorSchemePanel;
    private ButtonCustomAddRemovePanel<String> rangeColorSchemePanel;
    private JButton deleteSelectedColorSchemesButton;
    private JButton deleteAllVisibleColorSchemesButton;
    private JPanel basicPropertiesPanel;
    private JTextField schemeNameTextField;
    private JCheckBox discreteSchemeCheckBox;
    private JCheckBox rangeSchemeCheckBox;
    private JPanel colorRangeTypePanel;
    private JRadioButton fixedColorsRadioButton;
    private JRadioButton colorRangeRadioButton;
    private ButtonGroup colorRangeTypeButtonGroup = new ButtonGroup();
    private JPanel colorRangePanel;
    private NumberSpinner colorNumberSpinner;
    private JLabel colorRangeNumberLabel;
    private NumberSpinner colorRangeNumberSpinner;
    private JPanel colorConfigurationPanel;
    private JButton[] colorButtons;
    private Color[] colors = DEFAULTCOLORS;
    private int numColors = 2;
    private OKCancelPanel okCancelPanel;
    private String lastSelectedColorScheme;

    public ColorSchemesConfigDialog(JDialog parent, boolean modal) {
        super((Dialog)parent, modal);
        this.setTitle(TITLE);
        this.setDefaultCloseOperation(0);
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                ColorSchemesConfigDialog.this.closingWindow();
            }
        });
        this.initialize();
        this.loadColorSchemes();
        this.pack();
    }

    private void loadColorSchemes() {
        Collection<String> rangeSchemes;
        Collection<String> discreteSchemes;
        Collection<String> currentDiscreteSchemesNames = ColorScheme.discreteColorSchemeNames();
        Collection<String> currentRangeSchemesNames = ColorScheme.rangeColorSchemeNames();
        if (this.filterDefaultColorSchemesCheckBox.isSelected()) {
            discreteSchemes = new ArrayList<String>();
            rangeSchemes = new ArrayList<String>();
            for (String currentSchemeName : currentDiscreteSchemesNames) {
                if (currentSchemeName.contains("ColorBrewer") || currentSchemeName.contains("Visual Mining Inc.")) continue;
                discreteSchemes.add(currentSchemeName);
            }
            for (String currentSchemeName : currentRangeSchemesNames) {
                if (currentSchemeName.contains("ColorBrewer") || currentSchemeName.contains("Visual Mining Inc.")) continue;
                rangeSchemes.add(currentSchemeName);
            }
        } else {
            discreteSchemes = currentDiscreteSchemesNames;
            rangeSchemes = currentRangeSchemesNames;
        }
        ArrayList<String> discreteOnlySchemes = new ArrayList<String>(discreteSchemes);
        discreteOnlySchemes.removeAll(rangeSchemes);
        ArrayList<String> rangeOnlySchemes = new ArrayList<String>(rangeSchemes);
        rangeOnlySchemes.removeAll(discreteSchemes);
        this.discreteColorSchemePanel.getLeftList().getModel().setItems(rangeOnlySchemes);
        this.discreteColorSchemePanel.getRightList().getModel().setItems(discreteSchemes);
        this.rangeColorSchemePanel.getLeftList().getModel().setItems(discreteOnlySchemes);
        this.rangeColorSchemePanel.getRightList().getModel().setItems(rangeSchemes);
        int visibleItemsNumber = this.discreteColorSchemePanel.getLeftItems().size() + this.discreteColorSchemePanel.getRightItems().size() + this.rangeColorSchemePanel.getLeftItems().size() + this.rangeColorSchemePanel.getRightItems().size();
        this.deleteAllVisibleColorSchemesButton.setEnabled(visibleItemsNumber > 0);
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        this.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 0, 0, this.getSchemeSelectionPanel());
        FormUtils.addRowInGBL((JComponent)mainPanel, 1, 0, (JComponent)this.getBasicPropertiesPanel(), false, true);
        FormUtils.addRowInGBL(mainPanel, 1, 1, this.getColorRangeTypePanel());
        FormUtils.addRowInGBL(mainPanel, 2, 0, this.getColorRangePanel());
        FormUtils.addRowInGBL(mainPanel, 3, 0, this.getOkCancelPanel());
        FormUtils.addFiller(mainPanel, 4, 0);
    }

    private JPanel getSchemeSelectionPanel() {
        if (this.schemeSelectionPanel == null) {
            this.schemeSelectionPanel = new JPanel(new GridBagLayout());
            this.schemeSelectionPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.select-the-color-scheme")));
            Dimension panelSize = new Dimension(340, 180);
            this.filterDefaultColorSchemesCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.filter-the-default-color-schemes"));
            this.filterDefaultColorSchemesCheckBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorSchemesConfigDialog.this.loadColorSchemes();
                }
            });
            this.discreteColorSchemePanel = new ButtonCustomAddRemovePanel(true);
            this.discreteColorSchemePanel.setLeftText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.available-schemes"));
            this.discreteColorSchemePanel.setRightText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.discrete-schemes"));
            this.discreteColorSchemePanel.setMinimumSize(panelSize);
            this.discreteColorSchemePanel.setPreferredSize(panelSize);
            this.discreteColorSchemePanel.getLeftList().add(new PreMoveElementBetweenListsListener(){

                @Override
                public void moveElementBetweenListsFired() {
                    List itemsToMove = ColorSchemesConfigDialog.this.discreteColorSchemePanel.getLeftList().getSelectedItems();
                    ColorScheme.discreteColorSchemeNames().addAll(itemsToMove);
                }

                @Override
                public void moveElementBetweenListsFired(boolean allElements) {
                    List itemsToMove = null;
                    itemsToMove = allElements ? ColorSchemesConfigDialog.this.discreteColorSchemePanel.getLeftItems() : ColorSchemesConfigDialog.this.discreteColorSchemePanel.getLeftList().getSelectedItems();
                    ColorScheme.discreteColorSchemeNames().addAll(itemsToMove);
                }
            });
            ((DefaultAddRemoveList)this.discreteColorSchemePanel.getLeftList()).getList().setCellRenderer(new ColorSchemeListCellRenderer());
            ((DefaultAddRemoveList)this.discreteColorSchemePanel.getLeftList()).getList().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    List selectedItems = ColorSchemesConfigDialog.this.discreteColorSchemePanel.getLeftList().getSelectedItems();
                    if (selectedItems.size() > 0) {
                        ColorSchemesConfigDialog.this.discreteColorSchemePanel.getRightList().setSelectedItems(new Vector());
                        ColorSchemesConfigDialog.this.rangeColorSchemePanel.getLeftList().setSelectedItems(new Vector());
                        ColorSchemesConfigDialog.this.rangeColorSchemePanel.getRightList().setSelectedItems(new Vector());
                    }
                    if (selectedItems.size() == 1) {
                        ColorSchemesConfigDialog.this.loadSelectedColorScheme((String)selectedItems.get(0));
                    } else {
                        ColorSchemesConfigDialog.this.loadSelectedColorScheme(null);
                    }
                }
            });
            this.discreteColorSchemePanel.getRightList().add(new PreMoveElementBetweenListsListener(){

                @Override
                public void moveElementBetweenListsFired() {
                    List itemsToMove = ColorSchemesConfigDialog.this.discreteColorSchemePanel.getRightList().getSelectedItems();
                    ColorScheme.discreteColorSchemeNames().removeAll(itemsToMove);
                }

                @Override
                public void moveElementBetweenListsFired(boolean allElements) {
                    List itemsToMove = null;
                    itemsToMove = allElements ? ColorSchemesConfigDialog.this.discreteColorSchemePanel.getRightItems() : ColorSchemesConfigDialog.this.discreteColorSchemePanel.getRightList().getSelectedItems();
                    ColorScheme.discreteColorSchemeNames().removeAll(itemsToMove);
                }
            });
            ((DefaultAddRemoveList)this.discreteColorSchemePanel.getRightList()).getList().setCellRenderer(new ColorSchemeListCellRenderer());
            ((DefaultAddRemoveList)this.discreteColorSchemePanel.getRightList()).getList().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    List selectedItems = ColorSchemesConfigDialog.this.discreteColorSchemePanel.getRightList().getSelectedItems();
                    if (selectedItems.size() > 0) {
                        ColorSchemesConfigDialog.this.discreteColorSchemePanel.getLeftList().setSelectedItems(new Vector());
                        ColorSchemesConfigDialog.this.rangeColorSchemePanel.getLeftList().setSelectedItems(new Vector());
                        ColorSchemesConfigDialog.this.rangeColorSchemePanel.getRightList().setSelectedItems(new Vector());
                    }
                    if (selectedItems.size() == 1) {
                        ColorSchemesConfigDialog.this.loadSelectedColorScheme((String)selectedItems.get(0));
                    } else {
                        ColorSchemesConfigDialog.this.loadSelectedColorScheme(null);
                    }
                }
            });
            this.rangeColorSchemePanel = new ButtonCustomAddRemovePanel(true);
            this.rangeColorSchemePanel.setLeftText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.available-schemes"));
            this.rangeColorSchemePanel.setRightText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.range-schemes"));
            this.rangeColorSchemePanel.setMinimumSize(panelSize);
            this.rangeColorSchemePanel.setPreferredSize(panelSize);
            this.rangeColorSchemePanel.getLeftList().add(new PreMoveElementBetweenListsListener(){

                @Override
                public void moveElementBetweenListsFired() {
                    List itemsToMove = ColorSchemesConfigDialog.this.rangeColorSchemePanel.getLeftList().getSelectedItems();
                    ColorScheme.rangeColorSchemeNames().addAll(itemsToMove);
                }

                @Override
                public void moveElementBetweenListsFired(boolean allElements) {
                    List itemsToMove = null;
                    itemsToMove = allElements ? ColorSchemesConfigDialog.this.rangeColorSchemePanel.getLeftItems() : ColorSchemesConfigDialog.this.rangeColorSchemePanel.getLeftList().getSelectedItems();
                    ColorScheme.rangeColorSchemeNames().addAll(itemsToMove);
                }
            });
            ((DefaultAddRemoveList)this.rangeColorSchemePanel.getLeftList()).getList().setCellRenderer(new ColorSchemeListCellRenderer());
            ((DefaultAddRemoveList)this.rangeColorSchemePanel.getLeftList()).getList().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    List selectedItems = ColorSchemesConfigDialog.this.rangeColorSchemePanel.getLeftList().getSelectedItems();
                    if (selectedItems.size() > 0) {
                        ColorSchemesConfigDialog.this.discreteColorSchemePanel.getRightList().setSelectedItems(new Vector());
                        ColorSchemesConfigDialog.this.discreteColorSchemePanel.getLeftList().setSelectedItems(new Vector());
                        ColorSchemesConfigDialog.this.rangeColorSchemePanel.getRightList().setSelectedItems(new Vector());
                    }
                    if (selectedItems.size() == 1) {
                        ColorSchemesConfigDialog.this.loadSelectedColorScheme((String)selectedItems.get(0));
                    } else {
                        ColorSchemesConfigDialog.this.loadSelectedColorScheme(null);
                    }
                }
            });
            this.rangeColorSchemePanel.getRightList().add(new PreMoveElementBetweenListsListener(){

                @Override
                public void moveElementBetweenListsFired() {
                    List itemsToMove = ColorSchemesConfigDialog.this.rangeColorSchemePanel.getRightList().getSelectedItems();
                    ColorScheme.rangeColorSchemeNames().removeAll(itemsToMove);
                }

                @Override
                public void moveElementBetweenListsFired(boolean allElements) {
                    List itemsToMove = null;
                    itemsToMove = allElements ? ColorSchemesConfigDialog.this.rangeColorSchemePanel.getRightItems() : ColorSchemesConfigDialog.this.rangeColorSchemePanel.getRightList().getSelectedItems();
                    ColorScheme.rangeColorSchemeNames().removeAll(itemsToMove);
                }
            });
            ((DefaultAddRemoveList)this.rangeColorSchemePanel.getRightList()).getList().setCellRenderer(new ColorSchemeListCellRenderer());
            ((DefaultAddRemoveList)this.rangeColorSchemePanel.getRightList()).getList().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    List selectedItems = ColorSchemesConfigDialog.this.rangeColorSchemePanel.getRightList().getSelectedItems();
                    if (selectedItems.size() > 0) {
                        ColorSchemesConfigDialog.this.discreteColorSchemePanel.getRightList().setSelectedItems(new Vector());
                        ColorSchemesConfigDialog.this.discreteColorSchemePanel.getLeftList().setSelectedItems(new Vector());
                        ColorSchemesConfigDialog.this.rangeColorSchemePanel.getLeftList().setSelectedItems(new Vector());
                    }
                    if (selectedItems.size() == 1) {
                        ColorSchemesConfigDialog.this.loadSelectedColorScheme((String)selectedItems.get(0));
                    } else {
                        ColorSchemesConfigDialog.this.loadSelectedColorScheme(null);
                    }
                }
            });
            JPanel buttonPanel = new JPanel(new FlowLayout());
            this.deleteSelectedColorSchemesButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("delete_small.gif")));
            this.deleteSelectedColorSchemesButton.setToolTipText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.remove-the-selected-color-schemes"));
            this.deleteSelectedColorSchemesButton.setEnabled(false);
            this.deleteSelectedColorSchemesButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorSchemesConfigDialog.this.deleteSelectedColorSchemes();
                }
            });
            this.deleteAllVisibleColorSchemesButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("deleteAll.png")));
            this.deleteAllVisibleColorSchemesButton.setToolTipText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.remove-all-visible-color-schemes"));
            this.deleteAllVisibleColorSchemesButton.setEnabled(false);
            this.deleteAllVisibleColorSchemesButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorSchemesConfigDialog.this.deleteAllVisibleColorSchemes();
                }
            });
            buttonPanel.add(this.deleteSelectedColorSchemesButton);
            buttonPanel.add(this.deleteAllVisibleColorSchemesButton);
            FormUtils.addRowInGBL((JComponent)this.schemeSelectionPanel, 0, 0, (JComponent)this.filterDefaultColorSchemesCheckBox, false, false);
            FormUtils.addRowInGBL((JComponent)this.schemeSelectionPanel, 1, 0, this.discreteColorSchemePanel, false, true);
            FormUtils.addRowInGBL((JComponent)this.schemeSelectionPanel, 1, 1, this.rangeColorSchemePanel, false, true);
            FormUtils.addRowInGBL((JComponent)this.schemeSelectionPanel, 2, 0, (JComponent)buttonPanel, true, false);
        }
        return this.schemeSelectionPanel;
    }

    private JPanel getBasicPropertiesPanel() {
        if (this.basicPropertiesPanel == null) {
            this.basicPropertiesPanel = new JPanel(new GridBagLayout());
            this.basicPropertiesPanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.basic-properties")));
            this.basicPropertiesPanel.setMinimumSize(new Dimension(320, 100));
            this.basicPropertiesPanel.setPreferredSize(new Dimension(320, 100));
            JLabel schemeNameLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.name")) + ": ");
            this.schemeNameTextField = new JTextField();
            this.schemeNameTextField.setInputVerifier(new NullTextFieldValidator(this, this.schemeNameTextField));
            this.discreteSchemeCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.discrete"));
            this.rangeSchemeCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.range"));
            FormUtils.addRowInGBL((JComponent)this.basicPropertiesPanel, 0, 0, schemeNameLabel, (JComponent)this.schemeNameTextField);
            FormUtils.addRowInGBL(this.basicPropertiesPanel, 1, 0, this.discreteSchemeCheckBox);
            FormUtils.addRowInGBL(this.basicPropertiesPanel, 2, 0, this.rangeSchemeCheckBox);
        }
        return this.basicPropertiesPanel;
    }

    private JPanel getColorRangeTypePanel() {
        if (this.colorRangeTypePanel == null) {
            this.colorRangeTypePanel = new JPanel(new GridBagLayout());
            this.colorRangeTypePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.range-color-type")));
            this.colorRangeTypePanel.setMinimumSize(new Dimension(320, 100));
            this.colorRangeTypePanel.setPreferredSize(new Dimension(320, 100));
            this.fixedColorsRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.fixed-colors"));
            this.fixedColorsRadioButton.setSelected(true);
            this.fixedColorsRadioButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorSchemesConfigDialog.this.updateComponents();
                }
            });
            this.colorRangeRadioButton = new JRadioButton(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.color-ramp"));
            this.colorRangeRadioButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorSchemesConfigDialog.this.updateComponents();
                }
            });
            this.colorRangeTypeButtonGroup.add(this.fixedColorsRadioButton);
            this.colorRangeTypeButtonGroup.add(this.colorRangeRadioButton);
            FormUtils.addRowInGBL(this.colorRangeTypePanel, 1, 0, this.fixedColorsRadioButton);
            FormUtils.addRowInGBL(this.colorRangeTypePanel, 2, 0, this.colorRangeRadioButton);
            FormUtils.addFiller(this.colorRangeTypePanel, 3, 0);
        }
        return this.colorRangeTypePanel;
    }

    private JPanel getColorRangePanel() {
        if (this.colorRangePanel == null) {
            this.colorRangePanel = new JPanel(new GridBagLayout());
            this.colorRangePanel.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.color-range")));
            JLabel colorNumberLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.color-number")) + ": ");
            this.colorNumberSpinner = new NumberSpinner(2, 2, Integer.MAX_VALUE, 1);
            this.colorNumberSpinner.addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent e) {
                    ColorSchemesConfigDialog.this.updateComponents();
                }
            });
            this.colorRangeNumberLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.ramp-number")) + ": ");
            this.colorRangeNumberSpinner = new NumberSpinner(2, 2, Integer.MAX_VALUE, 1);
            JPanel buttonPanel = new JPanel(new FlowLayout(1, 0, 0));
            JButton saveChangesButton = new JButton(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.save-changes"), GUIUtil.toSmallIcon(IconLoader.icon("Save.gif")));
            saveChangesButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ColorSchemesConfigDialog.this.isInputValid()) {
                        ColorSchemesConfigDialog.this.saveChangesToCurrentScheme();
                    }
                }
            });
            buttonPanel.add(saveChangesButton);
            FormUtils.addRowInGBL((JComponent)this.colorRangePanel, 1, 0, (JComponent)colorNumberLabel, false, true);
            FormUtils.addRowInGBL((JComponent)this.colorRangePanel, 1, 1, (JComponent)this.colorNumberSpinner, false, true);
            FormUtils.addRowInGBL((JComponent)this.colorRangePanel, 1, 2, (JComponent)this.colorRangeNumberLabel, false, true);
            FormUtils.addRowInGBL((JComponent)this.colorRangePanel, 1, 3, (JComponent)this.colorRangeNumberSpinner, false, true);
            FormUtils.addRowInGBL(this.colorRangePanel, 2, 0, this.getColorConfigurationPanel());
            FormUtils.addRowInGBL(this.colorRangePanel, 3, 0, buttonPanel);
        }
        return this.colorRangePanel;
    }

    private JPanel getColorConfigurationPanel() {
        if (this.colorConfigurationPanel == null) {
            this.colorConfigurationPanel = new JPanel();
        } else {
            this.colorConfigurationPanel.removeAll();
        }
        int columns = Math.max(1, Math.min(this.numColors, 6));
        int rows = Math.max(1, this.numColors / columns + 1);
        this.updateColorArray();
        this.colorConfigurationPanel.setLayout(new GridLayout(rows, columns));
        this.colorButtons = new JButton[this.numColors];
        int i = 0;
        while (i < this.numColors) {
            this.colorButtons[i] = new JButton();
            if (i == 0) {
                this.colorButtons[i].setText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.minimum"));
            } else if (i == this.numColors - 1) {
                this.colorButtons[i].setText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.maximum"));
            } else {
                this.colorButtons[i].setText(String.valueOf(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.intermediate")) + " - " + i);
            }
            this.colorButtons[i].setContentAreaFilled(false);
            this.colorButtons[i].setOpaque(true);
            if (this.colorButtons[i] != null) {
                this.colorButtons[i].setBackground(this.colors[i]);
            } else {
                this.colorButtons[i].setBackground(DEFAULTCOLOR);
            }
            this.colorButtons[i].addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent ae) {
                    int num = Integer.parseInt(ae.getActionCommand());
                    if (ColorSchemesConfigDialog.this.colors.length > num) {
                        if (ColorSchemesConfigDialog.this.colors[num] == null) {
                            ((ColorSchemesConfigDialog)ColorSchemesConfigDialog.this).colors[num] = DEFAULTCOLOR;
                        }
                        Color bufferColor = ColorSchemesConfigDialog.this.colors[num];
                        ((ColorSchemesConfigDialog)ColorSchemesConfigDialog.this).colors[num] = JColorChooser.showDialog(ColorSchemesConfigDialog.this, I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.select-a-new-color"), ColorSchemesConfigDialog.this.colors[num]);
                        if (ColorSchemesConfigDialog.this.colors[num] == null) {
                            ((ColorSchemesConfigDialog)ColorSchemesConfigDialog.this).colors[num] = bufferColor;
                        }
                    }
                    ColorSchemesConfigDialog.this.colorButtons[num].setBackground(ColorSchemesConfigDialog.this.colors[num]);
                    ColorSchemesConfigDialog.this.setButtonForeground(ColorSchemesConfigDialog.this.colorButtons[num]);
                }
            });
            this.colorButtons[i].setActionCommand(new Integer(i).toString());
            this.setButtonForeground(this.colorButtons[i]);
            this.colorConfigurationPanel.add(this.colorButtons[i]);
            ++i;
        }
        return this.colorConfigurationPanel;
    }

    protected void updateColorArray() {
        if (this.colors.length < this.numColors) {
            Color[] oldColors = new Color[this.colors.length];
            System.arraycopy(this.colors, 0, oldColors, 0, this.colors.length);
            this.colors = new Color[this.numColors];
            int i = 0;
            while (i < this.numColors) {
                this.colors[i] = i < oldColors.length ? oldColors[i] : DEFAULTCOLOR;
                ++i;
            }
        } else if (this.numColors < this.colors.length) {
            Color[] oldColors = new Color[this.numColors];
            System.arraycopy(this.colors, 0, oldColors, 0, this.numColors);
            this.colors = oldColors;
        }
    }

    protected void updateComponents() {
        this.colorRangeNumberLabel.setEnabled(this.colorRangeRadioButton.isSelected());
        this.colorRangeNumberSpinner.setEnabled(this.colorRangeRadioButton.isSelected());
        int newNumColors = this.colorNumberSpinner.getIntValue();
        if (newNumColors != this.numColors) {
            this.numColors = newNumColors;
            this.updateColorPanel();
        }
    }

    protected void updateColorPanel() {
        this.getColorConfigurationPanel();
        this.pack();
        this.colorConfigurationPanel.repaint();
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.setAcceptButtonText(I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.close"));
            this.okCancelPanel.setCancelVisible(false);
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ColorSchemesConfigDialog.this.closingWindow();
                }
            });
        }
        return this.okCancelPanel;
    }

    protected void closingWindow() {
        if (ColorScheme.nameToColorsMap().size() > 0) {
            if (ColorScheme.discreteColorSchemeNames().size() == 0) {
                DialogFactory.showErrorDialog(this, I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.at-least-a-discrete-color-scheme-must-be-configured"), I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.wrong-discrete-color-scheme-number"));
            } else if (ColorScheme.rangeColorSchemeNames().size() == 0) {
                DialogFactory.showErrorDialog(this, I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.at-least-a-range-color-scheme-must-be-configured"), I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.wrong-range-color-scheme-number"));
            } else {
                this.setVisible(false);
            }
        } else {
            DialogFactory.showErrorDialog(this, I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.at-least-a-color-scheme-must-be-configured"), I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.wrong-color-scheme-number"));
        }
    }

    protected void setButtonForeground(JButton button) {
        if (button.getBackground().getGreen() < 100 && button.getBackground().getBlue() < 100 && button.getBackground().getRed() < 100) {
            button.setForeground(Color.WHITE);
        } else {
            button.setForeground(Color.BLACK);
        }
    }

    protected void loadSelectedColorScheme(String colorSchemeName) {
        this.lastSelectedColorScheme = colorSchemeName;
        int selectedItemsNumber = this.discreteColorSchemePanel.getLeftList().getSelectedItems().size() + this.discreteColorSchemePanel.getRightList().getSelectedItems().size() + this.rangeColorSchemePanel.getLeftList().getSelectedItems().size() + this.rangeColorSchemePanel.getRightList().getSelectedItems().size();
        int visibleItemsNumber = this.discreteColorSchemePanel.getLeftItems().size() + this.discreteColorSchemePanel.getRightItems().size() + this.rangeColorSchemePanel.getLeftItems().size() + this.rangeColorSchemePanel.getRightItems().size();
        this.deleteSelectedColorSchemesButton.setEnabled(selectedItemsNumber > 0);
        this.deleteAllVisibleColorSchemesButton.setEnabled(visibleItemsNumber > 0);
        if (colorSchemeName != null) {
            ColorScheme scheme = ColorScheme.create(colorSchemeName);
            this.schemeNameTextField.setText(scheme.getName());
            this.discreteSchemeCheckBox.setSelected(ColorScheme.discreteColorSchemeNames().contains(colorSchemeName));
            this.rangeSchemeCheckBox.setSelected(ColorScheme.rangeColorSchemeNames().contains(colorSchemeName));
            Color[] newColors = new Color[scheme.getColors().size()];
            this.colors = scheme.getColors().toArray(newColors);
            this.numColors = newColors.length;
            this.colorNumberSpinner.setValue(scheme.getColors().size());
            this.updateComponents();
            this.updateColorPanel();
        }
    }

    protected void deleteSelectedColorSchemes() {
        HashSet selectedItems = new HashSet();
        selectedItems.addAll(this.discreteColorSchemePanel.getLeftList().getSelectedItems());
        selectedItems.addAll(this.discreteColorSchemePanel.getRightList().getSelectedItems());
        selectedItems.addAll(this.rangeColorSchemePanel.getLeftList().getSelectedItems());
        selectedItems.addAll(this.rangeColorSchemePanel.getRightList().getSelectedItems());
        for (String currentSchemeName : selectedItems) {
            ColorScheme.nameToColorsMap().remove(currentSchemeName);
            ColorScheme.rangeColorSchemeNames().remove(currentSchemeName);
            ColorScheme.discreteColorSchemeNames().remove(currentSchemeName);
        }
        this.loadColorSchemes();
    }

    protected void deleteAllVisibleColorSchemes() {
        HashSet visibleItems = new HashSet();
        visibleItems.addAll(this.discreteColorSchemePanel.getLeftItems());
        visibleItems.addAll(this.discreteColorSchemePanel.getRightItems());
        visibleItems.addAll(this.rangeColorSchemePanel.getLeftItems());
        visibleItems.addAll(this.rangeColorSchemePanel.getRightItems());
        for (String currentSchemeName : visibleItems) {
            ColorScheme.nameToColorsMap().remove(currentSchemeName);
            ColorScheme.rangeColorSchemeNames().remove(currentSchemeName);
            ColorScheme.discreteColorSchemeNames().remove(currentSchemeName);
        }
        this.loadColorSchemes();
    }

    protected boolean isInputValid() {
        boolean ok = this.schemeNameTextField.getInputVerifier().verify(this.schemeNameTextField);
        if (ok) {
            boolean bl = ok = this.discreteSchemeCheckBox.isSelected() || this.rangeSchemeCheckBox.isSelected();
            if (!ok) {
                DialogFactory.showErrorDialog(this, I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.you-must-select-at-least-a-range-color-type"), I18N.getString("org.saig.jump.widgets.simbology.ColorSchemesConfigDialog.color-type-no-selected"));
            }
        }
        return ok;
    }

    protected void saveChangesToCurrentScheme() {
        String schemeName = this.schemeNameTextField.getText().trim();
        int numberOfSteps = this.colorRangeNumberSpinner.getIntValue();
        boolean discreteScheme = this.discreteSchemeCheckBox.isSelected();
        boolean rangeScheme = this.rangeSchemeCheckBox.isSelected();
        if (this.lastSelectedColorScheme != null) {
            ColorScheme.discreteColorSchemeNames().remove(this.lastSelectedColorScheme);
            ColorScheme.rangeColorSchemeNames().remove(this.lastSelectedColorScheme);
        } else {
            ColorScheme.discreteColorSchemeNames().remove(schemeName);
            ColorScheme.rangeColorSchemeNames().remove(schemeName);
        }
        if (discreteScheme) {
            ColorScheme.discreteColorSchemeNames().add(schemeName);
            Collections.sort((List)ColorScheme.discreteColorSchemeNames());
        }
        if (rangeScheme) {
            ColorScheme.rangeColorSchemeNames().add(schemeName);
            Collections.sort((List)ColorScheme.rangeColorSchemeNames());
        }
        if (ColorScheme.nameToColorsMap().containsKey(schemeName)) {
            ColorScheme.nameToColorsMap().remove(schemeName);
        }
        if (this.colorRangeRadioButton.isSelected()) {
            ColorGenerator generator = null;
            generator = this.colors.length == 2 ? new ColorGenerator(numberOfSteps, this.colors[0], this.colors[1]) : (this.colors.length == 3 ? new ColorGenerator(numberOfSteps, this.colors[0], this.colors[1], this.colors[2]) : new ColorGenerator(numberOfSteps, this.colors));
            ColorScheme.nameToColorsMap().addItems(schemeName, Arrays.asList(generator.getColorArray()));
        } else {
            ColorScheme.nameToColorsMap().addItems(schemeName, Arrays.asList(this.colors));
        }
        this.loadColorSchemes();
    }
}

