/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.widgets.datasource;

import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.core.dao.datasource.filedatasource.dxf.DxfWriterFactoryFinder;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
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
import org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser;

public class DXFFileSaveQueryChooser
extends SaveFileDataSourceQueryChooser {
    private static final Logger LOGGER = Logger.getLogger(DXFFileSaveQueryChooser.class);
    public static final String SAVE_GEOMETRIES_AS_BLOCKS_KEY = String.valueOf(DXFFileSaveQueryChooser.class.getName()) + " - SAVE GEOMETRIES AS BLOCKS";
    public static final String WRITE_FEATURE_ATTRIBUTES_AS_XDATA_KEY = String.valueOf(DXFFileSaveQueryChooser.class.getName()) + " - WRITE FEATURE ATTRIBUTES AS XDATA";
    public static final String WRITE_POINT_FCS_AS_INSERTS_WITH_ATTRS_KEY = String.valueOf(DXFFileSaveQueryChooser.class.getName()) + " - WRITE POINT FCS AS INSERT WITH ATTRS";
    public static final String SELECTED_DXF_VERSION = String.valueOf(DXFFileSaveQueryChooser.class.getName()) + " - SELECTED DXF VERSION";
    protected boolean southComponent1Initialized = false;
    protected JCheckBox cbSaveGeometriesAsBlocks;
    protected JCheckBox cbWriteFeatureAttributesAsXData;
    protected JCheckBox cbWritePointFcsAsInsertsWithAttrs;
    protected JComboBox selectDXFVersionComboBox;
    protected JComboBox selectCharsetCombobox;

    public DXFFileSaveQueryChooser(Class<?> dataSourceClass, String description, String[] extensions, WorkbenchContext context) {
        super(dataSourceClass, description, extensions, context);
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        PersistentBlackboardPlugIn.get(this.context).put(SAVE_GEOMETRIES_AS_BLOCKS_KEY, this.cbSaveGeometriesAsBlocks.isSelected());
        PersistentBlackboardPlugIn.get(this.context).put(WRITE_FEATURE_ATTRIBUTES_AS_XDATA_KEY, this.cbWriteFeatureAttributesAsXData.isSelected());
        PersistentBlackboardPlugIn.get(this.context).put(WRITE_POINT_FCS_AS_INSERTS_WITH_ATTRS_KEY, this.cbWritePointFcsAsInsertsWithAttrs.isSelected());
        PersistentBlackboardPlugIn.get(this.context).put(SELECTED_DXF_VERSION, this.selectDXFVersionComboBox.getSelectedItem());
        PersistentBlackboardPlugIn.get(this.context).put(ShapeFileLoadQueryChooser.CHARSET_ENCODING_NAME_KEY, this.selectCharsetCombobox.getSelectedItem());
        return super.getDataSourceQueries();
    }

    @Override
    protected Component getSouthComponent1() {
        if (!this.southComponent1Initialized) {
            Charset systemCharset;
            this.southComponent1 = new JPanel(new GridBagLayout());
            String labelOptions = I18N.getString("com.vividsolutions.jump.workbench.datasource.SaveShapeFileDataSourceQueryChooser.options");
            String labelSaveGeometryAsBlocksField = String.valueOf(I18N.getString("es.kosmo.desktop.widgets.datasource.DXFFileSaveQueryChooser.Save-geometries-as-blocks")) + ":";
            String labelWriteFeaturesFieldAsXData = String.valueOf(I18N.getString("es.kosmo.desktop.widgets.datasource.DXFFileSaveQueryChooser.Save-attributes-as-XData")) + ":";
            String labelWritePointFcsAsInsertsWithAttribs = String.valueOf(I18N.getString("es.kosmo.desktop.widgets.datasource.DXFFileSaveQueryChooser.Save-point-layers-as-unique-block-with-attributes")) + ":";
            String labelVersionField = String.valueOf(I18N.getString("es.kosmo.desktop.widgets.datasource.DXFFileSaveQueryChooser.DXF-version")) + ":";
            SortedMap<String, Charset> charsets = Charset.availableCharsets();
            Vector charsetVector = new Vector(charsets.keySet());
            JLabel charsetSelection = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Coding")) + ":");
            this.selectCharsetCombobox = new JComboBox(charsetVector);
            String lastCharsetEncoding = (String)PersistentBlackboardPlugIn.get(this.context).get(ShapeFileLoadQueryChooser.CHARSET_ENCODING_NAME_KEY, "windows-1252");
            try {
                systemCharset = Charset.forName(lastCharsetEncoding);
            }
            catch (UnsupportedCharsetException e) {
                LOGGER.error((Object)"", (Throwable)e);
                systemCharset = ShapeFileDataSource.DEFAULT_STRING_CHARSET;
            }
            this.selectCharsetCombobox.setSelectedItem(systemCharset.name());
            this.southComponent1.setBorder(BorderFactory.createTitledBorder(labelOptions));
            this.cbSaveGeometriesAsBlocks = new JCheckBox();
            this.cbWriteFeatureAttributesAsXData = new JCheckBox();
            this.cbWritePointFcsAsInsertsWithAttrs = new JCheckBox();
            this.selectDXFVersionComboBox = new JComboBox<String>(DxfWriterFactoryFinder.getAvailableWriterDescriptions());
            FormUtils.addRowInGBL((JComponent)this.southComponent1, 0, 0, new JLabel(labelSaveGeometryAsBlocksField), (JComponent)this.cbSaveGeometriesAsBlocks, true);
            FormUtils.addRowInGBL((JComponent)this.southComponent1, 1, 0, new JLabel(labelWriteFeaturesFieldAsXData), (JComponent)this.cbWriteFeatureAttributesAsXData, true);
            FormUtils.addRowInGBL((JComponent)this.southComponent1, 2, 0, new JLabel(labelWritePointFcsAsInsertsWithAttribs), (JComponent)this.cbWritePointFcsAsInsertsWithAttrs, true);
            FormUtils.addRowInGBL((JComponent)this.southComponent1, 3, 0, new JLabel(labelVersionField), (JComponent)this.selectDXFVersionComboBox, false);
            FormUtils.addRowInGBL((JComponent)this.southComponent1, 4, 0, charsetSelection, (JComponent)this.selectCharsetCombobox, false);
            FormUtils.addFiller(this.southComponent1, 5, 0);
            this.southComponent1Initialized = true;
        }
        return this.southComponent1;
    }
}

