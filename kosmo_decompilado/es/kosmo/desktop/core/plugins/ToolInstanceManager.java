/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.core.plugins;

import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ToolInstanceManager {
    private static ToolInstanceManager _instance = null;
    protected Map<Class<PlugIn>, Collection<PlugIn>> pluginClassToInstanceMap = new HashMap<Class<PlugIn>, Collection<PlugIn>>();
    protected Map<Class<CursorTool>, Collection<CursorTool>> toolClassToInstanceMap = new HashMap<Class<CursorTool>, Collection<CursorTool>>();

    protected ToolInstanceManager() {
    }

    public static ToolInstanceManager instance() {
        if (_instance == null) {
            _instance = new ToolInstanceManager();
        }
        return _instance;
    }

    public void registerPlugIn(PlugIn plugIn) {
        Class<?> plugInClass = plugIn.getClass();
        if (this.pluginClassToInstanceMap.containsKey(plugInClass)) {
            Collection<PlugIn> plugInInstances = this.pluginClassToInstanceMap.get(plugInClass);
            plugInInstances.add(plugIn);
        } else {
            ArrayList<PlugIn> plugInInstances = new ArrayList<PlugIn>();
            plugInInstances.add(plugIn);
            this.pluginClassToInstanceMap.put(plugInClass, plugInInstances);
        }
    }

    public void unRegisterPlugIn(PlugIn plugIn) {
        Class<?> plugInClass = plugIn.getClass();
        this.pluginClassToInstanceMap.remove(plugInClass);
    }

    public void registerCursorTool(CursorTool tool) {
        Class<?> toolClass = tool.getClass();
        if (this.toolClassToInstanceMap.containsKey(toolClass)) {
            Collection<CursorTool> cursorToolInstances = this.toolClassToInstanceMap.get(toolClass);
            cursorToolInstances.add(tool);
        } else {
            ArrayList<CursorTool> cursorToolInstances = new ArrayList<CursorTool>();
            cursorToolInstances.add(tool);
            this.toolClassToInstanceMap.put(toolClass, cursorToolInstances);
        }
    }

    public void unRegisterCursorTool(CursorTool tool) {
        Class<?> toolClass = tool.getClass();
        this.toolClassToInstanceMap.remove(toolClass);
    }

    public Collection<PlugIn> getPlugInInstances(Class<PlugIn> plugInClass) {
        return this.pluginClassToInstanceMap.get(plugInClass);
    }

    public Collection<CursorTool> getCursorToolInstances(Class<CursorTool> cursorToolClass) {
        return this.toolClassToInstanceMap.get(cursorToolClass);
    }
}

