/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.jump.widgets.print;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.saig.core.renderer.print.PrintRenderer;
import org.saig.jump.widgets.dnd.DragAndDropLock;
import org.saig.jump.widgets.dnd.GhostGlassPane;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.dnd.GraphicElementTransferHandler;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.map.MapFrame;
import org.saig.jump.widgets.print.util.PrintWaitDialog;

public class Page {
    private PrintLayoutFrame parent;
    private int width;
    private int height;
    private int widthWithoutMargin;
    private int heightWithoutMargin;
    private int top;
    private int left;
    private static int orderCounter = 0;
    private PageForPrint print;
    private PageDrawOnScreen drawOnScreen;
    private List<GraphicElements> graphicElements = new ArrayList<GraphicElements>();
    private float zoomValue = 1.0f;
    private float northRotation = 0.0f;
    private PageFormat pageFormat = null;
    private int activeZoom = 0;
    private String name = "";
    private String taskName = "";
    private int sizeSelection = 0;
    private List<Envelope> envelopes;
    private List<String> pageLabels;

    public Page() {
    }

    public Page(PrintLayoutFrame plf) {
        this.parent = plf;
        this.initPage();
    }

    public JLayeredPane getPageForPrint() {
        return this.print;
    }

    public JPanel getPageDrawOnScreen() {
        return this.drawOnScreen;
    }

    public void posGraphicElement(PrintLayoutFrame plf) {
        this.drawOnScreen.posGraphicElement(plf);
    }

    public void setEnvelopesAndLabels(List<Envelope> envelopes, List<String> labels) {
        if (labels == null || envelopes.size() == labels.size()) {
            this.envelopes = envelopes;
            this.pageLabels = labels;
        }
    }

    public void dispose() {
        this.parent = null;
        if (this.graphicElements != null) {
            int i = 0;
            while (i < this.graphicElements.size()) {
                this.graphicElements.get(i).dispose();
                ++i;
            }
            this.graphicElements.clear();
            this.graphicElements = null;
        }
        this.print = null;
        if (this.drawOnScreen != null) {
            this.drawOnScreen.dispose();
            this.drawOnScreen = null;
        }
    }

    public void setSize(PageFormat pf) {
        this.width = (int)pf.getWidth();
        this.height = (int)pf.getHeight();
        this.widthWithoutMargin = (int)pf.getImageableWidth();
        this.heightWithoutMargin = (int)pf.getImageableHeight();
        this.top = (int)pf.getImageableY();
        this.left = (int)pf.getImageableX();
        if (pf.getOrientation() == 0) {
            this.top = (int)((double)(this.width - this.widthWithoutMargin) - pf.getImageableX());
            this.left = (int)pf.getImageableY();
        }
    }

    public void resize(PageFormat pf) {
        this.setSize(pf);
        this.drawOnScreen.resize();
        int i = 0;
        while (i < this.graphicElements.size()) {
            if (this.sizeSelection == 0) {
                this.graphicElements.get(i).resize(this.widthWithoutMargin, this.print.getWidth(), this.heightWithoutMargin, this.print.getHeight(), this.drawOnScreen.getCenter().getWidth(), this.drawOnScreen.getCenter().getHeight());
            } else {
                this.graphicElements.get(i).repaint();
            }
            ++i;
        }
        this.print.resize();
    }

    public void setZoomValue(float f) {
        this.zoomValue = f;
    }

    public float getZoomValue() {
        return this.zoomValue;
    }

    public void setNorthRotation(float f) {
        this.northRotation = f;
    }

    public float getNorthRotation() {
        return this.northRotation;
    }

    public void zoom() {
        this.drawOnScreen.resize();
        if (this.parent.getActiveZoom() == 3) {
            float f = this.parent.getZoomValue();
            int i = 0;
            while (i < this.graphicElements.size()) {
                this.graphicElements.get(i).zoom(f);
                ++i;
            }
        } else {
            int i = 0;
            while (i < this.graphicElements.size()) {
                this.graphicElements.get(i).zoom(this.widthWithoutMargin, this.print.getWidth(), this.heightWithoutMargin, this.print.getHeight(), this.drawOnScreen.getCenter().getWidth(), this.drawOnScreen.getCenter().getHeight());
                ++i;
            }
        }
    }

