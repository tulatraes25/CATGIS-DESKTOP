/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.saig.core.gui.swing.sldeditor.property.FontListChooser;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class DefaultFontListChooser
extends FontListChooser {
    private static final long serialVersionUID = 1L;
    JTextField textField = new JTextField();
    JButton btnChooseFonts;
    String[] fontList;

    public DefaultFontListChooser() {
        this(new String[0]);
    }

    public DefaultFontListChooser(String[] selectedFonts) {
        this.textField.setEditable(false);
        this.textField.setText(this.buildFontList(selectedFonts));
        this.btnChooseFonts = new JButton("...");
        this.btnChooseFonts.setToolTipText(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser.select-font"));
        this.btnChooseFonts.setPreferredSize(FormUtils.getButtonDimension());
        this.setLayout(new BorderLayout());
        this.add(this.textField);
        this.add((Component)this.btnChooseFonts, "East");
        this.btnChooseFonts.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultFontListChooser.this.openFontDialog();
            }
        });
    }

    @Override
    public String[] getFontNames() {
        return this.fontList;
    }

    @Override
    public void setFontNames(String[] fonts) {
        this.fontList = fonts;
        this.textField.setText(this.buildFontList(fonts));
    }

    private void openFontDialog() {
        Window w = FormUtils.getWindowForComponent(this);
        FontListChooserDialog dialog = w instanceof Frame ? new FontListChooserDialog((Frame)w, this.fontList) : new FontListChooserDialog((Dialog)w, this.fontList);
        dialog.setVisible(true);
        if (dialog.exitOk()) {
            this.fontList = dialog.getSelectedFonts();
            this.textField.setText(this.buildFontList(this.fontList));
        }
    }

    private String buildFontList(String[] selectedFonts) {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (i < selectedFonts.length) {
            sb.append(selectedFonts[i]);
            if (i < selectedFonts.length - 1) {
                sb.append(", ");
            }
            ++i;
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultFontListChooser(new String[0]));
    }

    private static class FontListChooserDialog
    extends JDialog {
        private static final long serialVersionUID = 1L;
        protected boolean exitOK;
        JList lstSystemFonts;
        JList lstChosenFonts;
        JCheckBox chkChooseMultipleFonts;
        JButton btnAddFont;
        JButton btnInsertFont;
        JButton btnRemoveFont;
        JButton btnMoveUpFont;
        JButton btnMoveDownFont;
        JButton btnOk;
        JButton btnCancel;
        JScrollPane scpSystemFonts;
        JScrollPane scpChosenFonts;

        public FontListChooserDialog(Frame parent, String[] chosenFonts) {
            super(parent, true);
            this.init(chosenFonts);
        }

        public FontListChooserDialog(Dialog parent, String[] chosenFonts) {
            super(parent, true);
            this.init(chosenFonts);
        }

        public String[] getSelectedFonts() {
            if (this.chkChooseMultipleFonts.isSelected()) {
                DefaultListModel model = (DefaultListModel)this.lstChosenFonts.getModel();
                if (model.getSize() == 0) {
                    return new String[0];
                }
                String[] fonts = new String[model.getSize()];
                int i = 0;
                while (i < fonts.length) {
                    fonts[i] = (String)model.getElementAt(i);
                    ++i;
                }
                return fonts;
            }
            String chosen = (String)this.lstSystemFonts.getSelectedValue();
            if (chosen == null) {
                return new String[0];
            }
            return new String[]{chosen};
        }

        private void init(String[] chosenFonts) {
            if (chosenFonts == null) {
                chosenFonts = new String[]{};
            }
            Object[] systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            this.lstSystemFonts = new JList(new SimpleListModel(systemFonts));
            this.scpSystemFonts = new JScrollPane(this.lstSystemFonts);
            this.lstChosenFonts = new JList(new SimpleListModel(chosenFonts));
            this.scpChosenFonts = new JScrollPane(this.lstChosenFonts);
            this.chkChooseMultipleFonts = new JCheckBox(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser.choose-a-font-list"));
            this.chkChooseMultipleFonts.setSelected(true);
            this.btnAddFont = new JButton("+");
            this.btnInsertFont = new JButton(">");
            this.btnRemoveFont = new JButton("x");
            this.btnMoveUpFont = new JButton("u");
            this.btnMoveDownFont = new JButton("d");
            this.btnOk = new JButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser.ok"));
            this.btnCancel = new JButton(I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser.cancel"));
            JPanel mainPanel = new JPanel();
            JPanel fontPanel = new JPanel();
            JPanel commandPanel = new JPanel();
            commandPanel.setLayout(new FlowLayout(2, 3, 3));
            commandPanel.add(this.btnOk);
            commandPanel.add(this.btnCancel);
            fontPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = FormUtils.getDefaultInsets();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 0;
            gbc.weightx = 1.0;
            gbc.fill = 2;
            fontPanel.add((Component)this.chkChooseMultipleFonts, gbc);
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 0;
            gbc.weighty = 1.0;
            gbc.fill = 1;
            fontPanel.add((Component)this.scpSystemFonts, gbc);
            gbc.gridx = 2;
            fontPanel.add((Component)this.scpChosenFonts, gbc);
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.fill = 1;
            fontPanel.add((Component)this.btnAddFont, gbc);
            gbc.gridy = 2;
            fontPanel.add((Component)this.btnInsertFont, gbc);
            gbc.gridy = 3;
            fontPanel.add((Component)this.btnRemoveFont, gbc);
            gbc.gridy = 4;
            fontPanel.add((Component)this.btnMoveUpFont, gbc);
            gbc.gridy = 5;
            fontPanel.add((Component)this.btnMoveDownFont, gbc);
            gbc.gridy = 6;
            gbc.gridheight = 0;
            fontPanel.add((Component)new JLabel(), gbc);
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(fontPanel);
            mainPanel.add((Component)commandPanel, "South");
            this.setContentPane(mainPanel);
            this.chkChooseMultipleFonts.addChangeListener(new ChangeListener(){

                @Override
                public void stateChanged(ChangeEvent e) {
                    FontListChooserDialog.this.chooseMultipleFontsStateChanged();
                }
            });
            this.lstSystemFonts.addMouseListener(new MouseAdapter(){

                @Override
                public void mouseClicked(MouseEvent e) {
                    FontListChooserDialog.this.systemFontsMouseClicked(e);
                }
            });
            this.btnAddFont.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    FontListChooserDialog.this.btnAddActionPerformed();
                }
            });
            this.btnRemoveFont.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    FontListChooserDialog.this.btnRemoveFontActionPerformed();
                }
            });
            this.btnInsertFont.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    FontListChooserDialog.this.btnInsertFontActionPerformed();
                }
            });
            this.btnMoveUpFont.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    FontListChooserDialog.this.btnMoveUpActionPerformed();
                }
            });
            this.btnMoveDownFont.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    FontListChooserDialog.this.btnMoveDownActionPerformed();
                }
            });
            this.btnOk.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (FontListChooserDialog.this.chkChooseMultipleFonts.isSelected() && FontListChooserDialog.this.lstChosenFonts.getModel().getSize() == 0 || !FontListChooserDialog.this.chkChooseMultipleFonts.isSelected() && FontListChooserDialog.this.lstSystemFonts.getSelectedIndex() == -1) {
                        DialogFactory.showErrorDialog(FontListChooserDialog.this, I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser.you-should-select-at-least-one-item"), I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser.font-chooser"));
                        return;
                    }
                    FontListChooserDialog.this.exitOK = true;
                    FontListChooserDialog.this.dispose();
                }
            });
            this.btnCancel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    FontListChooserDialog.this.exitOK = false;
                    FontListChooserDialog.this.dispose();
                }
            });
            this.pack();
        }

        public boolean exitOk() {
            return this.exitOK;
        }

        private void btnMoveDownActionPerformed() {
            int selectedIndex = this.lstChosenFonts.getSelectedIndex();
            int size = this.lstChosenFonts.getModel().getSize();
            if (selectedIndex == -1 || selectedIndex == size - 1) {
                return;
            }
            DefaultListModel model = (DefaultListModel)this.lstChosenFonts.getModel();
            Object font1 = model.getElementAt(selectedIndex);
            Object font2 = model.getElementAt(selectedIndex + 1);
            model.setElementAt(font2, selectedIndex);
            model.setElementAt(font1, selectedIndex + 1);
            this.lstChosenFonts.setSelectedIndex(selectedIndex + 1);
        }

        private void btnMoveUpActionPerformed() {
            int selectedIndex = this.lstChosenFonts.getSelectedIndex();
            if (selectedIndex <= 0) {
                return;
            }
            DefaultListModel model = (DefaultListModel)this.lstChosenFonts.getModel();
            Object font1 = model.getElementAt(selectedIndex);
            Object font2 = model.getElementAt(selectedIndex - 1);
            model.setElementAt(font2, selectedIndex);
            model.setElementAt(font1, selectedIndex - 1);
            this.lstChosenFonts.setSelectedIndex(selectedIndex - 1);
        }

        private void btnInsertFontActionPerformed() {
            this.insertSelectedSystemFonts();
        }

        private void btnRemoveFontActionPerformed() {
            int[] selectedIndices = this.lstChosenFonts.getSelectedIndices();
            DefaultListModel model = (DefaultListModel)this.lstChosenFonts.getModel();
            int i = 0;
            while (i < selectedIndices.length) {
                model.remove(selectedIndices[i] - i);
                ++i;
            }
        }

        private void btnAddActionPerformed() {
            String result = (String)DialogFactory.showInputDialog(this, I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser.add-font"), I18N.getString("org.saig.core.gui.swing.sldeditor.property.std.DefaultFontListChooser.add-font"), null);
            if (result != null) {
                this.addToChosenFonts(result);
            }
        }

        private void chooseMultipleFontsStateChanged() {
            boolean selected = this.chkChooseMultipleFonts.isSelected();
            this.btnAddFont.setVisible(selected);
            this.btnInsertFont.setVisible(selected);
            this.btnRemoveFont.setVisible(selected);
            this.btnMoveUpFont.setVisible(selected);
            this.btnMoveDownFont.setVisible(selected);
            this.scpChosenFonts.setVisible(selected);
            if (selected) {
                String font = (String)this.lstSystemFonts.getSelectedValue();
                if (font != null) {
                    this.addToChosenFonts(font);
                }
            } else {
                this.lstSystemFonts.setSelectionMode(0);
                if (this.lstSystemFonts.getSelectedIndex() != -1) {
                    this.lstSystemFonts.setSelectedIndex(this.lstSystemFonts.getSelectedIndex());
                }
            }
        }

        private void systemFontsMouseClicked(MouseEvent e) {
            if (e.getClickCount() >= 2) {
                this.insertSelectedSystemFonts();
            }
        }

        private void insertSelectedSystemFonts() {
            Object[] fonts = this.lstSystemFonts.getSelectedValues();
            if (fonts != null) {
                int i = 0;
                while (i < fonts.length) {
                    this.addToChosenFonts((String)fonts[i]);
                    ++i;
                }
            }
        }

        private void addToChosenFonts(String font) {
            DefaultListModel model = (DefaultListModel)this.lstChosenFonts.getModel();
            int i = 0;
            while (i < model.getSize()) {
                if (model.getElementAt(i).equals(font)) {
                    return;
                }
                ++i;
            }
            model.addElement(font);
            this.lstChosenFonts.setSelectedIndex(model.getSize() - 1);
        }
    }

    private static class SimpleListModel
    extends DefaultListModel {
        private static final long serialVersionUID = 1L;

        public SimpleListModel(Object[] list) {
            if (list != null && list.length > 0) {
                this.ensureCapacity(list.length);
                int i = 0;
                while (i < list.length) {
                    this.addElement(list[i]);
                    ++i;
                }
            }
        }
    }
}

