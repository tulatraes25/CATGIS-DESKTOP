package ar.com.catgis.ui.components.layout;

import ar.com.catgis.AppIcons;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Bottom bar with status label and action buttons.
 * Extracted from {@code buildBottomPanel()}.
 */
public class LayoutBottomBar extends JPanel {

    public LayoutBottomBar(JLabel statusLabel,
                           Runnable onExportImage,
                           Runnable onExportPdf,
                           Runnable onPrint,
                           Runnable onClose) {
        super(new BorderLayout(8, 0));
        setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
        setOpaque(false);

        statusLabel.setForeground(new Color(77, 86, 100));
        add(statusLabel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton exportImageButton = new JButton("Exportar imagen", AppIcons.exportIcon());
        exportImageButton.addActionListener(e -> onExportImage.run());
        JButton exportPdfButton = new JButton("Exportar PDF", AppIcons.saveIcon());
        exportPdfButton.addActionListener(e -> onExportPdf.run());
        JButton printButton = new JButton("Imprimir...", AppIcons.projectIcon());
        printButton.addActionListener(e -> onPrint.run());
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> onClose.run());

        buttons.add(exportImageButton);
        buttons.add(exportPdfButton);
        buttons.add(printButton);
        buttons.add(closeButton);
        add(buttons, BorderLayout.EAST);
    }
}
