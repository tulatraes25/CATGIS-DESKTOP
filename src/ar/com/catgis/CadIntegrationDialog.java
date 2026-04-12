package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.util.List;

public class CadIntegrationDialog extends JDialog {

    private final JTextField modeField;
    private final JTextField customPathField;
    private final JTextField activeConverterField;
    private final DefaultListModel<String> convertersModel;

    public CadIntegrationDialog(Frame owner) {
        super(owner, "Integracion DWG / CAD", true);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        header.setBackground(new Color(247, 250, 252));
        JLabel title = new JLabel("Diagnostico e integracion CAD");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JLabel subtitle = new JLabel("<html>CATGIS no incorpora hoy un lector DWG in-process propio. "
                + "La experiencia profesional actual se resuelve con integracion local de convertidor oficial + carga DXF interna.</html>");
        subtitle.setForeground(new Color(75, 85, 99));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        modeField = readOnlyField();
        customPathField = readOnlyField();
        activeConverterField = readOnlyField();

        JPanel form = new JPanel(new java.awt.GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.insets = new java.awt.Insets(4, 0, 4, 8);
        gc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gc.anchor = java.awt.GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(new JLabel("Modo DWG actual"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(modeField, gc);
        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 0;
        form.add(new JLabel("Ruta manual configurada"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(customPathField, gc);
        gc.gridx = 0;
        gc.gridy++;
        gc.weightx = 0;
        form.add(new JLabel("Convertidor activo"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        form.add(activeConverterField, gc);

        convertersModel = new DefaultListModel<>();
        JList<String> convertersList = new JList<>(convertersModel);
        JScrollPane listScroll = new JScrollPane(convertersList);
        listScroll.setBorder(BorderFactory.createTitledBorder("Convertidores detectados"));

        JTextArea note = new JTextArea();
        note.setEditable(false);
        note.setWrapStyleWord(true);
        note.setLineWrap(true);
        note.setOpaque(false);
        note.setText("CATGIS intenta usar primero una ruta manual configurada y luego autodeteccion. "
                + "Si encuentra ODA File Converter, puede resolver DWG de forma automatica hacia DXF para carga de referencia.\n\n"
                + "Esto mantiene el flujo integrado sin prometer soporte nativo falso.");

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.add(form, BorderLayout.NORTH);
        center.add(listScroll, BorderLayout.CENTER);
        center.add(note, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton browse = new JButton("Elegir convertidor...");
        browse.addActionListener(e -> chooseConverter());
        JButton auto = new JButton("Usar autodeteccion");
        auto.addActionListener(e -> {
            CadIntegrationSettings.setCustomConverterPath("");
            refreshState();
        });
        JButton validate = new JButton("Validar");
        validate.addActionListener(e -> refreshState());
        JButton close = new JButton("Cerrar");
        close.addActionListener(e -> dispose());
        buttons.add(browse);
        buttons.add(auto);
        buttons.add(validate);
        buttons.add(close);
        add(buttons, BorderLayout.SOUTH);

        refreshState();
        setMinimumSize(new java.awt.Dimension(760, 460));
        pack();
        setLocationRelativeTo(owner);
    }

    public static void open() {
        Frame owner = CatgisDesktopApp.getMainFrame();
        new CadIntegrationDialog(owner).setVisible(true);
    }

    private void chooseConverter() {
        JFileChooser chooser = FileChooserSupport.createChooser("cad-converter", "Seleccionar convertidor CAD");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Ejecutables CAD (*.exe)", "exe"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file == null || !file.exists()) {
            return;
        }
        CadIntegrationSettings.setCustomConverterPath(file.getAbsolutePath());
        FileChooserSupport.rememberSelection("cad-converter", chooser);
        refreshState();
    }

    private void refreshState() {
        String customPath = CadIntegrationSettings.getCustomConverterPath();
        File active = DwgImportSupport.detectPreferredCadConverter();
        List<File> detected = DwgImportSupport.listAvailableCadConverters();

        modeField.setText(active != null
                ? "Integracion CAD automatica disponible"
                : "Solo flujo CAD asistido sin convertidor activo");
        customPathField.setText(customPath == null || customPath.isBlank() ? "(sin ruta manual)" : customPath);
        activeConverterField.setText(active != null ? active.getAbsolutePath() : "(no detectado)");

        convertersModel.clear();
        if (detected.isEmpty()) {
            convertersModel.addElement("(no se detectaron convertidores)");
        } else {
            for (File file : detected) {
                convertersModel.addElement(file.getAbsolutePath());
            }
        }
    }

    private JTextField readOnlyField() {
        JTextField field = new JTextField();
        field.setEditable(false);
        return field;
    }
}