    public void remove(GraphicElements ge) {
        this.graphicElements.remove(ge);
        this.drawOnScreen.remove(ge);
        this.print.remove(ge);
        ge.dispose();
        if (this.parent.getElementsViewerDialog() != null) {
            this.parent.getElementsViewerDialog().contentsChanged();
        }
    }

    public List<GraphicElements> getGraphicElements() {
        return this.graphicElements;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int i) {
        this.width = i;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int i) {
        this.height = i;
    }

    public int getWidthWithoutMargin() {
        return this.widthWithoutMargin;
    }

    public void setWidthWithoutMargin(int i) {
        this.widthWithoutMargin = i;
    }

    public int getHeightWithoutMargin() {
        return this.heightWithoutMargin;
    }

    public void setHeightWithoutMargin(int i) {
        this.heightWithoutMargin = i;
    }

    public int getTop() {
        return this.top;
    }

    public void setTop(int i) {
        this.top = i;
    }

    public int getLeft() {
        return this.left;
    }

    public void setLeft(int i) {
        this.left = i;
    }

    public List<GraphicElements> getGraphicElement() {
        return this.graphicElements;
    }

    public void addGraphicElement(GraphicElements elem) {
        this.graphicElements.add(elem);
    }

    public void initPage() {
        this.setSize(this.parent.getPageFormat());
        this.print = new PageForPrint();
        this.drawOnScreen = new PageDrawOnScreen();
    }

    public void setParent(PrintLayoutFrame plf) {
        this.parent = plf;
    }

    public PageFormat getPageFormat() {
        return this.pageFormat;
    }

