/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.LoadFileDataSourceQueryChooser;
import java.util.ArrayList;
import java.util.Collection;

public class ImageFileLoadQueryChooser
extends LoadFileDataSourceQueryChooser {
    public static final String IMAGE_FILE_CHOOSER_PANEL_KEY = String.valueOf(ImageFileLoadQueryChooser.class.getName()) + " - LOAD FILE CHOOSER PANEL";

    public ImageFileLoadQueryChooser(Class<?> dataSourceClass, String description, String[] extensions, WorkbenchContext contexto) {
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
        return this.checkSelection();
    }
}

