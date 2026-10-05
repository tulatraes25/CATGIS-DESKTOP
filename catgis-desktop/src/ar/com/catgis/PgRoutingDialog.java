package ar.com.catgis;

import ar.com.catgis.analysis.PgRoutingService;
import ar.com.catgis.analysis.PgRoutingService.PgRouteStep;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog for pgRouting: verify connection, list tables, and compute routes.
 */
public class PgRoutingDialog extends JDialog {

    private final JTextField hostField, portField, dbField, userField;
    private final JPasswordField passField;
    private final JComboBox<String> tableCombo;
    private final JTextField sourceField, targetField;
    private final JTextArea resultArea;
    private final JLabel statusLabel;
    private final JButton verifyBtn, routeBtn;
    private List<String> foundTables;

    public PgRoutingDialog(Frame owner) {
        super(owner, "pgRouting", true);
        setSize(450, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 4, 2, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        PostgisConnectionInfo saved = PostgisConnectionStore.loadLastConnection();

        // Connection fields
        g.gridy = 0; g.gridx = 0; form.add(new JLabel("Host:"), g);
        g.gridx = 1; g.weightx = 1;
        hostField = new JTextField(saved != null ? saved.getHost() : "localhost", 15);
        form.add(hostField, g);

        g.gridy = 1; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Puerto:"), g);
        g.gridx = 1; g.weightx = 1;
        portField = new JTextField(saved != null ? String.valueOf(saved.getPort()) : "5432", 6);
        form.add(portField, g);

        g.gridy = 2; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Base:"), g);
        g.gridx = 1; g.weightx = 1;
        dbField = new JTextField(saved != null ? saved.getDatabase() : "gis", 15);
        form.add(dbField, g);

        g.gridy = 3; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Usuario:"), g);
        g.gridx = 1; g.weightx = 1;
        userField = new JTextField(saved != null ? saved.getUser() : "postgres", 15);
        form.add(userField, g);

        g.gridy = 4; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Clave:"), g);
        g.gridx = 1; g.weightx = 1;
        passField = new JPasswordField(15);
        form.add(passField, g);

        // Routing inputs (initially disabled)
        g.gridy = 5; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Tabla:"), g);
        g.gridx = 1; g.weightx = 1;
        tableCombo = new JComboBox<>();
        tableCombo.setEnabled(false);
        form.add(tableCombo, g);

        g.gridy = 6; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Nodo inicio:"), g);
        g.gridx = 1; g.weightx = 1;
        sourceField = new JTextField("1", 6);
        sourceField.setEnabled(false);
        form.add(sourceField, g);

        g.gridy = 7; g.gridx = 0; g.weightx = 0;
        form.add(new JLabel("Nodo destino:"), g);
        g.gridx = 1; g.weightx = 1;
        targetField = new JTextField("2", 6);
        targetField.setEnabled(false);
        form.add(targetField, g);

        add(form, BorderLayout.NORTH);

        resultArea = new JTextArea(8, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusLabel = new JLabel("");
        verifyBtn = new JButton("Verificar");
        verifyBtn.addActionListener(e -> verify());
        routeBtn = new JButton("Calcular ruta");
        routeBtn.setEnabled(false);
        routeBtn.addActionListener(e -> computeRoute());
        bottom.add(statusLabel);
        bottom.add(verifyBtn);
        bottom.add(routeBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private String getUrl() {
        String host = hostField.getText().trim();
        String port = portField.getText().trim();
        String db = dbField.getText().trim();
        return "jdbc:postgresql://" + host + ":" + port + "/" + db;
    }

    private String getUser() { return userField.getText().trim(); }
    private String getPass() { return new String(passField.getPassword()); }

    private void verify() {
        statusLabel.setText("Conectando...");
        resultArea.setText("");
        String url = getUrl();
        String user = getUser();
        String pass = getPass();

        SwingUtilities.invokeLater(() -> {
            try {
                if (!PgRoutingService.isAvailable(url, user, pass)) {
                    resultArea.setText("pgRouting no esta instalado en esta base.\n"
                            + "Ejecute: CREATE EXTENSION pgrouting;");
                    statusLabel.setText("No disponible");
                    return;
                }

                foundTables = PgRoutingService.listRoutingTables(url, user, pass);
                tableCombo.removeAllItems();
                if (foundTables.isEmpty()) {
                    resultArea.setText("pgRouting detectado pero sin tablas de routing.");
                    statusLabel.setText("Sin tablas");
                    tableCombo.setEnabled(false);
                    sourceField.setEnabled(false);
                    targetField.setEnabled(false);
                    routeBtn.setEnabled(false);
                    return;
                }

                for (String t : foundTables) tableCombo.addItem(t);
                tableCombo.setEnabled(true);
                sourceField.setEnabled(true);
                targetField.setEnabled(true);
                routeBtn.setEnabled(true);

                StringBuilder sb = new StringBuilder("pgRouting disponible.\n");
                sb.append("Tablas encontradas:\n");
                for (String t : foundTables) sb.append("  - ").append(t).append("\n");
                sb.append("\nSeleccione tabla, nodo inicio y destino, luego Calcular ruta.");
                resultArea.setText(sb.toString());
                statusLabel.setText(foundTables.size() + " tabla(s)");
            } catch (Exception ex) {
                resultArea.setText("Error: " + ex.getMessage());
                statusLabel.setText("Error");
            }
        });
    }

    private void computeRoute() {
        String table = (String) tableCombo.getSelectedItem();
        if (table == null) return;

        int source, target;
        try {
            source = Integer.parseInt(sourceField.getText().trim());
            target = Integer.parseInt(targetField.getText().trim());
        } catch (NumberFormatException e) {
            resultArea.setText("Nodo inicio/destino debe ser un numero entero.");
            return;
        }

        statusLabel.setText("Calculando...");
        String url = getUrl();
        String user = getUser();
        String pass = getPass();

        SwingUtilities.invokeLater(() -> {
            try {
                List<PgRouteStep> steps = PgRoutingService.dijkstra(
                        url, user, pass, table, source, target);

                if (steps.isEmpty()) {
                    resultArea.setText("Sin ruta entre nodo " + source + " y " + target + ".");
                    statusLabel.setText("Sin ruta");
                    return;
                }

                double totalCost = steps.get(steps.size() - 1).aggCost();
                StringBuilder sb = new StringBuilder();
                sb.append("Ruta encontrada: ").append(steps.size()).append(" segmentos\n");
                sb.append("Costo total: ").append(String.format("%.2f", totalCost)).append("\n\n");
                sb.append("Segmentos:\n");
                for (PgRouteStep s : steps) {
                    sb.append("  ").append(s.seq()).append(". nodo=").append(s.node())
                            .append(" edge=").append(s.edge())
                            .append(" cost=").append(String.format("%.1f", s.cost()))
                            .append(" acum=").append(String.format("%.1f", s.aggCost()));
                    if (s.geomWkt() != null && !s.geomWkt().isEmpty()) {
                        String wkt = s.geomWkt();
                        if (wkt.length() > 50) wkt = wkt.substring(0, 47) + "...";
                        sb.append(" geom=").append(wkt);
                    }
                    sb.append("\n");
                }
                resultArea.setText(sb.toString());
                statusLabel.setText(steps.size() + " segmentos, costo " + String.format("%.1f", totalCost));
            } catch (Exception ex) {
                resultArea.setText("Error: " + ex.getMessage());
                statusLabel.setText("Error");
            }
        });
    }

    public static void open() {
        JFrame owner = CatgisDesktopApp.getMainFrame();
        new PgRoutingDialog(owner).setVisible(true);
    }
}
