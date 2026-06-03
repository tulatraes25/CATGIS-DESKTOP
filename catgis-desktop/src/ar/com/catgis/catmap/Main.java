package ar.com.catgis.catmap;

import ar.com.catgis.*;
import ar.com.catgis.layout.*;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Standalone entry point for CATMAP - Cartographic Layout Composer.
 * Can run independently or be launched from CATGIS Desktop.
 */
public class Main {

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

            // Create and show composer
            LayoutContext ctx = LayoutContext.fromCatgis();
            JFrame frame = createStandaloneFrame(ctx);
            frame.setVisible(true);

            // Load layout if specified
            if (layoutPath != null) {
                // TODO: load .catmap file into the composer
                frame.setTitle("CATMAP - " + layoutPath);
            }
        });
    }

    private static JFrame createStandaloneFrame(LayoutContext ctx) {
        JFrame frame = new JFrame("CATMAP - Cartographic Layout Composer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create the composer panel
        LayoutModel model = new LayoutModel();

        // Add default elements if project exists
        if (ctx.hasProject()) {
            addDefaultElements(model, ctx);
        }

        // Create preview panel
        LayoutRenderContext renderCtx = new LayoutRenderContext(
                LayoutRenderContext.Mode.PREVIEW, 96, 297, 210);
        LayoutPreviewPanel preview = new LayoutPreviewPanel(model, renderCtx);

        frame.add(new JScrollPane(preview), BorderLayout.CENTER);

        // Status bar
        JLabel status = new JLabel("Listo");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        frame.add(status, BorderLayout.SOUTH);

        // Menu bar
        frame.setJMenuBar(createMenuBar(frame, model, ctx, status));

        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private static void addDefaultElements(LayoutModel model, LayoutContext ctx) {
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
    }

    private static JMenuBar createMenuBar(JFrame frame, LayoutModel model,
                                           LayoutContext ctx, JLabel status) {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("Archivo");
        JMenuItem newItem = new JMenuItem("Nuevo layout");
        JMenuItem openItem = new JMenuItem("Abrir layout...");
        JMenuItem saveItem = new JMenuItem("Guardar layout");
        JMenuItem exportPdf = new JMenuItem("Exportar PDF");
        JMenuItem exportImg = new JMenuItem("Exportar imagen");
        JMenuItem exitItem = new JMenuItem("Salir");

        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exportPdf);
        fileMenu.add(exportImg);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // View menu
        JMenu viewMenu = new JMenu("Vista");
        JMenuItem zoomIn = new JMenuItem("Acercar");
        JMenuItem zoomOut = new JMenuItem("Alejar");
        JMenuItem fitPage = new JMenuItem("Ajustar a pagina");
        viewMenu.add(zoomIn);
        viewMenu.add(zoomOut);
        viewMenu.add(fitPage);
        menuBar.add(viewMenu);

        // Insert menu
        JMenu insertMenu = new JMenu("Insertar");
        JMenuItem addMap = new JMenuItem("Mapa");
        JMenuItem addLegend = new JMenuItem("Leyenda");
        JMenuItem addScale = new JMenuItem("Escala grafica");
        JMenuItem addNorth = new JMenuItem("Norte");
        JMenuItem addText = new JMenuItem("Texto");
        insertMenu.add(addMap);
        insertMenu.add(addLegend);
        insertMenu.add(addScale);
        insertMenu.add(addNorth);
        insertMenu.add(addText);
        menuBar.add(insertMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Ayuda");
        JMenuItem aboutItem = new JMenuItem("Acerca de CATMAP");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "CATMAP - Cartographic Layout Composer\nVersion 1.0\n\nParte de CATGIS Desktop",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        return menuBar;
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
