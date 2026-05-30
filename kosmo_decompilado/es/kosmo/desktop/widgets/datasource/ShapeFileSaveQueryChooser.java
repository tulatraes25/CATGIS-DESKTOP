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

public class ShapeFileSaveQueryChooser
extends SaveFileDataSourceQueryChooser {
    private static final Logger LOGGER = Logger.getLogger(ShapeFileSaveQueryChooser.class);
    public static final String SAVE_PRIMARY_KEY_OPTION_KEY = String.valueOf(ShapeFileSaveQueryChooser.class.getName()) + " - SAVE PRIMARY KEY";
    public static final Boolean DEFAULT_SAVE_PRIMARY_KEY_VALUE = false;
    protected boolean southComponent1Initialized = false;
    protected JCheckBox cbSavePk;
    protected JComboBox selectCharsetCombobox;

    public ShapeFileSaveQueryChooser(Class<?> dataSourceClass, String description, String[] extensions, WorkbenchContext context) {
        super(dataSourceClass, description, extensions, context);
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        PersistentBlackboardPlugIn.get(this.context).put(SAVE_PRIMARY_KEY_OPTION_KEY, this.cbSavePk.isSelected());
        PersistentBlackboardPlugIn.get(this.context).put(ShapeFileLoadQueryChooser.CHARSET_ENCODING_NAME_KEY, this.selectCharsetCombobox.getSelectedItem());
        return super.getDataSourceQueries();
    }

    @Override
    protected Component getSouthComponent1() {
        if (!this.southComponent1Initialized) {
            Charset systemCharset;
            this.southComponent1 = new JPanel(new GridBagLayout());
            SortedMap<String, Charset> charsets = Charset.availableCharsets();
            Vector charsetVector = new Vector(charsets.keySet());
            JLabel charsetSelection = new JLabel(String.valueOf(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Coding")) + ":");
            this.selectCharsetCombobox = new JComboBox(charsetVector);
            String lastCharsetEncoding = (String)PersistentBlackboardPlugIn.get(this.context).get(ShapeFileLoadQueryChooser.CHARSET_ENCODING_NAME_KEY, ShapeFileDataSource.DEFAULT_STRING_CHARSET.name());
            try {
                systemCharset = Charset.forName(lastCharsetEncoding);
            }
            catch (UnsupportedCharsetException e) {
                LOGGER.error((Object)"", (Throwable)e);
                systemCharset = ShapeFileDataSource.DEFAULT_STRING_CHARSET;
            }
            this.selectCharsetCombobox.setSelectedItem(systemCharset.name());
            String labelOptions = I18N.getString("com.vividsolutions.jump.workbench.datasource.SaveShapeFileDataSourceQueryChooser.options");
            String labelSaveKeyField = String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.datasource.SaveShapeFileDataSourceQueryChooser.save-key-field")) + ":";
            this.southComponent1.setBorder(BorderFactory.createTitledBorder(labelOptions));
            this.cbSavePk = new JCheckBox();
            FormUtils.addRowInGBL((JComponent)this.southComponent1, 0, 0, new JLabel(labelSaveKeyField), (JComponent)this.cbSavePk, true);
            FormUtils.addRowInGBL((JComponent)this.southComponent1, 1, 0, charsetSelection, (JComponent)this.selectCharsetCombobox, false);
            this.southComponent1Initialized = true;
        }
        return this.southComponent1;
    }
}

