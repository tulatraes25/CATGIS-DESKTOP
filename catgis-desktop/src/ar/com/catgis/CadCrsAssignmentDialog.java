package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.io.File;

public class CadCrsAssignmentDialog extends JDialog {

    private final String projectCrs;
    private String selectedCrs;
    private Result result = new Result(false, "", false);
    private boolean selectorRequested;

    private JRadioButton undefinedRadio;
    private JRadioButton useProjectRadio;
    private JRadioButton customRadio;
    private JTextField selectedCrsField;
    private JButton chooseButton;

    public CadCrsAssignmentDialog(Frame owner,
                                  String title,
                                  String subjectLabel,
                                  String currentCode,
                                  String projectCrs) {
        super(owner, title, true);
        this.projectCrs = CRSDefinitions.normalizeCode(projectCrs);
        this.selectedCrs = CRSDefinitions.normalizeCode(currentCode);

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildHeader(subjectLabel), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        applyInitialSelection();
        refreshState();

        setMinimumSize(new Dimension(720, 360));
        pack();
        setLocationRelativeTo(owner);
    }

    public static Result chooseForImport(Component parent, File cadFile, String currentCode) {
        Frame owner = resolveOwner(parent);
        String subject = cadFile != null ? CadLayerSupport.describeCadReference(cadFile) : "Referencia CAD";
        CadCrsAssignmentDialog dialog = new CadCrsAssignmentDialog(
                owner,
                "CRS para referencia CAD",
                subject,
                currentCode,
                resolveProjectCrs()
        );
        dialog.setVisible(true);
        return dialog.result;
    }

    public static Result chooseForLayer(Component parent, Layer layer) {
        Frame owner = resolveOwner(parent);
        StringBuilder subject = new StringBuilder(layer != null ? layer.getName() : "Referencia CAD");
        if (layer != null && layer.getPath() != null && !layer.getPath().isBlank()) {
            subject.append("  [").append(layer.getPath()).append("]");
        }
        CadCrsAssignmentDialog dialog = new CadCrsAssignmentDialog(
                owner,
                "Definir CRS de referencia CAD",
                subject.toString(),
                layer != null ? layer.getSourceCRS() : "",
                resolveProjectCrs()
        );
        dialog.setVisible(true);
        return dialog.result;
    }

