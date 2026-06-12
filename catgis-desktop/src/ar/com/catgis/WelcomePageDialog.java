package ar.com.catgis;
import ar.com.catgis.core.model.Project;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Professional welcome/start page for CATGIS Desktop.
 * Shows on first launch or when no project is open.
 * Provides quick access to recent projects, new project, and online maps.
 */
public class WelcomePageDialog extends JDialog {

    private static final Color BG_COLOR = new Color(248, 249, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color ACCENT_BLUE = new Color(41, 128, 185);
    private static final Color ACCENT_GREEN = new Color(39, 174, 96);
    private static final Color ACCENT_ORANGE = new Color(230, 126, 34);
    private static final Color TEXT_DARK = new Color(33, 37, 41);
    private static final Color TEXT_MUTED = new Color(108, 117, 125);

    public WelcomePageDialog() {
        setTitle("CATGIS Desktop");
        setSize(780, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(BG_COLOR);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(BG_COLOR);

        // Top: Branding header
        content.add(buildHeader(), BorderLayout.NORTH);

        // Center: main content
        content.add(buildCenter(), BorderLayout.CENTER);

        // Bottom: version/footer
        content.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    public static void open() {
        SwingUtilities.invokeLater(() -> new WelcomePageDialog().setVisible(true));
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(9, 34, 52));
        header.setBorder(new EmptyBorder(28, 32, 22, 32));

        JLabel title = new JLabel("CATGIS Desktop");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Sistema de Informacion Geografica — Analisis Espacial Profesional");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(150, 180, 210));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(title, BorderLayout.CENTER);
        textPanel.add(subtitle, BorderLayout.SOUTH);

        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(BG_COLOR);
        center.setBorder(new EmptyBorder(16, 24, 8, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;

        // Left: Quick actions
        JPanel leftPanel = buildQuickActions();
        center.add(leftPanel, gbc);

        // Right: Recent projects
        gbc.gridx = 1; gbc.insets = new Insets(0, 12, 0, 0);
        JPanel rightPanel = buildRecentProjects();
        center.add(rightPanel, gbc);

        return center;
    }

    private JPanel buildQuickActions() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_COLOR);

        JLabel sectionTitle = new JLabel("Acciones rapidas");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionTitle.setForeground(TEXT_DARK);
        panel.add(sectionTitle, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 6, 0);

        gbc.gridy = 0; actions.add(createActionCard(
                "Nuevo proyecto",
                "Crea un proyecto vacio y comienza a agregar capas",
                ACCENT_BLUE,
                () -> {
                    dispose();
                    CatgisDesktopApp.currentProject = new Project("Nuevo proyecto");
                    CatgisDesktopApp.layersPanel.clearLayers();
                    CatgisDesktopApp.markProjectDirty();
                }
        ), gbc);

        gbc.gridy = 1; actions.add(createActionCard(
                "Abrir proyecto",
                "Carga un archivo .catgis existente",
                ACCENT_GREEN,
                () -> {
                    dispose();
                    LoadProjectAction.loadProject();
                }
        ), gbc);

        gbc.gridy = 2; actions.add(createActionCard(
                "Agregar capa",
                "Carga archivos vectoriales o raster al proyecto",
                ACCENT_ORANGE,
                () -> {
                    dispose();
                    AddLayerAction.openLayer();
                }
        ), gbc);

        gbc.gridy = 3; actions.add(createActionCard(
                "Mapas online",
                "Conecta con WMS, XYZ tiles y otros servicios",
                new Color(142, 68, 173),
                () -> {
                    dispose();
                    OnlineBaseMapDialog.open(CatgisDesktopApp.getMainFrameSafe());
                }
        ), gbc);

        gbc.gridy = 4; actions.add(createActionCard(
                "Consola de analisis",
                "Combina capas online y locales en un analisis",
                new Color(41, 128, 185),
                () -> {
                    dispose();
                    AnalysisConsoleDialog.open();
                }
        ), gbc);

        gbc.gridy = 5; actions.add(createActionCard(
                "Ayuda y tutoriales",
                "Documentacion, atajos de teclado y guias rapidas",
                TEXT_MUTED,
                () -> {
                    dispose();
                    HelpCenterDialog.open();
                }
        ), gbc);

        JScrollPane scroll = new JScrollPane(actions);
        scroll.setBorder(null);
        scroll.setBackground(BG_COLOR);
        scroll.getViewport().setBackground(BG_COLOR);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildRecentProjects() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_COLOR);

        JLabel sectionTitle = new JLabel("Proyectos recientes");
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionTitle.setForeground(TEXT_DARK);
        panel.add(sectionTitle, BorderLayout.NORTH);

        JPanel projects = new JPanel(new GridBagLayout());
        projects.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 6, 0);

        List<RecentProject> recent = loadRecentProjects();
        if (recent.isEmpty()) {
            gbc.gridy = 0;
            JLabel empty = new JLabel("<html><body style='padding:16px; color:#666;'>"
                    + "No hay proyectos recientes.<br>"
                    + "Cree o abra un proyecto para comenzar.</body></html>");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 12));
            projects.add(empty, gbc);
        } else {
            int maxProjects = Math.min(recent.size(), 8);
            for (int i = 0; i < maxProjects; i++) {
                RecentProject rp = recent.get(i);
                gbc.gridy = i;
                projects.add(createProjectCard(rp), gbc);
            }
        }

        JScrollPane scroll = new JScrollPane(projects);
        scroll.setBorder(null);
        scroll.setBackground(BG_COLOR);
        scroll.getViewport().setBackground(BG_COLOR);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        footer.setBackground(new Color(233, 236, 239));

        String version = "CATGIS Desktop v" + AppBranding.getAppVersion();
        JLabel versionLabel = new JLabel(version);
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        versionLabel.setForeground(TEXT_MUTED);
        footer.add(versionLabel);

        JLabel dateLabel = new JLabel(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dateLabel.setForeground(TEXT_MUTED);
        footer.add(dateLabel);

        JButton closeBtn = new JButton("Comenzar");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeBtn.setBackground(ACCENT_BLUE);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE.darker(), 1),
                BorderFactory.createEmptyBorder(4, 20, 4, 20)
        ));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        JButton skipBtn = new JButton("Saltar bienvenida");
        skipBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        skipBtn.setForeground(TEXT_MUTED);
        skipBtn.setBorderPainted(false);
        skipBtn.setContentAreaFilled(false);
        skipBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        skipBtn.addActionListener(e -> {
            AppBranding.setShowWelcomePage(false);
            dispose();
        });
        footer.add(skipBtn);

        return footer;
    }

    // --- Action Card ---

    private JPanel createActionCard(String title, String description, Color accent, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Accent stripe
        JPanel stripe = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 2, 4, 16, 2, 2);
                g2.dispose();
            }
        };
        stripe.setPreferredSize(new Dimension(6, 20));
        stripe.setOpaque(false);

        JPanel textPanel = new JPanel(new BorderLayout(0, 2));
        textPanel.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleLabel.setForeground(TEXT_DARK);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_MUTED);

        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(descLabel, BorderLayout.CENTER);

        card.add(stripe, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 244, 248));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });

        return card;
    }

    // --- Project Card ---

    private JPanel createProjectCard(RecentProject rp) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // File type icon area
        JLabel iconLabel = new JLabel("📁");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));

        JPanel info = new JPanel(new BorderLayout(0, 2));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel(rp.name);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLabel.setForeground(TEXT_DARK);

        JLabel pathLabel = new JLabel(rp.path);
        pathLabel.setFont(new Font("Monospaced", Font.PLAIN, 10));
        pathLabel.setForeground(TEXT_MUTED);

        JLabel dateLabel = new JLabel(rp.lastOpened);
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dateLabel.setForeground(TEXT_MUTED);

        info.add(nameLabel, BorderLayout.NORTH);
        info.add(pathLabel, BorderLayout.CENTER);
        info.add(dateLabel, BorderLayout.SOUTH);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(240, 244, 248));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BG);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    File f = new File(rp.path);
                    if (!f.exists()) {
                        JOptionPane.showMessageDialog(null,
                                "El proyecto ya no existe: " + rp.path,
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    LoadProjectAction.loadProjectFile(f);
                });
            }
        });

        return card;
    }

    // --- Recent projects ---

    private record RecentProject(String name, String path, String lastOpened) {}

    private List<RecentProject> loadRecentProjects() {
        List<RecentProject> projects = new ArrayList<>();

        // Try to read from a simple text file in user's app data
        String appDataDir = System.getenv("APPDATA");
        if (appDataDir == null) return projects;

        File recentFile = new File(appDataDir, "CATGIS/recent_projects.txt");
        if (!recentFile.exists()) return projects;

        try {
            List<String> lines = java.nio.file.Files.readAllLines(recentFile.toPath());
            for (String line : lines) {
                String[] parts = line.split("\\|", 3);
                if (parts.length >= 2) {
                    String name = parts[0].trim();
                    String path = parts[1].trim();
                    String date = parts.length >= 3 ? parts[2].trim() : "";
                    if (!name.isEmpty() && new File(path).exists()) {
                        projects.add(new RecentProject(name, path, date));
                    }
                }
            }
        } catch (Exception ignored) { CatgisLogger.warn("WelcomePageDialog: operation failed", ignored); }

        return projects;
    }

    // --- Static helper to record a project as recent ---

    public static void recordProjectOpen(String name, String filePath) {
        try {
            String appDataDir = System.getenv("APPDATA");
            if (appDataDir == null) return;
            File catgisDir = new File(appDataDir, "CATGIS");
            catgisDir.mkdirs();

            File recentFile = new File(catgisDir, "recent_projects.txt");
            List<String> lines = new ArrayList<>();

            // Read existing
            if (recentFile.exists()) {
                lines = new ArrayList<>(java.nio.file.Files.readAllLines(recentFile.toPath()));
            }

            // Remove duplicate if exists
            lines.removeIf(l -> l.contains("|" + filePath + "|"));

            // Add new entry at top
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            lines.add(0, name + "|" + filePath + "|" + date);

            // Keep only last 20
            if (lines.size() > 20) {
                lines = lines.subList(0, 20);
            }

            java.nio.file.Files.write(recentFile.toPath(), lines);
        } catch (Exception ignored) { CatgisLogger.warn("WelcomePageDialog: operation failed", ignored); }
    }
}
