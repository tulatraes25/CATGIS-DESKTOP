/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.OptionsPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.datasource.IndexedShapeFilePlugIn;
import org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser;

public class DatasourceOptionsPanel
extends OptionsPanel {
    private static final long serialVersionUID = 1L;
    public static final String NAME = I18N.getString("es.kosmo.desktop.widgets.config.DatasourceOptionsPanel.Data-sources");
    private Blackboard blackboard;
    private JPanel shapefileDatasourcePanel;
    private JCheckBox optimizeShapefileMemoryResourcesCheckBox;

    public DatasourceOptionsPanel(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.setLayout(new GridBagLayout());
        this.shapefileDatasourcePanel = new JPanel(new GridBagLayout());
        this.shapefileDatasourcePanel.setBorder(BorderFactory.createTitledBorder(IndexedShapeFilePlugIn.NAME));
        this.optimizeShapefileMemoryResourcesCheckBox = new JCheckBox(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Optimize-memory-resources"));
        this.optimizeShapefileMemoryResourcesCheckBox.setToolTipText(I18N.getString("org.saig.jump.widgets.datasource.ShapeFileLoadQueryChooser.Bost-memory-consumption-reduction-for-lower-resource-computers-in-exchange-for-reducing-layer-rendering-speed"));
        FormUtils.addRowInGBL(this.shapefileDatasourcePanel, 0, 0, this.optimizeShapefileMemoryResourcesCheckBox);
        FormUtils.addRowInGBL(this, 0, 0, this.shapefileDatasourcePanel);
        FormUtils.addFiller(this, 1, 0);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public void init() {
        boolean optimizeShapeFileMemoryResourcesSelected = PersistentBlackboardPlugIn.get(this.blackboard).get(ShapeFileLoadQueryChooser.OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES_KEY, false);
        this.optimizeShapefileMemoryResourcesCheckBox.setSelected(optimizeShapeFileMemoryResourcesSelected);
    }

    @Override
    public String validateInput() {
        return null;
    }

    @Override
    public void okPressed() {
        PersistentBlackboardPlugIn.get(this.blackboard).put(ShapeFileLoadQueryChooser.OPTIMIZE_SHAPEFILE_MEMORY_RESOURCES_KEY, this.optimizeShapefileMemoryResourcesCheckBox.isSelected());
    }
}

