package ar.com.catgis.catmap;

import ar.com.catgis.*;
import ar.com.catgis.layout.*;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * CATMAP Standalone - Cartographic Layout Composer.
 * Independent application for map layout, composition, and export.
 */
public class Main {

    private static LayoutModel layoutModel;
    private static LayoutPreviewPanel previewPanel;
    private static JLabel statusLabel;
    private static JFrame mainFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            installLookAndFeel();
            I18n.initialize();

            String projectPath = null;
            String layoutPath = null;

            // Parse arguments
            for (int i = 0; i < args.length; i++) {
                if ("--project".equals(args[i]) && i + 1 < args.length) {
                    projectPath = args[++i];
                } else if ("--catmap".equals(args[i]) && i + 1 < args.length) {
                    layoutPath = args[++i];
                } else if (args[i].endsWith(".catgis")) {
                    projectPath = args[i];
                } else if (args[i].endsWith(".catmap")) {
                    layoutPath = args[i];
                }
            }

            // Initialize minimal app context
            ModuleRegistry.initializeDefaults();
            CatgisDesktopApp.currentProject = new Project("CATMAP Standalone");
            CatgisDesktopApp.mapPanel = new MapPanel();
            CatgisDesktopApp.layersPanel = new LayersPanel();
            CatgisDesktopApp.statusBar = new StatusBar();

