package ar.com.catgis.ui.components.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

/**
 * Layout structure tree panel with Edit / Mostrar / Bloq action buttons.
 * Extracted from {@code buildLayoutStructurePanel()}.
 */
public class LayoutStructureTreePanel extends JPanel {

    public LayoutStructureTreePanel(JTree layoutStructureTree,
                                    Runnable onEdit,
                                    Runnable onToggleVisibility,
                                    Runnable onToggleLock) {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Elementos del layout"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));

        JScrollPane treeScroll = new JScrollPane(layoutStructureTree);
        treeScroll.setPreferredSize(new Dimension(286, 122));
        add(treeScroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new java.awt.GridLayout(1, 3, 6, 0));
        actions.setOpaque(false);
        JButton editButton = new JButton("Editar");
        editButton.addActionListener(e -> onEdit.run());
        JButton visibilityButton = new JButton("Mostrar");
        visibilityButton.addActionListener(e -> onToggleVisibility.run());
        JButton lockButton = new JButton("Bloq");
        lockButton.addActionListener(e -> onToggleLock.run());
        actions.add(editButton);
        actions.add(visibilityButton);
        actions.add(lockButton);

        JLabel hint = new JLabel("<html>Doble clic edita el elemento seleccionado. Mostrar/Bloq controlan visibilidad y movimiento.</html>");
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        hint.setForeground(new Color(88, 98, 112));
        JPanel footer = new JPanel(new BorderLayout(0, 6));
        footer.setOpaque(false);
        footer.add(actions, BorderLayout.NORTH);
        footer.add(hint, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
    }
}
