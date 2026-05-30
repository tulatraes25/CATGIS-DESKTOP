/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.print.elements.text;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.dnd.DragAndDropLock;
import org.saig.jump.widgets.dnd.GhostGlassPane;
import org.saig.jump.widgets.print.PrintLayoutFrame;
import org.saig.jump.widgets.print.PrintStatic;
import org.saig.jump.widgets.print.dnd.GraphicElementTransferHandler;
import org.saig.jump.widgets.print.elements.GraphicElements;
import org.saig.jump.widgets.print.elements.GraphicElementsListener;
import org.saig.jump.widgets.print.elements.text.FullLabel;
import org.saig.jump.widgets.print.elements.text.TextProperties;
import org.saig.jump.widgets.print.images.PrintIconLoader;

public class GraphicText
implements GraphicElements {
    private PrintLayoutFrame parent;
    private String name = "";
    public Point topLeftCorner = new Point();
    public Point topRightCorner = new Point();
    public Point bottomLeftCorner = new Point();
    public Point bottomRightCorner = new Point();
    private String text = I18N.getString("org.saig.jump.widgets.print.elements.text.GraphicText.your-text");
    private Font font = new Font("Arial", 0, 12);
    private Color fontColor = Color.BLACK;
    private Color borderColor = null;
    private int borderThickness = 0;
    private Border border = null;
    private Color backgroundColor = null;
    private int verticalAlignment = 0;
    private int horizontalAlignment = 0;
    private boolean isOpaque = false;
    private boolean isUnderline = false;
    private boolean selected = false;
    private GraphicTextOnScreen onScreen;
    private GraphicTextForPrint forPrint;
    private GraphicTextListener listener;

    public GraphicText() {
        this.listener = new GraphicTextListener(this, null);
        this.onScreen = new GraphicTextOnScreen();
        this.forPrint = new GraphicTextForPrint();
    }

    public GraphicText(PrintLayoutFrame plf) {
        this.parent = plf;
        this.listener = new GraphicTextListener(this, this.parent);
        this.onScreen = new GraphicTextOnScreen();
        this.forPrint = new GraphicTextForPrint();
        this.setName(this.parent.createName(this.getClass()));
    }

    @Override
    public JComponent getGraphicElementsOnScreen() {
        return this.onScreen;
    }

    @Override
    public JComponent getGraphicElementsForPrint() {
        return this.forPrint;
    }

    @Override
    public void fixerDimensions(int x, int y, int w, int h, int facteur1, int facteur2) {
        this.onScreen.setBounds(x, y, w, h);
        this.onScreen.setLocation(x, y);
        this.forPrint.fixerDimensions();
        this.initCornerPoint();
        this.repaint();
    }

    @Override
    public void initCornerPoint() {
        this.topLeftCorner = new Point(0, 0);
        this.topRightCorner = new Point((int)this.onScreen.getBounds().getWidth(), 0);
        this.bottomLeftCorner = new Point(0, (int)this.onScreen.getBounds().getHeight());
        this.bottomRightCorner = new Point((int)this.onScreen.getBounds().getWidth(), (int)this.onScreen.getBounds().getHeight());
    }

    @Override
    public Point[] getCornerPoint() {
        Point[] points = new Point[]{this.topLeftCorner, this.topRightCorner, this.bottomLeftCorner, this.bottomRightCorner};
        return points;
    }

    @Override
    public void refreshForPrintBounds() {
        this.forPrint.fixerDimensions();
    }

    @Override
    public void resize(int newForPrintWidth, int oldForPrintWidth, int newForPrintHeight, int oldForPrintHeight, int widthOnScreen, int heightOnScreen) {
        Rectangle oldForPrintBounds = (Rectangle)this.forPrint.getBounds().clone();
        this.forPrint.setBounds((int)Math.round(oldForPrintBounds.getX() * (double)newForPrintWidth / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getY() * (double)newForPrintHeight / (double)oldForPrintHeight), (int)Math.round(oldForPrintBounds.getWidth() * (double)newForPrintWidth / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getHeight() * (double)newForPrintHeight / (double)oldForPrintHeight));
        this.forPrint.setLocation((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY());
        switch (this.parent.getActiveZoom()) {
            case 3: {
                Rectangle rect = this.forPrint.getBounds();
                double f = this.parent.getZoomValue();
                this.onScreen.setBounds((int)((double)rect.x * f), (int)((double)rect.y * f), (int)((double)rect.width * f), (int)((double)rect.height * f));
                break;
            }
            case 0: {
                this.onScreen.setBounds((int)Math.round(oldForPrintBounds.getX() * (double)widthOnScreen / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getY() * (double)heightOnScreen / (double)oldForPrintHeight), (int)Math.round(oldForPrintBounds.getWidth() * (double)widthOnScreen / (double)oldForPrintWidth), (int)Math.round(oldForPrintBounds.getHeight() * (double)heightOnScreen / (double)oldForPrintHeight));
                this.onScreen.setLocation((int)this.onScreen.getBounds().getX(), (int)this.onScreen.getBounds().getY());
                break;
            }
            case 1: {
                this.onScreen.setFont(this.font);
                this.onScreen.setBounds((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY(), (int)this.forPrint.getBounds().getWidth(), (int)this.forPrint.getBounds().getHeight());
                this.onScreen.setLocation((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY());
            }
        }
        this.initCornerPoint();
        this.repaint();
    }

    @Override
    public void zoom(float f) {
        this.onScreen.setFont(new Font(this.forPrint.getFont().getName(), this.forPrint.getFont().getStyle(), (int)((float)this.forPrint.getFont().getSize() * f)));
        this.onScreen.setBounds((int)Math.round(this.forPrint.getBounds().getX() * (double)f), (int)Math.round(this.forPrint.getBounds().getY() * (double)f), (int)Math.round(this.forPrint.getBounds().getWidth() * (double)f), (int)Math.round(this.forPrint.getBounds().getHeight() * (double)f));
        this.initCornerPoint();
        this.onScreen.repaint();
    }

    @Override
    public void zoom(int newForPrintWidth, int oldForPrintWidth, int newForPrintHeight, int oldForPrintHeight, int widthOnScreen, int heightOnScreen) {
        switch (this.parent.getActiveZoom()) {
            case 0: {
                double widthFactor = (double)widthOnScreen / (double)newForPrintWidth;
                double heightFactor = (double)heightOnScreen / (double)newForPrintHeight;
                this.onScreen.setBounds((int)Math.round(this.forPrint.getBounds().getX() * widthFactor), (int)Math.round(this.forPrint.getBounds().getY() * heightFactor), (int)Math.round(this.forPrint.getBounds().getWidth() * widthFactor), (int)Math.round(this.forPrint.getBounds().getHeight() * heightFactor));
                this.onScreen.setLocation((int)this.onScreen.getBounds().getX(), (int)this.onScreen.getBounds().getY());
                break;
            }
            case 1: {
                this.onScreen.setFont(this.font);
                this.onScreen.setBounds((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY(), (int)this.forPrint.getBounds().getWidth(), (int)this.forPrint.getBounds().getHeight());
                this.onScreen.setLocation((int)this.forPrint.getBounds().getX(), (int)this.forPrint.getBounds().getY());
            }
        }
        this.initCornerPoint();
        this.onScreen.repaint();
    }

    public void setFont(Font f) {
        this.font = f;
        this.onScreen.setFont();
        this.forPrint.setFont(this.font);
        this.repaint();
    }

    public Font getFont() {
        return this.font;
    }

    public int getHorizontalAlignment() {
        return this.horizontalAlignment;
    }

    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
        this.onScreen.setHorizontalAlignment(horizontalAlignment);
        this.forPrint.setHorizontalAlignment(horizontalAlignment);
        this.repaint();
    }

    public int getVerticalAlignment() {
        return this.verticalAlignment;
    }

    public void setVerticalAlignment(int verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
        this.onScreen.setVerticalAlignment(verticalAlignment);
        this.forPrint.setVerticalAlignment(verticalAlignment);
        this.repaint();
    }

    public Color getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.onScreen.setBackground(backgroundColor);
        this.forPrint.setBackground(backgroundColor);
        this.repaint();
    }

    public boolean isOpaque() {
        return this.isOpaque;
    }

    public void setOpaque(boolean backgroundIsOpaque) {
        this.isOpaque = backgroundIsOpaque;
        this.onScreen.setOpaque(backgroundIsOpaque);
        this.forPrint.setOpaque(backgroundIsOpaque);
        this.repaint();
    }

    public Border getBorder() {
        return this.border;
    }

    @Override
    public void setBorder(Border border) {
        this.border = border;
        if (border != null) {
            this.borderThickness = ((LineBorder)this.getBorder()).getThickness();
            this.borderColor = ((LineBorder)this.getBorder()).getLineColor();
        } else {
            this.borderColor = null;
            this.borderThickness = 0;
        }
        this.onScreen.setBorder(border);
        this.forPrint.setBorder(border);
        this.repaint();
    }

    public Color getBorderColor() {
        return this.borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        this.border = borderColor != null ? BorderFactory.createLineBorder(borderColor, this.borderThickness) : null;
        this.onScreen.setBorder(this.border);
        this.forPrint.setBorder(this.border);
        this.repaint();
    }

    public int getBorderThickness() {
        return this.borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
        this.border = borderThickness != 0 ? BorderFactory.createLineBorder(this.borderColor, borderThickness) : null;
        this.onScreen.setBorder(this.border);
        this.forPrint.setBorder(this.border);
        this.repaint();
    }

    public Color getFontColor() {
        return this.fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
        this.onScreen.setForeground(fontColor);
        this.forPrint.setForeground(fontColor);
        this.repaint();
    }

    public boolean isUnderline() {
        return this.isUnderline;
    }

    public void setUnderline(boolean isUnderline) {
        this.isUnderline = isUnderline;
        if (isUnderline) {
            if (this.text.lastIndexOf("<HTML><U>") != -1 && this.text.lastIndexOf("<html><u>") != -1) {
                this.text = "<HTML><U>" + this.text + "</U></HTML>";
            }
        } else {
            this.text = this.text.replaceAll("(?i)<html><u>", "");
            this.text = this.text.replaceAll("(?i)</u></html>", "");
        }
        this.setText(this.text);
        this.repaint();
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
        this.onScreen.setText(text);
        this.forPrint.setText(text);
        this.repaint();
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(boolean select) {
        this.selected = select;
        if (select) {
            int borderSize = Math.max(this.borderThickness, 1);
            this.onScreen.setBorder(BorderFactory.createLineBorder(Color.RED, borderSize));
        } else {
            this.setBorder(this.border);
        }
        this.repaint();
    }

    @Override
    public void repaint() {
        this.onScreen.repaintOnScreen();
        this.forPrint.repaint();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        this.onScreen.setToolTipText(this.getName());
    }

    @Override
    public void setGraphicAttributes() {
    }

    @Override
    public void initGraphicAttributes(PrintLayoutFrame plf) {
    }

    @Override
    public void setParent(PrintLayoutFrame plf) {
        this.parent = plf;
        this.listener.setFrame(plf);
    }

    public int getTopLeftCornerX() {
        return this.topLeftCorner.x;
    }

    public void setTopLeftCornerX(int n) {
        this.topLeftCorner.x = n;
    }

    public int getTopLeftCornerY() {
        return this.topLeftCorner.y;
    }

    public void setTopLeftCornerY(int n) {
        this.topLeftCorner.y = n;
    }

    public int getTopRightCornerX() {
        return this.topRightCorner.x;
    }

    public void setTopRightCornerX(int n) {
        this.topRightCorner.x = n;
    }

    public int getTopRightCornerY() {
        return this.topRightCorner.y;
    }

    public void setTopRightCornerY(int n) {
        this.topRightCorner.y = n;
    }

    public int getBottomLeftCornerX() {
        return this.bottomLeftCorner.x;
    }

    public void setBottomLeftCornerX(int n) {
        this.bottomLeftCorner.x = n;
    }

    public int getBottomLeftCornerY() {
        return this.bottomLeftCorner.y;
    }

    public void setBottomLeftCornerY(int n) {
        this.bottomLeftCorner.y = n;
    }

    public int getBottomRightCornerX() {
        return this.bottomRightCorner.x;
    }

    public void setBottomRightCornerX(int n) {
        this.bottomRightCorner.x = n;
    }

    public int getBottomRightCornerY() {
        return this.bottomRightCorner.y;
    }

    public void setBottomRightCornerY(int n) {
        this.bottomRightCorner.y = n;
    }

    @Override
    public int getPrintHeight() {
        return this.forPrint.getHeight();
    }

    public void setPrintHeight(int h) {
        int w = this.forPrint.getWidth();
        Dimension d = new Dimension();
        d.setSize(w, h);
        this.forPrint.setSize(d);
    }

    @Override
    public int getPrintWidth() {
        return this.forPrint.getWidth();
    }

    public void setPrintWidth(int w) {
        int h = this.forPrint.getHeight();
        Dimension d = new Dimension();
        d.setSize(w, h);
        this.forPrint.setSize(d);
    }

    @Override
    public int getPrintX() {
        return this.forPrint.getX();
    }

    public void setPrintX(int x) {
        int y = this.forPrint.getY();
        Point p = new Point();
        p.setLocation(x, y);
        this.forPrint.setLocation(p);
    }

    @Override
    public int getPrintY() {
        return this.forPrint.getY();
    }

    public void setPrintY(int y) {
        int x = this.forPrint.getX();
        Point p = new Point();
        p.setLocation(x, y);
        this.forPrint.setLocation(p);
    }

    public int getScreenHeight() {
        return this.onScreen.getHeight();
    }

    public void setScreenHeight(int h) {
        int w = this.onScreen.getWidth();
        Dimension d = new Dimension();
        d.setSize(w, h);
        this.onScreen.setSize(d);
    }

    public int getScreenWidth() {
        return this.onScreen.getWidth();
    }

    public void setScreenWidth(int w) {
        int h = this.onScreen.getHeight();
        Dimension d = new Dimension();
        d.setSize(w, h);
        this.onScreen.setSize(d);
    }

    public int getScreenX() {
        return this.onScreen.getX();
    }

    public void setScreenX(int x) {
        int y = this.onScreen.getY();
        Point p = new Point();
        p.setLocation(x, y);
        this.onScreen.setLocation(p);
    }

    public int getScreenY() {
        return this.onScreen.getY();
    }

    public void setScreenY(int y) {
        int x = this.onScreen.getX();
        Point p = new Point();
        p.setLocation(x, y);
        this.onScreen.setLocation(p);
    }

    public String getFontName() {
        return this.font.getName();
    }

    public void setFontName(String fn) {
        Font nf;
        int style = this.font.getStyle();
        int size = this.font.getSize();
        this.font = nf = new Font(fn, style, size);
        this.onScreen.setFont();
        this.forPrint.setFont(this.font);
    }

    public int getFontStyle() {
        return this.font.getStyle();
    }

    public void setFontStyle(int st) {
        Font nf;
        String name = this.font.getName();
        int size = this.font.getSize();
        this.font = nf = new Font(name, st, size);
        this.onScreen.setFont();
        this.forPrint.setFont(this.font);
    }

    public int getFontSize() {
        return this.font.getSize();
    }

    public void setFontSize(int s) {
        Font nf;
        String name = this.font.getName();
        int style = this.font.getStyle();
        this.font = nf = new Font(name, style, s);
        this.onScreen.setFont();
        this.forPrint.setFont(this.font);
    }

    @Override
    public void refresh() {
        if (this.parent.getSelectedComponent() != null) {
            this.parent.getSelectedComponent().setSelected(false);
        }
        this.setSelected(true);
        this.parent.setSelectedComponent(this);
        this.repaint();
    }

    @Override
    public Icon getIcon() {
        return PrintIconLoader.icon("addText.gif");
    }

    @Override
    public void dispose() {
        this.parent = null;
        this.forPrint = null;
        if (this.onScreen != null) {
            this.onScreen.dispose();
            this.onScreen = null;
        }
        if (this.listener != null) {
            this.listener.dispose();
            this.listener = null;
        }
    }

    @Override
    public PrintLayoutFrame getParent() {
        return this.parent;
    }

    public String toString() {
        return I18N.getMessage("org.saig.jump.widgets.print.elements.text.GraphicText.text-{0}", new Object[]{this.getText()});
    }

    @Override
    public void setResizing(boolean resizing) {
    }

    private class GraphicTextForPrint
    extends FullLabel {
        public GraphicTextForPrint() {
            super(GraphicText.this.text);
            super.setFont(GraphicText.this.font);
            super.setForeground(GraphicText.this.fontColor);
            super.setBorder(GraphicText.this.border);
            super.setOpaque(GraphicText.this.isOpaque);
            super.setHorizontalAlignment(GraphicText.this.horizontalAlignment);
            super.setVerticalAlignment(GraphicText.this.verticalAlignment);
        }

        public void fixerDimensions() {
            switch (GraphicText.this.parent.getActiveZoom()) {
                case 0: 
                case 3: {
                    super.setBounds((int)Math.round(GraphicText.this.onScreen.getBounds().getX() * GraphicText.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(GraphicText.this.onScreen.getBounds().getY() * GraphicText.this.parent.getPrintScreenHeightAspectRatio()), (int)Math.round(GraphicText.this.onScreen.getBounds().getWidth() * GraphicText.this.parent.getPrintScreenWidthAspectRatio()), (int)Math.round(GraphicText.this.onScreen.getBounds().getHeight() * GraphicText.this.parent.getPrintScreenHeightAspectRatio()));
                    break;
                }
                case 1: {
                    super.setBounds((int)GraphicText.this.onScreen.getBounds().getX(), (int)GraphicText.this.onScreen.getBounds().getY(), (int)GraphicText.this.onScreen.getBounds().getWidth(), (int)GraphicText.this.onScreen.getBounds().getHeight());
                }
            }
            super.setLocation((int)super.getBounds().getX(), (int)super.getBounds().getY());
            super.repaint();
        }
    }

    private class GraphicTextListener
    extends GraphicElementsListener {
        public GraphicTextListener(GraphicElements ge, PrintLayoutFrame plf) {
            super(ge, plf);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            GraphicText.this.onScreen.requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (e.getClickCount() == 2) {
                    new TextProperties((GraphicText)this.ge);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (GraphicText.this.parent.getSelectedComponent() != null) {
                        GraphicText.this.parent.getSelectedComponent().setSelected(false);
                    }
                    this.ge.setSelected(true);
                    GraphicText.this.parent.setSelectedComponent(this.ge);
                }
                GraphicText.this.repaint();
            }
        }
    }

    private class GraphicTextOnScreen
    extends FullLabel
    implements DragGestureListener,
    DragSourceListener,
    DragSourceMotionListener {
        public GraphicTextOnScreen() {
            super(GraphicText.this.text);
            super.setFont(new Font(GraphicText.this.font.getName(), GraphicText.this.font.getStyle(), 8));
            super.setForeground(GraphicText.this.fontColor);
            super.setBorder(GraphicText.this.border);
            super.setOpaque(GraphicText.this.isOpaque);
            super.setHorizontalAlignment(GraphicText.this.horizontalAlignment);
            super.setVerticalAlignment(GraphicText.this.verticalAlignment);
            this.addMouseListener(GraphicText.this.listener);
            this.addMouseMotionListener(GraphicText.this.listener);
            this.addKeyListener(GraphicText.this.listener);
            this.setFocusable(true);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this, 2, this);
            dragSource.addDragSourceMotionListener(this);
        }

        public void setFont() {
            double factor = GraphicText.this.parent != null ? (double)GraphicText.this.parent.getZoomValue() : 1.0;
            super.setFont(new Font(GraphicText.this.font.getName(), GraphicText.this.font.getStyle(), (int)Math.round((double)GraphicText.this.font.getSize() * factor)));
        }

        public void dispose() {
            this.removeMouseListener(GraphicText.this.listener);
            this.removeMouseMotionListener(GraphicText.this.listener);
            this.removeKeyListener(GraphicText.this.listener);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.removeDragSourceMotionListener(this);
        }

        public void repaintOnScreen() {
            if (GraphicText.this.onScreen.getHeight() > 0 && GraphicText.this.forPrint.getHeight() > 0) {
                this.setFont();
            }
            super.repaint();
        }

        @Override
        public void dragEnter(DragSourceDragEvent e) {
            DragSourceContext context = e.getDragSourceContext();
            int myaction = e.getDropAction();
            if ((myaction & 2) != 0) {
                context.setCursor(DragSource.DefaultMoveDrop);
            } else {
                context.setCursor(DragSource.DefaultMoveNoDrop);
            }
        }

        @Override
        public void dragOver(DragSourceDragEvent arg0) {
        }

        @Override
        public void dropActionChanged(DragSourceDragEvent arg0) {
        }

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (!DragAndDropLock.isDragAndDropStarted()) {
                return;
            }
            DragAndDropLock.setDragAndDropStarted(false);
            GhostGlassPane glassPane = (GhostGlassPane)SwingUtilities.getRootPane(this).getGlassPane();
            Point p = (Point)dsde.getLocation().clone();
            SwingUtilities.convertPointFromScreen(p, glassPane);
            if (!dsde.getDropSuccess()) {
                DragAndDropLock.setLocked(false);
                DragAndDropLock.setDragAndDropStarted(false);
                glassPane.setImage(null);
                glassPane.setVisible(false);
            } else {
                glassPane.setPoint(p);
                glassPane.repaint(glassPane.getRepaintRect());
            }
        }

        @Override
        public void dragExit(DragSourceEvent dse) {
            DragSourceContext context = dse.getDragSourceContext();
            context.setCursor(DragSource.DefaultMoveNoDrop);
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge) {
            if (DragAndDropLock.isLocked() || DragAndDropLock.isResizing()) {
                DragAndDropLock.setDragAndDropStarted(false);
                return;
            }
            DragAndDropLock.setLocked(true);
            DragAndDropLock.setDragAndDropStarted(true);
            BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), 1);
            Graphics2D graphics2 = (Graphics2D)image.getGraphics();
            graphics2.setColor(Color.WHITE);
            graphics2.fillRect(0, 0, this.getWidth(), this.getHeight());
            GraphicText.this.onScreen.paint(graphics2);
            dge.startDrag(Cursor.getDefaultCursor(), new GraphicElementTransferHandler(image, GraphicText.this), this);
            GhostGlassPane glassPane = (GhostGlassPane)SwingUtilities.getRootPane(this).getGlassPane();
            glassPane.setVisible(true);
            Point panelMiddlePoint = new Point(this.getBounds().width / 2, this.getBounds().height / 2);
            SwingUtilities.convertPointToScreen(panelMiddlePoint, this);
            Point p = (Point)dge.getDragOrigin().clone();
            SwingUtilities.convertPointToScreen(p, this);
            PrintStatic.xtrans = panelMiddlePoint.x - p.x;
            PrintStatic.ytrans = panelMiddlePoint.y - p.y;
            SwingUtilities.convertPointFromScreen(p, glassPane);
            p.translate(PrintStatic.xtrans, PrintStatic.ytrans);
            glassPane.setPoint(p);
            glassPane.setImage(image);
            glassPane.repaint();
        }

        @Override
        public void dragMouseMoved(DragSourceDragEvent dsde) {
            if (!DragAndDropLock.isDragAndDropStarted()) {
                return;
            }
            if (SwingUtilities.getRootPane(this) != null && SwingUtilities.getRootPane(this).getGlassPane() != null) {
                GhostGlassPane glassPane = (GhostGlassPane)SwingUtilities.getRootPane(this).getGlassPane();
                Point p = (Point)dsde.getLocation().clone();
                SwingUtilities.convertPointFromScreen(p, glassPane);
                p.translate(PrintStatic.xtrans, PrintStatic.ytrans);
                glassPane.setPoint(p);
                glassPane.repaint(glassPane.getRepaintRect());
            }
        }
    }
}

