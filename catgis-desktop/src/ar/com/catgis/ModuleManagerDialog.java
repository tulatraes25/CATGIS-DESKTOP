package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public class ModuleManagerDialog extends JDialog {

    private final JPanel contentPanel;

    public ModuleManagerDialog() {
        setTitle("Gestor de modulos");
        setModal(false);
        setSize(760, 520);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setLayout(new BorderLayout(8, 8));

        JLabel header = new JLabel("Modulos nativos inspirados en extensiones utiles de Kosmo, integrados como bloques propios de CATGIS.");
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(header, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JButton closeButton = new JButton("Cerrar");
        closeButton.addActionListener(e -> dispose());
        footer.add(closeButton);
        add(footer, BorderLayout.SOUTH);

        reloadModules();
        ModuleRegistry.addChangeListener(this::reloadModules);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new ModuleManagerDialog().setVisible(true));
    }

    private void reloadModules() {
        contentPanel.removeAll();

        for (CatgisModule module : ModuleRegistry.getModules()) {
            JPanel card = new JPanel(new BorderLayout(8, 8));
            int cardHeight = 110 + (module.getActions().size() * 18);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardHeight));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 224, 230)),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            JPanel top = new JPanel(new BorderLayout(8, 4));
            JCheckBox enabledCheck = new JCheckBox(module.getName(), module.isEnabled());
            enabledCheck.addActionListener(e -> ModuleRegistry.setModuleEnabled(module.getId(), enabledCheck.isSelected()));
            enabledCheck.setToolTipText(module.isCore()
                    ? "Modulo nativo de CATGIS. Se puede activar o desactivar sin romper la arquitectura."
                    : "Activar o desactivar este modulo.");
            top.add(enabledCheck, BorderLayout.WEST);
            top.add(new JLabel(module.getCategory().getDisplayName()
                    + " | Origen Kosmo: " + module.getKosmoOrigin()
                    + (module.isCore() ? " | Nativo" : "")), BorderLayout.EAST);

            JPanel center = new JPanel();
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.add(BoxLabel.of(module.getDescription()));
            center.add(BoxLabel.of("Acciones: " + module.getActions().size()
                    + " | Estado del modulo: " + (module.isEnabled() ? "Activo" : "Desactivado")
                    + (module.isCore() ? " | Nativo" : "")));
            for (CatgisModuleAction action : module.getActions()) {
                String actionState;
                if (!module.isEnabled()) {
                    actionState = "desactivada por modulo";
                } else if (!action.isAvailable()) {
                    actionState = "no disponible por contexto";
                } else {
                    actionState = "disponible ahora";
                }
                center.add(BoxLabel.of("- " + action.getName() + " (" + action.getPlacement().getDisplayName() + " | " + actionState + ")"));
            }

            card.add(top, BorderLayout.NORTH);
            card.add(center, BorderLayout.CENTER);
            contentPanel.add(card);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private static final class BoxLabel extends JLabel {
        private BoxLabel(String text) {
            super(text);
            setAlignmentX(LEFT_ALIGNMENT);
        }

        private static BoxLabel of(String text) {
            return new BoxLabel(text);
        }
    }
}
