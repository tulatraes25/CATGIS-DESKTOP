/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.images.norths.PredefinedNorthLoader;

public class NorthSelectionPanel
extends JPanel {
    private JComboBox predefinedImagesComboBox = null;
    private JLabel previewLabel = null;
    private JPanel predefinedSymbolsPanel = new JPanel();
    private JPanel imagePreviewPanel = new JPanel();
    private ImageIcon selectedIcon = null;

    public NorthSelectionPanel(ImageIcon icon, float angle) {
        this.initialize();
        this.loadPredefinedNorths();
        this.initializePreviewLabel(icon);
    }

    private void initializePreviewLabel(ImageIcon icon) {
        this.selectedIcon = icon;
        this.previewLabel.setIcon(icon);
    }

    private void loadPredefinedNorths() {
        ImageIcon icon = PredefinedNorthLoader.icon("North.gif");
        this.getPredefinedImagesComboBox().addItem(icon);
        String ceros = "00";
        int i = 1;
        while (i < 27) {
            if (i > 9) {
                ceros = "0";
            }
            String iconName = String.valueOf(ceros) + i + ".jpg";
            icon = PredefinedNorthLoader.icon(iconName);
            icon.setDescription(iconName);
            this.getPredefinedImagesComboBox().addItem(icon);
            ++i;
        }
    }

    private void initialize() {
        GridBagConstraints gridBagConstraintsPredefinedImages = new GridBagConstraints();
        gridBagConstraintsPredefinedImages.gridx = 0;
        gridBagConstraintsPredefinedImages.gridy = 0;
        gridBagConstraintsPredefinedImages.gridheight = 1;
        gridBagConstraintsPredefinedImages.gridwidth = 1;
        gridBagConstraintsPredefinedImages.anchor = 10;
        gridBagConstraintsPredefinedImages.fill = 2;
        gridBagConstraintsPredefinedImages.insets = new Insets(10, 10, 10, 10);
        GridBagConstraints gridBagConstraintsPreview = new GridBagConstraints();
        gridBagConstraintsPreview.gridx = 0;
        gridBagConstraintsPreview.gridy = 10;
        gridBagConstraintsPreview.gridheight = 1;
        gridBagConstraintsPreview.gridwidth = 1;
        gridBagConstraintsPreview.anchor = 10;
        gridBagConstraintsPreview.insets = new Insets(10, 10, 10, 10);
        gridBagConstraintsPreview.fill = 0;
        GridBagConstraints gridBagConstraintsRotation = new GridBagConstraints();
        gridBagConstraintsPreview.gridx = 0;
        gridBagConstraintsPreview.gridy = 1;
        gridBagConstraintsPreview.gridheight = 1;
        gridBagConstraintsPreview.gridwidth = 1;
        gridBagConstraintsPreview.anchor = 10;
        gridBagConstraintsPreview.insets = new Insets(10, 10, 10, 10);
        gridBagConstraintsPreview.fill = 0;
        this.previewLabel = new JLabel();
        this.previewLabel.setHorizontalAlignment(0);
        this.previewLabel.setVerticalAlignment(0);
        Dimension dim = new Dimension(300, 300);
        this.previewLabel.setMinimumSize(dim);
        this.previewLabel.setPreferredSize(dim);
        this.previewLabel.setMaximumSize(dim);
        this.predefinedSymbolsPanel.add(this.getPredefinedImagesComboBox());
        this.predefinedSymbolsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), I18N.getString("org.saig.jump.widgets.print.elements.NorthSelectionPanel.predefined-north-symbol"), 0, 0, null, null));
        this.imagePreviewPanel.add(this.previewLabel);
        this.imagePreviewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), I18N.getString("org.saig.jump.widgets.print.elements.NorthSelectionPanel.preview"), 0, 0, null, null));
        this.setLayout(new GridBagLayout());
        this.add((Component)this.predefinedSymbolsPanel, gridBagConstraintsPredefinedImages);
        this.add((Component)this.imagePreviewPanel, gridBagConstraintsPreview);
    }

    private JComboBox getPredefinedImagesComboBox() {
        if (this.predefinedImagesComboBox == null) {
            this.predefinedImagesComboBox = new JComboBox();
            this.predefinedImagesComboBox.setPreferredSize(new Dimension(80, 35));
            this.predefinedImagesComboBox.setRenderer(new ComboBoxRenderer());
            this.predefinedImagesComboBox.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    NorthSelectionPanel.this.selectedIcon = (ImageIcon)cb.getSelectedItem();
                    ImageIcon thumbnail = null;
                    thumbnail = NorthSelectionPanel.this.selectedIcon.getIconWidth() > ((NorthSelectionPanel)NorthSelectionPanel.this).previewLabel.getPreferredSize().width ? new ImageIcon(NorthSelectionPanel.this.selectedIcon.getImage().getScaledInstance(((NorthSelectionPanel)NorthSelectionPanel.this).previewLabel.getPreferredSize().width, -1, 1)) : NorthSelectionPanel.this.selectedIcon;
                    NorthSelectionPanel.this.previewLabel.setIcon(thumbnail);
                }
            });
        }
        return this.predefinedImagesComboBox;
    }

    public ImageIcon getSelectedIcon() {
        return this.selectedIcon;
    }

    public float getSelectedAngle() {
        return 0.0f;
    }

    private class ComboBoxRenderer
    extends JLabel
    implements ListCellRenderer {
        public ComboBoxRenderer() {
            this.setHorizontalAlignment(0);
            this.setVerticalAlignment(0);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            ImageIcon icon = (ImageIcon)value;
            this.setIcon(GUIUtil.resize(icon, 30));
            return this;
        }
    }
}

