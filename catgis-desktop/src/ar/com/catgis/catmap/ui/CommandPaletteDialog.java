package ar.com.catgis.catmap.ui;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Task-oriented command palette: type a task, pick a command, run it. Reads from
 * a {@link CommandPaletteIndex}; no command is duplicated in menus.
 */
public final class CommandPaletteDialog {

    private CommandPaletteDialog() {}

    public static void show(Component parent, List<CommandPaletteIndex.Command> commands) {
        CommandPaletteIndex index = new CommandPaletteIndex(commands);
        JDialog dialog = new JDialog(parent instanceof java.awt.Window window ? window : null,
                "Buscar comando", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField search = new JTextField();
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JLabel hint = new JLabel("Escribi una tarea (ej.: exportar pdf, pagina, plantillas).");
        JLabel status = new JLabel(" ");

        final List<CommandPaletteIndex.Command>[] current = new List[]{List.of()};
        Runnable refresh = () -> {
            String query = search.getText();
            List<CommandPaletteIndex.Command> matches = index.search(query);
            current[0] = matches;
            model.clear();
            for (CommandPaletteIndex.Command command : matches) {
                model.addElement(command.label() + "  —  " + command.description());
            }
            if (!model.isEmpty()) {
                list.setSelectedIndex(0);
            }
            status.setText(matches.isEmpty() ? "Sin resultados para: " + query
                    : matches.size() + " comando(s)");
        };
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refresh.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refresh.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refresh.run();
            }
        });
        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_DOWN && !model.isEmpty()) {
                    list.setSelectedIndex(Math.min(model.size() - 1, list.getSelectedIndex() + 1));
                    event.consume();
                } else if (event.getKeyCode() == KeyEvent.VK_UP && !model.isEmpty()) {
                    list.setSelectedIndex(Math.max(0, list.getSelectedIndex() - 1));
                    event.consume();
                } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    runSelected(dialog, current[0], list.getSelectedIndex());
                    event.consume();
                } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dialog.dispose();
                }
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() >= 2) {
                    runSelected(dialog, current[0], list.getSelectedIndex());
                }
            }
        });

        refresh.run();
        JPanel header = new JPanel(new BorderLayout(6, 6));
        header.add(search, BorderLayout.CENTER);
        header.add(hint, BorderLayout.SOUTH);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(560, 300));
        dialog.add(header, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(status, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        SwingUtilities.invokeLater(search::requestFocusInWindow);
        dialog.setVisible(true);
    }

    private static void runSelected(JDialog dialog, List<CommandPaletteIndex.Command> commands,
                                    int index) {
        if (index < 0 || index >= commands.size()) {
            return;
        }
        CommandPaletteIndex.Command command = commands.get(index);
        dialog.dispose();
        if (command.action() != null) {
            command.action().run();
        }
    }
}
