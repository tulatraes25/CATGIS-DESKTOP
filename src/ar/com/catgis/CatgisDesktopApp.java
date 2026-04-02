package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CatgisDesktopApp extends JFrame {

    public static MapPanel mapPanel;
    public static LayersPanel layersPanel;
    public static StatusBar statusBar;
    public static Project currentProject;
    public static FloatingVectorEditToolbar floatingVectorEditToolbar;
    private static JLabel sidebarTitleLabel;
    private static JLabel sidebarSubtitleLabel;
    private static JLabel sidebarBadgeLabel;
    private static JLabel mapStatusHintLabel;

    public CatgisDesktopApp() {
        setTitle("CATGIS Desktop");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1360, 860);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        mapPanel = new MapPanel();
        layersPanel = new LayersPanel();
        statusBar = new StatusBar();
        floatingVectorEditToolbar = new FloatingVectorEditToolbar();

        setJMenuBar(new MainMenuBar());
        add(buildTopContainer(), BorderLayout.NORTH);
        add(buildCenterContainer(), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        initializeProjectAtStartup();
        installWindowCloseHandler();
    }

    private JPanel buildTopContainer() {
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(true);
        topContainer.setBackground(new Color(245, 247, 250));
        topContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 223, 230)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        topContainer.add(new MainToolBar(), BorderLayout.CENTER);
        return topContainer;
    }

    private JPanel buildCenterContainer() {
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setOpaque(true);
        centerContainer.setBackground(new Color(240, 243, 247));
        centerContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftSidebar(),
                buildMapContainer()
        );
        splitPane.setDividerLocation(320);
        splitPane.setResizeWeight(0.0);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setBackground(new Color(240, 243, 247));
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);

        centerContainer.add(splitPane, BorderLayout.CENTER);
        return centerContainer;
    }

    private JPanel buildLeftSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 8));
        sidebar.setOpaque(true);
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        sidebar.setPreferredSize(new Dimension(320, 100));

        sidebar.add(buildSidebarHeader(), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(layersPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setOpaque(false);

        sidebar.add(scrollPane, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildSidebarHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 2));

        sidebarTitleLabel = new JLabel("Gestor de proyecto");
        sidebarTitleLabel.setFont(sidebarTitleLabel.getFont().deriveFont(Font.BOLD, 17f));
        sidebarTitleLabel.setForeground(new Color(28, 37, 54));
        sidebarTitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Gestión visual de capas y accesos rápidos");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11.5f));
        subtitle.setForeground(new Color(106, 116, 130));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        JPanel badgePanel = new JPanel(new BorderLayout());
        badgePanel.setOpaque(false);
        badgePanel.setAlignmentX(LEFT_ALIGNMENT);
        badgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel badge = new JLabel("CATGIS", SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(new Color(33, 120, 210));
        badge.setForeground(Color.WHITE);
        badge.setFont(badge.getFont().deriveFont(Font.BOLD, 10.5f));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        sidebarBadgeLabel = badge;
        sidebarSubtitleLabel = subtitle;

        JPanel badgeWrap = new JPanel(new BorderLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge, BorderLayout.WEST);
        badgePanel.add(badgeWrap, BorderLayout.WEST);

        header.add(sidebarTitleLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(sidebarSubtitleLabel);
        header.add(Box.createVerticalStrut(8));
        header.add(badgePanel);

        return header;
    }

    private JPanel buildMapContainer() {
        JPanel mapContainer = new JPanel(new BorderLayout(0, 8));
        mapContainer.setOpaque(true);
        mapContainer.setBackground(Color.WHITE);
        mapContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        mapContainer.add(buildMapHeader(), BorderLayout.NORTH);
        mapContainer.add(buildMapWorkspace(), BorderLayout.CENTER);
        return mapContainer;
    }

    private JLayeredPane buildMapWorkspace() {
        JLayeredPane layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                if (mapPanel != null) {
                    mapPanel.setBounds(0, 0, getWidth(), getHeight());
                }

                if (floatingVectorEditToolbar != null) {
                    Dimension pref = floatingVectorEditToolbar.getPreferredSize();
                    int width = pref.width;
                    int height = pref.height;
                    int x = floatingVectorEditToolbar.getX() > 0 ? floatingVectorEditToolbar.getX() : 16;
                    int y = floatingVectorEditToolbar.getY() > 0 ? floatingVectorEditToolbar.getY() : 16;

                    int maxX = Math.max(12, getWidth() - width - 12);
                    int maxY = Math.max(12, getHeight() - height - 12);

                    x = Math.max(12, Math.min(x, maxX));
                    y = Math.max(12, Math.min(y, maxY));

                    floatingVectorEditToolbar.setBounds(x, y, width, height);
                }
            }
        };

        layeredPane.setOpaque(true);
        layeredPane.setBackground(Color.WHITE);
        layeredPane.add(mapPanel, Integer.valueOf(JLayeredPane.DEFAULT_LAYER));
        layeredPane.add(floatingVectorEditToolbar, Integer.valueOf(JLayeredPane.PALETTE_LAYER));
        return layeredPane;
    }

    private JPanel buildMapHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 2));

        JLabel title = new JLabel("Vista de mapa");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
        title.setForeground(new Color(28, 37, 54));

        JLabel subtitle = new JLabel("Exploración, edición y análisis visual");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11.5f));
        subtitle.setForeground(new Color(106, 116, 130));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(subtitle);

        JLabel statusHint = new JLabel("Proyecto activo");
        statusHint.setOpaque(true);
        statusHint.setBackground(new Color(236, 245, 255));
        statusHint.setForeground(new Color(33, 120, 210));
        statusHint.setFont(statusHint.getFont().deriveFont(Font.BOLD, 10.5f));
        statusHint.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        mapStatusHintLabel = statusHint;

        header.add(left, BorderLayout.WEST);
        header.add(statusHint, BorderLayout.EAST);
        return header;
    }

    private void initializeProjectAtStartup() {
        if (currentProject == null) {
            currentProject = new Project("Proyecto actual");
        }

        SwingUtilities.invokeLater(() -> {
            promptInitialProjectCRS();
            updateWindowTitle();
        });
    }

    private void promptInitialProjectCRS() {
        String currentCode = currentProject.getProjectCRS();
        if (currentCode == null || currentCode.isBlank()) {
            currentProject.setProjectCRS("EPSG:4326");
            currentCode = "EPSG:4326";
        }

        final String fallbackCode = currentCode;

        CRSSelectorDialog.open(
                "Seleccione el CRS inicial del proyecto",
                currentCode,
                code -> {
                    String finalCode = (code != null && !code.isBlank()) ? code : fallbackCode;
                    currentProject.setProjectCRS(finalCode);

                    if (statusBar != null) {
                        statusBar.setMessage("CRS inicial del proyecto: " + CRSDefinitions.getLabelForCode(finalCode));
                    }

                    if (mapPanel != null) {
                        mapPanel.resetView();
                        mapPanel.repaint();
                    }

                    syncFloatingVectorEditToolbar();
                    updateWindowTitle();
                }
        );
    }

    public static void updateWindowTitle() {
        JFrame frame = getMainFrame();
        if (frame == null || currentProject == null) {
            return;
        }

        String projectName = currentProject.getName() != null ? currentProject.getName() : "Proyecto actual";
        String crs = currentProject.getProjectCRS() != null && !currentProject.getProjectCRS().isBlank()
                ? currentProject.getProjectCRS()
                : "EPSG:4326";
        String dirtyPrefix = currentProject.isModified() ? "* " : "";

        frame.setTitle("CATGIS Desktop - " + dirtyPrefix + projectName + " - " + CRSDefinitions.getLabelForCode(crs));
        refreshProjectHeader();
    }

    public static void refreshProjectHeader() {
        if (currentProject == null) {
            return;
        }

        String projectName = currentProject.getName() != null && !currentProject.getName().isBlank()
                ? currentProject.getName()
                : "Proyecto actual";
        String crs = currentProject.getProjectCRS() != null && !currentProject.getProjectCRS().isBlank()
                ? currentProject.getProjectCRS()
                : "EPSG:4326";
        int layerCount = currentProject.getLayers() != null ? currentProject.getLayers().size() : 0;

        if (sidebarTitleLabel != null) {
            sidebarTitleLabel.setText("Gestor de proyecto");
        }
        if (sidebarSubtitleLabel != null) {
            sidebarSubtitleLabel.setText(projectName + " | " + CRSDefinitions.getLabelForCode(crs));
        }
        if (sidebarBadgeLabel != null) {
            sidebarBadgeLabel.setText(currentProject.isModified() ? "CATGIS *" : "CATGIS");
        }
        if (mapStatusHintLabel != null) {
            mapStatusHintLabel.setText(projectName + " | " + layerCount + " capas");
        }
    }

    public static void markProjectDirty() {
        if (currentProject == null) {
            return;
        }
        currentProject.setModified(true);
        updateWindowTitle();
    }

    public static void markProjectClean() {
        if (currentProject == null) {
            return;
        }
        currentProject.setModified(false);
        updateWindowTitle();
    }

    public static boolean hasUnsavedProjectChanges() {
        return currentProject != null && currentProject.isModified();
    }

    public static void renameCurrentProject() {
        if (currentProject == null) {
            currentProject = new Project("Proyecto actual");
        }

        String currentName = currentProject.getName() != null ? currentProject.getName() : "Proyecto actual";
        String newName = JOptionPane.showInputDialog(getMainFrame(), "Nombre del proyecto:", currentName);
        if (newName == null) {
            return;
        }

        String trimmed = newName.trim();
        if (trimmed.isEmpty()) {
            JOptionPane.showMessageDialog(getMainFrame(), "El nombre del proyecto no puede quedar vacío.");
            return;
        }

        currentProject.setName(trimmed);
        markProjectDirty();
        if (statusBar != null) {
            statusBar.setMessage("Proyecto renombrado: " + trimmed);
        }
        refreshProjectHeader();
    }

    public static void syncFloatingVectorEditToolbar() {
        if (floatingVectorEditToolbar != null) {
            floatingVectorEditToolbar.refreshState();
        }
    }

    public static boolean confirmProjectContinuation(String actionLabel) {
        if (!hasUnsavedProjectChanges()) {
            return true;
        }

        String projectName = currentProject.getName() != null ? currentProject.getName() : "Proyecto actual";
        Object[] options = {"Guardar", "No guardar", "Cancelar"};
        int result = JOptionPane.showOptionDialog(
                getMainFrame(),
                "El proyecto \"" + projectName + "\" tiene cambios sin guardar.\n\n¿Querés guardar antes de " + actionLabel + "?",
                "Cambios sin guardar",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == JOptionPane.CLOSED_OPTION || result == 2) {
            return false;
        }
        if (result == 0) {
            return SaveProjectAction.saveProject();
        }
        return true;
    }

    private void installWindowCloseHandler() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmProjectContinuation("cerrar CATGIS")) {
                    dispose();
                }
            }
        });
    }

    public static java.awt.Window getMainFrameSafe() {
        java.awt.Window[] windows = JFrame.getWindows();
        for (java.awt.Window window : windows) {
            if (window instanceof JFrame && window.isDisplayable()) {
                return window;
            }
        }
        return null;
    }

    public static JFrame getMainFrame() {
        java.awt.Window[] windows = JFrame.getWindows();
        for (java.awt.Window window : windows) {
            if (window instanceof JFrame && window.isDisplayable()) {
                return (JFrame) window;
            }
        }
        return null;
    }
}
