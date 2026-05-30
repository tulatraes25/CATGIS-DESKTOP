/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.saig.core.filter.Filter;
import org.saig.core.model.data.dao.TableDBRecordDataSource;
import org.saig.jump.plugin.query.QueryWizardPlugIn;
import org.saig.jump.widgets.query.IQueryWizardDialog;
import org.saig.jump.widgets.query.TableQueryWizardPanel;

public class TableQueryWizardDialog
extends JDialog
implements IQueryWizardDialog {
    private static final long serialVersionUID = 1L;
    private TableQueryWizardPanel tableQueryWizardPanel;

    public TableQueryWizardDialog(JFrame parent, boolean modal, TableDBRecordDataSource tds) {
        super((Frame)parent, modal);
        this.setTitle(QueryWizardPlugIn.NAME);
        this.tableQueryWizardPanel = new TableQueryWizardPanel(tds, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
        this.setContentPane(this.tableQueryWizardPanel);
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    @Override
    public void setFilter(Filter filter) {
        this.tableQueryWizardPanel.setFilter(filter);
    }

    @Override
    public Filter getFilter() {
        return this.tableQueryWizardPanel.getFilter();
    }

    @Override
    public String getRawText() {
        if (this.tableQueryWizardPanel.getFilter() != null) {
            return this.tableQueryWizardPanel.getRawText();
        }
        return "";
    }

    @Override
    public boolean exitOk() {
        return this.tableQueryWizardPanel.exitOk();
    }
}

