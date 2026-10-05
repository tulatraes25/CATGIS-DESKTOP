package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

/**
 * Discoverable keyboard shortcuts dialog.
 * Shows all available key bindings organized by category.
 * Opens via F1 or Ctrl+Shift+/ from anywhere.
 */
public class ShortcutsDialog extends JDialog {

    private static final Color BG_CARD = Color.WHITE;
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color TEXT_MUTED = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(222, 226, 230);
    private static final Color HEADER_BG = new Color(248, 249, 250);

    public ShortcutsDialog() {
        setTitle("Atajos de teclado");
        setSize(640, 520);
        setLocationRelativeTo(CatgisDesktopApp.getMainFrameSafe());
        setModal(false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("General", buildGeneralPanel());
        tabs.addTab("Navegacion", buildNavigationPanel());
        tabs.addTab("Edicion", buildEditPanel());
        tabs.addTab("Analisis", buildAnalysisPanel());

        add(tabs, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        JLabel hint = new JLabel("Presione una tecla para buscar | F1 para abrir esta guia desde cualquier lugar");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(TEXT_MUTED);
        footer.add(hint);
        add(footer, BorderLayout.SOUTH);

        // Install global shortcut listener
        installGlobalShortcutListener();
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new ShortcutsDialog().setVisible(true));
    }

    /**
     * Installs a global key event listener on the main frame
     * that opens this dialog on F1 or Ctrl+Shift+/.
     */
    public static void installGlobalShortcutListener() {
        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        // F1
                        if (e.getKeyCode() == KeyEvent.VK_F1) {
                            if (!(e.getComponent() instanceof javax.swing.text.JTextComponent
                                    && ((javax.swing.text.JTextComponent) e.getComponent()).isEditable())) {
                                open();
                                return true;
                            }
                        }
                        // Ctrl+Shift+/ (question mark)
                        if (e.getKeyCode() == KeyEvent.VK_SLASH
                                && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0
                                && (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0) {
                            open();
                            return true;
                        }
                    }
                    return false;
                });
    }

    // --- Panel builders ---

    private JScrollPane buildGeneralPanel() {
        return buildShortcutPanel(new ShortcutEntry[]{
                new ShortcutEntry("Ctrl + O", "Abrir proyecto", "Carga un archivo .catgis existente"),
                new ShortcutEntry("Ctrl + S", "Guardar proyecto", "Guarda el proyecto actual"),
                new ShortcutEntry("Ctrl + Shift + S", "Guardar como...", "Guarda el proyecto con otro nombre"),
                new ShortcutEntry("Ctrl + Z", "Deshacer", "Deshace la ultima accion"),
                new ShortcutEntry("Ctrl + Y", "Rehacer", "Rehace la ultima accion deshecha"),
                new ShortcutEntry("Ctrl + N", "Nuevo proyecto", "Crea un nuevo proyecto vacio"),
                new ShortcutEntry("F1 / Ctrl+Shift+?", "Atajos de teclado", "Muestra esta guia"),
                new ShortcutEntry("F5", "Refrescar capas", "Recarga todas las capas del proyecto"),
        });
    }

    private JScrollPane buildNavigationPanel() {
        return buildShortcutPanel(new ShortcutEntry[]{
                new ShortcutEntry("Ctrl + +", "Acercar", "Amplia el zoom del mapa"),
                new ShortcutEntry("Ctrl + -", "Alejar", "Reduce el zoom del mapa"),
                new ShortcutEntry("Ctrl + 0", "Zoom a todo", "Ajusta el zoom a todas las capas"),
                new ShortcutEntry("Ctrl + Shift + Z", "Zoom a capa", "Zoom a la capa seleccionada"),
                new ShortcutEntry("Ctrl + G", "Ir a coordenadas", "Abre el buscador de coordenadas"),
                new ShortcutEntry("Ctrl + M", "Medir distancia", "Activa la herramienta de medicion"),
                new ShortcutEntry("Ctrl + Shift + M", "Medir area", "Activa la herramienta de area"),
                new ShortcutEntry("Ctrl + I", "Identificar", "Activa la herramienta de consulta"),
                new ShortcutEntry("Ctrl + Shift + P", "Desplazar mapa", "Activa el modo de desplazamiento"),
        });
    }

    private JScrollPane buildEditPanel() {
        return buildShortcutPanel(new ShortcutEntry[]{
                new ShortcutEntry("Ctrl + Shift + D", "Dibujar punto", "Activa el modo de dibujo de puntos"),
                new ShortcutEntry("Ctrl + Shift + L", "Dibujar linea", "Activa el modo de dibujo de lineas"),
                new ShortcutEntry("Ctrl + Shift + G", "Dibujar poligono", "Activa el modo de dibujo de poligonos"),
                new ShortcutEntry("Ctrl + X", "Cortar", "Corta la entidad seleccionada"),
                new ShortcutEntry("Ctrl + C", "Copiar", "Copia la entidad seleccionada"),
                new ShortcutEntry("Ctrl + V", "Pegar", "Pega la entidad copiada"),
                new ShortcutEntry("Supr", "Eliminar", "Elimina la entidad seleccionada"),
                new ShortcutEntry("Ctrl + Shift + V", "Pegado especial", "Opciones avanzadas de pegado"),
                new ShortcutEntry("Ctrl + Shift + T", "Terminar dibujo", "Finaliza el modo de dibujo actual"),
                new ShortcutEntry("Esc", "Cancelar", "Cancela la operacion actual"),
        });
    }

    private JScrollPane buildAnalysisPanel() {
        return buildShortcutPanel(new ShortcutEntry[]{
                new ShortcutEntry("Ctrl + A", "Consola de analisis", "Abre la consola de analisis unificada"),
                new ShortcutEntry("Ctrl + T", "Tabla de atributos", "Abre la tabla de atributos de la capa seleccionada"),
                new ShortcutEntry("Ctrl + L", "Configurar capa", "Abre las propiedades de la capa seleccionada"),
                new ShortcutEntry("Ctrl + Shift + O", "Mapas online", "Abre el catalogo de mapas online"),
                new ShortcutEntry("Ctrl + Shift + C", "Conversor de coordenadas", "Abre el conversor de CRS"),
                new ShortcutEntry("Ctrl + Shift + R", "Exportar capa", "Exporta la capa seleccionada a otro formato"),
                new ShortcutEntry("Ctrl + Shift + E", "Exportar vista", "Guarda la vista actual del mapa como imagen"),
        });
    }

    // --- Shortcut rendering ---

    private record ShortcutEntry(String keys, String action, String description) {}

    private JScrollPane buildShortcutPanel(ShortcutEntry[] entries) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(2, 0, 2, 0);

        for (int i = 0; i < entries.length; i++) {
            ShortcutEntry entry = entries[i];
            gbc.gridy = i * 2;

            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setBackground(BG_CARD);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            // Key bindings badge
            JLabel keysLabel = new JLabel("  " + entry.keys + "  ");
            keysLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
            keysLabel.setBackground(new Color(230, 242, 255));
            keysLabel.setForeground(new Color(0, 82, 200));
            keysLabel.setOpaque(true);
            keysLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            // Action + description
            JPanel textPanel = new JPanel(new BorderLayout(0, 2));
            textPanel.setOpaque(false);
            JLabel actionLabel = new JLabel(entry.action);
            actionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            actionLabel.setForeground(TEXT_DARK);

            JLabel descLabel = new JLabel(entry.description);
            descLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            descLabel.setForeground(TEXT_MUTED);

            textPanel.add(actionLabel, BorderLayout.NORTH);
            textPanel.add(descLabel, BorderLayout.CENTER);

            row.add(keysLabel, BorderLayout.WEST);
            row.add(textPanel, BorderLayout.CENTER);
            panel.add(row, gbc);
        }

        // Bottom filler
        gbc.gridy = entries.length * 2;
        gbc.weighty = 1;
        panel.add(new JLabel(""), gbc);

        return new JScrollPane(panel);
    }
}
