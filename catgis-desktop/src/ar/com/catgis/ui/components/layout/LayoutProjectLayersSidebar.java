package ar.com.catgis.ui.components.layout;

import ar.com.catgis.AppIcons;
import ar.com.catgis.core.model.Layer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Project layers sidebar with visibility/organization controls.
 * Extracted from {@code buildProjectLayersSidebar()}.
 */
public class LayoutProjectLayersSidebar extends JPanel {

    public LayoutProjectLayersSidebar(
            JLabel summaryLabel,
            JList<Layer> projectLayersList,
            JLabel detailLabel,
            Runnable onToggleVisibility,
            Runnable onOpenAppearance,
            IntConsumer onMoveLayer,
            Runnable onRefreshSnapshot) {
        super(new BorderLayout(0, 8));
        setPreferredSize(new Dimension(320, 100));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel("Capas del mapa");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(27, 38, 56));

        JLabel subtitle = new JLabel("<html>Control de visibilidad, orden y simbologia sin salir de CATMAP.</html>");
        subtitle.setForeground(new Color(88, 98, 112));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11.5f));

        summaryLabel.setForeground(new Color(63, 74, 88));
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD, 11.5f));

        JPanel header = new JPanel(new GridBagLayout());
        header.setOpaque(false);
        GridBagConstraints hc = new GridBagConstraints();
        hc.gridx = 0;
        hc.gridy = 0;
        hc.weightx = 1;
        hc.fill = GridBagConstraints.HORIZONTAL;
        hc.anchor = GridBagConstraints.WEST;
        hc.insets = new Insets(0, 0, 4, 0);
        header.add(title, hc);
        hc.gridy++;
        header.add(subtitle, hc);
        hc.gridy++;
        hc.insets = new Insets(6, 0, 0, 0);
        header.add(summaryLabel, hc);

        JScrollPane scrollPane = new JScrollPane(projectLayersList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scrollPane.setPreferredSize(new Dimension(290, 360));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel buttons = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        buttons.setOpaque(false);
        JButton visibilityButton = new JButton("Visible", AppIcons.visibleIcon());
        visibilityButton.addActionListener(e -> onToggleVisibility.run());
        JButton propertiesButton = new JButton("Simbologia...", AppIcons.propertiesIcon());
        propertiesButton.addActionListener(e -> onOpenAppearance.run());
        JButton upButton = new JButton("Subir", AppIcons.upIcon());
        upButton.addActionListener(e -> onMoveLayer.accept(-1));
        JButton downButton = new JButton("Bajar", AppIcons.downIcon());
        downButton.addActionListener(e -> onMoveLayer.accept(1));
        JButton refreshButton = new JButton("Refrescar", AppIcons.attrRefreshIcon());
        refreshButton.addActionListener(e -> onRefreshSnapshot.run());
        buttons.add(visibilityButton);
        buttons.add(propertiesButton);
        buttons.add(upButton);
        buttons.add(downButton);
        buttons.add(refreshButton);

        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        footer.add(detailLabel, BorderLayout.CENTER);
        footer.add(buttons, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }
}
