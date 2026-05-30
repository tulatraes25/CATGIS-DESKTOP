/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.widgets.utils.topology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;
import org.saig.jump.widgets.utils.topology.AddNewTopologyRulePanel;
import org.saig.jump.widgets.utils.topology.ConfigureTopologyRulesTablePanel;

public class ConfigureTopologyRulesDialog
extends JDialog
implements ListSelectionListener {
    private static final long serialVersionUID = 1L;
    public static final String TITLE = I18N.getString(ConfigureTopologyRulesDialog.class, "topological-rules-configuration");
    private ConfigureTopologyRulesTablePanel tablePanel;
    private AddNewTopologyRulePanel newRulePanel;
    private JPanel buttonPanel;
    private WorkbenchToolBar topologyRulesToolbar;
    private PlugIn addTopologyRulePlugIn;
    private PlugIn removeTopologyRulePlugIn;
    private PlugIn removeAllTopologyRulesPlugIn;
    private OKCancelPanel okCancelPanel;
    private String filterLayerName;
    private TitledBorder panelBorder;

    public ConfigureTopologyRulesDialog(JFrame owner, boolean modal, List<ITopologyRelation> topologyRulesList, boolean onlyOneLayerMode) {
        this(owner, modal, topologyRulesList, null, onlyOneLayerMode);
    }

    public ConfigureTopologyRulesDialog(JFrame owner, boolean modal, List<ITopologyRelation> topologyRulesList, String filterLayerName, boolean onlyOneLayerMode) {
        super((Frame)owner, modal);
        this.filterLayerName = filterLayerName;
        this.updateTitle();
        this.initialize();
        this.setTopologyRulesList(topologyRulesList);
        this.newRulePanel.setOnlyOneLayerMode(onlyOneLayerMode);
        this.newRulePanel.refresh(filterLayerName, JUMPWorkbench.getFrameInstance().getContext().getLayerManager());
        this.topologyRulesToolbar.updateEnabledState();
        this.pack();
    }

    private void updateTitle() {
        if (StringUtils.isEmpty((String)this.filterLayerName)) {
            this.setTitle(TITLE);
        } else {
            this.setTitle(String.valueOf(TITLE) + " - " + I18N.getString(this.getClass(), "layer") + this.filterLayerName);
        }
    }

    private void setTopologyRulesList(List<ITopologyRelation> topologyRulesList) {
        this.getTablePanel().setTopologyRulesList(topologyRulesList);
    }

    private void initialize() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topologyRulesPanel = new JPanel(new BorderLayout());
        this.panelBorder = BorderFactory.createTitledBorder(this.getTablePanelTitle());
        topologyRulesPanel.setBorder(this.panelBorder);
        this.setContentPane(mainPanel);
        JScrollPane tableScrollPane = new JScrollPane(22, 30);
        int dim = this.getTablePanel().getTableSize();
        tableScrollPane.setPreferredSize(new Dimension(dim + 180, 250));
        tableScrollPane.setMinimumSize(new Dimension(dim + 180, 250));
        tableScrollPane.getViewport().add((Component)this.getTablePanel(), null);
        tableScrollPane.getVerticalScrollBar().setUnitIncrement(new JTable().getRowHeight());
        topologyRulesPanel.add((Component)this.getTablePanel().getTableHeader(), "North");
        topologyRulesPanel.add((Component)tableScrollPane, "Center");
        JPanel southPanel = new JPanel(new GridBagLayout());
        FormUtils.addRowInGBL(southPanel, 0, 0, this.getButtonPanel());
        FormUtils.addRowInGBL(southPanel, 1, 0, this.getNewRulePanel());
        FormUtils.addRowInGBL(southPanel, 2, 0, this.getOkCancelPanel());
        mainPanel.add((Component)topologyRulesPanel, "Center");
        mainPanel.add((Component)southPanel, "South");
    }

    private String getTablePanelTitle() {
        String panelTitle = I18N.getString(this.getClass(), "topological-rules-configured");
        if (!StringUtils.isEmpty((String)this.filterLayerName)) {
            panelTitle = I18N.getMessage(this.getClass(), "topological-rules-configured-for-layer-{0}", new Object[]{this.filterLayerName});
        }
        return panelTitle;
    }

    private JPanel getNewRulePanel() {
        if (this.newRulePanel == null) {
            this.newRulePanel = new AddNewTopologyRulePanel(this.filterLayerName);
        }
        return this.newRulePanel;
    }

    private JPanel getButtonPanel() {
        if (this.buttonPanel == null) {
            this.buttonPanel = new JPanel(new FlowLayout());
            WorkbenchContext wc = JUMPWorkbench.getFrameInstance().getContext();
            this.topologyRulesToolbar = new WorkbenchToolBar(null);
            this.addTopologyRulePlugIn = new PlugIn(){

                @Override
                public String getName() {
                    return I18N.getString(this.getClass(), "add-new-topological-rule");
                }

                @Override
                public Icon getIcon() {
                    return IconLoader.icon("add.png");
                }

                @Override
                public void initialize(PlugInContext context) throws Exception {
                }

                @Override
                public boolean execute(PlugInContext context) throws Exception {
                    ITopologyRelation relation = ConfigureTopologyRulesDialog.this.newRulePanel.getConfiguredTopologyRelation();
                    if (relation == null) {
                        DialogFactory.showWarningDialog(ConfigureTopologyRulesDialog.this, I18N.getString(this.getClass(), "configured-topological-rule-is-not-correct"), I18N.getString(this.getClass(), "incorrect-topological-rule"));
                        return false;
                    }
                    ArrayList<ITopologyRelation> relationsToAdd = new ArrayList<ITopologyRelation>();
                    relationsToAdd.add(relation);
                    ConfigureTopologyRulesDialog.this.tablePanel.addTopologyRules(relationsToAdd);
                    ConfigureTopologyRulesDialog.this.topologyRulesToolbar.updateEnabledState();
                    return true;
                }

                @Override
                public void finish(PlugInContext context) {
                }

                @Override
                public EnableCheck getCheck() {
                    return null;
                }

                @Override
                public Icon getDisabledIcon() {
                    return null;
                }
            };
            this.removeTopologyRulePlugIn = new PlugIn(){

                @Override
                public String getName() {
                    return I18N.getString(this.getClass(), "remove-selected-topological-rule");
                }

                @Override
                public Icon getIcon() {
                    return IconLoader.icon("delete_small.gif");
                }

                @Override
                public void initialize(PlugInContext context) throws Exception {
                }

                @Override
                public boolean execute(PlugInContext context) throws Exception {
                    ConfigureTopologyRulesDialog.this.tablePanel.removeSelectedTopologyRules();
                    ConfigureTopologyRulesDialog.this.topologyRulesToolbar.updateEnabledState();
                    return true;
                }

                @Override
                public void finish(PlugInContext context) {
                }

                @Override
                public EnableCheck getCheck() {
                    return new EnableCheck(){

                        @Override
                        public String check(JComponent component) {
                            if (!ConfigureTopologyRulesDialog.this.tablePanel.hasSelectedTopologyRules()) {
                                return I18N.getString(this.getClass(), "there-are-no-topological-rules-selected");
                            }
                            return null;
                        }
                    };
                }

                @Override
                public Icon getDisabledIcon() {
                    return null;
                }
            };
            this.removeAllTopologyRulesPlugIn = new PlugIn(){

                @Override
                public String getName() {
                    return I18N.getString(this.getClass(), "remove-all-topological-rules");
                }

                @Override
                public Icon getIcon() {
                    return IconLoader.icon("deleteAll.png");
                }

                @Override
                public void initialize(PlugInContext context) throws Exception {
                }

                @Override
                public boolean execute(PlugInContext context) throws Exception {
                    ConfigureTopologyRulesDialog.this.tablePanel.removeAllTopologyRules();
                    ConfigureTopologyRulesDialog.this.topologyRulesToolbar.updateEnabledState();
                    return true;
                }

                @Override
                public void finish(PlugInContext context) {
                }

                @Override
                public EnableCheck getCheck() {
                    return new EnableCheck(){

                        @Override
                        public String check(JComponent component) {
                            if (ConfigureTopologyRulesDialog.this.tablePanel.getTopologyRules().isEmpty()) {
                                return I18N.getString(this.getClass(), "there-are-no-topological-rules-configured");
                            }
                            return null;
                        }
                    };
                }

                @Override
                public Icon getDisabledIcon() {
                    return null;
                }
            };
            this.topologyRulesToolbar.setFloatable(false);
            this.topologyRulesToolbar.add(Box.createHorizontalGlue());
            this.topologyRulesToolbar.addPlugIn(this.addTopologyRulePlugIn, wc);
            this.topologyRulesToolbar.addPlugIn(this.removeTopologyRulePlugIn, wc);
            this.topologyRulesToolbar.addPlugIn(this.removeAllTopologyRulesPlugIn, wc);
            this.topologyRulesToolbar.add(Box.createHorizontalGlue());
            this.buttonPanel.add(this.topologyRulesToolbar);
        }
        return this.buttonPanel;
    }

    private OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigureTopologyRulesDialog.this.setVisible(false);
                }
            });
        }
        return this.okCancelPanel;
    }

    private ConfigureTopologyRulesTablePanel getTablePanel() {
        if (this.tablePanel == null) {
            this.tablePanel = new ConfigureTopologyRulesTablePanel(this.filterLayerName);
            this.tablePanel.addListSelectionListener(this);
        }
        return this.tablePanel;
    }

    public boolean wasOkPressed() {
        return this.okCancelPanel.wasOKPressed();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            this.okCancelPanel.setOKPressed(false);
        }
        super.setVisible(visible);
    }

    public List<ITopologyRelation> getTopologyRules() {
        return this.tablePanel.getTopologyRules();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        this.topologyRulesToolbar.updateEnabledState();
    }

    public void refresh(String selectedLayerName, boolean onlyOneLayerMode, LayerManager layerManager, List<ITopologyRelation> topologyRulesList) {
        this.filterLayerName = selectedLayerName;
        this.setTopologyRulesList(topologyRulesList);
        this.updateTitle();
        this.panelBorder.setTitle(this.getTablePanelTitle());
        this.newRulePanel.setOnlyOneLayerMode(onlyOneLayerMode);
        this.newRulePanel.refresh(selectedLayerName, layerManager);
        this.tablePanel.refresh(selectedLayerName);
    }
}

