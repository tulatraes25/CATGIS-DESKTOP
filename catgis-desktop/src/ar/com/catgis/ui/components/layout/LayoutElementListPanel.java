package ar.com.catgis.ui.components.layout;

import ar.com.catgis.layout.LayoutElement;
import ar.com.catgis.layout.LayoutModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

/**
 * Element list panel (left sidebar) with add, organize, state controls.
 * Extracted from {@code buildElementListPanel()}, {@code wrapSection()}, {@code menuItem()}, and {@code miniBtn()}.
 */
public class LayoutElementListPanel extends JPanel {

    private final DefaultListModel<String> elementListModel;
    private final LayoutModel layoutModel;
    private final Runnable refreshPropertiesPanel;
    private final Runnable repaintPreview;
    private final Consumer<LayoutElement> centerOnElement;

    private static JButton miniBtn(String text, String tip, java.awt.event.ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 10f));
        b.setMargin(new Insets(2, 6, 2, 6));
        b.setToolTipText(tip);
        b.addActionListener(al);
        b.setFocusPainted(false);
        return b;
    }

    private static JPanel wrapSection(JLabel title, JPanel content) {
        JPanel p = new JPanel(new BorderLayout(0, 1));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        p.add(title, BorderLayout.NORTH);
        p.add(content, BorderLayout.SOUTH);
        return p;
    }

    private static JMenuItem menuItem(String text, Runnable action) {
        JMenuItem mi = new JMenuItem(text);
        mi.addActionListener(e -> action.run());
        return mi;
    }

    private static JMenuItem menuItem(String text, String type, Consumer<String> onAddQuickElement) {
        JMenuItem mi = new JMenuItem(text);
        mi.addActionListener(e -> onAddQuickElement.accept(type));
        return mi;
    }

    private static String extractNameFromDisplay(String display) {
        if (display == null) return "";
        String s = display.replaceAll("^\u25C9 |^\u25CB |\uD83D\uDD12 |\uD83D\uDD13 |^\\> |^  ", "");
        s = s.replaceAll("\\s*\\(oculto\\)|\\s*\\(bloq\\)", "");
        return s.trim();
    }

    public LayoutElementListPanel(
            LayoutModel layoutModel,
            Runnable refreshElementListCallback,
            Runnable refreshPropertiesPanel,
            Runnable repaintPreview,
            Consumer<LayoutElement> centerOnElement,
            Consumer<String> onAddQuickElement,
            Runnable onShowTemplates,
            Runnable onUndo,
            Runnable onRedo,
            Runnable onDuplicateSelected,
            Runnable onDeleteSelected,
            Consumer<Integer> onAlignElements) {
        super(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setBackground(new Color(0xF7F8FA));
        setPreferredSize(new Dimension(185, 100));

        this.layoutModel = layoutModel;
        this.refreshPropertiesPanel = refreshPropertiesPanel;
        this.repaintPreview = repaintPreview;
        this.centerOnElement = centerOnElement;

        JLabel hdr = new JLabel("Elementos del layout");
        hdr.setFont(hdr.getFont().deriveFont(Font.BOLD, 11f));
        hdr.setForeground(new Color(0x333333));

        elementListModel = new DefaultListModel<>();
        JList<String> list = new JList<>(elementListModel);
        list.setFont(list.getFont().deriveFont(Font.PLAIN, 11f));
        list.setFixedCellHeight(26);
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list.setSelectionBackground(new Color(0xDBEAFE));
        list.setSelectionForeground(new Color(0x1E3A5F));
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = list.getSelectedValue();
                if (sel == null) return;
                layoutModel.clearSelection();
                for (LayoutElement el : layoutModel.getElements()) {
                    if (el.getName().equals(extractNameFromDisplay(sel))) {
                        el.setSelected(true);
                        break;
                    }
                }
                refreshPropertiesPanel.run();
                repaintPreview.run();
            }
        });
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String sel = list.getSelectedValue();
                    if (sel == null) return;
                    for (LayoutElement el : layoutModel.getElements()) {
                        if (el.getName().equals(extractNameFromDisplay(sel))) {
                            layoutModel.clearSelection();
                            el.setSelected(true);
                            centerOnElement.accept(el);
                            refreshPropertiesPanel.run();
                            repaintPreview.run();
                            break;
                        }
                    }
                }
            }
        });

        // Section: Agregar
        JLabel addSec = new JLabel("Agregar");
        addSec.setFont(addSec.getFont().deriveFont(Font.BOLD, 9f));
        addSec.setForeground(new Color(0x1976D2));

        JPopupMenu addMenu = new JPopupMenu();
        addMenu.add(menuItem("Mapa", "map", onAddQuickElement));
        addMenu.add(menuItem("Leyenda", "legend", onAddQuickElement));
        addMenu.add(menuItem("Escala grafica", "scale", onAddQuickElement));
        addMenu.add(menuItem("Norte", "north", onAddQuickElement));
        addMenu.add(menuItem("Texto", "text", onAddQuickElement));
        addMenu.add(menuItem("Imagen / Logo", "image", onAddQuickElement));
        addMenu.add(menuItem("Cartucho", "cartouche", onAddQuickElement));
        addMenu.add(menuItem("Grilla coord.", "graticule", onAddQuickElement));
        addMenu.addSeparator();
        addMenu.add(menuItem("Rectangulo", "rect", onAddQuickElement));
        addMenu.add(menuItem("Tabla (CSV)", "table", onAddQuickElement));

        JButton addBtn = new JButton("+ Elemento \u25BE");
        addBtn.setFont(addBtn.getFont().deriveFont(Font.PLAIN, 10f));
        addBtn.setMargin(new Insets(3, 8, 3, 8));
        addBtn.addActionListener(e -> addMenu.show(addBtn, 0, addBtn.getHeight()));

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        actionBar.setOpaque(false);
        actionBar.add(addBtn);
        JButton tmplBtn = new JButton("Plantillas...");
        tmplBtn.setFont(tmplBtn.getFont().deriveFont(Font.PLAIN, 10f));
        tmplBtn.setMargin(new Insets(3, 6, 3, 6));
        tmplBtn.setToolTipText("Abrir galeria de plantillas con vista preliminar.");
        tmplBtn.addActionListener(e -> onShowTemplates.run());
        actionBar.add(tmplBtn);
        actionBar.add(miniBtn("Duplicar", "Duplicar seleccionado (Ctrl+D)", e -> onDuplicateSelected.run()));
        JButton delBtn2 = miniBtn("Eliminar", "Eliminar seleccionado (Supr)", e -> onDeleteSelected.run());
        delBtn2.setForeground(new Color(0xCC3333));
        actionBar.add(delBtn2);
        actionBar.add(Box.createHorizontalStrut(2));
        actionBar.add(miniBtn("\u21A9", "Deshacer (Ctrl+Z)", e -> onUndo.run()));
        actionBar.add(miniBtn("\u21AA", "Rehacer (Ctrl+Y)", e -> onRedo.run()));

        // Section: Organizar
        JLabel orgSec = new JLabel("Organizar");
        orgSec.setFont(orgSec.getFont().deriveFont(Font.BOLD, 9f));
        orgSec.setForeground(new Color(0x1976D2));

        JPopupMenu alignMenu = new JPopupMenu();
        String[] alignLabels = {"Izquierda", "Centro horizontal", "Derecha", "Arriba", "Medio vertical", "Abajo"};
        for (int i = 0; i < 6; i++) {
            final int mode = i;
            alignMenu.add(menuItem(alignLabels[i], () -> onAlignElements.accept(mode)));
        }
        JButton alignBtn = new JButton("Alinear \u25BE");
        alignBtn.setFont(alignBtn.getFont().deriveFont(Font.PLAIN, 10f));
        alignBtn.setMargin(new Insets(3, 6, 3, 6));
        alignBtn.addActionListener(e -> alignMenu.show(alignBtn, 0, alignBtn.getHeight()));

        JPopupMenu orderMenu = new JPopupMenu();
        orderMenu.add(menuItem("Traer al frente", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.moveToFront(sel); refreshElementListCallback.run(); repaintPreview.run(); }
        }));
        orderMenu.add(menuItem("Enviar atras", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.moveToBack(sel); refreshElementListCallback.run(); repaintPreview.run(); }
        }));
        orderMenu.add(menuItem("Subir uno", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.moveUp(sel); refreshElementListCallback.run(); repaintPreview.run(); }
        }));
        orderMenu.add(menuItem("Bajar uno", () -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { layoutModel.moveDown(sel); refreshElementListCallback.run(); repaintPreview.run(); }
        }));
        JButton orderBtn = new JButton("Orden \u25BE");
        orderBtn.setFont(orderBtn.getFont().deriveFont(Font.PLAIN, 10f));
        orderBtn.setMargin(new Insets(3, 6, 3, 6));
        orderBtn.addActionListener(e -> orderMenu.show(orderBtn, 0, orderBtn.getHeight()));

        JPanel orgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        orgRow.setOpaque(false);
        orgRow.add(alignBtn);
        orgRow.add(orderBtn);

        // Section: Estado
        JLabel stSec = new JLabel("Estado");
        stSec.setFont(stSec.getFont().deriveFont(Font.BOLD, 9f));
        stSec.setForeground(new Color(0x1976D2));

        JPanel toggleBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        toggleBar.setOpaque(false);
        toggleBar.add(miniBtn("Visible", "Mostrar/Ocultar seleccionado", e -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { sel.setVisible(!sel.isVisible()); refreshElementListCallback.run(); repaintPreview.run(); }
        }));
        toggleBar.add(miniBtn("Bloquear", "Bloquear/Desbloquear seleccionado", e -> {
            LayoutElement sel = layoutModel.getSelected();
            if (sel != null) { sel.setLocked(!sel.isLocked()); refreshElementListCallback.run(); repaintPreview.run(); }
        }));

        // Assemble top area with sections
        JPanel sectionsPanel = new JPanel();
        sectionsPanel.setLayout(new BoxLayout(sectionsPanel, BoxLayout.Y_AXIS));
        sectionsPanel.setOpaque(false);
        JPanel addPanel = wrapSection(addSec, actionBar);
        JPanel orgPanel = wrapSection(orgSec, orgRow);
        JPanel stPanel = wrapSection(stSec, toggleBar);
        sectionsPanel.add(addPanel);
        sectionsPanel.add(Box.createVerticalStrut(2));
        sectionsPanel.add(orgPanel);
        sectionsPanel.add(Box.createVerticalStrut(2));
        sectionsPanel.add(stPanel);

        JPanel northWrap = new JPanel(new BorderLayout(0, 3));
        northWrap.setOpaque(false);
        northWrap.add(hdr, BorderLayout.NORTH);
        northWrap.add(sectionsPanel, BorderLayout.SOUTH);

        add(northWrap, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE0E0E0)));
        add(sp, BorderLayout.CENTER);
    }

    public DefaultListModel<String> getElementListModel() {
        return elementListModel;
    }

    public void refreshElementList() {
        if (elementListModel == null) return;
        elementListModel.clear();
        List<LayoutElement> elems = new ArrayList<>(layoutModel.getElements());
        java.util.Collections.reverse(elems);
        for (LayoutElement el : elems) {
            String icon = getTypeIcon(el);
            String visDot = el.isVisible() ? "\u25C9" : "\u25CB";
            String lockIcon = el.isLocked() ? " \uD83D\uDD12" : " \uD83D\uDD13";
            String selPrefix = el.isSelected() ? "> " : "  ";
            String atenuado = el.isVisible() ? "" : " (oculto)";
            elementListModel.addElement(selPrefix + visDot + " " + icon + " " + el.getName() + lockIcon + atenuado);
        }
    }

    private static String getTypeIcon(LayoutElement el) {
        if (el instanceof ar.com.catgis.layout.LayoutMap) return "\uD83D\uDDFA";
        if (el instanceof ar.com.catgis.layout.LayoutLegend) return "\uD83D\uDCCB";
        if (el instanceof ar.com.catgis.layout.LayoutNorthArrow) return "\uD83E\uDDED";
        if (el instanceof ar.com.catgis.layout.LayoutScaleBar) return "\uD83D\uDCCF";
        if (el instanceof ar.com.catgis.layout.LayoutImage) return "\uD83D\uDDBC";
        if (el instanceof ar.com.catgis.layout.LayoutEllipse) return "\u2B55";
        if (el instanceof ar.com.catgis.layout.LayoutLine) return "\u2795";
        if (el instanceof ar.com.catgis.layout.LayoutRectangle) return "\u25AD";
        if (el instanceof ar.com.catgis.layout.LayoutTable) return "\uD83D\uDCCA";
        if (el instanceof ar.com.catgis.layout.LayoutCartouche) return "\uD83D\uDCC4";
        if (el instanceof ar.com.catgis.layout.LayoutGraticule) return "\uD83D\uDCC8";
        return "\uD83D\uDCC4";
    }
}
