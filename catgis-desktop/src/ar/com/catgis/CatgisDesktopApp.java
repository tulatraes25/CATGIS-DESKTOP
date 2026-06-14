package ar.com.catgis;

import ar.com.catgis.core.model.Project;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import ar.com.catgis.core.model.Layer;

public class CatgisDesktopApp extends JFrame {

    /**
     * @deprecated Use {@code AppContext.get().getMapPanel()} instead.
     */
    @Deprecated
    public static volatile MapPanel mapPanel;
    /**
     * @deprecated Use {@code AppContext.get().getLayersPanel()} instead (once available).
     */
    @Deprecated
    public static volatile LayersPanel layersPanel;
    /**
     * @deprecated Use {@code AppContext.get().getStatusBar()} instead.
     */
    @Deprecated
    public static volatile StatusBar statusBar;

    /**
     * @deprecated Use {@link #getProject()} or {@link AppContext#getProject()} instead.
     * This field delegates to AppContext for centralized state management.
     */
    @Deprecated
    public static volatile Project currentProject;

    /** Get current project, backed by AppContext. */
    public static Project getProject() {
        Project ctx = AppContext.get().getProject();
        return ctx != null ? ctx : currentProject;
    }

    /** Set current project, synced to AppContext. */
    public static void setProject(Project p) {
        currentProject = p;
        AppContext.get().setProject(p);
    }
    /**
     * @deprecated Use {@code AppContext.get().getFloatingEditToolbar()} instead.
     */
    @Deprecated
    public static volatile FloatingVectorEditToolbar floatingVectorEditToolbar;
    public static volatile CartographyToolbar cartographyToolbar;
    public static volatile CatserverToolbar catserverToolbar;
    public static volatile OnlineConnectionsToolbar onlineConnectionsToolbar;
    public static volatile TopographyToolbar topographyToolbar;
    public static volatile QuickStylePanel quickStylePanel;
    public static volatile boolean quickStylePanelVisible = false;
    private static JLabel sidebarTitleLabel;
    private static JLabel sidebarSubtitleLabel;
    private static JLabel sidebarOrderHintLabel;
    private static JLabel sidebarBadgeLabel;
    private static JLabel mapTitleLabel;
    private static JLabel mapSubtitleLabel;
    private static JLabel mapStatusHintLabel;
    private boolean startupProjectCrsPromptShown;
    private JPanel moduleCards;
    private CardLayout moduleCardLayout;
    private JToggleButton datosBtn, edicionBtn, topografiaBtn, salidaBtn;

    public CatgisDesktopApp() {
        setTitle("CATGIS Desktop");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        AppBranding.applyFrameBranding(this);

        ModuleRegistry.initializeDefaults();
        mapPanel = new MapPanel();
        layersPanel = new LayersPanel();
        statusBar = new StatusBar();
        floatingVectorEditToolbar = new FloatingVectorEditToolbar();
        cartographyToolbar = new CartographyToolbar();
        catserverToolbar = new CatserverToolbar();
        onlineConnectionsToolbar = new OnlineConnectionsToolbar();
        topographyToolbar = new TopographyToolbar();

        // Wire AppContext (migration in progress — new code should use AppContext, not static fields)
        AppContext ctx = AppContext.get();
        ctx.setMapPanel(mapPanel);
        ctx.setLayersPanel(layersPanel);
        ctx.setMainFrame(this);
        ctx.setStatusBar(statusBar);
        ctx.setFloatingEditToolbar(floatingVectorEditToolbar);
        ctx.setCartographyToolbar(cartographyToolbar);
        ctx.setCatserverToolbar(catserverToolbar);
        ctx.setOnlineConnectionsToolbar(onlineConnectionsToolbar);
        ctx.setTopographyToolbar(topographyToolbar);
        statusBar.setScaleApplyListener(value -> {
            if (mapPanel != null) {
                mapPanel.applyRequestedScale(value);
            }
        });

        setJMenuBar(new MainMenuBar());
        add(buildTopContainer(), BorderLayout.NORTH);
        add(buildCenterContainer(), BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
        FileDropSupport.install(getRootPane(), mapPanel, layersPanel);

        // Start CATMAP socket server
        ar.com.catgis.catmap.CatgisSocketServer.start();

        installWindowCloseHandler();
        initializeProjectAtStartup();
        WindowLayoutSupport.fitFrameToScreen(this, 1280, 720, 1024, 680);
        setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> {
            if (mapPanel != null) {
                mapPanel.refreshStatusBarScale();
            }
        });
    }

