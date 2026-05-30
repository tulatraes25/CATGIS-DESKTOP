/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.io.File;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import org.saig.core.dao.datasource.AbstractTextBalloonDataSource;
import org.saig.core.dao.datasource.filedatasource.textballoon.XMLTextBalloonDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.balloon.XmlbFilter;

public class SaveTextBalloonsAsXMLPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString(SaveTextBalloonsAsXMLPlugIn.class, "save-balloons-layer-as-xml");
    public static final Icon ICON = IconLoader.icon("SaveTheme.gif");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layerable[] layers = context.getSelectedLayers();
        if (layers.length < 1 || !(layers[0] instanceof TextBalloonLayer)) {
            return false;
        }
        TextBalloonLayer blayer = (TextBalloonLayer)layers[0];
        XMLTextBalloonDataSource newDs = SaveTextBalloonsAsXMLPlugIn.saveBalloons(context.getWorkbenchFrame(), blayer.getDataSource());
        if (newDs != null) {
            blayer.setDataSource(newDs);
        }
        return true;
    }

    public static XMLTextBalloonDataSource saveBalloons(WorkbenchFrame wf, AbstractTextBalloonDataSource ds) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new XmlbFilter());
        int returnVal = fc.showSaveDialog(wf);
        if (returnVal == 0) {
            File file = fc.getSelectedFile();
            String extension = FileUtil.getExtension(file);
            if (StringUtil.isEmpty(extension)) {
                file = new File(String.valueOf(file.getAbsolutePath()) + ".xmlb");
            }
            XMLTextBalloonDataSource datasource = new XMLTextBalloonDataSource();
            datasource.setXmlFile(file);
            datasource.addTextBalloons(ds.getTextBalloons());
            datasource.commit();
            return datasource;
        }
        return null;
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

