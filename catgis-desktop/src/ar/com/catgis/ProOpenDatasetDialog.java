package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ProOpenDatasetDialog extends JDialog {

    private final JTextField targetField;
    private final JTextArea summaryArea;
    private final DefaultTableModel tableModel;
    private final JTable entryTable;
    private final JButton inspectButton;
    private final JButton openButton;
    private final JButton cancelButton;
    private File selectedTarget;
    private List<ProDatasetOpenService.Entry> currentEntries = List.of();

    private ProOpenDatasetDialog(Window owner) {
        super(owner, "Abrir dataset Pro", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        JLabel targetLabel = new JLabel("Fuente Pro:");
        targetField = new JTextField();
        targetField.setEditable(false);

        JButton browseButton = new JButton("Buscar...");
        browseButton.addActionListener(e -> chooseTarget());

        inspectButton = new JButton("Inspeccionar");
        inspectButton.addActionListener(e -> inspectSelection());

        JLabel hintLabel = new JLabel("<html><span style='color:#555555'>"
                + "MVP Pro: abre rasters preparados, materializa variables NetCDF/HDF y deja visible su preset tematico y clasificacion metodologica."
                + "</span></html>");

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        topPanel.add(targetLabel, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        topPanel.add(targetField, gc);

        gc.gridx = 2;
        gc.gridy = 0;
        gc.weightx = 0;
        topPanel.add(browseButton, gc);

        gc.gridx = 3;
        gc.gridy = 0;
        topPanel.add(inspectButton, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 4;
        gc.weightx = 1;
        topPanel.add(hintLabel, gc);

        summaryArea = new JTextArea(4, 20);
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Resumen"),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        summaryArea.setText("Selecciona una carpeta, un raster preparado, un sidecar Pro o un contenedor NetCDF/HDF para inspeccion.");

        tableModel = new DefaultTableModel(new Object[]{"Variable", "Descripcion", "Preset", "Metodologia", "Tiempo", "Estado", "Fuente"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        entryTable = new JTable(tableModel);
        entryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        entryTable.setAutoCreateRowSorter(true);

        JScrollPane tableScroll = new JScrollPane(entryTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Variables Pro detectadas"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        openButton = new JButton("Abrir seleccionadas");
        openButton.addActionListener(e -> openSelectedEntries());
        openButton.setEnabled(false);
        cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(openButton);
        buttonPanel.add(cancelButton);

        content.add(topPanel, BorderLayout.NORTH);
        content.add(summaryArea, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(content, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, openButton, this::dispose);
        setSize(980, 620);
        setLocationRelativeTo(owner);
    }

    static void open(Window owner) {
        ProOpenDatasetDialog dialog = new ProOpenDatasetDialog(owner);
        dialog.setVisible(true);
    }

    private void chooseTarget() {
        JFileChooser chooser = FileChooserSupport.createChooser("open-pro-dataset", "Abrir dataset Pro");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                "Fuentes Pro (*.tif, *.tiff, *.img, *.asc, *.nc, *.nc4, *.hdf, *.h5, *.json)",
                "tif", "tiff", "img", "asc", "nc", "nc4", "hdf", "h5", "json"
        ));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        selectedTarget = chooser.getSelectedFile();
        if (selectedTarget == null) {
            return;
        }
        FileChooserSupport.rememberSelection("open-pro-dataset", chooser);
        targetField.setText(selectedTarget.getAbsolutePath());
        inspectSelection();
    }

    private void inspectSelection() {
        if (selectedTarget == null) {
            String raw = targetField.getText() != null ? targetField.getText().trim() : "";
            if (!raw.isBlank()) {
                selectedTarget = new File(raw);
            }
        }
        if (selectedTarget == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero selecciona una fuente Pro.",
                    "Abrir dataset Pro",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        ProDatasetOpenService.Catalog catalog = ProDatasetOpenService.inspect(selectedTarget);
        currentEntries = catalog.entries();
        rebuildTable(currentEntries);
        summaryArea.setText(catalog.summaryText());
        openButton.setEnabled(currentEntries.stream().anyMatch(ProDatasetOpenService.Entry::openable));

        if (entryTable.getRowCount() > 0) {
            entryTable.setRowSelectionInterval(0, 0);
        }
    }

    private void rebuildTable(List<ProDatasetOpenService.Entry> entries) {
        tableModel.setRowCount(0);
        for (ProDatasetOpenService.Entry entry : entries) {
            tableModel.addRow(new Object[]{
                    entry.variableLabel(),
                    entry.descriptionLabel(),
                    entry.presetLabel(),
                    entry.methodologyLabel(),
                    entry.acquisitionLabel(),
                    entry.statusLabel(),
                    entry.sourceLabel()
            });
        }
    }

    private void openSelectedEntries() {
        int[] selectedRows = entryTable.getSelectedRows();
        if (selectedRows == null || selectedRows.length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona al menos una variable Pro para abrir.",
                    "Abrir dataset Pro",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        List<ProDatasetOpenService.Entry> selectedEntries = new ArrayList<>();
        for (int selectedRow : selectedRows) {
            int modelRow = entryTable.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0 && modelRow < currentEntries.size()) {
                selectedEntries.add(currentEntries.get(modelRow));
            }
        }

        List<String> skipped = new ArrayList<>();
        List<ProDatasetOpenService.Entry> openableEntries = new ArrayList<>();
        for (ProDatasetOpenService.Entry entry : selectedEntries) {
            if (entry == null) {
                continue;
            }
            if (!entry.openable()) {
                skipped.add(entry.variableLabel());
                continue;
            }
            openableEntries.add(entry);
        }

        if (openableEntries.isEmpty()) {
            boolean gdalBlocked = selectedEntries.stream().anyMatch(ProDatasetOpenService.Entry::blockedByGdal);
            JOptionPane.showMessageDialog(
                    this,
                    skipped.isEmpty()
                            ? "La seleccion actual no contiene variables Pro abribles en este MVP."
                            : "No se pudieron abrir estas variables en el MVP actual:\n- " + String.join("\n- ", skipped)
                            + "\n\nQuedaron catalogadas, pero requieren materializacion desde backend Pro."
                            + (gdalBlocked
                            ? "\n\nDetalle: en esta instalacion no se detecto gdal_translate."
                            : ""),
                    "Abrir dataset Pro",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        setBusy(true);
        ProOpenBatchCoordinator.CancellationToken cancellationToken = new ProOpenBatchCoordinator.CancellationToken();
        BatchProgressDialog progressDialog = createBatchProgressDialog(openableEntries.size(), () -> {
            cancellationToken.requestCancel();
            if (CatgisDesktopApp.statusBar != null) {
                CatgisDesktopApp.statusBar.setMessage("Aviso: cancelacion solicitada para el job Pro.");
            }
        });
        SwingWorker<ProOpenBatchCoordinator.BatchResult, ProOpenBatchCoordinator.ProgressSnapshot> worker = new SwingWorker<>() {
            @Override
            protected ProOpenBatchCoordinator.BatchResult doInBackground() {
                return ProOpenBatchCoordinator.prepareEntries(
                        openableEntries,
                        List.copyOf(skipped),
                        snapshot -> publish(snapshot),
                        cancellationToken
                );
            }

            @Override
            protected void process(List<ProOpenBatchCoordinator.ProgressSnapshot> chunks) {
                if (chunks == null || chunks.isEmpty()) {
                    return;
                }
                ProOpenBatchCoordinator.ProgressSnapshot snapshot = chunks.get(chunks.size() - 1);
                progressDialog.update(snapshot);
                if (CatgisDesktopApp.statusBar != null) {
                    CatgisDesktopApp.statusBar.setMessage(
                            snapshot.cancelRequested()
                                    ? "Aviso: " + snapshot.message()
                                    : snapshot.message()
                    );
                }
            }

            @Override
            protected void done() {
                progressDialog.close();
                setBusy(false);
                try {
                    ProOpenBatchCoordinator.BatchResult result = get();
                    ProOpenBatchCoordinator.BatchResult committed = commitPreparedBatch(result);
                    showBatchSummary(committed);
                    if (!committed.canceled()
                            && committed.failures().isEmpty()
                            && committed.skipped().isEmpty()
                            && !committed.prepared().isEmpty()) {
                        dispose();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(
                            ProOpenDatasetDialog.this,
                            "No se pudo completar la apertura Pro.\n\n"
                                    + (cause.getMessage() != null && !cause.getMessage().isBlank()
                                    ? cause.getMessage()
                                    : cause.getClass().getSimpleName()),
                            "Abrir dataset Pro",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
        progressDialog.open();
    }

    private ProOpenBatchCoordinator.BatchResult commitPreparedBatch(ProOpenBatchCoordinator.BatchResult result) {
        List<OpenFileAction.PreparedProRaster> committed = new ArrayList<>();
        List<ProOpenBatchCoordinator.BatchFailure> failures = new ArrayList<>(result.failures());
        for (OpenFileAction.PreparedProRaster prepared : result.prepared()) {
            try {
                OpenFileAction.commitPreparedProRaster(prepared);
                committed.add(prepared);
            } catch (Exception ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                String reason = cause.getMessage() != null && !cause.getMessage().isBlank()
                        ? cause.getMessage()
                        : cause.getClass().getSimpleName();
                failures.add(new ProOpenBatchCoordinator.BatchFailure(prepared.entry().variableLabel(), reason));
            }
        }
        if (CatgisDesktopApp.statusBar != null) {
            CatgisDesktopApp.statusBar.setMessage(
                    result.canceled()
                            ? "Aviso: job Pro cancelado. Variables abiertas: " + committed.size()
                            : committed.isEmpty()
                            ? "Aviso: no quedaron variables Pro abiertas."
                            : committed.size() == 1
                            ? "Variable Pro abierta correctamente."
                            : committed.size() + " variables Pro abiertas correctamente."
            );
        }
        return new ProOpenBatchCoordinator.BatchResult(List.copyOf(committed), List.copyOf(failures), result.skipped(), result.canceled());
    }

    private void showBatchSummary(ProOpenBatchCoordinator.BatchResult result) {
        int opened = result.prepared().size();
        boolean hasIssues = result.canceled() || !result.failures().isEmpty() || !result.skipped().isEmpty();
        if (opened > 0 && !hasIssues) {
            return;
        }
        StringBuilder msg = new StringBuilder();
        msg.append("Variables Pro abiertas: ").append(opened);
        if (result.canceled()) {
            msg.append("\n\nEstado del job: cancelado por el usuario.");
        }
        if (!result.skipped().isEmpty()) {
            msg.append("\n\nSin backend MVP para abrir:");
            for (String skippedVariable : result.skipped()) {
                msg.append("\n- ").append(skippedVariable);
            }
        }
        if (!result.failures().isEmpty()) {
            msg.append("\n\nCon error:");
            for (ProOpenBatchCoordinator.BatchFailure failure : result.failures()) {
                msg.append("\n- ").append(failure.label()).append(": ").append(failure.reason());
            }
        }
        JOptionPane.showMessageDialog(
                this,
                msg.toString(),
                "Abrir dataset Pro",
                opened > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );
    }

    private void setBusy(boolean busy) {
        inspectButton.setEnabled(!busy);
        openButton.setEnabled(!busy && currentEntries.stream().anyMatch(ProDatasetOpenService.Entry::openable));
        cancelButton.setEnabled(!busy);
        entryTable.setEnabled(!busy);
        targetField.setEnabled(!busy);
    }

    private BatchProgressDialog createBatchProgressDialog(int total, Runnable cancelAction) {
        return new BatchProgressDialog(this, total, cancelAction);
    }

    private static final class BatchProgressDialog {
        private final JDialog dialog;
        private final JLabel messageLabel;
        private final JProgressBar progressBar;
        private final JButton cancelJobButton;

        private BatchProgressDialog(Window owner, int total, Runnable cancelAction) {
            dialog = new JDialog(owner, "Procesando variables Pro", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            messageLabel = new JLabel("Preparando variables Pro...");
            progressBar = new JProgressBar(0, Math.max(1, total));
            progressBar.setStringPainted(true);
            progressBar.setValue(0);
            progressBar.setString("0 / " + Math.max(1, total));

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            cancelJobButton = new JButton("Cancelar job");
            cancelJobButton.addActionListener(e -> {
                cancelJobButton.setEnabled(false);
                messageLabel.setText("Cancelando job Pro...");
                if (cancelAction != null) {
                    cancelAction.run();
                }
            });
            buttonPanel.add(cancelJobButton);

            panel.add(messageLabel, BorderLayout.NORTH);
            panel.add(progressBar, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setResizable(false);
            dialog.setLocationRelativeTo(owner);
        }

        private void update(ProOpenBatchCoordinator.ProgressSnapshot progress) {
            if (progress == null) {
                return;
            }
            messageLabel.setText(progress.message());
            progressBar.setMaximum(Math.max(1, progress.total()));
            progressBar.setValue(Math.max(0, Math.min(progress.completed(), progress.total())));
            progressBar.setString(progress.completed() + " / " + progress.total());
            if (progress.cancelRequested()) {
                cancelJobButton.setEnabled(false);
            }
        }

        private void markCancelRequested() {
            cancelJobButton.setEnabled(false);
            messageLabel.setText("Cancelando job Pro...");
        }

        private void open() {
            dialog.setVisible(true);
        }

        private void close() {
            dialog.dispose();
        }
    }
}
