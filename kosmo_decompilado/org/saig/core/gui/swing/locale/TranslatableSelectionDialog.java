/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.locale;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Set;
import javax.swing.AbstractCellEditor;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.LocaleIconFactory;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.language.ITranslatable;
import org.saig.jump.lang.I18N;

public class TranslatableSelectionDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    private JScrollPane localeListScrollPane;
    private JTable table;
    private ITranslatable objTranslatable;
    private boolean ok;
    private TitleLanguage[] titleRuleLanguages;

    public TranslatableSelectionDialog(JFrame parent, boolean modal, String title, ITranslatable obj) {
        super((Frame)parent, modal);
        this.objTranslatable = obj;
        this.setContentPane(this.getMainPanel());
        this.setTitle(title);
        this.pack();
        GUIUtil.centreOnWindow(this);
        this.setVisible(true);
    }

    private JPanel getMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.localeListScrollPane = new JScrollPane();
        Set<Locale> availablesLanguages = LocaleManager.getAvailablesLocales();
        this.titleRuleLanguages = new TitleLanguage[availablesLanguages.size()];
        int i = 0;
        for (Locale locale : availablesLanguages) {
            String title = "";
            title = this.objTranslatable.getTitle(locale);
            this.titleRuleLanguages[i++] = new TitleLanguage(locale, title);
        }
        TitleLanguageTableModel model = new TitleLanguageTableModel(this.titleRuleLanguages);
        this.table = new JTable(model);
        this.table.setRowHeight(100);
        this.table.setColumnSelectionAllowed(false);
        TableColumn column = this.table.getColumnModel().getColumn(0);
        column.setWidth(150);
        column.setPreferredWidth(150);
        TitleRuleLanguageTableCellRenderer renderer = new TitleRuleLanguageTableCellRenderer();
        column.setCellRenderer(renderer);
        TitleLanguageEditor editor = new TitleLanguageEditor();
        column.setCellEditor(editor);
        this.localeListScrollPane.getViewport().add((Component)this.table, null);
        this.localeListScrollPane.setMinimumSize(new Dimension(400, 320));
        this.localeListScrollPane.setPreferredSize(new Dimension(400, 320));
        mainPanel.add((Component)this.localeListScrollPane, "Center");
        mainPanel.add((Component)this.getOKCancelPanel(), "South");
        return mainPanel;
    }

    private OKCancelPanel getOKCancelPanel() {
        final OKCancelPanel okCancelPanel = new OKCancelPanel();
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ae) {
                TranslatableSelectionDialog.this.ok = okCancelPanel.wasOKPressed();
                if (TranslatableSelectionDialog.this.ok) {
                    int row = TranslatableSelectionDialog.this.table.getSelectedRow();
                    TableCellEditor tableCellEditor = TranslatableSelectionDialog.this.table.getCellEditor(row, 0);
                    tableCellEditor.stopCellEditing();
                    int i = 0;
                    while (i < TranslatableSelectionDialog.this.titleRuleLanguages.length) {
                        TranslatableSelectionDialog.this.objTranslatable.setTitle(TranslatableSelectionDialog.this.titleRuleLanguages[i].getText(), TranslatableSelectionDialog.this.titleRuleLanguages[i].locale);
                        ++i;
                    }
                }
                TranslatableSelectionDialog.this.setVisible(false);
                TranslatableSelectionDialog.this.dispose();
            }
        });
        return okCancelPanel;
    }

    public boolean isOk() {
        return this.ok;
    }

    private class LocalePanel
    extends JPanel {
        private static final long serialVersionUID = 1L;
        private JTextArea textArea;
        private JLabel localeIconLabel;
        private TitleLanguage titleLanguage;

        public LocalePanel() {
            this.setLayout(new GridBagLayout());
            this.setOpaque(true);
            JScrollPane listScrollPane = new JScrollPane(22, 31);
            this.textArea = new JTextArea();
            this.textArea.setLineWrap(true);
            this.textArea.setWrapStyleWord(true);
            this.textArea.setColumns(10);
            this.textArea.setRows(5);
            this.textArea.setEditable(true);
            this.textArea.setFont(new JLabel().getFont());
            listScrollPane.setViewportView(this.textArea);
            listScrollPane.setMinimumSize(new Dimension(150, 50));
            listScrollPane.setPreferredSize(new Dimension(150, 50));
            this.localeIconLabel = new JLabel();
            FormUtils.addRowInGBL(this, 0, 0, this.localeIconLabel);
            FormUtils.addRowInGBL(this, 1, 0, listScrollPane);
        }

        public void setText(String text) {
            this.titleLanguage.text = text;
            this.textArea.setText(this.titleLanguage.getText());
        }

        public TitleLanguage getTitleLanguage() {
            return this.titleLanguage;
        }

        public void setTitleRuleLanguage(TitleLanguage titleLanguage) {
            this.titleLanguage = titleLanguage;
            this.localeIconLabel.setIcon(LocaleIconFactory.getIcon(titleLanguage.getLocale()));
            this.localeIconLabel.setText(titleLanguage.getLocale().getDisplayName());
            this.textArea.setText(titleLanguage.getText());
        }
    }

    private class TitleLanguage {
        private Locale locale;
        private String text;

        public TitleLanguage(Locale locale, String text) {
            this.locale = locale;
            this.text = text;
        }

        public Locale getLocale() {
            return this.locale;
        }

        public String getText() {
            return this.text;
        }
    }

    private class TitleLanguageEditor
    extends AbstractCellEditor
    implements TableCellEditor {
        private static final long serialVersionUID = 1L;
        private LocalePanel ruleLocalePanel;

        @Override
        public Object getCellEditorValue() {
            String translation = this.ruleLocalePanel.textArea.getText().trim();
            this.ruleLocalePanel.setText(translation);
            return this.ruleLocalePanel.getTitleLanguage();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            TitleLanguage titleRuleLanguage = (TitleLanguage)value;
            this.ruleLocalePanel = new LocalePanel();
            this.ruleLocalePanel.setTitleRuleLanguage(titleRuleLanguage);
            this.ruleLocalePanel.setNextFocusableComponent(this.ruleLocalePanel.textArea);
            return this.ruleLocalePanel;
        }
    }

    class TitleLanguageTableModel
    extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private TitleLanguage[] titleLanguages;
        private String[] columnNames = new String[]{I18N.getString("org.saig.core.gui.swing.locale.TranslatableSelectionDialog.Languages")};

        public TitleLanguageTableModel(TitleLanguage[] titleLanguages) {
            this.titleLanguages = titleLanguages;
        }

        @Override
        public int getColumnCount() {
            return this.columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return this.columnNames[column];
        }

        @Override
        public int getRowCount() {
            return this.titleLanguages.length;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return this.titleLanguages[row];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return this.getValueAt(0, column).getClass();
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            this.titleLanguages[row] = (TitleLanguage)value;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }
    }

    private class TitleRuleLanguageTableCellRenderer
    implements TableCellRenderer {
        private LocalePanel ruleLocalePanel;

        private TitleRuleLanguageTableCellRenderer() {
            this.ruleLocalePanel = new LocalePanel();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            this.ruleLocalePanel.setTitleRuleLanguage((TitleLanguage)value);
            return this.ruleLocalePanel;
        }
    }
}

