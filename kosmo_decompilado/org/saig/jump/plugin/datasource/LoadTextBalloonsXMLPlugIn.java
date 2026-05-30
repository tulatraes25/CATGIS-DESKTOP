/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.textballoon.XMLTextBalloonDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.balloon.XmlbFilter;

public class LoadTextBalloonsXMLPlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(XMLTextBalloonDataSource.class);
    public static final String NAME = I18N.getString(LoadTextBalloonsXMLPlugIn.class, "load-balloons-from-xml");
    public static final Icon ICON = IconLoader.icon("addbocata.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new XmlbFilter());
        TextBalloonLayer globeLayer = new TextBalloonLayer();
        int returnVal = fc.showOpenDialog(context.getWorkbenchFrame());
        if (returnVal == 0) {
            XMLTextBalloonDataSource ds;
            File file;
            block4: {
                try {
                    file = fc.getSelectedFile();
                    ds = XMLTextBalloonDataSource.parseXML(file);
                    if (ds != null) break block4;
                    context.getWorkbenchFrame().warnUser(I18N.getString(this.getClass(), "file-was-not-a-valid-balloons-file"));
                    return false;
                }
                catch (FileNotFoundException e) {
                    LOGGER.info((Object)"", (Throwable)e);
                    context.getWorkbenchFrame().warnUser(I18N.getString(this.getClass(), "file-not-exists"));
                    return false;
                }
            }
            globeLayer.setDataSource(ds);
            globeLayer.setName(file.getName());
            globeLayer.setVisible(true);
            Collection<Category> selectedCategories = context.getLayerNamePanel().getSelectedCategories();
            context.getLayerManager().addLayerable(selectedCategories.isEmpty() ? StandardCategoryNames.WORKING : selectedCategories.iterator().next().toString(), globeLayer);
            globeLayer.setLayerManager(context.getLayerManager());
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

