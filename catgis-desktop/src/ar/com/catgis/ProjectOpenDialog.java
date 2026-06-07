package ar.com.catgis;
import ar.com.catgis.core.model.Project;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ProjectOpenDialog extends JDialog {

    private final DefaultListModel<File> entriesModel = new DefaultListModel<>();
    private final JList<File> entriesList = new JList<>(entriesModel);
    private final JTextField currentPathField = new JTextField();
    private final JLabel statusLabel = new JLabel("Selecciona una carpeta o un archivo .catgis.");
    private File currentDirectory;
    private File selectedProject;

    private ProjectOpenDialog(Window owner) {
        super(owner instanceof Frame ? (Frame) owner : null, "Abrir proyecto CATGIS", true);
        buildUi();
        setPreferredSize(new Dimension(720, 520));
        setMinimumSize(new Dimension(620, 440));
        setLocationRelativeTo(owner);
    }

    static File open(Window owner) {
        File initialDirectory = FileChooserSupport.resolveInitialDirectoryHint("project-open");
        File selected = openStandardChooser(owner, initialDirectory);
        if (selected != null) {
            FileChooserSupport.rememberFile("project-open", selected);
            return selected;
        }

        ProjectOpenDialog dialog = new ProjectOpenDialog(owner);
        dialog.openDirectory(initialDirectory != null ? initialDirectory : new File(System.getProperty("user.home", ".")));
        dialog.setVisible(true);
        return dialog.selectedProject;
    }

    static List<File> listProjectEntries(File directory) {
        List<File> entries = new ArrayList<>();
        File resolved = directory != null && directory.isDirectory() ? directory : new File(System.getProperty("user.home", "."));
        File[] children = resolved.listFiles();
        if (children == null) {
            return entries;
        }
        for (File child : children) {
            if (child == null) {
                continue;
            }
            if (child.isDirectory() || child.getName().toLowerCase().endsWith(".catgis")) {
                entries.add(child);
            }
        }
        entries.sort(Comparator
                .comparing((File file) -> !file.isDirectory())
                .thenComparing(file -> file.getName().toLowerCase()));
        return entries;
    }

    private static File openStandardChooser(Window owner, File initialDirectory) {
        try {
            JFileChooser chooser = FileChooserSupport.createChooser("project-open", "Abrir proyecto CATGIS");
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileFilter(new FileNameExtensionFilter("Proyectos CATGIS (*.catgis)", "catgis"));
            if (initialDirectory != null && initialDirectory.isDirectory()) {
                chooser.setCurrentDirectory(initialDirectory);
            }
            int result = chooser.showOpenDialog(owner instanceof Component ? (Component) owner : null);
            if (result != JFileChooser.APPROVE_OPTION) {
                return null;
            }
            File selected = chooser.getSelectedFile();
            if (selected == null) {
                return null;
            }
            if (!selected.getName().toLowerCase().endsWith(".catgis")) {
                return null;
            }
            return selected;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private void buildUi() {
        setLayout(new BorderLayout(10, 10));
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel("Abrir proyecto CATGIS");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
        title.setForeground(new Color(27, 38, 56));

        JLabel subtitle = new JLabel("<html>Selector seguro de proyectos .catgis. Doble clic en una carpeta para entrar o en un proyecto para abrirlo.</html>");
        subtitle.setForeground(new Color(88, 98, 112));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11.5f));

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.SOUTH);

        currentPathField.setEditable(true);
        JPanel pathPanel = new JPanel(new BorderLayout(6, 0));
        pathPanel.setOpaque(false);
        pathPanel.add(currentPathField, BorderLayout.CENTER);
        JButton goButton = new JButton("Ir");
        goButton.addActionListener(e -> navigateToTypedPath());
        pathPanel.add(goButton, BorderLayout.EAST);

        entriesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        entriesList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                File file = value instanceof File ? (File) value : null;
                if (file != null) {
                    label.setText(file.getName());
                    label.setIcon(file.isDirectory() ? AppIcons.openIcon() : AppIcons.projectIcon());
                    label.setToolTipText(file.getAbsolutePath());
                }
                return label;
            }
        });
        entriesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openSelectedEntry();
                }
            }
        });
        entriesList.addListSelectionListener(e -> updateStatusFromSelection());

        JScrollPane scrollPane = new JScrollPane(entriesList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        JButton upButton = new JButton("Subir", AppIcons.upIcon());
        upButton.addActionListener(e -> goUpDirectory());
        JButton homeButton = new JButton("Inicio", AppIcons.openIcon());
        homeButton.addActionListener(e -> openDirectory(new File(System.getProperty("user.home", "."))));
        JButton refreshButton = new JButton("Refrescar", AppIcons.attrRefreshIcon());
        refreshButton.addActionListener(e -> refreshEntries());
        actions.add(upButton);
        actions.add(homeButton);
        actions.add(refreshButton);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.setOpaque(false);
        statusLabel.setForeground(new Color(77, 86, 100));
        bottom.add(statusLabel, BorderLayout.CENTER);

        JPanel confirmButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        confirmButtons.setOpaque(false);
        JButton openButton = new JButton("Abrir", AppIcons.openIcon());
        openButton.addActionListener(e -> openSelectedEntry());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        confirmButtons.add(openButton);
        confirmButtons.add(cancelButton);
        bottom.add(confirmButtons, BorderLayout.EAST);

        content.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(pathPanel, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);
        content.add(center, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(openButton);
        pack();
    }

    private void navigateToTypedPath() {
        String text = currentPathField.getText() != null ? currentPathField.getText().trim() : "";
        if (text.isBlank()) {
            return;
        }
        File target = new File(text);
        if (target.isDirectory()) {
            openDirectory(target);
            return;
        }
        if (target.isFile() && target.getName().toLowerCase().endsWith(".catgis")) {
            selectedProject = target;
            FileChooserSupport.rememberFile("project-open", target);
            dispose();
            return;
        }
        JOptionPane.showMessageDialog(this, "Ingresa una carpeta valida o un archivo .catgis existente.");
    }

    private void goUpDirectory() {
        if (currentDirectory == null) {
            return;
        }
        File parent = currentDirectory.getParentFile();
        if (parent != null && parent.isDirectory()) {
            openDirectory(parent);
        }
    }

    private void openDirectory(File directory) {
        currentDirectory = directory != null && directory.isDirectory() ? directory : new File(System.getProperty("user.home", "."));
        currentPathField.setText(currentDirectory.getAbsolutePath());
        refreshEntries();
    }

    private void refreshEntries() {
        entriesModel.clear();
        List<File> entries = listProjectEntries(currentDirectory);
        for (File entry : entries) {
            entriesModel.addElement(entry);
        }
        if (!entries.isEmpty()) {
            entriesList.setSelectedIndex(0);
        } else {
            statusLabel.setText("No hay carpetas ni proyectos .catgis en esta ubicacion.");
        }
    }

    private void updateStatusFromSelection() {
        File selected = entriesList.getSelectedValue();
        if (selected == null) {
            statusLabel.setText("Selecciona una carpeta o un archivo .catgis.");
            return;
        }
        statusLabel.setText(selected.isDirectory()
                ? "Carpeta: " + selected.getAbsolutePath()
                : "Proyecto listo para abrir: " + selected.getAbsolutePath());
    }

    private void openSelectedEntry() {
        File selected = entriesList.getSelectedValue();
        if (selected == null) {
            navigateToTypedPath();
            return;
        }
        if (selected.isDirectory()) {
            openDirectory(selected);
            return;
        }
        if (!selected.getName().toLowerCase().endsWith(".catgis")) {
            JOptionPane.showMessageDialog(this, "Selecciona un archivo .catgis valido.");
            return;
        }
        selectedProject = selected;
        FileChooserSupport.rememberFile("project-open", selected);
        dispose();
    }
}
