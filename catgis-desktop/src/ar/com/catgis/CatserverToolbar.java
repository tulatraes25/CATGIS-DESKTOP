package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.net.URL;

public class CatserverToolbar extends JPanel {

    public CatserverToolbar() {
        setLayout(new BorderLayout(10, 0));
        setOpaque(true);
        setBackground(new Color(245, 250, 255));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(201, 214, 235)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        add(buildInfoPanel(), BorderLayout.WEST);
        add(buildButtonsPanel(), BorderLayout.CENTER);
    }

    private JPanel buildInfoPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(I18n.t("CATSERVER"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(new Color(24, 40, 72));

        JLabel subtitle = new JLabel(I18n.t("Conexion y capas PostgreSQL / PostGIS"));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11.5f));
        subtitle.setForeground(new Color(88, 98, 112));

        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitle);
        return panel;
    }

    private JPanel buildButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);

        JButton openButton = createButton(I18n.t("Abrir CATSERVER"), createCatserverIcon());
        openButton.setToolTipText(I18n.t("Abrir CATSERVER para conectar, listar y cargar capas."));
        openButton.addActionListener(e -> PostgisDataSourceAction.openCatserverBrowser());

        panel.add(openButton);
        return panel;
    }

    private javax.swing.Icon createCatserverIcon() {
        URL url = CatserverToolbar.class.getResource("/help/assets/catserver-connect.png");
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            java.awt.Image scaled = icon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return AppIcons.projectIcon();
    }

    private JButton createButton(String text, javax.swing.Icon icon) {
        JButton button = new JButton(text, icon);
        button.setFocusable(false);
        button.setMargin(new Insets(6, 10, 6, 10));
        button.setPreferredSize(new Dimension(Math.max(190, button.getPreferredSize().width), 34));
        return button;
    }
}
