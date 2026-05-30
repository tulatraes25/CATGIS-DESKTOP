/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.toolbox;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.Border;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

public class ToolboxDialog
extends JDialog {
    private static final long serialVersionUID = 1L;
    protected TaskMonitorManager taskMonitorManager = new TaskMonitorManager();
    protected int currentToolbarRow = 0;
    protected List<AbstractButton> buttons = new ArrayList<AbstractButton>();
    protected List<WorkbenchToolBar> toolBars = new ArrayList<WorkbenchToolBar>();
    protected GUIUtil.Location initialLocation = new GUIUtil.Location(0, false, 0, false);
    protected WorkbenchContext context;
    protected JPanel centerPanel;
    protected JPanel toolbarsPanel;

    public ToolboxDialog(final WorkbenchContext context) {
        super(context.getWorkbench().getFrame(), "", false);
        try {
            this.jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.context = context;
        context.getWorkbench().getFrame().addToolbarDialog(this);
        this.setResizable(true);
        this.setDefaultCloseOperation(1);
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentShown(ComponentEvent e) {
                if (ToolboxDialog.this.firstButton() != null) {
                    ToolboxDialog.this.firstButton().doClick();
                }
                ToolboxDialog.this.updateEnabledState();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                if (ToolboxDialog.this.buttons.contains(context.getWorkbench().getFrame().getToolBar().getSelectedCursorToolButton())) {
                    context.getWorkbench().getFrame().getToolBar().getButtonGroup().getElements().nextElement().doClick();
                }
            }
        });
    }

    public AbstractButton getButton(Class<?> cursorToolClass) {
        for (WorkbenchToolBar toolBar : this.toolBars) {
            AbstractButton button = toolBar.getButton(cursorToolClass);
            if (button == null) continue;
            return button;
        }
        return null;
    }

    public WorkbenchToolBar getToolBar() {
        if (this.toolBars.isEmpty()) {
            this.addToolBar();
        }
        return this.toolBars.get(this.toolBars.size() - 1);
    }

    public WorkbenchContext getContext() {
        return this.context;
    }

    public WorkbenchToolBar.ToolConfig add(CursorTool tool) {
        return this.add(tool, null);
    }

    public JButton addPlugIn(PlugIn plugIn) {
        return this.addPlugIn(plugIn.getIcon(), plugIn, plugIn.getCheck(), this.getContext());
    }

    public JButton addPlugIn(Icon icon, PlugIn plugIn, EnableCheck enableCheck, WorkbenchContext workbenchContext) {
        JButton button = this.getToolBar().addPlugIn(icon, plugIn, enableCheck, workbenchContext);
        return button;
    }

    public void addPlugIn(JButton button, EnableCheck enableCheck) {
        MultiEnableCheck trueCheck = null;
        trueCheck = enableCheck == null ? new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(JUMPWorkbench.getPostGenericCheck()) : new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(enableCheck).add(JUMPWorkbench.getPostGenericCheck());
        this.getToolBar().setEnableCheck(button, trueCheck);
        button.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ToolboxDialog.this.context.getWorkbench().getFrame().toFront();
            }
        });
    }

    public WorkbenchToolBar.ToolConfig add(CursorTool tool, EnableCheck enableCheck) {
        WorkbenchToolBar.ToolConfig config = this.getToolBar().addCursorTool(tool);
        this.buttons.add(config.getButton());
        MultiEnableCheck trueCheck = null;
        trueCheck = enableCheck == null ? new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(JUMPWorkbench.getPostGenericCheck()) : new MultiEnableCheck().add(JUMPWorkbench.getPreGenericCheck()).add(enableCheck).add(JUMPWorkbench.getPostGenericCheck());
        this.getToolBar().setEnableCheck(config.getButton(), trueCheck);
        config.getButton().addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ToolboxDialog.this.context.getWorkbench().getFrame().toFront();
            }
        });
        return config;
    }

    public AbstractButton firstButton() {
        if (this.buttons.isEmpty()) {
            return null;
        }
        return this.buttons.iterator().next();
    }

    public void addToolBar() {
        this.addToolBar(0, null);
    }

    public void addToolBar(int vgap, Border border) {
        WorkbenchToolBar newToolBar = new WorkbenchToolBar(this.context, this.context.getWorkbench().getFrame().getToolBar().getButtonGroup());
        newToolBar.setTaskMonitorManager(this.taskMonitorManager);
        this.toolBars.add(newToolBar);
        this.getToolBar().setBorder(border);
        this.getToolBar().setFloatable(false);
        FormUtils.addRowInGBL(this.toolbarsPanel, this.currentToolbarRow++, 0, this.getToolBar());
    }

    public void finishAddingComponents() {
        this.pack();
        GUIUtil.setLocation(this, this.initialLocation, this.context.getWorkbench().getFrame().getDesktopPane());
    }

    private void jbInit() throws Exception {
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.setContentPane(mainPanel);
        this.centerPanel = new JPanel(new BorderLayout());
        this.toolbarsPanel = new JPanel(new GridBagLayout());
        mainPanel.add((Component)this.toolbarsPanel, "North");
        mainPanel.add((Component)this.centerPanel, "Center");
    }

    public JPanel getCenterPanel() {
        return this.centerPanel;
    }

    public JPanel getToolbarPanel() {
        return this.toolbarsPanel;
    }

    public void updateEnabledState() {
        for (WorkbenchToolBar toolBar : this.toolBars) {
            toolBar.updateEnabledState();
        }
    }

    public void setInitialLocation(GUIUtil.Location location) {
        this.initialLocation = location;
    }

    public void addSeparationLine(String text) {
        FormUtils.addRowInGBL(this.toolbarsPanel, this.currentToolbarRow++, 0, FormUtils.getTitleLabel(text));
    }

    public void addFormRow(JComponent label, JComponent component) {
        FormUtils.addRowInGBL((JComponent)this.toolbarsPanel, this.currentToolbarRow++, 0, label, component);
    }
}

