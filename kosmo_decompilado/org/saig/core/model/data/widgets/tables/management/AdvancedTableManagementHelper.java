/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.widgets.tables.management;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.io.InputStream;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.data.widgets.tables.management.AdvancedDetailTableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.AdvancedTableManagementPanel;
import org.saig.core.model.data.widgets.tables.management.control.AdvancedControlPanel;
import org.saig.core.model.data.widgets.tables.management.control.AdvancedDBControlPanel;
import org.saig.core.model.data.widgets.tables.management.control.AdvancedDBDetailControlPanel;
import org.saig.core.model.data.widgets.tables.management.control.AdvancedNotEditableDBControlPanel;
import org.saig.core.model.data.widgets.tables.management.definition.DefinitionMapping;
import org.saig.core.model.data.widgets.tables.management.definition.DefinitionMappingHelper;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class AdvancedTableManagementHelper {
    private static final Logger LOGGER = Logger.getLogger(AdvancedTableManagementHelper.class);
    protected DefinitionMapping definitionMapping;
    private final boolean useCache;

    public AdvancedTableManagementHelper() {
        this((String)null, false);
        LOGGER.warn((Object)I18N.getString(this.getClass(), "a-configuration-file-was-not-indicated-for-tables-management-using-default-configuration"));
    }

    public AdvancedTableManagementHelper(String definitionXmlPath) {
        this(definitionXmlPath, false);
    }

    public AdvancedTableManagementHelper(String definitionXmlPath, boolean useCache) {
        this.useCache = useCache;
        DefinitionMappingHelper definitionMappingHelper = new DefinitionMappingHelper();
        if (definitionXmlPath != null) {
            definitionMappingHelper.setDefinitionMappingLocation(definitionXmlPath);
        }
        this.definitionMapping = definitionMappingHelper.getDefinitionMapping();
    }

    public AdvancedTableManagementHelper(InputStream definitionXml) {
        this(definitionXml, false);
    }

    public AdvancedTableManagementHelper(InputStream definitionXml, boolean useCache) {
        this.useCache = useCache;
        DefinitionMappingHelper definitionMappingHelper = new DefinitionMappingHelper();
        if (definitionXml != null) {
            definitionMappingHelper.setDefinitionMappingXml(definitionXml);
        }
        this.definitionMapping = definitionMappingHelper.getDefinitionMapping();
    }

    public JInternalFrame getTableManagementDialog(String table) throws Exception {
        return this.getTableManagementDialog(table, false);
    }

    public JInternalFrame getTableManagementDialog(String table, final boolean notEditable) throws Exception {
        String notEditableTag = notEditable ? I18N.getString(this.getClass(), "read-only") : "";
        final JInternalFrame internalFrame = new JInternalFrame(String.valueOf(I18N.getMessage("org.saig.core.model.data.widgets.tables.management.TableManagementHelper.{0}-management", new Object[]{table})) + notEditableTag, true, true);
        final AdvancedDBControlPanel controlPanel = notEditable ? new AdvancedNotEditableDBControlPanel() : new AdvancedDBControlPanel();
        final AdvancedTableManagementPanel tmPanel = new AdvancedTableManagementPanel(table, (AdvancedControlPanel)controlPanel, this.definitionMapping.getTable(table), internalFrame, notEditable, this.useCache);
        Container c = internalFrame.getContentPane();
        c.add((Component)tmPanel, "Center");
        internalFrame.setDefaultCloseOperation(0);
        internalFrame.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent ife) {
                if (notEditable || !controlPanel.isEditing() || DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "there-are-changes-not-saved-yet-and-if-you-close-the-window-it-will-be-lost-do-you-want-to-close-it-anyway"), I18N.getString(this.getClass(), "changes-not-saved")) == 0) {
                    tmPanel.close();
                    internalFrame.dispose();
                }
            }
        });
        return internalFrame;
    }

    public JInternalFrame getTableManagementDialog(Layer layer, final boolean notEditable) throws Exception {
        String notEditableTag = notEditable ? I18N.getString(this.getClass(), "read-only") : "";
        final JInternalFrame internalFrame = new JInternalFrame(String.valueOf(I18N.getMessage("org.saig.core.model.data.widgets.tables.management.TableManagementHelper.{0}-management", new Object[]{layer.getName()})) + notEditableTag, true, true);
        final AdvancedDBControlPanel controlPanel = notEditable ? new AdvancedNotEditableDBControlPanel() : new AdvancedDBControlPanel();
        final AdvancedTableManagementPanel tmPanel = new AdvancedTableManagementPanel(layer, (AdvancedControlPanel)controlPanel, this.definitionMapping.getTable(layer.getName()), internalFrame, notEditable, this.useCache);
        Container c = internalFrame.getContentPane();
        c.add((Component)tmPanel, "Center");
        internalFrame.setDefaultCloseOperation(0);
        internalFrame.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent ife) {
                if (notEditable || !controlPanel.isEditing() || DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString(this.getClass(), "there-are-changes-not-saved-yet-and-if-you-close-the-window-it-will-be-lost-do-you-want-to-close-it-anyway"), I18N.getString(this.getClass(), "changes-not-saved")) == 0) {
                    tmPanel.close();
                    internalFrame.dispose();
                }
            }
        });
        return internalFrame;
    }

    public JInternalFrame getMasterDetailManagementDialog(String master, String detail, String linkField) throws Exception {
        JInternalFrame dialogo = new JInternalFrame(I18N.getMessage("org.saig.core.model.data.widgets.tables.management.TableManagementHelper.{0}-management", new Object[]{master}), true);
        AdvancedDBControlPanel controlPanelMaster = new AdvancedDBControlPanel();
        final AdvancedTableManagementPanel tmPanelMaster = new AdvancedTableManagementPanel(master, (AdvancedControlPanel)controlPanelMaster, this.definitionMapping.getTable(master), dialogo, false, this.useCache);
        AdvancedDBDetailControlPanel controlPanelDetail = new AdvancedDBDetailControlPanel(tmPanelMaster);
        AdvancedDetailTableManagementPanel tmPanelDetail = new AdvancedDetailTableManagementPanel(detail, (AdvancedControlPanel)controlPanelDetail, tmPanelMaster, linkField, this.definitionMapping.getTable(detail), dialogo);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        dialogo.setContentPane(mainPanel);
        FormUtils.addRowInGBL(mainPanel, 1, 0, tmPanelMaster);
        FormUtils.addRowInGBL(mainPanel, 2, 0, tmPanelDetail);
        dialogo.addInternalFrameListener(new InternalFrameAdapter(){

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                tmPanelMaster.close();
            }
        });
        return dialogo;
    }
}

