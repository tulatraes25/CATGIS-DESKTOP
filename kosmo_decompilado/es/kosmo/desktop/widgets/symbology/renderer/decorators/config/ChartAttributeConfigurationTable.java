/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.renderer.decorators.config;

import es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartAttributeSelectionTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.saig.jump.lang.I18N;

public class ChartAttributeConfigurationTable
extends JTable {
    private static final long serialVersionUID = 1L;

    public ChartAttributeConfigurationTable(ChartAttributeSelectionTableModel tm) {
        super(tm);
        this.getTableHeader().setReorderingAllowed(false);
        this.setDefaultRenderer(Color.class, new ColorRenderer(true));
        this.setDefaultEditor(Color.class, new ColorEditor());
    }

    @Override
    public ChartAttributeSelectionTableModel getModel() {
        return (ChartAttributeSelectionTableModel)super.getModel();
    }

    private class ColorEditor
    extends AbstractCellEditor
    implements TableCellEditor,
    ActionListener {
        private static final long serialVersionUID = 1L;
        Color currentColor;
        JButton button = new JButton();
        JColorChooser colorChooser;
        JDialog dialog;
        protected static final String EDIT = "edit";

        public ColorEditor() {
            this.button.setActionCommand(EDIT);
            this.button.addActionListener(this);
            this.button.setBorderPainted(false);
            this.colorChooser = new JColorChooser();
            this.dialog = JColorChooser.createDialog(this.button, I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartAttributeConfigurationTable.Pick-a-color"), true, this.colorChooser, this, null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                this.button.setBackground(this.currentColor);
                this.colorChooser.setColor(this.currentColor);
                this.dialog.setVisible(true);
                this.fireEditingStopped();
            } else {
                this.currentColor = this.colorChooser.getColor();
            }
        }

        @Override
        public Object getCellEditorValue() {
            return this.currentColor;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentColor = (Color)value;
            return this.button;
        }
    }

    private class ColorRenderer
    extends JLabel
    implements TableCellRenderer {
        private static final long serialVersionUID = 1L;
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            this.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int column) {
            Color newColor = (Color)color;
            this.setBackground(newColor);
            if (this.isBordered) {
                if (isSelected) {
                    if (this.selectedBorder == null) {
                        this.selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                    }
                    this.setBorder(this.selectedBorder);
                } else {
                    if (this.unselectedBorder == null) {
                        this.unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                    }
                    this.setBorder(this.unselectedBorder);
                }
            }
            this.setToolTipText(String.valueOf(I18N.getString("es.kosmo.desktop.widgets.symbology.renderer.decorators.config.ChartAttributeConfigurationTable.RGB-value")) + ": " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());
            return this;
        }
    }
}

