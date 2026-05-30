/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.SaveTextBalloonsAsXMLPlugIn;

public class SaveTextBalloonsPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString(SaveTextBalloonsPlugIn.class, "save-balloons-layer");
    public static final Icon ICON = IconLoader.icon("SaveTheme.gif");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layerable[] layers = context.getSelectedLayers();
        if (layers.length < 1 || !(layers[0] instanceof TextBalloonLayer)) {
            return false;
        }
        TextBalloonLayer blayer = (TextBalloonLayer)layers[0];
        if (!blayer.getDataSource().commit()) {
            SaveTextBalloonsAsXMLPlugIn plugin = new SaveTextBalloonsAsXMLPlugIn();
            plugin.execute(context);
        }
        return true;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

