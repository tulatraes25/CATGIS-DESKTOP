/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.FileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.datasource.LoadFileDataSourceQueryChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class GMLFileLoadQueryChooser
extends LoadFileDataSourceQueryChooser {
    public GMLFileLoadQueryChooser(Class<?> dataSourceClass, String description, String[] extensions, WorkbenchContext contexto) {
        super(dataSourceClass, description, extensions, contexto);
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        Collection<DataSourceQuery> dataSourceQuerysFile = super.getDataSourceQueries();
        ArrayList<DataSourceQuery> solucion = new ArrayList<DataSourceQuery>();
        for (DataSourceQuery dataSourceQuery : dataSourceQuerysFile) {
            DataSource dataSource = dataSourceQuery.getDataSource();
            String selectedFile = (String)dataSource.getProperties().get("File");
            DataSourceQuery query = new DataSourceQuery(dataSource, selectedFile, dataSourceQuery.toString());
            solucion.add(query);
        }
        return solucion;
    }

    @Override
    public boolean isInputValid() {
        boolean solucion = super.isInputValid();
        if (solucion) {
            FileDataSourceQueryChooser.FileChooserPanel chooserPanel = (FileDataSourceQueryChooser.FileChooserPanel)JUMPWorkbench.getFrameInstance().getContext().getBlackboard().get(this.LOAD_FILE_CHOOSER_PANEL_KEY);
            File[] files = chooserPanel.getChooser().getSelectedFiles();
            int i = 0;
            while (i < files.length && solucion) {
                File file = files[i];
                if (!FileUtil.getExtension(file).equalsIgnoreCase("gml")) {
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.widgets.datasource.ScalableIndexedShapeFileLoadQueryChooser.the-file-{0}-is-not-a-file-with-a-valid-extension-{1}", new Object[]{file.getAbsolutePath(), "gml"}), I18N.getString("org.saig.jump.widgets.datasource.ScalableIndexedShapeFileLoadQueryChooser.error-loading-file"));
                    return false;
                }
                ++i;
            }
        }
        return solucion;
    }
}

