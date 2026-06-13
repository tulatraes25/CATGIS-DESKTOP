package ar.com.catgis.layout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Toolbar for layout operations: add elements, zoom, undo/redo, export.
 */
public class LayoutToolBar extends JToolBar {

    private final LayoutController controller;

    public LayoutToolBar(LayoutController controller) {
        this.controller = controller;
        setFloatable(false);
    }

    public void addButton(String text, String tooltip, Runnable action) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.addActionListener(e -> action.run());
        add(btn);
    }

    public void addSeparator() {
        add(new JToolBar.Separator());
    }

    /**
     * Build the default toolbar with standard layout buttons.
     */
    public static LayoutToolBar createDefault(LayoutController controller) {
        LayoutToolBar tb = new LayoutToolBar(controller);
        tb.addButton("Mapa", "Agregar marco de mapa", controller::addMapFrame);
        tb.addButton("Leyenda", "Agregar leyenda", controller::addLegend);
        tb.addButton("Escala", "Agregar barra de escala", controller::addScaleBar);
        tb.addButton("Norte", "Agregar flecha norte", controller::addNorthArrow);
        tb.addButton("Texto", "Agregar texto", controller::addText);
        tb.addButton("Imagen", "Agregar imagen", controller::addImage);
        tb.addSeparator();
        tb.addButton("Undo", "Deshacer", controller::undo);
        tb.addButton("Redo", "Rehacer", controller::redo);
        tb.addSeparator();
        tb.addButton("Exportar PDF", "Exportar a PDF", controller::exportPdf);
        return tb;
    }
}
