/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.cit.gvsig.gui.GUIUtil
 */
package org.saig.jump.plugin.data;

import com.iver.cit.gvsig.gui.GUIUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.datasource.DBFPanel;
import es.kosmo.desktop.widgets.datasource.GenericJDBCDatabasePanel;
import es.kosmo.desktop.widgets.datasource.LoadTableDialog;
import es.kosmo.desktop.widgets.datasource.MDBPanel;
import java.awt.Component;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.TableFactory;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.widgets.DataManagerPanel;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.jump.lang.I18N;

public class AddTablePlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.data.AddTablePlugIn.Load-table")) + "...";
    public static final Icon ICON = IconLoader.icon("addTable.png");

    @Override
    public void initialize(PlugInContext context) throws Exception {
        DBFPanel dbfPanel = new DBFPanel();
        MDBPanel mdbPanel = new MDBPanel();
        GenericJDBCDatabasePanel genericJDBCDatabasePanel = new GenericJDBCDatabasePanel();
        DataManagerPanel.getDialog().registerTableSelectionPanel(dbfPanel);
        DataManagerPanel.getDialog().registerTableSelectionPanel(mdbPanel);
        DataManagerPanel.getDialog().registerTableSelectionPanel(genericJDBCDatabasePanel);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        LoadTableDialog dialog = DataManagerPanel.getDialog();
        dialog.refresh();
        GUIUtil.centreOnScreen((Component)dialog);
        dialog.setVisible(true);
        return dialog.wasOkPressed();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
    }

    @Override
    public EnableCheck getCheck() {
        return AddTablePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.data.AddTablePlugIn.Loading-selected-tables")) + "...");
        monitor.allowCancellationRequests();
        DataManager dataManager = JUMPWorkbench.getFrameInstance().getContext().getDataManager();
        LoadTableDialog dialog = DataManagerPanel.getDialog();
        List<? extends TableRecordDataSource> dataSources = dialog.getTableDataSources();
        int cont = 0;
        int totalDS = dataSources.size();
        Iterator<? extends TableRecordDataSource> itDS = dataSources.iterator();
        while (itDS.hasNext() && !monitor.isCancelRequested()) {
            TableRecordDataSource currentDS = itDS.next();
            monitor.report(cont++, totalDS, currentDS.getName());
            Table recordCollection = TableFactory.getRecordCollection(currentDS);
            ViewTableFrame dataFrame = new ViewTableFrame(recordCollection, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
            dataManager.addTable(dataFrame);
        }
        if (monitor.isCancelRequested()) {
            this.warnOperationCancelled(context);
        }
    }
}

