/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.widgets.tables.management.DetailTableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.TableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.control.DBControlPanel;
import org.saig.core.model.data.widgets.tables.management.control.DBDetailControlPanel;
import org.saig.core.model.data.widgets.tables.management.definition.DefinitionMapping;
import org.saig.core.model.data.widgets.tables.management.definition.DefinitionMappingHelper;
import org.saig.jump.lang.I18N;

public class TableManagementHelper {
    protected DefinitionMapping definitionMapping;
    private final boolean useCache;

    public TableManagementHelper() {
        this(null, false);
    }

    public TableManagementHelper(String definitionXmlPath) {
        this(definitionXmlPath, false);
    }

    public TableManagementHelper(String definitionXmlPath, boolean useCache) {
        this.useCache = useCache;
        DefinitionMappingHelper definitionMappingHelper = new DefinitionMappingHelper();
        if (definitionXmlPath != null) {
            definitionMappingHelper.setDefinitionMappingLocation(definitionXmlPath);
        }
        this.definitionMapping = definitionMappingHelper.getDefinitionMapping();
    }

    public JInternalFrame getTableManagementDialog(String table) throws Exception {
        DBControlPanel controlPanel = new DBControlPanel();
        TableManagementPanel tmPanel = new TableManagementPanel(table, controlPanel, this.definitionMapping.getTable(table), this.useCache);
        JInternalFrame internalFrame = new JInternalFrame(I18N.getMessage("org.saig.core.model.data.widgets.tables.management.TableManagementHelper.{0}-management", new Object[]{table}), true, true, true);
        Container c = internalFrame.getContentPane();
        c.add((Component)tmPanel, "Center");
        return internalFrame;
    }

    public JDialog getMasterDetailManagementDialog(String master, String detail, String linkField) throws Exception {
        DBControlPanel controlPanelMaster = new DBControlPanel();
        TableManagementPanel tmPanelMaster = new TableManagementPanel(master, controlPanelMaster, this.definitionMapping.getTable(master), this.useCache);
        DBDetailControlPanel controlPanelDetail = new DBDetailControlPanel(tmPanelMaster);
        DetailTableManagementPanel tmPanelDetail = new DetailTableManagementPanel(detail, controlPanelDetail, tmPanelMaster, linkField, this.definitionMapping.getTable(detail));
        JDialog dialogo = new JDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.core.model.data.widgets.tables.management.TableManagementHelper.{0}-management", new Object[]{master}), true);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        dialogo.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, tmPanelMaster);
        FormUtils.addRowInGBL(mainPanel, 2, 0, tmPanelDetail);
        return dialogo;
    }
}

