/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.collections.BidiMap
 *  org.apache.commons.collections.bidimap.DualHashBidiMap
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorManager;
import es.kosmo.desktop.gui.layouts.WrapLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

public class WorkbenchToolBar
extends EnableableToolBar {
    private static final long serialVersionUID = 1L;
    private BidiMap cursorToolClassToButtonMap = new DualHashBidiMap();
    private BidiMap plugInClassToButtonMap = new DualHashBidiMap();
    private LayerViewPanelProxy layerViewPanelProxy;
    private TaskMonitorManager taskMonitorManager = null;
    private CursorTool defaultCursorTool;
    private EnableCheck cursorToolEnableCheck = new EnableCheck(){

        @Override
        public String check(JComponent component) {
            return null;
        }
    };
    private ButtonGroup cursorToolButtonGroup;
    private CursorTool defaultEditingCursorTool;

    public WorkbenchToolBar(LayerViewPanelProxy layerViewPanelProxy) {
        this(layerViewPanelProxy, new ButtonGroup());
    }

    public WorkbenchToolBar(LayerViewPanelProxy layerViewPanelProxy, ButtonGroup cursorToolButtonGroup) {
        this.cursorToolButtonGroup = cursorToolButtonGroup;
        this.layerViewPanelProxy = layerViewPanelProxy;
    }

    public AbstractButton getButton(Class<?> cursorToolClass) {
        Assert.isTrue((boolean)CursorTool.class.isAssignableFrom(cursorToolClass));
        return (AbstractButton)this.cursorToolClassToButtonMap.get(cursorToolClass);
    }

    public AbstractButton getPlugInButton(Class<?> plugInButtonClass) {
        Assert.isTrue((boolean)PlugIn.class.isAssignableFrom(plugInButtonClass));
        return (AbstractButton)this.plugInClassToButtonMap.get(plugInButtonClass);
    }

    public void setCursorToolEnableCheck(EnableCheck cursorToolEnableCheck) {
        this.cursorToolEnableCheck = cursorToolEnableCheck;
    }

    public void setTaskMonitorManager(TaskMonitorManager taskMonitorManager) {
        this.taskMonitorManager = taskMonitorManager;
    }

    public ToolConfig addCursorTool(CursorTool cursorTool) {
        return this.addCursorTool(cursorTool.getName(), cursorTool);
    }

    public ToolConfig addCursorTool(String tooltip, CursorTool cursorTool) {
        JToggleButton button = new JToggleButton();
        return this.addCursorTool(tooltip, cursorTool, button);
    }

    public ToolConfig addCursorTool(String tooltip, CursorTool cursorTool, EnableCheck cursorToolEnableCheck) {
        JToggleButton button = new JToggleButton();
        return this.addCursorTool(tooltip, cursorTool, cursorTool.getIcon(), button, cursorToolEnableCheck);
    }

    public ToolConfig addCursorTool(String tooltip, CursorTool cursorTool, Icon toolIcon, EnableCheck cursorToolEnableCheck) {
        JToggleButton button = new JToggleButton();
        return this.addCursorTool(tooltip, cursorTool, toolIcon, button, cursorToolEnableCheck);
    }

    private ToolConfig addCursorTool(String tooltip, CursorTool cursorTool, Icon toolIcon, JToggleButton button, EnableCheck cursorToolEnableCheck) {
        this.cursorToolButtonGroup.add(button);
        this.cursorToolClassToButtonMap.put(cursorTool.getClass(), (Object)button);
        final QuasimodeTool quasimodeTool = QuasimodeTool.addStandardQuasimodes(cursorTool);
        this.add(button, tooltip, toolIcon, new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (WorkbenchToolBar.this.layerViewPanelProxy.getLayerViewPanel() != null) {
                    WorkbenchToolBar.this.layerViewPanelProxy.getLayerViewPanel().setCurrentCursorTool(quasimodeTool);
                }
            }
        }, cursorToolEnableCheck);
        if (this.cursorToolButtonGroup.getButtonCount() == 1) {
            this.cursorToolButtonGroup.setSelected(button.getModel(), true);
            this.reClickSelectedCursorToolButton();
        }
        return new ToolConfig(button, quasimodeTool);
    }

    private ToolConfig addCursorTool(String tooltip, CursorTool cursorTool, JToggleButton button) {
        this.cursorToolButtonGroup.add(button);
        this.cursorToolClassToButtonMap.put(cursorTool.getClass(), (Object)button);
        final QuasimodeTool quasimodeTool = QuasimodeTool.addStandardQuasimodes(cursorTool);
        this.add(button, tooltip, cursorTool.getIcon(), new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (WorkbenchToolBar.this.layerViewPanelProxy.getLayerViewPanel() != null) {
                    WorkbenchToolBar.this.layerViewPanelProxy.getLayerViewPanel().setCurrentCursorTool(quasimodeTool);
                }
            }
        }, this.cursorToolEnableCheck);
        if (this.cursorToolButtonGroup.getButtonCount() == 1) {
            this.cursorToolButtonGroup.setSelected(button.getModel(), true);
            this.reClickSelectedCursorToolButton();
        }
        return new ToolConfig(button, quasimodeTool);
    }

    public ButtonGroup getButtonGroup() {
        return this.cursorToolButtonGroup;
    }

    public JToggleButton getSelectedCursorToolButton() {
        Enumeration<AbstractButton> e = this.cursorToolButtonGroup.getElements();
        while (e.hasMoreElements()) {
            JToggleButton button = (JToggleButton)e.nextElement();
            if (button.getModel() != this.cursorToolButtonGroup.getSelection()) continue;
            return button;
        }
        return null;
    }

    public void reClickSelectedCursorToolButton() {
        if (this.cursorToolButtonGroup.getButtonCount() == 0) {
            return;
        }
        if (this.getSelectedCursorToolButton() != null) {
            this.getSelectedCursorToolButton().doClick();
        }
    }

    public JButton addPlugIn(Icon icon, PlugIn plugIn, EnableCheck enableCheck, WorkbenchContext workbenchContext) {
        return this.addPlugIn(icon, null, plugIn, enableCheck, workbenchContext);
    }

    public JButton addPlugIn(PlugIn plugIn, WorkbenchContext workbenchContext) {
        return this.addPlugIn(plugIn.getIcon(), plugIn.getDisabledIcon(), plugIn, plugIn.getCheck(), workbenchContext);
    }

    public JButton addPlugIn(Icon enableIcon, Icon disableIcon, PlugIn plugIn, EnableCheck enableCheck, WorkbenchContext workbenchContext) {
        JButton button = new JButton();
        button.setDisabledIcon(disableIcon);
        this.add(button, plugIn.getName(), enableIcon, AbstractPlugIn.toActionListener(plugIn, workbenchContext, this.taskMonitorManager), enableCheck);
        this.plugInClassToButtonMap.put(plugIn.getClass(), (Object)button);
        return button;
    }

    public JButton addPlugIn(Icon enableIcon, Icon disableIcon, PlugIn plugIn, String buttonText, EnableCheck enableCheck, WorkbenchContext workbenchContext) {
        JButton button = new JButton(buttonText);
        button.setDisabledIcon(disableIcon);
        this.add(button, plugIn.getName(), enableIcon, AbstractPlugIn.toActionListener(plugIn, workbenchContext, this.taskMonitorManager), enableCheck);
        this.plugInClassToButtonMap.put(plugIn.getClass(), (Object)button);
        return button;
    }

    public void removePlugIn(PlugIn plugIn) {
        AbstractButton button = this.getPlugInButton(plugIn.getClass());
        this.plugInClassToButtonMap.remove(plugIn.getClass());
        this.remove(button);
    }

    public void removeCursorTool(CursorTool cursorTool) {
        AbstractButton button = this.getButton(cursorTool.getClass());
        this.cursorToolClassToButtonMap.remove(cursorTool.getClass());
        this.cursorToolButtonGroup.remove(button);
        if (cursorTool.isGestureInProgress()) {
            cursorTool.deactivate();
        }
        this.remove(button);
    }

    public CursorTool getDefaultCursorTool() {
        return this.defaultCursorTool;
    }

    public void setDefaultCursorTool(CursorTool defaultCursorTool) {
        this.defaultCursorTool = defaultCursorTool;
    }

    public void setDefaultEditingCursorTool(CursorTool defaultEditingCursorTool) {
        this.defaultEditingCursorTool = defaultEditingCursorTool;
    }

    public CursorTool getDefaultEditingCursorTool() {
        return this.defaultEditingCursorTool;
    }

    @Override
    public void updateEnabledState() {
        Iterator i = this.buttonToEnableCheckMap.keySet().iterator();
        while (i.hasNext()) {
            AbstractButton component;
            EnableCheck enableCheck = (EnableCheck)this.buttonToEnableCheckMap.get(component = (AbstractButton)i.next());
            String error = enableCheck.check(component);
            component.setEnabled(error == null);
            if (error != null) {
                component.setToolTipText("<HTML><B>" + component.getName() + "</B><BR>" + error + "</HTML>");
                continue;
            }
            component.setToolTipText("<HTML><B>" + component.getName() + "</B>");
        }
    }

    public void setLayoutAlignment(int alignment) {
        this.setLayout(new WrapLayout(alignment));
    }

    public static class ToolConfig {
        private JToggleButton button;
        private QuasimodeTool quasimodeTool;

        public ToolConfig(JToggleButton button, QuasimodeTool quasimodeTool) {
            this.button = button;
            this.quasimodeTool = quasimodeTool;
        }

        public JToggleButton getButton() {
            return this.button;
        }

        public QuasimodeTool getQuasimodeTool() {
            return this.quasimodeTool;
        }
    }
}

