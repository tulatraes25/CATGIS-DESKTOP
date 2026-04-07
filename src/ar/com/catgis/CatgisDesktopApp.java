package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CatgisDesktopApp extends JFrame {

    public static MapPanel mapPanel;
    public static LayersPanel layersPanel;
    public static StatusBar statusBar;
    public static Project currentProject;
    public static FloatingVectorEditToolbar floatingVectorEditToolbar;
    public static OnlineConnectionsToolbar onlineConnectionsToolbar;
    public static CartographyToolbar cartographyToolbar;
    public static TopographyToolbar topographyToolbar;
    private static JLabel sidebarTitleLabel;
    private static JLabel sidebarSubtitleLabel;
    private static JLabel sidebarOrderHintLabel;
    private static JLabel sidebarBadgeLabel;
    private static JLabel mapTitleLabel;
    private static JLabel mapSubtitleLabel;
    private static JLabel mapStatusHintLabel;

    public CatgisDesktopApp() {
        setTitle("CATGIS Desktop");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1360, 860);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        AppBranding.applyFrameBranding(this);

        ModuleRegistry.initializeDefaults();
        mapPanel = new MapPanel();
        layersPanel = new LayersPanel();
        statusBar = new StatusBar();
        floatingVectorEditToolbar = new FloatingVectorEditToolbar();
        onlineConnectionsToolbar = new OnlineConnectionsToolbar();
        cartographyToolbar = new CartographyToolbar();
        topographyToolbar = new TopographyToolbar();

        setJMenuBar(new MainMenuBar());
        add(buildTopContainer(), BorderLayout.NORTH);
        add(buildCenterContainer(), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        initializeProjectAtStartup();
        installWindowCloseHandler();
    }

    private JPanel buildTopContainer() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(true);
        topContainer.setBackground(new Color(245, 247, 250));
        topContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(218, 223, 230)),
                BorderFactory.createEmptyBorder(3, 6, 4, 6)
        ));

        JPanel mainToolsRow = new JPanel(new BorderLayout());
        mainToolsRow.setOpaque(false);
        mainToolsRow.add(new MainToolBar(), BorderLayout.CENTER);
        mainToolsRow.setAlignmentX(LEFT_ALIGNMENT);
        mainToolsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));

        JPanel editToolsRow = new JPanel(new BorderLayout());
        editToolsRow.setOpaque(false);
        editToolsRow.add(floatingVectorEditToolbar, BorderLayout.CENTER);
        editToolsRow.setAlignmentX(LEFT_ALIGNMENT);
        editToolsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel servicesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        servicesRow.setOpaque(false);
        servicesRow.add(topographyToolbar);
        servicesRow.add(onlineConnectionsToolbar);
        servicesRow.setAlignmentX(LEFT_ALIGNMENT);
        servicesRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel cartographyRow = new JPanel(new BorderLayout());
        cartographyRow.setOpaque(false);
        cartographyRow.add(cartographyToolbar, BorderLayout.CENTER);
        cartographyRow.setAlignmentX(LEFT_ALIGNMENT);
        cartographyRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        topContainer.add(mainToolsRow);
        topContainer.add(cartographyRow);
        topContainer.add(servicesRow);
        topContainer.add(editToolsRow);
        return topContainer;
    }

    private JPanel buildCenterContainer() {
        JPanel centerContainer = new JPanel(new BorderLayout(10, 10));
        centerContainer.setOpaque(true);
        centerContainer.setBackground(new Color(240, 243, 247));
        centerContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftSidebar(),
                buildMapContainer()
        );
        splitPane.setDividerLocation(300);
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
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        sidebar.setPreferredSize(new Dimension(300, 100));

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

        sidebarTitleLabel = new JLabel(I18n.t("Gestor de proyecto"));
        sidebarTitleLabel.setFont(sidebarTitleLabel.getFont().deriveFont(Font.BOLD, 17f));
        sidebarTitleLabel.setForeground(new Color(28, 37, 54));
        sidebarTitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(I18n.t("Gestion visual de capas y accesos rapidos"));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11.5f));
        subtitle.setForeground(new Color(106, 116, 130));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel orderHint = new JLabel(I18n.t("Arriba = frente | Abajo = fondo"));
        orderHint.setFont(orderHint.getFont().deriveFont(Font.BOLD, 10.5f));
        orderHint.setForeground(new Color(33, 120, 210));
        orderHint.setAlignmentX(LEFT_ALIGNMENT);

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
        sidebarOrderHintLabel = orderHint;

        JPanel badgeWrap = new JPanel(new BorderLayout());
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge, BorderLayout.WEST);
        badgePanel.add(badgeWrap, BorderLayout.WEST);

        header.add(sidebarTitleLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(sidebarSubtitleLabel);
        header.add(Box.createVerticalStrut(3));
        header.add(sidebarOrderHintLabel);
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
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        mapContainer.add(buildMapHeader(), BorderLayout.NORTH);
        mapContainer.add(buildMapWorkspace(), BorderLayout.CENTER);
        return mapContainer;
    }

    private JPanel buildMapWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setOpaque(true);
        workspace.setBackground(Color.WHITE);
        workspace.add(mapPanel, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel buildMapHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 2));

        mapTitleLabel = new JLabel(I18n.t("Vista de mapa"));
        mapTitleLabel.setFont(mapTitleLabel.getFont().deriveFont(Font.BOLD, 17f));
        mapTitleLabel.setForeground(new Color(28, 37, 54));

        mapSubtitleLabel = new JLabel(I18n.t("Exploracion, edicion y analisis visual"));
        mapSubtitleLabel.setFont(mapSubtitleLabel.getFont().deriveFont(Font.PLAIN, 11.5f));
        mapSubtitleLabel.setForeground(new Color(106, 116, 130));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(mapTitleLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(mapSubtitleLabel);

        JLabel statusHint = new JLabel(I18n.t("Proyecto activo"));
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
            currentProject = new Project(I18n.t("Proyecto actual"));
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
                I18n.t("Seleccione el CRS inicial del proyecto"),
                currentCode,
                code -> {
                    String finalCode = (code != null && !code.isBlank()) ? code : fallbackCode;
                    currentProject.setProjectCRS(finalCode);

                    if (statusBar != null) {
                        statusBar.setMessage(I18n.format("CRS inicial del proyecto: {0}", CRSDefinitions.getLabelForCode(finalCode)));
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

        String projectName = currentProject.getName() != null ? currentProject.getName() : I18n.t("Proyecto actual");
        String crs = currentProject.getProjectCRS() != null && !currentProject.getProjectCRS().isBlank()
                ? currentProject.getProjectCRS()
                : "EPSG:4326";
        String dirtyPrefix = currentProject.isModified() ? "* " : "";

        frame.setTitle(I18n.format("CATGIS Desktop - {0}{1} - {2}", dirtyPrefix, projectName, CRSDefinitions.getLabelForCode(crs)));
        refreshProjectHeader();
    }

    public static void refreshProjectHeader() {
        if (currentProject == null) {
            return;
        }

        String projectName = currentProject.getName() != null && !currentProject.getName().isBlank()
                ? currentProject.getName()
                : I18n.t("Proyecto actual");
        String crs = currentProject.getProjectCRS() != null && !currentProject.getProjectCRS().isBlank()
                ? currentProject.getProjectCRS()
                : "EPSG:4326";
        int layerCount = currentProject.getLayers() != null ? currentProject.getLayers().size() : 0;

        if (sidebarTitleLabel != null) {
            sidebarTitleLabel.setText(I18n.t("Gestor de proyecto"));
        }
        if (sidebarSubtitleLabel != null) {
            sidebarSubtitleLabel.setText(projectName + " | " + CRSDefinitions.getLabelForCode(crs));
        }
        if (sidebarOrderHintLabel != null) {
            sidebarOrderHintLabel.setText(I18n.t("Arriba = frente | Abajo = fondo"));
        }
        if (sidebarBadgeLabel != null) {
            sidebarBadgeLabel.setText(currentProject.isModified() ? "CATGIS *" : "CATGIS");
        }
        if (mapTitleLabel != null) {
            mapTitleLabel.setText(I18n.t("Vista de mapa"));
        }
        if (mapSubtitleLabel != null) {
            mapSubtitleLabel.setText(I18n.t("Exploracion, edicion y analisis visual"));
        }
        if (mapStatusHintLabel != null) {
            mapStatusHintLabel.setText(I18n.format("{0} | {1} capas", projectName, layerCount));
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
            currentProject = new Project(I18n.t("Proyecto actual"));
        }

        String currentName = currentProject.getName() != null ? currentProject.getName() : I18n.t("Proyecto actual");
        String newName = JOptionPane.showInputDialog(getMainFrame(), I18n.t("Nombre del proyecto:"), currentName);
        if (newName == null) {
            return;
        }

        String trimmed = newName.trim();
        if (trimmed.isEmpty()) {
            JOptionPane.showMessageDialog(getMainFrame(), I18n.t("El nombre del proyecto no puede quedar vacio."));
            return;
        }

        currentProject.setName(trimmed);
        markProjectDirty();
        if (statusBar != null) {
            statusBar.setMessage(I18n.format("Proyecto renombrado: {0}", trimmed));
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

        String projectName = currentProject.getName() != null ? currentProject.getName() : I18n.t("Proyecto actual");
        Object[] options = {I18n.t("Guardar"), I18n.t("No guardar"), I18n.t("Cancelar")};
        int result = JOptionPane.showOptionDialog(
                getMainFrame(),
                I18n.format("El proyecto \"{0}\" tiene cambios sin guardar.\n\n¿Queres guardar antes de {1}?", projectName, actionLabel),
                I18n.t("Cambios sin guardar"),
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
                if (confirmProjectContinuation(I18n.t("cerrar CATGIS"))) {
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
