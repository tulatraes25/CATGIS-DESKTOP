/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.GeometryFactory
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.ui.EditOptionsPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.snap.MidPointSnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapManager;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToAbsolutAnglePolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToAnglePolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToCentroidPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToCrossPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToFeaturesPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToGridPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToPerpendicularPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToStartEndPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToTangentPolicy;
import com.vividsolutions.jump.workbench.ui.snap.SnapToVerticesPolicy;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.ZManager;

public abstract class AbstractCursorTool
implements CursorTool {
    private boolean snappingConfigured = false;
    private boolean configuringSnapping = false;
    private boolean controlPressed;
    private boolean shiftPressed;
    private Color color = Color.red;
    private boolean filling = false;
    private Shape lastShapeDrawn;
    protected boolean activate = true;
    protected static GeometryFactory geomFac = new GeometryFactory();
    private LayerViewPanelListener layerViewPanelListener = new LayerViewPanelListener(){

        @Override
        public void cursorPositionChanged(String x, String y) {
        }

        @Override
        public void selectionChanged() {
        }

        @Override
        public void painted(Graphics graphics) {
            try {
                if (AbstractCursorTool.this.shapeOnScreen) {
                    AbstractCursorTool.this.setShapeOnScreen(false);
                    AbstractCursorTool.this.redrawShape((Graphics2D)graphics);
                }
            }
            catch (Throwable t) {
                AbstractCursorTool.this.panel.getContext().handleThrowable(t);
            }
        }

        @Override
        public void renderingFinished() {
        }

        @Override
        public void renderingStarted() {
        }
    };
    private Color originalColor;
    private Stroke originalStroke;
    private LayerViewPanel panel;
    private boolean shapeOnScreen = false;
    private SnapManager snapManager = new SnapManager();
    private Stroke stroke = new BasicStroke(1.0f);
    private List<Listener> listeners = new ArrayList<Listener>();

    public void allowSnapping() {
        this.configuringSnapping = true;
    }

    protected boolean wasShiftPressed() {
        return this.shiftPressed;
    }

    protected void setShiftPressed(boolean isShiftPressed) {
        this.shiftPressed = isShiftPressed;
    }

    protected boolean wasControlPressed() {
        return this.controlPressed;
    }

    protected void setControlPressed(boolean isControlPressed) {
        this.controlPressed = isControlPressed;
    }

    public static Cursor createCursor(Image image) {
        return AbstractCursorTool.createCursor(image, new Point(16, 16));
    }

    public static Cursor createCursor(Image image, Point hotSpot) {
        if (image == null) {
            return Cursor.getDefaultCursor();
        }
        if (Toolkit.getDefaultToolkit().getBestCursorSize(32, 32).equals(new Dimension(0, 0))) {
            return Cursor.getDefaultCursor();
        }
        return Toolkit.getDefaultToolkit().createCustomCursor(image, hotSpot, I18N.getString("workbench.ui.cursortool.AbstractCursorTool.jcs-workbench-custom-cursor"));
    }

    @Override
    public Cursor getCursor() {
        return Cursor.getDefaultCursor();
    }

    @Override
    public boolean isGestureInProgress() {
        return this.isShapeOnScreen();
    }

    @Override
    public boolean isRightMouseButtonUsed() {
        return false;
    }

    public boolean isShapeOnScreen() {
        return this.shapeOnScreen;
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        if (AbstractCursorTool.workbenchFrame(layerViewPanel) != null) {
            AbstractCursorTool.workbenchFrame(layerViewPanel).log(String.valueOf(I18N.getString("workbench.ui.cursortool.AbstractCursorTool.activating")) + this.getName());
            AbstractCursorTool.workbenchFrame(layerViewPanel).setStatusMessage("");
        }
        if (this.panel != null) {
            this.panel.removeListener(this.layerViewPanelListener);
        }
        this.panel = layerViewPanel;
        this.panel.addListener(this.layerViewPanelListener);
        if (this.configuringSnapping && !this.snappingConfigured) {
            this.getSnapManager().addPolicies(AbstractCursorTool.createStandardSnappingPolicies(JUMPWorkbench.getBlackboard()));
            this.snappingConfigured = true;
        }
    }

    public static WorkbenchFrame workbenchFrame(LayerViewPanel layerViewPanel) {
        Window window = SwingUtilities.windowForComponent(layerViewPanel);
        return window instanceof WorkbenchFrame ? (WorkbenchFrame)window : null;
    }

    @Override
    public void deactivate() {
        if (this.layerViewPanelListener != null) {
            this.panel.removeListener(this.layerViewPanelListener);
        }
        this.cancelGesture();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.setControlPressed(e.isControlDown());
        this.setShiftPressed(e.isShiftDown());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFilling(boolean filling) {
        this.filling = filling;
    }

    protected void setStrokeWidth(int strokeWidth) {
        this.setStroke(new BasicStroke(strokeWidth));
    }

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    protected void setup(Graphics2D graphics) {
        this.originalColor = graphics.getColor();
        this.originalStroke = graphics.getStroke();
        graphics.setColor(this.color);
        graphics.setXORMode(Color.WHITE);
        graphics.setStroke(this.stroke);
    }

    protected LayerViewPanel getPanel() {
        return this.panel;
    }

    protected abstract Shape getShape() throws Exception;

    protected void cleanup(Graphics2D graphics) {
        graphics.setPaintMode();
        graphics.setColor(this.originalColor);
        graphics.setStroke(this.originalStroke);
    }

    protected void clearShape() {
        if (this.panel != null) {
            this.clearShape((Graphics2D)this.panel.getGraphics());
        }
    }

    @Override
    public void cancelGesture() {
        this.clearShape();
    }

    protected void drawShapeXOR(Graphics2D g) throws Exception {
        Shape newShape = this.getShape();
        this.drawShapeXOR(newShape, g);
        this.lastShapeDrawn = newShape;
    }

    protected void drawShapeXOR(Shape shape, Graphics2D graphics) {
        this.setup(graphics);
        try {
            if (shape != null) {
                if (this.filling) {
                    graphics.fill(shape);
                } else {
                    graphics.draw(shape);
                }
            }
        }
        finally {
            this.cleanup(graphics);
        }
    }

    protected void redrawShape() throws Exception {
        this.redrawShape((Graphics2D)this.panel.getGraphics());
    }

    protected Coordinate snap(Point2D viewPoint) throws NoninvertibleTransformException {
        Coordinate c = this.snap(this.getPanel().getViewport().toModelCoordinate(viewPoint));
        if (ZManager.isZUseActive()) {
            c.z = ZManager.getActiveZ();
        }
        return c;
    }

    protected Coordinate snap(Coordinate modelCoordinate) {
        Coordinate snap = this.snapManager.snap(this.getPanel(), modelCoordinate);
        return snap;
    }

    private synchronized void setShapeOnScreen(boolean shapeOnScreen) {
        this.shapeOnScreen = shapeOnScreen;
    }

    private void clearShape(Graphics2D graphics) {
        if (!this.shapeOnScreen) {
            return;
        }
        this.drawShapeXOR(this.lastShapeDrawn, graphics);
        this.setShapeOnScreen(false);
    }

    protected synchronized void redrawShape(Graphics2D graphics) throws Exception {
        this.clearShape(graphics);
        this.drawShapeXOR(graphics);
        this.setShapeOnScreen(true);
    }

    protected TaskFrame getTaskFrame() {
        return (TaskFrame)SwingUtilities.getAncestorOfClass(TaskFrame.class, this.getPanel());
    }

    public JUMPWorkbench getWorkbench() {
        return AbstractCursorTool.workbench(this.getPanel());
    }

    public static JUMPWorkbench workbench(LayerViewPanel panel) {
        if (panel == null) {
            return null;
        }
        return JUMPWorkbench.getFrameInstance().getContext().getWorkbench();
    }

    protected abstract void gestureFinished() throws Exception;

    protected void fireGestureFinished() throws Exception {
        this.getPanel().getContext().setStatusMessage("");
        if (!this.activate) {
            return;
        }
        if (this.getTaskFrame() != null) {
            ((WorkbenchFrame)SwingUtilities.getAncestorOfClass(WorkbenchFrame.class, this.getTaskFrame())).log(String.valueOf(I18N.getString("workbench.ui.cursortool.AbstractCursorTool.gesture-finished")) + this.getName());
        }
        this.getPanel().getLayerManager().getUndoableEditReceiver().startReceiving();
        try {
            this.gestureFinished();
        }
        finally {
            this.getPanel().getLayerManager().getUndoableEditReceiver().stopReceiving();
        }
        for (Listener listener : this.listeners) {
            listener.gestureFinished();
        }
    }

    public void add(Listener listener) {
        this.listeners.add(listener);
    }

    protected void execute(UndoableCommand command) throws Exception {
        AbstractPlugIn.execute(command, this.getPanel());
    }

    protected void reportNothingToUndoYet() {
        this.getPanel().getLayerManager().getUndoableEditReceiver().reportNothingToUndoYet();
    }

    public String toString() {
        return this.getName();
    }

    @Override
    public String getName() {
        return AbstractCursorTool.name(this);
    }

    public static String name(CursorTool tool) {
        return StringUtil.toFriendlyName(tool.getClass().getName(), I18N.getString("workbench.ui.cursortool.AbstractCursorTool.tool"));
    }

    protected boolean check(EnableCheck check) {
        String warning = check.check(null);
        if (warning != null) {
            this.getPanel().getContext().warnUser(warning);
            return false;
        }
        return true;
    }

    public SnapManager getSnapManager() {
        return this.snapManager;
    }

    public void setSnapManager(SnapManager newSnap) {
        this.snapManager = newSnap;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public boolean isActivate() {
        return this.activate;
    }

    @Override
    public void setActivate(boolean activate) {
        boolean wasInactive = !this.activate;
        this.activate = activate;
        LayerViewPanel layerViewPanel = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        if (layerViewPanel != null && layerViewPanel != null && layerViewPanel.checkCurrentCursorTool(activate, this)) {
            if (activate) {
                layerViewPanel.setCursor(layerViewPanel.getCurrentCursorTool().getCursor());
                if (wasInactive) {
                    this.activate(layerViewPanel);
                }
            } else {
                layerViewPanel.getCurrentCursorTool().deactivate();
                layerViewPanel.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    @Override
    public boolean checkConditions() throws Exception {
        return true;
    }

    protected boolean isRollingBackInvalidEdits() {
        return EditOptionsPanel.isRollingBackInvalidEdits();
    }

    protected boolean isConcurrentEditionActivated() {
        return EditOptionsPanel.isConcurrentEditionActivated();
    }

    protected boolean isAdjacentEditionActivated() {
        return EditOptionsPanel.isAdjacentEditionActivated();
    }

    protected List<SnapPolicy> getSnappingPolicies(Blackboard blackboard) {
        return AbstractCursorTool.createStandardSnappingPolicies(JUMPWorkbench.getBlackboard());
    }

    public static List<SnapPolicy> createStandardSnappingPolicies(Blackboard blackboard) {
        return Arrays.asList(new SnapToStartEndPolicy(blackboard), new SnapToVerticesPolicy(blackboard), new MidPointSnapPolicy(blackboard), new SnapToFeaturesPolicy(blackboard), new SnapToGridPolicy(blackboard), new SnapToCentroidPolicy(blackboard), new SnapToPerpendicularPolicy(blackboard), new SnapToAnglePolicy(blackboard), new SnapToAbsolutAnglePolicy(blackboard), new SnapToTangentPolicy(blackboard), new SnapToCrossPolicy(blackboard));
    }

    public static interface Listener {
        public void gestureFinished();
    }
}