    public void setPageFormat(PageFormat pf) {
        this.pageFormat = pf;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getActiveZoom() {
        return this.activeZoom;
    }

    public void setActiveZoom(int activeZoom) {
        this.activeZoom = activeZoom;
    }

    public int getSizeSelection() {
        return this.sizeSelection;
    }

    public void setSizeSelection(int sizeSelection) {
        this.sizeSelection = sizeSelection;
    }

    public class PageDrawOnScreen
    extends JPanel {
        private static final long serialVersionUID = 1L;
        private Marge topMargin = new Marge();
        private Marge leftMargin = new Marge();
        private Marge bottomMargin = new Marge();
        private Marge rightMargin = new Marge();
        private PrintableZone center;

        public PageDrawOnScreen() {
            this.center = new PrintableZone();
            this.setBackground(Color.WHITE);
            this.resize();
        }

        public void resize() {
            this.removeAll();
            block0 : switch (Page.this.parent.getActiveZoom()) {
                case 0: {
                    switch (Page.this.parent.getPageFormat().getOrientation()) {
                        case 1: {
                            this.topMargin.setPreferredSize(new Dimension(Page.this.width * 600 / Page.this.height, Page.this.top * 600 / Page.this.height));
                            this.leftMargin.setPreferredSize(new Dimension(Page.this.left * 600 / Page.this.height, Page.this.heightWithoutMargin * 600 / Page.this.height));
                            this.center.reSize(600, Page.this.height);
                            this.rightMargin.setPreferredSize(new Dimension((Page.this.width - (Page.this.left + Page.this.widthWithoutMargin)) * 600 / Page.this.height, Page.this.heightWithoutMargin * 600 / Page.this.height));
                            this.bottomMargin.setPreferredSize(new Dimension(Page.this.width * 600 / Page.this.height, (Page.this.height - (Page.this.top + Page.this.heightWithoutMargin)) * 600 / Page.this.height));
                            super.setPreferredSize(new Dimension(Page.this.width * 600 / Page.this.height, 600));
                            break;
                        }
                        case 0: {
                            this.topMargin.setPreferredSize(new Dimension(800, Page.this.top * 800 / Page.this.width));
                            this.leftMargin.setPreferredSize(new Dimension(Page.this.left * 800 / Page.this.width, Page.this.heightWithoutMargin * 800 / Page.this.width));
                            this.center.reSize(800, Page.this.width);
                            this.rightMargin.setPreferredSize(new Dimension((Page.this.height - Page.this.left - Page.this.heightWithoutMargin) * 800 / Page.this.width, Page.this.heightWithoutMargin * 800 / Page.this.width));
                            this.bottomMargin.setPreferredSize(new Dimension(800, (Page.this.width - Page.this.top - Page.this.widthWithoutMargin) * 800 / Page.this.width));
                            super.setPreferredSize(new Dimension(800, Page.this.height * 800 / Page.this.width));
                        }
                    }
                    break;
                }
                case 1: {
                    switch (Page.this.parent.getPageFormat().getOrientation()) {
                        case 1: {
                            this.setPreferredSize(new Dimension(Page.this.width, Page.this.height));
                            this.topMargin.setPreferredSize(new Dimension(Page.this.width, Page.this.top));
                            this.leftMargin.setPreferredSize(new Dimension(Page.this.left, Page.this.heightWithoutMargin));
                            this.center.reSize(Page.this.widthWithoutMargin, Page.this.heightWithoutMargin);
                            this.rightMargin.setPreferredSize(new Dimension(Page.this.width - Page.this.widthWithoutMargin - Page.this.left, Page.this.heightWithoutMargin));
                            this.bottomMargin.setPreferredSize(new Dimension(Page.this.width, Page.this.height - Page.this.top - Page.this.heightWithoutMargin));
                            break;
                        }
                        case 0: {
                            this.setPreferredSize(new Dimension(Page.this.width, Page.this.height));
                            this.topMargin.setPreferredSize(new Dimension(Page.this.width, Page.this.top));
                            this.leftMargin.setPreferredSize(new Dimension(Page.this.left, Page.this.heightWithoutMargin));
                            this.center.reSize(Page.this.widthWithoutMargin, Page.this.heightWithoutMargin);
                            this.rightMargin.setPreferredSize(new Dimension(Page.this.height - Page.this.heightWithoutMargin - Page.this.left, Page.this.heightWithoutMargin));
                            this.bottomMargin.setPreferredSize(new Dimension(Page.this.width, Page.this.width - Page.this.top - Page.this.widthWithoutMargin));
                        }
                    }
                    break;
                }
                case 3: {
                    switch (Page.this.parent.getPageFormat().getOrientation()) {
                        case 1: {
                            float f = Page.this.getZoomValue();
                            int newwidth = (int)(f * (float)Page.this.width);
                            int newheight = (int)(f * (float)Page.this.height);
                            int newtop = (int)(f * (float)Page.this.top);
                            int newleft = (int)(f * (float)Page.this.left);
                            int newheightWithoutMargin = (int)(f * (float)Page.this.heightWithoutMargin);
                            int newwidthWithoutMargin = (int)(f * (float)Page.this.widthWithoutMargin);
                            this.setPreferredSize(new Dimension(newwidth, newheight));
                            this.topMargin.setPreferredSize(new Dimension(newwidth, newtop));
                            this.leftMargin.setPreferredSize(new Dimension(newleft, newheightWithoutMargin));
                            this.center.reSize(newwidthWithoutMargin, newheightWithoutMargin);
                            this.rightMargin.setPreferredSize(new Dimension(newwidth - newwidthWithoutMargin - newleft, newheightWithoutMargin));
                            this.bottomMargin.setPreferredSize(new Dimension(newwidth, newheight - newtop - newheightWithoutMargin));
                            break block0;
                        }
                        case 0: {
                            float f = Page.this.getZoomValue();
                            int newwidth = (int)(f * (float)Page.this.width);
                            int newheight = (int)(f * (float)Page.this.height);
                            int newtop = (int)(f * (float)Page.this.top);
                            int newleft = (int)(f * (float)Page.this.left);
                            int newheightWithoutMargin = (int)(f * (float)Page.this.heightWithoutMargin);
                            int newwidthWithoutMargin = (int)(f * (float)Page.this.widthWithoutMargin);
                            this.setPreferredSize(new Dimension(newwidth, newheight));
                            this.topMargin.setPreferredSize(new Dimension(newwidth, newtop));
                            this.leftMargin.setPreferredSize(new Dimension(newleft, newheightWithoutMargin));
                            this.center.reSize(newwidthWithoutMargin, newheightWithoutMargin);
                            this.rightMargin.setPreferredSize(new Dimension(newheight - newheightWithoutMargin - newleft, newheightWithoutMargin));
                            this.bottomMargin.setPreferredSize(new Dimension(newwidth, newwidth - newtop - newwidthWithoutMargin));
                        }
                    }
                }
            }
            this.setSize(this.getPreferredSize());
            this.topMargin.setSize(this.topMargin.getPreferredSize());
            this.leftMargin.setSize(this.leftMargin.getPreferredSize());
            this.center.setSize(this.center.getPreferredSize());
            this.rightMargin.setSize(this.rightMargin.getPreferredSize());
            this.bottomMargin.setSize(this.bottomMargin.getPreferredSize());
            this.setLayout(new BorderLayout());
            this.add((Component)this.topMargin, "North");
            this.add((Component)this.leftMargin, "West");
            this.add((Component)this.center, "Center");
            this.add((Component)this.rightMargin, "East");
            this.add((Component)this.bottomMargin, "South");
            this.repaint();
        }

        public JLayeredPane getCenter() {
            return this.center;
        }

        public void posGraphicElement(PrintLayoutFrame plf) {
            this.center.posGraphicElement(plf);
        }

        public void remove(GraphicElements ge) {
            int i = 0;
            while (i < this.getCenter().getComponents().length) {
                if (this.getCenter().getComponent(i).equals(ge.getGraphicElementsOnScreen())) {
                    this.getCenter().remove(i);
                }
                ++i;
            }
            this.repaint();
        }

        @Override
        public void setBounds(Rectangle rect) {
            super.setBounds(rect);
            Page.this.parent.getPrintLayoutPreviewPanel().repaintRules();
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
            Page.this.parent.getPrintLayoutPreviewPanel().repaintRules();
        }

        @Override
        public void setSize(Dimension dim) {
            super.setSize(dim);
            Page.this.parent.getPrintLayoutPreviewPanel().repaintRules();
        }

        public void dispose() {
            this.center.dispose();
            this.center = null;
        }

        private class Marge
        extends JPanel {
            private static final long serialVersionUID = 1L;

            public Marge() {
                this.setBackground(Color.WHITE);
            }
        }
    }

    public class PageForPrint
    extends JLayeredPane
    implements Printable {
        private static final long serialVersionUID = 1L;
        private Envelope lastPrintedEnvelope = null;

        public PageForPrint() {
            super.setBackground(Color.WHITE);
            super.setPreferredSize(new Dimension(Page.this.widthWithoutMargin, Page.this.heightWithoutMargin));
            super.setSize(this.getPreferredSize());
        }

        public void resize() {
            super.setSize(Page.this.widthWithoutMargin, Page.this.heightWithoutMargin);
            this.repaint();
        }

        public void remove(GraphicElements ge) {
            int i = 0;
            while (i < this.getComponents().length) {
                if (this.getComponent(i).equals(ge.getGraphicElementsForPrint())) {
                    this.remove(i);
                }
                ++i;
            }
            this.repaint();
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (Page.this.envelopes == null || Page.this.envelopes.size() == 0) {
                if (pageIndex > 0 || Page.this.getGraphicElements().size() == 0) {
                    return 1;
                }
                if (!PrintWaitDialog.canceled) {
                    Graphics2D g2d = (Graphics2D)graphics;
                    MapFrame mapElement = Page.this.parent.getMapElement();
                    if (mapElement != null) {
                        mapElement.changeZoom();
                    }
                    PrintRenderer print = new PrintRenderer();
                    print.print(g2d, Page.this.getGraphicElements());
                    return 0;
                }
                return 1;
            }
            if (pageIndex < Page.this.envelopes.size()) {
                if (!PrintWaitDialog.canceled) {
                    Graphics2D g2d = (Graphics2D)graphics;
                    Envelope env = (Envelope)Page.this.envelopes.get(pageIndex);
                    MapFrame mapElement = Page.this.parent.getMapElement();
                    mapElement.changeZoom(env);
                    PrintRenderer print = new PrintRenderer();
                    if (!env.equals((Object)this.lastPrintedEnvelope)) {
                        PrintRenderer.eraseCache();
                    }
                    print.print(g2d, Page.this.getGraphicElements());
                    this.lastPrintedEnvelope = env;
                    if (Page.this.pageLabels != null) {
                        g2d.setClip(null);
                        g2d.setFont(new Font(null, 0, 8));
                        Rectangle2D markRect = g2d.getFontMetrics().getStringBounds((String)Page.this.pageLabels.get(pageIndex), g2d);
                        g2d.setColor(Color.WHITE);
                        g2d.fillRect((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY(), (int)markRect.getWidth(), (int)markRect.getHeight());
                        g2d.setColor(Color.BLACK);
                        g2d.drawString((String)Page.this.pageLabels.get(pageIndex), (int)pageFormat.getImageableX(), (int)pageFormat.getImageableY() + (int)markRect.getHeight());
                    }
                    return 0;
                }
                Page.this.envelopes = null;
                return 1;
            }
            Page.this.envelopes = null;
            return 1;
        }
    }

    public class PrintableZone
    extends JLayeredPane
    implements MouseListener,
    MouseMotionListener,
    DropTargetListener {
        private static final long serialVersionUID = 1L;
        private DropTarget dropTarget;
        private int largeurComposant;
        private int hauteurComposant;

        public PrintableZone() {
            this.setBackground(Color.WHITE);
            this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
            this.dropTarget = new DropTarget(this, this);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && Page.this.parent.getSelectedComponent() != null) {
                Page.this.parent.getSelectedComponent().setSelected(false);
                Page.this.parent.setSelectedComponent(null);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (Page.this.parent.getGraphic() != null) {
                this.setCursor(Cursor.getPredefinedCursor(1));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (Page.this.parent.getGraphic() != null) {
                Page.this.parent.getGraphic().setResizing(true);
                Page.this.parent.getGraphic().getGraphicElementsOnScreen().setBounds(new Rectangle(e.getPoint()));
                Page.this.parent.getGraphic().getGraphicElementsForPrint().setBounds(new Rectangle(e.getPoint()));
                JComponent jComponent = Page.this.parent.getGraphic().getGraphicElementsOnScreen();
                int n = orderCounter;
                orderCounter = n + 1;
                this.add((Component)jComponent, new Integer(n));
                Page.this.print.add((Component)Page.this.parent.getGraphic().getGraphicElementsForPrint(), 0);
                Page.this.graphicElements.add(Page.this.parent.getGraphic());
                if (Page.this.parent.getElementsViewerDialog() != null) {
                    Page.this.parent.getElementsViewerDialog().contentsChanged();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (Page.this.parent.getGraphic() != null) {
                Page.this.parent.getGraphic().setResizing(false);
                this.mouseDragged(e);
                Page.this.parent.getGraphic().setBorder(null);
                Page.this.parent.resetGraphic();
                this.setCursor(Cursor.getPredefinedCursor(0));
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (Page.this.parent.getGraphic() != null) {
                if (e.getPoint().getX() > Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getX()) {
                    this.largeurComposant = (int)(e.getPoint().getX() - Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getX());
                    this.hauteurComposant = e.getPoint().getY() > Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY() ? (int)(e.getPoint().getY() - Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY()) : (int)(Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY() - e.getPoint().getY());
                } else {
                    this.largeurComposant = (int)(Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getX() - e.getPoint().getX());
                    this.hauteurComposant = e.getPoint().getY() < Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY() ? (int)(Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY() - e.getPoint().getY()) : (int)(e.getPoint().getY() - Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY());
                }
                Page.this.parent.getGraphic().getGraphicElementsOnScreen().setBounds((int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getX(), (int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY(), this.largeurComposant, this.hauteurComposant);
                Page.this.parent.getGraphic().fixerDimensions((int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getX(), (int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY(), (int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getWidth(), (int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getHeight(), Page.this.widthWithoutMargin, this.getWidth());
                this.repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        public void reSize(int factor1, int factor2) {
            double factor = (double)factor1 / (double)factor2;
            switch (Page.this.parent.getActiveZoom()) {
                case 0: {
                    this.setPreferredSize(new Dimension((int)Math.round((double)Page.this.widthWithoutMargin * factor), (int)Math.round((double)Page.this.heightWithoutMargin * factor)));
                    break;
                }
                case 1: {
                    this.setPreferredSize(new Dimension(Page.this.widthWithoutMargin, Page.this.heightWithoutMargin));
                }
            }
            this.setSize(this.getPreferredSize());
            this.repaint();
        }

        public void posGraphicElement(PrintLayoutFrame plf) {
            if (Page.this.parent.getGraphic() != null) {
                Page.this.parent.getGraphic().setParent(plf);
                Page.this.parent.getGraphic().initGraphicAttributes(plf);
                JComponent jComponent = Page.this.parent.getGraphic().getGraphicElementsOnScreen();
                int n = orderCounter;
                orderCounter = n + 1;
                this.add((Component)jComponent, new Integer(n));
                Page.this.print.add((Component)Page.this.parent.getGraphic().getGraphicElementsForPrint(), 0);
                Page.this.parent.getGraphic().fixerDimensions((int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getX(), (int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getY(), (int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getWidth(), (int)Page.this.parent.getGraphic().getGraphicElementsOnScreen().getBounds().getHeight(), Page.this.widthWithoutMargin, this.getWidth());
                Page.this.parent.getGraphic().setGraphicAttributes();
                Page.this.parent.getGraphic().repaint();
                Page.this.parent.resetGraphic();
                this.setCursor(Cursor.getPredefinedCursor(0));
                this.repaint();
            }
        }

        @Override
        public void dragEnter(DropTargetDragEvent arg0) {
        }

        @Override
        public void dragOver(DropTargetDragEvent arg0) {
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent arg0) {
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            DataFlavor[] flavors = dtde.getCurrentDataFlavors();
            if (flavors == null) {
                return;
            }
            int i = flavors.length - 1;
            while (i >= 0) {
                if (flavors[i].equals(GraphicElementTransferHandler.GRAPHIC_ELEMENTS_FLAVOR)) {
                    GhostGlassPane glassPane = (GhostGlassPane)SwingUtilities.getRootPane(this).getGlassPane();
                    dtde.acceptDrop(2);
                    Transferable transferable = dtde.getTransferable();
                    BufferedImage newImage = null;
                    GraphicElements ge = null;
                    try {
                        Object[] dataTransfer = (Object[])transferable.getTransferData(GraphicElementTransferHandler.GRAPHIC_ELEMENTS_FLAVOR);
                        newImage = (BufferedImage)dataTransfer[0];
                        ge = (GraphicElements)dataTransfer[1];
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (newImage == null || ge == null) {
                        return;
                    }
                    Rectangle rect = SwingUtilities.convertRectangle(SwingUtilities.getRootPane(this), glassPane.getGhostGlassPaneBounds(), this);
                    Rectangle newRect = new Rectangle(rect.x, rect.y, newImage.getWidth(), newImage.getHeight());
                    ge.getGraphicElementsOnScreen().setBounds(newRect);
                    ge.initCornerPoint();
                    ge.refreshForPrintBounds();
                    glassPane.setImage(null);
                    glassPane.setVisible(false);
                    DragAndDropLock.setLocked(false);
                    dtde.dropComplete(true);
                    break;
                }
                --i;
            }
        }

        @Override
        public void dragExit(DropTargetEvent arg0) {
        }

        public void dispose() {
            Page.this.parent = null;
            this.removeMouseListener(this);
            this.removeMouseMotionListener(this);
            this.dropTarget = null;
        }
    }
}

