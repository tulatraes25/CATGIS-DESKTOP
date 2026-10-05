package ar.com.catgis.ui.components.layout;

import ar.com.catgis.layout.LayoutTemplate;
import ar.com.catgis.layout.PageOrientation;
import ar.com.catgis.layout.PageSizePreset;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

/**
 * Properties panel (right sidebar) with page size, orientation, template, and card layout.
 * Extracted from {@code buildPropertiesPanel()}.
 */
public class LayoutPropertiesPanel extends JPanel {

    private final JLabel infoLabel;
    private final JPanel cardPanel;

    public LayoutPropertiesPanel(
            JComboBox<PageSizePreset> pageSizeCombo,
            JComboBox<PageOrientation> orientationCombo,
            JComboBox<LayoutTemplate> templateCombo,
            Runnable onShowTemplates,
            JPanel cardPanel,
            JLabel infoLabel) {
        super(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setBackground(new Color(0xF7F8FA));
        setPreferredSize(new Dimension(230, 100));

        this.cardPanel = cardPanel;
        this.infoLabel = infoLabel;

        JLabel hdr = new JLabel("Propiedades");
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 11f));
        hdr.setForeground(new Color(0x333333));
        add(hdr, BorderLayout.NORTH);

        // Page section
        JPanel pageSection = new JPanel(new BorderLayout(2, 2));
        pageSection.setOpaque(false);
        JLabel pageLbl = new JLabel("Pagina");
        pageLbl.setFont(pageLbl.getFont().deriveFont(Font.BOLD, 10f));
        pageSection.add(pageLbl, BorderLayout.NORTH);
        JPanel pageControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        pageControls.setOpaque(false);
        pageControls.add(new JLabel("Tamano:"));
        pageSizeCombo.setPreferredSize(new Dimension(120, 22));
        pageControls.add(pageSizeCombo);
        pageControls.add(orientationCombo);
        pageSection.add(pageControls, BorderLayout.CENTER);

        // Template section
        JPanel tmplRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        tmplRow.setOpaque(false);
        tmplRow.add(new JLabel("Plantilla:"));
        templateCombo.setPreferredSize(new Dimension(150, 22));
        tmplRow.add(templateCombo);

        JPanel headerArea = new JPanel(new BorderLayout(0, 2));
        headerArea.setOpaque(false);
        headerArea.add(pageSection, BorderLayout.NORTH);
        headerArea.add(tmplRow, BorderLayout.SOUTH);
        add(headerArea, BorderLayout.NORTH);

        JSeparator sep = new JSeparator();
        headerArea.add(sep, BorderLayout.SOUTH);

        // Card panel is populated by caller
        cardPanel.setOpaque(false);

        JScrollPane sp = new JScrollPane(cardPanel);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        add(sp, BorderLayout.CENTER);
    }
}
