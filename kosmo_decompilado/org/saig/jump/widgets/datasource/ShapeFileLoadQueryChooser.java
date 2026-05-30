/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.datasource;

import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.FileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.datasource.LoadFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ShapeFileLoadQueryChooser
extends LoadFileDataSourceQueryChooser {
    private static final Logger LOGGER = Logger.getLogger(ShapeFileLoadQueryChooser.class);
    public static final String CHARSET_ENCODING_NAME_KEY = String.valueOf(ShapeFileLoadQueryChooser.class.getName()) + " - CHARSET ENCODING NAME KEY";
    public static final String OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES_KEY = String.valueOf(ShapeFileLoadQueryChooser.class.getName()) + " - OPTIMIZE SHAPEFILE MEMORY RESOURCES KEY";
    public static final Boolean DEFAULT_OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES = false;
    protected JComboBox selectCharsetCombobox;
    protected JCheckBox optimizeMemoryResourcesOptionCheckBox;

    public ShapeFileLoadQueryChooser(Class<?> dataSourceClass, String description, String[] extensions, WorkbenchContext contexto) {
        super(dataSourceClass, description, extensions, contexto);
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        Collection<DataSourceQuery> dataSourceQuerysFile = super.getDataSourceQueries();
        ArrayList<DataSourceQuery> result = new ArrayList<DataSourceQuery>();
        Iterator<DataSourceQuery> iterator = dataSourceQuerysFile.iterator();
        PersistentBlackboardPlugIn.get(this.context).put(CHARSET_ENCODING_NAME_KEY, this.selectCharsetCombobox.getSelectedItem());
        while (iterator.hasNext()) {
            DataSourceQuery dataSourceQuery = iterator.next();
            DataSource dataSource = dataSourceQuery.getDataSource();
            String selectedFile = (String)dataSource.getProperties().get("File");
            DataSourceQuery query = new DataSourceQuery(dataSource, selectedFile, dataSourceQuery.toString());
            result.add(query);
        }
        return result;
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
                if (!FileUtil.getExtension(file).equalsIgnoreCase("shp")) {
                    DialogFactory.showErrorDialog(JUMPWorkbench.getFrameInstance(), I18N.getMessage("org.saig.jump.widgets.datasource.ScalableIndexedShapeFileLoadQueryChooser.the-file-{0}-is-not-a-file-with-a-valid-extension-{1}", new Object[]{file.getAbsolutePath(), "shp"}), I18N.getString("org.saig.jump.widgets.datasource.ScalableIndexedShapeFileLoadQueryChooser.error-loading-file"));
                    return false;
                }
                ++i;
            }
        }
        return solucion;
    }

    @Override
    protected DataSourceQuery toDataSourceQuery(File file) {
        DataSourceQuery dataSourceQuery = super.toDataSourceQuery(file);
        dataSourceQuery.getDataSource().getProperties().put("Selected charset", this.selectCharsetCombobox.getSelectedItem());
        dataSourceQuery.getDataSource().getProperties().put("Optimize shapefile memory resources", this.optimizeMemoryResourcesOptionCheckBox.isSelected());
        return dataSourceQuery;
    }

    @Override
    protected Component getSouthComponent1() {
        Charset systemCharset;
        this.southComponent1 = new JPanel(new GridBagLayout());
        this.southComponent1.setBorder(BorderFactory.createTitledBorder(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Advanced-options")));
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        Vector charsetVector = new Vector(charsets.keySet());
        JLabel charsetSelection = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Coding")) + ":");
        this.selectCharsetCombobox = new JComboBox(charsetVector);
        String lastCharsetEncoding = (String)PersistentBlackboardPlugIn.get(this.context).get(CHARSET_ENCODING_NAME_KEY, ShapeFileDataSource.DEFAULT_STRING_CHARSET.name());
        try {
            systemCharset = Charset.forName(lastCharsetEncoding);
        }
        catch (UnsupportedCharsetException e) {
            LOGGER.error((Object)"", (Throwable)e);
            systemCharset = ShapeFileDataSource.DEFAULT_STRING_CHARSET;
        }
        this.selectCharsetCombobox.setSelectedItem(systemCharset.name());
        JLabel optimizeMemoryResourcesOptionLabel = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Optimize-memory-resources")) + ":");
        this.optimizeMemoryResourcesOptionCheckBox = new JCheckBox();
        this.optimizeMemoryResourcesOptionCheckBox.setToolTipText(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Bost-memory-consumption-reduction-for-lower-resource-computers-in-exchange-for-reducing-layer-rendering-speed"));
        Boolean optimizeResourcesOptionSelected = (Boolean)PersistentBlackboardPlugIn.get(this.context).get(OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES_KEY, (Object)false);
        this.optimizeMemoryResourcesOptionCheckBox.setSelected(optimizeResourcesOptionSelected);
        FormUtils.addRowInGBL((JComponent)this.southComponent1, 0, 0, charsetSelection, (JComponent)this.selectCharsetCombobox, false);
        FormUtils.addRowInGBL((JComponent)this.southComponent1, 1, 0, optimizeMemoryResourcesOptionLabel, (JComponent)this.optimizeMemoryResourcesOptionCheckBox);
        return this.southComponent1;
    }
}