    private JPanel buildTopContainer() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(true);
        topContainer.setBackground(new Color(241, 245, 249));

        JPanel mainToolsRow = new JPanel(new BorderLayout());
        mainToolsRow.setOpaque(false);
        mainToolsRow.add(new MainToolBar(), BorderLayout.CENTER);
        mainToolsRow.setAlignmentX(LEFT_ALIGNMENT);

        JPanel tabRow = buildModuleTabPane();
        tabRow.setAlignmentX(LEFT_ALIGNMENT);

        topContainer.add(mainToolsRow);
        topContainer.add(tabRow);
        return topContainer;
    }

    private JPanel buildModuleTabPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(true);
        panel.setBackground(new Color(241, 245, 249));

        JPanel selectorBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        selectorBar.setOpaque(true);
        selectorBar.setBackground(new Color(241, 245, 249));
        selectorBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 32));
        selectorBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 4, 0, 4),
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)
        ));

        moduleCardLayout = new CardLayout();
        moduleCards = new JPanel(moduleCardLayout);
        moduleCards.setOpaque(true);
        moduleCards.setBackground(new Color(241, 245, 249));
        moduleCards.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        moduleCards.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)));

        JPanel datosInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        datosInner.setOpaque(false);
        datosInner.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        datosInner.add(onlineConnectionsToolbar);
        datosInner.add(new JSeparator(JSeparator.VERTICAL));
        datosInner.add(buildCatserverButton());
        JPanel datosCard = new JPanel(new BorderLayout());
        datosCard.setOpaque(true);
        datosCard.setBackground(new Color(241, 245, 249));
        datosCard.add(datosInner, BorderLayout.WEST);

        JPanel edicionStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        edicionStrip.setOpaque(false);
        JButton startEditBtn = new JButton("Iniciar edicion", AppIcons.attrEditIcon());
        startEditBtn.setFont(startEditBtn.getFont().deriveFont(Font.PLAIN, 11f));
        startEditBtn.setMargin(new Insets(2, 8, 2, 8));
        startEditBtn.addActionListener(e -> startEditingSelectedLayer());
        JButton editToolsBtn = new JButton("Herramientas", AppIcons.toolboxIcon());
        editToolsBtn.setFont(editToolsBtn.getFont().deriveFont(Font.PLAIN, 11f));
        editToolsBtn.setMargin(new Insets(2, 8, 2, 8));
        editToolsBtn.addActionListener(e -> EditingToolsWindow.showWindow());
        JButton saveEditBtn = new JButton("Guardar y cerrar", AppIcons.saveIcon());
        saveEditBtn.setFont(saveEditBtn.getFont().deriveFont(Font.PLAIN, 11f));
        saveEditBtn.setMargin(new Insets(2, 8, 2, 8));
        saveEditBtn.addActionListener(e -> { if (EditingToolsWindow.isOpen()) EditingToolsWindow.saveAndClose(); });
        JButton cancelEditBtn = new JButton("Cancelar", AppIcons.cancelIcon());
        cancelEditBtn.setFont(cancelEditBtn.getFont().deriveFont(Font.PLAIN, 11f));
        cancelEditBtn.setMargin(new Insets(2, 8, 2, 8));
        cancelEditBtn.addActionListener(e -> { if (EditingToolsWindow.isOpen()) EditingToolsWindow.cancelEditing(); });
        edicionStrip.add(startEditBtn);
        edicionStrip.add(editToolsBtn);
        edicionStrip.add(saveEditBtn);
        edicionStrip.add(cancelEditBtn);

        JPanel edicionCard = new JPanel(new BorderLayout());
        edicionCard.setOpaque(false);
        edicionCard.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        edicionCard.add(edicionStrip, BorderLayout.WEST);

        JPanel topografiaCard = new JPanel(new BorderLayout());
        topografiaCard.setOpaque(false);
        topografiaCard.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        topografiaCard.add(topographyToolbar, BorderLayout.CENTER);

        JPanel salidaInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        salidaInner.setOpaque(false);
        salidaInner.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        salidaInner.add(cartographyToolbar);
        JPanel salidaCard = new JPanel(new BorderLayout());
        salidaCard.setOpaque(true);
        salidaCard.setBackground(new Color(241, 245, 249));
        salidaCard.add(salidaInner, BorderLayout.WEST);

        moduleCards.add(datosCard, "Datos");
        moduleCards.add(edicionCard, "Edicion");
        moduleCards.add(topografiaCard, "Topografia");
        moduleCards.add(salidaCard, "Salida");

        datosBtn = createModuleButton("Datos", 0);
        edicionBtn = createModuleButton("Edicion", 1);
        topografiaBtn = createModuleButton("Topografia", 2);
        salidaBtn = createModuleButton("Salida", 3);

        ButtonGroup group = new ButtonGroup();
        group.add(datosBtn);
        group.add(edicionBtn);
        group.add(topografiaBtn);
        group.add(salidaBtn);

        selectorBar.add(datosBtn);
        selectorBar.add(edicionBtn);
        selectorBar.add(topografiaBtn);
        selectorBar.add(salidaBtn);

        selectModule(0);

        panel.add(selectorBar, BorderLayout.NORTH);
        panel.add(moduleCards, BorderLayout.CENTER);
        return panel;
    }

    private JButton buildCatserverButton() {
        JButton btn = new JButton("CATSERVER");
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 11f));
        btn.setFocusable(false);
        btn.setMargin(new Insets(2, 10, 2, 10));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setToolTipText(I18n.t("Conectar CATSERVER para capas PostgreSQL / PostGIS."));
        btn.addActionListener(e -> PostgisDataSourceAction.openCatserverBrowser());
        return btn;
    }

    private JToggleButton createModuleButton(String text, int index) {
        javax.swing.Icon icon = switch (index) {
            case 0 -> AppIcons.addLayerIcon();     // Datos
            case 1 -> AppIcons.attrEditIcon();     // Edicion
            case 2 -> AppIcons.demIcon();           // Topografia
            case 3 -> AppIcons.exportIcon();        // Salida
            default -> null;
        };
        JToggleButton btn = new JToggleButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 11f));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBorderPainted(true);
        btn.setBackground(new Color(241, 245, 249));
        btn.setForeground(new Color(100, 116, 139));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(241, 245, 249)),
                BorderFactory.createEmptyBorder(5, 16, 5, 16)
        ));
        btn.addActionListener(e -> selectModule(index));
        return btn;
    }

    private void startEditingSelectedLayer() {
        Layer selected = layersPanel.getSelectedLayer();
        if (selected == null) {
            NotificationManager.info(this, "Iniciar edicion", "Selecciona una capa vectorial en el panel Capas.");
            return;
        }
        if (selected instanceof RasterLayer || selected instanceof OnlineTileLayer) {
            NotificationManager.warn(this, "Iniciar edicion", "La capa seleccionada no es vectorial editable.");
            return;
        }
        Layer existing = mapPanel != null ? mapPanel.getEditingLayerRef() : null;
        if (existing != null && existing != selected) {
            boolean yes = NotificationManager.confirm(this, "Cambiar capa de edicion", "Ya hay una capa en edicion: " + existing.getName() + ".\nDesea finalizarla y editar " + selected.getName() + "?");
            if (!yes) return;
            EditingToolsWindow.hideIfOpen();
        }
        if (mapPanel != null) {
            mapPanel.prepareLayerForEditing(selected);
        }
        syncFloatingVectorEditToolbar();
        EditingToolsWindow.showWindow();
        if (statusBar != null) statusBar.setMessage("Editando capa: " + selected.getName());
    }

    private void selectModule(int index) {
        JToggleButton[] buttons = {datosBtn, edicionBtn, topografiaBtn, salidaBtn};
        String[] names = {"Datos", "Edicion", "Topografia", "Salida"};
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != null) {
                boolean sel = i == index;
                buttons[i].setSelected(sel);
                buttons[i].setBackground(sel ? Color.WHITE : new Color(241, 245, 249));
                buttons[i].setForeground(sel ? new Color(30, 58, 138) : new Color(100, 116, 139));
                buttons[i].setFont(buttons[i].getFont().deriveFont(sel ? Font.BOLD : Font.PLAIN, 11f));
                buttons[i].setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, sel ? 2 : 0, 0, sel ? new Color(217, 164, 47) : new Color(241, 245, 249)),
                        BorderFactory.createEmptyBorder(5, 16, 5, 16)
                ));
            }
        }
        moduleCardLayout.show(moduleCards, names[index]);
    }

    private JPanel buildCenterContainer() {
        JPanel centerContainer = new JPanel(new BorderLayout(0, 0));
        centerContainer.setOpaque(true);
        centerContainer.setBackground(new Color(226, 232, 240));

        quickStylePanel.setVisible(false);
        quickStylePanel.setPreferredSize(new java.awt.Dimension(260, 100));

        JSplitPane mapAndStyle = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildMapContainer(),
                quickStylePanel
        );
        mapAndStyle.setDividerSize(4);
        mapAndStyle.setResizeWeight(1.0);
        mapAndStyle.setOneTouchExpandable(true);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftSidebar(),
                mapAndStyle
        );
        splitPane.setDividerLocation(280);
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0.0);
        splitPane.setOpaque(true);
        splitPane.setBackground(new Color(226, 232, 240));
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);

        centerContainer.add(splitPane, BorderLayout.CENTER);
        return centerContainer;
    }

    private JPanel buildLeftSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 4));
        sidebar.setOpaque(true);
        sidebar.setBackground(new Color(248, 250, 252));
        sidebar.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        sidebar.setPreferredSize(new Dimension(280, 100));

        sidebar.add(buildSidebarHeader(), BorderLayout.NORTH);

        sidebar.add(layersPanel, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel buildSidebarHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)),
                BorderFactory.createEmptyBorder(0, 2, 4, 2)
        ));

        sidebarTitleLabel = new JLabel(I18n.t("Capas"));
        sidebarTitleLabel.setFont(sidebarTitleLabel.getFont().deriveFont(Font.BOLD, 11f));
        sidebarTitleLabel.setForeground(new Color(15, 23, 42));
        sidebarTitleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(I18n.t("Gestion visual de capas"));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 10f));
        subtitle.setForeground(new Color(100, 116, 139));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        sidebarSubtitleLabel = subtitle;

        header.add(sidebarTitleLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(sidebarSubtitleLabel);

        return header;
    }

    private JPanel buildMapContainer() {
        JPanel mapContainer = new JPanel(new BorderLayout(0, 4));
        mapContainer.setOpaque(true);
        mapContainer.setBackground(Color.WHITE);
        mapContainer.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

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
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)),
                BorderFactory.createEmptyBorder(0, 2, 4, 2)
        ));

        mapTitleLabel = new JLabel(I18n.t("Mapa"));
        mapTitleLabel.setFont(mapTitleLabel.getFont().deriveFont(Font.BOLD, 11f));
        mapTitleLabel.setForeground(new Color(15, 23, 42));

        mapSubtitleLabel = new JLabel(I18n.t("Exploracion y edicion visual"));
        mapSubtitleLabel.setFont(mapSubtitleLabel.getFont().deriveFont(Font.PLAIN, 10f));
        mapSubtitleLabel.setForeground(new Color(100, 116, 139));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(mapTitleLabel);
        left.add(Box.createVerticalStrut(2));
        left.add(mapSubtitleLabel);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private void initializeProjectAtStartup() {
        if (currentProject == null) {
            currentProject = new Project(I18n.t("Proyecto actual"));
        }
        AppContext.get().setProject(currentProject);
        String currentCode = currentProject.getProjectCRS();
        if (currentCode == null || currentCode.isBlank()) {
            currentProject.setProjectCRS("EPSG:4326");
            if (statusBar != null) {
                statusBar.setMessage(I18n.format(
                        "CRS del proyecto inicial: {0}. Podés cambiarlo desde Proyecto > CRS del proyecto.",
                        CRSDefinitions.getLabelForCode("EPSG:4326")
                ));
            }
        }
        updateWindowTitle();
    }

    public void showStartupProjectCrsPromptIfNeeded() {
        if (startupProjectCrsPromptShown) {
            return;
        }
        startupProjectCrsPromptShown = true;

        if (currentProject == null) {
            currentProject = new Project(I18n.t("Proyecto actual"));
            AppContext.get().setProject(currentProject);
        }

        String fallbackCode = currentProject.getProjectCRS();
        if (fallbackCode == null || fallbackCode.isBlank()) {
            fallbackCode = "EPSG:4326";
            currentProject.setProjectCRS(fallbackCode);
        }

        String chosenCode = CRSSelectorDialog.chooseBlocking(
                this,
                I18n.t("Seleccione el CRS inicial del proyecto"),
                fallbackCode
        );
        if (chosenCode != null && !chosenCode.isBlank()) {
            currentProject.setProjectCRS(chosenCode);
        }

        if (statusBar != null) {
            statusBar.setMessage(I18n.format(
                    "CRS inicial del proyecto: {0}",
                    CRSDefinitions.getLabelForCode(currentProject.getProjectCRS())
            ));
        }

        if (mapPanel != null) {
            mapPanel.reloadRasterLayersForProjectCRS();
            mapPanel.refreshStatusBarScale();
        }

        markProjectClean();
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

        frame.setTitle(I18n.format("CATGIS \u2014 {0}{1} | {2}", dirtyPrefix, projectName, CRSDefinitions.getLabelForCode(crs)));
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
            sidebarTitleLabel.setText(I18n.t("Capas"));
        }
        if (sidebarSubtitleLabel != null) {
            sidebarSubtitleLabel.setText(projectName + " | " + CRSDefinitions.getLabelForCode(crs));
        }
        if (sidebarOrderHintLabel != null) {
            sidebarOrderHintLabel.setText("");
        }
        if (sidebarBadgeLabel != null) {
            sidebarBadgeLabel.setText("");
        }
        if (mapTitleLabel != null) {
            mapTitleLabel.setText(I18n.t("Mapa"));
        }
        if (mapSubtitleLabel != null) {
            mapSubtitleLabel.setText(I18n.t("Exploracion, edicion y analisis visual"));
        }
        if (mapStatusHintLabel != null) {
            mapStatusHintLabel.setText(I18n.format("{0} | {1} capas", projectName, layerCount));
        }
    }

    /**
     * @deprecated Use {@code AppContext.get().markProjectDirty()} instead.
     */
    @Deprecated
    public static void markProjectDirty() {
        AppContext.get().markProjectDirty();
        updateWindowTitle();
    }

    /**
     * @deprecated Use {@code AppContext.get().markProjectClean()} instead.
     */
    @Deprecated
    public static void markProjectClean() {
        AppContext.get().markProjectClean();
    }

    public static boolean hasUnsavedProjectChanges() {
        return currentProject != null && currentProject.isModified();
    }

    public static void renameCurrentProject() {
        if (currentProject == null) {
            currentProject = new Project(I18n.t("Proyecto actual"));
            AppContext.get().setProject(currentProject);
        }

        String currentName = currentProject.getName() != null ? currentProject.getName() : I18n.t("Proyecto actual");
        String newName = JOptionPane.showInputDialog(getMainFrame(), I18n.t("Nombre del proyecto:"), currentName);
        if (newName == null) {
            return;
        }

        String trimmed = newName.trim();
        if (trimmed.isEmpty()) {
            NotificationManager.warn(getMainFrame(), null, I18n.t("El nombre del proyecto no puede quedar vacio."));
            return;
        }

        currentProject.setName(trimmed);
        markProjectDirty();
        if (statusBar != null) {
            statusBar.setMessage(I18n.format("Proyecto renombrado: {0}", trimmed));
        }
        refreshProjectHeader();
    }

    /**
     * @deprecated Use {@code AppContext.get().getFloatingEditToolbar().refreshState()} directly.
     */
    @Deprecated
    public static void syncFloatingVectorEditToolbar() {
        FloatingVectorEditToolbar tb = AppContext.get().getFloatingEditToolbar();
        if (tb != null) tb.refreshState();
    }

    public static void syncProInterpretationToolbar() {
        // Pro desactivado en CATGIS Desktop base.
    }

    public static void toggleQuickStylePanel() {
        quickStylePanelVisible = !quickStylePanelVisible;
        if (quickStylePanel != null) {
            quickStylePanel.setVisible(quickStylePanelVisible);
            if (quickStylePanelVisible) {
                quickStylePanel.setLayer(AppContext.getSelectedLayer());
            }
        }
        if (getMainFrame() != null) {
            getMainFrame().revalidate();
            getMainFrame().repaint();
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

    /**
     * @deprecated Use {@code AppContext.get().getMainFrame()} instead.
     */
    @Deprecated
    public static java.awt.Window getMainFrameSafe() {
        java.awt.Window[] windows = JFrame.getWindows();
        for (java.awt.Window window : windows) {
            if (window instanceof JFrame && window.isDisplayable()) {
                return window;
            }
        }
        return null;
    }

    /**
     * @deprecated Use {@code AppContext.get().getMainFrame()} instead.
     */
    @Deprecated
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