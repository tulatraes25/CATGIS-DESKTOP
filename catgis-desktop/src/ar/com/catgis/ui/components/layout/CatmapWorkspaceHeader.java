package ar.com.catgis.ui.components.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The CATMAP Workspace header banner -- title and subtitle.
 * Extracted from {@code buildCatmapWorkspaceHeader()}.
 */
public class CatmapWorkspaceHeader extends JPanel {

    public CatmapWorkspaceHeader() {
        super(new BorderLayout(0, 2));
        setOpaque(false);

        JLabel title = new JLabel("CATMAP Workspace");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(19, 31, 48));

        JLabel subtitle = new JLabel("Subprograma cartografico para maquetacion, mapa vivo y salida final");
        subtitle.setForeground(new Color(77, 87, 101));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));

        add(title, BorderLayout.NORTH);
        add(subtitle, BorderLayout.SOUTH);
    }
}
