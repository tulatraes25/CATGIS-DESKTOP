package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

public class OnlineBaseMapDialog extends JDialog {

    private final JComboBox<OnlineRasterSource> sourceCombo;
    private final JTextArea infoArea;

    private OnlineBaseMapDialog(Window owner) {
        super(owner, "Agregar mapa base online", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel sourceLabel = new JLabel("Proveedor:");
        sourceCombo = new JComboBox<>(OnlineMapCatalog.getBaseMaps().toArray(new OnlineRasterSource[0]));
        sourceCombo.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new javax.swing.DefaultListCellRenderer().getListCellRendererComponent(
                        list,
                        value instanceof OnlineRasterSource ? ((OnlineRasterSource) value).getName() : "",
                        index,
                        isSelected,
                        cellHasFocus
                ));
        sourceCombo.addActionListener(e -> refreshInfo());

        infoArea = new JTextArea(7, 42);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        infoArea.setBackground(new java.awt.Color(248, 250, 252));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton addButton = new JButton("Agregar");
        addButton.addActionListener(e -> onAdd());
        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());
        buttons.add(addButton);
        buttons.add(cancelButton);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0;
        content.add(sourceLabel, gc);

        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 1;
        content.add(sourceCombo, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;
        content.add(infoArea, gc);

        add(content, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        DialogKeyboardSupport.install(this, addButton, this::dispose);
        refreshInfo();
        pack();
        setLocationRelativeTo(owner);
    }

    public static void open(Window owner) {
        OnlineBaseMapDialog dialog = new OnlineBaseMapDialog(owner);
        dialog.setVisible(true);
    }

    private void refreshInfo() {
        OnlineRasterSource source = (OnlineRasterSource) sourceCombo.getSelectedItem();
        if (source == null) {
            infoArea.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Nombre: ").append(source.getName()).append("\n");
        sb.append("Proveedor: ").append(source.getProvider()).append("\n");
        sb.append("Servicio: ").append(source.getServiceType()).append("\n");
        sb.append("Zoom soportado: ").append(source.getMinZoom()).append(" - ").append(source.getMaxZoom()).append("\n");
        sb.append("CRS de origen: ").append(source.getSourceCRS()).append("\n");
        sb.append("Atribucion: ").append(source.getAttribution()).append("\n");
        if (!source.getTermsUrl().isBlank()) {
            sb.append("Referencia: ").append(source.getTermsUrl()).append("\n");
        }
        sb.append("\nSe agregara como capa de fondo online para usar debajo de tus capas vectoriales y raster locales.");
        infoArea.setText(sb.toString());
        infoArea.setCaretPosition(0);
    }

    private void onAdd() {
        OnlineRasterSource source = (OnlineRasterSource) sourceCombo.getSelectedItem();
        if (OnlineBaseMapAction.addBaseMap(source)) {
            dispose();
        }
    }
}
