/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.measure.quantity.Area
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package org.saig.jump.widgets.print;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.JFrame;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.dnd.GhostGlassPane;
import org.saig.jump.widgets.print.ElementsViewerDialog;
import org.saig.jump.widgets.print.Page;
import org.saig.jump.widgets.print.PreviewPanel;
import org.saig.jump.widgets.print.PrintLayoutMenuBar;
import org.saig.jump.widgets.print.PrintLayoutPreviewPanel;
import org.saig.jump.widgets.print.PrintLayoutToolBar;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.print.util.GraphicElementNameFactory;
import org.saig.jump.widgets.util.DialogFactory;

public class PrintLayoutFrame
extends JFrame
implements Comparable<PrintLayoutFrame> {
    private static final long serialVersionUID = 1L;
    public static int cont = 0;
    public static final int FULL_PAGE = 0;
    public static final int PAGE_WIDTH = 1;
    public static final int CUSTOM_ZOOM = 3;
    private int activeZoom = 3;
    private PrinterJob printJob = PrinterJob.getPrinterJob();
    private PageFormat pageFormat = this.printJob.defaultPage();
    private PrintLayoutToolBar printLayoutToolBar;
    private PrintLayoutPreviewPanel printLayoutPreviewPanel;
    public GraphicElements graphic;
    private GraphicElements select = null;
    private TaskFrame taskFrame;
    private GraphicElementNameFactory nameFactory;
    private ElementsViewerDialog elementsViewerDialog;
    private String name;
    private String fileName = "";
    private boolean updateGraphicElements = false;
    PreviewPanel preview;
    private PrintLayoutMenuBar printLayoutMenuBar;
    private WindowFocusListener windowsFocusListener;
    public static double LEFT_MARGIN = 0.0;
    public static double RIGHT_MARGIN = 0.0;
    public static double TOP_MARGIN = 0.0;
    public static double BOTTOM_MARGIN = 0.0;

    public PrintLayoutFrame(TaskFrame taskFrame, WorkbenchFrame workbenchFrame) {
        this(taskFrame, workbenchFrame, true);
    }

    public PrintLayoutFrame(TaskFrame taskFrame, WorkbenchFrame workbenchFrame, boolean isVisible) {
        this.taskFrame = taskFrame;
        this.name = String.valueOf(I18N.getString("org.saig.jump.widgets.print.PrintLayoutFrame.map")) + " - " + cont++;
        this.setDefaultCloseOperation(1);
        this.setIconImage(JUMPWorkbench.APP_ICON.getImage());
        this.setName(this.name);
        this.setTitle(this.name);
        this.nameFactory = new GraphicElementNameFactory();
        this.windowsFocusListener = new WindowFocusListener(){

            @Override
            public void windowGainedFocus(WindowEvent e) {
                PrintLayoutFrame.this.updateGraphicElements = true;
                for (GraphicElements element : PrintLayoutFrame.this.getGraphicElements()) {
                    if (!(element instanceof MapFrame)) continue;
                    MapFrame mapFrame = (MapFrame)element;
                    mapFrame.refreshForFocusGained();
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                PrintLayoutFrame.this.updateGraphicElements = false;
                for (GraphicElements element : PrintLayoutFrame.this.getGraphicElements()) {
                    if (!(element instanceof MapFrame)) continue;
                    MapFrame mapFrame = (MapFrame)element;
                    mapFrame.refreshForFocusLost();
                }
            }
        };
        this.addWindowFocusListener(this.windowsFocusListener);
        this.printLayoutMenuBar = new PrintLayoutMenuBar(this);
        this.setJMenuBar(this.printLayoutMenuBar);
        this.printLayoutToolBar = new PrintLayoutToolBar(this);
        this.getContentPane().add((Component)this.printLayoutToolBar, "North");
        this.printLayoutPreviewPanel = new PrintLayoutPreviewPanel(this);
        this.printLayoutPreviewPanel.setSize(new Dimension(875, 675));
        this.preview = new PreviewPanel(this);
        this.printLayoutPreviewPanel.setPreview(this.preview);
        this.getContentPane().add((Component)this.printLayoutPreviewPanel, "Center");
        this.pack();
        this.setGlassPane(new GhostGlassPane());
        GUIUtil.centreOnScreen(this);
        this.setVisible(isVisible);
    }

    @Override
    public void dispose() {
        if (this.preview != null) {
            this.preview.dispose();
            this.preview = null;
        }
        if (this.elementsViewerDialog != null) {
            this.elementsViewerDialog.dispose();
            this.elementsViewerDialog = null;
        }
        if (this.graphic != null) {
            this.graphic.dispose();
            this.graphic = null;
        }
        if (this.printLayoutPreviewPanel != null) {
            this.printLayoutPreviewPanel.dispose();
            this.remove(this.printLayoutPreviewPanel);
            this.printLayoutPreviewPanel = null;
        }
        if (this.select != null) {
            this.select.dispose();
            this.select = null;
        }
        if (this.printLayoutToolBar != null) {
            this.printLayoutToolBar.dispose();
            this.remove(this.printLayoutToolBar);
            this.printLayoutToolBar = null;
        }
        if (this.printLayoutMenuBar != null) {
            this.printLayoutMenuBar.dispose();
            this.remove(this.printLayoutMenuBar);
            this.printLayoutMenuBar = null;
        }
        if (this.windowsFocusListener != null) {
            this.removeWindowFocusListener(this.windowsFocusListener);
            this.windowsFocusListener = null;
        }
        this.taskFrame = null;
        super.dispose();
    }

    public void changeUnits(double factor, Unit<Length> mapLengthUnits, Unit<Length> userLengthUnits, Unit<Area> userAreaUnits) {
        List<GraphicElements> ge = this.getGraphicElements();
        for (GraphicElements o : ge) {
            if (!(o instanceof MapFrame)) continue;
            MapFrame mf = (MapFrame)o;
            mf.changeUnits(factor, mapLengthUnits, userLengthUnits, userAreaUnits);
        }
    }

    public PrintLayoutToolBar getPrintLayoutToolBar() {
        return this.printLayoutToolBar;
    }

    public void orderLayerableElements() {
        ((Page.PageDrawOnScreen)this.getPage().getPageDrawOnScreen()).getCenter().removeAll();
        int i = 0;
        while (i < this.getGraphicElements().size()) {
            ((Page.PageDrawOnScreen)this.getPage().getPageDrawOnScreen()).getCenter().add((Component)this.getGraphicElements().get(i).getGraphicElementsOnScreen(), new Integer(i));
            ++i;
        }
    }

    public float getZoomValue() {
        return this.getPage().getZoomValue();
    }

    public void setZoomValue(float zoomValue) {
        this.getPage().setZoomValue(zoomValue);
    }

    public void setNorthRotation(float f) {
        this.getPage().setNorthRotation(f);
    }

    public float getNorthRotation() {
        return this.getPage().getNorthRotation();
    }

    public ElementsViewerDialog getElementsViewerDialog() {
        return this.elementsViewerDialog;
    }

    public void setElementsViewerDialog(ElementsViewerDialog dialog) {
        this.elementsViewerDialog = dialog;
    }

    public PrintLayoutToolBar getToolBar() {
        return this.printLayoutToolBar;
    }

    public PrinterJob getPrinterJob() {
        return this.printJob;
    }

    public PageFormat getPageFormat() {
        return this.pageFormat;
    }

    public void setPageFormat(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
    }

    public PrintLayoutPreviewPanel getPrintLayoutPreviewPanel() {
        return this.printLayoutPreviewPanel;
    }

    public int getActiveZoom() {
        return this.activeZoom;
    }

    public void setActiveZoom(int zoom) {
        this.activeZoom = zoom;
    }

    public void setGraphic(GraphicElements component) {
        this.graphic = component;
    }

    public GraphicElements getGraphic() {
        return this.graphic;
    }

    public void resetGraphic() {
        this.graphic = null;
    }

    public GraphicElements getSelectedComponent() {
        return this.select;
    }

    public void setSelectedComponent(GraphicElements component) {
        this.select = component;
        if (this.select != null) {
            this.printLayoutToolBar.getDeleteButton().setEnabled(true);
        } else {
            this.printLayoutToolBar.getDeleteButton().setEnabled(false);
        }
    }

    public String createName(Class<? extends GraphicElements> graphicElementClass) {
        return this.nameFactory.generateName(graphicElementClass, this.getGraphicElements());
    }

    public List<GraphicElements> getGraphicElements() {
        return this.preview.getGraphicElements();
    }

    public LayerViewPanel getLayerViewPanel() {
        if (this.taskFrame != null) {
            return this.taskFrame.getLayerViewPanel();
        }
        return null;
    }

    public Page getPage() {
        return this.preview.getPage();
    }

    public void setPage(Page page) {
        page.setParent(this);
        this.preview.setPage(page);
    }

    @Override
    public String toString() {
        return this.getTitle();
    }

    public TaskFrame getTaskFrame() {
        return this.taskFrame;
    }

    public PreviewPanel getPreviewPanel() {
        return this.preview;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getPrintScreenWidthAspectRatio() {
        double solucion = 0.0;
        solucion = this.getPageFormat().getWidth() / (double)((Page.PageDrawOnScreen)this.getPage().getPageDrawOnScreen()).getWidth();
        return solucion;
    }

    public double getPrintScreenHeightAspectRatio() {
        double solucion = 0.0;
        solucion = this.getPageFormat().getHeight() / (double)((Page.PageDrawOnScreen)this.getPage().getPageDrawOnScreen()).getHeight();
        return solucion;
    }

    public MapFrame getMapElement() {
        Object selectedMapFrame;
        MapFrame mapFrame = null;
        ArrayList<MapFrame> availableMapFrames = new ArrayList<MapFrame>();
        for (GraphicElements element : this.getGraphicElements()) {
            if (!(element instanceof MapFrame)) continue;
            availableMapFrames.add((MapFrame)element);
        }
        if (availableMapFrames.size() == 1) {
            mapFrame = (MapFrame)availableMapFrames.get(0);
        } else if (availableMapFrames.size() > 1 && (selectedMapFrame = DialogFactory.showSelectionDialog(this, I18N.getString("org.saig.jump.widgets.print.PrintLayoutFrame.select-which-view-you-want-to-associate-the-scale-to"), I18N.getString("org.saig.jump.widgets.print.PrintLayoutFrame.select-view"), availableMapFrames.toArray(), availableMapFrames.get(0))) != null) {
            mapFrame = (MapFrame)selectedMapFrame;
        }
        return mapFrame;
    }

    public void setMapElement(MapFrame mapFrame) {
        boolean enc = false;
        int i = 0;
        List<GraphicElements> graphicElements = this.getGraphicElements();
        i = 0;
        while (i < graphicElements.size() && !enc) {
            GraphicElements element = graphicElements.get(i);
            if (element instanceof MapFrame) {
                enc = true;
                graphicElements.remove(i);
                graphicElements.add(i, mapFrame);
            }
            ++i;
        }
    }

    public List<MapFrame> getAllMapElements() {
        ArrayList<MapFrame> availableMapElements = new ArrayList<MapFrame>();
        for (GraphicElements element : this.getGraphicElements()) {
            if (!(element instanceof MapFrame)) continue;
            availableMapElements.add((MapFrame)element);
        }
        return availableMapElements;
    }

    public MapFrame getMapFrameByName(String name) {
        for (GraphicElements element : this.getGraphicElements()) {
            MapFrame candidate;
            if (!(element instanceof MapFrame) || !(candidate = (MapFrame)element).getName().equals(name)) continue;
            return candidate;
        }
        return null;
    }

    public boolean isUpdateGraphicElements() {
        return this.updateGraphicElements;
    }

    @Override
    public int compareTo(PrintLayoutFrame o) {
        Collator col = Collator.getInstance(I18N.getLocale());
        return col.compare(this.getName(), o.getName());
    }
}

