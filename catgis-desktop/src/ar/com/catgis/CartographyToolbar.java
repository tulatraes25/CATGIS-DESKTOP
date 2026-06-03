package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

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

        JButton standaloneButton = new JButton(I18n.t("CATMAP Standalone"));
        standaloneButton.setFont(standaloneButton.getFont().deriveFont(Font.PLAIN, 11f));
        standaloneButton.setFocusable(false);
        standaloneButton.setMargin(new Insets(2, 8, 2, 8));
        standaloneButton.setContentAreaFilled(false);
        standaloneButton.setBorderPainted(false);
        standaloneButton.setOpaque(false);
        standaloneButton.setToolTipText(I18n.t("Abrir CATMAP como aplicacion independiente."));
        standaloneButton.addActionListener(e -> launchCatmapStandalone());
        standaloneButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { standaloneButton.setOpaque(true); standaloneButton.setBackground(new Color(0xE0E0E0)); }
            public void mouseExited(MouseEvent e) { standaloneButton.setOpaque(false); standaloneButton.repaint(); }
        });

        add(compositorButton);
        add(standaloneButton);
    }

    private void launchCatmapStandalone() {
        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classPath = System.getProperty("java.class.path");

            ProcessBuilder pb = new ProcessBuilder(
                    javaBin, "-cp", classPath,
                    "ar.com.catgis.catmap.Main"
            );
            pb.directory(new File(System.getProperty("user.dir")));
            pb.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir CATMAP Standalone:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
