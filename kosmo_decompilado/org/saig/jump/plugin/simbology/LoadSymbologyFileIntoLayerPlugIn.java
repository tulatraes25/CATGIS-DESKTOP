/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.exolab.castor.mapping.Mapping
 *  org.exolab.castor.xml.Unmarshaller
 */
package org.saig.jump.plugin.simbology;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerSimbology;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import javax.swing.Icon;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.saig.core.styling.StyleImpl;
import org.saig.jump.util.LoadXMLMappings;

public class LoadSymbologyFileIntoLayerPlugIn
extends AbstractPlugIn {
    protected String layerName;
    protected String symbologyFilePath;
    protected String name;
    protected Icon icon;

    public LoadSymbologyFileIntoLayerPlugIn(String layerName, String path, Icon icon) {
        this.layerName = layerName;
        this.symbologyFilePath = path;
        this.icon = icon;
        this.name = FileUtil.nameWithoutExtension(new File(this.symbologyFilePath).getName());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Icon getIcon() {
        return this.icon;
    }

    public EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createLayerNameMustExistCheck(this.layerName)).add(checkFactory.createFileMustBeReadableCheck(new File(this.symbologyFilePath)));
    }

    @Override
    public boolean execute(final PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        FileReader reader = new FileReader(this.symbologyFilePath);
        Mapping mappings = LoadXMLMappings.loadLayerSimbologyMappings();
        Unmarshaller unmar = new Unmarshaller(mappings);
        unmar.setWhitespacePreserve(true);
        final LayerSimbology simbology = (LayerSimbology)unmar.unmarshal((Reader)reader);
        final Layer selectedLayer = JUMPWorkbench.getLayer(this.layerName);
        org.saig.core.styling.Style oldStyle = selectedLayer.getModelStyle();
        final List<Style> jumpStyles = selectedLayer.getStyles();
        final org.saig.core.styling.Style cloneStyle = (org.saig.core.styling.Style)((StyleImpl)oldStyle).clone();
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() {
                selectedLayer.setModelStyle(simbology.getModelStyle());
                selectedLayer.setStyles(simbology.getJumpStyles());
                context.getLayerManager().fireLayerChanged(selectedLayer, LayerEventType.APPEARANCE_CHANGED);
            }

            @Override
            public void unexecute() {
                selectedLayer.setStyles(jumpStyles);
                selectedLayer.setModelStyle(cloneStyle);
                context.getLayerManager().fireLayerChanged(selectedLayer, LayerEventType.APPEARANCE_CHANGED);
            }
        }, context);
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        return this.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

