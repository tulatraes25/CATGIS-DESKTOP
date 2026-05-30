/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.text.Collator;
import java.util.Collection;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.RecordSelectionListener;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.TableAttributeTab;
import org.saig.core.model.data.widgets.TableInfoModel;
import org.saig.jump.lang.I18N;

public class ViewTableFrame
extends JInternalFrame
implements RecordSelectionListener,
Comparable<ViewTableFrame> {
    private static final long serialVersionUID = 1L;
    private TableAttributeTab attributeTab;

    protected ViewTableFrame() {
    }

    public ViewTableFrame(Table table, final PlugInContext context) {
        TableInfoModel model = new TableInfoModel();
        context.getWorkbenchContext().getDataManager().getRecordSelectionManager().addSelectionListener(this);
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                context.getWorkbenchContext().getDataManager().getRecordSelectionManager().removeSelectionListener(ViewTableFrame.this);
            }
        });
        this.setResizable(true);
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
        this.getContentPane().setLayout(new BorderLayout());
        this.attributeTab = new TableAttributeTab(model, context.getWorkbenchContext(), this, true).setTable(table);
        this.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                ViewTableFrame.this.attributeTab.getToolBar().updateEnabledState();
            }
        });
        this.getContentPane().add((Component)this.attributeTab, "Center");
        Rectangle parentBounds = JUMPWorkbench.getFrameInstance().getDesktopPane().getBounds();
        Dimension dim = this.attributeTab.getTableSize();
        int frameHeight = 300;
        int desiredFrameWidth = Math.min(parentBounds.width, dim.width + 57);
        int toolbarWidth = (int)this.attributeTab.getToolBar().getPreferredSize().getWidth();
        if (desiredFrameWidth <= toolbarWidth) {
            this.setSize(toolbarWidth + 50, frameHeight);
        } else {
            this.setSize(desiredFrameWidth, frameHeight);
        }
        this.setLocation(0, Math.max(0, parentBounds.height - 300));
        this.updateTitle(this.attributeTab.getModel().getTable());
    }

    protected void updateTitle(Table table) {
        this.setTitle(table.getName());
    }

    @Override
    public void selectionChanged() {
        this.attributeTab.setLayerViewPanelUpdates(false);
        try {
            Collection<Record> col = JUMPWorkbench.getFrameInstance().getContext().getDataManager().getRecordSelectionManager().getRecordSelection(this.attributeTab.getTable());
            this.attributeTab.selectRecords(col, this.attributeTab.getTable());
            this.attributeTab.updateRelationSelection(this.attributeTab.getTable(), col);
        }
        finally {
            this.attributeTab.setLayerViewPanelUpdates(true);
        }
    }

    @Override
    public int compareTo(ViewTableFrame vtf) {
        Collator col = Collator.getInstance(I18N.getLocale());
        return col.compare(this.getTable().getName(), vtf.getTable().getName());
    }

    public Table getTable() {
        return this.attributeTab.getTable();
    }

    public void makeEnabled(boolean enable) {
        boolean visible = this.isVisible();
        boolean closed = this.isClosed();
        if (enable) {
            if (!visible && closed) {
                JUMPWorkbench.getFrameInstance().getContext().getDataManager().addTable(this);
            } else {
                this.setVisible(true);
                this.moveToFront();
            }
        } else if (visible) {
            this.setVisible(false);
            if (!closed) {
                try {
                    this.setClosed(true);
                }
                catch (PropertyVetoException propertyVetoException) {
                    // empty catch block
                }
            }
            this.dispose();
            JUMPWorkbench.getFrameInstance().removeInternalFrame(this);
        }
    }

    @Override
    public String toString() {
        return this.title;
    }

    public void explicitSort(boolean ascending) {
        this.attributeTab.explicitSort(ascending);
    }
}

