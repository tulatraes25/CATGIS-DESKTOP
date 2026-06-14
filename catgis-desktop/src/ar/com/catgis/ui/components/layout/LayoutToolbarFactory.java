package ar.com.catgis.ui.components.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 * Static factory for toolbar buttons and groups.
 * Extracted from {@code createToolbarButton()}, {@code buildToolbarGroup()}, and {@code styleWorkToolButton()}.
 */
public final class LayoutToolbarFactory {

    private LayoutToolbarFactory() {
        // static utility
    }

    /**
     * Creates a toolbar-style button with icon, text, and tooltip.
     */
    public static JButton createToolbarButton(String text, Icon icon, String toolTip, Runnable action) {
        JButton button = new JButton(text, icon);
        button.setFocusable(false);
        button.setHorizontalTextPosition(JButton.CENTER);
        button.setVerticalTextPosition(JButton.BOTTOM);
        button.setMargin(new Insets(4, 6, 4, 6));
        button.setToolTipText(toolTip);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.setBackground(Color.WHITE);
        button.setOpaque(true);
        button.addActionListener(e -> {
            if (action != null) {
                action.run();
            }
        });
        return button;
    }

    /**
     * Builds a bordered toolbar group with a title label and buttons in a JToolBar.
     */
    public static JPanel buildToolbarGroup(String title, JButton... buttons) {
        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setOpaque(false);
        group.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 225, 233)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        JLabel label = new JLabel(title);
        label.setForeground(new Color(76, 85, 97));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        group.add(label, BorderLayout.NORTH);

        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setRollover(true);
        for (JButton button : buttons) {
            if (button != null) {
                bar.add(button);
            }
        }
        group.add(bar, BorderLayout.CENTER);
        return group;
    }

    /**
     * Styles a work-tool button as active (blue highlight) or inactive (default).
     */
    public static void styleWorkToolButton(JButton button, boolean active) {
        if (button == null) {
            return;
        }
        if (active) {
            button.setBackground(new Color(221, 235, 255));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(66, 133, 244)),
                    BorderFactory.createEmptyBorder(2, 4, 2, 4)
            ));
        } else {
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 218, 228)),
                    BorderFactory.createEmptyBorder(2, 4, 2, 4)
            ));
        }
    }
}