    private JPanel buildHeader(String subjectLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        panel.setBackground(new Color(247, 250, 252));

        JLabel title = new JLabel("Asignacion de CRS para CAD");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 19f));
        title.setForeground(new Color(21, 40, 74));

        JLabel subtitle = new JLabel("<html>DWG y DXF no traen un CRS GIS confiable dentro del flujo de CATGIS. "
                + "Elegí como ubicar esta referencia CAD en el proyecto.</html>");
        subtitle.setForeground(new Color(72, 86, 104));

        JLabel subject = new JLabel("<html><b>Referencia:</b> " + escapeHtml(subjectLabel) + "</html>");
        subject.setForeground(new Color(55, 65, 81));

        panel.add(title, BorderLayout.NORTH);
        panel.add(subtitle, BorderLayout.CENTER);
        panel.add(subject, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildCenter() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        JPanel context = new JPanel();
        context.setLayout(new BoxLayout(context, BoxLayout.Y_AXIS));
        context.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        context.setBackground(Color.WHITE);

        JLabel projectLabel = new JLabel("<html><b>CRS del proyecto:</b> "
                + escapeHtml(CadLayerSupport.formatSourceCrsLabel(projectCrs)) + "</html>");
        projectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        projectLabel.setForeground(new Color(31, 41, 55));

        JLabel note = new JLabel("<html>La decision se guarda en la capa y tambien dentro del proyecto <b>.catgis</b>. "
                + "Si el dibujo esta en coordenadas CAD puras, dejalo sin CRS. "
                + "Si ya esta en coordenadas GIS, usá el CRS real.</html>");
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setForeground(new Color(75, 85, 99));

        context.add(projectLabel);
        context.add(Box.createVerticalStrut(8));
        context.add(note);

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        options.setBackground(Color.WHITE);

        undefinedRadio = buildOptionRadio(
                "Sin CRS",
                "Carga el CAD como referencia sin sistema definido. Es la opcion mas segura si no conoces la proyeccion."
        );
        useProjectRadio = buildOptionRadio(
                "Usar CRS del proyecto",
                "Hace que el CAD use exactamente el mismo CRS del proyecto activo."
        );
        useProjectRadio.setEnabled(projectCrs != null && !projectCrs.isBlank());

        customRadio = buildOptionRadio(
                "Elegir CRS manualmente",
                "Usa el selector moderno de CRS para elegir una proyeccion especifica del dibujo."
        );

        ButtonGroup group = new ButtonGroup();
        group.add(undefinedRadio);
        group.add(useProjectRadio);
        group.add(customRadio);

        selectedCrsField = new JTextField();
        selectedCrsField.setEditable(false);
        chooseButton = new JButton("Selector de CRS...");
        chooseButton.addActionListener(e -> requestCrsSelector());

        JPanel customPanel = new JPanel(new BorderLayout(6, 0));
        customPanel.setOpaque(false);
        customPanel.setBorder(BorderFactory.createEmptyBorder(4, 26, 0, 0));
        customPanel.add(selectedCrsField, BorderLayout.CENTER);
        customPanel.add(chooseButton, BorderLayout.EAST);

        undefinedRadio.addActionListener(e -> refreshState());
        useProjectRadio.addActionListener(e -> refreshState());
        customRadio.addActionListener(e -> refreshState());

        options.add(undefinedRadio);
        options.add(Box.createVerticalStrut(6));
        options.add(useProjectRadio);
        options.add(Box.createVerticalStrut(6));
        options.add(customRadio);
        options.add(customPanel);

        panel.add(context, BorderLayout.NORTH);
        panel.add(options, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton accept = new JButton("Aplicar");
        JButton cancel = new JButton("Cancelar");
        accept.addActionListener(e -> applyAndClose());
        cancel.addActionListener(e -> {
            result = new Result(false, "", false);
            dispose();
        });
        buttons.add(accept);
        buttons.add(cancel);
        return buttons;
    }

    private JRadioButton buildOptionRadio(String title, String description) {
        JRadioButton radio = new JRadioButton("<html><b>" + escapeHtml(title) + "</b><br>"
                + "<span style='color:#6b7280'>" + escapeHtml(description) + "</span></html>");
        radio.setOpaque(false);
        radio.setAlignmentX(Component.LEFT_ALIGNMENT);
        return radio;
    }

    private void applyInitialSelection() {
        if (selectedCrs != null && !selectedCrs.isBlank()) {
            if (projectCrs != null && !projectCrs.isBlank() && projectCrs.equalsIgnoreCase(selectedCrs)) {
                useProjectRadio.setSelected(true);
            } else {
                customRadio.setSelected(true);
            }
        } else {
            undefinedRadio.setSelected(true);
        }
    }

    private void refreshState() {
        if (customRadio == null) {
            return;
        }
        boolean custom = customRadio.isSelected();
        selectedCrsField.setEnabled(custom);
        chooseButton.setEnabled(custom);
        selectedCrsField.setText(CadLayerSupport.formatSourceCrsLabel(selectedCrs));
    }

    private void requestCrsSelector() {
        customRadio.setSelected(true);
        selectorRequested = true;
        result = new Result(false, CRSDefinitions.normalizeCode(selectedCrs), true);
        dispose();
    }

    private void applyAndClose() {
        String code = "";
        if (useProjectRadio.isSelected()) {
            code = projectCrs != null ? projectCrs : "";
        } else if (customRadio.isSelected()) {
            if (selectedCrs == null || selectedCrs.isBlank()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Elegí un CRS desde el selector o cambiá la opcion a 'Sin CRS'.",
                        "CRS para CAD",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            code = selectedCrs;
        }

        result = new Result(true, CRSDefinitions.normalizeCode(code), false);
        dispose();
    }

    private static String resolveProjectCrs() {
        return CatgisDesktopApp.currentProject != null
                ? CRSDefinitions.normalizeCode(CatgisDesktopApp.currentProject.getProjectCRS())
                : "";
    }

    private static Frame resolveOwner(Component component) {
        if (component instanceof Frame frame) {
            return frame;
        }
        if (component instanceof Window window) {
            if (window instanceof Frame frame) {
                return frame;
            }
        }
        Window window = component != null ? SwingUtilities.getWindowAncestor(component) : null;
        if (window instanceof Frame frame) {
            return frame;
        }
        Window main = CatgisDesktopApp.getMainFrameSafe();
        return main instanceof Frame frame ? frame : null;
    }

    private static String escapeHtml(String text) {
        if (text == null || text.isBlank()) {
            return "-";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public record Result(boolean approved, String sourceCrs, boolean selectorRequested) {
    }
}
