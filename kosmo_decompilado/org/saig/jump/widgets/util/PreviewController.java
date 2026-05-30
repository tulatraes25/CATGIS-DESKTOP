/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.util;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.PreviewPanel;
import org.saig.jump.widgets.util.Previewable;

public class PreviewController
implements ListSelectionListener {
    private static final String UNNAMED_STRING = "(" + I18N.getString("org.saig.jump.widgets.util.PreviewController.Unnamed") + ")";
    private final PreviewPanel previewPanel;

    public PreviewController(PreviewPanel previewPanel) {
        this.previewPanel = previewPanel;
        this.previewPanel.getElementsList().getSelectionModel().addListSelectionListener(this);
        this.previewPanel.getElementsList().setCellRenderer(new PreviewableListCellRenderer());
        previewPanel.getElementsList().installJTextComponent(previewPanel.getSearchTextField());
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (!lse.getValueIsAdjusting() && lse.getSource() == this.previewPanel.getElementsList().getSelectionModel()) {
            this.performListSelectionChange();
        }
    }

    private void performListSelectionChange() {
        Object[] selectedValues = this.previewPanel.getElementsList().getSelectedValues();
        Previewable selectedElement = null;
        if (selectedValues.length == 1) {
            selectedElement = (Previewable)selectedValues[0];
            this.previewPanel.getImagePanel().setImage(selectedElement.getImage());
        } else {
            this.previewPanel.getImagePanel().setImage(null);
        }
    }

    public void refresSelection() {
        this.performListSelectionChange();
    }

    private class PreviewableListCellRenderer
    extends JLabel
    implements ListCellRenderer {
        private static final long serialVersionUID = 1L;

        private PreviewableListCellRenderer() {
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Previewable previewable = (Previewable)value;
            String nombre = previewable.getTitle();
            if (StringUtils.isBlank((String)nombre)) {
                nombre = UNNAMED_STRING;
            }
            this.setText(nombre);
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            this.setEnabled(list.isEnabled());
            this.setFont(list.getFont());
            this.setOpaque(true);
            return this;
        }
    }
}

