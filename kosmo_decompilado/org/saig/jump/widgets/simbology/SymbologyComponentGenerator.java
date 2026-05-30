/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 */
package org.saig.jump.widgets.simbology;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import org.apache.commons.lang.ArrayUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.simbology.LoadSymbologyFileIntoLayerPlugIn;
import org.saig.jump.widgets.simbology.SymbologyFileFilter;

public class SymbologyComponentGenerator {
    public static SymbologyComponentGenerator instance;

    private SymbologyComponentGenerator() {
    }

    public static SymbologyComponentGenerator getInstance() {
        if (instance == null) {
            instance = new SymbologyComponentGenerator();
        }
        return instance;
    }

    public void generateSymbologyToolBar(String layerName, String simbologyDirectoryPath, ToolboxDialog dialog, boolean addBorder) {
        File symbologyDirectory = new File(simbologyDirectoryPath);
        Object[] validSources = symbologyDirectory.listFiles(new SymbologyFileFilter());
        if (!ArrayUtils.isEmpty((Object[])validSources)) {
            dialog.addToolBar();
            if (addBorder) {
                dialog.getToolBar().setBorder(BorderFactory.createTitledBorder(String.valueOf(layerName) + " - " + I18N.getString(this.getClass(), "symbology")));
            }
            int i = 0;
            while (i < validSources.length) {
                Object currentFile = validSources[i];
                String iconPath = String.valueOf(FileUtil.nameWithoutExtension((File)currentFile)) + ".png";
                File iconFile = new File(iconPath);
                ImageIcon currentIcon = null;
                currentIcon = iconFile.canRead() ? new ImageIcon(iconPath) : IconLoader.DEFAULT_UNKNOW_ICON;
                LoadSymbologyFileIntoLayerPlugIn loadSymbologyPlugIn = new LoadSymbologyFileIntoLayerPlugIn(layerName, ((File)currentFile).getAbsolutePath(), currentIcon);
                dialog.getToolBar().addPlugIn(loadSymbologyPlugIn.getIcon(), loadSymbologyPlugIn, loadSymbologyPlugIn.getCheck(), dialog.getContext());
                ++i;
            }
        }
    }
}

