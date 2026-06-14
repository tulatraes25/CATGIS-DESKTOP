package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EditingToolsWindow extends JDialog {

    private static EditingToolsWindow openInstance;
    private final JLabel statusLabel;

    public static void showWindow() {
        if (openInstance != null && openInstance.isDisplayable()) {
            openInstance.toFront();
            openInstance.requestFocus();
            return;
        }
        Window owner = CatgisDesktopApp.getMainFrameSafe();
        openInstance = new EditingToolsWindow(owner);
        openInstance.setLocationRelativeTo(owner);
        openInstance.setVisible(true);
    }

    public static void hideIfOpen() {
        if (openInstance != null) {
            openInstance.setVisible(false);
            openInstance.dispose();
            openInstance = null;
        }
    }

    public static boolean isOpen() {
        return openInstance != null && openInstance.isDisplayable();
    }

    public static void saveAndClose() {
        if (openInstance != null) openInstance.doSaveAndClose();
    }

    public static void cancelEditing() {
        if (openInstance != null) openInstance.doCancelEditing();
    }

    private static Layer getEditingLayer() {
        MapPanel map = AppContext.mapPanel();
        return map != null ? map.getEditingLayerRef() : null;
    }

    private static boolean hasUnsavedChanges() {
        MapPanel map = AppContext.mapPanel();
        return map != null && map.canUndoFeatureEdit();
    }

    private EditingToolsWindow(Window owner) {
        super(owner instanceof javax.swing.JFrame ? (javax.swing.JFrame) owner : null, "Herramientas de edicion", ModalityType.MODELESS);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                handleClose();
            }
            public void windowClosed(WindowEvent e) { openInstance = null; }
        });

        statusLabel = new JLabel(" ", SwingConstants.LEFT);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 10f));
        statusLabel.setForeground(new Color(0x888888));

        getContentPane().add(buildContent(), BorderLayout.CENTER);
        getContentPane().add(statusLabel, BorderLayout.SOUTH);
        pack();
        setResizable(true);
        updateStatus();
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(6, 4));
        content.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        content.setBackground(new Color(0xF7F8FA));

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tools.setOpaque(false);
        MapPanel map = AppContext.mapPanel();

        addGroup(tools, "Seleccion",
                btn("Seleccionar", AppIcons.selectIcon(), () -> { if (map != null) map.enableSelectMode(); }),
                btn("Mover entidad", AppIcons.moveFeatureIcon(), () -> FloatingVectorEditToolbar.triggerMoveFeature()),
                btn("Identificar", AppIcons.identifyIcon(), () -> { if (map != null) map.enableIdentifyMode(); })
        );
        addSep(tools);

        addGroup(tools, "Crear",
                btn("Punto", AppIcons.pointIcon(), () -> FloatingVectorEditToolbar.triggerDrawPoint()),
                btn("Multipunto", AppIcons.multiPointIcon(), () -> FloatingVectorEditToolbar.triggerDrawMultiPoint()),
                btn("Linea", AppIcons.lineIcon(), () -> FloatingVectorEditToolbar.triggerDrawLine()),
                btn("Rectangulo", AppIcons.rectangleIcon(), () -> FloatingVectorEditToolbar.triggerDrawRectangle()),
                btn("Poligono", AppIcons.polygonIcon(), () -> FloatingVectorEditToolbar.triggerDrawPolygon())
        );
        addSep(tools);

        addGroup(tools, "Vertices",
                btn("Mover", AppIcons.moveVertexIcon(), () -> FloatingVectorEditToolbar.triggerMoveVertex()),
                btn("Agregar", AppIcons.addVertexIcon(), () -> FloatingVectorEditToolbar.triggerAddVertex()),
                btn("Eliminar", AppIcons.removeVertexIcon(), () -> FloatingVectorEditToolbar.triggerRemoveVertex()),
                btn("Unir", AppIcons.joinVerticesIcon(), () -> FloatingVectorEditToolbar.triggerJoinVertices())
        );
        addSep(tools);

        addGroup(tools, "Modificar",
                btn("Cortar", AppIcons.cutIcon(), () -> FloatingVectorEditToolbar.triggerCut()),
                btn("Borrar", AppIcons.removeIcon(), () -> FloatingVectorEditToolbar.triggerDeleteSelection()),
                btn("Deshacer", AppIcons.undoIcon(), () -> { if (map != null) map.undoFeatureEdit(); }),
                btn("Rehacer", AppIcons.redoIcon(), () -> { if (map != null) map.redoFeatureEdit(); })
        );

        content.add(tools, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        JButton saveCloseBtn = new JButton("Guardar y cerrar", AppIcons.saveIcon());
        saveCloseBtn.setFont(saveCloseBtn.getFont().deriveFont(Font.BOLD, 11f));
        saveCloseBtn.setMargin(new Insets(4, 12, 4, 12));
        saveCloseBtn.addActionListener(e -> doSaveAndClose());

        JButton cancelBtn = new JButton("Cancelar edicion", AppIcons.cancelIcon());
        cancelBtn.setFont(cancelBtn.getFont().deriveFont(Font.PLAIN, 11f));
        cancelBtn.setMargin(new Insets(4, 12, 4, 12));
        cancelBtn.addActionListener(e -> cancelEditing());

        actions.add(cancelBtn);
        actions.add(saveCloseBtn);
        content.add(actions, BorderLayout.SOUTH);

        return content;
    }

    private void addGroup(JPanel panel, String title, JButton... buttons) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 9f));
        lbl.setForeground(new Color(0x999999));
        lbl.setPreferredSize(new Dimension(48, 12));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl);
        for (JButton b : buttons) {
            if (b != null) panel.add(b);
        }
        panel.add(Box.createHorizontalStrut(6));
    }

    private void addSep(JPanel strip) {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 24));
        sep.setForeground(new Color(0xCCCCCC));
        sep.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        strip.add(sep);
    }

    private JButton btn(String tip, javax.swing.Icon icon, Runnable action) {
        JButton b = new JButton(icon);
        b.setToolTipText(tip);
        b.setFocusable(false);
        b.setMargin(new Insets(2, 2, 2, 2));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setPreferredSize(new Dimension(32, 32));
        b.addActionListener(e -> {
            action.run();
            statusLabel.setText("  Herramienta activa: " + tip);
        });
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setOpaque(true); b.setBackground(new Color(0xE0E0E0)); }
            public void mouseExited(MouseEvent e) { b.setOpaque(false); b.repaint(); }
        });
        return b;
    }

    public void doSaveAndClose() {
        MapPanel map = AppContext.mapPanel();
        Layer layer = getEditingLayer();
        String layerName = layer != null ? layer.getName() : "desconocida";

        if (map != null) {
            map.finishFeatureEdit();
        }
        hideIfOpen();
        if (AppContext.mapPanel() != null) {
            AppContext.mapPanel().enablePanMode();
        }
        updateStatusBar("Edicion guardada: " + layerName);
    }

    public void doCancelEditing() {
        Layer layer = getEditingLayer();
        String layerName = layer != null ? layer.getName() : "desconocida";

        if (hasUnsavedChanges()) {
            boolean discard = NotificationManager.confirm(
                    this,
                    "Cancelar edicion",
                    "Hay cambios sin guardar en " + layerName + ".\nDesea descartarlos?");
            if (!discard) {
                return;
            }
        }

        MapPanel map = AppContext.mapPanel();
        if (map != null) {
            map.cancelFeatureEdit();
            map.enablePanMode();
        }
        hideIfOpen();
        updateStatusBar("Edicion cancelada: " + layerName);
    }

    private void handleClose() {
        Layer layer = getEditingLayer();
        String layerName = layer != null ? layer.getName() : "desconocida";

        if (hasUnsavedChanges()) {
            String[] options = {"Guardar y cerrar", "Descartar", "Cancelar"};
            int result = JOptionPane.showOptionDialog(
                    this,
                    "Hay cambios sin guardar en " + layerName + ".\nQue desea hacer?",
                    "Cerrar edicion",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (result == 0) {
                saveAndClose();
            } else if (result == 1) {
                cancelWithoutPrompt();
            }
            return;
        }
        cancelWithoutPrompt();
    }

    private void cancelWithoutPrompt() {
        MapPanel map = AppContext.mapPanel();
        if (map != null) {
            map.cancelFeatureEdit();
            map.enablePanMode();
        }
        hideIfOpen();
    }

    private void updateStatus() {
        Layer layer = getEditingLayer();
        if (layer != null) {
            statusLabel.setText("  Editando: " + layer.getName());
        }
    }

    private void updateStatusBar(String msg) {
        if (CatgisDesktopApp.statusBar != null) {
            AppContext.setStatusMessage(msg);
        }
    }
}
