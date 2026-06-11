package ar.com.catgis;

import ar.com.catgis.scripting.ScriptEngine;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Basic script console for CATGIS.
 * Allows users to write and execute Python scripts interactively.
 */
public class ScriptConsoleDialog extends JDialog {

    private JTextArea codeArea;
    private JTextArea outputArea;
    private JButton runButton;
    private JButton loadButton;
    private JButton saveButton;
    private JButton clearButton;
    private JFileChooser fileChooser;

    public ScriptConsoleDialog(Frame owner) {
        super(owner, "CATGIS Script Console", false);
        setSize(800, 600);
        setLocationRelativeTo(owner);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        runButton = new JButton("Run");
        loadButton = new JButton("Load");
        saveButton = new JButton("Save");
        clearButton = new JButton("Clear Output");
        toolbar.add(runButton);
        toolbar.add(loadButton);
        toolbar.add(saveButton);
        toolbar.add(clearButton);
        add(toolbar, BorderLayout.NORTH);

        // Split pane: code on top, output on bottom
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);

        // Code area
        codeArea = new JTextArea();
        codeArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        codeArea.setTabSize(4);
        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createTitledBorder("Python Code"));
        splitPane.setTopComponent(codeScroll);

        // Output area
        outputArea = new JTextArea();
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(30, 30, 30));
        outputArea.setForeground(new Color(200, 200, 200));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Output"));
        splitPane.setBottomComponent(outputScroll);

        add(splitPane, BorderLayout.CENTER);

        // Status bar
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        add(statusLabel, BorderLayout.SOUTH);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Python files (*.py)", "py"));

        // Actions
        runButton.addActionListener(e -> {
            runButton.setEnabled(false);
            statusLabel.setText("Running...");
            new Thread(() -> {
                String code = codeArea.getText();
                ScriptEngine.ScriptResult result = ScriptEngine.executeCode(code);
                SwingUtilities.invokeLater(() -> {
                    outputArea.setText(result.output());
                    if (!result.error().isEmpty()) {
                        outputArea.append("\n--- ERROR ---\n" + result.error());
                    }
                    outputArea.append("\n--- " + (result.success() ? "SUCCESS" : "FAILED") + " ---");
                    runButton.setEnabled(true);
                    statusLabel.setText(result.success() ? "Completed" : "Failed");
                });
            }).start();
        });

        loadButton.addActionListener(e -> {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String content = new String(java.nio.file.Files.readAllBytes(
                            fileChooser.getSelectedFile().toPath()));
                    codeArea.setText(content);
                    statusLabel.setText("Loaded: " + fileChooser.getSelectedFile().getName());
                } catch (Exception ex) {
                    outputArea.setText("Error loading file: " + ex.getMessage());
                }
            }
        });

        saveButton.addActionListener(e -> {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().endsWith(".py")) {
                        file = new File(file.getAbsolutePath() + ".py");
                    }
                    java.nio.file.Files.writeString(file.toPath(), codeArea.getText());
                    statusLabel.setText("Saved: " + file.getName());
                } catch (Exception ex) {
                    outputArea.setText("Error saving file: " + ex.getMessage());
                }
            }
        });

        clearButton.addActionListener(e -> outputArea.setText(""));
    }

    /**
     * Show the script console dialog.
     */
    public static void showConsole(Frame owner) {
        ScriptConsoleDialog dialog = new ScriptConsoleDialog(owner);
        dialog.setVisible(true);
    }
}
