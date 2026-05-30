/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.balloon;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.saig.core.dao.datasource.filedatasource.textballoon.XMLTextBalloonDataSource;
import org.saig.jump.lang.I18N;

public class AddTextBalloonLayerPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.balloon.AddTextBalloonLayerPlugIn.Add-balloons-layer");
    public static final ImageIcon ICON = IconLoader.icon("bocata.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        TextBalloonLayer globeLayer = new TextBalloonLayer();
        globeLayer.setDataSource(new XMLTextBalloonDataSource());
        globeLayer.setName(I18N.getString(this.getClass(), "balloons"));
        globeLayer.setVisible(true);
        Collection<Category> selectedCategories = context.getLayerNamePanel().getSelectedCategories();
        context.getLayerManager().addLayerable(selectedCategories.isEmpty() ? StandardCategoryNames.WORKING : ((Object)selectedCategories.iterator().next()).toString(), globeLayer);
        globeLayer.setLayerManager(context.getLayerManager());
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

    @Override
    public EnableCheck getCheck() {
        return AddTextBalloonLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
    }
}

