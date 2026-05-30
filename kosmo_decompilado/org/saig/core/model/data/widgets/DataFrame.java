/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.text.Collator;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.widgets.DataPanel;
import org.saig.jump.lang.I18N;

public class DataFrame
extends JInternalFrame
implements Comparable {
    private static final long serialVersionUID = 1L;
    private DataPanel dataPanel;
    private Table table;
    private JScrollPane tableScrollPane;
    private JPanel mainPanel;

    public DataFrame(Table recordCollection) {
        this.setDefaultCloseOperation(1);
        this.table = recordCollection;
        this.setResizable(true);
        this.setClosable(true);
        this.setMaximizable(true);
        this.setIconifiable(true);
        this.setTitle(this.table.getName());
        this.makeEnabled(recordCollection.isEnabled());
    }

    public void makeEnabled(boolean valor) {
        if (valor) {
            Rectangle parentBounds = JUMPWorkbench.getFrameInstance().getDesktopPane().getBounds();
            this.dataPanel = new DataPanel(this.table);
            int dim = this.dataPanel.getTableSize();
            this.setPreferredSize(new Dimension(Math.min(parentBounds.width, dim + 50), 150));
            this.setMinimumSize(new Dimension(Math.min(parentBounds.width, dim + 50), 150));
            this.setLocation(0, Math.max(0, parentBounds.height - 150));
            try {
                this.initialize();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (this.mainPanel != null) {
                this.getContentPane().remove(this.mainPanel);
            }
            this.setVisible(false);
            this.dataPanel = null;
        }
    }

    private void initialize() throws Exception {
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BorderLayout());
        this.setContentPane(this.mainPanel);
        this.tableScrollPane = new JScrollPane();
        this.tableScrollPane.setColumnHeaderView(this.dataPanel.getTableHeather());
        this.tableScrollPane.setHorizontalScrollBarPolicy(30);
        this.tableScrollPane.setVerticalScrollBarPolicy(22);
        this.tableScrollPane.getVerticalScrollBar().setUnitIncrement(new JTable().getRowHeight());
        this.tableScrollPane.getViewport().add(this.dataPanel);
        this.mainPanel.add((Component)this.tableScrollPane, "Center");
        this.mainPanel.add((Component)new JLabel(this.dataPanel.getTitle()), "South");
        this.pack();
        this.setVisible(true);
    }

    @Override
    public String toString() {
        return this.title;
    }

    public Table getTable() {
        return this.table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public void fireTableChanged(TableModelEvent e) {
        this.dataPanel.tableChanged(e);
    }

    public void refresh() {
        if (this.mainPanel != null) {
            this.getContentPane().remove(this.mainPanel);
        }
        Rectangle parentBounds = JUMPWorkbench.getFrameInstance().getDesktopPane().getBounds();
        this.dataPanel = new DataPanel(this.table);
        int dim = this.dataPanel.getTableSize();
        this.setPreferredSize(new Dimension(Math.min(parentBounds.width, dim + 50), 150));
        this.setMinimumSize(new Dimension(Math.min(parentBounds.width, dim + 50), 150));
        this.setLocation(0, Math.max(0, parentBounds.height - 150));
        try {
            this.initialize();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int compareTo(Object o) {
        Collator col = Collator.getInstance(I18N.getLocale());
        return col.compare(this.getTable().getName(), ((DataFrame)o).getTable().getName());
    }
}

