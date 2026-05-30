package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CartographyToolbar extends JPanel {

    public CartographyToolbar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        setOpaque(false);

        JButton compositorButton = new JButton(I18n.t("Abrir CATMAP"));
        compositorButton.setFont(compositorButton.getFont().deriveFont(Font.PLAIN, 11f));
        compositorButton.setFocusable(false);
        compositorButton.setMargin(new Insets(2, 8, 2, 8));
        compositorButton.setContentAreaFilled(false);
        compositorButton.setBorderPainted(false);
        compositorButton.setOpaque(false);
        compositorButton.setToolTipText(I18n.t("Compositor cartografico para maquetacion, impresion y salida final."));
        compositorButton.addActionListener(e -> MapLayoutComposerDialog.open());
        compositorButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { compositorButton.setOpaque(true); compositorButton.setBackground(new Color(0xE0E0E0)); }
            public void mouseExited(MouseEvent e) { compositorButton.setOpaque(false); compositorButton.repaint(); }
        });

        add(compositorButton);
    }
}