            // Load project if specified
            if (projectPath != null) {
                try {
                    LoadProjectAction.loadProjectFile(new File(projectPath));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "No se pudo cargar el proyecto:\n" + ex.getMessage(),
                            "CATMAP", JOptionPane.ERROR_MESSAGE);
                }
            }

            // Create and show main window
            mainFrame = createMainWindow();
            mainFrame.setVisible(true);

            // Try to connect to CATGIS
            if (CatmapSocketClient.connect()) {
                statusLabel.setText("Conectado a CATGIS Desktop");
                syncFromCatgis();
            } else {
                statusLabel.setText("CATMAP Standalone (sin conexión a CATGIS)");
            }

            // Load layout if specified
            if (layoutPath != null) {
                loadLayoutFile(new File(layoutPath));
            }
        });
    }

    private static JFrame createMainWindow() {
        JFrame frame = new JFrame("CATMAP - Cartographic Layout Composer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Initialize layout model
        layoutModel = new LayoutModel();

        // Add default elements if project exists
        if (CatgisDesktopApp.currentProject != null
                && CatgisDesktopApp.currentProject.getLayers() != null
                && !CatgisDesktopApp.currentProject.getLayers().isEmpty()) {
            addDefaultElements(layoutModel);
        }

        // Create preview panel
        LayoutRenderContext renderCtx = new LayoutRenderContext(
                LayoutRenderContext.Mode.PREVIEW, 96, 297, 210);
        previewPanel = new LayoutPreviewPanel(layoutModel, renderCtx);
        previewPanel.setSelectionCallback(element -> updatePropertiesPanel(element));

        // Build UI
        frame.setJMenuBar(createMenuBar());
        frame.add(createToolBar(), BorderLayout.NORTH);
        frame.add(createMainPanel(), BorderLayout.CENTER);

        // Status bar
        statusLabel = new JLabel("Listo | CATMAP Standalone v1.0");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        frame.add(statusLabel, BorderLayout.SOUTH);

        frame.setSize(1400, 900);
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // --- Archivo ---
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        addMenuItem(menuArchivo, "Nuevo layout", KeyEvent.VK_N, e -> newLayout());
        addMenuItem(menuArchivo, "Abrir layout...", KeyEvent.VK_O, e -> openLayout());
        addMenuItem(menuArchivo, "Abrir proyecto .catgis...", 0, e -> openProject());
        menuArchivo.addSeparator();
        addMenuItem(menuArchivo, "Guardar", KeyEvent.VK_S, e -> saveLayout());
        addMenuItem(menuArchivo, "Guardar como...", 0, e -> saveLayoutAs());
        menuArchivo.addSeparator();
        addMenuItem(menuArchivo, "Exportar PDF", 0, e -> exportPdf());
        addMenuItem(menuArchivo, "Exportar PNG", 0, e -> exportPng());
        addMenuItem(menuArchivo, "Exportar SVG", 0, e -> exportSvg());
        addMenuItem(menuArchivo, "Exportar imagen...", 0, e -> exportImage());
        menuArchivo.addSeparator();
        addMenuItem(menuArchivo, "Imprimir...", KeyEvent.VK_P, e -> printLayout());
        menuArchivo.addSeparator();
        addMenuItem(menuArchivo, "Salir", KeyEvent.VK_X, e -> System.exit(0));
        menuBar.add(menuArchivo);

        // --- Edición ---
        JMenu menuEdicion = new JMenu("Edición");
        menuEdicion.setMnemonic(KeyEvent.VK_E);
        addMenuItem(menuEdicion, "Deshacer", KeyEvent.VK_Z, e -> { layoutModel.undo(); previewPanel.invalidateRender(); previewPanel.repaint(); });
        addMenuItem(menuEdicion, "Rehacer", KeyEvent.VK_Y, e -> { layoutModel.redo(); previewPanel.invalidateRender(); previewPanel.repaint(); });
        menuEdicion.addSeparator();
        addMenuItem(menuEdicion, "Copiar", KeyEvent.VK_C, e -> {});
        addMenuItem(menuEdicion, "Pegar", KeyEvent.VK_V, e -> {});
        addMenuItem(menuEdicion, "Duplicar", 0, e -> {});
        addMenuItem(menuEdicion, "Eliminar", KeyEvent.VK_DELETE, e -> {});
        menuEdicion.addSeparator();
        addMenuItem(menuEdicion, "Seleccionar todo", KeyEvent.VK_A, e -> {});
        addMenuItem(menuEdicion, "Bloquear elemento", 0, e -> {});
        addMenuItem(menuEdicion, "Desbloquear elemento", 0, e -> {});
        menuBar.add(menuEdicion);

        // --- Vista ---
        JMenu menuVista = new JMenu("Vista");
        menuVista.setMnemonic(KeyEvent.VK_V);
        addMenuItem(menuVista, "Zoom a página", 0, e -> previewPanel.setZoom(1.0));
        addMenuItem(menuVista, "Zoom al ancho", 0, e -> {});
        addMenuItem(menuVista, "Zoom 100%", 0, e -> previewPanel.setZoom(1.0));
        menuVista.addSeparator();
        JCheckBoxMenuItem showRulers = new JCheckBoxMenuItem("Mostrar reglas", true);
        menuVista.add(showRulers);
        JCheckBoxMenuItem showGrid = new JCheckBoxMenuItem("Mostrar grilla", false);
        menuVista.add(showGrid);
        JCheckBoxMenuItem showGuides = new JCheckBoxMenuItem("Mostrar guías", false);
        menuVista.add(showGuides);
        menuVista.addSeparator();
        JCheckBoxMenuItem showLayers = new JCheckBoxMenuItem("Mostrar panel de capas", true);
        menuVista.add(showLayers);
        JCheckBoxMenuItem showProps = new JCheckBoxMenuItem("Mostrar panel de propiedades", true);
        menuVista.add(showProps);
        menuBar.add(menuVista);

        // --- Insertar ---
        JMenu menuInsertar = new JMenu("Insertar");
        menuInsertar.setMnemonic(KeyEvent.VK_I);
        addMenuItem(menuInsertar, "Texto", 0, e -> insertText());
        addMenuItem(menuInsertar, "Imagen", 0, e -> insertImage());
        menuInsertar.addSeparator();
        addMenuItem(menuInsertar, "Rectángulo", 0, e -> insertRectangle());
        addMenuItem(menuInsertar, "Elipse", 0, e -> insertEllipse());
        addMenuItem(menuInsertar, "Línea", 0, e -> insertLine());
        menuInsertar.addSeparator();
        addMenuItem(menuInsertar, "Mapa principal", 0, e -> insertMap());
        addMenuItem(menuInsertar, "Leyenda", 0, e -> insertLegend());
        addMenuItem(menuInsertar, "Escala gráfica", 0, e -> insertScaleBar());
        addMenuItem(menuInsertar, "Norte", 0, e -> insertNorth());
        addMenuItem(menuInsertar, "Tabla simple", 0, e -> insertTable());
        menuBar.add(menuInsertar);

        // --- Mapa ---
        JMenu menuMapa = new JMenu("Mapa");
        menuMapa.setMnemonic(KeyEvent.VK_M);
        addMenuItem(menuMapa, "Actualizar desde CATGIS", 0, e -> refreshFromCatgis());
        addMenuItem(menuMapa, "Sincronizar capas visibles", 0, e -> {});
        addMenuItem(menuMapa, "Sincronizar simbología", 0, e -> {});
        addMenuItem(menuMapa, "Sincronizar etiquetas", 0, e -> {});
        menuMapa.addSeparator();
        addMenuItem(menuMapa, "Usar extent actual de CATGIS", 0, e -> useCatgisExtent());
        addMenuItem(menuMapa, "Fijar extent del mapa", 0, e -> {});
        addMenuItem(menuMapa, "Ajustar mapa al extent de capas visibles", 0, e -> fitToVisibleLayers());
        addMenuItem(menuMapa, "Refrescar mapa", KeyEvent.VK_F5, e -> refreshMap());
        menuBar.add(menuMapa);

        // --- Exportar ---
        JMenu menuExportar = new JMenu("Exportar");
        menuExportar.setMnemonic(KeyEvent.VK_X);
        addMenuItem(menuExportar, "Exportar PDF", 0, e -> exportPdf());
        addMenuItem(menuExportar, "Exportar PNG", 0, e -> exportPng());
        addMenuItem(menuExportar, "Exportar JPG", 0, e -> exportJpg());
        addMenuItem(menuExportar, "Exportar SVG", 0, e -> {});
        menuExportar.addSeparator();
        addMenuItem(menuExportar, "Configuración de resolución/DPI...", 0, e -> {});
        menuBar.add(menuExportar);

        // --- Ayuda ---
        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.setMnemonic(KeyEvent.VK_H);
        addMenuItem(menuAyuda, "Atajos de teclado", 0, e -> showShortcuts());
        addMenuItem(menuAyuda, "Documentación", 0, e -> {});
        menuAyuda.addSeparator();
        addMenuItem(menuAyuda, "Acerca de CATMAP", 0, e -> showAbout());
        menuBar.add(menuAyuda);

        return menuBar;
    }

    private static JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        // File actions
        addToolIconButton(toolbar, "Nuevo", null, e -> newLayout());
        addToolIconButton(toolbar, "Abrir", AppIcons.openIcon(), e -> openLayout());
        addToolIconButton(toolbar, "Guardar", AppIcons.saveIcon(), e -> saveLayout());
        toolbar.addSeparator();
        addToolIconButton(toolbar, "Exportar PDF", AppIcons.exportIcon(), e -> exportPdf());
        addToolIconButton(toolbar, "Exportar PNG", AppIcons.exportIcon(), e -> exportPng());
        addToolIconButton(toolbar, "Imprimir", null, e -> printLayout());
        toolbar.addSeparator();

        // Layout tools
        addToolIconButton(toolbar, "Seleccionar", AppIcons.selectIcon(), e -> {});
        addToolIconButton(toolbar, "Pan mapa", AppIcons.panIcon(), e -> {});
        addToolIconButton(toolbar, "Zoom mapa", AppIcons.zoomInIcon(), e -> {});
        toolbar.addSeparator();

        // Insert elements
        addToolIconButton(toolbar, "Texto", null, e -> insertText());
        addToolIconButton(toolbar, "Imagen", null, e -> insertImage());
        addToolIconButton(toolbar, "Rectángulo", null, e -> insertRectangle());
        addToolIconButton(toolbar, "Elipse", null, e -> insertEllipse());
        addToolIconButton(toolbar, "Línea", AppIcons.lineIcon(), e -> insertLine());
        toolbar.addSeparator();
        addToolIconButton(toolbar, "Mapa", null, e -> insertMap());
        addToolIconButton(toolbar, "Leyenda", null, e -> insertLegend());
        addToolIconButton(toolbar, "Escala", null, e -> insertScaleBar());
        addToolIconButton(toolbar, "Norte", null, e -> insertNorth());
        toolbar.addSeparator();

        // Edit actions
        addToolIconButton(toolbar, "Duplicar", AppIcons.attrCopyIcon(), e -> {});
        addToolIconButton(toolbar, "Subir", AppIcons.upIcon(), e -> {});
        addToolIconButton(toolbar, "Bajar", AppIcons.downIcon(), e -> {});
        addToolIconButton(toolbar, "Quitar", AppIcons.removeIcon(), e -> {});

        return toolbar;
    }

    private static JPanel propsPanel;
    private static JLabel selectedElementLabel;

    private static JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Left panel: element list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        leftPanel.setPreferredSize(new Dimension(200, 0));
        JLabel leftTitle = new JLabel("Elementos del layout");
        leftTitle.setFont(leftTitle.getFont().deriveFont(Font.BOLD, 12f));
        leftPanel.add(leftTitle, BorderLayout.NORTH);
        JList<String> elementList = new JList<>(new DefaultListModel<>());
        leftPanel.add(new JScrollPane(elementList), BorderLayout.CENTER);

        // Center: preview
        JScrollPane previewScroll = new JScrollPane(previewPanel);
        previewScroll.getHorizontalScrollBar().setUnitIncrement(16);
        previewScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Right panel: contextual properties
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        rightPanel.setPreferredSize(new Dimension(240, 0));
        JLabel rightTitle = new JLabel("Propiedades");
        rightTitle.setFont(rightTitle.getFont().deriveFont(Font.BOLD, 12f));
        rightPanel.add(rightTitle, BorderLayout.NORTH);
        propsPanel = new JPanel();
        propsPanel.setLayout(new BoxLayout(propsPanel, BoxLayout.Y_AXIS));
        selectedElementLabel = new JLabel("Selecciona un elemento");
        selectedElementLabel.setFont(selectedElementLabel.getFont().deriveFont(Font.PLAIN, 11f));
        selectedElementLabel.setForeground(new Color(107, 114, 128));
        propsPanel.add(selectedElementLabel);
        rightPanel.add(new JScrollPane(propsPanel), BorderLayout.CENTER);

        // Bottom: layer panel with checkboxes
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        bottomPanel.setPreferredSize(new Dimension(0, 150));
        JPanel bottomHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomHeader.setOpaque(false);
        JLabel bottomTitle = new JLabel("Capas del mapa");
        bottomTitle.setFont(bottomTitle.getFont().deriveFont(Font.BOLD, 12f));
        JButton refreshLayersBtn = new JButton("Refrescar");
        refreshLayersBtn.setFont(refreshLayersBtn.getFont().deriveFont(Font.PLAIN, 10f));
        refreshLayersBtn.addActionListener(e -> refreshLayerList());
        bottomHeader.add(bottomTitle);
        bottomHeader.add(refreshLayersBtn);
        bottomPanel.add(bottomHeader, BorderLayout.NORTH);
        JList<String> layerList = new JList<>(new DefaultListModel<>());
        bottomPanel.add(new JScrollPane(layerList), BorderLayout.CENTER);

        // Split panes
        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, previewScroll);
        leftSplit.setDividerLocation(200);
        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, rightPanel);
        rightSplit.setDividerLocation(900);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightSplit, bottomPanel);
        mainSplit.setDividerLocation(600);

        mainPanel.add(mainSplit, BorderLayout.CENTER);
        return mainPanel;
    }

    private static void updatePropertiesPanel(LayoutElement element) {
        propsPanel.removeAll();
        if (element == null) {
            selectedElementLabel.setText("Selecciona un elemento");
            propsPanel.add(selectedElementLabel);
        } else {
            selectedElementLabel.setText(element.getClass().getSimpleName());
            propsPanel.add(selectedElementLabel);

            // Position & Size (editable)
            JTextField xField = addEditablePropRow(propsPanel, "X (mm):", String.format("%.1f", element.getBoundsMm().x),
                    e -> { layoutModel.saveSnapshot(); try { element.setBoundsMm(Double.parseDouble(e), element.getBoundsMm().y, element.getBoundsMm().width, element.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
            JTextField yField = addEditablePropRow(propsPanel, "Y (mm):", String.format("%.1f", element.getBoundsMm().y),
                    e -> { layoutModel.saveSnapshot(); try { element.setBoundsMm(element.getBoundsMm().x, Double.parseDouble(e), element.getBoundsMm().width, element.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
            JTextField wField = addEditablePropRow(propsPanel, "Ancho (mm):", String.format("%.1f", element.getBoundsMm().width),
                    e -> { layoutModel.saveSnapshot(); try { element.setBoundsMm(element.getBoundsMm().x, element.getBoundsMm().y, Double.parseDouble(e), element.getBoundsMm().height); previewPanel.repaint(); } catch (Exception ignored) {} });
            JTextField hField = addEditablePropRow(propsPanel, "Alto (mm):", String.format("%.1f", element.getBoundsMm().height),
                    e -> { layoutModel.saveSnapshot(); try { element.setBoundsMm(element.getBoundsMm().x, element.getBoundsMm().y, element.getBoundsMm().width, Double.parseDouble(e)); previewPanel.repaint(); } catch (Exception ignored) {} });

            // Type-specific editable properties
            if (element instanceof LayoutLabel label) {
                addEditablePropRow(propsPanel, "Texto:", label.getText(),
                        e -> { layoutModel.saveSnapshot(); label.setText(e); previewPanel.repaint(); });
                addEditablePropRow(propsPanel, "Fuente:", label.getFont().getFamily(),
                        e -> { layoutModel.saveSnapshot(); label.setFont(new Font(e, label.getFont().getStyle(), label.getFont().getSize())); previewPanel.repaint(); });
                addEditablePropRow(propsPanel, "Tamaño:", String.valueOf(label.getFont().getSize()),
                        e -> { layoutModel.saveSnapshot(); try { label.setFont(new Font(label.getFont().getFamily(), label.getFont().getStyle(), Integer.parseInt(e))); previewPanel.repaint(); } catch (Exception ignored) {} });
                JCheckBox boldCheck = new JCheckBox("Negrita", label.getFont().isBold());
                boldCheck.addActionListener(e -> { label.setFont(label.getFont().deriveFont(boldCheck.isSelected() ? label.getFont().getStyle() | Font.BOLD : label.getFont().getStyle() & ~Font.BOLD)); previewPanel.repaint(); });
                JCheckBox italicCheck = new JCheckBox("Cursiva", label.getFont().isItalic());
                italicCheck.addActionListener(e -> { label.setFont(label.getFont().deriveFont(italicCheck.isSelected() ? label.getFont().getStyle() | Font.ITALIC : label.getFont().getStyle() & ~Font.ITALIC)); previewPanel.repaint(); });
                JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                stylePanel.setOpaque(false);
                stylePanel.add(boldCheck);
                stylePanel.add(italicCheck);
                addPropRowCustom(propsPanel, "Estilo:", stylePanel);
            } else if (element instanceof LayoutMap map) {
                JCheckBox gridCheck = new JCheckBox("Mostrar grilla", map.isShowGrid());
                gridCheck.addActionListener(e -> { map.setShowGrid(gridCheck.isSelected()); previewPanel.repaint(); });
                addPropRowCustom(propsPanel, "Grilla:", gridCheck);
            } else if (element instanceof LayoutLegend legend) {
                addEditablePropRow(propsPanel, "Título:", legend.getTitle(),
                        e -> { layoutModel.saveSnapshot(); legend.setTitle(e); previewPanel.repaint(); });
                JCheckBox autoCheck = new JCheckBox("Auto-alto", legend.isAutoHeight());
                autoCheck.addActionListener(e -> { legend.setAutoHeight(autoCheck.isSelected()); previewPanel.repaint(); });
                addPropRowCustom(propsPanel, "Auto-alto:", autoCheck);
            } else if (element instanceof LayoutScaleBar scale) {
                addEditablePropRow(propsPanel, "Escala 1:", String.valueOf((int) scale.getMapScaleDenominator()),
                        e -> { layoutModel.saveSnapshot(); try { scale.setMapScaleDenominator(Double.parseDouble(e)); previewPanel.repaint(); } catch (Exception ignored) {} });
                addEditablePropRow(propsPanel, "Segmentos:", String.valueOf(scale.getSegments()),
                        e -> { layoutModel.saveSnapshot(); try { scale.setSegments(Integer.parseInt(e)); previewPanel.repaint(); } catch (Exception ignored) {} });
            } else if (element instanceof LayoutNorthArrow) {
                addPropRow(propsPanel, "Tipo:", "Flecha norte");
            } else if (element instanceof LayoutCartouche cartouche) {
                for (var entry : cartouche.getFields().entrySet()) {
                    addEditablePropRow(propsPanel, entry.getKey() + ":", entry.getValue(),
                            e -> { layoutModel.saveSnapshot(); cartouche.setField(entry.getKey(), e); previewPanel.repaint(); });
                }
            }
        }
        propsPanel.revalidate();
        propsPanel.repaint();
    }

    private static void addPropRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 10f));
        lbl.setForeground(new Color(107, 114, 128));
        lbl.setPreferredSize(new Dimension(70, 20));
        JLabel val = new JLabel(value);
        val.setFont(val.getFont().deriveFont(Font.PLAIN, 10f));
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        panel.add(row);
    }

    private static JTextField addEditablePropRow(JPanel panel, String label, String value, java.util.function.Consumer<String> onChange) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 10f));
        lbl.setForeground(new Color(107, 114, 128));
        lbl.setPreferredSize(new Dimension(70, 20));
        JTextField field = new JTextField(value, 12);
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 10f));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent e) { onChange.accept(field.getText()); }
        });
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        panel.add(row);
        return field;
    }

    private static void addPropRowCustom(JPanel panel, String label, java.awt.Component component) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 10f));
        lbl.setForeground(new Color(107, 114, 128));
        lbl.setPreferredSize(new Dimension(70, 20));
        row.add(lbl, BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        panel.add(row);
    }

    private static void refreshLayerList() {
        statusLabel.setText("Capas actualizadas");
    }

    // --- Actions ---

    private static void newLayout() {
        layoutModel = new LayoutModel();
        currentFile = null;
        addDefaultElements(layoutModel);
        previewPanel.invalidateRender();
        previewPanel.repaint();
        mainFrame.setTitle("CATMAP - Nuevo layout");
        statusLabel.setText("Nuevo layout creado");
    }

    private static void openLayout() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CATMAP Layout (*.catmap)", "catmap"));
        if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            loadLayoutFile(chooser.getSelectedFile());
        }
    }

    private static void openProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CATGIS Project (*.catgis)", "catgis"));
        if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            try {
                LoadProjectAction.loadProjectFile(chooser.getSelectedFile());
                statusLabel.setText("Proyecto cargado: " + chooser.getSelectedFile().getName());
                refreshMap();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Error al cargar proyecto:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static File currentFile = null;

    private static void saveLayout() {
        if (currentFile == null) {
            saveLayoutAs();
            return;
        }
        try {
            CatmapSerializer.save(layoutModel, currentFile);
            statusLabel.setText("Layout guardado: " + currentFile.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Error al guardar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void saveLayoutAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CATMAP Layout (*.catmap)", "catmap"));
        if (chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".catmap")) {
                file = new File(file.getAbsolutePath() + ".catmap");
            }
            try {
                CatmapSerializer.save(layoutModel, file);
                currentFile = file;
                mainFrame.setTitle("CATMAP - " + file.getName());
                statusLabel.setText("Layout guardado: " + file.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Error al guardar:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void exportPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }
            statusLabel.setText("Exportando PDF...");
            try {
                LayoutExportEngine.exportPdf(layoutModel, file, 150);
                statusLabel.setText("PDF exportado: " + file.getName());
                JOptionPane.showMessageDialog(mainFrame,
                        "PDF exportado correctamente:\n" + file.getAbsolutePath(),
                        "Exportar PDF", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Error al exportar PDF:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error al exportar PDF");
            }
        }
    }

    private static void exportPng() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
        if (chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            statusLabel.setText("Exportando PNG...");
            try {
                LayoutExportEngine.exportPng(layoutModel, file, 150);
                statusLabel.setText("PNG exportado: " + file.getName());
                JOptionPane.showMessageDialog(mainFrame,
                        "PNG exportado correctamente:\n" + file.getAbsolutePath(),
                        "Exportar PNG", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Error al exportar PNG:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error al exportar PNG");
            }
        }
    }

    private static void exportJpg() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("JPG (*.jpg)", "jpg"));
        if (chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".jpg")) {
                file = new File(file.getAbsolutePath() + ".jpg");
            }
            statusLabel.setText("Exportando JPG...");
            try {
                LayoutExportEngine.exportJpg(layoutModel, file, 150);
                statusLabel.setText("JPG exportado: " + file.getName());
                JOptionPane.showMessageDialog(mainFrame,
                        "JPG exportado correctamente:\n" + file.getAbsolutePath(),
                        "Exportar JPG", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Error al exportar JPG:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error al exportar JPG");
            }
        }
    }

    private static void exportSvg() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("SVG (*.svg)", "svg"));
        if (chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".svg")) {
                file = new File(file.getAbsolutePath() + ".svg");
            }
            statusLabel.setText("Exportando SVG...");
            try {
                SvgExportEngine.exportSvg(layoutModel, file, 150);
                statusLabel.setText("SVG exportado: " + file.getName());
                JOptionPane.showMessageDialog(mainFrame,
                        "SVG exportado correctamente:\n" + file.getAbsolutePath(),
                        "Exportar SVG", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Error al exportar SVG:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error al exportar SVG");
            }
        }
    }

    private static void exportImage() {
        exportPng(); // Default to PNG
    }

    private static void printLayout() {
        statusLabel.setText("Preparando impresión...");
        try {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setJobName("CATMAP - Layout");

            // Render at print resolution
            java.awt.image.BufferedImage image = LayoutExportEngine.renderLayout(layoutModel, 200);

            job.setPrintable((java.awt.print.Printable) (java.awt.Graphics graphics,
                    java.awt.print.PageFormat pageFormat, int pageIndex) -> {
                if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics;
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double sx = pageFormat.getImageableWidth() / image.getWidth();
                double sy = pageFormat.getImageableHeight() / image.getHeight();
                double scale = Math.min(sx, sy);
                g2.scale(scale, scale);
                g2.drawImage(image, 0, 0, null);
                return java.awt.print.Printable.PAGE_EXISTS;
            });

            if (job.printDialog()) {
                statusLabel.setText("Imprimiendo...");
                job.print();
                statusLabel.setText("Impresión completada");
            } else {
                statusLabel.setText("Impresión cancelada");
            }
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(mainFrame,
                    "Error al imprimir:\n" + ex.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Error al imprimir");
        }
    }

    private static void insertText() {
        LayoutLabel lbl = new LayoutLabel("text-" + System.currentTimeMillis(), "Texto libre", 60, 60, 160, 24);
        lbl.setZOrder(layoutModel.nextZ());
        lbl.setName("Texto " + (layoutModel.size() + 1));
        layoutModel.addElement(lbl);
        previewPanel.repaint();
        statusLabel.setText("Texto insertado");
    }

    private static void insertImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes (*.png, *.jpg)", "png", "jpg"));
        if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            try {
                java.awt.image.BufferedImage bi = javax.imageio.ImageIO.read(chooser.getSelectedFile());
                if (bi != null) {
                    LayoutImage img = new LayoutImage("img-" + System.currentTimeMillis(), bi, 40, 40, 100, 100);
                    img.setZOrder(layoutModel.nextZ());
                    img.setName("Imagen " + (layoutModel.size() + 1));
                    layoutModel.addElement(img);
                    previewPanel.repaint();
                    statusLabel.setText("Imagen insertada");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Error al cargar imagen:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void insertRectangle() {
        LayoutRectangle r = new LayoutRectangle("rect-" + System.currentTimeMillis(), 40, 40, 100, 60);
        r.setZOrder(layoutModel.nextZ());
        r.setName("Rectángulo " + (layoutModel.size() + 1));
        layoutModel.addElement(r);
        previewPanel.repaint();
        statusLabel.setText("Rectángulo insertado");
    }

    private static void insertEllipse() {
        LayoutEllipse el = new LayoutEllipse("ellipse-" + System.currentTimeMillis(), 40, 40, 80, 60);
        el.setZOrder(layoutModel.nextZ());
        el.setName("Elipse " + (layoutModel.size() + 1));
        layoutModel.addElement(el);
        previewPanel.repaint();
        statusLabel.setText("Elipse insertada");
    }

    private static void insertLine() {
        LayoutLine l = new LayoutLine("line-" + System.currentTimeMillis(), 40, 40, 140, 40);
        l.setZOrder(layoutModel.nextZ());
        l.setName("Línea " + (layoutModel.size() + 1));
        layoutModel.addElement(l);
        previewPanel.repaint();
        statusLabel.setText("Línea insertada");
    }

    private static void insertMap() {
        LayoutMap map = new LayoutMap("map-" + System.currentTimeMillis(), 15, 25, 267, 160);
        map.setZOrder(layoutModel.nextZ());
        map.setName("Mapa " + (layoutModel.size() + 1));
        map.setFrameColor(new Color(0x4A5568));
        map.setFrameWidth(0.8f);
        layoutModel.addElement(map);
        previewPanel.repaint();
        statusLabel.setText("Mapa insertado");
    }

    private static void insertLegend() {
        LayoutLegend legend = new LayoutLegend("legend-" + System.currentTimeMillis(), 155, 55, 75, 40);
        legend.setZOrder(layoutModel.nextZ());
        legend.setAutoHeight(true);
        legend.setName("Leyenda " + (layoutModel.size() + 1));
        layoutModel.addElement(legend);
        previewPanel.repaint();
        statusLabel.setText("Leyenda insertada");
    }

    private static void insertScaleBar() {
        LayoutScaleBar sb = new LayoutScaleBar("scale-" + System.currentTimeMillis(), 145, 175, 95, 10);
        sb.setZOrder(layoutModel.nextZ());
        sb.setName("Escala " + (layoutModel.size() + 1));
        layoutModel.addElement(sb);
        previewPanel.repaint();
        statusLabel.setText("Escala insertada");
    }

    private static void insertNorth() {
        LayoutNorthArrow na = new LayoutNorthArrow("north-" + System.currentTimeMillis(), 250, 30, 16, 16);
        na.setZOrder(layoutModel.nextZ());
        na.setName("Norte " + (layoutModel.size() + 1));
        layoutModel.addElement(na);
        previewPanel.repaint();
        statusLabel.setText("Norte insertado");
    }

    private static void insertTable() {
        LayoutTable t = new LayoutTable("table-" + System.currentTimeMillis(), 40, 180, 200, 30);
        t.setZOrder(layoutModel.nextZ());
        t.setName("Tabla " + (layoutModel.size() + 1));
        layoutModel.addElement(t);
        previewPanel.repaint();
        statusLabel.setText("Tabla insertada");
    }

    private static void syncFromCatgis() {
        CatmapSocketClient.ProjectState state = CatmapSocketClient.getProjectState();
        if (state != null) {
            statusLabel.setText("Proyecto: " + state.name() + " | CRS: " + state.crs());
        }
    }

    private static void refreshFromCatgis() {
        if (CatmapSocketClient.isConnected()) {
            syncFromCatgis();
        } else if (CatgisDesktopApp.currentProject != null) {
            statusLabel.setText("Actualizando desde CATGIS...");
        }
        refreshMap();
        statusLabel.setText("Mapa actualizado");
    }

    private static void useCatgisExtent() {
        statusLabel.setText("Usando extent de CATGIS...");
        refreshMap();
    }

    private static void fitToVisibleLayers() {
        statusLabel.setText("Ajustando a capas visibles...");
        refreshMap();
    }

    private static void refreshMap() {
        previewPanel.invalidateRender();
        previewPanel.repaint();
        statusLabel.setText("Mapa actualizado");
    }

    private static void loadLayoutFile(File file) {
        try {
            layoutModel = CatmapSerializer.load(file);
            currentFile = file;
            mainFrame.setTitle("CATMAP - " + file.getName());
            previewPanel = new LayoutPreviewPanel(layoutModel, new LayoutRenderContext(
                    LayoutRenderContext.Mode.PREVIEW, 96, 297, 210));
            previewPanel.setSelectionCallback(element -> updatePropertiesPanel(element));
            // Rebuild preview panel in the center
            Component center = ((JSplitPane) mainFrame.getContentPane().getComponent(0)).getLeftComponent();
            if (center instanceof JSplitPane leftSplit) {
                leftSplit.setRightComponent(new JScrollPane(previewPanel));
            }
            previewPanel.repaint();
            statusLabel.setText("Layout cargado: " + file.getName());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Error al cargar layout:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showShortcuts() {
        JOptionPane.showMessageDialog(mainFrame,
                "Atajos de teclado:\n\n" +
                "Ctrl+N - Nuevo layout\n" +
                "Ctrl+O - Abrir layout\n" +
                "Ctrl+S - Guardar\n" +
                "Ctrl+Z - Deshacer\n" +
                "Ctrl+Y - Rehacer\n" +
                "Ctrl+C - Copiar\n" +
                "Ctrl+V - Pegar\n" +
                "Delete - Eliminar\n" +
                "F5 - Refrescar mapa\n" +
                "Ctrl+P - Imprimir",
                "Atajos de teclado", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showAbout() {
        JOptionPane.showMessageDialog(mainFrame,
                "CATMAP - Cartographic Layout Composer\n" +
                "Version 1.0\n\n" +
                "Parte de CATGIS Desktop\n" +
                "Copyright 2026\n\n" +
                "Herramienta de composición cartográfica\n" +
                "para mapas técnicos y ambientales.",
                "Acerca de CATMAP", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void addDefaultElements(LayoutModel model) {
        int z = 0;

        // Title
        LayoutLabel title = new LayoutLabel("Titulo", "Mapa", 15, 8, 267, 14);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setColor(new Color(0x1A2434));
        title.setZOrder(z++);
        title.setName("Titulo");
        model.addElement(title);

        // Map
        LayoutMap map = new LayoutMap("Mapa principal", 15, 25, 267, 145);
        map.setZOrder(z++);
        map.setName("Mapa principal");
        map.setFrameColor(new Color(0x4A5568));
        map.setFrameWidth(0.8f);
        model.addElement(map);

        // Legend
        LayoutLegend legend = new LayoutLegend("Leyenda", 15, 175, 120, 22);
        legend.setZOrder(z++);
        legend.setAutoHeight(true);
        legend.setName("Leyenda");
        model.addElement(legend);

        // Scale bar
        LayoutScaleBar scale = new LayoutScaleBar("Escala", 145, 175, 95, 10);
        scale.setZOrder(z++);
        scale.setName("Escala");
        model.addElement(scale);

        // North arrow
        LayoutNorthArrow north = new LayoutNorthArrow("Norte", 250, 30, 16, 16);
        north.setZOrder(z++);
        north.setName("Norte");
        model.addElement(north);
    }

    // --- Helper methods ---

    private static void addMenuItem(JMenu menu, String text, int mnemonic, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        if (mnemonic != 0) item.setAccelerator(KeyStroke.getKeyStroke(mnemonic, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        item.addActionListener(action);
        menu.add(item);
    }

    private static void addToolButton(JToolBar toolbar, String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 10f));
        btn.setFocusable(false);
        btn.setMargin(new Insets(4, 6, 4, 6));
        btn.addActionListener(action);
        toolbar.add(btn);
    }

    private static void addToolIconButton(JToolBar toolbar, String tooltip, javax.swing.Icon icon, java.awt.event.ActionListener action) {
        JButton btn = new JButton();
        if (icon != null) btn.setIcon(icon);
        btn.setToolTipText(tooltip);
        btn.setFocusable(false);
        btn.setMargin(new Insets(4, 6, 4, 6));
        btn.setPreferredSize(new Dimension(40, 36));
        btn.addActionListener(action);
        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(0xE0E7EE));
                btn.setBorder(BorderFactory.createLineBorder(new Color(0xB0BEC5), 1));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(UIManager.getColor("Button.background"));
                btn.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("Button.border"));
            }
        });
        toolbar.add(btn);
    }

    private static void installLookAndFeel() {
        try {
            Class<?> themeClass = Class.forName("com.formdev.flatlaf.intellijthemes.FlatArcIJTheme");
            Object laf = themeClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel((javax.swing.LookAndFeel) laf);
        } catch (Throwable ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }
    }
}
